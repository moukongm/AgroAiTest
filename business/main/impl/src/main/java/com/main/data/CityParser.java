package com.main.data;

import com.network.model.LocationItem;

import java.util.ArrayList;
import java.util.List;

import java.util.ArrayList;
import java.util.List;

public class CityParser {

    public static List<String> parseLocationItemsToDisplayList(List<LocationItem> items) {
        List<String> displayList = new ArrayList<>();
        if (items == null || items.isEmpty()) {
            return displayList;
        }

        for (LocationItem item : items) {
            // 获取字段值，处理 null 情况
            String name = item.getName() != null ? item.getName() : "";
            String adm2 = item.getAdm2() != null ? item.getAdm2() : "";
            String adm1 = item.getAdm1() != null ? item.getAdm1() : "";

            // 跳过所有关键字段都为空的无效数据（可选）
            if (name.isEmpty() && adm2.isEmpty() && adm1.isEmpty()) {
                continue;
            }

            // 格式：name-adm2，adm1
            String formatted = name + "-" + adm2 + "，" + adm1;
            displayList.add(formatted);
        }
        return displayList;
    }
}