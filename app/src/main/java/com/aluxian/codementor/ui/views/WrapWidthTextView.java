package com.aluxian.codementor.ui.views;

import android.content.Context;
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

//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        Layout layout = getLayout();
//
//        if (layout != null) {
//            int width = getMaxLineWidth(layout) + getCompoundPaddingLeft() + getCompoundPaddingRight();
//            int height = getMeasuredHeight();
//            setMeasuredDimension(width, height);
//        }
//    }
//
//    private int getMaxLineWidth(Layout layout) {
//        float maxWidth = 0.0f;
//        int lines = layout.getLineCount();
//
//        for (int i = 0; i < lines; i++) {
//            if (layout.getLineWidth(i) > maxWidth) {
//                maxWidth = layout.getLineWidth(i);
//            }
//        }
//
//        return (int) Math.ceil(maxWidth);
//    }

}
