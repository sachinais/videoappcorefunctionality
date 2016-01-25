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
    private String videoTags = "";
    private String videoDescriotion = "";


    public String getVideoTags() {
        return videoTags;
    }

    public void setVideoTags(String videoTags) {
        this.videoTags = videoTags;
    }

    public String getVideoDescription() {
        return videoDescriotion;
    }

    public void setVideoDescription(String videoDescriotion) {
        this.videoDescriotion = videoDescriotion;
    }

}
