package com.detection.ui.page;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.net.Uri;
import android.os.Build;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import com.detection.viewmodel.DetectionViewModelFactory;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseActivity;
import com.common.notice.BusKey;
import com.common.notice.LiveDataBus;
import com.common.router.RouterPath;
import com.common.utils.FileUtils;
import com.common.utils.ImageLoader;
import com.common.utils.ImagePickerUtil;
import com.common.utils.LiveDataExtKt;
import com.common.utils.LogUtils;
import com.common.utils.PermissionUtils;
import com.common.utils.ThreadUtils;
import com.common.utils.ToastUtils;
import com.detection.R;
import com.detection.databinding.ActivityDetectionBinding;
import com.detection.viewmodel.DetectionViewModel;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;


@Route(path = RouterPath.DETECTION_ACTIVITY)
public class DetectionActivity extends BaseActivity<ActivityDetectionBinding> {
    private DetectionViewModel viewModel;
    private ProcessCameraProvider cameraProvider;
    private Camera camera;
    private ImagePickerUtil imagePickerUtil;
    private ImageCapture imageCapture;
    private boolean flashOn = false;
    private File selectedFile = null;

    @NonNull
    @Override
    public ActivityDetectionBinding getViewBinding() {
        return ActivityDetectionBinding.inflate(getLayoutInflater());
    }

    @Override
    public void initView() {
        // 使用 DetectionViewModelFactory 创建 ViewModel（传入 Application）
        DetectionViewModelFactory factory = new DetectionViewModelFactory(getApplication());
        viewModel = new ViewModelProvider(this, factory).get(DetectionViewModel.class);

        initCamera();
        binding.btnRecognize.setEnabled(false);
        setupClickListeners();
        observeViewModel();

        if (imagePickerUtil == null) {
            imagePickerUtil = new ImagePickerUtil(this, uri -> {
                LogUtils.INSTANCE.d("ljx",uri.toString());
                // 相册选图回调：保存文件并显示预览

                selectedFile = FileUtils.INSTANCE.uriToFile(getApplicationContext(), uri,
                        this.getCacheDir());
                LogUtils.INSTANCE.d("ljx",selectedFile+"");
                if (selectedFile == null || !selectedFile.exists()) {
                    viewModel.getLoadingState().postValue(false);
                    ToastUtils.INSTANCE.showLong(getApplicationContext(), "图片文件无效，请重新选择");
                    return null;
                }
                binding.previewView.setVisibility(View.GONE);
                showPreview(uri.toString());
                showLoading("稍等一下呢...");
                // 改为双路识别（自动判断网络状态）
                viewModel.recognizeImage(selectedFile);
                return null;
            });
        }
    }

