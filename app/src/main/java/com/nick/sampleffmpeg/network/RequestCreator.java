package com.nick.sampleffmpeg.network;

import android.text.TextUtils;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.nick.sampleffmpeg.MainApplication;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.impl.cookie.BasicClientCookie;
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

    //String dummyString = "{\"success\":true,\"message\":\"\",\"templates\":[{\"id\":223,\"user_id\":67,\"company_id\":37,\"title\":\"Example Template\",\"directory\":\"a29e40923a225b9c5f8e6cbbd59796c9\",\"firstname\":\"Simon\",\"lastname\":\"Rahme\",\"data\":{\"youtube\":{\"brand\":{\"x\":\"4.84375\",\"y\":\"8.333333333333332\",\"w\":\"22.34375\",\"h\":\"14.444444444444443\"},\"displaypicture\":{\"x\":\"70.78125\",\"y\":\"7.777777777777778\",\"h\":\"37.03703703703704\",\"w\":\"20.833333333333336\"},\"text\":{\"color\":\"#FFFFFF\",\"size\":\"22\",\"font\":\"Arial, sans-serif\",\"text\":\"Watch this Video Job Ad\",\"x\":\"30.625000000000004\",\"y\":\"88.61111111111111\"},\"background\":{\"color\":\"#DBDBDB\",\"image\":\"false\"},\"playbutton\":{\"x\":\"39.53125\",\"y\":\"40\"},\"jobtitle\":{\"color\":\"#FFFFFF\",\"size\":\"36\",\"font\":\"Arial, sans-serif\",\"x\":\"26.09375\",\"y\":\"69.16666666666667\"}},\"brand-logo\":{\"x\":\"5\",\"y\":\"7.777777777777778\",\"w\":\"22.34375\",\"h\":\"14.444444444444443\",\"exists\":\"true\",\"video_top\":\"\",\"top_height\":\"0\",\"top_width\":\"0\",\"video_tail\":\"\",\"tail_height\":\"0\",\"tail_width\":\"0\"},\"name-overlay\":{\"color\":\"#FFFFFF\",\"size\":\"20\",\"font\":\"Arial, sans-serif\",\"x\":\"0\",\"y\":\"78.05555555555556\",\"h\":\"21.9444444444%\",\"w\":\"100%\",\"background-img\":\"\\/assets\\/img\\/template\\/strip-square-svg.svg\",\"l\":\"4\",\"t\":\"14\",\"r\":\"4\",\"b\":\"14\",\"is_custom\":\"0\",\"count\":\"strip-square\",\"background-color\":\"rgba(74, 217, 217, 0.6)\"},\"contact-overlay\":{\"color\":\"#FFFFFF\",\"size\":\"20\",\"font\":\"Arial, sans-serif\",\"x\":\"0\",\"y\":\"78.05555555555556\",\"h\":\"21.9444444444%\",\"w\":\"100%\",\"background-img\":\"\\/assets\\/img\\/template\\/strip-square-svg.svg\",\"l\":\"4\",\"t\":\"14\",\"r\":\"4\",\"b\":\"14\",\"is_custom\":\"0\",\"count\":\"strip-square\",\"background-color\":\"rgba(74, 217, 217, 0.6)\"},\"captions\":[]}},{\"id\":225,\"user_id\":67,\"company_id\":37,\"title\":\"XYZ\",\"directory\":\"15e46054ba3c0c1f2b74e09bfcf600f0\",\"firstname\":\"Simon\",\"lastname\":\"Rahme\",\"data\":{\"youtube\":{\"brand\":{\"x\":\"4.84375\",\"y\":\"7.222222222222221\",\"w\":\"-1\",\"h\":\"-1\"},\"displaypicture\":{\"x\":\"70.78125\",\"y\":\"7.777777777777778\",\"h\":\"37.03703703703704\",\"w\":\"20.833333333333336\"},\"text\":{\"color\":\"#FFFFFF\",\"size\":\"22\",\"font\":\"Arial, sans-serif\",\"text\":\"Watch this Video Job Ad\",\"x\":\"30.625000000000004\",\"y\":\"88.61111111111111\"},\"background\":{\"color\":\"#DBDBDB\",\"image\":\"false\"},\"playbutton\":{\"x\":\"39.53125\",\"y\":\"40\"},\"jobtitle\":{\"color\":\"#FFFFFF\",\"size\":\"36\",\"font\":\"Arial, sans-serif\",\"x\":\"26.09375\",\"y\":\"69.16666666666667\"}},\"brand-logo\":{\"x\":\"5\",\"y\":\"7.777777777777778\",\"w\":\"-1\",\"h\":\"-1\",\"exists\":\"true\",\"video_top\":\"\",\"top_height\":\"0\",\"top_width\":\"0\",\"video_tail\":\"\",\"tail_height\":\"0\",\"tail_width\":\"0\"},\"name-overlay\":{\"color\":\"#FFFFFF\",\"size\":\"20\",\"font\":\"Arial, sans-serif\",\"x\":\"0\",\"y\":\"78.05555555555556\",\"h\":\"21.9444444444%\",\"w\":\"100%\",\"background-img\":\"\\/assets\\/img\\/template\\/strip-square-svg.svg\",\"l\":\"4\",\"t\":\"14\",\"r\":\"4\",\"b\":\"14\",\"is_custom\":\"0\",\"count\":\"strip-square\",\"background-color\":\"rgba(74, 217, 217, 0.6)\"},\"contact-overlay\":{\"color\":\"#FFFFFF\",\"size\":\"20\",\"font\":\"Arial, sans-serif\",\"x\":\"0\",\"y\":\"78.05555555555556\",\"h\":\"21.9444444444%\",\"w\":\"100%\",\"background-img\":\"\\/assets\\/img\\/template\\/strip-square-svg.svg\",\"l\":\"4\",\"t\":\"14\",\"r\":\"4\",\"b\":\"14\",\"is_custom\":\"0\",\"count\":\"strip-square\",\"background-color\":\"rgba(74, 217, 217, 0.6)\"},\"captions\":[{\"color\":\"#FFFFFF\",\"size\":\"18\",\"font\":\"Arial, sans-serif\",\"title\":\"Caption \",\"x\":\"12.1875\",\"y\":\"4.444444444444445\",\"h\":\"33.3333333333%\",\"w\":\"33.3333333333%\",\"background-img\":\"\\/assets\\/img\\/template\\/bubble-square-svg.svg\",\"l\":\"5\",\"t\":\"5\",\"r\":\"5\",\"b\":\"5\",\"is_custom\":\"0\",\"count\":\"bubble-square-svg\",\"background-color\":\"rgba(74,217,217,0.6)\"},{\"color\":\"#FFFFFF\",\"size\":\"18\",\"font\":\"Arial, sans-serif\",\"title\":\"Caption 2\",\"x\":\"4.21875\",\"y\":\"50.83333333333333\",\"h\":\"33.3333333333%\",\"w\":\"33.3333333333%\",\"background-img\":\"\\/assets\\/img\\/template\\/bubble-square-svg.svg\",\"l\":\"5\",\"t\":\"5\",\"r\":\"5\",\"b\":\"5\",\"is_custom\":\"0\",\"count\":\"bubble-square-svg\",\"background-color\":\"rgba(74,217,217,0.6)\"},{\"color\":\"#FFFFFF\",\"size\":\"18\",\"font\":\"Arial, sans-serif\",\"title\":\"Caption 3\",\"x\":\"0\",\"y\":\"58.05555555555556\",\"h\":\"33.3333333333%\",\"w\":\"33.3333333333%\",\"background-img\":\"\\/assets\\/img\\/template\\/bubble-square-svg.svg\",\"l\":\"5\",\"t\":\"5\",\"r\":\"5\",\"b\":\"5\",\"is_custom\":\"0\",\"count\":\"bubble-square-svg\",\"background-color\":\"rgba(43,203,203,0.6)\"}]}}]}";
	public static RequestCreator getInstance() {
		if (null == httpRequestHandler)
			httpRequestHandler = new RequestCreator();
		return httpRequestHandler;
	}

	public JSONObject makehttpRequest(String url, List<NameValuePair> params) {
		try {
          //  String cookies = CookieManager.getInstance().getCookie(url);

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
         //   HttpContext localContext = new BasicHttpContext();
            /*if( SharedPreferenceWriter.getInstance(MainApplication.getInstance()).getString("cookies") != null){
                BasicCookieStore lCS = getCookieStore(SharedPreferenceWriter.getInstance(MainApplication.getInstance()).getString("cookies"), AppConstants.BASE_URL);
                httpClient.setCookieStore(lCS);
                localContext.setAttribute(ClientContext.COOKIE_STORE, lCS);
            }*/
            CookieSyncManager.createInstance(MainApplication.getInstance());
            if(CookieManager.getInstance().getCookie("live.videomyjob.com") != null){
                String[] keyValueSets = CookieManager.getInstance().getCookie(url).split(";");
                for(String cookie : keyValueSets)
                {
                    String[] keyValue = cookie.split("=");
                    String key = keyValue[0];
                    String value = "";
                    if(keyValue.length>1) value = keyValue[1];
					BasicClientCookie c = new BasicClientCookie(key, value);
                    c.setDomain("live.videomyjob.com"); httpClient.getCookieStore().addCookie(c);
					//httpClient.getCookieStore().addCookie(new BasicClientCookie(key, value));
                }
            }


			HttpResponse response = httpClient.execute(httpPost);
           try{
               List<Cookie> cookies = httpClient.getCookieStore().getCookies();

               if(cookies != null)
               {
                   for(Cookie cookie : cookies)
                   {
                       String cookieString = cookie.getName() + "=" + cookie.getValue() + "; domain=" + cookie.getDomain();
                       CookieManager.getInstance().setCookie(cookie.getDomain(), cookieString);
                   }
               }

               CookieSyncManager.getInstance().sync();
           }catch (Exception e){
               e.printStackTrace();
           }
          //  List<Cookie> cookiesList = httpClient.getCookieStore().getCookies();
          /*  if (cookiesList.isEmpty()) {
                System.out.println("None");
            } else {
                for (int i = 0; i < cookiesList.size(); i++) {
                    System.out.println("- " + cookiesList.get(i).toString());
                    SharedPreferenceWriter.getInstance(MainApplication.getInstance()).writeStringValue("cookies",cookiesList.get(i).toString());
                }
            }*/


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

    public static BasicCookieStore getCookieStore(String cookies, String domain) {
        String[] cookieValues = cookies.split(";");
        BasicCookieStore cs = new BasicCookieStore();

        BasicClientCookie cookie;
        for (int i = 0; i < cookieValues.length; i++) {
            String[] split = cookieValues[i].split("=");
            if (split.length == 2)
                cookie = new BasicClientCookie(split[0], split[1]);
            else
                cookie = new BasicClientCookie(split[0], null);

            cookie.setDomain(domain);
            cs.addCookie(cookie);
        }
        return cs;
    }


}	