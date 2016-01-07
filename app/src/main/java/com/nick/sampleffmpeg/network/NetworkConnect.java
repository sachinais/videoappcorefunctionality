package com.nick.sampleffmpeg.network;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

public class NetworkConnect {
	private String str = null;
	private JSONObject responseJsonObject = null;
	private int responseCode;
	public static NetworkConnect httpRequestHandler = null;
	static HttpResponse httpResponse = null;


	public static NetworkConnect getInstance() {

		if (null == httpRequestHandler)
			httpRequestHandler = new NetworkConnect();
		return httpRequestHandler;
	}
	

	public JSONObject uploadFileRequest(String url, MultipartEntity entity) {
		try {
			HttpClient client = new DefaultHttpClient();
		//	HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000);
			HttpResponse response;
			HttpPost post = new HttpPost(url);
		//	post.addHeader("accessToken", SharedPreferenceWriter.getInstance(MainApplication.getInstance()).getString(SPreferenceKey.ACCESS_TOKEN));
			post.setEntity(entity);
			response = client.execute(post);
			responseCode = response.getStatusLine().getStatusCode();

			switch (responseCode) {

			case HttpURLConnection.HTTP_OK:
				InputStream inputStream = response.getEntity().getContent();
				str = convertStreamToString(inputStream);

				responseJsonObject = new JSONObject(str);
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
			e.getMessage();
			e.printStackTrace();
			return null;


		}

		return responseJsonObject;
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

			// FlurryAgent.onError(Definitions.FLURRY_ERROR_NETWORK_OPERATION,
			// e.getMessage(), e.getClass().getName());
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				// FlurryAgent.onError(Definitions.FLURRY_ERROR_NETWORK_OPERATION,
				// e.getMessage(), e.getClass().getName());
				throw new RuntimeException(e.getMessage());
			}
		}
		return sb.toString();
	}




}
