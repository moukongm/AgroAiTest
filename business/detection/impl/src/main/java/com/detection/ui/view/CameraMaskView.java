package com.detection.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

import com.common.utils.ScreenUtils;
import com.detection.R;

import eightbitlab.com.blurview.BlurView;

public class CameraMaskView extends View {

    private final float cornerRadius;
    private final RectF rectF = new RectF();
    //抗锯齿
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public CameraMaskView(Context context) {
        super(context);
        cornerRadius = ScreenUtils.INSTANCE.dp2px(getContext(), 10);
    }

    public CameraMaskView(Context context, AttributeSet attrs) {
        super(context, attrs);
        cornerRadius = ScreenUtils.INSTANCE.dp2px(getContext(), 10);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        BlurView blurView = findViewById(R.id.blur_view);
//        View decorView = getWindow().getDecorView();
//
//        blurView.setupWith(decorView, new CompositeBlur(this));

        int w = getWidth();
        int h = getHeight();

        int saveCount = canvas.saveLayer(0,0,w,h,null);
        paint.setColor(0x30000000);
        paint.setXfermode(null);
        canvas.drawRect(0,0,w,h,paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        rectF.set(
                (float) w * 0.02f,
                (float) h * 0.15f,
                (float) w * 0.98f,
                (float) h * 0.80f);
        canvas.drawRoundRect(rectF,cornerRadius,cornerRadius,paint);

        canvas.restoreToCount(saveCount);

    }




















}
