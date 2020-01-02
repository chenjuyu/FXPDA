package com.fuxi.activity;

import java.util.Properties;
import java.util.TimerTask;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebSettings.TextSize;
import android.webkit.WebSettings.ZoomDensity;
import android.webkit.WebView;
import com.fuxi.dao.ParamerDao;
import com.fuxi.main.R;
import com.fuxi.util.LoginParameterUtil;
import com.fuxi.util.ParamterFileUtil;
import com.fuxi.vo.Customer;
import com.fuxi.vo.Paramer;

/**
 * Title: PrintActivity Description: 打印活动界面
 * 
 * @author LYJ
 * 
 */
public class PrintActivity extends BaseWapperActivity {

    private static String TAG = "PrintActivity";
    private static String strIp;
    private WebView mWebView;
    private ParamerDao paramerDao = new ParamerDao(this);
    private String strPrintIp;
    private String strPrintPort;
    private String loadIp;
    private String id = null;
    private String tableName = null;
    private String docType = null;

    private Paramer printPIp;
    private Paramer printPPort;

    private boolean isfinish = true;
    private String printTemplate = null;// 打印模板选择

    @Override
    public void onClick(View v) {}

    @Override
    protected void onHeadRightButton(View v) {
        showProgressDialog(null, "系统正在生成打印预览页,请稍后...");
        // 打印
        mWebView.loadUrl("javascript:print();");
        // 延迟关闭打印效果
        closeDialog();
    }

