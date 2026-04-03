package com.agroai;

import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.agroai.databinding.ActivityMainBinding;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.amap.api.location.AMapLocationClient;
import com.common.base.BaseActivity;
import com.common.router.RouterPath;
import com.community.ui.CommunityFragment;
import com.main.MessageFragment;
import com.main.ui.page.HomeFragment;
import com.user.profile.ui.page.ProfileFragment;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;

@Route(path = RouterPath.APP_MAIN_ACTIVITY)
public class MainActivity extends BaseActivity<ActivityMainBinding>
        implements CommunityFragment.OnNavigationControlListener {

    private CommunityFragment communityFragment;

    private HomeFragment homeFragment;

    private ProfileFragment profileFragment;

    private MessageFragment messageFragment;

    private Fragment activeFragment;

    public ActivityMainBinding getViewBinding() {
        return ActivityMainBinding.inflate(getLayoutInflater());
    }

    @Override
    public void initView() {
        setupFragments();
        setupBottomNavigation();
        setupBlurEffect();
    }

    @Override
    public void initData() {
// 告知用户隐私政策是否展示（参数依次为 context, 是否显示隐私弹窗, 是否显示隐私弹窗的详情）
        AMapLocationClient.updatePrivacyShow(this, true, true);
        // 告知用户隐私政策是否同意（参数：context, 是否同意）
        AMapLocationClient.updatePrivacyAgree(this, true);
    }

    private void setupFragments() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        homeFragment = new HomeFragment();

        communityFragment = new CommunityFragment();

        profileFragment = new ProfileFragment();

        messageFragment = new MessageFragment();

        transaction.add(R.id.fragment_container, profileFragment, "profile")
                .hide(profileFragment);
        transaction.add(R.id.fragment_container, messageFragment, "message")
                .hide(messageFragment);
        transaction.add(R.id.fragment_container, communityFragment, "community")
                .hide(communityFragment);
        transaction.add(R.id.fragment_container, homeFragment, "home");

        transaction.commit();

        activeFragment = homeFragment;
    }
    private void setupBottomNavigation() {
        binding.bottomNavigation.setItemIconTintList(null);
        binding.bottomNavigation.setItemRippleColor(null);
        binding.bottomNavigation.setItemActiveIndicatorEnabled(false);
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                switchFragment(homeFragment);
                return true;
            } else if (itemId == R.id.nav_community) {
                switchFragment(communityFragment);
                return true;
            } else if (itemId == R.id.nav_message) {
                switchFragment(messageFragment);
                return true;
            } else if (itemId == R.id.nav_profile) {
                switchFragment(profileFragment);
                return true;
            }
            return false;
        });
        binding.bottomNavigation.setSelectedItemId(R.id.nav_home);
    }
    private void switchFragment(Fragment fragment) {
        if (fragment != activeFragment) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.hide(activeFragment)
                    .show(fragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
            activeFragment = fragment;
        }
    }
    private void setupBlurEffect() {
        BlurView blurView = binding.blurView;

        ViewGroup rootView = (ViewGroup) getWindow().getDecorView().findViewById(android.R.id.content);

        blurView.setupWith(rootView, new RenderScriptBlur(this))
                .setBlurRadius(15f)
                .setOverlayColor(0x33FFFFFF);
    }
    public void hideBottomNavigation() {
        binding.navContainer.setVisibility(View.GONE);
    }

    public void showBottomNavigation() {
        binding.navContainer.setVisibility(View.VISIBLE);
    }
}
