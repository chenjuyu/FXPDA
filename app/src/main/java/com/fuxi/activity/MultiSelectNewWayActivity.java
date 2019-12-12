package com.fuxi.activity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import com.fuxi.dao.ParamerDao;
import com.fuxi.main.R;
import com.fuxi.util.LoginParameterUtil;
import com.fuxi.util.ParamterFileUtil;
import com.fuxi.vo.Paramer;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebSettings.TextSize;
import android.webkit.WebSettings.ZoomDensity;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Title: MultiSelectActivity Description: 多尺码多颜色选择界面
 * 
 * @author LYJ
 * 
 */
public class MultiSelectNewWayActivity extends BaseWapperActivity {

    private static final String TAG = "MultiSelectActivity";
    private static final String url = "/common.do?generalColorAndSizeByGoodsId";

    private static boolean innerVersion = false;
    private static String strIp;

    private WebView mWebView;
    private TextView tvSave;

    private List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
    private JsToJava jsToJava = new JsToJava();
    private ParamerDao paramerDao = new ParamerDao(this);

    private String goodsId;
    private String deptId;
    private String tableName;

    /**
     * 获取颜色尺码信息
     */
    private void getColorAndSize() {
        // 打印设置
        WebSettings settings = mWebView.getSettings();
        settings.setTextSize(TextSize.SMALLEST);
        // 自适应屏幕
        settings.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
        settings.setUseWideViewPort(true);// 关键点
        // // 设置可以支持缩放
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true); // 启用内置缩放装置
        // 去掉缩放按钮
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        // //扩大比例的缩放
        // settings.setUseWideViewPort(true);
        // 支持JS
        settings.setJavaScriptEnabled(true);
        // 设置 缓存模式
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        // 开启 DOM storage API 功能
        settings.setDomStorageEnabled(true);
        // 自适应屏幕
        settings.setLoadWithOverviewMode(true);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int mDensity = metrics.densityDpi;
        if (mDensity == 240) {
            settings.setDefaultZoom(ZoomDensity.FAR);
        } else if (mDensity == 160) {
            settings.setDefaultZoom(ZoomDensity.MEDIUM);
        } else if (mDensity == 120) {
            settings.setDefaultZoom(ZoomDensity.CLOSE);
        } else if (mDensity == DisplayMetrics.DENSITY_XHIGH) {
            settings.setDefaultZoom(ZoomDensity.FAR);
        } else if (mDensity == DisplayMetrics.DENSITY_TV) {
            settings.setDefaultZoom(ZoomDensity.FAR);
        } else {
            settings.setDefaultZoom(ZoomDensity.MEDIUM);
        }
        getRequestPath();
        String loadIp = strIp + url + "&GoodsID=" + goodsId + "&DeptID=" + deptId + "&onLineId=" + LoginParameterUtil.onLineId + "&userId=" + LoginParameterUtil.userId + "&tableName=" + tableName;
        mWebView.addJavascriptInterface(jsToJava, "androidShare");
        mWebView.loadUrl(loadIp);
    }

    /**
     * 获取JS的回调值 Title: JsToJava Description:
     * 
     * @author LYJ
     * 
     */
    private class JsToJava {

        @JavascriptInterface
        public void jsMethod(String result) {
            // 处理返回的结果
            if (result != null && !"".equals(result)) {
                if ("-1".equals(result)) {
                    finish();
                    Toast.makeText(MultiSelectNewWayActivity.this, "当前货品未定义货品颜色, 请选择其它商品", Toast.LENGTH_LONG).show();
                } else if ("-2".equals(result)) {
                    finish();
                    Toast.makeText(MultiSelectNewWayActivity.this, "当前货品未定义尺码, 请选择其它商品", Toast.LENGTH_LONG).show();
                } else {
                    String[] objs = result.split(",");
                    for (String obj : objs) {
                        String[] lists = obj.split("_");
                        String sizeId = lists[0];
                        String sizeNo = lists[1];
                        String size = lists[2];
                        String colorId = lists[3];
                        String colorNo = lists[4];
                        String color = lists[5];
                        String quantity = lists[6];
                        // 封装集合
                        Map<String, Object> temp = new HashMap<String, Object>();
                        temp.put("ColorID", colorId);
                        temp.put("ColorCode", colorNo);
                        temp.put("ColorName", color);
                        temp.put("GoodsID", goodsId);
                        temp.put("SizeID", sizeId);
                        temp.put("SizeCode", sizeNo);
                        temp.put("SizeName", size);
                        temp.put("Quantity", quantity);
                        dataList.add(temp);
                    }
                    Intent inte = new Intent();
                    inte.putExtra("datas", (Serializable) dataList);
                    setResult(1, inte);
                    finish();
                }
            }
        }
    }

    /**
     * 获取当前服务器路径
     */
    private void getRequestPath() {
        String flagStr = this.getString(R.string.inner_version);
        innerVersion = Boolean.parseBoolean(flagStr);
        if (innerVersion) {
            // 内部版本
            strIp = this.getString(R.string.inner_ip);
        } else {
            // 对外版本
            Paramer ip = paramerDao.find("ip");
            if (null != ip) {
                strIp = ip.getValue();
            } else {
                Properties userInfo = ParamterFileUtil.getIpInfo(this);
                if (null != userInfo) {
                    strIp = userInfo.getProperty("path");
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.save:
                mWebView.loadUrl("javascript:save();");
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    protected void loadViewLayout() {
        setContentView(R.layout.activity_multi_select_new_way);
        setTitle("货品录入");
    }

    @Override
    protected void setListener() {
        tvSave.setOnClickListener(this);
    }

    @Override
    protected void processLogic() {
        Bundle bundle = this.getIntent().getExtras();
        if (null != bundle) {
            goodsId = bundle.getString("goodsId");
            deptId = bundle.getString("deptId");
            tableName = bundle.getString("tableName");
        }
        // 加载颜色和尺码信息
        getColorAndSize();
    }

    @Override
    protected void findViewById() {
        mWebView = (WebView) findViewById(R.id.myWeb);
        tvSave = (TextView) findViewById(R.id.save);
    }

    @Override
    protected void onHeadLeftButton(View v) {
        // 点击返回时返回上一页面
        saveOrNot();
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            saveOrNot();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 点击返回后询问是否保存单据内容
     */
    private void saveOrNot() {
        // 询问是否保存修改的发货单数据
        Builder dialog = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
        dialog.setTitle("系统提示");
        dialog.setMessage("多颜色多尺码录入尚未完成,确定要返回吗？");
        // 相当于点击确认按钮
        dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        // 相当于点击取消按钮
        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {}
        });
        dialog.create();
        dialog.show();
    }

}
