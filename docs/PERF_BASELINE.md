# P4 性能基线采集指南（真机）

> 模拟器是软件渲染，帧率与启动耗时没有参考价值。以下流程需要一台真机（建议就是你的骁龙 8 Elite 机型）。

## 0. 准备

```bash
# 手机开启 USB 调试，连上电脑后确认
adb devices

# 安装待测包（release，不是 debug）
adb install -r OmniHealth-release.apk

# 关闭系统动画干扰（可选，测完记得改回 1.0）
adb shell settings put global window_animation_scale 0
adb shell settings put global transition_animation_scale 0
adb shell settings put global animator_duration_scale 0
```

记录三项环境：机型、系统版本、屏幕刷新率。

```bash
adb shell getprop ro.product.model
adb shell getprop ro.build.version.release
adb shell dumpsys display | findstr "fps"
```

## 1. 冷启动（P50 / P95）

```bash
# 跑 10 次，每次强制停止并清后台
for i in $(seq 1 10); do
  adb shell am force-stop com.example.omnihealth
  sleep 2
  adb shell am start -W -n com.example.omnihealth/.MainActivity | findstr TotalTime
done
```

判定：
- **P50 < 800 ms**（本机基线，中端机可放宽到 1200 ms）
- **P95 / P50 < 1.6**（抖动不能太大）

输出十次 `TotalTime` 的原始数值即可，我来算 P50/P95。

## 2. 滑动与切页掉帧

```bash
adb shell dumpsys gfxinfo com.example.omnihealth reset
# 手动操作 30 秒：首页上下滑动、切 5 个 Tab 各两次、打开一次快捷记录
adb shell dumpsys gfxinfo com.example.omnihealth
```

看 `Janky frames: x%`：
- 60Hz 机型 **< 5%**
- 120Hz 机型 **< 8%**（帧预算只有 8.3 ms）

若要逐帧数据：

```bash
adb shell dumpsys gfxinfo com.example.omnihealth framestats > framestats.txt
```

把这文件原样发我，我按帧耗时分布找长尾。

## 3. 首页加载与数据库

```bash
adb shell am force-stop com.example.omnihealth
adb logcat -c
adb shell am start -n com.example.omnihealth/.MainActivity
sleep 5
adb logcat -d | findstr /i "omnihealth"
```

若首页有超过 200 ms 的卡顿，通常是 5 条 Flow 首次合并 + 图表初始化；可用 CPU profiler 二次定位。

## 4. 内存

```bash
adb shell dumpsys meminfo com.example.omnihealth | findstr "TOTAL PSS"
```

- 首页停留 5 分钟基线：**< 180 MB PSS**
- 切页 50 次后增长：**< 20 MB**（超出说明有泄漏）

## 5. 后台耗电

```bash
adb shell dumpsys batterystats --reset
# 正常使用 2 小时（或待机 2 小时）
adb shell dumpsys batterystats com.example.omnihealth | findstr "Estimated power"
```

重点看 WorkManager 提醒是否被系统频繁唤醒；饮水提醒 2 小时一次属于预期。

## 6. 需要回传的最小信息

1. 机型 / 系统版本 / 刷新率
2. 冷启动十次 `TotalTime`
3. `Janky frames` 百分比
4. `TOTAL PSS`
5. 若有明显卡顿：卡顿发生时的操作路径（哪个页面、什么动作）

有这五项就能判断是否达标，以及下一步该优化渲染、查询还是首帧布局。
