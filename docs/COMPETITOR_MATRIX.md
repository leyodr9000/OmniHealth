# 更广的同类产品对照（补充）

> 数据来源：各产品官方商店页与公开文章标题/摘要，**未逐项在 App 内核验**。
> 凡标注「据商店页」的信息，仅依据页面标题或简介，不推断细节实现。

## 一、覆盖范围

| 产品 | 定位 | 关键记录能力（公开信息） |
|---|---|---|
| 薄荷健康 | 中文营养记录 + 商城 | 长期饮食记录、食物库、周月总结（[产品分析](https://www.163.com/dy/article/FADQOSSI0511805E.html)） |
| Keep | 运动课程 + 社交 | 课程、社区、少量饮食记录 |
| 好轻 | 体重 + 饮食，AI 拍照 | 商店页标题即「[AI 拍照识热量](https://app.xiaomi.com/details?back=true&id=com.yunmai.scale&ref=mobileWeb)」「[AI 减肥热量记录与体重管理](https://apps.apple.com/cn/app/%E5%A5%BD%E8%BD%BB-ai%E5%87%8F%E8%82%A5%E7%83%AD%E9%87%8F%E8%AE%B0%E5%BD%95%E4%B8%8E%E4%BD%93%E9%87%8D%E7%AE%A1%E7%90%86/id889565307)」 |
| 华为运动健康 | 系统级健康中枢 | 桌面小卡片（[用户提问帖](https://club-api.c.honor.com/thread-28921378-1-1.html?authorid=273431679)）、步数轨迹、设备联动 |
| Zepp Life | 穿戴配套 | 体重、睡眠、活动数据（依赖设备） |
| MyFitnessPal | 食物库规模 | [跨天复制一餐](https://support.myfitnesspal.com/hc/en-us/articles/360032622131-How-do-I-copy-a-meal-from-one-day-to-another)、[Saved Meals](https://support.myfitnesspal.com/hc/en-us/articles/360032625331-Create-find-and-log-your-saved-meals) |
| Lose It! | 美国主流记录 | 官方文章列出「[让记录更简单的 5 个功能](https://cdn-www.loseit.com/articles/5-lose-it-features-to-make-logging-easier)」、[Snap It 拍照记录](https://prod.loseitblog.com/articles/how-do-i-use-lose-it-s-new-snap-it-feature/)、[AI 记录提升依从性](https://www.loseit.com/press/releases/AIPoweredLoggingBoostsSuccess.md) |
| Yazio | 欧洲记录 + 断食 | 商店页定位「[AI 热量追踪](https://apps.apple.com/tm/app/ai-calorie-counter-by-yazio/id946099227)」，含断食 |
| Cronometer | 微量营养素 | [以营养深度与易用性对比](https://nutrientmetrics.com/en/guides/cronometer-vs-yazio-micronutrient-vs-ease-of-use)，主打维生素/矿物质 |

## 二、六个维度横向对比

| 维度 | 行业普遍做法 | 我们的现状 | 结论 |
|---|---|---|---|
| **录入方式** | 搜索、最近常吃、复制上一餐、拍照/条码、AI 文字 | 搜索、最近常吃、复制上一餐、套餐、一句话记餐 | 除拍照/条码外已齐 |
| **重复劳动** | 全部产品都做「最近/复制/套餐」 | ✅ v3.4–v3.5 | 已补齐 |
| **拍照识别** | 好轻、Lose It、Yazio 均主打 | 不做 | 取舍：包体与准确率成本高，用文本解析替代 |
| **小组件/卡片** | 华为、好轻等系统级产品标配 | 无 | **最大缺口** |
| **社交/排名** | 薄荷、Keep 做 | 不做 | 保持不做，与定位一致 |
| **付费墙** | 拍照识别、断食、营养深度多为付费点 | 全功能免费 | 无商业化诉求，不构成问题 |

## 三、从"更多产品"里新增的三点判断

### 1. 拍照识别已是行业标配，但我们不做是对的

好轻、Lose It（Snap It）、Yazio 都把拍照作为卖点。但它依赖大模型/图像服务，离线不可用、包体增加、误识别率不透明。

我们的替代方案是**一句话记餐**（文本 → 本地规则解析 → 词表匹配），代价是用户要打几个字，收益是完全离线、可解释、不产生幻觉数值。对"个人长期使用"这个场景，这是合理取舍。

### 2. 小组件不是"锦上添花"，是记录类产品的入口基建

华为运动健康的桌面卡片、好轻的体重组件、Zepp 的表盘数据，本质都是**把"打开 App"这一步省掉**。我们目前每次记录都要：解锁 → 找图标 → 打开 → 等首帧 → 切到记录页。

这是当前最影响"愿不愿意每天用"的单点，优先级应排在食物库扩充之前。

### 3. 微量营养素（Cronometer 路线）不做，但可保留一个轻量差异点

Cronometer 用维生素/矿物质深度做差异化，代价是记录成本极高（几乎要吃什么都精确称重）。

我们的数据模型里已有 `fiber` 字段，可以只做一个**「本周纤维达标天数」**的轻量指标，既不透支记录成本，又能体现"不只是热量"。

## 四、据此调整后的优先级

| 排序 | 项 | 变化 |
|---|---|---|
| 1 | 桌面小组件 | 上调（跨产品验证为入口基建） |
| 2 | 记录提醒 | 不变 |
| 3 | 食物库扩充 | 下调（数据来源未定） |
| 4 | 份量双单位 | 不变 |
| 5 | 纤维达标轻指标 | 新增（低成本差异化） |
| 6 | 周报图片分享 | 不变 |
| 7 | 条码/拍照 | 明确后置 |

## 五、仍不确定的部分

- 各产品的**真实日活/留存数据**只有第三方（易观等）旧数据，无法据此假设我们的合理目标；
- 华为/Zepp 的小组件实现细节未核验，只能确认"存在";
- 国内产品多为商城+内容驱动，其留存机制不可直接移植到无运营的个人项目。
