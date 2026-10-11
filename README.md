# 本地 HTML 预览器 MVP

这是一个 Android 10+ 的原生 Kotlin MVP，支持：

- SAF 选择 HTML/ZIP
- 应用私有目录存储
- ZIP 路径穿越、文件数量和解压大小限制
- `index.html` 入口识别
- 127.0.0.1 随机端口静态 HTTP 服务
- WebView JavaScript、ES Module、CSS、图片和 `fetch` 预览
- 外部 URL、非本地资源和 WebView 权限请求拦截

## 构建

使用 Android Studio Hedgehog 或更高版本打开目录，使用 JDK 17，同步 Gradle 后运行 `app`。

## 当前 MVP 边界

- 默认阻止外部网络、下载、弹窗和系统 URI。
- 支持普通 ZIP 目录项，并拒绝路径穿越和超限内容。
- 仅实现 Android 版本；HarmonyOS NEXT 需要按 ArkTS/ArkWeb 单独实现平台层。
