package com.sertinty.pieplus;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debugchart.ProfilerPieChart;

import java.lang.reflect.Method;

/**
 * 多位数选择的客户端逻辑。
 *
 * <p>原版 {@code KeyboardHandler} 把一次按键直接转成一个 0-9 的数字交给
 * {@code ProfilerPieChart.profilerPieChartKeyPress(int)}，所以饼图里编号 10 及以后的切片
 * 永远选不中。
 *
 * <p>修复后的手感：
 *
 * <ul>
 *   <li><b>直接按数字</b> → 立刻生效，和原版完全一样，<b>没有任何延迟</b>；</li>
 *   <li><b>按住 Shift 再按数字</b> → 拼成多位数，<b>松开 Shift 生效</b>。</li>
 * </ul>
 *
 * <p>用 Shift 是因为原版选号那段逻辑的守卫条件是「没按住 F3」，Shift 本身没有被占用。
 */
public final class PieNumberInput {

    private static final PieDigitBuffer BUFFER = new PieDigitBuffer();
    private static ProfilerPieChart owner;

    /**
     * 26.1 的 {@code InputConstants.isKeyDown(Window, int)} 在 26.3 变成了
     * {@code isKeyDown(int)}。为了同一份源码能覆盖整个 26.x，这里解析一次方法签名再调用，
     * 这样源码里不出现任何随版本变化的符号。
     */
    private static final Method IS_KEY_DOWN = resolveIsKeyDown();

    private PieNumberInput() {
    }

    /**
     * 由 {@code ProfilerPieChartMixin} 的 {@code @WrapMethod} 调用。
     *
     * @return 要原样交给原版处理的键值；{@link PieDigitBuffer#EMPTY} 表示本次按键已被本模组吸收
     */
    public static int press(ProfilerPieChart chart, int digit) {
        if (!isShiftDown()) {
            // 没有按 Shift：完全走原版语义（0 = 返回上一级）
            BUFFER.reset();
            owner = null;
            return digit;
        }

        // 按住 Shift：拼多位数，超出范围的位直接丢弃
        int max = lineCount(chart);
        if (BUFFER.append(digit, max)) {
            owner = chart;
        }
        return PieDigitBuffer.EMPTY;
    }

    /** 每 tick 调用：Shift 一松开就提交缓冲。 */
    public static void tick() {
        if (BUFFER.value() == PieDigitBuffer.EMPTY || isShiftDown()) {
            return;
        }
        int value = BUFFER.take();
        ProfilerPieChart chart = owner;
        owner = null;
        if (value != PieDigitBuffer.EMPTY && chart != null) {
            // 这里会再走一遍 @WrapMethod，但此时没按 Shift，会直接透传给原版
            chart.profilerPieChartKeyPress(value);
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

    private static int lineCount(ProfilerPieChart chart) {
        if (chart instanceof PieChartInfo info) {
            return info.pieplus$lineCount();
        }
        return 0;
    }

    private static Method resolveIsKeyDown() {
        for (Method method : InputConstants.class.getMethods()) {
            if (!"isKeyDown".equals(method.getName())) {
                continue;
            }
            Class<?>[] params = method.getParameterTypes();
            if (params.length == 1 && params[0] == int.class) {
                return method;
            }
            if (params.length == 2 && params[1] == int.class) {
                return method;
            }
        }
        return null;
    }

    private static boolean isShiftDown() {
        Method method = IS_KEY_DOWN;
        if (method == null) {
            return false;
        }
        try {
            Object[] args = method.getParameterCount() == 1
                    ? new Object[]{InputConstants.KEY_LSHIFT}
                    : new Object[]{Minecraft.getInstance().getWindow(), InputConstants.KEY_LSHIFT};
            if (Boolean.TRUE.equals(method.invoke(null, args))) {
                return true;
            }
            args[args.length - 1] = InputConstants.KEY_RSHIFT;
            return Boolean.TRUE.equals(method.invoke(null, args));
        } catch (Throwable ignored) {
            return false;
        }
    }
}
