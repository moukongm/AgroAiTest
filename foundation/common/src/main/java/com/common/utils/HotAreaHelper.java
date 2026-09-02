package com.common.utils;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import com.common.widget.HotAreaBorderView;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class HotAreaHelper {

    private static float minSizeDp = 30f;

    public static void setMinSizeDp(float dp) {
        minSizeDp = dp;
    }

    public static List<HotAreaBorderView> highlight(ViewGroup container, ViewGroup rootView) {
        List<HotAreaBorderView> highlights = new ArrayList<>();
        if (container == null) return highlights;
        scanAndHighlight(container, highlights, rootView);
        return highlights;
    }

    private static void scanAndHighlight(ViewGroup parent, List<HotAreaBorderView> highlights, ViewGroup rootView) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            if (child == null) continue;

            if (shouldHighlight(child)) {
                try {
                    HotAreaBorderView borderView = new HotAreaBorderView(child.getContext());
                    borderView.attachToView(child, rootView);
                    highlights.add(borderView);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (child instanceof ViewGroup) {
                scanAndHighlight((ViewGroup) child, highlights, rootView);
            }
        }
    }

    private static boolean shouldHighlight(View view) {
        // 1. 必须是小型热点区域
        if (!isSmallHotArea(view)) {
            return false;
        }

        // 2. 必须是可点击的
        if (!isClickable(view)) {
            return false;
        }

        // 3. 不能是容器类型
        if (isContainerView(view)) {
            return false;
        }

        return true;
    }

    private static boolean isContainerView(View view) {
        // 不标记这些容器类型
        if (view instanceof androidx.recyclerview.widget.RecyclerView) return true;
        if (view instanceof android.widget.ListView) return true;
        if (view instanceof android.widget.ExpandableListView) return true;
        if (view instanceof android.widget.ScrollView) return true;
        if (view instanceof android.widget.HorizontalScrollView) return true;
        if (view instanceof android.widget.SearchView) return true;
        if (view instanceof android.webkit.WebView) return true;
        if (view instanceof androidx.viewpager.widget.ViewPager) return true;

        // 不标记自定义的 Container/Layout 类型的视图（常见命名约定）
        String className = view.getClass().getSimpleName().toLowerCase();
        if (className.contains("container") ||
                className.contains("wrapper") ||
                className.contains("root") ||
                className.contains("content") ||
                className.contains("body") ||
                className.contains("main")) {
            return true;
        }

        // 不标记宽度或高度接近屏幕宽度的视图
        int screenWidth = view.getContext().getResources().getDisplayMetrics().widthPixels;
        int screenHeight = view.getContext().getResources().getDisplayMetrics().heightPixels;

        // 如果宽度超过屏幕的 70%，认为是容器
        if (view.getWidth() > screenWidth * 0.7f) {
            return true;
        }

        // 如果高度超过屏幕的 50%，认为是容器
        if (view.getHeight() > screenHeight * 0.5f) {
            return true;
        }

        return false;
    }

    private static boolean isClickable(View view) {
        // 特定类型通常是可点击的
        if (view instanceof android.widget.Button) return true;
        if (view instanceof android.widget.ImageButton) return true;
        if (view instanceof android.widget.TextView && view.isClickable()) return true;
        if (view instanceof android.widget.ImageView && view.isClickable()) return true;
        if (view instanceof android.widget.CheckBox) return true;
        if (view instanceof android.widget.RadioButton) return true;
        if (view instanceof android.widget.ToggleButton) return true;
        if (view instanceof android.widget.Switch) return true;
        if (view instanceof android.widget.EditText) return true;

        // 检查通用的点击属性
        if (view.isClickable() || view.isFocusable()) {
            return true;
        }

        // 检查是否有点击监听器
        try {
            Method method = View.class.getDeclaredMethod("hasOnClickListeners");
            method.setAccessible(true);
            return (boolean) method.invoke(view);
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isSmallHotArea(View view) {
        int minSizePx = dpToPx(view.getContext(), minSizeDp);
        return view.getWidth() > 0 && view.getHeight() > 0
                && (view.getWidth() < minSizePx || view.getHeight() < minSizePx);
    }

    private static int dpToPx(Context context, float dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                context.getResources().getDisplayMetrics()
        );
    }
}