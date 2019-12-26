package com.fuxi.activity;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;
import java.util.TimerTask;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import com.fuxi.dao.ParamerDao;
import com.fuxi.main.R;
import com.fuxi.util.InstallSlientManager;
import com.fuxi.util.Logger;
import com.fuxi.util.LoginParameterUtil;
import com.fuxi.util.NetUtil;
import com.fuxi.util.ParamterFileUtil;
import com.fuxi.vo.Paramer;
import com.fuxi.vo.UpdataInfo;

/**
 * Title: WelcomeActivity Description: 程序起始欢迎活动界面
 * 
 * @author LJ,LYJ
 * 
 */
public class WelcomeActivity extends Activity {

    private static final String TAG = "WelcomeActivity";
    private static final String updatePath = "/common.do?checkVersion";
    private static final int UPDATA_CLIENT = 1;
    private static final int GET_UNDATAINFO_ERROR = 0;
    private static final int DOWN_ERROR = -1;

    private static String strIp;
    private static boolean forceUpdate = false;
    private static boolean innerVersion = false;

    private ParamerDao paramerDao = new ParamerDao(this);
    private UpdataInfo info = new UpdataInfo();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


      //解决点击图标重新打开应用
        if((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) !=0){
            finish();
            return;
        }

        setContentView(R.layout.welcome_activity);
        // 实例化TextView
        TextView tv_wel = (TextView) findViewById(R.id.welcome_version);
        try {
            // 获取当前的版本号
            tv_wel.setText(getVersionName());
            // 获取服务器IP
            getRequestPath();
            if (null != strIp && !"".equals(strIp)) {
                getUpdataInfo();
            } else {
                runToLogin();
            }
        } catch (Exception e1) {
            Toast.makeText(WelcomeActivity.this, "系统参数错误", Toast.LENGTH_LONG).show();
            Logger.e(TAG, e1.getMessage());
            finish();
            System.exit(0);
        }
    }

    /**
     * 获取服务器连接地址
     */
    private void getRequestPath() {
        try {
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
                        if (strIp != null && !"".equals(strIp) && "null".equalsIgnoreCase(strIp)) {
                            // 保存IP到服务器缓存
                            ip = new Paramer("ip", strIp);
                            paramerDao.insert(ip);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Logger.e(TAG, e);
        }
    }

    /**
     * 检测程序新版本,是否需要更新程序
     */
    private void checkVersion() {
        try {
            if (info.getVersion().equals(getVersionName())) {
                Log.i(TAG, "版本号相同无需升级");
                runToLogin();
            } else {
                Log.i(TAG, "版本号不同 ,提示用户升级 ");
                Message msg = new Message();
                msg.what = UPDATA_CLIENT;
                handler.sendMessage(msg);
            }
        } catch (Exception e) {
            Toast.makeText(WelcomeActivity.this, "系统错误", Toast.LENGTH_LONG).show();
            Logger.e(TAG, e.getMessage());
        }
    }

    /**
     * 登录方法
     */
    private void gotoLogin() {
        Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * 获取当前程序的版本号
     * 
     * @return
     * @throws Exception
     */
    private String getVersionName() throws Exception {
        // 获取PackageManager的实例
        PackageManager packageManager = getPackageManager();
        // getPackageName()是你当前类的包名，0代表是获取版本信息
        PackageInfo packInfo = packageManager.getPackageInfo(getPackageName(), 0);
        return packInfo.versionName;
    }

    /**
     * 访问数据库并返回JSON数据字符串
     * 
     * @param params 向服务器端传的参数
     * @param url
     * @return
     * @throws Exception
     */
    public void getUpdataInfo() throws Exception {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    // 获取HttpClient对象
                    HttpClient httpClient = NetUtil.getHttpClient();
                    // 新建HttpPost对象
                    HttpPost httpPost = new HttpPost(strIp + updatePath);
                    // 获取HttpResponse实例
                    HttpResponse httpResp = httpClient.execute(httpPost);
                    // 判断是够请求成功
                    if (httpResp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                        // 获取返回的数据
                        String json = EntityUtils.toString(httpResp.getEntity(), "UTF-8");
                        JSONObject obj = new JSONObject(json);
                        obj = obj.getJSONObject("obj");
                        info.setVersion(obj.getString("Version")); // 获取版本号
                        info.setUrl(obj.getString("Url")); // 获取要升级的APK文件
                        info.setDescription(obj.getString("Description")); // 获取该文件的信息
                        forceUpdate = obj.getBoolean("ForceUpdate"); // 是否强制更新
                        // info.setDescription("检测到最新版本,请及时更新!"); //获取该文件的信息
                        LoginParameterUtil.corpName = obj.getString("corpName"); // 获取该文件的信息
                        LoginParameterUtil.regId = obj.getString("regId"); // 获取该文件的信息
                        LoginParameterUtil.EditQty =obj.getBoolean("EditQty");//装箱单明细，允许修改数量
                        // 销售订货和发货单使用区域保护
                        LoginParameterUtil.useAreaProtection =obj.getBoolean("useAreaProtection");
                        //#根据厂商货品编码校验地区重版
                        LoginParameterUtil.useSupplierCodeToAreaProtection =obj.getBoolean("useSupplierCodeToAreaProtection");
                        //
                        LoginParameterUtil.notUseNegativeInventoryCheck =obj.getBoolean("notUseNegativeInventoryCheck");
                        //#单据不使用负库存检查
                        checkVersion();
                    } else {
                        Log.i("HttpGet", "HttpGet方式请求失败");
                        throw new Exception("网络连接超时");
                    }
                } catch (Exception e) {
                    Logger.e(TAG, e.getMessage());
                    Message msg = new Message();
                    msg.what = GET_UNDATAINFO_ERROR;
                    handler.sendMessage(msg);
                }
            }
        }).start();
    }

    /**
     * 下载服务端的新版本程序
     * 
     * @param path
     * @param pd
     * @return
     * @throws Exception
     */
    public File getFileFromServer(String path, ProgressDialog pd) throws Exception {
        // 如果相等的话表示当前的SDCard挂载在手机上并且是可用的
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            URL url = new URL(path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(8000);
            // 获取到文件的大小
            int max = conn.getContentLength();
            pd.setMax(100);
            InputStream is = conn.getInputStream();
            File file = new File(Environment.getExternalStorageDirectory(), "fuxiPDA_" + info.getVersion() + ".apk");
            FileOutputStream fos = new FileOutputStream(file);
            BufferedInputStream bis = new BufferedInputStream(is);
            byte[] buffer = new byte[1024];
            int len;
            int total = 0;
            while ((len = bis.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
                total += len;
                // 获取当前下载量
                pd.setProgress((total * 100) / max);
            }
            fos.close();
            bis.close();
            is.close();
            return file;
        } else {
            return null;
        }
    }

    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATA_CLIENT:
                    // 对话框通知用户升级程序
                    showUpdataDialog();
                    break;
                case GET_UNDATAINFO_ERROR:
                    // 服务器超时
                    boolean flag = NetUtil.hasNetwork(getApplicationContext());
                    if (!flag) {
                        settingNetWork();
                    } else {
                        if (innerVersion) {
                            runToLogin();
                        } else {
                            changeUrl();
                        }
                    }
                    break;
                case DOWN_ERROR:
                    // 下载APK失败
                    Toast.makeText(getApplicationContext(), "下载新版本失败", 1).show();
                    runToLogin();
                    break;
            }
        }
    };

    /**
     * 设备无网络连接时的提示
     */
    protected void settingNetWork() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(WelcomeActivity.this, AlertDialog.THEME_HOLO_LIGHT);
        dialog.setTitle("提示");
        dialog.setMessage("当前无网络连接，请检查网络设置！请点击「检查网络」检查设备连网状况，点击「离线版本」系统将直接进入系统主界面，除离线盘点功能外其余功能将不可用！");
        // 相当于点击确认按钮
        dialog.setPositiveButton("检查网络", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent("android.net.wifi.PICK_WIFI_NETWORK");
                startActivityForResult(intent, 0);
            }
        });
        dialog.setNegativeButton("离线版本", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 跳转至主界面
                LoginParameterUtil.online = false;
                Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        dialog.setCancelable(false);
        dialog.create();
        dialog.show();
    }

    /**
     * 
     * 弹出对话框通知用户更新程序
     * 
     * 弹出对话框的步骤： 1.创建alertDialog的builder. 2.要给builder设置属性, 对话框的内容,样式,按钮 3.通过builder 创建一个对话框
     * 4.对话框show()出来
     */
    protected void showUpdataDialog() {
        AlertDialog.Builder builer = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
        builer.setTitle("版本升级");
        builer.setMessage(info.getDescription());
        // 当点确定按钮时从服务器上下载 新的apk 然后安装
        builer.setPositiveButton("确定", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Log.i(TAG, "下载apk,更新");
                downLoadApk();
            }
        });
        // 当点取消按钮时进行登录
        if (!forceUpdate) {
            builer.setNegativeButton("取消", new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    runToLogin();;
                }
            });
        }
        builer.setCancelable(false);
        AlertDialog dialog = builer.create();
        dialog.show();
    }

    /**
     * 从服务器中下载APK文件
     */
    protected void downLoadApk() {
        final ProgressDialog pd; // 进度条对话框
        pd = new ProgressDialog(this);
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setCanceledOnTouchOutside(false);
        pd.setMessage("正在下载更新");
        pd.show();
        new Thread() {
            @Override
            public void run() {
                try {
                    File file = getFileFromServer(strIp + info.getUrl(), pd);
                    sleep(3000);
                    installApk(file);
                    pd.dismiss(); // 结束掉进度条对话框
                } catch (Exception e) {
                    Message msg = new Message();
                    msg.what = DOWN_ERROR;
                    handler.sendMessage(msg);
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * 安装APK应用程序
     * 
     * @param file
     */
    protected void installApk(File file) {
        // boolean flag =
        // InstallSlientManager.deleteDatabase(getApplicationContext());
        // if(flag){
        InstallSlientManager.install(file.getAbsolutePath(), getApplicationContext());
        // }
        finish();
    }

    /**
     * 跳转至登陆页面
     */
    private void runToLogin() {
        // 跳转登陆页面，定时模拟等待效果
        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                gotoLogin();
            }
        };
        java.util.Timer timer = new java.util.Timer(true);
        timer.schedule(tt, 1000);
    }

    /**
     * 设置连接网络后重新加载数据
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 0:
                if (resultCode == 0) {
                    try {
                        getUpdataInfo();
                    } catch (Exception e) {
                        Toast.makeText(WelcomeActivity.this, "系统参数错误", Toast.LENGTH_LONG).show();
                        Logger.e(TAG, e.getMessage());
                        finish();
                        System.exit(0);
                    }
                }
                break;
            default:
                break;
        }
    }

    // /**
    // * 检测网络是否连接
    // * @return
    // */
    // protected boolean checkNetworkState() {
    // boolean flag = false;
    // // 得到网络连接信息
    // ConnectivityManager manager = (ConnectivityManager)
    // getSystemService(Context.CONNECTIVITY_SERVICE);
    // // 去进行判断网络是否连接
    // if (manager.getActiveNetworkInfo() != null) {
    // flag = manager.getActiveNetworkInfo().isAvailable();
    // }
    // return flag;
    // }

    /**
     * 询问是否更换系统设置参数
     */
    protected void changeUrl() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(WelcomeActivity.this, AlertDialog.THEME_HOLO_LIGHT);
        dialog.setTitle("提示");
        dialog.setMessage("服务器连接超时，系统配置参数加载失败，请点击「前往检查」后仔细检查服务器访问地址，若地址有误，请重新配置服务器访问地址");
        // 相当于点击确认按钮
        dialog.setPositiveButton("前往检查", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(WelcomeActivity.this, SystemSettingsActivity.class);
                startActivityForResult(intent, 0);
            }
        });
        dialog.setNegativeButton("稍后重试", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
                System.exit(0);
            }
        });
        dialog.setCancelable(false);
        dialog.create();
        dialog.show();
    }

}
