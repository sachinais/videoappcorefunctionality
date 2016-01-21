package com.nick.sampleffmpeg.ui.view;

import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nick.sampleffmpeg.Define.Constant;
import com.nick.sampleffmpeg.R;
import com.nick.sampleffmpeg.bean.OverlayBean;

/**
 * Created by baebae on 12/25/15.
 */
public class ChildTextTimelineLayout extends LinearLayout{
    private float startTime = 0;
    private float endTime = 0;
    private OverlayBean.Overlay overlay = null;
    private boolean flagRemovable = false;

    private String titleText = "";
    private DisplayMetrics displayMetrics = null;

    long childTagID = 0;
    private float left = 0.f;
    private float right = 0.f;

    private int parentWidth = 0;
    public ChildTextTimelineLayout(Context context) {
        super(context);
    }

    public ChildTextTimelineLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChildTextTimelineLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setParentWidth(int width) {
        parentWidth = width;
    }

    public float getStartTime() {
        return startTime;
    }

    public float getEndTime() {
        return endTime;
    }

    public String getTitleText() {
        return titleText;
    }

    public OverlayBean.Overlay getCaptionOverlay() {
        return overlay;
    }

    public boolean isRemovable() {
        return flagRemovable;
    }

    public void setDisplayMetrics(DisplayMetrics displayMetrics) {
        this.displayMetrics = displayMetrics;
    }

    public void setInformation(float startTime, float endTime, String strText, long tagID, OverlayBean.Overlay overlay, boolean flagRemovable) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.titleText = strText;
        this.childTagID = tagID;
        this.overlay = overlay;
        this.flagRemovable = flagRemovable;

        left = startTime * Constant.SP_PER_SECOND * displayMetrics.scaledDensity;
        right = endTime * Constant.SP_PER_SECOND * displayMetrics.scaledDensity;
        setLayoutParameters(left, right);

        ((TextView)findViewById(R.id.txt_title)).setText(strText);
    }

    public void updateStartEndTime(float startTime, float endTime) {
        setInformation(startTime, endTime, this.titleText, this.childTagID, this.overlay, this.flagRemovable);
    }

    private void setLayoutParameters(float newLeft, float newRight) {
        if (newLeft < 0) {
            newLeft = 0;
            newRight = this.right;
        }

        if (parentWidth > 0 && newRight > parentWidth) {
            newRight = parentWidth;
            newLeft = left;
        }

        float _startTime = newLeft / Constant.SP_PER_SECOND / displayMetrics.scaledDensity ;
        float _endTime = newRight / Constant.SP_PER_SECOND / displayMetrics.scaledDensity ;

        if (_endTime - _startTime < Constant.TIMELINE_UNIT_SECOND * 0.9f) {
            return;
        }
        startTime = _startTime;
        endTime = _endTime;
        left = newLeft;
        right = newRight;


        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                (int)(right - left),
                LayoutParams.MATCH_PARENT
        );

        params.setMargins((int)left, 0, 0, 0);
        setLayoutParams(params);
    }

    /**
     * get timeline object (caption timeline area)'s left position
     * @return left position of selected caption
     */
    public float getLayoutLeft() {
        return left;
    }

    /**
     * get timeline object (caption timeline area)'s right position
     * @return right position of selected caption
     */
    public float getLayoutRight() {
        return right;
    }

    /**
     * set timeline object (caption timeline area)'s left position
     */
    public void setLayoutLeft(float left) {
        if (left < right - displayMetrics.scaledDensity * 30 ) {
            float newLeft = left;
            setLayoutParameters(newLeft, this.right);
        }
    }

    /**
     * set timeline object (caption timeline area)'s right position
     */
    public void setLayoutRight(float right) {
        if (right > left + displayMetrics.scaledDensity * 30 ) {
            float newRight = right;
            setLayoutParameters(this.left, newRight);
        }
    }

    /**
     * move timeline object (caption timeline area)'s left position
     */
    public void moveLayout(float x) {
        float offset = x - this.left;
        float newLeft = x;
        float newRight = this.right + offset;
        setLayoutParameters(newLeft, newRight);
    }


    public long getChildTagID() {
        return childTagID;
    }
}
