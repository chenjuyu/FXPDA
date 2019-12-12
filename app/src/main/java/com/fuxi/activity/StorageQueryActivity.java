package com.fuxi.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.fuxi.adspter.StorageLockAdapter;
import com.fuxi.adspter.StorageQueryAdspter;
import com.fuxi.main.R;
import com.fuxi.util.AppManager;
import com.fuxi.util.Logger;
import com.fuxi.util.LoginParameterUtil;
import com.fuxi.util.NetUtil;
import com.fuxi.vo.RequestVo;
import com.fuxi.widget.FontTextView;

/**
 * Title: StorageQueryActivity Description: 货架货品库存查询(仓位管理)
 * 
 * @author LYJ
 * 
 */
public class StorageQueryActivity extends BaseWapperActivity {

    private static final String TAG = "StorageQueryActivity";
    private static String queryPath = "/storageQuery.do?queryStorage";
    private static String getLockDetail = "/storageQuery.do?getLockDetail";

    private List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
    private List<Map<String, Object>> datas = new ArrayList<Map<String, Object>>();

    private FontTextView productIcon;
    private FontTextView storageIcon;
    private EditText et_storage; // 仓位
    private EditText et_product; // 货品
    private TextView tv_totalCount; // 合计
    private TextView tv_search; // 查询
    private TextView tv_lockDetail; // 暂时锁定的货品明细
    private ListView lv_detail; // 添加的货品集合

