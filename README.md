# 🌿 AgroAi - 农业识别病虫害 App

**AgroAi** 是一款专注于农业病虫害识别与社区交流的 Android 应用程序。项目采用最新的 Android 开发技术栈，基于组件化架构进行开发，旨在提供高效、稳定、易扩展的业务体验。

---

## 🚀 快速开始

无论你使用的是 Windows、macOS 还是 Linux，在克隆本项目后，请**第一时间运行环境配置脚本**。该脚本会自动检查并安装所需的 Java 17、Android SDK 路径，并自动配置团队统一的 Git 提交拦截钩子（防止误推 master）。

### 对于 macOS / Linux 用户：
打开终端，在项目根目录下执行：
```bash
./setup_env.sh
```

### 对于 Windows 用户：
在项目根目录下，直接双击运行：
```cmd
setup_env.bat
```

> **注意：** 脚本执行成功并显示“环境检查完成”后，你可以直接使用 Android Studio 打开本项目并点击 `Run` 按钮运行 App。

---

## 📚 团队开发文档 (必读)

为了保证团队协作的高效和代码质量，所有新加入的开发者在正式写代码前，请务必阅读以下文档：

1. **[🏗 项目架构与开发规范指南](docs/1_Project_Architecture.md)**
   - 了解项目的组件化分层（Foundation层、Business层、App层）。
   - 学习如何规范地新增一个业务模块。
2. **[🛠 项目工具与组件使用手册](docs/2_Tools_And_Utils.md)**
   - 熟悉现成的 UI 组件（如 BaseActivity、TopBar）。
   - 了解如何使用图片加载、本地存储、网络请求等工具。
3. **[🤝 Git 团队协作流程与规范](docs/3_Git_Workflow.md)**
   - 学习团队的分支管理模型。
   - 了解如何提交 PR/MR，以及绝对禁止的“红线行为”。
4. **[📦 项目开源框架与依赖指南](docs/4_Open_Source_Libraries.md)**
   - 查看项目中引入的所有第三方库（ARouter, Retrofit, Coil 等）的 GitHub 链接与基础用法。

---

## 🧩 核心技术栈

- **语言**: Kotlin 1.9+
- **构建工具**: Gradle 8.0+ (Kotlin DSL `.gradle.kts`)
- **架构模式**: 组件化 + MVVM
- **路由组件**: [ARouter](https://github.com/alibaba/ARouter)
- **网络请求**: [Retrofit](https://github.com/square/retrofit) + [OkHttp](https://github.com/square/okhttp) + [RxJava3](https://github.com/ReactiveX/RxJava)
- **本地存储**: [MMKV](https://github.com/Tencent/MMKV) (多实例分场景存储)
- **图片加载**: [Coil](https://github.com/coil-kt/coil)
- **调试利器**: [CodeLocator](https://github.com/bytedance/CodeLocator)

---

## 📁 目录结构简介

```text
AgroAi/
├── app/                  # 壳工程（负责组装所有模块和全局初始化）
├── business/             # 业务逻辑层（按功能划分）
│   ├── community/        # 社区交流模块 (api / impl)
│   ├── detection/        # 病虫害检测模块 (api / impl)
│   ├── main/             # 主框架与首页模块 (api / impl)
│   └── user/             # 用户中心模块 (api / impl)
├── foundation/           # 基础通用层
│   ├── common/           # 核心工具类、基类、通用 UI 组件
│   ├── network/          # 全局网络请求封装
│   ├── storage/          # MMKV 存储封装
│   └── webview/          # 通用网页容器模块
├── docs/                 # 团队开发与规范文档
├── .githooks/            # 团队共享的 Git 拦截脚本
├── setup_env.sh          # Mac/Linux 环境一键配置脚本
└── setup_env.bat         # Windows 环境一键配置脚本
```

---

## 🛡️ License

本项目仅供学习与内部交流使用。