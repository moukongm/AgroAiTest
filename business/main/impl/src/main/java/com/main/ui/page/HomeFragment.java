package com.main.ui.page;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.agri.pest.client.model.response.MessageResponseDto;
import com.agri.pest.client.model.response.PageResultMessageResponseDto;
import com.agri.pest.client.model.response.MyCropResponseDto;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseFragment;
import com.common.notice.BusKey;
import com.common.notice.LiveDataBus;
import com.common.router.RouterPath;
import com.common.storage.MMKVUtils;
import com.common.storage.database.AppDatabase;
import com.common.storage.database.UserDao;
import com.common.storage.database.UserRecord;
import com.common.utils.ImageLoader;
import com.common.utils.LogUtils;
import com.common.utils.PermissionUtils;
import com.main.Utils;
import com.main.impl.R;
import com.main.impl.databinding.ActivityHomeBinding;
import com.main.ui.adapter.MyCropAdapter;
import com.common.storage.database.CropRecord;
import com.main.viewmodel.HomeViewModel;
import com.network.model.AlertResponse;

import java.util.Arrays;
import java.util.concurrent.Executors;

@Route(path = RouterPath.HOME_FRAGMENT)
public class HomeFragment extends BaseFragment<ActivityHomeBinding> {

    private HomeViewModel viewModel;
    ActivityHomeBinding binding;
    private MyCropAdapter cropAdapter;


    private static final int REQUEST_PLANT_ADD = 1001;

    @NonNull
    @Override
    public ActivityHomeBinding getViewBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return ActivityHomeBinding.inflate(inflater, container, false);
    }

    @Override
    public void initView() {

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        viewModel.initContext(requireContext());
        binding=  getBinding();

        // 初始化RecyclerView
        cropAdapter = new MyCropAdapter();
        binding.recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recycler.setAdapter(cropAdapter);



        viewModel.getUserNameLiveData().observe(getViewLifecycleOwner(), userName -> {
            if (userName != null && !userName.isEmpty()) {
                LogUtils.INSTANCE.d("init",userName);
                binding.mainpageUsername.setText(userName);
            }
        });

        viewModel.getAvatarUrlLiveData().observe(getViewLifecycleOwner(), avatarUrl -> {
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                ImageLoader.INSTANCE.loadCircle(binding.ivSettingTitle, avatarUrl);
            }
        });
        viewModel.getHistoryCountLiveData().observe(getViewLifecycleOwner(), avatarUrl -> {
            if (avatarUrl != null ) {
                String s = String.valueOf(avatarUrl);
               binding.mianpageCnt.setText("共"+s+"次识别记录");
            }
        });
        LiveDataBus.getInstance().with(BusKey.DETECTIONHISTORY)
                .observe(getViewLifecycleOwner(),ob->{
                    viewModel.getUserInfo();
                });
