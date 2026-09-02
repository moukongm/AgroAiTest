package com.common.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class HotAreaBorderView extends View {

    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rectF = new RectF();

    private ViewGroup contentView;
    private View targetView;
    private android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
    private Runnable positionRunnable;
    private View.OnLayoutChangeListener layoutChangeListener;
    private View.OnAttachStateChangeListener targetAttachStateChangeListener;
    private View.OnAttachStateChangeListener containerAttachStateChangeListener;
    private RecyclerView.OnScrollListener recyclerViewScrollListener;
    private RecyclerView recyclerView;
    private boolean isDetached = false;

    public HotAreaBorderView(Context context) {
        super(context);
        init();
    }

    public HotAreaBorderView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HotAreaBorderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(4f);
        borderPaint.setColor(Color.parseColor("#FF5722"));
        setVisibility(GONE);
    }

    public void setBorderColor(int color) {
        borderPaint.setColor(color);
        invalidate();
    }

    public void attachToView(View targetView, ViewGroup container) {
        if (targetView == null || container == null) return;
        isDetached = false;

        this.targetView = targetView;
        this.contentView = container;

        if (!targetView.isAttachedToWindow()) {
            targetAttachStateChangeListener = new View.OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(View v) {
                    targetView.removeOnAttachStateChangeListener(this);
                    targetAttachStateChangeListener = null;
                    attachToView(targetView, container);
                }

                @Override
                public void onViewDetachedFromWindow(View v) {
                    detach();
                }
            };
            targetView.addOnAttachStateChangeListener(targetAttachStateChangeListener);
            return;
        }

        // 添加到指定容器
        container.addView(this, new FrameLayout.LayoutParams(0, 0));
        setVisibility(VISIBLE);

        // 监听容器销毁
        containerAttachStateChangeListener = new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {}

            @Override
            public void onViewDetachedFromWindow(View v) {
                detach();
            }
        };
        container.addOnAttachStateChangeListener(containerAttachStateChangeListener);

        targetView.post(() -> {
            if (isDetached) return;
            updatePosition();

            layoutChangeListener = (v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
                if (!isDetached && contentView != null) updatePosition();
            };
            targetView.addOnLayoutChangeListener(layoutChangeListener);

            setupRecyclerViewScrollListener(targetView);
            startPositionUpdater();
        });
    }

    private void setupRecyclerViewScrollListener(View view) {
        ViewParent parent = view.getParent();
        while (parent != null) {
            if (parent instanceof RecyclerView) {
                recyclerView = (RecyclerView) parent;
                recyclerViewScrollListener = new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                        if (!isDetached && contentView != null) updatePosition();
                    }

                    @Override
                    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                        if (!isDetached && contentView != null) updatePosition();
                    }
                };
                recyclerView.addOnScrollListener(recyclerViewScrollListener);
                break;
            }
            parent = parent.getParent();
        }
    }

    private void startPositionUpdater() {
        positionRunnable = new Runnable() {
            @Override
            public void run() {
                if (isDetached) return;
                if (targetView != null && targetView.isAttachedToWindow() && contentView != null) {
                    updatePosition();
                    handler.postDelayed(this, 50);
                }
            }
        };
        handler.removeCallbacks(positionRunnable);
        handler.post(positionRunnable);
    }

    private void updatePosition() {
        if (isDetached) return;
        if (targetView == null || contentView == null) return;

        try {
            if (!targetView.isAttachedToWindow()) {
                detach();
                return;
            }

            if (targetView.getWidth() <= 0 || targetView.getHeight() <= 0) {
                return;
            }

            int[] location = new int[2];
            targetView.getLocationInWindow(location);

            int[] contentLocation = new int[2];
            contentView.getLocationInWindow(contentLocation);

            int left = location[0] - contentLocation[0];
            int top = location[1] - contentLocation[1];

            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) getLayoutParams();
            if (params == null) {
                params = new FrameLayout.LayoutParams(targetView.getWidth(), targetView.getHeight());
            }

            boolean needsUpdate = false;
            if (params.width != targetView.getWidth() || params.height != targetView.getHeight()
                    || params.leftMargin != left || params.topMargin != top) {
                params.width = targetView.getWidth();
                params.height = targetView.getHeight();
                params.leftMargin = left;
                params.topMargin = top;
                needsUpdate = true;
            }

            if (needsUpdate) {
                setLayoutParams(params);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void detach() {
        isDetached = true;

        if (positionRunnable != null) {
            handler.removeCallbacks(positionRunnable);
            positionRunnable = null;
        }

        if (targetView != null) {
            if (layoutChangeListener != null) {
                targetView.removeOnLayoutChangeListener(layoutChangeListener);
                layoutChangeListener = null;
            }
            if (targetAttachStateChangeListener != null) {
                targetView.removeOnAttachStateChangeListener(targetAttachStateChangeListener);
                targetAttachStateChangeListener = null;
            }
            targetView = null;
        }

        if (contentView != null && containerAttachStateChangeListener != null) {
            contentView.removeOnAttachStateChangeListener(containerAttachStateChangeListener);
            containerAttachStateChangeListener = null;
        }

        if (recyclerView != null && recyclerViewScrollListener != null) {
            recyclerView.removeOnScrollListener(recyclerViewScrollListener);
            recyclerViewScrollListener = null;
            recyclerView = null;
        }

        ViewParent parent = getParent();
        if (parent instanceof ViewGroup) {
            ViewGroup parentView = (ViewGroup) parent;
            parentView.post(() -> {
                if (getParent() == parentView) {
                    parentView.removeView(this);
                }
            });
        }

        contentView = null;
    }

    public void hide() {
        setVisibility(GONE);
    }

    public void show() {
        setVisibility(VISIBLE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getWidth() > 0 && getHeight() > 0) {
            rectF.set(0, 0, getWidth(), getHeight());
            canvas.drawRoundRect(rectF, 8f, 8f, borderPaint);
        }
    }
}
