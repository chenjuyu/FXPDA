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
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.Toast;
import com.fuxi.adspter.SalesAdspter;
import com.fuxi.main.R;
import com.fuxi.util.AppManager;
import com.fuxi.util.Logger;
import com.fuxi.util.LoginParameterUtil;
import com.fuxi.util.NetUtil;
import com.fuxi.vo.RequestVo;
import com.fuxi.widget.CheckBoxTextView;
import com.fuxi.widget.OnRefreshListener;
import com.fuxi.widget.RefreshListView;

/**
 * Title: StockMoveInActivity Description: 进仓单活动界面
 * 
 * @author LYJ
 * 
 */
public class WarehouseStockInActivity extends BaseWapperActivity implements OnRefreshListener {

    private static final String TAG = "WarehouseStockInActivity";
    private static final String url = "/stock.do?getStockIn";

    private RefreshListView lvDatas; // 刷新ListView
    private CheckBoxTextView tvShowAll;
    private CheckBoxTextView tvShowAudit;
    private CheckBoxTextView tvShowUnAudit;
    private EditText etDocType;
    private EditText etWarehouseId;

    private List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
    private SalesAdspter adapter;

    public String audit = null;
    private String warehouseId;
    private boolean refresh;
    private int type;
    private int currPage = 1;

    /**
     * 点击新增单据
     */
    private void addItem() {
        Intent intent = new Intent(WarehouseStockInActivity.this, WarehouseStockInDetailActivity.class);
        startActivity(intent);
    }

    /**
     * 根据筛选条件获取进仓单列表
     * 
     * @param refresh
     * @param type
     */
    public void getData(boolean refresh, int type) {
        RequestVo vo = new RequestVo();
        vo.requestUrl = url;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("audit", audit);
        map.put("currPage", String.valueOf(currPage));
        map.put("type", etDocType.getText().toString());
        map.put("warehouseId", warehouseId);
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
                        if (WarehouseStockInActivity.this.refresh) {
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
                        if (WarehouseStockInActivity.this.type == 1) {
                            lvDatas.hideFooterView();
                            if (array.length() == 0) {
                                Toast.makeText(WarehouseStockInActivity.this, "已经到达最后一页", Toast.LENGTH_SHORT).show();
                            }
                        } else if (WarehouseStockInActivity.this.type == 2) {
                            lvDatas.hideHeaderView();
                        }
                        adapter.refresh(dataList);

                    } else {

                    }
                } catch (Exception e) {
                    Toast.makeText(WarehouseStockInActivity.this, "系统错误", Toast.LENGTH_LONG).show();
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
            default:
                break;
        }
    }

    private class TouchListener implements OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (v.getId()) {
                case R.id.warehouseId:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Intent intent = new Intent(WarehouseStockInActivity.this, SelectActivity.class);
                        intent.putExtra("selectType", "selectWarehouse");
                        startActivityForResult(intent, R.id.warehouseId);
                        overridePendingTransition(R.anim.activity_open, 0);
                    }
                    break;
                case R.id.docType:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Intent intent = new Intent(WarehouseStockInActivity.this, SelectActivity.class);
                        intent.putExtra("param", warehouseId);
                        intent.putExtra("selectType", "selectStockTypeIn");
                        startActivityForResult(intent, R.id.docType);
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
            case R.id.warehouseId:
                if (resultCode == 1) {
                    etWarehouseId.setText(data.getStringExtra("Name"));
                    warehouseId = data.getStringExtra("DepartmentID");
                    // 加载数据
                    getData(true, 0);
                }
                break;
            case R.id.docType:
                if (resultCode == 1) {
                    etDocType.setText(data.getStringExtra("Name"));
                    // 加载数据
                    getData(true, 0);
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onHeadRightButton(View v) {
        // 获取用户操作权限(新增)
        boolean addRight = LoginParameterUtil.stockInRightMap.get("AddRight");
        if (addRight) {
            // 添加进仓单明细
            addItem();
        } else {
            Builder dialog = new AlertDialog.Builder(WarehouseStockInActivity.this, AlertDialog.THEME_HOLO_LIGHT);
            dialog.setTitle("提示");
            dialog.setMessage("当前暂无新增进仓单的操作权限");
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
        setContentView(R.layout.activity_warehouse_stock);
        setTitle("进 仓 单");
        setHeadRightVisibility(View.INVISIBLE);
    }

    @Override
    protected void setListener() {
        TouchListener tl = new TouchListener();
        etWarehouseId.setOnTouchListener(tl);
        etDocType.setOnTouchListener(tl);
        // ListView设置
        adapter = new SalesAdspter(this, dataList);
        lvDatas.setAdapter(adapter);
        tvShowAll.setOnClickListener(this);
        tvShowAudit.setOnClickListener(this);
        tvShowUnAudit.setOnClickListener(this);
        tvShowAll.setChecked(true);
        lvDatas.setOnRefreshListener(this);
        lvDatas.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                RefreshListView listView = (RefreshListView) arg0;
                HashMap<String, String> map = (HashMap<String, String>) listView.getItemAtPosition(arg2);
                String stockMoveId = (String) map.get("stockId");
                String AuditFlag = (String) map.get("AuditFlag");
                Intent intent = new Intent(WarehouseStockInActivity.this, WarehouseStockInDetailActivity.class);
                intent.putExtra("stockId", stockMoveId);
                intent.putExtra("AuditFlag", AuditFlag);
                intent.putExtra("typeStr", etDocType.getText().toString());
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
                boolean browseRight = LoginParameterUtil.stockInRightMap.get("BrowseRight");
                if (browseRight) {
                    warehouseId = LoginParameterUtil.deptId;
                    etWarehouseId.setText(LoginParameterUtil.deptName);
                    etDocType.setText("全部");
                    Bundle bundle = this.getIntent().getExtras();
                    if (null != bundle) {
                        audit = bundle.getString("auditFlag");
                        // 选择的单据类型
                        String typeStr = bundle.getString("typeStr");
                        etDocType.setText(typeStr);
                        // 选择的单据仓库
                        warehouseId = bundle.getString("warehouseId");
                        String warehouse = bundle.getString("warehouse");
                        etWarehouseId.setText(warehouse);
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
                    Builder dialog = new AlertDialog.Builder(WarehouseStockInActivity.this, AlertDialog.THEME_HOLO_LIGHT);
                    dialog.setTitle("提示");
                    dialog.setMessage("当前暂无浏览进仓单的操作权限");
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
                AlertDialog.Builder dialog = new AlertDialog.Builder(WarehouseStockInActivity.this, AlertDialog.THEME_HOLO_LIGHT);
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
            AlertDialog.Builder dialog = new AlertDialog.Builder(WarehouseStockInActivity.this, AlertDialog.THEME_HOLO_LIGHT);
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
        etDocType = (EditText) findViewById(R.id.docType);
        etWarehouseId = (EditText) findViewById(R.id.warehouseId);
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
