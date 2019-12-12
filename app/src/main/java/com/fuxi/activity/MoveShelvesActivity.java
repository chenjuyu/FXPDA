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
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import com.fuxi.dao.StockRemoveDao;
import com.fuxi.main.R;
import com.fuxi.util.AppManager;
import com.fuxi.util.Logger;
import com.fuxi.util.LoginParameterUtil;
import com.fuxi.util.NetUtil;
import com.fuxi.util.SerializableMap;
import com.fuxi.vo.RequestVo;
import com.fuxi.vo.StockRemove;

/**
 * Title: MoveShelvesActivity Description: 移位活动界面
 * 
 * @author LYJ
 * 
 */
public class MoveShelvesActivity extends BaseWapperActivity {

    private static final String TAG = "ShelvesInActivity";
    private static final String PATH = "/shelvesIn.do?saveStorageIn";
    private static final String MoveShelvesOut = "/shelvesOut.do?scanningShelvesOut";
    private static final String AnalyticalBarcode = "/select.do?getGoodsByBarcode";
    private static final String judgeWarehouseType = "/storageQuery.do?judgeWarehouseType";

    private RelativeLayout llMain;
    private LinearLayout llShelvesInCount;
    private LinearLayout llCount;
    private LinearLayout llSplit1;
    private EditText etWarehouseId;
    private EditText etShelvesBarcode;
    private EditText etGoodsBarcode;
    private EditText etQty;
    private TextView tvQty;
    // private TextView tvAdd;
    private TextView tvClean;
    private TextView tvShelvesIn;
    private EditText etSupplierCode;
    private EditText etGoods;
    private EditText etColor;
    private EditText etSize;
    private EditText etSum;
    private TextView tvSave;

    private List<SerializableMap> completeList = new ArrayList<SerializableMap>();
    private StockRemoveDao stockRemoveDao = new StockRemoveDao(this);
    private StockRemove sr = null;

    private String stockId; // 进仓单ID
    private String stockNo; // 进仓单号
    private String departmentId; // 进仓单对应的部门
    private String goodsBarcode; // 货品条码
    private String goodsId;
    private String colorId;
    private String sizeId;
    private String goodsCode;
    private String goodsName;
    private String supplierCode;
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
            case R.id.shelvesIn:
                Intent intent = new Intent(MoveShelvesActivity.this, StockOutRemoveDetailActivity.class);
                intent.putExtra("type", type);
                startActivity(intent);
                finish();
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
     * 保存移位记录
     */
    private void save() {// 关联
        // 非空判断
        String storage = etShelvesBarcode.getText().toString().trim();
        String qty = etQty.getText().toString().trim();
        if (storage == null || storage.isEmpty() || storageId == null || storageId.isEmpty()) {
            etShelvesBarcode.setEnabled(true);
            Toast.makeText(MoveShelvesActivity.this, "仓位不能为空,请录入上架货品对应的仓位", Toast.LENGTH_SHORT).show();
            return;
        }
        if (goodsBarcode == null || goodsBarcode.isEmpty() || goodsId == null || goodsId.isEmpty() || colorId == null || goodsId.isEmpty() || sizeId == null || sizeId.isEmpty()) {
            Toast.makeText(MoveShelvesActivity.this, "请录入要上架的货品", Toast.LENGTH_SHORT).show();
            return;
        }
        if (qty == null || qty.isEmpty()) {
            Toast.makeText(MoveShelvesActivity.this, "货品的上架数量不能为空或小于1", Toast.LENGTH_SHORT).show();
            return;
        }
        // 保存上架
        save(goodsBarcode, qty, "");
    }

