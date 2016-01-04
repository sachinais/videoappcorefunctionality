package com.nick.sampleffmpeg.ui.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.Gravity;
import android.view.OrientationEventListener;
import android.view.Surface;
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
import com.nick.sampleffmpeg.R;
import com.nick.sampleffmpeg.bean.ProvinceBean;
import com.nick.sampleffmpeg.ui.control.UITouchButton;
import com.nick.sampleffmpeg.ui.view.VideoCaptureView;
import com.nick.sampleffmpeg.utils.FileUtils;
import com.nick.sampleffmpeg.utils.LogFile;
import com.nick.sampleffmpeg.utils.StringUtils;
import com.nick.sampleffmpeg.utils.ffmpeg.FFMpegUtils;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by baebae on 12/22/15.
 */
public class RecordingVideo extends BaseActivity implements ActivityCompat.OnRequestPermissionsResultCallback {
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

    @InjectView(R.id.img_recording_frame_border)
    ImageView imgRecordingFrameBorderLayout;

    @InjectView(R.id.img_status_recording)
    ImageView imgStatusRecording;

    @InjectView(R.id.txt_recording_time)
    TextView txtRecordingTime;
    @InjectView(R.id.rl_Menu)
    RelativeLayout rl_Menu;

    private boolean isRecording = false;
    private static final String TAG = "Recorder";
    private static boolean flagInitialized = false;

    private int recordingTime = 0;
    private Thread timerThread = null;
    private Dialog optionDialog;

    SimpleOrientationListener mOrientationListener = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FileUtils.DeleteFolder(Constant.getPreviewDirectory());
        FileUtils.DeleteFolder(Constant.getOverlayDirectory());

        setContentView(R.layout.recording_video_view);
        ButterKnife.inject(this);

        initializeUIControls();
        showReadyForRecordingLayout();

        LogFile.clearLogText();
        try {
            FileUtils.copyAssets(this, Constant.getApplicationDirectory());
        } catch (Exception e) {
            LogFile.logText("Error on copy assets" + e.getMessage(), null);
        }

        mOrientationListener = new SimpleOrientationListener(RecordingVideo.this) {

            @Override
            public void onSimpleOrientationChanged(int orientation) {
                if(orientation == Configuration.ORIENTATION_LANDSCAPE){
                    findViewById(R.id.left_area).setVisibility(View.GONE);
                    findViewById(R.id.right_area).setVisibility(View.GONE);
//                    cameraView.onOrientationChanged(1);
                }else if(orientation == Configuration.ORIENTATION_PORTRAIT){
                    findViewById(R.id.left_area).setVisibility(View.VISIBLE);
                    findViewById(R.id.right_area).setVisibility(View.VISIBLE);
//                    cameraView.onOrientationChanged(0);
                }
            }
        };

