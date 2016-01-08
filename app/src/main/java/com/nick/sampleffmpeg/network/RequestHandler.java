package com.nick.sampleffmpeg.network;

import android.app.Activity;
import android.app.Dialog;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.view.Window;
import android.widget.TextView;

import com.nick.sampleffmpeg.R;
import com.nick.sampleffmpeg.utils.AppConstants;

import org.apache.http.NameValuePair;
import org.json.JSONObject;

import java.util.List;


public class RequestHandler extends AsyncTask<String, Integer, JSONObject> {

	Activity activity = null;
	private String url;
	private List<NameValuePair> params;
	boolean showProgressDialog;
	private String message = "";
	private RequestBean requestBean;
	private Dialog dialog;
	RequestListner responseListner;

	public RequestHandler(RequestBean requestBean, RequestListner responseListner){
		  this.requestBean = requestBean;
		  this.activity =  requestBean.getActivity();
		  this.url = requestBean.getUrl();
		  this.params = requestBean.getParams();
			this.responseListner = responseListner;
			Log.d("REQUEST FULL DATA", this.url+"XXXXXXXXXXX"+ "current url");
			Log.d("REQUEST FULL DATA", this.params.toString());
	}
	

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		try {
			showProgressBar();
		} catch (Exception e) {
			Log.e("JsonException", "exception occures" + e.getMessage().toString());
		}
	}

	@Override
	protected JSONObject doInBackground(String... entity) {
		// TODO Auto-generated method stub
		JSONObject fileUploadResponse = null;
		try {
			/*if(url.equalsIgnoreCase("reorderPrescription")){
				fileUploadResponse = RequestCreator.getInstance().makehttpRequest(AppConstants.REORDER_BASE_URL+url, params);
			}else {*/
				fileUploadResponse = RequestCreator.getInstance().makehttpRequest(AppConstants.BASE_URL+url, params);
		//	}
		} catch (Exception e) {
			e.toString();
		}
		return fileUploadResponse;
	}
	

	@Override
	protected void onPostExecute(JSONObject value) {
		// TODO Auto-generated method stub
		if (dialog != null)
			dialog.dismiss();
		if (responseListner != null)
			responseListner.getResponse(value);
		if(value!=null)
				Log.d("REQUEST FULL DATA", value.toString());
	}

	private void showProgressBar(){
		if (requestBean.isProgressBarEnable() && (dialog == null || !dialog.isShowing())) {
			dialog = new Dialog(activity);
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.setCanceledOnTouchOutside(false);
			dialog.setCancelable(false);
			dialog.setContentView(R.layout.loader_view);
			if(!TextUtils.isEmpty(requestBean.getProgressBarMessage()))
				((TextView) dialog.findViewById(R.id.text)).setText(requestBean.getProgressBarMessage());
			dialog.show();
		}
	}

}
