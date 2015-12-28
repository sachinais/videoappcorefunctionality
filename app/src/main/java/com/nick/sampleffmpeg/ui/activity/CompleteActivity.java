package com.nick.sampleffmpeg.ui.activity;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;

import com.nick.sampleffmpeg.Define.Constant;
import com.nick.sampleffmpeg.R;
import com.nick.sampleffmpeg.ui.control.UITouchButton;
import com.nick.sampleffmpeg.ui.view.StretchVideoView;
import com.nick.sampleffmpeg.utils.FileUtils;
import com.nick.sampleffmpeg.utils.LogFile;
import com.nick.sampleffmpeg.utils.StringUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by baebae on 12/25/15.
 */
public class CompleteActivity extends BaseActivity{

    @InjectView(R.id.video_view_tap_area)
    View video_view_tap_area;

    @InjectView(R.id.video_view)
    StretchVideoView videoView;

    @InjectView(R.id.img_preview)
    ImageView imgPreview;
    private boolean flagPlay = false;
    private boolean flagInitialized = false;
    private Handler mHandler = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_complete);
        ButterKnife.inject(this);

        UITouchButton.applyEffect(video_view_tap_area, UITouchButton.EFFECT_ALPHA, Constant.BUTTON_NORMAL_ALPHA,
                Constant.BUTTON_FOCUS_ALPHA, new Runnable() {
                    @Override
                    public void run() {
                        onPlay();
                    }
                });

        mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                initializeVideoView();
            }
        }, 500);
    }

    /**
     * initialize video view
     * set media file for video view, once it is initialized control layout will be shown below video view
     */
    private void initializeVideoView() {
        videoView.setVideoPath(Constant.getMergedVideo());
        videoView.requestFocus();

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                flagInitialized = true;
            }
        });

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                imgPreview.setVisibility(View.VISIBLE);
            }
        });
    }
    /**
     * Play & Pause video in video view
     */
    private void onPlay() {
        if (!flagPlay) {
            flagPlay = true;
            imgPreview.setVisibility(View.GONE);
            videoView.start();
        } else {
            flagPlay = false;
            videoView.pause();
            imgPreview.setVisibility(View.VISIBLE);
        }
    }
}
