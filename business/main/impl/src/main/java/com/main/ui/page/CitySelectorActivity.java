package com.main.ui.page;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.common.base.BaseActivity;
import com.common.router.RouterPath;
import com.main.impl.R;
import com.main.impl.databinding.ActivityCitySelectorBinding;

@Route(path = RouterPath.CITY_SELECTOR_ACTIVITY)
public class CitySelectorActivity extends BaseActivity<ActivityCitySelectorBinding> {

    private CityDefaultFragment defaultFragment;
    private CitySearchFragment searchFragment;
    private boolean isInSearchMode = false;

    @Override
    public ActivityCitySelectorBinding getViewBinding() {
        return ActivityCitySelectorBinding.inflate(getLayoutInflater());
    }

    @Override
    public void initView() {
        setupToolbar();
        setupSearchInput();
        setupBackHandler();
        showDefaultFragment();
    }

    private void setupToolbar() {
        binding.ivBack.setOnClickListener(v -> {
            if (isInSearchMode) {
                // 在搜索模式时，返回到默认页面
                binding.etSearch.setText("");
            } else {
                finish();
            }
        });
    }

    private void setupSearchInput() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    showSearchFragment(s.toString());
                    isInSearchMode = true;
                } else {
                    showDefaultFragment();
                    isInSearchMode = false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

    }

    private void setupBackHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isInSearchMode) {
                    // 在搜索模式时，返回到默认页面
                    binding.etSearch.setText("");
                } else {
                    // 退出 Activity
                    finish();
                }
            }
        });
    }

    private void showDefaultFragment() {
        if (defaultFragment == null) {
            defaultFragment = new CityDefaultFragment();
        }
        switchFragment(defaultFragment);
        binding.tvTitle.setText("选择城市");
    }

    private void showSearchFragment(String keyword) {
        if (searchFragment == null) {
            searchFragment = new CitySearchFragment();
            searchFragment.setOnCitySelectedListener(cityName -> {
                // 搜索页选择城市后，切换回默认页面
                showDefaultFragment();
                binding.etSearch.setText("");
                isInSearchMode = false;
            });
        }
        searchFragment.setSearchKeyword(keyword);
        switchFragment(searchFragment);
        binding.tvTitle.setText("搜索结果");
    }

    private void switchFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(binding.flContent.getId(), fragment);
        transaction.commit();
    }

    public void onCitySelected(String cityName) {
        setResult(RESULT_OK, getIntent().putExtra("city_name", cityName));
        finish();
    }

    @Override
    protected void onDestroy() {
        searchFragment = null;
        defaultFragment = null;
        super.onDestroy();
    }

    @Override
    public void initData() {

    }
}
