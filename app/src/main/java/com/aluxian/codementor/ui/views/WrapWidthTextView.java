package com.aluxian.codementor.ui.views;

import android.content.Context;
import android.text.Layout;
import android.util.AttributeSet;
import android.widget.TextView;

public class WrapWidthTextView extends TextView {

    public WrapWidthTextView(Context context) {
        super(context);
    }

    public WrapWidthTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WrapWidthTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY) {
            int width = getMaxWidth(getLayout());
            if (width > 0 && width < getMeasuredWidth()) {
                super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST), heightMeasureSpec);
            }
        }
    }

    private int getMaxWidth(Layout layout) {
        int linesCount = layout.getLineCount();
        if (linesCount < 2) {
            return 0;
        }

        float maxWidth = 0;
        for (int i = 0; i < linesCount; i++) {
            maxWidth = Math.max(maxWidth, layout.getLineWidth(i));
        }

        return (int) Math.ceil(maxWidth);
    }

}
