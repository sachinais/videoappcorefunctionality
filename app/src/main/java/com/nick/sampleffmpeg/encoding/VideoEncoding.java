package com.nick.sampleffmpeg.encoding;

import android.media.MediaMetadataRetriever;

import com.nick.sampleffmpeg.Define.Constant;
import com.nick.sampleffmpeg.MainApplication;
import com.nick.sampleffmpeg.R;
import com.nick.sampleffmpeg.bean.VideoOverlay;
import com.nick.sampleffmpeg.ui.activity.CompleteActivity;
import com.nick.sampleffmpeg.utils.FileUtils;
import com.nick.sampleffmpeg.utils.VideoUtils;
import com.nick.sampleffmpeg.utils.ffmpeg.FFMpegUtils;

import java.util.ArrayList;

/**
 * Created by baebae on 1/13/16.
 */
public class VideoEncoding {
    public interface Callback {
        public abstract void onProgress(int progress);
        public abstract void onFinish();
    }

    private static Callback callback = null;
    private static int stepProgress = 20;
    private static int progress = 0;
    private static String strVideoSize = "";

    /**
     * convert & merge overlay, videos...
     * @param callback callback
     * @param width new video width
     * @param height new video height
     */
    public static void startVideoEncoding(Callback callback, int width, int height) {
        progress = 0;
        VideoEncoding.callback = callback;
        strVideoSize = Integer.toString(width) + "x" + Integer.toString(height);

        //skip convert recorded video if size is same...
        progress = -48;
        stepProgress = 48;
        startEncodingVideo();
    }

    private static int getProgressStatus(String str, int videoLength) {
        int start = str.indexOf("time=");
        int end = str.indexOf("bitrate=");
        if (start != -1 && end != -1) {
            String strTime = str.substring(start + 5, end);
            String[] times = strTime.split(":");

            int hour = Integer.parseInt(times[0]);
            int min = Integer.parseInt(times[1]);
            int sec = (int)Double.parseDouble(times[2]);

            int progressTime = sec + min * 60 + hour * 3600;
            int progress = (progressTime * stepProgress) / videoLength;
            if (progress >= stepProgress) {
                progress = stepProgress;
            }
            return progress;
        } else {
            return 0;
        }
    }
    /**
     * Convert recording video into unique video format
     */
    public static void convertTopTailVideoToUniqueFormat(int videoWidth, int videoHeight, final boolean flagTop, final Runnable successCallback) {
        if (flagTop) {
            progress += stepProgress;
        } else {
            progress += stepProgress;
        }


        strVideoSize = Integer.toString(videoWidth) + "x" + Integer.toString(videoHeight);
        //String commands = "-y -threads 5 -i src.mp4 -crf 22 -preset ultrafast -ar 44100 -c:a aac -strict experimental -s " + strVideoSize + " -r 25 -c:v libx264 dst.mp4";
        String commands = "-y -threads 5 -i src.mp4 -c:a aac -strict experimental -crf 22 -preset ultrafast -r 25 -c:v libx264 -s " + strVideoSize + " dst.mp4";
        String srcVideoFilePath = "";
        String dstVideoFilePath = "";
        //progressDialog.show();

        if (flagTop) {
            srcVideoFilePath = Constant.getDownloadTopVideo();
            dstVideoFilePath = Constant.getTopVideo();
        } else {
            srcVideoFilePath = Constant.getDownloadTailVideo();
            dstVideoFilePath = Constant.getTailVideo();
        }
        if (srcVideoFilePath.length() > 0) {
            final int videoLength = VideoUtils.getVideoLength(srcVideoFilePath);
            commands = commands.replace("src.mp4", srcVideoFilePath);
            commands = commands.replace("dst.mp4", dstVideoFilePath);

            String[] command = commands.split(" ");

            FFMpegUtils.execFFmpegBinary(command, new FFMpegUtils.Callback() {
                @Override
                public void onProgress(String msg) {
                }

                @Override
                public void onFinish() {
                    successCallback.run();
                }
            });
        }
    }

