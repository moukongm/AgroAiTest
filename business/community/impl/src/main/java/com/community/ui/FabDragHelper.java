package com.community.ui;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import com.common.storage.MMKVInstance;
public class FabDragHelper {

    private final View fab;
    private final MMKVInstance mmkv;
    private final String keyPrefix;

    private int screenWidth;
    private int screenHeight;
    private int fabWidth;
    private int fabHeight;
    private int statusBarHeight;
    private int leftEdgeMarginPx;
    private int rightEdgeMarginPx;
    private int topEdgeMarginPx;
    private int bottomEdgeMarginPx;
    private int lastX;
    private int lastY;
    private boolean isDragging;
    private ViewGroup.MarginLayoutParams layoutParams;
    private static final int LEFT_EDGE_MARGIN_DP = 75;  // 左侧边缘间距(dp)
    private static final int RIGHT_EDGE_MARGIN_DP = 15; // 右侧边缘间距(dp)
    private static final int TOP_EDGE_MARGIN_DP = 25;    // 上方边缘间距(dp)
    private static final int BOTTOM_EDGE_MARGIN_DP = 15; // 下方边缘间距(dp)

    private OnSnapListener snapListener;

    public interface OnSnapListener {
        void onSnapToEdge(boolean isLeft);
    }

    public FabDragHelper(View fab, MMKVInstance mmkv, String keyPrefix) {
        this.fab = fab;
        this.mmkv = mmkv;
        this.keyPrefix = keyPrefix;
    }

    public void attach() {
        fab.post(() -> {
            screenWidth = fab.getResources().getDisplayMetrics().widthPixels;
            screenHeight = fab.getResources().getDisplayMetrics().heightPixels;
            fabWidth = fab.getWidth();
            fabHeight = fab.getHeight();
            layoutParams = (ViewGroup.MarginLayoutParams) fab.getLayoutParams();

            int resourceId = fab.getResources().getIdentifier("status_bar_height", "dimen", "android");
            statusBarHeight = resourceId > 0 ? fab.getResources().getDimensionPixelSize(resourceId) : 0;

            float density = fab.getResources().getDisplayMetrics().density;
            leftEdgeMarginPx = (int) (LEFT_EDGE_MARGIN_DP * density);
            rightEdgeMarginPx = (int) (RIGHT_EDGE_MARGIN_DP * density);
            topEdgeMarginPx = (int) (TOP_EDGE_MARGIN_DP * density);
            bottomEdgeMarginPx = (int) (BOTTOM_EDGE_MARGIN_DP * density);

            restorePosition();
            fab.setOnTouchListener(this::onTouch);
        });
    }

    public void setOnSnapListener(OnSnapListener listener) {
        this.snapListener = listener;
    }

    private void restorePosition() {
        int savedX = mmkv.getInt(keyPrefix + "_x", -1);
        int savedY = mmkv.getInt(keyPrefix + "_y", -1);
        if (savedX >= 0 && savedY >= 0) {
            int maxX = screenWidth - fabWidth;
            int maxY = screenHeight - fabHeight - statusBarHeight;
            savedX = Math.max(0, Math.min(savedX, maxX));
            savedY = Math.max(0, Math.min(savedY, maxY));
            
            layoutParams.rightMargin = screenWidth - savedX - fabWidth;
            layoutParams.bottomMargin = savedY;
            fab.setLayoutParams(layoutParams);
        }
    }

    private boolean onTouch(View v, MotionEvent event) {
        int x = (int) event.getRawX();
        int y = (int) event.getRawY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isDragging = false;
                lastX = x;
                lastY = y;
                return true;

            case MotionEvent.ACTION_MOVE:
                int dx = x - lastX;
                int dy = y - lastY;

                if (!isDragging && (Math.abs(dx) > 10 || Math.abs(dy) > 10)) {
                    isDragging = true;
                }

                if (isDragging) {
                    // 拖动时限制不超出屏幕边界
                    // rightMargin 范围: 0 ~ screenWidth - fabWidth - rightEdgeMarginPx
                    // 即按钮可以拖到贴屏幕右边，但左边不能超过 leftEdgeMarginPx
                    int newRight = Math.max(0, Math.min(layoutParams.rightMargin - dx, screenWidth - fabWidth - rightEdgeMarginPx));
                    int newBottom = Math.max(statusBarHeight + topEdgeMarginPx, Math.min(layoutParams.bottomMargin - dy, screenHeight - fabHeight - statusBarHeight - bottomEdgeMarginPx));

                    layoutParams.rightMargin = newRight;
                    layoutParams.bottomMargin = newBottom;
                    fab.setLayoutParams(layoutParams);
                }

                lastX = x;
                lastY = y;
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (isDragging) {
                    snapToEdge(() -> {
                        mmkv.put(keyPrefix + "_x", screenWidth - layoutParams.rightMargin - fabWidth);
                        mmkv.put(keyPrefix + "_y", layoutParams.bottomMargin);
                    });
                    isDragging = false;
                } else {
                    v.performClick();
                }
                return true;
        }
        return false;
    }

    private void snapToEdge(Runnable onComplete) {
        // 判断当前是在左侧还是右侧
        // rightMargin 小 -> 在左侧（按钮贴左）；rightMargin 大 -> 在右侧（按钮贴右）
        boolean snapLeft = layoutParams.rightMargin > (screenWidth - fabWidth) / 2;
        
        // 左贴边：按钮左侧距屏幕左边 60dp，即 rightMargin = screenWidth - fabWidth - 60dp
        // 右贴边：按钮右侧距屏幕右边 15dp，即 rightMargin = 15dp
        int targetRight = snapLeft ? screenWidth - fabWidth - leftEdgeMarginPx : rightEdgeMarginPx;

        ValueAnimator anim = ValueAnimator.ofInt(layoutParams.rightMargin, targetRight);
        anim.setDuration(200);
        anim.setInterpolator(new DecelerateInterpolator());
        anim.addUpdateListener(a -> {
            layoutParams.rightMargin = (int) a.getAnimatedValue();
            fab.setLayoutParams(layoutParams);
        });
        anim.addListener(new Animator.AnimatorListener() {
            @Override public void onAnimationStart(Animator a) {}
            @Override public void onAnimationEnd(Animator a) {
                if (snapListener != null) snapListener.onSnapToEdge(snapLeft);
                onComplete.run();
            }
            @Override public void onAnimationCancel(Animator a) {}
            @Override public void onAnimationRepeat(Animator a) {}
        });
        anim.start();
    }
}
