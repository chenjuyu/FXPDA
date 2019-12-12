package com.fuxi.activity;

import java.util.Properties;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;
import com.fuxi.dao.ParamerDao;
import com.fuxi.main.R;
import com.fuxi.util.Logger;
import com.fuxi.util.ParamterFileUtil;
import com.fuxi.vo.Paramer;

/**
 * Title: SystemSettingsActivity Description: 系统设置活动界面(登录界面右下角的系统设置) 管理用户登录IP
 * 
 * @author LYJ
 * 
 */
public class SystemSettingsActivity extends BaseWapperActivity {

    private static final String TAG = "SystemSettingsActivity";

    private ParamerDao paramerDao = new ParamerDao(this);

    // 输入控件
    private EditText etIp; // 服务器地址

    // 其它属性
    private String ip;

    // private String accountUserName;
    // private String accountPassword;
    // private String versionNumber;

    private Paramer pIp;

    // private Paramer pAccountUserName;
    // private Paramer pAccountPassword;
    // private Paramer pVersionNumber;

    private static boolean flag = false;

    /**
     * 保存连接IP和其它系统参数
     */
    public void save() {
        int count = 0;
        ip = etIp.getText().toString().trim();
        // accountUserName = etAccountUserName.getText().toString();
        // accountPassword = etAccountPassword.getText().toString();
        // versionNumber = etVersionNumber.getText().toString();
        // 非空判断
        if (ip == null || "".equals(ip)) {
            Toast.makeText(SystemSettingsActivity.this, R.string.ip_null, Toast.LENGTH_SHORT).show();
            return;
        }
        // if (accountUserName == null || "".equals(accountUserName)) {
        // Toast.makeText(SystemSettingsActivity.this,
        // R.string.accountUserName_null, Toast.LENGTH_SHORT).show();
        // return;
        // }
        // if (accountPassword == null || "".equals(accountPassword)) {
        // Toast.makeText(SystemSettingsActivity.this,
        // R.string.accountPassword_null, Toast.LENGTH_SHORT).show();
        // return;
        // }
        // if (versionNumber == null || "".equals(versionNumber)) {
        // Toast.makeText(SystemSettingsActivity.this,
        // R.string.versionNumber_null, Toast.LENGTH_SHORT).show();
        // return;
        // }
        // 保存并更新系统设置参数
        pIp = new Paramer("ip", ip);
        // pAccountUserName = new Paramer("accountUserName", accountUserName);
        // pAccountPassword = new Paramer("accountPassword", accountPassword);
        // pVersionNumber = new Paramer("versionNumber", versionNumber);
        count = paramerDao.update(pIp);
        ParamterFileUtil.saveIpInfo("path", ip, this);
        if (count == 0) {
            count = paramerDao.insert(pIp);
        }
        // count = paramerDao.update(pAccountUserName);
        // if(count == 0){
        // count = paramerDao.insert(pAccountUserName);
        // }
        // count = paramerDao.update(pAccountPassword);
        // if(count == 0){
        // count = paramerDao.insert(pAccountPassword);
        // }
        // count = paramerDao.update(pVersionNumber);
        // if(count == 0){
        // count = paramerDao.insert(pVersionNumber);
        // }
        if (count > 0) {
            // 保存成功
            Toast.makeText(SystemSettingsActivity.this, R.string.save_success, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(SystemSettingsActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            // 保存失败
            Toast.makeText(SystemSettingsActivity.this, R.string.save_failed, Toast.LENGTH_SHORT).show();
            Logger.e(TAG, "系统设置保存异常");
        }
    }

    /**
     * 显示系统设置保存的数据
     */
    private void show() {
        // 获取显示值
        pIp = paramerDao.find("ip");
        // 非空判断并绑定显示值
        if (null != pIp) {
            etIp.setText(pIp.getValue());
        } else {
            Properties userInfo = ParamterFileUtil.getIpInfo(this);
            if (null != userInfo) {
                etIp.setText(userInfo.getProperty("path"));
            }
        }
    }

    @Override
    public void onClick(View v) {}

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        switch (v.getId()) {
            case R.id.et_ip:
                // Enter键点击触发保存
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm.isActive()) {
                        imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
                    }
                    save();
                    return true;
                }
                return false;
            default:
                return false;
        }
    }

    @Override
    protected void loadViewLayout() {
        setContentView(R.layout.activity_system_settings);
        setTitle("登 录 设 置");
        setHeadLeftVisibility(View.VISIBLE);
    }

    @Override
    protected void setListener() {
        // etVersionNumber.setOnKeyListener(this);
    }

    @Override
    protected void processLogic() {
        Bundle bundle = this.getIntent().getExtras();
        if (null != bundle) {
            flag = bundle.getBoolean("Login");
        }
        show();
        // 设置右上角图标
        setHeadRightVisibility(View.VISIBLE);
        setHeadRightText(R.string.complete);
    }

    @Override
    protected void onHeadRightButton(View v) {
        save();
    }

    /**
     * 点击返回时的操作
     */
    private void back() {
        if (flag) {
            // 上一个界面是登录界面
            Intent intent = new Intent(SystemSettingsActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            finish();
        }
    }

    @Override
    protected void onHeadLeftButton(View v) {
        back();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            back();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void findViewById() {
        etIp = (EditText) findViewById(R.id.et_ip);
        // etAccountUserName = (EditText)
        // findViewById(R.id.et_account_user_name);
        // etAccountPassword = (EditText)
        // findViewById(R.id.et_account_password);
        // etVersionNumber = (EditText) findViewById(R.id.et_version_number);
    }
}
