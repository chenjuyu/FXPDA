package com.fuxi.activity;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.os.Vibrator;
import android.text.SpannableString;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
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
import com.fuxi.activity.BaseWapperActivity.DataCallback;
import com.fuxi.adspter.BarcodeVerificationAdapter;
import com.fuxi.adspter.BarcodeVerificationDifferentAdapter;
import com.fuxi.main.R;
import com.fuxi.util.AppManager;
import com.fuxi.util.ExcelUtil;
import com.fuxi.util.Logger;
import com.fuxi.util.LoginParameterUtil;
import com.fuxi.util.SpUtil;
import com.fuxi.vo.BarCode;
import com.fuxi.vo.RequestVo;
import com.fuxi.widget.PopWinShare;

/**
 * Title: BarcodeVerificationActivity Description: 条码校验活动界面
 * 
 * @author LYJ
 * 
 */
public class BarcodeVerificationActivity extends BaseWapperActivity {

    private static final String TAG = "BarcodeVerificationActivity";
    private static final String AnalyticalBarcode = "/select.do?analyticalBarcode";
    private static final String AnalyticalGoodsBoxBarcode = "/select.do?getBoxCode";
    private static final String sendDatasToGenerateExcel = "/common.do?generateExcel";

    private BarcodeVerificationDifferentAdapter verificationAdapter;
    private BarcodeVerificationAdapter adapter;
    private PopWinShare popWinShare;
    private TouchListener tl = new TouchListener();
    private List<Map<String, Object>> diffList = new ArrayList<Map<String, Object>>();
    private List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
    private List<Map<String, Object>> pinkList;
    private List<Map<String, Object>> redList;
    private List<Map<String, Object>> greenList;

    private LinearLayout llMain;
    private LinearLayout llButtom;
    private LinearLayout llSave;
    private LinearLayout llColorSize;
    // private LinearLayout llPrompt;
    private ListView lvDetails;
    private EditText etBarcode;
    private EditText etColorCode;
    private EditText etSizeCode;
    private EditText etQty;
    private EditText etDocTotalCount;
    private EditText etScanTotalCount;
    private TextView tvSave;
    private TextView tvComplete;
    private AlertDialog alertDialog;

    private String no; // 单号
    private String path = null;// 校验的路径
    private String coverSavePath = null;// 校验后要修改的路径
    private String idStr;// 校验的单据ID的名称
    private String id;// 校验的单据ID
    private String goodsCode;
    private String colorCode;
    private String sizeCode;
    private String goodsId;
    private String colorId;
    private String sizeId;
    private String sizeStr;
    private String type;
    private String customerId;// 发货单(客户ID),采购单(厂商ID)
    private int oneBoxQty;
    private int inputType = 1;// 装箱方式，1为散件，2为装箱
    private boolean auditFlag = false;
    private boolean verificationAccord = false; // //条码校验结果与单据结果符合的标志
    private boolean isNotAllow = false; // 条码校验时大于或非单据货品不允许录入
    private boolean barcodeInputByManual = false; // 条码录入

