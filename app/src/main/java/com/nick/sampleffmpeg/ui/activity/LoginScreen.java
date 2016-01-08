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
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.nick.sampleffmpeg.R;
import com.nick.sampleffmpeg.utils.youtube.UploadService;

import java.io.File;

public class LoginScreen extends AppCompatActivity implements  GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener{
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
    private Uri uri =Uri.fromFile(new File("sdcard/vid123.mp4"));
    private static final int REQUEST_AUTHORIZATION = 3;
    private UploadBroadcastReceiver broadcastReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);

        googlePlusLogin();
        findViewById(R.id.btnLogin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithGplus();

            }
        });
        findViewById(R.id.btnUpload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // signInWithGplus();
                uploadVideo();

            }
        });
    }

    public void uploadVideo() {
        if (mChosenAccountName == null) {
            return;
        }
        // if a video is picked or recorded.
        if (uri != null) {
            Intent uploadIntent = new Intent(this, UploadService.class);
            uploadIntent.setData(Uri.parse("content://media/external/video/media/111470"));
            uploadIntent.putExtra(LoginScreen.ACCOUNT_KEY, mChosenAccountName);
            startService(uploadIntent);
            Toast.makeText(this, R.string.youtube_upload_started,
                    Toast.LENGTH_LONG).show();
            // Go back to MainActivity after upload
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
             //   startActivity(new Intent(LoginScreen.this,MainActivity.class));

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
            dialog = new Dialog(LoginScreen.this);
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

}
