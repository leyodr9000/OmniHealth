# OmniHealth UI 重构方案（v2）

> 触发背景：v0.7.0 真机体验暴露四类问题——审美偏「AI 生成感」、MIUIX/MD3 只是换色而非换框架、设置页结构混乱、布局 C 底栏遮挡内容，以及滚动/切页不够跟手。本文给出定位、方案与验收标准。

---

## 1. 对标国内同类 App 的取向

参考对象：**Keep、薄荷健康、华为运动健康、小米运动健康、体重小本、咕咚**。共性做法：

| 维度 | 国内主流做法 | 我们当前的问题 |
|---|---|---|
| 卡片 | 实心 surface（白/浅灰）+ 1px 描边或 2–4dp 投影 | 半透明玻璃叠加网格渐变，层次糊 |
| 强调色 | 全屏只有 1 处强调（主 CTA / 选中态） | 渐变按钮、渐变标题、渐变 chip 到处都是 |
| 色彩 | 单色系深浅（同色相 2–3 级） | 蓝紫粉三色渐变，饱和度偏高 |
| 数字排版 | 大号等宽数字 + 小号单位，右对齐或居中 | 数字与单位混排，字距不统一 |
| 文案 | 陈述事实 + 一个动作，≤12 字 | 「一秒入账，健康数据不求人」这类营销腔 + emoji |
| 首页结构 | 问候 → 主指标 → 今日任务 → 数据网格 → 建议 | 结构接近，但卡片噪音多 |
| 设置 | 独立页 + 分组 + 二级页 | 外观实验室独占整页，资料/接口混排 |
| 底栏 | 4–5 个纯图标 + 小字，固定贴底 | 布局 C 用悬浮胶囊，遮挡列表 |

**结论**：不是"再调一版配色"，而是要把「装饰驱动」改成「信息驱动」。

---

## 2. MIUIX / MD3 是两套框架，不是两套配色

参考 **KernelSU 管理器**：它直接使用 `top.yukonga.miuix.kmp`（Miuix Compose 组件库），页面骨架、按钮、开关、列表项、导航栏都来自该库；而 Material3 版本则用 M3 组件。**同一份业务 UI，切换的是组件实现，不是颜色。**

### 2.1 依赖

`top.yukonga.miuix.kmp:miuix:0.8.8`（Maven Central 已确认存在，Android artifact 可用）。

### 2.2 抽象层

新增 `ui/kit/`：

```kotlin
interface UiKit {
    @Composable fun Screen(title: String?, onBack: (() -> Unit)?, content: @Composable ColumnScope.() -> Unit)
    @Composable fun Card(modifier: Modifier, onClick: (() -> Unit)?, content: @Composable ColumnScope.() -> Unit)
    @Composable fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier)
    @Composable fun TextButton(text: String, onClick: () -> Unit)
    @Composable fun SettingRow(title: String, summary: String?, onClick: (() -> Unit)?, trailing: @Composable (() -> Unit)?)
    @Composable fun SwitchRow(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit)
    @Composable fun Segmented(options: List<String>, selected: Int, onSelect: (Int) -> Unit)
    @Composable fun NumberField(label: String, value: String, onValueChange: (String) -> Unit)
}

object MiuixKit : UiKit   // 使用 top.yukonga.miuix.kmp.* 实现
object Md3Kit : UiKit     // 使用 androidx.compose.material3.* 实现

val LocalUiKit = staticCompositionLocalOf<UiKit> { MiuixKit }
```

主题枚举拆成两层：

```kotlin
enum class UiFramework { MIUIX, MATERIAL3 }   // 决定组件实现 + 形状/动效语言
enum class Palette { MIUIX_GLASS, MORANDI, CYBER }  // 决定颜色
```

`ThemeMode` 保留为组合（`Framework × Palette`）以兼容已存设置：`miuix_glass → MIUIX+GLASS`，`md3_morandi → MATERIAL3+MORANDI`，`cyber_dark → MATERIAL3+CYBER`（暗色）。

### 2.3 迁移顺序

1. 抽 `UiKit` 接口 + `Md3Kit`（用现有 M3 代码填充，行为不变，可立即编译）
2. 引入 miuix 依赖，实现 `MiuixKit`
3. 逐屏替换：设置 → 首页卡片 → 饮食 → 统计 → AI
4. 删除 `ui/components/` 里与框架强耦合的组件，只保留图表与业务组件

**风险**：miuix 0.8.8 依赖 Compose 运行时版本需与 BOM 2024.10.01 对齐；若冲突，退化为「miuix 组件 + 固定 compose 版本」并锁死。备选：仅引入 miuix 的 Card/Button/NavBar 三个组件做渐进式替换。

---

## 3. 去掉「AI 味」

### 3.1 视觉

