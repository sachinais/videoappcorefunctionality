package com.nick.sampleffmpeg.network;

import android.app.Activity;
import android.support.v4.app.Fragment;

import org.apache.http.NameValuePair;

import java.util.List;

/**
 * Created by ompalsingh on 7/9/2015.
 */
public class RequestBean {
    public Activity activity;
    private Fragment fragment = null;
    private boolean isProgressBarEnable;
    private String progressBarMessage = "";
    private String url = "";
    private List<NameValuePair> params = null;

    public String getProgressBarMessage() {
        return progressBarMessage;
    }

    public void setProgressBarMessage(String progressBarMessage) {
        this.progressBarMessage = progressBarMessage;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public Fragment getFragment() {
        return fragment;
    }

    public void setFragment(Fragment fragment) {
        this.fragment = fragment;
    }

    public boolean isProgressBarEnable() {
        return isProgressBarEnable;
    }

    public void setIsProgressBarEnable(boolean isProgressBarEnable) {
        this.isProgressBarEnable = isProgressBarEnable;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<NameValuePair> getParams() {
        return params;
    }

    public void setParams(List<NameValuePair> params) {
        this.params = params;
    }



}
