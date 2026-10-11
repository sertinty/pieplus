package com.sertinty.pieplus;

/**
 * 由 {@code ProfilerPieChartMixin} 织入饼图类，把当前层级的条目数暴露出来。
 *
 * <p>饼图显示的行数随层级变化，直接问它当前有几行最可靠，不要自己复制布局公式。
 */
public interface PieChartInfo {

    /** 当前层级下可选的条目数量（不含代表当前层级自身的那一行）。 */
    int pieplus$lineCount();
}
