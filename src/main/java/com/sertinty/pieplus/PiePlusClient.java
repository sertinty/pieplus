package com.sertinty.pieplus;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PiePlus —— 修复 F3 调试饼图只能选 0-9 号切片的问题。
 *
 * <p>纯客户端，只依赖 Fabric Loader，不需要 Fabric API。
 *
 * <p>逻辑入口都在混入里：{@code ProfilerPieChartMixin} 包装选号并把当前层级条目数暴露出来，
 * {@code KeyboardHandlerMixin} 驱动「Shift 松开就提交」。
 */
public class PiePlusClient implements ClientModInitializer {

    public static final String MOD_ID = "pieplus";
    public static final Logger LOGGER = LoggerFactory.getLogger("PiePlus");

    @Override
    public void onInitializeClient() {
        LOGGER.info("[PiePlus] 已加载：饼图序号超过 9 之后可以按住 Shift 接着输入下一位数字");
    }
}
