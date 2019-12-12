package com.fuxi.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import android.app.AlertDialog;
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
import android.widget.LinearLayout;
import android.widget.Toast;
import com.fuxi.adspter.StockAdspter;
import com.fuxi.main.R;
import com.fuxi.util.AppManager;
import com.fuxi.util.Logger;
import com.fuxi.util.LoginParameterUtil;
import com.fuxi.util.NetUtil;
import com.fuxi.vo.RequestVo;
import com.fuxi.widget.OnRefreshListener;
import com.fuxi.widget.RefreshListView;

/**
 * Title: StockInActivity Description: 进仓单列表显示活动界面(仓位管理)
 * 
 * @author LYJ
 * 
 */
public class StockInActivity extends BaseWapperActivity implements OnRefreshListener {

    private static final String TAG = "StockActivity";
    private static final String url = "/shelvesIn.do?getStockIn";
    private static final String judgeWarehouseType = "/storageQuery.do?judgeWarehouseType";

    private List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
    private StockAdspter adapter;

    private RefreshListView lvDatas; // ListView
    private LinearLayout llMain;
    private LinearLayout llType;
    private View view1;
    private EditText etWarehouseId;
    private EditText etDocType;

    private String warehouseId;
    private boolean refresh;
    private int type;
    private int currPage = 1;
    private String typeStr;

    /**
     * 根据筛选条件获取进仓单
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
        map.put("type", typeStr);
        map.put("docType", etDocType.getText().toString());
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
                        if (StockInActivity.this.refresh) {
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
                        if (StockInActivity.this.type == 1) {
                            lvDatas.hideFooterView();
                            if (array.length() == 0) {
                                Toast.makeText(StockInActivity.this, "已经到达最后一页", Toast.LENGTH_SHORT).show();
                            }
                        } else if (StockInActivity.this.type == 2) {
                            lvDatas.hideHeaderView();
                        }
                        adapter.refresh(dataList);

                    } else {

                    }
                } catch (Exception e) {
                    Toast.makeText(StockInActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 判断仓库是是否处于初始化状态
     */
    private void judgeWarehouseType() {
        if (LoginParameterUtil.deptId == null || LoginParameterUtil.deptId.isEmpty() || "".equals(LoginParameterUtil.deptId)) {
            return;
        }
        RequestVo vo = new RequestVo();
        vo.requestUrl = judgeWarehouseType;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("warehouseId", LoginParameterUtil.deptId);
        vo.requestDataMap = map;
        super.getDataFromServer(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    int count = retObj.getInt("obj");
                    if (retObj.getBoolean("success") && count > 0) {
                        etWarehouseId.setText(LoginParameterUtil.deptName);
                        warehouseId = LoginParameterUtil.deptId;
                        // 加载数据
                        getData(true, 0);
                    }
                } catch (Exception e) {
                    Toast.makeText(StockInActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    @Override
    public void onClick(View v) {}

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }

    private class TouchListener implements OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (v.getId()) {
                case R.id.warehouseId:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Intent intent = new Intent(StockInActivity.this, SelectActivity.class);
                        intent.putExtra("selectType", "selectWarehouseHasStorage");
                        startActivityForResult(intent, R.id.warehouseId);
                        overridePendingTransition(R.anim.activity_open, 0);
                    }
                    break;
                case R.id.docType:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Intent intent = new Intent(StockInActivity.this, SelectActivity.class);
                        intent.putExtra("param", warehouseId);
                        intent.putExtra("selectType", "selectDocTypeIn");
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
    protected void loadViewLayout() {
        setContentView(R.layout.activity_stock);
        setTitle("上 架 列 表");
    }

    @Override
    protected void setListener() {
        TouchListener tl = new TouchListener();
        etWarehouseId.setOnTouchListener(tl);
        etDocType.setOnTouchListener(tl);
        // ListView设置
        adapter = new StockAdspter(this, dataList);
        lvDatas.setAdapter(adapter);
        lvDatas.setOnRefreshListener(this);
        lvDatas.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                RefreshListView listView = (RefreshListView) arg0;
                HashMap<String, String> map = (HashMap<String, String>) listView.getItemAtPosition(arg2);
                String stockId = (String) map.get("StockID");
                String stockNo = (String) map.get("No");
                String departmentId = (String) map.get("DepartmentID");
                Intent intent = new Intent(StockInActivity.this, ShelvesInActivity.class);
                intent.putExtra("stockId", stockId);
                intent.putExtra("stockNo", stockNo);
                intent.putExtra("departmentId", departmentId);
                intent.putExtra("type", typeStr);
                startActivity(intent);
                finish();
            }
        });

    }

    @Override
    protected void processLogic() {
        if (NetUtil.hasNetwork(getApplicationContext())) {
            if (LoginParameterUtil.online) {
                Bundle bundle = this.getIntent().getExtras();
                if (null != bundle) {
                    typeStr = bundle.getString("type");
                }
                // 设置默认部门(必须含仓位)
                if (LoginParameterUtil.hasStorage) {
                    judgeWarehouseType();
                }
                // 默认显示全部单据
                etDocType.setText("全部");
                if ("调入".equals(typeStr)) {
                    llMain.removeView(llType);
                    llMain.removeView(view1);
                }
                // 设置默认部门(必须含仓位)
                if (!LoginParameterUtil.hasStorage) {
                    // 加载数据
                    getData(true, 0);
                }
            } else {
                AlertDialog.Builder dialog = new AlertDialog.Builder(StockInActivity.this, AlertDialog.THEME_HOLO_LIGHT);
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
            AlertDialog.Builder dialog = new AlertDialog.Builder(StockInActivity.this, AlertDialog.THEME_HOLO_LIGHT);
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
        llMain = (LinearLayout) findViewById(R.id.ll_main);
        llType = (LinearLayout) findViewById(R.id.ll_type);
        view1 = findViewById(R.id.view1);
        lvDatas = (RefreshListView) findViewById(R.id.lv_datas);
        etWarehouseId = (EditText) findViewById(R.id.warehouseId);
        etDocType = (EditText) findViewById(R.id.docType);
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
