package com.common.router

/**
 * ARouter 路由路径常量定义
 * 统一管理所有页面的跳转路径
 */
object RouterPath {
    // 首页模块 (Main Module)
    const val MAIN_ACTIVITY = "/main/activity"
    const val MAIN_SERVICE = "/main/service"   // 首页对外服务接口
    const val HOME_FRAGMENT = "/home/fragment" // 首页 Fragment
    const val MESSAGE_FRAGMENT = "/main/message_fragment" // 消息 Fragment
    const val NOTICE_MESSAGE_FRAGMENT = "/main/notice_message" // 通知消息页面
    const val NOTICE_CONTENT_FRAGMENT = "/main/notice_content" // 通知内容页面（ViewPager2 内容页）

    // 调试模块 (Demo Module)
    const val DEBUG_DEMO_ACTIVITY = "/demo/debug_demo" // 调试工具页面
    const val RV_DEMO_ACTIVITY = "/demo/rv_demo"       // 列表工具页面
    const val SPEECH_DEMO_ACTIVITY = "/demo/speech_demo" // 语音测试页面

    // 检测模块 (Detection Module)
    const val DETECTION_ACTIVITY = "/detection/activity" // 识别检测页面
    const val DETECTION_SERVICE = "/detection/service"   // 检测服务接口
    const val DETECTION_HISTORY = "/detection/history" // 识别历史记录页面
    const val HISTORY_COUNT_SERVICE = "/detection/history_count" // 历史记录数量服务


    // 社区模块 (Community Module)
    const val COMMUNITY_ACTIVITY = "/community/activity" // 社区主页
    const val COMMUNITY_POST_DETAIL = "/community/post_detail" // 帖子详情
    const val COMMUNITY_SERVICE = "/community/service"   // 社区服务接口

    // 用户模块 (User Module)
    const val USER_LOGIN_ACTIVITY = "/user/login"     // 登录页面
    const val USER_PROFILE_ACTIVITY = "/user/profile" // 个人中心页面
    const val USER_EDIT_PROFILE_ACTIVITY = "/user/edit_profile" // 编辑资料页面
    const val USER_SETTING_PROFILE_ACTIVITY = "/user/setting_profile" // 设置页面
    const val USER_FAVORITE_POST_ACTIVITY = "/user/favorite_post" // 我的收藏页面
    const val USER_SERVICE = "/user/service"          // 用户服务接口
    const val USER_PROFILE_SERVICE = "/user/profile_service" // 用户资料服务（跨模块通信）
    const val TOKEN_SERVICE = "/user/token"

    const val APP_MAIN_ACTIVITY = "/app/activity"
}
