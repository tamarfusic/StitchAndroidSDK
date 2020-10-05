package com.fusit.stitchutils.customui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by Leon on 21.02.2017.
 */

public class DynamicSizeImageView extends ImageView {
    private float wRatio = 0;
    private float hRatio = 0;

    public DynamicSizeImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DynamicSizeImageView(Context context) {
        super(context);
    }

    public void setRatio(float wRatio, float hRatio) {
        this.wRatio = wRatio;
        this.hRatio = hRatio;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (hRatio != 0) {
            int width = getMeasuredWidth();
            int height = (int)(width * hRatio);
            setMeasuredDimension(width, height);
        } else if (wRatio != 0) {
            int height = getMeasuredHeight();
            int width = (int)(height / wRatio);
            setMeasuredDimension(width, height);
        }
    }
}