- **移除全局渐变**：主按钮改纯 `accent` 实色；标题不再用渐变；chip 选中态用 `accent.copy(alpha = 0.12f)` 底 + `accent` 字
- **背景**：渐变网格从 3 团高饱和色斑改为 2 团低饱和（透明度 ≤ 12%），并**默认静态**（性能 + 克制）
- **玻璃**：只保留底栏与顶部栏的毛玻璃，卡片一律实心 `surface`；玻璃用 haze 真模糊，不再用多层半透明白
- **圆角**：统一 16 / 12 / 8 三档，取消 24dp 超大圆角
- **阴影**：只允许 `Card` 的 2dp 环境阴影，禁止彩色阴影
- **数字**：`fontFeatureSettings = "tnum"`，单位 10sp 三级色

### 3.2 文案

- 删除所有装饰性 emoji（保留习惯 emoji 与图表无）
- 标题与描述改为事实陈述：`今日摄入 1,486 kcal · 剩余 514`
- 空状态给行动指令：`还没有记录，点右下角开始`
- AI 卡片标题从「✨ AI 健康顾问 · 生成 7 日报告 →」改为「本周建议」+ 具体结论
- 删除「一秒入账，健康数据不求人」「保持节奏」等口号

---

## 4. 设置独立成页 + 分类

底栏最后一项「我的」进入 **Settings 首页**，只放分组入口：

```
账号       头像 · 昵称 · 目标体重
资料       性别/年龄/身高/体重/活动量      → 二级页
目标       步数 / 饮水 / 睡眠 / 热量       → 二级页
提醒       饮水提醒 / 睡眠提醒 / 通知权限  → 二级页
外观       框架(MIUIX/MD3) · 配色 · 语言   → 二级页（现有外观实验室）
AI 接口    Provider / BaseURL / Key / 模型 → 二级页
数据       导出 CSV / 清空数据 / 版本信息
关于       开源许可 / 隐私说明
```

「外观实验室」从整页降级为二级页的一个分组；主题/布局/语言选择器沿用现有卡片但改成列表行 + 右侧当前值。

---

## 5. 布局 C 底栏遮挡

**原因**：布局 C 是悬浮胶囊（280dp 宽、64dp 高、距底部 10dp），Scaffold 的 bottomBar 高度虽计入 `innerPadding`，但代码里又减了 8dp，且列表自身 `contentPadding.bottom` 只有 28dp，在窄屏 / 大字体 / 手势导航条机型上叠加后不足。

**方案**：
- 新增 `LocalBottomBarInset`，由各布局返回精确高度：A/B = bar 高度 + 导航栏 inset；C = 64 + 上下 padding + inset + 12dp 呼吸位
- 所有屏幕的 `LazyColumn.contentPadding.bottom` 改用该值，不再各写 28dp
- 移除 `innerPadding - 8.dp` 的魔数
- 增加 UI 测试断言：最后一项与底栏无重叠（`assertIsDisplayed` + 截图像素差）

---

## 6. 流畅度优化清单

| 项 | 现状 | 改法 | 预期 |
|---|---|---|---|
| 网格背景动画 | 全屏 Canvas 每帧重绘 3 个巨大径向渐变 | 默认静态；可选 8fps 相位更新 | 消除持续 GPU 填充开销 |
| 毛玻璃 | 多层半透明 + border 叠加 | haze 真模糊，仅底栏/顶栏 | 合成层数 -60% |
| 列表卡片 | 每项 3 层 draw（gradient+border+bg） | 合并单层 `drawBehind` | 列表滚动掉帧减少 |
| `waterToday()` | 拉 200 条按日期内存过滤 | 新增范围聚合查询 | 首次进入首页查询 <5ms |
| 首页 Flow | 5 路 combine，任一变化全量重算 | 拆 `distinctUntilChanged` + 派生 StateFlow | 减少重组 |
| 图表 | 每次重组重建 DataSet | `remember(data)` 缓存 + `update` 只换 data | 切换标签不掉帧 |
| 基线 | 无 | M9 加 Baseline Profile + `R8 full mode` | 冷启动 -30% |
| 重组 | 大 Composable 读多个 CompositionLocal | 参数下沉、`@Immutable` 数据类 | 重组范围收敛 |

验收：`Macrobenchmark` 记录 `frameTimingMetric`，目标 P90 ≤ 16ms、`frameOverrunMs` ≤ 8ms；冷启动 ≤ 1.2s（Pixel 6 级别）。

---

## 7. 执行顺序（建议）

| 阶段 | 内容 | 产出 |
|---|---|---|
| P0（1 天） | 底栏 inset 修复、网格背景静态化、查询优化、文案去 emoji | v0.8.1 热修 |
| P1（2 天） | 设置独立页 + 分类 + 二级页 | v0.8.2 |
| P2（3 天） | 去渐变 / 实心卡片 / 数字排版 / 文案重写 | v0.9.0 |
| P3（3 天） | `UiKit` 抽象 + miuix 接入 + 全站替换 | v1.0.0-rc1 |
| P4（1 天） | 性能回归 + Baseline Profile + 发布 | v1.0.0 |

> P3 是唯一有技术风险的阶段（第三方库版本冲突），单独成分支 `feat/uikit`，失败可整体回滚，不影响 P0–P2。
