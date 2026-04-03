package com.main.ui.page;

import android.Manifest;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.amap.api.location.AMapLocationClient;
import com.common.base.BaseFragment;
import com.common.router.RouterPath;
import com.common.utils.ImageLoader;
import com.common.utils.LogUtils;
import com.common.utils.PermissionUtils;
import com.main.Utils;
import com.main.impl.databinding.ActivityHomeBinding;
import com.main.viewmodel.HomeViewModel;
import com.network.model.AlertResponse;

import java.util.Arrays;

@Route(path = RouterPath.HOME_FRAGMENT)
public class HomeFragment extends BaseFragment<ActivityHomeBinding> {

    private HomeViewModel viewModel;
    ActivityHomeBinding binding;

    @NonNull
    @Override
    public ActivityHomeBinding getViewBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return ActivityHomeBinding.inflate(inflater, container, false);
    }

    @Override
    public void initView() {

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        binding=  getBinding();


        viewModel.getUserNameLiveData().observe(getViewLifecycleOwner(), userName -> {
            if (userName != null && !userName.isEmpty()) {
                binding.mainpageUsername.setText(userName);
            }
        });

        viewModel.getAvatarUrlLiveData().observe(getViewLifecycleOwner(), avatarUrl -> {
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                ImageLoader.INSTANCE.loadCircle(binding.ivSettingTitle, avatarUrl);
            }
        });

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
        viewModel.getAlertLiveData().observe(getViewLifecycleOwner(), alerts -> {
            if (alerts != null && !alerts.isEmpty()) {
                AlertResponse.Alert first = alerts.get(0);
                String title = "【" + (first.getColor() != null ? Utils.colorEnToZh( first.getColor().getCode()): "") +
                        (first.getEventType() != null ? first.getEventType().getName() : "") +
                        "预警警报】";
                LogUtils.INSTANCE.d("lyy", title);
                binding.mainpageWarningTitle.setText(title);
                binding.tvWarnning.setText(first.getDescription());
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
    public void initData() {
        viewModel.getLocation();
        viewModel.getLocationLivedata().observe(getViewLifecycleOwner(), city -> {
            LogUtils.INSTANCE.d("lyy",city);
            if (city != null && !city.isEmpty()) {
                binding.mainpagePlacename.setText(city);
            }
        });
        PermissionUtils.INSTANCE.request(this, Arrays.asList(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        ), () -> {
            LogUtils.INSTANCE.d("ljx", "定位");
            viewModel.getLocation(getActivity().getApplicationContext());
            return null;
        }, deniedList -> {
            LogUtils.INSTANCE.d("ljx", "定位权限被拒绝");
            return null;
        });
        viewModel.getUserInfo();

    }
}