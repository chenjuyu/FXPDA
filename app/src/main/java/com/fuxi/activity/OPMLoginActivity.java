package com.fuxi.activity;

import java.util.HashMap;
import org.json.JSONObject;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import com.fuxi.main.R;
import com.fuxi.util.AppManager;
import com.fuxi.util.Logger;
import com.fuxi.util.LoginParameterUtil;
import com.fuxi.util.NetUtil;
import com.fuxi.util.OPMLoginParameterUtil;
import com.fuxi.vo.RequestVo;

public class OPMLoginActivity extends BaseWapperActivity implements OnCheckedChangeListener {

    private static final String TAG = "OPMLoginActivity";
    private static final String checkUserOfCustomerStyle = "/OPMManager.do?checkUserOfCustomerStyle";
    private static final String login = "/OPMManager.do?login";

    private RadioGroup loginTypeGroup;
    private RadioButton rbCustomerType;
    private TextView tvCustomerType;
    private RadioButton rbEmployeeType;
    private TextView tvEmployeeType;
    private EditText etUserName;
    private EditText etCustomerName;
    private EditText etPassword;
    private TextView tvLogin;
    private TextView tvBack;

    private String customerId; // 客户ID
    private String departmentId; // 部门ID
    private boolean isEmployeeType;

    private TouchListener tl = new TouchListener();
    private FocusChangeListener fcl = new FocusChangeListener();


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.customer_type:
            case R.id.tv_customer_type:
                onCheckedChanged(null, R.id.customer_type);
                break;
            case R.id.employee_type:
            case R.id.tv_employee_type:
                onCheckedChanged(null, R.id.employee_type);
                break;
            case R.id.back:
                finish();
                break;
            case R.id.login:
                login();
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        switch (v.getId()) {
            case R.id.password:
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm.isActive()) {
                        imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
                    }
                    tvLogin.callOnClick();
                    return true;
                }
                return false;
            default:
                return false;
        }
    }

    @Override
    protected void loadViewLayout() {
        setContentView(R.layout.activity_opm_login);
        setTitle("订 货 会 系 统 登 录");
        setHeadLeftVisibility(View.INVISIBLE);
        setHeadRightVisibility(View.INVISIBLE);
    }

    @Override
    protected void setListener() {
        loginTypeGroup.setOnCheckedChangeListener(this);
        rbCustomerType.setOnClickListener(this);
        rbEmployeeType.setOnClickListener(this);
        tvCustomerType.setOnClickListener(this);
        tvEmployeeType.setOnClickListener(this);
        tvLogin.setOnClickListener(this);
        tvBack.setOnClickListener(this);
        etPassword.setOnKeyListener(this);
        etCustomerName.setOnTouchListener(tl);
        etPassword.setOnFocusChangeListener(fcl);
    }

    @Override
    protected void processLogic() {
        if (NetUtil.hasNetwork(getApplicationContext())) {
            if (LoginParameterUtil.online) {
                // 默认选择客户模式
                rbCustomerType.setChecked(true);
                rbEmployeeType.setChecked(false);
                etCustomerName.setEnabled(false);
            } else {
                AlertDialog.Builder dialog = new AlertDialog.Builder(OPMLoginActivity.this, AlertDialog.THEME_HOLO_LIGHT);
                dialog.setTitle("系统提示");
                dialog.setMessage("当前用户未登录，操作非法！");
                // 相当于点击确认按钮
                dialog.setPositiveButton("退出", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AppManager.getAppManager().AppExit(getApplicationContext());
                    }
                });
                dialog.setCancelable(false);
                dialog.create();
                dialog.show();
            }
        } else {
            AlertDialog.Builder dialog = new AlertDialog.Builder(OPMLoginActivity.this, AlertDialog.THEME_HOLO_LIGHT);
            dialog.setTitle("系统提示");
            dialog.setMessage("当前为离线状态，此功能不可用！");
            // 相当于点击确认按钮
            dialog.setPositiveButton("返回", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            dialog.setCancelable(false);
            dialog.create();
            dialog.show();
        }
    }

    @Override
    protected void findViewById() {
        loginTypeGroup = (RadioGroup) findViewById(R.id.login_type_group);
        rbCustomerType = (RadioButton) findViewById(R.id.customer_type);
        tvCustomerType = (TextView) findViewById(R.id.tv_customer_type);
        rbEmployeeType = (RadioButton) findViewById(R.id.employee_type);
        tvEmployeeType = (TextView) findViewById(R.id.tv_employee_type);
        etUserName = (EditText) findViewById(R.id.user_name);
        etCustomerName = (EditText) findViewById(R.id.customer_name);
        etPassword = (EditText) findViewById(R.id.password);
        tvLogin = (TextView) findViewById(R.id.login);
        tvBack = (TextView) findViewById(R.id.back);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.customer_type:
                rbCustomerType.setChecked(true);
                rbEmployeeType.setChecked(false);
                etCustomerName.setEnabled(false);
                isEmployeeType = false;
                customerId = null;
                etCustomerName.setText(null);
                break;
            case R.id.employee_type:
                rbEmployeeType.setChecked(true);
                rbCustomerType.setChecked(false);
                etCustomerName.setEnabled(true);
                isEmployeeType = true;
                customerId = null;
                etCustomerName.setText(null);
                break;
            default:
                break;
        }
    }

    private class TouchListener implements OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (v.getId()) {
                case R.id.customer_name:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        String userName = etUserName.getText().toString().trim();
                        if (userName == null || "".equals(userName) || "null".equalsIgnoreCase(userName)) {
                            Toast.makeText(OPMLoginActivity.this, R.string.username_null, Toast.LENGTH_SHORT).show();
                        } else {
                            Intent intent = new Intent(OPMLoginActivity.this, SelectActivity.class);
                            intent.putExtra("selectType", "selectCustomerByUserNo");
                            intent.putExtra("param", userName);
                            startActivityForResult(intent, R.id.customer_name);
                            overridePendingTransition(R.anim.activity_open, 0);
                        }
                    }
                    break;
                default:
                    break;
            }
            return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case R.id.customer_name:
                if (resultCode == 1) {
                    etCustomerName.setText(data.getStringExtra("Name"));
                    customerId = data.getStringExtra("CustomerID");
                }
                break;
            default:
                break;
        }
    }

    private class FocusChangeListener implements OnFocusChangeListener {

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus && !isEmployeeType) {
                String userName = etUserName.getText().toString().trim();
                // 判断非空
                if (userName != null && !"".equals(userName) && !"null".equalsIgnoreCase(userName)) {
                    checkUserOfCustomerStyle(userName);
                }
            }
        }

    }

    /**
     * 登录订货会系统
     */
    private void login() {
        String userName = etUserName.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        // 判断非空
        if (userName == null || "".equals(userName) || "null".equalsIgnoreCase(userName)) {
            Toast.makeText(OPMLoginActivity.this, R.string.username_null, Toast.LENGTH_SHORT).show();
            return;
        }
        if (password == null || "".equals(password) || "null".equalsIgnoreCase(userName)) {
            Toast.makeText(OPMLoginActivity.this, R.string.psw_null, Toast.LENGTH_SHORT).show();
            return;
        }
        if (customerId == null || "".equals(customerId) || "null".equalsIgnoreCase(userName)) {
            Toast.makeText(OPMLoginActivity.this, R.string.customer_null, Toast.LENGTH_SHORT).show();
            return;
        }
        RequestVo vo = new RequestVo();
        vo.requestUrl = login;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("username", userName);
        map.put("password", password);
        map.put("customerId", customerId);
        vo.requestDataMap = map;
        super.getDataFromServer(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    if (retObj.getBoolean("success")) {
                        JSONObject jsonObject = retObj.getJSONArray("obj").getJSONObject(0);
                        departmentId = jsonObject.getString("departmentId");
                        OPMLoginParameterUtil.departmentId = departmentId;
                        OPMLoginParameterUtil.userId = jsonObject.getString("userId");
                        OPMLoginParameterUtil.userNo = jsonObject.getString("userNo");
                        OPMLoginParameterUtil.userName = jsonObject.getString("userName");
                        OPMLoginParameterUtil.customerId = customerId;
                        OPMLoginParameterUtil.customerName = etCustomerName.getText().toString();
                        OPMLoginParameterUtil.customerOrderField = jsonObject.getString("orderField");
                        Intent intent = new Intent(OPMLoginActivity.this, OPMMainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(OPMLoginActivity.this, retObj.getString("msg"), Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(OPMLoginActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 客户模式下检查客户编码是否存在并返回客户信息
     * 
     * @param userNo
     */
    private void checkUserOfCustomerStyle(String userNo) {
        RequestVo vo = new RequestVo();
        vo.requestUrl = checkUserOfCustomerStyle;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("userNo", userNo);
        vo.requestDataMap = map;
        super.getDataFromServerNoProcess(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    if (retObj.getBoolean("success")) {
                        JSONObject jsonObject = retObj.getJSONArray("obj").getJSONObject(0);
                        etCustomerName.setText(jsonObject.getString("Customer"));
                        customerId = jsonObject.getString("CustomerID");
                    } else {
                        Toast.makeText(OPMLoginActivity.this, "用户编码不存在", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(OPMLoginActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

}
