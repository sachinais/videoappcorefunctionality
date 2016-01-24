package com.nick.sampleffmpeg.ui.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nick.sampleffmpeg.MainApplication;
import com.nick.sampleffmpeg.R;
import com.nick.sampleffmpeg.network.CheckNetworkConnection;
import com.nick.sampleffmpeg.network.CustomDialogs;
import com.nick.sampleffmpeg.network.RequestBean;
import com.nick.sampleffmpeg.network.RequestHandler;
import com.nick.sampleffmpeg.network.RequestListner;
import com.nick.sampleffmpeg.ui.view.StretchVideoView;
import com.nick.sampleffmpeg.utils.AppConstants;
import com.nick.sampleffmpeg.utils.FontTypeface;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SharingVideoScreen extends Activity {
    public static String ACTION_PROGRESS_UPDATE = "ACTION_PROGRESS_UPDATE";
    public static String ACTION_UPLOAD_COMPLETED = "ACTION_UPLOAD_COMPLETED";
    private StretchVideoView mVideoView;
    private  String description;
    private Dialog optionDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_share_upload);
        try {
            findViewById(R.id.tv_ShareUrl).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shareVideo(MainApplication.getInstance().getYoutubeUrl());
                }
            });
            if (getIntent() != null) {
                String videoTitle = getIntent().getExtras().getString("parameter1");
                //   Toast.makeText(getBaseContext(),text,Toast.LENGTH_LONG).show();
                ((TextView) findViewById(R.id.tvVideTitle)).setText(videoTitle);
                Log.d("", videoTitle);


                description= getIntent().getExtras().getString("Description");
            }
            IntentFilter intentfilter = new IntentFilter();
            intentfilter.addAction(ACTION_PROGRESS_UPDATE);
            intentfilter.addAction(ACTION_UPLOAD_COMPLETED);
            registerReceiver(receiver, intentfilter);
            mVideoView = (StretchVideoView) findViewById(R.id.videoview);
            findViewById(R.id.tvVideoUrlBtn).setOnClickListener(onClickListener);
            findViewById(R.id.tvEmbedCodeBtn).setOnClickListener(onClickListener);

            ((ProgressBar) findViewById(R.id.progress_encoding_bar)).setProgress(MainApplication.getInstance().getEncodeingProgres());
            ((ProgressBar) findViewById(R.id.pbarUploadVideo)).setProgress(MainApplication.getInstance().getUploadingProgress());
            ((TextView) findViewById(R.id.tvUploaPercent)).setText(MainApplication.getInstance().getUploadingProgress() + "%");
            ((TextView) findViewById(R.id.progress_encoding_text)).setText(MainApplication.getInstance().getEncodeingProgres() + "%");
            ((TextView) findViewById(R.id.tvVideoUrl)).setText(MainApplication.getInstance().getYoutubeUrl());

            ((TextView) findViewById(R.id.progress_encoding_text)).setTypeface(FontTypeface.getTypeface(SharingVideoScreen.this, AppConstants.FONT_SUFI_REGULAR));
            // ((TextView)findViewById(R.id.tvUploadVideo)).setTypeface(FontTypeface.getTypeface(SharingVideoScreen.this, AppConstants.FONT_SUFI_REGULAR));
            ((TextView) findViewById(R.id.tvUploaPercent)).setTypeface(FontTypeface.getTypeface(SharingVideoScreen.this, AppConstants.FONT_SUFI_REGULAR));
            if (MainApplication.getInstance().getUploadingProgress() == 100) {
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

            registerReceiver(receiverToutubeLink, new IntentFilter("YouTube_Link"));

            String id = MainApplication.getInstance().getYoutubeData().getId();
            if(id!=null && !id.equalsIgnoreCase("")){
                String url = "https://www.youtube.com/watch?v=" + id;
                ((TextView) findViewById(R.id.tvVideoUrl)).setText(url);
            }



        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void shareVideo(String videoLink){
        try {


            if (CheckNetworkConnection.isNetworkAvailable(SharingVideoScreen.this)) {
                List<NameValuePair> paramePairs = new ArrayList<NameValuePair>();
                paramePairs.add(new BasicNameValuePair("c_facebook", "1"));
                paramePairs.add(new BasicNameValuePair("c_twitter", "1"));
                paramePairs.add(new BasicNameValuePair("c_linkedin", "1"));

                //paramePairs.add(new BasicNameValuePair("p_facebook", );
               // paramePairs.add(new BasicNameValuePair("p_twitter", MainApplication.getInstance().getTemplate().strDirectoryID));
                //paramePairs.add(new BasicNameValuePair("p_linkedin", MainApplication.getInstance().getTemplate().strDirectoryID));

                paramePairs.add(new BasicNameValuePair("facebook_message", "Hello Facebook  Message"));
                paramePairs.add(new BasicNameValuePair("linkedin_message", "Hello LinkedIn Message"));
                paramePairs.add(new BasicNameValuePair("twitter_message", "Hello Twitter Message"));

                paramePairs.add(new BasicNameValuePair("link", videoLink));

                paramePairs.add(new BasicNameValuePair("message", description));

                RequestBean requestBean = new RequestBean();
                requestBean.setActivity(SharingVideoScreen.this);
                requestBean.setUrl("post_social.php");
                requestBean.setParams(paramePairs);
                requestBean.setIsProgressBarEnable(true);
                RequestHandler requestHandler = new RequestHandler(requestBean, listnerSocialPost);
                requestHandler.execute(null, null, null);
            } else {
                CustomDialogs.showOkDialog(SharingVideoScreen.this, "Please check network connection");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private RequestListner listnerSocialPost = new RequestListner() {

        @Override
        public void getResponse(JSONObject jsonObject) {
            try {
                String message =null;
                String access_token="", refresh_token="";
                String url = "";
                if (jsonObject != null) {
                    if(!jsonObject.isNull("success") && jsonObject.getBoolean("success")){

                        showAlertDialog("Link shared successfully");

                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private void showAlertDialog(String message) {
        try {
            optionDialog = new Dialog(SharingVideoScreen.this);
            optionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            optionDialog.setCancelable(false);
            optionDialog.setCanceledOnTouchOutside(false);
            optionDialog.setContentView(R.layout.alert_dialog);

            ((TextView) optionDialog.findViewById(R.id.textView2)).setText(message);
            ((TextView) optionDialog.findViewById(R.id.buttonYes)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    optionDialog.dismiss();
                    finish();

                }
            });
            optionDialog.show();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    BroadcastReceiver receiverToutubeLink = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String id = MainApplication.getInstance().getYoutubeData().getId();

            String url = "https://www.youtube.com/watch?v=" + id;

            ((TextView) findViewById(R.id.tvVideoUrl)).setText(url);
        }
    };


    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            toogleButton(v);
        }
    };

    private void getBundleData() {
        if (getIntent() != null) {
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
            if (intent.getAction() == ACTION_PROGRESS_UPDATE) {
                ((ProgressBar) findViewById(R.id.pbarUploadVideo)).setProgress(intent.getExtras().getInt("progress"));
                ((TextView) findViewById(R.id.tvUploaPercent)).setText(intent.getExtras().getInt("progress") + "%");
            } else if (intent.getAction() == ACTION_UPLOAD_COMPLETED) {
                ((TextView) findViewById(R.id.tvVideoUrl)).setText(intent.getExtras().getString("url"));

            }
            if (intent.getExtras().getInt("progress") == 100) {
                findViewById(R.id.llProgress).setVisibility(View.GONE);
                findViewById(R.id.llUploadComple).setVisibility(View.VISIBLE);

            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
    }

    private void toogleButton(View view) {
        if (view.getId() == R.id.tvVideoUrlBtn) {
            ((TextView) findViewById(R.id.tvVideoUrlBtn)).setBackgroundColor(getResources().getColor(R.color.color_sky_blue));
            ((TextView) findViewById(R.id.tvVideoUrlBtn)).setTextColor(getResources().getColor(R.color.color_white));

            ((TextView) findViewById(R.id.tvEmbedCodeBtn)).setBackgroundColor(Color.parseColor("#00000000"));
            ((TextView) findViewById(R.id.tvEmbedCodeBtn)).setTextColor(getResources().getColor(R.color.color_greyish));
            ((TextView) findViewById(R.id.tvVideoUrl)).setText(MainApplication.getInstance().getYoutubeUrl());
        } else if (view.getId() == R.id.tvEmbedCodeBtn) {
            ((TextView) findViewById(R.id.tvEmbedCodeBtn)).setBackgroundColor(getResources().getColor(R.color.color_sky_blue));
            ((TextView) findViewById(R.id.tvEmbedCodeBtn)).setTextColor(getResources().getColor(R.color.color_white));

            ((TextView) findViewById(R.id.tvVideoUrlBtn)).setBackgroundColor(Color.parseColor("#00000000"));
            ((TextView) findViewById(R.id.tvVideoUrlBtn)).setTextColor(getResources().getColor(R.color.color_greyish));
            ((TextView) findViewById(R.id.tvVideoUrl)).setText("");

        }
    }
}