    /**
     * 连接服务器保存移位记录
     * 
     * @param barcode
     * @param qty
     * @param memo
     */
    private void save(final String barcode, final String qty, String memo) {
        // 保存到服务器
        RequestVo vo = new RequestVo();
        if ("移出".equals(type)) {
            vo.requestUrl = MoveShelvesOut;
        } else {
            vo.requestUrl = PATH;
        }
        vo.context = this;
        HashMap map = new HashMap();
        map.put("stockNo", stockNo);
        map.put("storageId", storageId);
        map.put("departmentId", departmentId);
        map.put("type", type);
        if (!"移出".equals(type)) {
            map.put("barcode", barcode);
            // 判断数量的合法性
            if (Integer.parseInt(qty) > sr.getQuantity()) {
                Toast.makeText(MoveShelvesActivity.this, "货品上架数量不能大于现有货品数量" + sr.getQuantity(), Toast.LENGTH_LONG).show();
                etQty.setText(String.valueOf(sr.getQuantity()));
                etQty.setSelection(String.valueOf(sr.getQuantity()).length());
                return;
            } else if (Integer.parseInt(qty) == 0) {
                Toast.makeText(MoveShelvesActivity.this, "货品上架数量不能为0", Toast.LENGTH_LONG).show();
                etQty.setText(String.valueOf(sr.getQuantity()));
                etQty.setSelection(String.valueOf(sr.getQuantity()).length());
                return;
            }
        }
        map.put("qty", qty);
        map.put("memo", memo);
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
                        Toast.makeText(MoveShelvesActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                        if ("移出".equals(type)) {
                            // 保存已经下架的货品
                            saveCompleteGoods(barcode, supplierCode, goodsCode, goodsName, color, size, storage, storageId, goodsId, colorId, sizeId, Integer.parseInt(qty));
                            getShelvesInTotalCount();
                            // 锁定库位
                            etShelvesBarcode.setEnabled(false);
                            // 清空货品信息
                            etSupplierCode.setText(null);
                            etGoods.setText(null);
                            etColor.setText(null);
                            etSize.setText(null);
                            goodsBarcode = null;
                            reset();
                        }
                        if ("移入".equals(type)) {
                            if (null != sr) {
                                if (sr.getQuantity() == Integer.parseInt(qty)) {
                                    stockRemoveDao.delete(String.valueOf(sr.getId()));
                                    Intent intent = new Intent(MoveShelvesActivity.this, StockOutRemoveDetailActivity.class);
                                    intent.putExtra("type", type);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    int total = sr.getQuantity() - Integer.parseInt(qty);
                                    if (total > 0) {
                                        stockRemoveDao.update(new StockRemove(sr.getId(), total));
                                        sr = stockRemoveDao.find(new StockRemove(sr.getDeptId(), sr.getStorageId(), sr.getGoodsId(), sr.getColorId(), sr.getSizeId()));
                                        etQty.setText(String.valueOf(sr.getQuantity()));
                                        etQty.setSelection(String.valueOf(sr.getQuantity()).length());
                                    } else {
                                        stockRemoveDao.delete(String.valueOf(sr.getId()));
                                        Intent intent = new Intent(MoveShelvesActivity.this, StockOutRemoveDetailActivity.class);
                                        intent.putExtra("type", type);
                                        startActivity(intent);
                                        finish();
                                    }
                                }
                            }
                        }
                    } else {
                        if (!flag && !"移出".equals(type)) {
                            Toast.makeText(MoveShelvesActivity.this, "此仓位的货架剩余数量不足,请选择其它仓位", Toast.LENGTH_SHORT).show();
                        } else {
                            if ("移出".equals(type)) {
                                Toast.makeText(MoveShelvesActivity.this, "货品下架数大于此仓位的货品剩余数量,请修改下架数量或选择其它仓位", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MoveShelvesActivity.this, "保存失败", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(MoveShelvesActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 显示货品待上架数量
     */
    private void getShelvesInTotalCount() {
        int total = stockRemoveDao.getStockRemoveTotalCount();
        if (total == 0) {
            tvShelvesIn.setVisibility(View.INVISIBLE);
        } else {
            tvShelvesIn.setVisibility(View.VISIBLE);
        }
        etSum.setText(String.valueOf(total));
    }

    /**
     * 保存已经上架的货品
     * 
     * @param barcode
     * @param supplierCode
     * @param goodsCode
     * @param goodsName
     * @param color
     * @param size
     * @param storage
     * @param storageId
     * @param goodsId
     * @param colorId
     * @param sizeId
     * @param qtyStr
     */
    private void saveCompleteGoods(String barcode, String supplierCode, String goodsCode, String goodsName, String color, String size, String storage, String storageId, String goodsId, String colorId, String sizeId, int qtyStr) {
        boolean flag = false;
        for (int i = 0; i < completeList.size(); i++) {
            Map<String, Object> temp = completeList.get(i).getMap();
            String storageID = String.valueOf(temp.get("storageId"));
            String goodsID = String.valueOf(temp.get("goodsId"));
            String colorID = String.valueOf(temp.get("colorId"));
            String sizeID = String.valueOf(temp.get("sizeId"));
            String deptID = String.valueOf(temp.get("deptId"));
            int qty = Integer.parseInt(String.valueOf(temp.get("Quantity")));
            if (deptID.equals(departmentId) && storageID.equals(storageId) && goodsID.equals(goodsId) && colorID.equals(colorId) && sizeID.equals(sizeId)) {
                qty = qty + qtyStr;
                temp.put("Quantity", String.valueOf(qty));
                StockRemove sr = new StockRemove(departmentId, storageID, goodsID, colorID, sizeID);
                sr = stockRemoveDao.find(sr);
                if (null != sr) {
                    sr.setQuantity(qty);
                    stockRemoveDao.update(sr);
                }
                flag = true;
            }
        }
        if (!flag) {
            String deptName = etWarehouseId.getText().toString();
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("Barcode", barcode);
            map.put("Code", goodsCode);
            map.put("SupplierCode", supplierCode);
            map.put("Name", goodsName);
            map.put("Color", color);
            map.put("Size", size);
            map.put("Storage", storage);
            map.put("storageId", storageId);
            map.put("goodsId", goodsId);
            map.put("colorId", colorId);
            map.put("sizeId", sizeId);
            map.put("Quantity", String.valueOf(qtyStr));
            map.put("deptId", departmentId);
            map.put("deptName", deptName);
            SerializableMap sm = new SerializableMap();
            sm.setMap(map);
            completeList.add(sm);
            StockRemove sr = new StockRemove(departmentId, deptName, barcode, supplierCode, goodsCode, goodsName, color, size, storage, storageId, goodsId, colorId, sizeId, qtyStr);
            stockRemoveDao.insert(sr);
        }
    }

    /**
     * 解析条码
     */
    private void analyticalBarcode() {
        if (departmentId == null || departmentId.isEmpty()) {
            etWarehouseId.setEnabled(true);
            etGoodsBarcode.setText(null);
            Toast.makeText(MoveShelvesActivity.this, "请先选择仓库", Toast.LENGTH_SHORT).show();
            return;
        }
        if (storage == null || storage.isEmpty() || storageId == null || storageId.isEmpty()) {
            etShelvesBarcode.setEnabled(true);
            etShelvesBarcode.requestFocus();
            etGoodsBarcode.setText(null);
            Toast.makeText(MoveShelvesActivity.this, "仓位不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        String barcode = etGoodsBarcode.getText().toString().trim();
        if (barcode == null || barcode.isEmpty()) {
            reset();
            Toast.makeText(MoveShelvesActivity.this, "请录入要上架的货品", Toast.LENGTH_SHORT).show();
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
                        supplierCode = obj.getString("SupplierCode");
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
                    } else {
                        etGoodsBarcode.requestFocus();
                        etGoodsBarcode.selectAll();
                        Toast.makeText(MoveShelvesActivity.this, "不存在此条码", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(MoveShelvesActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 保存后重置方法(重置扫码区域)
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
            etWarehouseId.setEnabled(true);
            etShelvesBarcode.setText(null);
            Toast.makeText(MoveShelvesActivity.this, "请先选择仓库", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(MoveShelvesActivity.this, "仓位编码错误或不存在此仓位", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(MoveShelvesActivity.this, "系统错误", Toast.LENGTH_LONG).show();
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
                        departmentId = LoginParameterUtil.deptId;
                        etWarehouseId.setText(LoginParameterUtil.deptName);
                    }
                } catch (Exception e) {
                    Toast.makeText(MoveShelvesActivity.this, "系统错误", Toast.LENGTH_LONG).show();
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
                        etWarehouseId.setEnabled(true);
                        etShelvesBarcode.setText(null);
                        Toast.makeText(MoveShelvesActivity.this, "请先选择仓库", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Intent intent = new Intent(MoveShelvesActivity.this, SelectActivity.class);
                        intent.putExtra("selectType", "selectStorageByInner");
                        intent.putExtra("param", departmentId);
                        startActivityForResult(intent, R.id.shelves_barcode);
                        overridePendingTransition(R.anim.activity_open, 0);
                    }
                    break;
                case R.id.goods_barcode:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Intent intent = new Intent(MoveShelvesActivity.this, SelectActivity.class);
                        intent.putExtra("selectType", "selectBarcode");
                        startActivityForResult(intent, R.id.goods_barcode);
                        overridePendingTransition(R.anim.activity_open, 0);
                    }
                    break;
                case R.id.warehouseId:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Intent intent = new Intent(MoveShelvesActivity.this, SelectActivity.class);
                        intent.putExtra("selectType", "selectWarehouseHasStorage");
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
            case R.id.warehouseId:
                if (resultCode == 1) {
                    etWarehouseId.setText(data.getStringExtra("Name"));
                    departmentId = data.getStringExtra("DepartmentID");
                    etShelvesBarcode.setText(null);
                    storageId = null;
                    etShelvesBarcode.requestFocus();
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
     * 点击返回时的操作
     */
    private void back() {
        // 询问是否错误操作
        Builder dialog = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
        dialog.setTitle("提示");
        if ("移入".equals(type)) {
            dialog.setMessage("确定返回并退出货品上架操作吗？");
        } else {
            dialog.setMessage("确定返回并退出货品下架操作吗？");
        }
        // 相当于点击确认按钮
        dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if ("移入".equals(type)) {
                    Intent intent = new Intent(MoveShelvesActivity.this, StockOutRemoveDetailActivity.class);
                    intent.putExtra("type", type);
                    startActivity(intent);
                    finish();
                } else if ("移出".equals(type)) {
                    finish();
                }
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
        setContentView(R.layout.activity_move_shelves);
        setTitle("移出下架");
    }

    @Override
    protected void processLogic() {
        if (NetUtil.hasNetwork(getApplicationContext())) {
            if (LoginParameterUtil.online) {
                // 初始化
                etQty.setText("1");
                llMain.removeView(llShelvesInCount);
                // 设置默认部门(必须含仓位)
                if (LoginParameterUtil.hasStorage) {
                    judgeWarehouseType();
                }
                Bundle bundle = this.getIntent().getExtras();
                if (null != bundle) {
                    type = bundle.getString("type");
                    if ("移入".equals(type)) {
                        goodsId = bundle.getString("goodsId");
                        colorId = bundle.getString("colorId");
                        sizeId = bundle.getString("sizeId");
                        goodsBarcode = bundle.getString("barcode");
                        etGoodsBarcode.setText(bundle.getString("barcode"));
                        goodsName = bundle.getString("goodsName");
                        goodsCode = bundle.getString("goodsCode");
                        supplierCode = bundle.getString("supplierCode");
                        color = bundle.getString("color");
                        size = bundle.getString("size");
                        etQty.setText(bundle.getString("qty"));
                        // 设置默认部门
                        departmentId = bundle.getString("deptId");
                        etWarehouseId.setText(bundle.getString("deptName"));
                        etWarehouseId.setFocusable(false);
                        sr = stockRemoveDao.find(new StockRemove(bundle.getString("deptId"), bundle.getString("storageId"), goodsId, colorId, sizeId));
                    }
                }
                if ("移入".equals(type)) {
                    llMain.removeView(llSplit1);
                    setTitle("移进上架");
                    etGoodsBarcode.setEnabled(false);
                    etGoodsBarcode.setFocusable(false);
                    etGoods.setText(goodsName + "(" + goodsCode + ")");
                    etSupplierCode.setText(supplierCode);
                    etColor.setText(color);
                    etSize.setText(size);
                    // llCount.removeView(tvAdd);
                } else if ("移出".equals(type)) {
                    setTitle("移出下架");
                    llMain.addView(llShelvesInCount);
                    getShelvesInTotalCount();
                    // setHeadRightVisibility(View.VISIBLE);
                    // setHeadRightText(R.string.detailed);
                    tvQty.setText("下架数量");
                }
                if ("移入".equals(type)) {
                    etWarehouseId.setEnabled(false);
                    etWarehouseId.setCompoundDrawables(null, null, null, null);
                } else {
                    etWarehouseId.setEnabled(true);
                    Drawable drawable = getResources().getDrawable(R.drawable.input_bg);
                    drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight()); // 此为必须写的
                    etWarehouseId.setCompoundDrawables(null, null, drawable, null);
                }
            } else {
                AlertDialog.Builder dialog = new AlertDialog.Builder(MoveShelvesActivity.this, AlertDialog.THEME_HOLO_LIGHT);
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
            AlertDialog.Builder dialog = new AlertDialog.Builder(MoveShelvesActivity.this, AlertDialog.THEME_HOLO_LIGHT);
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

    // 右上角选项
    // setHeadRightText(R.string.reset);
    // setHeadRightVisibility(View.VISIBLE);

    @Override
    protected void setListener() {
        TouchListener tl = new TouchListener();
        // tvAdd.setOnClickListener(this);
        tvClean.setOnClickListener(this);
        tvShelvesIn.setOnClickListener(this);
        tvSave.setOnClickListener(this);
        etShelvesBarcode.setOnTouchListener(tl);
        etWarehouseId.setOnTouchListener(tl);
        etShelvesBarcode.setOnEditorActionListener(new ShelvesBarcodeActionListener());
        // etGoodsBarcode.setOnTouchListener(tl);
        etGoodsBarcode.setOnEditorActionListener(new BarcodeActionListener());
    }

    @Override
    protected void findViewById() {
        llShelvesInCount = (LinearLayout) findViewById(R.id.ll_shelvesInCount);
        llSplit1 = (LinearLayout) findViewById(R.id.split1);
        llCount = (LinearLayout) findViewById(R.id.ll_count);
        llMain = (RelativeLayout) findViewById(R.id.ll_main);
        etWarehouseId = (EditText) findViewById(R.id.warehouseId);
        etShelvesBarcode = (EditText) findViewById(R.id.shelves_barcode);
        etGoodsBarcode = (EditText) findViewById(R.id.goods_barcode);
        etQty = (EditText) findViewById(R.id.qty);
        // tvAdd = (TextView) findViewById(R.id.add);
        tvClean = (TextView) findViewById(R.id.clean);
        etSupplierCode = (EditText) findViewById(R.id.supplierCode);
        etGoods = (EditText) findViewById(R.id.goods);
        etColor = (EditText) findViewById(R.id.color);
        etSize = (EditText) findViewById(R.id.size);
        etSum = (EditText) findViewById(R.id.sum);
        tvShelvesIn = (TextView) findViewById(R.id.shelvesIn);
        tvSave = (TextView) findViewById(R.id.save);
        tvQty = (TextView) findViewById(R.id.tv_qty);
    }

}
