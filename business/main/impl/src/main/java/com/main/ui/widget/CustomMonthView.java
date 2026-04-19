package com.main.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;

import androidx.core.content.ContextCompat;

import com.haibin.calendarview.Calendar;
import com.haibin.calendarview.MonthView;
import com.main.impl.R;

public class CustomMonthView extends MonthView {

    private Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mCurDayTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mSelectedTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mOtherMonthTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mSchemeTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private int mTextSize;
    private float mRadius;

    private static final int[][] MARK_ICONS = {
            {R.drawable.ic_water, R.drawable.ic_not_water},
            {R.drawable.ic_manure, R.drawable.ic_not_manure},
            {R.drawable.ic_medicine, R.drawable.ic_not_medicine},
            {R.drawable.ic_notebook, R.drawable.ic_notebook}
    };

    private static final String[] TYPE_KEYWORDS = {"浇水", "施肥", "用药", "笔记"};

    public CustomMonthView(Context context) {
        super(context);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14,
                getContext().getResources().getDisplayMetrics());
        mRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 18,
                getContext().getResources().getDisplayMetrics());

        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        mCurDayTextPaint.setTextSize(mTextSize);
        mCurDayTextPaint.setTextAlign(Paint.Align.CENTER);

        mSelectedTextPaint.setTextSize(mTextSize);
        mSelectedTextPaint.setTextAlign(Paint.Align.CENTER);

        mOtherMonthTextPaint.setTextSize(mTextSize);
        mOtherMonthTextPaint.setTextAlign(Paint.Align.CENTER);

        mSchemeTextPaint.setColor(0xFFFFFFFF);
        mSchemeTextPaint.setTextSize(mTextSize);
        mSchemeTextPaint.setTextAlign(Paint.Align.CENTER);
    }

    private int parseTypeFromScheme(String scheme) {
        if (scheme == null || scheme.isEmpty()) {
            return 0;
        }
        for (int i = 0; i < TYPE_KEYWORDS.length; i++) {
            if (scheme.contains(TYPE_KEYWORDS[i])) {
                return i;
            }
        }
        return 0;
    }

    @Override
    protected boolean onDrawSelected(Canvas canvas, Calendar calendar, int x, int y, boolean hasScheme) {
        float cx = x + mItemWidth / 2f;
        float cy = y + mItemHeight / 2f;
        int iconWidth = (int) (mRadius * 1.5f);
        int iconHeight = (int) (mRadius * 2.5f);
        int left = (int) (cx - iconWidth / 2);
        int top = (int) (cy - iconHeight / 2 + mRadius * 0.5f);
        int right = left + iconWidth;
        int bottom = top + iconHeight;

        int iconResId;
        if (hasScheme && calendar.getScheme() != null && !calendar.getScheme().isEmpty()) {
            String scheme = calendar.getScheme();
            int type = parseTypeFromScheme(scheme);
            boolean complete = scheme.contains("●");
            int iconState = complete ? 0 : 1;
            iconResId = MARK_ICONS[type][iconState];
        } else {
            iconResId = R.drawable.ic_date_select;
        }

        Drawable drawable = ContextCompat.getDrawable(getContext(), iconResId);
        if (drawable != null) {
            drawable.setBounds(left, top, right, bottom);
            drawable.draw(canvas);
        }
        return true;
    }

    @Override
    protected void onDrawScheme(Canvas canvas, Calendar calendar, int x, int y) {
        Paint bgPaint = new Paint();
        bgPaint.setAntiAlias(true);
        bgPaint.setStyle(Paint.Style.FILL);
        int color = calendar.getSchemeColor();
        bgPaint.setColor(color != 0 ? color : 0xFF01CFAC);
        canvas.drawCircle(x + mItemWidth / 2f, y + mItemHeight / 2f, mRadius, bgPaint);

        if (calendar.getScheme() != null && !calendar.getScheme().isEmpty()) {
            float cx = x + mItemWidth / 2f;
            float cy = y + mItemHeight / 2f;
            int iconWidth = (int) (mRadius * 1.5f);
            int iconHeight = (int) (mRadius * 2.5f);
            int left = (int) (cx - iconWidth / 2);
            int top = (int) (cy - iconHeight / 2 + mRadius * 0.5f);
            int right = left + iconWidth;
            int bottom = top + iconHeight;

            String scheme = calendar.getScheme();
            int type = parseTypeFromScheme(scheme);
            boolean complete = scheme.contains("●");
            int iconState = complete ? 0 : 1;
            int iconResId = MARK_ICONS[type][iconState];

            Drawable drawable = ContextCompat.getDrawable(getContext(), iconResId);
            if (drawable != null) {
                drawable.setBounds(left, top, right, bottom);
                drawable.draw(canvas);
            }
        }
    }

    @Override
    protected void onDrawText(Canvas canvas, Calendar calendar, int x, int y, boolean hasScheme, boolean isSelected) {
        float cx = x + mItemWidth / 2f;
        float cy = y + mItemHeight / 2f;

        Paint paint;
        if (hasScheme) {
            paint = mSchemeTextPaint;
        } else if (calendar.isCurrentMonth() && calendar.isCurrentDay()) {
            paint = mCurDayTextPaint;
            paint.setColor(getResources().getColor(R.color.primary));
        } else if (isSelected) {
            paint = mSelectedTextPaint;
            paint.setColor(0xFF1A1A1A);
        } else if (!calendar.isCurrentMonth()) {
            paint = mOtherMonthTextPaint;
            paint.setColor(0xFFCCCCCC);
        } else {
            paint = mTextPaint;
            paint.setColor(0xFF1A1A1A);
        }

        canvas.drawText(String.valueOf(calendar.getDay()), cx, cy + mTextSize / 3, paint);
    }
}
