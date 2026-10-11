package com.sertinty.pieplus;

/**
 * 多位数累积器。纯逻辑，不依赖任何 Minecraft 类，可以直接用普通 JVM 跑单测。
 *
 * <p>只负责「把按下的数字拼成一个数」，不做任何超时或猜测 ——
 * 是否需要多位数由调用方（是否按住了修饰键）决定，所以单个数字永远是瞬时的。
 */
public final class PieDigitBuffer {

    /** 表示当前没有缓冲。 */
    public static final int EMPTY = -1;

    private int value = EMPTY;

    /**
     * 追加一位数字。
     *
     * @param digit 0-9
     * @param max   当前层级可选条目的最大序号
     * @return true 表示这一位被接受；false 表示拼出来的数已经超出范围，缓冲被清空
     */
    public boolean append(int digit, int max) {
        if (max <= 0) {
            value = EMPTY;
            return false;
        }
        int next = (value == EMPTY ? 0 : value) * 10 + digit;
        if (next > max) {
            value = EMPTY;
            return false;
        }
        value = next;
        return true;
    }

    /** 取出并清空。没有缓冲时返回 {@link #EMPTY}。 */
    public int take() {
        int result = value;
        value = EMPTY;
        return result;
    }

    /** 当前缓冲的数字，{@link #EMPTY} 表示没有。 */
    public int value() {
        return value;
    }

    public void reset() {
        value = EMPTY;
    }
}
