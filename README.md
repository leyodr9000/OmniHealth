# OmniHealth · 全能现代个人健康管理 App

> Kotlin 2.0 · Jetpack Compose · Room · DataStore · WorkManager · Retrofit · MPAndroidChart
> 三套配色 × 三套导航布局 × 中英实时切换 · 离线可用

---

## 下载

**正式包（推荐）**：https://github.com/leyodr9000/OmniHealth/releases/latest/download/OmniHealth-release.apk

- 1.9 MB，R8 混淆 + 资源压缩 + baseline profile
- `minSdk 26` / `targetSdk 35`，arm64 / armv7 / x86_64 全 ABI

---

## 功能

| 模块 | 说明 |
|---|---|
| 🏠 首页 | 综合健康分（5 维加权）、今日四指标、习惯打卡、AI 一句话结论 |
| 🍽 饮食 | 211 条预置食物字典（中英名 / 热量 / 三大宏量）、克数选择、三餐分类、当日宏量汇总 |
| 🏃 运动 | `TYPE_STEP_COUNTER` 计步（跨天基线重置）、模拟器补步、训练计时器 |
| 📊 统计 | MPAndroidChart 五类图：折线 / 组合（柱+线）/ 饼图 / 柱状（目标线）/ 雷达 |
| 🤖 AI 顾问 | 7 日报告 + 对话；OpenAI 兼容接口，**无 Key 时走离线规则引擎** |
| ⚙️ 设置 | 分组列表 + 二级页：资料 / 目标 / 提醒 / 外观 / AI 接口 / 导出 / 关于 |
| 🔔 提醒 | WorkManager：饮水每 2 小时、睡前 22:30，本地通知 |
| 📤 导出 | 全量数据 CSV（体征 / 饮食 / 步数 / 训练 / 习惯） |

## 界面引擎

- **三套配色**：MIUIX 玻璃 / 莫兰迪 MD3 / 赛博暗黑，`OmniPalette` 驱动全部颜色
- **三套导航骨架**：A 4 Tab + 中央 FAB、B 5 Tab 玻璃底栏、C 3 Tab 悬浮胶囊
- **中英实时切换**：`LocalizedApp` 覆盖 Context + Configuration，切完不重启、不跳页
- 全部选择持久化在 DataStore，重启保持

## 架构

```
data/    local(Room 8 表 + DAO) · datastore · remote(Retrofit + 离线引擎) · repository · work · export
domain/  util(HealthCalculator / StepSensorManager / TimeUtils) · model(HealthContext)
ui/      theme(三套调色板 + 玻璃修饰符 + 弹性点击) · chart(五类图 + 样式器)
         navigation(三套底栏) · components(Bento / 快捷记录 / 主按钮)
         screens(home / diet_sport / stats / ai_advisor / settings)
```

- MVVM：每页一个 `ViewModel` + `StateFlow`，ViewModel 挂在 Activity 作用域，切 Tab 不重建
- 计算纯函数化：`HealthCalculator`（BMI / Mifflin-St Jeor BMR / TDEE / 宏量 / 5D 评分）无 Android 依赖，可单测

## 性能

| 项 | 做法 |
|---|---|
| 冷启动 | ~0.3 s（模拟器实测）：WorkManager 按需初始化、Release + R8、baseline profile |
| 重组 | Compose 编译器报告审计：**0 个 non-skippable Composable**，状态类标 `@Immutable` |
| 滚动 | 列表行用无图层 `quietClick`、`contentType` 复用、Tab 瞬切 |
| 绘制 | 单层线性渐变背景、无阴影、无离屏层 |

## 构建

```bash
# 需要 JDK 17 + Android SDK 35
./gradlew :app:assembleRelease      # 正式包（需 keystore.properties）
./gradlew :app:testDebugUnitTest    # 14 个单元测试
```

`keystore.properties` 不入库：

```properties
storeFile=keystore/omnihealth.jks
storePassword=****
keyAlias=omnihealth
keyPassword=****
```

## 隐私

所有记录保存在本机 Room 数据库，不上传云端。只有你主动配置 API Key 后，AI 请求才会离开设备；未配置时全部由本地规则引擎生成。

## 里程碑

- [x] M0 工程骨架 · 主题引擎 · 三套导航
- [x] M1 数据层（Room + 211 条食物字典）
- [x] M2 健康计算引擎 + 步数传感器 + 单元测试
- [x] M3 UI 引擎（玻璃卡片 / Bento / 快捷记录）
- [x] M4 首页仪表盘（真实数据 + 5D 评分）
- [x] M5 饮食与运动
- [x] M6 图表分析
- [x] M7 AI 顾问（云 + 离线兜底）
- [x] M8 设置分组 + 编辑 + 习惯 + 提醒
- [x] M9 发布：签名包、R8、启动优化、数据导出、关于页
