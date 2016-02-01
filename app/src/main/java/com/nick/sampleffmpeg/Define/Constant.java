package com.nick.sampleffmpeg.Define;

import android.Manifest;
import android.os.Environment;

import java.io.File;

/**
 * Created by baebae on 12/22/15.
 */
public class Constant {
    public static boolean           flagDebug = false;
    public static int               VIDEO_WIDTH = 1280;
    public static int               VIDEO_HEIGHT = 720;
    public final static int			BUTTON_NORMAL_ALPHA	= 100;
    public final static int			BUTTON_FOCUS_ALPHA	= 50;
    public static int               TIMELINE_UNIT_SECOND = 1;
    public static int               SP_PER_SECOND = 60 / TIMELINE_UNIT_SECOND;
    public final static String      APP_NAME = "VideoEditorApp";
    public static String strTemporaryFolderName = Long.toString(System.currentTimeMillis());

    public static String CLIENT_ID = "815107145608-2hc3kfand4bomob5thte673amk17k4c2.apps.googleusercontent.com";
    public static String CLIENT_SECRATE = "46ZZiJ5z01zj7Lgoz9f35Fd0";

    private static float scaleDensity = 0.f;
    public static float getScaleDensity() {
        return  scaleDensity;
    }

    public static void setScaleDensity(float scaleDensity) {
        Constant.scaleDensity = scaleDensity;
    }

    public static void updateTimeUnit(int time) {
        TIMELINE_UNIT_SECOND = time;
        SP_PER_SECOND = 60 / TIMELINE_UNIT_SECOND;
    }

    public static void setVideoSize(int width, int height) {
        VIDEO_WIDTH = width;
        VIDEO_HEIGHT = height;
    }
    /**
     * get Device FFMpeg folder path in sdcard
     * @return
     */
    public synchronized static String getApplicationDirectory()
    {
        String dirName = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + APP_NAME + File.separator;
        File directory = new File(dirName);
        directory.mkdirs();
        return dirName;
    }

    public synchronized static String getOverlayDirectory() {
        String dirName = getApplicationDirectory() + "Overlay" + File.separator;
        File directory = new File(dirName);
        directory.mkdirs();
        return dirName;
    }
    public synchronized static String getPreviewDirectory() {
        String dirName = getApplicationDirectory() + "Preview" + File.separator;
        File directory = new File(dirName);
        directory.mkdirs();
        return dirName;
    }

    public synchronized static String getUniqueFormatDirectory() {
        String dirName = getApplicationDirectory() + "unique_format" + File.separator;
        File directory = new File(dirName);
        directory.mkdirs();
        return dirName;
    }

    private static String strSrcVideoFilePath = "";
    public synchronized static String getSourceVideo() {
        if (strSrcVideoFilePath.length() == 0) {
            strSrcVideoFilePath = getApplicationDirectory() + "camera.mp4";
        }
        return strSrcVideoFilePath;
    }

    public synchronized static void setSourceVideo(String path) {
        if (path.length() > 0) {
            strSrcVideoFilePath = path;
        }
    }

    public synchronized static String getConvertedAudio() {
        return getApplicationDirectory() + "recording.wav";
    };

    public synchronized static String getEncodedVideo() {
        return getUniqueFormatDirectory() + "encoding.mp4";
    }

    public synchronized static String getMergedVideo() {
        return getUniqueFormatDirectory() + "merged.mp4";
    }

    public synchronized static String getLogFilePath() {
        return getApplicationDirectory() + "app.log";
    }

    public static String downloadedTopVideoUrl = "";
    public static String downloadedTailVideoUrl = "";;
    public static String downloadedThumbnailImageUrl = "";;

    public synchronized static String getDownloadTopVideo() {
        return downloadedTopVideoUrl;
    }
    public synchronized static String getDownloadTailVideo() {
        return downloadedTailVideoUrl;
    }

    public synchronized static void setDownloadTopVideo(String url) {
        downloadedTopVideoUrl = url;
    }

    public synchronized static void setThumbanulImageUrl(String url) {
        downloadedThumbnailImageUrl = url;
    }

    public static synchronized String getThumbnailImageUrl(){
        return downloadedThumbnailImageUrl;
    }

    public synchronized static void setDownloadTailVideo(String url) {
        downloadedTailVideoUrl = url;
    }

    public synchronized static String getTopVideo() {
        return getUniqueFormatDirectory() + "top.mp4";
    }

    public synchronized static String getTailVideo() {
        return getUniqueFormatDirectory() + "tail.mp4";
    }
}
