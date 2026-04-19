package com.main.ui.widget;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.haibin.calendarview.Calendar;
import com.haibin.calendarview.CalendarView;
import com.main.impl.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PlantCalendarView {

    private final Context context;
    private final CalendarView calendarView;
    private final TextView tvMonthTitle;
    private final ImageView ivPrevMonth;
    private final ImageView ivNextMonth;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final SimpleDateFormat monthYearFormatter = new SimpleDateFormat("yyyy年 M月", Locale.CHINESE);
    private java.util.Calendar selectedDate;
    private int displayYear;
    private int displayMonth;

    // 按类型和状态存储标记日期: type -> (state -> dates)
    // state: 0=已完成, 1=待处理
    private final Map<Integer, Map<Integer, Map<String, Calendar>>> markDates = new HashMap<>();

    private OnDateSelectedListenerCustom dateSelectedListener;
    private OnMonthChangedListenerCustom monthChangedListener;

    // 标记类型常量
    public static final int TYPE_WATER = 0;     // 浇水 - 蓝色
    public static final int TYPE_FERTILIZE = 1; // 施肥 - 绿色
    public static final int TYPE_MEDICINE = 2;   // 用药 - 红色
    public static final int TYPE_NOTE = 3;       // 笔记 - 橙色

    // 状态常量
    public static final int STATE_COMPLETE = 0;  // 已完成
    public static final int STATE_PENDING = 1;   // 待处理
    

    // 标记类型文字 [类型][状态]
    private static final String[][] MARK_LABELS = {
            {"已浇水", "待浇水"},   // 浇水
            {"已施肥", "待施肥"},   // 施肥
            {"已用药", "待用药"},   // 用药
            {"笔记", "待笔记"}      // 笔记
    };

    // 标记类型关键字（用于 scheme 解析）
    public static final String[] TYPE_KEYWORDS = {"浇水", "施肥", "用药", "笔记"};

    // 标记类型背景图片 [类型][状态]
    // 类型: 0=浇水, 1=施肥, 2=用药, 3=笔记
    // 状态: 0=已完成, 1=待处理
    private static final int[][] MARK_ICONS = {
            {R.drawable.ic_water, R.drawable.ic_not_water},      // 浇水
            {R.drawable.ic_manure, R.drawable.ic_not_manure}, // 施肥
            {R.drawable.ic_medicine, R.drawable.ic_not_medicine}, // 用药
            {R.drawable.ic_notebook, R.drawable.ic_notebook}                       // 笔记（无待处理状态）
    };

    // 当前显示的标记类型 (-1表示显示所有)
    private int currentMarkType = -1;

    // 当前显示的状态 (-1表示显示所有)
    private int currentMarkState = -1;

    // 准备标记的日期（用户已选择状态但还未按"打上标签"）
    private String preparedMarkDate = null;
    private int preparedMarkType = -1;
    private int preparedMarkState = -1;

    // 选中日期标记（用于显示选中的日期图标）
    private String selectedDateMark = null;

    // 日期选中监听器接口 - 使用 java.util.Calendar
    public interface OnDateSelectedListenerCustom {
        void onDateSelected(java.util.Calendar date);
    }

    // 月份变化监听器接口
    public interface OnMonthChangedListenerCustom {
        void onMonthChanged(int year, int month);
    }

    public PlantCalendarView(Context context, CalendarView calendarView,
                             TextView tvMonthTitle, ImageView ivPrevMonth, ImageView ivNextMonth) {
        this.context = context;
        this.calendarView = calendarView;
        this.tvMonthTitle = tvMonthTitle;
        this.ivPrevMonth = ivPrevMonth;
        this.ivNextMonth = ivNextMonth;

        this.selectedDate = java.util.Calendar.getInstance();

        // 初始化每种类型的存储: type -> (state -> dates)
        for (int i = 0; i < 4; i++) {
            markDates.put(i, new HashMap<>());
            markDates.get(i).put(STATE_COMPLETE, new HashMap<>());
            markDates.get(i).put(STATE_PENDING, new HashMap<>());
        }

        init();
    }

    private void init() {
        // 初始化显示年月为当前年月
        java.util.Calendar now = java.util.Calendar.getInstance();
        displayYear = now.get(java.util.Calendar.YEAR);
        displayMonth = now.get(java.util.Calendar.MONTH) + 1;
        
        setupCalendar();
        setupMonthNavigation();
        updateMonthTitle();
    }

    private void setupCalendar() {
        // 使用 SINGLE 单选模式：只有用户点击才会选中日期，初始化时不会自动选中当天
        calendarView.setSelectSingleMode();

        // 设置日期选中监听
        calendarView.setOnCalendarSelectListener(new CalendarView.OnCalendarSelectListener() {
            @Override
            public void onCalendarOutOfRange(Calendar calendar) {
            }

            @Override
            public void onCalendarSelect(Calendar calendar, boolean isClick) {
                if (isClick) {
                    selectedDate.set(calendar.getYear(), calendar.getMonth() - 1, calendar.getDay());
                    if (dateSelectedListener != null) {
                        dateSelectedListener.onDateSelected((java.util.Calendar) selectedDate.clone());
                    }
                }
            }
        });

        // 设置月份变化监听 - 3.7.1 版本使用 OnMonthChangeListener
        calendarView.setOnMonthChangeListener(new CalendarView.OnMonthChangeListener() {
            @Override
            public void onMonthChange(int year, int month) {
                // 同步更新显示年月
                displayYear = year;
                displayMonth = month;
                updateMonthTitle(year, month);
                if (monthChangedListener != null) {
                    monthChangedListener.onMonthChanged(year, month);
                }
            }
        });


        refreshDecorators();

        // 清除初始选中状态（今天不应被默认选中）
        mainHandler.postDelayed(() -> {
            clearSelection();
        }, 300);
    }

    private void setupMonthNavigation() {
        ivPrevMonth.setOnClickListener(v -> {
            // 计算上一个月
            if (displayMonth == 1) {
                displayYear--;
                displayMonth = 12;
            } else {
                displayMonth--;
            }
            calendarView.scrollToCalendar(displayYear, displayMonth, 1);
            updateMonthTitle(displayYear, displayMonth);
        });

        ivNextMonth.setOnClickListener(v -> {
            // 计算下一个月
            if (displayMonth == 12) {
                displayYear++;
                displayMonth = 1;
            } else {
                displayMonth++;
            }
            calendarView.scrollToCalendar(displayYear, displayMonth, 1);
            updateMonthTitle(displayYear, displayMonth);
        });
    }

    private void updateMonthTitle() {
        updateMonthTitle(calendarView.getCurYear(), calendarView.getCurMonth());
    }

    private void updateMonthTitle(int year, int month) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(year, month - 1, 1);
        tvMonthTitle.setText(monthYearFormatter.format(cal.getTime()));
    }

    private void refreshDecorators() {
        updateMarkSchemes();
    }


    private void updateMarkSchemes() {
        Map<String, Calendar> schemes = new HashMap<>();

        // 1. 首先处理已保存的标记
        List<Integer> typesToShow = new ArrayList<>();
        if (currentMarkType >= 0 && currentMarkType < 4) {
            typesToShow.add(currentMarkType);
        } else {
            for (int i = 0; i < 4; i++) {
                typesToShow.add(i);
            }
        }

        for (Integer type : typesToShow) {
            // 获取要显示的状态
            List<Integer> statesToShow = new ArrayList<>();
            if (currentMarkState == STATE_COMPLETE) {
                statesToShow.add(STATE_COMPLETE);
            } else if (currentMarkState == STATE_PENDING) {
                statesToShow.add(STATE_PENDING);
            } else {
                statesToShow.add(STATE_COMPLETE);
                statesToShow.add(STATE_PENDING);
            }

            for (Integer state : statesToShow) {
                Map<String, Calendar> stateMarks = markDates.get(type).get(state);
                if (stateMarks != null) {
                    for (Map.Entry<String, Calendar> entry : stateMarks.entrySet()) {
                        Calendar calendar = entry.getValue();
                        // 跳过准备标记的日期（它们会单独处理）
                        if (preparedMarkDate != null && preparedMarkDate.equals(entry.getKey())) {
                            continue;
                        }
                        Calendar scheme = new Calendar();
                        scheme.setYear(calendar.getYear());
                        scheme.setMonth(calendar.getMonth());
                        scheme.setDay(calendar.getDay());
                        // 设置 schemeColor 使 hasScheme() 返回 true
                        scheme.setSchemeColor(0xFFFFFFFF);
                        // 格式: "类型:符号" 例如 "浇水:●", "施肥:○"
                        String symbol = state == STATE_COMPLETE ? "●" : "○";
                        scheme.setScheme(TYPE_KEYWORDS[type] + ":" + symbol);
                        schemes.put(getKey(calendar.getYear(), calendar.getMonth(), calendar.getDay()), scheme);
                    }
                }
            }
        }

        if (preparedMarkDate != null && preparedMarkType >= 0) {
            Calendar preparedCalendar = parseCalendarFromKey(preparedMarkDate);
            if (preparedCalendar != null) {
                preparedCalendar.setSchemeColor(0xFFFFFFFF);
                String symbol = preparedMarkState == STATE_COMPLETE ? "●" : "○";
                preparedCalendar.setScheme(symbol);
                schemes.put(preparedMarkDate, preparedCalendar);
            }
        }

        if (selectedDateMark != null && !selectedDateMark.equals(preparedMarkDate)) {
            Calendar selectedCalendar = parseCalendarFromKey(selectedDateMark);
            if (selectedCalendar != null) {
                selectedCalendar.setSchemeColor(0xFF01CFAC);
                selectedCalendar.setScheme("selected:");
                schemes.put(selectedDateMark, selectedCalendar);
            }
        }

        calendarView.setSchemeDate(schemes);
    }

    private String getKey(int year, int month, int day) {
        return year + "" + (month < 10 ? "0" + month : month) + "" + (day < 10 ? "0" + day : day);
    }


    private Calendar parseCalendarFromKey(String key) {
        if (key == null || key.length() < 8) {
            return null;
        }
        try {
            int year = Integer.parseInt(key.substring(0, 4));
            int month = Integer.parseInt(key.substring(4, 6));
            int day = Integer.parseInt(key.substring(6, 8));
            Calendar calendar = new Calendar();
            calendar.setYear(year);
            calendar.setMonth(month);
            calendar.setDay(day);
            return calendar;
        } catch (NumberFormatException e) {
            return null;
        }
    }


    private java.util.Calendar parseUtilCalendarFromKey(String key) {
        if (key == null || key.length() < 8) {
            return null;
        }
        try {
            int year = Integer.parseInt(key.substring(0, 4));
            int month = Integer.parseInt(key.substring(4, 6));
            int day = Integer.parseInt(key.substring(6, 8));
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            calendar.set(year, month - 1, day);
            return calendar;
        } catch (NumberFormatException e) {
            return null;
        }
    }


    public void setMarkType(int type) {
        this.currentMarkType = type;
        refreshDecorators();
    }


    public void setMarkState(int state) {
        this.currentMarkState = state;
        refreshDecorators();
    }


    public void addMark(java.util.Calendar date, int type, int state) {
        if (type < 0 || type >= 4) return;
        if (state < 0 || state > 1) return;

        // 先移除同一类型另一个状态的标记（同一日期同类型只能存在一个状态）
        int oppositeState = state == STATE_COMPLETE ? STATE_PENDING : STATE_COMPLETE;
        String key = getKey(
                date.get(java.util.Calendar.YEAR),
                date.get(java.util.Calendar.MONTH) + 1,
                date.get(java.util.Calendar.DAY_OF_MONTH)
        );
        markDates.get(type).get(oppositeState).remove(key);

        Calendar calendar = new Calendar();
        calendar.setYear(date.get(java.util.Calendar.YEAR));
        calendar.setMonth(date.get(java.util.Calendar.MONTH) + 1);
        calendar.setDay(date.get(java.util.Calendar.DAY_OF_MONTH));
        calendar.setSchemeColor(0xFFFFFFFF);

        String symbol = state == STATE_COMPLETE ? "●" : "○";
        calendar.setScheme(TYPE_KEYWORDS[type] + ":" + symbol);

        markDates.get(type).get(state).put(key, calendar);
        refreshDecorators();
    }



    public void addMarkWithNote(java.util.Calendar date, int type, int state, String noteContent) {
        if (type < 0 || type >= 4) return;
        if (state < 0 || state > 1) return;

        // 先移除同一类型另一个状态的标记
        int oppositeState = state == STATE_COMPLETE ? STATE_PENDING : STATE_COMPLETE;
        String key = getKey(
                date.get(java.util.Calendar.YEAR),
                date.get(java.util.Calendar.MONTH) + 1,
                date.get(java.util.Calendar.DAY_OF_MONTH)
        );
        markDates.get(type).get(oppositeState).remove(key);

        Calendar calendar = new Calendar();
        calendar.setYear(date.get(java.util.Calendar.YEAR));
        calendar.setMonth(date.get(java.util.Calendar.MONTH) + 1);
        calendar.setDay(date.get(java.util.Calendar.DAY_OF_MONTH));
        calendar.setSchemeColor(0xFFFFFFFF);

        String symbol = state == STATE_COMPLETE ? "●" : "○";
        calendar.setScheme(TYPE_KEYWORDS[type] + ":" + symbol);

        markDates.get(type).get(state).put(key, calendar);
        refreshDecorators();
    }


    public void removeMark(java.util.Calendar date, int type, int state) {
        if (type < 0 || type >= 4) return;
        if (state < 0 || state > 1) return;

        String key = getKey(
                date.get(java.util.Calendar.YEAR),
                date.get(java.util.Calendar.MONTH) + 1,
                date.get(java.util.Calendar.DAY_OF_MONTH)
        );
        markDates.get(type).get(state).remove(key);
        refreshDecorators();
    }


    public boolean hasMark(java.util.Calendar date, int type, int state) {
        if (type < 0 || type >= 4) return false;
        if (state < 0 || state > 1) return false;
        String key = getKey(
                date.get(java.util.Calendar.YEAR),
                date.get(java.util.Calendar.MONTH) + 1,
                date.get(java.util.Calendar.DAY_OF_MONTH)
        );
        return markDates.get(type).get(state).containsKey(key);
    }



    public int getMarkState(java.util.Calendar date, int type) {
        if (type < 0 || type >= 4) return -1;
        String key = getKey(
                date.get(java.util.Calendar.YEAR),
                date.get(java.util.Calendar.MONTH) + 1,
                date.get(java.util.Calendar.DAY_OF_MONTH)
        );
        if (markDates.get(type).get(STATE_COMPLETE).containsKey(key)) {
            return STATE_COMPLETE;
        }
        if (markDates.get(type).get(STATE_PENDING).containsKey(key)) {
            return STATE_PENDING;
        }
        return -1;
    }

    public void clearAllMarks() {
        for (int i = 0; i < 4; i++) {
            markDates.get(i).get(STATE_COMPLETE).clear();
            markDates.get(i).get(STATE_PENDING).clear();
        }
        refreshDecorators();
    }

    public java.util.Calendar getSelectedDate() {
        return (java.util.Calendar) selectedDate.clone();
    }

    public void setSelectedDate(java.util.Calendar date) {
        if (date != null) {
            selectedDate = (java.util.Calendar) date.clone();
            calendarView.scrollToCalendar(
                    date.get(java.util.Calendar.YEAR),
                    date.get(java.util.Calendar.MONTH) + 1,
                    date.get(java.util.Calendar.DAY_OF_MONTH)
            );
        }
    }

    public int getCurrentYear() {
        return calendarView.getCurYear();
    }

    public int getCurrentMonth() {
        return calendarView.getCurMonth();
    }

    public void scrollToMonth(int year, int month) {
        calendarView.scrollToCalendar(year, month, 1);
        updateMonthTitle(year, month);
    }

    public void setOnDateSelectedListener(OnDateSelectedListenerCustom listener) {
        this.dateSelectedListener = listener;
    }

    public void setOnMonthChangedListener(OnMonthChangedListenerCustom listener) {
        this.monthChangedListener = listener;
    }

    /**
     * 销毁时调用，清理所有资源避免内存泄漏
     */
    public void onDestroy() {
        // 移除所有 Handler 回调
        mainHandler.removeCallbacksAndMessages(null);
        
        // 清理监听器引用
        dateSelectedListener = null;
        monthChangedListener = null;
        
        // 清理数据
        markDates.clear();
        preparedMarkDate = null;
        selectedDateMark = null;
    }

    public void refreshCalendar() {
        refreshDecorators();
    }

    public CalendarView getCalendarView() {
        return calendarView;
    }

    public java.util.Calendar getToday() {
        return java.util.Calendar.getInstance();
    }


    private void clearSelection() {
        // 滚动到1号再滚回来以清除选中状态
        int year = calendarView.getCurYear();
        int month = calendarView.getCurMonth();
        calendarView.scrollToCalendar(year, month, 1);
        
        // 使用弱引用包装 Runnable，避免内存泄漏
        java.lang.ref.WeakReference<Runnable> weakRunnable = new java.lang.ref.WeakReference<>(new Runnable() {
            @Override
            public void run() {
                // 检查 calendarView 是否仍可用
                if (calendarView != null) {
                    calendarView.scrollToCalendar(year, month, calendarView.getCurDay());
                }
            }
        });
        
        mainHandler.postDelayed(weakRunnable.get(), 50);
    }

    public void setDateRange(int minYear, int minMonth, int minDay, int maxYear, int maxMonth, int maxDay) {
        calendarView.setRange(minYear, minMonth, minDay, maxYear, maxMonth, maxDay);
    }

    public static int getMarkColor(int type, int state) {
        if (type >= 0 && type < MARK_ICONS.length && state >= 0 && state < 2) {
            return MARK_ICONS[type][state];
        }
        return Color.GRAY;
    }


    public static String getMarkLabel(int type, int state) {
        if (type >= 0 && type < MARK_LABELS.length && state >= 0 && state < 2) {
            return MARK_LABELS[type][state];
        }
        return "未知";
    }


    public void setPreparedMark(java.util.Calendar date, int type, int state) {
        if (date == null) {
            preparedMarkDate = null;
            preparedMarkType = -1;
            preparedMarkState = -1;
        } else {
            preparedMarkDate = getKey(
                    date.get(java.util.Calendar.YEAR),
                    date.get(java.util.Calendar.MONTH) + 1,
                    date.get(java.util.Calendar.DAY_OF_MONTH)
            );
            preparedMarkType = type;
            preparedMarkState = state;
            // 清除选中日期标记
            clearSelectedDateMarkWithoutRefresh();
        }
        refreshDecorators();
    }


    public boolean confirmPreparedMark() {
        if (preparedMarkDate == null || preparedMarkType < 0) {
            return false;
        }

        String dateToMark = preparedMarkDate;
        int typeToMark = preparedMarkType;
        int stateToMark = preparedMarkState;

        preparedMarkDate = null;
        preparedMarkType = -1;
        preparedMarkState = -1;

        selectedDateMark = null;
        calendarView.clearSingleSelect();

        java.util.Calendar date = parseUtilCalendarFromKey(dateToMark);
        if (date != null) {
            addMark(date, typeToMark, stateToMark);
        }
        return true;
    }


    public void cancelPreparedMark() {
        preparedMarkDate = null;
        preparedMarkType = -1;
        preparedMarkState = -1;
        refreshDecorators();
    }




    public void clearSelectedDateMark() {
        selectedDateMark = null;
        refreshDecorators();
    }


    private void clearSelectedDateMarkWithoutRefresh() {
        selectedDateMark = null;
    }
}
