# LuluCalendar

LuluCalendar 是一个基于 Kotlin 与 Jetpack Compose 的开源 Android 日程与任务管理应用。

当前版本：**v0.0.1**

## 功能

- 每日任务与日历视图
- 任务开始/结束时间与优先级
- 重复任务与提醒
- 番茄钟
- 空闲时间分析
- 本地 JSON 数据备份
- 多主题与动态主题
- 多语言，默认语言为中文

## v0.0.1 定制变化

- 应用品牌更名为 **LuluCalendar**
- 默认语言改为中文
- 移除 Restore 恢复入口与崩溃报告功能
- 移除设置中的“支持”入口
- 移除桌面 Widget 入口与系统注册
- 移除设备日历同步入口与系统日历权限
- 移除 ICS / Import events 导入入口
- GitHub Actions 自动编译 APK，并在发布提交时创建 GitHub Release

## 构建

```bash
./gradlew :app:assembleDebug
```

APK 输出位置：

```text
app/build/outputs/apk/debug/app-debug.apk
```

## License

本项目继续遵循仓库中的 GNU GPL v3.0 许可证。
