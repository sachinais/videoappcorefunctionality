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
    private static int outputVideoWidth = 1280;
    private static int outputVideoHeight = 720;
    private static String strVideoSize = "";

    /**
     * convert & merge overlay, videos...
     * @param callback callback
     * @param width new video width
     * @param height new video height
     * @param isCameraSize if width, height is same as camera recording video size, it will be true
     */
    public static void startVideoEncoding(Callback callback, int width, int height, boolean isCameraSize) {
        progress = 0;
        VideoEncoding.callback = callback;
        outputVideoWidth = width;
        outputVideoHeight = height;
        strVideoSize = Integer.toString(width) + "x" + Integer.toString(height);

        //skip convert recorded video if size is same...
        if (isCameraSize) {
            progress = -25;
            stepProgress = 25;
            convertTopTailVideoToUniqueFormat(true);
        } else {
            progress = -20;
            stepProgress = 20;
            convertVideoToUniqueFormat();
        }
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
            return progress;
        } else {
            return 0;
        }
    }

    /**
     * Convert recording videos(top, tail, camera video) into unique video format with captured video size
     */
    private static void convertVideoToUniqueFormat() {

        progress = 0;
        String commands = "-y -threads 5 -i src.mp4 -crf 30 -preset ultrafast -ar 44100 -c:a aac -strict experimental -s " + strVideoSize + " -r 30 -force_key_frames expr:gte(t,n_forced*1) -c:v libx264 dst.mp4";

        String srcVideoFilePath = Constant.getCameraVideo();
        String dstVideoFilePath = Constant.getConvertedVideo();

        final int videoLength = VideoUtils.getVideoLength(srcVideoFilePath);

        commands = commands.replace("src.mp4", srcVideoFilePath);
        commands = commands.replace("dst.mp4", dstVideoFilePath);

        String[] command = commands.split(" ");

        FFMpegUtils.execFFmpegBinary(command, new FFMpegUtils.Callback() {
            @Override
            public void onProgress(String msg) {
                callback.onProgress(getProgressStatus(msg, videoLength) + progress);
            }

            @Override
            public void onFinish() {
                convertTopTailVideoToUniqueFormat(true);
            }
        });
    }

    /**
     * Convert recording video into unique video format
     */
    private static void convertTopTailVideoToUniqueFormat(final boolean flagTop) {

        if (flagTop) {
            progress += stepProgress;
        } else {
            progress += stepProgress;
        }

        String commands = "-y -threads 5 -i src.mp4 -crf 30 -preset ultrafast -ar 44100 -c:a aac -strict experimental -s " + strVideoSize + " -r 30 -force_key_frames expr:gte(t,n_forced*1) -c:v libx264 dst.mp4";
        String srcVideoFilePath = "";
        String dstVideoFilePath = "";
        //progressDialog.show();

        if (flagTop) {
            srcVideoFilePath = Constant.getAssetTopVideo();
            dstVideoFilePath = Constant.getTopVideo();
        } else {
            srcVideoFilePath = Constant.getAssetTailVideo();
            dstVideoFilePath = Constant.getTailVideo();
        }

        final int videoLength = VideoUtils.getVideoLength(srcVideoFilePath);
        commands = commands.replace("src.mp4", srcVideoFilePath);
        commands = commands.replace("dst.mp4", dstVideoFilePath);

        String[] command = commands.split(" ");

        callback.onProgress(progress);
        FFMpegUtils.execFFmpegBinary(command, new FFMpegUtils.Callback() {
            @Override
            public void onProgress(String msg) {
                int subProgress = getProgressStatus(msg, videoLength);
                if (subProgress != 0) {
                    callback.onProgress(progress + subProgress);
                }
            }

            @Override
            public void onFinish() {
                if (flagTop) {
                    convertTopTailVideoToUniqueFormat(false);
                } else {
                    startEncodingVideo();
                }
            }
        });
    }

    /**
     * add overlay into recorded video.
     */
    private static void startEncodingVideo() {
        progress += stepProgress;

        ArrayList<VideoOverlay> videoOverlayInformation = MainApplication.getInstance().getVideoOverlayInformation();
        final int videoLength = VideoUtils.getVideoLength(Constant.getCameraVideo());
        if (videoOverlayInformation.size() > 0) {
            //make ffmpeg command
            String command = "-y ";
            command = command + "-i" + " " + Constant.getCameraVideo() +" ";

            for (int i = 0; i < videoOverlayInformation.size(); i ++) {
                command = command + "-i" + " " + Constant.getOverlayDirectory() + i + ".png ";
            }

            command = command + "-c:a aac -strict experimental -threads 5 -preset ultrafast -r 30 -c:v libx264 -filter_complex";
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
        final int videoLength = VideoUtils.getVideoLength(Constant.getTopVideo()) +
                VideoUtils.getVideoLength(Constant.getEncodedVideo()) + VideoUtils.getVideoLength(Constant.getTailVideo());

        //make ffmpeg command
        String command = "-y ";
        command = command + "-i" + " " + Constant.getTopVideo() +" ";
        command = command + "-i" + " " + Constant.getEncodedVideo() +" ";
        command = command + "-i" + " " + Constant.getTailVideo() +" ";

        command = command + "-c:a aac -strict experimental -threads 5 -preset ultrafast -r 30 -c:v libx264 -map [v] -map [a] -filter_complex";

        String strFilterComplex = "[0:0] [0:1] [1:0] [1:1] [2:0] [2:1] concat=n=3:v=1:a=1 [v] [a]";

        String[] subCommands = command.split(" ");

        String[] commands = new String[subCommands.length + 2];
        for (int i = 0; i < subCommands.length; i ++) {
            commands[i] = subCommands[i];
        }
        commands[subCommands.length] = strFilterComplex;
        commands[subCommands.length + 1] = Constant.getMergedVideo();

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
                callback.onFinish();
            }
        });
    }
}
