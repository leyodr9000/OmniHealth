# OmniHealth · 全能现代个人健康管理 App

> Kotlin 2.0 · Jetpack Compose · Room · DataStore · Retrofit · MPAndroidChart · 三主题 × 三布局实时切换

OmniHealth 是一个「开箱即用」的 Android 个人健康管理应用：体征记录、习惯打卡、步数传感、饮食热量字典、图表分析、AI 健康顾问，全部本地可跑，无需联网也能用。

---

## 功能总览

| 模块 | 说明 |
|---|---|
| 🏠 首页 Bento | 综合健康分、2×2 指标卡、AI 一句话建议 |
| 🍽 饮食 | 预置 200+ 食物热量字典、三餐记录、宏量营养环 |
| 🏃 运动 | 训练计时器、步数传感器、活动消耗 |
| 📊 统计 | MPAndroidChart：折线 / 柱状 / 饼图 / 组合图 / 雷达图 |
| 🤖 AI 顾问 | 7 日健康报告 + 对话，支持 DeepSeek / 自定义 API，离线兜底 |
| 🎨 外观实验室 | MIUIX 玻璃 / 莫兰迪 MD3 / 赛博暗黑，三套主题实时切换 |
| 🧭 导航骨架 | 4 Tab + FAB / 5 Tab 玻璃底栏 / 3 Tab 胶囊 Bento，实时切换 |
| ⚙️ 设置 | 个人资料（BMR/TDEE 计算）、目标、API 配置 |

## 技术栈

- **语言**：Kotlin 2.0.21，JDK 17
- **UI**：Jetpack Compose + Material 3，自研 MIUIX 组件（超椭圆、弹簧按压、分段控件）
- **毛玻璃**：`dev.chrisbanes.haze` + 动态网格渐变画布
- **架构**：MVVM + Clean（单 Activity、Compose Navigation、StateFlow）
- **数据**：Room（KSP）+ DataStore Preferences
- **网络**：Retrofit2 + OkHttp3 + kotlinx.serialization
- **图表**：MPAndroidChart 3.1.0（JitPack）+ `AndroidView` 封装
- **后台**：WorkManager（饮水 / 睡眠提醒）

## 构建

```bash
# 需要 JDK 17 + Android SDK (compileSdk 35)
./gradlew :app:assembleDebug
```

产物：`app/build/outputs/apk/debug/app-debug.apk`

## 下载安装

每次里程碑构建都会上传到 [Releases](https://github.com/leyodr9000/OmniHealth/releases)：

- 最新直达：`https://github.com/leyodr9000/OmniHealth/releases/latest/download/OmniHealth-debug.apk`

## 里程碑

- [x] M0 工程骨架 · 主题引擎 · 三套导航
- [x] M1 数据层（Room + 211 条食物字典预置）
- [x] M2 健康计算引擎 + 步数传感器 + 单元测试
- [x] M3 UI 引擎（玻璃卡片 / Bento / 快捷记录）
- [x] M4 首页仪表盘（真实数据 + 5D 评分）
- [x] M5 饮食与运动（食物字典搜索/记录、步数模拟、训练计时）
- [ ] M6 图表分析
- [ ] M7 AI 顾问
- [ ] M8 设置与外观实验室
- [ ] M9 打磨与发布
