package com.common.router

/**
 * ARouter 路由路径常量定义
 * 统一管理所有页面的跳转路径
 */
object RouterPath {
    // 首页模块 (Main Module)
    const val MAIN_ACTIVITY = "/main/activity" // 首页 Activity
    const val MAIN_SERVICE = "/main/service"   // 首页对外服务接口

    // 调试模块 (Demo Module)
    const val DEBUG_DEMO_ACTIVITY = "/demo/debug_demo" // 调试工具页面
    const val RV_DEMO_ACTIVITY = "/demo/rv_demo"       // 列表工具页面
    const val SPEECH_DEMO_ACTIVITY = "/demo/speech_demo" // 语音测试页面

    // 检测模块 (Detection Module)
    const val DETECTION_ACTIVITY = "/detection/activity" // 识别检测页面
    const val DETECTION_SERVICE = "/detection/service"   // 检测服务接口

    // 社区模块 (Community Module)
    const val COMMUNITY_ACTIVITY = "/community/activity" // 社区主页
    const val COMMUNITY_SERVICE = "/community/service"   // 社区服务接口

    // 用户模块 (User Module)
    const val USER_LOGIN_ACTIVITY = "/user/login"     // 登录页面
    const val USER_PROFILE_ACTIVITY = "/user/profile" // 个人中心页面
    const val USER_SERVICE = "/user/service"          // 用户服务接口
}
