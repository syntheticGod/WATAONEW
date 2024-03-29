package com.dfire.retail.app.manage.network;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.util.Log;


public class NetUtils {
	private static final String TAG = "NetUtils";
	private static final String HTTPPOST_HEADER_NAME = "User-Agent";
	private static final String HTTPPOST_HEADER_CONTENT = "Mozilla/5.0 (X11; U; Linux i686; en-GB; rv:1.8.1.6) Gecko/20070914 Firefox/2.0.0.7";
	private static final int WAIT_TIME = 20*1000;
	private static final int GET_RESPONSE_SUCCESSCODE = 200;
	private static final String GET_DATA_SUCCESS = "get data success";
	private static final String GET_DATA_FAILED = "get data failed";
	
	private static final boolean DEBUG_FLAG = true;
	
	/**
	 * 失败返回null
	 * @param params
	 * @param targetURL
	 * @return
	 */
	public static String getPostResult(Map<String, Object> params,String targetURL){
		String result = null;
		try {
			HttpPost post = new HttpPost(targetURL);
			post.setHeader(HTTPPOST_HEADER_NAME, HTTPPOST_HEADER_CONTENT);
			List<NameValuePair> httpParams = generateHttpParams(params);
			post.setEntity(new UrlEncodedFormEntity(httpParams,HTTP.UTF_8));
			HttpConnectionParams.setConnectionTimeout(new BasicHttpParams(), WAIT_TIME);
			HttpResponse response = MySSLSocketFactory.getDefaultHttpClient().execute(post);
			if (response.getStatusLine().getStatusCode() == GET_RESPONSE_SUCCESSCODE) {
				result = EntityUtils.toString(response.getEntity());
			}
			if (DEBUG_FLAG) {
				System.out.println(targetURL);
				String temp = targetURL + "/?";
				for (String key : params.keySet()) {
					System.out.println(key);
					System.out.println(params.get(key));
					temp += key +"=" +params.get(key) + "&";
				}
				System.out.println(temp);
				System.out.println(response.getStatusLine());
				
				if (result != null) {
					System.out.println(result);
				}
			}
			Log.i(TAG, GET_DATA_SUCCESS);
		} catch (Exception e) {
			Log.e(TAG, GET_DATA_FAILED);
			e.printStackTrace();
			result = null;
		}
		return result;
	}
	
	public static int post(HashMap<String, Object> hashMap, String account, File file, String urlServer)  
			throws Exception {  
		MultipartEntity mpEntity = new MultipartEntity(); // 文件传输  
		ContentBody cbFile = new FileBody(file);  
		mpEntity.addPart("file", cbFile); 
		ContentBody accountBody = new StringBody(account);
		mpEntity.addPart("phone", accountBody);
		for (String key : hashMap.keySet()) {
			ContentBody body = new StringBody((String) hashMap.get(key));
			mpEntity.addPart(key, body);
			System.out.println(hashMap.get(key));
		}

		HttpPost httppost = new HttpPost(urlServer);  
		httppost.setHeader(HTTPPOST_HEADER_NAME, HTTPPOST_HEADER_CONTENT);
		httppost.setEntity(mpEntity);  
		
		HttpResponse response = MySSLSocketFactory.getDefaultHttpClient().execute(httppost);  
		HttpEntity resEntity = response.getEntity();  

		System.out.println(urlServer);
		System.out.println(response.getStatusLine());// 通信Ok  
		
		if (resEntity != null) {
			resEntity.consumeContent();  
		}  
		return response.getStatusLine().getStatusCode();
	}  

//	public static String getGetResult(Map<String, Object> params,String targetURL){
//		String result = null;
//		try {
//			targetURL += genParamsForGen
//			HttpGet post = new HttpGet(targetURL);
//			post.setHeader(HTTPPOST_HEADER_NAME, HTTPPOST_HEADER_CONTENT);
//			List<NameValuePair> httpParams = generateHttpParams(params);
//			post.setEntity(new UrlEncodedFormEntity(httpParams,HTTP.UTF_8));
//			HttpConnectionParams.setConnectionTimeout(new BasicHttpParams(), WAIT_TIME);
//			HttpResponse response = MySSLSocketFactory.getDefaultHttpClient().execute(post);
//			if (response.getStatusLine().getStatusCode() == GET_RESPONSE_SUCCESSCODE) {
//				result = EntityUtils.toString(response.getEntity());
//			}
//			System.out.println(result);
//			Log.i(TAG, GET_DATA_SUCCESS);
//		} catch (Exception e) {
//			Log.e(TAG, GET_DATA_FAILED);
//			e.printStackTrace();
//			result = null;
//		}
//		return result;
//	}

	private static List<NameValuePair> generateHttpParams(
			Map<String, Object> params) {
		List<NameValuePair> httpParams = new ArrayList<NameValuePair>();
		for (String key : params.keySet()) {
			httpParams.add(new BasicNameValuePair(key, params.get(key).toString()));
		}
		return httpParams;
	}
}
