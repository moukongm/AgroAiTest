package com.detection;

import com.common.utils.LogUtils;

public class Utils {
    public static String extractDiseaseName(String text) {
        // 优先匹配：识别出的病虫害名称：XXX（XXX）
        int idx = text.indexOf("识别出的病虫害名称**\n  ");
        LogUtils.INSTANCE.d("ljx", idx + "");
        if (idx != -1) {
            int start = idx + "识别出的病虫害名称**\n  ".length();
            LogUtils.INSTANCE.d("ljx", start + "");
            // 找冒号
            for (int i = start; i < text.length() && i < start + 10; i++) {
                String name = text.substring(i).trim();
                // 去掉括号内容
                int bracketIdx = name.indexOf("（");
                if (bracketIdx == -1) bracketIdx = name.indexOf("(");
                if (bracketIdx != -1) {
                    name = name.substring(0, bracketIdx).trim();
                }
                LogUtils.INSTANCE.d("ljx", name + bracketIdx + i);
                return name;

            }
        }
        return "无法识别";
    }
}
