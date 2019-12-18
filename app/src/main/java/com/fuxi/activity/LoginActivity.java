package com.fuxi.activity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.fuxi.application.ECApplication;
import com.fuxi.dao.ParamerDao;
import com.fuxi.dao.RegisterDao;
import com.fuxi.main.R;
import com.fuxi.util.AppManager;
import com.fuxi.util.Logger;
import com.fuxi.util.LoginParameterUtil;
import com.fuxi.util.ParamterFileUtil;
import com.fuxi.util.Tools;
import com.fuxi.vo.Customer;
import com.fuxi.vo.Paramer;
import com.fuxi.vo.Register;
import com.fuxi.vo.RequestVo;

/**
 * Title: LoginActivity Description: 登录活动界面
 * 
 * @author LYJ
 * 
 */
public class LoginActivity extends BaseWapperActivity {

    private static final String TAG = "LoginActivity";
    private final static String login_method = "/login.do?login";
    private static final String updatePath = "/common.do?checkVersion";
    private static final int CHECK_LOGIN_ERROR = -1;
    private static final int CHECK_LOGIN_SUCCESS = 1;
    private static final int LOAD_FILE_ERROR = 2;

    private static String checkPath = "http://rz.fxsoft88.com:6080/ws.do?pda";
    private static boolean innerVersion = false;
    private static AppManager manager = new AppManager();
    private static Customer customer = LoginParameterUtil.customer;

    private ParamerDao paramerDao = new ParamerDao(this);
    private RegisterDao registerDao = new RegisterDao(this);

    private EditText etUserName;
    private EditText etPassword;
    private Button btLogin;
    private TextView settings;
    private CheckBox cbRememberPassword;
    private TextView tvRememberPassword;

    private String userName;
    private String userPwd;
    private String strIp;
    private Intent intent;
    private boolean rememberPassword;
    private long exitTime = 0; // 记录按回退键的时间间隔
    
    private Paramer pRememberPassword;

