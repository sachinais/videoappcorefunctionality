package com.nick.sampleffmpeg.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.nick.sampleffmpeg.R;

public class SplashScreen extends Activity {
    private Handler handler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
            // startActivity(new Intent(SplashScreen.this,RecordingVideo.class));
                startActivity(new Intent(SplashScreen.this,LoginActivity.class));
                SplashScreen.this.finish();
            }
        }, 1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(handler != null){
            handler.removeCallbacksAndMessages(null);
        }
    }
}
