package com.nick.sampleffmpeg.network;

import android.text.TextUtils;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

public class RequestCreator {
	
	private String str = null;
	private JSONObject responseJsonObject = null;
	private int responseCode;
	public static RequestCreator httpRequestHandler = null;
	int timeoutConnection = 20000;
	int timeoutSocket = 10000;
	private static final int CONNECT_TIME_OUT = 20000;
	private static final int SOCKET_TIME_OUT = 10000;


	public static RequestCreator getInstance() {
		if (null == httpRequestHandler)
			httpRequestHandler = new RequestCreator();
		return httpRequestHandler;
	}

	public JSONObject makehttpRequest(String url, List<NameValuePair> params) {
		try {

			HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
			DefaultHttpClient client = new DefaultHttpClient();
			SchemeRegistry registry = new SchemeRegistry();
			SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
			socketFactory.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
			registry.register(new Scheme("https", socketFactory, 443));
			SingleClientConnManager mgr = new SingleClientConnManager(client.getParams(), registry);
			DefaultHttpClient httpClient = new DefaultHttpClient(mgr, client.getParams());
			HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
			HttpPost httpPost = new HttpPost(url);
//---------------------------------------------------------------------------------------------------------->
			httpPost.setEntity(new UrlEncodedFormEntity(params));
			HttpResponse response = httpClient.execute(httpPost);
			responseCode = response.getStatusLine().getStatusCode();
			switch (responseCode) {
			case HttpURLConnection.HTTP_OK:
				InputStream inputStream = response.getEntity().getContent();
				str = convertStreamToString(inputStream);
				if(!TextUtils.isEmpty(str)){
					Object json = new JSONTokener(str).nextValue();
					if (json instanceof JSONObject) {
						responseJsonObject = new JSONObject(str);
					}
					else if (json instanceof JSONArray){
						JSONArray jsonArray = new JSONArray(str);
						 JSONObject jsonObject = new JSONObject();
						jsonObject.put("jsonarray",jsonArray);
						 responseJsonObject = jsonObject;
					 }
					//you have an array
				}
				System.out.println("Response ::::::: " + responseJsonObject);
				break;
			case HttpURLConnection.HTTP_ACCEPTED:
				break;
			case HttpURLConnection.HTTP_BAD_GATEWAY:
				break;
			case HttpURLConnection.HTTP_BAD_METHOD:
				break;
			case HttpURLConnection.HTTP_BAD_REQUEST:
				break;
			case HttpURLConnection.HTTP_CLIENT_TIMEOUT:
				break;
			case HttpURLConnection.HTTP_CONFLICT:
				break;
			case HttpURLConnection.HTTP_CREATED:
				break;
			case HttpURLConnection.HTTP_ENTITY_TOO_LARGE:
				break;
			case HttpURLConnection.HTTP_FORBIDDEN:
				break;
			case HttpURLConnection.HTTP_GATEWAY_TIMEOUT:
				break;
			case HttpURLConnection.HTTP_GONE:
				break;
			case HttpURLConnection.HTTP_INTERNAL_ERROR:
				break;
			case HttpURLConnection.HTTP_LENGTH_REQUIRED:
				break;
			case HttpURLConnection.HTTP_MOVED_PERM:
				break;
			case HttpURLConnection.HTTP_MOVED_TEMP:
				break;
			case HttpURLConnection.HTTP_MULT_CHOICE:
				break;
			case HttpURLConnection.HTTP_NOT_ACCEPTABLE:
				break;
			case HttpURLConnection.HTTP_NOT_AUTHORITATIVE:
				break;
			case HttpURLConnection.HTTP_NOT_IMPLEMENTED:
				break;
			case HttpURLConnection.HTTP_NOT_MODIFIED:
				break;
			case HttpURLConnection.HTTP_NO_CONTENT:
				break;
			case HttpURLConnection.HTTP_PARTIAL:
				break;
			case HttpURLConnection.HTTP_PAYMENT_REQUIRED:
				break;
			case HttpURLConnection.HTTP_PRECON_FAILED:
				break;
			case HttpURLConnection.HTTP_PROXY_AUTH:
				break;
			case HttpURLConnection.HTTP_REQ_TOO_LONG:
				break;
			case HttpURLConnection.HTTP_RESET:
				break;
			case HttpURLConnection.HTTP_SEE_OTHER:
				break;
			case HttpURLConnection.HTTP_UNAUTHORIZED:
				break;
			case HttpURLConnection.HTTP_UNAVAILABLE:
				break;
			case HttpURLConnection.HTTP_UNSUPPORTED_TYPE:
				break;
			case HttpURLConnection.HTTP_USE_PROXY:
				break;
			case HttpURLConnection.HTTP_VERSION:
				break;
			default:

				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
			responseJsonObject= getErrorMessage(e.getMessage());

		}
		return responseJsonObject;
	}

	private JSONObject getErrorMessage(String msg){
		JSONObject jsonObject=null;
		try{
			 jsonObject = new JSONObject();
			jsonObject.put("success",true);
			jsonObject.put("message",msg);
		}catch (Exception e){
			e.printStackTrace();
		}
		return jsonObject;
	}
	
	private static String convertStreamToString(InputStream is) {

		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e.getMessage());
			}
		}
		return sb.toString();
	}
}	