    private void initCamera() {
        ProcessCameraProvider.getInstance(this).addListener(() -> {
            try {
                cameraProvider = ProcessCameraProvider.getInstance(this).get();
                bindCameraPreview();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCameraPreview() {
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(binding.previewView.getSurfaceProvider());

        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setFlashMode(flashOn ? ImageCapture.FLASH_MODE_ON : ImageCapture.FLASH_MODE_OFF)
                .build();

        if (cameraProvider != null) {
            cameraProvider.unbindAll();
            camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
            binding.btnRecognize.setEnabled(true);
        }
    }

    private void setupClickListeners() {
        // 返回按钮
        binding.cvBack.setOnClickListener(v -> finish());

        // 识别按钮（拍照路径：拍完会显示预览并自动识别，无需再点）
        binding.btnRecognize.setOnClickListener(v -> {

            Bitmap frozenBitmap = binding.previewView.getBitmap();
            if (frozenBitmap != null) {
                binding.ivFrozenFrame.setImageBitmap(frozenBitmap);
                binding.ivFrozenFrame.setVisibility(View.VISIBLE);
            }
            if(flashOn){
                flashOn = !flashOn;
                updateFlashState();
            }
            viewModel.takePhoto(this, imageCapture);
        });

        // 闪光灯
        binding.llFlash.setOnClickListener(v -> {
            flashOn = !flashOn;
            updateFlashState();
        });
        binding.ivHistory.setOnClickListener(v -> {
            ARouter.getInstance().build(RouterPath.DETECTION_HISTORY).navigation();
        });

        // 相册
        binding.llGallery.setOnClickListener(v -> {
            if(flashOn){
                flashOn = !flashOn;
                updateFlashState();
            }
            PermissionUtils.INSTANCE.request(this, Arrays.asList(Manifest.permission.CAMERA),
                    () -> {
                        imagePickerUtil.getStorage();
                        return null;
                    },
                    deniedList -> {
                        ToastUtils.INSTANCE.showShort(getApplicationContext(), "您拒绝了权限，功能无法使用");
                        return null;
                    });
        });
    }

    private void updateFlashState() {
        binding.ivFlash.setAlpha(flashOn ? 1.0f : 0.5f);
        // 使用 Camera 对象控制闪光灯（手电筒）
        if (camera != null) {
            camera.getCameraControl().enableTorch(flashOn);
        }
    }


    private void observeViewModel() {
        viewModel.getLoadingState().observe(this, isLoading -> {
            if (isLoading != null && isLoading) {
                showLoading("稍等一下呢...");
            } else {
                hideLoading();
            }
        });

        // 拍照完成，立即显示预览图（用户得到反馈）
        LiveDataExtKt.observeNonNull(viewModel.getPhotoCaptured(), this, photoFile -> {
            showPreview(Uri.fromFile(photoFile).toString());
            return null;
        });

        // 上传成功（显示上传后的网络图片 URL）
        LiveDataExtKt.observeNonNull(viewModel.getPhotoUriResult(), this, imageUrl -> {
            // 预览图已经是本地文件了，上传后如果需要替换成网络图可以在这里做
            return null;
        });

        // AI 识别结果，跳转结果页
        LiveDataExtKt.observeNonNull(viewModel.getChatResult(), this, res -> {
//            ToastUtils.INSTANCE.showLong(getApplicationContext(), res);
            showPreview("");
            LiveDataBus.getInstance().with(BusKey.DETECTIONHISTORY).setValue(true);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fl_dete_activity, new RecognitionResultFragment())
                    .addToBackStack(null)
                    .commit();

            return null;
        });

        // 错误提示
        LiveDataExtKt.observeNonNull(viewModel.getErrorMessage(), this, msg -> {
            ToastUtils.INSTANCE.showLong(getApplicationContext(), msg);
            showPreview("");
            return null;
        });
    }

    private void showPreview(String imageUrl) {
        binding.ivFrozenFrame.setVisibility(View.GONE);
        if(imageUrl != null && !imageUrl.isEmpty()){
            binding.previewView.setVisibility(View.GONE);
//            binding.maskView.setVisibility(View.GONE);
            binding.ivSelectedImage.setVisibility(View.VISIBLE);
            ImageLoader.INSTANCE.load(binding.ivSelectedImage, imageUrl);
        }
        else{
            binding.ivSelectedImage.setVisibility(View.GONE);
            binding.previewView.setVisibility(View.VISIBLE);
//            binding.maskView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 页面不可见时隐藏预览，节省资源
        binding.previewView.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 释放相机资源
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
            cameraProvider = null;
        }
        camera = null;
        imageCapture = null;

        // 释放图片选择器
        if (imagePickerUtil != null) {
            imagePickerUtil.release();
            imagePickerUtil = null;
        }

        // 清理 LiveDataBus
        LiveDataBus.getInstance().with(BusKey.DETECTIONHISTORY).setValue(false);

        // 清理 ImageView Bitmap
        if (binding != null && binding.ivSelectedImage != null) {
            binding.ivSelectedImage.setImageBitmap(null);
        }

        // 清理 ViewModel 引用
        viewModel = null;

        // 清理选中文件引用
        selectedFile = null;

        // 重置闪光灯状态
        flashOn = false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 如果相机还未初始化（首次进入或销毁后返回），重新初始化
        if (cameraProvider == null) {
            initCamera();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 恢复预览可见性
        if (cameraProvider != null) {
            binding.previewView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void initData() {

    }
}
