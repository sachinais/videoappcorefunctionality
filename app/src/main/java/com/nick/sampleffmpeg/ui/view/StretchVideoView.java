package com.nick.sampleffmpeg.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

/**
 * Created by baebae on 12/24/15.
 */
public class StretchVideoView extends VideoView {
    public StretchVideoView(Context context) {
        super(context);
    }

    public StretchVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StretchVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }
}
