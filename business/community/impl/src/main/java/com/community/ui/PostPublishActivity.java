package com.community.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseActivity;
import com.common.notice.BusKey;
import com.common.notice.LiveDataBus;
import com.common.router.RouterPath;
import com.common.utils.FileUtils;
import com.common.utils.LiveDataExtKt;
import com.community.databinding.ActivityPostPublishBinding;
import com.community.ui.adapter.PublishImageAdapter;
import com.community.viewmodel.PublishPostViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Route(path = RouterPath.COMMUNITY_POST_PUBLISH)
public class PostPublishActivity extends BaseActivity<ActivityPostPublishBinding> {

    private static final int REQUEST_CODE_CROP_SELECT = 1001;
    private static final String KEY_SELECTED_CROPS = "selected_crops";

    private PublishPostViewModel viewModel;
    private PublishImageAdapter imageAdapter;
    private final List<Uri> selectedImages = new ArrayList<>();
    private String currentSelectedCrops = "";

    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(9), uris -> {
                if (uris != null && !uris.isEmpty()) {
                    int currentImageCount = imageAdapter.getRealImageCount();
                    int remainingSlots = 9 - currentImageCount;
                    if (remainingSlots > 0) {
                        int addCount = Math.min(uris.size(), remainingSlots);
                        for (int i = 0; i < addCount; i++) {
                            selectedImages.add(uris.get(i));
                        }
                        imageAdapter.setImages(selectedImages);
                    }
                }
            });

    @Override
    public ActivityPostPublishBinding getViewBinding() {
        return ActivityPostPublishBinding.inflate(getLayoutInflater());
    }

    @Override
    public void initView() {
        viewModel = new ViewModelProvider(this).get(PublishPostViewModel.class);

        binding.btnBack.setOnClickListener(v -> finish());

        imageAdapter = new PublishImageAdapter();
        binding.rvImages.setLayoutManager(new GridLayoutManager(this, 3));
        binding.rvImages.setAdapter(imageAdapter);
        binding.rvImages.setNestedScrollingEnabled(false);

        imageAdapter.initEmpty();

        imageAdapter.setOnAddClickListener(() -> {
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });

        imageAdapter.setOnDeleteClickListener(position -> {
            Object item = imageAdapter.getItem(position);
            if (item instanceof PublishImageAdapter.ImageItem) {
                Uri uriToRemove = ((PublishImageAdapter.ImageItem) item).uri;
                for (int i = selectedImages.size() - 1; i >= 0; i--) {
                    if (selectedImages.get(i).equals(uriToRemove)) {
                        selectedImages.remove(i);
                        break;
                    }
                }
                imageAdapter.setImages(selectedImages);
            }
        });

        binding.btCropChevron.setOnClickListener(v -> {
            String cropsToPass = binding.tvCropValue.getText().toString();
            if ("未选择".equals(cropsToPass)) {
                cropsToPass = "";
            }
            ARouter.getInstance()
                    .build(RouterPath.USER_CROP_SELECT_ACTIVITY)
                    .withString("returnTo", "post")
                    .withString(KEY_SELECTED_CROPS, cropsToPass)
                    .navigation(this, REQUEST_CODE_CROP_SELECT);
        });

        binding.btnConfirmPublish.setOnClickListener(v -> handlePublish());

        String crop = getIntent().getStringExtra("selected_crop");
        if (crop != null && !crop.isEmpty()) {
            binding.tvCropValue.setText(crop);
            currentSelectedCrops = crop;
        }
    }

    private void handlePublish() {
        String title = binding.etPublishTitle.getText().toString().trim();
        String content = binding.etPublishContent.getText().toString().trim();

        if (TextUtils.isEmpty(content)) {
            Toast.makeText(this, "请输入发布内容", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> tags = null;
        if (!TextUtils.isEmpty(currentSelectedCrops)) {
            tags = Arrays.asList(currentSelectedCrops.split(","));
        }

        List<File> imageFiles = new ArrayList<>();
        for (Uri uri : selectedImages) {
            File file = FileUtils.INSTANCE.uriToFile(this, uri, getExternalCacheDir());
            if (file != null) {
                imageFiles.add(file);
            }
        }

        viewModel.publishPost(TextUtils.isEmpty(title) ? null : title, content, imageFiles, tags);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CROP_SELECT && resultCode == RESULT_OK && data != null) {
            String selectedCrop = data.getStringExtra("selected_crop");
            if (selectedCrop != null && !selectedCrop.isEmpty()) {
                binding.tvCropValue.setText(selectedCrop);
                currentSelectedCrops = selectedCrop;
            } else {
                binding.tvCropValue.setText("未选择");
                currentSelectedCrops = "";
            }
        }
    }

    @Override
    public void initData() {
        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading) {
                showLoading("发布中...");
            } else {
                hideLoading();
            }
        });

        viewModel.getPublishState().observe(this, state -> {
            switch (state) {
                case PublishPostViewModel.STATE_UPLOADING:
                    binding.btnConfirmPublish.setEnabled(false);
                    binding.btnConfirmPublish.setText("上传图片中...");
                    break;
                case PublishPostViewModel.STATE_PUBLISHING:
                    binding.btnConfirmPublish.setEnabled(false);
                    binding.btnConfirmPublish.setText("发布中...");
                    break;
                case PublishPostViewModel.STATE_SUCCESS:
                    binding.btnConfirmPublish.setEnabled(true);
                    binding.btnConfirmPublish.setText("确认发布");
                    break;
                case PublishPostViewModel.STATE_ERROR:
                case PublishPostViewModel.STATE_IDLE:
                    binding.btnConfirmPublish.setEnabled(true);
                    binding.btnConfirmPublish.setText("确认发布");
                    break;
            }
        });

        viewModel.getErrorMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                viewModel.resetState();
            }
        });

        viewModel.getPublishSuccess().observe(this, post -> {
            if (post != null) {
                showPublishSuccessDialog();
                LiveDataBus.getInstance().with(BusKey.SENTPOST).setValue(true);
            }
        });
    }

    private void showPublishSuccessDialog() {
        new AlertDialog.Builder(this)
                .setMessage("上传成功")
                .setPositiveButton("好的", (dialog, which) -> {
                    dialog.dismiss();
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("refresh", true);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                })
                .setCancelable(true)
                .show();
    }
}
