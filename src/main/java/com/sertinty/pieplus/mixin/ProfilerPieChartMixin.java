package com.sertinty.pieplus.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.sertinty.pieplus.PieChartInfo;
import com.sertinty.pieplus.PieDigitBuffer;
import com.sertinty.pieplus.PieNumberInput;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.debugchart.ProfilerPieChart;
import net.minecraft.util.profiling.ProfileResults;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 饼图注入点。
 *
 * <p>原版的选号入口是 {@code profilerPieChartKeyPress(int)}，它由
 * {@code KeyboardHandler} 的一个 lambda 调用。**不能去重定向那个 lambda**：
 * lambda 的合成方法名在开发与生产环境并不一致（1.21.11 上已经栽过一次）。
 * 这里改为包装**被调用方**本身，目标是一个有稳定映射的公开方法。
 */
@Mixin(ProfilerPieChart.class)
public class ProfilerPieChartMixin implements PieChartInfo {

    @Shadow
    @Final
    private Font font;

    @Shadow
    private @Nullable ProfileResults profilerPieChartResults;

    @Shadow
    private String profilerTreePath;

    @Shadow
    private int bottomOffset;

    @Override
    public int pieplus$lineCount() {
        if (this.profilerPieChartResults == null) {
            return 0;
        }
        // 原版渲染时会 removeFirst()，第一行是当前层级自身，不算可选条目
        return Math.max(0, this.profilerPieChartResults.getTimes(this.profilerTreePath).size() - 1);
    }

    @WrapMethod(method = "profilerPieChartKeyPress")
    private void pieplus$keyPress(int key, Operation<Void> original) {
        int decision = PieNumberInput.press((ProfilerPieChart) (Object) this, key);
        if (decision != PieDigitBuffer.EMPTY) {
            original.call(decision);
        }
    }

    /** 在面板上方显示正在拼的数字，例如 {@code [12_]}。 */
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void pieplus$drawPendingDigits(GuiGraphicsExtractor graphics, int scaledScreenWidth,
                                           int scaledScreenHeight, CallbackInfo ci) {
        int pending = PieNumberInput.pending();
        if (pending < 0 || this.profilerPieChartResults == null) {
            return;
        }

        // 复现 extractRenderState 里的面板坐标
        int chartCenterX = scaledScreenWidth - 130 - 10;
        int left = chartCenterX - 130;
        int textUnderChartHeight = pieplus$lineCount() * 9;
        int bottom = scaledScreenHeight - this.bottomOffset - 5;
        int textStartY = bottom - textUnderChartHeight;
        int chartCenterY = textStartY - 62 - 5;
        int panelTop = chartCenterY - 62 - 5;

        String text = "\u00a7e[" + pending + "_\u00a7e] \u00a77\u677e\u5f00 Shift \u8fdb\u5165";
        graphics.text(this.font, text, left, Math.max(2, panelTop - 11), -1);
    }
}
