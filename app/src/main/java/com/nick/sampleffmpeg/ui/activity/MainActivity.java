package com.nick.sampleffmpeg.ui.activity;

/**
 * Created by Admin on 1/21/2016.
 */import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.nick.sampleffmpeg.R;


public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    private static final int CAPTURE_RETURN = 1;
    private static final int GALLERY_RETURN = 2;
    private static final int SUBMIT_RETURN = 3;

    private ProgressBar progressBar;

    private Button btnCaptureVideo;
    private Button btnSelectFromGallery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main1);

        btnCaptureVideo = (Button) findViewById(R.id.btnCaptureVideo);
        btnCaptureVideo.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent i = new Intent();
                i.setAction("android.media.action.VIDEO_CAPTURE");
                startActivityForResult(i, CAPTURE_RETURN);
            }
        });

        btnSelectFromGallery = (Button) findViewById(R.id.btnSelectFromGallery);
        btnSelectFromGallery.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_PICK);
                intent.setType("video/*");

                List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent,  PackageManager.MATCH_DEFAULT_ONLY);
                if (list.size() <= 0) {
                    Log.d(TAG, "no video picker intent on this hardware");
                    return;
                }

                startActivityForResult(intent, GALLERY_RETURN);
            }
        });

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        progressBar.setVisibility(View.GONE);
        btnSelectFromGallery.setEnabled(true);
        btnCaptureVideo.setEnabled(true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case CAPTURE_RETURN:
            case GALLERY_RETURN:
                if (resultCode == RESULT_OK) {

                /*Intent intent = new Intent(this, SubmitActivity.class);
                intent.setData(data.getData());
                startActivityForResult(intent, SUBMIT_RETURN);*/

                    progressBar.setVisibility(View.VISIBLE);
                    btnSelectFromGallery.setEnabled(false);
                    btnCaptureVideo.setEnabled(false);

                    uploadYoutube(data.getData());
                }
                break;
            case SUBMIT_RETURN:
                if (resultCode == RESULT_OK) {
                    Toast.makeText(MainActivity.this, "thank you!", Toast.LENGTH_LONG).show();
                } else {
                    // Toast.makeText(DetailsActivity.this, "submit failed or cancelled",
                    // Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private void uploadYoutube(final Uri data) {

        new AsyncTask<Void, Integer, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                YoutubeUploadRequest request = new YoutubeUploadRequest();
                request.setUri(data);
                //request.setCategory(category);
                //request.setTags(tags);
                request.setTitle("MPRJ Video Tite");
                request.setDescription("MPRJ Video Test");

                YoutubeUploader.upload(request, new YoutubeUploader.ProgressListner() {

                    @Override
                    public void onUploadProgressUpdate(int progress) {

                        publishProgress(progress);
                    }
                }, MainActivity.this);
                return null;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                progressBar.setProgress(values[0]);

                if(values[0] == 100){
                    progressBar.setVisibility(View.GONE);
                    btnSelectFromGallery.setEnabled(true);
                    btnCaptureVideo.setEnabled(true);
                }
            };
        }.execute();
    }
}