package com.nick.sampleffmpeg.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nick.sampleffmpeg.MainApplication;
import com.nick.sampleffmpeg.R;
import com.nick.sampleffmpeg.dataobject.SelectPlatFromDataObject;
import com.nick.sampleffmpeg.network.CheckNetworkConnection;
import com.nick.sampleffmpeg.network.CustomDialogs;
import com.nick.sampleffmpeg.network.RequestBean;
import com.nick.sampleffmpeg.network.RequestHandler;
import com.nick.sampleffmpeg.network.RequestListner;
import com.nick.sampleffmpeg.sharedpreference.SPreferenceKey;
import com.nick.sampleffmpeg.sharedpreference.SharedPreferenceWriter;
import com.nick.sampleffmpeg.utils.AppConstants;
import com.nick.sampleffmpeg.utils.FontTypeface;

import org.apache.http.NameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.Authenticator;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Admin on 1/23/2016.
 */
public class SelectPlatfom extends Activity {

    ImageView tickCompanyFacebook;
    ImageView tickCompanyTwitter;
    ImageView tickCompanyLinkedIn;

    ImageView tickPersonalFacebook;
    ImageView tickPersonalTwitter;
    ImageView tickPersonaLinkedIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_platform);
        setFonts();
        //getBundle();
    }


    private void setFonts() {
        ((TextView) findViewById(R.id.tv_CompnyFacebookTtile)).setTypeface(FontTypeface.getTypeface(SelectPlatfom.this, AppConstants.FONT_SUFI_SEMIBOLD));
        ((TextView) findViewById(R.id.tv_CompanyFacebookValue)).setTypeface(FontTypeface.getTypeface(SelectPlatfom.this, AppConstants.FONT_SUFI_REGULAR));

        ((TextView) findViewById(R.id.tv_CompnyLinkedInTitle)).setTypeface(FontTypeface.getTypeface(SelectPlatfom.this, AppConstants.FONT_SUFI_REGULAR));
        ((TextView) findViewById(R.id.tv_CompnyLinkedInValue)).setTypeface(FontTypeface.getTypeface(SelectPlatfom.this, AppConstants.FONT_SUFI_REGULAR));

        ((TextView) findViewById(R.id.tv_CompnyTwitterTitle)).setTypeface(FontTypeface.getTypeface(SelectPlatfom.this, AppConstants.FONT_SUFI_REGULAR));
        ((TextView) findViewById(R.id.tv_CompnyTwitterValue)).setTypeface(FontTypeface.getTypeface(SelectPlatfom.this, AppConstants.FONT_SUFI_REGULAR));

        ((TextView) findViewById(R.id.tv_PersonalFacebookTitle)).setTypeface(FontTypeface.getTypeface(SelectPlatfom.this, AppConstants.FONT_SUFI_REGULAR));
        ((TextView) findViewById(R.id.tv_PersonalFacebookValue)).setTypeface(FontTypeface.getTypeface(SelectPlatfom.this, AppConstants.FONT_SUFI_REGULAR));


        ((TextView) findViewById(R.id.tv_PersonalLinkedInTitle)).setTypeface(FontTypeface.getTypeface(SelectPlatfom.this, AppConstants.FONT_SUFI_REGULAR));
        ((TextView) findViewById(R.id.tv_PersonalLinkedInValue)).setTypeface(FontTypeface.getTypeface(SelectPlatfom.this, AppConstants.FONT_SUFI_REGULAR));

        ((TextView) findViewById(R.id.tv_PersonalTwitterTitle)).setTypeface(FontTypeface.getTypeface(SelectPlatfom.this, AppConstants.FONT_SUFI_REGULAR));
        ((TextView) findViewById(R.id.tv_PersonalTwitterValue)).setTypeface(FontTypeface.getTypeface(SelectPlatfom.this, AppConstants.FONT_SUFI_REGULAR));

        ((TextView) findViewById(R.id.tv_addAccountTitle1)).setTypeface(FontTypeface.getTypeface(SelectPlatfom.this, AppConstants.FONT_SUFI_REGULAR));
        ((TextView) findViewById(R.id.tv_addAccountTitle2)).setTypeface(FontTypeface.getTypeface(SelectPlatfom.this, AppConstants.FONT_SUFI_REGULAR));


        tickPersonalFacebook = (ImageView) findViewById(R.id.tickPersonalFacebook);
        tickPersonaLinkedIn = (ImageView) findViewById(R.id.tickPersonaLinkedIn);
        tickPersonalTwitter = (ImageView) findViewById(R.id.tickPersonalTwitter);


        tickCompanyFacebook = (ImageView) findViewById(R.id.tickCompanyFacebook);
        tickCompanyLinkedIn = (ImageView) findViewById(R.id.tickCompanyLinkedIn);
        tickCompanyTwitter= (ImageView) findViewById(R.id.tickCompanyTwitter);


        findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.rl_AddPlatform).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                addPlatform();

            }
        });


        popuateData();
    }


    public void popuateData() {
        try {
            List<SelectPlatFromDataObject> selectPlatFromDataObjectList = MainApplication.getInstance().getDiffrentPlatformDataValue();

            for (int i = 0; i < selectPlatFromDataObjectList.size(); i++) {
                if (selectPlatFromDataObjectList.get(i)._nameOfAccout.equalsIgnoreCase("p_facebook")) {

                    if (selectPlatFromDataObjectList.get(i)._has_valid_auth) {
                        ((ImageView) findViewById(R.id.iv_PersonalFacebook)).setImageResource(R.drawable.enable_facebook_selectale_platfrom);
                        ((TextView) findViewById(R.id.tv_PersonalFacebookValue)).setText(selectPlatFromDataObjectList.get(i)._name);
                        tickPersonalFacebook.setVisibility(View.VISIBLE);

                    } else {

                        ((ImageView) findViewById(R.id.iv_PersonalFacebook)).setImageResource(R.drawable.facebook_icon);
                        ((TextView) findViewById(R.id.tv_PersonalFacebookValue)).setText(selectPlatFromDataObjectList.get(i)._name);
                        tickPersonalFacebook.setImageResource(R.drawable.info);
                    }

                } else if (selectPlatFromDataObjectList.get(i)._nameOfAccout.equalsIgnoreCase("p_linkedin")) {

                    if (selectPlatFromDataObjectList.get(i)._has_valid_auth) {
                        ((ImageView) findViewById(R.id.iv_PersonalLinkedIn)).setImageResource(R.drawable.enable_linkedin_selectale_platfrom);
                        ((TextView) findViewById(R.id.tv_PersonalLinkedInValue)).setText(selectPlatFromDataObjectList.get(i)._name);
                        tickPersonaLinkedIn.setVisibility(View.VISIBLE);

                    } else {

                        ((ImageView) findViewById(R.id.iv_PersonalLinkedIn)).setImageResource(R.drawable.linekedin_icon);
                        ((TextView) findViewById(R.id.tv_PersonalLinkedInValue)).setText(selectPlatFromDataObjectList.get(i)._name);
                        tickPersonaLinkedIn.setImageResource(R.drawable.info);

                    }

                } else if (selectPlatFromDataObjectList.get(i)._nameOfAccout.equalsIgnoreCase("p_twitter")) {

                    if (selectPlatFromDataObjectList.get(i)._has_valid_auth) {
                        ((ImageView) findViewById(R.id.iv_PersonalTwitter)).setImageResource(R.drawable.enable_twitter_selectale_platfrom);
                        ((TextView) findViewById(R.id.tv_PersonalTwitterValue)).setText(selectPlatFromDataObjectList.get(i)._name);
                        tickPersonalTwitter.setVisibility(View.VISIBLE);

                    } else {

                        ((ImageView) findViewById(R.id.iv_PersonalTwitter)).setImageResource(R.drawable.twitter_icon);
                        ((TextView) findViewById(R.id.tv_PersonalTwitterValue)).setText(selectPlatFromDataObjectList.get(i)._name);
                        tickPersonalTwitter.setVisibility(View.VISIBLE);
                        tickPersonalTwitter.setImageResource(R.drawable.info);

                    }

                }


                if (selectPlatFromDataObjectList.get(i)._nameOfAccout.equalsIgnoreCase("c_facebook")) {

                    if (selectPlatFromDataObjectList.get(i)._has_valid_auth) {
                        ((ImageView) findViewById(R.id.iv_CompnayFacebook)).setImageResource(R.drawable.enable_facebook_selectale_platfrom);
                        ((TextView) findViewById(R.id.tv_CompanyFacebookValue)).setText(selectPlatFromDataObjectList.get(i)._name);
                        tickCompanyFacebook.setVisibility(View.VISIBLE);

                    } else {

                        ((ImageView) findViewById(R.id.iv_CompnayFacebook)).setImageResource(R.drawable.facebook_icon);
                        ((TextView) findViewById(R.id.tv_CompanyFacebookValue)).setText(selectPlatFromDataObjectList.get(i)._name);
                        tickCompanyFacebook.setVisibility(View.VISIBLE);
                        tickCompanyFacebook.setImageResource(R.drawable.info);

                    }

                } else if (selectPlatFromDataObjectList.get(i)._nameOfAccout.equalsIgnoreCase("c_linkedin")) {

                    if (selectPlatFromDataObjectList.get(i)._has_valid_auth) {
                        ((ImageView) findViewById(R.id.iv_CompanyLinkedIn)).setImageResource(R.drawable.enable_linkedin_selectale_platfrom);
                        ((TextView) findViewById(R.id.tv_CompnyLinkedInValue)).setText(selectPlatFromDataObjectList.get(i)._name);
                        tickCompanyLinkedIn.setVisibility(View.VISIBLE);

                    } else {

                        ((ImageView) findViewById(R.id.iv_CompanyLinkedIn)).setImageResource(R.drawable.linekedin_icon);
                        ((TextView) findViewById(R.id.tv_CompnyLinkedInValue)).setText(selectPlatFromDataObjectList.get(i)._name);
                        tickCompanyLinkedIn.setVisibility(View.VISIBLE);
                        tickCompanyLinkedIn.setImageResource(R.drawable.info);
                    }

                } else if (selectPlatFromDataObjectList.get(i)._nameOfAccout.equalsIgnoreCase("c_twitter")) {

                    if (selectPlatFromDataObjectList.get(i)._has_valid_auth) {
                        ((ImageView) findViewById(R.id.iv_CompanyTwitter)).setImageResource(R.drawable.enable_twitter_selectale_platfrom);
                        ((TextView) findViewById(R.id.tv_CompnyTwitterValue)).setText(selectPlatFromDataObjectList.get(i)._name);
                        tickCompanyTwitter.setVisibility(View.VISIBLE);

                    } else {

                        ((ImageView) findViewById(R.id.iv_CompanyTwitter)).setImageResource(R.drawable.twitter_icon);
                        ((TextView) findViewById(R.id.tv_CompnyTwitterValue)).setText(selectPlatFromDataObjectList.get(i)._name);
                        tickCompanyTwitter.setVisibility(View.VISIBLE);

                        tickCompanyTwitter.setImageResource(R.drawable.info);
                    }

                }

            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void addPlatform() {
        try {
            if (CheckNetworkConnection.isNetworkAvailable(SelectPlatfom.this)) {
                List<NameValuePair> paramePairs = new ArrayList<NameValuePair>();
                RequestBean requestBean = new RequestBean();
                requestBean.setActivity(SelectPlatfom.this);
                requestBean.setUrl("get_sid.php");
                requestBean.setParams(paramePairs);
                requestBean.setIsProgressBarEnable(true);
                RequestHandler requestHandler = new RequestHandler(requestBean, requestSid);
                requestHandler.execute(null, null, null);
            } else {
                CustomDialogs.showOkDialog(SelectPlatfom.this, "Please check network connection");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private RequestListner requestSid = new RequestListner() {

        @Override
        public void getResponse(JSONObject jsonObject) {
            try {
                String sid = "";
                String url = "";
                if (jsonObject != null) {
                    if (!jsonObject.isNull("sid")) {
                        sid = jsonObject.getString("sid");
                    }

                    url = "https://live.videomyjob.com/api/app_login.php?user_id=" + SharedPreferenceWriter.getInstance().getString(SPreferenceKey.USERID) + " & sid=" + sid + " & " + "redirect=1";


                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(browserIntent);

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };


}
