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

/**
 * Created by baebae on 12/25/15.
 */
public class ChildTextTimelineLayout extends LinearLayout{
    private double startTime = 0;
    private double endTime = 0;

    private int videoLength = 0; //second unit
    private String titleText = "";
    private DisplayMetrics displayMetrics = null;

    long childTagID = 0;
    private double left = 0.f;
    private double right = 0.f;

    public ChildTextTimelineLayout(Context context) {
        super(context);
    }

    public ChildTextTimelineLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChildTextTimelineLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setVideoLength(int length) {
        videoLength = length;
    }

    public double getStartTime() {
        return startTime;
    }

    public double getEndTime() {
        return endTime;
    }

    public String getTitleText() {
        return titleText;
    }

    public void setDisplayMetrics(DisplayMetrics displayMetrics) {
        this.displayMetrics = displayMetrics;
    }

    public void setInformation(double startTime, double endTime, String strText, long tagID) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.titleText = strText;
        this.childTagID = tagID;

        left = startTime * Constant.SP_PER_SECOND * displayMetrics.scaledDensity;
        right = endTime * Constant.SP_PER_SECOND * displayMetrics.scaledDensity;
        setLayoutParameters();

        ((TextView)findViewById(R.id.txt_title)).setText(strText);
    }

    private void setLayoutParameters() {

        startTime = left / Constant.SP_PER_SECOND / displayMetrics.scaledDensity ;
        endTime = right / Constant.SP_PER_SECOND / displayMetrics.scaledDensity ;

        if (startTime < 0 ) {
            left = 0;
        }
        if (endTime > videoLength) {
            endTime = videoLength;
            right = endTime * Constant.SP_PER_SECOND * displayMetrics.scaledDensity;
        }

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
    public double getLayoutLeft() {
        return left;
    }

    /**
     * get timeline object (caption timeline area)'s right position
     * @return right position of selected caption
     */
    public double getLayoutRight() {
        return right;
    }

    /**
     * set timeline object (caption timeline area)'s left position
     */
    public void setLayoutLeft(double left) {
        if (left < right - displayMetrics.scaledDensity * 30 ) {
            this.left = left;
            setLayoutParameters();
        }
    }

    /**
     * set timeline object (caption timeline area)'s right position
     */
    public void setLayoutRight(double right) {
        if (right > left + displayMetrics.scaledDensity * 30 ) {
            this.right = right;
            setLayoutParameters();
        }
    }

    /**
     * move timeline object (caption timeline area)'s left position
     */
    public void moveLayout(double x) {
        double offset = x - this.left;
        this.left = x;
        this.right = this.right + offset;
        setLayoutParameters();
    }


    public long getChildTagID() {
        return childTagID;
    }
}
