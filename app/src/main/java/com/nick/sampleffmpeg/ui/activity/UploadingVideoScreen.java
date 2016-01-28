package com.nick.sampleffmpeg.ui.activity;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender.SendIntentException;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.nick.sampleffmpeg.Define.Constant;
import com.nick.sampleffmpeg.MainApplication;
import com.nick.sampleffmpeg.R;
import com.nick.sampleffmpeg.dataobject.SelectPlatFromDataObject;
import com.nick.sampleffmpeg.encoding.VideoEncoding;
import com.nick.sampleffmpeg.network.CheckNetworkConnection;
import com.nick.sampleffmpeg.network.CustomDialogs;
import com.nick.sampleffmpeg.network.RequestBean;
import com.nick.sampleffmpeg.network.RequestHandler;
import com.nick.sampleffmpeg.network.RequestListner;
import com.nick.sampleffmpeg.sharedpreference.SPreferenceKey;
import com.nick.sampleffmpeg.sharedpreference.SharedPreferenceWriter;
import com.nick.sampleffmpeg.utils.AppConstants;
import com.nick.sampleffmpeg.utils.FontTypeface;
import com.nick.sampleffmpeg.utils.VideoUtils;
import com.nick.sampleffmpeg.utils.youtube.UploadService;
import com.nick.sampleffmpeg.utils.youtube.util.Upload;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class UploadingVideoScreen extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final int CHOOSE_LOGIN_TYPE_ID = 1;
    private static final String CHOOSE_LOGIN_TYPE_TAG = "CHOOSE_LOGIN_TYPE_TAG";
    private static final int WHO_GIFTED_FRAGMENT_ID = 2;
    private static final String WHO_GIFTED_FRAGMENT_TAG = "WHO_GIFTED_FRAGMENT_TAG";
    // private GoogleApiClient mGoogleApiClient;
    private boolean mSignInClicked, mIntentInProgress;
    private ConnectionResult mConnectionResult;
    private String userName, profilePicUrl, email;
    private Dialog dialog;
    public static final String REQUEST_AUTHORIZATION_INTENT = "com.google.example.yt.RequestAuth";
    public static final String REQUEST_AUTHORIZATION_INTENT_PARAM = "com.google.example.yt.RequestAuth.param";
    public static final String YOUTUBE_ID = "youtubeId";
    public static final String ACCOUNT_KEY = "accountName";
    private String mChosenAccountName;
    private Uri uri = null;
    private static final int REQUEST_AUTHORIZATION = 3;
    private UploadBroadcastReceiver broadcastReceiver;
    public static final String VIDEO_TITLE = "VIDEO_TITLE";
    public static final String VIDEO_TAGS = "VIDEO_TAGS";
    public static final String VIDEO_DESCRIPTION = "VIDEO_DESCRIPTION";


    public static final String VIDEO_TYPE = "VIDEO_TYPE";
    public static String ACTION_PROGRESS_UPDATE = "ACTION_PROGRESS_UPDATE";
    public static String ACTION_CANCEL_UPLOAD = "ACTION_CANCEL_UPLOAD";
    public static String ACTION_UPLOAD_COMPLETED = "ACTION_UPLOAD_COMPLETED";
    private int encodingProgress, uploadingProgress;
    private static String videoLink;
    private static String videoTitle;
    private Dialog optionDialog;

    public static String ACCESS_TOKEN = "ACCESS_TOKEN";
    public static String REFREST_TOKEN = "REFREST_TOKEN";
    public String compnayFacebookDescription = "";
    public String compnayTwitterescription = "";
    public String compnayLinkedInkDescription = "";

    public String personalFaceookDescription = "";
    public String personalTwitterDescription = "";
    public String personalLinkedInkDescription = "";
    public String youtTubeDescription = "";


    public String CURRENT_CLICKABLE_ACCOUNT = "YOUTUBE";

    public static String YOUTUBE = "YOUTUBE";
    public static String PERSONAL_FACEBOOK = "PERSONAL_FACEBOOK";
    public static String PERSONAL_TWITTER = "PERSONAL_TWITTER";
    public static String PERSONAL_LINKEDIN = "PERSONAL_LINKEDIN";
    public static String COMPANY_FACEBOOK = "COMPANY_FACEBOOK";
    public static String COMPANY_TWITTER = "COMPANY_TWITTER";
    public static String COMPANY_LINKEDIN = "COMPANY_LINKEDIN";


    public static boolean ENABLE_PERSONAL_FACEBOOK;
    public static boolean ENABLE_PERSONAL_TWITTER;
    public static boolean ENABLE_PERSONAL_LINKEDIN;
    public static boolean ENABLE_COMPANY_FACEBOOK;
    public static boolean ENABLE_COMPANY_TWITTER;
    public static boolean ENABLE_COMPANY_LINKEDIN;


    public String CURRENT_ACCOUNT_OPTION = "SINGLE_ACCOUNT";
    public static String MULTIPLE_ACCOUNT = "MULTIPLE_ACCOUNT";
    public static String SINGLE_ACCOUNT = "SINGLE_ACCOUNT";

    public List<SelectPlatFromDataObject> selectPlatFromDataObjectList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_upload);
        if (getIntent() != null) {
            videoTitle = getIntent().getExtras().getString("parameter1");
            //   Toast.makeText(getBaseContext(),text,Toast.LENGTH_LONG).show();
            if (videoTitle != null) {
                ((EditText) findViewById(R.id.etVideTitle)).setText(videoTitle);
                ((EditText) findViewById(R.id.etVideTitle)).setSelection(videoTitle.length());
            }
            Log.d("", videoTitle);
        }
        ((TextView) findViewById(R.id.btnNext)).setTypeface(FontTypeface.getTypeface(UploadingVideoScreen.this, AppConstants.FONT_SUFI_SEMIBOLD));
        ((TextView) findViewById(R.id.btnCancel)).setTypeface(FontTypeface.getTypeface(UploadingVideoScreen.this, AppConstants.FONT_SUFI_SEMIBOLD));

        MainApplication.getInstance().setEncodeingProgres(0);
        MainApplication.getInstance().setUploadingProgress(0);
        MainApplication.getInstance().setYoutubeUrl("");
        IntentFilter intentfilter = new IntentFilter();
        intentfilter.addAction(ACTION_PROGRESS_UPDATE);
        intentfilter.addAction(ACTION_UPLOAD_COMPLETED);
        registerReceiver(receiver, intentfilter);
        setFonts();

        findViewById(R.id.btnNext).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                setDescription();
                ;

                if (checkUploading() && isScreenDataValidate()) {
                    Intent intent = new Intent(UploadingVideoScreen.this, SharingVideoScreen.class);
                    intent.putExtra("uripath", uri.toString());
                    intent.putExtra("encoding_progress", encodingProgress);
                    intent.putExtra("uploading_progress", uploadingProgress);
                    intent.putExtra("video_link", videoLink);
                    intent.putExtra("Title", videoTitle);
                    intent.putExtra("Description", ((EditText) findViewById(R.id.et_Description)).getText());


                    intent.putExtra("YouTubeDescription", youtTubeDescription);

                    intent.putExtra("PersonalFacebookDescription", personalFaceookDescription);
                    intent.putExtra("PersonalTwitterDescription", personalTwitterDescription);
                    intent.putExtra("PersonalLinkedInDescription", personalLinkedInkDescription);

                    intent.putExtra("CompanyFacebookDescription", compnayFacebookDescription);
                    intent.putExtra("CompanyTwitterDescription", compnayTwitterescription);
                    intent.putExtra("CompanyLinkedInDescription", compnayLinkedInkDescription);

                    intent.putExtra("Visibility", getVideType());
                    setAllWhiteBackGround();
                    ((RelativeLayout) findViewById(R.id.rl_youtube)).setBackgroundColor(getResources().getColor(R.color.color_blue));
                    startActivity(intent);
                    //finish();
                }

            }
        });

        registerReceiver(finishActivity, new IntentFilter("Finish_Activity"));
        SwitchCompat switchCompat = (SwitchCompat) findViewById(R.id.switch_compat);
        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {

                    setAllWhiteBackGround();
                    CURRENT_ACCOUNT_OPTION = SINGLE_ACCOUNT;
                    CURRENT_CLICKABLE_ACCOUNT = YOUTUBE;
                    ((RelativeLayout) findViewById(R.id.rl_youtube)).setBackgroundColor(getResources().getColor(R.color.color_blue));
                    ((EditText) findViewById(R.id.et_Description)).setText(youtTubeDescription);
                    findViewById(R.id.socialShare).setVisibility(View.GONE);
                } else {
                    CURRENT_ACCOUNT_OPTION = MULTIPLE_ACCOUNT;

                    findViewById(R.id.socialShare).setVisibility(View.VISIBLE);
                }
            }
        });

        findViewById(R.id.ll_Cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelEncodingandUploadingDilaod();
            }
        });
        findViewById(R.id.ll_SlectPlatform).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainApplication.getInstance().getDiffrentPlatformDataValue() != null) {
                    startActivity(new Intent(UploadingVideoScreen.this, SelectPlatfom.class));
                } else {
                    showAlertDialog("Please wait, Social accounts details still downloading");
                }


            }
        });
        findViewById(R.id.rl_youtube).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAllWhiteBackGround();
                ((RelativeLayout) findViewById(R.id.rl_youtube)).setBackgroundColor(getResources().getColor(R.color.color_blue));
                ((EditText) findViewById(R.id.et_Description)).setText(youtTubeDescription);

                CURRENT_CLICKABLE_ACCOUNT = YOUTUBE;
            }
        });

        encodingVideo();

        getCredentials();
    }


    public void setAllWhiteBackGround() {
        try {

            ((RelativeLayout) findViewById(R.id.rl_youtube)).setBackgroundColor(getResources().getColor(R.color.color_white));
            ((RelativeLayout) findViewById(R.id.rl_CompnayFacebook)).setBackgroundColor(getResources().getColor(R.color.color_white));
            ((RelativeLayout) findViewById(R.id.rl_CompnayLinkedin)).setBackgroundColor(getResources().getColor(R.color.color_white));
            ((RelativeLayout) findViewById(R.id.rl_CompnayTwitter)).setBackgroundColor(getResources().getColor(R.color.color_white));
            ((RelativeLayout) findViewById(R.id.rl_PerosnlFacebook)).setBackgroundColor(getResources().getColor(R.color.color_white));
            ((RelativeLayout) findViewById(R.id.rl_PersonalTwitter)).setBackgroundColor(getResources().getColor(R.color.color_white));
            ((RelativeLayout) findViewById(R.id.rl_PerosnlLinkedIn)).setBackgroundColor(getResources().getColor(R.color.color_white));
            setDescription();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void setDescription() {
        try {

            if (CURRENT_CLICKABLE_ACCOUNT.equalsIgnoreCase(YOUTUBE)) {

                youtTubeDescription = ((EditText) findViewById(R.id.et_Description)).getText().toString();
            }
            if (CURRENT_CLICKABLE_ACCOUNT.equalsIgnoreCase(PERSONAL_FACEBOOK)) {

                personalFaceookDescription = ((EditText) findViewById(R.id.et_Description)).getText().toString();
            }
            if (CURRENT_CLICKABLE_ACCOUNT.equalsIgnoreCase(PERSONAL_LINKEDIN)) {

                personalLinkedInkDescription = ((EditText) findViewById(R.id.et_Description)).getText().toString();
            }
            if (CURRENT_CLICKABLE_ACCOUNT.equalsIgnoreCase(PERSONAL_TWITTER)) {

                personalTwitterDescription = ((EditText) findViewById(R.id.et_Description)).getText().toString();
            }
            if (CURRENT_CLICKABLE_ACCOUNT.equalsIgnoreCase(COMPANY_FACEBOOK)) {

                compnayFacebookDescription = ((EditText) findViewById(R.id.et_Description)).getText().toString();
            }
            if (CURRENT_CLICKABLE_ACCOUNT.equalsIgnoreCase(COMPANY_LINKEDIN)) {

                compnayLinkedInkDescription = ((EditText) findViewById(R.id.et_Description)).getText().toString();
            }
            if (CURRENT_CLICKABLE_ACCOUNT.equalsIgnoreCase(COMPANY_TWITTER)) {

                compnayTwitterescription = ((EditText) findViewById(R.id.et_Description)).getText().toString();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isScreenDataValidate() {

        if (CURRENT_ACCOUNT_OPTION.equalsIgnoreCase(MULTIPLE_ACCOUNT)) {


            if (youtTubeDescription.length() > 0) {

            } else {

                showAccountAlertDialog("Oops, We can't proceed with the provieded info", "Your YouTube description is too short");
                return false;
            }


            if (ENABLE_PERSONAL_FACEBOOK) {

                if (personalFaceookDescription.length() > 0) {

                } else {

                    showAccountAlertDialog("Oops, We can't proceed with the provieded info", "You did not enter a description for your personal Facebook Post");
                    return false;
                }
            }

            if (ENABLE_PERSONAL_LINKEDIN) {

                if (personalLinkedInkDescription.length() > 0) {

                } else {

                    showAccountAlertDialog("Oops, We can't proceed with the provieded info", "You did not enter a description for your personal LinkedIn Post");
                    return false;
                }
            }
            if (ENABLE_PERSONAL_TWITTER) {

                if (personalTwitterDescription.length() > 0) {

                } else {

                    showAccountAlertDialog("Oops, We can't proceed with the provieded info", "You did not enter a description for your personal Twitter Post");
                    return false;
                }
            }
            if (ENABLE_COMPANY_FACEBOOK) {

                if (compnayFacebookDescription.length() > 0) {

                } else {

                    showAccountAlertDialog("Oops, We can't proceed with the provieded info", "You did not enter a description for your company Facebook Post");
                    return false;
                }
            }
            if (ENABLE_COMPANY_LINKEDIN) {

                if (personalLinkedInkDescription.length() > 0) {

                } else {

                    showAccountAlertDialog("Oops, We can't proceed with the provieded info", "You did not enter a description for your company LinkedIn Post");
                    return false;
                }
            }

            if (ENABLE_COMPANY_TWITTER) {

                if (personalLinkedInkDescription.length() > 0) {

                } else {

                    showAccountAlertDialog("Oops, We can't proceed with the provieded info", "You did not enter a description for your company Twitter Post");
                    return false;
                }
            }
            return true;
        } else {
            if (((EditText) findViewById(R.id.et_Description)).getText().length() > 0) {
                return true;
            } else {
                showAlertDialog("Please enter description");
            }
            return false;
        }


    }


    public boolean checkUploading() {
        if (uri != null) {

            return true;
        } else {

        }
        return false;
    }


    private void showAccountAlertDialog(String title, String message) {
        try {


            optionDialog = new Dialog(UploadingVideoScreen.this);
            optionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            optionDialog.setContentView(R.layout.alert_dialog);

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

    private void showAlertDialog(String message) {
        try {


            optionDialog = new Dialog(UploadingVideoScreen.this);
            optionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            optionDialog.setContentView(R.layout.alert_dialog);

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

    private void setFonts() {
        ((TextView) findViewById(R.id.btnNext)).setTypeface(FontTypeface.getTypeface(UploadingVideoScreen.this, AppConstants.FONT_SUFI_SEMIBOLD));
        ((TextView) findViewById(R.id.tvEnocdeVideo)).setTypeface(FontTypeface.getTypeface(UploadingVideoScreen.this, AppConstants.FONT_SUFI_REGULAR));
        ((TextView) findViewById(R.id.progress_encoding_text)).setTypeface(FontTypeface.getTypeface(UploadingVideoScreen.this, AppConstants.FONT_SUFI_REGULAR));
        ((TextView) findViewById(R.id.tvUploadVideo)).setTypeface(FontTypeface.getTypeface(UploadingVideoScreen.this, AppConstants.FONT_SUFI_REGULAR));
        ((TextView) findViewById(R.id.tvUploaPercent)).setTypeface(FontTypeface.getTypeface(UploadingVideoScreen.this, AppConstants.FONT_SUFI_REGULAR));

        ((TextView) findViewById(R.id.textView4)).setTypeface(FontTypeface.getTypeface(UploadingVideoScreen.this, AppConstants.FONT_SUFI_REGULAR));
        ((TextView) findViewById(R.id.tvOneDescription)).setTypeface(FontTypeface.getTypeface(UploadingVideoScreen.this, AppConstants.FONT_SUFI_REGULAR));
        ((TextView) findViewById(R.id.tvSelectPlateForm)).setTypeface(FontTypeface.getTypeface(UploadingVideoScreen.this, AppConstants.FONT_SUFI_REGULAR));
        ((EditText) findViewById(R.id.et_VideTag)).setTypeface(FontTypeface.getTypeface(UploadingVideoScreen.this, AppConstants.FONT_SUFI_REGULAR));
        //((TextView)findViewById(R.id.tvVideTitle)).setTypeface(FontTypeface.getTypeface(UploadingVideoScreen.this, AppConstants.FONT_SUFI_REGULAR));
        ((EditText) findViewById(R.id.et_Description)).setTypeface(FontTypeface.getTypeface(UploadingVideoScreen.this, AppConstants.FONT_SUFI_REGULAR));
        ((EditText) findViewById(R.id.etVideTitle)).setTypeface(FontTypeface.getTypeface(UploadingVideoScreen.this, AppConstants.FONT_SUFI_REGULAR));


    }


    private void cancelEncodingandUploadingDilaod() {
        try {


            optionDialog = new Dialog(UploadingVideoScreen.this);
            optionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            optionDialog.setContentView(R.layout.encoding_uplaoding_cancel_dialog);
            ((TextView) optionDialog.findViewById(R.id.buttonNo)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    optionDialog.dismiss();


                }
            });
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


    @Override
    public void onBackPressed() {

        cancelEncodingandUploadingDilaod();

    }


    private void encodingVideo() {

        VideoEncoding.startVideoEncoding(new VideoEncoding.Callback() {
            @Override
            public void onProgress(int progress) {
                encodingProgress = progress;
                MainApplication.getInstance().setEncodeingProgres(progress);
                ((ProgressBar) findViewById(R.id.progress_encoding_bar)).setProgress(progress);
                ((TextView) findViewById(R.id.progress_encoding_text)).setText(progress + "%");
            }

            @Override
            public void onFinish() {

                uri = Uri.fromFile(new File(Constant.getMergedVideo()));
                ((TextView) findViewById(R.id.btnNext)).setTextColor(getResources().getColor(R.color.color_sign_btn));
                geyCredentials();
            }
        }, Constant.VIDEO_WIDTH, Constant.VIDEO_HEIGHT);
    }

    public void geyCredentials() {
        try {
            if (CheckNetworkConnection.isNetworkAvailable(UploadingVideoScreen.this)) {
                List<NameValuePair> paramePairs = new ArrayList<NameValuePair>();
                RequestBean requestBean = new RequestBean();
                requestBean.setActivity(UploadingVideoScreen.this);
                requestBean.setUrl("update_youtube_api_credentials.php");
                requestBean.setParams(paramePairs);
                requestBean.setIsProgressBarEnable(false);
                RequestHandler requestHandler = new RequestHandler(requestBean, requestCredentials);
                requestHandler.execute(null, null, null);
            } else {
                CustomDialogs.showOkDialog(UploadingVideoScreen.this, "Please check network connection");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private RequestListner requestCredentials = new RequestListner() {

        @Override
        public void getResponse(JSONObject jsonObject) {
            try {
                String access_token = "", refresh_token = "";
                String url = "";
                if (jsonObject != null) {
                    if (!jsonObject.isNull("yt_credentials")) {
                        if (!jsonObject.getJSONObject("yt_credentials").isNull("access_token")) {
                            access_token = jsonObject.getJSONObject("yt_credentials").getString("access_token");

                        }
                        if (!jsonObject.getJSONObject("yt_credentials").isNull("refresh_token")) {
                            refresh_token = jsonObject.getJSONObject("yt_credentials").getString("refresh_token");

                        }

                    }
                }
                uploadVideo(access_token, refresh_token);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };


    public void uploadVideo(String accessToken, String refreshTocken) {

        if (uri != null) {
            Intent uploadIntent = new Intent(this, UploadService.class);
            uploadIntent.setData(uri);
            uploadIntent.putExtra(UploadingVideoScreen.ACCOUNT_KEY, mChosenAccountName);
            uploadIntent.putExtra(VIDEO_TYPE, getVideType());
            uploadIntent.putExtra(ACCESS_TOKEN, accessToken);
            uploadIntent.putExtra(REFREST_TOKEN, refreshTocken);
            uploadIntent.putExtra(VIDEO_TITLE, ((EditText) findViewById(R.id.etVideTitle)).getText().toString().trim());
            uploadIntent.putExtra(VIDEO_TAGS, ((EditText) findViewById(R.id.et_VideTag)).getText().toString().trim());
            uploadIntent.putExtra(VIDEO_DESCRIPTION, ((EditText) findViewById(R.id.et_Description)).getText().toString().trim());
            startService(uploadIntent);


        }
    }


    public File getVideoFile() {

        File sdcard = Environment.getExternalStorageDirectory();

        File file = new File(sdcard, "TopVideo.mp4");

        if (file.exists()) {
            System.out.println();

        } else {
            System.out.println();

        }
        return file;

    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        mConnectionResult = connectionResult;
    }


    @Override
    public void onConnected(Bundle arg0) {
        mSignInClicked = false;
        Toast.makeText(this, "User is connected!", Toast.LENGTH_LONG).show();

        // Get user's information

        // Update the UI after signin
        //  updateUI(true);

    }


    private void saveAccount() {
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(this);
        sp.edit().putString(ACCOUNT_KEY, mChosenAccountName).commit();
    }

    /**
     * Sign-out from google
     */


    /**
     * Facebook Login
     */

   /* private void sendData(){
       // ParseFile parseFile = new ParseFile("file.jpg", "");
        ParseObject gameScore = new ParseObject("Feeds");
        gameScore.put("UserName", userName);
        gameScore.put("ProfilePicUrl", profilePicUrl);
        gameScore.put("Email", email);
        gameScore.put("IsActive",true);
        gameScore.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e != null)  e.printStackTrace();
                   *//*if(progressDialog!=null){
                       progressDialog.dismiss();
                   }*//*
                hideProgressBar();
                replaceFragment(WHO_GIFTED_FRAGMENT_ID);
                finish();
            }
        });
    }*/
    private void showProgressBar() {
        if ((dialog == null || !dialog.isShowing())) {
            dialog = new Dialog(UploadingVideoScreen.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            //dialog.setContentView(R.layout.loader_view);
            ((TextView) dialog.findViewById(R.id.text)).setText("loading...");
            dialog.show();
        }
    }

    private void hideProgressBar() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
       /* if (broadcastReceiver == null)
            broadcastReceiver = new UploadBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(
                REQUEST_AUTHORIZATION_INTENT);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                broadcastReceiver, intentFilter);*/

        setDiffrentPlatformData(MainApplication.getInstance().getDiffrentPlatformDataValue());
    }


    private class UploadBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(REQUEST_AUTHORIZATION_INTENT)) {
                Log.d("TAG", "Request auth received - executing the intent");
                Intent toRun = intent
                        .getParcelableExtra(REQUEST_AUTHORIZATION_INTENT_PARAM);
                startActivityForResult(toRun, REQUEST_AUTHORIZATION);
            }
        }
    }

    /***
     * -------------------------------------------------------------------------------------->
     */
    private static final int RESULT_PICK_IMAGE_CROP = 4;

    public void pickFile() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("video/*");
        startActivityForResult(intent, RESULT_PICK_IMAGE_CROP);
    }

    private String getVideType() {
        RadioGroup rbg = (RadioGroup) findViewById(R.id.rbg1);
        String videoType = "public";
        switch (rbg.getCheckedRadioButtonId()) {
            case R.id.rbPublic:
                videoType = "public";
                break;

            case R.id.rbPrivate:
                videoType = "private";
                break;
            case R.id.rbUnlisted:
                videoType = "unlisted";
                break;
        }

        return videoType;

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
                    findViewById(R.id.llProgressEncode).setVisibility(View.GONE);
                    findViewById(R.id.llUploadComple).setVisibility(View.VISIBLE);
                    //videoLink = intent.getExtras().getString("url");
                    //Toast.makeText(UploadingVideoScreen.this, "Video uploaded successfully", Toast.LENGTH_LONG).show();
                    //updateYoutubeKeyOnServer(videoLink);
                }
            } else if (intent.getAction() == ACTION_UPLOAD_COMPLETED) {
                ((TextView) findViewById(R.id.btnNext)).setTextColor(getResources().getColor(R.color.color_sign_btn));
                videoLink = intent.getExtras().getString("url");
                Toast.makeText(UploadingVideoScreen.this, "Video uploaded successfully", Toast.LENGTH_LONG).show();
                updateYoutubeKeyOnServer(videoLink);
            }
        }
    };

    public void updateYoutubeKeyOnServer(String videoLink) {
        try {


            if (CheckNetworkConnection.isNetworkAvailable(UploadingVideoScreen.this)) {
                List<NameValuePair> paramePairs = new ArrayList<NameValuePair>();
                paramePairs.add(new BasicNameValuePair("youtube_id", videoLink));
                paramePairs.add(new BasicNameValuePair("template_id", MainApplication.getInstance().getTemplate().strDirectoryID));
                paramePairs.add(new BasicNameValuePair("title", ((EditText) findViewById(R.id.etVideTitle)).getText().toString()));
                RequestBean requestBean = new RequestBean();
                requestBean.setActivity(UploadingVideoScreen.this);
                requestBean.setUrl("video_uploaded.php");
                requestBean.setParams(paramePairs);
                requestBean.setIsProgressBarEnable(false);
                RequestHandler requestHandler = new RequestHandler(requestBean, listenerupdateKeyOnServer);
                requestHandler.execute(null, null, null);
            } else {
                CustomDialogs.showOkDialog(UploadingVideoScreen.this, "Please check network connection");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private RequestListner listenerupdateKeyOnServer = new RequestListner() {

        @Override
        public void getResponse(JSONObject jsonObject) {
            try {
                String message = null;
                String access_token = "", refresh_token = "";
                String url = "";
                if (jsonObject != null) {
                    if (!jsonObject.isNull("message")) {

                        message = jsonObject.getString("message");
                      //  showAlertDialog(message);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
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

    public void getCredentials() {
        try {
            if (CheckNetworkConnection.isNetworkAvailable(UploadingVideoScreen.this)) {
                List<NameValuePair> paramePairs = new ArrayList<NameValuePair>();
                RequestBean requestBean = new RequestBean();
                requestBean.setActivity(UploadingVideoScreen.this);
                requestBean.setUrl("load_credentials.php");
                requestBean.setParams(paramePairs);
                requestBean.setIsProgressBarEnable(false);
                RequestHandler requestHandler = new RequestHandler(requestBean, selectPlatform);
                requestHandler.execute(null, null, null);
            } else {
                CustomDialogs.showOkDialog(UploadingVideoScreen.this, "Please check network connection");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private RequestListner selectPlatform = new RequestListner() {

        @Override
        public void getResponse(JSONObject jsonObject) {
            try {

                if (jsonObject != null) {
                    if (!jsonObject.isNull("c_facebook")) {

                        SelectPlatFromDataObject selectPlatFromDataObject = new SelectPlatFromDataObject();
                        selectPlatFromDataObject._nameOfAccout = "c_facebook";
                        if (!jsonObject.getJSONObject("c_facebook").isNull("has_valid_auth")) {

                            selectPlatFromDataObject._has_valid_auth = jsonObject.getJSONObject("c_facebook").getBoolean("has_valid_auth");
                            if (selectPlatFromDataObject._has_valid_auth) {
                                selectPlatFromDataObject._platformEnabled = true;
                            }
                        }
                        if (!jsonObject.getJSONObject("c_facebook").isNull("message")) {

                            selectPlatFromDataObject._message = jsonObject.getJSONObject("c_facebook").getString("message");

                        }
                        if (!jsonObject.getJSONObject("c_facebook").isNull("name")) {

                            selectPlatFromDataObject._name = jsonObject.getJSONObject("c_facebook").getString("name");

                        }
                        if (!jsonObject.getJSONObject("c_facebook").isNull("character_limit")) {

                            selectPlatFromDataObject._character_limit = jsonObject.getJSONObject("c_facebook").getString("character_limit");

                        }
                        selectPlatFromDataObjectList.add(selectPlatFromDataObject);
                    }
                    if (!jsonObject.isNull("c_twitter")) {

                        SelectPlatFromDataObject selectPlatFromDataObject = new SelectPlatFromDataObject();
                        selectPlatFromDataObject._nameOfAccout = "c_twitter";
                        if (!jsonObject.getJSONObject("c_twitter").isNull("has_valid_auth")) {

                            selectPlatFromDataObject._has_valid_auth = jsonObject.getJSONObject("c_twitter").getBoolean("has_valid_auth");
                            if (selectPlatFromDataObject._has_valid_auth) {
                                selectPlatFromDataObject._platformEnabled = true;
                            }
                        }
                        if (!jsonObject.getJSONObject("c_twitter").isNull("message")) {

                            selectPlatFromDataObject._message = jsonObject.getJSONObject("c_twitter").getString("message");

                        }
                        if (!jsonObject.getJSONObject("c_twitter").isNull("character_limit")) {

                            selectPlatFromDataObject._character_limit = jsonObject.getJSONObject("c_twitter").getString("character_limit");

                        }
                        if (!jsonObject.getJSONObject("c_twitter").isNull("name")) {

                            selectPlatFromDataObject._name = jsonObject.getJSONObject("c_twitter").getString("name");

                        }
                        selectPlatFromDataObjectList.add(selectPlatFromDataObject);
                    }


                    if (!jsonObject.isNull("c_linkedin")) {

                        SelectPlatFromDataObject selectPlatFromDataObject = new SelectPlatFromDataObject();
                        selectPlatFromDataObject._nameOfAccout = "c_linkedin";
                        if (!jsonObject.getJSONObject("c_linkedin").isNull("has_valid_auth")) {

                            selectPlatFromDataObject._has_valid_auth = jsonObject.getJSONObject("c_linkedin").getBoolean("has_valid_auth");
                            if (selectPlatFromDataObject._has_valid_auth) {
                                selectPlatFromDataObject._platformEnabled = true;
                            }
                        }
                        if (!jsonObject.getJSONObject("c_linkedin").isNull("message")) {

                            selectPlatFromDataObject._message = jsonObject.getJSONObject("c_linkedin").getString("message");

                        }
                        if (!jsonObject.getJSONObject("c_linkedin").isNull("character_limit")) {

                            selectPlatFromDataObject._character_limit = jsonObject.getJSONObject("c_linkedin").getString("character_limit");

                        }
                        if (!jsonObject.getJSONObject("c_linkedin").isNull("name")) {

                            selectPlatFromDataObject._name = jsonObject.getJSONObject("c_linkedin").getString("name");

                        }
                        selectPlatFromDataObjectList.add(selectPlatFromDataObject);
                    }


                    if (!jsonObject.isNull("p_facebook")) {

                        SelectPlatFromDataObject selectPlatFromDataObject = new SelectPlatFromDataObject();
                        selectPlatFromDataObject._nameOfAccout = "p_facebook";
                        if (!jsonObject.getJSONObject("p_facebook").isNull("has_valid_auth")) {

                            selectPlatFromDataObject._has_valid_auth = jsonObject.getJSONObject("p_facebook").getBoolean("has_valid_auth");
                            if (selectPlatFromDataObject._has_valid_auth) {
                                selectPlatFromDataObject._platformEnabled = true;
                            }
                        }
                        if (!jsonObject.getJSONObject("p_facebook").isNull("message")) {

                            selectPlatFromDataObject._message = jsonObject.getJSONObject("p_facebook").getString("message");

                        }
                        if (!jsonObject.getJSONObject("p_facebook").isNull("character_limit")) {

                            selectPlatFromDataObject._character_limit = jsonObject.getJSONObject("p_facebook").getString("character_limit");


                        }

                        if (!jsonObject.getJSONObject("p_facebook").isNull("name")) {

                            selectPlatFromDataObject._name = jsonObject.getJSONObject("p_facebook").getString("name");

                        }
                        selectPlatFromDataObjectList.add(selectPlatFromDataObject);
                    }
                    if (!jsonObject.isNull("p_twitter")) {

                        SelectPlatFromDataObject selectPlatFromDataObject = new SelectPlatFromDataObject();
                        selectPlatFromDataObject._nameOfAccout = "p_twitter";
                        if (!jsonObject.getJSONObject("p_twitter").isNull("has_valid_auth")) {

                            selectPlatFromDataObject._has_valid_auth = jsonObject.getJSONObject("p_twitter").getBoolean("has_valid_auth");
                            if (selectPlatFromDataObject._has_valid_auth) {
                                selectPlatFromDataObject._platformEnabled = true;
                            }
                        }
                        if (!jsonObject.getJSONObject("p_twitter").isNull("message")) {

                            selectPlatFromDataObject._message = jsonObject.getJSONObject("p_twitter").getString("message");

                        }
                        if (!jsonObject.getJSONObject("p_twitter").isNull("character_limit")) {

                            selectPlatFromDataObject._character_limit = jsonObject.getJSONObject("p_twitter").getString("character_limit");

                        }
                        if (!jsonObject.getJSONObject("p_twitter").isNull("name")) {

                            selectPlatFromDataObject._name = jsonObject.getJSONObject("p_twitter").getString("name");

                        }
                        selectPlatFromDataObjectList.add(selectPlatFromDataObject);
                    }


                    if (!jsonObject.isNull("p_linkedin")) {

                        SelectPlatFromDataObject selectPlatFromDataObject = new SelectPlatFromDataObject();
                        selectPlatFromDataObject._nameOfAccout = "p_linkedin";
                        if (!jsonObject.getJSONObject("p_linkedin").isNull("has_valid_auth")) {

                            selectPlatFromDataObject._has_valid_auth = jsonObject.getJSONObject("p_linkedin").getBoolean("has_valid_auth");
                            if (selectPlatFromDataObject._has_valid_auth) {
                                selectPlatFromDataObject._platformEnabled = true;
                            }
                        }
                        if (!jsonObject.getJSONObject("p_linkedin").isNull("message")) {

                            selectPlatFromDataObject._message = jsonObject.getJSONObject("p_linkedin").getString("message");

                        }
                        if (!jsonObject.getJSONObject("p_linkedin").isNull("character_limit")) {

                            selectPlatFromDataObject._character_limit = jsonObject.getJSONObject("p_linkedin").getString("character_limit");

                        }
                        if (!jsonObject.getJSONObject("p_linkedin").isNull("name")) {

                            selectPlatFromDataObject._name = jsonObject.getJSONObject("p_linkedin").getString("name");

                        }
                        selectPlatFromDataObjectList.add(selectPlatFromDataObject);
                    }
                }
                MainApplication.getInstance().setDiffrentAccountDataValue(selectPlatFromDataObjectList);
                setDiffrentPlatformData(selectPlatFromDataObjectList);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };


    public void setDiffrentPlatformData(List<SelectPlatFromDataObject> selectPlatFromDataObjectList) {
        try {
            for (int i = 0; i < selectPlatFromDataObjectList.size(); i++) {
                if (selectPlatFromDataObjectList.get(i)._nameOfAccout.equalsIgnoreCase("p_facebook")) {

                    if (!selectPlatFromDataObjectList.get(i)._platformEnabled) {
                        ((RelativeLayout) findViewById(R.id.rl_PerosnlFacebook)).setOnClickListener(null);
                        ((ImageView) findViewById(R.id.iv_PersonalFacebook)).setImageResource(R.drawable.facebook_icon);
                    } else {
                        ((ImageView) findViewById(R.id.iv_PersonalFacebook)).setImageResource(R.drawable.enable_facebook_icon);
                        ENABLE_PERSONAL_FACEBOOK = true;
                        findViewById(R.id.rl_PerosnlFacebook).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                setAllWhiteBackGround();
                                ((RelativeLayout) findViewById(R.id.rl_PerosnlFacebook)).setBackgroundColor(getResources().getColor(R.color.color_blue));
                                ((EditText) findViewById(R.id.et_Description)).setText(personalFaceookDescription);
                                CURRENT_CLICKABLE_ACCOUNT = PERSONAL_FACEBOOK;

                            }
                        });
                    }

                } else if (selectPlatFromDataObjectList.get(i)._nameOfAccout.equalsIgnoreCase("p_twitter")) {

                    if (!selectPlatFromDataObjectList.get(i)._platformEnabled) {
                        ((RelativeLayout) findViewById(R.id.rl_PersonalTwitter)).setOnClickListener(null);
                        ((ImageView) findViewById(R.id.iv_PersonalTwitter)).setImageResource(R.drawable.twitter_icon);
                    } else {
                        ((ImageView) findViewById(R.id.iv_PersonalTwitter)).setImageResource(R.drawable.enable_twitter_icon);
                        ENABLE_PERSONAL_TWITTER = true;
                        findViewById(R.id.rl_PersonalTwitter).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                setAllWhiteBackGround();
                                ((RelativeLayout) findViewById(R.id.rl_PersonalTwitter)).setBackgroundColor(getResources().getColor(R.color.color_blue));
                                ((EditText) findViewById(R.id.et_Description)).setText(personalTwitterDescription);

                                CURRENT_CLICKABLE_ACCOUNT = PERSONAL_TWITTER;
                            }
                        });

                    }

                } else if (selectPlatFromDataObjectList.get(i)._nameOfAccout.equalsIgnoreCase("p_linkedin")) {

                    if (!selectPlatFromDataObjectList.get(i)._platformEnabled) {
                        ((RelativeLayout) findViewById(R.id.rl_PerosnlLinkedIn)).setOnClickListener(null);
                        ((ImageView) findViewById(R.id.iv_PersonalLinkedIn)).setImageResource(R.drawable.linekedin_icon);

                    } else {
                        ((ImageView) findViewById(R.id.iv_PersonalLinkedIn)).setImageResource(R.drawable.enable_linked_in_icon);
                        ENABLE_PERSONAL_LINKEDIN = true;
                        findViewById(R.id.rl_PerosnlLinkedIn).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                setAllWhiteBackGround();
                                ((RelativeLayout) findViewById(R.id.rl_PerosnlLinkedIn)).setBackgroundColor(getResources().getColor(R.color.color_blue));
                                ((EditText) findViewById(R.id.et_Description)).setText(personalLinkedInkDescription);

                                CURRENT_CLICKABLE_ACCOUNT = PERSONAL_LINKEDIN;
                            }
                        });

                    }

                } else if (selectPlatFromDataObjectList.get(i)._nameOfAccout.equalsIgnoreCase("c_facebook")) {

                    if (!selectPlatFromDataObjectList.get(i)._platformEnabled) {
                        ((RelativeLayout) findViewById(R.id.rl_CompnayFacebook)).setOnClickListener(null);
                    } else {
                        ((ImageView) findViewById(R.id.iv_CompnayFacebook)).setImageResource(R.drawable.enable_facebook_icon);
                        ENABLE_COMPANY_FACEBOOK = true;
                        findViewById(R.id.rl_CompnayFacebook).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                setAllWhiteBackGround();
                                ((RelativeLayout) findViewById(R.id.rl_CompnayFacebook)).setBackgroundColor(getResources().getColor(R.color.color_blue));
                                ((EditText) findViewById(R.id.et_Description)).setText(compnayFacebookDescription);

                                CURRENT_CLICKABLE_ACCOUNT = COMPANY_FACEBOOK;
                            }
                        });
                    }

                } else if (selectPlatFromDataObjectList.get(i)._nameOfAccout.equalsIgnoreCase("c_twitter")) {

                    if (!selectPlatFromDataObjectList.get(i)._platformEnabled) {
                        ((RelativeLayout) findViewById(R.id.rl_CompnayLinkedin)).setOnClickListener(null);
                        ((ImageView) findViewById(R.id.iv_CompanyTwitter)).setImageResource(R.drawable.twitter_icon);
                    } else {
                        ((ImageView) findViewById(R.id.iv_CompanyTwitter)).setImageResource(R.drawable.enable_twitter_icon);
                        ENABLE_COMPANY_TWITTER = true;
                        findViewById(R.id.rl_CompnayTwitter).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                setAllWhiteBackGround();
                                ((RelativeLayout) findViewById(R.id.rl_CompnayTwitter)).setBackgroundColor(getResources().getColor(R.color.color_blue));
                                ((EditText) findViewById(R.id.et_Description)).setText(compnayTwitterescription);

                                CURRENT_CLICKABLE_ACCOUNT = COMPANY_TWITTER;
                            }
                        });

                    }

                } else if (selectPlatFromDataObjectList.get(i)._nameOfAccout.equalsIgnoreCase("c_linkedin")) {

                    if (!selectPlatFromDataObjectList.get(i)._platformEnabled) {
                        ((RelativeLayout) findViewById(R.id.rl_CompnayTwitter)).setOnClickListener(null);
                        ((ImageView) findViewById(R.id.iv_CompanyLinkedIn)).setImageResource(R.drawable.linekedin_icon);
                    } else {
                        ((ImageView) findViewById(R.id.iv_CompanyLinkedIn)).setImageResource(R.drawable.enable_linked_in_icon);
                        ENABLE_COMPANY_LINKEDIN = true;
                        findViewById(R.id.rl_CompnayLinkedin).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                setAllWhiteBackGround();
                                ((RelativeLayout) findViewById(R.id.rl_CompnayLinkedin)).setBackgroundColor(getResources().getColor(R.color.color_blue));
                                ((EditText) findViewById(R.id.et_Description)).setText(compnayLinkedInkDescription);

                                CURRENT_CLICKABLE_ACCOUNT = COMPANY_LINKEDIN;
                            }
                        });

                    }

                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    BroadcastReceiver finishActivity = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            finish();

        }
    };


}