// 监听离线模式，设置 HomeFragment 的 iv_settingTitle 头像
        viewModel.getIsOfflineModeLiveData().observe(getViewLifecycleOwner(), isOffline -> {
            if (isOffline) {
                // 离线模式：从 UserRecord 数据库获取本地头像
                String avatarPath = null;
                UserRecord userRecord = viewModel.getUserRecordLiveData().getValue();
                if (userRecord != null && userRecord.getAvatarLocalPath() != null) {
                    avatarPath = userRecord.getAvatarLocalPath();
                }
                if (avatarPath != null && !avatarPath.isEmpty()) {
                    ImageLoader.INSTANCE.loadCircle(binding.ivSettingTitle, avatarPath);
                }
                String username = null;
                username = MMKVUtils.INSTANCE.custom("user_module").getString("username","");
                if(username != null){
                    getBinding().mainpageUsername.setText(username);
                    LogUtils.INSTANCE.d("usermodel",username);
                }
                // 禁用添加作物功能
                binding.mainpagePlantPhoto.setEnabled(false);
                binding.mainpagePlantPhoto.setAlpha(0.5f);
            } else {
                // 在线模式：恢复添加作物功能
                binding.mainpagePlantPhoto.setEnabled(true);
                binding.mainpagePlantPhoto.setAlpha(1.0f);
            }
        });

        // 监听用户头像本地路径，设置到作物列表
        viewModel.getUserRecordLiveData().observe(getViewLifecycleOwner(), userRecord -> {
            if (userRecord != null) {
                if (userRecord.getAvatarLocalPath() != null) {
                    cropAdapter.setUserAvatarPath(userRecord.getAvatarLocalPath());
                    ImageLoader.INSTANCE.loadCircle(binding.ivItemSettingTitle, userRecord.getAvatarLocalPath());
                }
                // 同时更新定位信息
                if (userRecord.getLocation() != null) {
                    cropAdapter.setUserLocation(userRecord.getLocation());
                }
            }
        });

        viewModel.getLocationLivedata().observe(getViewLifecycleOwner(), city -> {
            LogUtils.INSTANCE.d("lyy++", city);
            if (city != null && !city.isEmpty()) {
                binding.mainpagePlacename.setText(city);
                binding.mainpagePlantLocal.setText(city);
                cropAdapter.setUserLocation(city);
            }
        });

        // 监听天气信息
        viewModel.getWeatherLiveData().observe(getViewLifecycleOwner(), now -> {
            if (now != null) {
                LogUtils.INSTANCE.d("ljx","whynot");
                binding.mainpageTemp.setText(now.getTemp());
                binding.mainpageWeather.setText(now.getText());
                binding.mainpageWeatherIcon.setImageResource(Utils.handleicon(now.getIcon()));
            }
        });

        binding.mainpageTime.setText(Utils.getTodayLunar());


        binding.llTakephoto.setOnClickListener(v -> {
            PermissionUtils.INSTANCE.request(this, Arrays.asList(Manifest.permission.CAMERA),
                    () -> {
                        ARouter.getInstance().build(RouterPath.DETECTION_ACTIVITY).navigation();
                        return null;
                    },
                    deniedList -> {
                        Toast.makeText(requireContext(),"您拒绝了权限，功能无法使用", Toast.LENGTH_SHORT).show();
                        return null;
                    });
        });

        binding.mainpagePlantPhoto.setOnClickListener(v -> {
            ARouter.getInstance()
                    .build(RouterPath.PLANT_ADD_ACTIVITY)
                    .navigation(requireActivity(), REQUEST_PLANT_ADD);
        });
        binding.mainpageWarningRight.setOnClickListener(v -> {
            binding.mainpageWarningRight.setVisibility(View.GONE);
            binding.mainpageWarningGoneright.setVisibility(View.VISIBLE);
            binding.mainpageWarningHistory.setVisibility(View.VISIBLE);
            binding.tvWarnning.setMaxLines(100);
        });
        binding.mainpageWarningGoneright.setOnClickListener(v -> {
            binding.mainpageWarningRight.setVisibility(View.VISIBLE);
            binding.mainpageWarningGoneright.setVisibility(View.GONE);
            binding.mainpageWarningHistory.setVisibility(View.GONE);
            binding.tvWarnning.setMaxLines(2);
        });
        binding.mainpageHistory.setOnClickListener(v -> {
            LogUtils.INSTANCE.d("ljx","history");
           ARouter.getInstance().build(RouterPath.DETECTION_HISTORY).navigation();

        });

        viewModel.getAlertLiveData().observe(getViewLifecycleOwner(), alerts -> {
            LogUtils.INSTANCE.d("lyy", alerts+"");
            if (alerts != null) {
                String title = "【" +  alerts.getType() +
                        "】";
                binding.mainpageWarningTitle.setText(title);
                binding.tvWarnning.setText(alerts.getContent());
                binding.consHaveWarn.setVisibility(View.VISIBLE);
                binding.tvNothaveWarn.setVisibility(View.GONE);
                binding.consNothaveWarn.setVisibility(View.GONE);

            } else {
                binding.consHaveWarn.setVisibility(View.GONE);
                binding.tvNothaveWarn.setVisibility(View.VISIBLE);
                binding.consNothaveWarn.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        //viewModel.getMyCrops();
    }

    @Override
    public void initData() {
        PermissionUtils.INSTANCE.request(this, Arrays.asList(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        ), () -> {
            LogUtils.INSTANCE.d("ljx", "定位");
            viewModel.getLocation(getActivity().getApplicationContext());
            return null;
        }, deniedList -> {
            LogUtils.INSTANCE.d("ljx", "定位权限被拒绝");
            // 权限被拒绝，仍然从数据库加载上次的定位
            viewModel.loadLocationFromDb();
            return null;
        });
        viewModel.getUserInfo();


        LiveDataBus.getInstance().with(BusKey.PROFILE_CHANGED).observe(getViewLifecycleOwner(),object->{
            LogUtils.INSTANCE.d("init","home");
            if(object instanceof Boolean){
                Boolean b = (Boolean) object;
                if(b){
                    viewModel.getUserInfo();
                    viewModel.getMyCrops();
                }
            }
        });

        // 监听作物添加成功事件
        LiveDataBus.getInstance().with(BusKey.CROP_ADDED).observe(getViewLifecycleOwner(), added -> {
            if (added != null) {
                viewModel.getMyCrops();
            }
        });

        viewModel.getMyCrops();
        viewModel.getCropListLiveData().observe(getViewLifecycleOwner(), crops -> {
            if (crops != null) {
                // 获取用户头像路径并一起设置
                String avatarPath = null;
                // 优先从 UserRecord 获取本地路径
                UserRecord userRecord = viewModel.getUserRecordLiveData().getValue();
                if (userRecord != null && userRecord.getAvatarLocalPath() != null) {
                    avatarPath = userRecord.getAvatarLocalPath();
                }
                cropAdapter.setDataWithUserAvatar(crops, avatarPath);
            }
        });

        viewModel.getLocalCropListLiveData().observe(getViewLifecycleOwner(), crops -> {
            if (cropAdapter.getItemCount() > 0) {
                return;
            }
            if (crops != null && viewModel.getIsOfflineModeLiveData().getValue()) {
                // 仅离线模式下才显示本地数据
                cropAdapter.setLocalData(crops);
            }
        });

        // 监听是否离线模式
        viewModel.getIsOfflineModeLiveData().observe(getViewLifecycleOwner(), isOffline -> {
            if (isOffline) {
                // 离线模式：禁用删除和点击功能
                cropAdapter.setDeleteEnabled(false);
                cropAdapter.setCropClickEnabled(false);
            } else {
                // 在线模式：恢复功能
                cropAdapter.setDeleteEnabled(true);
                cropAdapter.setCropClickEnabled(true);
            }
        });
        cropAdapter.setOnDeleteClickListener((crop, position) -> {
            if (crop instanceof MyCropResponseDto) {
                MyCropResponseDto dto = (MyCropResponseDto) crop;
                new AlertDialog.Builder(requireContext())
                        .setTitle("删除确认")
                        .setMessage("确定要删除 \"" + dto.getPlantName() + "\" 吗？")
                        .setPositiveButton("删除", (dialog, which) -> {
                            viewModel.deleteCrop(dto.getId());
                        })
                        .setNegativeButton("取消", null)
                        .show();
            }
        });

        // RecyclerView 子项点击跳转到 PlantManageActivity
        cropAdapter.setOnCropClickListener(crop -> {
            long plantId = -1;
            // 在线模式下确保只处理 MyCropResponseDto
            if (crop instanceof MyCropResponseDto) {
                plantId = ((MyCropResponseDto) crop).getId();
                if (plantId > 0) {
                    ARouter.getInstance()
                            .build(RouterPath.PLANT_MANAGE_ACTIVITY)
                            .withLong("plantId", plantId)
                            .navigation();
                }
            }
        });

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtils.INSTANCE.d("PlantAddActivity", "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);
        if (requestCode == REQUEST_PLANT_ADD && resultCode == RESULT_OK) {
            // 添加成功，刷新作物列表
            viewModel.getMyCrops();
        }
    }

    public void stopLocationIfNeeded() {
        if (viewModel != null) {
            viewModel.stopLocation();
        }
    }
}
