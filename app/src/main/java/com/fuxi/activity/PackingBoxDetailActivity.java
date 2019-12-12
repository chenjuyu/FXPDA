package com.fuxi.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.json.JSONArray;
import org.json.JSONObject;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebSettings.TextSize;
import android.webkit.WebSettings.ZoomDensity;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import com.fuxi.adspter.PackingBoxDetailAdapter;
import com.fuxi.dao.GeneralPackingBoxNoDao;
import com.fuxi.dao.ParamerDao;
import com.fuxi.main.R;
import com.fuxi.util.Logger;
import com.fuxi.util.LoginParameterUtil;
import com.fuxi.util.ParamterFileUtil;
import com.fuxi.util.Tools;
import com.fuxi.vo.GeneralPackingBoxNo;
import com.fuxi.vo.Paramer;
import com.fuxi.vo.RequestVo;
import com.fuxi.widget.FontTextView;

/**
 * Title: SalesDetailActivity Description: 装箱单明细活动界面
 * 
 * @author LYJ
 * 
 */
public class PackingBoxDetailActivity extends BaseWapperActivity implements OnItemLongClickListener {

    private static final String TAG = "PackingBoxDetailActivity";
    private static final String AnalyticalBarcode = "/select.do?analyticalBarcode";
    private static final String dataPath = "/packing.do?getData";
    private static final String updatePacking = "/packing.do?updatePacking";
    private static final String getAlreadyPackingBoxByBoxNo = "/packing.do?getAlreadyPackingBoxByBoxNo";
    private static final String releasePacking = "/packing.do?releasePacking";
    private static final String getAlreadyPackingBoxCount = "/packing.do?getAlreadyPackingBoxCount";
    private static final String savePath = "/packing.do?savePacking";
    private static final String completePath = "/packing.do?completePacking";
    private static String strIp;

    private List<String> boxNos = new ArrayList<String>(); // 存储箱号
    private List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
    private List<Map<String, Object>> tempList = new ArrayList<Map<String, Object>>();
    private List<Map<String, Object>> alreadyList = new ArrayList<Map<String, Object>>();
    private Map<String, String> codeMap = new HashMap<String, String>();
    // private List<Map<String, Object>> subList = new ArrayList<Map<String, Object>>();// 提交的数量集合
    private ParamerDao paramerDao = new ParamerDao(this);
    private GeneralPackingBoxNoDao boxNoDao = new GeneralPackingBoxNoDao(this);
    private PackingBoxDetailAdapter adapter;
    private AlertDialog alertDialog;

    private ListView lvDataDetail;
    private EditText etNo;
    private EditText etCustomer;
    private EditText etBarcode;
    private EditText etQty;
    private EditText etBoxNo;
    private EditText et_count; // 长按修改装箱的数量
    private TextView tvBoxCount;
    private TextView tvDocCount;
    private TextView tvScanACount;
    private TextView tvScanCount;
    private TextView tvBoxACount;
    private TextView tvChangeBox;
    private TextView tvComplete;
    private FontTextView ftvShowBoxs;
    private WebView mWebView;

    private String no;
    private String id;
    private String packingBoxId;
    private String packingBoxNo;
    private String departmentId;
    private String memo; // 备注信息
    private String type;
    private String customerId;
    private String employeeId;
    private String brandId;
    private String customer;
    private String boxNo; // 箱号
    // private String printBoxNo; //打印的箱号
    private String goodsTypeCode; // 货品类别编码
    private String brandCode; // 主表品牌的编码
    private String quantitySum; // 主表品牌的编码
    private String printIp; // 打印机IP
    private String printPort; // 打印机端口
    private String printer; // 打印机
    // 临时变量
    private String tgoodsId;
    private String tcolorId;
    private String tsizeId;
    private String tretailSales;
    private String tbarcode; // 存储条码

    private int alreadyBoxing; // 已经装箱的数量
    private int tcount;// 长按修改对应货品的装箱数量(实际装箱的数量)
    private int tmaxCount;// 长按修改对应货品的装箱数量(可修改的最大数量)


    private Paramer printPIp;
    private Paramer printPPort;
    private Paramer pPrinter;

    private boolean isComplete = false; // 是否点击完成装箱
    private boolean readOnly = false;

