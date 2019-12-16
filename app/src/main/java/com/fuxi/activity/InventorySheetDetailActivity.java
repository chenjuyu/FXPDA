package com.fuxi.activity;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.Manifest;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.dommy.qrcode.util.Constant;
import com.fuxi.adspter.OddAdapter;
import com.fuxi.dao.BarCodeDao;
import com.fuxi.dao.GoodsBoxBarcodeRecordDao;
import com.fuxi.main.R;
import com.fuxi.task.ImageTask;
import com.fuxi.util.Logger;
import com.fuxi.util.LoginParameterUtil;
import com.fuxi.util.Tools;
import com.fuxi.vo.BarCode;
import com.fuxi.vo.GoodsBoxBarcodeRecord;
import com.fuxi.vo.RequestVo;
import com.fuxi.widget.FontTextView;
import com.fuxi.widget.PopWinShare;
import com.google.zxing.activity.CaptureActivity;

import jxl.format.Font;

/**
 * Title: InventorySheetDetailActivity Description: 盘点单明细活动界面
 * 
 * @author LYJ
 * 
 */
public class InventorySheetDetailActivity extends BaseWapperActivity implements OnItemLongClickListener {

    // 静态常量
    private static final String TAG = "InventorySheetDetailActivity";
    private static final String method = "/inventorySheet.do?inventoryEdit";
    private static final String analyticalBarcode = "/select.do?analyticalBarcode";
    private static final String addInventory = "/inventorySheet.do?addInventory";
    private static final String inventoryAudit = "/inventorySheet.do?inventoryAudit";
    private static final String inventoryUpdateCount = "/inventorySheet.do?updateCount";
    private static final String inventoryMemo = "/inventorySheet.do?updateMemo";

    // 实例化对象
    private Handler handler = new Handler();
    private BarCodeDao barCodeDao = new BarCodeDao(this);
    private GoodsBoxBarcodeRecordDao recordDao = new GoodsBoxBarcodeRecordDao(this);

    // 控件属性
    private PopWinShare popWinShare;
    private GestureDetector gd;
    private OddAdapter oddAdapter;

    private LinearLayout ll_saomiao_div; // 扫码区
    private LinearLayout ll_split2; // 分隔符2
    private LinearLayout ll_brand; // 品牌
    private TextView tv_add; // 添加
    private TextView tv_save; // 保存
    private TextView tv_qtysum; // 合计
    private TextView tv_amount; // 总金额
    private TextView tv_barcode;
    private TextView tv_qty;
    private TextView tv_symbol;
    private EditText et_department;
    private EditText et_brand;
    private EditText et_employee; // 经手人
    private EditText et_barcode; // 条码
    private EditText et_qty; // 添加的数量
    private ImageView iv_pic; // 图片
    private FontTextView ftv_scanIcon;

    // 其它属性
    private ArrayList<HashMap<String, Object>> datas = new ArrayList<HashMap<String, Object>>();// 存储箱条码扫描记录
    private List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
    private List<Map<String, Object>> addList = new ArrayList<Map<String, Object>>();
    private List<Map<String, Object>> subList = new ArrayList<Map<String, Object>>();
    private List<Map<String, Object>> delList = new ArrayList<Map<String, Object>>();// 要删除的货品

    private ListView lv_detail; // 添加的货品集合
    private String stocktakingID; // 盘点单ID
    private String employeeId; // 经手人ID
    private String brandId; // 品牌ID
    private String auditFlag; // 盘点单的状态(是否审核)
    private String departmentid; // 部门编号
    private String memo; // 备注
    private String lastMemo = "";// 上一次的备注
    private String goodsCode; // 货品编码
    private Integer qtysum; // 总数量
    private int inputType = 1;// 装箱方式，1为散件，2为装箱
    private int boxQtySum; // 货品的总箱数(区别箱条码和散件)
    private int saveTime = 3 * 6000; // 线程触发的时间
    private boolean isAdd = false;// 是否添加的新的货品
    private boolean sendFlag = false;// 防止手工点击保存跟后台自动保存重复提交
    private boolean modifyRight = false;// 修改单据的权限
    private boolean auditRight = false;// 审核单据的权限
    private boolean isFirst = false;// 防止首次开单时扫码区消失

