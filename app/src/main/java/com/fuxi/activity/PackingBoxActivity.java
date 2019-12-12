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
import android.content.DialogInterface;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.fuxi.adspter.PackingBoxAdspter;
import com.fuxi.main.R;
import com.fuxi.util.AppManager;
import com.fuxi.util.Logger;
import com.fuxi.util.LoginParameterUtil;
import com.fuxi.util.NetUtil;
import com.fuxi.vo.RequestVo;
import com.fuxi.widget.OnRefreshListener;
import com.fuxi.widget.RefreshListView;

/**
 * Title: PackingActivity Description: 装箱单
 * 
 * @author LYJ
 * 
 */
public class PackingBoxActivity extends BaseWapperActivity implements OnItemClickListener, OnRefreshListener {

    private static final String TAG = "PackingActivity";
    private static final String url = "/packing.do?getList";

    private RefreshListView lvDatas; // ListView
    private EditText etNo;
    private EditText etCustomer;
    private EditText etEmployee;
    private TextView tvSearch;
    private TextView tvTotalCount;

    private List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
    private PackingBoxAdspter adapter;

    private String no;
    private String customerId;
    private String employeeId;
    private String relationType;
    private boolean refresh;
    private int type;
    private int currPage = 1;

    /**
     * 获取销售发货单列表
     * 
     * @param refresh
     * @param type
     */
    private void getData(boolean refresh, int type) {
        RequestVo vo = new RequestVo();
        vo.requestUrl = url;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("no", no);
        map.put("currPage", String.valueOf(currPage));
        map.put("customerId", customerId);
        map.put("employeeId", employeeId);
        map.put("relationType", relationType);
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
                        if (PackingBoxActivity.this.refresh) {
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
                        if (PackingBoxActivity.this.type == 1) {
                            lvDatas.hideFooterView();
                            if (array.length() == 0) {
                                Toast.makeText(PackingBoxActivity.this, "已经到达最后一页", Toast.LENGTH_SHORT).show();
                            }
                        } else if (PackingBoxActivity.this.type == 2) {
                            lvDatas.hideHeaderView();
                        }
                        countTotal();
                        adapter.refresh(dataList);
                    } else {
                    }
                } catch (Exception e) {
                    Toast.makeText(PackingBoxActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 计算并显示总数量
     */
    private void countTotal() {
        int sum = 0;
        for (int j = 0; j < dataList.size(); j++) {
            int num = Integer.parseInt(String.valueOf(dataList.get(j).get("QuantitySum")));
            sum += num;
        }
        tvTotalCount.setText(String.valueOf(sum));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.search:
                // 获取数据
                getData(true, type);
                break;
            default:
                break;
        }
    }

    /**
     * 重置筛选条件
     */
    private void reset() {
        etNo.setText(null);
        etCustomer.setText(null);
        etEmployee.setText(null);
        no = null;
        customerId = null;
        employeeId = null;
    }

    private class TouchListener implements OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            try {
                switch (v.getId()) {
                    case R.id.no:
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            reset();
                            Intent intent = new Intent(PackingBoxActivity.this, SelectActivity.class);
                            if ("sales".equals(relationType)) {
                                intent.putExtra("selectType", "selectPackingBoxSalesNo");
                            } else if ("stockOut".equals(relationType)) {
                                intent.putExtra("selectType", "selectPackingBoxStockOutNo");
                            }
                            startActivityForResult(intent, R.id.no);
                            overridePendingTransition(R.anim.activity_open, 0);
                        }
                        break;
                    case R.id.customer:
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            reset();
                            Intent intent = new Intent(PackingBoxActivity.this, SelectActivity.class);
                            intent.putExtra("selectType", "selectCustomer");
                            startActivityForResult(intent, R.id.customer);
                            overridePendingTransition(R.anim.activity_open, 0);
                        }
                        break;
                    case R.id.employee:
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            reset();
                            Intent intent = new Intent(PackingBoxActivity.this, SelectActivity.class);
                            intent.putExtra("selectType", "selectEmployee");
                            startActivityForResult(intent, R.id.employee);
                            overridePendingTransition(R.anim.activity_open, 0);
                        }
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case R.id.no:
                if (resultCode == 1) {
                    no = data.getStringExtra("Name");
                    etNo.setText(data.getStringExtra("Name"));
                }
                break;
            case R.id.customer:
                if (resultCode == 1) {
                    customerId = data.getStringExtra("CustomerID");
                    etCustomer.setText(data.getStringExtra("Name"));
                }
                break;
            case R.id.employee:
                if (resultCode == 1) {
                    employeeId = data.getStringExtra("EmployeeID");
                    etEmployee.setText(data.getStringExtra("Name"));
                }
                break;
            default:
                break;
        }
    }

    // @Override
    // protected void onHeadRightButton(View v) {
    // // 获取用户操作权限(新增)
    // boolean addRight = LoginParameterUtil.salesRightMap.get("AddRight");
    // if (addRight) {
    // // 新增销售发货单(散件)
    // addItem();
    // } else {
    // Builder dialog = new AlertDialog.Builder(PackingBoxActivity.this,
    // AlertDialog.THEME_HOLO_LIGHT);
    // dialog.setTitle("提示");
    // dialog.setMessage("当前暂无新增销售发货单的操作权限");
    // // 相当于点击确认按钮
    // dialog.setPositiveButton("确认",
    // new DialogInterface.OnClickListener() {
    // @Override
    // public void onClick(DialogInterface dialog, int which) {
    // }
    // });
    // dialog.setCancelable(false);
    // dialog.create();
    // dialog.show();
    // }
    // }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    protected void onHeadRightButton(View v) {
        Intent intent = new Intent(PackingBoxActivity.this, PackingBoxSelectPrintActivity.class);
        startActivity(intent);
    }

    @Override
    protected void loadViewLayout() {
        setContentView(R.layout.activity_packing_box);
        setTitle("装 箱 单");
        setHeadRightVisibility(View.VISIBLE);
        setHeadRightText(R.string.already_complete);
    }

    @Override
    protected void setListener() {
        // ListView设置
        adapter = new PackingBoxAdspter(this, dataList);
        lvDatas.setAdapter(adapter);
        lvDatas.setOnItemClickListener(this);
        lvDatas.setOnRefreshListener(this);
        TouchListener tl = new TouchListener();
        etNo.setOnTouchListener(tl);
        etCustomer.setOnTouchListener(tl);
        etEmployee.setOnTouchListener(tl);
        tvSearch.setOnClickListener(this);
    }

    @Override
    protected void processLogic() {
        if (NetUtil.hasNetwork(getApplicationContext())) {
            if (LoginParameterUtil.online) {
                // 根据参数获取关联类别
                if (LoginParameterUtil.packingBoxRelationType == 0) {
                    relationType = "sales";
                } else {
                    relationType = "stockOut";
                }
                boolean browseRight = LoginParameterUtil.packingBoxRightMap.get("BrowseRight");
                if (browseRight) {
                    // 加载数据
                    getData(true, type);
                } else {
                    Builder dialog = new AlertDialog.Builder(PackingBoxActivity.this, AlertDialog.THEME_HOLO_LIGHT);
                    dialog.setTitle("提示");
                    dialog.setMessage("当前暂无装箱单的操作权限");
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
                AlertDialog.Builder dialog = new AlertDialog.Builder(PackingBoxActivity.this, AlertDialog.THEME_HOLO_LIGHT);
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
            AlertDialog.Builder dialog = new AlertDialog.Builder(PackingBoxActivity.this, AlertDialog.THEME_HOLO_LIGHT);
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
        etNo = (EditText) findViewById(R.id.no);
        etCustomer = (EditText) findViewById(R.id.customer);
        etEmployee = (EditText) findViewById(R.id.employee);
        tvSearch = (TextView) findViewById(R.id.search);
        tvTotalCount = (TextView) findViewById(R.id.totalCount);
        lvDatas = (RefreshListView) findViewById(R.id.lv_datas);
        lvDatas.setOnRefreshListener(this);
    }

    /**
     * ListView点击事件
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        RefreshListView listView = (RefreshListView) parent;
        HashMap<String, String> map = (HashMap<String, String>) listView.getItemAtPosition(position);
        String no = (String) map.get("No");
        String docId = (String) map.get("ID");
        String customerId = (String) map.get("CustomerID");
        String employeeId = (String) map.get("EmployeeID");
        String brandId = (String) map.get("BrandID");
        String brandCode = (String) map.get("BrandCode");
        String type = (String) map.get("Type");
        String departmentId = (String) map.get("DepartmentID");
        String customer = (String) map.get("Customer");
        Intent intent = new Intent(PackingBoxActivity.this, PackingBoxDetailActivity.class);
        intent.putExtra("No", no);
        intent.putExtra("ID", docId);
        intent.putExtra("CustomerID", customerId);
        intent.putExtra("Type", type);
        intent.putExtra("DepartmentID", departmentId);
        intent.putExtra("EmployeeID", employeeId);
        intent.putExtra("BrandID", brandId);
        intent.putExtra("BrandCode", brandCode);
        intent.putExtra("Customer", customer);
        startActivity(intent);
        finish();
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
