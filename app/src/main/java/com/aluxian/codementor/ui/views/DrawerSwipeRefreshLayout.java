package com.aluxian.codementor.ui.views;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

public class DrawerSwipeRefreshLayout extends SwipeRefreshLayout {

    private int touchSlop;
    private float initialDownY;

    public DrawerSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initialDownY = event.getY();
                break;

            case MotionEvent.ACTION_MOVE:
                final float eventY = event.getY();
                float yDiff = Math.abs(eventY - initialDownY);

                if (yDiff > touchSlop) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
        }

        return super.onInterceptTouchEvent(event);
    }

}
