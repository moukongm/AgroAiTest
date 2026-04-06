# ARouter 路由配置
-keep class com.alibaba.android.arouter.** { *; }
-keep class * implements com.alibaba.android.arouter.facade.template.ISyringe { *; }
-keep class * implements com.alibaba.android.arouter.facade.template.IProvider { *; }
-keep class * implements com.alibaba.android.arouter.facade.template.IRouteGroup { *; }

# ARouter 自动生成的路由类
-keep class com.main.** { *; }
-keep class com.detection.** { *; }
-keep class com.community.** { *; }
-keep class com.user.** { *; }
-keep class com.agroai.** { *; }
