package com.user.login.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseActivity;
import com.common.router.RouterPath;
import com.common.utils.ToastUtils;
import com.user.databinding.ActivityModeSelectBinding;
import com.user.login.data.UserStorageConstant;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

@Route(path = RouterPath.USER_MODE_SELECT_ACTIVITY)
public class ModeSelectActivity extends BaseActivity<ActivityModeSelectBinding> {

    private String selectedMode = null;
    private ActivityResultLauncher<Intent> cropSelectLauncher;

    @Override
    public ActivityModeSelectBinding getViewBinding() {
        return ActivityModeSelectBinding.inflate(getLayoutInflater());
    }

    @Override
    public void initView() {
        binding.ivOld.setOnClickListener(v -> {
            selectedMode = UserStorageConstant.KEY_MODE_SENIOR;
            updateSelection();
            ToastUtils.INSTANCE.showShort(this,"您已选择适老化模式");
        });

        binding.ivYoung.setOnClickListener(v -> {
            selectedMode = UserStorageConstant.KEY_MODE_NORMAL;
            updateSelection();
            ToastUtils.INSTANCE.showShort(this,"您已选择普通模式");
        });

        binding.btnConfirm.setOnClickListener(v -> {
            if (selectedMode != null) {
                UserStorageConstant.saveElderlyModeSelected(true);
                UserStorageConstant.saveSelectedMode(selectedMode);
                navigateToCropSelect();
            }
        });
    }

    private void updateSelection() {
        binding.btnConfirm.setEnabled(selectedMode != null);
        binding.btnConfirm.setAlpha(selectedMode != null ? 1.0f : 0.5f);
    }

    private void navigateToCropSelect() {
        cropSelectLauncher.launch(new Intent(this, com.user.profile.ui.CropSelectActivity.class)
                .putExtra("standalone", true)
                .putExtra("returnTo", "guide"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cropSelectLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                }
        );
    }

    @Override
    public void initData() {
    }
}
