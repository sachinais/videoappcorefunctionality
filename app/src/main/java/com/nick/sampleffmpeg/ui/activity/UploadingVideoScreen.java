package com.nick.sampleffmpeg.ui.activity;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender.SendIntentException;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.nick.sampleffmpeg.Define.Constant;
import com.nick.sampleffmpeg.MainApplication;
import com.nick.sampleffmpeg.R;
import com.nick.sampleffmpeg.encoding.VideoEncoding;
import com.nick.sampleffmpeg.utils.AppConstants;
import com.nick.sampleffmpeg.utils.FontTypeface;
import com.nick.sampleffmpeg.utils.VideoUtils;
import com.nick.sampleffmpeg.utils.youtube.UploadService;

import java.io.File;

public class UploadingVideoScreen extends AppCompatActivity implements  GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener{
    private static final int CHOOSE_LOGIN_TYPE_ID = 1;
    private static final String CHOOSE_LOGIN_TYPE_TAG = "CHOOSE_LOGIN_TYPE_TAG";
    private static final int WHO_GIFTED_FRAGMENT_ID = 2;
    private static final String WHO_GIFTED_FRAGMENT_TAG = "WHO_GIFTED_FRAGMENT_TAG";
    private GoogleApiClient mGoogleApiClient;
    private boolean mSignInClicked,mIntentInProgress;
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
    public static final String VIDEO_TYPE = "VIDEO_TYPE";
    public static String ACTION_PROGRESS_UPDATE = "ACTION_PROGRESS_UPDATE";
    public static String ACTION_CANCEL_UPLOAD = "ACTION_CANCEL_UPLOAD";
    public static String ACTION_UPLOAD_COMPLETED = "ACTION_UPLOAD_COMPLETED";
    private int encodingProgress, uploadingProgress;
    private static String videoLink;
    private static String videoTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_upload);
        if(getIntent() != null){
             videoTitle = getIntent().getExtras().getString("parameter1");
         //   Toast.makeText(getBaseContext(),text,Toast.LENGTH_LONG).show();
            ((EditText)findViewById(R.id.etVideTitle)).setText(videoTitle);
            Log.d("",videoTitle);
        }
        ((TextView)findViewById(R.id.btnNext)).setTypeface(FontTypeface.getTypeface(UploadingVideoScreen.this, AppConstants.FONT_SUFI_SEMIBOLD));
        ((TextView)findViewById(R.id.btnCancel)).setTypeface(FontTypeface.getTypeface(UploadingVideoScreen.this, AppConstants.FONT_SUFI_SEMIBOLD));

        MainApplication.getInstance().setEncodeingProgres(0);
        MainApplication.getInstance().setUploadingProgress(0);
        MainApplication.getInstance().setYoutubeUrl("");
        IntentFilter intentfilter = new IntentFilter();
        intentfilter.addAction(ACTION_PROGRESS_UPDATE);
        intentfilter.addAction(ACTION_UPLOAD_COMPLETED);
        registerReceiver(receiver, intentfilter);
        googlePlusLogin();
        setFonts();
        findViewById(R.id.btnNext).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // if()

            }
        });
        findViewById(R.id.tvUploadVideo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // signInWithGplus();
                uploadVideo();

            }
        });
        findViewById(R.id.tvEnocdeVideo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // signInWithGplus();
                pickFile();

            }
        });
        findViewById(R.id.btnNext).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // signInWithGplus();
                if (uri != null) {
                    Intent intent = new Intent(UploadingVideoScreen.this, SharingVideoScreen.class);
                    intent.putExtra("uripath", uri.toString());
                    intent.putExtra("encoding_progress", encodingProgress);
                    intent.putExtra("uploading_progress", uploadingProgress);
                    intent.putExtra("video_link", videoLink);
                    intent.putExtra("parameter1",videoTitle);
                    startActivity(intent);
                }

            }
        });
        SwitchCompat switchCompat = (SwitchCompat)findViewById(R.id.switch_compat);
        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    findViewById(R.id.socialShare).setVisibility(View.GONE);
                }else {
                    findViewById(R.id.socialShare).setVisibility(View.VISIBLE);
                }
            }
        });

        encodingVideo();
    }

    private void setFonts() {
        ((TextView)findViewById(R.id.btnNext)).setTypeface(FontTypeface.getTypeface(UploadingVideoScreen.this, AppConstants.FONT_SUFI_SEMIBOLD));
        ((TextView)findViewById(R.id.tvEnocdeVideo)).setTypeface(FontTypeface.getTypeface(UploadingVideoScreen.this, AppConstants.FONT_SUFI_REGULAR));
        ((TextView)findViewById(R.id.progress_encoding_text)).setTypeface(FontTypeface.getTypeface(UploadingVideoScreen.this, AppConstants.FONT_SUFI_REGULAR));
        ((TextView)findViewById(R.id.tvUploadVideo)).setTypeface(FontTypeface.getTypeface(UploadingVideoScreen.this, AppConstants.FONT_SUFI_REGULAR));
        ((TextView)findViewById(R.id.tvUploaPercent)).setTypeface(FontTypeface.getTypeface(UploadingVideoScreen.this, AppConstants.FONT_SUFI_REGULAR));

        ((TextView)findViewById(R.id.textView4)).setTypeface(FontTypeface.getTypeface(UploadingVideoScreen.this, AppConstants.FONT_SUFI_REGULAR));
        ((TextView)findViewById(R.id.etVideTitle)).setTypeface(FontTypeface.getTypeface(UploadingVideoScreen.this, AppConstants.FONT_SUFI_REGULAR));
        ((TextView)findViewById(R.id.tvOneDescription)).setTypeface(FontTypeface.getTypeface(UploadingVideoScreen.this, AppConstants.FONT_SUFI_REGULAR));
        ((TextView)findViewById(R.id.tvSelectPlateForm)).setTypeface(FontTypeface.getTypeface(UploadingVideoScreen.this, AppConstants.FONT_SUFI_REGULAR));
        ((TextView)findViewById(R.id.tvJobTags)).setTypeface(FontTypeface.getTypeface(UploadingVideoScreen.this, AppConstants.FONT_SUFI_REGULAR));
        ((TextView)findViewById(R.id.tvVideTitle)).setTypeface(FontTypeface.getTypeface(UploadingVideoScreen.this, AppConstants.FONT_SUFI_REGULAR));

    }

    private void encodingVideo() {

        int videoWidth = VideoUtils.getVideoWidth(Constant.getSourceVideo());
        int videoHeight = VideoUtils.getVideoHeight(Constant.getSourceVideo());

        VideoEncoding.startVideoEncoding(new VideoEncoding.Callback() {
            @Override
            public void onProgress(int progress) {
                encodingProgress = progress;
                MainApplication.getInstance().setEncodeingProgres(progress);
                        ((ProgressBar) findViewById(R.id.progress_encoding_bar)).setProgress(progress);
                ((TextView)findViewById(R.id.progress_encoding_text)).setText(progress + "%");
            }

            @Override
            public void onFinish() {
                ((TextView)findViewById(R.id.btnNext)).setTextColor(getResources().getColor(R.color.color_sky_blue));
                uri = Uri.fromFile(new File(Constant.getMergedVideo()));
                uploadVideo();
            }
        }, videoWidth, videoHeight, true);
    }
    public void uploadVideo() {
        if (mChosenAccountName == null) {
            signInWithGplus();
            Toast.makeText(UploadingVideoScreen.this, "Please Login First", Toast.LENGTH_LONG).show();
            return;
        }else if(uri == null){
            Toast.makeText(UploadingVideoScreen.this, "Please select a video", Toast.LENGTH_LONG).show();
            return;
        }else if(((EditText)findViewById(R.id.etVideTitle)).getText().toString().trim().isEmpty()){
            Toast.makeText(UploadingVideoScreen.this, "Please give title", Toast.LENGTH_LONG).show();
            return;
        }


        // if a video is picked or recorded.
        if (uri != null) {
            Intent uploadIntent = new Intent(this, UploadService.class);
            // uploadIntent.setData(Uri.parse("content://media/external/video/media/111470"));
            uploadIntent.setData(uri);
            uploadIntent.putExtra(UploadingVideoScreen.ACCOUNT_KEY, mChosenAccountName);
            uploadIntent.putExtra(VIDEO_TYPE, getVideType());
            uploadIntent.putExtra(VIDEO_TITLE,((EditText)findViewById(R.id.etVideTitle)).getText().toString().trim());

            startService(uploadIntent);
            Toast.makeText(this, R.string.youtube_upload_started,
                    Toast.LENGTH_LONG).show();
            // Go back to ImportVideoActivty after upload
            //      finish();
        }
    }




    private void googlePlusLogin(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_PROFILE)
                .build();
    }

    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        mConnectionResult = connectionResult;
    }

    /**
     * Sign-in into google
     * */
    private void signInWithGplus() {
        if (!mGoogleApiClient.isConnecting()) {
            mSignInClicked = true;
            resolveSignInError();
        }
    }
    private static final int RC_SIGN_IN = 0;

    /**
     * Method to resolve any signin errors
     * */
    private void resolveSignInError() {
        if (mConnectionResult != null && mConnectionResult.hasResolution()) {
            try {
                mIntentInProgress = true;
                mConnectionResult.startResolutionForResult(this, RC_SIGN_IN);
            } catch (SendIntentException e) {
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode,
                                    Intent intent) {
        if (requestCode == RC_SIGN_IN) {
            if (responseCode != RESULT_OK) {
                mSignInClicked = false;
            }

            mIntentInProgress = false;

            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        }
        if(requestCode == RESULT_PICK_IMAGE_CROP){
            if (responseCode == RESULT_OK) {
                uri = intent.getData();
                if (uri != null) {
//                    ((TextView)findViewById(R.id.tvFileName)).setText(String.valueOf(uri));
                   /* Intent intent = new Intent(this, ReviewActivity.class);
                    intent.setData(mFileURI);
                    startActivity(intent);*/
                }
            }
        }
    }


    @Override
    public void onConnected(Bundle arg0) {
        mSignInClicked = false;
        Toast.makeText(this, "User is connected!", Toast.LENGTH_LONG).show();

        // Get user's information
        getProfileInformation();

        // Update the UI after signin
        //  updateUI(true);

    }

    private void getProfileInformation() {
        try {
            if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
                Person currentPerson = Plus.PeopleApi
                        .getCurrentPerson(mGoogleApiClient);
                String personName = currentPerson.getDisplayName();
                String personPhotoUrl = currentPerson.getImage().getUrl();
                String personGooglePlusProfile = currentPerson.getUrl();
                String emailAddress = Plus.AccountApi.getAccountName(mGoogleApiClient);

                Log.e("TAG", "Name: " + personName + ", plusProfile: "
                        + personGooglePlusProfile + ", email: " + emailAddress
                        + ", Image: " + personPhotoUrl);
                userName = personName;
                profilePicUrl = personPhotoUrl;
                email = emailAddress;
                mChosenAccountName = Plus.AccountApi.getAccountName(mGoogleApiClient);
                saveAccount();
                //sendData();
             /*   personPhotoUrl = personPhotoUrl.substring(0,
                        personPhotoUrl.length() - 2)
                        + PROFILE_PIC_SIZE;

                new LoadProfileImage(imgProfilePic).execute(personPhotoUrl);*/
                //   startActivity(new Intent(UploadingVideoScreen.this,ImportVideoActivty.class));

            } else {
                Toast.makeText(getApplicationContext(),
                        "Person information is null", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveAccount() {
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(this);
        sp.edit().putString(ACCOUNT_KEY, mChosenAccountName).commit();
    }

    /**
     * Sign-out from google
     * */
    private void signOutFromGplus() {
        if (mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            mGoogleApiClient.disconnect();
            mGoogleApiClient.connect();
            //   updateUI(false);
        }
    }

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

    private void showProgressBar(){
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

    private void hideProgressBar(){
        if(dialog != null){
            dialog.dismiss();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (broadcastReceiver == null)
            broadcastReceiver = new UploadBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(
                REQUEST_AUTHORIZATION_INTENT);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                broadcastReceiver, intentFilter);
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

    private String getVideType(){
        RadioGroup rbg = (RadioGroup)findViewById(R.id.rbg1);
        String videoType = "public";
        switch (rbg.getCheckedRadioButtonId()){
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
            if(intent.getAction() == ACTION_PROGRESS_UPDATE){
                if(intent.getExtras().getInt("progress") == 0){
                    ((ProgressBar)findViewById(R.id.pbarUploadVideo)).setIndeterminate(true);

                }else {
                    ((ProgressBar)findViewById(R.id.pbarUploadVideo)).setIndeterminate(false);

                }
                ((ProgressBar)findViewById(R.id.pbarUploadVideo)).setProgress(intent.getExtras().getInt("progress"));
                ((TextView)findViewById(R.id.tvUploaPercent)).setText(intent.getExtras().getInt("progress") + "%");
                if(intent.getExtras().getInt("progress") == 100){
                    findViewById(R.id.llProgressEncode).setVisibility(View.GONE);
                    findViewById(R.id.llUploadComple).setVisibility(View.VISIBLE);

                }
            }else if(intent.getAction() == ACTION_PROGRESS_UPDATE){
                videoLink = intent.getExtras().getString("url");
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
}
