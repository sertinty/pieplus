package com.sertinty.pieplus;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.debug.chart.PieChart;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.Window;
import org.lwjgl.glfw.GLFW;

/**
 * 多位数选择的 Minecraft 侧胶水。
 *
 * <p>原版 {@code Keyboard.onKey} 里是 {@code select(input.asNumber())}，
 * 而 {@code asNumber()} 一次只认一个按键（返回 0-9），所以饼图里编号 10 及以后的切片
 * 永远选不中。
 *
 * <p>修复后的手感：
 *
 * <ul>
 *   <li><b>直接按数字</b> → 立刻选中，和原版完全一样，<b>没有任何延迟</b>；</li>
 *   <li><b>按住 Shift 再按数字</b> → 把数字拼成多位数，<b>松开 Shift 生效</b>。</li>
 * </ul>
 *
 * <p>用 Shift 是因为原版那段选号逻辑的守卫条件是 {@code !debugModifierKey.isPressed()}
 * （也就是「没按住 F3」），Shift 本身没有被占用。
 */
public final class PieNumberInput {

    private static final PieDigitBuffer BUFFER = new PieDigitBuffer();
    private static PieChart owner;

    private PieNumberInput() {
    }

    /** 由 {@code KeyboardMixin} 接管原版的 {@code pieChart.select(index)} 调用。 */
    public static void onDigit(PieChart chart, int digit) {
        if (!isShiftDown()) {
            // 没有按 Shift：完全走原版语义，0 就是「返回上一级」
            BUFFER.reset();
            owner = null;
            chart.select(digit);
            return;
        }

        // 按住 Shift：拼多位数，超出范围的位直接丢弃
        int max = lineCount(chart);
        if (BUFFER.append(digit, max)) {
            owner = chart;
        }
    }

    /** 每 tick 调用：Shift 一松开就提交缓冲；饼图关掉时清空。 */
    public static void tick(MinecraftClient client) {
        if (client.getDebugHud() == null || !client.getDebugHud().shouldShowRenderingChart()) {
            reset();
            return;
        }
        if (BUFFER.value() != PieDigitBuffer.EMPTY && !isShiftDown()) {
            commit();
        }
    }

    public static void reset() {
        BUFFER.reset();
        owner = null;
    }

    /** 正在拼的数字，{@link PieDigitBuffer#EMPTY} 表示没有。仅用于界面提示。 */
    public static int pending() {
        return BUFFER.value();
    }

    private static void commit() {
        int value = BUFFER.take();
        PieChart chart = owner;
        owner = null;
        if (value != PieDigitBuffer.EMPTY && chart != null) {
            chart.select(value);
        }
    }

    private static int lineCount(PieChart chart) {
        if (chart instanceof PieChartInfo info) {
            return info.pieplus$lineCount();
        }
        return 0;
    }

    private static boolean isShiftDown() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return false;
        }
        Window window = client.getWindow();
        return InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_LEFT_SHIFT)
                || InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_RIGHT_SHIFT);
    }
}
