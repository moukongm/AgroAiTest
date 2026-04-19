package com.main.data;

import com.common.utils.LogUtils;
import com.network.model.LocationItem;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class CityParser {

    public static List<String> parseLocationItemsToDisplayList(List<LocationItem> items) {
        List<String> displayList = new ArrayList<>();
        if (items == null || items.isEmpty()) {
            LogUtils.INSTANCE.d("CityParser", "输入为空");
            return displayList;
        }

        LogUtils.INSTANCE.d("CityParser", "开始解析，共" + items.size() + "条数据");
        Set<String> addedCities = new LinkedHashSet<>();
        for (LocationItem item : items) {
            String name = item.getName();
            String adm1 = item.getAdm1();
            String adm2 = item.getAdm2();
            
            if (name != null && !name.isEmpty()) {
                String displayText = name;
                if (adm1 != null && !adm1.isEmpty()) {
                    displayText = name + "-" + adm1;
                }
                if (adm2 != null && !adm2.isEmpty() && !adm2.equals(adm1)) {
                    displayText = name + "-" + adm1 + "-" + adm2;
                }
                
                LogUtils.INSTANCE.d("CityParser", "添加: " + displayText);
                if (addedCities.add(displayText)) {
                    displayList.add(displayText);
                }
            }
        }
        return displayList;
    }
}
