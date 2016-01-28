package com.nick.sampleffmpeg.ui.view;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.RelativeLayout;

import com.nick.sampleffmpeg.Define.Constant;
import com.nick.sampleffmpeg.utils.FileUtils;
import com.nick.sampleffmpeg.utils.camera.CameraHelper;

import java.util.List;

/**
 * Created by baebae on 12/22/15.
 */
public class VideoCaptureView extends SurfaceView implements SurfaceHolder.Callback {

    private MediaRecorder mMediaRecorder;
    private SurfaceHolder surfaceHolder;

    private Camera mCamera;
    private boolean flagFrontFaceCamera = true;

    private Runnable callback = null;
    private Context context = null;
    public VideoCaptureView(Context context) {
        super(context);
        this.context = context;
        if (!isInEditMode()) {
            initializeSurface();
        }
    }

    public VideoCaptureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        if (!isInEditMode()) {
            initializeSurface();
        }
    }

    public VideoCaptureView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        if (!isInEditMode()) {
            initializeSurface();
        }
    }

    private void initializeSurface() {
        SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        if (sizes == null)
            return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;


        minDiff = Double.MAX_VALUE;
        for (Camera.Size size : sizes) {
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }
        return optimalSize;
    }

    @SuppressLint("NewApi")
    public void initializeRecording(boolean flagPreview) {
        try {
            mMediaRecorder = new MediaRecorder();
            int cameraID = initializeCamera();
            Camera.Parameters p = mCamera.getParameters();
            List<Camera.Size> videoSizes = p.getSupportedVideoSizes();
            mCamera.unlock();

            CamcorderProfile mProfile = CamcorderProfile.get(cameraID, CamcorderProfile.QUALITY_HIGH );
            mMediaRecorder = new MediaRecorder();
            mMediaRecorder.setCamera( mCamera );
            //2nd. Initialized state
            mMediaRecorder.setAudioSource( MediaRecorder.AudioSource.CAMCORDER );
            mMediaRecorder.setVideoSource( MediaRecorder.VideoSource.CAMERA );

            //set correct layout size of video recording view

            double newCameraWidth = getMeasuredWidth();
            double newCameraHeight = getMeasuredHeight();
            double previewWidth = mProfile.videoFrameWidth;
            double previewHeight = mProfile.videoFrameHeight;

            newCameraWidth = previewWidth / previewHeight * newCameraHeight;
            RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams((int)newCameraWidth, (int)newCameraHeight);
            param.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            setLayoutParams(param);

            //3rd. config
            Camera.Size optimalVideoSize = getOptimalPreviewSize(videoSizes, Constant.VIDEO_WIDTH, Constant.VIDEO_HEIGHT);

            mProfile.videoFrameWidth = optimalVideoSize.width;
            mProfile.videoFrameHeight = optimalVideoSize.height;

            Constant.setVideoSize(optimalVideoSize.width, optimalVideoSize.height);
            mMediaRecorder.setProfile(mProfile);

            mMediaRecorder.setOutputFile(CameraHelper.getOutputMediaFilePath(flagPreview));


            mMediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());
            mMediaRecorder.prepare();
            mMediaRecorder.start();

            if (callback != null) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(500);
                            callback.run();
                            callback = null;
                        }catch (Exception e) {

                        }
                    }
                }).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
        int k =1;
    }

    @Override
    public void surfaceCreated(SurfaceHolder mHolder) {
        try {
            surfaceHolder = mHolder;
            stopPreview();
            initializeRecording(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    @TargetApi(5)
    public void surfaceDestroyed(SurfaceHolder arg0) {
        stopPreview();
    }


    private void releaseMediaRecorder(){
        if (mMediaRecorder != null) {
            // clear recorder configuration
            mMediaRecorder.reset();
            // release the recorder object
            mMediaRecorder.release();
            mMediaRecorder = null;
            // Lock camera for later use i.e taking it back from MediaRecorder.
            // MediaRecorder doesn't need it anymore and we will release it if the activity pauses.
            mCamera.lock();
        }
    }

    private void releaseCamera(){
        if (mCamera != null){
            // release the camera for other applications
            mCamera.release();
            mCamera = null;
        }
    }

    public void stopPreview() {
        releaseMediaRecorder();
        releaseCamera();
        FileUtils.DeleteFolder(Constant.getPreviewDirectory());
    }

    public void stopVideoCapture() {
        stopPreview();
        initializeRecording(true);
    }

    public void startVideoCapture(Runnable callback) {
        this.callback = callback;
        stopPreview();
        initializeRecording(false);
    }

    /**
     * switch device camera back/front.
     */
    public void switchCamera() {
        flagFrontFaceCamera = !flagFrontFaceCamera;
        stopPreview();
        stopVideoCapture();
    }


    /**
     * when orientation is changed
     * @param orientation 1: landscape, 0: portrait
     */
    public void onOrientationChanged(int orientation) {
        if (orientation == 0) {
            mCamera.setDisplayOrientation(90);
        } else {
            mCamera.setDisplayOrientation(0);
        }
    }

    /**
     * initialize device camera for selected camera
     * @return selected camera id
     */
    private int initializeCamera() {
        int cameraID;
        if (flagFrontFaceCamera) {
            mCamera = CameraHelper.getDefaultFrontFacingCameraInstance();
            cameraID = CameraHelper.getDefaultFrontCameraID();

        } else {
            mCamera = CameraHelper.getDefaultBackFacingCameraInstance();
            cameraID = CameraHelper.getDefaultBackCameraID();
        }

        if (android.os.Build.VERSION.SDK_INT>=android.os.Build.VERSION_CODES.JELLY_BEAN_MR1){
            mCamera.enableShutterSound(false);
        }
        else{
            AudioManager audio= (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
            audio.setStreamVolume(AudioManager.STREAM_SYSTEM, 0,   AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        }
        return cameraID;
    }

    public boolean isFrontCamera() {
        return flagFrontFaceCamera;
    }
}