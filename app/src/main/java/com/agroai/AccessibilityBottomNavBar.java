package com.agroai;


import static com.google.android.material.internal.ViewUtils.dpToPx;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.common.storage.MMKVUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.internal.ViewUtils;

import eightbitlab.com.blurview.BlurView;

public class AccessibilityBottomNavBar extends FrameLayout {

    private BottomNavigationView nav;
    private String module = MMKVUtils.INSTANCE.custom("user_module").getString("selected_mode","");

    private boolean isA11y;


    public AccessibilityBottomNavBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        checkmodel();
        init();
    }

    public AccessibilityBottomNavBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        checkmodel();
        init();
    }

    public AccessibilityBottomNavBar(Context context) {
        super(context);
        checkmodel();
        init();
    }

    private void checkmodel(){
        if(!module.isEmpty()&&module.equals("senior")){
            isA11y = true;
        }else {
            isA11y = false;
        }
    }

    private void init() {
        if (isA11y) {
            inflate(getContext(), R.layout.bottom_nav_a11y, this);
        } else {
            inflate(getContext(), R.layout.bottom_nav_normal, this);
        }
        nav = (BottomNavigationView) getChildAt(0);
        nav.setItemIconTintList(null);
        nav.setItemRippleColor(null);
        nav.setItemActiveIndicatorEnabled(false);
        post(() -> adjustNavContainer());
    }

    private void adjustNavContainer() {
        ViewParent parent = getParent();
        if (parent instanceof ViewGroup) {
            ViewGroup parentLayout = (ViewGroup) parent;
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) parentLayout.getLayoutParams();
            BlurView blurView = parentLayout.findViewById(R.id.blur_view);
            FrameLayout.LayoutParams blurParams = blurView != null ?
                    (FrameLayout.LayoutParams) blurView.getLayoutParams() : null;

            if (isA11y) {
                parentLayout.setBackgroundResource(R.drawable.bg_nav_rect_a11y);
                params.leftMargin = 0;
                params.rightMargin = 0;
                params.topMargin = 0;
                params.bottomMargin = 0;

                if (blurParams != null) {
                    blurParams.leftMargin = 0;
                    blurParams.rightMargin = 0;
                    blurParams.topMargin = 0;
                    blurParams.bottomMargin = 0;
                    blurView.setLayoutParams(blurParams);
                    blurView.setBackgroundResource(R.drawable.bg_nav_rect_a11y);
                }
            } else {
                parentLayout.setBackgroundResource(0);
                int margin = Math.round(ViewUtils.dpToPx(getContext(), 16));
                params.setMargins(margin, 0, margin, margin);
                if (blurParams != null) {
                    blurParams.setMargins(
                            Math.round(ViewUtils.dpToPx(getContext(), 10)), 0,
                            Math.round(ViewUtils.dpToPx(getContext(), 10)), 0
                    );
                    blurView.setBackgroundResource(R.drawable.bg_nav_rounded);
                    blurView.setLayoutParams(blurParams);
                }
            }
            parentLayout.setLayoutParams(params);
        }
    }
    public void setOnItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener listener) {
        nav.setOnItemSelectedListener(listener);
    }

    public void setSelectedItemId(int itemId) {
        nav.setSelectedItemId(itemId);
    }

    public void switchMode(boolean a11y) {
        if (isA11y == a11y) return;
        isA11y = a11y;
        removeAllViews();
        if (isA11y) {
            inflate(getContext(), R.layout.bottom_nav_a11y, this);
        } else {
            inflate(getContext(), R.layout.bottom_nav_normal, this);
        }
        nav = (BottomNavigationView) getChildAt(0);
        nav.setItemIconTintList(null);
        nav.setItemRippleColor(null);
        nav.setItemActiveIndicatorEnabled(false);
        adjustNavContainer();
    }
}