package com.main.ui.page;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.common.base.BaseFragment;
import com.common.utils.LogUtils;
import com.main.data.CityParser;
import com.main.impl.databinding.FragmentCitySearchBinding;
import com.main.ui.adapter.CityInfo;
import com.main.ui.adapter.CitySearchAdapter;
import com.main.viewmodel.HomeViewModel;

import java.util.ArrayList;
import java.util.List;

public class CitySearchFragment extends BaseFragment<FragmentCitySearchBinding> {

    private CitySearchAdapter adapter;
    private String searchKeyword;
    private HomeViewModel viewModel;
    private OnCitySelectedListener citySelectedListener;

    public interface OnCitySelectedListener {
        void onCitySelected(String cityName);
    }

    public void setOnCitySelectedListener(OnCitySelectedListener listener) {
        this.citySelectedListener = listener;
    }

    @NonNull
    @Override
    public FragmentCitySearchBinding getViewBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentCitySearchBinding.inflate(inflater, container, false);
    }

    @Override
    public void initView() {
        // 使用 Activity 作为 ViewModelStoreOwner，让 CitySearchFragment 和 CityDefaultFragment 共享同一个 ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

        initRecyclerView();

        // 监听搜索结果
        viewModel.getCityLiveData().observe(getViewLifecycleOwner(), results -> {
            LogUtils.INSTANCE.d("CitySearch", "收到结果数量: " + (results == null ? "null" : results.size()));
            if (results == null || results.isEmpty()) {
                showEmptyState(true);
                adapter.setList(new ArrayList<>());
            } else {
                showEmptyState(false);
                for (int i = 0; i < Math.min(3, results.size()); i++) {
                    LogUtils.INSTANCE.d("CitySearch", "结果" + i + ": name=" + results.get(i).getName() 
                        + ", adm1=" + results.get(i).getAdm1() + ", adm2=" + results.get(i).getAdm2());
                }
                List<String> displayList = CityParser.parseLocationItemsToDisplayList(results);
                adapter.setList(displayList);
            }
        });

        // 监听错误
        viewModel.getCityError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                showEmptyState(true);
            }
        });
    }

    @Override
    public void initData() {
        // 首次进入时执行搜索
        if (searchKeyword != null && !searchKeyword.isEmpty()) {
            searchCity();
        }
    }

    public void setSearchKeyword(String keyword) {
        this.searchKeyword = keyword;
        searchCity();
    }

    private void initRecyclerView() {
        adapter = new CitySearchAdapter();
        getBinding().rvSearchResult.setLayoutManager(new LinearLayoutManager(requireContext()));
        getBinding().rvSearchResult.setAdapter(adapter);

        adapter.setOnItemClickListener(cityInfo -> {
            // 只传递 name（第一个 - 之前的部分）
            String name = cityInfo.split("-")[0];
            viewModel.updateLocation(name);
            // 通过回调通知 Activity 切换回默认页面
            if (citySelectedListener != null) {
                citySelectedListener.onCitySelected(name);
            }
        });
    }

    private void searchCity() {
        if (adapter == null) {
            return;
        }
        if (searchKeyword == null || searchKeyword.isEmpty()) {
            showEmptyState(true);
            return;
        }

        viewModel.getCity(searchKeyword);
    }

    private void showEmptyState(boolean show) {
        if (show) {
            getBinding().rvSearchResult.setVisibility(View.GONE);
            getBinding().llEmpty.setVisibility(View.VISIBLE);
        } else {
            getBinding().rvSearchResult.setVisibility(View.VISIBLE);
            getBinding().llEmpty.setVisibility(View.GONE);
        }
    }

}
