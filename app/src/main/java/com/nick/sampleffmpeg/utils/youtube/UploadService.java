/*
 * Copyright (c) 2013 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.nick.sampleffmpeg.utils.youtube;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.BasicAuthentication;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.youtube.YouTube;
import com.google.common.collect.Lists;
import com.nick.sampleffmpeg.Define.Constant;
import com.nick.sampleffmpeg.R;
import com.nick.sampleffmpeg.bean.YoutubeDataBean;
import com.nick.sampleffmpeg.network.CheckNetworkConnection;
import com.nick.sampleffmpeg.network.CustomDialogs;
import com.nick.sampleffmpeg.network.RequestBean;
import com.nick.sampleffmpeg.network.RequestHandler;
import com.nick.sampleffmpeg.network.RequestListner;
import com.nick.sampleffmpeg.ui.activity.LoginScreen;
import com.nick.sampleffmpeg.ui.activity.UploadingVideoScreen;

import org.apache.http.NameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ibrahim Ulukaya <ulukaya@google.com>
 *         <p/>
 *         Intent service to handle uploads.
 */
public class UploadService extends IntentService {

    /**
     * defines how long we'll wait for a video to finish processing
     */
    private static final int PROCESSING_TIMEOUT_SEC = 60 * 20; // 20 minutes

    /**
     * controls how often to poll for video processing status
     */
    private static final int PROCESSING_POLL_INTERVAL_SEC = 60;
    /**
     * how long to wait before re-trying the upload
     */
    private static final int UPLOAD_REATTEMPT_DELAY_SEC = 10;
    /**
     * max number of retry attempts
     */
    private static final int MAX_RETRY = 3;
    private static final String TAG = "UploadService";
    /**
     * processing start time
     */

    private static long mStartTime;
    final HttpTransport transport = AndroidHttp.newCompatibleTransport();
    final JsonFactory jsonFactory = new GsonFactory();
    //  GoogleAccountCredential credential;
    /**
     * tracks the number of upload attempts
     */
    private int mUploadAttemptCount;
    GoogleCredential credential =null;
    Intent intent;
    Uri fileUri;
    public UploadService() {
        super("YTUploadService");
    }
    YoutubeDataBean youtubeDataBean;
    private static void zzz(int duration) throws InterruptedException {
        Log.d(TAG, String.format("Sleeping for [%d] ms ...", duration));
        Thread.sleep(duration);
        Log.d(TAG, String.format("Sleeping for [%d] ms ... done", duration));
    }

    private static boolean timeoutExpired(long startTime, int timeoutSeconds) {
        long currTime = System.currentTimeMillis();
        long elapsed = currTime - startTime;
        if (elapsed >= timeoutSeconds * 1000) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        registerReceiver(receiver, new IntentFilter(UploadingVideoScreen.ACTION_PROGRESS_UPDATE));
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == UploadingVideoScreen.ACTION_CANCEL_UPLOAD) {
                stopSelf();
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (receiver != null) {
            unregisterReceiver(receiver);
        }

        Log.d("Destroy", "Destroy");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        this.intent =intent;
         fileUri = intent.getData();
         youtubeDataBean = new YoutubeDataBean();
        youtubeDataBean.setVideoTitle(intent.getStringExtra(LoginScreen.VIDEO_TITLE));
        youtubeDataBean.setVideoType(intent.getStringExtra(LoginScreen.VIDEO_TYPE));
        youtubeDataBean.setVideoTags(intent.getStringExtra(UploadingVideoScreen.VIDEO_TAGS));
        youtubeDataBean.setVideoDescription(intent.getStringExtra(UploadingVideoScreen.VIDEO_DESCRIPTION));




 /*       TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.setRefreshToken(intent.getStringExtra(UploadingVideoScreen.ACCESS_TOKEN));*/

     /*   GoogleCredential credential =  createCredentialWithRefreshToken(com.nick.sampleffmpeg.ui.activity.Auth.HTTP_TRANSPORT, com.nick.sampleffmpeg.ui.activity.Auth.JSON_FACTORY
                , tokenResponse);
        */


       /* GoogleCredential credential = new GoogleCredential.Builder()
                .setTransport(com.nick.sampleffmpeg.ui.activity.Auth.HTTP_TRANSPORT).setJsonFactory(com.nick.sampleffmpeg.ui.activity.Auth.JSON_FACTORY)
                .setClientSecrets(Constant.CLIENT_ID, Constant.CLIENT_SECRATE).build();
        credential.setAccessToken(intent.getStringExtra(UploadingVideoScreen.ACCESS_TOKEN));
        credential.setRefreshToken(intent.getStringExtra(UploadingVideoScreen.REFREST_TOKEN));*/


        credential = new GoogleCredential.Builder()
                .setTransport(com.nick.sampleffmpeg.ui.activity.Auth.HTTP_TRANSPORT).setJsonFactory(com.nick.sampleffmpeg.ui.activity.Auth.JSON_FACTORY)
                .setClientSecrets("815107145608-2hc3kfand4bomob5thte673amk17k4c2.apps.googleusercontent.com", "46ZZiJ5z01zj7Lgoz9f35Fd0").build();
        credential.setAccessToken(intent.getStringExtra(UploadingVideoScreen.ACCESS_TOKEN));


        String appName = getResources().getString(R.string.app_name);
        final YouTube youtube =
                new YouTube.Builder(transport, jsonFactory, credential).setApplicationName(
                        appName).build();


        try {


            tryUploadAndShowSelectableNotification(fileUri, youtube, youtubeDataBean);
        } catch (InterruptedException e) {
            // ignore
        }
    }

