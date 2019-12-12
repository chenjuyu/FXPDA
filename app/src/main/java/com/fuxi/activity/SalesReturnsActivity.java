package com.fuxi.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.fuxi.adspter.SalesAdspter;
import com.fuxi.main.R;
import com.fuxi.util.AppManager;
import com.fuxi.util.Logger;
import com.fuxi.util.LoginParameterUtil;
import com.fuxi.util.NetUtil;
import com.fuxi.util.Tools;
import com.fuxi.vo.RequestVo;
import com.fuxi.widget.CheckBoxTextView;
import com.fuxi.widget.FontTextView;
import com.fuxi.widget.OnRefreshListener;
import com.fuxi.widget.RefreshListView;

/**
 * Title: SalesReturnsActivity Description: 销售退货单活动界面
 * 
 * @author LYJ
 * 
 */
public class SalesReturnsActivity extends BaseWapperActivity implements OnRefreshListener {

    private static final String TAG = "SalesActivity";
    private static final String url = "/sales.do?salesReturns";

    private List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
    private SharedPreferences sp;

    private RefreshListView lvDatas; // ListView
    private CheckBoxTextView tvShowAll;
    private CheckBoxTextView tvShowAudit;
    private CheckBoxTextView tvShowUnAudit;
    private EditText etNo; // 单号
    private FontTextView ftvScreenIcon; // 筛选按钮
    // 条件筛选控件(全局控件)
    private EditText et_begin_date;
    private EditText et_end_date;
    private EditText et_department;
    private EditText et_customer;
    private EditText et_employee;
    private TextView tv_reset;
    private TextView tv_search;

    private AlertDialog alertDialog;
    private SalesAdspter adapter;
    private TouchListener tl = new TouchListener();

    private String audit = null;// 是否审核的标志
    private boolean refresh;
    private int type;
    private int currPage = 1;
    // 条件筛选变量(全局变量)
    private String selectDate;
    private String beginDate;
    private String endDate;
    private String departmentId;
    private String customerId;
    private String employeeId;
    private String departmentName;
    private String customerName;
    private String employeeName;
    private int viewId;

    /**
     * 点击新增单据
     */
    private void addItem() {
        Intent intent = new Intent(SalesReturnsActivity.this, SalesReturnsDetailActivity.class);
        startActivity(intent);
    }

    /**
     * 获取销售发货单列表
     * 
     * @param refresh
     * @param type
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void getData(boolean refresh, int type) {
        RequestVo vo = new RequestVo();
        vo.requestUrl = url;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("currPage", String.valueOf(currPage));
        map.put("audit", audit);
        map.put("no", etNo.getText().toString());
        map.put("beginDate", beginDate);
        map.put("endDate", endDate);
        map.put("departmentId", departmentId);
        map.put("customerId", customerId);
        map.put("employeeId", employeeId);
        vo.requestDataMap = map;
        this.refresh = refresh;
        this.type = type;
        super.getDataFromServerNoProcess(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    if (retObj.getBoolean("success")) {
                        if (SalesReturnsActivity.this.refresh) {
                            dataList.clear();
                        }
                        JSONArray array = retObj.getJSONArray("obj");
                        for (int i = 0; i < array.length(); i++) {
                            Map temp = new HashMap();
                            JSONObject json = array.getJSONObject(i);
                            Iterator ite = json.keys();
                            while (ite.hasNext()) {
                                String key = ite.next().toString();
                                String value = json.getString(key);
                                temp.put(key, value);
                            }
                            dataList.add(temp);
                        }
                        if (SalesReturnsActivity.this.type == 1) {
                            lvDatas.hideFooterView();
                            if (array.length() == 0) {
                                Toast.makeText(SalesReturnsActivity.this, "已经到达最后一页", Toast.LENGTH_SHORT).show();
                            }
                        } else if (SalesReturnsActivity.this.type == 2) {
                            lvDatas.hideHeaderView();
                        }
                        adapter.refresh(dataList);

                    } else {

                    }
                } catch (Exception e) {
                    Toast.makeText(SalesReturnsActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        currPage = 1;
        switch (v.getId()) {
            case R.id.show_all:
                tvShowAll.setChecked(true);
                tvShowAudit.setChecked(false);
                tvShowUnAudit.setChecked(false);
                // 设置参数
                audit = null;
                // 获取数据
                getData(true, type);
                break;
            case R.id.show_audit:
                tvShowAll.setChecked(false);
                tvShowAudit.setChecked(true);
                tvShowUnAudit.setChecked(false);
                // 设置参数
                audit = "1";
                // 获取数据
                getData(true, type);
                break;
            case R.id.show_unaudit:
                tvShowAll.setChecked(false);
                tvShowAudit.setChecked(false);
                tvShowUnAudit.setChecked(true);
                // 设置参数
                audit = "0";
                // 获取数据
                getData(true, type);
                break;
            case R.id.screenIcon:
                etNo.setText(null);
                View view = (LinearLayout) getLayoutInflater().inflate(R.layout.list_doc_screen, null);
                // 筛选选项(初始化控件)
                et_begin_date = (EditText) view.findViewById(R.id.begin_date);
                et_end_date = (EditText) view.findViewById(R.id.end_date);
                et_department = (EditText) view.findViewById(R.id.department);
                et_customer = (EditText) view.findViewById(R.id.customer);
                et_employee = (EditText) view.findViewById(R.id.employee);
                tv_reset = (TextView) view.findViewById(R.id.reset);
                tv_search = (TextView) view.findViewById(R.id.search);
                // 筛选选项(绑定事件)
                et_begin_date.setOnTouchListener(tl);
                et_end_date.setOnTouchListener(tl);
                et_department.setOnTouchListener(tl);
                et_customer.setOnTouchListener(tl);
                et_employee.setOnTouchListener(tl);
                tv_reset.setOnClickListener(this);
                tv_search.setOnClickListener(this);
                // 获取显示值
                et_begin_date.setText(beginDate);
                et_end_date.setText(endDate);
                et_department.setText(departmentName);
                et_customer.setText(customerName);
                et_employee.setText(employeeName);
                // 显示界面
                Builder dialog = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
                dialog.setTitle("添加筛选条件");
                dialog.setView(view);
                dialog.setCancelable(false);
                dialog.create();
                alertDialog = dialog.show();
                break;
            case R.id.reset:
                reset();
                break;
            case R.id.search:
                if ((beginDate != null && endDate == null) || (beginDate == null && endDate != null)) {
                    Toast.makeText(SalesReturnsActivity.this, "日期范围不完整", Toast.LENGTH_SHORT).show();
                } else {
                    alertDialog.dismiss();
                    // 获取数据
                    getData(true, type);
                }
                break;
            default:
                break;
        }
    }

    /**
     * 重置筛选条件
     */
    private void reset() {
        if (et_begin_date != null && et_end_date != null && et_department != null && et_customer != null && et_employee != null) {
            et_begin_date.setText(null);
            et_end_date.setText(null);
            et_department.setText(null);
            et_customer.setText(null);
            et_employee.setText(null);
            beginDate = null;
            endDate = null;
            departmentId = null;
            customerId = null;
            employeeId = null;
            departmentName = null;
            customerName = null;
            employeeName = null;
        }
    }