    /**
     * 关闭打印等待
     */
    private void closeDialog() {
        // 等待效果
        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                closeProgressDialog();
            }
        };
        java.util.Timer timer = new java.util.Timer(true);
        timer.schedule(tt, 5000);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    protected void loadViewLayout() {
        setContentView(R.layout.activity_print);
        setTitle("打印");
    }

    @Override
    protected void setListener() {}

    @Override
    protected void processLogic() {
        Bundle bundle = this.getIntent().getExtras();
        if (null != bundle) {
            id = bundle.getString("id");
            tableName = bundle.getString("tableName");
            docType = bundle.getString("docType");
            if (bundle.containsKey("printTemplate")) {
                printTemplate = bundle.getString("printTemplate");
            }
        }

        // 获取连接地址
        getRequestPath();
        printByLodop();
    }

    /**
     * 通过Lodop云打印控件打印
     */
    private void printByLodop() {
        showProgressDialog(null, "打印预览资源加载,请稍等...");
        // 右上角按钮显示
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
        // 其它设置
        Customer customer = LoginParameterUtil.customer;
        if (null != customer) {
            if (null == customer.getPrintIp() || "".equals(customer.getPrintIp()) || null == customer.getPrintPort() || "".equals(customer.getPrintPort())) {
                show();
                if (strPrintPort != null && !"".equals(strPrintIp) && strPrintPort != null && !"".equals(strPrintPort)) {
                    customer.setPrintIp(strPrintIp);
                    customer.setPrintPort(strPrintPort);
                    LoginParameterUtil.customer = customer;
                } else {
                    isfinish = false;
                    showDialog("获取打印机IP或端口失败,请检查系统设置", "setting");
                }
            }
            if (printTemplate == null || "".equals(printTemplate) || "null".equalsIgnoreCase(printTemplate)) {
                if (LoginParameterUtil.printTemplate == null || "".equals(LoginParameterUtil.printTemplate) || "null".equalsIgnoreCase(LoginParameterUtil.printTemplate)) {
                    printTemplate = "print";
                } else {
                    printTemplate = LoginParameterUtil.printTemplate;
                }
            }
            loadIp = strIp + "/common.do?" + printTemplate + "&id=" + id + "&tableName=" + tableName + "&docType=" + docType + "&userName=" + customer.getUserName() + "&printIp=" + customer.getPrintIp() + "&printPort=" + customer.getPrintPort() + "";
            mWebView.loadUrl(loadIp);
        } else {
            isfinish = false;
            showDialog("无法获取当前用户的登录信息,请重新登录", "login");
        }
        // mWebView.loadUrl("file:///android_asset/print.html");
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress >= 1000) {
                    mWebView.loadUrl(loadIp);
                }
                super.onProgressChanged(view, newProgress);
            }
        });
        // 发送打印请求后结束当前界面
        if (isfinish) {
            closeDialog();
        }
    }

    // /**
    // * 通过网络打印机打印
    // */
    // private void printByNetwork() {
    // // 其它设置
    // final Customer customer = LoginParameterUtil.customer;
    // if (null != customer) {
    // if (null == customer.getPrintIp()
    // || "".equals(customer.getPrintIp())
    // || null == customer.getPrintPort()
    // || "".equals(customer.getPrintPort())) {
    // show();
    // if (strPrintPort != null && !"".equals(strPrintIp)
    // && strPrintPort != null && !"".equals(strPrintPort)) {
    // customer.setPrintIp(strPrintIp);
    // customer.setPrintPort(strPrintPort);
    // LoginParameterUtil.customer = customer;
    // } else {
    // isfinish = false;
    // showDialog("获取打印机IP或端口失败,请检查系统设置", "setting");
    // }
    // }
    // if (printTemplate == null || "".equals(printTemplate)
    // || "null".equalsIgnoreCase(printTemplate)) {
    // if (LoginParameterUtil.printTemplate == null
    // || "".equals(LoginParameterUtil.printTemplate)
    // || "null"
    // .equalsIgnoreCase(LoginParameterUtil.printTemplate)) {
    // printTemplate = "print";
    // } else {
    // printTemplate = LoginParameterUtil.printTemplate;
    // }
    // }
    // loadIp = strIp + "/common.do?" + printTemplate + "&id=" + id
    // + "&tableName=" + tableName + "&docType=" + docType
    // + "&userName=" + customer.getUserName() + "&printIp="
    // + customer.getPrintIp() + "&printPort="
    // + customer.getPrintPort() + "";
    // // doWebViewPrint();
    // } else {
    // showDialog("无法获取当前用户的登录信息,请重新登录", "login");
    // }
    // mWebView.loadUrl(loadIp);
    // createWebPrintJob(mWebView);

    // RequestVo vo = new RequestVo();
    // vo.requestUrl = "/common.do?" + printTemplate + "";
    // vo.context = this;
    // HashMap map = new HashMap();
    // map.put("id", id);
    // map.put("tableName", tableName);
    // map.put("docType", docType);
    // map.put("userName", customer.getUserName());
    // map.put("printIp", customer.getPrintIp());
    // map.put("printPort", customer.getPrintPort());
    // map.put("printType", String.valueOf(printType));
    // vo.requestDataMap = map;
    // super.getDataFromServer(vo, new DataCallback<JSONObject>() {
    // @Override
    // public void processData(JSONObject retObj, boolean paramBoolean) {
    // if (retObj == null) {
    // return;
    // }
    // try {
    // if (retObj.getBoolean("success")) {
    // final String printStr = retObj.getString("obj");
    // new Thread(new Runnable() {
    //
    // @Override
    // public void run() {
    // String html = getHtml(loadIp);
    // String htmlDocument = "<html><body><h1>Test Content</h1><p>Testing, " +
    // "testing, testing...</p></body></html>";
    // NetPrinter printer = new NetPrinter();
    // printer.Open(customer.getPrintIp(), Integer.parseInt(customer.getPrintPort()));
    // printer.Set();
    // printer.PrintText(htmlDocument, 1, 0, 1);
    // closeDialog();
    // }
    // }).start();
    // } else {
    // Toast.makeText(PrintActivity.this, "打印失败,请稍后重试",
    // Toast.LENGTH_SHORT).show();
    // }
    // } catch (Exception e) {
    // Toast.makeText(PrintActivity.this, "系统错误",
    // Toast.LENGTH_LONG).show();
    // Logger.e(TAG, e.getMessage());
    // }
    //
    // }
    // });
    // }

    // private void createWebPrintJob() {
    // // Get a PrintManager instance
    // PrintManager printManager = (PrintManager) this
    // .getSystemService(Context.PRINT_SERVICE);
    // // Get a print adapter instance
    // PrintDocumentAdapter printAdapter = mWebView.createPrintDocumentAdapter();
    // // Create a print job with name and adapter instance
    // String jobName = getString(R.string.app_name) + " Document";
    // PrintJob printJob = printManager.print(jobName, printAdapter,
    // new PrintAttributes.Builder().build());
    // // Save the job object for later status checking
    // // mPrintJobs.add(printJob);
    // }



    /**
     * 打印提示对话框
     * 
     * @param msg
     * @param type
     */
    protected void showDialog(String msg, final String type) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(PrintActivity.this, AlertDialog.THEME_HOLO_LIGHT);
        dialog.setTitle("提示");
        dialog.setMessage(msg);
        // 相当于点击确认按钮
        dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if ("login".equals(type)) {
                    Intent intent = new Intent(PrintActivity.this, LoginActivity.class);
                    startActivity(intent);
                } else if ("setting".equals(type)) {
                    Intent intent = new Intent(PrintActivity.this, SettingActivity.class);
                    startActivity(intent);
                }
                finish();
            }
        });
        dialog.setCancelable(false);
        dialog.create();
        dialog.show();
    }

    /**
     * 获取远程连接服务器的IP地址
     */
    private void getRequestPath() {
        Paramer ip = paramerDao.find("ip");
        if (null != ip) {
            strIp = ip.getValue();
        }
    }

    /**
     * 获取系统设置中的打印机IP和端口
     */
    private void show() {
        // 获取显示值
        printPIp = paramerDao.find("printIp");
        printPPort = paramerDao.find("printPort");
        // 非空判断并绑定显示值
        if (null != printPIp) {
            strPrintIp = printPIp.getValue();
        } else {
            Properties userInfo = ParamterFileUtil.getIpInfo(this);
            if (null != userInfo) {
                strPrintIp = userInfo.getProperty("printIp");
            }
        }
        if (null != printPPort) {
            strPrintPort = printPPort.getValue();
        } else {
            Properties userInfo = ParamterFileUtil.getIpInfo(this);
            if (null != userInfo) {
                strPrintPort = userInfo.getProperty("printPort");
            }
        }
    }

    @JavascriptInterface
    public void print() {
        mWebView.loadUrl("javascript:print()");
    }

    @Override
    protected void findViewById() {
        mWebView = (WebView) findViewById(R.id.myWeb);
    }

}
