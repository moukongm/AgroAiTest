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
    private int lastX;
    private int lastY;
    private boolean isDragging;
    private ViewGroup.MarginLayoutParams layoutParams;

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
                    int newRight = Math.max(0, Math.min(layoutParams.rightMargin - dx, screenWidth - fabWidth));
                    int newBottom = Math.max(statusBarHeight, Math.min(layoutParams.bottomMargin - dy, screenHeight - fabHeight - statusBarHeight));

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
        boolean snapLeft = layoutParams.rightMargin > (screenWidth - fabWidth) / 2;
        int targetRight = snapLeft ? screenWidth - fabWidth : 0;

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
