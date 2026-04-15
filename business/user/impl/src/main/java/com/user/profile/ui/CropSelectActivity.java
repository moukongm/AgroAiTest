package com.user.profile.ui;

import android.content.Intent;
import android.os.Bundle;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseActivity;
import com.common.router.RouterPath;
import com.user.databinding.ActivityCropSelectBinding;
import com.user.profile.ui.page.GuideFragment;

@Route(path = RouterPath.USER_CROP_SELECT_ACTIVITY)
public class CropSelectActivity extends BaseActivity<ActivityCropSelectBinding> {

    @Override
    public ActivityCropSelectBinding getViewBinding() {
        return ActivityCropSelectBinding.inflate(getLayoutInflater());
    }

    @Override
    public void initView() {
        String returnTo = getIntent().getStringExtra("returnTo");
        String selectedCrops = getIntent().getStringExtra("selected_crops");

        GuideFragment guideFragment = new GuideFragment();
        Bundle args = new Bundle();
        args.putString("selected_crops", selectedCrops);
        args.putString("returnTo", returnTo);
        guideFragment.setArguments(args);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(binding.fragmentContainer.getId(), guideFragment)
                .commit();

        getSupportFragmentManager().setFragmentResultListener("crop_select_result", this, (requestKey, result) -> {
            if ("main".equals(returnTo) || "login".equals(returnTo)) {
                navigateToMain();
            } else if ("post".equals(returnTo)) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("selected_crop", result.getString("selected_crop"));
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("selected_crop", result.getString("selected_crop"));
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
    }

    private void navigateToMain() {
        ARouter.getInstance()
                .build(RouterPath.APP_MAIN_ACTIVITY)
                .navigation(this);
        finish();
    }

    @Override
    public void initData() {
    }
}
