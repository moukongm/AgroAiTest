package com.detection.ui.page;

import android.Manifest;
import android.net.Uri;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.common.base.BaseActivity;
import com.common.router.RouterPath;
import com.common.utils.FileUtils;
import com.common.utils.ImageLoader;
import com.common.utils.ImagePickerUtil;
import com.common.utils.LiveDataExtKt;
import com.common.utils.LogUtils;
import com.common.utils.PermissionUtils;
import com.common.utils.ToastUtils;
import com.detection.R;
import com.detection.databinding.ActivityDetectionBinding;
import com.detection.viewmodel.DetectionViewModel;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import eightbitlab.com.blurview.BlurView;

@Route(path = RouterPath.DETECTION_ACTIVITY)
public class DetectionActivity extends BaseActivity<ActivityDetectionBinding> {
    private DetectionViewModel viewModel;
    private ProcessCameraProvider cameraProvider;
    private ImagePickerUtil imagePickerUtil;
    private ImageCapture imageCapture;
    private boolean flashOn = false;
    // 相册选中的文件，null 表示未选
    private File selectedFile = null;

    @NonNull
    @Override
    public ActivityDetectionBinding getViewBinding() {
        return ActivityDetectionBinding.inflate(getLayoutInflater());
    }

    @Override
    public void initView() {
        viewModel = new ViewModelProvider(this).get(DetectionViewModel.class);


        initCamera();
        binding.btnRecognize.setEnabled(false);
        setupClickListeners();
        observeViewModel();

        BlurView blurView = binding.blurView;
        blurView.setupWith(binding.getRoot())
                .setBlurRadius(50f)
                .setOverlayColor(0x30FFFFFF);

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
                viewModel.uploadAndRecognizeFromGallery(getApplicationContext(), selectedFile);
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
                .build();

        if (cameraProvider != null) {
            cameraProvider.unbindAll();
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
            binding.btnRecognize.setEnabled(true);
        }
    }

    private void setupClickListeners() {
        // 返回按钮
        binding.cvBack.setOnClickListener(v -> finish());

        // 识别按钮（拍照路径：拍完会显示预览并自动识别，无需再点）
        binding.btnRecognize.setOnClickListener(v -> {
            if (selectedFile == null) {
                // 未选相册图 → 拍照，拍完自动识别
                binding.previewView.setVisibility(View.GONE);
                viewModel.takePhoto(this, imageCapture);
            } else {
                // 已选相册图 → 直接上传并识别
//                viewModel.uploadAndRecognizeFromGallery(getApplicationContext(), selectedFile);
//                selectedFile = null;
            }
        });

        // 闪光灯
        binding.llFlash.setOnClickListener(v -> {
            flashOn = !flashOn;
            updateFlashState();
        });
        binding.ivHistory.setOnClickListener(v -> {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fl_dete_activity, new HistoryFragment())
                    .addToBackStack(null)
                    .commit();
        });
        // 相册
        binding.llGallery.setOnClickListener(v -> {
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
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fl_dete_activity, new RecognitionResultFragment())
                    .addToBackStack(null)
                    .commit();
            return null;
        });

        // 错误提示
        LiveDataExtKt.observeNonNull(viewModel.getErrorMessage(), this, msg -> {
            ToastUtils.INSTANCE.showLong(getApplicationContext(), msg);
            if(msg.equals("AI 识别失败，请重试")){
                showPreview("");
            }
            return null;
        });
    }

    private void showPreview(String imageUrl) {
        if(imageUrl != null && !imageUrl.isEmpty()){
            binding.ivSelectedImage.setVisibility(View.VISIBLE);
//            binding.previewView.setVisibility(View.GONE);
            ImageLoader.INSTANCE.load(binding.ivSelectedImage, imageUrl);
        }
        else{
            binding.ivSelectedImage.setVisibility(View.GONE);
            binding.previewView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding.ivSelectedImage.setVisibility(View.GONE);
        binding.previewView.setVisibility(View.VISIBLE);
    }

    @Override
    public void initData() {
    }
}
