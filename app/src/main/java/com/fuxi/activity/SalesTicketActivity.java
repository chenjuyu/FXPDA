package com.fuxi.activity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

import android.Manifest;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.SpannableString;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.dommy.qrcode.util.Constant;
import com.fuxi.activity.BaseWapperActivity.DataCallback;
import com.fuxi.adspter.SalesTicketAdapter;
import com.fuxi.dao.BarCodeDao;
import com.fuxi.main.R;
import com.fuxi.util.AppManager;
import com.fuxi.util.Logger;
import com.fuxi.util.LoginParameterUtil;
import com.fuxi.util.NetUtil;
import com.fuxi.util.Tools;
import com.fuxi.vo.BarCode;
import com.fuxi.vo.RequestVo;
import com.fuxi.widget.FontTextView;
import com.fuxi.widget.PopWinShare;
import com.google.zxing.activity.CaptureActivity;

/**
 * Title: SalesTicketActivity Description: 销售小票活动界面
 * 
 * @author LYJ
 * 
 */
public class SalesTicketActivity extends BaseWapperActivity implements OnItemLongClickListener {

    private static final String TAG = "SalesTicketActivity";
    private static final String analyticalBarcode = "/select.do?analyticalBarcode";
    private static final String saveMethod = "/salesTicket.do?saveSalesTicket";
    private static final String checkSPGoodsMethod = "/salesTicket.do?checkSPGoods";
    private static final String getSPMethod = "/salesTicket.do?getSP";
    private static final String checkDailyKnots = "/dailyknots.do?checkDailyKnots";
    private static final String querySalesTicketReturnDateBySalesNoPath = "/salesTicket.do?getSalesTicketReturnDateBySalesNo";
    private static final String getPossalesDetailByNo = "/salesTicket.do?getPossalesDetailByNo";

    private List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
    private List<Map<String, Object>> spList = new ArrayList<Map<String, Object>>();
    private List<Map<String, Object>> spVipList = new ArrayList<Map<String, Object>>();
    private ArrayList<HashMap<String, Object>> tempDatas = new ArrayList<HashMap<String, Object>>();// 负库存检查返回的记录
    private List<String> spNames = new ArrayList<String>(); // 积分促销规则
    private List<String> spIDs = new ArrayList<String>(); // 促销规则ID
    private Map<String, Object> spMap; // 选中的促销
    private SalesTicketAdapter oddAdapter;
    private BarCodeDao barCodeDao = new BarCodeDao(this);
    private Intent printIntent = new Intent("COM.QSPDA.PRINTTEXT");
    private TouchListener tl = new TouchListener();
    private AlertDialog alertDialog;

    private LinearLayout ll_saomiao_div; // 扫码区
    private LinearLayout ll_split2; // 分隔符2
    private PopWinShare popWinShare;
    private ListView lv_detail;
    private EditText et_qty;
    private EditText et_paid;
    private EditText et_barcode;
    private EditText et_integral; // 抵用的积分
    private EditText et_exchange_amount; // 积分兑换的金额
    private EditText et_discount;// 折让
    private EditText et_salesperson;// 售货员
    private EditText et_vip;// VIP
    private TextView bt_submit;
    private TextView bt_addDetail;
    private TextView tv_qtysum;
    private TextView tv_amount; // 总金额
    private FontTextView ftvToggle;
    private FontTextView ftv_scanIcon;

    private String posSalesId;// 销售小票ID
    private String posSalesNo;// 销售小票ID
    private String employeeId;
    private String vipId;
    private String vipTypeId;
    private String vipCode;
    private String tvAmount;
    private String retailAmount;
    private String discountMoney; // 折让金额
    private String exchangedPoint; // 兑换积分
    private String exchangeAmount; // 兑换积分对应的金额
    private String goodsRetailSales; // 商品零售价
    private String goodsUnitPrice; // 商品单价
    private String goodsDiscount; // 商品折扣
    private String goodsDiscountFlag; // 货品是否折上折
    private String goodsCode; // 货品编码
    private String goodsName; // 货品名称
    private String availableIntegral = ""; // 可用积分
    private String memo;
    private String type = "销售";

    private double exchangeMoney; // 记录积分兑换折让的金额

    private BigDecimal usablePoint = new BigDecimal(0); // 获取VIP的可用积分
    private BigDecimal vipDiscount = new BigDecimal(10); // 获取VIP的折扣
    private BigDecimal vipPointRate = new BigDecimal(0); // 获取VIP的积分倍数

    private boolean posBackAudit; // 销售小票后台审核标志
    private boolean checkVip; // 是否根据促销VIP检查VIP的积分兑换条件
    private boolean addRight = false;
    private boolean modifyRight = false;
    private boolean inputFlag = false; // 输入框是否可用的标志
    private boolean specialPriceFlag = false; // 调价单货品是否特价标志
    private boolean useOriginalReturnFlag = false;

