package com.fuxi.vo;

import java.util.HashMap;
import android.content.Context;
import com.fuxi.util.LoginParameterUtil;

/**
 * Title: RequestVo Description: HTTP连接请求时的对象封装
 * 
 * @author LYJ
 * 
 */
public class RequestVo {

    public String requestUrl;
    public Context context;
    public HashMap<String, String> requestDataMap;
    public Customer customer = LoginParameterUtil.customer;

    public RequestVo() {}

    public RequestVo(String requestUrl, Context context, HashMap<String, String> requestDataMap) {
        super();
        this.requestUrl = requestUrl;
        this.context = context;
        this.requestDataMap = requestDataMap;
    }

}
