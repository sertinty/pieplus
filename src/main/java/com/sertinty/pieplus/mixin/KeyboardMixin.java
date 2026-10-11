package com.sertinty.pieplus.mixin;

import com.sertinty.pieplus.PieNumberInput;
import net.minecraft.client.Keyboard;
import net.minecraft.client.gui.hud.debug.chart.PieChart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 接管原版的饼图选号调用。
 *
 * <p>原代码（{@code Keyboard.onKey}）：
 *
 * <pre>{@code
 * if (this.client.getDebugHud().shouldShowRenderingChart() && !bl2) {
 *     int i = input.asNumber();          // 单次按键 -> 只能 0..9
 *     if (i != -1) {
 *         this.client.getDebugHud().getPieChart().select(i);
 *     }
 * }
 * }</pre>
 *
 * <p>这里只把最后那句 {@code select(i)} 重定向到 {@link PieNumberInput}，
 * 外层的「饼图是否可见 / 是否按了修饰键 / 按键是不是数字」等判断全部保持原样。
 */
@Mixin(Keyboard.class)
public class KeyboardMixin {

    @Redirect(method = "onKey",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/hud/debug/chart/PieChart;select(I)V"))
    private void pieplus$selectMultipleDigits(PieChart chart, int index) {
        PieNumberInput.onDigit(chart, index);
    }
}
