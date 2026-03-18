package com.common.utils;

/**
 * 权限请求工具类
 * 基于 PermissionX 封装，简化权限申请流程
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002JF\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\b2\f\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u00040\u000b2\u001a\b\u0002\u0010\f\u001a\u0014\u0012\n\u0012\b\u0012\u0004\u0012\u00020\t0\b\u0012\u0004\u0012\u00020\u00040\rJF\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u000e\u001a\u00020\u000f2\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\b2\f\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u00040\u000b2\u001a\b\u0002\u0010\f\u001a\u0014\u0012\n\u0012\b\u0012\u0004\u0012\u00020\t0\b\u0012\u0004\u0012\u00020\u00040\r\u00a8\u0006\u0010"}, d2 = {"Lcom/common/utils/PermissionUtils;", "", "()V", "request", "", "activity", "Landroidx/appcompat/app/AppCompatActivity;", "permissions", "", "", "onGranted", "Lkotlin/Function0;", "onDenied", "Lkotlin/Function1;", "fragment", "Landroidx/fragment/app/Fragment;", "common_debug"})
public final class PermissionUtils {
    @org.jetbrains.annotations.NotNull
    public static final com.common.utils.PermissionUtils INSTANCE = null;
    
    private PermissionUtils() {
        super();
    }
    
    /**
     * 在 Activity 中请求权限
     *
     * @param activity 当前 Activity
     * @param permissions 需要请求的权限列表
     * @param onGranted 权限全部授予时的回调
     * @param onDenied 权限被拒绝时的回调
     */
    public final void request(@org.jetbrains.annotations.NotNull
    androidx.appcompat.app.AppCompatActivity activity, @org.jetbrains.annotations.NotNull
    java.util.List<java.lang.String> permissions, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onGranted, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super java.util.List<java.lang.String>, kotlin.Unit> onDenied) {
    }
    
    /**
     * 在 Fragment 中请求权限
     *
     * @param fragment 当前 Fragment
     * @param permissions 需要请求的权限列表
     * @param onGranted 权限全部授予时的回调
     * @param onDenied 权限被拒绝时的回调
     */
    public final void request(@org.jetbrains.annotations.NotNull
    androidx.fragment.app.Fragment fragment, @org.jetbrains.annotations.NotNull
    java.util.List<java.lang.String> permissions, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onGranted, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super java.util.List<java.lang.String>, kotlin.Unit> onDenied) {
    }
}