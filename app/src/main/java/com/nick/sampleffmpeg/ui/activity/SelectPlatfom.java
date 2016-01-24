package com.nick.sampleffmpeg.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.nick.sampleffmpeg.R;
import com.nick.sampleffmpeg.network.CheckNetworkConnection;
import com.nick.sampleffmpeg.network.CustomDialogs;
import com.nick.sampleffmpeg.network.RequestBean;
import com.nick.sampleffmpeg.network.RequestHandler;
import com.nick.sampleffmpeg.network.RequestListner;
import com.nick.sampleffmpeg.utils.AppConstants;
import com.nick.sampleffmpeg.utils.FontTypeface;

import org.apache.http.NameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Admin on 1/23/2016.
 */
public class SelectPlatfom extends Activity {

    android.support.v7.widget.SwitchCompat switchCompanyFacebook;
    android.support.v7.widget.SwitchCompat switchCompanyTwitter;
    android.support.v7.widget.SwitchCompat switchCompanyLinedin;

    android.support.v7.widget.SwitchCompat switchPersonalFacebook;
    android.support.v7.widget.SwitchCompat switchPersonalTwitter;
    android.support.v7.widget.SwitchCompat switchPersonalLinedin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_platform);
        setFonts();
        getCredentials();
    }


    private void setFonts() {
        ((TextView) findViewById(R.id.tv_CompanyAccount)).setTypeface(FontTypeface.getTypeface(SelectPlatfom.this, AppConstants.FONT_SUFI_SEMIBOLD));
        ((TextView) findViewById(R.id.tv_compnyfacebook)).setTypeface(FontTypeface.getTypeface(SelectPlatfom.this, AppConstants.FONT_SUFI_REGULAR));
        ((TextView) findViewById(R.id.tv_compnyLinkedIn)).setTypeface(FontTypeface.getTypeface(SelectPlatfom.this, AppConstants.FONT_SUFI_REGULAR));
        ((TextView) findViewById(R.id.tv_compnyLinkedIn)).setTypeface(FontTypeface.getTypeface(SelectPlatfom.this, AppConstants.FONT_SUFI_REGULAR));

        ((TextView) findViewById(R.id.tv_PersonalAccount)).setTypeface(FontTypeface.getTypeface(SelectPlatfom.this, AppConstants.FONT_SUFI_REGULAR));
        ((TextView) findViewById(R.id.tv_PersonalFacebook)).setTypeface(FontTypeface.getTypeface(SelectPlatfom.this, AppConstants.FONT_SUFI_REGULAR));
        ((TextView) findViewById(R.id.tv_PersonalLinedIn)).setTypeface(FontTypeface.getTypeface(SelectPlatfom.this, AppConstants.FONT_SUFI_REGULAR));
        ((TextView) findViewById(R.id.tv_PersonalTwitter)).setTypeface(FontTypeface.getTypeface(SelectPlatfom.this, AppConstants.FONT_SUFI_REGULAR));
        switchCompanyFacebook = (android.support.v7.widget.SwitchCompat) findViewById(R.id.switchCompanyFacebook);
        switchCompanyTwitter = (android.support.v7.widget.SwitchCompat) findViewById(R.id.switchCompanyTwitter);
        switchCompanyLinedin = (android.support.v7.widget.SwitchCompat) findViewById(R.id.switchCompanyLinedin);

        switchPersonalFacebook = (android.support.v7.widget.SwitchCompat) findViewById(R.id.switchPersonalFacebook);
        switchPersonalTwitter = (android.support.v7.widget.SwitchCompat) findViewById(R.id.switchPersonalTwitter);
        switchPersonalLinedin = (android.support.v7.widget.SwitchCompat) findViewById(R.id.switchPersonalLinedin);


        findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void getCredentials() {
        try {
            if (CheckNetworkConnection.isNetworkAvailable(SelectPlatfom.this)) {
                List<NameValuePair> paramePairs = new ArrayList<NameValuePair>();
                RequestBean requestBean = new RequestBean();
                requestBean.setActivity(SelectPlatfom.this);
                requestBean.setUrl("load_credentials.php");
                requestBean.setParams(paramePairs);
                requestBean.setIsProgressBarEnable(true);
                RequestHandler requestHandler = new RequestHandler(requestBean, requestCredentials);
                requestHandler.execute(null, null, null);
            } else {
                CustomDialogs.showOkDialog(SelectPlatfom.this, "Please check network connection");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private RequestListner requestCredentials = new RequestListner() {

        @Override
        public void getResponse(JSONObject jsonObject) {
            try {
                String access_token = "", refresh_token = "";
                String url = "";
                if (jsonObject != null) {
                    if (!jsonObject.isNull("c_facebook")) {
                        if (!jsonObject.getJSONObject("c_facebook").isNull("has_valid_auth")) {

                            if (jsonObject.getJSONObject("c_facebook").getBoolean("has_valid_auth")) {

                                switchCompanyFacebook.setChecked(true);
                                if (!jsonObject.getJSONObject("c_facebook").isNull("name")) {
                                    ((TextView) findViewById(R.id.tv_compnyfacebook)).setText(jsonObject.getJSONObject("c_facebook").getString("name"));
                                }

                            } else {
                                switchCompanyFacebook.setChecked(false);
                            }

                        }
                    }
                    if (!jsonObject.isNull("c_twitter")) {
                        if (!jsonObject.getJSONObject("c_twitter").isNull("has_valid_auth")) {

                            if (jsonObject.getJSONObject("c_twitter").getBoolean("has_valid_auth")) {

                                switchCompanyTwitter.setChecked(true);
                                if (!jsonObject.getJSONObject("c_twitter").isNull("name")) {
                                    ((TextView) findViewById(R.id.tv_compnyTwitter)).setText(jsonObject.getJSONObject("c_twitter").getString("name"));
                                }

                            } else {
                                switchCompanyTwitter.setChecked(false);
                            }

                        }
                    }
                    if (!jsonObject.isNull("c_linkedin")) {
                        if (!jsonObject.getJSONObject("c_linkedin").isNull("has_valid_auth")) {

                            if (jsonObject.getJSONObject("c_linkedin").getBoolean("has_valid_auth")) {

                                switchCompanyFacebook.setChecked(true);
                                if (!jsonObject.getJSONObject("c_linkedin").isNull("name")) {
                                    ((TextView) findViewById(R.id.tv_compnyLinkedIn)).setText(jsonObject.getJSONObject("c_linkedin").getString("name"));
                                }

                            } else {
                                switchCompanyLinedin.setChecked(false);
                            }

                        }

                    }


                    if (!jsonObject.isNull("p_facebook")) {

                        if (!jsonObject.getJSONObject("p_facebook").isNull("has_valid_auth")) {

                            if (jsonObject.getJSONObject("p_facebook").getBoolean("has_valid_auth")) {

                                switchPersonalFacebook.setChecked(true);
                                if (!jsonObject.getJSONObject("p_facebook").isNull("name")) {
                                    ((TextView) findViewById(R.id.tv_PersonalAccount)).setText(jsonObject.getJSONObject("p_facebook").getString("name"));
                                }

                            } else {
                                switchPersonalFacebook.setChecked(false);
                            }

                        }
                    }
                    if (!jsonObject.isNull("p_twitter")) {
                        if (!jsonObject.getJSONObject("p_twitter").isNull("has_valid_auth")) {

                            if (jsonObject.getJSONObject("p_twitter").getBoolean("has_valid_auth")) {

                                switchCompanyTwitter.setChecked(true);
                                if (!jsonObject.getJSONObject("p_twitter").isNull("name")) {
                                    ((TextView) findViewById(R.id.tv_PersonalTwitter)).setText(jsonObject.getJSONObject("p_twitter").getString("name"));
                                }

                            } else {
                                switchCompanyTwitter.setChecked(false);
                            }

                        }
                    }
                    if (!jsonObject.isNull("p_linkedin")) {
                        if (!jsonObject.getJSONObject("p_linkedin").isNull("has_valid_auth")) {

                            if (jsonObject.getJSONObject("p_linkedin").getBoolean("has_valid_auth")) {

                                switchCompanyLinedin.setChecked(true);
                                if (!jsonObject.getJSONObject("p_linkedin").isNull("name")) {
                                    ((TextView) findViewById(R.id.tv_PersonalLinedIn)).setText(jsonObject.getJSONObject("p_linkedin").getString("name"));
                                }

                            } else {
                                switchCompanyLinedin.setChecked(false);
                            }

                        }

                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
}
