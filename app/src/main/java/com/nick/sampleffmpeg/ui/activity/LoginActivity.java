package com.nick.sampleffmpeg.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.nick.sampleffmpeg.R;
import com.nick.sampleffmpeg.network.CheckNetworkConnection;
import com.nick.sampleffmpeg.network.CustomDialogs;
import com.nick.sampleffmpeg.network.RequestBean;
import com.nick.sampleffmpeg.network.RequestHandler;
import com.nick.sampleffmpeg.network.RequestListner;
import com.nick.sampleffmpeg.sharedpreference.SPreferenceKey;
import com.nick.sampleffmpeg.sharedpreference.SharedPreferenceWriter;
import com.nick.sampleffmpeg.utils.AppConstants;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends Activity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        findViewById(R.id.tvSignInBtn).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tvSignInBtn:
                if(isValidData()){
                    sendRequest();
                }
                break;
        }
    }

    private boolean isValidData(){
        String a = ((EditText)findViewById(R.id.etEmail)).getText().toString().trim();
        String b = ((EditText)findViewById(R.id.etPassword)).getText().toString().trim();
        if(TextUtils.isEmpty(a)){
            ((EditText)findViewById(R.id.etEmail)).setError("Please enter your email");
        }else if(!isEmailValid(a)){
            ((EditText)findViewById(R.id.etEmail)).setError("Please enter a valid email");
        }else if(a.length() <8){
            ((EditText)findViewById(R.id.etPassword)).setError("Password must be minimum 8 char");
        }else {
            return true;
        }

        return false;
    }
    /**
     * @param email
     * @return tue if email is valid and false if not
     */
    private boolean isEmailValid(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void sendRequest(){
        try{
            if(CheckNetworkConnection.isNetworkAvailable(LoginActivity.this)){
                List<NameValuePair> paramePairs = new ArrayList<NameValuePair>();
                paramePairs.add(new BasicNameValuePair("email",((EditText)findViewById(R.id.etEmail)).getText().toString().trim()));
                paramePairs.add(new BasicNameValuePair("password",((EditText)findViewById(R.id.etPassword)).getText().toString().trim()));
             //   paramePairs.add(new BasicNameValuePair("login_resource",String.valueOf(AppConstants.DEVICE_TYPE_ANDROID)));
             //   paramePairs.add(new BasicNameValuePair("device_key", GCMRegistrar.getRegistrationId(LoginActivity.this)));
                RequestBean requestBean = new RequestBean();
                requestBean.setActivity(LoginActivity.this);
                requestBean.setUrl("signin.php");
                requestBean.setParams(paramePairs);
                requestBean.setIsProgressBarEnable(true);
                RequestHandler requestHandler = new RequestHandler(requestBean, requestListner);
                requestHandler.execute(null, null, null);
            }else {
                CustomDialogs.showOkDialog(LoginActivity.this, "Please check network connection");
            }
        }catch (Exception e){
            e.printStackTrace();
        }


    }
    private RequestListner requestListner = new RequestListner() {

        @Override
        public void getResponse(JSONObject jsonObject) {
            try{
                if(jsonObject != null){
                    parseJsonData(jsonObject);
                }
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
    };

    private void parseJsonData(JSONObject jsonObject) throws JSONException{
        if(!jsonObject.isNull(AppConstants.SUCCESS)){
            if(jsonObject.getBoolean(AppConstants.SUCCESS)){
                if(!jsonObject.isNull("user_id")){
                    SharedPreferenceWriter.getInstance(LoginActivity.this).writeStringValue(SPreferenceKey.USERID,jsonObject.getString("user_id"));
                }
                if(!jsonObject.isNull("email")){
                    SharedPreferenceWriter.getInstance(LoginActivity.this).writeStringValue(SPreferenceKey.EMAIL,jsonObject.getString("email"));
                }
                if(!jsonObject.isNull("firstname")){
                    SharedPreferenceWriter.getInstance(LoginActivity.this).writeStringValue(SPreferenceKey.FIRST_NAME,jsonObject.getString("firstname"));
                }
                if(!jsonObject.isNull("lastname")){
                    SharedPreferenceWriter.getInstance(LoginActivity.this).writeStringValue(SPreferenceKey.LAST_NAME,jsonObject.getString("lastname"));
                }
                startActivity(new Intent(LoginActivity.this,RecordingVideo.class));
                LoginActivity.this.finish();

            }else {
                if(!jsonObject.isNull(AppConstants.MESSAGE)){
                    Toast.makeText(getBaseContext(),"Error",Toast.LENGTH_LONG).show();

                }
            }

        }
    }
}
