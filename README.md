# PiePlus 饼图多位数选择

Minecraft **1.21.11** / Fabric 的**纯客户端**模组。

修复 F3 调试饼图（profiler pie chart）里**编号大于 9 的切片无法选中**的问题。

---

## 安装

1. 装好 Fabric Loader（**≥ 0.19.3**）
2. 到 [Releases](https://github.com/sertinty/pieplus/releases) 下载 `pieplus-1.0.0.jar`
3. 把 jar 放进 `.minecraft/mods/`

**不需要 Fabric API。**

---

## 使用方法

打开 F3 调试界面、切到饼图，然后按下面的方式选择切片：

| 操作 | 结果 |
| --- | --- |
| 直接按 `1` ~ `9` | 立刻进入对应编号的切片 |
| 直接按 `0` | 返回上一级 |
| 按住 `Shift` → 依次按数字 → 松开 `Shift` | 进入多位编号的切片 |

### 例子

| 想进入 | 怎么按 |
| --- | --- |
| [9] | 直接按 `9` |
| [12] | 按住 `Shift` → 按 `1` → 按 `2` → 松开 `Shift` |
| [25] | 按住 `Shift` → 按 `2` → 按 `5` → 松开 `Shift` |
| 返回上一级 | 直接按 `0` |

按住 `Shift` 拼数字时，饼图面板上方会显示当前拼到的数字：

```
[12_] 松开 Shift 进入
```

松开 `Shift` 就会进入该编号。

如果拼出的数字超过当前层的条目数，这一位会被忽略，不会跳错地方。

---

## 环境要求

| 组件 | 版本 |
| --- | --- |
| Minecraft | 1.21.11 |
| Fabric Loader | ≥ 0.19.3 |
| Fabric API | 不需要 |
| Java | 21 |

---

## 自己编译

```bash
JAVA_HOME=/path/to/jdk-21 ./gradlew build
# 产物：build/libs/pieplus-1.0.0.jar
```

## License

MIT
