package com.nick.sampleffmpeg.utils;

import android.media.MediaMetadataRetriever;

/**
 * Created by baebae on 1/13/16.
 */
public class VideoUtils {

    /**
     * get video length in sec from path
     * @param path video file path
     * @return video length
     */
    public static int getVideoLength(String path) {
        int duration = 0;
        try {

            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(path);
            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            int timeInmillisec = Integer.parseInt( time );
            duration = timeInmillisec / 1000;
        } catch (Exception e) {

        }
        return duration;
    }

    public static int getVideoWidth(String path) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(path);
        String width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
        return Integer.parseInt(width);
    }

    public static int getVideoHeight(String path) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(path);
        String width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
        return Integer.parseInt(width);
    }

}
