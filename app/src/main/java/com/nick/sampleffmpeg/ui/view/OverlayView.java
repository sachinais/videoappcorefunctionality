package com.nick.sampleffmpeg.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nick.sampleffmpeg.R;

/**
 * Created by baebae on 12/25/15.
 */
public class OverlayView extends RelativeLayout {

    private TextView txtView = null;
    private View backgroundView = null;
    public OverlayView(Context context) {
        super(context);
    }

    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();

        txtView = (TextView)findViewById(R.id.overlay_txt_view);
        backgroundView = findViewById(R.id.overlay_background_view);
    }

    public void setText(String text) {
        txtView.setText(text);
    }
}
