# 项目开源框架与依赖指南

本项目集成了一系列成熟、稳定的开源框架，以提升开发效率和应用性能。以下是本项目所依赖的核心开源库介绍、GitHub 链接以及简单的使用示例。

---

## 1. 路由与组件化通信

### **ARouter**
- **GitHub**: [alibaba/ARouter](https://github.com/alibaba/ARouter)
- **作用**: 阿里开源的 Android 路由框架，用于实现组件化架构下的页面跳转、服务获取和参数传递。
- **使用示例**:
  **页面跳转**：
  ```kotlin
  // 简单跳转
  ARouter.getInstance().build("/main/home").navigation()
  
  // 携带参数跳转
  ARouter.getInstance().build("/user/detail")
      .withString("userId", "12345")
      .navigation()
  ```
  **获取跨模块服务**：
  ```kotlin
  val userService = ARouter.getInstance().navigation(UserService::class.java)
  val isLogin = userService?.isLogin() ?: false
  ```

---

## 2. 网络请求与数据解析

### **Retrofit**
- **GitHub**: [square/retrofit](https://github.com/square/retrofit)
- **作用**: Type-safe 的 HTTP 客户端，用于定义和发起网络请求。
- **使用示例**:
  ```kotlin
  interface ApiService {
      @GET("api/user/info")
      fun getUserInfo(): Observable<UserInfo>
  }
  ```

### **OkHttp**
- **GitHub**: [square/okhttp](https://github.com/square/okhttp)
- **作用**: 底层的高效 HTTP 客户端，处理连接池、缓存、拦截器等。本项目中使用它来配置 SSL 信任和日志拦截。

### **Gson**
- **GitHub**: [google/gson](https://github.com/google/gson)
- **作用**: 将 JSON 字符串与 Kotlin/Java 对象相互转换。
- **使用示例**:
  ```kotlin
  data class User(
      @SerializedName("id") val id: Int,
      @SerializedName("name") val name: String
  )
  ```

---

## 3. 异步与响应式编程

### **RxJava3 & RxAndroid**
- **GitHub**: [ReactiveX/RxJava](https://github.com/ReactiveX/RxJava) / [ReactiveX/RxAndroid](https://github.com/ReactiveX/RxAndroid)
- **作用**: 响应式编程框架，用于处理复杂的异步任务、线程切换和网络请求的链式调用。
- **使用示例**:
  ```kotlin
  NetworkManager.api.getUserInfo()
      .subscribeOn(Schedulers.io()) // 在 IO 线程发起请求
      .observeOn(AndroidSchedulers.mainThread()) // 在主线程接收结果
      .subscribe({ user ->
          // 成功
          binding.tvName.text = user.name
      }, { error ->
          // 失败
          ToastUtils.showShort(this, error.message)
      })
  ```

---

## 4. 图片加载

### **Coil**
- **GitHub**: [coil-kt/coil](https://github.com/coil-kt/coil)
- **作用**: 基于 Kotlin 协程的轻量级、现代化的图片加载库。
- **使用示例**:
  ```kotlin
  // 直接使用 Coil 的扩展函数
  imageView.load("https://example.com/image.jpg") {
      crossfade(true)
      transformations(CircleCropTransformation()) // 圆形裁剪
  }
  ```
  *(注：项目中已在 `foundation:common` 封装了 `ImageLoader` 工具类，推荐直接使用封装类。)*

---

## 5. 本地数据存储

### **MMKV**
- **GitHub**: [Tencent/MMKV](https://github.com/Tencent/MMKV)
- **作用**: 腾讯开源的基于 mmap 的高性能键值对存储组件，速度远超 SharedPreferences。
- **使用示例**:
  ```kotlin
  // 获取指定场景的 MMKV 实例
  val kv = MMKVUtils.custom("user_settings")
  
  // 存储
  kv.put("token", "abcdefg")
  
  // 读取
  val token = kv.getString("token", "")
  ```

### **Room (Android Jetpack)**
- **官方文档**: [Room Persistence Library](https://developer.android.com/training/data-storage/room)
- **作用**: Google 官方提供的 SQLite 对象映射库，用于处理复杂的本地数据库存储。

---

## 6. 权限请求

### **PermissionX**
- **GitHub**: [guolindev/PermissionX](https://github.com/guolindev/PermissionX)
- **作用**: 郭霖开源的极其易用的 Android 运行时权限请求库。
- **使用示例**:
  ```kotlin
  PermissionX.init(this)
      .permissions(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
      .request { allGranted, grantedList, deniedList ->
          if (allGranted) {
              Toast.makeText(this, "所有权限已授予", Toast.LENGTH_SHORT).show()
          } else {
              Toast.makeText(this, "被拒绝的权限：$deniedList", Toast.LENGTH_SHORT).show()
          }
      }
  ```

---

## 7. 调试与开发工具

### **CodeLocator**
- **GitHub**: [bytedance/CodeLocator](https://github.com/bytedance/CodeLocator)
- **作用**: 字节跳动开源的极其强大的 Android UI 调试工具集。支持抓取 View 树、实时修改 UI、查看应用信息等。
- **使用方法**:
  在 Android Studio 中安装 CodeLocator 插件，运行项目的 Debug 包，点击 AS 侧边栏的 CodeLocator 抓取当前页面即可。