    @Override
    public void onClick(View v) {
        Builder dialog = null;
        switch (v.getId()) {
            case R.id.save:
                // if(checkDataList()){
                dialog = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
                dialog.setTitle("提示");
                dialog.setCancelable(false);
                dialog.setMessage("确认保存条码校验记录吗？");
                // 相当于点击确认按钮
                dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveProgressOfBarcodeScanning();
                    }
                });
                // 相当于点击取消按钮
                dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {}
                });
                dialog.create();
                dialog.show();
                // }
                break;
            case R.id.complete:
                if (!verificationAccord || greenList.size() != dataList.size()) {
                    dialog = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
                    dialog.setTitle("提示");
                    dialog.setMessage("当前校验结果与原单不相符，点击「确认」系统将以条码校验的结果覆盖原单据，点击「取消」将结束本次校验操作，请谨慎操作！");
                    dialog.setCancelable(false);
                    // 相当于点击确认按钮
                    dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (LoginParameterUtil.updateToDoc) {
                                if (auditFlag) {
                                    dialog.dismiss();
                                    Builder dialogs = new AlertDialog.Builder(BarcodeVerificationActivity.this, AlertDialog.THEME_HOLO_LIGHT);
                                    dialogs.setTitle("提示");
                                    dialogs.setMessage("此单据已经审核，无法以条码校验的结果覆盖原单据");
                                    // 相当于点击确认按钮
                                    dialogs.setPositiveButton("退出", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            SpUtil.removeObject(getApplicationContext(), "barcode_verification" + no);
                                            finish();
                                        }
                                    });
                                    dialogs.create();
                                    dialogs.show();
                                } else {
                                    coverSave();
                                }
                            } else {
                                dialog.dismiss();
                                Builder dialogs = new AlertDialog.Builder(BarcodeVerificationActivity.this, AlertDialog.THEME_HOLO_LIGHT);
                                dialogs.setTitle("提示");
                                dialogs.setMessage("暂无条码校验允许覆盖原单据的权限，请前往「系统设置」中设置后再进行此操作");
                                // 相当于点击确认按钮
                                dialogs.setPositiveButton("退出", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        SpUtil.removeObject(getApplicationContext(), "barcode_verification" + no);
                                        finish();
                                    }
                                });
                                dialogs.create();
                                dialogs.show();
                            }
                        }
                    });
                    // 相当于点击取消按钮
                    dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            completeBarcodeScanning();
                        }
                    });
                    dialog.create();
                    dialog.show();
                } else {
                    // 完成条码校验,清除记录并退出
                    completeBarcodeScanning();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }

    /**
     * 完成条码校验
     */
    private void completeBarcodeScanning() {
        Builder dialog = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
        dialog.setTitle("提示");
        dialog.setMessage("单据 " + no + " 的条码校验已完成，点击「确认」退出条码校验并清除单据 " + no + " 的校验记录");
        dialog.setCancelable(false);
        // 相当于点击确认按钮
        dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SpUtil.removeObject(getApplicationContext(), "barcode_verification" + no);
                finish();
            }
        });
        dialog.create();
        dialog.show();
    }

    /**
     * 保存单据的条码校验记录
     */
    private void saveProgressOfBarcodeScanning() {
        try {
            SpUtil.put(getApplicationContext(), "barcode_verification" + no, dataList);
            Builder dialog = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
            dialog.setTitle("提示");
            dialog.setCancelable(false);
            dialog.setMessage("单据 " + no + " 的条码校验记录已保存成功");
            // 相当于点击确认按钮
            dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            dialog.create();
            dialog.show();
        } catch (IOException e) {
            Toast.makeText(BarcodeVerificationActivity.this, "条码校验记录保存失败,请稍后重试", Toast.LENGTH_SHORT).show();
            Logger.e(TAG, e.getMessage());
        }
    }

    /**
     * 获取缓存的条码校验进度记录
     * 
     * @return
     */
    private List<Map<String, Object>> getDocs() {
        List<Map<String, Object>> datas = null;
        try {
            datas = (List<Map<String, Object>>) SpUtil.get(getApplicationContext(), "barcode_verification" + no);
        } catch (Exception e) {
            Toast.makeText(BarcodeVerificationActivity.this, "此单据暂无条码校验记录", Toast.LENGTH_SHORT).show();
            Logger.e(TAG, e.getMessage());
        }
        return datas;
    }

    /**
     * 检查条码是否都已经校验过
     * 
     * @return
     */
    private boolean checkDataList() {
        boolean flag = true;
        for (int i = 0; i < dataList.size(); i++) {
            Map<String, Object> temp = dataList.get(i);
            int qty = Integer.parseInt(String.valueOf(temp.get("Qty")));
            int quantity = Integer.parseInt(String.valueOf(temp.get("Quantity")));
            if (quantity != 0 && qty == 0) {
                flag = false;
                return flag;
            }
        }
        return flag;
    }

    /**
     * 覆盖原来保存的单据(已条码校验的结果覆盖原始单据)
     */
    private void coverSave() {
        RequestVo vo = new RequestVo();
        vo.requestUrl = coverSavePath;
        vo.context = this;
        HashMap map = new HashMap();
        map.put(idStr, id);
        net.sf.json.JSONArray json = net.sf.json.JSONArray.fromObject(dataList);
        map.put("data", json.toString());
        vo.requestDataMap = map;
        super.getDataFromServer(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    if (retObj.getBoolean("success")) {
                        Builder dialog = new AlertDialog.Builder(BarcodeVerificationActivity.this, AlertDialog.THEME_HOLO_LIGHT);
                        dialog.setTitle("提示");
                        dialog.setMessage("单据修改成功，单据中新增的货品价格默认为0，请前往单据明细界面中修改货品的对应价格");
                        // 相当于点击确认按钮
                        dialog.setPositiveButton("退出", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SpUtil.removeObject(getApplicationContext(), "barcode_verification" + no);
                                finish();
                            }
                        });
                        dialog.create();
                        dialog.show();
                    } else {
                        Toast.makeText(BarcodeVerificationActivity.this, "单据修改过程中出现错误,单据修改失败", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(BarcodeVerificationActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 获取单据详细信息
     */
    private void getData() {
        RequestVo vo = new RequestVo();
        vo.requestUrl = path;
        vo.context = this;
        HashMap map = new HashMap();
        map.put(idStr, id);
        vo.requestDataMap = map;
        super.getDataFromServer(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    if (retObj.getBoolean("success")) {
                        // 提示
                        prompt();
                        JSONObject rs = retObj.getJSONObject("attributes");
                        no = rs.getString("No");
                        String quantitySun = rs.getString("QuantitySum");
                        auditFlag = Boolean.parseBoolean(rs.getString("AuditFlag"));
                        int boxQtySum = rs.getInt("BoxQtySum");
                        JSONArray array = rs.getJSONArray("detailList");
                        if (boxQtySum == 0) {
                            // 去除array中的重复集合
                            array = mergeData(array);
                        }
                        for (int i = 0; i < array.length(); i++) {
                            Map temp = new HashMap();
                            JSONObject json = array.getJSONObject(i);
                            temp.put("GoodsID", json.getString("GoodsID"));
                            temp.put("ColorID", json.getString("ColorID"));
                            temp.put("SizeID", json.getString("SizeID"));
                            temp.put("Color", json.getString("Color"));
                            temp.put("Size", json.getString("Size"));
                            temp.put("Quantity", Math.abs(json.getInt("Quantity")));
                            temp.put("GoodsCode", json.getString("GoodsCode"));
                            temp.put("OneBoxQty", json.getInt("OneBoxQty"));
                            temp.put("BoxQty", Math.abs(json.getInt("BoxQty")));
                            temp.put("DiscountPrice", json.getString("DiscountPrice"));
                            temp.put("RetailSales", json.getString("RetailSales"));
                            temp.put("UnitPrice", json.getString("UnitPrice"));
                            temp.put("QuantitySum", json.getInt("QuantitySum"));
                            temp.put("IndexNo", json.getString("IndexNo"));
                            temp.put("SizeStr", json.getString("SizeStr"));
                            temp.put("GoodsName", json.getString("GoodsName"));
                            temp.put("ColorCode", json.getString("ColorCode"));
                            temp.put("SizeCode", json.getString("SizeCode"));
                            temp.put("Qty", "0");
                            dataList.add(temp);
                        }
                        adapter.refresh();
                        // 获取单据总数
                        etDocTotalCount.setText(quantitySun);
                        // 获取单据条码校验记录
                        List<Map<String, Object>> lists = getDocs();
                        if (lists != null && lists.size() > 0) {
                            dataList.clear();
                            dataList.addAll(lists);
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(BarcodeVerificationActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 去除JSONArray中重复的集合
     * 
     * @param array
     * @return
     * @throws JSONException
     */
    private JSONArray mergeData(JSONArray array) throws JSONException {
        for (int i = 0; i < array.length() - 1; i++) {
            JSONObject obj1 = array.getJSONObject(i);
            for (int j = array.length() - 1; j > i; j--) {
                JSONObject obj2 = array.getJSONObject(j);
                if (obj1.getString("GoodsID").equals(obj2.getString("GoodsID")) && obj1.getString("ColorID").equals(obj2.getString("ColorID")) && obj1.getString("SizeID").equals(obj2.getString("SizeID"))) {
                    array.remove(j);
                }
            }
        }
        return array;
    }

    /**
     * 提示
     */
    private void prompt() {
        Builder dialog = new AlertDialog.Builder(BarcodeVerificationActivity.this, AlertDialog.THEME_HOLO_LIGHT);
        dialog.setTitle("注意");
        dialog.setMessage("条码校验过程中如商品扫错或多扫，请将数量改为负数后扫描相应的货品进行修改。如需限制单据以外的货品不录入及扫描数不超过单据的货品数，请前往设置中心进行设置！");
        // 相当于点击确认按钮
        dialog.setPositiveButton("我知道了", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {}
        });
        dialog.setCancelable(false);
        dialog.create();
        dialog.show();
    }

    /**
     * 统计校验时扫描的货品总数量
     */
    private void countTotal() {
        int sum = 0;
        for (int j = 0; j < dataList.size(); j++) {
            int num = Integer.parseInt(String.valueOf(dataList.get(j).get("Qty")));
            sum += num;
        }
        etScanTotalCount.setText(String.valueOf(sum));
    }

    /**
     * 条码扫描后的操作
     */
    private void scanningBarcode() {
        final String barcode = etBarcode.getText().toString().trim();
        if (barcode == null || barcode.isEmpty()) {
            Toast.makeText(BarcodeVerificationActivity.this, "货品条码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        String qtyStr = etQty.getText().toString().trim();
        if (qtyStr == null || qtyStr.isEmpty()) {
            Toast.makeText(BarcodeVerificationActivity.this, "货品数量不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (Integer.parseInt(qtyStr) == 0) {
            Toast.makeText(BarcodeVerificationActivity.this, "货品数量不能为0", Toast.LENGTH_SHORT).show();
            return;
        }
        final int qty = Integer.parseInt(qtyStr);
        if (inputType == 1) {
            analyticalBarcode(barcode, qty);
        } else {
            analyticalGoodsBoxBarcode(barcode, qty);
        }
    }

    /**
     * 解析条码
     * 
     * @param barcode
     * @param qty
     */
    private void analyticalBarcode(final String barcode, final int qty) {
        RequestVo vo = new RequestVo();
        vo.requestUrl = AnalyticalBarcode;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("BarCode", barcode);
        map.put("CustomerId", customerId);
        map.put("Type", type);
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
                        goodsId = obj.getString("GoodsID");
                        colorId = obj.getString("ColorID");
                        sizeId = obj.getString("SizeID");
                        BigDecimal retailSales = null;
                        BigDecimal unitPrice = null;
                        BigDecimal discountPrice = null;
                        // 货品价格
                        if (!obj.isNull("RetailSales")) {
                            retailSales = new BigDecimal(obj.getDouble("RetailSales"));
                        }
                        if (!obj.isNull("UnitPrice")) {
                            unitPrice = new BigDecimal(obj.getDouble("UnitPrice"));
                            discountPrice = unitPrice;
                        }
                        // 累加相同的货品
                        // 条码是否正确的标志
                        boolean flag = false;
                        for (int i = 0; i < dataList.size(); i++) {
                            Map<String, Object> map = dataList.get(i);
                            String tGoodsId = String.valueOf(map.get("GoodsID"));
                            String tColorId = String.valueOf(map.get("ColorID"));
                            String tSizeId = String.valueOf(map.get("SizeID"));
                            if (tGoodsId.equals(goodsId) && tColorId.equals(colorId) && tSizeId.equals(sizeId)) {
                                // 判断是否多扫描
                                int quantity = Integer.parseInt(String.valueOf(map.get("Quantity")));
                                // 计算数量
                                String tQtyStr = String.valueOf(map.get("Qty"));
                                if (null == tQtyStr || tQtyStr.isEmpty() || "null".equalsIgnoreCase(tQtyStr)) {
                                    tQtyStr = "0";
                                }
                                int tQty = Integer.parseInt(tQtyStr);
                                if (quantity == 0) {
                                    settingVoice("noFound");
                                }
                                // 是否允许大于单据数量
                                if (isNotAllow && quantity != 0 && tQty >= quantity) {
                                    // 不允许大于单据数量
                                    if (tQty > quantity) {
                                        settingVoice("error");
                                    }
                                } else {
                                    map.put("Qty", String.valueOf(tQty + qty));
                                    flag = true;
                                }
                            }
                        }
                        // 参数控制
                        // 允许扫描单据以外的货品
                        if (!flag) {
                            if (isNotAllow) {
                                // 不允许扫描单据以外的货品
                                settingVoice("noFound");
                            } else {
                                // 单据中不存在此条码
                                settingVoice("noFound");
                                Map<String, Object> temp = new HashMap<String, Object>();
                                temp.put("GoodsID", obj.getString("GoodsID"));
                                temp.put("ColorID", obj.getString("ColorID"));
                                temp.put("SizeID", obj.getString("SizeID"));
                                temp.put("Color", obj.getString("ColorName"));
                                temp.put("Size", obj.getString("SizeName"));
                                temp.put("SizeStr", null);
                                temp.put("RetailSales", retailSales);
                                temp.put("UnitPrice", unitPrice);
                                temp.put("DiscountPrice", discountPrice);
                                temp.put("Quantity", "0");
                                temp.put("GoodsCode", obj.getString("GoodsCode"));
                                temp.put("GoodsName", obj.getString("GoodsName"));
                                temp.put("ColorCode", obj.getString("ColorCode"));
                                temp.put("SizeCode", obj.getString("SizeCode"));
                                temp.put("OneBoxQty", "0");
                                temp.put("BoxQty", "0");
                                temp.put("Qty", Math.abs(qty));
                                dataList.add(temp);
                            }
                        }
                        // 重新计算数量
                        countTotal();
                        // 重置扫码区
                        resetBarcode();
                        // 刷新ListView集合
                        adapter.notifyDataSetChanged();
                        // 条码排序
                        sortList(dataList);
                        // 动态更新ListView
                    } else {
                        settingVoice("error");
                        etBarcode.requestFocus();
                        etBarcode.selectAll();
                        Toast.makeText(BarcodeVerificationActivity.this, "条码" + barcode + "无法识别", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    etBarcode.requestFocus();
                    etBarcode.selectAll();
                    Toast.makeText(BarcodeVerificationActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 解析箱条码
     * 
     * @param barcode
     * @param qty
     */
    private void analyticalGoodsBoxBarcode(final String barcode, final int qty) {
        RequestVo vo = new RequestVo();
        vo.requestUrl = AnalyticalGoodsBoxBarcode;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("BarCode", barcode);
        map.put("CustomerId", customerId);
        map.put("Type", type);
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
                        for (int j = 0; j < jsonArray.length(); j++) {
                            JSONObject obj = jsonArray.getJSONObject(j);
                            goodsId = obj.getString("GoodsID");
                            colorId = obj.getString("ColorID");
                            sizeId = obj.getString("SizeID");
                            sizeStr = obj.getString("SizeStr");
                            oneBoxQty = Math.abs(obj.getInt("Quantity"));
                            BigDecimal retailSales = null;
                            BigDecimal unitPrice = null;
                            BigDecimal discountPrice = null;
                            // 货品价格
                            if (!obj.isNull("RetailSales")) {
                                retailSales = new BigDecimal(obj.getDouble("RetailSales"));
                            }
                            if (!obj.isNull("UnitPrice")) {
                                unitPrice = new BigDecimal(obj.getDouble("UnitPrice"));
                                discountPrice = unitPrice;
                            }
                            // 累加相同的货品
                            // 条码是否正确的标志
                            boolean flag = false;
                            for (int i = 0; i < dataList.size(); i++) {
                                Map<String, Object> map = dataList.get(i);
                                String tGoodsId = String.valueOf(map.get("GoodsID"));
                                String tColorId = String.valueOf(map.get("ColorID"));
                                String tSizeId = String.valueOf(map.get("SizeID"));
                                String tSizeStr = String.valueOf(map.get("SizeStr"));
                                // 箱条码
                                if (tGoodsId.equals(goodsId) && tColorId.equals(colorId) && tSizeId.equals(sizeId) && tSizeStr.equals(sizeStr)) {
                                    String tQtyStr = String.valueOf(map.get("Qty"));
                                    // 判断是否多扫描
                                    int quantity = Integer.parseInt(String.valueOf(map.get("Quantity")));
                                    // 计算数量
                                    if (null == tQtyStr || tQtyStr.isEmpty() || "null".equalsIgnoreCase(tQtyStr)) {
                                        tQtyStr = "0";
                                    }
                                    int tQty = Integer.parseInt(tQtyStr);
                                    if (quantity == 0) {
                                        settingVoice("noFound");
                                    }
                                    // 是否允许大于单据数量
                                    if (isNotAllow && quantity != 0 && tQty >= quantity) {
                                        // 不允许大于单据数量
                                        if (tQty > quantity) {
                                            settingVoice("error");
                                        }
                                    } else {
                                        int tTotal = qty * oneBoxQty;
                                        int boxQty = (tTotal + tQty) / oneBoxQty;
                                        map.put("Qty", String.valueOf(tTotal + tQty));
                                        map.put("OneBoxQty", oneBoxQty);
                                        map.put("BoxQty", boxQty);
                                        // 计算QuantitySum
                                        int quantitySum = countQuantitySum(jsonArray, boxQty);
                                        map.put("QuantitySum", quantitySum);
                                        flag = true;
                                    }
                                }
                            }
                            // 允许扫描单据以外的货品
                            if (!flag) {
                                // 参数控制
                                if (isNotAllow) {
                                    // 不允许扫描单据以外的货品
                                    settingVoice("noFound");
                                } else {
                                    // 单据中不存在此条码
                                    settingVoice("noFound");
                                    Map<String, Object> temp = new HashMap<String, Object>();
                                    temp.put("GoodsID", obj.getString("GoodsID"));
                                    temp.put("ColorID", obj.getString("ColorID"));
                                    temp.put("SizeID", obj.getString("SizeID"));
                                    temp.put("Color", obj.getString("ColorName"));
                                    temp.put("Size", obj.getString("SizeName"));
                                    temp.put("SizeStr", obj.getString("SizeStr"));
                                    temp.put("GoodsName", obj.getString("GoodsName"));
                                    temp.put("ColorCode", obj.getString("ColorCode"));
                                    temp.put("SizeCode", obj.getString("SizeCode"));
                                    temp.put("RetailSales", retailSales);
                                    temp.put("UnitPrice", unitPrice);
                                    temp.put("DiscountPrice", discountPrice);
                                    temp.put("Quantity", "0");
                                    temp.put("GoodsCode", obj.getString("GoodsCode"));
                                    temp.put("OneBoxQty", Math.abs(obj.getInt("Quantity")));
                                    // 计算QuantitySum
                                    int quantitySum = countQuantitySum(jsonArray, qty);
                                    temp.put("QuantitySum", quantitySum);
                                    temp.put("BoxQty", qty);
                                    temp.put("Qty", Math.abs(obj.getInt("Quantity") * qty));
                                    dataList.add(temp);
                                }
                            }
                        }
                        // 重新计算数量
                        countTotal();
                        // 重置扫描区域
                        resetBarcode();
                        // 刷新ListView集合
                        adapter.notifyDataSetChanged();
                        // 条码排序
                        sortList(dataList);
                        // 动态更新ListView
                    } else {
                        settingVoice("error");
                        etBarcode.requestFocus();
                        etBarcode.selectAll();
                        Toast.makeText(BarcodeVerificationActivity.this, "箱条码" + barcode + "无法识别", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    etBarcode.requestFocus();
                    etBarcode.selectAll();
                    Toast.makeText(BarcodeVerificationActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 手输货号添加货品记录
     */
    private void addIteamByManual() {
        String barcodeStr = etBarcode.getText().toString().trim();
        if ("".equals(barcodeStr)) {
            Toast.makeText(BarcodeVerificationActivity.this, "货品编码不能为空", Toast.LENGTH_SHORT).show();
            goodsId = null;
            etColorCode.setText(null);
            etSizeCode.setText(null);
            return;
        }
        String tcolorCode = etColorCode.getText().toString().trim();
        if ("".equals(tcolorCode)) {
            Toast.makeText(BarcodeVerificationActivity.this, "颜色编码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        String tsizeCode = etSizeCode.getText().toString().trim();
        if ("".equals(tsizeCode)) {
            Toast.makeText(BarcodeVerificationActivity.this, "尺码编码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        String qtyStr = etQty.getText().toString().trim();
        if (qtyStr == null || qtyStr.isEmpty()) {
            Toast.makeText(BarcodeVerificationActivity.this, "货品数量不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (Integer.parseInt(qtyStr) == 0) {
            Toast.makeText(BarcodeVerificationActivity.this, "货品数量不能为0", Toast.LENGTH_SHORT).show();
            return;
        }
        String barcode = goodsCode + colorCode + sizeCode;
        Integer qty = Integer.valueOf(qtyStr);
        ajaxAddItemByGoodsCode(barcode, qty);
    }

    /**
     * 根据货号,颜色,尺码ID加载货品明细记录
     * 
     * @param barcodeStr
     * @param qty
     */
    public void ajaxAddItemByGoodsCode(final String barcodeStr, final int qty) {
        RequestVo vo = new RequestVo();
        vo.requestUrl = "/select.do?addIteamByGoodsCode";
        vo.context = this;
        HashMap map = new HashMap();
        map.put("Barcode", barcodeStr);
        map.put("GoodsID", goodsId);
        map.put("ColorID", colorId);
        map.put("SizeID", sizeId);
        map.put("CustomerId", "");
        map.put("Type", type);
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
                        goodsId = obj.getString("GoodsID");
                        colorId = obj.getString("ColorID");
                        sizeId = obj.getString("SizeID");
                        BigDecimal retailSales = null;
                        BigDecimal unitPrice = null;
                        BigDecimal discountPrice = null;
                        // 货品价格
                        if (!obj.isNull("RetailSales")) {
                            retailSales = new BigDecimal(obj.getDouble("RetailSales"));
                        }
                        if (!obj.isNull("UnitPrice")) {
                            unitPrice = new BigDecimal(obj.getDouble("UnitPrice"));
                            discountPrice = unitPrice;
                        }
                        // 累加相同的货品
                        // 条码是否正确的标志
                        boolean flag = false;
                        for (int i = 0; i < dataList.size(); i++) {
                            Map<String, Object> map = dataList.get(i);
                            String tGoodsId = String.valueOf(map.get("GoodsID"));
                            String tColorId = String.valueOf(map.get("ColorID"));
                            String tSizeId = String.valueOf(map.get("SizeID"));
                            if (tGoodsId.equals(goodsId) && tColorId.equals(colorId) && tSizeId.equals(sizeId)) {
                                // 判断是否多扫描
                                int quantity = Integer.parseInt(String.valueOf(map.get("Quantity")));
                                // 计算数量
                                String tQtyStr = String.valueOf(map.get("Qty"));
                                if (null == tQtyStr || tQtyStr.isEmpty() || "null".equalsIgnoreCase(tQtyStr)) {
                                    tQtyStr = "0";
                                }
                                int tQty = Integer.parseInt(tQtyStr);
                                if (quantity == 0) {
                                    settingVoice("noFound");
                                }
                                // 是否允许大于单据数量
                                if (isNotAllow && quantity != 0 && tQty >= quantity) {
                                    // 不允许大于单据数量
                                    if (tQty > quantity) {
                                        settingVoice("error");
                                    }
                                } else {
                                    map.put("Qty", String.valueOf(tQty + qty));
                                    flag = true;
                                }
                            }
                        }
                        // 参数控制
                        // 允许扫描单据以外的货品
                        if (!flag) {
                            if (isNotAllow) {
                                // 不允许扫描单据以外的货品
                                settingVoice("noFound");
                            } else {
                                // 单据中不存在此条码
                                settingVoice("noFound");
                                Map<String, Object> temp = new HashMap<String, Object>();
                                temp.put("GoodsID", obj.getString("GoodsID"));
                                temp.put("ColorID", obj.getString("ColorID"));
                                temp.put("SizeID", obj.getString("SizeID"));
                                temp.put("Color", obj.getString("ColorName"));
                                temp.put("Size", obj.getString("SizeName"));
                                temp.put("SizeStr", null);
                                temp.put("RetailSales", retailSales);
                                temp.put("UnitPrice", unitPrice);
                                temp.put("DiscountPrice", discountPrice);
                                temp.put("Quantity", "0");
                                temp.put("GoodsCode", obj.getString("GoodsCode"));
                                temp.put("GoodsName", obj.getString("GoodsName"));
                                temp.put("ColorCode", obj.getString("ColorCode"));
                                temp.put("SizeCode", obj.getString("SizeCode"));
                                temp.put("OneBoxQty", "0");
                                temp.put("BoxQty", "0");
                                temp.put("Qty", Math.abs(qty));
                                dataList.add(temp);
                            }
                        }
                        // 重新计算数量
                        countTotal();
                        // 重置扫码区
                        resetBarcode();
                        // 刷新ListView集合
                        adapter.notifyDataSetChanged();
                        // 条码排序
                        sortList(dataList);
                        // 动态更新ListView
                    } else {
                        settingVoice("error");
                        etBarcode.requestFocus();
                        etBarcode.selectAll();
                        Toast.makeText(BarcodeVerificationActivity.this, "条码" + barcodeStr + "无法识别", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(BarcodeVerificationActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 计算总数量
     * 
     * @param jsonArray
     * @param boxQty
     * @return
     * @throws Exception
     */
    private int countQuantitySum(JSONArray jsonArray, int boxQty) throws Exception {
        int total = 0;
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jo = jsonArray.getJSONObject(i);
            String tgoodsId = jo.getString("GoodsID");
            String tcolorId = jo.getString("ColorID");
            String tsizeStr = jo.getString("SizeStr");
            if (tgoodsId.equals(goodsId) && tcolorId.equals(colorId) && tsizeStr.equals(sizeStr)) {
                int num = Math.abs(jo.getInt("Quantity"));
                total += num;
            }
        }
        total = boxQty * total;
        return total;
    }

    /**
     * ListView排序(条码校验时根据校验情况更新集合列表显示顺序)
     * 
     * @param dataList
     */
    private void sortList(List<Map<String, Object>> dataList) {
        // 条码校验结果与单据结果符合的标志
        verificationAccord = true;
        pinkList = new ArrayList<Map<String, Object>>();
        redList = new ArrayList<Map<String, Object>>();
        greenList = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < dataList.size(); i++) {
            Map<String, Object> temp = dataList.get(i);
            String qtyStr = String.valueOf(temp.get("Qty"));
            if (null == qtyStr || qtyStr.isEmpty() || "null".equalsIgnoreCase(qtyStr)) {
                qtyStr = "0";
            }
            int qty = Integer.parseInt(qtyStr);
            int quantity = Integer.parseInt(String.valueOf(temp.get("Quantity")));
            if (quantity > 0) {
                if (qty > quantity) {
                    pinkList.add(temp);
                    dataList.remove(temp);
                    i--;
                } else if (qty == quantity) {
                    greenList.add(temp);
                    dataList.remove(temp);
                    i--;
                }
            } else {
                redList.add(temp);
                dataList.remove(temp);
                i--;
            }
        }
        // 粉红色集合(单据数<扫描数)
        for (int i = 0; i < pinkList.size(); i++) {
            verificationAccord = false;
            dataList.add(pinkList.get(i));
        }
        // 绿色集合(单据数=扫描数)
        for (int i = 0; i < greenList.size(); i++) {
            dataList.add(greenList.get(i));
        }
        // 红色集合(单据中没有的条码)
        for (int i = 0; i < redList.size(); i++) {
            verificationAccord = false;
            // 去除redList中的重复记录
            for (int j = 0; j < redList.size(); j++) {
                Map<String, Object> temp = redList.get(i);
                String qtyStr = String.valueOf(temp.get("Qty"));
                if (null == qtyStr || qtyStr.isEmpty() || "null".equalsIgnoreCase(qtyStr)) {
                    qtyStr = "0";
                }
                int qty = Integer.parseInt(qtyStr);
                if (qty == 0) {
                    redList.remove(temp);
                    j--;
                }
            }
            dataList.add(redList.get(i));
        }
        // 刷新ListView集合
        adapter.refresh();
    }

    /**
     * 设置条码扫描后的提示声音(条码扫描出现错误时)
     * 
     * @param type
     */
    private void settingVoice(String type) {
        NotificationManager manger = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification();
        // 自定义声音 声音文件放在ram目录下，没有此目录自己创建一个
        if ("noFound".equals(type)) {
            notification.sound = Uri.parse("android.resource://" + BarcodeVerificationActivity.this.getPackageName() + "/" + R.raw.no_find);
            // 设置震动
            Vibrator vibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
            vibrator.vibrate(1500);
            // vibrator.cancel();
        } else if ("error".equals(type)) {
            notification.sound = Uri.parse("android.resource://" + BarcodeVerificationActivity.this.getPackageName() + "/" + R.raw.error);
            // 设置震动
            Vibrator vibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
            vibrator.vibrate(1500);
            // vibrator.cancel();
        }
        // 使用系统默认声音用下面这条
        // notification.defaults=Notification.DEFAULT_SOUND;
        manger.notify(1, notification);
    }

    @Override
    protected void onHeadRightButton(View v) {
        // 显示菜单
        showpopWinShare();
    }

    /**
     * 导出到本地
     */
    private void exportToLocal() {
        try {
            // 导出Excel文件
            final String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "fxpda" + File.separator + "export_file";
            File direct = new File(path);
            if (!direct.exists()) {
                direct.mkdirs();
            }
            String excelPath = path + File.separator + no + "校验差异.xls";
            String showPath = "fxpda" + File.separator + "export_file" + File.separator + no + "校验差异.xls";
            List<String> listTitle = generalExcelTitle();
            List<Object[]> objs = generalExcelDatas();
            ExcelUtil.exportExcel(excelPath, LoginParameterUtil.corpName, listTitle, objs);
            Builder dialog = new AlertDialog.Builder(BarcodeVerificationActivity.this, AlertDialog.THEME_HOLO_LIGHT);
            dialog.setTitle("提示");
            dialog.setMessage("条码校验差异记录已导出到 " + showPath + " ，是否结束当前操作?");
            // 相当于点击确认按钮
            dialog.setPositiveButton("是", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    saveProgressOfBarcodeScanning();
                }
            });
            // 相当于点击取消按钮
            dialog.setNegativeButton("否", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {}
            });
            dialog.setCancelable(false);
            dialog.create();
            dialog.show();
        } catch (Exception e) {
            Toast.makeText(BarcodeVerificationActivity.this, "系统错误,条码校验差异表导出失败", Toast.LENGTH_LONG).show();
            Logger.e(TAG, e.getMessage());
        }
    }

    /**
     * 导出到服务器上
     */
    private void exportToServer() {
        List<String> listTitle = generalExcelTitle();
        List<Object[]> objs = generalExcelDatas();
        sendDatasToServer(no, listTitle, objs);
    }

    /**
     * 条码检验Excel显示标题
     * 
     * @return
     */
    private List<String> generalExcelTitle() {
        List<String> listTitle = new ArrayList<String>();
        listTitle.add("货品条码");
        listTitle.add("货品编码");
        listTitle.add("货品名称");
        listTitle.add("颜色");
        listTitle.add("尺码");
        listTitle.add("单据数量");
        listTitle.add("实际数量");
        listTitle.add("差异数量");
        return listTitle;
    }

    /**
     * 条码校验结果生成Excel报表数据
     * 
     * @return
     */
    private List<Object[]> generalExcelDatas() {
        int sumQuantity = 0, sumQty = 0;
        List<Object[]> objs = new ArrayList<Object[]>();
        for (int i = 0; i < dataList.size(); i++) {
            Map<String, Object> map = dataList.get(i);
            String goodsCode = (String) map.get("GoodsCode");
            String goodsName = (String) map.get("GoodsName");
            String colorCode = (String) map.get("ColorCode");
            String sizeCode = (String) map.get("SizeCode");
            String barcode = goodsCode + colorCode + sizeCode;
            String color = (String) map.get("Color");
            String size = (String) map.get("Size");
            int quantity = Integer.parseInt(String.valueOf(map.get("Quantity")));
            int qty = Integer.parseInt(String.valueOf(map.get("Qty")));
            int diff = quantity - qty;
            // 计算合计
            sumQuantity += quantity;
            sumQty += qty;
            Object[] obj = new Object[] {barcode, goodsCode, goodsName, color, size, quantity, qty, diff};
            objs.add(obj);
        }
        // 添加合计
        Object[] obj = new Object[] {"合计", "", "", "", "", sumQuantity, sumQty, (sumQuantity - sumQty)};
        objs.add(obj);
        return objs;
    }

    /**
     * 单选提示框选择条码检验导出方式 1:导出到设备 2.导出到服务器
     * 
     * @param view
     */
    private void showSingleAlertDialog(View view) {
        final String[] items = {"导出到本地", "导出到服务器"};
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
        alertBuilder.setTitle("选择条码校验差异导出方式");
        alertBuilder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int index) {
                if (index == 0) {
                    // 导出到本地
                    exportToLocal();
                } else if (index == 1) {
                    // 导出到服务器
                    exportToServer();
                }
                // 关闭提示框
                alertDialog.dismiss();
            }
        });
        alertDialog = alertBuilder.create();
        alertDialog.show();
    }

    /**
     * 发送差异数据到服务端生成Excel
     * 
     * @param no
     * @param listTitle
     * @param objs
     */
    private void sendDatasToServer(String no, List<String> listTitle, List<Object[]> objs) {
        RequestVo vo = new RequestVo();
        vo.requestUrl = sendDatasToGenerateExcel;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("no", no);
        net.sf.json.JSONArray json = net.sf.json.JSONArray.fromObject(listTitle);
        map.put("listTitle", json.toString());
        net.sf.json.JSONArray jsons = net.sf.json.JSONArray.fromObject(objs);
        map.put("objs", jsons.toString());
        vo.requestDataMap = map;
        super.getDataFromServer(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    if (retObj.getBoolean("success")) {
                        String exportPath = retObj.getString("obj");
                        Builder dialog = new AlertDialog.Builder(BarcodeVerificationActivity.this, AlertDialog.THEME_HOLO_LIGHT);
                        dialog.setTitle("提示");
                        dialog.setMessage("条码校验差异文件已导出到服务器 " + exportPath + " ，是否结束当前操作?");
                        // 相当于点击确认按钮
                        dialog.setPositiveButton("是", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                saveProgressOfBarcodeScanning();
                            }
                        });
                        // 相当于点击取消按钮
                        dialog.setNegativeButton("否", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {}
                        });
                        dialog.setCancelable(false);
                        dialog.create();
                        dialog.show();
                    }
                } catch (Exception e) {
                    etBarcode.requestFocus();
                    etBarcode.selectAll();
                    Toast.makeText(BarcodeVerificationActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 显示条码校验差异
     */
    private void showDifferent() {
        // 清空原始数据
        diffList.clear();
        // 获取差异数据
        for (int i = 0; i < dataList.size(); i++) {
            Map<String, Object> map = dataList.get(i);
            int qty = Integer.parseInt(String.valueOf(map.get("Qty")));
            int quantity = Integer.parseInt(String.valueOf(map.get("Quantity")));
            if (qty != quantity) {
                diffList.add(map);
            }
        }
        // 显示数据
        View view = (LinearLayout) getLayoutInflater().inflate(R.layout.activity_barcode_verification_different, null);
        // 初始化
        ListView lv_datas = (ListView) view.findViewById(R.id.lv_datas);
        // 显示数据
        verificationAdapter = new BarcodeVerificationDifferentAdapter(this, diffList);
        lv_datas.setAdapter(verificationAdapter);
        // 操作提示
        Builder dialog = new AlertDialog.Builder(BarcodeVerificationActivity.this, AlertDialog.THEME_HOLO_LIGHT);
        dialog.setCancelable(false);
        dialog.setTitle("条码校验差异明细");
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
        // 点击返回时返回上一页面并刷新
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

    /**
     * 点击返回时的操作
     */
    private void back() {
        Builder dialog = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
        dialog.setTitle("提示");
        dialog.setMessage("确定要退出条码校验码？");
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
     * 重置扫码区
     */
    private void resetBarcode() {
        etBarcode.setText(null);
        etColorCode.setText(null);
        etSizeCode.setText(null);
        goodsId = null;
        colorId = null;
        sizeId = null;
        goodsCode = null;
        colorCode = null;
        sizeCode = null;
        etQty.setText("1");
        etBarcode.setSelection(0);
        etBarcode.requestFocus();
    }

    @Override
    protected void loadViewLayout() {
        boolean hasprice = checkActivityHasPrice();
        if (hasprice) {
            setContentView(R.layout.activity_barcode_verification);
        } else {
            setContentView(R.layout.activity_barcode_verification_no_price);
        }
        setTitle("条码校验");
    }

    /**
     * 检查单据活动界面是否显示单价
     * 
     * @return
     */
    private boolean checkActivityHasPrice() {
        boolean hasprice = false;
        Activity activity = AppManager.findSecondToLastActivity();
        if (activity instanceof SalesOrderDetailActivity) {
            if (LoginParameterUtil.salesOrderUnitPriceRight) {
                hasprice = true;
            }
        } else if (activity instanceof SalesDetailActivity) {
            if (LoginParameterUtil.salesUnitPriceRight) {
                hasprice = true;
            }
        } else if (activity instanceof SalesReturnsDetailActivity) {
            if (LoginParameterUtil.salesReturnUnitPriceRight) {
                hasprice = true;
            }
        } else if (activity instanceof PurchaseDetailActivity) {
            if (LoginParameterUtil.purchaseUnitPriceRight) {
                hasprice = true;
            }
        } else if (activity instanceof PurchaseReturnDetailActivity) {
            if (LoginParameterUtil.purchaseReturnUnitPriceRight) {
                hasprice = true;
            }
        } else if (activity instanceof StockMoveActivity) {
            if (LoginParameterUtil.stockMoveUnitPriceRight) {
                hasprice = true;
            }
        } else if (activity instanceof WarehouseStockInDetailActivity) {
            if (LoginParameterUtil.stockInUnitPriceRight) {
                hasprice = true;
            }
        } else if (activity instanceof WarehouseStockOutDetailActivity) {
            if (LoginParameterUtil.stockOutUnitPriceRight) {
                hasprice = true;
            }
        }
        return hasprice;
    }

    @Override
    protected void processLogic() {
        // 默认隐藏扫码区的颜色尺码选项
        llColorSize.setVisibility(View.GONE);
        isNotAllow = LoginParameterUtil.notAllow;
        resetBarcode();
        Bundle bundle = this.getIntent().getExtras();
        if (null != bundle) {
            idStr = bundle.getString("idStr");
            id = bundle.getString("id");
            path = bundle.getString("path");
            coverSavePath = bundle.getString("coverSavePath");
            inputType = bundle.getInt("inputType");
            type = bundle.getString("type");
            customerId = bundle.getString("customerId");
        }
        if (idStr == null || idStr.isEmpty() || id == null || id.isEmpty() || path == null || path.isEmpty()) {
            Builder dialog = new AlertDialog.Builder(BarcodeVerificationActivity.this, AlertDialog.THEME_HOLO_LIGHT);
            dialog.setTitle("提示");
            dialog.setMessage("初始化单据信息失败,稍后请重试");
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
        } else {
            // 获取单据数据
            getData();
        }
    }

    private class TouchListener implements OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (v.getId()) {
                case R.id.barcode:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Intent intent = new Intent(BarcodeVerificationActivity.this, SelectActivity.class);
                        intent.putExtra("selectType", "selectProduct");
                        startActivityForResult(intent, R.id.barcode);
                        overridePendingTransition(R.anim.activity_open, 0);
                    }
                    break;
                case R.id.colorCode:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        if (goodsId == null || "".equals(goodsId) || "null".equalsIgnoreCase(goodsId)) {
                            Toast.makeText(BarcodeVerificationActivity.this, "请先输入货品编码", Toast.LENGTH_SHORT).show();
                            break;
                        }
                        Intent intent = new Intent(BarcodeVerificationActivity.this, SelectActivity.class);
                        intent.putExtra("selectType", "selectColorByGoodsCode");
                        intent.putExtra("param", goodsId);
                        startActivityForResult(intent, R.id.colorCode);
                        overridePendingTransition(R.anim.activity_open, 0);
                    }
                    break;
                case R.id.sizeCode:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        if (goodsId == null || "".equals(goodsId) || "null".equalsIgnoreCase(goodsId)) {
                            Toast.makeText(BarcodeVerificationActivity.this, "请先输入货品编码", Toast.LENGTH_SHORT).show();
                            break;
                        }
                        Intent intent = new Intent(BarcodeVerificationActivity.this, SelectActivity.class);
                        intent.putExtra("selectType", "selectSizeByGoodsCode");
                        intent.putExtra("param", goodsId);
                        startActivityForResult(intent, R.id.sizeCode);
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
            case R.id.barcode:
                if (resultCode == 1) {
                    etBarcode.setText(data.getStringExtra("Name"));
                    etBarcode.setSelection(etBarcode.getText().toString().length());
                    goodsId = data.getStringExtra("GoodsID");
                    goodsCode = data.getStringExtra("Code");
                    // 设置扫码区自动获取焦点
                    etColorCode.requestFocus();
                    etColorCode.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, etColorCode.getLeft() + 5, etColorCode.getTop() + 5, 0));
                    etColorCode.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, etColorCode.getLeft() + 5, etColorCode.getTop() + 5, 0));
                }
                break;
            case R.id.colorCode:
                if (resultCode == 1) {
                    etColorCode.setText(data.getStringExtra("Name"));
                    etColorCode.setSelection(etColorCode.getText().toString().length());
                    colorId = data.getStringExtra("ColorID");
                    colorCode = data.getStringExtra("ColorCode");
                    // 设置扫码区自动获取焦点
                    etSizeCode.requestFocus();
                    etSizeCode.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, etSizeCode.getLeft() + 5, etSizeCode.getTop() + 5, 0));
                    etSizeCode.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, etSizeCode.getLeft() + 5, etSizeCode.getTop() + 5, 0));
                }
                break;
            case R.id.sizeCode:
                if (resultCode == 1) {
                    etSizeCode.setText(data.getStringExtra("Name"));
                    etSizeCode.setSelection(etSizeCode.getText().toString().length());
                    sizeId = data.getStringExtra("SizeID");
                    sizeCode = data.getStringExtra("SizeCode");
                    // 设置扫码区自动获取焦点
                    etBarcode.requestFocus();
                    // 选择尺码后自动查询
                    addIteamByManual();
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void setListener() {
        adapter = new BarcodeVerificationAdapter(this, dataList);
        lvDetails.setAdapter(adapter);
        // lvDetails.setOnItemLongClickListener(this);
        etBarcode.setOnEditorActionListener(new BarcodeActionListener());
        etBarcode.setOnKeyListener(this);
        tvSave.setOnClickListener(this);
        tvComplete.setOnClickListener(this);
        etColorCode.setOnTouchListener(tl);
        etSizeCode.setOnTouchListener(tl);
        etColorCode.setOnClickListener(this);
        etSizeCode.setOnClickListener(this);
    }

    @Override
    protected void findViewById() {
        llMain = (LinearLayout) findViewById(R.id.ll_main);
        llButtom = (LinearLayout) findViewById(R.id.ll_buttom);
        llSave = (LinearLayout) findViewById(R.id.ll_save);
        llColorSize = (LinearLayout) findViewById(R.id.ll_color_size);
        // llPrompt = (LinearLayout) findViewById(R.id.ll_prompt);
        lvDetails = (ListView) findViewById(R.id.details);
        etBarcode = (EditText) findViewById(R.id.barcode);
        etQty = (EditText) findViewById(R.id.qty);
        etColorCode = (EditText) findViewById(R.id.colorCode);
        etSizeCode = (EditText) findViewById(R.id.sizeCode);
        etDocTotalCount = (EditText) findViewById(R.id.doc_total_count);
        etScanTotalCount = (EditText) findViewById(R.id.scan_total_count);
        tvSave = (TextView) findViewById(R.id.save);
        tvComplete = (TextView) findViewById(R.id.complete);

        int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        llButtom.measure(w, h);
        int height = llButtom.getMeasuredHeight();

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, height);
        llMain.setLayoutParams(lp);

        setHeadRightVisibility(View.VISIBLE);
        setHeadRightText(R.string.setting);
    }

    /**
     * 根据条件动态显示菜单选项
     */
    private void showpopWinShare() {
        if (popWinShare == null) {
            // 自定义的单击事件
            OnClickLintener paramOnClickListener = new OnClickLintener();
            popWinShare = new PopWinShare(BarcodeVerificationActivity.this, paramOnClickListener);
            // 隐藏切换的选项
            popWinShare.ll_parent.removeView(popWinShare.layoutSwitch);
            popWinShare.ll_parent.removeView(popWinShare.layoutRemark);
            popWinShare.ll_parent.removeView(popWinShare.layoutPrint);
            popWinShare.ll_parent.removeView(popWinShare.layout_price);
            popWinShare.ll_parent.removeView(popWinShare.view);
            popWinShare.ll_parent.removeView(popWinShare.view_2);
            popWinShare.ll_parent.removeView(popWinShare.view_3);
            popWinShare.ll_parent.removeView(popWinShare.layoutAddCustomer);
            popWinShare.ll_parent.removeView(popWinShare.view_4);
            popWinShare.ll_parent.removeView(popWinShare.view_5);
            popWinShare.ll_parent.removeView(popWinShare.layoutBarcodeVerification);
            popWinShare.ll_parent.removeView(popWinShare.layoutGoodsBoxBarcode);
            popWinShare.ll_parent.removeView(popWinShare.view_6);
            popWinShare.ll_parent.removeView(popWinShare.view_10);
            popWinShare.ll_parent.removeView(popWinShare.layoutSingleDiscount);
            popWinShare.ll_parent.removeView(popWinShare.view_11);
            popWinShare.ll_parent.removeView(popWinShare.layoutOtherOptions);
            if (inputType != 1) {
                popWinShare.ll_parent.removeView(popWinShare.layoutSwitchInputMode);
                popWinShare.ll_parent.removeView(popWinShare.view_7);
            }
            // 监听窗口的焦点事件，点击窗口外面则取消显示
            popWinShare.getContentView().setOnFocusChangeListener(new View.OnFocusChangeListener() {

                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus) {
                        popWinShare.dismiss();
                    }
                }
            });
        }
        // 设置默认获取焦点
        popWinShare.setFocusable(true);
        // 以某个控件的x和y的偏移量位置开始显示窗口
        popWinShare.showAsDropDown(super.headRightBtn, 0, 10);
        // 如果窗口存在，则更新
        popWinShare.update();
    }

    /**
     * Title: BarcodeActionListener Description: 条码扫描完成触发Enter事件
     */
    class BarcodeActionListener implements OnEditorActionListener {

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm.isActive()) {
                    imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
                }
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (!barcodeInputByManual) {
                        scanningBarcode();
                    } else {
                        etBarcode.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, etColorCode.getLeft() + 5, etColorCode.getTop() + 5, 0));
                        etBarcode.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, etColorCode.getLeft() + 5, etColorCode.getTop() + 5, 0));
                    }
                } else if (event != null && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (!barcodeInputByManual) {
                        scanningBarcode();
                    } else {
                        etBarcode.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, etColorCode.getLeft() + 5, etColorCode.getTop() + 5, 0));
                        etBarcode.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, etColorCode.getLeft() + 5, etColorCode.getTop() + 5, 0));
                    }
                    return true;
                } else if (event != null && event.getAction() == KeyEvent.ACTION_UP) {
                    return true;
                }
            }
            return false;
        }

    }

    /**
     * Title: OnClickLintener Description: 菜单选项的点击监听
     */
    class OnClickLintener implements OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
            // 切换条码录入方式
                case R.id.layout_switch_input_mode:
                    if (inputType == 1) {
                        if (barcodeInputByManual) {
                            popWinShare.tvInput.setText("货号录入");
                            llColorSize.setVisibility(View.GONE);
                            // 取消触屏事件
                            resetBarcode();
                            etBarcode.setOnTouchListener(null);
                            SpannableString s = new SpannableString("请输入条码");
                            etBarcode.setHint(s);
                            etBarcode.setCompoundDrawables(null, null, null, null);
                            barcodeInputByManual = false;
                        } else {
                            popWinShare.tvInput.setText("条码录入");
                            llColorSize.setVisibility(View.VISIBLE);
                            // 设置触屏事件
                            etBarcode.setFocusableInTouchMode(true);
                            etBarcode.setOnTouchListener(tl);
                            // 选择的样式
                            resetBarcode();
                            Drawable drawable = getResources().getDrawable(R.drawable.input_bg);
                            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight()); // 此为必须写的
                            etBarcode.setCompoundDrawables(null, null, drawable, null);
                            SpannableString s = new SpannableString("请输入货号");
                            etBarcode.setHint(s);
                            barcodeInputByManual = true;
                        }
                    }
                    popWinShare.dismiss();
                    break;
                // 显示校验差异
                case R.id.layout_barcode_verification_different:
                    // 显示差异
                    showDifferent();
                    break;
                // 导出校验的Excel
                case R.id.layout_export_excel:
                    // 选择导出方式
                    showSingleAlertDialog(v);
                    break;
                default:
                    popWinShare.dismiss();
                    break;
            }
        }
    }


}
