package com.nick.sampleffmpeg.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;

import com.nick.sampleffmpeg.R;
import com.nick.sampleffmpeg.sharedpreference.SPreferenceKey;
import com.nick.sampleffmpeg.sharedpreference.SharedPreferenceWriter;

public class SplashScreen extends Activity {
    private Handler handler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        //  ((TextView)findViewById(R.id.tvComplete)).setTypeface(FontTypeface.getTypeface(SplashScreen.this, AppConstants.FONT_SUFI_SEMIBOLD));

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // startActivity(new Intent(SplashScreen.this,RecordingVideoActivity.class));
                if(!TextUtils.isEmpty(SharedPreferenceWriter.getInstance(SplashScreen.this).getString(SPreferenceKey.USERID))){
                    startActivity(new Intent(SplashScreen.this,LoginActivity.class));
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
