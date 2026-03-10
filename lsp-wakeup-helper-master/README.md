# WakeUp Helper (LSPosed)

用于拦截 WakeUp 课程表广告链路的 LSPosed 模块。

## 信息

- 模块包名（Package Name）：`cn.dorimu.lsp.wakeuphelper`
- 目标应用包名：`com.suda.yzune.wakeupschedule`
- 当前适配版本：`WakeUp 6.1.30`

## 说明

安装模块后在 LSPosed 中启用，并确认作用域仅勾选 WakeUp 课程表。

## 构建

环境要求：

- JDK 17
- Android SDK（compileSdk 35）
- 使用项目内置 Gradle Wrapper（8.7）

命令：

```bash
./gradlew :app:assembleDebug
```

## 协议

本项目使用 [GPLv3](./LICENSE) 协议。

## 免责声明

- 本项目仅用于技术研究与学习交流，请在遵守当地法律法规及目标应用用户协议的前提下使用。
- 使用本项目产生的任何直接或间接后果（包括但不限于账号、服务、设备或数据风险）由使用者自行承担。
- 本项目与 WakeUp 课程表及其开发团队无任何隶属、授权或商业合作关系。
