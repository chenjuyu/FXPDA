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
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
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
 * Title: ShelvesInActivity Description: 仓位上架活动界面
 * 
 * @author LYJ
 * 
 */
public class ShelvesInActivity extends BaseWapperActivity {

    private static final String TAG = "ShelvesInActivity";
    private static final String PATH = "/shelvesIn.do?saveStorageIn";
    private static final String StockDetail = "/shelvesIn.do?getStockDetail";
    private static final String CheckBarcodeAndQty = "/shelvesIn.do?checkBarcodeAndQty";
    private static final String GetDifferentialStockDetail = "/shelvesIn.do?getDifferentialStockDetail";
    private static final String GetUnShelvesInTotal = "/shelvesIn.do?getUnShelvesInTotal";
    private static final String AnalyticalBarcode = "/select.do?getGoodsByBarcode";
    private static final String quickShelvesIn = "/shelvesIn.do?quickStorageIn";
    private static final String checkUniqueOperation = "/shelvesIn.do?checkUniqueOperation";
    private static final String ReleasingResources = "/shelvesIn.do?releasingResources";

    private RelativeLayout llMain;
    private LinearLayout llShelvesInCount;
    private LinearLayout llSplit1;
    private EditText etStockNo;
    private EditText etShelvesBarcode;
    private EditText etGoodsBarcode;
    private EditText etQty;
    private TextView tvStockNo;
    // private TextView tvAdd;
    private TextView tvClean;
    private TextView tvShelvesIn;
    private EditText etSupplierCode;
    private EditText etGoods;
    private EditText etColor;
    private EditText etSize;
    private EditText etSum;
    private TextView tvSave;
    private TextView tvQuickShelvesIn;

    private List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
    private OddAdapter oddAdapter;

    private int relationMovein;// 上架单是否关联进仓单(0:不关联,1:关联)
    private int total;
    private String stockId; // 进仓单ID
    private String stockNo; // 进仓单号
    private String departmentId; // 进仓单对应的部门
    private String goodsBarcode; // 货品条码
    private String goodsId;
    private String colorId;
    private String sizeId;
    private String goodsCode;
    private String goodsName;
    private String color;
    private String size;
    private String storageId; // 仓位ID
    private String storage; // 仓位ID
    private String type; // 上架类型

