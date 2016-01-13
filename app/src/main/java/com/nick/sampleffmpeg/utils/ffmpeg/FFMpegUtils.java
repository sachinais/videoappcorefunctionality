package com.nick.sampleffmpeg.utils.ffmpeg;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;

import com.nick.libffmpeg.ExecuteBinaryResponseHandler;
import com.nick.libffmpeg.FFmpeg;
import com.nick.libffmpeg.LoadBinaryResponseHandler;
import com.nick.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.nick.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.nick.sampleffmpeg.R;
import com.nick.sampleffmpeg.utils.LogFile;

/**
 * Created by baebae on 12/23/15.
 */
public class FFMpegUtils {
    private static Context appContext = null;
    private static FFmpeg ffMpeg = null;

    public interface Callback {
        public abstract void onProgress(String msg);
        public abstract void onFinish();
    }
    public static void initializeFFmpeg(Context context) {
        appContext = context;
        if (ffMpeg == null) {
            ffMpeg = FFmpeg.getInstance(context);
            loadFFMpegBinary();
        }
    }

    /**
     * Load ffmpeg dynaimc library from asset.
     */
    private static void loadFFMpegBinary() {
        try {
            ffMpeg.loadBinary(new LoadBinaryResponseHandler() {
                @Override
                public void onFailure() {
                    showUnsupportedExceptionDialog();
                }
            });
        } catch (FFmpegNotSupportedException e) {
            showUnsupportedExceptionDialog();
        }
    }

    /**
     * IF there's no ffmpeg binary library doesn't support current mobile cpu type.
     */
    private static void showUnsupportedExceptionDialog() {
        new AlertDialog.Builder(appContext)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(appContext.getString(R.string.device_not_supported))
                .setMessage(appContext.getString(R.string.device_not_supported_message))
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .create()
                .show();

    }

    public static void execFFmpegBinary(final String[] command, final Callback callback) {
        try {
            ffMpeg.execute(command, new ExecuteBinaryResponseHandler() {
                @Override
                public void onFailure(String s) {
                    LogFile.logText("Failed: " + s, null);
                }

                @Override
                public void onSuccess(String s) {
                    LogFile.logText("onSuccess: " + s, null);
                }

                @Override
                public void onProgress(String s) {
                    LogFile.logText("Progress: " + s, null);
                    callback.onProgress(s);
                }

                @Override
                public void onStart() {
                }

                @Override
                public void onFinish() {
                    callback.onFinish();
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
        }
    }
}
