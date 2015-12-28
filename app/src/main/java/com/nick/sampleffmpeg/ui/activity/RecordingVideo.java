package com.nick.sampleffmpeg.ui.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nick.sampleffmpeg.Define.Constant;
import com.nick.sampleffmpeg.R;
import com.nick.sampleffmpeg.ui.control.UITouchButton;
import com.nick.sampleffmpeg.ui.view.VideoCaptureView;
import com.nick.sampleffmpeg.utils.FileUtils;
import com.nick.sampleffmpeg.utils.LogFile;
import com.nick.sampleffmpeg.utils.StringUtils;
import com.nick.sampleffmpeg.utils.ffmpeg.FFMpegUtils;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by baebae on 12/22/15.
 */
public class RecordingVideo extends BaseActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

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

    private boolean isRecording = false;
    private static final String TAG = "Recorder";
    private static boolean flagInitialized = false;

    private int recordingTime = 0;
    private Thread timerThread = null;



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
    }

    @Override
    protected void onResume() {
        super.onResume();
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

                            convertVideoToUniqueFormat();
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
     * Convert recording video into unique video format 1280 * 720 720p format
     */
    private void convertVideoToUniqueFormat() {

        String commands = "-y -threads 5 -i src.mp4 -crf 30 -preset ultrafast -ar 44100 -c:a aac -strict experimental -s 1280x720 -r 30 -force_key_frames expr:gte(t,n_forced*1) -c:v libx264 dst.mp4";

        String srcVideoFilePath = Constant.getCameraVideo();
        String dstVideoFilePath = Constant.getConvertedVideo();

        commands = commands.replace("src.mp4", srcVideoFilePath);
        commands = commands.replace("dst.mp4", dstVideoFilePath);

        String[] command = commands.split(" ");

        progressDialog.show();
        progressDialog.setMessage(getString(R.string.str_convert_camera_unique_format));
        FFMpegUtils.execFFmpegBinary(command, new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                extractAudioFromVideo();
            }
        });
    }

    /**
     * Convert recording video into unique video format 1280 * 720 720p format
     */
    private void convertTopTailVideoToUniqueFormat(final boolean flagTop) {

        String commands = "-y -threads 5 -i src.mp4 -crf 30 -preset ultrafast -ar 44100 -c:a aac -strict experimental -s 1280x720 -r 30 -force_key_frames expr:gte(t,n_forced*1) -c:v libx264 dst.mp4";
        String srcVideoFilePath = "";
        String dstVideoFilePath = "";
        progressDialog.show();

        if (flagTop) {
            srcVideoFilePath = Constant.getAssetTopVideo();
            dstVideoFilePath = Constant.getTopVideo();
            progressDialog.setMessage(getString(R.string.str_convert_asset_top_video));
        } else {
            srcVideoFilePath = Constant.getAssetTailVideo();
            dstVideoFilePath = Constant.getTailVideo();
            progressDialog.setMessage(getString(R.string.str_convert_asset_tail_video));
        }

        commands = commands.replace("src.mp4", srcVideoFilePath);
        commands = commands.replace("dst.mp4", dstVideoFilePath);

        String[] command = commands.split(" ");

        FFMpegUtils.execFFmpegBinary(command, new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                if (flagTop) {
                    convertTopTailVideoToUniqueFormat(false);
                } else {
                    showActivity(EditingVideo.class, null);
                }
            }
        });
    }

    /**
     * extract wav format from video file to show timeline on next page
     */
    private void extractAudioFromVideo() {

        String commands = "-y -i src.mp4 -vn dst.wav";

        String srcVideoFilePath = Constant.getConvertedVideo();
        String dstAudioFilePath = Constant.getConvertedAudio();

        commands = commands.replace("src.mp4", srcVideoFilePath);
        commands = commands.replace("dst.wav", dstAudioFilePath);

        String[] command = commands.split(" ");
        progressDialog.show();
        progressDialog.setMessage(getString(R.string.str_extract_audio));

        FFMpegUtils.execFFmpegBinary(command, new Runnable() {
            @Override
            public void run() {
                if (FileUtils.isExistFile(Constant.getTailVideo()) && FileUtils.isExistFile(Constant.getTopVideo())) {
                    progressDialog.dismiss();
                    RecordingVideo.this.finish();
                    showActivity(EditingVideo.class, null);
                } else {
                    convertTopTailVideoToUniqueFormat(true);
                }
            }
        });
    }
}
