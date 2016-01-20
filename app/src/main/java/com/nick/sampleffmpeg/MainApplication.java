package com.nick.sampleffmpeg;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.nick.sampleffmpeg.bean.OverlayBean;
import com.nick.sampleffmpeg.bean.VideoOverlay;
import com.nick.sampleffmpeg.ui.view.ChildTextTimelineLayout;
import com.nick.sampleffmpeg.utils.LogFile;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.CookieStore;
import java.util.ArrayList;

/**
 * Created by Vindhya Pratap on 1/7/2016.
 */
@ReportsCrashes(formKey = "", // will not be used
        mailTo = "sachink@auxiliumit.com.au", mode = ReportingInteractionMode.TOAST, resToastText = R.string.app_name)
public class MainApplication extends Application{

    private static  MainApplication mainApplication;
    private CookieStore cookieStore;
    private JSONArray templateArray;
    private OverlayBean selectedOverlay = null;
    private int encodeingProgres=0;

    private static ArrayList<ChildTextTimelineLayout>  timelineTitlesInformation = new ArrayList<>();

    public static ArrayList<ChildTextTimelineLayout> getTimelineTitlesInformation() {
        return  timelineTitlesInformation;
    }

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
        ACRA.init(MainApplication.this);
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

    public SharedPreferences getEditor(){
       SharedPreferences mPrefs = getApplicationContext().getSharedPreferences("LAST_LOGIN_DETAILS", Context.MODE_PRIVATE);
        return mPrefs;
    }
}
