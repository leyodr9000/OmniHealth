# OmniHealth 项目总结报告

- **仓库**：https://github.com/leyodr9000/OmniHealth （public）
- **周期**：2026-10-19 单日完成 M0 → M9
- **代码**：29 次提交 / 74 个文件 / 48 个 Kotlin 文件 / 7,876 行
- **发布**：27 个 Release，最新 **v1.2.2**
- **安装包**：1.96 MB（release，R8 + 资源压缩）

---

## 1. 交付物

| 项 | 说明 |
|---|---|
| 正式包 | `OmniHealth-release.apk`，签名（keystore 未入库） |
| 源码 | `main` 分支，每完成一个里程碑即提交推送 |
| 文档 | README、UI 重构方案（docs/UI_REDESIGN_PLAN.md）、本报告 |
| 构建 | `./gradlew :app:assembleRelease`，JDK 17 + SDK 35 |

## 2. 技术栈

Kotlin 2.0.21 · AGP 8.7.3 · Compose BOM 2024.10.01 · Room 2.6.1(KSP) · DataStore 1.1.1 ·
WorkManager 2.9.1 · Retrofit 2.11 + OkHttp 4.12 + kotlinx.serialization 1.7.3 ·
MPAndroidChart 3.1.0(JitPack) · haze 1.5.4 · core-splashscreen 1.0.1 · profileinstaller 1.4.0

最低 API 26，目标 API 35。

## 3. 里程碑交付

| 里程碑 | 内容 | 结果 |
|---|---|---|
| M0 | 工程骨架、三套配色、三套导航、i18n 引擎 | ✅ |
| M1 | Room 8 表 + 5 DAO + **211 条**食物字典 + DataStore + Repository | ✅ |
| M2 | BMI/BMR(Mifflin-St Jeor)/TDEE/宏量/5D 评分 + 步数传感器 + 单测 | ✅ |
| M3 | Bento 卡片、进度环、快捷记录弹窗、弹性按压 | ✅ |
| M4 | 首页真实数据、5 路 Flow 聚合、设置持久化 | ✅ |
| M5 | 食物搜索/克数/三餐记录、步数模拟、训练计时 | ✅ |
| M6 | MPAndroidChart 五类图 + 统一样式器 | ✅ |
| M7 | AI 顾问：OpenAI 兼容客户端 + **离线规则引擎** | ✅ |
| M8 | 设置分组二级页、资料/目标/AI 编辑、习惯打卡、WorkManager 提醒 | ✅ |
| M9 | 签名包、R8、启动图、CSV 导出、关于页、冷启动优化 | ✅ |

## 4. 功能清单

- **首页**：5 维加权健康分、今日四指标、习惯打卡、基于真实数据的一行结论
- **饮食**：211 条食物（中英名/热量/蛋白/碳水/脂肪/纤维/参考份量）、搜索、克数选择、三餐分类、当日宏量
- **运动**：`TYPE_STEP_COUNTER` 计步（跨天基线重置）、模拟器补步、训练计时器
- **统计**：折线 / 柱+线组合 / 饼图 / 柱状（目标线）/ 雷达
- **AI**：7 日报告 + 对话；无 Key 时本地规则引擎（6 类意图），断网不降级体验
- **提醒**：饮水每 2 小时、睡前 22:30，本地通知，权限流完整
- **导出**：全量 CSV（体征/饮食/步数/训练/习惯）
- **外观**：默认 Material 3；可切 MIUI X（液态玻璃卡片 + 底栏）、三套配色、三套导航、中英实时切换、减少动效开关

## 5. 架构

```
data/    local(Room) · datastore · remote(Retrofit+离线引擎) · repository · work · export
domain/  util(HealthCalculator/StepSensorManager/TimeUtils) · model(HealthContext)
ui/      theme · chart · navigation · components · screens(5)
```

