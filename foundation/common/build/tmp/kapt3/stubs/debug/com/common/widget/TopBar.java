package com.common.widget;

/**
 * 通用顶部导航栏
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000B\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\u0018\u00002\u00020\u0001B%\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\u000e\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u0011J\u001a\u0010\u0012\u001a\u00020\u000f2\u0006\u0010\u0013\u001a\u00020\u00142\n\b\u0002\u0010\u0010\u001a\u0004\u0018\u00010\u0011J\u000e\u0010\u0015\u001a\u00020\u000f2\u0006\u0010\u0016\u001a\u00020\u0014R\u000e\u0010\t\u001a\u00020\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0017"}, d2 = {"Lcom/common/widget/TopBar;", "Landroid/widget/FrameLayout;", "context", "Landroid/content/Context;", "attrs", "Landroid/util/AttributeSet;", "defStyleAttr", "", "(Landroid/content/Context;Landroid/util/AttributeSet;I)V", "ivBack", "Landroid/widget/ImageView;", "tvRight", "Landroid/widget/TextView;", "tvTitle", "setBackListener", "", "listener", "Landroid/view/View$OnClickListener;", "setRightText", "text", "", "setTitle", "title", "common_debug"})
public final class TopBar extends android.widget.FrameLayout {
    @org.jetbrains.annotations.NotNull
    private android.widget.ImageView ivBack;
    @org.jetbrains.annotations.NotNull
    private android.widget.TextView tvTitle;
    @org.jetbrains.annotations.NotNull
    private android.widget.TextView tvRight;
    
    @kotlin.jvm.JvmOverloads
    public TopBar(@org.jetbrains.annotations.NotNull
    android.content.Context context, @org.jetbrains.annotations.Nullable
    android.util.AttributeSet attrs, int defStyleAttr) {
        super(null);
    }
    
    public final void setTitle(@org.jetbrains.annotations.NotNull
    java.lang.String title) {
    }
    
    public final void setRightText(@org.jetbrains.annotations.NotNull
    java.lang.String text, @org.jetbrains.annotations.Nullable
    android.view.View.OnClickListener listener) {
    }
    
    public final void setBackListener(@org.jetbrains.annotations.NotNull
    android.view.View.OnClickListener listener) {
    }
    
    @kotlin.jvm.JvmOverloads
    public TopBar(@org.jetbrains.annotations.NotNull
    android.content.Context context) {
        super(null);
    }
    
    @kotlin.jvm.JvmOverloads
    public TopBar(@org.jetbrains.annotations.NotNull
    android.content.Context context, @org.jetbrains.annotations.Nullable
    android.util.AttributeSet attrs) {
        super(null);
    }
}