    /**
     * 获取单据详细信息
     */
    private void getData() {
        RequestVo vo = new RequestVo();
        vo.requestUrl = dataPath;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("relationId", id);
        map.put("relationType", String.valueOf(LoginParameterUtil.packingBoxRelationType));
        vo.requestDataMap = map;
        super.getDataFromServer(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    if (retObj.getBoolean("success")) {
                        boolean inPacking = retObj.getBoolean("obj");
                        if (!inPacking) {
                            JSONObject rs = retObj.getJSONObject("attributes");
                            // 获取箱号集合
                            if (!rs.isNull("boxNoList")) {
                                JSONArray array = rs.getJSONArray("boxNoList");
                                for (int i = 0; i < array.length(); i++) {
                                    JSONObject json = array.getJSONObject(i);
                                    String tboxNo = json.getString("BoxNo");
                                    boxNos.add(tboxNo);
                                    // 显示最大的箱号
                                    if (i == array.length() - 1) {
                                        boxNo = tboxNo;
                                        etBoxNo.setText(boxNo);
                                    }
                                }
                            }
                            // 获取货品类别编码的自定义编码
                            if (!rs.isNull("codeMap")) {
                                JSONObject json = rs.getJSONObject("codeMap");
                                Iterator<String> ite = json.keys();
                                while (ite.hasNext()) {
                                    String key = ite.next().toString();
                                    String value = json.getString(key);
                                    codeMap.put(key, value);
                                }
                            }
                            no = rs.getString("No");
                            alreadyBoxing = Integer.parseInt(rs.getString("alreadyBoxing"));
                            quantitySum = rs.getString("QuantitySum");
                            packingBoxId = rs.getString("PackingBoxID");
                            packingBoxNo = rs.getString("PackingBoxNo");
                            JSONArray array = rs.getJSONArray("detailList");
                            for (int i = 0; i < array.length(); i++) {
                                Map temp = new HashMap();
                                Map tmap = new HashMap();
                                JSONObject json = array.getJSONObject(i);
                                temp.put("GoodsID", json.getString("GoodsID"));
                                temp.put("ColorID", json.getString("ColorID"));
                                temp.put("SizeID", json.getString("SizeID"));
                                temp.put("Color", json.getString("Color"));
                                temp.put("Size", json.getString("Size"));
                                temp.put("Quantity", Math.abs(json.getInt("Quantity")));
                                temp.put("GoodsCode", json.getString("GoodsCode"));
                                temp.put("GoodsName", json.getString("GoodsName"));
                                temp.put("ColorCode", json.getString("ColorCode"));
                                temp.put("SizeCode", json.getString("SizeCode"));
                                temp.put("RetailSales", json.getString("RetailSales"));
                                temp.put("Qty", json.getString("Qty"));
                                temp.put("BoxNo", json.getString("BoxNo"));
                                tmap.putAll(temp);
                                dataList.add(temp);
                                tempList.add(tmap);
                            }
                            // 去除已经装箱的单据明细记录(初始化处理)
                            // InitialPackingBox();
                            // 计算合计数量
                            countTotal();
                            adapter.refresh();
                        } else {
                            // 此单据正在装箱
                            inPackingBox();
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(PackingBoxDetailActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 扫描添加集合
     */
    private void addItem() {
        String barcode = etBarcode.getText().toString().trim();
        if (barcode == null || "".equals(barcode) || "null".equalsIgnoreCase(barcode)) {
            barcode = tbarcode;
            etBarcode.setText(barcode);
        }
        if (barcode == null || barcode.isEmpty()) {
            Toast.makeText(PackingBoxDetailActivity.this, "货品条码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        String qtyStr = etQty.getText().toString().trim();
        if (qtyStr == null || qtyStr.isEmpty()) {
            etQty.setText("1");
            qtyStr = "1";
        }
        if (Integer.parseInt(qtyStr) == 0) {
            Toast.makeText(PackingBoxDetailActivity.this, "货品数量不能为0", Toast.LENGTH_SHORT).show();
            return;
        }
        int qty = Integer.parseInt(qtyStr);
        analyticalBarcode(barcode, qty);
    }

    /**
     * 去除boxNos中的重复箱号
     */
    private List<String> mergeData(List<String> boxNos) {
        for (int i = 0; i < boxNos.size() - 1; i++) {
            String tboxNo = boxNos.get(i);
            for (int j = boxNos.size() - 1; j > i; j--) {
                String ttboxNo = boxNos.get(j);
                if (tboxNo.equals(ttboxNo)) {
                    boxNos.remove(ttboxNo);
                }
            }
        }
        return boxNos;
    }

    /**
     * 保存装箱扫描记录
     */
    private void save(final String goodsId, final String colorId, final String sizeId, final String qty, String retailSales) {
        if (!Tools.isNumeric(qty)) {
            Toast.makeText(PackingBoxDetailActivity.this, "装箱数量非法，请修改装箱数量", Toast.LENGTH_SHORT).show();
            etQty.selectAll();
            return;
        }
        int quantity = 1;
        try {
            quantity = Integer.parseInt(qty);
        } catch (NumberFormatException e1) {
            Toast.makeText(PackingBoxDetailActivity.this, "装箱数量非法", Toast.LENGTH_SHORT).show();
            etQty.selectAll();
            return;
        }
        if (quantity == 0) {
            Toast.makeText(PackingBoxDetailActivity.this, "装箱数量不能为0", Toast.LENGTH_SHORT).show();
            etQty.selectAll();
            return;
        }
        // 获取箱号
        boxNo = etBoxNo.getText().toString();
        boolean flag = checkSave(goodsId, colorId, sizeId, quantity);
        if (flag) {
            RequestVo vo = new RequestVo();
            vo.requestUrl = savePath;
            vo.context = this;
            HashMap map = new HashMap();
            map.put("relationId", id);
            map.put("relationNo", no);
            map.put("boxNo", boxNo);
            map.put("retailSales", retailSales);
            map.put("customerId", customerId);
            map.put("departmentId", departmentId);
            map.put("employeeId", employeeId);
            map.put("memo", memo);
            map.put("brandId", brandId);
            map.put("type", type);
            map.put("relationType", String.valueOf(LoginParameterUtil.packingBoxRelationType));
            map.put("packingBoxId", packingBoxId);
            map.put("goodsId", goodsId);
            map.put("colorId", colorId);
            map.put("sizeId", sizeId);
            map.put("qty", qty);
            vo.requestDataMap = map;
            super.getDataFromServer(vo, new DataCallback<JSONObject>() {
                @Override
                public void processData(JSONObject retObj, boolean paramBoolean) {
                    try {
                        if (retObj == null) {
                            return;
                        }
                        if (retObj.getBoolean("success")) {
                            JSONObject jsonObject = (JSONObject) retObj.get("obj");
                            packingBoxId = jsonObject.getString("packingBoxId");
                            packingBoxNo = jsonObject.getString("packingBoxNo");
                            alreadyBoxing = Integer.parseInt(jsonObject.getString("alreadyBoxing"));
                            // 去除已经装箱的记录
                            removeSaveItem(goodsId, colorId, sizeId, qty);
                            // 将箱号标记为已使用
                            boxNoDao.update(new GeneralPackingBoxNo(boxNo, customerId, Tools.dateToString(new Date()), 1));
                            // 重新计算数量
                            countTotal();
                            // 刷新ListView集合
                            adapter.refresh();
                            Toast.makeText(PackingBoxDetailActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                            reset();
                        } else {
                            Toast.makeText(PackingBoxDetailActivity.this, "保存失败", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(PackingBoxDetailActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                        Logger.e(TAG, e.getMessage());
                    }
                }
            });
        }
    }

    /**
     * 检查保存的数量
     * 
     * @param goodsId
     * @param colorId
     * @param sizeId
     * @param qty
     * @return
     */
    private boolean checkSave(String goodsId, String colorId, String sizeId, int qty) {
        // 条码是否正确的标志
        boolean flag = false;
        for (int i = 0; i < tempList.size(); i++) {
            Map<String, Object> map = tempList.get(i);
            String tGoodsId = String.valueOf(map.get("GoodsID"));
            String tColorId = String.valueOf(map.get("ColorID"));
            String tSizeId = String.valueOf(map.get("SizeID"));
            if (tGoodsId.equals(goodsId) && tColorId.equals(colorId) && tSizeId.equals(sizeId)) {
                int quantity = Integer.parseInt(String.valueOf(map.get("Quantity")));
                // 计算数量
                String tQtyStr = String.valueOf(map.get("Qty"));
                if (null == tQtyStr || tQtyStr.isEmpty() || "null".equalsIgnoreCase(tQtyStr)) {
                    tQtyStr = "0";
                }
                int tQty = Integer.parseInt(tQtyStr);
                if (quantity <= 0) {
                    Toast.makeText(PackingBoxDetailActivity.this, "装箱数量不能大于单据对应的货品数", Toast.LENGTH_SHORT).show();
                    etQty.requestFocus();
                    etQty.selectAll();
                } else {
                    if (tQty + qty > quantity) {
                        Toast.makeText(PackingBoxDetailActivity.this, "装箱数量不能大于单据对应的货品数", Toast.LENGTH_SHORT).show();
                        etQty.requestFocus();
                        etQty.selectAll();
                    } else {
                        flag = true;
                        break;
                    }
                }
            }
        }
        return flag;
    }

    /**
     * 完成装箱操作
     */
    private void complete() {
        if (boxNos.size() == 0) {
            Toast.makeText(PackingBoxDetailActivity.this, "当前无箱号，完成装箱失败", Toast.LENGTH_LONG).show();
            return;
        }
        synchronized (this) {
            // 先执行换箱操作
            String tboxNo = boxNos.get(boxNos.size() - 1);
            boolean flag = checkBoxNoIsUsed(tboxNo);
            boolean inuse = false;
            GeneralPackingBoxNo generalPackingBoxNo = boxNoDao.find(customerId, Tools.dateToString(new Date()));
            if (generalPackingBoxNo != null && generalPackingBoxNo.getType() > 0) {
                inuse = true;
            }
            if (flag && inuse) {
                final String printBoxNo = etBoxNo.getText().toString();
                if (boxNos.size() > 0) {
                    boxNo = boxNos.get(boxNos.size() - 1);
                }
                // 生成新的箱号
                boxNo = Tools.generaterNextNumber(boxNo);
                boxNoDao.update(new GeneralPackingBoxNo(boxNo, customerId, Tools.dateToString(new Date()), 0));
                // 记录箱号
                boxNos.add(boxNo);
                // 显示新的箱号
                etBoxNo.setText(boxNo);
                selectPrint(printBoxNo);
            }
        }
        // 执行完成装箱操作
        RequestVo vo = new RequestVo();
        vo.requestUrl = completePath;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("relationId", id);
        vo.requestDataMap = map;
        super.getDataFromServer(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    if (retObj.getBoolean("success") && retObj.getBoolean("obj")) {
                        Thread.sleep(1000);
                        isComplete = true;
                        Builder dialog = new AlertDialog.Builder(PackingBoxDetailActivity.this, AlertDialog.THEME_HOLO_LIGHT);
                        dialog.setTitle("提示");
                        dialog.setMessage("单据 " + no + " 已完成装箱操作");
                        // 相当于点击确认按钮
                        dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                releasePackingBox();
                            }
                        });
                        dialog.create();
                        dialog.setCancelable(false);
                        dialog.show();
                    } else {
                        Toast.makeText(PackingBoxDetailActivity.this, "完成装箱操作失败", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(PackingBoxDetailActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 返回时释放装箱单据
     */
    private void update(String goodsId, String colorId, String sizeId, String count, String retailSales, String tboxNo) {
        RequestVo vo = new RequestVo();
        vo.requestUrl = updatePacking;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("packingBoxId", packingBoxId);
        map.put("boxNo", tboxNo);
        map.put("goodsId", goodsId);
        map.put("colorId", colorId);
        map.put("sizeId", sizeId);
        map.put("retailSales", retailSales);
        map.put("count", count);
        vo.requestDataMap = map;
        super.getDataFromServer(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    if (retObj.getBoolean("success")) {
                        alreadyBoxing = retObj.getInt("obj");
                        // 重新计算数量
                        countTotal();
                        Toast.makeText(PackingBoxDetailActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(PackingBoxDetailActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 根据箱号查询装箱明细
     * 
     * @param tboxNo
     */
    private void getAlreadyPackingBoxByBoxNo(final String tboxNo) {
        RequestVo vo = new RequestVo();
        vo.requestUrl = getAlreadyPackingBoxByBoxNo;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("packingBoxId", packingBoxId);
        map.put("relationType", String.valueOf(LoginParameterUtil.packingBoxRelationType));
        map.put("relationId", id);
        map.put("boxNo", tboxNo);
        vo.requestDataMap = map;
        super.getDataFromServer(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    if (retObj.getBoolean("success")) {
                        JSONObject jsonObject = retObj.getJSONObject("obj");
                        String talreadyBoxing = jsonObject.getString("alreadyBoxing");
                        tvScanACount.setText(talreadyBoxing);
                        tvBoxACount.setText(String.valueOf(boxNos.indexOf(tboxNo) + 1));
                        JSONArray jsonArray = jsonObject.getJSONArray("datas");
                        alreadyList.clear();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject json = jsonArray.getJSONObject(i);
                            Map temp = new HashMap();
                            Iterator<String> ite = json.keys();
                            while (ite.hasNext()) {
                                String key = ite.next().toString();
                                String value = json.getString(key);
                                temp.put(key, value);
                            }
                            alreadyList.add(temp);
                        }
                        // 界面设置为只读
                        readOnly = true;
                        tvChangeBox.setText("继续装箱");
                        etBarcode.setEnabled(false);
                        etQty.setEnabled(false);
                        // 动态刷新ListView
                        PackingBoxDetailAdapter tadapter = new PackingBoxDetailAdapter(PackingBoxDetailActivity.this, alreadyList);
                        lvDataDetail.setAdapter(tadapter);
                        tadapter.refresh();
                        // 禁用扫描区域和禁止改
                    }
                } catch (Exception e) {
                    Toast.makeText(PackingBoxDetailActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 返回时释放装箱单据
     */
    private void releasePackingBox() {
        RequestVo vo = new RequestVo();
        vo.requestUrl = releasePacking;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("relationId", id);
        vo.requestDataMap = map;
        super.getDataFromServer(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    if (retObj.getBoolean("success")) {
                        Thread.sleep(1000);
                        if (isComplete) {
                            selectPrint(null);
                        }
                        Intent intent = new Intent(PackingBoxDetailActivity.this, PackingBoxActivity.class);
                        startActivity(intent);
                        finish();
                    }
                } catch (Exception e) {
                    Toast.makeText(PackingBoxDetailActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 获取已经装箱的货品的数量
     * 
     * @param goodsId
     * @param colorId
     * @param sizeId
     * @param boxNo
     */
    private void getAlreadyPackingBoxCount(String goodsId, String colorId, String sizeId, String tboxNo, final int ucount) {
        RequestVo vo = new RequestVo();
        vo.requestUrl = getAlreadyPackingBoxCount;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("packingBoxId", packingBoxId);
        map.put("relationId", id);
        map.put("boxNo", tboxNo);
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
                    if (retObj.getBoolean("success")) {
                        tcount = retObj.getInt("obj");
                        tmaxCount = retObj.getInt("obj") + ucount;
                        et_count.setText(String.valueOf(tcount));
                        et_count.setSelection(String.valueOf(tcount).length());
                    }
                } catch (Exception e) {
                    Toast.makeText(PackingBoxDetailActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 根据客户名称自动选择打印格式
     * 
     * @param printBoxNo
     */
    private void selectPrint(final String printBoxNo) {
        // 关联的单据名称
        String tableName = null;
        String printPath = null;
        if (LoginParameterUtil.packingBoxRelationType == 0) {
            tableName = "Sales";
        } else {
            tableName = "StockOut";
        }
        // 唯品会装箱单
        if (customer.contains("丨")) {
            if (printBoxNo == null || "".equals(printBoxNo) || "null".equalsIgnoreCase(printBoxNo)) {
                printPath = strIp + "/common.do?packingBoxPrintVip&id=" + packingBoxId + "&tableName=" + tableName + "&customer=" + customer + "&boxNo=" + printBoxNo + "&docType=唯品会装箱清单&userName=&printIp=" + printIp + "&printPort=" + printPort + "&printer=" + printer;
            } else {
                printPath = strIp + "/common.do?packingBoxDetailPrintVip&id=" + packingBoxId + "&tableName=" + tableName + "&customer=" + customer + "&boxNo=" + printBoxNo + "&docType=唯品会装箱清单&userName=&printIp=" + printIp + "&printPort=" + printPort + "&printer=" + printer;
            }
            // 打印
            print(printPath);
        } else {
            // 不打印单价和金额
            if (customer.contains("-")) {
                if (printBoxNo == null || "".equals(printBoxNo) || "null".equalsIgnoreCase(printBoxNo)) {
                    printPath = strIp + "/common.do?packingBoxPrintNoPrice&id=" + packingBoxId + "&tableName=" + tableName + "&customer=" + customer + "&boxNo=" + printBoxNo + "&docType=装箱单&userName=&printIp=" + printIp + "&printPort=" + printPort + "&printer=" + printer;
                } else {
                    printPath = strIp + "/common.do?packingBoxDetailPrintNoPrice&id=" + packingBoxId + "&tableName=" + tableName + "&customer=" + customer + "&boxNo=" + printBoxNo + "&docType=装箱明细&userName=&printIp=" + printIp + "&printPort=" + printPort + "&printer=" + printer;
                }
                // 打印
                print(printPath);
            } else {
                if (printBoxNo == null || "".equals(printBoxNo) || "null".equalsIgnoreCase(printBoxNo)) {
                    printPath = strIp + "/common.do?packingBoxPrintHasPrice&id=" + packingBoxId + "&tableName=" + tableName + "&customer=" + customer + "&boxNo=" + printBoxNo + "&docType=装箱单&userName=&printIp=" + printIp + "&printPort=" + printPort + "&printer=" + printer;
                } else {
                    printPath = strIp + "/common.do?packingBoxDetailPrintHasPrice&id=" + packingBoxId + "&tableName=" + tableName + "&customer=" + customer + "&boxNo=" + printBoxNo + "&docType=装箱明细&userName=&printIp=" + printIp + "&printPort=" + printPort + "&printer=" + printer;
                }
                // 打印
                print(printPath);
            }
        }
    }

    private synchronized void print(final String printPath) {
        WebSettings settings = mWebView.getSettings();
        settings.setTextSize(TextSize.SMALLEST);
        // 自适应屏幕
        settings.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
        settings.setUseWideViewPort(true);// 关键点
        // // 设置可以支持缩放
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true); // 启用内置缩放装置
        // 去掉缩放按钮
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        // //扩大比例的缩放
        // settings.setUseWideViewPort(true);
        // 支持JS
        settings.setJavaScriptEnabled(true);
        // 设置 缓存模式
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        // 开启 DOM storage API 功能
        settings.setDomStorageEnabled(true);
        // 自适应屏幕
        settings.setLoadWithOverviewMode(true);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int mDensity = metrics.densityDpi;
        if (mDensity == 240) {
            settings.setDefaultZoom(ZoomDensity.FAR);
        } else if (mDensity == 160) {
            settings.setDefaultZoom(ZoomDensity.MEDIUM);
        } else if (mDensity == 120) {
            settings.setDefaultZoom(ZoomDensity.CLOSE);
        } else if (mDensity == DisplayMetrics.DENSITY_XHIGH) {
            settings.setDefaultZoom(ZoomDensity.FAR);
        } else if (mDensity == DisplayMetrics.DENSITY_TV) {
            settings.setDefaultZoom(ZoomDensity.FAR);
        } else {
            settings.setDefaultZoom(ZoomDensity.MEDIUM);
        }
        mWebView.loadUrl(printPath);
        // mWebView.loadUrl("javascript:print();");
    }

    /**
     * 获取服务器连接地址
     */
    private void getRequestPath() {
        try {
            {
                // 对外版本
                Paramer ip = paramerDao.find("ip");
                if (null != ip) {
                    strIp = ip.getValue();
                } else {
                    Properties userInfo = ParamterFileUtil.getIpInfo(this);
                    if (null != userInfo) {
                        strIp = userInfo.getProperty("path");
                        if (strIp != null && !"".equals(strIp) && "null".equalsIgnoreCase(strIp)) {
                            // 保存IP到服务器缓存
                            ip = new Paramer("ip", strIp);
                            paramerDao.insert(ip);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Logger.e(TAG, e);
        }
    }

    /**
     * 完成装箱操作提示
     */
    private void completePackingBox() {
        // 询问是否删除
        Builder dialog = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
        dialog.setTitle("提示");
        dialog.setMessage("单据 " + no + " 已完成装箱操作");
        // 相当于点击确认按钮
        dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                complete();
            }
        });
        dialog.create();
        dialog.setCancelable(false);
        dialog.show();
    }

    /**
     * 完成装箱操作提示
     */
    private void inPackingBox() {
        // 询问是否删除
        Builder dialog = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
        dialog.setTitle("提示");
        dialog.setMessage("单据 " + no + " 正在进行装箱操作，请选择其它单据");
        // 相当于点击确认按钮
        dialog.setPositiveButton("返回", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                releasePackingBox();
            }
        });
        dialog.create();
        dialog.setCancelable(false);
        dialog.show();
    }

    /**
     * 询问是否完成装箱操作
     */
    private void completePackingBoxOrNot() {
        Builder dialog = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
        dialog.setTitle("提示");
        dialog.setMessage("单据 " + no + " 尚有货品未装箱，确定完成装箱操作？");
        // 相当于点击确认按钮
        dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                complete();
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
     * 去除已经装箱的集合
     */
    private void removeSaveItem(String goodsId, String colorId, String sizeId, String qty) {
        // 未装箱集合
        for (int i = 0; i < dataList.size(); i++) {
            Map<String, Object> map = dataList.get(i);
            String tgoodsId = String.valueOf(map.get("GoodsID"));
            String tcolorId = String.valueOf(map.get("ColorID"));
            String tsizeId = String.valueOf(map.get("SizeID"));
            if (goodsId.equals(tgoodsId) && colorId.equals(tcolorId) && sizeId.equals(tsizeId)) {
                int tquantity = Integer.parseInt(String.valueOf(map.get("Quantity")));
                int tqty = Integer.parseInt(String.valueOf(map.get("Qty")));
                int ttqty = tqty + Integer.parseInt(qty);
                if (ttqty >= tquantity) {
                    tbarcode = null;
                    etBarcode.setText(null);
                    // dataList.remove(map);
                    // i--;
                    map.put("Qty", ttqty);
                } else {
                    map.put("Qty", ttqty);
                }
                map.put("BoxNo", boxNo);
            }
        }
        adapter.refresh();
        // 已装箱集合
        for (int i = 0; i < tempList.size(); i++) {
            Map<String, Object> map = tempList.get(i);
            String tgoodsId = String.valueOf(map.get("GoodsID"));
            String tcolorId = String.valueOf(map.get("ColorID"));
            String tsizeId = String.valueOf(map.get("SizeID"));
            if (goodsId.equals(tgoodsId) && colorId.equals(tcolorId) && sizeId.equals(tsizeId)) {
                String oldboxNo = String.valueOf(map.get("Quantity"));
                int quantity = Integer.parseInt(String.valueOf(map.get("Qty")));
                int tquantity = quantity + Integer.parseInt(qty);
                map.put("Qty", tquantity);
                if (oldboxNo == null || "".equals(oldboxNo) || "null".equalsIgnoreCase(oldboxNo)) {
                    map.put("BoxNo", boxNo);
                } else {
                    map.put("BoxNo", oldboxNo + "," + boxNo);
                }
            }
        }
    }

    /**
     * 去除已经装箱的单据明细记录
     */
    private void InitialPackingBox() {
        // 处理dataList -- > 去除已经装箱的集合
        for (int i = 0; i < dataList.size(); i++) {
            Map<String, Object> map = dataList.get(i);
            int quantity = Integer.parseInt(String.valueOf(map.get("Quantity")));
            int qty = Integer.parseInt(String.valueOf(map.get("Qty")));
            if (qty > 0) {
                if (qty >= quantity) {
                    dataList.remove(map);
                    i--;
                } else {
                    // map.put("Quantity", String.valueOf(quantity-qty));
                }
            }
            // map.put("Qty", "0");
        }
    }

    /**
     * 重置扫码区
     */
    private void reset() {
        etQty.setText("1");
        etBarcode.postDelayed(new Runnable() {
            @Override
            public void run() {
                etBarcode.requestFocus();
                int index = etBarcode.getText().toString().trim().length();
                etBarcode.setSelection(index);
                etBarcode.selectAll();
            }
        }, 300);
    }

    /**
     * 生成箱号
     */
    private void generateBoxNo() {
        GeneralPackingBoxNo generalPackingBoxNo = boxNoDao.find(customerId, Tools.dateToString(new Date()));
        if (generalPackingBoxNo != null) {
            if (generalPackingBoxNo.getType() > 0) {
                boxNo = Tools.generaterNextNumber(boxNo);
            }
            boxNo = generalPackingBoxNo.getBoxNo();
            boxNoDao.update(new GeneralPackingBoxNo(boxNo, customerId, Tools.dateToString(new Date()), 0));
        } else {
            boxNoDao.delete(Tools.dateToString(new Date()));
            // 日两位
            StringBuffer sb = new StringBuffer();
            SimpleDateFormat yyyymmddhhmmss = new SimpleDateFormat("dd");
            String code = yyyymmddhhmmss.format(new Date());
            boxNo = sb.append(removeNullStr(goodsTypeCode)).append(removeNullStr(brandCode)).append(removeNullStr(code)).append("-").append("001").toString();
            boxNoDao.insert(new GeneralPackingBoxNo(boxNo, customerId, Tools.dateToString(new Date()), 0));
        }
        etBoxNo.setText(boxNo);
        // 记录箱号
        if (!boxNos.contains(boxNo)) {
            boxNos.add(boxNo);
        }
    }

    /**
     * 去除null字符串
     * 
     * @param key
     * @return
     */
    private String removeNullStr(String key) {
        String result = "";
        if (null == key || "null".equalsIgnoreCase(key)) {
            return result;
        } else {
            return key;
        }
    }

    /**
     * 统计校验时扫描的货品总数量
     */
    private void countTotal() {
        int qtySum = 0, tquantitySum = 0;
        // 已经装箱数量
        for (int j = 0; j < tempList.size(); j++) {
            String qty = String.valueOf(tempList.get(j).get("Qty"));
            if (null == qty || "".equals(qty) || "null".equalsIgnoreCase(qty)) {
                qty = "0";
            }
            int num = Integer.parseInt(qty);
            qtySum += num;
        }
        // 未装箱剩余数量
        for (int j = 0; j < dataList.size(); j++) {
            String qty = String.valueOf(dataList.get(j).get("Quantity"));
            if (null == qty || "".equals(qty) || "null".equalsIgnoreCase(qty)) {
                qty = "0";
            }
            int num = Integer.parseInt(qty);
            tquantitySum += num;
        }
        // 计算箱数
        tvBoxCount.setText(String.valueOf(boxNos.size()));
        // 单箱
        tvBoxACount.setText(String.valueOf(boxNos.size()));
        tvScanACount.setText(String.valueOf(alreadyBoxing));
        // 扫描的总数
        tvScanCount.setText(String.valueOf(qtySum));
        // 单据总数
        tvDocCount.setText(quantitySum);
        // 单箱数大于1时显示切换箱号按钮[查询箱号中的货品]
        if (boxNos.size() < 2) {
            ftvShowBoxs.setVisibility(View.GONE);
        } else {
            ftvShowBoxs.setVisibility(View.VISIBLE);
        }
        // 去除boxNos中的重复箱号
        boxNos = mergeData(boxNos);
        // 完成装箱操作时提示
        if (tquantitySum == 0) {
            // completePackingBox();
        }
        // 刷新ListView集合
        adapter.refresh();
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
        map.put("Type", null);
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
                        String goodsId = obj.getString("GoodsID");
                        String colorId = obj.getString("ColorID");
                        String sizeId = obj.getString("SizeID");
                        String retailSales = obj.getString("RetailSales");
                        String tgoodsTypeCode = obj.getString("GoodsTypeCode");
                        // 判断货品类别编码是否相同
                        if (boxNo == null) {
                            goodsTypeCode = getGoodsTypeCode(tgoodsTypeCode);
                            // 重新生成新箱号
                            generateBoxNo();
                        }
                        // 累加相同的货品
                        // 条码是否正确的标志
                        boolean flag = false;
                        for (int i = 0; i < tempList.size(); i++) {
                            Map<String, Object> map = tempList.get(i);
                            String tGoodsId = String.valueOf(map.get("GoodsID"));
                            String tColorId = String.valueOf(map.get("ColorID"));
                            String tSizeId = String.valueOf(map.get("SizeID"));
                            if (tGoodsId.equals(goodsId) && tColorId.equals(colorId) && tSizeId.equals(sizeId)) {
                                int quantity = Integer.parseInt(String.valueOf(map.get("Quantity")));
                                // 计算数量
                                String tQtyStr = String.valueOf(map.get("Qty"));
                                if (null == tQtyStr || tQtyStr.isEmpty() || "null".equalsIgnoreCase(tQtyStr)) {
                                    tQtyStr = "0";
                                }
                                int tQty = Integer.parseInt(tQtyStr);
                                if (quantity <= 0) {
                                    settingVoice("noFound", getApplicationContext());
                                    Toast.makeText(PackingBoxDetailActivity.this, "条码" + barcode + "不在单据中", Toast.LENGTH_SHORT).show();
                                    etBarcode.requestFocus();
                                    etBarcode.selectAll();
                                    return;
                                } else {
                                    if (tQty + qty > quantity) {
                                        settingVoice("error", getApplicationContext());
                                        Toast.makeText(PackingBoxDetailActivity.this, "条码" + barcode + "对应的货品数不能大于单据中相应货品的数量", Toast.LENGTH_SHORT).show();
                                        etBarcode.requestFocus();
                                        etBarcode.selectAll();
                                        return;
                                    } else {
                                        flag = true;
                                        break;
                                    }
                                }
                            }
                        }
                        // 单据外的货品
                        if (!flag) {
                            // 不允许扫描单据以外的货品
                            settingVoice("noFound", getApplicationContext());
                            Toast.makeText(PackingBoxDetailActivity.this, "条码" + barcode + "不在单据中", Toast.LENGTH_SHORT).show();
                            etBarcode.requestFocus();
                            etBarcode.selectAll();
                            return;
                        } else {
                            // 实时保存
                            tgoodsId = goodsId;
                            tcolorId = colorId;
                            tsizeId = sizeId;
                            tretailSales = retailSales;
                            tbarcode = barcode;
                           if(LoginParameterUtil.EditQty)//2019装箱单明细，允许修改数量
                           {   
                            etQty.setText(getGoodsSurplusCount(goodsId, colorId, sizeId));
                            etQty.requestFocus();
                            etQty.selectAll();
                           }else{
                        	   if(!"".equals(etQty.getText().toString()) || etQty.getText().toString() !=null ){
                        	
                        	   save(tgoodsId, tcolorId, tsizeId, etQty.getText().toString(), tretailSales);
                        	   etBarcode.setText("");
                        	   etBarcode.requestFocus();
                        	   etBarcode.selectAll();
                        	   etQty.setOnEditorActionListener(null);//取消回车事件
                        	   }
                           }
                            // save(goodsId, colorId, sizeId, String.valueOf(qty), retailSales);
                        }
                    } else {
                        settingVoice("error", getApplicationContext());
                        etBarcode.requestFocus();
                        etBarcode.selectAll();
                        Toast.makeText(PackingBoxDetailActivity.this, "条码" + barcode + "无法识别", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    etBarcode.requestFocus();
                    etBarcode.selectAll();
                    Toast.makeText(PackingBoxDetailActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 获取货品剩余未装箱的数量
     * 
     * @return
     */
    private String getGoodsSurplusCount(String goodsId, String colorId, String sizeId) {
        String qty = null;
        for (int i = 0; i < dataList.size(); i++) {
            Map<String, Object> map = dataList.get(i);
            String tgoodsId = String.valueOf(map.get("GoodsID"));
            String tcolorId = String.valueOf(map.get("ColorID"));
            String tsizeId = String.valueOf(map.get("SizeID"));
            if (tgoodsId.equals(goodsId) && tcolorId.equals(colorId) && tsizeId.equals(sizeId)) {
                int quantity = Integer.parseInt(String.valueOf(map.get("Quantity")));
                int tqty = Integer.parseInt(String.valueOf(map.get("Qty")));
                qty = String.valueOf(quantity - tqty);
            }
        }
        return qty;
    }

    /**
     * 检查箱号是否已经使用(箱号是否对应有货品)
     * 
     * @param boxNo
     * @return
     */
    private boolean checkBoxNoIsUsed(String boxNo) {
        boolean flag = false;
        for (int i = 0; i < tempList.size(); i++) {
            Map<String, Object> map = tempList.get(i);
            String tBoxNo = String.valueOf(map.get("BoxNo"));
            if (tBoxNo.contains(boxNo)) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    /**
     * 点击返回时询问是否保存当前信息
     */
    private void saveOrNot() {
        // 询问是否删除
        Builder dialog = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
        dialog.setTitle("提示");
        dialog.setMessage("确定退出装箱操作并返回吗？");
        // 相当于点击确认按钮
        dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                releasePackingBox();
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
    protected void onHeadLeftButton(View v) {
        // 点击返回时返回上一页面并刷新
        saveOrNot();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            saveOrNot();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MENU) {
            // 点击菜单键触发换箱
            tvChangeBox.callOnClick();
            return true;
        }
        return super.onKeyDown(keyCode, event);
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
                    addItem();
                } else if (event != null && event.getAction() == KeyEvent.ACTION_DOWN) {
                    addItem();
                    return true;
                } else if (event != null && event.getAction() == KeyEvent.ACTION_UP) {
                    return true;
                }
            }
            return false;
        }

    }

    /**
     * Title: BarcodeActionListener Description: 修改数量后触发Enter事件
     */
    class QuantityActionListener implements OnEditorActionListener {

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm.isActive()) {
                    imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
                }
                String tqty = etQty.getText().toString();
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    save(tgoodsId, tcolorId, tsizeId, tqty, tretailSales);
                } else if (event != null && event.getAction() == KeyEvent.ACTION_DOWN) {
                    save(tgoodsId, tcolorId, tsizeId, tqty, tretailSales);
                    return true;
                } else if (event != null && event.getAction() == KeyEvent.ACTION_UP) {
                    return true;
                }
            }
            return false;
        }

    }

    /**
     * 单选提示框选择已经生成的箱号
     * 
     * @param view
     */
    private void showBoxListAlertDialog(View view) {
        final String[] items = boxNos.toArray(new String[boxNos.size()]);
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
        alertBuilder.setTitle("选择箱号");
        alertBuilder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int index) {
                boxNo = items[index];
                etBoxNo.setText(boxNo);
                // 重新获取数据
                getAlreadyPackingBoxByBoxNo(boxNo);
                // 关闭提示框
                alertDialog.dismiss();
            }
        });
        alertDialog = alertBuilder.create();
        alertDialog.show();
    }

    /**
     * 获取自定义的货品类别编码
     * 
     * @param goodsType
     * @return
     */
    private String getGoodsTypeCode(String goodsType) {
        String goodsTypeCode = goodsType;
        for (int i = 0; i < codeMap.size(); i++) {
            if (codeMap.containsKey(goodsType)) {
                goodsTypeCode = codeMap.get(goodsType);
            }
        }
        return goodsTypeCode;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.changeBox:
                String keyWord = tvChangeBox.getText().toString();
                if ("换箱".equals(keyWord)) {
                    if (boxNos.size() == 0) {
                        Toast.makeText(PackingBoxDetailActivity.this, "当前无箱号，换箱失败", Toast.LENGTH_LONG).show();
                        return;
                    }
                    String tboxNo = boxNos.get(boxNos.size() - 1);
                    boolean flag = checkBoxNoIsUsed(tboxNo);
                    boolean inuse = false;
                    GeneralPackingBoxNo generalPackingBoxNo = boxNoDao.find(customerId, Tools.dateToString(new Date()));
                    if (generalPackingBoxNo != null && generalPackingBoxNo.getType() > 0) {
                        inuse = true;
                    }
                    final String printBoxNo = etBoxNo.getText().toString();
                    if (flag && inuse) {
                        if (boxNos.size() > 0) {
                            boxNo = boxNos.get(boxNos.size() - 1);
                        }
                        // 生成新的箱号
                        boxNo = Tools.generaterNextNumber(boxNo);
                        boxNoDao.update(new GeneralPackingBoxNo(boxNo, customerId, Tools.dateToString(new Date()), 0));
                        // 记录箱号
                        boxNos.add(boxNo);
                        // 显示新的箱号
                        etBoxNo.setText(boxNo);
                        // 修改单箱显示
                        alreadyBoxing = 0;
                        tvScanACount.setText("0");
                        tvBoxACount.setText(String.valueOf(boxNos.size()));
                        // 询问是否打印前前一箱单据的装箱信息
                        Builder dialog = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
                        dialog.setTitle("提示");
                        dialog.setMessage("是否打印上一箱的装箱记录？");
                        // 相当于点击确认按钮
                        dialog.setPositiveButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {}
                        });
                        // 相当于点击取消按钮
                        dialog.setNegativeButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                selectPrint(printBoxNo);
                            }
                        });
                        dialog.create();
                        dialog.show();
                    } else if (flag && generalPackingBoxNo != null && generalPackingBoxNo.getType() == 0) {
                        boxNo = generalPackingBoxNo.getBoxNo();
                        boxNoDao.update(new GeneralPackingBoxNo(boxNo, customerId, Tools.dateToString(new Date()), 0));
                        // 记录箱号
                        boxNos.add(boxNo);
                        // 显示新的箱号
                        etBoxNo.setText(boxNo);
                        // 修改单箱显示
                        alreadyBoxing = 0;
                        tvScanACount.setText("0");
                        tvBoxACount.setText(String.valueOf(boxNos.size()));
                        // 询问是否打印前前一箱单据的装箱信息
                        Builder dialog = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
                        dialog.setTitle("提示");
                        dialog.setMessage("是否打印上一箱的装箱记录？");
                        // 相当于点击确认按钮
                        dialog.setPositiveButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {}
                        });
                        // 相当于点击取消按钮
                        dialog.setNegativeButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                selectPrint(printBoxNo);
                            }
                        });
                        dialog.create();
                        dialog.show();
                    } else {
                        Toast.makeText(PackingBoxDetailActivity.this, "箱号 " + tboxNo + " 尚未使用，请勿重复生成", Toast.LENGTH_LONG).show();
                    }
                } else if ("继续装箱".equals(keyWord)) {
                    tvScanACount.setText(String.valueOf(alreadyBoxing));
                    tvBoxACount.setText(String.valueOf(boxNos.size()));
                    readOnly = false;
                    tvChangeBox.setText("换箱");
                    etBarcode.setEnabled(true);
                    etQty.setEnabled(true);
                    etBarcode.setFocusableInTouchMode(true);
                    etQty.setFocusableInTouchMode(true);
                    // 刷新ListView
                    lvDataDetail.setAdapter(adapter);
                    adapter.refresh();
                    reset();
                }
                break;
            case R.id.complete:
                // 执行完成操作
                String scanCount = tvScanCount.getText().toString();
                String docCount = tvDocCount.getText().toString();
                if (!scanCount.equals(docCount)) {
                    // 提示是否完成装箱
                    completePackingBoxOrNot();
                } else {
                    complete();
                }
                break;
            case R.id.showBoxs:
                // 选择其它箱
                showBoxListAlertDialog(v);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
        }
        switch (v.getId()) {
            case R.id.barcode:
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    addItem();
                    return true;
                }
            default:
                return false;
        }
    }

    @Override
    protected void loadViewLayout() {
        setContentView(R.layout.activity_packing_box_detail);
        setTitle("装箱单(明细)");
        setHeadRightVisibility(View.INVISIBLE);
    }

    @Override
    protected void processLogic() {
        boolean addRight = LoginParameterUtil.packingBoxRightMap.get("AddRight");
        boolean modifyRight = LoginParameterUtil.packingBoxRightMap.get("ModifyRight");
        if (addRight && modifyRight) {
            Bundle bundle = this.getIntent().getExtras();
            if (null != bundle) {
                id = bundle.getString("ID");
                no = bundle.getString("No");
                customerId = bundle.getString("CustomerID");
                employeeId = bundle.getString("EmployeeID");
                brandCode = bundle.getString("BrandCode");
                brandId = bundle.getString("BrandID");
                departmentId = bundle.getString("DepartmentID");
                customer = bundle.getString("Customer");
                type = bundle.getString("Type");
                etNo.setText(no);
                etCustomer.setText(customer);
            }
            reset();
            // 扫码区获取焦点
            etBarcode.requestFocus();
            // 获取单据数据
            getData();
            // 获取连接IP
            getRequestPath();
            // 获取打印机配置信息
            getPrintMsg();
        } else {
            Builder dialog = new AlertDialog.Builder(PackingBoxDetailActivity.this, AlertDialog.THEME_HOLO_LIGHT);
            dialog.setTitle("提示");
            dialog.setMessage("当前暂无装箱单的新增和修改操作权限");
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
    }

    /**
     * 获取指定行的箱号
     * 
     * @param goodsId
     * @param colorId
     * @param sizeId
     * @return
     */
    private String getBoxNo(String goodsId, String colorId, String sizeId) {
        String boxNo = null;
        for (int i = 0; i < tempList.size(); i++) {
            Map<String, Object> map = tempList.get(i);
            String tgoodsId = String.valueOf(map.get("GoodsID"));
            String tcolorId = String.valueOf(map.get("ColorID"));
            String tsizeId = String.valueOf(map.get("SizeID"));
            if (goodsId.equals(tgoodsId) && colorId.equals(tcolorId) && sizeId.equals(tsizeId)) {
                boxNo = String.valueOf(map.get("BoxNo"));
            }
        }
        return boxNo;
    }

    /**
     * 获取系统设置中的打印机IP和端口
     */
    private void getPrintMsg() {
        // 获取显示值
        printPIp = paramerDao.find("printIp");
        printPPort = paramerDao.find("printPort");
        pPrinter = paramerDao.find("printer");
        // 非空判断并绑定显示值
        if (null != printPIp) {
            printIp = printPIp.getValue();
        } else {
            Properties userInfo = ParamterFileUtil.getIpInfo(this);
            if (null != userInfo) {
                printIp = userInfo.getProperty("printIp");
            }
        }
        if (null != printPPort) {
            printPort = printPPort.getValue();
        } else {
            Properties userInfo = ParamterFileUtil.getIpInfo(this);
            if (null != userInfo) {
                printPort = userInfo.getProperty("printPort");
            }
        }
        if (null != pPrinter) {
            printer = pPrinter.getValue();
        } else {
            Properties userInfo = ParamterFileUtil.getIpInfo(this);
            if (null != userInfo) {
                printer = userInfo.getProperty("printer");
            }
        }
    }



    @Override
    protected void setListener() {
        adapter = new PackingBoxDetailAdapter(this, dataList);
        lvDataDetail.setAdapter(adapter);
        lvDataDetail.setOnItemLongClickListener(this);
        etBarcode.setOnEditorActionListener(new BarcodeActionListener());
        etQty.setOnEditorActionListener(new QuantityActionListener());
        etBarcode.setOnKeyListener(this);
        tvChangeBox.setOnClickListener(this);
        tvComplete.setOnClickListener(this);
        ftvShowBoxs.setOnClickListener(this);
    }

    @Override
    protected void findViewById() {
        lvDataDetail = (ListView) findViewById(R.id.dataDetail);
        etNo = (EditText) findViewById(R.id.no);
        etCustomer = (EditText) findViewById(R.id.customer);
        etBarcode = (EditText) findViewById(R.id.barcode);
        etQty = (EditText) findViewById(R.id.qty);
        etBoxNo = (EditText) findViewById(R.id.boxNo);
        tvBoxCount = (TextView) findViewById(R.id.boxCount);
        tvDocCount = (TextView) findViewById(R.id.docCount);
        tvScanACount = (TextView) findViewById(R.id.scanACount);
        tvScanCount = (TextView) findViewById(R.id.scanCount);
        tvBoxACount = (TextView) findViewById(R.id.boxACount);
        tvChangeBox = (TextView) findViewById(R.id.changeBox);
        tvComplete = (TextView) findViewById(R.id.complete);
        ftvShowBoxs = (FontTextView) findViewById(R.id.showBoxs);
        mWebView = (WebView) findViewById(R.id.myWeb);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (readOnly) {// 只读状态时取消点击事件
            Toast.makeText(PackingBoxDetailActivity.this, "当前数据为只读状态，如需修改装箱数量请点击「继续装箱」后再操作", Toast.LENGTH_SHORT).show();
            return false;
        }
        final Map<String, Object> map = dataList.get(position);
        String goodsCode = String.valueOf(map.get("GoodsCode"));
        final String goodsId = String.valueOf(map.get("GoodsID"));
        final String colorId = String.valueOf(map.get("ColorID"));
        final String sizeId = String.valueOf(map.get("SizeID"));
        final String quantity = String.valueOf(map.get("Quantity"));
        final String qty = String.valueOf(map.get("Qty"));
        final String retailSales = String.valueOf(map.get("RetailSales"));
        final String tboxNo = etBoxNo.getText().toString();
        if (tboxNo == null || "".equals(tboxNo) || "null".equals(tboxNo) || packingBoxId == null || "".equals(packingBoxId) || "null".equals(packingBoxId)) {
            Toast.makeText(PackingBoxDetailActivity.this, "货品 " + goodsCode + " 暂时无法修改， 请先进行装箱操作", Toast.LENGTH_SHORT).show();
            return false;
        }
        // 查询选择货品已经装箱的数量
        int ucount = Integer.parseInt(quantity) - Integer.parseInt(qty);
        getAlreadyPackingBoxCount(goodsId, colorId, sizeId, tboxNo, ucount);
        View v = (LinearLayout) getLayoutInflater().inflate(R.layout.menu_inventory, null);
        // 隐藏提示
        TextView tv_prompt = (TextView) v.findViewById(R.id.tv_prompt);
        tv_prompt.setVisibility(View.INVISIBLE);
        et_count = (EditText) v.findViewById(R.id.et_count);
        LinearLayout ll_meno = (LinearLayout) v.findViewById(R.id.ll_meno);
        View view1 = v.findViewById(R.id.view1);
        ll_meno.setVisibility(View.GONE);
        view1.setVisibility(View.GONE);
        // 询问是否删除
        Builder dialog = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
        dialog.setMessage("修改货品" + goodsCode + "的装箱数量 :");
        dialog.setView(v);
        // 相当于点击确认按钮
        dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String count = et_count.getText().toString();
                if ("".equals(count.trim()) || "0".equals(count.trim())) {
                    Builder dialogs = new AlertDialog.Builder(PackingBoxDetailActivity.this, AlertDialog.THEME_HOLO_LIGHT);
                    dialogs.setTitle("提示");
                    dialogs.setMessage("数量不合法,请输入小于或等于单据数量的非0");
                    // 相当于点击确认按钮
                    dialogs.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {}
                    });
                    dialogs.setCancelable(false);
                    dialogs.create();
                    dialogs.show();
                    return;
                } else {
                    if (Integer.parseInt(count) > tmaxCount) {
                        Toast.makeText(PackingBoxDetailActivity.this, "修改数量不能超过单据货品剩余最大装箱数量" + tmaxCount, Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                String upcount = String.valueOf(Integer.parseInt(count) - tcount + Integer.parseInt(qty));
                for (int i = 0; i < dataList.size(); i++) {
                    Map<String, Object> map = dataList.get(i);
                    String tgoodsId = String.valueOf(map.get("GoodsID"));
                    String tcolorId = String.valueOf(map.get("ColorID"));
                    String tsizeId = String.valueOf(map.get("SizeID"));
                    if (goodsId.equals(tgoodsId) && colorId.equals(tcolorId) && sizeId.equals(tsizeId)) {
                        map.put("Qty", upcount);
                    }
                }
                for (int i = 0; i < tempList.size(); i++) {
                    Map<String, Object> map = tempList.get(i);
                    String tgoodsId = String.valueOf(map.get("GoodsID"));
                    String tcolorId = String.valueOf(map.get("ColorID"));
                    String tsizeId = String.valueOf(map.get("SizeID"));
                    if (goodsId.equals(tgoodsId) && colorId.equals(tcolorId) && sizeId.equals(tsizeId)) {
                        map.put("Qty", upcount);
                    }
                }
                // 修改装箱单数量
                update(goodsId, colorId, sizeId, count, retailSales, tboxNo);
                // 动态更新ListView
                adapter.notifyDataSetChanged();
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
