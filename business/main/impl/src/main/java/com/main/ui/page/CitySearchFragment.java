package com.main.ui.page;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.common.base.BaseFragment;
import com.common.notice.BusKey;
import com.common.notice.LiveDataBus;
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

    @NonNull
    @Override
    public FragmentCitySearchBinding getViewBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentCitySearchBinding.inflate(inflater, container, false);
    }

    @Override
    public void initView() {
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        initRecyclerView();
        searchCity();
    }

    @Override
    public void initData() {

        viewModel.getCityLiveData().observe(this,results -> {
            if (results == null || results.isEmpty()) {
                showEmptyState(true);
                LogUtils.INSTANCE.d("citysearch",results.toString());
                adapter.setNewInstance(new ArrayList<>());
            } else {
                showEmptyState(false);
                LogUtils.INSTANCE.d("citysearch",results.toString());
                List<String> displayList = CityParser.parseLocationItemsToDisplayList(results);
                adapter.setNewInstance(displayList);
            }
        });

        viewModel.getUpdateLocationResult().observe(this,city -> {
            if (city != null && !city.isEmpty()) {
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
                LiveDataBus.getInstance().with(BusKey.SEARCH_LOCATION).setValue(true);
            }
        });

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
            viewModel.updateLocation(cityInfo);
        });
    }

    private void searchCity() {
        if (adapter == null) {
            return;
        }
        if (searchKeyword == null || searchKeyword.isEmpty()) {
            viewModel.getCity("");
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
