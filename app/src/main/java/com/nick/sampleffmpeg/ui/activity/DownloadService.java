package com.nick.sampleffmpeg.ui.activity;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.View;

import com.nick.sampleffmpeg.Define.Constant;
import com.nick.sampleffmpeg.MainApplication;
import com.nick.sampleffmpeg.R;
import com.nick.sampleffmpeg.bean.ProvinceBean;
import com.nick.sampleffmpeg.sharedpreference.SPreferenceKey;
import com.nick.sampleffmpeg.sharedpreference.SharedPreferenceWriter;
import com.nick.sampleffmpeg.utils.TailDownloader;
import com.nick.sampleffmpeg.utils.TopDownloader;
import com.nick.sampleffmpeg.utils.youtube.FileDownloader;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Admin on 1/23/2016.
 */
public class DownloadService extends Service {
    private ArrayList<ProvinceBean> options1Items = new ArrayList<ProvinceBean>();


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                downloadThumbNail();
                downloadTopVideo();
                downloadTailVideo();

            }
        });
        t.start();

        return START_STICKY;
    }

    public void downloadTailVideo() {
        try {
            int pos = MainApplication.getInstance().getSelectedTemplePosition();
            if (MainApplication.getInstance().getTemplateArray() != null && MainApplication.getInstance().getTemplateArray().length() > 0) {
                JSONArray jsonArray = MainApplication.getInstance().getTemplateArray();
                // for (int i = jsonArray.length() - 1; i >= 0; i--) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    options1Items.add(new ProvinceBean(i, jsonObject.optString("title"), jsonObject.optString("directory"), ""));
                }
            }
            JSONArray jsonArray = MainApplication.getInstance().getTemplateArray();
            String extenstion = jsonArray.getJSONObject(pos).optJSONObject("data").getJSONObject("brand-logo").getString("video_tail");

            if (extenstion != null && !extenstion.equalsIgnoreCase("")) {
                TailDownloader fileDownloader = new TailDownloader(this, getTemplateUrl((int) options1Items.get(pos).getId(), "tail"), extenstion, options1Items.get(pos).getDirectoryId());
                /*fileDownloader.startDownload(new TopVideoDownload() {
                    @Override
                    public void getTopVideoUrl(String url) {
                        Constant.setDownloadTailVideo(url);

                    }
                });*/
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void downloadThumbNail() {
        try {
            int pos = MainApplication.getInstance().getSelectedTemplePosition();
            if (MainApplication.getInstance().getTemplateArray() != null && MainApplication.getInstance().getTemplateArray().length() > 0) {
                JSONArray jsonArray = MainApplication.getInstance().getTemplateArray();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    options1Items.add(new ProvinceBean(i, jsonObject.optString("title"), jsonObject.optString("directory"), ""));
                }
            }
            JSONArray jsonArray = MainApplication.getInstance().getTemplateArray();

            FileDownloader fileDownloader = new FileDownloader(this, getThumbnailUrl((int) options1Items.get(pos).getId(), "tail"), options1Items.get(pos).getDirectoryId());
           /* fileDownloader.startDownload(new TopVideoDownload() {
                @Override
                public void getTopVideoUrl(String url) {

                    Constant.setDownloadTailVideo(url);
                }
            });*/


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void downloadTopVideo() {
        try {
            int pos = MainApplication.getInstance().getSelectedTemplePosition();
            if (MainApplication.getInstance().getTemplateArray() != null && MainApplication.getInstance().getTemplateArray().length() > 0) {
                JSONArray jsonArray = MainApplication.getInstance().getTemplateArray();
                // for (int i = jsonArray.length() - 1; i >= 0; i--) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    options1Items.add(new ProvinceBean(i, jsonObject.optString("title"), jsonObject.optString("directory"), ""));
                }
            }
            JSONArray jsonArray = MainApplication.getInstance().getTemplateArray();
            String extenstion = jsonArray.getJSONObject(pos).optJSONObject("data").getJSONObject("brand-logo").getString("video_top");

            if (extenstion != null && !extenstion.equalsIgnoreCase("")) {
                TopDownloader fileDownloader = new TopDownloader(this, getTemplateUrl((int) options1Items.get(pos).getId(), "top"), extenstion, options1Items.get(pos).getDirectoryId());
               /* fileDownloader.startDownload(new TopVideoDownload() {
                    @Override
                    public void getTopVideoUrl(String url) {
                        Constant.setDownloadTopVideo(url);


                    }
                });*/
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public interface TopVideoDownload {
        public void getTopVideoUrl(String url);
    }

    private String getThumbnailUrl(int postion, String topTail) {
        StringBuilder builder = new StringBuilder();
        http:
//syd.static.videomyjob.com/profile/6P9UFyOSl8_5_lg.jpg"

        try {
            JSONArray jsonArray = MainApplication.getInstance().getTemplateArray();
            builder.append("http://")
                    .append(getServer(SharedPreferenceWriter.getInstance().getString(SPreferenceKey.REGION)))
                    .append("/profile/")
                    .append(SharedPreferenceWriter.getInstance().getString(SPreferenceKey.PEPPER_ID))
                    .append("_" + SharedPreferenceWriter.getInstance().getString(SPreferenceKey.USERID))
                    .append("_" + "lg.jpg");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return builder.toString();
    }

    private String getTemplateUrl(int postion, String topTail) {
        StringBuilder builder = new StringBuilder();

        try {
            JSONArray jsonArray = MainApplication.getInstance().getTemplateArray();
            builder.append("http://")
                    .append(getServer(SharedPreferenceWriter.getInstance().getString(SPreferenceKey.REGION)))
                    .append("/company/")
                    .append(SharedPreferenceWriter.getInstance().getString(SPreferenceKey.COMPANY_DIRECTORY))
                    .append("/" + jsonArray.getJSONObject(postion).optString("directory"))
                    .append("/" + topTail + "." + jsonArray.getJSONObject(postion).optJSONObject("data").getJSONObject("brand-logo").getString("video_top"));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return builder.toString();
    }

    public String getServer(String company_region) {
        String toReturn = "syd.static.videomyjob.com";

        //Should we ever change the default
        if (company_region == "AU") {
            toReturn = "syd.static.videomyjob.com";
        } else if (company_region == "HN") {
            toReturn = "hn.static.videomyjob.com";
        }

        return toReturn;
    }


}
