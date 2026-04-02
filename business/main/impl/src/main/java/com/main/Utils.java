package com.main;

import android.icu.util.ChineseCalendar;
import android.util.Log;

import com.main.impl.R;

public class Utils {
    private static final String[] MONTH_NAMES = {
            "正", "二", "三", "四", "五", "六",
            "七", "八", "九", "十", "冬", "腊"
    };

    private static final String[] DAY_NAMES = {
            "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
            "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
            "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十"
    };

    public static String getTodayLunar() {
        //中国历直接获取农历
        ChineseCalendar lunar = new ChineseCalendar();
        int month = lunar.get(ChineseCalendar.MONTH) ;
        int day = lunar.get(ChineseCalendar.DAY_OF_MONTH);
        return "农历" + MONTH_NAMES[month ] + "月" + DAY_NAMES[day - 1];
    }
    public static int handleicon(String s){
        if(s==null){
            return R.drawable.ic_weather_unknown;
        }
        int l = Integer.parseInt(s);
        switch (l){
            case 100:
                return R.drawable.ic_weather_sunny;
            case 101:
                return R.drawable.ic_weather_cloudy;
            case 102:
                return R.drawable.ic_weather_cloudy;
            case 104:
                return R.drawable.ic_weather_cloudy;
            case 103:
                return R.drawable.ic_weather_cloudy;
            case 300:
                return R.drawable.ic_weather_thunderstorm;
            case 301:
                return R.drawable.ic_weather_thunderstorm;
            case 302:
                return R.drawable.ic_weather_thunderstorm;
            case 305:
                return R.drawable.ic_weather_light_rain;
            case 306:
                return R.drawable.ic_weather_heavy_rain;
            case 150:
                return R.drawable.ic_weather_night;
            case 151:
                return R.drawable.ic_weather_partly_cloudy;
            case 152:
                return R.drawable.ic_weather_partly_cloudy;
            case 153:
                return R.drawable.ic_weather_partly_cloudy;
            case 154:
                return R.drawable.ic_weather_overcast;

            case 310:
                return R.drawable.ic_weather_storm;
            case 307:
                return R.drawable.ic_weather_downpour;
            case 304:
                return R.drawable.ic_weather_hail;
            case 402:
                return R.drawable.ic_weather_heavy_snow;
            case 502:
                return R.drawable.ic_weather_fog;
            case 503:
                return R.drawable.ic_weather_sandstorm;
            case 404:
                return R.drawable.ic_weather_sleet;
            case 400:
                return R.drawable.ic_weather_light_snow;
            case 401:
                return R.drawable.ic_weather_snow;


            default:
                return R.drawable.ic_weather_unknown;
        }
    }
}
