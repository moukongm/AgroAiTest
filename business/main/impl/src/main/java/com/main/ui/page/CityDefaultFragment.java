package com.main.ui.page;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.common.base.BaseFragment;
import com.common.notice.BusKey;
import com.common.notice.LiveDataBus;
import com.common.utils.LogUtils;
import com.main.impl.databinding.FragmentCityDefaultBinding;
import com.main.ui.adapter.CityListAdapter;
import com.main.ui.adapter.HotCityRowAdapter;
import com.main.ui.adapter.LetterNavAdapter;
import com.main.viewmodel.HomeViewModel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CityDefaultFragment extends BaseFragment<FragmentCityDefaultBinding> {

    private CityListAdapter cityListAdapter;
    private LetterNavAdapter letterNavAdapter;
    private HotCityRowAdapter hotCityRowAdapter;
    private HomeViewModel viewModel;

    private List<String> hotCityList = new ArrayList<>();
    private List<String> letterList = new ArrayList<>();
    private Map<String, List<String>> cityDataMap = null;

    @NonNull
    @Override
    public FragmentCityDefaultBinding getViewBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentCityDefaultBinding.inflate(inflater, container, false);
    }

    @Override
    public void initView() {
        viewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        showLoading("加载定位中...");
        viewModel.getUserInfo();

        observeLiveData();
        initCityDataAsync();
    }

    /**
     * 异步加载城市数据，不卡主线程
     */
    private void initCityDataAsync() {
        new Thread(() -> {
            letterList = new ArrayList<>();
            for (char c = 'A'; c <= 'Z'; c++) {
                letterList.add(String.valueOf(c));
            }

            cityDataMap = new LinkedHashMap<>();
            cityDataMap.put("A", makeList("阿坝藏族羌族自治州", "阿克苏地区", "阿拉尔", "阿拉善盟", "阿勒泰地区", "安康", "安庆", "鞍山", "安顺", "安阳"));
            cityDataMap.put("B", makeList("八师", "巴彦淖尔", "巴音郭楞蒙古自治州", "巴中", "白城", "白沙黎族自治县", "白山", "白银", "保定", "宝鸡", "保山", "北海", "北京", "北屯", "本溪", "毕节", "滨州", "亳州"));
            cityDataMap.put("C", makeList("沧州", "昌都", "昌吉回族自治州", "长春", "常德", "常熟", "常州", "朝阳", "潮州", "成都", "承德", "池州", "赤峰", "崇左", "滁州", "楚雄彝族自治州"));
            cityDataMap.put("D", makeList("达州", "大理白族自治州", "大连", "大庆", "大同", "大兴安岭", "丹东", "儋州", "德宏傣族景颇族自治州", "德州", "迪庆藏族自治州", "定安", "定西", "东方", "东莞", "东营", "都江堰"));
            cityDataMap.put("E", makeList("鄂尔多斯", "鄂州", "恩施土家族苗族自治州"));
            cityDataMap.put("F", makeList("防城港", "佛山", "抚顺", "抚州", "阜新", "阜阳"));
            cityDataMap.put("G", makeList("甘孜藏族自治州", "赣州", "固原", "广安", "广元", "广州", "贵港", "贵阳", "桂林", "果洛藏族自治州"));
            cityDataMap.put("H", makeList("哈尔滨", "哈密", "海北藏族自治州", "海东", "海口", "海南藏族自治州", "海西蒙古族藏族自治州", "邯郸", "汉中", "杭州", "毫州", "合肥", "合江", "合作", "和田地区", "河池", "河源", "菏泽", "贺州", "鹤壁", "鹤岗", "黑河", "衡水", "衡阳", "红河哈尼族彝族自治州", "呼和浩特", "湖州", "葫芦岛", "怀化", "淮安", "淮北", "淮南", "黄冈", "黄山", "黄石", "惠州", "鸡西", "吉林", "吉安", "济南", "济宁", "佳木斯", "嘉兴", "嘉峪关", "江门", "焦作", "揭阳", "金昌", "金华", "锦州", "晋城", "晋中", "荆州", "荆门", "景德镇", "九江", "酒泉", "喀什地区", "开封"));
            cityDataMap.put("K", makeList("克拉玛依", "克孜勒苏柯尔克孜自治州", "昆明", "昆山"));
            cityDataMap.put("L", makeList("拉萨", "来宾", "莱芜", "兰州", "廊坊", "乐山", "丽水", "丽江", "连云港", "辽阳", "辽源", "聊城", "林芝", "临沧", "临汾", "临夏回族自治州", "临沂", "柳州", "六安", "六盘水", "龙岩", "陇南", "娄底", "泸州", "洛阳", "漯河"));
            cityDataMap.put("M", makeList("马鞍山", "茂名", "眉山", "梅州", "绵阳", "牡丹江"));
            cityDataMap.put("N", makeList("南昌", "南充", "南京", "南宁", "南平", "南通", "南阳", "内江", "宁波", "宁德", "怒江傈僳族自治州"));
            cityDataMap.put("P", makeList("盘锦", "攀枝花", "平顶山", "平凉", "萍乡", "莆田", "濮阳"));
            cityDataMap.put("Q", makeList("齐齐哈尔", "七台河", "秦皇岛", "青岛", "清远", "庆阳", "曲靖", "衢州", "泉州"));
            cityDataMap.put("R", makeList("日喀则", "日照", "荣成"));
            cityDataMap.put("S", makeList("三门峡", "三明", "三亚", "山南", "汕头", "汕尾", "商洛", "商丘", "上饶", "韶关", "绍兴", "邵阳", "沈阳", "十堰", "石家庄", "石河子", "石嘴山", "双鸭山", "朔州", "四平", "松原", "苏州", "宿迁", "宿州", "绥化", "随州", "遂宁"));
            cityDataMap.put("T", makeList("塔城地区", "台州", "太原", "泰安", "泰州", "唐山", "天津", "天水", "铁岭", "通化", "通辽", "铜川", "铜陵", "铜仁", "吐鲁番"));
            cityDataMap.put("W", makeList("威海", "潍坊", "渭南", "温州", "乌海", "武汉", "乌兰察布", "乌鲁木齐", "吴忠", "芜湖", "五家渠", "武威", "梧州"));
            cityDataMap.put("X", makeList("西安", "西宁", "西双版纳傣族自治州", "锡林郭勒盟", "厦门", "咸宁", "咸阳", "湘潭", "湘西土家族苗族自治州", "襄阳", "孝感", "忻州", "新乡", "新余", "信阳", "兴安盟", "邢台", "徐州", "许昌", "宣城", "雅安", "烟台", "延安", "延边朝鲜族自治州", "盐城", "扬州", "阳江", "阳泉", "伊春", "伊犁哈萨克自治州", "宜宾", "宜昌", "宜春", "益阳", "银川", "鹰潭", "营口", "永州", "榆林", "玉林", "玉树藏族自治州", "玉溪", "岳阳", "云浮", "运城"));
            cityDataMap.put("Z", makeList("枣庄", "湛江", "张家界", "张家口", "张掖", "漳州", "昭通", "肇庆", "镇江", "郑州", "中山", "中卫", "周口", "株洲", "珠海", "驻马店", "淄博", "自贡", "资阳", "遵义"));

            if (isAdded() && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    initRecyclerView();
                    initDataInternal();
                    initListeners();
                    hideLoading();
                });
            }
        }).start();
    }

    private void observeLiveData() {
        viewModel.getLocationLivedata().observe(getViewLifecycleOwner(), city -> {
            hideLoading();
            if (city != null && !city.isEmpty()) {
                getBinding().tvLocationCity.setText(city);
            }
        });

        LiveDataBus.getInstance().with(BusKey.LOCATION_CITY).observe(getViewLifecycleOwner(), city -> {
            hideLoading();
            if (city != null && !((String) city).isEmpty()) {
                getBinding().tvLocationCity.setText((String) city);
            }
        });

        viewModel.getUpdateLocationResult().observe(getViewLifecycleOwner(), city -> {
            if (city != null && !city.isEmpty()) {
                LiveDataBus.getInstance().with(BusKey.LOCATION).setValue(city + "_" + System.currentTimeMillis());
                getBinding().tvCurrentCity.setText(city);
            }
        });

        getBinding().llCurrentLocation.setOnClickListener(v -> {
            if (viewModel != null) {
                showLoading("定位中...");
                viewModel.getLocation(requireContext());
            }
        });
    }

    private void initRecyclerView() {
        // 热门城市
        hotCityRowAdapter = new HotCityRowAdapter();
        getBinding().rvHotCity.setLayoutManager(new LinearLayoutManager(requireContext()));
        getBinding().rvHotCity.setAdapter(hotCityRowAdapter);
        hotCityRowAdapter.setOnCityClickListener(cityName -> viewModel.updateLocation(cityName));

        // 字母导航
        letterNavAdapter = new LetterNavAdapter();
        getBinding().rvLetterNav.setLayoutManager(new GridLayoutManager(requireContext(), 6));
        getBinding().rvLetterNav.setAdapter(letterNavAdapter);
        getBinding().rvLetterNav.setItemAnimator(null);

        // 城市列表
        cityListAdapter = new CityListAdapter();
        getBinding().rvCityList.setLayoutManager(new LinearLayoutManager(requireContext()));
        getBinding().rvCityList.setAdapter(cityListAdapter);

        getBinding().rvCityList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int firstVisible = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                if (firstVisible >= 0) {
                    String letter = getLetterAtPosition(firstVisible);
                    if (letter != null) {
                        letterNavAdapter.setSelectedLetter(letter);
                    }
                }
            }
        });
    }

    /**
     * 真正的初始化数据，只有适配器初始化完成后才调用
     */
    private void initDataInternal() {
        hotCityList.clear();
        hotCityList.add("北京");
        hotCityList.add("上海");
        hotCityList.add("广州");
        hotCityList.add("深圳");
        hotCityList.add("杭州");
        hotCityList.add("成都");
        hotCityList.add("武汉");
        hotCityList.add("西安");
        hotCityList.add("南京");
        hotCityList.add("重庆");
        hotCityList.add("天津");
        hotCityList.add("苏州");

        hotCityRowAdapter.setData(hotCityList);
        letterNavAdapter.setNewInstance(letterList);
        letterNavAdapter.setSelectedLetter("A");
        cityListAdapter.setData(letterList, cityDataMap);
    }

    private void initListeners() {
        letterNavAdapter.setOnItemClickListener(letter -> {
            letterNavAdapter.setSelectedLetter(letter);
            scrollToLetter(letter);
        });

        cityListAdapter.setOnCityClickListener(cityName -> {
            viewModel.updateLocation(cityName);
            hotCityRowAdapter.setSelectedCity("");
            scrollToTop();
        });
    }

    private String getLetterAtPosition(int position) {
        int currentPos = 0;
        for (Map.Entry<String, List<String>> entry : cityDataMap.entrySet()) {
            if (position == currentPos) return entry.getKey();
            currentPos++;
            if (position < currentPos + entry.getValue().size()) return entry.getKey();
            currentPos += entry.getValue().size();
        }
        return null;
    }

    private void scrollToTop() {
        getBinding().rvCityList.scrollToPosition(0);
        ViewGroup parent = (ViewGroup) getBinding().rvCityList.getParent();
        while (parent != null && !(parent instanceof androidx.core.widget.NestedScrollView)) {
            parent = (ViewGroup) parent.getParent();
        }
        if (parent instanceof androidx.core.widget.NestedScrollView) {
            ((androidx.core.widget.NestedScrollView) parent).smoothScrollTo(0, 0);
        }
    }

    private void scrollToLetter(String letter) {
        int position = getPositionForLetter(letter);
        if (position < 0) return;

        androidx.core.widget.NestedScrollView nsv = null;
        ViewGroup parent = (ViewGroup) getBinding().rvCityList.getParent();
        while (parent != null && !(parent instanceof androidx.core.widget.NestedScrollView)) {
            parent = (ViewGroup) parent.getParent();
        }
        if (parent instanceof androidx.core.widget.NestedScrollView) {
            nsv = (androidx.core.widget.NestedScrollView) parent;
        }

        if (nsv == null) {
            LinearLayoutManager lm = (LinearLayoutManager) getBinding().rvCityList.getLayoutManager();
            if (lm != null) lm.scrollToPositionWithOffset(position, 0);
            return;
        }

        final int finalPosition = position;
        final androidx.core.widget.NestedScrollView finalNsv = nsv;
        LinearLayoutManager lm = (LinearLayoutManager) getBinding().rvCityList.getLayoutManager();
        if (lm != null) lm.scrollToPositionWithOffset(position, 0);

        getBinding().rvCityList.postDelayed(() -> {
            int totalHeight = 0;
            LinearLayoutManager layoutManager = (LinearLayoutManager) getBinding().rvCityList.getLayoutManager();
            if (layoutManager != null) {
                for (int i = 0; i < finalPosition; i++) {
                    View view = layoutManager.findViewByPosition(i);
                    if (view != null) totalHeight += view.getHeight();
                }
            }

            int[] listLocation = new int[2];
            int[] nsvLocation = new int[2];
            getBinding().rvCityList.getLocationInWindow(listLocation);
            finalNsv.getLocationInWindow(nsvLocation);
            int targetScrollY = totalHeight + (listLocation[1] - nsvLocation[1]) - 20;
            finalNsv.smoothScrollTo(0, Math.max(0, targetScrollY));

            getBinding().rvCityList.postDelayed(() -> {
                LinearLayoutManager manager = (LinearLayoutManager) getBinding().rvCityList.getLayoutManager();
                if (manager != null) manager.scrollToPositionWithOffset(finalPosition, 0);
            }, 300);
        }, 200);
    }

    private int getPositionForLetter(String letter) {
        int position = 0;
        for (Map.Entry<String, List<String>> entry : cityDataMap.entrySet()) {
            if (entry.getKey().equals(letter)) return position;
            position++;
            position += entry.getValue().size();
        }
        return -1;
    }

    private static List<String> makeList(String... items) {
        List<String> list = new ArrayList<>(items.length);
        for (String item : items) list.add(item);
        return list;
    }

    // 空实现，避免 BaseFragment 自动调用导致空指针
    @Override
    public void initData() {
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .remove(this)
                        .commitAllowingStateLoss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}