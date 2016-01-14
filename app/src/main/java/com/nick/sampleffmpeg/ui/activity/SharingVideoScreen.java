package com.nick.sampleffmpeg.ui.activity;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.widget.VideoView;

import com.nick.sampleffmpeg.R;

public class SharingVideoScreen extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_share_upload);

        VideoView mVideoView = (VideoView)findViewById(R.id.videoview);

        String uriPath = getIntent().getExtras().getString("uripath");

        Uri uri = Uri.parse(uriPath);
        mVideoView.setVideoURI(uri);
        mVideoView.requestFocus();
        mVideoView.start();
    }
}
