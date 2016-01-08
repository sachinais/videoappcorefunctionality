package com.nick.sampleffmpeg.network;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Window;
import android.widget.TextView;

import com.nick.sampleffmpeg.R;

import org.apache.http.entity.mime.MultipartEntity;
import org.json.JSONObject;

public class UploadFileHandler extends AsyncTask<MultipartEntity, Integer, JSONObject> {


	private String url, pBarMessage;
	private RequestListner serverResponse = null;
	private Dialog dialog;
	private Context context;
	private boolean isProgressBarEnable;
	public UploadFileHandler( Context context, boolean isProgressBarEnable, String url, String pBarMessage, RequestListner serverResponse) {
		this.context = context;
		this.isProgressBarEnable =  isProgressBarEnable;
		this.url = url;
		this.pBarMessage = pBarMessage;
		this.serverResponse = serverResponse;

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
	protected JSONObject doInBackground(MultipartEntity... entity) {
		JSONObject fileUploadResponse = null;
		try {
			fileUploadResponse = NetworkConnect.getInstance().uploadFileRequest(url, entity[0]);
		} catch (Exception e) {
			e.toString();
		}
		return fileUploadResponse;
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);
	}

	@Override
	protected void onPostExecute(JSONObject value) {
		try {
			if(dialog !=null){
				dialog.dismiss();
			}
			if(serverResponse!=null){
				serverResponse.getResponse(value);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void showProgressBar(){
		if (isProgressBarEnable) {
			dialog = new Dialog(context);
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.setCanceledOnTouchOutside(false);
			dialog.setCancelable(false);
			dialog.setContentView(R.layout.loader_view);
			((TextView) dialog.findViewById(R.id.text)).setText("uploading...");
			dialog.show();
		}
	}



}