    private StorageLockAdapter oddAdapter;
    private StorageQueryAdspter adspter;
    private String storageCode;
    private String storageId;
    private String goodsCode;
    private String goodsId;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.search:
                createQuery();
                break;
            case R.id.storage:
                closeInputMethodManager(v);
                et_storage.requestFocus();
                break;
            case R.id.product:
                closeInputMethodManager(v);
                et_product.requestFocus();
                break;
            case R.id.lockDetail:
                datas.clear();
                queryLockDetail();
                break;
            default:
                break;
        }
    }

    /**
     * 关闭活动的软键盘
     * 
     * @param v
     */
    private void closeInputMethodManager(View v) {
        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
        }
    }

    /**
     * 查询暂时锁定的货品信息
     */
    private void queryLockDetail() {
        RequestVo vo = new RequestVo();
        vo.requestUrl = getLockDetail;
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
                        JSONArray array = retObj.getJSONArray("obj");
                        if (array.length() > 0) {// 有库存
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject obj = array.getJSONObject(i);
                                Map<String, Object> map = new HashMap<String, Object>();
                                map.put("StockNo", obj.getString("StockNo"));
                                map.put("Storage", obj.getString("Storage"));
                                map.put("UserName", obj.getString("UserName"));
                                map.put("GoodsCode", obj.getString("GoodsCode"));
                                map.put("Color", obj.getString("Color"));
                                map.put("Size", obj.getString("Size"));
                                map.put("Quantity", obj.getInt("Quantity"));
                                datas.add(map);
                            }
                            showLockStorageDataDetail();
                            // 刷新ListView
                            oddAdapter.refresh(datas);
                        } else {
                            Toast.makeText(StorageQueryActivity.this, "没有锁定的货品信息", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(StorageQueryActivity.this, "没有锁定的货品信息", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(StorageQueryActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 显示暂时锁定的仓位货品信息明细
     */
    private void showLockStorageDataDetail() {
        View view = (LinearLayout) getLayoutInflater().inflate(R.layout.activity_storage_lock_detail, null);
        // 初始化
        ListView lv_datas = (ListView) view.findViewById(R.id.lv_datas);
        // 显示数据
        oddAdapter = new StorageLockAdapter(this, datas);
        lv_datas.setAdapter(oddAdapter);
        // 操作提示
        Builder dialog = new AlertDialog.Builder(StorageQueryActivity.this, AlertDialog.THEME_HOLO_LIGHT);
        dialog.setCancelable(false);
        dialog.setTitle("暂时锁定的仓位货品信息明细");
        dialog.setView(view);
        // 相当于点击确认按钮
        dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {}
        });
        dialog.create();
        dialog.show();
    }

    /**
     * 仓位库存查询方法
     */
    private void createQuery() {
        goodsCode = et_product.getText().toString();
        storageCode = et_storage.getText().toString();
        // 非空判断
        if ((null == goodsCode || goodsCode.isEmpty()) && (null == storageCode || storageCode.isEmpty())) {
            Toast.makeText(StorageQueryActivity.this, "请选择仓位或扫描货品条码后再进行查询", Toast.LENGTH_SHORT).show();
            return;
        }
        RequestVo vo = new RequestVo();
        vo.requestUrl = queryPath;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("storageId", storageId);
        map.put("storageCode", storageCode);
        map.put("goodsId", goodsId);
        map.put("goodsCode", goodsCode);
        vo.requestDataMap = map;
        // 初始化
        initialData();
        super.getDataFromServer(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    if (retObj.getBoolean("success")) {
                        JSONArray array = retObj.getJSONArray("obj");
                        if (array.length() > 0) {// 有库存
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
                        } else {
                            Toast.makeText(StorageQueryActivity.this, "暂无货品仓位信息", Toast.LENGTH_SHORT).show();
                            Logger.e(TAG, "暂无货品仓位信息");
                        }
                        // 刷新ListView
                        adspter.refresh(dataList);
                        // 计算价格
                        countTotal();
                    } else {
                        if ("条码或货号错误".equals(retObj.getString("msg"))) {
                            et_product.selectAll();
                            Toast.makeText(StorageQueryActivity.this, retObj.getString("msg"), Toast.LENGTH_SHORT).show();
                            Logger.e(TAG, retObj.getString("msg"));
                        } else if ("仓位编码错误".equals(retObj.getString("msg"))) {
                            et_storage.selectAll();
                            Toast.makeText(StorageQueryActivity.this, retObj.getString("msg"), Toast.LENGTH_SHORT).show();
                            Logger.e(TAG, retObj.getString("msg"));
                        } else {
                            Toast.makeText(StorageQueryActivity.this, "暂无货品仓位信息", Toast.LENGTH_SHORT).show();
                            Logger.e(TAG, "暂无货品仓位信息");
                        }
                    }
                } catch (JSONException e) {
                    Toast.makeText(StorageQueryActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 初始化仓位库存查询界面
     */
    private void initialData() {
        dataList.clear();
        goodsId = null;
        storageId = null;
        et_product.setText(null);
        et_storage.setText(null);
    }

    /**
     * 计算并显示总数量和价格
     */
    private void countTotal() {
        int sum = 0;
        for (int j = 0; j < dataList.size(); j++) {
            int num = Integer.parseInt(String.valueOf(dataList.get(j).get("Quantity")));
            sum += num;
        }
        tv_totalCount.setText(String.valueOf(sum));
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    protected void setListener() {
        TouchListener tl = new TouchListener();
        adspter = new StorageQueryAdspter(this, dataList);
        lv_detail.setAdapter(adspter);
        tv_search.setOnClickListener(this);
        tv_lockDetail.setOnClickListener(this);
        et_product.setOnClickListener(this);
        et_storage.setOnClickListener(this);
        productIcon.setOnTouchListener(tl);
        storageIcon.setOnTouchListener(tl);
        // lv_detail.setOnItemClickListener(new OnItemClickListener() {
        // @Override
        // public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long
        // arg3) {
        // ListView listView = (ListView)arg0;
        // HashMap<String, String> map = (HashMap<String,
        // String>)listView.getItemAtPosition(arg2);
        // String goodsId = (String)map.get("GoodsID");
        // String colorId = (String)map.get("ColorID");
        // String sizeId = (String)map.get("SizeID");
        // Intent intent = new
        // Intent(StorageQueryActivity.this,InventoryDistributionActivity.class);
        // intent.putExtra("goodsId", goodsId);
        // intent.putExtra("colorId", colorId);
        // intent.putExtra("sizeId", sizeId);
        // startActivity(intent);
        // }
        //
        // });
    }

    private class TouchListener implements OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (v.getId()) {
                case R.id.productIcon:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Intent intent = new Intent(StorageQueryActivity.this, SelectActivity.class);
                        intent.putExtra("selectType", "selectProduct");
                        startActivityForResult(intent, R.id.productIcon);
                        overridePendingTransition(R.anim.activity_open, 0);
                    }
                    break;
                case R.id.storageIcon:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Intent intent = new Intent(StorageQueryActivity.this, SelectActivity.class);
                        intent.putExtra("selectType", "selectStorage");
                        startActivityForResult(intent, R.id.storageIcon);
                        overridePendingTransition(R.anim.activity_open, 0);
                    }
                    break;
                default:
                    InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm.isActive()) {
                        imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
                    }
                    break;
            }
            return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case R.id.productIcon:
                if (resultCode == 1) {
                    et_product.setText(data.getStringExtra("Code"));
                    goodsId = data.getStringExtra("GoodsID");
                    et_product.setSelection(data.getStringExtra("Code").length());
                }
                break;
            case R.id.storageIcon:
                if (resultCode == 1) {
                    et_storage.setText(data.getStringExtra("Name"));
                    storageId = data.getStringExtra("StorageID");
                    et_storage.setSelection(data.getStringExtra("Name").length());
                    et_product.requestFocus();
                }
                break;
            default:
                break;
        }
    }

    // 条码扫描完成触发Enter事件
    // class BarcodeActionListener implements OnEditorActionListener{
    //
    // @Override
    // public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
    // if ((actionId == EditorInfo.IME_ACTION_DONE || actionId ==
    // EditorInfo.IME_ACTION_SEARCH || actionId ==
    // EditorInfo.IME_ACTION_UNSPECIFIED) && ((event != null &&
    // event.getKeyCode() == KeyEvent.KEYCODE_ENTER))) {
    // InputMethodManager imm = (InputMethodManager)
    // v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
    // if (imm.isActive()) {
    // imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
    // }
    // }
    // return false;
    // }
    //
    // }

    @Override
    protected void loadViewLayout() {
        setContentView(R.layout.activity_storage_query);
        setTitle("仓位分布查询");
    }

    @Override
    protected void processLogic() {
        if (NetUtil.hasNetwork(getApplicationContext())) {
            if (LoginParameterUtil.online) {
                // 设置扫码区自动获取焦点
                et_storage.requestFocus();
            } else {
                AlertDialog.Builder dialog = new AlertDialog.Builder(StorageQueryActivity.this, AlertDialog.THEME_HOLO_LIGHT);
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
            AlertDialog.Builder dialog = new AlertDialog.Builder(StorageQueryActivity.this, AlertDialog.THEME_HOLO_LIGHT);
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
        lv_detail = (ListView) findViewById(R.id.lv_datas);
        et_storage = (EditText) findViewById(R.id.storage);
        et_product = (EditText) findViewById(R.id.product);
        tv_search = (TextView) findViewById(R.id.search);
        tv_lockDetail = (TextView) findViewById(R.id.lockDetail);
        tv_totalCount = (TextView) findViewById(R.id.totalCount);
        productIcon = (FontTextView) findViewById(R.id.productIcon);
        storageIcon = (FontTextView) findViewById(R.id.storageIcon);
    }

}
