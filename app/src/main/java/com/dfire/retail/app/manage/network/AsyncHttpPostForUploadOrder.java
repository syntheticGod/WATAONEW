package com.dfire.retail.app.manage.network;

import android.os.AsyncTask;
import android.util.Log;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import me.wowtao.pottery.Constants;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 异步HttpPost请求
 * 异步HttpPost请求, 线程的终止工作交给线程池，当activity停止的时候，设置回调函数为false ，就不会执行回调方法
 */
public class AsyncHttpPostForUploadOrder {
    private static final String TAG = "AsyncHttpPost";

    private static DefaultHttpClient httpClient;

    private RequestParameter mUrlParams;
    private String mUrl; //网络请求地址
    private RequestResultCallback mRequestCallback; //请求回调
    private HttpClient mHttpClient;
    private HttpPostTask mAsyncTask = null;


    private static void initHttpClient(HttpClient httpClient, int timeout) {
        HttpParams httpParams = httpClient.getParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 2000);
        HttpConnectionParams.setSoTimeout(httpParams, timeout);
//    	httpParams.setParameter(CoreProtocolPNames.PROTOCOL_VERSION,
//    			HttpVersion.HTTP_1_1);
    }

    private static final String HTTPPOST_HEADER_NAME = "User-Agent";
    private static final String HTTPPOST_HEADER_CONTENT = "Mozilla/5.0 (X11; U; Linux i686; en-GB; rv:1.8.1.6) Gecko/20070914 Firefox/2.0.0.7";

    public static String post(String actionUrl, Map<String, String> params,
                              Map<String, List<File>> files) throws IOException {
        String BOUNDARY = java.util.UUID.randomUUID().toString();
        String PREFIX = "--", LINEND = "\r\n";
        String MULTIPART_FROM_DATA = "multipart/form-data";
        String CHARSET = "UTF-8";

        URL uri = new URL(actionUrl);
        HttpURLConnection conn = (HttpURLConnection) uri.openConnection();
        conn.setReadTimeout(5 * 1000); // 缓存的最长时间
        conn.setDoInput(true);// 允许输入
        conn.setDoOutput(true);// 允许输出
        conn.setUseCaches(false); // 不允许使用缓存
        conn.setRequestMethod("POST");
        conn.setRequestProperty("connection", "keep-alive");
        conn.setRequestProperty("Charsert", "UTF-8");
        conn.setRequestProperty("Content-Type", MULTIPART_FROM_DATA
                + ";boundary=" + BOUNDARY);

        // 首先组拼文本类型的参数
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            sb.append(PREFIX);
            sb.append(BOUNDARY);
            sb.append(LINEND);
            sb.append("Content-Disposition: form-data; name=\""
                    + entry.getKey() + "\"" + LINEND);
            sb.append("Content-Type: text/plain; charset=" + CHARSET + LINEND);
            sb.append("Content-Transfer-Encoding: 8bit" + LINEND);
            sb.append(LINEND);
            sb.append(entry.getValue());
            sb.append(LINEND);
        }

        DataOutputStream outStream = new DataOutputStream(conn
                .getOutputStream());
        outStream.write(sb.toString().getBytes());
        // 发送文件数据
        if (files != null) {
            for (Map.Entry<String, List<File>> fileEntry : files.entrySet()) {
                String multipartFileFieldName = fileEntry.getKey();
                for (File file : fileEntry.getValue()) {
                    StringBuilder sb1 = new StringBuilder();
                    sb1.append(PREFIX);
                    sb1.append(BOUNDARY);
                    sb1.append(LINEND);
                    sb1.append("Content-Disposition: form-data; name=\"" + multipartFileFieldName + "\"; filename=\"" + file.getName() + "\"" + LINEND);
                    sb1.append("Content-Type: application/octet-stream; charset=" + CHARSET + LINEND);
                    sb1.append(LINEND);
                    outStream.write(sb1.toString().getBytes());

                    InputStream is = new FileInputStream(file);
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = is.read(buffer)) != -1) {
                        outStream.write(buffer, 0, len);
                    }

                    is.close();
                    outStream.write(LINEND.getBytes());
                }
            }

            // 请求结束标志
            byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINEND).getBytes();
            outStream.write(end_data);
            outStream.flush();
            // 得到响应码
            int res = conn.getResponseCode();
            if (res == 200) {
                InputStream in = conn.getInputStream();
                int ch;
                StringBuilder sb2 = new StringBuilder();
                while ((ch = in.read()) != -1) {
                    sb2.append((char) ch);
                }
                return sb2.toString();
            }
            outStream.close();
            conn.disconnect();
        }

        return "error";
    }


    /////上传就是在这个函数里面!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    public static String postParm(String url, Map<String, Object> params) {
        String res = "";
