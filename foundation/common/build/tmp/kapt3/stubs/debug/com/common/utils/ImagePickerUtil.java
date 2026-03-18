package com.common.utils;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000>\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0011\n\u0002\b\u0007\u0018\u00002\u00020\u0001B!\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0012\u0010\u0004\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00070\u0005\u00a2\u0006\u0002\u0010\bJ\b\u0010\u0014\u001a\u00020\u0007H\u0002J\b\u0010\u0015\u001a\u00020\u0007H\u0002J\b\u0010\u0016\u001a\u00020\u0007H\u0002J\b\u0010\u0017\u001a\u00020\u0007H\u0002J\u0006\u0010\u0018\u001a\u00020\u0007R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\t\u001a\u0004\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010\n\u001a\b\u0012\u0004\u0012\u00020\f0\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\r\u001a\u0004\u0018\u00010\u000eX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0004\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00070\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00100\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0011\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\f0\u00120\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00060\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0019"}, d2 = {"Lcom/common/utils/ImagePickerUtil;", "", "activity", "Landroidx/activity/ComponentActivity;", "onImagePicked", "Lkotlin/Function1;", "Landroid/net/Uri;", "", "(Landroidx/activity/ComponentActivity;Lkotlin/jvm/functions/Function1;)V", "cameraImageUri", "cameraPermissionLauncher", "Landroidx/activity/result/ActivityResultLauncher;", "", "dialogPick", "Landroid/app/AlertDialog;", "pickMediaLauncher", "Landroidx/activity/result/PickVisualMediaRequest;", "storagePermissionLauncher", "", "takePictureLauncher", "checkCameraPermissionAndLaunch", "launchCamera", "launchImagePicker", "requestStoragePermissions", "showImageSourceDialog", "common_debug"})
public final class ImagePickerUtil {
    @org.jetbrains.annotations.NotNull
    private final androidx.activity.ComponentActivity activity = null;
    @org.jetbrains.annotations.NotNull
    private final kotlin.jvm.functions.Function1<android.net.Uri, kotlin.Unit> onImagePicked = null;
    @org.jetbrains.annotations.Nullable
    private android.net.Uri cameraImageUri;
    @org.jetbrains.annotations.Nullable
    private android.app.AlertDialog dialogPick;
    @org.jetbrains.annotations.NotNull
    private final androidx.activity.result.ActivityResultLauncher<java.lang.String> cameraPermissionLauncher = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.activity.result.ActivityResultLauncher<java.lang.String[]> storagePermissionLauncher = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.activity.result.ActivityResultLauncher<androidx.activity.result.PickVisualMediaRequest> pickMediaLauncher = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.activity.result.ActivityResultLauncher<android.net.Uri> takePictureLauncher = null;
    
    public ImagePickerUtil(@org.jetbrains.annotations.NotNull
    androidx.activity.ComponentActivity activity, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super android.net.Uri, kotlin.Unit> onImagePicked) {
        super();
    }
    
    /**
     * 显示选择图片来源的对话框（相机或相册）
     */
    public final void showImageSourceDialog() {
    }
    
    private final void checkCameraPermissionAndLaunch() {
    }
    
    private final void launchCamera() {
    }
    
    private final void requestStoragePermissions() {
    }
    
    private final void launchImagePicker() {
    }
}