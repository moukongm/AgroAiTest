package com.network.model;

import com.google.gson.annotations.SerializedName;

public class WeatherResponse {

    @SerializedName("now")
    private Now now;

    // 无参构造（Gson 反序列化需要）
    public WeatherResponse() {
    }

    public Now getNow() {
        return now;
    }

    public void setNow(Now now) {
        this.now = now;
    }

    /**
     * 当前天气信息，只包含需要的字段
     */
    public static class Now {

        @SerializedName("temp")
        private String temp;      // 温度

        @SerializedName("icon")
        private String icon;      // 天气图标代码

        @SerializedName("text")
        private String text;      // 天气状况文字描述

        public Now() {
        }

        public String getTemp() {
            return temp;
        }

        public void setTemp(String temp) {
            this.temp = temp;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }
}