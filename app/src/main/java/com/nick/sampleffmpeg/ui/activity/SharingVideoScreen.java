package com.nick.sampleffmpeg.ui.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nick.sampleffmpeg.MainApplication;
import com.nick.sampleffmpeg.R;
import com.nick.sampleffmpeg.ui.view.StretchVideoView;
import com.nick.sampleffmpeg.utils.AppConstants;
import com.nick.sampleffmpeg.utils.FontTypeface;

public class SharingVideoScreen extends Activity {
    public static String ACTION_PROGRESS_UPDATE = "ACTION_PROGRESS_UPDATE";
    public static String ACTION_UPLOAD_COMPLETED = "ACTION_UPLOAD_COMPLETED";
    private StretchVideoView mVideoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_share_upload);
        try{
            if(getIntent() != null){
                String  videoTitle = getIntent().getExtras().getString("parameter1");
                //   Toast.makeText(getBaseContext(),text,Toast.LENGTH_LONG).show();
                ((TextView)findViewById(R.id.tvVideTitle)).setText(videoTitle);
                Log.d("", videoTitle);
            }
            IntentFilter intentfilter = new IntentFilter();
            intentfilter.addAction(ACTION_PROGRESS_UPDATE);
            intentfilter.addAction(ACTION_UPLOAD_COMPLETED);
            registerReceiver(receiver, intentfilter);
            mVideoView = (StretchVideoView)findViewById(R.id.videoview);
            findViewById(R.id.tvVideoUrlBtn).setOnClickListener(onClickListener);
            findViewById(R.id.tvEmbedCodeBtn).setOnClickListener(onClickListener);

            ((ProgressBar)findViewById(R.id.progress_encoding_bar)).setProgress(MainApplication.getInstance().getEncodeingProgres());
            ((ProgressBar)findViewById(R.id.pbarUploadVideo)).setProgress(MainApplication.getInstance().getUploadingProgress());
            ((TextView)findViewById(R.id.tvUploaPercent)).setText(MainApplication.getInstance().getUploadingProgress() + "%");
            ((TextView)findViewById(R.id.progress_encoding_text)).setText(MainApplication.getInstance().getEncodeingProgres() + "%");
            ((TextView)findViewById(R.id.tvVideoUrl)).setText(MainApplication.getInstance().getYoutubeUrl());

            //  ((TextView)findViewById(R.id.tvEnocdeVideo)).setTypeface(FontTypeface.getTypeface(SharingVideoScreen.this, AppConstants.FONT_SUFI_REGULAR));
            ((TextView)findViewById(R.id.progress_encoding_text)).setTypeface(FontTypeface.getTypeface(SharingVideoScreen.this, AppConstants.FONT_SUFI_REGULAR));
            // ((TextView)findViewById(R.id.tvUploadVideo)).setTypeface(FontTypeface.getTypeface(SharingVideoScreen.this, AppConstants.FONT_SUFI_REGULAR));
            ((TextView)findViewById(R.id.tvUploaPercent)).setTypeface(FontTypeface.getTypeface(SharingVideoScreen.this, AppConstants.FONT_SUFI_REGULAR));
            if(MainApplication.getInstance().getUploadingProgress() == 100){
                findViewById(R.id.llProgress).setVisibility(View.GONE);
                findViewById(R.id.llUploadComple).setVisibility(View.VISIBLE);

            }
            findViewById(R.id.llBack).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            getBundleData();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            toogleButton(v);
        }
    };

    private void getBundleData(){
        if(getIntent() !=null){
            String uriPath = getIntent().getExtras().getString("uripath");
            Uri uri = Uri.parse(uriPath);
            mVideoView.setVideoURI(uri);
            mVideoView.requestFocus();
            mVideoView.start();


        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction() == ACTION_PROGRESS_UPDATE){
                ((ProgressBar)findViewById(R.id.pbarUploadVideo)).setProgress(intent.getExtras().getInt("progress"));
                ((TextView)findViewById(R.id.tvUploaPercent)).setText(intent.getExtras().getInt("progress") + "%");
            }else if(intent.getAction() == ACTION_PROGRESS_UPDATE){
                ((TextView)findViewById(R.id.tvVideoUrl)).setText(intent.getExtras().getString("url"));

            }
            if(intent.getExtras().getInt("progress") == 100){
                findViewById(R.id.llProgress).setVisibility(View.GONE);
                findViewById(R.id.llUploadComple).setVisibility(View.VISIBLE);

            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(receiver !=null){
            unregisterReceiver(receiver);
        }
    }

    private void toogleButton(View view){
        if(view.getId() == R.id.tvVideoUrlBtn){
            ((TextView)findViewById(R.id.tvVideoUrlBtn)).setBackgroundColor(getResources().getColor(R.color.color_sky_blue));
            ((TextView)findViewById(R.id.tvVideoUrlBtn)).setTextColor(getResources().getColor(R.color.color_white));

            ((TextView)findViewById(R.id.tvEmbedCodeBtn)).setBackgroundColor(Color.parseColor("#00000000"));
            ((TextView)findViewById(R.id.tvEmbedCodeBtn)).setTextColor(getResources().getColor(R.color.color_greyish));
            ((TextView)findViewById(R.id.tvVideoUrl)).setText(MainApplication.getInstance().getYoutubeUrl());
        }else if(view.getId() == R.id.tvEmbedCodeBtn){
            ((TextView)findViewById(R.id.tvEmbedCodeBtn)).setBackgroundColor(getResources().getColor(R.color.color_sky_blue));
            ((TextView)findViewById(R.id.tvEmbedCodeBtn)).setTextColor(getResources().getColor(R.color.color_white));

            ((TextView)findViewById(R.id.tvVideoUrlBtn)).setBackgroundColor(Color.parseColor("#00000000"));
            ((TextView)findViewById(R.id.tvVideoUrlBtn)).setTextColor(getResources().getColor(R.color.color_greyish));
            ((TextView)findViewById(R.id.tvVideoUrl)).setText("");

        }
    }
}
