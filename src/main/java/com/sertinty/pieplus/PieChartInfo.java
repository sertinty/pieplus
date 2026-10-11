package com.sertinty.pieplus;

/**
 * 由 {@code PieChartMixin} 织入 {@code PieChart}，把渲染时用到的私有信息暴露出来。
 *
 * <p>为什么不复制一份布局公式：饼图的列表内容随层级变化，直接问它当前有几行最可靠。
 */
public interface PieChartInfo {

    /** 当前层级下可选的条目数量（不含代表当前层级自身的那一行）。 */
    int pieplus$lineCount();

    /** 饼图面板距屏幕底部的留白，用来复现面板坐标。 */
    int pieplus$bottomMargin();
}
