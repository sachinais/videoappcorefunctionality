package com.nick.sampleffmpeg.ui.view;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.RelativeLayout;

import com.nick.sampleffmpeg.Define.Constant;
import com.nick.sampleffmpeg.utils.FileUtils;
import com.nick.sampleffmpeg.utils.camera.CameraHelper;

/**
 * Created by baebae on 12/22/15.
 */
public class VideoCaptureView extends SurfaceView implements SurfaceHolder.Callback {

    private MediaRecorder mMediaRecorder;
    private SurfaceHolder surfaceHolder;

    private Camera mCamera;
    private boolean flagFrontFaceCamera = true;
    public VideoCaptureView(Context context) {
        super(context);
        if (!isInEditMode()) {
            initializeSurface();
        }
    }

    public VideoCaptureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            initializeSurface();
        }
    }

    public VideoCaptureView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (!isInEditMode()) {
            initializeSurface();
        }
    }

    private void initializeSurface() {
        SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }


    @SuppressLint("NewApi")
    public void initializeRecording(boolean flagPreview) {
        try {
            mMediaRecorder = new MediaRecorder();

            int cameraID = initializeCamera();
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
            mMediaRecorder.setOutputFormat( mProfile.fileFormat );
            mMediaRecorder.setAudioEncoder( mProfile.audioCodec );
            mMediaRecorder.setVideoEncoder( mProfile.videoCodec );

            mMediaRecorder.setVideoSize( mProfile.videoFrameWidth, mProfile.videoFrameHeight );
            mMediaRecorder.setVideoFrameRate( mProfile.videoFrameRate );
            mMediaRecorder.setVideoEncodingBitRate( mProfile.videoBitRate );
            mMediaRecorder.setAudioEncodingBitRate( mProfile.audioBitRate );
            mMediaRecorder.setAudioChannels( mProfile.audioChannels );
            mMediaRecorder.setAudioSamplingRate( mProfile.audioSampleRate );

            mMediaRecorder.setOutputFile(CameraHelper.getOutputMediaFilePath(flagPreview));


            mMediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());
            mMediaRecorder.prepare();
            mMediaRecorder.start();

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

    public void startVideoCapture() {
        stopPreview();
        initializeRecording(false);
    }

    /**
     * switch device camera back/front.
     */
    public void switchCamera() {
        flagFrontFaceCamera = !flagFrontFaceCamera;
        stopPreview();
        startVideoCapture();
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
        return cameraID;
    }
}