    /**
     * DatePickerDialog的选择监听事件
     */
    private DatePickerDialog.OnDateSetListener dateListener = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {
            selectDate = year + "-" + (month + 1) + "-" + day;
            setDate(viewId);
        }

    };

    /**
     * 动态显示日期的值
     * 
     * @param viewId
     */
    private void setDate(int viewId) {
        switch (viewId) {
            case R.id.begin_date:
                beginDate = selectDate;
                et_begin_date.setText(selectDate);
                break;
            case R.id.end_date:
                endDate = selectDate;
                et_end_date.setText(selectDate);
                break;
            default:
                break;
        }
    }

    private class TouchListener implements OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (v.getId()) {
                case R.id.no:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        reset();
                        Intent intent = new Intent(SalesReturnsActivity.this, SelectActivity.class);
                        intent.putExtra("selectType", "selectSalesReturnNo");
                        startActivityForResult(intent, R.id.no);
                        overridePendingTransition(R.anim.activity_open, 0);
                    }
                    break;
                case R.id.department:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Intent intent = new Intent(SalesReturnsActivity.this, SelectActivity.class);
                        intent.putExtra("selectType", "selectWarehouse");
                        startActivityForResult(intent, R.id.department);
                        overridePendingTransition(R.anim.activity_open, 0);
                    }
                    break;
                case R.id.customer:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Intent intent = new Intent(SalesReturnsActivity.this, SelectActivity.class);
                        intent.putExtra("selectType", "selectCustomer");
                        startActivityForResult(intent, R.id.customer);
                        overridePendingTransition(R.anim.activity_open, 0);
                    }
                    break;
                case R.id.employee:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Intent intent = new Intent(SalesReturnsActivity.this, SelectActivity.class);
                        intent.putExtra("selectType", "selectEmployee");
                        startActivityForResult(intent, R.id.employee);
                        overridePendingTransition(R.anim.activity_open, 0);
                    }
                    break;
                case R.id.begin_date:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        viewId = R.id.begin_date;
                        DatePickerDialog pickerDialog = new DatePickerDialog(SalesReturnsActivity.this, AlertDialog.THEME_HOLO_LIGHT, dateListener, Tools.year, Tools.month, Tools.day);
                        pickerDialog.setCancelable(false);
                        pickerDialog.show();
                    }
                    break;
                case R.id.end_date:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        viewId = R.id.end_date;
                        DatePickerDialog pickerDialog = new DatePickerDialog(SalesReturnsActivity.this, AlertDialog.THEME_HOLO_LIGHT, dateListener, Tools.year, Tools.month, Tools.day);
                        pickerDialog.setCancelable(false);
                        pickerDialog.show();
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
            case R.id.no:
                if (resultCode == 1) {
                    etNo.setText(data.getStringExtra("Name"));
                    // 加载数据
                    getData(true, 0);
                }
                break;
            case R.id.department:
                if (resultCode == 1) {
                    departmentName = data.getStringExtra("Name");
                    et_department.setText(departmentName);
                    departmentId = data.getStringExtra("DepartmentID");
                }
                break;
            case R.id.customer:
                if (resultCode == 1) {
                    customerName = data.getStringExtra("Name");
                    et_customer.setText(customerName);
                    customerId = data.getStringExtra("CustomerID");
                }
                break;
            case R.id.employee:
                if (resultCode == 1) {
                    employeeName = data.getStringExtra("Name");
                    et_employee.setText(employeeName);
                    employeeId = data.getStringExtra("EmployeeID");
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onHeadRightButton(View v) {
        // 获取用户操作权限(新增)
        boolean addRight = LoginParameterUtil.salesReturnRightMap.get("AddRight");
        if (addRight) {
            // 新增销售发货单(散件)
            addItem();
        } else {
            Builder dialog = new AlertDialog.Builder(SalesReturnsActivity.this, AlertDialog.THEME_HOLO_LIGHT);
            dialog.setTitle("提示");
            dialog.setMessage("当前暂无新增销售退货单的操作权限");
            // 相当于点击确认按钮
            dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {}
            });
            dialog.setCancelable(false);
            dialog.create();
            dialog.show();
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    protected void loadViewLayout() {
        setContentView(R.layout.activity_sales);
        setTitle("销 售 退 货 单");
        setHeadRightVisibility(View.VISIBLE);
    }

    @Override
    protected void setListener() {
        // ListView设置
        adapter = new SalesAdspter(this, dataList);
        lvDatas.setAdapter(adapter);
        tvShowAll.setOnClickListener(this);
        tvShowAudit.setOnClickListener(this);
        tvShowUnAudit.setOnClickListener(this);
        tvShowAll.setChecked(true);
        lvDatas.setOnRefreshListener(this);
        TouchListener tl = new TouchListener();
        etNo.setOnTouchListener(tl);
        ftvScreenIcon.setOnClickListener(this);
        lvDatas.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                RefreshListView listView = (RefreshListView) arg0;
                HashMap<String, String> map = (HashMap<String, String>) listView.getItemAtPosition(arg2);
                String SalesID = (String) map.get("SalesID");
                String AuditFlag = (String) map.get("AuditFlag");
                Intent intent = new Intent(SalesReturnsActivity.this, SalesReturnsDetailActivity.class);
                intent.putExtra("SalesID", SalesID);
                intent.putExtra("AuditFlag", AuditFlag);
                startActivity(intent);
                finish();
            }

        });

    }

    @Override
    protected void processLogic() {
        if (NetUtil.hasNetwork(getApplicationContext())) {
            if (LoginParameterUtil.online) {
                // 获取用户操作权限(浏览)
                boolean browseRight = LoginParameterUtil.salesReturnRightMap.get("BrowseRight");
                if (browseRight) {
                    Bundle bundle = this.getIntent().getExtras();
                    if (null != bundle) {
                        audit = bundle.getString("auditFlag");
                        refresh = true;
                        if ("true".equals(audit)) {
                            tvShowAudit.callOnClick();
                        } else if ("false".equals(audit)) {
                            tvShowUnAudit.callOnClick();
                        } else {
                            tvShowAll.callOnClick();
                        }
                    } else {
                        // 加载数据
                        getData(true, 0);
                    }
                } else {
                    Builder dialog = new AlertDialog.Builder(SalesReturnsActivity.this, AlertDialog.THEME_HOLO_LIGHT);
                    dialog.setTitle("提示");
                    dialog.setMessage("当前暂无浏览销售退货单的操作权限");
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
                AlertDialog.Builder dialog = new AlertDialog.Builder(SalesReturnsActivity.this, AlertDialog.THEME_HOLO_LIGHT);
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
            AlertDialog.Builder dialog = new AlertDialog.Builder(SalesReturnsActivity.this, AlertDialog.THEME_HOLO_LIGHT);
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
        tvShowAll = (CheckBoxTextView) findViewById(R.id.show_all);
        tvShowAudit = (CheckBoxTextView) findViewById(R.id.show_audit);
        tvShowUnAudit = (CheckBoxTextView) findViewById(R.id.show_unaudit);
        lvDatas = (RefreshListView) findViewById(R.id.lv_datas);
        etNo = (EditText) findViewById(R.id.no);
        ftvScreenIcon = (FontTextView) findViewById(R.id.screenIcon);
    }

    @Override
    public void onLoadingMore() {
        currPage++;
        getData(false, 1);
    }

    @Override
    public void onDownPullRefresh() {
        currPage = 1;
        getData(true, 2);
    }
}
