# 项目架构与开发规范指南

欢迎加入 AgroAi 开发团队！本文档旨在帮助新手快速了解项目的整体架构、模块划分、依赖关系，并提供添加新模块及开发的标准规范。

## 1. 项目基础配置与运行环境

在开始开发前，请确保你的开发环境满足以下要求：
- **IDE**: 推荐使用最新版的 Android Studio (至少 Giraffe 及以上版本)。
- **Java 版本**: JDK 17 (`sourceCompatibility = JavaVersion.VERSION_17`)。
- **Kotlin 版本**: `1.9.0`。
- **Android Gradle Plugin (AGP)**: `8.0.2`。
- **AndroidX**: 项目已完全迁移至 AndroidX (`android.useAndroidX=true`)。

**SDK 版本控制**：
- `compileSdk`: **34** (Android 14)
- `targetSdk`: **34**
- `minSdk`: **26** (Android 8.0)，意味着本应用最低支持 Android 8.0 设备。

---

## 2. 项目整体架构

本项目采用 **组件化 / 模块化** 架构，并结合 **MVVM** 模式进行开发。
整个工程自下而上分为三层：

### 2.1 Foundation 层 (基础层)
提供最底层、最通用的技术支撑，不包含任何具体的业务逻辑。所有上层业务模块都可以依赖这一层。
- **`foundation:common`**：核心基础库，包含 BaseActivity、BaseFragment、各种 Utils 工具类（图片、尺寸、日期、权限等）和通用的自定义 View（TopBar、LoadingDialog）。
- **`foundation:network`**：网络请求模块，封装了 Retrofit + OkHttp + RxJava，统一管理 API 接口、拦截器和证书配置。
- **`foundation:storage`**：本地存储模块，基于 MMKV 封装，支持高性能、分场景的键值对存储。
- **`foundation:webview`**：网页容器模块，封装了通用的 WebViewActivity 和配置工具。

### 2.2 Business 层 (业务层)
根据产品功能划分的独立业务模块。为了彻底解耦，每个业务模块被拆分为 `api` 和 `impl` 两个子模块：
- **`api`**：暴露给其他模块的接口（Interface）和数据模型。例如 `UserService`。
- **`impl`**：具体的业务实现和 UI 页面。例如 `LoginActivity` 和 `UserServiceImpl`。

目前包含的业务模块：
- **`business:main`**：App的主框架、首页承载。
- **`business:user`**：用户模块（登录、注册、个人中心）。
- **`business:detection`**：核心病虫害检测模块。
- **`business:community`**：社区交流模块。

### 2.3 App 层 (壳工程)
- **`app`**：项目的最终入口。它负责将所有的业务模块（`impl`）打包组合在一起，并进行全局的初始化操作（如在 `App.kt` 中初始化 ARouter、MMKV、网络等）。

---

## 3. 模块依赖关系原则

为了避免模块间的强耦合和循环依赖，团队必须遵守以下依赖原则：

1. **下层不可依赖上层**：`foundation` 层绝对不能依赖 `business` 层。
2. **业务模块实现层互相隔离**：`business:A:impl` 绝对不能直接依赖 `business:B:impl`。
3. **通过 API 层进行通讯**：如果 `main` 模块需要调用 `user` 模块的方法，`main:impl` 只能依赖 `user:api`。
4. **统一路由跳转**：所有 Activity 之间的跳转必须通过 **ARouter**，禁止使用显式的 Intent 跳转。

---

## 4. 如何新增一个业务模块

假设我们要新增一个商城模块（`mall`），请按以下步骤操作：

### Step 1: 创建目录结构
在 `business/` 目录下新建 `mall` 文件夹，并在其内部新建 `api` 和 `impl` 两个 Android Library 模块。

### Step 2: 配置 `build.gradle.kts`
**`business/mall/api/build.gradle.kts`**
```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
}
android { namespace = "com.mall.api" }
dependencies {
    // api 层通常只依赖最基础的路由组件
    implementation(libs.arouter.api)
}
```

**`business/mall/impl/build.gradle.kts`**
```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.jetbrains.kotlin.kapt)
}
android {
    namespace = "com.mall.impl"
    buildFeatures { viewBinding = true }
}
dependencies {
    implementation(project(":business:mall:api"))
    implementation(project(":foundation:common")) // 依赖基础组件
    kapt(libs.arouter.compiler) // ARouter 编译生成代码
}
```

### Step 3: 在 `settings.gradle.kts` 中注册模块
```kotlin
include(":business:mall:api")
include(":business:mall:impl")
```

### Step 4: 在壳工程 `app/build.gradle.kts` 中引入实现
```kotlin
dependencies {
    implementation(project(":business:mall:impl"))
}
```

### Step 5: 定义和实现路由服务
1. 在 `mall:api` 中定义接口 `MallService` 继承 `IProvider`。
2. 在 `mall:impl` 中创建 `MallServiceImpl` 实现该接口，并加上 `@Route(path = "/mall/service")` 注解。

---

## 5. 开发规范

- **包名规范**：统一使用 `com.模块名`。例如 `com.user`，`com.detection`。
- **UI 规范**：
  - 所有的 Activity 必须继承 `BaseActivity<VB>`。
  - 所有的 Fragment 必须继承 `BaseFragment<VB>`。
  - 开启了 ViewBinding，禁止使用 `findViewById`。
- **状态栏规范**：系统已在 `BaseActivity` 中全局配置了沉浸式透明状态栏。在写 XML 布局时，根节点务必加上 `android:fitsSystemWindows="true"` 防止内容与状态栏重叠。
- **网络请求**：所有的 API 接口统一定义在 `foundation/network` 的 `ApiService` 中，通过 `NetworkManager.api` 发起请求。
- **依赖管理**：所有第三方库的版本统一在 `gradle/libs.versions.toml` 中管理，禁止在各个模块的 build.gradle 中写死版本号。