package me.wowtao.pottery.network;

import android.content.Context;
import android.graphics.Bitmap;
import com.android.volley.*;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonSyntaxException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Created by ac on 5/25/15.
 * get data from server
 */
public class DataProvider {
    private static final String BASE_URL = "http://watao.jian-yin.com/index.php/webservices/";
//    public static final String BASE_URL = "http://watao-test.jian-yin.com/index.php/webservices/";
    public static final String GET_ORDER_LIST = "get_order_list";
    public static final String KEY_USER_NAME = "user_name";
    private static DataProvider mInstance;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private static Context mCtx;

    private DataProvider(final Context context) {
        mCtx = context;
        mRequestQueue = getRequestQueue();

        mImageLoader = new ImageLoader(mRequestQueue,
                new ImageLoader.ImageCache() {

                    @Override
                    public Bitmap getBitmap(String url) {
                        return null;
                    }

                    @Override
                    public void putBitmap(String url, Bitmap bitmap) {
                    }
                });
    }

    public static synchronized DataProvider getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new DataProvider(context);
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
            mRequestQueue.start();
        }
        return mRequestQueue;
    }

    public ImageLoader getImageLoader() {

        return mImageLoader;
    }

    public static String buildGetURL(String api, Map<String, String> params) {
        String url = BASE_URL + api;
        url += "?";
        for (String key : params.keySet()) {
            url += key + "=" + encodeParam(params.get(key)) + "&";
        }
        return url;
    }

    private static String encodeParam(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }


    public static class CustomRequest extends Request<String> {

        Listener<String> listener;
        Map<java.lang.String, java.lang.String> params;

        public static class CustomGetRequest extends CustomRequest {
            public CustomGetRequest(java.lang.String url, Listener<String> reponseListener, ErrorListener errorListener) {
                super(Method.GET, url, errorListener);
                this.listener = reponseListener;
            }
        }

        CustomRequest(int method, java.lang.String url, ErrorListener listener) {
            super(method, url, listener);
        }


        protected Map<java.lang.String, java.lang.String> getParams() throws com.android.volley.AuthFailureError {
            return params;
        }

        @Override
        protected Response<java.lang.String> parseNetworkResponse(NetworkResponse response) {
            try {
                java.lang.String jsonString = new java.lang.String(response.data,
                        HttpHeaderParser.parseCharset(response.headers));
                return Response.success(jsonString, HttpHeaderParser.parseCacheHeaders(response));
            } catch (UnsupportedEncodingException e) {
                return Response.error(new ParseError(e));
            } catch (JsonSyntaxException e) {
                return Response.error(new ParseError(e));
            }
        }

        @Override
        protected void deliverResponse(String response) {
            listener.onResponse(response);
        }

    }

    public static class BaseErrorListener implements ErrorListener {

        public BaseErrorListener() {
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            error.printStackTrace();
        }
    }
}
