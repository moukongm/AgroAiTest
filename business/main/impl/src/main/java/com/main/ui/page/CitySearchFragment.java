package com.main.ui.page;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.common.base.BaseFragment;
import com.main.impl.databinding.FragmentCitySearchBinding;
import com.main.ui.adapter.CityInfo;
import com.main.ui.adapter.CitySearchAdapter;

import java.util.ArrayList;
import java.util.List;

public class CitySearchFragment extends BaseFragment<FragmentCitySearchBinding> {

    private CitySearchAdapter adapter;
    private List<CityInfo> cityList = new ArrayList<>();
    private String searchKeyword;

    @NonNull
    @Override
    public FragmentCitySearchBinding getViewBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentCitySearchBinding.inflate(inflater, container, false);
    }

    @Override
    public void initView() {
        initRecyclerView();
        searchCity();
    }

    @Override
    public void initData() {

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
            if (getActivity() instanceof CitySelectorActivity) {
                ((CitySelectorActivity) getActivity()).onCitySelected(cityInfo.name);
            }
        });
    }

    private void searchCity() {
        if (adapter == null) {
            return;
        }
        if (searchKeyword == null || searchKeyword.isEmpty()) {
            cityList.clear();
            adapter.setNewInstance(cityList);
            showEmptyState(true);
            return;
        }

        cityList.clear();
        List<CityInfo> allCities = getMockCities();
        String keyword = searchKeyword.toLowerCase();

        for (CityInfo city : allCities) {
            if (city.name.contains(searchKeyword) ||
                city.pinyin.toLowerCase().contains(keyword)) {
                cityList.add(city);
            }
        }

        adapter.setNewInstance(cityList);
        showEmptyState(cityList.isEmpty());
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

    private List<CityInfo> getMockCities() {
        List<CityInfo> cities = new ArrayList<>();
        cities.add(new CityInfo("北京", "beijing"));
        cities.add(new CityInfo("上海", "shanghai"));
        cities.add(new CityInfo("广州", "guangzhou"));
        cities.add(new CityInfo("深圳", "shenzhen"));
        cities.add(new CityInfo("杭州", "hangzhou"));
        cities.add(new CityInfo("成都", "chengdu"));
        cities.add(new CityInfo("武汉", "wuhan"));
        cities.add(new CityInfo("西安", "xian"));
        cities.add(new CityInfo("南京", "nanjing"));
        cities.add(new CityInfo("重庆", "chongqing"));
        cities.add(new CityInfo("天津", "tianjin"));
        cities.add(new CityInfo("苏州", "suzhou"));
        cities.add(new CityInfo("长沙", "changsha"));
        cities.add(new CityInfo("郑州", "zhengzhou"));
        cities.add(new CityInfo("青岛", "qingdao"));
        cities.add(new CityInfo("沈阳", "shenyang"));
        cities.add(new CityInfo("大连", "dalian"));
        cities.add(new CityInfo("济南", "jinan"));
        cities.add(new CityInfo("哈尔滨", "haerbin"));
        cities.add(new CityInfo("长春", "changchun"));
        cities.add(new CityInfo("昆明", "kunming"));
        cities.add(new CityInfo("贵阳", "guiyang"));
        cities.add(new CityInfo("福州", "fuzhou"));
        cities.add(new CityInfo("厦门", "xiamen"));
        cities.add(new CityInfo("南昌", "nanchang"));
        cities.add(new CityInfo("合肥", "hefei"));
        cities.add(new CityInfo("石家庄", "shijiazhuang"));
        cities.add(new CityInfo("太原", "taiyuan"));
        cities.add(new CityInfo("兰州", "lanzhou"));
        cities.add(new CityInfo("乌鲁木齐", "wulumuqi"));
        cities.add(new CityInfo("呼和浩特", "huhehaote"));
        cities.add(new CityInfo("南宁", "nanning"));
        cities.add(new CityInfo("海口", "haikou"));
        cities.add(new CityInfo("银川", "yinchuan"));
        cities.add(new CityInfo("西宁", "xining"));
        cities.add(new CityInfo("拉萨", "lasa"));
        return cities;
    }
}
