package com.usth.githubclient.util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.GridView;

/**
 * A GridView that expands to show all of its rows when placed inside a scrollable parent.
 * This avoids the default GridView behaviour where only a single row is visible when the
 * view is nested inside another vertically scrollable container.
 */
public class ExpandedHeightGridView extends GridView {

    public ExpandedHeightGridView(Context context) {
        super(context);
    }

    public ExpandedHeightGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ExpandedHeightGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
        ViewGroup.LayoutParams params = getLayoutParams();
        if (params != null) {
            params.height = getMeasuredHeight();
        }
    }
}