        timerThread = (new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.interrupted())
                    try {
                        Thread.sleep(1000);
                        recordingTime ++;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String time = StringUtils.getMinuteSecondString(recordingTime, true);
                                txtRecordingTime.setText(time);
                            }
                        });
                    } catch (InterruptedException e) {
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
        showPicker();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerSensorListener();
        if (flagInitialized) {
            cameraView.stopVideoCapture();
            flagInitialized = false;
        }

        if (timerThread != null && !timerThread.isAlive()) {
            timerThread.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopSensorListener();
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
                        if (!isRecording) {
                            isRecording = true;
                            cameraView.startVideoCapture();
                            showRecordingLayout();
                            recordingTime = 0;
                        }
                    }
                });

        UITouchButton.applyEffect(btnSwitchCamera, UITouchButton.EFFECT_ALPHA, Constant.BUTTON_NORMAL_ALPHA,
                Constant.BUTTON_FOCUS_ALPHA, new Runnable() {
                    @Override
                    public void run() {
                        cameraView.switchCamera();
                    }
                });

        UITouchButton.applyEffect(btnStopCapture, UITouchButton.EFFECT_ALPHA, Constant.BUTTON_NORMAL_ALPHA,
                Constant.BUTTON_FOCUS_ALPHA, new Runnable() {
                    @Override
                    public void run() {
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
                        cameraView.startVideoCapture();
                        isRecording = true;
                        recordingTime = 0;
                    }
                });
    }

    /**
     * showing recording layout like as stop button, cancel & restart button, orange frame border, etc
     */
    private void showRecordingLayout() {
        btnStopCapture.setVisibility(View.VISIBLE);
        imgRecordingFrameBorderLayout.setVisibility(View.VISIBLE);
        btnRestartCapture.setVisibility(View.VISIBLE);
        imgStatusRecording.setVisibility(View.VISIBLE);
        txtRecordingTime.setVisibility(View.VISIBLE);
        btnStartCapture.setVisibility(View.GONE);
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
    }

    /**
     * extract wav format from video file to show timeline on next page
     */
    private void extractAudioFromVideo() {

        String commands = "-y -i src.mp4 -vn dst.wav";

        String srcVideoFilePath = Constant.getCameraVideo();
        String dstAudioFilePath = Constant.getConvertedAudio();

        commands = commands.replace("src.mp4", srcVideoFilePath);
        commands = commands.replace("dst.wav", dstAudioFilePath);

        String[] command = commands.split(" ");
        progressDialog.show();
        progressDialog.setMessage(getString(R.string.str_extract_audio));

        FFMpegUtils.execFFmpegBinary(command, new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                RecordingVideo.this.finish();
                showActivity(EditingVideo.class, null);
            }
        });
    }

    private void showDialog() {
        optionDialog = new Dialog(RecordingVideo.this, R.style.DialogSlideAnim);
        optionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(optionDialog.getWindow().getAttributes());
        lp.gravity = Gravity.BOTTOM;
        optionDialog.getWindow().setAttributes(lp);
        optionDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        optionDialog.setContentView(R.layout.dialog_menu_options);
        ((Button)optionDialog.findViewById(R.id.btnChangeTemplate)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                optionDialog.dismiss();
                pvOptions.show();


            }
        });
        ((Button)optionDialog.findViewById(R.id.btnOpenDashboard)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        ((Button)optionDialog.findViewById(R.id.btnCancel)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                optionDialog.dismiss();
            }
        });

        optionDialog.show();


    }
    private void showPicker(){
        options1Items.add(new ProvinceBean(0,"Rose Gold","2","3"));
        options1Items.add(new ProvinceBean(1, "Space Gray", "2", "3"));
        options1Items.add(new ProvinceBean(3,"Silver","3",""));
        options1Items.add(new ProvinceBean(0,"Rose Gold","2","3"));
        options1Items.add(new ProvinceBean(1, "Space Gray", "2", "3"));
        options1Items.add(new ProvinceBean(3, "Silver", "3", ""));
        pvOptions = new OptionsPickerView(this);
        pvOptions.setPicker(options1Items);
        pvOptions.setTitle("Select a Template");
        pvOptions.setCyclic(false, true, true);
        pvOptions.setSelectOptions(1, 1, 1);
        pvOptions.setOnoptionsSelectListener(new OptionsPickerView.OnOptionsSelectListener() {

            @Override
            public void onOptionsSelect(int options1, int option2, int options3) {
                //返回的分别是三个级别的选中位置
               /* String tx = options1Items.get(options1).getPickerViewText()
                        + options2Items.get(options1).get(option2)
                        + options3Items.get(options1).get(option2).get(options3);
                tvOptions.setText(tx);
                vMasker.setVisibility(View.GONE);*/
                Toast.makeText(getBaseContext(), options1Items.get(options1).getPickerViewText(), Toast.LENGTH_LONG).show();
            }
        });
        //点击弹出选项选择器

    }

    public abstract class SimpleOrientationListener extends OrientationEventListener {

        public static final int CONFIGURATION_ORIENTATION_UNDEFINED = Configuration.ORIENTATION_UNDEFINED;
        private volatile int defaultScreenOrientation = CONFIGURATION_ORIENTATION_UNDEFINED;
        public int prevOrientation = OrientationEventListener.ORIENTATION_UNKNOWN;
        private Context ctx;
        private ReentrantLock lock = new ReentrantLock(true);

        public SimpleOrientationListener(Context context) {
            super(context);
            ctx = context;
        }

        public SimpleOrientationListener(Context context, int rate) {
            super(context, rate);
            ctx = context;
        }

        @Override
        public void onOrientationChanged(final int orientation) {
            int currentOrientation = OrientationEventListener.ORIENTATION_UNKNOWN;
            if (orientation >= 330 || orientation < 30) {
                currentOrientation = Surface.ROTATION_0;
            } else if (orientation >= 60 && orientation < 120) {
                currentOrientation = Surface.ROTATION_90;
            } else if (orientation >= 150 && orientation < 210) {
                currentOrientation = Surface.ROTATION_180;
            } else if (orientation >= 240 && orientation < 300) {
                currentOrientation = Surface.ROTATION_270;
            }

            if (prevOrientation != currentOrientation && orientation != OrientationEventListener.ORIENTATION_UNKNOWN) {
                prevOrientation = currentOrientation;
                if (currentOrientation != OrientationEventListener.ORIENTATION_UNKNOWN)
                    reportOrientationChanged(currentOrientation);
            }

        }

        private void reportOrientationChanged(final int currentOrientation) {

            int defaultOrientation = getDeviceDefaultOrientation();
            int orthogonalOrientation = defaultOrientation == Configuration.ORIENTATION_LANDSCAPE ? Configuration.ORIENTATION_PORTRAIT
                    : Configuration.ORIENTATION_LANDSCAPE;

            int toReportOrientation;

            if (currentOrientation == Surface.ROTATION_0 || currentOrientation == Surface.ROTATION_180)
                toReportOrientation = defaultOrientation;
            else
                toReportOrientation = orthogonalOrientation;

            onSimpleOrientationChanged(toReportOrientation);
        }

        /**
         * Must determine what is default device orientation (some tablets can have default landscape). Must be initialized when device orientation is defined.
         *
         * @return value of {@link Configuration#ORIENTATION_LANDSCAPE} or {@link Configuration#ORIENTATION_PORTRAIT}
         */
        private int getDeviceDefaultOrientation() {
            if (defaultScreenOrientation == CONFIGURATION_ORIENTATION_UNDEFINED) {
                lock.lock();
                defaultScreenOrientation = initDeviceDefaultOrientation(ctx);
                lock.unlock();
            }
            return defaultScreenOrientation;
        }

        /**
         * Provides device default orientation
         *
         * @return value of {@link Configuration#ORIENTATION_LANDSCAPE} or {@link Configuration#ORIENTATION_PORTRAIT}
         */
        private int initDeviceDefaultOrientation(Context context) {

            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Configuration config = context.getResources().getConfiguration();
            int rotation = windowManager.getDefaultDisplay().getRotation();

            boolean isLand = config.orientation == Configuration.ORIENTATION_LANDSCAPE;
            boolean isDefaultAxis = rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180;

            int result = CONFIGURATION_ORIENTATION_UNDEFINED;
            if ((isDefaultAxis && isLand) || (!isDefaultAxis && !isLand)) {
                result = Configuration.ORIENTATION_LANDSCAPE;
            } else {
                result = Configuration.ORIENTATION_PORTRAIT;
            }
            return result;
        }

        /**
         * Fires when orientation changes from landscape to portrait and vice versa.
         *
         * @param orientation value of {@link Configuration#ORIENTATION_LANDSCAPE} or {@link Configuration#ORIENTATION_PORTRAIT}
         */
        public abstract void onSimpleOrientationChanged(int orientation);

    }

    private void registerSensorListener () {
        mOrientationListener.enable();
    }

    private void stopSensorListener() {
        mOrientationListener.disable();
        //SensorManager sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        //sensorManager.unregisterListener(sensorListener);
    }
}
