# 会话交接 / 上下文锚点

> 用途：把「当前状态 + 下一步」压缩成一份可独立阅读的文件。后续轮次只读这份文件，不再翻历史对话。

## 一、项目基线

- 仓库 `leyodr9000/OmniHealth`，`main` 分支，最新提交见 `git log -1`
- 最新发布 **v1.2.2**（1.96 MB release 包），构建：`./gradlew :app:assembleRelease`
- 技术栈：Kotlin 2.0.21 / AGP 8.7.3 / Compose BOM 2024.10.01 / Room / DataStore / WorkManager / MPAndroidChart / haze
- 默认界面 Material 3；外观页可切 MIUI X（液态玻璃卡片 + 底栏）
- 测试：14 个单测（HealthCalculatorTest 9、OfflineAiEngineTest 5）

## 二、已完成（不要再重做）

M0 骨架 → M1 数据层(211 食物) → M2 计算引擎+步数 → M3 UI 组件 → M4 首页真实数据 →
M5 饮食运动 → M6 五类图表 → M7 AI(云+离线) → M8 设置/习惯/提醒 → M9 签名包/启动图/导出/关于。

性能：冷启动 ~0.3 s（WorkManager 按需初始化）、包体 18.87→1.96 MB、0 个 non-skippable Composable。

## 三、已回滚 / 明确不做

- 官方 `miuix-kmp` 全站替换：布局适配失败，已回滚（其最新版要求 Compose 1.12 + Kotlin 2.4）。
- 垂直位移 + 淡入淡出转场：造成两页半透明重叠（重影），已改整屏水平翻页。
- 首屏逐段入场：与转场叠加致重影，已关闭。
- 渐变卡片、彩色阴影：已移除（真机透方框 / AI 味）。

## 四、进度（截至 v1.9.0）

P0 数据正确性与隐私 ✅ / P1 记录管理与备份恢复 ✅ / P2 统一体验 ✅（5 Tab、未记录≠0、inset 单一归属、大字体、深色模式）/ P3 分析可信度 ✅ / P4 待真机数据（协议见 docs/PERF_BASELINE.md）。

## 四.1 原计划清单

### P0 数据正确性与隐私（下一批）

1. ~~移除 destructive fallback~~ ✅ v1.3.0 已改为显式 Migration 列表 + 注释约束。
2. ~~缺失数据语义~~ ✅ v1.3.0：评分支持缺失维度归一化 + completeness/reliable，首页数据不足显示「—」。
3. ~~模拟步数隔离~~ ✅ v1.3.0：仅 BuildConfig.DEBUG 可见。
4. API Key 明文存 DataStore，需改用 Keystore 保护；导出/日志不得包含。
5. ~~CSV 公式注入~~ ✅ v1.3.0：公式前缀加单引号。

### P1 记录闭环

编辑/删除/撤销、历史补录、导出走系统分享、备份与恢复（含格式版本）。

### P2 统一体验

主导航收敛、首页层级、底栏 inset 单一归属、大字体/深色/键盘边界。

### P3 分析可信度

健康分改名「生活方式参考分」并披露权重与数据完整度；AI 标注本地/在线。

### P4 发布治理

真机 gfxinfo 基线、迁移回归、自动化 UI 冒烟。

## 五、工作约束（用户明确要求）

- 只发 release 包；debug 包不作为交付。
- 表面对话尽量短；大段内容写文件，不在对话里堆。
- 不无必要升级 Kotlin/Compose；不引入双 UI 框架；不清库解决迁移。
- 不把「编译通过」当完成，必须有行为验证。

## 六、环境备注

- 模拟器 AVD `omni_test`（android-35 x86_64），`screencap` 全黑，无法做像素验证，只能靠 `uiautomator dump` + `logcat`。
- 输出 token 上限已从 32k 调到 **64k**（`~/.dsh/profiles/web/cordis.patch.yml`），截断问题应缓解；仍建议分批输出。
