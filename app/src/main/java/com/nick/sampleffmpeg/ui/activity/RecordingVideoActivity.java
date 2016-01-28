package com.nick.sampleffmpeg.ui.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bigkoo.pickerview.OptionsPickerView;
import com.nick.sampleffmpeg.Define.Constant;
import com.nick.sampleffmpeg.MainApplication;
import com.nick.sampleffmpeg.R;
import com.nick.sampleffmpeg.bean.ProvinceBean;
import com.nick.sampleffmpeg.network.CheckNetworkConnection;
import com.nick.sampleffmpeg.network.CustomDialogs;
import com.nick.sampleffmpeg.network.RequestBean;
import com.nick.sampleffmpeg.network.RequestHandler;
import com.nick.sampleffmpeg.network.RequestListner;
import com.nick.sampleffmpeg.sharedpreference.SPreferenceKey;
import com.nick.sampleffmpeg.sharedpreference.SharedPreferenceWriter;
import com.nick.sampleffmpeg.ui.control.DonutProgress;
import com.nick.sampleffmpeg.ui.control.UITouchButton;
import com.nick.sampleffmpeg.ui.view.OverlayView;
import com.nick.sampleffmpeg.ui.view.VideoCaptureView;
import com.nick.sampleffmpeg.utils.AppConstants;
import com.nick.sampleffmpeg.utils.FileDownloader;
import com.nick.sampleffmpeg.utils.FileUtils;
import com.nick.sampleffmpeg.utils.LogFile;
import com.nick.sampleffmpeg.utils.StringUtils;
import com.nick.sampleffmpeg.utils.ffmpeg.FFMpegUtils;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by baebae on 12/22/15.
 */
public class RecordingVideoActivity extends BaseActivity implements ActivityCompat.OnRequestPermissionsResultCallback {
    private ArrayList<ProvinceBean> options1Items = new ArrayList<ProvinceBean>();
    private OptionsPickerView pvOptions;
    @InjectView(R.id.btnStartCapture)
    ImageView btnStartCapture;

    @InjectView(R.id.btnSwitchCamera)
    ImageView btnSwitchCamera;

    @InjectView(R.id.btnRestartCapture)
    ImageView btnRestartCapture;

    @InjectView(R.id.btnStopCapture)
    ImageView btnStopCapture;

    @InjectView(R.id.surface_view)
    VideoCaptureView cameraView;

    @InjectView(R.id.overlayview)
    OverlayView overlayview;

    @InjectView(R.id.img_recording_frame_border)
    ImageView imgRecordingFrameBorderLayout;

    @InjectView(R.id.img_status_recording)
    ImageView imgStatusRecording;

    @InjectView(R.id.txt_recording_time)
    TextView txtRecordingTime;

    @InjectView(R.id.txtCountDown)
    TextView txtCountDown;

    @InjectView(R.id.rl_Menu)
    RelativeLayout rl_Menu;

    @InjectView(R.id.progressCountDown)
    DonutProgress progressCountDown;

    private boolean isRecording = false;
    private static final String TAG = "Recorder";
    private boolean flagInitialized = false;

    private float recordingTime = 0;
    private Thread timerThread = null;
    private Dialog optionDialog;

    private double defaultWidth = 0;
    private double defaultHeight = 0;

    private int countDownValue = -1000;
    private SharedPreferenceWriter sharedPreferenceWriter = null;
    private  String RequestType = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FileUtils.DeleteFolder(Constant.getPreviewDirectory());
        FileUtils.DeleteFolder(Constant.getOverlayDirectory());
        setContentView(R.layout.recording_video_view);
        getStoredTemplateIfNull();
        ButterKnife.inject(this);
        getWindow().setFormat(PixelFormat.UNKNOWN);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initializeUIControls();
        showReadyForRecordingLayout();

        progressCountDown.setProgress(10);
        LogFile.clearLogText();
//        try {
//            FileUtils.copyAssets(this, Constant.getApplicationDirectory());
//        } catch (Exception e) {
//            LogFile.logText("Error on copy assets" + e.getMessage(), null);
//        }

