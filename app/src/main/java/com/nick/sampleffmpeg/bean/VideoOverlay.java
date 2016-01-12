package com.nick.sampleffmpeg.bean;

/**
 * Created by baebae on 1/12/16.
 */
public class VideoOverlay {
    public double startTime;
    public double endTime;
    public double duration;
    public int xPos;
    public int yPos;

    public String overlayImagePath;

    public VideoOverlay(double startTime, double endTime, int xPos, int yPos, String filePath) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = endTime - startTime;
        this.xPos = xPos;
        this.yPos = yPos;
        this.overlayImagePath = filePath;
    }
}
