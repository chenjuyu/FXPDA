package com.fuxi.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
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
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import com.fuxi.adspter.OddAdapter;
import com.fuxi.main.R;
import com.fuxi.util.AppManager;
import com.fuxi.util.Logger;
import com.fuxi.util.LoginParameterUtil;
import com.fuxi.util.NetUtil;
import com.fuxi.vo.RequestVo;

/**
 * Title: InitializeShelvesInActivity Description: 仓位初始化上架活动界面
 * 
 * @author LYJ
 * 
 */
public class InitializeShelvesInActivity extends BaseWapperActivity implements OnItemLongClickListener {

    private static final String TAG = "InitializeShelvesInActivity";
    private static final String AnalyticalBarcode = "/select.do?getGoodsByBarcode";
    private static final String savePath = "/shelvesIn.do?saveStorageIn";
    private static final String updatePath = "/shelvesIn.do?updateStorageInCount";
    private static final String getDataPath = "/shelvesIn.do?getInitStorageIn";

    private EditText et_warehouseId;
    private EditText et_storageCode;
    private EditText et_goodsBarcode;
    private EditText et_supplierCode;
    private EditText et_goods;
    private EditText et_color;
    private EditText et_size;
    private EditText et_qty;
    private TextView tv_sum;
    private TextView tv_clean;
    private TextView tv_reset;
    private TextView tv_save;
    private ListView lv_details;

    private OddAdapter oddAdapter;
    private List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();

