package com.nick.sampleffmpeg;

import android.app.Application;

import org.json.JSONArray;

import java.net.CookieStore;

/**
 * Created by Vindhya Pratap on 1/7/2016.
 */
public class MainApplication extends Application{
    private static  MainApplication mainApplication;
    private CookieStore cookieStore;
    private JSONArray templateArray;
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
}
