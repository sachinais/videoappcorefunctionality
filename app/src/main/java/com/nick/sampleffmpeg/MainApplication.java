package com.nick.sampleffmpeg;

import android.app.Application;

import com.nick.sampleffmpeg.bean.OverlayBean;
import com.nick.sampleffmpeg.bean.VideoOverlay;
import com.nick.sampleffmpeg.utils.LogFile;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.CookieStore;
import java.util.ArrayList;

/**
 * Created by Vindhya Pratap on 1/7/2016.
 */
public class MainApplication extends Application{
    private static  MainApplication mainApplication;
    private CookieStore cookieStore;
    private JSONArray templateArray;
    private OverlayBean selectedOverlay = null;
    private int encodeingProgres=0;

    public String getYoutubeUrl() {
        return youtubeUrl;
    }

    public void setYoutubeUrl(String youtubeUrl) {
        this.youtubeUrl = youtubeUrl;
    }

    private String youtubeUrl="";

    public int getUploadingProgress() {
        return uploadingProgress;
    }

    public void setUploadingProgress(int uploadingProgress) {
        this.uploadingProgress = uploadingProgress;
    }

    public int getEncodeingProgres() {
        return encodeingProgres;
    }

    public void setEncodeingProgres(int encodeingProgres) {
        this.encodeingProgres = encodeingProgres;
    }

    private int uploadingProgress=0;

    private ArrayList<VideoOverlay> videoOverlayInformation = new ArrayList<>();
    public static  MainApplication getInstance(){
        return mainApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mainApplication =(MainApplication)getApplicationContext();
    }

    public void setCookieStore(CookieStore cookieStore){
        this.cookieStore = cookieStore;
    }
    public CookieStore getCookieStore(){
       return cookieStore;
    }

    public void setTemplateArray(JSONArray templateArray){
        this.templateArray = templateArray;
    }

    public  JSONArray getTemplateArray(){
        return templateArray;
    }

    public void setTemplate(int index) {
        try {
            JSONObject overlayObj = templateArray.getJSONObject(index).getJSONObject("data");
            String directoryID = templateArray.getJSONObject(index).getString("directory");
            selectedOverlay = new OverlayBean();
            selectedOverlay.parseFromJson(overlayObj, directoryID);
        } catch (Exception e) {
            LogFile.logText(e.getMessage(), null);
        }
    }

    public ArrayList<VideoOverlay> getVideoOverlayInformation() {
        return videoOverlayInformation;
    }

    public OverlayBean getTemplate() {
        return selectedOverlay;
    }
}