    public static Credential createCredentialWithRefreshToken(
            HttpTransport transport, JsonFactory jsonFactory, TokenResponse tokenResponse) {
        return new Credential.Builder(BearerToken.authorizationHeaderAccessMethod()).setTransport(
                transport)
                .setJsonFactory(jsonFactory)
                .setClientAuthentication(new BasicAuthentication(Constant.CLIENT_ID, Constant.CLIENT_SECRATE))
                .build()
                .setFromTokenResponse(tokenResponse);
    }


    private void tryUploadAndShowSelectableNotification(final Uri fileUri, final YouTube youtube, final YoutubeDataBean youtubeDataBean) throws InterruptedException {
        while (true) {
            Log.i(TAG, String.format("Uploading [%s] to YouTube", fileUri.toString()));
            String videoId = tryUpload(fileUri, youtube, youtubeDataBean);
            if (videoId != null) {
                Log.i(TAG, String.format("Uploaded video with ID: %s", videoId));
                tryShowSelectableNotification(videoId, youtube);
                return;
            } else {




                Log.e(TAG, String.format("Failed to upload %s", fileUri.toString()));
                if (mUploadAttemptCount++ < MAX_RETRY) {
                    Log.i(TAG, String.format("Will retry to upload the video ([%d] out of [%d] reattempts)",
                            mUploadAttemptCount, MAX_RETRY));
                    zzz(UPLOAD_REATTEMPT_DELAY_SEC * 1000);
                 //   geyCredentials();
                } else {
                    Intent intent1 = new Intent(UploadingVideoScreen.ACTION_CANCEL_UPLOAD);
                    sendBroadcast(intent1);
                    Log.e(TAG, String.format("Giving up on trying to upload %s after %d attempts",
                            fileUri.toString(), mUploadAttemptCount));
                    return;
                }
            }
        }
    }

    public void geyCredentials() {
        try {
            if (CheckNetworkConnection.isNetworkAvailable(this)) {
                List<NameValuePair> paramePairs = new ArrayList<NameValuePair>();
                RequestBean requestBean = new RequestBean();
                requestBean.setUrl("update_youtube_api_credentials.php");
                requestBean.setParams(paramePairs);
                requestBean.setIsProgressBarEnable(false);
                RequestHandler requestHandler = new RequestHandler(requestBean, requestCredentials);
                requestHandler.execute(null, null, null);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private RequestListner requestCredentials = new RequestListner() {

        @Override
        public void getResponse(JSONObject jsonObject) {
            try {
                String url = "";
                if (jsonObject != null) {
                    if (!jsonObject.isNull("yt_credentials")) {
                        if (!jsonObject.getJSONObject("yt_credentials").isNull("access_token")) {
                            credential = new GoogleCredential.Builder()
                                    .setTransport(com.nick.sampleffmpeg.ui.activity.Auth.HTTP_TRANSPORT).setJsonFactory(com.nick.sampleffmpeg.ui.activity.Auth.JSON_FACTORY)
                                    .setClientSecrets("815107145608-2hc3kfand4bomob5thte673amk17k4c2.apps.googleusercontent.com", "46ZZiJ5z01zj7Lgoz9f35Fd0").build();
                            credential.setAccessToken(jsonObject.getJSONObject("yt_credentials").getString("access_token"));
                            String appName = getResources().getString(R.string.app_name);
                            final YouTube youtube =
                                    new YouTube.Builder(transport, jsonFactory, credential).setApplicationName(
                                            appName).build();
                            try {


                                tryUploadAndShowSelectableNotification(fileUri, youtube, youtubeDataBean);
                            } catch (InterruptedException e) {
                                // ignore
                            }
                        }



                    }
                }
                //uploadVideo(access_token, refresh_token);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private void tryShowSelectableNotification(final String videoId, final YouTube youtube)
            throws InterruptedException {
        mStartTime = System.currentTimeMillis();
        boolean processed = false;
        while (!processed) {
            processed = ResumableUpload.checkIfProcessed(videoId, youtube);
            if (!processed) {
                // wait a while
                Log.d(TAG, String.format("Video [%s] is not processed yet, will retry after [%d] seconds",
                        videoId, PROCESSING_POLL_INTERVAL_SEC));
                if (!timeoutExpired(mStartTime, PROCESSING_TIMEOUT_SEC)) {
                    zzz(PROCESSING_POLL_INTERVAL_SEC * 1000);
                } else {
                    Log.d(TAG, String.format("Bailing out polling for processing status after [%d] seconds",
                            PROCESSING_TIMEOUT_SEC));
                    return;
                }
            } else {
                ResumableUpload.showSelectableNotification(videoId, getApplicationContext());
                return;
            }
        }
    }

    private String tryUpload(Uri mFileUri, YouTube youtube, YoutubeDataBean youtubeDataBean) {
        long fileSize;
        InputStream fileInputStream = null;
        String videoId = null;
        try {
            fileSize = getContentResolver().openFileDescriptor(mFileUri, "r").getStatSize();
            fileInputStream = getContentResolver().openInputStream(mFileUri);
            String[] proj = {MediaStore.Images.Media.DATA};
           /* Cursor cursor = getContentResolver().query(mFileUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();*/

            videoId = ResumableUpload.upload(youtube, fileInputStream, fileSize, getApplicationContext(), youtubeDataBean);


        } catch (FileNotFoundException e) {
            Log.e(getApplicationContext().toString(), e.getMessage());
        } finally {
            try {
                fileInputStream.close();
            } catch (IOException e) {
                // ignore
            }
        }
        return videoId;
    }

}
