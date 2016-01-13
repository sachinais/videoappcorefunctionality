package com.nick.sampleffmpeg.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.nick.sampleffmpeg.MainApplication;
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
            // startActivity(new Intent(SplashScreen.this,RecordingVideoActivity.class));
                CookieSyncManager.createInstance(MainApplication.getInstance());
                if(CookieManager.getInstance().getCookie("live.videomyjob.com") != null){
                    startActivity(new Intent(SplashScreen.this,LoginActivity.class));
                    //startActivity(new Intent(SplashScreen.this,RecordingVideoActivity.class));
                    SplashScreen.this.finish();
                }else {
                    startActivity(new Intent(SplashScreen.this,LoginActivity.class));
                    SplashScreen.this.finish();
                }

            }
        }, 1000);
       /* FileDownloader fileDownloader = new FileDownloader(SplashScreen.this,"http://syd.static.videomyjob.com/company/154fd4a18ee7e9d931059a76bea28a79/15e46054ba3c0c1f2b74e09bfcf600f0/template_225.zip");
        fileDownloader.startDownload();*/
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(handler != null){
            handler.removeCallbacksAndMessages(null);
        }
    }

}