        sharedPreferenceWriter = SharedPreferenceWriter.getInstance(this);
        timerThread = (new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.interrupted()) {
                    if (isRecording) {
                        try {
                            Thread.sleep(500);
                            recordingTime += 0.5f;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (recordingTime > 90.f) {
                                        imgRecordingFrameBorderLayout.setImageDrawable(getResources().getDrawable(R.drawable.frame_recording_border_red));
                                        txtRecordingTime.setTextColor(Color.argb(255, 255, 0, 0));
                                    }else if (recordingTime > 75 ) {
                                        imgRecordingFrameBorderLayout.setImageDrawable(getResources().getDrawable(R.drawable.frame_recording_border_orange));
                                        txtRecordingTime.setTextColor(Color.argb(255, 255, 138, 0));
                                    }else {
                                        imgRecordingFrameBorderLayout.setImageDrawable(getResources().getDrawable(R.drawable.frame_recording_border_green));
                                        txtRecordingTime.setTextColor(Color.argb(255, 255, 255, 255));
                                    }
                                    if (imgStatusRecording.getVisibility() == View.VISIBLE) {
                                        imgStatusRecording.setVisibility(View.GONE);
                                    } else {
                                        imgStatusRecording.setVisibility(View.VISIBLE);
                                    }
                                    String time = StringUtils.getMinuteSecondString((int) recordingTime, true);
                                    txtRecordingTime.setText(time);
                                }
                            });
                        } catch (InterruptedException e) {
                        }
                    } else {
                        try {
                            final int threadStep = 50;
                            Thread.sleep(threadStep);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (countDownValue > 0) {
                                        txtCountDown.setVisibility(View.VISIBLE);
                                        progressCountDown.setVisibility(View.VISIBLE);
                                        int percent = countDownValue % 1000;
                                        int value = (countDownValue - percent) / 1000;

                                        percent = 1000 - percent;
                                        progressCountDown.setProgress(percent / 10);
                                        txtCountDown.setText(Integer.toString(value + 1));
                                    } else {
                                        txtCountDown.setVisibility(View.GONE);
                                        progressCountDown.setVisibility(View.GONE);
                                    }

                                    if (countDownValue == -threadStep) {
                                        recordingTime = 0;
                                        txtRecordingTime.setText("");
                                        isRecording = true;
                                        showRecordingLayout();
                                    }

                                    countDownValue -= threadStep;
                                }
                            });
                        } catch (InterruptedException e) {
                        }
                    }
                }

            }
        }));

        timerThread.start();

        rl_Menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });
        findViewById(R.id.img_left_person_template).setVisibility(View.VISIBLE);
        findViewById(R.id.img_right_person_template).setVisibility(View.INVISIBLE);
        try {
            showPicker();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        selectTemplateItem(0);
    }

    private void getStoredTemplateIfNull() {
        try {
            if (MainApplication.getInstance().getTemplateArray() == null) {
                MainApplication.getInstance().setTemplateArray(new JSONObject(SharedPreferenceWriter.getInstance(RecordingVideoActivity.this).getString(SPreferenceKey.TEMPLATE_ARRAY)).getJSONArray("templates"));
            }
        } catch (Exception e) {

        }
    }





    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (flagInitialized) {
                cameraView.stopVideoCapture();
                flagInitialized = false;
            }

            if (timerThread != null && !timerThread.isAlive()) {
                timerThread.start();
            }

        } catch (Exception e) {

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        flagInitialized = true;
        cameraView.stopPreview();
        if (timerThread != null && timerThread.isAlive()) {
            timerThread.interrupt();
        }

    }

    /**
     * Add touch event on activity.
     */
    private void initializeUIControls() {
        UITouchButton.applyEffect(btnStartCapture, UITouchButton.EFFECT_ALPHA, Constant.BUTTON_NORMAL_ALPHA,
                Constant.BUTTON_FOCUS_ALPHA, new Runnable() {
                    @Override
                    public void run() {
                        if (findViewById(R.id.layout_loading_template).getVisibility() == View.VISIBLE) {
                            showAlert(R.string.str_alert_title_information, "Template is still loading.", "OK");
                            return;
                        }
                        if (!isRecording) {
                            showCountDownLayout();
                            cameraView.startVideoCapture(new Runnable() {
                                @Override
                                public void run() {
                                    countDownValue = 2900;
                                }
                            });
                            recordingTime = 0;
                        }


                    }
                });

        UITouchButton.applyEffect(btnSwitchCamera, UITouchButton.EFFECT_ALPHA, Constant.BUTTON_NORMAL_ALPHA,
                Constant.BUTTON_FOCUS_ALPHA, new Runnable() {
                    @Override
                    public void run() {
                        cameraView.switchCamera();
                        overlayview.setRecordingView(true, cameraView.isFrontCamera());
                        if (cameraView.isFrontCamera()) {
                            findViewById(R.id.img_left_person_template).setVisibility(View.VISIBLE);
                            findViewById(R.id.img_right_person_template).setVisibility(View.INVISIBLE);
                        } else {
                            findViewById(R.id.img_left_person_template).setVisibility(View.INVISIBLE);
                            findViewById(R.id.img_right_person_template).setVisibility(View.VISIBLE);
                        }
                        overlayview.updateOverlay();
                    }
                });

        UITouchButton.applyEffect(btnStopCapture, UITouchButton.EFFECT_ALPHA, Constant.BUTTON_NORMAL_ALPHA,
                Constant.BUTTON_FOCUS_ALPHA, new Runnable() {
                    @Override
                    public void run() {
                        if (recordingTime <= 5.f) {
                            showAlert(R.string.str_alert_short_video_title, "Be sure you have recorded at least 5 seconds of footage before stopping your recording.", "OK");
                            return;
                        }
                        if (isRecording) {
                            isRecording = false;
                            cameraView.stopPreview();
                            showReadyForRecordingLayout();
                            extractAudioFromVideo();
                            //convertVideoToUniqueFormat();
                        }
                    }
                });

        UITouchButton.applyEffect(btnRestartCapture, UITouchButton.EFFECT_ALPHA, Constant.BUTTON_NORMAL_ALPHA,
                Constant.BUTTON_FOCUS_ALPHA, new Runnable() {
                    @Override
                    public void run() {
                        showAlert(R.string.str_alert_title_information, R.string.str_stop_recording,
                                getString(R.string.str_yes), new Runnable() {
                                    @Override
                                    public void run() {
                                        cameraView.stopVideoCapture();
                                        showReadyForRecordingLayout();
                                        isRecording = false;
                                    }
                                }, getString(R.string.str_no));


                    }
                });

        overlayview.setRecordingView(true, cameraView.isFrontCamera());
    }

    /**
     * showing recording layout like as stop button, cancel & restart button, orange frame border, etc
     */
    private void showRecordingLayout() {
        btnStopCapture.setVisibility(View.VISIBLE);
        imgRecordingFrameBorderLayout.setVisibility(View.VISIBLE);
        imgRecordingFrameBorderLayout.setImageDrawable(getResources().getDrawable(R.drawable.frame_recording_border_green));
        btnRestartCapture.setVisibility(View.VISIBLE);
        imgStatusRecording.setVisibility(View.VISIBLE);
        txtRecordingTime.setVisibility(View.VISIBLE);
        btnSwitchCamera.setVisibility(View.GONE);
        btnStartCapture.setVisibility(View.GONE);
        rl_Menu.setVisibility(View.GONE);
    }

    private void showCountDownLayout() {
        btnStopCapture.setVisibility(View.GONE);
        imgRecordingFrameBorderLayout.setVisibility(View.GONE);
        btnRestartCapture.setVisibility(View.GONE);
        imgStatusRecording.setVisibility(View.GONE);
        txtRecordingTime.setVisibility(View.GONE);
        btnSwitchCamera.setVisibility(View.GONE);
        btnStartCapture.setVisibility(View.GONE);
        rl_Menu.setVisibility(View.GONE);
    }

    /**
     * hide recording layout like as stop button, cancel & restart button, orange frame border, etc
     * show start recording button
     */
    private void showReadyForRecordingLayout() {
        btnStopCapture.setVisibility(View.GONE);
        imgRecordingFrameBorderLayout.setVisibility(View.GONE);
        btnRestartCapture.setVisibility(View.GONE);
        imgStatusRecording.setVisibility(View.GONE);
        txtRecordingTime.setVisibility(View.GONE);
        btnStartCapture.setVisibility(View.VISIBLE);
        btnSwitchCamera.setVisibility(View.VISIBLE);
        rl_Menu.setVisibility(View.VISIBLE);
    }

    /**
     * extract wav format from video file to show timeline on next page
     */
    private void extractAudioFromVideo() {

        String commands = "-y -i src.mp4 -vn dst.wav";

        String srcVideoFilePath = Constant.getSourceVideo();
        String dstAudioFilePath = Constant.getConvertedAudio();

        commands = commands.replace("src.mp4", srcVideoFilePath);
        commands = commands.replace("dst.wav", dstAudioFilePath);

        String[] command = commands.split(" ");
        progressDialog.show();
        progressDialog.setMessage(getString(R.string.str_extract_audio));
        FFMpegUtils.killProcesses();
        FFMpegUtils.execFFmpegBinary(command, new FFMpegUtils.Callback() {
            @Override
            public void onProgress(String msg) {

            }

            @Override
            public void onFinish() {
                progressDialog.dismiss();
                RecordingVideoActivity.this.finish();
                if (recordingTime > 15.f) {
                    Constant.updateTimeUnit(2);
                }

                if (recordingTime > 25) {
                    Constant.updateTimeUnit(3);
                }

                if (recordingTime > 40) {
                    Constant.updateTimeUnit(4);
                }
                showActivity(EditingVideoActivity.class, null);
            }
        });
    }

    private void showDialog() {
        optionDialog = new Dialog(RecordingVideoActivity.this, R.style.DialogSlideAnim);
        optionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(optionDialog.getWindow().getAttributes());
        lp.gravity = Gravity.BOTTOM;
        optionDialog.getWindow().setAttributes(lp);
        optionDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        optionDialog.setContentView(R.layout.dialog_menu_options);
        ((Button) optionDialog.findViewById(R.id.btnChangeTemplate)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                optionDialog.dismiss();
                pvOptions.show();
                //sendTemplateRequest();


            }
        });
        ((Button) optionDialog.findViewById(R.id.btnOpenDashboard)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                optionDialog.dismiss();
                RequestType = "DashBoard";
               getSid();
            }
        });
        ((Button) optionDialog.findViewById(R.id.btnAddUser)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                optionDialog.dismiss();

                RequestType = "AddUser";
                getSid();
            }
        });

        ((Button) optionDialog.findViewById(R.id.btnTutorials)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                optionDialog.dismiss();

                RequestType = "Tutorial";
                getSid();

            }
        });
        ((Button) optionDialog.findViewById(R.id.btnCancel)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                optionDialog.dismiss();
            }
        });
        ((Button) optionDialog.findViewById(R.id.btnImportVideo)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                optionDialog.dismiss();

                startActivity(new Intent(RecordingVideoActivity.this, ImportVideoActivty.class));
            }
        });

        ((Button) optionDialog.findViewById(R.id.btnSignOut)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                optionDialog.dismiss();
                finish();
                SharedPreferenceWriter.getInstance().writeStringValue(SPreferenceKey.USERID, "");
                startActivity(new Intent(RecordingVideoActivity.this, LoginActivity.class));

            }
        });

        optionDialog.show();


    }




    public void getSid(){
        try {
            if (CheckNetworkConnection.isNetworkAvailable(RecordingVideoActivity.this)) {
                List<NameValuePair> paramePairs = new ArrayList<NameValuePair>();
                RequestBean requestBean = new RequestBean();
                requestBean.setActivity(RecordingVideoActivity.this);
                requestBean.setUrl("get_sid.php");
                requestBean.setParams(paramePairs);
                requestBean.setIsProgressBarEnable(true);
                RequestHandler requestHandler = new RequestHandler(requestBean, requestSid);
                requestHandler.execute(null, null, null);
            } else {
                CustomDialogs.showOkDialog(RecordingVideoActivity.this, "Please check network connection");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void showPicker() throws JSONException {
        if (MainApplication.getInstance().getTemplateArray() != null && MainApplication.getInstance().getTemplateArray().length() > 0) {
            JSONArray jsonArray = MainApplication.getInstance().getTemplateArray();
            // for (int i = jsonArray.length() - 1; i >= 0; i--) {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                options1Items.add(new ProvinceBean(i, jsonObject.optString("title"), jsonObject.optString("directory"), ""));
            }
        }

       /* options1Items.add(new ProvinceBean(0,"Rose Gold","2","3"));
        options1Items.add(new ProvinceBean(1, "Space Gray", "2", "3"));
        options1Items.add(new ProvinceBean(3, "Silver", "3", ""));
        options1Items.add(new ProvinceBean(0, "Rose Gold", "2", "3"));
        options1Items.add(new ProvinceBean(1, "Space Gray", "2", "3"));
        options1Items.add(new ProvinceBean(3, "Silver", "3", ""));*/
        pvOptions = new OptionsPickerView(this, getDisplayMetric().scaledDensity);
        pvOptions.setPicker(options1Items);
        pvOptions.setTitle("Select a Template");
        pvOptions.setCyclic(false, true, true);
        pvOptions.setSelectOptions(1, 1, 1);
        pvOptions.setOnoptionsSelectListener(new OptionsPickerView.OnOptionsSelectListener() {

            @Override
            public void onOptionsSelect(final int options1, int option2, int options3) {
                selectTemplateItem(options1);
                MainApplication.getInstance().setSelectedTemplePosition(options1);
                //返回的分别是三个级别的选中位置
               /* String tx = options1Items.get(options1).getPickerViewText()
                        + options2Items.get(options1).get(option2)
                        + options3Items.get(options1).get(option2).get(options3);
                tvOptions.setText(tx);
                vMasker.setVisibility(View.GONE);*/
                // Toast.makeText(getBaseContext(), options1Items.get(options1).getPickerViewText(), Toast.LENGTH_LONG).show();


//                MainApplication.getInstance().setTemplate((int)options1Items.get(options1).getId());
//                overlayview.invalidate();
            }
        });

    }

    private void selectTemplateItem(final int options1) {
        if (findViewById(R.id.layout_loading_template).getVisibility() == View.VISIBLE) {
            showAlert(R.string.str_alert_title_information, "Template is still loading.", "OK");
            return;
        }
//
        MainApplication.getInstance().setTemplate((int) options1Items.get(options1).getId());
        overlayview.updateOverlay();

     FileDownloader fileDownloader = new FileDownloader(RecordingVideoActivity.this, getTemplateUrl((int) options1Items.get(options1).getId()), options1Items.get(options1).getPickerViewText(), options1Items.get(options1).getDirectoryId());
       findViewById(R.id.layout_loading_template).setVisibility(View.VISIBLE);
        fileDownloader.startDownload(new Runnable() {
           @Override
            public void run() {
              MainApplication.getInstance().setTemplate((int) options1Items.get(options1).getId());
               runOnUiThread(new Runnable() {
                    @Override
                   public void run() {
                        findViewById(R.id.layout_loading_template).setVisibility(View.GONE);
                        overlayview.updateOverlay();
                   }
                });
           }
        });
    }


    private void sendTemplateRequest() {
        try {
            if (CheckNetworkConnection.isNetworkAvailable(RecordingVideoActivity.this)) {
                List<NameValuePair> paramePairs = new ArrayList<NameValuePair>();
                //   paramePairs.add(new BasicNameValuePair("login_resource",String.valueOf(AppConstants.DEVICE_TYPE_ANDROID)));
                //   paramePairs.add(new BasicNameValuePair("device_key", GCMRegistrar.getRegistrationId(LoginActivity.this)));
                RequestBean requestBean = new RequestBean();
                requestBean.setActivity(RecordingVideoActivity.this);
                requestBean.setUrl("list_templates.php");
                requestBean.setParams(paramePairs);
                requestBean.setIsProgressBarEnable(true);
                RequestHandler requestHandler = new RequestHandler(requestBean, requestListner);
                requestHandler.execute(null, null, null);
            } else {
                CustomDialogs.showOkDialog(RecordingVideoActivity.this, "Please check network connection");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private RequestListner requestSid = new RequestListner() {

        @Override
        public void getResponse(JSONObject jsonObject) {
            try {
                String sid="";
                String url = "";
                if (jsonObject != null) {
                    if(!jsonObject.isNull("sid")){
                     sid=   jsonObject.getString("sid");
                    }

                    if(RequestType.equalsIgnoreCase("DashBoard")){
                        url = "https://live.videomyjob.com/api/app_login.php?user_id="+SharedPreferenceWriter.getInstance().getString(SPreferenceKey.USERID)+" & sid="+sid+" & "+"redirect=1";

                    } else if(RequestType.equalsIgnoreCase("AddUser")){
                        url = "https://live.videomyjob.com/api/app_login.php?user_id="+SharedPreferenceWriter.getInstance().getString(SPreferenceKey.USERID)+" & sid="+sid+" & "+"redirect=2";

                    }else  if(RequestType.equalsIgnoreCase("Tutorial")){
                        url = "https://live.videomyjob.com/api/app_login.php?user_id="+SharedPreferenceWriter.getInstance().getString(SPreferenceKey.USERID)+" & sid="+sid+" & "+"redirect=3";

                    }

                  //  https://live.videomyjob.com/api/app_login.php?user_id=5 & sid=95a65aadd8c905bcc2c0751d96f77aa0 & redirect=1

                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(browserIntent);

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private RequestListner requestListner = new RequestListner() {

        @Override
        public void getResponse(JSONObject jsonObject) {
            try {
                if (jsonObject != null) {
                    parseJsonData(jsonObject);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private void parseJsonData(JSONObject jsonObject) throws JSONException {
        if (!jsonObject.isNull(AppConstants.SUCCESS)) {
            if (jsonObject.getBoolean(AppConstants.SUCCESS)) {
                if (!jsonObject.isNull("user_id")) {
                    SharedPreferenceWriter.getInstance(RecordingVideoActivity.this).writeStringValue(SPreferenceKey.USERID, jsonObject.getString("user_id"));
                }
                if (!jsonObject.isNull("email")) {
                    SharedPreferenceWriter.getInstance(RecordingVideoActivity.this).writeStringValue(SPreferenceKey.EMAIL, jsonObject.getString("email"));
                }
                if (!jsonObject.isNull("firstname")) {
                    SharedPreferenceWriter.getInstance(RecordingVideoActivity.this).writeStringValue(SPreferenceKey.FIRST_NAME, jsonObject.getString("firstname"));
                }
                if (!jsonObject.isNull("lastname")) {
                    SharedPreferenceWriter.getInstance(RecordingVideoActivity.this).writeStringValue(SPreferenceKey.LAST_NAME, jsonObject.getString("lastname"));
                }
                startActivity(new Intent(RecordingVideoActivity.this, RecordingVideoActivity.class));
                RecordingVideoActivity.this.finish();

            } else {
                if (!jsonObject.isNull(AppConstants.MESSAGE)) {
                    Toast.makeText(getBaseContext(), "Error", Toast.LENGTH_LONG).show();

                }
            }

        }
    }

    private String getTemplateUrl(int postion) {
        StringBuilder builder = new StringBuilder();

        try {
            JSONArray jsonArray = MainApplication.getInstance().getTemplateArray();

            builder.append("http://")
                    .append(getServer(sharedPreferenceWriter.getString(SPreferenceKey.REGION)))
                    .append("/company/")
                    .append(sharedPreferenceWriter.getString(SPreferenceKey.COMPANY_DIRECTORY))
                    .append("/" + jsonArray.getJSONObject(postion).optString("directory"))
                    .append("/template_" + jsonArray.getJSONObject(postion).getInt("id") + ".zip");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return builder.toString();
    }

    public String getServer(String company_region) {
        String toReturn = "syd.static.videomyjob.com";

        //Should we ever change the default
        if (company_region == "AU") {
            toReturn = "syd.static.videomyjob.com";
        } else if (company_region == "HN") {
            toReturn = "hn.static.videomyjob.com";
        }

        return toReturn;
    }

    @Override
    public void onBackPressed() {
    }
}
