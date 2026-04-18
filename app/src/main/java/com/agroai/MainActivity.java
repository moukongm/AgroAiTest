package com.agroai;

import android.content.Intent;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.agroai.databinding.ActivityMainBinding;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.amap.api.location.AMapLocationClient;
import com.common.NavigationController;
import com.common.base.BaseActivity;
import com.common.notice.BusKey;
import com.common.notice.LiveDataBus;
import com.common.router.RouterPath;
import com.common.storage.MMKVUtils;
import com.common.utils.LogUtils;
import com.community.ui.CommunityFragment;
import com.community.ui.ElderCommunityFragment;
import com.main.ui.page.ElderHomeFragment;
import com.main.ui.page.ElderMessageFragment;
import com.main.ui.page.MessageFragment;
import com.main.ui.page.HomeFragment;
import com.user.profile.ui.page.ElderProfileFragment;
import com.user.profile.ui.page.ProfileFragment;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;

@Route(path = RouterPath.APP_MAIN_ACTIVITY)
public class MainActivity extends BaseActivity<ActivityMainBinding>
        implements NavigationController {

    private CommunityFragment communityFragment;

    private HomeFragment homeFragment;

    private ProfileFragment profileFragment;

    private MessageFragment messageFragment;
    private ElderCommunityFragment elderCommunityFragment;

    private ElderHomeFragment elderHomeFragment;

    private ElderProfileFragment elderProfileFragment;

    private ElderMessageFragment elderMessageFragment;

    private Fragment activeFragment;

    private boolean isA11yMode = false;

    public ActivityMainBinding getViewBinding() {
        return ActivityMainBinding.inflate(getLayoutInflater());
    }

    @Override
    public void initView() {
        setupFragments();
        if (!isA11yMode) {
            setupBottomNavigation();
        }
        setupBlurEffect();
    }

    @Override
    public void initData() {
        // 告知用户隐私政策是否展示（参数依次为 context, 是否显示隐私弹窗, 是否显示隐私弹窗的详情）
        AMapLocationClient.updatePrivacyShow(this, true, true);
        // 告知用户隐私政策是否同意（参数：context, 是否同意）
        AMapLocationClient.updatePrivacyAgree(this, true);

        LiveDataBus.getInstance().with(BusKey.UNLOGIN)
                .observe(this,observer ->{
                    LogUtils.INSTANCE.d("unlogin","wai");
                    if(observer instanceof Boolean){
                        Boolean b = (Boolean) observer;
                        if(b){
                            LogUtils.INSTANCE.d("unlogin","li");
                            finish();
                            ARouter.getInstance().build(RouterPath.USER_LOGIN_ACTIVITY).navigation();
                        }
                    }
                });
        
        LiveDataBus.getInstance().with(BusKey.ACCESSIBILITY_MODE_CHANGED)
                .observe(this, isA11y -> {
                    if (isA11y instanceof Boolean && (Boolean) isA11y) {
                        switchToA11yMode();
                    } else {
                        switchToNormalMode();
                    }
                });
    }

    private void setupFragments() {
        isA11yMode = MMKVUtils.INSTANCE.custom("user_module").getString("selected_mode","normal").equals("senior");

        // 先清理可能存在的旧 Fragment
        clearAllFragments();

        if (isA11yMode) {
            switchToA11yMode();
        } else {
            switchToNormalMode();
        }
    }
    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                switchFragment(isA11yMode() ? elderHomeFragment : homeFragment);
                return true;
            } else if (itemId == R.id.nav_community) {
                switchFragment(isA11yMode() ? elderCommunityFragment : communityFragment);
                return true;
            } else if (itemId == R.id.nav_message) {
                switchFragment(isA11yMode() ? elderMessageFragment : messageFragment);
                return true;
            } else if (itemId == R.id.nav_profile) {
                switchFragment(isA11yMode() ? elderProfileFragment : profileFragment);
                return true;
            }
            return false;
        });
        binding.bottomNavigation.setSelectedItemId(R.id.nav_home);
    }

    private boolean isA11yMode() {
        return isA11yMode;
    }

    private void switchFragment(Fragment fragment) {
        if (fragment != null && fragment != activeFragment) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            if (activeFragment != null) {
                transaction.hide(activeFragment);
            }
            transaction.show(fragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
            activeFragment = fragment;
        }
    }
    private void setupBlurEffect() {
        BlurView blurView = binding.blurView;
        ViewGroup rootView = (ViewGroup) getWindow().getDecorView().findViewById(android.R.id.content);
        float radius = 15f;
        blurView.setupWith(rootView, new RenderScriptBlur(this))
                .setBlurRadius(radius)
                .setOverlayColor(0x33FFFFFF);
    }
    public void hideBottomNavigation() {
        binding.navContainer.setVisibility(View.GONE);
    }

    public void showBottomNavigation() {
        binding.navContainer.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        // 停止定位，防止 HomeViewModel 中的 AMapLocationClient 泄漏
        if (homeFragment != null) {
            homeFragment.stopLocationIfNeeded();
        }
        super.onDestroy();
    }

    private void switchToA11yMode() {
        isA11yMode = true;
        clearAllFragments();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        elderHomeFragment = new ElderHomeFragment();
        elderCommunityFragment = new ElderCommunityFragment();
        elderMessageFragment = new ElderMessageFragment();
        elderProfileFragment = new ElderProfileFragment();

        transaction.add(R.id.fragment_container, elderProfileFragment, "elder_profile")
                .hide(elderProfileFragment);
        transaction.add(R.id.fragment_container, elderMessageFragment, "elder_message")
                .hide(elderMessageFragment);
        transaction.add(R.id.fragment_container, elderCommunityFragment, "elder_community")
                .hide(elderCommunityFragment);
        transaction.add(R.id.fragment_container, elderHomeFragment, "elder_home");

        transaction.commit();

        activeFragment = elderHomeFragment;
        binding.bottomNavigation.switchMode(true);
        setupBottomNavigation();
        binding.bottomNavigation.setSelectedItemId(R.id.nav_home);
    }

    private void switchToNormalMode() {
        isA11yMode = false;
        clearAllFragments();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        homeFragment = new HomeFragment();
        communityFragment = new CommunityFragment();
        messageFragment = new MessageFragment();
        profileFragment = new ProfileFragment();

        transaction.add(R.id.fragment_container, profileFragment, "profile")
                .hide(profileFragment);
        transaction.add(R.id.fragment_container, messageFragment, "message")
                .hide(messageFragment);
        transaction.add(R.id.fragment_container, communityFragment, "community")
                .hide(communityFragment);
        transaction.add(R.id.fragment_container, homeFragment, "home");

        transaction.commit();

        activeFragment = homeFragment;
        binding.bottomNavigation.switchMode(false);
        setupBottomNavigation();
        binding.bottomNavigation.setSelectedItemId(R.id.nav_home);
    }

    private void clearAllFragments() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();

        Fragment oldHome = fm.findFragmentByTag("home");
        Fragment oldCommunity = fm.findFragmentByTag("community");
        Fragment oldMessage = fm.findFragmentByTag("message");
        Fragment oldProfile = fm.findFragmentByTag("profile");
        Fragment oldElderHome = fm.findFragmentByTag("elder_home");
        Fragment oldElderCommunity = fm.findFragmentByTag("elder_community");
        Fragment oldElderMessage = fm.findFragmentByTag("elder_message");
        Fragment oldElderProfile = fm.findFragmentByTag("elder_profile");

        if (oldHome != null) transaction.remove(oldHome);
        if (oldCommunity != null) transaction.remove(oldCommunity);
        if (oldMessage != null) transaction.remove(oldMessage);
        if (oldProfile != null) transaction.remove(oldProfile);
        if (oldElderHome != null) transaction.remove(oldElderHome);
        if (oldElderCommunity != null) transaction.remove(oldElderCommunity);
        if (oldElderMessage != null) transaction.remove(oldElderMessage);
        if (oldElderProfile != null) transaction.remove(oldElderProfile);

        transaction.commitAllowingStateLoss();

        homeFragment = null;
        communityFragment = null;
        messageFragment = null;
        profileFragment = null;
        elderHomeFragment = null;
        elderCommunityFragment = null;
        elderMessageFragment = null;
        elderProfileFragment = null;
        activeFragment = null;
    }

}