- MVVM：每页一个 `ViewModel` + `StateFlow`，ViewModel 挂在 **Activity 作用域**（切 Tab 不重建、不重查数据库）
- 计算纯函数化，`HealthCalculator` 无 Android 依赖，可单测

## 6. 性能工程（全部为实测数据）

| 指标 | 优化前 | 优化后 | 手段 |
|---|---|---|---|
| 安装包 | 18.87 MB | **1.96 MB** | 签名 release + R8 + `shrinkResources` |
| 冷启动 | 1060 / 1037 / 658 ms | **332 / 373 / 331 / 300 / 337 ms** | WorkManager 改按需初始化（去掉启动 Provider） |
| 不可跳过 Composable | 未审计 | **0 个** | Compose 编译器报告 + `@Immutable` 状态类 |
| 列表滚动 | 每个 item 一个 graphicsLayer | 无图层 | `quietClick` 替代弹性缩放 |
| 背景绘制 | 3 个全屏径向渐变 | 单层线性渐变 | 删 Canvas 色斑 |
| 重复查询 | 饮水拉 200 行内存过滤 | SQL 范围聚合 | DAO 新增 `observeSumBetween` |

冷启动另含：baseline profile（AGP 自动打包 + profileinstaller 运行时安装）。

## 7. 真实 Bug 与根因

| 现象 | 根因 | 修复 |
|---|---|---|
| 卡片后透出方框 | `Modifier.shadow(clip = false)` 阴影图形在真机不裁切 | 全站移除阴影，改用描边 |
| 底部弹窗语言不跟随 | ModalBottomSheet 使用独立窗口上下文，`stringResource` 回落系统语言 | 弹窗内容内再包 `LocalizedApp` |
| 点饮食页直接崩溃 | 覆盖 `LocalContext` 后 `ActivityResultRegistryOwner` 断链 | 显式转发 owner |
| 切框架/语言跳回首页 | 主题/语言分支改变组件树结构，NavController 被重建 | 结构固定（MiuixTheme 常驻、语言分支合并） |
| 切 Tab 后滑动掉帧 | 转场动画抢首帧 + 每项 graphicsLayer + ViewModel 被销毁重查 6 表 | 瞬切 + 去图层 + Activity 级 VM |
| 布局大面积错乱（miuix） | 第三方组件尺寸契约与现有布局不一致（内边距/高度/阴影） | **整体回滚**，改为自建两套视觉 |
| 页面切换重影 | 垂直位移 + 淡入淡出导致两页半透明叠加 | 改整屏水平翻页（两页并排不叠加） |

## 8. 验证方式

- **编译**：每次改动 `assembleRelease` 通过才提交
- **单元测试**：14 个（健康计算 9 + AI 引擎 5）全绿
- **模拟器实测**：安装 → 启动 → 各页切换 → 数据写入/读取 → 导出文件 → WorkManager 任务入队，`FATAL` 计数 0
- **数据链路**：快捷记录 250 ml → 首页 0.25 L；添加食物 120 g → 249 kcal；勾选习惯 → 一致性维度变化

## 9. 已知问题

1. **液态玻璃的观感依赖真机 GPU**：模拟器为软渲染，无法验证模糊质量与重影；需真机反馈。
2. **MIUI X 为自建视觉**，不是官方 miuix-kmp 组件库（该库要求 Compose 1.12 + Kotlin 2.4，升级风险过大）。
3. **无真机帧率数据**：`gfxinfo` 在无头模拟器无效；需设备连着做 `framestats` 分析。
4. **导出文件在应用私有目录**（`Android/data/<pkg>/files/exports`），未接系统分享面板。

## 10. 后续建议

1. 真机 `dumpsys gfxinfo framestats` 定位残余掉帧
2. 导出接 `FileProvider` + 系统分享
3. 自定义 Baseline Profile（macrobenchmark 模块）
4. 若坚持官方 miuix，需要一次 Compose 1.12 + Kotlin 2.4 的整体升级，建议单开分支评估
