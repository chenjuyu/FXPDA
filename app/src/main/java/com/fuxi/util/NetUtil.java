package com.fuxi.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.fuxi.vo.RequestVo;

/**
 * Title: NetUtil Description: HTTP辅助类
 * 
 * @author LJ
 * 
 */
@SuppressWarnings("deprecation")
public class NetUtil {
    private static final String NOT_LOGIN = "notlogin";
    private static final String TAG = "NetUtil";
    private static final int WAIT_TIMEOUT = 8000;
    private static final int DEFAULT_MAX_CONNECTIONS = 30;
    private static final int DEFAULT_CONNECTION_TIMEOUT = 10 * 1000;
    private static final int DEFAULT_SOCKET_TIMEOUT = 0;
    private static final int DEFAULT_SOCKET_BUFFER_SIZE = 8192;
    private static final int DEFAULT_HOST_CONNECTIONS = 400;
    private static Header[] headers = new BasicHeader[2];
    private static HttpClient httpClient = null;
    static {
        headers[0] = new BasicHeader("Cookie", "");
        headers[1] = new BasicHeader("Connection", "Keep-Alive");
    }

    /**
     * 获取HttpClient
     * 
     * @return
     */
    public static synchronized HttpClient getHttpClient() {
        if (httpClient == null) {
            HttpParams httpParams = new BasicHttpParams();
            ConnManagerParams.setTimeout(httpParams, WAIT_TIMEOUT);
            HttpConnectionParams.setConnectionTimeout(httpParams, DEFAULT_CONNECTION_TIMEOUT);
            HttpConnectionParams.setSoTimeout(httpParams, DEFAULT_SOCKET_TIMEOUT);
            ConnManagerParams.setMaxConnectionsPerRoute(httpParams, new ConnPerRouteBean(DEFAULT_HOST_CONNECTIONS));
            ConnManagerParams.setMaxTotalConnections(httpParams, DEFAULT_MAX_CONNECTIONS);
            HttpProtocolParams.setUseExpectContinue(httpParams, true);
            HttpConnectionParams.setStaleCheckingEnabled(httpParams, false);
            HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(httpParams, HTTP.UTF_8);
            HttpClientParams.setRedirecting(httpParams, false);
            String userAgent = "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.2) Gecko/20100115 Firefox/3.6";
            HttpProtocolParams.setUserAgent(httpParams, userAgent);
            HttpConnectionParams.setTcpNoDelay(httpParams, true);
            HttpConnectionParams.setSocketBufferSize(httpParams, DEFAULT_SOCKET_BUFFER_SIZE);
            SchemeRegistry schemeRegistry = new SchemeRegistry();
            schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
            ClientConnectionManager manager = new ThreadSafeClientConnManager(httpParams, schemeRegistry);
            httpClient = new DefaultHttpClient(manager, httpParams);
        }
        return httpClient;
    }

    /**
     * POST请求
     * 
     * @param vo
     * @return
     */
    public static Object post(RequestVo vo) {
        HttpClient client = getHttpClient();
        String url = vo.customer.getIp().concat(vo.requestUrl);
        Logger.d(TAG, "Post " + url);
        HttpPost post = new HttpPost(url);
        post.setHeaders(headers);
        try {
            if (vo.requestDataMap != null) {
                HashMap<String, String> map = vo.requestDataMap;
                ArrayList<BasicNameValuePair> pairList = new ArrayList<BasicNameValuePair>();
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    BasicNameValuePair pair = new BasicNameValuePair(entry.getKey(), entry.getValue());
                    pairList.add(pair);
                }
                HttpEntity entity = new UrlEncodedFormEntity(pairList, "UTF-8");
                post.setEntity(entity);
            }
            HttpResponse response = client.execute(post);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                setCookie(response);
                String result = EntityUtils.toString(response.getEntity(), "UTF-8");
                JSONObject jsonObject = new JSONObject(result);
                if (jsonObject.has("toLogin")) {
                    return "toLogin";
                }
                return jsonObject;
            }
        } catch (ClientProtocolException e) {
            Logger.e(TAG, e.getLocalizedMessage(), e);
        } catch (IOException e) {
            Logger.e(TAG, e.getLocalizedMessage(), e);
        } catch (JSONException e) {
            Logger.e(TAG, e.getLocalizedMessage(), e);
        }
        return null;
    }

    /**
     * 添加Cookie
     * 
     * @param response
     */
    private static void setCookie(HttpResponse response) {
        if (response.getHeaders("Set-Cookie").length > 0) {
            Logger.d(TAG, response.getHeaders("Set-Cookie")[0].getValue());
            headers[0] = new BasicHeader("Cookie", response.getHeaders("Set-Cookie")[0].getValue());
        }
    }


    /**
     * 验证是否需要登录
     * 
     * @param result
     * @return
     * @throws JSONException
     */
    private static boolean invilidateLogin(String result) throws JSONException {
        JSONObject jsonObject = new JSONObject(result);
        String responseCode = jsonObject.getString("response");
        if (NOT_LOGIN.equals(responseCode)) {
            return true;
        }
        return false;
    }

    /**
     * GET请求
     * 
     * @param vo
     * @return
     */
    public static Object get(RequestVo vo) {
        HttpClient client = getHttpClient();
        String url = vo.customer.getIp().concat(vo.requestUrl);
        Logger.d(TAG, "Get " + url);
        HttpGet get = new HttpGet(url);
        get.setHeaders(headers);
        Object obj = null;
        try {
            HttpResponse response = client.execute(get);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                setCookie(response);
                String result = EntityUtils.toString(response.getEntity(), "UTF-8");
                Logger.d(TAG, result);
                obj = net.sf.json.JSONObject.fromObject(result);
                return obj;
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获得网络连接是否可用
     * 
     * @param context
     * @return
     */
    public static boolean hasNetwork(Context context) {
        ConnectivityManager con = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (null == con) {
            return false;
        }
        NetworkInfo workinfo = con.getActiveNetworkInfo();
        if (workinfo != null && workinfo.isAvailable() && workinfo.isConnectedOrConnecting() && workinfo.getState() == NetworkInfo.State.CONNECTED) {
            // 有网络连接
            int connectedType = getConnectedType(context);
            if (connectedType == ConnectivityManager.TYPE_WIFI) {
                return isWifiConnected(context);
            } else {
                return isMobileConnected(context);
            }
        }
        return false;
    }

    /**
     * 判断WiFi是否可用
     * 
     * @param context
     * @return
     */
    public static boolean isWifiConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWiFiNetworkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (mWiFiNetworkInfo != null) {
                return mWiFiNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    /**
     * 判断手机网络是否可用
     * 
     * @param context
     * @return
     */
    public static boolean isMobileConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mMobileNetworkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (mMobileNetworkInfo != null) {
                return mMobileNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    /**
     * 获取网络的连接类型
     * 
     * @param context
     * @return
     */
    public static int getConnectedType(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null && mNetworkInfo.isAvailable()) {
                return mNetworkInfo.getType();
            }
        }
        return -1;
    }

}
