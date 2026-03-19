# 项目工具与组件使用手册

在 `foundation` 层，我们封装了许多常用且高效的工具。为了避免重复造轮子，请在开发前仔细阅读本手册，了解已有的工具及其使用方法。

***

## 1. 基础 UI 组件 (位于 `foundation:common`)

### 1.1 BaseActivity & BaseFragment

所有页面都应该继承这两个基类，它们已经为你处理好了 ViewBinding 的初始化、沉浸式状态栏适配以及全局加载弹窗的逻辑。

```kotlin
class MyActivity : BaseActivity<ActivityMyBinding>() {
    override fun getViewBinding(): ActivityMyBinding {
        return ActivityMyBinding.inflate(layoutInflater)
    }

    override fun initView() {
        // 初始化视图
        showLoading("正在处理...") // 显示加载框
        hideLoading() // 隐藏加载框
    }

    override fun initData() {
        // 请求数据
    }
}
```

### 1.2 TopBar (统一顶部导航栏)

在 XML 布局中直接引入，提供统一的标题和返回逻辑：

```xml
<com.common.widget.TopBar
    android:id="@+id/top_bar"
    android:layout_width="match_parent"
    android:layout_height="wrap_content" />
```

```kotlin
binding.topBar.setTitle("病虫害详情")
binding.topBar.setRightText("分享") { 
    // 点击事件
}
```

***

## 2. 常用工具类 (位于 `foundation:common/utils`)

### 2.1 ImageLoader (图片加载)

基于 Coil 封装。它已经配置好了全局的 Http 证书信任（修复了 SSL 校验问题）。

```kotlin
// 加载网络图片
ImageLoader.load(imageView, "https://xxx.jpg")
// 加载圆角图片 (20f 为圆角半径)
ImageLoader.loadRounded(imageView, "https://xxx.jpg", 20f)
// 加载圆形图片 (常用于头像)
ImageLoader.loadCircle(imageView, "https://xxx.jpg")
// 加载本地资源
ImageLoader.loadLocal(imageView, R.drawable.ic_logo)
```

### 2.2 MMKVUtils (高性能本地存储)

支持分场景（多实例）存储，方便模块解耦和数据清理。

```kotlin
// 获取名为 "user_info" 的存储实例
val userStorage = MMKVUtils.custom("user_info")

// 存储数据
userStorage.put("token", "abc123xxx")
userStorage.put("age", 25)

// 读取数据
val token = userStorage.getString("token", "")

// 清除该实例下的所有数据（用户退出登录时调用，不影响其他场景的数据）
userStorage.clear()
```

### 2.3 ImagePickerUtil (图片选择器)

封装了 Android 最新的照片选择器和拍照逻辑，自动处理权限申请（适配了 Android 13/14）。
**注意：** 必须在 Activity 的初始化阶段（如 `initView`）进行实例化。

```kotlin
// 1. 初始化
val imagePicker = ImagePickerUtil(this) { uri ->
    // 获取到图片的 URI，可以直接通过 ImageLoader 显示或上传
    ImageLoader.load(binding.ivImage, uri.toString())
}

// 2. 在点击事件中弹出选择框（拍照/相册）
binding.btnSelect.setOnClickListener {
    imagePicker.showImageSourceDialog()
}
```

### 2.4 LogUtils (规范化日志工具)

全局统一日志工具，用于替代原生的 `android.util.Log`。
- **自动 TAG**：无需硬编码，自动提取当前类所在模块及类名作为前缀（如 `main-DebugDemoActivity`）。
- **超长文本**：自动分段输出，打破 Logcat 4000 字符限制。

```kotlin
LogUtils.d("这是一条 Debug 日志，无需传 TAG")
LogUtils.e("发生错误", RuntimeException("error detail"))
```

### 2.5 ThreadUtils & ThreadPoolManager (多线程调度)

封装了全局线程池和主线程切换，防 OOM 和线程饥饿，替代原生的 `Thread {}.start()`。

```kotlin
// 切换到主线程
ThreadUtils.runOnUiThread { binding.tvName.text = "Update" }

// 延迟任务
ThreadUtils.executeDelayed({ /* 2秒后执行 */ }, 2000)

// 提交耗时 IO 任务 (网络/数据库/文件)
ThreadUtils.executeByIo { 
    val data = db.query() 
    ThreadUtils.runOnUiThread { updateUI(data) }
}

// 提交 CPU 密集型任务 (计算/大图处理)
ThreadUtils.executeByCpu { compressImage() }
```

### 2.6 ViewExt & LiveDataExt (高级扩展函数)

提升开发效率的扩展：

**View 扩展：**
```kotlin
// 防抖点击（默认 500ms 内拦截连击）
binding.btnSubmit.setOnDebouncedClickListener { submit() }

// 可见性切换
binding.view.visible()
binding.view.gone()
binding.view.toggleVisibility()

// 获取真实宽高
binding.view.afterMeasured { v -> LogUtils.d("Width: ${v.measuredWidth}") }
```

**LiveData 扩展：**
```kotlin
// 自动拦截 null 值
viewModel.data.observeNonNull(this) { data -> 
    // data 必定非空
}

// SingleLiveEvent 解决数据倒灌问题（常用于 Toast/弹窗 等一次性事件）
val toastEvent = SingleLiveEvent<String>()
```

### 2.7 其他工具类

- **`ToastUtils`**：全局安全的 Toast，防止内存泄漏。
- **`DateUtils`**：时间格式化（支持转换为“刚刚”、“x分钟前”）。
- **`ScreenUtils`**：获取屏幕宽高、dp与px互相转换。

***

## 3. 本地数据库 (位于 `foundation:storage/database`)

项目集成了 **Room** 数据库，用于存储结构化数据（识别记录、AI 对话等）。所有数据库操作必须在 **IO 线程** 中执行。

### 3.1 实体与 Dao
数据库定义在 `AppDatabase` 中，包含 `RecognitionDao` 和 `ChatDao`。

### 3.2 使用示例
结合 RxJava 或者 `ThreadUtils` 进行异步读写：

```kotlin
// 插入数据示例
Single.fromCallable {
    val db = AppDatabase.getInstance(context)
    val record = RecognitionRecord(
        imagePath = "/path/to/img",
        diseaseName = "苹果黑星病",
        confidence = 0.95f,
        treatment = "..."
    )
    db.recognitionDao().insert(record)
}
.subscribeOn(Schedulers.io())
.observeOn(AndroidSchedulers.mainThread())
.subscribe({ 
    ToastUtils.showShort(context, "保存成功") 
}, { error ->
    LogUtils.e("保存失败", error)
})
```

***

## 4. 网络请求 (位于 `foundation:network`)

采用 Retrofit + RxJava 架构。
如果需要增加新的网络请求，请直接在 `ApiService.kt` 中添加接口：

```kotlin
interface ApiService {
    @GET("api/user/info")
    fun getUserInfo(): Observable<UserInfoResponse>
}
```

在业务代码中直接调用：

```kotlin
NetworkManager.api.getUserInfo()
    .subscribeOn(Schedulers.io())
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe({ response ->
        // 成功处理
    }, { error ->
        // 失败处理
    })
```

***

## 5. 调试工具 CodeLocator

项目已集成字节跳动开源的 `CodeLocator` 插件。
**如何使用：**

》 <https://github.com/bytedance/CodeLocator>

1. 确保在 Android Studio 中安装了 CodeLocator 桌面插件。
2. 运行 App（必须是 Debug 包）。
3. 点击 AS 侧边栏的 CodeLocator 面板，点击抓取。
