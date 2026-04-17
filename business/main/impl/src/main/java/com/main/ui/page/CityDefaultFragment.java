package com.main.ui.page;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.common.base.BaseFragment;
import com.main.impl.databinding.FragmentCityDefaultBinding;
import com.main.ui.adapter.CityListAdapter;
import com.main.ui.adapter.LetterNavAdapter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CityDefaultFragment extends BaseFragment<FragmentCityDefaultBinding> {

    private CityListAdapter cityListAdapter;
    private LetterNavAdapter letterNavAdapter;
    private List<String> hotCityList = new ArrayList<>();
    private List<String> letterList = new ArrayList<>();
    private Map<String, List<String>> cityDataMap = new LinkedHashMap<>();

    @NonNull
    @Override
    public FragmentCityDefaultBinding getViewBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentCityDefaultBinding.inflate(inflater, container, false);
    }

    @Override
    public void initView() {
        initRecyclerView();
        initData();
        initListeners();
    }

    private void initRecyclerView() {
        // 城市列表（主列表）
        cityListAdapter = new CityListAdapter();
        getBinding().rvCityList.setLayoutManager(new LinearLayoutManager(requireContext()));
        getBinding().rvCityList.setAdapter(cityListAdapter);

        // 右侧字母导航
        letterNavAdapter = new LetterNavAdapter();
        getBinding().rvLetterNav.setLayoutManager(new LinearLayoutManager(requireContext()));
        getBinding().rvLetterNav.setAdapter(letterNavAdapter);

        // 列表滚动监听，同步右侧字母高亮
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

    private String getLetterAtPosition(int position) {
        int currentPos = 0;
        // 跳过热门城市头部
        currentPos++;
        for (int i = 0; i < hotCityList.size(); i++) {
            currentPos++;
        }
        if (position < currentPos) {
            return "";
        }
        position -= currentPos;
        
        for (Map.Entry<String, List<String>> entry : cityDataMap.entrySet()) {
            if (position == 0) {
                return entry.getKey();
            }
            position--;
            if (position < entry.getValue().size()) {
                return entry.getKey();
            }
            position -= entry.getValue().size();
        }
        return null;
    }

    public void initData() {
        // 热门城市
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

        // 字母列表
        letterList.clear();
        for (char c = 'A'; c <= 'Z'; c++) {
            letterList.add(String.valueOf(c));
        }
        letterNavAdapter.setNewInstance(letterList);

        // 城市数据
        cityDataMap.clear();
        cityDataMap.put("A", createList("阿坝藏族羌族自治州", "阿克苏地区", "阿拉尔", "阿拉善盟", "阿勒泰地区", "安康", "安庆", "鞍山", "安顺", "安阳"));
        cityDataMap.put("B", createList("八师", "巴彦淖尔", "巴音郭楞蒙古自治州", "巴中", "白城", "白沙黎族自治县", "白山", "白银", "保定", "宝鸡", "保山", "北海", "北京", "北屯", "本溪", "毕节", "滨州", "亳州"));
        cityDataMap.put("C", createList("沧州", "昌都", "昌吉回族自治州", "长春", "常德", "常熟", "常州", "朝阳", "潮州", "成都", "承德", "池州", "赤峰", "崇左", "滁州", "楚雄彝族自治州"));
        cityDataMap.put("D", createList("达州", "大理白族自治州", "大连", "大庆", "大同", "大兴安岭", "丹东", "儋州", "德宏傣族景颇族自治州", "德州", "迪庆藏族自治州", "定安", "定西", "东方", "东莞", "东营", "都江堰"));
        cityDataMap.put("E", createList("鄂尔多斯", "鄂州", "恩施土家族苗族自治州"));
        cityDataMap.put("F", createList("防城港", "佛山", "抚顺", "抚州", "阜新", "阜阳"));
        cityDataMap.put("G", createList("甘孜藏族自治州", "赣州", "固原", "广安", "广元", "广州", "贵港", "贵阳", "桂林", "果洛藏族自治州"));
        cityDataMap.put("H", createList("哈尔滨", "哈密", "海北藏族自治州", "海东", "海口", "海南藏族自治州", "海西蒙古族藏族自治州", "邯郸", "汉中", "杭州", "毫州", "合肥", "合江", "合作", "和田地区", "河池", "河源", "菏泽", "贺州", "鹤壁", "鹤岗", "黑河", "衡水", "衡阳", "红河哈尼族彝族自治州", "呼和浩特", "湖州", "葫芦岛", "怀化", "淮安", "淮北", "淮南", "黄冈", "黄山", "黄石", "惠州", "鸡西", "吉林", "吉安", "济南", "济宁", "佳木斯", "嘉兴", "嘉峪关", "江门", "焦作", "揭阳", "金昌", "金华", "锦州", "晋城", "晋中", "荆州", "荆门", "景德镇", "九江", "酒泉", "喀什地区", "开封"));
        cityDataMap.put("K", createList("克拉玛依", "克孜勒苏柯尔克孜自治州", "昆明", "昆山"));
        cityDataMap.put("L", createList("拉萨", "来宾", "莱芜", "兰州", "廊坊", "乐山", "丽水", "丽江", "连云港", "辽阳", "辽源", "聊城", "林芝", "临沧", "临汾", "临夏回族自治州", "临沂", "柳州", "六安", "六盘水", "龙岩", "陇南", "娄底", "泸州", "洛阳", "漯河"));
        cityDataMap.put("M", createList("马鞍山", "茂名", "眉山", "梅州", "绵阳", "牡丹江"));
        cityDataMap.put("N", createList("南昌", "南充", "南京", "南宁", "南平", "南通", "南阳", "内江", "宁波", "宁德", "怒江傈僳族自治州"));
        cityDataMap.put("P", createList("盘锦", "攀枝花", "平顶山", "平凉", "萍乡", "莆田", "濮阳"));
        cityDataMap.put("Q", createList("齐齐哈尔", "七台河", "秦皇岛", "青岛", "清远", "庆阳", "曲靖", "衢州", "泉州"));
        cityDataMap.put("R", createList("日喀则", "日照", "荣成"));
        cityDataMap.put("S", createList("三门峡", "三明", "三亚", "山南", "汕头", "汕尾", "商洛", "商丘", "上饶", "韶关", "绍兴", "邵阳", "沈阳", "十堰", "石家庄", "石河子", "石嘴山", "双鸭山", "朔州", "四平", "松原", "苏州", "宿迁", "宿州", "绥化", "随州", "遂宁"));
        cityDataMap.put("T", createList("塔城地区", "台州", "太原", "泰安", "泰州", "唐山", "天津", "天水", "铁岭", "通化", "通辽", "铜川", "铜陵", "铜仁", "吐鲁番"));
        cityDataMap.put("W", createList("威海", "潍坊", "渭南", "温州", "乌海", "武汉", "乌兰察布", "乌鲁木齐", "吴忠", "芜湖", "五家渠", "武威", "梧州"));
        cityDataMap.put("X", createList("西安", "西宁", "西双版纳傣族自治州", "锡林郭勒盟", "厦门", "咸宁", "咸阳", "湘潭", "湘西土家族苗族自治州", "襄阳", "孝感", "忻州", "新乡", "新余", "信阳", "兴安盟", "邢台", "徐州", "许昌", "宣城", "雅安", "烟台", "延安", "延边朝鲜族自治州", "盐城", "扬州", "阳江", "阳泉", "伊春", "伊犁哈萨克自治州", "宜宾", "宜昌", "宜春", "益阳", "银川", "鹰潭", "营口", "永州", "榆林", "玉林", "玉树藏族自治州", "玉溪", "岳阳", "云浮", "运城"));
        cityDataMap.put("Z", createList("枣庄", "湛江", "张家界", "张家口", "张掖", "漳州", "昭通", "肇庆", "镇江", "郑州", "中山", "中卫", "周口", "株洲", "珠海", "驻马店", "淄博", "自贡", "资阳", "遵义"));

        cityListAdapter.setData(hotCityList, letterList, cityDataMap);
    }

    private List<String> createList(String... items) {
        List<String> list = new ArrayList<>();
        for (String item : items) {
            list.add(item);
        }
        return list;
    }

    private void initListeners() {
        // 字母导航点击
        letterNavAdapter.setOnItemClickListener(letter -> {
            scrollToLetter(letter);
        });

        // 城市点击
        cityListAdapter.setOnCityClickListener(cityName -> {
            if (getActivity() instanceof CitySelectorActivity) {
                ((CitySelectorActivity) getActivity()).onCitySelected(cityName);
            }
        });
    }

    private void scrollToLetter(String letter) {
        int position = getPositionForLetter(letter);
        if (position >= 0) {
            ((LinearLayoutManager) getBinding().rvCityList.getLayoutManager()).scrollToPositionWithOffset(position, 0);
        }
    }

    private int getPositionForLetter(String letter) {
        int position = 0;
        // 热门城市头部 + 热门城市数量
        position++;
        position += hotCityList.size();
        
        // 遍历城市数据找对应字母
        for (Map.Entry<String, List<String>> entry : cityDataMap.entrySet()) {
            if (entry.getKey().equals(letter)) {
                return position;
            }
            position++; // 字母header
            position += entry.getValue().size(); // 城市数量
        }
        return -1;
    }
}
