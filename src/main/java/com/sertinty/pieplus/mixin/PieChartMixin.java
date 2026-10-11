package com.sertinty.pieplus.mixin;

import com.sertinty.pieplus.PieChartInfo;
import com.sertinty.pieplus.PieNumberInput;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.debug.chart.PieChart;
import net.minecraft.util.profiler.ProfileResult;
import net.minecraft.util.profiler.ProfilerTiming;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * 把饼图的私有状态暴露给 {@link PieNumberInput}，并在面板上方画出正在缓冲的数字。
 */
@Mixin(PieChart.class)
public class PieChartMixin implements PieChartInfo {

    @Shadow
    @Final
    private TextRenderer textRenderer;

    @Shadow
    private @Nullable ProfileResult profileResult;

    @Shadow
    private String currentPath;

    @Shadow
    private int bottomMargin;

    @Override
    public int pieplus$lineCount() {
        if (this.profileResult == null) {
            return 0;
        }
        List<ProfilerTiming> timings = this.profileResult.getTimings(this.currentPath);
        // render() 会 removeFirst()，第一行是当前层级自身，不算可选条目
        return Math.max(0, timings.size() - 1);
    }

    @Override
    public int pieplus$bottomMargin() {
        return this.bottomMargin;
    }

    /**
     * 在面板上方显示正在拼的数字，例如 {@code [12_]}。
     * 没有这个提示的话，按住 Shift 输入时看不到自己拼到了哪一位。
     */
    @Inject(method = "render", at = @At("TAIL"))
    private void pieplus$drawPendingDigits(DrawContext context, CallbackInfo ci) {
        int pending = PieNumberInput.pending();
        if (pending < 0 || this.profileResult == null) {
            return;
        }

        // 复现 render() 里的面板坐标
        int width = context.getScaledWindowWidth();
        int height = context.getScaledWindowHeight();
        int right = width - 105 - 10;
        int left = right - 105;
        int lines = pieplus$lineCount() * 9;
        int listBottom = height - this.bottomMargin - 5;
        int listTop = listBottom - lines;
        int chartTop = listTop - 62 - 5;
        int panelTop = chartTop - 62 - 5;

        String text = "\u00a7e[" + pending + "_\u00a7e] \u00a77\u677e\u5f00 Shift \u8fdb\u5165";
        int y = Math.max(2, panelTop - 11);
        context.drawTextWithShadow(this.textRenderer, text, left, y, 0xFFFFFFFF);
    }
}
