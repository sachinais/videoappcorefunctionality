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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.nick.sampleffmpeg.Define.Constant;
import com.nick.sampleffmpeg.MainApplication;
import com.nick.sampleffmpeg.R;
import com.nick.sampleffmpeg.dataobject.SelectPlatFromDataObject;
import com.nick.sampleffmpeg.network.CheckNetworkConnection;
import com.nick.sampleffmpeg.network.CustomDialogs;
import com.nick.sampleffmpeg.network.RequestBean;
import com.nick.sampleffmpeg.network.RequestHandler;
import com.nick.sampleffmpeg.network.RequestListner;
import com.nick.sampleffmpeg.sharedpreference.SPreferenceKey;
import com.nick.sampleffmpeg.sharedpreference.SharedPreferenceWriter;
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
    private String description;
    private Dialog optionDialog;
    private String videoTitle = "";


    private String personal_facebook_message = "";
    private String personal_Twitter_message = "";
    private String personal_LinkedIn_message = "";

    private String company_facebook_message = "";
    private String company_Twitter_message = "";
    private String company_LinkedIn_message = "";

    private String youTube_Message;
    private static String videoLink;
    String uriPath="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_share_upload);
        try {


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



            findViewById(R.id.ll_Post).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shareVideo(MainApplication.getInstance().getYoutubeUrl());
                }
            });
            findViewById(R.id.ll_Share).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                    sharingIntent.setType("text/plain");
                    String shareBody = ((TextView) findViewById(R.id.tvVideoUrl)).getText().toString();
                    sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "VideoMyJob");
                    sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                    startActivity(Intent.createChooser(sharingIntent, "Share via"));
                }
            });


            findViewById(R.id.videoview).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String uriPath = getIntent().getExtras().getString("uripath");
                    if (uriPath != null && uriPath.length() > 0) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uriPath));
                        intent.setDataAndType(Uri.parse(uriPath), "video/mp4");
                        startActivity(intent);
                    }

                }
            });

            findViewById(R.id.ll_OpenDashboard).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openDashboard();
                }
            });

            findViewById(R.id.ll_RecordVideo).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendBroadcast(new Intent("Finish_Activity"));
                    startActivity(new Intent(SharingVideoScreen.this, RecordingVideoActivity.class));
                    finish();
                }
            });

            findViewById(R.id.videoview).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (uriPath.length() > 0) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uriPath));
                        intent.setDataAndType(Uri.parse(Constant.getDownloadTopVideo()), "video/mp4");
                        startActivity(intent);
                    }
                }
            });


            getBundleData();
            setFont();
            setSocialAccountInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void openDashboard() {
        try {
            if (CheckNetworkConnection.isNetworkAvailable(SharingVideoScreen.this)) {
                List<NameValuePair> paramePairs = new ArrayList<NameValuePair>();
                RequestBean requestBean = new RequestBean();
                requestBean.setActivity(SharingVideoScreen.this);
                requestBean.setUrl("get_sid.php");
                requestBean.setParams(paramePairs);
                requestBean.setIsProgressBarEnable(true);
                RequestHandler requestHandler = new RequestHandler(requestBean, requestSid);
                requestHandler.execute(null, null, null);
            } else {
                CustomDialogs.showOkDialog(SharingVideoScreen.this, "Please check network connection");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private RequestListner requestSid = new RequestListner() {

        @Override
        public void getResponse(JSONObject jsonObject) {
            try {
                String sid = "";
                String url = "";
                if (jsonObject != null) {
                    if (!jsonObject.isNull("sid")) {
                        sid = jsonObject.getString("sid");
                    }

                    url = "https://live.videomyjob.com/api/app_login.php?user_id=" + SharedPreferenceWriter.getInstance().getString(SPreferenceKey.USERID) + " & sid=" + sid + " & " + "redirect=1";


                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(browserIntent);

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    public void setFont() {
        try {

            ((TextView) findViewById(R.id.tv_ImportantText)).setTypeface(FontTypeface.getTypeface(SharingVideoScreen.this, AppConstants.FONT_SUFI_REGULAR));
            ((TextView) findViewById(R.id.tv_OpenDashboard)).setTypeface(FontTypeface.getTypeface(SharingVideoScreen.this, AppConstants.FONT_SUFI_REGULAR));
            ((TextView) findViewById(R.id.tv_RecordNewVideo)).setTypeface(FontTypeface.getTypeface(SharingVideoScreen.this, AppConstants.FONT_SUFI_REGULAR));


            ((TextView) findViewById(R.id.tv_Or)).setTypeface(FontTypeface.getTypeface(SharingVideoScreen.this, AppConstants.FONT_SUFI_REGULAR));
            ((TextView) findViewById(R.id.tvVideoUrl)).setTypeface(FontTypeface.getTypeface(SharingVideoScreen.this, AppConstants.FONT_SUFI_REGULAR));
            ((TextView) findViewById(R.id.tv_ShareUrl)).setTypeface(FontTypeface.getTypeface(SharingVideoScreen.this, AppConstants.FONT_SUFI_REGULAR));

            ((TextView) findViewById(R.id.tvVideoUrlBtn)).setTypeface(FontTypeface.getTypeface(SharingVideoScreen.this, AppConstants.FONT_SUFI_REGULAR));
            ((TextView) findViewById(R.id.tvEmbedCodeBtn)).setTypeface(FontTypeface.getTypeface(SharingVideoScreen.this, AppConstants.FONT_SUFI_REGULAR));
            ((TextView) findViewById(R.id.tvVideTitle)).setTypeface(FontTypeface.getTypeface(SharingVideoScreen.this, AppConstants.FONT_SUFI_REGULAR));
            ((TextView) findViewById(R.id.tv_PostToPlatform)).setTypeface(FontTypeface.getTypeface(SharingVideoScreen.this, AppConstants.FONT_SUFI_REGULAR));


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void shareVideo(String videoLink) {
        try {


            if (CheckNetworkConnection.isNetworkAvailable(SharingVideoScreen.this)) {
                List<NameValuePair> paramePairs = new ArrayList<NameValuePair>();

                if (personal_facebook_message.length() > 0) {
                    paramePairs.add(new BasicNameValuePair("p_facebook", "1"));
                    paramePairs.add(new BasicNameValuePair("facebook_message", personal_facebook_message));

                }
                if (personal_LinkedIn_message.length() > 0) {
                    paramePairs.add(new BasicNameValuePair("p_linkedin", "1"));
                    paramePairs.add(new BasicNameValuePair("linkedin_message", personal_LinkedIn_message));

                }
                if (personal_Twitter_message.length() > 0) {
                    paramePairs.add(new BasicNameValuePair("p_twitter", "1"));
                    paramePairs.add(new BasicNameValuePair("twitter_message", personal_Twitter_message));

                }
                if (company_facebook_message.length() > 0) {
                    paramePairs.add(new BasicNameValuePair("c_facebook", "1"));
                    paramePairs.add(new BasicNameValuePair("facebook_message", company_facebook_message));

                }
                if (company_LinkedIn_message.length() > 0) {
                    paramePairs.add(new BasicNameValuePair("c_linkedin", "1"));
                    paramePairs.add(new BasicNameValuePair("linkedin_message", company_LinkedIn_message));

                }
                if (company_Twitter_message.length() > 0) {
                    paramePairs.add(new BasicNameValuePair("c_twitter", "1"));
                    paramePairs.add(new BasicNameValuePair("facebook_message", company_Twitter_message));

                }
                paramePairs.add(new BasicNameValuePair("link", videoLink));

                paramePairs.add(new BasicNameValuePair("message", youTube_Message));

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

                if (jsonObject != null) {
                    if (!jsonObject.isNull("success") && jsonObject.getBoolean("success")) {

                        findViewById(R.id.ll_BeforePost).setVisibility(View.GONE);
                        findViewById(R.id.ll_AfterPost).setVisibility(View.VISIBLE);

                        showAlertDialog("Success", "Video posted successfully.");

                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private void showAlertDialog(String title, String message) {
        try {


            optionDialog = new Dialog(SharingVideoScreen.this);
            optionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            optionDialog.setContentView(R.layout.alert_dialog);
            ((TextView) optionDialog.findViewById(R.id.textView1)).setText(title);
            ((TextView) optionDialog.findViewById(R.id.textView2)).setText(message);
            ((TextView) optionDialog.findViewById(R.id.buttonYes)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    optionDialog.dismiss();
                    //finish();

                }
            });
            optionDialog.show();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }





    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            toogleButton(v);
        }
    };

    private void getBundleData() {
        if (getIntent() != null) {
             uriPath = getIntent().getExtras().getString("uripath");
            Uri uri = Uri.parse(uriPath);
            mVideoView.setVideoURI(uri);
            mVideoView.requestFocus();
            mVideoView.start();

            if (getIntent().getExtras().containsKey("Title"))

                videoTitle = getIntent().getExtras().getString("Title");
            if (getIntent().getExtras().containsKey("Description"))

                description = getIntent().getExtras().getString("Description");

            if (getIntent().getExtras().containsKey("PersonalFacebookDescription"))
                personal_facebook_message = getIntent().getExtras().getString("PersonalFacebookDescription");
            if (getIntent().getExtras().containsKey("PersonalTwitterDescription"))

                personal_Twitter_message = getIntent().getExtras().getString("PersonalTwitterDescription");
            if (getIntent().getExtras().containsKey("PersonalLinkedInDescription"))

                personal_LinkedIn_message = getIntent().getExtras().getString("PersonalLinkedInDescription");
            if (getIntent().getExtras().containsKey("CompanyFacebookDescription"))

                company_facebook_message = getIntent().getExtras().getString("CompanyFacebookDescription");
            if (getIntent().getExtras().containsKey("CompanyTwitterDescription"))

                company_Twitter_message = getIntent().getExtras().getString("CompanyTwitterDescription");
            if (getIntent().getExtras().containsKey("CompanyLinkedInDescription"))

                company_LinkedIn_message = getIntent().getExtras().getString("CompanyLinkedInDescription");

            if (getIntent().getExtras().containsKey("YouTubeDescription"))

                youTube_Message = getIntent().getExtras().getString("YouTubeDescription");

            ((TextView) findViewById(R.id.tvVideTitle)).setText(videoTitle);
        }


    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == ACTION_PROGRESS_UPDATE) {
                if (intent.getExtras().getInt("progress") == 0) {
                    ((ProgressBar) findViewById(R.id.pbarUploadVideo)).setIndeterminate(true);

                } else {
                    ((ProgressBar) findViewById(R.id.pbarUploadVideo)).setIndeterminate(false);

                }
                ((ProgressBar) findViewById(R.id.pbarUploadVideo)).setProgress(intent.getExtras().getInt("progress"));
                ((TextView) findViewById(R.id.tvUploaPercent)).setText(intent.getExtras().getInt("progress") + "%");
                if (intent.getExtras().getInt("progress") == 100) {
                    findViewById(R.id.llProgress).setVisibility(View.GONE);
                    findViewById(R.id.llUploadComple).setVisibility(View.VISIBLE);
                    //videoLink = intent.getExtras().getString("url");
                    //Toast.makeText(UploadingVideoScreen.this, "Video uploaded successfully", Toast.LENGTH_LONG).show();
                    //updateYoutubeKeyOnServer(videoLink);
                }
            } else if (intent.getAction() == ACTION_UPLOAD_COMPLETED) {
                videoLink = intent.getExtras().getString("url");
                ((TextView)findViewById(R.id.tvVideoUrl)).setText(videoLink);
                //updateYoutubeKeyOnServer(videoLink);

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


            String url = "<iframe width=\"560\"  height=\"315\" src = \" " + MainApplication.getInstance().getYoutubeUrl() + "\"" + " frameborder=\"0\" allowfullscreen></iframe>";
            ((TextView) findViewById(R.id.tvVideoUrl)).setText(url);

        }
    }


    public void setSocialAccountInfo(){
        try{
            List<SelectPlatFromDataObject> selectPlatFromDataObjectList = MainApplication.getInstance().getDiffrentPlatformDataValue();

            for (int i = 0; i < selectPlatFromDataObjectList.size(); i++) {
                if (selectPlatFromDataObjectList.get(i)._nameOfAccout.equalsIgnoreCase("p_facebook")) {

                    if (selectPlatFromDataObjectList.get(i)._has_valid_auth) {
                        if (selectPlatFromDataObjectList.get(i)._platformEnabled)
                            ((ImageView) findViewById(R.id.iv_PersonalFacebook)).setImageResource(R.drawable.enable_facebook_selectale_platfrom);
                    }

                } else if (selectPlatFromDataObjectList.get(i)._nameOfAccout.equalsIgnoreCase("p_linkedin")) {

                    if (selectPlatFromDataObjectList.get(i)._has_valid_auth) {
                        if (selectPlatFromDataObjectList.get(i)._platformEnabled)
                            ((ImageView) findViewById(R.id.iv_PersonalLinkedIn)).setImageResource(R.drawable.enable_linkedin_selectale_platfrom);

                    }


                } else if (selectPlatFromDataObjectList.get(i)._nameOfAccout.equalsIgnoreCase("p_twitter")) {

                    if (selectPlatFromDataObjectList.get(i)._has_valid_auth) {

                        if (selectPlatFromDataObjectList.get(i)._platformEnabled)
                            ((ImageView) findViewById(R.id.iv_PersonalTwitter)).setImageResource(R.drawable.enable_twitter_selectale_platfrom);
                    }


                }


                if (selectPlatFromDataObjectList.get(i)._nameOfAccout.equalsIgnoreCase("c_facebook")) {

                    if (selectPlatFromDataObjectList.get(i)._has_valid_auth) {
                        if (selectPlatFromDataObjectList.get(i)._platformEnabled)
                            ((ImageView) findViewById(R.id.iv_CompnayFacebook)).setImageResource(R.drawable.enable_facebook_selectale_platfrom);

                    }


                } else if (selectPlatFromDataObjectList.get(i)._nameOfAccout.equalsIgnoreCase("c_linkedin")) {

                    if (selectPlatFromDataObjectList.get(i)._has_valid_auth) {
                        if (selectPlatFromDataObjectList.get(i)._platformEnabled)
                            ((ImageView) findViewById(R.id.iv_CompanyLinkedIn)).setImageResource(R.drawable.enable_linkedin_selectale_platfrom);

                    }

                } else if (selectPlatFromDataObjectList.get(i)._nameOfAccout.equalsIgnoreCase("c_twitter")) {

                    if (selectPlatFromDataObjectList.get(i)._has_valid_auth) {
                        if (selectPlatFromDataObjectList.get(i)._platformEnabled)
                            ((ImageView) findViewById(R.id.iv_CompanyTwitter)).setImageResource(R.drawable.enable_twitter_selectale_platfrom);

                    }


                }

            }



        }catch (Exception e){
            e.printStackTrace();
        }
    }



}
