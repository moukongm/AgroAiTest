package com.detection;

public class Utils {
    public static String extractDiseaseName(String text) {
        // 优先匹配：识别出的病虫害名称：XXX（XXX）
        int idx = text.indexOf("识别出的病虫害名称");
        if (idx != -1) {
            int start = idx + "识别出的病虫害名称".length();
            // 找冒号
            for (int i = start; i < text.length() && i < start + 10; i++) {
                if (text.charAt(i) == '：' || text.charAt(i) == ':') {
                    String name = text.substring(i + 1).trim();
                    // 去掉括号内容
                    int bracketIdx = name.indexOf("（");
                    if (bracketIdx == -1) bracketIdx = name.indexOf("(");
                    if (bracketIdx != -1) {
                        name = name.substring(0, bracketIdx).trim();
                    }
                    return name;
                }
            }
        }
        return "";
    }
}