    private String warehouseId;
    private String storageId;
    private String goodsId;
    private String colorId;
    private String sizeId;
    private String goodsCode;
    private String goodsName;
    private String color;
    private String storage;
    private String size;
    private String type = "初始化上架"; // 上架类型
    private boolean show = false;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.save:
                // 保存
                save();
                break;
            case R.id.clean:
                et_storageCode.setText(null);
                storageId = null;
                et_storageCode.setEnabled(true);
                et_storageCode.requestFocus();
                dataList.clear();
                oddAdapter.refresh();
                tv_sum.setText("0");
                break;
            case R.id.reset:
                et_warehouseId.setText(null);
                warehouseId = null;
                et_warehouseId.setEnabled(true);
                et_storageCode.setText(null);
                storageId = null;
                et_storageCode.setEnabled(true);
                dataList.clear();
                oddAdapter.refresh();
                tv_sum.setText("0");
                break;
            default:
                break;
        }
    }

    /**
     * 保存初始化上架信息
     */
    private void save() {
        // 非空判断
        String storage = et_storageCode.getText().toString().trim();
        String qty = et_qty.getText().toString().trim();
        if (warehouseId == null || warehouseId.isEmpty()) {
            et_warehouseId.setEnabled(true);
            Toast.makeText(InitializeShelvesInActivity.this, "请先选择仓库", Toast.LENGTH_SHORT).show();
            return;
        }
        if (storage == null || storage.isEmpty() || storageId == null || storageId.isEmpty()) {
            et_storageCode.setEnabled(true);
            Toast.makeText(InitializeShelvesInActivity.this, "仓位不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (goodsId == null || goodsId.isEmpty() || colorId == null || goodsId.isEmpty() || sizeId == null || sizeId.isEmpty()) {
            Toast.makeText(InitializeShelvesInActivity.this, "请录入要上架的货品", Toast.LENGTH_SHORT).show();
            return;
        }
        if (qty == null || qty.isEmpty() || Integer.parseInt(qty) < 1) {
            Toast.makeText(InitializeShelvesInActivity.this, "货品的上架数量不能为空或小于1", Toast.LENGTH_SHORT).show();
            return;
        }
        RequestVo vo = new RequestVo();
        vo.requestUrl = savePath;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("stockNo", null);
        map.put("storageId", storageId);
        map.put("departmentId", warehouseId);
        map.put("type", type);
        map.put("barcode", null);
        map.put("qty", qty);
        map.put("memo", null);
        map.put("goodsId", goodsId);
        map.put("colorId", colorId);
        map.put("sizeId", sizeId);
        vo.requestDataMap = map;
        super.getDataFromServer(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    JSONObject rs = retObj.getJSONObject("attributes");
                    boolean flag = rs.getBoolean("flag");
                    int count = rs.getInt("count");
                    if (retObj.getBoolean("success") && count > 0 && flag) {
                        Toast.makeText(InitializeShelvesInActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                        // 锁定库位
                        et_warehouseId.setEnabled(false);
                        et_storageCode.setEnabled(false);
                        showDetails();
                        oddAdapter.refresh();
                        countTotal();
                        reset();
                    } else {
                        Toast.makeText(InitializeShelvesInActivity.this, "保存失败", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(InitializeShelvesInActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 获取同仓库同仓位的初始化数据
     */
    private void getData() {
        // 非空判断
        String storage = et_storageCode.getText().toString().trim();
        String qty = et_qty.getText().toString().trim();
        if (warehouseId == null || warehouseId.isEmpty()) {
            et_warehouseId.setEnabled(true);
            return;
        }
        if (storage == null || storage.isEmpty() || storageId == null || storageId.isEmpty()) {
            et_storageCode.setEnabled(true);
            return;
        }
        RequestVo vo = new RequestVo();
        vo.requestUrl = getDataPath;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("storageId", storageId);
        map.put("departmentId", warehouseId);
        map.put("type", type);
        vo.requestDataMap = map;
        super.getDataFromServer(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    if (retObj.getBoolean("success")) {
                        JSONArray rs = retObj.getJSONArray("obj");
                        if (rs.length() > 0) {
                            for (int i = 0; i < rs.length(); i++) {
                                JSONObject obj = rs.getJSONObject(i);
                                Map<String, Object> map = new HashMap<String, Object>();
                                map.put("GoodsID", obj.getString("GoodsID"));
                                map.put("ColorID", obj.getString("ColorID"));
                                map.put("SizeID", obj.getString("SizeID"));
                                map.put("GoodsCode", obj.getString("GoodsCode"));
                                map.put("Color", obj.getString("Color"));
                                map.put("Size", obj.getString("Size"));
                                map.put("Quantity", obj.getInt("Quantity"));
                                dataList.add(map);
                            }
                            if (!show) {
                                show();
                            }
                        } else {
                            // Toast.makeText(InitializeShelvesInActivity.this,
                            // "此仓位无初始化上架货品", Toast.LENGTH_SHORT).show();
                        }
                        // 锁定库位
                        et_warehouseId.setEnabled(false);
                        et_storageCode.setEnabled(false);
                        oddAdapter.refresh();
                        countTotal();
                        reset();
                    } else {
                        Toast.makeText(InitializeShelvesInActivity.this, "获取仓位初始化上架信息失败,请重试", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(InitializeShelvesInActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 初始化上架信息时的提示
     */
    private void show() {
        show = true;
        Builder dialog = new AlertDialog.Builder(InitializeShelvesInActivity.this, AlertDialog.THEME_HOLO_LIGHT);
        dialog.setTitle("提示");
        dialog.setMessage("选择仓库和仓位后会显示所选仓库和仓位的初始化上架信息");
        // 相当于点击确认按钮
        dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {}
        });
        dialog.setCancelable(false);
        dialog.create();
        dialog.show();
    }

    /**
     * 修改初始化上架的货品数量
     * 
     * @param goodsId
     * @param colorId
     * @param sizeId
     * @param qty
     */
    private void updateStorageInCount(String goodsId, String colorId, String sizeId, String qty) {
        RequestVo vo = new RequestVo();
        vo.requestUrl = updatePath;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("storageId", storageId);
        map.put("departmentId", warehouseId);
        map.put("type", type);
        map.put("qty", qty);
        map.put("goodsId", goodsId);
        map.put("colorId", colorId);
        map.put("sizeId", sizeId);
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
                        Toast.makeText(InitializeShelvesInActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
                        oddAdapter.refresh();
                        countTotal();
                    } else {
                        Toast.makeText(InitializeShelvesInActivity.this, "修改失败", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(InitializeShelvesInActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 显示保存的集合明细
     */
    private void showDetails() {
        // 添加集合
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("Barcode", null);
        map.put("GoodsID", goodsId);
        map.put("ColorID", colorId);
        map.put("SizeID", sizeId);
        map.put("GoodsCode", goodsCode);
        map.put("Color", color);
        map.put("Size", size);
        map.put("Quantity", et_qty.getText().toString().trim());
        dataList.add(map);
        dataList = mergeData(dataList);
    }

    /**
     * 合并addList中的集合
     * 
     * @param addList
     * @return
     */
    private List<Map<String, Object>> mergeData(List<Map<String, Object>> addList) {
        for (int i = 0; i < addList.size() - 1; i++) {
            Map temp1 = (Map) addList.get(i);
            for (int j = addList.size() - 1; j > i; j--) {
                Map temp2 = (Map) addList.get(j);
                if (temp1.get("GoodsID").equals(temp2.get("GoodsID")) && temp1.get("ColorID").equals(temp2.get("ColorID")) && temp1.get("SizeID").equals(temp2.get("SizeID"))) {
                    Map map = new HashMap();
                    int count1 = Integer.parseInt(String.valueOf(temp1.get("Quantity")));
                    int count2 = Integer.parseInt(String.valueOf(temp2.get("Quantity")));
                    temp1.put("Quantity", count1 + count2);
                    addList.remove(j);
                }
            }
        }
        return addList;
    }

    /**
     * 计算总数量(合计)
     */
    private void countTotal() {
        int sum = 0;
        for (int j = 0; j < dataList.size(); j++) {
            int num = Integer.parseInt(String.valueOf(dataList.get(j).get("Quantity")));
            sum += num;
        }
        tv_sum.setText(String.valueOf(sum));
    }

    /**
     * 点击返回时询问是否保存
     */
    private void back() {
        Builder dialog = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
        dialog.setTitle("提示");
        dialog.setMessage("确认退出货架初始化操作吗？");
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

    /**
     * 解析条码
     */
    private void analyticalBarcode() {
        if (warehouseId == null || warehouseId.isEmpty()) {
            et_warehouseId.setEnabled(true);
            et_goodsBarcode.setText(null);
            Toast.makeText(InitializeShelvesInActivity.this, "请先选择仓库", Toast.LENGTH_SHORT).show();
            return;
        }
        if (storage == null || storage.isEmpty() || storageId == null || storageId.isEmpty()) {
            et_storageCode.setEnabled(true);
            et_storageCode.requestFocus();
            et_goodsBarcode.setText(null);
            Toast.makeText(InitializeShelvesInActivity.this, "仓位不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        String barcode = et_goodsBarcode.getText().toString().trim();
        if (barcode == null || barcode.isEmpty()) {
            reset();
            Toast.makeText(InitializeShelvesInActivity.this, "请录入要上架的货品", Toast.LENGTH_SHORT).show();
            return;
        }
        RequestVo vo = new RequestVo();
        vo.requestUrl = AnalyticalBarcode;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("barcode", barcode);
        vo.requestDataMap = map;
        super.getDataFromServer(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    if (retObj.getBoolean("success")) {
                        JSONArray jsonArray = retObj.getJSONArray("obj");
                        JSONObject obj = jsonArray.getJSONObject(0);
                        goodsId = obj.getString("GoodsID");
                        colorId = obj.getString("ColorID");
                        sizeId = obj.getString("SizeID");
                        // 显示货品名称和编号
                        goodsCode = obj.getString("GoodsCode");
                        goodsName = obj.getString("GoodsName");
                        color = obj.getString("Color");
                        size = obj.getString("Size");
                        et_goods.setText(obj.getString("GoodsName") + "(" + obj.getString("GoodsCode") + ")");
                        et_supplierCode.setText(obj.getString("SupplierCode"));
                        et_color.setText(obj.getString("Color"));
                        et_size.setText(obj.getString("Size"));
                        et_goodsBarcode.setText(null);
                        et_goodsBarcode.requestFocus();
                    } else {
                        et_goodsBarcode.requestFocus();
                        et_goodsBarcode.selectAll();
                        Toast.makeText(InitializeShelvesInActivity.this, "不存在此条码", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(InitializeShelvesInActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 保存后重置方法
     */
    private void reset() {
        // 清空货品信息
        goodsId = null;
        goodsCode = null;
        goodsName = null;
        colorId = null;
        color = null;
        sizeId = null;
        size = null;
        et_supplierCode.setText(null);
        et_goods.setText(null);
        et_color.setText(null);
        et_size.setText(null);
        et_goodsBarcode.setText(null);
        et_goodsBarcode.requestFocus();
        et_qty.setText("1");
    }

    /**
     * 查询仓位
     */
    private void searchStorage() {
        if (warehouseId == null || warehouseId.isEmpty()) {
            et_warehouseId.setEnabled(true);
            et_storageCode.setText(null);
            Toast.makeText(InitializeShelvesInActivity.this, "请先选择仓库", Toast.LENGTH_SHORT).show();
            return;
        }
        RequestVo vo = new RequestVo();
        vo.requestUrl = "/select.do?getStorage";
        vo.context = this;
        HashMap map = new HashMap();
        map.put("currPage", String.valueOf(1));
        map.put("param", et_storageCode.getText().toString());
        vo.requestDataMap = map;
        super.getDataFromServerNoProcess(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    JSONArray json = retObj.getJSONArray("obj");
                    if (json.length() > 0) {
                        JSONObject obj = (JSONObject) json.get(0);
                        storage = obj.getString("Name");
                        et_storageCode.setText(obj.getString("Name"));
                        et_storageCode.setSelection(storage.length());
                        storageId = obj.getString("StorageID");
                        // 货品条码获取焦点
                        et_goodsBarcode.requestFocus();
                        // 获取数据
                        getData();
                    } else {
                        et_storageCode.requestFocus();
                        et_storageCode.selectAll();
                        Toast.makeText(InitializeShelvesInActivity.this, "仓位编码错误或不存在此仓位", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(InitializeShelvesInActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    @Override
    protected void onHeadLeftButton(View v) {
        back();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            back();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    protected void loadViewLayout() {
        setContentView(R.layout.activity_initialize_shelves_in);
        setTitle("货架初始化");
    }

    @Override
    protected void processLogic() {
        if (NetUtil.hasNetwork(getApplicationContext())) {
            if (LoginParameterUtil.online) {
                // 设置默认数量
                reset();
                et_storageCode.requestFocus();
                tv_sum.setText("0");
                // //设置默认部门(必须含仓位)
                // if(LoginParameterUtil.hasStorage &&
                // LoginParameterUtil.initWarehouse){
                // warehouseId = LoginParameterUtil.deptId;
                // et_warehouseId.setText(LoginParameterUtil.deptName);
                // }
            } else {
                AlertDialog.Builder dialog = new AlertDialog.Builder(InitializeShelvesInActivity.this, AlertDialog.THEME_HOLO_LIGHT);
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
            AlertDialog.Builder dialog = new AlertDialog.Builder(InitializeShelvesInActivity.this, AlertDialog.THEME_HOLO_LIGHT);
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
        tv_clean.setOnClickListener(this);
        tv_reset.setOnClickListener(this);
        tv_save.setOnClickListener(this);
        et_storageCode.setOnTouchListener(tl);
        et_warehouseId.setOnTouchListener(tl);
        et_storageCode.setOnEditorActionListener(new ShelvesBarcodeActionListener());
        et_goodsBarcode.setOnEditorActionListener(new BarcodeActionListener());
        oddAdapter = new OddAdapter(this, dataList);
        lv_details.setAdapter(oddAdapter);
        lv_details.setOnItemLongClickListener(this);
    }

    private class TouchListener implements OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (v.getId()) {
                case R.id.storageCode:
                    if (warehouseId == null || warehouseId.isEmpty()) {
                        et_warehouseId.setEnabled(true);
                        et_storageCode.setText(null);
                        Toast.makeText(InitializeShelvesInActivity.this, "请先选择仓库", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Intent intent = new Intent(InitializeShelvesInActivity.this, SelectActivity.class);
                        intent.putExtra("selectType", "selectStorageByInner");
                        intent.putExtra("param", warehouseId);
                        startActivityForResult(intent, R.id.storageCode);
                        overridePendingTransition(R.anim.activity_open, 0);
                    }
                    break;
                case R.id.warehouseId:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Intent intent = new Intent(InitializeShelvesInActivity.this, SelectActivity.class);
                        intent.putExtra("selectType", "selectWarehouseHasStorageHasInit");
                        startActivityForResult(intent, R.id.warehouseId);
                        overridePendingTransition(R.anim.activity_open, 0);
                    }
                    break;
                default:
                    break;
            }
            return false;
        }
    }

    // remark
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case R.id.storageCode:
                if (resultCode == 1) {
                    storage = data.getStringExtra("Name");
                    et_storageCode.setText(data.getStringExtra("Name"));
                    et_storageCode.setSelection(storage.length());
                    storageId = data.getStringExtra("StorageID");
                    // 货品条码获取焦点
                    et_goodsBarcode.requestFocus();
                    // 获取数据
                    getData();
                }
                break;
            case R.id.warehouseId:
                if (resultCode == 1) {
                    et_warehouseId.setText(data.getStringExtra("Name"));
                    warehouseId = data.getStringExtra("DepartmentID");
                    et_storageCode.setText(null);
                    storageId = null;
                    et_storageCode.requestFocus();
                }
                break;
            default:
                break;
        }
    }

    /**
     * Title: ShelvesBarcodeActionListener Description: 库位条码扫描完成触发Enter事件
     */
    class ShelvesBarcodeActionListener implements OnEditorActionListener {

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if ((actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_UNSPECIFIED) && ((event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER))) {
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm.isActive()) {
                    imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
                }
                searchStorage();
                // 货品条码获取焦点
                et_goodsBarcode.requestFocus();
            }
            return false;
        }

    }

    /**
     * Title: BarcodeActionListener Description: 货品条码扫描完成触发Enter事件
     */
    class BarcodeActionListener implements OnEditorActionListener {

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if ((actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_UNSPECIFIED) && ((event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER))) {
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm.isActive()) {
                    imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
                }
                analyticalBarcode();
                // 货品条码获取焦点
                et_goodsBarcode.requestFocus();
            }
            return false;
        }

    }

    @Override
    protected void findViewById() {
        et_warehouseId = (EditText) findViewById(R.id.warehouseId);
        et_storageCode = (EditText) findViewById(R.id.storageCode);
        et_goodsBarcode = (EditText) findViewById(R.id.goodsBarcode);
        et_supplierCode = (EditText) findViewById(R.id.supplierCode);
        et_goods = (EditText) findViewById(R.id.goods);
        et_color = (EditText) findViewById(R.id.color);
        et_size = (EditText) findViewById(R.id.size);
        et_qty = (EditText) findViewById(R.id.qty);
        tv_sum = (TextView) findViewById(R.id.sum);
        tv_clean = (TextView) findViewById(R.id.clean);
        tv_reset = (TextView) findViewById(R.id.reset);
        tv_save = (TextView) findViewById(R.id.save);
        lv_details = (ListView) findViewById(R.id.details);
    }

    /**
     * 长按选中ListView的项
     */
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
        Map<String, Object> map = dataList.get(position);
        String goodsCode = String.valueOf(map.get("GoodsCode"));
        final String upGoodsId = String.valueOf(map.get("GoodsID"));
        final String upColorId = String.valueOf(map.get("ColorID"));
        final String upSizeId = String.valueOf(map.get("SizeID"));
        String count = String.valueOf(map.get("Quantity"));
        View v = (LinearLayout) getLayoutInflater().inflate(R.layout.menu_inventory, null);
        final EditText et_count = (EditText) v.findViewById(R.id.et_count);
        // 询问是否删除
        Builder dialog = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
        dialog.setMessage("修改货品" + goodsCode + "的数量 :");
        et_count.setText(count);
        et_count.setSelection(count.length());
        dialog.setView(v);
        // 相当于点击确认按钮
        dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String count = et_count.getText().toString();
                if ("".equals(count.trim())) {
                    count = "0";
                }
                if (Integer.parseInt(count) < 1) {
                    dataList.remove(position);
                } else {
                    for (int i = 0; i < dataList.size(); i++) {
                        Map map = dataList.get(i);
                        if (map.get("GoodsID").equals(upGoodsId) && map.get("ColorID").equals(upColorId) && map.get("SizeID").equals(upSizeId)) {
                            map.put("Quantity", count);
                        }
                    }
                }
                updateStorageInCount(upGoodsId, upColorId, upSizeId, count);
                // 动态更新ListView
                oddAdapter.notifyDataSetChanged();
                // 计算价格
                countTotal();
            }
        });
        // 相当于点击取消按钮
        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {}
        });
        dialog.create();
        dialog.show();
        return true;
    }

}