    /**
     * 提交保存数据
     */
    private void submitData() {
        if (null == tvAmount || "".equals(tvAmount)) {
            tvAmount = "0";
        }
        if (null == retailAmount || "".equals(retailAmount)) {
            retailAmount = "0";
        }
        discountMoney = et_discount.getText().toString();
        if (null == discountMoney || "".equals(discountMoney)) {
            discountMoney = "0";
        }
        exchangedPoint = et_integral.getText().toString();
        if (null == exchangedPoint || "".equals(exchangedPoint)) {
            exchangedPoint = "0";
        }
        if (dataList.size() == 2) {
            Map map1 = dataList.get(0);
            Map map2 = dataList.get(1);
            if (map1.get("GoodsID").equals(map2.get("GoodsID")) && map1.get("ColorID").equals(map2.get("ColorID")) && map1.get("SizeID").equals(map2.get("SizeID")) && Math.abs(Integer.parseInt(String.valueOf(map1.get("Quantity")))) == Math.abs(Integer.parseInt(String.valueOf(map2.get("SizeID"))))) {
                type = "退货";
            }
        }
        String qty = tv_qtysum.getText().toString();
        RequestVo vo = new RequestVo();
        vo.requestUrl = saveMethod;
        vo.context = this;
        HashMap map = new HashMap();
        net.sf.json.JSONArray json = net.sf.json.JSONArray.fromObject(dataList);
        map.put("data", json.toString());
        map.put("vipId", vipId);
        map.put("vipCode", vipCode);
        map.put("employeeId", employeeId);
        map.put("amount", tvAmount);
        map.put("retailAmount", retailAmount);
        map.put("discountMoney", String.valueOf(exchangeMoney + Double.parseDouble(discountMoney)));
        map.put("exchangedPoint", exchangedPoint);
        map.put("vipPointRate", String.valueOf(vipPointRate));
        map.put("vipDiscount", String.valueOf(vipDiscount));
        map.put("posBackAudit", String.valueOf(posBackAudit));
        map.put("qty", String.valueOf(qty));
        map.put("memo", memo);
        map.put("type", type);
        vo.requestDataMap = map;
        // 发送数据前，改变按钮文件，禁用按钮，防止重复提交
        bt_submit.setText("保存中");
        bt_submit.setClickable(false);
        super.getDataFromServerNoProcess(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                bt_submit.setClickable(true);
                bt_submit.setText("保存");
                if (retObj == null) {
                    return;
                }
                try {
                    if (retObj.getBoolean("success")) {
                        JSONObject rs = retObj.getJSONObject("attributes");
                        // 负库存检查结果
                        JSONArray array = rs.getJSONArray("tempList");
                        if (array.length() > 0) {// 单据中有负库存记录,先处理负库存记录
                            tempDatas.clear();
                            for (int i = 0; i < array.length(); i++) {
                                HashMap<String, Object> temp = new HashMap<String, Object>();
                                JSONObject jsonObject = array.getJSONObject(i);
                                temp.put("GoodsCode", jsonObject.getString("GoodsCode"));
                                temp.put("Color", jsonObject.getString("Color"));
                                temp.put("Size", jsonObject.getString("Size"));
                                temp.put("Quantity", jsonObject.getInt("Quantity"));
                                temp.put("StockQty", jsonObject.getInt("StockQty"));
                                tempDatas.add(temp);
                            }
                            Builder dialog = new AlertDialog.Builder(SalesTicketActivity.this, AlertDialog.THEME_HOLO_LIGHT);
                            dialog.setTitle("提示");
                            dialog.setMessage("单据中含有负库存货品，无法执行保存操作!");
                            // 相当于点击确认按钮
                            dialog.setPositiveButton("点击查看", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // 负库存货品显示界面
                                    Intent intent = new Intent(SalesTicketActivity.this, NegativeStockActivity.class);
                                    intent.putExtra("tempDatas", tempDatas);
                                    startActivity(intent);
                                }
                            });
                            dialog.setCancelable(false);
                            dialog.create();
                            dialog.show();
                        } else {
                            popWinShare = null;
                            posSalesId = rs.getString("PosSalesID");
                            posSalesNo = rs.getString("PosSalesNo");
                            availableIntegral = rs.getString("AvailableIntegral");
                            if (null != posSalesId || "".equals(posSalesId) || "null".equalsIgnoreCase(posSalesId)) {
                                // 隐藏扫码区
                                ll_saomiao_div.setVisibility(View.GONE);
                                ll_split2.setVisibility(View.GONE);
                                // 继续操作
                                bt_submit.setText("新增");
                                // 打印小票
                                // print();
                                getPOSsalesDetails(posSalesNo, vipCode);
                            }
                            posSalesId = null;
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(SalesTicketActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 检查ListView中的重复记录
     * 
     * @param barcode
     * @return
     */
    private Map getList(BarCode barcode) {
        for (int i = 0; i < dataList.size(); i++) {
            Map temp = (Map) dataList.get(i);
            if (barcode.getGoodsid().equals(temp.get("GoodsID")) && barcode.getColorid().equals(temp.get("ColorID")) && barcode.getSizeid().equals(temp.get("SizeID"))) {
                int quantity = Integer.parseInt(String.valueOf(temp.get("Quantity")));
                if (barcode.getQty() > 0 && quantity > 0) {
                    return temp;
                } else if (barcode.getQty() < 0 && quantity < 0) {
                    return temp;
                }
            }
        }
        return null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.addDetail:
                addBarCode();
                // 设置扫码区自动获取焦点
                et_barcode.requestFocus();
                break;
            case R.id.submit:
                if ("提交".equals(bt_submit.getText())) {
                    submitData();
                    et_barcode.requestFocus();
                } else if ("新增".equals(bt_submit.getText())) {
                    Intent intent = new Intent(SalesTicketActivity.this, SalesTicketActivity.class);
                    intent.putExtra("employeeName", et_salesperson.getText().toString());
                    intent.putExtra("employeeId", employeeId);
                    startActivity(intent);
                    finish();
                }
                break;
            case R.id.toggle:
                if (inputFlag) {
                    et_barcode.setText(null);
                    et_barcode.setFocusable(false);
                    et_barcode.setOnTouchListener(tl);
                    Drawable drawable = getResources().getDrawable(R.drawable.input_bg);
                    drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight()); // 此为必须写的
                    et_barcode.setCompoundDrawables(null, null, drawable, null);
                    SpannableString s = new SpannableString("点击选择货品");
                    et_barcode.setHint(s);
                    ftv_scanIcon.setVisibility(View.GONE);
                } else {
                    et_barcode.setText(null);
                    et_barcode.setFocusable(true);
                    et_barcode.setFocusableInTouchMode(true);
                    et_barcode.setClickable(true);
                    ftv_scanIcon.setVisibility(View.VISIBLE);
                    et_barcode.setOnTouchListener(null);
                    et_barcode.setCompoundDrawables(null, null, null, null);
                    SpannableString s = new SpannableString("输入条码/货号");
                    et_barcode.setHint(s);
                    et_barcode.requestFocus();
                }
                inputFlag = !inputFlag;
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
            ActivityCompat.requestPermissions(SalesTicketActivity.this, new String[]{Manifest.permission.CAMERA}, Constant.REQ_PERM_CAMERA);
            return;
        }
        // 申请文件读写权限（部分朋友遇到相册选图需要读写权限的情况，这里一并写一下）
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // 申请权限
            ActivityCompat.requestPermissions(SalesTicketActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, Constant.REQ_PERM_EXTERNAL_STORAGE);
            return;
        }
        // 二维码扫码
        Intent intent = new Intent(SalesTicketActivity.this, CaptureActivity.class);
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
                    Toast.makeText(SalesTicketActivity.this, "请至权限中心打开本应用的相机访问权限", Toast.LENGTH_LONG).show();
                }
                break;
            case Constant.REQ_PERM_EXTERNAL_STORAGE:
                // 文件读写权限申请
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 获得授权
                    startQrCode();
                } else {
                    // 被禁止授权
                    Toast.makeText(SalesTicketActivity.this, "请至权限中心打开本应用的文件读写权限", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    /**
     * 添加记录(扫描条码)
     */
    private void addBarCode() {
        // 添加条码之前先判断是否选择了客户跟部门
        if (employeeId == null) {
            Toast.makeText(SalesTicketActivity.this, "请先选择售货员", Toast.LENGTH_SHORT).show();
            et_barcode.setText(null);
            return;
        }
        String barcodeStr = et_barcode.getText().toString();
        if ("".equals(barcodeStr)) {
            Toast.makeText(SalesTicketActivity.this, R.string.barcode_null, Toast.LENGTH_SHORT).show();
            return;
        }
        Integer qty = Integer.valueOf(et_qty.getText().toString());
        BarCode barcode = barCodeDao.find(barcodeStr, "", "");
        if (barcode == null) {
            ajaxAddItem(barcodeStr, qty);
        } else {
            addItem(barcode);
        }
    }

    /**
     * 添加货品记录
     * 
     * @param barcodeStr
     * @param qty
     */
    public void ajaxAddItem(String barcodeStr, final int qty) {
        RequestVo vo = new RequestVo();
        vo.requestUrl = analyticalBarcode;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("BarCode", barcodeStr);
        map.put("Type", "possales");
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
                        bc.setGoodsname(barCodeInfo.getString("GoodsName"));
                        bc.setColorid(barCodeInfo.getString("ColorID"));
                        bc.setColorcode(barCodeInfo.getString("ColorCode"));
                        bc.setColorname(barCodeInfo.getString("ColorName"));
                        bc.setSizeid(barCodeInfo.getString("SizeID"));
                        bc.setSizename(barCodeInfo.getString("SizeName"));
                        bc.setSizecode(barCodeInfo.getString("SizeCode"));
                        bc.setIndexno(barCodeInfo.getInt("IndexNo"));
                        bc.setGoodsDiscountFlag(String.valueOf(barCodeInfo.get("DiscountFlag")));
                        bc.setGoodsDiscount(new BigDecimal(String.valueOf(barCodeInfo.get("Discount"))));
                        bc.setDiscountRate(new BigDecimal(String.valueOf(barCodeInfo.get("Discount"))));
                        bc.setQty(qty);
                        if (barCodeInfo.isNull("RetailSales")) {
                            bc.setRetailSales(null);
                        } else {
                            bc.setRetailSales(new BigDecimal(barCodeInfo.getDouble("RetailSales")));
                        }
                        bc.setUnitPrice(new BigDecimal(barCodeInfo.getDouble("UnitPrice")));
                        checkSPGoods(bc);
                    } else {
                        et_barcode.selectAll();
                        Toast.makeText(SalesTicketActivity.this, "条码错误或不存在此条码", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(SalesTicketActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 检查货品是否为促销货品
     * 
     * @param barcodeStr
     * @param qty
     */
    public void checkSPGoods(final BarCode barCode) {
        RequestVo vo = new RequestVo();
        vo.requestUrl = checkSPGoodsMethod;
        vo.context = this;
        HashMap map = new HashMap();
        net.sf.json.JSONArray json = net.sf.json.JSONArray.fromObject(spIDs);
        map.put("spIDs", json.toString());
        map.put("goodsId", barCode.getGoodsid());
        vo.requestDataMap = map;
        super.getDataFromServer(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    BarCode code = barCode;
                    specialPriceFlag = false;
                    if (retObj.getBoolean("success")) {
                        JSONObject jsonObject = retObj.getJSONObject("obj");
                        if (jsonObject.isNull("UnitPrice")) {
                            code.setUnitPrice(barCode.getUnitPrice());
                        } else {
                            code.setUnitPrice(new BigDecimal(jsonObject.getString("UnitPrice")));
                        }
                        if (jsonObject.isNull("DiscountRate")) {
                            code.setDiscountRate(new BigDecimal(10));
                        } else {
                            code.setDiscountRate(new BigDecimal(jsonObject.getString("DiscountRate")));
                        }
                        if (!jsonObject.isNull("SpecialPriceFlag")) {
                            specialPriceFlag = jsonObject.getBoolean("SpecialPriceFlag");
                        }
                    }
                    barCodeDao.insert(code);
                    addItem(code);
                } catch (Exception e) {
                    Toast.makeText(SalesTicketActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 获取可用的积分兑换规则
     * 
     * @param barcodeStr
     * @param qty
     */
    public void getSP() {
        RequestVo vo = new RequestVo();
        vo.requestUrl = getSPMethod;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("deptId", LoginParameterUtil.deptId);
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
                        JSONArray array = jsonObject.getJSONArray("spList");
                        for (int i = 0; i < array.length(); i++) {
                            Map temp = new HashMap();
                            JSONObject json = array.getJSONObject(i);
                            String sptype = json.getString("SPType");
                            if ("积分兑换".equals(sptype)) {
                                spNames.add(json.getString("SPName"));
                            }
                            spIDs.add(json.getString("SPID"));
                            Iterator ite = json.keys();
                            while (ite.hasNext()) {
                                String key = ite.next().toString();
                                String value = json.getString(key);
                                temp.put(key, value);
                            }
                            spList.add(temp);
                        }
                        JSONArray jsonArray = jsonObject.getJSONArray("spVipList");
                        if (jsonArray.length() > 0) {
                            checkVip = true;
                        }
                        for (int i = 0; i < jsonArray.length(); i++) {
                            Map temp = new HashMap();
                            JSONObject json = jsonArray.getJSONObject(i);
                            Iterator ite = json.keys();
                            while (ite.hasNext()) {
                                String key = ite.next().toString();
                                String value = json.getString(key);
                                temp.put(key, value);
                            }
                            spVipList.add(temp);
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(SalesTicketActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * ListView添加记录,同时去除重复的记录
     * 
     * @param barcode
     */
    private void addItem(BarCode barcode) {
        Map temp = getList(barcode);
        if (temp == null) {
            temp = new HashMap();
            temp.put("Barcode", barcode.getBarcode());
            temp.put("GoodsCode", barcode.getGoodscode());
            temp.put("GoodsName", barcode.getGoodsname());
            temp.put("Color", barcode.getColorname());
            temp.put("Size", barcode.getSizename());
            temp.put("Quantity", barcode.getQty());
            temp.put("GoodsID", barcode.getGoodsid());
            temp.put("ColorID", barcode.getColorid());
            temp.put("SizeID", barcode.getSizeid());
            temp.put("UnitPrice", barcode.getUnitPrice());
            if ("true".equals(barcode.getGoodsDiscountFlag())) {// 货品允许折上折
                if (!specialPriceFlag) {// 货品非特价
                    barcode.setDiscountRate(barcode.getDiscountRate().multiply(vipDiscount).divide(new BigDecimal(10)).setScale(2, BigDecimal.ROUND_HALF_UP));
                }
            } else {
                if (!specialPriceFlag) {// 货品非特价
                    barcode.setDiscountRate(barcode.getGoodsDiscount().multiply(vipDiscount).divide(new BigDecimal(10)).setScale(2, BigDecimal.ROUND_HALF_UP));
                }
            }
            if (barcode.getDiscountPrice() == null) {
                temp.put("DiscountPrice", barcode.getUnitPrice().multiply(barcode.getDiscountRate()).divide(new BigDecimal(10)).setScale(2, BigDecimal.ROUND_HALF_UP));
            } else {
                temp.put("DiscountPrice", barcode.getDiscountPrice());
            }
            temp.put("DiscountRate", barcode.getDiscountRate());
            temp.put("RetailSales", barcode.getRetailSales());
            dataList.add(0, temp);
        } else {
            Integer qty = barcode.getQty();
            Integer oldQty = Integer.valueOf(String.valueOf(temp.get("Quantity")));
            temp.put("Quantity", oldQty + qty);
        }
        oddAdapter.refresh();
        // 设置扫码区默认值
        resetBarcode();
        // 计算并显示总数量
        countTotal();
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            saveOrNot();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void loadViewLayout() {
        setContentView(R.layout.activity_sales_ticket);
        setTitle("销售小票");
    }

    @Override
    protected void setListener() {
        oddAdapter = new SalesTicketAdapter(this, dataList);
        lv_detail.setAdapter(oddAdapter);
        lv_detail.setOnItemLongClickListener(this);
        et_salesperson.setOnTouchListener(tl);
        et_vip.setOnTouchListener(tl);
        et_integral.setOnTouchListener(tl);
        et_discount.setOnTouchListener(tl);

        bt_addDetail.setOnClickListener(this);
        ftv_scanIcon.setOnClickListener(this);

        bt_submit.setOnClickListener(this);
        ftvToggle.setOnClickListener(this);
        et_barcode.setOnEditorActionListener(new EditorActionListener());
        et_qty.setOnEditorActionListener(new EditorActionListener());

    }

    private class TouchListener implements OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (v.getId()) {
                case R.id.salesperson:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Intent intent = new Intent(SalesTicketActivity.this, SelectActivity.class);
                        intent.putExtra("selectType", "selectEmployee");
                        startActivityForResult(intent, R.id.salesperson);
                        overridePendingTransition(R.anim.activity_open, 0);
                    }
                    break;
                case R.id.vip:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Intent intent = new Intent(SalesTicketActivity.this, SelectActivity.class);
                        intent.putExtra("selectType", "selectVip");
                        startActivityForResult(intent, R.id.vip);
                        overridePendingTransition(R.anim.activity_open, 0);
                    }
                    break;
                case R.id.barcode:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Intent intent = new Intent(SalesTicketActivity.this, SelectActivity.class);
                        intent.putExtra("selectType", "selectPosSalesGoods");
                        startActivityForResult(intent, R.id.barcode);
                        overridePendingTransition(R.anim.activity_open, 0);
                    }
                    break;
                case R.id.discount:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        final View v1 = (LinearLayout) getLayoutInflater().inflate(R.layout.menu_single_input, null);
                        final TextView tv_count1 = (TextView) v1.findViewById(R.id.tv_count);
                        final EditText et_count1 = (EditText) v1.findViewById(R.id.et_count);
                        // 弹出输入折让金额
                        Builder dialog = new AlertDialog.Builder(SalesTicketActivity.this, AlertDialog.THEME_HOLO_LIGHT);
                        dialog.setTitle("输入折让金额");
                        dialog.setView(v1);
                        tv_count1.setText("折让金额");
                        // 相当于点击确认按钮
                        dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 重新计算金额
                                et_discount.setText(et_count1.getText());
                                countRetailAmount();
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
                    break;
                case R.id.integral:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        if (spNames.size() == 0) {
                            Toast.makeText(SalesTicketActivity.this, "当前无积分兑换活动", Toast.LENGTH_SHORT).show();
                            break;
                        }
                        if (vipId == null || "".equals(vipId) || "null".equalsIgnoreCase(vipId)) {
                            Toast.makeText(SalesTicketActivity.this, "请先选择VIP", Toast.LENGTH_SHORT).show();
                            break;
                        }

                        final View v2 = (LinearLayout) getLayoutInflater().inflate(R.layout.menu_single_input, null);
                        final TextView tv_count2 = (TextView) v2.findViewById(R.id.tv_count);
                        final EditText et_count2 = (EditText) v2.findViewById(R.id.et_count);

                        // 检查VIP是否符合积分兑换条件
                        boolean exist = false;
                        if (checkVip) {
                            for (int i = 0; i < spVipList.size(); i++) {
                                Map<String, Object> map = spVipList.get(i);
                                String tvipTypeId = String.valueOf(map.get("VIPTypeID"));
                                String spId = String.valueOf(map.get("SPID"));
                                for (int j = 0; j < spList.size(); j++) {
                                    Map<String, Object> tmap = spList.get(j);
                                    String tspId = String.valueOf(tmap.get("SPID"));
                                    if (spId.equals(tspId) && tvipTypeId.equals(vipTypeId)) {
                                        exist = true;
                                        break;
                                    }
                                }
                            }
                            if (!exist) {
                                for (int i = 0; i < spVipList.size(); i++) {
                                    Map<String, Object> map = spVipList.get(i);
                                    String spId = String.valueOf(map.get("SPID"));
                                    for (int j = 0; j < spList.size(); j++) {
                                        Map<String, Object> tmap = spList.get(j);
                                        String tspId = String.valueOf(tmap.get("SPID"));
                                        if (spId.equals(tspId)) {
                                            spNames.remove(tmap.get("SPName"));
                                            spList.remove(j);
                                            j--;
                                        }
                                    }
                                }
                                if (spList.size() > 0) {
                                    exist = true;
                                } else {
                                    if (spList.size() == 0 && spNames.size() > 0) {
                                        exist = true;
                                    }
                                }
                            }
                        } else {
                            exist = true;
                        }
                        final String[] items = spNames.toArray(new String[spNames.size()]);
                        if (exist) {
                            // 选择积分兑换规则
                            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(SalesTicketActivity.this, AlertDialog.THEME_HOLO_LIGHT);
                            alertBuilder.setTitle("选择积分兑换促销规则");
                            alertBuilder.setItems(items, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int index) {
                                    if (items != null) {
                                        String spName = spNames.get(index);
                                        for (int i = 0; i < spList.size(); i++) {
                                            spMap = spList.get(i);
                                            String tspName = String.valueOf(spMap.get("SPName"));
                                            if (spName.equals(tspName)) {
                                                break;
                                            }
                                        }
                                    }
                                    // 关闭提示框
                                    alertDialog.dismiss();
                                    if (spMap != null) {
                                        // 可用消费积分满条件
                                        double spUsablePoint = Double.parseDouble((String.valueOf(spMap.get("UsablePoint"))));
                                        if (usablePoint.doubleValue() >= spUsablePoint) {
                                            // 弹出输入抵用积分
                                            Builder dialog1 = new AlertDialog.Builder(SalesTicketActivity.this, AlertDialog.THEME_HOLO_LIGHT);
                                            dialog1.setTitle("输入积分");
                                            tv_count2.setText("使用积分");
                                            dialog1.setView(v2);
                                            // 相当于点击确认按钮
                                            dialog1.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    et_integral.setText(et_count2.getText());
                                                    countIntegral();
                                                }
                                            });
                                            // 相当于点击取消按钮
                                            dialog1.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {}
                                            });
                                            dialog1.create();
                                            dialog1.show();
                                        } else {
                                            Toast.makeText(SalesTicketActivity.this, "当前积分未达到积分兑换条件", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            });
                            alertDialog = alertBuilder.create();
                            alertDialog.show();
                        } else {
                            Toast.makeText(SalesTicketActivity.this, "当前VIP不符合积分兑换条件", Toast.LENGTH_SHORT).show();
                        }
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
            case R.id.salesperson:
                if (resultCode == 1) {
                    et_salesperson.setText(data.getStringExtra("Name"));
                    employeeId = data.getStringExtra("EmployeeID");
                }
                break;
            case R.id.barcode:
                if (resultCode == 1) {
                    et_barcode.setText(data.getStringExtra("Name"));
                    et_barcode.setSelection(et_barcode.getText().toString().length());
                    String goodsId = data.getStringExtra("GoodsID");
                    goodsRetailSales = data.getStringExtra("RetailSales");
                    goodsUnitPrice = data.getStringExtra("UnitPrice");
                    goodsName = data.getStringExtra("GoodsName");
                    goodsCode = data.getStringExtra("Code");
                    goodsDiscount = data.getStringExtra("Discount");
                    goodsDiscountFlag = data.getStringExtra("DiscountFlag");
                    if (LoginParameterUtil.multiSelectType == 1) {
                        // 选择多颜色和尺码方式二
                        Intent intent = new Intent(SalesTicketActivity.this, MultiSelectNewWayActivity.class);
                        intent.putExtra("goodsId", goodsId);
                        intent.putExtra("deptId", LoginParameterUtil.deptId);
                        intent.putExtra("tableName", "posSales");
                        startActivityForResult(intent, 10);
                        overridePendingTransition(R.anim.activity_open, 0);
                    } else {
                        // 选择多颜色和尺码方式一
                        Intent intent = new Intent(SalesTicketActivity.this, MultiSelectActivity.class);
                        intent.putExtra("goodsId", goodsId);
                        startActivityForResult(intent, 10);
                        overridePendingTransition(R.anim.activity_open, 0);
                    }
                }
                break;
            case R.id.vip:
                if (resultCode == 1) {
                    et_vip.setText(data.getStringExtra("Name"));
                    vipCode = data.getStringExtra("Code");
                    vipId = data.getStringExtra("VIPID");
                    vipTypeId = data.getStringExtra("VIPTypeID");
                    vipDiscount = new BigDecimal(data.getStringExtra("DiscountRate"));
                    vipPointRate = new BigDecimal(data.getStringExtra("PointRate"));
                    String usablePointStr = data.getStringExtra("UsablePoint");
                    if (usablePointStr == null || "".equals(usablePointStr) || "null".equalsIgnoreCase(usablePointStr)) {
                        usablePoint = new BigDecimal(0);
                    } else {
                        usablePoint = new BigDecimal(usablePointStr);
                    }
                    posBackAudit = data.getBooleanExtra("PosBackAudit", false);
                    int i = vipDiscount.compareTo(BigDecimal.ZERO);
                    if (i == 0) {
                        vipDiscount = new BigDecimal(10);
                    }
                    // 剩余积分显示
                    et_integral.setHint("可用:" + usablePoint);
                }
                break;
            case 1:
                if (resultCode == 1) {
                    memo = data.getStringExtra("remark");
                }
                break;
            case 10:
                if (resultCode == 1) {
                    Bundle bundle = data.getExtras();
                    List<Map<String, Object>> tdatas = (List<Map<String, Object>>) bundle.get("datas");
                    for (int i = 0; i < tdatas.size(); i++) {
                        // 修改单据后允许提交数据
                        Map<String, Object> map = tdatas.get(i);
                        String tcolorCode = String.valueOf(map.get("ColorCode"));
                        String tsizeCode = String.valueOf(map.get("SizeCode"));
                        int qty = Integer.parseInt(String.valueOf(map.get("Quantity")));
                        String barcodeStr = goodsCode + tcolorCode + tsizeCode;
                        map.put("BarCode", barcodeStr);
                        BarCode bc = new BarCode();
                        bc.setBarcode(String.valueOf(map.get("BarCode")));
                        bc.setGoodsid(String.valueOf(map.get("GoodsID")));
                        bc.setGoodscode(goodsCode);
                        bc.setGoodsDiscountFlag(goodsDiscountFlag);
                        bc.setGoodsname(goodsName);
                        bc.setColorid(String.valueOf(map.get("ColorID")));
                        bc.setColorcode(String.valueOf(map.get("ColorCode")));
                        bc.setColorname(String.valueOf(map.get("ColorName")));
                        bc.setSizeid(String.valueOf(map.get("SizeID")));
                        bc.setSizename(String.valueOf(map.get("SizeName")));
                        bc.setSizecode(String.valueOf(map.get("SizeCode")));
                        bc.setGoodsDiscount(new BigDecimal(goodsDiscount));
                        bc.setDiscountRate(new BigDecimal(goodsDiscount));
                        bc.setIndexno(null);
                        bc.setQty(qty);
                        bc.setRetailSales(new BigDecimal(goodsRetailSales));
                        if (goodsUnitPrice != null && !"".equals(goodsUnitPrice) && !"null".equalsIgnoreCase(goodsUnitPrice)) {
                            bc.setUnitPrice(new BigDecimal(goodsUnitPrice));
                        } else {
                            bc.setUnitPrice(new BigDecimal(goodsRetailSales));
                        }
                        checkSPGoods(bc);
                    }
                }
                break;

            case Constant.REQ_QR_CODE :
                if(resultCode == RESULT_OK ){
                    Bundle bundle = data.getExtras();
                    String scanResult = bundle.getString(Constant.INTENT_EXTRA_KEY_QR_SCAN);
                    et_barcode.setText(scanResult);
                    addBarCode();
                    // Toast.makeText(SalesDetailActivity.this,scanResult,Toast.LENGTH_LONG).show();
                }
                break;


            default:
                break;
        }
        // 设置扫码区自动获取焦点
        et_barcode.requestFocus();
    }

    /**
     * Title: EditorActionListener Description: EditText的Enter事件
     */
    class EditorActionListener implements OnEditorActionListener {

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            switch (v.getId()) {
                case R.id.barcode:
                    // 条码扫描完成触发Enter事件
                    if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm.isActive()) {
                            imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
                        }
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            bt_addDetail.callOnClick();
                        } else if (event != null && event.getAction() == KeyEvent.ACTION_DOWN) {
                            bt_addDetail.callOnClick();
                            return true;
                        } else if (event != null && event.getAction() == KeyEvent.ACTION_UP) {
                            return true;
                        }
                    }
                    return false;
                case R.id.qty:
                    // 货品数量完成触发Enter事件
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm.isActive()) {
                            imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
                        }
                        // 设置扫码区自动获取焦点
                        et_barcode.requestFocus();
                    }
                    return false;
                default:
                    return false;
            }
        }

    }

    @Override
    protected void processLogic() {
        bt_addDetail.setVisibility(View.GONE);
        if (NetUtil.hasNetwork(getApplicationContext())) {
            if (LoginParameterUtil.online) {
                // 获取用户操作权限(新增)
                addRight = LoginParameterUtil.posSalesRightMap.get("AddRight");
                if (addRight) {
                    // 获取用户操作权限(修改)
                    modifyRight = LoginParameterUtil.posSalesRightMap.get("ModifyRight");
                    Bundle bundle = this.getIntent().getExtras();
                    if (null != bundle) {
                        et_salesperson.setText(bundle.getString("employeeName"));
                        employeeId = bundle.getString("employeeId");
                    }
                    // 隐藏提交按钮
                    bt_submit.setVisibility(View.GONE);
                    // 设置扫码区自动获取焦点
                    et_barcode.requestFocus();
                    resetBarcode();
                    // 显示右上角图标
                    setHeadRightVisibility(View.VISIBLE);
                    setHeadRightText(R.string.setting);
                    // 积分,折让不可用
                    et_integral.setEnabled(false);
                    et_discount.setEnabled(false);
                    // if(employeeId == null ||
                    // "".equals(et_salesperson.getText().toString())){
                    // //经手人默认为登录的员工
                    // employeeId = LoginParameterUtil.employeeId;
                    // et_salesperson.setText(LoginParameterUtil.customer.getUserName());
                    // }
                    getSP();
                    // 设置实付金额不能修改
                    // et_paid.setEnabled(false);
                    // 检查当前店铺是否日结
                    checkDailyKnots();
                } else {
                    Builder dialog = new AlertDialog.Builder(SalesTicketActivity.this, AlertDialog.THEME_HOLO_LIGHT);
                    dialog.setTitle("提示");
                    dialog.setMessage("当前暂无新增销售小票的操作权限");
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
                AlertDialog.Builder dialog = new AlertDialog.Builder(SalesTicketActivity.this, AlertDialog.THEME_HOLO_LIGHT);
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
            AlertDialog.Builder dialog = new AlertDialog.Builder(SalesTicketActivity.this, AlertDialog.THEME_HOLO_LIGHT);
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

    /**
     * 重置扫码区
     */
    private void resetBarcode() {
        et_barcode.setText(null);
        et_qty.setText("1");
    }

    /**
     * 计算并显示总数量和价格
     */
    private void countTotal() {
        int sum = 0;
        double amount = 0.0;
        for (int j = 0; j < dataList.size(); j++) {
            Map<String, Object> map = dataList.get(j);
            int num = Integer.parseInt(String.valueOf(map.get("Quantity")));
            double peice = Double.parseDouble(String.valueOf(map.get("DiscountPrice") == null ? "0" : map.get("DiscountPrice")));
            map.put("DiscountPrice", new BigDecimal(peice).setScale(2, BigDecimal.ROUND_HALF_UP));
            peice = new BigDecimal(peice).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            map.put("Amount", new BigDecimal(peice * num).setScale(2, BigDecimal.ROUND_HALF_UP));
            amount += peice * num;
            sum += num;
        }
        tv_qtysum.setText(String.valueOf(sum));
        tvAmount = String.valueOf(amount);
        tv_amount.setText(Tools.formatDecimal(String.valueOf(amount)));
        // 计算实际金额
        // et_paid.setText(Tools.formatDecimal(String.valueOf(amount)));
        countRetailAmount();
        // 添加盘点单明细时显示保存按钮
        if (dataList.size() > 0) {
            // 当添加新的货品时候，把审核按钮变成保存按钮，如果是隐藏的则显示
            bt_submit.setVisibility(View.VISIBLE);
            bt_submit.setText("提交");
            // //售货员,VIP设置为可读
            // et_salesperson.setEnabled(false);
            et_vip.setEnabled(false);
            // 积分,折让可用
            et_integral.setEnabled(true);
            et_discount.setEnabled(true);
        } else {
            bt_submit.setVisibility(View.GONE);
            // 售货员,VIP设置为可读
            et_salesperson.setEnabled(true);
            et_vip.setEnabled(true);
            // 其它设置
            et_discount.setText(null);
            et_integral.setText(null);
            et_integral.setHint("可用:" + usablePoint);
            et_paid.setText("0.00");
            et_exchange_amount.setText("0.00");
            // 设置扫码区自动获取焦点
            et_barcode.requestFocus();
            // 积分,折让不可用
            et_integral.setEnabled(false);
            et_discount.setEnabled(false);
        }
    }

    @Override
    protected void findViewById() {
        lv_detail = (ListView) findViewById(R.id.sales_detail);
        et_salesperson = (EditText) findViewById(R.id.salesperson);
        et_vip = (EditText) findViewById(R.id.vip);
        tv_qtysum = (TextView) findViewById(R.id.qtysum);
        tv_amount = (TextView) findViewById(R.id.amount);
        et_barcode = (EditText) findViewById(R.id.barcode);
        et_qty = (EditText) findViewById(R.id.qty);
        et_paid = (EditText) findViewById(R.id.paid);
        et_exchange_amount = (EditText) findViewById(R.id.exchange_amount);
        et_integral = (EditText) findViewById(R.id.integral);
        et_discount = (EditText) findViewById(R.id.discount);
        bt_addDetail = (TextView) findViewById(R.id.addDetail);
        bt_submit = (TextView) findViewById(R.id.submit);
        ll_saomiao_div = (LinearLayout) findViewById(R.id.saomiao_div);
        ll_split2 = (LinearLayout) findViewById(R.id.split2);
        ftvToggle = (FontTextView) findViewById(R.id.toggle);
        ftv_scanIcon =(FontTextView) findViewById(R.id.scanIcon);
    }

    /**
     * 计算使用积分的折算额
     */
    private void countIntegral() {
        // 要抵换的积分
        String exchangedPointStr = et_integral.getText().toString();
        if (null == exchangedPointStr || "".equals(exchangedPointStr) || "null".equals(exchangedPointStr)) {
            exchangedPointStr = "0";
        }
        // 实付金额
        String paidStr = et_paid.getText().toString();
        if (null == paidStr || "".equals(paidStr) || "null".equals(paidStr)) {
            paidStr = "0";
        }
        // 抵扣的积分转换类型
        double exchangedPoint = Double.parseDouble(exchangedPointStr);
        // 抵扣的积分转换类型
        double paid = Double.parseDouble(paidStr);
        // 判断输入的积分是否越界
        if (exchangedPoint > usablePoint.doubleValue()) {
            et_integral.setText(null);
            et_exchange_amount.setText("0.00");
            et_integral.setHint("可用:" + usablePoint);
            Toast.makeText(SalesTicketActivity.this, "积分消费不能超过" + usablePoint + "分", Toast.LENGTH_LONG).show();
        } else {
            // 根据选择的积分兑换规则进行积分兑换
            if (spMap != null) {
                // 自动递增标志
                boolean autoIncrementFlag = Boolean.parseBoolean(String.valueOf(spMap.get("AutoIncrementFlag")));
                // 兑换积分
                double integralExchange = Double.parseDouble((String.valueOf(spMap.get("IntegralExchange"))));
                // 兑换积分抵扣金额
                double invoiceDiscount = Double.parseDouble((String.valueOf(spMap.get("InvoiceDiscount"))));

                if (autoIncrementFlag) { // 兑换可递增
                    // 判断是否满足兑换条件
                    if (exchangedPoint % integralExchange == 0) {
                        // 兑换倍数
                        double multiple = exchangedPoint / integralExchange;
                        // 记录积分兑换折让的金额
                        exchangeMoney = multiple * invoiceDiscount;
                        // 判断抵扣的金额是否大于实付金额
                        if (exchangeMoney > paid) {
                            // 剩余积分显示
                            et_integral.setHint("可用:" + usablePoint);
                            et_integral.setText(null);
                            et_paid.setText(paidStr);
                            et_exchange_amount.setText("0.00");
                            Toast.makeText(SalesTicketActivity.this, "积分抵扣金额不能超过实付金额", Toast.LENGTH_LONG).show();
                            return;
                        }
                        et_exchange_amount.setText(Tools.formatDecimal(String.valueOf(exchangeMoney)));
                    } else {
                        // 剩余积分显示
                        et_integral.setHint("可用:" + usablePoint);
                        et_integral.setText(null);
                        et_exchange_amount.setText("0.00");
                        Toast.makeText(SalesTicketActivity.this, "输入的积分不符合积分兑换条件", Toast.LENGTH_LONG).show();
                    }
                } else { // 兑换不可递增
                    if (exchangedPoint == integralExchange) {
                        // 记录积分兑换折让的金额
                        exchangeMoney = invoiceDiscount;
                        et_exchange_amount.setText(Tools.formatDecimal(String.valueOf(exchangeMoney)));
                    } else {
                        // 剩余积分显示
                        et_integral.setHint("可用:" + usablePoint);
                        et_integral.setText(null);
                        et_exchange_amount.setText("0.00");
                        Toast.makeText(SalesTicketActivity.this, "输入的积分不符合积分兑换条件", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
        // 重新计算金额
        countRetailAmount();
    }

    /**
     * 计算实际金额
     */
    private void countRetailAmount() {
        double temp = 0;
        double sum = 0;
        double discount = 0;
        discountMoney = et_discount.getText().toString();
        if (null == discountMoney || "".equals(discountMoney)) {
            discountMoney = "0";
        }
        discount = new BigDecimal(discountMoney).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        et_discount.setText(String.valueOf(discount));
        exchangeAmount = et_exchange_amount.getText().toString();
        if (null == exchangeAmount || "".equals(exchangeAmount)) {
            exchangeAmount = "0";
        }
        temp = Double.parseDouble(exchangeAmount);
        sum = Double.parseDouble(tvAmount);
        sum = sum - discount - temp;
        retailAmount = String.valueOf(sum);
        et_paid.setText(Tools.formatDecimal(String.valueOf(sum)));
    }

    @Override
    protected void onHeadLeftButton(View v) {
        // 点击返回时返回上一页面并刷新
        saveOrNot();
    }

    /**
     * 点击返回后询问是否保存单据内容
     */
    private void saveOrNot() {
        if (dataList.size() > 0 && "提交".equals(bt_submit.getText())) {
            // 询问是否删除
            Builder dialog = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
            dialog.setTitle("保存提示");
            dialog.setMessage("销售单尚未提交,确定要返回吗？");
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
     * 点击返回后的操作
     */
    private void back() {
        if (null == posSalesId || "".equals(posSalesId)) {
            finish();
        }
    }

    /**
     * ListView长按操作
     */
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
        if (posSalesNo != null && !"".equals(posSalesNo) && !"null".equals(posSalesNo)) {
            return false;
        }
        if (modifyRight) {
            final Map<String, Object> map = dataList.get(position);
            String tgoodsCode = String.valueOf(map.get("GoodsCode"));
            final String discountPrice = String.valueOf(map.get("DiscountPrice"));
            final String unitPrice = String.valueOf(map.get("UnitPrice"));
            final String discountRate = String.valueOf(map.get("DiscountRate"));
            String count = String.valueOf(map.get("Quantity"));
            if (Integer.parseInt(count) < 0) {
                Toast.makeText(SalesTicketActivity.this, "不能修改退货货品信息", Toast.LENGTH_SHORT).show();
                return false;
            }
            View v = (LinearLayout) getLayoutInflater().inflate(R.layout.menu_sales_ticket, null);
            final EditText et_price = (EditText) v.findViewById(R.id.et_price);
            final EditText et_count = (EditText) v.findViewById(R.id.et_count);
            final EditText et_discount = (EditText) v.findViewById(R.id.et_discount);
            final TextView tv_price = (TextView) v.findViewById(R.id.tv_price);
            tv_price.setText("折后价");
            LinearLayout ll_meno = (LinearLayout) v.findViewById(R.id.ll_meno);
            ll_meno.setVisibility(View.GONE);
            // 判断用户是否允许修改单价
            if (!LoginParameterUtil.discountRatePermitFlag || (LoginParameterUtil.discountRatePermitFlag && LoginParameterUtil.discountRate == 0) || (LoginParameterUtil.discountRatePermitFlag && LoginParameterUtil.discountRate == 10)) {
                LinearLayout ll_price = (LinearLayout) v.findViewById(R.id.ll_price);
                View v_price = (View) v.findViewById(R.id.v_price);
                ll_price.setVisibility(View.GONE);
                v_price.setVisibility(View.GONE);
                LinearLayout ll_discount = (LinearLayout) v.findViewById(R.id.ll_discount);
                View v_discount = (View) v.findViewById(R.id.v_price);
                ll_discount.setVisibility(View.GONE);
                v_discount.setVisibility(View.GONE);
            }
            // 询问是否删除
            Builder dialog = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
            dialog.setMessage("编辑货品" + tgoodsCode + "录入信息 :");
            et_price.setText(discountPrice);
            et_discount.setText(discountRate);
            et_count.setText(count);
            et_price.setSelection(discountPrice.length());
            et_discount.setSelection(discountRate.length());
            et_count.setSelection(count.length());
            dialog.setView(v);
            // 相当于点击确认按钮
            dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String tprice = et_price.getText().toString();
                    String tcount = et_count.getText().toString();
                    String tdiscountRate = et_discount.getText().toString();
                    if ("".equals(tprice.trim())) {
                        tprice = "0";
                    }
                    if ("".equals(tdiscountRate.trim())) {
                        tdiscountRate = "10";
                    }
                    if (Double.parseDouble(tdiscountRate.trim()) > 10) {
                        Toast.makeText(SalesTicketActivity.this, "折扣输入不合法", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // 判断货品的折扣是否在设置的范围内
                    if ("".equals(tcount.trim())) {
                        tcount = "0";
                    }
                    if (Integer.parseInt(tcount) < 1) {
                        dataList.remove(position);
                    }
                    // 修改单个货品的数量
                    map.put("Quantity", tcount);
                    // 修改单个货品的折扣
                    if (!tdiscountRate.equals(discountRate)) {
                        // 判断折扣是否在设置的范围内
                        if (Double.parseDouble(tdiscountRate) <= LoginParameterUtil.discountRate) {
                            Toast.makeText(SalesTicketActivity.this, "当前货品折扣大于前台设置的最小货品折扣", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        map.put("DiscountRate", tdiscountRate);
                        BigDecimal tempPrice = new BigDecimal(unitPrice).multiply(new BigDecimal(tdiscountRate)).divide(new BigDecimal(10), 2).setScale(2, BigDecimal.ROUND_HALF_UP);
                        map.put("DiscountPrice", String.valueOf(tempPrice));
                    }
                    // 修改单个货品的单价
                    boolean flag = false;
                    if (!discountPrice.equals(tprice)) {
                        if (new BigDecimal(unitPrice).compareTo(BigDecimal.ZERO) == 0) {
                            map.put("DiscountRate", String.valueOf("10"));
                            flag = true;
                        } else {
                            BigDecimal tempDiscountRate = new BigDecimal(Double.parseDouble(tprice) / Double.parseDouble(unitPrice) * 10).setScale(2, BigDecimal.ROUND_HALF_UP);
                            if (tempDiscountRate.compareTo(new BigDecimal(10)) > 0) {
                                map.put("DiscountRate", String.valueOf("10"));
                                flag = true;
                            } else {
                                if (tempDiscountRate.doubleValue() <= LoginParameterUtil.discountRate) {
                                    Toast.makeText(SalesTicketActivity.this, "当前货品折扣大于前台设置的最小货品折扣", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                map.put("DiscountRate", String.valueOf(tempDiscountRate));
                                flag = true;
                            }
                        }
                        if (flag) {
                            map.put("DiscountPrice", String.valueOf(tprice));
                        }
                    }
                    // 动态更新ListView
                    oddAdapter.notifyDataSetChanged();
                    // 计算价格
                    countTotal();
                    Toast.makeText(SalesTicketActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
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
            Builder dialog = new AlertDialog.Builder(SalesTicketActivity.this, AlertDialog.THEME_HOLO_LIGHT);
            dialog.setTitle("提示");
            dialog.setMessage("当前暂无修改销售小票的操作权限");
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

    @Override
    protected void onHeadRightButton(View v) {
        // 显示菜单
        showpopWinShare();
    }

    /**
     * 动态显示菜单选项
     */
    private void showpopWinShare() {
        if (popWinShare == null) {
            // 自定义的单击事件
            OnClickLintener paramOnClickListener = new OnClickLintener();
            popWinShare = new PopWinShare(SalesTicketActivity.this, paramOnClickListener);
            // 隐藏切换的选项
            popWinShare.ll_parent.removeView(popWinShare.layoutSwitch);
            popWinShare.ll_parent.removeView(popWinShare.layout_price);
            popWinShare.ll_parent.removeView(popWinShare.view);
            popWinShare.ll_parent.removeView(popWinShare.view_3);
            popWinShare.ll_parent.removeView(popWinShare.layoutAddCustomer);
            popWinShare.ll_parent.removeView(popWinShare.view_4);
            popWinShare.ll_parent.removeView(popWinShare.view_5);
            popWinShare.ll_parent.removeView(popWinShare.layoutBarcodeVerification);
            popWinShare.ll_parent.removeView(popWinShare.layoutGoodsBoxBarcode);
            popWinShare.ll_parent.removeView(popWinShare.view_6);
            popWinShare.ll_parent.removeView(popWinShare.layoutSwitchInputMode);
            popWinShare.ll_parent.removeView(popWinShare.view_7);
            popWinShare.ll_parent.removeView(popWinShare.view_8);
            popWinShare.ll_parent.removeView(popWinShare.view_9);
            popWinShare.ll_parent.removeView(popWinShare.layoutBarcodeVerificationDifferent);
            popWinShare.ll_parent.removeView(popWinShare.layoutExportExcel);
            popWinShare.ll_parent.removeView(popWinShare.view_10);
            popWinShare.ll_parent.removeView(popWinShare.layoutSingleDiscount);
            // popWinShare.ll_parent.removeView(popWinShare.view_11);
            // popWinShare.ll_parent.removeView(popWinShare.layoutOtherOptions);
            TextView tvOtherOptions = popWinShare.tvOtherOptions;
            tvOtherOptions.setText("退货");
            FontTextView ftvOtherOptions = popWinShare.ftvOtherOptions;
            ftvOtherOptions.setText(R.string.noReturn);
            if (posSalesNo != null && !"".equals(posSalesNo) && !"null".equals(posSalesNo)) {
                popWinShare.ll_parent.removeView(popWinShare.view_11);
                popWinShare.ll_parent.removeView(popWinShare.layoutOtherOptions);
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
     * Title: OnClickLintener Description: 菜单选择点击后监听
     */
    class OnClickLintener implements OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
            // 添加备注
                case R.id.layout_remark:
                    popWinShare.dismiss();
                    Intent intent = new Intent(SalesTicketActivity.this, RemarkActivity.class);
                    intent.putExtra("posSalesId", posSalesId);
                    intent.putExtra("memo", memo);
                    startActivityForResult(intent, 1);
                    overridePendingTransition(R.anim.activity_open, 0);
                    break;
                // 退货
                case R.id.layout_other_options:
                    popWinShare.dismiss();
                    if (employeeId == null) {
                        Toast.makeText(SalesTicketActivity.this, "请先选择售货员", Toast.LENGTH_SHORT).show();
                        et_barcode.setText(null);
                        return;
                    }
                    View view = (LinearLayout) getLayoutInflater().inflate(R.layout.menu_sales_ticket_return, null);
                    final LinearLayout llSalesNo = (LinearLayout) view.findViewById(R.id.ll_sales_no);
                    final CheckBox cbNotUseOriginalReturn = (CheckBox) view.findViewById(R.id.notUseOriginalReturn);
                    TextView tvNotUseOriginalReturn = (TextView) view.findViewById(R.id.tvNotUseOriginalReturn);
                    final EditText etSalesNo = (EditText) view.findViewById(R.id.et_sales_no);
                    final EditText etBarcode = (EditText) view.findViewById(R.id.et_barcode);
                    cbNotUseOriginalReturn.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            useOriginalReturnFlag = cbNotUseOriginalReturn.isChecked();
                            if (useOriginalReturnFlag) {
                                llSalesNo.setVisibility(View.GONE);
                                etSalesNo.setText(null);
                                etBarcode.setFocusable(true);
                            } else {
                                llSalesNo.setVisibility(View.VISIBLE);
                                etSalesNo.setFocusable(true);
                            }
                        }
                    });
                    tvNotUseOriginalReturn.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            useOriginalReturnFlag = cbNotUseOriginalReturn.isChecked();
                            if (useOriginalReturnFlag) {
                                llSalesNo.setVisibility(View.GONE);
                                etSalesNo.setText(null);
                                etBarcode.setFocusable(true);
                            } else {
                                llSalesNo.setVisibility(View.VISIBLE);
                                etSalesNo.setFocusable(true);
                            }
                        }
                    });
                    Builder dialog1 = new AlertDialog.Builder(SalesTicketActivity.this, AlertDialog.THEME_HOLO_LIGHT);
                    dialog1.setTitle("选择退货方式");
                    dialog1.setView(view);
                    // 相当于点击确认按钮
                    dialog1.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (useOriginalReturnFlag) {// 不使用原单退货
                                String barcode = etBarcode.getText().toString().trim();
                                if (barcode == null || "".equals(barcode) || "null".equalsIgnoreCase(barcode)) {
                                    Toast.makeText(SalesTicketActivity.this, "退货条码不能为空", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                ajaxAddItem(barcode, -1);
                                type = "换货";
                            } else {// 使用原单退货
                                String salesNo = etSalesNo.getText().toString().trim();
                                String barcode = etBarcode.getText().toString().trim();
                                if (salesNo == null || "".equals(salesNo) || "null".equalsIgnoreCase(salesNo)) {
                                    ajaxAddItem(barcode, -1);
                                } else {
                                    getSalesTicketReturnDateBySalesNo(salesNo, barcode);
                                }
                                type = "换货";
                            }
                        }
                    });
                    // 相当于点击取消按钮
                    dialog1.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {}
                    });
                    dialog1.setCancelable(false);
                    dialog1.create();
                    dialog1.show();

                    break;
                case R.id.layout_print:
                    popWinShare.dismiss();
                    // Intent intents = new Intent(SalesTicketActivity.this,
                    // PrintActivity.class);
                    // intents.putExtra("id", posSalesId);
                    // intents.putExtra("tableName", "posSales");
                    // intents.putExtra("docType", "销售小票");
                    // startActivity(intents);
                    // print();
                    getPOSsalesDetails(posSalesNo, vipCode);
                    break;
                default:
                    popWinShare.dismiss();
                    break;
            }
        }
    }

    /**
     * 根据销售单号获取对应的退货信息
     * 
     * @param salesNo
     * @param barcode
     */
    private void getSalesTicketReturnDateBySalesNo(String salesNo, String barcode) {
        RequestVo vo = new RequestVo();
        vo.requestUrl = querySalesTicketReturnDateBySalesNoPath;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("salesNo", salesNo);
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
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject barCodeInfo = jsonArray.getJSONObject(i);
                            BarCode bc = new BarCode();
                            bc.setBarcode(barCodeInfo.getString("BarCode"));
                            bc.setGoodsid(barCodeInfo.getString("GoodsID"));
                            bc.setGoodscode(barCodeInfo.getString("GoodsCode"));
                            bc.setGoodsname(barCodeInfo.getString("GoodsName"));
                            bc.setColorid(barCodeInfo.getString("ColorID"));
                            bc.setColorcode(barCodeInfo.getString("ColorCode"));
                            bc.setColorname(barCodeInfo.getString("ColorName"));
                            bc.setSizeid(barCodeInfo.getString("SizeID"));
                            bc.setSizename(barCodeInfo.getString("SizeName"));
                            bc.setSizecode(barCodeInfo.getString("SizeCode"));
                            bc.setIndexno(null);
                            bc.setGoodsDiscountFlag("false");
                            bc.setGoodsDiscount(new BigDecimal(String.valueOf(barCodeInfo.get("Discount"))));
                            bc.setDiscountRate(new BigDecimal(String.valueOf(barCodeInfo.get("Discount"))));
                            bc.setQty(barCodeInfo.getInt("Quantity"));
                            bc.setRetailSales(new BigDecimal(barCodeInfo.getDouble("RetailSales")));
                            bc.setUnitPrice(new BigDecimal(barCodeInfo.getDouble("UnitPrice")));
                            if (barCodeInfo.isNull("DiscountPrice")) {
                                bc.setDiscountPrice(null);
                            } else {
                                bc.setDiscountPrice(new BigDecimal(barCodeInfo.getDouble("DiscountPrice")));
                            }
                            addItem(bc);
                        }
                    } else {
                        Toast.makeText(SalesTicketActivity.this, "条码错误或销售单号错误", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(SalesTicketActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 检查是否日结
     */
    private void checkDailyKnots() {
        RequestVo vo = new RequestVo();
        vo.requestUrl = checkDailyKnots;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("date", Tools.dateToString(new Date()));
        map.put("departmentId", LoginParameterUtil.deptId);
        vo.requestDataMap = map;
        super.getDataFromServer(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    if (retObj.getBoolean("success")) {
                        boolean dayEndFlag = retObj.getBoolean("obj");
                        // 未日结
                        if (dayEndFlag) {
                            Builder dialog1 = new AlertDialog.Builder(SalesTicketActivity.this, AlertDialog.THEME_HOLO_LIGHT);
                            dialog1.setTitle("系统提示");
                            dialog1.setMessage("当前店铺已日结，无法新增销售小票！");
                            // 相当于点击确认按钮
                            dialog1.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            });
                            dialog1.setCancelable(false);
                            dialog1.create();
                            dialog1.show();
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(SalesTicketActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 打小票
     */
    private void getPOSsalesDetails(final String no, final String vipCode) {
        RequestVo vo = new RequestVo();
        vo.requestUrl = getPossalesDetailByNo;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("no", no);
        vo.requestDataMap = map;
        super.getDataFromServer(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    if (retObj.getBoolean("success")) {
                        JSONObject jsonMap = retObj.getJSONObject("obj");
                        JSONObject jsonObject = jsonMap.getJSONObject("possales");
                        JSONArray jsonArray = jsonMap.getJSONArray("possalesDetail");
                        print(no, vipCode, jsonObject, jsonArray);
                    }
                } catch (Exception e) {
                    Toast.makeText(SalesTicketActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 打印小票
     */
    private void print(String posSalesNo, String vipCode, JSONObject jsonObject, JSONArray jsonArray) {
        try {
            if (jsonArray.length() == 0) {
                Toast.makeText(SalesTicketActivity.this, "当前没有小票数据", Toast.LENGTH_SHORT).show();
                return;
            }
            // 封装打印数据
            StringBuilder sb = new StringBuilder();
            if (LoginParameterUtil.possalesTile != null && !"".equals(LoginParameterUtil.possalesTile) && !"null".equalsIgnoreCase(LoginParameterUtil.possalesTile)) {
                int len = Tools.countStringLength(LoginParameterUtil.possalesTile);
                len = Double.valueOf(Math.floor((32 - len) / 2)).intValue();
                String emptyCharacter = Tools.generalEmptyCharacter(len);
                sb.append(emptyCharacter + LoginParameterUtil.possalesTile + emptyCharacter);
            }
            if (LoginParameterUtil.possalesParam1 != null && !"".equals(LoginParameterUtil.possalesParam1) && !"null".equalsIgnoreCase(LoginParameterUtil.possalesParam1)) {
                int len = Tools.countStringLength(LoginParameterUtil.possalesParam1);
                len = Double.valueOf(Math.floor((32 - len) / 2)).intValue();
                String emptyCharacter = Tools.generalEmptyCharacter(len);
                sb.append(emptyCharacter + LoginParameterUtil.possalesParam1 + emptyCharacter);
            }
            if (LoginParameterUtil.possalesParam2 != null && !"".equals(LoginParameterUtil.possalesParam2) && !"null".equalsIgnoreCase(LoginParameterUtil.possalesParam2)) {
                int len = Tools.countStringLength(LoginParameterUtil.possalesParam2 + "   " + LoginParameterUtil.deptName);
                len = Double.valueOf(Math.floor((32 - len) / 2)).intValue();
                String emptyCharacter = Tools.generalEmptyCharacter(len);
                sb.append(emptyCharacter + LoginParameterUtil.possalesParam2 + "   " + LoginParameterUtil.deptName + emptyCharacter);
            }
            sb.append("\n");
            sb.append("开单 : ");
            sb.append(jsonObject.getString("Employee"));
            sb.append("\n");
            sb.append("日期 : ");
            sb.append(jsonObject.getString("Date"));
            sb.append("\n");
            sb.append("打印 : ");
            sb.append(Tools.dateTimeToString(new Date()));
            sb.append("\n");
            sb.append("票号 : ");
            sb.append(posSalesNo);
            sb.append("\n");
            sb.append("--------------------------------");
            sb.append("\n");
            sb.append("商品号");
            sb.append("\n");
            sb.append("商品名   数量*折扣*单价   实收");
            sb.append("\n");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonMap = jsonArray.getJSONObject(i);
                String barCode = jsonMap.getString("Barcode");
                String goodsName = jsonMap.getString("GoodsName");
                String unitPrice = jsonMap.getString("UnitPrice");
                String quantity = jsonMap.getString("Quantity");
                String discountRate = jsonMap.getString("DiscountRate");
                String amount = jsonMap.getString("Amount");
                sb.append(barCode);
                sb.append("\n");
                sb.append(goodsName);
                sb.append("    ");
                sb.append(quantity);
                sb.append("*");
                sb.append(discountRate);
                sb.append("*");
                sb.append(unitPrice);
                sb.append("    ");
                sb.append(amount);
                sb.append("\n");
            }
            sb.append("--------------------------------");
            sb.append("合计 : ");
            sb.append(jsonObject.getString("AmountSum"));
            sb.append("\n");
            sb.append("收银 : ");
            sb.append(jsonObject.getString("FactAmountSum"));
            sb.append("\n");
            if (vipCode != null && !"".equals(vipCode) && !"null".equalsIgnoreCase(vipCode)) {
                sb.append("VIP卡号 : ");
                sb.append(vipCode);
                sb.append("\n");
            }
            sb.append("已售商品数量 : ");
            sb.append(jsonObject.getString("QuantitySum"));
            sb.append("\n");
            sb.append("\n");
            if (LoginParameterUtil.possalesParam3 != null && !"".equals(LoginParameterUtil.possalesParam3) && !"null".equalsIgnoreCase(LoginParameterUtil.possalesParam3)) {
                int len = Tools.countStringLength(LoginParameterUtil.possalesParam3);
                len = Double.valueOf(Math.floor((32 - len) / 2)).intValue();
                String emptyCharacter = Tools.generalEmptyCharacter(len);
                sb.append(emptyCharacter + LoginParameterUtil.possalesParam3 + emptyCharacter);
            }
            if (LoginParameterUtil.possalesParam4 != null && !"".equals(LoginParameterUtil.possalesParam4) && !"null".equalsIgnoreCase(LoginParameterUtil.possalesParam4)) {
                int len = Tools.countStringLength(LoginParameterUtil.possalesParam4);
                len = Double.valueOf(Math.floor((32 - len) / 2)).intValue();
                String emptyCharacter = Tools.generalEmptyCharacter(len);
                sb.append(emptyCharacter + LoginParameterUtil.possalesParam4 + emptyCharacter);
            }
            if (LoginParameterUtil.possalesParam5 != null && !"".equals(LoginParameterUtil.possalesParam5) && !"null".equalsIgnoreCase(LoginParameterUtil.possalesParam5)) {
                int len = Tools.countStringLength(LoginParameterUtil.possalesParam5);
                len = Double.valueOf(Math.floor((32 - len) / 2)).intValue();
                String emptyCharacter = Tools.generalEmptyCharacter(len);
                sb.append(emptyCharacter + LoginParameterUtil.possalesParam5 + emptyCharacter);
            }
            sb.append("\n");
            sb.append("\n");
            sb.append("\n");
            printIntent.putExtra("text", sb.toString());
            sendBroadcast(printIntent);
            Toast.makeText(SalesTicketActivity.this, "正在打印小票", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(SalesTicketActivity.this, "打印发生错误，请检查打印参数是否正确并稍后重试", Toast.LENGTH_LONG).show();
            Logger.e(TAG, e.getMessage());
        }
    }

    /**
     * 打印小票
     */
    private void print() {
        try {
            if (dataList.size() == 0) {
                Toast.makeText(SalesTicketActivity.this, "当前没有小票数据", Toast.LENGTH_SHORT).show();
                return;
            }
            StringBuilder sb = new StringBuilder();
            if (LoginParameterUtil.possalesTile != null && !"".equals(LoginParameterUtil.possalesTile) && !"null".equalsIgnoreCase(LoginParameterUtil.possalesTile)) {
                int len = Tools.countStringLength(LoginParameterUtil.possalesTile);
                len = Double.valueOf(Math.floor((32 - len) / 2)).intValue();
                String emptyCharacter = Tools.generalEmptyCharacter(len);
                sb.append(emptyCharacter + LoginParameterUtil.possalesTile + emptyCharacter);
            }
            if (LoginParameterUtil.possalesParam1 != null && !"".equals(LoginParameterUtil.possalesParam1) && !"null".equalsIgnoreCase(LoginParameterUtil.possalesParam1)) {
                int len = Tools.countStringLength(LoginParameterUtil.possalesParam1);
                len = Double.valueOf(Math.floor((32 - len) / 2)).intValue();
                String emptyCharacter = Tools.generalEmptyCharacter(len);
                sb.append(emptyCharacter + LoginParameterUtil.possalesParam1 + emptyCharacter);
            }
            if (LoginParameterUtil.possalesParam2 != null && !"".equals(LoginParameterUtil.possalesParam2) && !"null".equalsIgnoreCase(LoginParameterUtil.possalesParam2)) {
                int len = Tools.countStringLength(LoginParameterUtil.possalesParam2 + "   " + LoginParameterUtil.deptName);
                len = Double.valueOf(Math.floor((32 - len) / 2)).intValue();
                String emptyCharacter = Tools.generalEmptyCharacter(len);
                sb.append(emptyCharacter + LoginParameterUtil.possalesParam2 + "   " + LoginParameterUtil.deptName + emptyCharacter);
            }
            sb.append("\n");
            sb.append("开单 : ");
            sb.append(et_salesperson.getText().toString());
            sb.append("\n");
            sb.append("日期 : ");
            sb.append(Tools.dateTimeToString(new Date()));
            sb.append("\n");
            sb.append("打印 : ");
            sb.append(Tools.dateTimeToString(new Date()));
            sb.append("\n");
            sb.append("票号 : ");
            sb.append(posSalesNo);
            sb.append("\n");
            sb.append("--------------------------------");
            sb.append("\n");
            sb.append("商品号");
            sb.append("\n");
            sb.append("商品名   数量*折扣*单价   实收");
            sb.append("\n");
            for (int i = 0; i < dataList.size(); i++) {
                Map<String, Object> map = dataList.get(i);
                String barCode = String.valueOf(map.get("Barcode"));
                String goodsName = String.valueOf(map.get("GoodsName"));
                // String discountPrice = String.valueOf(map.get("DiscountPrice"));
                String unitPrice = String.valueOf(map.get("UnitPrice"));
                String quantity = String.valueOf(map.get("Quantity"));
                String discountRate = String.valueOf(map.get("DiscountRate"));
                BigDecimal totalPrice = new BigDecimal(Integer.parseInt(quantity) * Double.parseDouble(discountRate) * Double.parseDouble(unitPrice) / 10).setScale(2, BigDecimal.ROUND_HALF_UP);
                sb.append(barCode);
                sb.append("\n");
                sb.append(goodsName);
                sb.append("    ");
                sb.append(quantity);
                sb.append("*");
                sb.append(discountRate);
                sb.append("*");
                sb.append(unitPrice);
                sb.append("    ");
                sb.append(totalPrice);
                sb.append("\n");
            }
            sb.append("--------------------------------");
            sb.append("合计 : ");
            sb.append(tv_amount.getText().toString());
            sb.append("\n");
            sb.append("收银 : ");
            sb.append(et_paid.getText().toString());
            sb.append("\n");
            if (vipCode != null && !"".equals(vipCode) && !"null".equalsIgnoreCase(vipCode)) {
                sb.append("VIP卡号 : ");
                sb.append(vipCode);
                sb.append("\n");
                sb.append("可用积分 : ");
                sb.append(availableIntegral);
                sb.append("\n");
            }
            sb.append("已售商品数量 : ");
            sb.append(tv_qtysum.getText().toString());
            sb.append("\n");
            sb.append("\n");
            if (LoginParameterUtil.possalesParam3 != null && !"".equals(LoginParameterUtil.possalesParam3) && !"null".equalsIgnoreCase(LoginParameterUtil.possalesParam3)) {
                int len = Tools.countStringLength(LoginParameterUtil.possalesParam3);
                len = Double.valueOf(Math.floor((32 - len) / 2)).intValue();
                String emptyCharacter = Tools.generalEmptyCharacter(len);
                sb.append(emptyCharacter + LoginParameterUtil.possalesParam3 + emptyCharacter);
            }
            if (LoginParameterUtil.possalesParam4 != null && !"".equals(LoginParameterUtil.possalesParam4) && !"null".equalsIgnoreCase(LoginParameterUtil.possalesParam4)) {
                int len = Tools.countStringLength(LoginParameterUtil.possalesParam4);
                len = Double.valueOf(Math.floor((32 - len) / 2)).intValue();
                String emptyCharacter = Tools.generalEmptyCharacter(len);
                sb.append(emptyCharacter + LoginParameterUtil.possalesParam4 + emptyCharacter);
            }
            if (LoginParameterUtil.possalesParam5 != null && !"".equals(LoginParameterUtil.possalesParam5) && !"null".equalsIgnoreCase(LoginParameterUtil.possalesParam5)) {
                int len = Tools.countStringLength(LoginParameterUtil.possalesParam5);
                len = Double.valueOf(Math.floor((32 - len) / 2)).intValue();
                String emptyCharacter = Tools.generalEmptyCharacter(len);
                sb.append(emptyCharacter + LoginParameterUtil.possalesParam5 + emptyCharacter);
            }
            sb.append("\n");
            sb.append("\n");
            sb.append("\n");
            printIntent.putExtra("text", sb.toString());
            sendBroadcast(printIntent);
            Toast.makeText(SalesTicketActivity.this, "正在打印小票", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(SalesTicketActivity.this, "打印发生错误，请检查打印参数是否正确并稍后重试", Toast.LENGTH_LONG).show();
            Logger.e(TAG, e.getMessage());
        }
    }
}