//    	StrictMode.setThreadPolicy(ThreadUtils.SHARE_THREAD_POLICY);
        if (httpClient == null) {
            httpClient = new DefaultHttpClient();
            initHttpClient(httpClient, 200000);
            httpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
        }
        try {
            Map<String, String> stringParams = new HashMap<>();
            Map<String, List<File>> fileParams = new HashMap<>();
            for (String key : params.keySet()) {
                if (params.get(key) instanceof File) {
                    File file = (File) params.get(key);
                    ArrayList<File> files = new ArrayList<>();
                    files.add(file);
                    fileParams.put(key, files);
                } else if (params.get(key) instanceof String) {
                    stringParams.put(key, (String) params.get(key));
                } else {
                    fileParams.put(key, (List<File>) params.get(key));
                }
            }

            res = post(url, stringParams, fileParams);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        Log.i(TAG, "result" + res);
        return res;
    }

    public AsyncHttpPostForUploadOrder(RequestParameter requestParams, RequestResultCallback requestCallback) {
        mUrlParams = requestParams;
        mUrl = requestParams.getUrl();
        mRequestCallback = requestCallback;
        if (mHttpClient == null) {
            mHttpClient = new DefaultHttpClient();
        }
    }

    /**
     * 开始异步Task的执行
     * 这里不同于Android AsyncTask, 每次执行时都要重新new 一次。只需创建一次，然后在调用的地方执行。
     */
    public void execute() {
        cancel();
        mAsyncTask = new HttpPostTask();
        mAsyncTask.execute();
    }

    /**
     * 取消异步Task的执行
     * 特别注意在Activity destory时要cancel正在在后台执行的Task. 不然会导致内存泄露或crash.
     */
    public void cancel() {
        if (mAsyncTask != null) {
            mAsyncTask.cancel(true);
            mAsyncTask = null;
        }
    }

    private class HttpPostTask extends AsyncTask<Void, Void, Object> {

        @Override
        protected void onPostExecute(Object result) {
            if (result instanceof RequestException) {
                mRequestCallback.onFail((RequestException) result);
            } else {
                mRequestCallback.onSuccess((String) result);
            }
        }

        @Override
        protected Object doInBackground(Void... params) {
            Object result = null;
            ByteArrayOutputStream content = null;
            try {
//                showDebugInfo();

                return postParm(mUrl, mUrlParams.getParamsMap());

            } catch (IllegalArgumentException e) {
                result = new RequestException(RequestException.IO_EXCEPTION, "连接错误");
                printErr(e);
            } catch (Exception e) {
                result = new RequestException(RequestException.EXCEPTION, "异常");
                printErr(e);
            } finally {
                if (content != null) {
                    try {
                        content.close();
                        content = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return result;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        private void showDebugInfo() {
            Log.i(TAG, mUrl);
            Log.i(TAG, mUrlParams.getParams().toString());
        }

        private void printErr(Exception e) {
            Log.e(TAG, "request to url :" + mUrl);
            e.printStackTrace();
        }

        /**
         * 格式网络请求数据，返回用于网络请求的url
         * 注意相关数据的完整性. 数据为null时有可能引起异常。
         */
        private void formatRequestParams(HttpPost request) throws Exception {
            request.getParams().setParameter(
                    CoreConnectionPNames.CONNECTION_TIMEOUT, Constants.CONNECT_TIMEOUT);
            request.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,
                    Constants.READ_TIMEOUT);

            StringEntity entity = new StringEntity(mUrlParams.getParams().toString());
            entity.setContentEncoding("UTF-8");
            entity.setContentType("application/json");
            request.setEntity(entity);
        }
    }
}