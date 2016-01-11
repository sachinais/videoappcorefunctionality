package com.nick.sampleffmpeg.Define;

import android.Manifest;
import android.os.Environment;

import java.io.File;

/**
 * Created by baebae on 12/22/15.
 */
public class Constant {
    public final static int			BUTTON_NORMAL_ALPHA	= 100;
    public final static int			BUTTON_FOCUS_ALPHA	= 50;
    public final static int         TIMELINE_UNIT_SECOND = 1;
    public final static int         SP_PER_SECOND = 60 / TIMELINE_UNIT_SECOND;
    public final static String      APP_NAME = "VideoEditorApp";
    public static String strTemporaryFolderName = Long.toString(System.currentTimeMillis());
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

    public synchronized static String getCameraVideo() {
        return getApplicationDirectory() + "camera.mp4";
    }

    public synchronized static String getConvertedVideo() {
        return getUniqueFormatDirectory() + "recording.mp4";
    };

    public synchronized static String getConvertedAudio() {
        return getUniqueFormatDirectory() + "recording.wav";
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

    public synchronized static String getAssetTopVideo() {
        return getApplicationDirectory() + "app_files" + File.separator + "top_tail.mp4";
    }
    public synchronized static String getAssetTailVideo() {
        return getApplicationDirectory() + "app_files" + File.separator + "top_tail.mp4";
    }

    public synchronized static String getTopVideo() {
        return getUniqueFormatDirectory() + "top.mp4";
    }

    public synchronized static String getTailVideo() {
        return getUniqueFormatDirectory() + "tail.mp4";
    }
}
