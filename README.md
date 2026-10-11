# PiePlus 饼图多位数选择

修复 Minecraft **1.21.11** 的一个原版 bug：**F3 调试饼图里编号大于 9 的切片永远选不中。**

- **纯客户端**，只依赖 Fabric Loader，**不需要 Fabric API**
- 不改按键绑定；**直接按数字的手感和原版一模一样，零延迟**

---

## Bug 是什么

`net.minecraft.client.Keyboard#onKey` 里：

```java
if (this.client.getDebugHud().shouldShowRenderingChart() && !bl2) {
    int i = input.asNumber();          // ← 只把「单个按键」转成 0..9
    if (i != -1) {
        this.client.getDebugHud().getPieChart().select(i);
    }
}
```

`AbstractInput#asNumber()` 的实现是：

```java
default int asNumber() {
    int i = this.getKeycode() - InputUtil.GLFW_KEY_0;
    return i >= 0 && i <= 9 ? i : -1;
}
```

一次只认一个按键。而 `PieChart.render()` 用 `"[" + (s + 1) + "]"` 给条目编号，可以编到
`[10]`、`[11]`…… 于是只要某一层超过 9 个子项，**第 10 个及以后的切片在界面上看得见、
按哪个键都进不去**。

---

## 修复后的手感

| 操作 | 结果 |
| --- | --- |
| 直接按 `9` | **立刻**进入 [9]（和原版完全一致，无延迟） |
| 直接按 `0` | 立刻返回上一级（原版语义） |
| 按住 `Shift`，按 `1` 再按 `2`，松开 `Shift` | 进入 [12] |
| 按住 `Shift`，按 `2` 再按 `5`，松开 `Shift` | 进入 [25] |
| 按住 `Shift` 拼出的数超过当前层条目数 | 该位被丢弃，不会误跳 |

拼数字期间，饼图面板上方会显示 `[12_] 松开 Shift 进入`。

### 为什么用 Shift

原版那段选号逻辑的守卫条件是 `!bl2`，而 `bl2 = gameOptions.debugModifierKey.isPressed()`
—— 也就是「没按住 **F3**」。所以 Shift / Ctrl 本身没有被这段逻辑占用，
而 `AbstractInput` 也原生提供了 `hasShift()`。

### 实现要点

只把最后那句 `select(i)` 重定向到自己的输入状态机，外层「饼图是否可见 / 是否按了 F3 /
按键是不是数字」的判断**全部保持原样**：

- 没按 Shift → 原样透传给 `select(digit)`，`0` 仍是返回上一级；
- 按了 Shift → 累积成多位数，由客户端 tick 检测到 Shift 松开时提交。

**没有任何超时、没有任何“猜你要选哪个”的逻辑**，所以单个数字永远是瞬时的。

---

## 安装

1. 装好 Fabric Loader（≥ 0.19.3），**不需要 Fabric API**
2. 把 `pieplus-1.0.0.jar` 丢进 `.minecraft/mods/`

---

## 验证情况

按你的要求**全程没有启动游戏**：

| 项目 | 结果 |
| --- | --- |
| `gradlew clean build` | BUILD SUCCESSFUL |
| 累积逻辑单测（纯 JVM，`PieDigitBuffer`） | **全部通过**，`failures=0`（35 项断言） |
| Mixin 目标重映射 | `Keyboard#onKey` → `method_1466`；`MinecraftClient#tick` → `method_1574`；`PieChart#render` → `method_61986` |
| `@Redirect` 目标重映射 | `PieChart#select` → `Lnet/minecraft/class_9931;method_61987(I)V` |

> 输入逻辑被刻意抽到不依赖 Minecraft 的 `PieDigitBuffer`，所以核心逻辑能用普通 JVM 覆盖。
> 游戏内的实际渲染与按键链路没有实机跑过 —— 需要的话我可以跑一次 `runClient`。

### 混入点

| Mixin | 目标 | 作用 |
| --- | --- | --- |
| `KeyboardMixin` | `Keyboard.onKey` 里的 `PieChart.select(I)V` 调用 | 接管选号 |
| `PieChartMixin` | `PieChart` | 暴露当前层级条目数；画出 `[12_]` 提示 |
| `MinecraftClientMixin` | `MinecraftClient.tick` | 检测 Shift 松开并提交、饼图关闭时清空 |

用 `MinecraftClientMixin` 而不是 Fabric API 的 `ClientTickEvents`，是为了让这个模组**零前置依赖**。

---

## 编译

| 组件 | 版本 |
| --- | --- |
| Minecraft | 1.21.11 |
| Fabric Loader | 0.19.3 |
| Yarn mappings | 1.21.11+build.6 |
| Fabric Loom | 1.17.20 |
| Gradle | 9.5.0 |
| Java | 21 |

```bash
JAVA_HOME=/path/to/jdk-21 ./gradlew build
# 产物：build/libs/pieplus-1.0.0.jar
```

> wrapper 的 `distributionUrl` 指向腾讯云 Gradle 镜像，因为 `services.gradle.org` 会 302 到 GitHub，
> 国内网络下会 `PKIX path building failed`。

## License

MIT
