# OmniHealth

个人健康数据记录与分析应用。Kotlin + Jetpack Compose + Room，单模块 MVVM 架构，离线可用；AI 功能可选，未配置时由本地规则引擎兜底。

---

## 适用范围

| 项 | 说明 |
|---|---|
| 平台 | Android 8.0 及以上（minSdk 26），compileSdk / targetSdk 35 |
| 数据存储 | 全部保存在本机 Room 数据库，不上传云端 |
| 网络 | 仅在主动配置 API Key 后发起 AI 请求；其余功能完全离线 |
| 定位 | 个人自用记录工具，非医疗器械，不提供诊断或治疗建议 |

---

## 安装

预编译包位于仓库 Releases：

```
https://github.com/leyodr9000/OmniHealth/releases/latest/download/OmniHealth-release.apk
```

release 构建经 R8 混淆与资源压缩，并带 baseline profile；包含 arm64-v8a / armeabi-v7a / x86_64 ABI。自行构建见「构建」。

---

## 功能

| 模块 | 内容 |
|---|---|
| 首页 | 五维加权健康分、今日四项指标、习惯打卡、AI 结论（单句） |
| 饮食 | 253 条预置食物字典（中英文名、热量、宏量营养素），按克数记录，三餐分类，当日汇总 |
| 运动 | `TYPE_STEP_COUNTER` 计步（跨天重置基线）、训练计时器 |
| 统计 | MPAndroidChart 五类图表：折线、柱+线组合、饼图、带目标线柱状、雷达 |
| AI 顾问 | 七日报告与对话；OpenAI 兼容接口，无 Key 时走离线规则引擎 |
| 提醒 | WorkManager 定时：饮水每 2 小时、睡前 22:30，本地通知 |
| 导出 | 全量 CSV：体征、饮食、步数、训练、习惯 |
| 设置 | 分组列表与二级页：资料、目标、提醒、外观、AI 接口、导出、关于 |

## 界面引擎

- **三套配色**：Miuix 玻璃、莫兰迪 MD3、暗色高对比，统一由 `OmniPalette` 驱动。
- **三套导航骨架**：四 Tab + 中央 FAB、五 Tab 玻璃底栏、三 Tab 悬浮胶囊。
- **中英实时切换**：`LocalizedApp` 覆盖 Context 与 Configuration，切换后不重启、不回到首页。
- 所有选择持久化于 DataStore。

## 架构

```
data/    local(Room 9 实体 + DAO) · datastore · remote(Retrofit + 离线引擎) · repository · work · export
domain/  util(HealthCalculator / StepSensorManager / TimeUtils) · model(HealthContext)
ui/      theme(三套调色板 + 玻璃修饰符) · chart(五类图 + 样式器)
         navigation(三套底栏) · components(Bento / 快捷记录 / 主按钮)
         screens(home / diet_sport / stats / ai_advisor / settings)
```

- MVVM：每页一个 ViewModel + StateFlow，ViewModel 挂在 Activity 作用域，切 Tab 不重建。
- 计算逻辑纯函数化：`HealthCalculator`（BMI、Mifflin-St Jeor BMR、TDEE、宏量分配、五维评分）不依赖 Android，可独立单测。

## 技术栈

| 项 | 版本 |
|---|---|
| Kotlin / KSP | 2.0.21 / 2.0.21-1.0.28 |
| AGP | 8.7.3 |
| Compose BOM | 2024.10.01 |
| Room | 2.6.1 |
| DataStore / WorkManager | 1.1.1 / 2.9.1 |
| Retrofit / OkHttp | 2.11.0 / 4.12.0 |
| Navigation Compose | 2.8.4 |

## 性能

| 项 | 做法 |
|---|---|
| 冷启动 | 模拟器实测约 0.3 s：WorkManager 按需初始化、release + R8、baseline profile |
| 重组 | Compose 编译器报告审计：无 non-skippable Composable，状态类标注 `@Immutable` |
| 滚动 | 列表行不建图层、使用 `contentType` 复用、Tab 瞬切 |
| 绘制 | 单层线性渐变背景，无阴影与离屏层 |

## 构建

环境：JDK 17、Android SDK 35。

```bash
./gradlew :app:assembleRelease      # 需要 keystore.properties
./gradlew :app:testDebugUnitTest    # 单元测试
```

`keystore.properties` 不入库：

```properties
storeFile=keystore/omnihealth.jks
storePassword=****
keyAlias=omnihealth
keyPassword=****
```

## 测试

`app/src/test` 下 10 个测试类、53 个 `@Test`，覆盖健康计算、步数基线与数据仓库逻辑。

## 已知限制

- 计步依赖 `TYPE_STEP_COUNTER` 硬件传感器，无该传感器的设备上计步不可用（提供模拟器补步入口）。
- 食物字典为静态内置数据，来源为常见食物近似值，未做逐条营养学审核。
- AI 输出质量取决于所用模型；未配置 Key 时离线规则引擎只生成模板化结论。
- 单模块单 Activity，未做平板/横屏专门适配。
- 图表库为 MPAndroidChart（View 体系），与 Compose 通过 AndroidView 桥接。

## 里程碑

- M0 工程骨架、主题引擎、三套导航
- M1 数据层（Room + 食物字典）
- M2 健康计算引擎、步数传感器、单元测试
- M3 UI 引擎（玻璃卡片、Bento、快捷记录）
- M4 首页仪表盘（真实数据 + 五维评分）
- M5 饮食与运动
- M6 图表分析
- M7 AI 顾问（云端 + 离线兜底）
- M8 设置分组、编辑、习惯、提醒
- M9 发布：签名包、R8、启动优化、数据导出、关于页
