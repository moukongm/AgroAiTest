package com.main.ui.page;

import android.net.Uri;
import android.view.View;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.common.base.BaseActivity;
import com.common.router.RouterPath;
import com.common.storage.database.AppDatabase;
import com.common.storage.database.UserDao;
import com.common.storage.database.UserRecord;
import com.common.utils.ImagePickerUtil;
import com.common.utils.ImageLoader;
import com.agri.pest.client.model.response.MyCropResponseDto;
import com.common.notice.BusKey;
import com.common.notice.LiveDataBus;
import com.common.utils.LogUtils;
import com.main.impl.databinding.ActivityPlantAddBinding;
import com.main.viewmodel.PlantAddViewModel;

import java.util.concurrent.Executors;

@Route(path = RouterPath.PLANT_ADD_ACTIVITY)
public class PlantAddActivity extends BaseActivity<ActivityPlantAddBinding> {

    private PlantAddViewModel viewModel;
    private ImagePickerUtil imagePicker;
    private Uri selectedImageUri;

    @Override
    public ActivityPlantAddBinding getViewBinding() {
        return ActivityPlantAddBinding.inflate(getLayoutInflater());
    }

    @Override
    public void initView() {
        viewModel = new ViewModelProvider(this).get(PlantAddViewModel.class);

        // 加载用户头像
        loadUserAvatar();

        // 返回按钮点击事件
        binding.flPlantAddBack.setOnClickListener(v -> finish());

        imagePicker = new ImagePickerUtil(this, uri -> {
            selectedImageUri = uri;
            ImageLoader.INSTANCE.load(binding.ivPlantPhoto, uri.toString());
            binding.ivPlantAdd.setVisibility(View.GONE);
            return null;
        });

        binding.mainpagePlantPhoto.setOnClickListener(v -> {
            imagePicker.showImageSourceDialog();
        });

        binding.btnPlantAddConfirm.setOnClickListener(v -> handleConfirm());
    }

    private void loadUserAvatar() {
        Executors.newSingleThreadExecutor().execute(() -> {
            UserDao userDao = AppDatabase.Companion.getInstance(getApplicationContext()).userDao();
            UserRecord userRecord = userDao.getUserById(0);
            if (userRecord != null && userRecord.getAvatarLocalPath() != null) {
                runOnUiThread(() -> {
                    ImageLoader.INSTANCE.loadCircle(binding.ivItemSettingTitle, userRecord.getAvatarLocalPath());
                });
            }
            // 同时加载定位信息
            if (userRecord != null && userRecord.getLocation() != null) {
                runOnUiThread(() -> {
                    binding.mainpagePlantLocal.setText(userRecord.getLocation());
                });
            }
        });
    }

    private void handleConfirm() {
        if (selectedImageUri == null) {
            Toast.makeText(this, "请选择植物图片", Toast.LENGTH_SHORT).show();
            return;
        }

        String plantName = binding.etPlantAddName.getText().toString().trim();
        if (plantName.isEmpty()) {
            Toast.makeText(this, "请输入植物名称", Toast.LENGTH_SHORT).show();
            return;
        }

        viewModel.createCropWithImage(this, selectedImageUri, plantName);
    }

    @Override
    public void initData() {
        viewModel.getStateLiveData().observe(this, state -> {
            switch (state) {
                case PlantAddViewModel.STATE_UPLOADING:
                    showLoading("上传图片中...");
                    binding.btnPlantAddConfirm.setEnabled(false);
                    binding.btnPlantAddConfirm.setText("上传中...");
                    break;
                case PlantAddViewModel.STATE_CREATING:
                    showLoading("创建中...");
                    binding.btnPlantAddConfirm.setEnabled(false);
                    binding.btnPlantAddConfirm.setText("创建中...");
                    break;
                case PlantAddViewModel.STATE_IDLE:
                case PlantAddViewModel.STATE_SUCCESS:
                case PlantAddViewModel.STATE_ERROR:
                    hideLoading();
                    binding.btnPlantAddConfirm.setEnabled(true);
                    binding.btnPlantAddConfirm.setText("确认添加");
                    break;
            }
        });

        viewModel.getErrorMessageLiveData().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                hideLoading();
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                binding.btnPlantAddConfirm.setEnabled(true);
                binding.btnPlantAddConfirm.setText("确认添加");
            }
        });

        viewModel.getCropResponseLiveData().observe(this, cropResponse -> {
            if (cropResponse != null) {
                hideLoading();
                Toast.makeText(this, "添加成功", Toast.LENGTH_SHORT).show();
                // 发送事件通知 HomeFragment 刷新作物列表
                LiveDataBus.getInstance().with(BusKey.CROP_ADDED).setValue(true);
                setResult(RESULT_OK);
                finish();
            }
        });
    }
}