    /**
     * 根据盘点单单号获取盘点单明细数据
     */
    public void getData() {
        RequestVo vo = new RequestVo();
        vo.requestUrl = method;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("StocktakingID", stocktakingID);
        vo.requestDataMap = map;
        super.getDataFromServer(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    if (retObj.getBoolean("success")) {
                        JSONObject rs = retObj.getJSONObject("attributes");
                        et_department.setText(rs.getString("Department"));
                        et_brand.setText(rs.getString("Brand"));
                        brandId = rs.getString("BrandID");
                        departmentid = rs.getString("DepartmentID");
                        employeeId = rs.getString("EmployeeID");
                        et_employee.setText(rs.getString("Employee"));
                        qtysum = rs.getInt("QuantitySum");
                        boxQtySum = rs.getInt("BoxQtySum");
                        memo = rs.getString("Memo");
                        tv_qtysum.setText(qtysum.toString());
                        JSONArray array = rs.getJSONArray("detailList");
                        if (boxQtySum == 0) {
                            // 去除array中的重复集合
                            array = mergeData(array);
                        }
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
                        // 部门,经手人不可用
                        et_department.setEnabled(false);
                        et_brand.setEnabled(false);
                        et_employee.setEnabled(false);
                        oddAdapter.refresh();
                        // 计算价格
                        countTotal();
                        // 设置按钮
                        if (dataList.size() <= 0) {
                            tv_save.setVisibility(View.GONE);
                        } else {
                            tv_save.setText("审核");
                        }
                        // 判断如果单据已经审核，隐藏相关显示
                        if (rs.getBoolean("AuditFlag")) {
                            tv_save.setVisibility(View.GONE);
                            findViewById(R.id.saomiao_div).setVisibility(View.GONE);
                            findViewById(R.id.split2).setVisibility(View.GONE);
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(InventorySheetDetailActivity.this, "系统错误", Toast.LENGTH_LONG).show();
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
     * 新增盘点单
     */
    private void add() {
        String dept = et_department.getText().toString(); // 部门名称
        String employee = et_employee.getText().toString(); // 经手人名称
        String brand = et_brand.getText().toString(); // 品牌名称
        String barcodeStr = et_barcode.getText().toString().trim();
        String qty = et_qty.getText().toString(); // 添加的数量
        // 非空判断
        if (null == dept || "".equals(dept) || "".equals(departmentid)) {
            et_barcode.setText(null);
            Toast.makeText(InventorySheetDetailActivity.this, R.string.dept_null, Toast.LENGTH_SHORT).show();
            return;
        }
        // if (null == employee || "".equals(employee) || "".equals(employeeId)) {
        // et_barcode.setText(null);
        // Toast.makeText(InventorySheetDetailActivity.this,
        // R.string.employee_null, Toast.LENGTH_SHORT).show();
        // }
        // if (LoginParameterUtil.useBrandPower
        // && (null == brand || "".equals(brand) || "".equals(brandId))) {
        // et_barcode.setText(null);
        // Toast.makeText(InventorySheetDetailActivity.this, "品牌不能为空",
        // Toast.LENGTH_SHORT).show();
        // }
        if (null == barcodeStr || "".equals(barcodeStr)) {
            Toast.makeText(InventorySheetDetailActivity.this, R.string.barcode_null, Toast.LENGTH_SHORT).show();
            return;
        }
        if (null == qty || "".equals(qty)) {
            Toast.makeText(InventorySheetDetailActivity.this, R.string.qty_null, Toast.LENGTH_SHORT).show();
            return;
        }
        // 判断数量的合法性
        Integer count = Integer.parseInt(qty);
        // if (count < 1) {
        // Toast.makeText(InventorySheetDetailActivity.this,
        // R.string.illegal_quantity, Toast.LENGTH_SHORT).show();
        // return;
        // }
        // 扫码后允许提交数据
        sendFlag = true;
        // 是否添加了新的货品
        isAdd = true;
        if (inputType == 1) {// 条码
            // 添加并显示记录
            BarCode barcode = barCodeDao.find(barcodeStr, "-1", "-1");
            if (barcode == null) {
                ajaxAddItem(barcodeStr, count);
            } else {
                barcode.setQty(count);
                addItem(barcode);
            }
        } else {// 箱条码
            ajaxAddBoxItem(barcodeStr, count);
        }
    }

    /**
     * 远程连接网络解析条码
     * 
     * @param barcodeStr
     */
    public void ajaxAddItem(String barcodeStr, final int qty) {
        RequestVo vo = new RequestVo();
        vo.requestUrl = analyticalBarcode;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("BarCode", barcodeStr);
        vo.requestDataMap = map;
        super.getDataFromServer(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    if (retObj.getBoolean("success")) {
                        JSONObject barCodeInfo = retObj.getJSONObject("obj");
                        BarCode bc = new BarCode();
                        bc.setBarcode(barCodeInfo.getString("BarCode"));
                        bc.setGoodsid(barCodeInfo.getString("GoodsID"));
                        bc.setGoodscode(barCodeInfo.getString("GoodsCode"));
                        bc.setColorid(barCodeInfo.getString("ColorID"));
                        bc.setColorcode(barCodeInfo.getString("ColorCode"));
                        bc.setColorname(barCodeInfo.getString("ColorName"));
                        bc.setSizeid(barCodeInfo.getString("SizeID"));
                        bc.setSizename(barCodeInfo.getString("SizeName"));
                        bc.setSizecode(barCodeInfo.getString("SizeCode"));
                        bc.setSizeGroupId(barCodeInfo.getString("SizeGroupID"));
                        bc.setQty(qty);
                        bc.setSizeStr(null);// 每箱的配码
                        if (barCodeInfo.isNull("RetailSales")) {
                            bc.setRetailSales(null);
                        } else {
                            bc.setRetailSales(new BigDecimal(barCodeInfo.getDouble("RetailSales")));
                        }
                        // 盘点单的条码设置客户,类别为-1
                        bc.setCustomerId("-1");
                        bc.setType("-1");
                        barCodeDao.insert(bc);
                        // 添加集合
                        addItem(bc);
                    } else {
                        // 设置条码提示音
                        if (LoginParameterUtil.barcodeWarningTone) {
                            settingVoice("error", getApplicationContext());
                        }
                        // 选中错误的条码
                        et_barcode.requestFocus();
                        et_barcode.selectAll();
                        Toast.makeText(InventorySheetDetailActivity.this, "条码错误或不存在此条码", Toast.LENGTH_SHORT).show();
                        Logger.e(TAG, "条码错误或不存在此条码");
                    }
                } catch (Exception e) {
                    Toast.makeText(InventorySheetDetailActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 解析箱条码
     * 
     * @param barcodeStr
     * @param qty
     */
    public void ajaxAddBoxItem(final String barcodeStr, final int qty) {
        RequestVo vo = new RequestVo();
        vo.requestUrl = "/select.do?getBoxCode";
        vo.context = this;
        HashMap map = new HashMap();
        map.put("BarCode", barcodeStr);
        vo.requestDataMap = map;
        super.getDataFromServerNoProcess(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    if (retObj.getBoolean("success")) {
                        JSONArray jsonList = retObj.getJSONArray("obj");
                        List<BarCode> list = new ArrayList<BarCode>();
                        for (int i = 0; i < jsonList.length(); i++) {
                            JSONObject barCodeInfo = jsonList.getJSONObject(i);
                            BarCode bc = new BarCode();
                            bc.setGoodsid(barCodeInfo.getString("GoodsID"));
                            bc.setGoodscode(barCodeInfo.getString("GoodsCode"));
                            bc.setColorid(barCodeInfo.getString("ColorID"));
                            bc.setColorname(barCodeInfo.getString("ColorName"));
                            bc.setSizeid(barCodeInfo.getString("SizeID"));
                            bc.setSizename(barCodeInfo.getString("SizeName"));
                            bc.setIndexno(barCodeInfo.getInt("IndexNo"));
                            bc.setSizeGroupId(barCodeInfo.getString("SizeGroupID"));
                            bc.setQty(barCodeInfo.getInt("Quantity"));
                            bc.setBoxQty(qty);
                            bc.setOneBoxQty(barCodeInfo.getInt("Quantity"));// 每箱中的数量
                            bc.setSizeStr(barCodeInfo.getString("SizeStr"));// 每箱的配码
                            if (barCodeInfo.isNull("RetailSales")) {
                                bc.setRetailSales(null);
                            } else {
                                bc.setRetailSales(new BigDecimal(barCodeInfo.getDouble("RetailSales")));
                            }
                            list.add(bc);
                        }
                        addItem(list);
                        // 保存箱条码扫描记录
                        saveGoodsBoxBarcode(barcodeStr, qty, list.get(0).getGoodsid(), list.get(0).getColorid(), list.get(0).getSizeStr());
                    } else {
                        // 设置条码提示音
                        if (LoginParameterUtil.barcodeWarningTone) {
                            settingVoice("error", getApplicationContext());
                        }
                        Toast.makeText(InventorySheetDetailActivity.this, "箱条码错误或不存在此箱条码", Toast.LENGTH_SHORT).show();
                        et_barcode.selectAll();
                    }
                } catch (Exception e) {
                    Toast.makeText(InventorySheetDetailActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 保存箱条码的扫描记录
     * 
     * @param barcodeStr
     * @param qty
     * @param goodsId
     * @param colorId
     * @param sizeStr
     */
    private void saveGoodsBoxBarcode(String barcodeStr, Integer qty, String goodsId, String colorId, String sizeStr) {
        boolean exit = false;
        for (int i = 0; i < datas.size(); i++) {
            HashMap<String, Object> hm = datas.get(i);
            String barcode = String.valueOf(hm.get("Barcode"));
            if (barcodeStr.equals(barcode)) {
                int boxQty = Integer.parseInt(String.valueOf(hm.get("BoxQty")));
                hm.put("BoxQty", (boxQty + qty));
                exit = true;
            }
        }
        // 集合中不存在箱条码
        if (!exit) {
            HashMap<String, Object> hm = new HashMap<String, Object>();
            hm.put("Barcode", barcodeStr);
            hm.put("BoxQty", qty);
            hm.put("GoodsID", goodsId);
            hm.put("ColorID", colorId);
            hm.put("SizeStr", sizeStr);
            datas.add(hm);
        }
    }

    /**
     * 箱条码扫描时往ListView中添加数据
     * 
     * @param barcodeList
     */
    private void addItem(List<BarCode> barcodeList) {
        for (BarCode barcode : barcodeList) {
            addItem(barcode);
        }
        oddAdapter.refresh();
    }

    /**
     * ListView添加新记录同时合并重复的记录
     * 
     * @param barcode
     */
    private void addItem(BarCode barcode) {
        goodsCode = barcode.getGoodscode();
        // 从DataList中查找
        Map temp = getList(barcode);
        Integer qty = barcode.getQty();
        if (inputType == 2) {
            qty = qty * barcode.getBoxQty();
        }
        if (temp == null) {
            temp = new HashMap();
            temp.put("Barcode", barcode.getBarcode());
            temp.put("GoodsCode", barcode.getGoodscode());
            temp.put("Color", barcode.getColorname());
            temp.put("Size", barcode.getSizename());
            temp.put("Quantity", qty);
            if (boxQtySum > 0) {
                temp.put("OneBoxQty", barcode.getOneBoxQty());
            } else {
                temp.put("OneBoxQty", 0);
            }
            temp.put("GoodsID", barcode.getGoodsid());
            temp.put("ColorID", barcode.getColorid());
            temp.put("SizeID", barcode.getSizeid());
            temp.put("IndexNo", barcode.getIndexno());
            temp.put("RetailSales", barcode.getRetailSales());
            temp.put("SizeGroupID", barcode.getSizeGroupId());
            temp.put("SizeStr", barcode.getSizeStr());
            temp.put("BoxQty", barcode.getBoxQty());
            temp.put("meno", lastMemo);
            dataList.add(0, temp);
        } else {
            Integer oldQty = Integer.parseInt(String.valueOf(temp.get("Quantity")));
            if (boxQtySum > 0) {
                Integer oldBoxQty = Integer.parseInt(String.valueOf(temp.get("BoxQty")));
                temp.put("BoxQty", oldBoxQty + barcode.getBoxQty());
            }
            temp.put("Quantity", oldQty + qty);
        }
        synchronized (addList) {
            Map upMap = new HashMap();
            upMap.put("Barcode", barcode.getBarcode());
            upMap.put("GoodsCode", barcode.getGoodscode());
            upMap.put("Color", barcode.getColorname());
            upMap.put("Size", barcode.getSizename());
            if (boxQtySum > 0) {
                upMap.put("OneBoxQty", barcode.getOneBoxQty());
            } else {
                upMap.put("OneBoxQty", 0);
            }
            upMap.put("GoodsID", barcode.getGoodsid());
            upMap.put("ColorID", barcode.getColorid());
            upMap.put("SizeID", barcode.getSizeid());
            upMap.put("IndexNo", barcode.getIndexno());
            upMap.put("RetailSales", barcode.getRetailSales());
            upMap.put("SizeGroupID", barcode.getSizeGroupId());
            upMap.put("SizeStr", barcode.getSizeStr());
            upMap.put("BoxQty", barcode.getBoxQty());
            upMap.put("BoxQtyTotal", 0);
            upMap.put("Quantity", qty);
            addList.add(upMap);
        }
        oddAdapter.refresh();
        resetBarcode();
        // 检查集合中是否有为0的集合
        checkDataList();
        // 计算并显示总数量
        countTotal();
        // 加载图片
        LoadImage(barcode);
    }

    /**
     * 装箱时获取总箱数
     * 
     * @param barcode
     * @return
     */
    private void getBoxQtyTotal(List<Map<String, Object>> addList) {
        for (int i = 0; i < dataList.size(); i++) {
            Map map = (Map) dataList.get(i);
            for (int j = 0; j < addList.size(); j++) {
                Map temp = (Map) addList.get(j);
                if (temp.get("GoodsID").equals(map.get("GoodsID")) && temp.get("ColorID").equals(map.get("ColorID")) && temp.get("SizeID").equals(map.get("SizeID")) && temp.get("SizeStr").equals(map.get("SizeStr"))) {
                    temp.put("BoxQtyTotal", map.get("BoxQty"));
                }
            }
        }
    }

    // 去除集合中的重复记录
    private List<Map<String, Object>> mergeData(List<Map<String, Object>> dataList) {
        for (int i = 0; i < dataList.size() - 1; i++) {
            Map temp1 = (Map) dataList.get(i);
            String sizeStr = String.valueOf(temp1.get("SizeStr"));
            if (sizeStr != null && !"".equals(sizeStr) && !"null".equalsIgnoreCase(sizeStr)) {
                for (int j = dataList.size() - 1; j > i; j--) {
                    Map temp2 = (Map) dataList.get(j);
                    if (temp1.get("GoodsID").equals(temp2.get("GoodsID")) && temp1.get("ColorID").equals(temp2.get("ColorID")) && temp1.get("SizeID").equals(temp2.get("SizeID")) && sizeStr.equals(String.valueOf(temp2.get("SizeStr")))) {
                        Map map = new HashMap();
                        int count1 = Integer.parseInt(String.valueOf(temp1.get("Quantity")));
                        int count2 = Integer.parseInt(String.valueOf(temp2.get("Quantity")));
                        int boxQty1 = Integer.parseInt(String.valueOf(temp1.get("BoxQty")).equalsIgnoreCase("null") ? "0" : String.valueOf(temp1.get("BoxQty")));
                        int boxQty2 = Integer.parseInt(String.valueOf(temp2.get("BoxQty")).equalsIgnoreCase("null") ? "0" : String.valueOf(temp2.get("BoxQty")));
                        int oneBoxQty = Integer.parseInt(String.valueOf(temp1.get("OneBoxQty") == null ? "0" : temp1.get("OneBoxQty")));
                        if (boxQty1 > 0 || boxQty2 > 0) {// 装箱
                            count1 = boxQty1 * oneBoxQty;
                            count2 = boxQty2 * oneBoxQty;
                            temp1.put("BoxQty", boxQty1 + boxQty2);
                        }
                        temp1.put("Quantity", count1 + count2);
                        dataList.remove(j);
                    }
                }
            } else {
                for (int j = dataList.size() - 1; j > i; j--) {
                    Map temp2 = (Map) dataList.get(j);
                    if (temp1.get("GoodsID").equals(temp2.get("GoodsID")) && temp1.get("ColorID").equals(temp2.get("ColorID")) && temp1.get("SizeID").equals(temp2.get("SizeID"))) {
                        Map map = new HashMap();
                        int count1 = Integer.parseInt(String.valueOf(temp1.get("Quantity")));
                        int count2 = Integer.parseInt(String.valueOf(temp2.get("Quantity")));
                        int boxQty1 = Integer.parseInt(String.valueOf(temp1.get("BoxQty")).equalsIgnoreCase("null") ? "0" : String.valueOf(temp1.get("BoxQty")));
                        int boxQty2 = Integer.parseInt(String.valueOf(temp2.get("BoxQty")).equalsIgnoreCase("null") ? "0" : String.valueOf(temp2.get("BoxQty")));
                        int oneBoxQty = Integer.parseInt(String.valueOf(temp1.get("OneBoxQty") == null ? "0" : temp1.get("OneBoxQty")));
                        if (boxQty1 > 0 || boxQty2 > 0) {// 装箱
                            count1 = boxQty1 * oneBoxQty;
                            count2 = boxQty2 * oneBoxQty;
                            temp1.put("BoxQty", boxQty1 + boxQty2);
                        }
                        temp1.put("Quantity", count1 + count2);
                        dataList.remove(j);
                    }
                }
            }
        }
        return dataList;
    }

    /**
     * 检查集合中是否有为0的集合
     */
    private void checkDataList() {
        for (int i = 0; i < dataList.size(); i++) {
            int num = Integer.parseInt(String.valueOf(dataList.get(i).get("Quantity")));
            if (num == 0) {
                dataList.remove(i);
                i--;
            }
        }
    }

    /**
     * 计算并显示总数量和价格
     */
    private void countTotal() {
        int sum = 0;
        double amount = 0.0;
        for (int j = 0; j < dataList.size(); j++) {
            int num = Integer.parseInt(String.valueOf(dataList.get(j).get("Quantity")));
            double peice = Double.parseDouble(String.valueOf(dataList.get(j).get("RetailSales") == null ? "0" : dataList.get(j).get("RetailSales")));
            amount += peice * num;
            sum += num;
        }
        tv_qtysum.setText(String.valueOf(sum));
        if (LoginParameterUtil.stocktakingAmountSumRight) {
            tv_amount.setText(Tools.formatDecimal(String.valueOf(amount)));
        } else {
            tv_symbol.setVisibility(View.GONE);
            tv_amount.setVisibility(View.GONE);
        }
        // 重新计算箱数
        if (boxQtySum > 0) {
            for (int i = 0; i < dataList.size(); i++) {
                int count = 0;
                Map map = dataList.get(i);
                // 获取同货号同颜色的数量
                for (int j = 0; j < dataList.size(); j++) {
                    Map temp = dataList.get(j);
                    if (map.get("GoodsID").equals(temp.get("GoodsID")) && map.get("ColorID").equals(temp.get("ColorID"))) {
                        count++;
                    }
                }
                if (null != stocktakingID && !"".equals(stocktakingID) && map.containsKey("QuantitySum")) {
                    int oneBox = Integer.parseInt(String.valueOf(map.get("OneBoxQty")));
                    if (oneBox >= count) {
                        map.put("OneBoxQty", oneBox / count);
                    }
                }
            }
        }
        // 添加发货单明细时显示保存按钮
        if ((dataList.size() > 0 && (!"true".equals(auditFlag)))) {
            // 判断用户是否有修改单据的操作权限
            if (stocktakingID == null || modifyRight || isFirst) {
                ll_saomiao_div.setVisibility(View.VISIBLE);
                ll_split2.setVisibility(View.VISIBLE);
            } else {
                ll_saomiao_div.setVisibility(View.GONE);
                ll_split2.setVisibility(View.GONE);
            }
            if (boxQtySum > 0) {
                showBoxTitle();
            }
            // 部门,经手人可读
            et_department.setEnabled(false);
            et_brand.setEnabled(false);
            et_employee.setEnabled(false);
            if (null != stocktakingID && !"".equals(stocktakingID) && !isAdd) {
                tv_save.setVisibility(View.VISIBLE);
                tv_save.setText("审核");
            } else {
                // 当添加新的货品时候，把审核按钮变成保存按钮，如果是隐藏的则显示
                tv_save.setVisibility(View.VISIBLE);
                tv_save.setText("保存");
            }
        } else if ("true".equals(auditFlag) && boxQtySum > 0) {
            showBoxTitle();
        }
        // 显示列表
        oddAdapter.refresh();
    }

    /**
     * 显示箱条码标题
     */
    private void showBoxTitle() {
        setTitle("盘点单(装箱)");
        inputType = 2;
        tv_barcode.setText("箱号");
        tv_qty.setText("箱数");
    }

    /**
     * 获取要保存(录入)的Item
     * 
     * @param barcode
     * @return
     */
    private Map getList(BarCode barcode) {
        String sizeStr = barcode.getSizeStr();
        if (null == sizeStr || "".equals(sizeStr) || "null".equalsIgnoreCase(sizeStr)) {
            for (int i = 0; i < dataList.size(); i++) {
                Map temp = (Map) dataList.get(i);
                if (barcode.getGoodsid().equals(temp.get("GoodsID")) && barcode.getColorid().equals(temp.get("ColorID")) && barcode.getSizeid().equals(temp.get("SizeID"))) {
                    return temp;
                }
            }
        } else {
            for (int i = 0; i < dataList.size(); i++) {
                Map temp = (Map) dataList.get(i);
                if (barcode.getGoodsid().equals(temp.get("GoodsID")) && barcode.getColorid().equals(temp.get("ColorID")) && barcode.getSizeStr().equals(temp.get("SizeStr")) && barcode.getSizeid().equals(temp.get("SizeID"))) {
                    return temp;
                }
            }
        }
        return null;
    }

    /**
     * 设置条码录入区的默认值
     */
    private void resetBarcode() {
        et_barcode.setText(null);
        et_qty.setText("1");
        et_barcode.setSelection(0);
        et_barcode.requestFocus();
    }

    /**
     * 连接服务器保存盘点单信息
     */
    private void save() {
        // 线程定时提交时的非空判断
        if ((addList.size() > 0 || subList.size() > 0) && sendFlag) {
            RequestVo vo = new RequestVo();
            vo.requestUrl = addInventory;
            vo.context = this;
            HashMap map = new HashMap();
            map.put("deptId", departmentid);
            map.put("employeeId", employeeId);
            map.put("brandId", brandId);
            synchronized (addList) {
                // 去除重复记录
                addList = mergeData(addList);
                if (inputType == 2) {
                    // 获取总箱数
                    getBoxQtyTotal(addList);
                }
                subList.addAll(addList);
                addList.clear();
            }
            net.sf.json.JSONArray data = net.sf.json.JSONArray.fromObject(subList);
            map.put("data", data.toString());
            map.put("StocktakingID", stocktakingID);
            map.put("memo", memo);
            vo.requestDataMap = map;
            // 发送数据前，改变按钮文件，禁用按钮，防止重复提交
            tv_save.setText("保存中");
            tv_save.setClickable(false);
            super.getDataFromServerNoProcess(vo, new DataCallback<JSONObject>() {
                @Override
                public void processData(JSONObject retObj, boolean paramBoolean) {
                    try {
                        sendFlag = true;
                        tv_save.setClickable(true);
                        tv_save.setText("保存");
                        if (retObj == null) {
                            return;
                        }
                        if (retObj.getBoolean("success")) {
                            JSONObject rs = retObj.getJSONObject("attributes");
                            stocktakingID = rs.getString("stocktakingId");
                            subList.clear();
                            // 部门,经手人不可用
                            et_department.setEnabled(false);
                            et_employee.setEnabled(false);
                            et_brand.setEnabled(false);
                            Toast.makeText(InventorySheetDetailActivity.this, "保存成功", Toast.LENGTH_LONG).show();
                            tv_save.setText("审核");
                            sendFlag = false;
                            isAdd = false;
                        } else {
                            Toast.makeText(InventorySheetDetailActivity.this, "保存失败", Toast.LENGTH_LONG).show();
                            Logger.e(TAG, "保存失败");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(InventorySheetDetailActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                        Logger.e(TAG, e.getMessage());
                    }
                }

            });
        } else {
            if ("审核".equals(tv_save.getText()) || stocktakingID == null || "null".equalsIgnoreCase(stocktakingID)) {
                return;
            } else {
                // 阻止数据提交
                closeProgressDialog();
                Toast.makeText(InventorySheetDetailActivity.this, "保存成功", Toast.LENGTH_LONG).show();
                tv_save.setText("审核");
            }
        }
    }

    /**
     * 审核盘点单
     */
    private void audit() {
        RequestVo vo = new RequestVo();
        vo.requestUrl = inventoryAudit;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("StocktakingID", stocktakingID);
        vo.requestDataMap = map;
        super.getDataFromServer(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj.getBoolean("success")) {
                        JSONObject rs = retObj.getJSONObject("attributes");
                        int result = rs.getInt("result");
                        if (result > 0) {
                            Toast.makeText(InventorySheetDetailActivity.this, "审核成功", Toast.LENGTH_LONG).show();
                            auditFlag = "true";
                            // 已审核的盘点单明细(无法修改) 隐藏扫描区和保存按钮
                            ll_saomiao_div.setVisibility(View.GONE);
                            ll_split2.setVisibility(View.GONE);
                            tv_save.setVisibility(View.GONE);
                        } else {
                            Toast.makeText(InventorySheetDetailActivity.this, "审核失败", Toast.LENGTH_LONG).show();
                            Logger.e(TAG, "审核失败");
                            // 审核失败时恢复添加操作
                            tv_add.setClickable(true);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(InventorySheetDetailActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }

        });
    }

    /**
     * 修改或删除盘点单明细
     */
    private void delete() {
        RequestVo vo = new RequestVo();
        vo.requestUrl = inventoryUpdateCount;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("StocktakingID", stocktakingID);
        net.sf.json.JSONArray json = net.sf.json.JSONArray.fromObject(delList);
        map.put("data", json.toString());
        vo.requestDataMap = map;
        super.getDataFromServerNoProcess(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject paramObject, boolean paramBoolean) {
                try {
                    if (paramObject.getBoolean("success")) {
                        Logger.e(TAG, "删除货品成功");
                        delList.clear();
                        Toast.makeText(InventorySheetDetailActivity.this, "删除货品成功", Toast.LENGTH_LONG).show();
                        tv_save.setText("审核");
                        // 重新计算数量和价格
                        countTotal();
                        if (dataList.size() == 0) {
                            back();
                        }
                        sendFlag = false;
                    }
                } catch (JSONException e) {
                    Toast.makeText(InventorySheetDetailActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 根据货号加载货品图片
     * 
     * @param barcode
     */
    private void LoadImage(BarCode barcode) {
        // 加载图片
        String url = LoginParameterUtil.customer.getIp();
        url = url.concat("/common.do?image&code=").concat(barcode.getGoodscode());
        File imageFile = Tools.getImage(getApplicationContext(), barcode.getGoodscode());
        if (!imageFile.exists()) {
            ImageTask itTask = new ImageTask(imageFile, url, barcode, iv_pic);
            itTask.execute();
        } else {
            // 显示图片
            setImage(imageFile);
        }
    }

    /**
     * 设置货品图片
     * 
     * @param imageFile
     */
    private void setImage(File imageFile) {
        setImage(imageFile.getAbsolutePath());
    }

    /**
     * 设置货品图片
     * 
     * @param imagePath
     */
    private void setImage(String imagePath) {
        Bitmap bm = BitmapFactory.decodeFile(imagePath);
        if (null == bm) {
            // 显示默认图片
            iv_pic.setImageResource(R.drawable.unfind);
            return;
        }
        iv_pic.setImageBitmap(bm);
    }

    private class TouchListener implements OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (v.getId()) {
                case R.id.department:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Intent intent = new Intent(InventorySheetDetailActivity.this, SelectActivity.class);
                        intent.putExtra("selectType", "selectWarehouse");
                        startActivityForResult(intent, R.id.department);
                        overridePendingTransition(R.anim.activity_open, 0);
                    }
                    break;
                case R.id.type:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Intent intent = new Intent(InventorySheetDetailActivity.this, SelectActivity.class);
                        intent.putExtra("selectType", "selectEmployee");
                        startActivityForResult(intent, R.id.type);
                        overridePendingTransition(R.anim.activity_open, 0);
                    }
                    break;
                case R.id.brand:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Intent intent = new Intent(InventorySheetDetailActivity.this, SelectActivity.class);
                        if (LoginParameterUtil.useBrandPower) {
                            intent.putExtra("selectType", "selectBrandByPower");
                        } else {
                            intent.putExtra("selectType", "selectBrand");
                        }
                        startActivityForResult(intent, R.id.brand);
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
            case R.id.department:
                if (resultCode == 1) {
                    et_department.setText(data.getStringExtra("Name"));
                    departmentid = data.getStringExtra("DepartmentID");
                    // 设置扫码区自动获取焦点
                    et_barcode.requestFocus();
                }
                break;
            case R.id.type:
                if (resultCode == 1) {
                    et_employee.setText(data.getStringExtra("Name"));
                    employeeId = data.getStringExtra("EmployeeID");
                    // 设置扫码区自动获取焦点
                    et_barcode.requestFocus();
                }
                break;
            case R.id.brand:
                if (resultCode == 1) {
                    et_brand.setText(data.getStringExtra("Name"));
                    brandId = data.getStringExtra("BrandID");
                    // 设置扫码区自动获取焦点
                    et_barcode.requestFocus();
                }
                break;
            case 1:
                if (resultCode == 1) {
                    memo = data.getStringExtra("remark");
                }
                break;

            case Constant.REQ_QR_CODE :
                if(resultCode == RESULT_OK ){
                    Bundle bundle = data.getExtras();
                    String scanResult = bundle.getString(Constant.INTENT_EXTRA_KEY_QR_SCAN);
                    et_barcode.setText(scanResult);
                    add();
                    // Toast.makeText(SalesDetailActivity.this,scanResult,Toast.LENGTH_LONG).show();
                }
                break;
            default:
                break;
        }
    }

    /**
     * 开启扫描区
     */
    private void closeScanButton() {
        tv_add.setText("开启\n扫描");
        tv_add.setBackground(this.getResources().getDrawable(R.drawable.upload_button));
        et_barcode.setEnabled(false);
        et_barcode.setFocusable(false);
    }

    /**
     * 禁用扫描区
     */
    private void openScanButton() {
        tv_add.setText("添加");
        tv_add.setBackground(this.getResources().getDrawable(R.drawable.custom_button));
        et_barcode.setEnabled(true);
        et_barcode.setFocusable(true);
        et_barcode.setFocusableInTouchMode(true);
        et_barcode.requestFocus();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        // 添加新记录
            case R.id.add:
                if ("添加".equals(tv_add.getText().toString())) {
                    add();
                } else {
                    openScanButton();
                }
                break;
            // 保存
            case R.id.save:
                if ("保存".equals(tv_save.getText())) {
                    save();
                } else if ("审核".equals(tv_save.getText())) {
                    if (auditRight) {
                        // 点击审核时禁用扫码区
                        tv_add.setClickable(false);
                        audit();
                    } else {
                        Builder dialog = new AlertDialog.Builder(InventorySheetDetailActivity.this, AlertDialog.THEME_HOLO_LIGHT);
                        dialog.setTitle("提示");
                        dialog.setMessage("当前暂无审核盘点单的操作权限");
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
                break;
            case R.id.salesorder_pic:
                InputMethodManager im = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (im.isActive()) {
                    im.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
                }
                if (null != goodsCode && !goodsCode.isEmpty()) {
                    Intent inten = new Intent(InventorySheetDetailActivity.this, PictureActivity.class);
                    inten.putExtra("goodsCode", goodsCode);
                    startActivity(inten);
                } else {
                    Toast.makeText(InventorySheetDetailActivity.this, "当前图片无对应的货品编码", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.scanIcon :
                startQrCode();
                break;


            default:
                break;
        }
    }
    // 开始扫码
    private void startQrCode() {
        // 申请相机权限
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // 申请权限
            ActivityCompat.requestPermissions(InventorySheetDetailActivity.this, new String[]{Manifest.permission.CAMERA}, Constant.REQ_PERM_CAMERA);
            return;
        }
        // 申请文件读写权限（部分朋友遇到相册选图需要读写权限的情况，这里一并写一下）
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // 申请权限
            ActivityCompat.requestPermissions(InventorySheetDetailActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, Constant.REQ_PERM_EXTERNAL_STORAGE);
            return;
        }
        // 二维码扫码
        Intent intent = new Intent(InventorySheetDetailActivity.this, CaptureActivity.class);
        startActivityForResult(intent, Constant.REQ_QR_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constant.REQ_PERM_CAMERA:
                // 摄像头权限申请
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 获得授权
                    startQrCode();
                } else {
                    // 被禁止授权
                    Toast.makeText(InventorySheetDetailActivity.this, "请至权限中心打开本应用的相机访问权限", Toast.LENGTH_LONG).show();
                }
                break;
            case Constant.REQ_PERM_EXTERNAL_STORAGE:
                // 文件读写权限申请
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 获得授权
                    startQrCode();
                } else {
                    // 被禁止授权
                    Toast.makeText(InventorySheetDetailActivity.this, "请至权限中心打开本应用的文件读写权限", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }
    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    protected void loadViewLayout() {
        setContentView(R.layout.activity_inventory_sheet_detail);
        setTitle("盘点单(明细)");
        // 设置双击事件
        gd = new GestureDetector(this, new OnDoubleClick());
    }

    @Override
    protected void setListener() {
        oddAdapter = new OddAdapter(this, dataList);
        lv_detail.setOnItemLongClickListener(this);
        lv_detail.setAdapter(oddAdapter);
        tv_add.setOnClickListener(this);
        tv_save.setOnClickListener(this);
        iv_pic.setOnClickListener(this);
        ftv_scanIcon.setOnClickListener(this);
        TouchListener tl = new TouchListener();
        et_department.setOnTouchListener(tl);
        et_employee.setOnTouchListener(tl);
        et_brand.setOnTouchListener(tl);
        et_barcode.setOnEditorActionListener(new BarcodeActionListener());
        et_qty.setOnEditorActionListener(new QtyBarcodeActionListener());
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
                    tv_add.callOnClick();
                } else if (event != null && event.getAction() == KeyEvent.ACTION_DOWN) {
                    tv_add.callOnClick();
                    return true;
                } else if (event != null && event.getAction() == KeyEvent.ACTION_UP) {
                    return true;
                }
            }
            return false;
        }

    }

    /**
     * Title: QtyBarcodeActionListener Description: 货品数量完成触发Enter事件
     */
    class QtyBarcodeActionListener implements OnEditorActionListener {

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm.isActive()) {
                    imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
                }
                // 设置扫码区自动获取焦点
                et_barcode.requestFocus();
                return true;
            }
            return false;
        }

    }

    /**
     * 绑定双击事件
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gd.onTouchEvent(event);
    }

    @Override
    protected void processLogic() {
        // 设置部门为登录用户的默认部门(仓库)
        departmentid = LoginParameterUtil.deptId;
        et_department.setText(LoginParameterUtil.deptName);
        // 获取用户操作权限(新增)
        modifyRight = LoginParameterUtil.stocktakingRightMap.get("ModifyRight");
        auditRight = LoginParameterUtil.stocktakingRightMap.get("AuditRight");
        Bundle bundle = this.getIntent().getExtras();
        if (null != bundle) {
            // 根据StocktakingID显示具体明细
            stocktakingID = bundle.getString("StocktakingID");
            auditFlag = bundle.getString("AuditFlag");
            if ("true".equals(auditFlag)) {
                // 已审核的盘点单明细(无法修改) 隐藏扫描区和保存按钮
                ll_saomiao_div.setVisibility(View.GONE);
                ll_split2.setVisibility(View.GONE);
                tv_save.setVisibility(View.GONE);
                // 隐藏右上角功能
                setHeadRightVisibility(View.GONE);
            } else if ("false".equals(auditFlag)) {
                // 未审核的盘点单明细(可以修改)
                tv_save.setText("审核");
                // 显示右上角功能
                setHeadRightVisibility(View.VISIBLE);
            }
            // 获取数据
            getData();
        } else {
            tv_save.setVisibility(View.GONE);
            // 显示右上角功能
            setHeadRightVisibility(View.VISIBLE);
            // 防止首次开单时扫码区隐藏
            isFirst = true;
        }
        // 设置扫码区自动获取焦点
        et_barcode.requestFocus();
        // 设置扫码区默认值
        resetBarcode();
        // 启动定时器，定时保存
        handler.postDelayed(runnable, saveTime);
        // 显示按钮
        setHeadRightText(R.string.setting);
        // 是否使用品牌权限
        // if (!LoginParameterUtil.useBrandPower) {
        // ll_brand.setVisibility(View.GONE);
        // }
        // 获取箱条码扫描记录
        if (stocktakingID != null && !"".equals(stocktakingID) && "false".equals(auditFlag)) {
            List<GoodsBoxBarcodeRecord> list = recordDao.getList(stocktakingID);
            // 禁用扫码区
            closeScanButton();
            for (int i = 0; i < list.size(); i++) {
                HashMap<String, Object> hm = new HashMap<String, Object>();
                hm.put("Barcode", list.get(i).getGoodsBoxBarcode());
                hm.put("BoxQty", list.get(i).getBoxQty());
                hm.put("SizeStr", list.get(i).getSizeStr());
                hm.put("GoodsID", list.get(i).getGoodsId());
                hm.put("ColorID", list.get(i).getColorId());
                datas.add(hm);
            }
        }

        tv_add.setVisibility(View.GONE);
    }

    @Override
    protected void findViewById() {
        lv_detail = (ListView) findViewById(R.id.salesorder_detail);
        et_department = (EditText) findViewById(R.id.department);
        et_employee = (EditText) findViewById(R.id.type);
        et_brand = (EditText) findViewById(R.id.brand);
        tv_qtysum = (TextView) findViewById(R.id.qtysum);
        et_barcode = (EditText) findViewById(R.id.salesorder_barcode);
        et_qty = (EditText) findViewById(R.id.salesorder_qty);
        tv_add = (TextView) findViewById(R.id.add);
        tv_amount = (TextView) findViewById(R.id.amount);
        tv_save = (TextView) findViewById(R.id.save);
        tv_barcode = (TextView) findViewById(R.id.tv_barcode);
        tv_symbol = (TextView) findViewById(R.id.tv_symbol);
        tv_qty = (TextView) findViewById(R.id.tv_qty);
        iv_pic = (ImageView) findViewById(R.id.salesorder_pic);
        ll_saomiao_div = (LinearLayout) findViewById(R.id.saomiao_div);
        ll_split2 = (LinearLayout) findViewById(R.id.split2);
        ll_brand = (LinearLayout) findViewById(R.id.ll_brand);
        ftv_scanIcon =(FontTextView) findViewById(R.id.scanIcon);
    }

    /**
     * 定时线程,用于定时保存盘点单信息
     */
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            save();
            handler.postDelayed(this, saveTime);
        }
    };

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
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 点击返回时的操作
     */
    private void back() {
        // 结束线程
        handler.removeCallbacks(runnable);
        if (null == stocktakingID || "".equals(stocktakingID)) {
            finish();
        } else {
            // 点击返回时返回上一页面并刷新
            Intent intent = new Intent(InventorySheetDetailActivity.this, InventorySheetActivity.class);
            intent.putExtra("auditFlag", auditFlag);// 把返回数据存入Intent
            startActivity(intent);
            finish();
        }
    }

    /**
     * 点击返回时询问是否保存当前信息
     */
    private void saveOrNot() {
        if (sendFlag) {
            // 询问是否删除
            Builder dialog = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
            dialog.setTitle("保存提示");
            dialog.setMessage("盘点单尚未保存,确定要返回吗？");
            // 相当于点击确认按钮
            dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    back();
                }
            });
            // 相当于点击取消按钮
            dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {}
            });
            dialog.create();
            dialog.show();
        } else {
            back();
        }
    }

    /**
     * Title: OnDoubleClick Description: 控件双击事件
     */
    class OnDoubleClick extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return false;
        }
    }