    @Override
    public void onClick(View v) {
        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
        }
        switch (v.getId()) {
            case R.id.add:
                String qtyStr = String.valueOf(etQty.getText());
                if (null == qtyStr || qtyStr.isEmpty()) {
                    qtyStr = "0";
                }
                int qty = Integer.parseInt(qtyStr);
                qty += 1;
                etQty.setText(String.valueOf(qty));
                break;
            case R.id.save:
                save();
                break;
            case R.id.quickShelvesIn:
                quickShelvesInOrNo();
                break;
            case R.id.shelvesIn:
                if (relationMovein == 1) {
                    getDifferentialData();
                }
                break;
            case R.id.clean:
                etShelvesBarcode.setText(null);
                storageId = null;
                etShelvesBarcode.setEnabled(true);
                etShelvesBarcode.requestFocus();
                break;
            default:
                break;
        }
    }

    /**
     * 上架保存方法
     */
    private void save() {// 关联
        // 非空判断
        if (relationMovein == 1) {
            if (stockNo == null || stockNo.isEmpty()) {
                Toast.makeText(ShelvesInActivity.this, "关联单号上架时,进仓单号不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        String storage = etShelvesBarcode.getText().toString().trim();
        String qty = etQty.getText().toString().trim();
        if (storage == null || storage.isEmpty() || storageId == null || storageId.isEmpty()) {
            etShelvesBarcode.setEnabled(true);
            Toast.makeText(ShelvesInActivity.this, "仓位不能为空,请录入上架货品对应的仓位", Toast.LENGTH_SHORT).show();
            return;
        }
        if (goodsBarcode == null || goodsBarcode.isEmpty() || goodsId == null || goodsId.isEmpty() || colorId == null || goodsId.isEmpty() || sizeId == null || sizeId.isEmpty()) {
            Toast.makeText(ShelvesInActivity.this, "请录入要上架的货品", Toast.LENGTH_SHORT).show();
            return;
        }
        if (qty == null || qty.isEmpty()) {
            Toast.makeText(ShelvesInActivity.this, "货品的上架数量不能为空或小于1", Toast.LENGTH_SHORT).show();
            return;
        }
        if (relationMovein == 1) {// 关联单号
            // 关联单号上架时检查货品的数量
            checkQuantity(qty, "");
        } else {
            // 保存上架
            save(goodsBarcode, qty, "");
        }
    }

    /**
     * 检查货品上架数量是否大于单据中货品对应的数据(关联单号)
     * 
     * @param qty
     * @param memo
     */
    private void checkQuantity(String qty, String memo) {
        if (Integer.parseInt(qty) > total) {
            Builder dialog = new AlertDialog.Builder(ShelvesInActivity.this, AlertDialog.THEME_HOLO_LIGHT);
            dialog.setTitle("提示");
            dialog.setMessage("已上架货品数量不能大于进仓单 " + stockNo + " 中对应的货品的数量 " + total + "");
            // 相当于点击确认按钮
            dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // 保存上架
                    etQty.requestFocus();
                    etQty.selectAll();
                }
            });
            dialog.setCancelable(false);
            dialog.create();
            dialog.show();
        } else {
            // 保存上架
            save(goodsBarcode, qty, memo);
        }
    }

    /**
     * 连接服务器保存货品上架记录
     * 
     * @param barcode
     * @param qty
     * @param memo
     */
    private void save(final String barcode, final String qty, String memo) {
        // 保存到服务器
        RequestVo vo = new RequestVo();
        vo.requestUrl = PATH;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("stockNo", stockNo);
        map.put("storageId", storageId);
        map.put("departmentId", departmentId);
        map.put("type", type);
        map.put("barcode", barcode);
        map.put("qty", qty);
        map.put("memo", memo);
        map.put("goodsId", goodsId);
        map.put("colorId", colorId);
        map.put("sizeId", sizeId);
        vo.requestDataMap = map;
        tvSave.setEnabled(false);
        tvSave.setBackgroundColor(Color.GRAY);
        super.getDataFromServer(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    tvSave.setEnabled(true);
                    tvSave.setBackground(ShelvesInActivity.this.getResources().getDrawable(R.drawable.custom_button));
                    if (retObj == null) {
                        return;
                    }
                    JSONObject rs = retObj.getJSONObject("attributes");
                    boolean flag = rs.getBoolean("flag");
                    int count = rs.getInt("count");
                    if (retObj.getBoolean("success") && count > 0 && flag) {
                        Toast.makeText(ShelvesInActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                        // 锁定库位
                        etShelvesBarcode.setEnabled(false);
                        // 清空货品信息
                        etSupplierCode.setText(null);
                        etGoods.setText(null);
                        etColor.setText(null);
                        etSize.setText(null);
                        goodsBarcode = null;
                        reset();
                        if (relationMovein == 1) {
                            getUnShelvesInTotal();
                        }
                    } else {
                        Toast.makeText(ShelvesInActivity.this, "保存失败", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    if ("货品上架失败".equals(e.getMessage())) {
                        Toast.makeText(ShelvesInActivity.this, "请勿重复上架", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ShelvesInActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                        Logger.e(TAG, e.getMessage());
                    }
                }
            }
        });
    }

    /**
     * 检测条码是否在进仓单内(关联进仓单时)
     */
    private void quickShelvesIn() {
        RequestVo vo = new RequestVo();
        vo.requestUrl = quickShelvesIn;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("stockNo", stockNo);
        map.put("type", type);
        map.put("departmentId", departmentId);
        map.put("useLastTimePosition", String.valueOf(LoginParameterUtil.useLastTimePosition));
        vo.requestDataMap = map;
        tvQuickShelvesIn.setEnabled(false);
        tvQuickShelvesIn.setBackgroundColor(Color.GRAY);
        super.getDataFromServer(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    if (retObj.getBoolean("success")) {
                        Toast.makeText(ShelvesInActivity.this, "进仓单 " + stockNo + " 已完成快速上架操作", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        tvQuickShelvesIn.setEnabled(true);
                        tvQuickShelvesIn.setBackground(ShelvesInActivity.this.getResources().getDrawable(R.drawable.upload_button));
                        Toast.makeText(ShelvesInActivity.this, "快速上架操作失败", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(ShelvesInActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 快速上架
     */
    private void quickShelvesInOrNo() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(ShelvesInActivity.this, AlertDialog.THEME_HOLO_LIGHT);
        dialog.setTitle("提示");
        dialog.setMessage("确认要快速上架进仓单 " + stockNo + " 中的货品吗?");
        // 相当于点击确认按钮
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                quickShelvesIn();
            }
        });
        // 相当于点击取消按钮
        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {}
        });
        dialog.setCancelable(false);
        dialog.create();
        dialog.show();
    }

    /**
     * 检测条码是否在进仓单内(关联进仓单时)
     * 
     * @param stockId
     * @param goodsId
     * @param colorId
     * @param sizeId
     */
    private void checkBarcode(String stockId, String goodsId, String colorId, String sizeId) {
        RequestVo vo = new RequestVo();
        vo.requestUrl = CheckBarcodeAndQty;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("stockNo", stockNo);
        map.put("goodsId", goodsId);
        map.put("colorId", colorId);
        map.put("sizeId", sizeId);
        map.put("departmentId", departmentId);
        map.put("useLastTimePosition", String.valueOf(LoginParameterUtil.useLastTimePosition));
        vo.requestDataMap = map;
        super.getDataFromServer(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    JSONObject obj = retObj.getJSONObject("obj");
                    int count = obj.getInt("count");
                    int quantity = obj.getInt("quantity");
                    JSONArray array = null;
                    if (!obj.isNull("storageList")) {
                        array = obj.getJSONArray("storageList");
                    }
                    if (retObj.getBoolean("success") && count > 0) {
                        total = quantity;
                        if (LoginParameterUtil.useLastTimePosition) {
                            // 显示最近一次上架的仓位
                            if (array != null && array.length() > 0) {
                                JSONObject jsonObject = array.getJSONObject(0);
                                storageId = jsonObject.getString("StorageID");
                                storage = jsonObject.getString("Storage");
                                etShelvesBarcode.setText(storage);
                            }
                        }
                        if (LoginParameterUtil.autoSave) {
                            // 模拟保存按钮的点击事件
                            tvSave.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                            tvSave.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
                        }
                    } else {
                        Toast.makeText(ShelvesInActivity.this, "此货品条码不在进仓单 " + stockNo + " 中", Toast.LENGTH_SHORT).show();
                        etGoodsBarcode.requestFocus();
                        etGoodsBarcode.selectAll();
                        // 清空货品信息
                        etSupplierCode.setText(null);
                        etGoods.setText(null);
                        etColor.setText(null);
                        etSize.setText(null);
                        goodsBarcode = null;
                    }
                } catch (Exception e) {
                    Toast.makeText(ShelvesInActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 解析条码
     */
    private void analyticalBarcode() {
        if (departmentId == null || departmentId.isEmpty()) {
            etStockNo.setEnabled(true);
            etGoodsBarcode.setText(null);
            Toast.makeText(ShelvesInActivity.this, "请先选择仓库", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!LoginParameterUtil.useLastTimePosition && (storage == null || storage.isEmpty() || storageId == null || storageId.isEmpty())) {
            etShelvesBarcode.setEnabled(true);
            etShelvesBarcode.requestFocus();
            etGoodsBarcode.setText(null);
            Toast.makeText(ShelvesInActivity.this, "仓位不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        String barcode = etGoodsBarcode.getText().toString().trim();
        if (barcode == null || barcode.isEmpty()) {
            reset();
            Toast.makeText(ShelvesInActivity.this, "请录入要上架的货品", Toast.LENGTH_SHORT).show();
            return;
        }
        goodsBarcode = barcode;
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
                        etGoods.setText(obj.getString("GoodsName") + "(" + obj.getString("GoodsCode") + ")");
                        etSupplierCode.setText(obj.getString("SupplierCode"));
                        etColor.setText(obj.getString("Color"));
                        etSize.setText(obj.getString("Size"));
                        etGoodsBarcode.requestFocus();
                        etGoodsBarcode.selectAll();
                        // 关联单号的时候检查条码
                        if (relationMovein == 1) {
                            checkBarcode(stockId, goodsId, colorId, sizeId);
                        }
                    } else {
                        etGoodsBarcode.requestFocus();
                        etGoodsBarcode.selectAll();
                        Toast.makeText(ShelvesInActivity.this, "不存在此条码", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(ShelvesInActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 保存后重置方法
     */
    private void reset() {
        etGoodsBarcode.setText(null);
        etGoodsBarcode.requestFocus();
        etQty.setText("1");
    }

    /**
     * 查询仓位
     */
    private void searchStorage() {
        if (departmentId == null || departmentId.isEmpty()) {
            etStockNo.setEnabled(true);
            etShelvesBarcode.setText(null);
            Toast.makeText(ShelvesInActivity.this, "请先选择仓库", Toast.LENGTH_SHORT).show();
            return;
        }
        RequestVo vo = new RequestVo();
        vo.requestUrl = "/select.do?getStorage";
        vo.context = this;
        HashMap map = new HashMap();
        map.put("currPage", String.valueOf(1));
        map.put("param", etShelvesBarcode.getText().toString());
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
                        etShelvesBarcode.setText(obj.getString("Name"));
                        etShelvesBarcode.setSelection(storage.length());
                        storageId = obj.getString("StorageID");
                        // 货品条码获取焦点
                        etGoodsBarcode.requestFocus();
                    } else {
                        etShelvesBarcode.requestFocus();
                        etShelvesBarcode.selectAll();
                        Toast.makeText(ShelvesInActivity.this, "仓位编码错误或不存在此仓位", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(ShelvesInActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    private class TouchListener implements OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (v.getId()) {
                case R.id.shelves_barcode:
                    if (departmentId == null || departmentId.isEmpty()) {
                        etStockNo.setEnabled(true);
                        etShelvesBarcode.setText(null);
                        Toast.makeText(ShelvesInActivity.this, "请先选择仓库", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Intent intent = new Intent(ShelvesInActivity.this, SelectActivity.class);
                        intent.putExtra("selectType", "selectStorageByInner");
                        intent.putExtra("param", departmentId);
                        startActivityForResult(intent, R.id.shelves_barcode);
                        overridePendingTransition(R.anim.activity_open, 0);
                    }
                    break;
                case R.id.goods_barcode:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Intent intent = new Intent(ShelvesInActivity.this, SelectActivity.class);
                        intent.putExtra("selectType", "selectBarcode");
                        startActivityForResult(intent, R.id.goods_barcode);
                        overridePendingTransition(R.anim.activity_open, 0);
                    }
                    break;
                case R.id.stock_no:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Intent intent = new Intent(ShelvesInActivity.this, SelectActivity.class);
                        intent.putExtra("selectType", "selectWarehouseHasStorage");
                        startActivityForResult(intent, R.id.stock_no);
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
            case R.id.shelves_barcode:
                if (resultCode == 1) {
                    storage = data.getStringExtra("Name");
                    etShelvesBarcode.setText(data.getStringExtra("Name"));
                    etShelvesBarcode.setSelection(storage.length());
                    storageId = data.getStringExtra("StorageID");
                    // 货品条码获取焦点
                    etGoodsBarcode.requestFocus();
                }
                break;
            case R.id.goods_barcode:
                if (resultCode == 1) {
                    etGoodsBarcode.setText(data.getStringExtra("Name"));
                    analyticalBarcode();
                    // 货品条码获取焦点
                    etGoodsBarcode.requestFocus();
                }
                break;
            case R.id.stock_no:
                if (resultCode == 1) {
                    etStockNo.setText(data.getStringExtra("Name"));
                    departmentId = data.getStringExtra("DepartmentID");
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
                etGoodsBarcode.requestFocus();
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
                etGoodsBarcode.requestFocus();
            }
            return false;
        }

    }

    /**
     * 关联单号时限制单号唯一操作
     */
    private void checkUniqueOperation() {
        RequestVo vo = new RequestVo();
        vo.requestUrl = checkUniqueOperation;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("stockNo", stockNo);
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
                        int count = obj.getInt("count");
                        String userName = obj.getString("userName");
                        if (null == userName || "".equals(userName) || "null".equalsIgnoreCase(userName)) {
                            userName = null;
                        }
                        if (count > 0) {
                            AlertDialog.Builder dialog = new AlertDialog.Builder(ShelvesInActivity.this, AlertDialog.THEME_HOLO_LIGHT);
                            dialog.setTitle("提示");
                            if (null != userName) {
                                dialog.setMessage("操作员 " + userName + " 正在操作 " + stockNo + " 单据,请选择其它单据上架");
                            } else {
                                addData();
                            }
                            // 相当于点击确认按钮
                            dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(ShelvesInActivity.this, StockInActivity.class);
                                    intent.putExtra("type", type);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                            dialog.setCancelable(false);
                            dialog.create();
                            dialog.show();
                        } else {
                            addData();
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(ShelvesInActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 关联单号时获取对应单号的数据
     */
    private void addData() {
        RequestVo vo = new RequestVo();
        vo.requestUrl = StockDetail;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("stockNo", stockNo);
        vo.requestDataMap = map;
        super.getDataFromServer(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    if (retObj.getBoolean("success")) {
                        if (relationMovein == 1) {
                            getUnShelvesInTotal();
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(ShelvesInActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 释放锁定的仓位资源
     * 
     * @param stockNo
     */
    private void releasingResources(final String stockNo) {
        RequestVo vo = new RequestVo();
        vo.requestUrl = ReleasingResources;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("stockNo", stockNo);
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
                        if (relationMovein == 1) {
                            Intent intent = new Intent(ShelvesInActivity.this, StockInActivity.class);
                            intent.putExtra("type", type);
                            startActivity(intent);
                            finish();
                        } else {
                            finish();
                        }
                    } else {
                        throw new Exception();
                    }
                } catch (Exception e) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(ShelvesInActivity.this, AlertDialog.THEME_HOLO_LIGHT);
                    dialog.setTitle("提示");
                    dialog.setMessage("当前锁定单据 " + stockNo + " 释放失败,请重试");
                    // 相当于点击确认按钮
                    dialog.setPositiveButton("点击重试", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (relationMovein == 1) {
                                Intent intent = new Intent(ShelvesInActivity.this, StockInActivity.class);
                                intent.putExtra("type", type);
                                startActivity(intent);
                                finish();
                            } else {
                                finish();
                            }
                        }
                    });
                    dialog.setCancelable(false);
                    dialog.create();
                    dialog.show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 点击返回后的操作
     */
    private void back() {
        // 询问是否错误操作
        Builder dialog = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
        dialog.setTitle("系统提示");
        dialog.setMessage("确定返回并退出货品上架操作吗？");
        // 相当于点击确认按钮
        dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                releasingResources(stockNo);
            }
        });
        // 相当于点击取消按钮
        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {}
        });
        dialog.setCancelable(false);
        dialog.create();
        dialog.show();
    }

    /**
     * 获取差异数据
     */
    private void getDifferentialData() {
        RequestVo vo = new RequestVo();
        vo.requestUrl = GetDifferentialStockDetail;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("stockNo", stockNo);
        vo.requestDataMap = map;
        super.getDataFromServer(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    if (retObj.getBoolean("success")) {
                        dataList.clear();
                        JSONArray array = retObj.getJSONArray("obj");
                        if (array.length() > 0) {
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject obj = array.getJSONObject(i);
                                Map<String, Object> map = new HashMap<String, Object>();
                                map.put("Barcode", null);
                                map.put("GoodsCode", obj.getString("GoodsCode"));
                                map.put("Color", obj.getString("Color"));
                                map.put("Size", obj.getString("Size"));
                                map.put("Quantity", obj.getInt("Quantity"));
                                dataList.add(map);
                            }
                            showDifferentialData();
                        }
                        // 刷新ListView
                        oddAdapter.refresh();
                    }
                } catch (Exception e) {
                    Toast.makeText(ShelvesInActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 获取未上架的数量(关联单号时)
     */
    private void getUnShelvesInTotal() {
        RequestVo vo = new RequestVo();
        vo.requestUrl = GetUnShelvesInTotal;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("stockNo", stockNo);
        vo.requestDataMap = map;
        super.getDataFromServer(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    if (retObj.getBoolean("success")) {
                        int countSum = retObj.getInt("obj");
                        etSum.setText(String.valueOf(countSum));
                        if (countSum == 0) {
                            AlertDialog.Builder dialog = new AlertDialog.Builder(ShelvesInActivity.this, AlertDialog.THEME_HOLO_LIGHT);
                            dialog.setTitle("提示");
                            dialog.setMessage("单据 " + stockNo + " 已完成上架");
                            // 相当于点击确认按钮
                            dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(ShelvesInActivity.this, PositionManagementActivity.class);
                                    intent.putExtra("type", type);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                            dialog.setCancelable(false);
                            dialog.create();
                            dialog.show();
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(ShelvesInActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 显示差异数据
     */
    private void showDifferentialData() {
        View view = (LinearLayout) getLayoutInflater().inflate(R.layout.activity_stock_out_remove_detail, null);
        // 初始化
        ListView lv_datas = (ListView) view.findViewById(R.id.lv_datas);
        // 显示数据
        oddAdapter = new OddAdapter(this, dataList);
        lv_datas.setAdapter(oddAdapter);
        // 操作提示
        Builder dialog = new AlertDialog.Builder(ShelvesInActivity.this, AlertDialog.THEME_HOLO_LIGHT);
        dialog.setCancelable(false);
        dialog.setTitle("进仓单 " + stockNo + " 未上架货品信息");
        dialog.setView(view);
        // 相当于点击确认按钮
        dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {}
        });
        dialog.create();
        dialog.show();
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
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    protected void loadViewLayout() {
        setContentView(R.layout.activity_shelves_in);
        setTitle("关联进仓单上架");
    }

    @Override
    protected void processLogic() {
        if (NetUtil.hasNetwork(getApplicationContext())) {
            if (LoginParameterUtil.online) {
                // 使用最近一次上架的仓位
                if (LoginParameterUtil.useLastTimePosition) {
                    // 默认仓位情况下不能选择仓位
                    etShelvesBarcode.setEnabled(false);
                    etGoodsBarcode.requestFocus();
                }
                // 初始化
                etQty.setText("1");
                Bundle bundle = this.getIntent().getExtras();
                if (null != bundle) {
                    if (bundle.containsKey("stockId") && bundle.containsKey("stockNo") && bundle.containsKey("departmentId")) {
                        stockId = bundle.getString("stockId");
                        stockNo = bundle.getString("stockNo");
                        departmentId = bundle.getString("departmentId");
                    }
                    type = bundle.getString("type");
                }
                String relationMoveinStr = LoginParameterUtil.relationMovein;
                if (null != relationMoveinStr && !relationMoveinStr.isEmpty()) {
                    relationMovein = Integer.parseInt(relationMoveinStr);
                }
                if (relationMovein == 1) {// 关联
                    if (null == stockId || stockId.isEmpty()) {
                        Intent intent = new Intent(ShelvesInActivity.this, StockInActivity.class);
                        intent.putExtra("type", type);
                        startActivity(intent);
                        finish();
                    } else {
                        // setHeadRightVisibility(View.VISIBLE);
                        // setHeadRightText(R.string.detailed);
                        tvShelvesIn.setText("待上架明细");
                        etStockNo.setText(stockNo);
                        // 获取数据
                        checkUniqueOperation();
                    }
                } else {// 不关联
                        // setHeadRightVisibility(View.INVISIBLE);
                    llMain.removeView(llSplit1);
                    llMain.removeView(llShelvesInCount);
                    setTitle("直接上架");
                    tvStockNo.setText("仓库");
                    etStockNo.setEnabled(true);
                    etStockNo.setHint("请选择仓库");
                    // 设置默认部门(必须含仓位)
                    if (LoginParameterUtil.hasStorage) {
                        departmentId = LoginParameterUtil.deptId;
                        etStockNo.setText(LoginParameterUtil.deptName);
                    }
                }
            } else {
                AlertDialog.Builder dialog = new AlertDialog.Builder(ShelvesInActivity.this, AlertDialog.THEME_HOLO_LIGHT);
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
            AlertDialog.Builder dialog = new AlertDialog.Builder(ShelvesInActivity.this, AlertDialog.THEME_HOLO_LIGHT);
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
        // tvAdd.setOnClickListener(this);
        tvClean.setOnClickListener(this);
        tvShelvesIn.setOnClickListener(this);
        tvSave.setOnClickListener(this);
        tvQuickShelvesIn.setOnClickListener(this);
        etShelvesBarcode.setOnTouchListener(tl);
        if (relationMovein != 1) {
            etStockNo.setOnTouchListener(tl);
        }
        etShelvesBarcode.setOnEditorActionListener(new ShelvesBarcodeActionListener());
        // etGoodsBarcode.setOnTouchListener(tl);
        etGoodsBarcode.setOnEditorActionListener(new BarcodeActionListener());
    }

    @Override
    protected void findViewById() {
        llShelvesInCount = (LinearLayout) findViewById(R.id.ll_shelvesInCount);
        llSplit1 = (LinearLayout) findViewById(R.id.split1);
        llMain = (RelativeLayout) findViewById(R.id.ll_main);
        etStockNo = (EditText) findViewById(R.id.stock_no);
        etShelvesBarcode = (EditText) findViewById(R.id.shelves_barcode);
        etGoodsBarcode = (EditText) findViewById(R.id.goods_barcode);
        etQty = (EditText) findViewById(R.id.qty);
        tvStockNo = (TextView) findViewById(R.id.tv_stock_no);
        // tvAdd = (TextView) findViewById(R.id.add);
        tvClean = (TextView) findViewById(R.id.clean);
        etSupplierCode = (EditText) findViewById(R.id.supplierCode);
        etGoods = (EditText) findViewById(R.id.goods);
        etColor = (EditText) findViewById(R.id.color);
        etSize = (EditText) findViewById(R.id.size);
        etSum = (EditText) findViewById(R.id.sum);
        tvShelvesIn = (TextView) findViewById(R.id.shelvesIn);
        tvSave = (TextView) findViewById(R.id.save);
        tvQuickShelvesIn = (TextView) findViewById(R.id.quickShelvesIn);
    }

}
