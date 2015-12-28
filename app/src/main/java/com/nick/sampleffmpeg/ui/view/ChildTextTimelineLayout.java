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
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                (int)(right - left),
                LayoutParams.MATCH_PARENT
        );
        params.setMargins((int)left, 0, 0, 0);
        setLayoutParams(params);

        startTime = left / Constant.SP_PER_SECOND / displayMetrics.scaledDensity ;
        endTime = right / Constant.SP_PER_SECOND / displayMetrics.scaledDensity ;
    }

    public double getLayoutLeft() {
        return left;
    }

    public double getLayoutRight() {
        return right;
    }

    public void setLayoutLeft(double left) {
        if (left < right - displayMetrics.scaledDensity * 30 ) {
            this.left = left;
            setLayoutParameters();
        }
    }

    public void setLayoutRight(double right) {
        if (right > left + displayMetrics.scaledDensity * 30 ) {
            this.right = right;
            setLayoutParameters();
        }
    }

    public long getChildTagID() {
        return childTagID;
    }
}
