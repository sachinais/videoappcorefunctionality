package com.nick.sampleffmpeg.bean;

/**
 * Created by Vindhya Pratap on 1/8/2016.
 */
public class YoutubeDataBean {
    public String getVideoType() {
        return videoType;
    }

    public void setVideoType(String videoType) {
        this.videoType = videoType;
    }

    public String getVideoTitle() {
        return videoTitle;
    }

    public void setVideoTitle(String videoTitle) {
        this.videoTitle = videoTitle;
    }

    private String videoType = "";
    private String videoTitle = "";


}
