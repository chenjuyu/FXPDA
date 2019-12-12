package com.fuxi.activity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import org.json.JSONObject;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.fuxi.main.R;
import com.fuxi.util.AppManager;
import com.fuxi.util.Logger;
import com.fuxi.util.LoginParameterUtil;
import com.fuxi.util.NetUtil;
import com.fuxi.vo.RequestVo;

/**
 * Title: AddCustomerActivity Description: 新增客户活动界面
 * 
 * @author LYJ
 * 
 */
public class AddCustomerActivity extends BaseWapperActivity {

    // 静态常量
    private static final String TAG = "AddCustomerActivity";
    private final static String add_customer = "/customer.do?addCustomer";

    // 控件属性
    private EditText et_cus_code;
    private EditText et_cus_name;
    private EditText et_cus_type;
    private EditText et_department;
    private EditText et_sp_type;
    private EditText et_mobile;
    private EditText et_memo;
    private TextView tv_save;

    // 自定义属性
    private String code;
    private String name;
    private String mobile;
    private String memo;// 备注信息
    private String cusTypeId;// 客户类别ID
    private String salesPriceType;// 批发价格类型
    private String departmentId;// 批发价格类型

    /**
     * 新增客户保存方法
     */
    private void save() {
        code = et_cus_code.getText().toString().trim();
        name = et_cus_name.getText().toString().trim();
        mobile = et_mobile.getText().toString().trim();
        if (null == code || "".equals(code) || "null".equalsIgnoreCase(code)) {
            Toast.makeText(AddCustomerActivity.this, "客户编码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (null == name || "".equals(name) || "null".equalsIgnoreCase(name)) {
            Toast.makeText(AddCustomerActivity.this, "客户名称不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (null == cusTypeId || "".equals(cusTypeId)) {
            Toast.makeText(AddCustomerActivity.this, "请选择客户类别", Toast.LENGTH_SHORT).show();
            return;
        }
        if (null == departmentId || "".equals(departmentId)) {
            Toast.makeText(AddCustomerActivity.this, "请选择客户所属部门", Toast.LENGTH_SHORT).show();
            return;
        }
        if (null == salesPriceType || "".equals(salesPriceType)) {
            Toast.makeText(AddCustomerActivity.this, "请选择批发价格类型", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mobile.length() != 11) {
            Toast.makeText(AddCustomerActivity.this, "输入的手机号不合法", Toast.LENGTH_SHORT).show();
            et_mobile.requestFocus();
            et_mobile.selectAll();
            return;
        }
        // 连接服务器
        RequestVo vo = new RequestVo();
        vo.requestUrl = add_customer;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("code", code);
        map.put("name", name);
        map.put("cusTypeId", cusTypeId);
        map.put("departmentId", departmentId);
        map.put("salesPriceType", salesPriceType);
        map.put("mobile", mobile);
        map.put("memo", memo);
        vo.requestDataMap = map;
        super.getDataFromServer(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    if (retObj.getBoolean("success")) {
                        Toast.makeText(AddCustomerActivity.this, "添加成功", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(AddCustomerActivity.this, "添加失败", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(AddCustomerActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 重置新增客户界面
     */
    private void reset() {
        // 设置默认客户编码
        getCusCode();
        // 清空其它值
        et_cus_name.setText(null);
        et_cus_type.setText(null);
        et_sp_type.setText(null);
        et_mobile.setText(null);
        et_memo.setText(null);
        code = null;
        memo = null;
        cusTypeId = null;
        salesPriceType = null;
    }

    /**
     * 生成默认客户编码
     * 
     * @return
     */
    private String generateCusCode() {
        String code = null;
        SimpleDateFormat yyyymmddhhmmss = new SimpleDateFormat("yyMMddHHmmss");
        code = yyyymmddhhmmss.format(new Date());
        return code;
    }

    /**
     * 显示默认客户编码
     */
    private void getCusCode() {
        code = generateCusCode();
        et_cus_code.setText(code);
        et_cus_code.setSelection(code.length());// 将光标移至文字末尾
    }

    /**
     * 点击返回时询问是否保存当前信息
     */
    private void saveOrNot() {
        Builder dialog = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
        dialog.setTitle("保存提示");
        dialog.setMessage("客户信息尚未保存,确定要返回吗？");
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.save:
                // 保存
                save();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onHeadRightButton(View v) {
        // 重置
        reset();
    }

    @Override
    protected void onHeadLeftButton(View v) {
        name = et_cus_name.getText().toString().trim();
        mobile = et_mobile.getText().toString().trim();
        if ((!"".equals(name)) || null != cusTypeId || null != salesPriceType || (!"".equals(mobile))) {
            saveOrNot();
        } else {
            finish();
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    protected void loadViewLayout() {
        setContentView(R.layout.activity_add_customer);
        setTitle("客户资料");
    }

    @Override
    protected void processLogic() {
        if (NetUtil.hasNetwork(getApplicationContext())) {
            if (LoginParameterUtil.online) {
                // 设置默认部门
                departmentId = LoginParameterUtil.deptId;
                et_department.setText(LoginParameterUtil.deptName);
                // 设置默认客户编码
                getCusCode();
                // 右上角选项
                setHeadRightText(R.string.reset);
                setHeadRightVisibility(View.VISIBLE);
                boolean customerAddRight = LoginParameterUtil.customerRightMap.get("AddRight");
                if (!customerAddRight) {
                    Builder dialog = new AlertDialog.Builder(AddCustomerActivity.this, AlertDialog.THEME_HOLO_LIGHT);
                    dialog.setTitle("提示");
                    dialog.setMessage("当前暂无新增客户的权限");
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
            } else {
                AlertDialog.Builder dialog = new AlertDialog.Builder(AddCustomerActivity.this, AlertDialog.THEME_HOLO_LIGHT);
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
            AlertDialog.Builder dialog = new AlertDialog.Builder(AddCustomerActivity.this, AlertDialog.THEME_HOLO_LIGHT);
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
    protected void setListener() {
        TouchListener tl = new TouchListener();
        et_cus_code.setOnClickListener(this);
        et_cus_name.setOnClickListener(this);
        et_cus_type.setOnTouchListener(tl);
        et_sp_type.setOnTouchListener(tl);
        et_department.setOnTouchListener(tl);
        et_mobile.setOnClickListener(this);
        et_memo.setOnTouchListener(tl);
        tv_save.setOnClickListener(this);
    }

    private class TouchListener implements OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (v.getId()) {
                case R.id.customer_type:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Intent intent = new Intent(AddCustomerActivity.this, SelectActivity.class);
                        intent.putExtra("selectType", "customerType");
                        startActivityForResult(intent, R.id.customer_type);
                        overridePendingTransition(R.anim.activity_open, 0);
                    }
                    break;
                case R.id.sales_price_type:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Intent intent = new Intent(AddCustomerActivity.this, SelectActivity.class);
                        intent.putExtra("selectType", "salesPriceType");
                        startActivityForResult(intent, R.id.sales_price_type);
                        overridePendingTransition(R.anim.activity_open, 0);
                    }
                    break;
                case R.id.department:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Intent intent = new Intent(AddCustomerActivity.this, SelectActivity.class);
                        intent.putExtra("selectType", "selectDepartment");
                        startActivityForResult(intent, R.id.department);
                        overridePendingTransition(R.anim.activity_open, 0);
                    }
                    break;
                case R.id.memo:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Intent intent = new Intent(AddCustomerActivity.this, RemarkActivity.class);
                        intent.putExtra("memo", memo);
                        startActivityForResult(intent, R.id.memo);
                        overridePendingTransition(R.anim.activity_open, 0);
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
            case R.id.customer_type:
                if (resultCode == 1) {
                    et_cus_type.setText(data.getStringExtra("Name"));
                    cusTypeId = data.getStringExtra("CustomerTypeID");
                }
                break;
            case R.id.sales_price_type:
                if (resultCode == 1) {
                    et_sp_type.setText(data.getStringExtra("Name"));
                    salesPriceType = data.getStringExtra("Name");
                }
                break;
            case R.id.department:
                if (resultCode == 1) {
                    et_department.setText(data.getStringExtra("Name"));
                    departmentId = data.getStringExtra("DepartmentID");
                }
                break;
            case R.id.memo:
                if (resultCode == 1) {
                    memo = data.getStringExtra("remark");
                    et_memo.setText(memo);
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void findViewById() {
        et_cus_code = (EditText) findViewById(R.id.customer_code);
        et_cus_name = (EditText) findViewById(R.id.customer_name);
        et_cus_type = (EditText) findViewById(R.id.customer_type);
        et_department = (EditText) findViewById(R.id.department);
        et_sp_type = (EditText) findViewById(R.id.sales_price_type);
        et_mobile = (EditText) findViewById(R.id.mobile);
        et_memo = (EditText) findViewById(R.id.memo);
        tv_save = (TextView) findViewById(R.id.save);
    }

}
