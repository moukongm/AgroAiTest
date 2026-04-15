package com.common.utils

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.permissionx.guolindev.PermissionX
import com.permissionx.guolindev.callback.RequestCallback

/**
 * 权限请求工具类
 * 基于 PermissionX 封装，简化权限申请流程
 */
object PermissionUtils {

    /**
     * 在 Activity 中请求权限
     *
     * @param activity 当前 Activity
     * @param permissions 需要请求的权限列表
     * @param onGranted 权限全部授予时的回调
     * @param onDenied 权限被拒绝时的回调
     */
    fun request(
        activity: AppCompatActivity,
        permissions: List<String>,
        onGranted: () -> Unit,
        onDenied: (List<String>) -> Unit = {}
    ) {
        PermissionX.init(activity)
            .permissions(permissions)
            .onExplainRequestReason { scope, deniedList ->
                scope.showRequestReasonDialog(deniedList, "为了应用的正常运行，请授予以下权限", "确定", "取消")
            }
            .onForwardToSettings { scope, deniedList ->
                scope.showForwardToSettingsDialog(deniedList, "请在设置中手动开启以下权限", "去设置", "取消")
            }
            .request { allGranted, grantedList, deniedList ->
                if (allGranted) {
                    onGranted()
                } else {
                    onDenied(deniedList)
                }
            }
    }

    /**
     * 在 Fragment 中请求权限
     *
     * @param fragment 当前 Fragment
     * @param permissions 需要请求的权限列表
     * @param onGranted 权限全部授予时的回调
     * @param onDenied 权限被拒绝时的回调
     */
    fun request(
        fragment: Fragment,
        permissions: List<String>,
        onGranted: () -> Unit,
        onDenied: (List<String>) -> Unit = {}
    ) {
        PermissionX.init(fragment)
            .permissions(permissions)
            .onExplainRequestReason { scope, deniedList ->
                scope.showRequestReasonDialog(deniedList, "为了应用的正常运行，请授予以下权限", "确定", "取消")
            }
            .onForwardToSettings { scope, deniedList ->
                scope.showForwardToSettingsDialog(deniedList, "请在设置中手动开启以下权限", "去设置", "取消")
            }
            .request { allGranted, grantedList, deniedList ->
                if (allGranted) {
                    onGranted()
                } else {
                    onDenied(deniedList)
                }
            }
    }

    /**
     * 请求录音权限（Activity）
     * @param activity 当前 Activity
     * @param onGranted 授权成功的回调
     * @param onDenied 拒绝的回调
     */
    fun requestRecordAudio(
        activity: AppCompatActivity,
        onGranted: () -> Unit,
        onDenied: (List<String>) -> Unit = {}
    ) {
        request(activity, listOf(android.Manifest.permission.RECORD_AUDIO), onGranted, onDenied)
    }

    /**
     * 请求录音权限（Activity）- Java 调用版本
     * @param activity 当前 Activity
     * @param callback 请求回调
     */
    @JvmStatic
    fun requestRecordAudio(activity: AppCompatActivity, callback: RequestCallback) {
        PermissionX.init(activity)
            .permissions(listOf(android.Manifest.permission.RECORD_AUDIO))
            .onExplainRequestReason { scope, deniedList ->
                scope.showRequestReasonDialog(deniedList, "为了应用的正常运行，请授予以下权限", "确定", "取消")
            }
            .onForwardToSettings { scope, deniedList ->
                scope.showForwardToSettingsDialog(deniedList, "请在设置中手动开启以下权限", "去设置", "取消")
            }
            .request(callback)
    }

    /**
     * 请求录音权限（Fragment）
     * @param fragment 当前 Fragment
     * @param onGranted 授权成功的回调
     * @param onDenied 拒绝的回调
     */
    fun requestRecordAudio(
        fragment: Fragment,
        onGranted: () -> Unit,
        onDenied: (List<String>) -> Unit = {}
    ) {
        request(fragment, listOf(android.Manifest.permission.RECORD_AUDIO), onGranted, onDenied)
    }
}