    /**
     * add overlay into recorded video.
     */
    private static void startEncodingVideo() {
        progress += stepProgress;

        String strStart = Float.toString(MainApplication.getInstance().getVideoStart() / 1000.f);
        String strEnd = Float.toString(MainApplication.getInstance().getVideoEnd() / 1000.f);
        ArrayList<VideoOverlay> videoOverlayInformation = MainApplication.getInstance().getVideoOverlayInformation();
        final int videoLength = (MainApplication.getInstance().getVideoEnd() - MainApplication.getInstance().getVideoStart()) / 1000;
        if (videoOverlayInformation.size() > 0) {
            //make ffmpeg command
            String command = "-y -threads 5 ";
            command = command + "-i" + " " + Constant.getSourceVideo() +" ";

            for (int i = 0; i < videoOverlayInformation.size(); i ++) {
                command = command + "-i" + " " + Constant.getOverlayDirectory() + i + ".png ";
            }

            //command = command + "-c:a aac -strict experimental -threads 5 -crf 22 -preset ultrafast -r 25 -c:v libx264 -filter_complex";
            command = command + "-c:a mp2 -ss " + strStart + " -t " + strEnd + " -s " + strVideoSize + " -r 25 -c:v mpeg2video -qscale:v 2 -filter_complex";
            VideoOverlay title = videoOverlayInformation.get(0);

            String strFilterComplex = "[0:v][1:v] overlay=" + Integer.toString(title.xPos) + ":" + Integer.toString(title.yPos) +
                    ":enable='between(t," + title.startTime + "," + + title.endTime + ")' ";
            for (int i = 1; i < videoOverlayInformation.size(); i ++) {
                title = videoOverlayInformation.get(i);
                strFilterComplex += "[tmp];[tmp][" + Integer.toString(i + 1) + ":v] overlay=" + Integer.toString(title.xPos) + ":" + Integer.toString(title.yPos) +
                        ":enable='between(t," + title.startTime + "," + + title.endTime + ")' ";
            }

            String[] subCommands = command.split(" ");

            String[] commands = new String[subCommands.length + 2];
            for (int i = 0; i < subCommands.length; i ++) {
                commands[i] = subCommands[i];
            }
            commands[subCommands.length] = strFilterComplex;
            commands[subCommands.length + 1] = Constant.getEncodedVideo();

            callback.onProgress(progress);
            FFMpegUtils.execFFmpegBinary(commands, new FFMpegUtils.Callback() {
                @Override
                public void onProgress(String msg) {
                    int subProgress = getProgressStatus(msg, videoLength);
                    if (subProgress != 0) {
                        callback.onProgress(progress + subProgress);
                    }
                }

                @Override
                public void onFinish() {
                    callback.onProgress(48);
                    mergeEncodingVideoWithTopTailVideo();
                }
            });
        } else {
            mergeEncodingVideoWithTopTailVideo();
        }
    }

    /**
     * merge top, encoded video, tail video
     */
    private static void mergeEncodingVideoWithTopTailVideo() {
        progress += stepProgress;
        final int videoLength = (MainApplication.getInstance().getVideoEnd() - MainApplication.getInstance().getVideoStart()) / 1000;

        //make ffmpeg command

        String command = "-y ";
        int fileCount = 1;
        if (Constant.getDownloadTopVideo().length() != 0) {
            command = command + "-i" + " " + Constant.getTopVideo() +" ";
            fileCount ++;
        }

        command = command + "-i" + " " + Constant.getEncodedVideo() +" ";
        if (Constant.getDownloadTailVideo().length() != 0) {
            command = command + "-i" + " " + Constant.getTailVideo() +" ";
            fileCount ++;
        }

        command = command + "-c:a aac -strict experimental -threads 5 -crf 22 -preset ultrafast -r 25 -c:v libx264";
        String strFilterComplex = "";
        String strMapComplex = "";
        if (fileCount == 2) {
            strMapComplex = " -map [v] -map [a] -filter_complex";
            strFilterComplex = "[0:0] [0:1] [1:0] [1:1] concat=n=2:v=1:a=1 [v] [a]";
        }
        if (fileCount == 3) {
            strMapComplex = " -map [v] -map [a] -filter_complex";
            strFilterComplex =  "[0:0] [0:1] [1:0] [1:1] [2:0] [2:1] concat=n=3:v=1:a=1 [v] [a]";
        }

        command = command + strMapComplex;
        String[] subCommands = command.split(" ");
        int len = subCommands.length + 1;

        if (strFilterComplex.length() > 0) {
            len = subCommands.length + 2;
        }

        String[] commands = new String[len];
        for (int i = 0; i < subCommands.length; i ++) {
            commands[i] = subCommands[i];
        }

        if (strFilterComplex.length() > 0) {
            commands[subCommands.length] = strFilterComplex;
        }

        commands[len - 1] = Constant.getMergedVideo();
        callback.onProgress(progress);
        FFMpegUtils.execFFmpegBinary(commands, new FFMpegUtils.Callback() {
            @Override
            public void onProgress(String msg) {
                int subProgress = getProgressStatus(msg, videoLength);
                if (subProgress != 0) {
                    callback.onProgress(progress + subProgress);
                }
            }

            @Override
            public void onFinish() {
                callback.onProgress(100);
                callback.onFinish();
            }
        });


    }
}