    @Override
    protected void onHeadRightButton(View v) {
        // 显示菜单
        showpopWinShare();
    }

    /**
     * 根据条件动态显示菜单选项
     */
    private void showpopWinShare() {
        if ("true".equals(auditFlag) || stocktakingID != null || dataList.size() > 0) {
            popWinShare = null;
        }
        if (popWinShare == null) {
            // 自定义的单击事件
            OnClickLintener paramOnClickListener = new OnClickLintener();
            popWinShare = new PopWinShare(InventorySheetDetailActivity.this, paramOnClickListener);
            // 隐藏切换的选项
            // 是否使用箱条码扫描
            if (!LoginParameterUtil.useGoodsboxBarcodeInStocktaking || dataList.size() > 0 || "true".equals(auditFlag)) {
                popWinShare.ll_parent.removeView(popWinShare.layoutSwitch);
            }
            if (dataList.size() == 0 || (dataList.size() > 0 && inputType == 1)) {
                popWinShare.ll_parent.removeView(popWinShare.layoutGoodsBoxBarcode);
                popWinShare.ll_parent.removeView(popWinShare.view_6);
            }
            popWinShare.ll_parent.removeView(popWinShare.layoutPrint);
            popWinShare.ll_parent.removeView(popWinShare.layout_price);
            popWinShare.ll_parent.removeView(popWinShare.view);
            popWinShare.ll_parent.removeView(popWinShare.view_2);
            popWinShare.ll_parent.removeView(popWinShare.view_3);
            popWinShare.ll_parent.removeView(popWinShare.layoutAddCustomer);
            popWinShare.ll_parent.removeView(popWinShare.view_4);
            popWinShare.ll_parent.removeView(popWinShare.view_5);
            popWinShare.ll_parent.removeView(popWinShare.layoutBarcodeVerification);
            popWinShare.ll_parent.removeView(popWinShare.layoutSwitchInputMode);
            popWinShare.ll_parent.removeView(popWinShare.view_7);
            popWinShare.ll_parent.removeView(popWinShare.view_8);
            popWinShare.ll_parent.removeView(popWinShare.view_9);
            popWinShare.ll_parent.removeView(popWinShare.layoutBarcodeVerificationDifferent);
            popWinShare.ll_parent.removeView(popWinShare.layoutExportExcel);
            popWinShare.ll_parent.removeView(popWinShare.view_10);
            popWinShare.ll_parent.removeView(popWinShare.layoutSingleDiscount);
            popWinShare.ll_parent.removeView(popWinShare.view_11);
            popWinShare.ll_parent.removeView(popWinShare.layoutOtherOptions);
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
     * Title: OnClickLintener Description: 菜单选项的点击监听
     */
    class OnClickLintener implements OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
            // 添加备注
                case R.id.layout_remark:
                    popWinShare.dismiss();
                    Intent intent = new Intent(InventorySheetDetailActivity.this, RemarkActivity.class);
                    intent.putExtra("id", stocktakingID);
                    intent.putExtra("idName", "StocktakingID");
                    intent.putExtra("updatePath", inventoryMemo);
                    intent.putExtra("memo", memo);
                    intent.putExtra("auditFlag", Boolean.parseBoolean(auditFlag));
                    startActivityForResult(intent, 1);
                    overridePendingTransition(R.anim.activity_open, 0);
                    break;
                // 切换装箱/散件
                case R.id.layout_switch:
                    // 改变货品成箱号，数量为箱数，条码查找箱条码，其余业务逻辑相同
                    if (inputType == 1) {
                        // 改变标题
                        popWinShare.tvToggle.setText("切换散件");
                        setTitle("盘点单(装箱)");
                        inputType = 2;
                        tv_barcode.setText("箱号");
                        tv_qty.setText("箱数");
                        boxQtySum = 1;
                    } else {
                        popWinShare.tvToggle.setText("切换装箱");
                        setTitle("盘点单(散件)");
                        inputType = 1;
                        tv_barcode.setText("货号");
                        tv_qty.setText("数量");
                        boxQtySum = 0;
                    }
                    popWinShare.dismiss();
                    break;
                case R.id.layout_goods_box_barcode:
                    popWinShare.dismiss();
                    Intent in = new Intent(InventorySheetDetailActivity.this, GoodsBoxBarcodeActivity.class);
                    in.putExtra("billId", stocktakingID);
                    in.putExtra("datas", datas);
                    startActivity(in);
                    break;
                default:
                    popWinShare.dismiss();
                    break;
            }
        }
    }

    /**
     * 长按选中ListView的项
     */
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
        if ("true".equals(auditFlag)) {
            return false;
        }
        // 长按时触发保存
        if (addList.size() > 0 || subList.size() > 0) {
            save();
        }
        if (modifyRight) {
            Map<String, Object> map = dataList.get(position);
            String goodsCode = String.valueOf(map.get("GoodsCode"));
            final String upGoodsId = String.valueOf(map.get("GoodsID"));
            final String upColorId = String.valueOf(map.get("ColorID"));
            final String upSizeId = String.valueOf(map.get("SizeID"));
            final String oneBoxQty = String.valueOf(map.get("OneBoxQty"));
            final String upSizeStr = String.valueOf(map.get("SizeStr"));
            String upMeno = String.valueOf(map.get("meno"));
            String boxCount = null;
            String count = null;
            if (boxQtySum > 0) {
                // 装箱时显示的是箱数
                boxCount = String.valueOf(map.get("BoxQty"));
            } else {
                count = String.valueOf(map.get("Quantity"));
            }
            View v = (LinearLayout) getLayoutInflater().inflate(R.layout.menu_inventory, null);
            final EditText et_count = (EditText) v.findViewById(R.id.et_count);
            final EditText et_meno = (EditText) v.findViewById(R.id.et_meno);
            LinearLayout ll_meno = (LinearLayout) v.findViewById(R.id.ll_meno);
            View view1 = v.findViewById(R.id.view1);
            // ll_meno.setVisibility(View.GONE);
            view1.setVisibility(View.GONE);
            TextView tv_count = (TextView) v.findViewById(R.id.tv_count);
            // 询问是否删除
            Builder dialog = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
            dialog.setMessage("修改货品" + goodsCode + "的数量 :");
            if (boxQtySum > 0) {
                tv_count.setText("箱数");
                et_count.setText(boxCount);
                et_count.setSelection(boxCount.length());
            } else {
                et_count.setText(count);
                et_count.setSelection(count.length());
            }
            et_meno.setText(upMeno);
            et_meno.setSelection(upMeno.length());
            dialog.setView(v);
            // 相当于点击确认按钮
            dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // 修改单据后允许提交数据
                    sendFlag = true;
                    isAdd = true;
                    String count = et_count.getText().toString();
                    String meno = et_meno.getText().toString();
                    if ("".equals(count.trim())) {
                        count = "1";
                    }
                    if (Integer.parseInt(count) < 1) {
                        // 单据生成后直接删除
                        if (null != stocktakingID && !"".equals(stocktakingID)) {
                            Map upMap = new HashMap();
                            if (boxQtySum > 0) {
                                for (int i = 0; i < dataList.size(); i++) {
                                    Map map = dataList.get(i);
                                    if (map.get("GoodsID").equals(upGoodsId) && map.get("ColorID").equals(upColorId) && map.get("SizeStr").equals(upSizeStr)) {
                                        upMap.put("GoodsID", upGoodsId);
                                        upMap.put("ColorID", upColorId);
                                        upMap.put("SizeID", map.get("SizeID"));
                                        upMap.put("OneBoxQty", map.get("QuantitySum"));
                                        upMap.put("SizeStr", upSizeStr);
                                        delList.add(upMap);
                                        dataList.remove(i);
                                        i--;
                                        // 删除扫描记录
                                        // 箱条码扫描记录
                                        for (int j = 0; j < datas.size(); j++) {
                                            HashMap hm = datas.get(j);
                                            if (hm.get("GoodsID").equals(upGoodsId) && hm.get("ColorID").equals(upColorId) && hm.get("SizeStr").equals(upSizeStr)) {
                                                datas.remove(j);
                                                j--;
                                            }
                                        }
                                    }
                                }
                                delete();
                            } else {
                                upMap.put("GoodsID", upGoodsId);
                                upMap.put("ColorID", upColorId);
                                upMap.put("SizeID", upSizeId);
                                upMap.put("OneBoxQty", oneBoxQty);
                                upMap.put("SizeStr", upSizeStr);
                                delList.add(upMap);
                                dataList.remove(position);
                                delete();
                            }
                        } else {
                            // 未生成单据时的处理
                            if (boxQtySum > 0) {
                                for (int i = 0; i < dataList.size(); i++) {
                                    Map map = dataList.get(i);
                                    if (map.get("GoodsID").equals(upGoodsId) && map.get("ColorID").equals(upColorId) && map.get("SizeStr").equals(upSizeStr)) {
                                        dataList.remove(i);
                                        i--;
                                    }
                                }
                                // 箱条码扫描记录
                                for (int i = 0; i < datas.size(); i++) {
                                    HashMap map = datas.get(i);
                                    if (map.get("GoodsID").equals(upGoodsId) && map.get("ColorID").equals(upColorId) && map.get("SizeStr").equals(upSizeStr)) {
                                        datas.remove(i);
                                        i--;
                                    }
                                }
                            } else {
                                dataList.remove(position);
                            }
                        }
                    } else {
                        for (int i = 0; i < dataList.size(); i++) {
                            Map map = dataList.get(i);
                            if (map.get("GoodsID").equals(upGoodsId)) {
                                // 改数量时要求同码,同色
                                if (map.get("ColorID").equals(upColorId)) {
                                    // // 添加备注
                                    map.put("meno", meno);
                                    lastMemo = meno;
                                    // if (boxQtySum > 0
                                    // && map.get("SizeStr")
                                    // .equals(upSizeStr)) {
                                    // int boxQty = Integer.parseInt(String
                                    // .valueOf(map
                                    // .get("BoxQty")));
                                    // int oneBox = Integer.parseInt(String.valueOf(map
                                    // .get("Quantity")))
                                    // / boxQty;
                                    // map.put("BoxQty", count);
                                    // map.put("Quantity",
                                    // Integer.parseInt(count)
                                    // * oneBox);
                                    // } else {
                                    // if (map.get("SizeID").equals(
                                    // upSizeId)) {
                                    // if (boxQtySum < 1) {
                                    // map.put("BoxQty", 0);
                                    // map.put("Quantity",
                                    // count);
                                    // }
                                    // }
                                    // }
                                }
                            }
                        }
                        // for (int i = 0; i < datas.size(); i++) {
                        // HashMap map = datas.get(i);
                        // if (map.get("GoodsID").equals(upGoodsId)
                        // && map.get("ColorID").equals(
                        // upColorId)
                        // && map.get("SizeStr").equals(
                        // upSizeStr)) {
                        // map.put("BoxQty",
                        // Integer.parseInt(count));
                        // }
                        // }
                        Builder tdialog = new AlertDialog.Builder(InventorySheetDetailActivity.this, AlertDialog.THEME_HOLO_LIGHT);
                        tdialog.setTitle("系统提示");
                        tdialog.setMessage("为保证数据准确性，盘点单仅支持长按删除单据记录或添加备注，如需增加盘点记录请输入正数后扫描相应货品，如需减少数据，请输入负数后扫描相应货品");
                        // 相当于点击确认按钮
                        tdialog.setPositiveButton("我知道了", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {}
                        });
                        tdialog.setCancelable(false);
                        tdialog.create();
                        tdialog.show();
                    }
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
        } else {
            Builder dialog = new AlertDialog.Builder(InventorySheetDetailActivity.this, AlertDialog.THEME_HOLO_LIGHT);
            dialog.setTitle("提示");
            dialog.setMessage("当前暂无修改盘点单的操作权限");
            // 相当于点击确认按钮
            dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {}
            });
            dialog.setCancelable(false);
            dialog.create();
            dialog.show();
            return false;
        }
    }

}