    /**
     * 用户登录方法
     */
    private void login() {
        userName = etUserName.getText().toString().trim();
        userPwd = etPassword.getText().toString().trim();
        // 判断非空
        if (userName == null || "".equals(userName)) {
            Toast.makeText(LoginActivity.this, R.string.username_null, Toast.LENGTH_SHORT).show();
            return;
        }
        if (userPwd == null || "".equals(userPwd)) {
            Toast.makeText(LoginActivity.this, R.string.psw_null, Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            // 登录时获取系统设置中的参数值
            getSettingsParam();
            Register register = registerDao.find();
            if (null == register || register.getSecret() == null || register.getSecret().isEmpty()) {
                if (LoginParameterUtil.regId == null || LoginParameterUtil.regId.isEmpty() || "".equals(LoginParameterUtil.regId)) {
                    throw new NullPointerException("配置文件加载失败");
                }
                checkLogin();
            } else {
                // 判断注册时间是否失效(重新认证)
                String registerDate = register.getMadeDate();
                if (registerDate == null || registerDate.isEmpty() || "null".equalsIgnoreCase(registerDate)) {
                    checkLogin();
                } else {
                    int daysBetween = Tools.daysBetween(Tools.dateStringToTime(registerDate), new Date());
                    if (daysBetween >= 7) {// 重新认证
                        checkLogin();
                    } else {
                        String mSerial = android.os.Build.SERIAL;
                        String mac = tryGetWifiMac(getApplicationContext());
                        if (mac == null) {
                            openWiFi();
                            mac = getMac();
                        }
                        if (mac == null) {
                            Toast.makeText(LoginActivity.this, "设备MAC地址获取失败，无法进行注册操作！", Toast.LENGTH_LONG).show();
                            return;
                        }
                        String mType = Build.MODEL;
                        String mBrand = Build.BRAND;
                        String mCPU = Build.CPU_ABI;
                        if (register.getType() == 0) {
                            // MD5加密
                            String regId = Tools.getByteToString(Tools.encryptMD5(LoginParameterUtil.regId.getBytes())).substring(0, 16);
                            String param = regId + "|" + mSerial + "|" + mCPU + "|" + mac + "|" + mBrand + "|" + mType + "|" + LoginParameterUtil.regId;
                            String encryptParam = Tools.getByteToString(Tools.encryptMD5(param.getBytes()));
                            if (encryptParam.equals(register.getSecret())) {
                                toLogin();
                            } else {
                                checkLogin();
                            }
                        } else if (register.getType() == 1) {
                            String param = mSerial + "|" + mCPU + "|" + mac + "|" + mBrand + "|" + mType;
                            String encryptParam = Tools.getByteToString(Tools.encryptMD5(param.getBytes()));
                            if (encryptParam.equals(register.getSecret())) {
                                toLogin();
                            } else {
                                checkLogin();
                            }
                        }
                    }
                }
            }
        } catch (NullPointerException e) {
            Logger.e(TAG, e.getMessage());
            AlertDialog.Builder dialog = new AlertDialog.Builder(LoginActivity.this, AlertDialog.THEME_HOLO_LIGHT);
            dialog.setTitle("系统提示");
            dialog.setMessage("服务器连接超时,配置文件加载失败");
            // 相当于点击确认按钮
            dialog.setPositiveButton("重新加载", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    loadSystemParam();
                }
            });
            dialog.create();
            dialog.show();
        } catch (Exception e1) {
            if ("系统设置中服务器地址不能为空".equals(e1.getMessage())) {
                Toast.makeText(LoginActivity.this, "服务器连接地址不能为空", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(LoginActivity.this, SystemSettingsActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(LoginActivity.this, "系统错误", Toast.LENGTH_LONG).show();
            }
            Logger.e(TAG, e1.getMessage());
        }
    }

    /**
     * 连接服务器,检查PDA是否注册
     * 
     * @throws Exception
     */
    private void checkLogin() throws Exception {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    if (LoginParameterUtil.regId == null || LoginParameterUtil.regId.isEmpty() || "".equals(LoginParameterUtil.regId)) {
                        throw new NullPointerException("配置文件加载失败");
                    }

                    // 获取设备的相关信息
                    String mSerial = android.os.Build.SERIAL;
                    String mac = tryGetWifiMac(getApplicationContext());
                    if (mac == null) {
                        openWiFi();
                        mac = getMac();
                    }
                    if (mac == null) {
                        Toast.makeText(LoginActivity.this, "设备MAC地址获取失败，无法进行注册操作！", Toast.LENGTH_LONG).show();
                        return;
                    }
                    String mType =Build.MODEL.replace(" ", "");
                    	  //mType=mType.replace(" ", "");
                    String mBrand = Build.BRAND.replace(" ", "");
                    String mCPU = Build.CPU_ABI.replace(" ", "");
                    // 获取HttpClient对象
                    HttpClient httpClient = new DefaultHttpClient();
                    // 新建HttpPost对象
                    String reqParam="";
                    reqParam =reqParam + "&REGID=" + LoginParameterUtil.regId + "&CorpName=" + LoginParameterUtil.corpName + "&MSerial=" + mSerial + "&Mac=" + mac + "&MType=" + mType + "&MBrand=" + mBrand + "&MCPU=" + mCPU;
                  //  System.out.println("aaa:"+reqParam);
                    //Log.i(TAG, "checkPath的值："+checkPath+reqParam);
                    HttpGet httpGet = new HttpGet(checkPath+reqParam);
                   // Log.i(TAG, "checkPath的值："+checkPath);
        
                    
                    // 链接服务器超时处理
                    httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 8000);
                    httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 8000);
                    // 获取HttpResponse实例
                    HttpResponse httpResp = httpClient.execute(httpGet);
                    // 判断是够请求成功
                    if (httpResp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                        // 获取返回的数据
                        String json = EntityUtils.toString(httpResp.getEntity(), "UTF-8");
                        JSONObject obj = new JSONObject(json);
                        obj = obj.getJSONObject("obj");
                        if (obj == null) {
                            throw new Exception("验证未通过");
                        }
                        // 返回登录验证参数
                        int resultCode = obj.getInt("result_code");
                        String secret = obj.getString("secret");
                        int type = obj.getInt("type");
                        if (resultCode == 1) {
                            String madeDate = Tools.dateTimeToString(new Date());
                            if (type == 0) {
                                // MD5加密
                                String regId = Tools.getByteToString(Tools.encryptMD5(LoginParameterUtil.regId.getBytes())).substring(0, 16);
                                String param = regId + "|" + mSerial + "|" + mCPU + "|" + mac + "|" + mBrand + "|" + mType + "|" + LoginParameterUtil.regId;
                                String encryptParam = Tools.getByteToString(Tools.encryptMD5(param.getBytes()));
                                if (encryptParam.equals(secret)) {
                                    registerDao.insert(new Register(secret, type, madeDate));
                                    Message msg = new Message();
                                    msg.what = CHECK_LOGIN_SUCCESS;
                                    handler.sendMessage(msg);
                                } else {
                                    throw new Exception("验证未通过");
                                }
                            } else if (type == 1) {
                                String param = mSerial + "|" + mCPU + "|" + mac + "|" + mBrand + "|" + mType;
                                String encryptParam = Tools.getByteToString(Tools.encryptMD5(param.getBytes()));
                                if (encryptParam.equals(secret)) {
                                    registerDao.insert(new Register(secret, type, madeDate));
                                    Message msg = new Message();
                                    msg.what = CHECK_LOGIN_SUCCESS;
                                    handler.sendMessage(msg);
                                } else {
                                    throw new Exception("验证未通过");
                                }
                            }
                        } else {
                            throw new Exception("验证未通过");
                        }
                    } else {
                        Log.i("HttpGet", "HttpGet方式请求失败");
                        throw new Exception("网络连接超时");
                    }
                } catch (NullPointerException e) {
                    Logger.e(TAG, e.getMessage());
                    Message msg = new Message();
                    msg.what = LOAD_FILE_ERROR;
                    handler.sendMessage(msg);
                } catch (Exception e) {
                    Logger.e(TAG, e.getMessage());
                    Message msg = new Message();
                    msg.what = CHECK_LOGIN_ERROR;
                    handler.sendMessage(msg);
                }
            }
        }).start();
    }

    /**
     * 加载系统参数配置文件
     */
    private void loadSystemParam() {
        try {
            RequestVo vo = new RequestVo();
            vo.requestUrl = updatePath;
            vo.context = this;
            HashMap map = new HashMap();
            vo.requestDataMap = map;
            super.getDataFromServer(vo, new DataCallback<JSONObject>() {
                @Override
                public void processData(JSONObject retObj, boolean paramBoolean) {
                    try {
                        if (retObj == null) {
                            return;
                        }
                        if (retObj.getBoolean("success")) {
                            JSONObject obj = retObj.getJSONObject("obj");
                            LoginParameterUtil.corpName = obj.getString("corpName"); // 获取该文件的信息
                            LoginParameterUtil.regId = obj.getString("regId"); // 获取该文件的信息
                            Toast.makeText(LoginActivity.this, "配置文件加载成功", Toast.LENGTH_SHORT).show();
                            login();
                        } else {
                            Toast.makeText(LoginActivity.this, "服务器连接超时,配置文件加载失败", Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(LoginActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                        Logger.e(TAG, e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            Toast.makeText(LoginActivity.this, "服务器地址错误,配置文件加载失败", Toast.LENGTH_LONG).show();
            Logger.e(TAG, e.getMessage());
        }
    }

    /**
     * 线程返回消息的处理
     */
    Handler handler = new Handler() {


		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case CHECK_LOGIN_ERROR:
                toLogin(); //toLogin();// prompt();// toLogin(); //手机不要验证 2019-02-27 直接登录
				break;
			case CHECK_LOGIN_SUCCESS:
				toLogin();
				break;
			case LOAD_FILE_ERROR:
				AlertDialog.Builder dialog = new AlertDialog.Builder(
						LoginActivity.this, AlertDialog.THEME_HOLO_LIGHT);
				dialog.setTitle("系统提示");
				dialog.setMessage("服务器连接超时,配置文件加载失败");
				// 相当于点击确认按钮
				dialog.setPositiveButton("重新加载",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								loadSystemParam();
							}
						});
				dialog.setCancelable(false);
				dialog.create();
				dialog.show();
				break;
			}
		}
	};
  /*
        public void handleMessage(Message msg) {
          // super.handleMessage(msg);
            switch (msg.what) {
                case CHECK_LOGIN_ERROR:
                	toLogin();//prompt();
                    break;
                case CHECK_LOGIN_SUCCESS:
                    toLogin();
                    break;
                case LOAD_FILE_ERROR:
                    AlertDialog.Builder dialog = new AlertDialog.Builder(LoginActivity.this, AlertDialog.THEME_HOLO_LIGHT);
                    dialog.setTitle("系统提示");
                    dialog.setMessage("服务器连接超时,配置文件加载失败");
                    // 相当于点击确认按钮
                    dialog.setPositiveButton("重新加载", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            loadSystemParam();
                        }
                    });
                    dialog.setCancelable(false);
                    dialog.create();
                    dialog.show();
                    break;
            }
        }
    */


    /**
     * 设备未注册时的系统提示
     */
    private void prompt() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(LoginActivity.this, AlertDialog.THEME_HOLO_LIGHT);
        dialog.setTitle("系统提示");
        dialog.setMessage("您的设备 " + android.os.Build.SERIAL + " 未注册,请联系开发商注册后再进行相应操作");
        // 相当于点击确认按钮
        dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        dialog.setCancelable(false);
        dialog.create();
        dialog.show();
    }

    /**
     * 远程连接服务器执行登录操作,返回登录结果
     */
    private void toLogin() {
        RequestVo vo = new RequestVo();
        vo.requestUrl = login_method;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("username", userName);
        map.put("password", userPwd);
        map.put("hostName", Build.MODEL);
        vo.requestDataMap = map;
        super.getDataFromServer(vo, new DataCallback<JSONObject>() {
            @SuppressWarnings("unchecked")
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    if (retObj.getBoolean("success")) {
                        LoginParameterUtil.online = true;
                        // 登录成功时保存账号和密码
                        saveProduct();
                        // 加载系统设置里面的参数
                        loadSysParam();
                        // 保存登录IP
                        String userId = retObj.getJSONObject("obj").getString("userId");
                        String onLineId = retObj.getJSONObject("obj").getString("onLineId");
                        String deptId = retObj.getJSONObject("obj").getString("deptId");
                        String deptName = retObj.getJSONObject("obj").getString("deptName");
                        String userName = retObj.getJSONObject("obj").getString("userName");
                        String imagePath = retObj.getJSONObject("obj").getString("imagePath");
                        String possalesTile = retObj.getJSONObject("obj").getString("possalesTile");
                        String possalesParam1 = retObj.getJSONObject("obj").getString("possalesParam1");
                        String possalesParam2 = retObj.getJSONObject("obj").getString("possalesParam2");
                        String possalesParam3 = retObj.getJSONObject("obj").getString("possalesParam3");
                        String possalesParam4 = retObj.getJSONObject("obj").getString("possalesParam4");
                        String possalesParam5 = retObj.getJSONObject("obj").getString("possalesParam5");
                        String printTemplate = retObj.getJSONObject("obj").getString("printTemplate");
                       
                        double discountRate = retObj.getJSONObject("obj").getDouble("discountRate");
                        Log.i("i","aaaaa:"+String.valueOf(discountRate));
                        boolean usingOrderGoodsModule = retObj.getJSONObject("obj").getBoolean("usingOrderGoodsModule");
                        boolean purchasePriceRight = retObj.getJSONObject("obj").getBoolean("purchasePriceRight");
                        boolean subTypeRight = retObj.getJSONObject("obj").getBoolean("subTypeRight");
                        boolean brandRight = retObj.getJSONObject("obj").getBoolean("brandRight");
                        boolean brandSerialRight = retObj.getJSONObject("obj").getBoolean("brandSerialRight");
                        boolean kindRight = retObj.getJSONObject("obj").getBoolean("kindRight");
                        boolean ageRight = retObj.getJSONObject("obj").getBoolean("ageRight");
                        boolean seasonRight = retObj.getJSONObject("obj").getBoolean("seasonRight");
                        boolean supplierRight = retObj.getJSONObject("obj").getBoolean("supplierRight");
                        boolean supplierCodeRight = retObj.getJSONObject("obj").getBoolean("supplierCodeRight");
                        boolean retailSalesRight = retObj.getJSONObject("obj").getBoolean("retailSalesRight");
                        boolean retailSales1Right = retObj.getJSONObject("obj").getBoolean("retailSales1Right");
                        boolean retailSales2Right = retObj.getJSONObject("obj").getBoolean("retailSales2Right");
                        boolean retailSales3Right = retObj.getJSONObject("obj").getBoolean("retailSales3Right");
                        boolean tradePriceRight = retObj.getJSONObject("obj").getBoolean("tradePriceRight");
                        boolean salesPrice1Right = retObj.getJSONObject("obj").getBoolean("salesPrice1Right");
                        boolean salesPrice2Right = retObj.getJSONObject("obj").getBoolean("salesPrice2Right");
                        boolean salesPrice3Right = retObj.getJSONObject("obj").getBoolean("salesPrice3Right");
                        boolean purchasedDateRight = retObj.getJSONObject("obj").getBoolean("purchasedDateRight");
                        boolean lastPurchasedDateRight = retObj.getJSONObject("obj").getBoolean("lastPurchasedDateRight");
                        boolean supplierPhoneRight = retObj.getJSONObject("obj").getBoolean("supplierPhoneRight");
                        boolean purchaseAmountSumRight = retObj.getJSONObject("obj").getBoolean("purchaseAmountSumRight");
                        boolean purchaseUnitPriceRight = retObj.getJSONObject("obj").getBoolean("purchaseUnitPriceRight");
                        boolean purchaseReturnAmountSumRight = retObj.getJSONObject("obj").getBoolean("purchaseReturnAmountSumRight");
                        boolean purchaseReturnUnitPriceRight = retObj.getJSONObject("obj").getBoolean("purchaseReturnUnitPriceRight");
                        boolean salesAmountSumRight = retObj.getJSONObject("obj").getBoolean("salesAmountSumRight");
                        boolean salesUnitPriceRight = retObj.getJSONObject("obj").getBoolean("salesUnitPriceRight");
                        boolean salesReturnAmountSumRight = retObj.getJSONObject("obj").getBoolean("salesReturnAmountSumRight");
                        boolean salesReturnUnitPriceRight = retObj.getJSONObject("obj").getBoolean("salesReturnUnitPriceRight");
                        boolean salesOrderAmountSumRight = retObj.getJSONObject("obj").getBoolean("salesOrderAmountSumRight");
                        boolean salesOrderUnitPriceRight = retObj.getJSONObject("obj").getBoolean("salesOrderUnitPriceRight");
                        boolean stockMoveAmountSumRight = retObj.getJSONObject("obj").getBoolean("stockMoveAmountSumRight");
                        boolean stockMoveUnitPriceRight = retObj.getJSONObject("obj").getBoolean("stockMoveUnitPriceRight");
                        boolean stocktakingAmountSumRight = retObj.getJSONObject("obj").getBoolean("stocktakingAmountSumRight");
                        boolean stockInAmountSumRight = retObj.getJSONObject("obj").getBoolean("stockInAmountSumRight");
                        boolean stockInUnitPriceRight = retObj.getJSONObject("obj").getBoolean("stockInUnitPriceRight");
                        boolean stockOutAmountSumRight = retObj.getJSONObject("obj").getBoolean("stockOutAmountSumRight");
                        boolean stockOutUnitPriceRight = retObj.getJSONObject("obj").getBoolean("stockOutUnitPriceRight");
                        boolean useBrandPower = retObj.getJSONObject("obj").getBoolean("useBrandPower");
                        boolean hasStorage = retObj.getJSONObject("obj").getBoolean("hasStorage");
                        boolean unitPricePermitFlag = retObj.getJSONObject("obj").getBoolean("unitPricePermitFlag");
                        boolean discountRatePermitFlag2 = retObj.getJSONObject("obj").getBoolean("discountRatePermitFlag2");
                        boolean warehouseFlag = retObj.getJSONObject("obj").getBoolean("warehouseFlag");
                        boolean queryStockTotal = retObj.getJSONObject("obj").getBoolean("queryStockTotal");
                        boolean discountRatePermitFlag = retObj.getJSONObject("obj").getBoolean("discountRatePermitFlag");
                        boolean showGiftMenuFlag = retObj.getJSONObject("obj").getBoolean("showGiftMenuFlag");
                        String relationMovein = retObj.getJSONObject("obj").getString("relationMovein");
                        JSONArray goodsMenuRight = retObj.getJSONObject("obj").getJSONArray("goodsUserMenuRight");
                        JSONArray customerMenuRight = retObj.getJSONObject("obj").getJSONArray("customerUserMenuRight");
                        JSONArray salesOrderMenuRight = retObj.getJSONObject("obj").getJSONArray("salesOrderMenuRight");
                        JSONArray salesMenuRight = retObj.getJSONObject("obj").getJSONArray("salesMenuRight");
                        JSONArray salesReturnMenuRight = retObj.getJSONObject("obj").getJSONArray("salesReturnMenuRight");
                        JSONArray purchaseMenuRight = retObj.getJSONObject("obj").getJSONArray("purchaseMenuRight");
                        JSONArray purchaseReturnMenuRight = retObj.getJSONObject("obj").getJSONArray("purchaseReturnMenuRight");
                        JSONArray stockMoveMenuRight = retObj.getJSONObject("obj").getJSONArray("stockMoveMenuRight");
                        JSONArray stocktakingMenuRight = retObj.getJSONObject("obj").getJSONArray("stocktakingMenuRight");
                        JSONArray stockInMenuRight = retObj.getJSONObject("obj").getJSONArray("stockInMenuRight");
                        JSONArray stockOutMenuRight = retObj.getJSONObject("obj").getJSONArray("stockOutMenuRight");
                        JSONArray stocktakingQueryMenuRight = retObj.getJSONObject("obj").getJSONArray("stocktakingQueryMenuRight");
                        JSONArray posSalesMenuRight = retObj.getJSONObject("obj").getJSONArray("posSalesMenuRight");
                        JSONArray packingBoxMenuRight = retObj.getJSONObject("obj").getJSONArray("packingBoxMenuRight");
                        JSONArray giftMenuRight = retObj.getJSONObject("obj").getJSONArray("giftMenuRight");
                        JSONArray dailyKnotsMenuRight = retObj.getJSONObject("obj").getJSONArray("dailyKnotsMenuRight");
                        JSONArray purchaseOrderMenuRight =retObj.getJSONObject("obj").getJSONArray("purchaseOrderMenuRight");
                        // 货品资料操作权限
                        LoginParameterUtil.goodsRightMap = convertCollection(goodsMenuRight.getJSONObject(0));
                        // 客户资料操作权限
                        LoginParameterUtil.customerRightMap = convertCollection(customerMenuRight.getJSONObject(0));
                        // 销售订单操作权限
                        LoginParameterUtil.salesOrderRightMap = convertCollection(salesOrderMenuRight.getJSONObject(0));
                        // 销售发货单操作权限
                        LoginParameterUtil.salesRightMap = convertCollection(salesMenuRight.getJSONObject(0));
                        // 销售退货单操作权限
                        LoginParameterUtil.salesReturnRightMap = convertCollection(salesReturnMenuRight.getJSONObject(0));

                        //采购订单操作权限
                        LoginParameterUtil.purchaseOrderRightMap =convertCollection(purchaseOrderMenuRight.getJSONObject(0));
                        // 采购收货单操作权限
                        LoginParameterUtil.purchaseRightMap = convertCollection(purchaseMenuRight.getJSONObject(0));
                        // 采购退货单操作权限
                        LoginParameterUtil.purchaseReturnRightMap = convertCollection(purchaseReturnMenuRight.getJSONObject(0));
                        // 转仓单操作权限
                        LoginParameterUtil.stockMoveRightMap = convertCollection(stockMoveMenuRight.getJSONObject(0));
                        // 盘点单操作权限
                        LoginParameterUtil.stocktakingRightMap = convertCollection(stocktakingMenuRight.getJSONObject(0));
                        // 进仓单操作权限
                        LoginParameterUtil.stockInRightMap = convertCollection(stockInMenuRight.getJSONObject(0));
                        // 出仓单操作权限
                        LoginParameterUtil.stockOutRightMap = convertCollection(stockOutMenuRight.getJSONObject(0));
                        // 库存查询操作权限
                        LoginParameterUtil.stocktakingQueryRightMap = convertCollection(stocktakingQueryMenuRight.getJSONObject(0));
                        // 销售小票操作权限
                        LoginParameterUtil.posSalesRightMap = convertCollection(posSalesMenuRight.getJSONObject(0));
                        // 装箱单操作权限
                        LoginParameterUtil.packingBoxRightMap = convertCollection(packingBoxMenuRight.getJSONObject(0));
                        // 赠品单操作权限
                        LoginParameterUtil.giftMenuRight = convertCollection(giftMenuRight.getJSONObject(0));
                        // 日结操作权限
                        LoginParameterUtil.dailyKnotsMenuRight = convertCollection(dailyKnotsMenuRight.getJSONObject(0));
                        // 其它操作权限
                        LoginParameterUtil.userId = userId;
                        LoginParameterUtil.onLineId = onLineId;
                        LoginParameterUtil.imagePath = imagePath;
                        LoginParameterUtil.usingOrderGoodsModule = usingOrderGoodsModule;
                        LoginParameterUtil.possalesTile = possalesTile;
                        LoginParameterUtil.possalesParam1 = possalesParam1;
                        LoginParameterUtil.possalesParam2 = possalesParam2;
                        LoginParameterUtil.possalesParam3 = possalesParam3;
                        LoginParameterUtil.possalesParam4 = possalesParam4;
                        LoginParameterUtil.possalesParam5 = possalesParam5;
                        LoginParameterUtil.printTemplate = printTemplate;
                        LoginParameterUtil.deptId = deptId;
                        LoginParameterUtil.deptName = deptName;
                        LoginParameterUtil.isWarehouse = warehouseFlag;
                        LoginParameterUtil.hasStorage = hasStorage;
                        LoginParameterUtil.purchasePriceRight = purchasePriceRight;
                        LoginParameterUtil.subTypeRight = subTypeRight;
                        LoginParameterUtil.brandRight = brandRight;
                        LoginParameterUtil.brandSerialRight = brandSerialRight;
                        LoginParameterUtil.kindRight = kindRight;
                        LoginParameterUtil.ageRight = ageRight;
                        LoginParameterUtil.seasonRight = seasonRight;
                        LoginParameterUtil.supplierRight = supplierRight;
                        LoginParameterUtil.supplierCodeRight = supplierCodeRight;
                        LoginParameterUtil.retailSalesRight = retailSalesRight;
                        LoginParameterUtil.retailSales1Right = retailSales1Right;
                        LoginParameterUtil.retailSales2Right = retailSales2Right;
                        LoginParameterUtil.retailSales3Right = retailSales3Right;
                        LoginParameterUtil.tradePriceRight = tradePriceRight;
                        LoginParameterUtil.salesPrice1Right = salesPrice1Right;
                        LoginParameterUtil.salesPrice2Right = salesPrice2Right;
                        LoginParameterUtil.salesPrice3Right = salesPrice3Right;
                        LoginParameterUtil.purchasedDateRight = purchasedDateRight;
                        LoginParameterUtil.lastPurchasedDateRight = lastPurchasedDateRight;
                        LoginParameterUtil.purchaseAmountSumRight = purchaseAmountSumRight;
                        LoginParameterUtil.purchaseUnitPriceRight = purchaseUnitPriceRight;
                        LoginParameterUtil.purchaseReturnAmountSumRight = purchaseReturnAmountSumRight;
                        LoginParameterUtil.purchaseReturnUnitPriceRight = purchaseReturnUnitPriceRight;
                        LoginParameterUtil.salesAmountSumRight = salesAmountSumRight;
                        LoginParameterUtil.salesUnitPriceRight = salesUnitPriceRight;
                        LoginParameterUtil.salesReturnAmountSumRight = salesReturnAmountSumRight;
                        LoginParameterUtil.salesReturnUnitPriceRight = salesReturnUnitPriceRight;
                        LoginParameterUtil.salesOrderAmountSumRight = salesOrderAmountSumRight;
                        LoginParameterUtil.salesOrderUnitPriceRight = salesOrderUnitPriceRight;
                        LoginParameterUtil.stockMoveAmountSumRight = stockMoveAmountSumRight;
                        LoginParameterUtil.stockMoveUnitPriceRight = stockMoveUnitPriceRight;
                        LoginParameterUtil.stocktakingAmountSumRight = stocktakingAmountSumRight;
                        LoginParameterUtil.stockInAmountSumRight = stockInAmountSumRight;
                        LoginParameterUtil.stockInUnitPriceRight = stockInUnitPriceRight;
                        LoginParameterUtil.stockOutAmountSumRight = stockOutAmountSumRight;
                        LoginParameterUtil.stockOutUnitPriceRight = stockOutUnitPriceRight;
                        LoginParameterUtil.supplierPhoneRight = supplierPhoneRight;
                        LoginParameterUtil.unitPricePermitFlag = unitPricePermitFlag;
                        LoginParameterUtil.discountRatePermitFlag2 = discountRatePermitFlag2;
                        LoginParameterUtil.queryStockTotal = queryStockTotal;
                        LoginParameterUtil.relationMovein = relationMovein;
                        LoginParameterUtil.useBrandPower = useBrandPower;
                        LoginParameterUtil.discountRatePermitFlag = discountRatePermitFlag;
                        LoginParameterUtil.discountRate = discountRate;
                        LoginParameterUtil.showGiftMenuFlag = showGiftMenuFlag;
                        LoginParameterUtil.customer.setUserName(userName);
                        LoginParameterUtil.customer.setUserId(userId);
                        // 跳转至主界面
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, retObj.getString("msg"), Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(LoginActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 集合转换
     * 
     * @param jsonObject
     * @return
     * @throws JSONException
     */
    private Map<String, Boolean> convertCollection(JSONObject jsonObject) throws JSONException {
        Map<String, Boolean> map = new HashMap<String, Boolean>();
        map.put("BrowseRight", jsonObject.getBoolean("BrowseRight"));
        map.put("AddRight", jsonObject.getBoolean("AddRight"));
        map.put("ModifyRight", jsonObject.getBoolean("ModifyRight"));
        map.put("AuditRight", jsonObject.getBoolean("AuditRight"));
        return map;
    }

    /**
     * 加载系统设置中的参数
     */
    private void loadSysParam() {
        Paramer pShowEmpAndMemo = paramerDao.find("useGoodsboxBarcodeInStocktaking");
        if (null != pShowEmpAndMemo) {
            LoginParameterUtil.useGoodsboxBarcodeInStocktaking = Boolean.parseBoolean(pShowEmpAndMemo.getValue());
        }
        Paramer pUseSupplierCodeToAreaProtection = paramerDao.find("useSupplierCodeToAreaProtection");
        if (null != pUseSupplierCodeToAreaProtection) {
            LoginParameterUtil.useSupplierCodeToAreaProtection = Boolean.parseBoolean(pUseSupplierCodeToAreaProtection.getValue());
        }
        Paramer pCoverDoc = paramerDao.find("coverDoc");
        if (null != pCoverDoc) {
            LoginParameterUtil.updateToDoc = Boolean.parseBoolean(pCoverDoc.getValue());
        }
        Paramer pNotAllow = paramerDao.find("notAllow");
        if (null != pNotAllow) {
            LoginParameterUtil.notAllow = Boolean.parseBoolean(pNotAllow.getValue());
        }
        Paramer pUseDefSupplier = paramerDao.find("useDefSupplier");
        if (null != pUseDefSupplier) {
            LoginParameterUtil.useDefSupplier = Boolean.parseBoolean(pUseDefSupplier.getValue());
        }
        Paramer pBarcodeWarningTone = paramerDao.find("barcodeWarningTone");
        if (null != pBarcodeWarningTone) {
            LoginParameterUtil.barcodeWarningTone = Boolean.parseBoolean(pBarcodeWarningTone.getValue());
        }
        Paramer pShowPriceInPurchase = paramerDao.find("useAreaProtection");
        if (null != pShowPriceInPurchase) {
            LoginParameterUtil.useAreaProtection = Boolean.parseBoolean(pShowPriceInPurchase.getValue());
        }
        Paramer pUseSingleDiscount = paramerDao.find("useSingleDiscount");
        if (null != pUseSingleDiscount) {
            LoginParameterUtil.useSingleDiscount = Boolean.parseBoolean(pUseSingleDiscount.getValue());
        }
        Paramer pDisplayInventory = paramerDao.find("displayInventory");
        if (null != pDisplayInventory) {
            LoginParameterUtil.displayInventory = Boolean.parseBoolean(pDisplayInventory.getValue());
        }
        Paramer pPreciseToQueryStock = paramerDao.find("preciseToQueryStock");
        if (null != pPreciseToQueryStock) {
            LoginParameterUtil.preciseToQueryStock = Boolean.parseBoolean(pPreciseToQueryStock.getValue());
        }
        // ////////////////////////////////////////////////////////
        Paramer pSalesOrderEmployee = paramerDao.find("salesOrderEmployee");
        if (null != pSalesOrderEmployee) {
            LoginParameterUtil.salesOrderEmployee = Boolean.parseBoolean(pSalesOrderEmployee.getValue());
        }
        Paramer pSalesOrderCustomer = paramerDao.find("salesOrderCustomer");
        if (null != pSalesOrderCustomer) {
            LoginParameterUtil.salesOrderCustomer = Boolean.parseBoolean(pSalesOrderCustomer.getValue());
        }
        Paramer pSalesOrderBrand = paramerDao.find("salesOrderBrand");
        if (null != pSalesOrderBrand) {
            LoginParameterUtil.salesOrderBrand = Boolean.parseBoolean(pSalesOrderBrand.getValue());
        }
        Paramer pSalesOrderMemo = paramerDao.find("salesOrderMemo");
        if (null != pSalesOrderMemo) {
            LoginParameterUtil.salesOrderMemo = Boolean.parseBoolean(pSalesOrderMemo.getValue());
        }
        Paramer pSalesOrderDepartment = paramerDao.find("salesOrderDepartment");
        if (null != pSalesOrderDepartment) {
            LoginParameterUtil.salesOrderDepartment = Boolean.parseBoolean(pSalesOrderDepartment.getValue());
        }
        Paramer pSalesOrderWarehouse = paramerDao.find("salesOrderWarehouse");
        if (null != pSalesOrderWarehouse) {
            LoginParameterUtil.salesOrderWarehouse = Boolean.parseBoolean(pSalesOrderWarehouse.getValue());
        }
        Paramer pSalesOrderAmountSum = paramerDao.find("salesOrderAmountSum");
        if (null != pSalesOrderAmountSum) {
            LoginParameterUtil.salesOrderAmountSum = Boolean.parseBoolean(pSalesOrderAmountSum.getValue());
        }
        Paramer pSalesEmployee = paramerDao.find("salesEmployee");
        if (null != pSalesEmployee) {
            LoginParameterUtil.salesEmployee = Boolean.parseBoolean(pSalesEmployee.getValue());
        }
        Paramer pSalesCustomer = paramerDao.find("salesCustomer");
        if (null != pSalesCustomer) {
            LoginParameterUtil.salesCustomer = Boolean.parseBoolean(pSalesCustomer.getValue());
        }
        Paramer pSalesBrand = paramerDao.find("salesBrand");
        if (null != pSalesBrand) {
            LoginParameterUtil.salesBrand = Boolean.parseBoolean(pSalesBrand.getValue());
        }
        Paramer pSalesMemo = paramerDao.find("salesMemo");
        if (null != pSalesMemo) {
            LoginParameterUtil.salesMemo = Boolean.parseBoolean(pSalesMemo.getValue());
        }
        Paramer pSalesOrderNo = paramerDao.find("salesOrderNo");
        if (null != pSalesOrderNo) {
            LoginParameterUtil.salesOrderNo = Boolean.parseBoolean(pSalesOrderNo.getValue());
        }
        Paramer pSalesWarehouse = paramerDao.find("salesWarehouse");
        if (null != pSalesWarehouse) {
            LoginParameterUtil.salesWarehouse = Boolean.parseBoolean(pSalesWarehouse.getValue());
        }
        Paramer pSalesAmountSum = paramerDao.find("salesAmountSum");
        if (null != pSalesAmountSum) {
            LoginParameterUtil.salesAmountSum = Boolean.parseBoolean(pSalesAmountSum.getValue());
        }
        Paramer pSalesReturnEmployee = paramerDao.find("salesReturnEmployee");
        if (null != pSalesReturnEmployee) {
            LoginParameterUtil.salesReturnEmployee = Boolean.parseBoolean(pSalesReturnEmployee.getValue());
        }
        Paramer pSalesReturnCustomer = paramerDao.find("salesReturnCustomer");
        if (null != pSalesReturnCustomer) {
            LoginParameterUtil.salesReturnCustomer = Boolean.parseBoolean(pSalesReturnCustomer.getValue());
        }
        Paramer pSalesReturnBrand = paramerDao.find("salesReturnBrand");
        if (null != pSalesReturnBrand) {
            LoginParameterUtil.salesReturnBrand = Boolean.parseBoolean(pSalesReturnBrand.getValue());
        }
        Paramer pSalesReturnMemo = paramerDao.find("salesReturnMemo");
        if (null != pSalesReturnMemo) {
            LoginParameterUtil.salesReturnMemo = Boolean.parseBoolean(pSalesReturnMemo.getValue());
        }
        Paramer pSalesReturnWarehouse = paramerDao.find("salesReturnWarehouse");
        if (null != pSalesReturnWarehouse) {
            LoginParameterUtil.salesReturnWarehouse = Boolean.parseBoolean(pSalesReturnWarehouse.getValue());
        }
        Paramer pSalesReturnDepartment = paramerDao.find("salesReturnDepartment");
        if (null != pSalesReturnDepartment) {
            LoginParameterUtil.salesReturnDepartment = Boolean.parseBoolean(pSalesReturnDepartment.getValue());
        }
        Paramer pSalesReturnAmountSum = paramerDao.find("salesReturnAmountSum");
        if (null != pSalesReturnAmountSum) {
            LoginParameterUtil.salesReturnAmountSum = Boolean.parseBoolean(pSalesReturnAmountSum.getValue());
        }
        Paramer pPurchaseEmployee = paramerDao.find("purchaseEmployee");
        if (null != pPurchaseEmployee) {
            LoginParameterUtil.purchaseEmployee = Boolean.parseBoolean(pPurchaseEmployee.getValue());
        }
        Paramer pPurchaseSupplier = paramerDao.find("purchaseSupplier");
        if (null != pPurchaseSupplier) {
            LoginParameterUtil.purchaseSupplier = Boolean.parseBoolean(pPurchaseSupplier.getValue());
        }
        Paramer pPurchaseBrand = paramerDao.find("purchaseBrand");
        if (null != pPurchaseBrand) {
            LoginParameterUtil.purchaseBrand = Boolean.parseBoolean(pPurchaseBrand.getValue());
        }
        Paramer pPurchaseMemo = paramerDao.find("purchaseMemo");
        if (null != pPurchaseMemo) {
            LoginParameterUtil.purchaseMemo = Boolean.parseBoolean(pPurchaseMemo.getValue());
        }
        Paramer pPurchaseDepartment = paramerDao.find("purchaseDepartment");
        if (null != pPurchaseDepartment) {
            LoginParameterUtil.purchaseDepartment = Boolean.parseBoolean(pPurchaseDepartment.getValue());
        }
        Paramer pPurchaseAmountSum = paramerDao.find("purchaseAmountSum");
        if (null != pPurchaseAmountSum) {
            LoginParameterUtil.purchaseAmountSum = Boolean.parseBoolean(pPurchaseAmountSum.getValue());
        }
        Paramer pPurchaseReturnEmployee = paramerDao.find("purchaseReturnEmployee");
        if (null != pPurchaseReturnEmployee) {
            LoginParameterUtil.purchaseReturnEmployee = Boolean.parseBoolean(pPurchaseReturnEmployee.getValue());
        }
        Paramer pPurchaseReturnSupplier = paramerDao.find("purchaseReturnSupplier");
        if (null != pPurchaseReturnSupplier) {
            LoginParameterUtil.purchaseReturnSupplier = Boolean.parseBoolean(pPurchaseReturnSupplier.getValue());
        }
        Paramer pPurchaseReturnBrand = paramerDao.find("purchaseReturnBrand");
        if (null != pPurchaseReturnBrand) {
            LoginParameterUtil.purchaseReturnBrand = Boolean.parseBoolean(pPurchaseReturnBrand.getValue());
        }
        Paramer pPurchaseReturnMemo = paramerDao.find("purchaseReturnMemo");
        if (null != pPurchaseReturnMemo) {
            LoginParameterUtil.purchaseReturnMemo = Boolean.parseBoolean(pPurchaseReturnMemo.getValue());
        }
        Paramer pPurchaseReturnDepartment = paramerDao.find("purchaseReturnDepartment");
        if (null != pPurchaseReturnDepartment) {
            LoginParameterUtil.purchaseReturnDepartment = Boolean.parseBoolean(pPurchaseReturnDepartment.getValue());
        }
        Paramer pPurchaseReturnAmountSum = paramerDao.find("purchaseReturnAmountSum");
        if (null != pPurchaseReturnAmountSum) {
            LoginParameterUtil.purchaseReturnAmountSum = Boolean.parseBoolean(pPurchaseReturnAmountSum.getValue());
        }
        Paramer pStockMoveEmployee = paramerDao.find("stockMoveEmployee");
        if (null != pStockMoveEmployee) {
            LoginParameterUtil.stockMoveEmployee = Boolean.parseBoolean(pStockMoveEmployee.getValue());
        }
        Paramer pStockMoveMemo = paramerDao.find("stockMoveMemo");
        if (null != pStockMoveMemo) {
            LoginParameterUtil.stockMoveMemo = Boolean.parseBoolean(pStockMoveMemo.getValue());
        }
        Paramer pStockMoveWarehouseIn = paramerDao.find("stockMoveWarehouseIn");
        if (null != pStockMoveWarehouseIn) {
            LoginParameterUtil.stockMoveWarehouseIn = Boolean.parseBoolean(pStockMoveWarehouseIn.getValue());
        }
        Paramer pStockMoveWarehouseOut = paramerDao.find("stockMoveWarehouseOut");
        if (null != pStockMoveWarehouseOut) {
            LoginParameterUtil.stockMoveWarehouseOut = Boolean.parseBoolean(pStockMoveWarehouseOut.getValue());
        }
        Paramer pStockMoveBrand = paramerDao.find("stockMoveBrand");
        if (null != pStockMoveBrand) {
            LoginParameterUtil.stockMoveBrand = Boolean.parseBoolean(pStockMoveBrand.getValue());
        }
        Paramer pStockMoveAmountSum = paramerDao.find("stockMoveAmountSum");
        if (null != pStockMoveAmountSum) {
            LoginParameterUtil.stockMoveAmountSum = Boolean.parseBoolean(pStockMoveAmountSum.getValue());
        }
        Paramer pStockInEmployee = paramerDao.find("stockInEmployee");
        if (null != pStockInEmployee) {
            LoginParameterUtil.stockInEmployee = Boolean.parseBoolean(pStockInEmployee.getValue());
        }
        Paramer pStockInMemo = paramerDao.find("stockInMemo");
        if (null != pStockInMemo) {
            LoginParameterUtil.stockInMemo = Boolean.parseBoolean(pStockInMemo.getValue());
        }
        Paramer pStockInWarehouse = paramerDao.find("stockInWarehouse");
        if (null != pStockInWarehouse) {
            LoginParameterUtil.stockInWarehouse = Boolean.parseBoolean(pStockInWarehouse.getValue());
        }
        Paramer pStockInWarehouseOut = paramerDao.find("stockInWarehouseOut");
        if (null != pStockInWarehouseOut) {
            LoginParameterUtil.stockInWarehouseOut = Boolean.parseBoolean(pStockInWarehouseOut.getValue());
        }
        Paramer pStockInBrand = paramerDao.find("stockInBrand");
        if (null != pStockInBrand) {
            LoginParameterUtil.stockInBrand = Boolean.parseBoolean(pStockInBrand.getValue());
        }
        Paramer pStockInRelationNo = paramerDao.find("stockInRelationNo");
        if (null != pStockInRelationNo) {
            LoginParameterUtil.stockInRelationNo = Boolean.parseBoolean(pStockInRelationNo.getValue());
        }
        Paramer pStockInAmountSum = paramerDao.find("stockInAmountSum");
        if (null != pStockInAmountSum) {
            LoginParameterUtil.stockInAmountSum = Boolean.parseBoolean(pStockInAmountSum.getValue());
        }
        Paramer pStockOutEmployee = paramerDao.find("stockOutEmployee");
        if (null != pStockOutEmployee) {
            LoginParameterUtil.stockOutEmployee = Boolean.parseBoolean(pStockOutEmployee.getValue());
        }
        Paramer pStockOutMemo = paramerDao.find("stockOutMemo");
        if (null != pStockOutMemo) {
            LoginParameterUtil.stockOutMemo = Boolean.parseBoolean(pStockOutMemo.getValue());
        }
        Paramer pStockOutWarehouse = paramerDao.find("stockOutWarehouse");
        if (null != pStockOutWarehouse) {
            LoginParameterUtil.stockOutWarehouse = Boolean.parseBoolean(pStockOutWarehouse.getValue());
        }
        Paramer pStockOutWarehouseIn = paramerDao.find("stockOutWarehouseIn");
        if (null != pStockOutWarehouseIn) {
            LoginParameterUtil.stockOutWarehouseIn = Boolean.parseBoolean(pStockOutWarehouseIn.getValue());
        }
        Paramer pStockOutBrand = paramerDao.find("stockOutBrand");
        if (null != pStockOutBrand) {
            LoginParameterUtil.stockOutBrand = Boolean.parseBoolean(pStockOutBrand.getValue());
        }
        Paramer pStockOutRelationNo = paramerDao.find("stockOutRelationNo");
        if (null != pStockOutRelationNo) {
            LoginParameterUtil.stockOutRelationNo = Boolean.parseBoolean(pStockOutRelationNo.getValue());
        }
        Paramer pStockOutAmountSum = paramerDao.find("stockOutAmountSum");
        if (null != pStockOutAmountSum) {
            LoginParameterUtil.stockOutAmountSum = Boolean.parseBoolean(pStockOutAmountSum.getValue());
        }
        Paramer pStocktakingEmployee = paramerDao.find("stocktakingEmployee");
        if (null != pStocktakingEmployee) {
            LoginParameterUtil.stocktakingEmployee = Boolean.parseBoolean(pStocktakingEmployee.getValue());
        }
        Paramer pStocktakingMemo = paramerDao.find("stocktakingMemo");
        if (null != pStocktakingMemo) {
            LoginParameterUtil.stocktakingMemo = Boolean.parseBoolean(pStocktakingMemo.getValue());
        }
        Paramer pStocktakingWarehouse = paramerDao.find("stocktakingWarehouse");
        if (null != pStocktakingWarehouse) {
            LoginParameterUtil.stocktakingWarehouse = Boolean.parseBoolean(pStocktakingWarehouse.getValue());
        }
        Paramer pStocktakingBrand = paramerDao.find("stocktakingBrand");
        if (null != pStocktakingBrand) {
            LoginParameterUtil.stocktakingBrand = Boolean.parseBoolean(pStocktakingBrand.getValue());
        }
        Paramer pStocktakingAmountSum = paramerDao.find("stocktakingAmountSum");
        if (null != pStocktakingAmountSum) {
            LoginParameterUtil.stocktakingAmountSum = Boolean.parseBoolean(pStocktakingAmountSum.getValue());
        }
        // 仓位设置
        Paramer pAutoSave = paramerDao.find("autoSave");
        if (null != pAutoSave) {
            LoginParameterUtil.autoSave = Boolean.parseBoolean(pAutoSave.getValue());
        }
        Paramer pUseLastTimePosition = paramerDao.find("useLastTimePosition");
        if (null != pUseLastTimePosition) {
            LoginParameterUtil.useLastTimePosition = Boolean.parseBoolean(pUseLastTimePosition.getValue());
        }
        Paramer pStorageOutType = paramerDao.find("storageOutType");
        if (null != pStorageOutType) {
            LoginParameterUtil.storageOutType = Integer.parseInt(pStorageOutType.getValue());
        }
        // 装箱单
        Paramer pPackingBoxRelationType = paramerDao.find("packingBoxRelationType");
        if (null != pPackingBoxRelationType) {
            LoginParameterUtil.packingBoxRelationType = Integer.parseInt(pPackingBoxRelationType.getValue());
        }
        // 多货号颜色尺码录入
        Paramer pGoodsMultiSelectType = paramerDao.find("goodsMultiSelectType");
        if (null != pGoodsMultiSelectType) {
            LoginParameterUtil.goodsMultiSelectType = Integer.parseInt(pGoodsMultiSelectType.getValue());
        }
        // 销售订单优先使用货号录入
        Paramer pFirstInputOfGoodsCode = paramerDao.find("firstInputOfGoodsCode");
        if (null != pFirstInputOfGoodsCode) {
            LoginParameterUtil.firstInputOfGoodsCode = Boolean.parseBoolean(pFirstInputOfGoodsCode.getValue());
        }
        // 单据是否取消负库存检查
        Paramer pNotUseNegativeInventoryCheck = paramerDao.find("notUseNegativeInventoryCheck");
        if (null != pNotUseNegativeInventoryCheck) {
            LoginParameterUtil.notUseNegativeInventoryCheck = Boolean.parseBoolean(pNotUseNegativeInventoryCheck.getValue());
        }
        // 条码打印是否显示货品编码
        Paramer pShowGoodsCode = paramerDao.find("showGoodsCode");
        if (null != pShowGoodsCode) {
            LoginParameterUtil.showGoodsCode = Boolean.parseBoolean(pShowGoodsCode.getValue());
        }
        // 条码打印是否显示货品名称
        Paramer pShowGoodsName = paramerDao.find("showGoodsName");
        if (null != pShowGoodsName) {
            LoginParameterUtil.showGoodsName = Boolean.parseBoolean(pShowGoodsName.getValue());
        }
        // 条码打印是否显示颜色
        Paramer pShowColor = paramerDao.find("showColor");
        if (null != pShowColor) {
            LoginParameterUtil.showColor = Boolean.parseBoolean(pShowColor.getValue());
        }
        // 条码打印是否显示尺码
        Paramer pShowSize = paramerDao.find("showSize");
        if (null != pShowSize) {
            LoginParameterUtil.showSize = Boolean.parseBoolean(pShowSize.getValue());
        }
        // 条码打印是否显示货品零售价
        Paramer pShowRetailSales = paramerDao.find("showRetailSales");
        if (null != pShowRetailSales) {
            LoginParameterUtil.showRetailSales = Boolean.parseBoolean(pShowRetailSales.getValue());
        }
        // 获取条码打印宽度
        Paramer pPrintBarcodeWidth = paramerDao.find("printBarcodeWidth");
        if (null != pPrintBarcodeWidth) {
            LoginParameterUtil.customer.setPrintBarcodeWidth(pPrintBarcodeWidth.getValue());
        }
        // 获取条码打印高度
        Paramer pPrintBarcodeHeight = paramerDao.find("printBarcodeHeight");
        if (null != pPrintBarcodeHeight) {
            LoginParameterUtil.customer.setPrintBarcodeHeight(pPrintBarcodeHeight.getValue());
        }
        // 获取条码打印类型
        Paramer pPrintBarcodeType = paramerDao.find("printBarcodeType");
        if (null != pPrintBarcodeType) {
            LoginParameterUtil.customer.setPrintBarcodeType(pPrintBarcodeType.getValue());
        }
        // 多颜色尺码录入方式
        Paramer pMultiSelectType = paramerDao.find("multiSelectType");
        if (null != pMultiSelectType) {
            LoginParameterUtil.multiSelectType = Integer.parseInt(pMultiSelectType.getValue());
        }
        // 更新记住密码参数
        if (null != pRememberPassword) {
            Paramer tpRememberPassword = paramerDao.find("rememberPassword");
            if (tpRememberPassword == null) {
                paramerDao.insert(pRememberPassword);
            } else {
                paramerDao.update(pRememberPassword);
            }
        }
    }

    /**
     * 登录时获取系统设置中的参数值
     * 
     * @throws Exception
     */
    private void getSettingsParam() throws Exception {
        String flagStr = this.getString(R.string.inner_version);
        innerVersion = Boolean.parseBoolean(flagStr);
        if (innerVersion) {
            // 内部版本
            strIp = this.getString(R.string.inner_ip);
        } else {
            // 外部版本
            // 获取显示值
            Paramer ip = paramerDao.find("ip");
            if (null == ip) {
                Properties userInfo = ParamterFileUtil.getIpInfo(this);
                if (null != userInfo) {
                    strIp = userInfo.getProperty("path");
                    paramerDao.insert(new Paramer("ip", strIp));
                }
            } else {
                strIp = ip.getValue();
            }

            // Paramer accountUserName = paramerDao.find("accountUserName");
            // Paramer accountPassword = paramerDao.find("accountPassword");
            // Paramer versionNumber = paramerDao.find("versionNumber");
            if (null == strIp || "".equals(strIp)) {
                throw new Exception("系统设置中服务器地址不能为空");
            }
            // if(null == accountUserName || "".equals(accountUserName)){
            // throw new Exception("系统设置参数为空");
            // }
            // if(null == accountPassword || "".equals(accountPassword)){
            // throw new Exception("系统设置参数为空");
            // }
            // if(null == versionNumber || "".equals(versionNumber)){
            // throw new Exception("系统设置参数为空");
            // }
        }
        // 构造Customer对象
        Customer customer = new Customer(null, userName, userPwd, strIp);
        // 存储Customer
        LoginParameterUtil.customer = customer;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_login:
                login();
                break;
            case R.id.tv_sys_settings:
                intent = new Intent(LoginActivity.this, SystemSettingsActivity.class);
                intent.putExtra("Login", true);
                startActivity(intent);
                finish();
                break;
            case R.id.remember_password:
            case R.id.tv_remember_password:
                if (rememberPassword) {
                    rememberPassword = false;
                } else {
                    rememberPassword = true;
                }
                if (null == pRememberPassword) {
                    pRememberPassword = new Paramer("rememberPassword", String.valueOf(rememberPassword));
                } else {
                    pRememberPassword.setValue(String.valueOf(rememberPassword));
                }
                cbRememberPassword.setChecked(rememberPassword);
            default:
                break;
        }

    }

    @Override
    protected void setListener() {
        btLogin.setOnClickListener(this);
        settings.setOnClickListener(this);
        etPassword.setOnKeyListener(this);
        cbRememberPassword.setOnClickListener(this);
        tvRememberPassword.setOnClickListener(this);
    }

    @Override
    protected void loadViewLayout() {
        setContentView(R.layout.activity_login);
        setTitle("系 统 登 录");
        setHeadLeftVisibility(View.INVISIBLE);
        setHeadRightVisibility(View.INVISIBLE);
    }

    @Override
    protected void processLogic() {
        // 读取登录信息
        readProduct();
        if (null != customer) {
            etUserName.setText(customer.getUserName());
            etUserName.setSelection(customer.getUserName().length());// 将光标移至文字末尾
            etPassword.setText(customer.getUserPwd());
            etPassword.setSelection(customer.getUserPwd().length());// 将光标移至文字末尾
        }
        String flagStr = this.getString(R.string.inner_version);
        innerVersion = Boolean.parseBoolean(flagStr);
        if (innerVersion) {
            // 内部版本 隐藏参数设置
            settings.setVisibility(View.INVISIBLE);
        }
        // 加载设置参数
        pRememberPassword = paramerDao.find("rememberPassword");
        if (null != pRememberPassword) {
            rememberPassword = Boolean.parseBoolean(pRememberPassword.getValue());
            cbRememberPassword.setChecked(rememberPassword);
        }
    }

    @Override
    protected void findViewById() {
        btLogin = (Button) findViewById(R.id.bt_login);
        settings = (TextView) findViewById(R.id.tv_sys_settings);
        cbRememberPassword = (CheckBox) findViewById(R.id.remember_password);
        tvRememberPassword = (TextView) findViewById(R.id.tv_remember_password);
        etUserName = (EditText) findViewById(R.id.et_user_name);
        etPassword = (EditText) findViewById(R.id.et_password);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        switch (v.getId()) {
            case R.id.et_password:
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm.isActive()) {
                        imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
                    }
                    btLogin.callOnClick();
                    return true;
                }
                return false;
            default:
                return false;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                // 退出系统
                manager.AppExit(getApplicationContext());
            }
            return true;// 消费掉后退键
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 封装用户的登录信息,并保存
     */
    public void saveProduct() {
        customer = new Customer();
        if(rememberPassword){
            customer.setUserName(userName);
            customer.setUserPwd(userPwd);
        }else{
            customer.setUserName("");
            customer.setUserPwd("");
        }
        SharedPreferences preferences = getSharedPreferences("base64", MODE_PRIVATE);
        // 创建字节输出流
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            // 创建对象输出流，并封装字节流
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            // 将对象写入字节流
            oos.writeObject(customer);
            // 将字节流编码成base64的字符窜
            String customerBase64 = new String(Base64.encodeBase64(baos.toByteArray()));
            Editor editor = preferences.edit();
            editor.putString("customer", customerBase64);
            editor.commit();
            Log.i("ok", "存储成功");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取用户的登录信息
     */
    public void readProduct() {
        SharedPreferences preferences = getSharedPreferences("base64", MODE_PRIVATE);
        String customerBase64 = preferences.getString("customer", "");
        if (customerBase64 == "") {
            // 重新登录
            return;
        }
        // 读取字节
        byte[] base64 = Base64.decodeBase64(customerBase64.getBytes());
        // 封装到字节流
        ByteArrayInputStream bais = new ByteArrayInputStream(base64);
        try {
            // 再次封装
            ObjectInputStream bis = new ObjectInputStream(bais);
            try {
                // 读取对象
                customer = (Customer) bis.readObject();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取MAC地址
     * 
     * @return
     */
    public static String getMac() {
        String macSerial = null;
        String str = "";
        try {
            Process pp = Runtime.getRuntime().exec("cat /sys/class/net/wlan0/address ");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);
            for (; null != str;) {
                str = input.readLine();
                if (str != null) {
                    macSerial = str.trim();// 去空格
                    break;
                }
            }
        } catch (IOException ex) {
            // 赋予默认值
            ex.printStackTrace();
        }
        // 返回MAC地址
        if ("02:00:00:00:00:00".equals(macSerial) || macSerial == null || "".equals(macSerial)) {
            return null;
        } else {
            return macSerial;
        }
    }

    /**
     * 通过WiFiManager获取MAC地址
     * 
     * @param context
     * @return
     */
    private static String tryGetWifiMac(Context context) {
        WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wi = wm.getConnectionInfo();
        if (wi == null || wi.getMacAddress() == null) {
            return null;
        }
        if ("02:00:00:00:00:00".equals(wi.getMacAddress().trim())) {
            return null;
        } else {
            return wi.getMacAddress().trim();
        }
    }

    /**
     * 打开设备WiFi
     */
    private void openWiFi() {
        WifiManager wifiManager = (WifiManager) ECApplication.getInstance().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
    }


}
