package com.fuxi.activity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.dommy.qrcode.util.Constant;
import com.fuxi.activity.BaseWapperActivity.DataCallback;
import com.fuxi.adspter.SalesTicketAdapter;
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
 * Title: GiftActivity Description: 赠品单活动界面
 * 
 * @author LYJ
 * 
 */
public class GiftActivity extends BaseWapperActivity implements OnItemLongClickListener {

    private static final String TAG = "GiftActivity";
    private static final String analyticalBarcode = "/select.do?analyticalBarcode";
    private static final String saveMethod = "/gift.do?saveGift";
    private static final String checkDailyKnots = "/dailyknots.do?checkDailyKnots";

    private List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
    private ArrayList<HashMap<String, Object>> tempDatas = new ArrayList<HashMap<String, Object>>();// 负库存检查返回的记录
    private SalesTicketAdapter oddAdapter;
    private Intent printIntent = new Intent("COM.QSPDA.PRINTTEXT");
    private TouchListener tl = new TouchListener();

    private LinearLayout ll_saomiao_div; // 扫码区
    private LinearLayout ll_split2; // 分隔符2
    private PopWinShare popWinShare;
    private ListView lv_detail;
    private EditText et_qty;
    private EditText et_barcode;
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
    private String vipCode;
    private String goodsCode; // 货品编码
    private String goodsName; // 货品名称
    private String goodsRetailSales; // 商品零售价
    private String goodsDiscount; // 商品折扣
    private String memo;
    private boolean addRight = false;
    private boolean modifyRight = false;
    private boolean inputFlag = false; // 输入框是否可用的标志

    /**
     * 提交保存数据
     */
    private void submitData() {
        String qty = tv_qtysum.getText().toString();
        RequestVo vo = new RequestVo();
        vo.requestUrl = saveMethod;
        vo.context = this;
        HashMap map = new HashMap();
        net.sf.json.JSONArray json = net.sf.json.JSONArray.fromObject(dataList);
        map.put("data", json.toString());
        map.put("amount", tv_amount.getText().toString());
        map.put("vipId", vipId);
        map.put("vipCode", vipCode);
        map.put("employeeId", employeeId);
        map.put("qty", String.valueOf(qty));
        map.put("memo", memo);
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
                            Builder dialog = new AlertDialog.Builder(GiftActivity.this, AlertDialog.THEME_HOLO_LIGHT);
                            dialog.setTitle("提示");
                            dialog.setMessage("单据中含有负库存货品，无法执行保存操作!");
                            // 相当于点击确认按钮
                            dialog.setPositiveButton("点击查看", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // 负库存货品显示界面
                                    Intent intent = new Intent(GiftActivity.this, NegativeStockActivity.class);
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
                            if (null != posSalesId || "".equals(posSalesId) || "null".equalsIgnoreCase(posSalesId)) {
                                // 隐藏扫码区
                                ll_saomiao_div.setVisibility(View.GONE);
                                ll_split2.setVisibility(View.GONE);
                                // 继续操作
                                bt_submit.setText("新增");
                                // 打印小票
                                print();
                            }
                            posSalesId = null;
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(GiftActivity.this, "系统错误", Toast.LENGTH_LONG).show();
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
                    Intent intent = new Intent(GiftActivity.this, GiftActivity.class);
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
                    et_barcode.setOnTouchListener(null);
                    et_barcode.setCompoundDrawables(null, null, null, null);
                    SpannableString s = new SpannableString("输入条码/货号");
                    et_barcode.setHint(s);
                    et_barcode.requestFocus();
                    ftv_scanIcon.setVisibility(View.VISIBLE);
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
            ActivityCompat.requestPermissions(GiftActivity.this, new String[]{Manifest.permission.CAMERA}, Constant.REQ_PERM_CAMERA);
            return;
        }
        // 申请文件读写权限（部分朋友遇到相册选图需要读写权限的情况，这里一并写一下）
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // 申请权限
            ActivityCompat.requestPermissions(GiftActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, Constant.REQ_PERM_EXTERNAL_STORAGE);
            return;
        }
        // 二维码扫码
        Intent intent = new Intent(GiftActivity.this, CaptureActivity.class);
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
                    Toast.makeText(GiftActivity.this, "请至权限中心打开本应用的相机访问权限", Toast.LENGTH_LONG).show();
                }
                break;
            case Constant.REQ_PERM_EXTERNAL_STORAGE:
                // 文件读写权限申请
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 获得授权
                    startQrCode();
                } else {
                    // 被禁止授权
                    Toast.makeText(GiftActivity.this, "请至权限中心打开本应用的文件读写权限", Toast.LENGTH_LONG).show();
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
            Toast.makeText(GiftActivity.this, "请先选择售货员", Toast.LENGTH_SHORT).show();
            et_barcode.setText(null);
            return;
        }
        String barcodeStr = et_barcode.getText().toString();
        if ("".equals(barcodeStr)) {
            Toast.makeText(GiftActivity.this, R.string.barcode_null, Toast.LENGTH_SHORT).show();
            return;
        }
        Integer qty = Integer.valueOf(et_qty.getText().toString());
        ajaxAddItem(barcodeStr, qty);
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
                        boolean presentFlag = barCodeInfo.getBoolean("PresentFlag");
                        if (!presentFlag) {
                            Toast.makeText(GiftActivity.this, "当前货品为非赠品", Toast.LENGTH_SHORT).show();
                            return;
                        }
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
                        bc.setGoodsDiscount(new BigDecimal(String.valueOf(barCodeInfo.get("Discount"))));
                        bc.setDiscountRate(new BigDecimal(String.valueOf(barCodeInfo.get("Discount"))));
                        bc.setQty(qty);
                        if (barCodeInfo.isNull("RetailSales")) {
                            bc.setRetailSales(null);
                        } else {
                            bc.setRetailSales(new BigDecimal(barCodeInfo.getDouble("RetailSales")));
                        }
                        bc.setUnitPrice(bc.getRetailSales());
                        addItem(bc);
                    } else {
                        et_barcode.selectAll();
                        Toast.makeText(GiftActivity.this, "条码错误或不存在此条码", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(GiftActivity.this, "系统错误", Toast.LENGTH_LONG).show();
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
            temp.put("UnitPrice", 0);
            temp.put("DiscountPrice", 0);
            temp.put("DiscountRate", 0);
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
        setContentView(R.layout.activity_gift);
        setTitle("赠品单");
    }

    @Override
    protected void setListener() {
        oddAdapter = new SalesTicketAdapter(this, dataList);
        lv_detail.setAdapter(oddAdapter);
        lv_detail.setOnItemLongClickListener(this);
        et_salesperson.setOnTouchListener(tl);
        et_vip.setOnTouchListener(tl);
        bt_addDetail.setOnClickListener(this);
        bt_submit.setOnClickListener(this);
        ftvToggle.setOnClickListener(this);
        et_barcode.setOnEditorActionListener(new EditorActionListener());
        et_qty.setOnEditorActionListener(new EditorActionListener());
        ftv_scanIcon.setOnClickListener(this);
    }

    private class TouchListener implements OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (v.getId()) {
                case R.id.salesperson:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Intent intent = new Intent(GiftActivity.this, SelectActivity.class);
                        intent.putExtra("selectType", "selectEmployee");
                        startActivityForResult(intent, R.id.salesperson);
                        overridePendingTransition(R.anim.activity_open, 0);
                    }
                    break;
                case R.id.vip:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Intent intent = new Intent(GiftActivity.this, SelectActivity.class);
                        intent.putExtra("selectType", "selectVip");
                        startActivityForResult(intent, R.id.vip);
                        overridePendingTransition(R.anim.activity_open, 0);
                    }
                    break;
                case R.id.barcode:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Intent intent = new Intent(GiftActivity.this, SelectActivity.class);
                        intent.putExtra("selectType", "selectPresentGoods");
                        startActivityForResult(intent, R.id.barcode);
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
                    goodsName = data.getStringExtra("GoodsName");
                    goodsCode = data.getStringExtra("Code");
                    goodsDiscount = data.getStringExtra("Discount");
                    if (LoginParameterUtil.multiSelectType == 1) {
                        // 选择多颜色和尺码方式二
                        Intent intent = new Intent(GiftActivity.this, MultiSelectNewWayActivity.class);
                        intent.putExtra("goodsId", goodsId);
                        intent.putExtra("deptId", LoginParameterUtil.deptId);
                        intent.putExtra("tableName", "posSales");
                        startActivityForResult(intent, 10);
                        overridePendingTransition(R.anim.activity_open, 0);
                    } else {
                        // 选择多颜色和尺码方式一
                        Intent intent = new Intent(GiftActivity.this, MultiSelectActivity.class);
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
                        bc.setUnitPrice(bc.getRetailSales());
                        addItem(bc);
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
                addRight = LoginParameterUtil.giftMenuRight.get("AddRight");
                if (addRight) {
                    // 获取用户操作权限(修改)
                    modifyRight = LoginParameterUtil.giftMenuRight.get("ModifyRight");
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
                } else {
                    Builder dialog = new AlertDialog.Builder(GiftActivity.this, AlertDialog.THEME_HOLO_LIGHT);
                    dialog.setTitle("提示");
                    dialog.setMessage("当前暂无新增赠品单的操作权限");
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
                // 检查当前店铺是否日结
                checkDailyKnots();
            } else {
                AlertDialog.Builder dialog = new AlertDialog.Builder(GiftActivity.this, AlertDialog.THEME_HOLO_LIGHT);
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
            AlertDialog.Builder dialog = new AlertDialog.Builder(GiftActivity.this, AlertDialog.THEME_HOLO_LIGHT);
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
            double peice = 0;
            map.put("DiscountPrice", new BigDecimal(peice).setScale(2, BigDecimal.ROUND_HALF_UP));
            peice = new BigDecimal(peice).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            map.put("Amount", new BigDecimal(peice * num).setScale(2, BigDecimal.ROUND_HALF_UP));
            amount += peice * num;
            sum += num;
        }
        tv_qtysum.setText(String.valueOf(sum));
        tv_amount.setText(Tools.formatDecimal(String.valueOf(amount)));
        // 添加盘点单明细时显示保存按钮
        if (dataList.size() > 0) {
            // 当添加新的货品时候，把审核按钮变成保存按钮，如果是隐藏的则显示
            bt_submit.setVisibility(View.VISIBLE);
            bt_submit.setText("提交");
            // //售货员,VIP设置为可读
            et_vip.setEnabled(false);
        } else {
            bt_submit.setVisibility(View.GONE);
            // 售货员,VIP设置为可读
            et_salesperson.setEnabled(true);
            et_vip.setEnabled(true);
            // 设置扫码区自动获取焦点
            et_barcode.requestFocus();
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
        bt_addDetail = (TextView) findViewById(R.id.addDetail);
        bt_submit = (TextView) findViewById(R.id.submit);
        ll_saomiao_div = (LinearLayout) findViewById(R.id.saomiao_div);
        ll_split2 = (LinearLayout) findViewById(R.id.split2);
        ftvToggle = (FontTextView) findViewById(R.id.toggle);
        ftv_scanIcon =(FontTextView) findViewById(R.id.scanIcon);
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
            String count = String.valueOf(map.get("Quantity"));
            View v = (LinearLayout) getLayoutInflater().inflate(R.layout.menu_sales_ticket, null);
            final EditText et_count = (EditText) v.findViewById(R.id.et_count);
            LinearLayout ll_meno = (LinearLayout) v.findViewById(R.id.ll_meno);
            ll_meno.setVisibility(View.GONE);
            // 不显示单价修改
            LinearLayout ll_price = (LinearLayout) v.findViewById(R.id.ll_price);
            View v_price = (View) v.findViewById(R.id.v_price);
            ll_price.setVisibility(View.GONE);
            v_price.setVisibility(View.GONE);

            // 询问是否删除
            Builder dialog = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
            dialog.setMessage("修改货品" + tgoodsCode + "的数量 :");
            et_count.setText(count);
            et_count.setSelection(count.length());
            dialog.setView(v);
            // 相当于点击确认按钮
            dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String tcount = et_count.getText().toString();
                    if ("".equals(tcount.trim())) {
                        tcount = "0";
                    }
                    if (Integer.parseInt(tcount) < 1) {
                        dataList.remove(position);
                    }
                    // 修改单个货品的数量
                    map.put("Quantity", tcount);
                    // 动态更新ListView
                    oddAdapter.notifyDataSetChanged();
                    // 计算价格
                    countTotal();
                    Toast.makeText(GiftActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
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
            Builder dialog = new AlertDialog.Builder(GiftActivity.this, AlertDialog.THEME_HOLO_LIGHT);
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
            popWinShare = new PopWinShare(GiftActivity.this, paramOnClickListener);
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
            popWinShare.ll_parent.removeView(popWinShare.view_11);
            popWinShare.ll_parent.removeView(popWinShare.layoutOtherOptions);
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
                    Intent intent = new Intent(GiftActivity.this, RemarkActivity.class);
                    intent.putExtra("posSalesId", posSalesId);
                    intent.putExtra("memo", memo);
                    startActivityForResult(intent, 1);
                    overridePendingTransition(R.anim.activity_open, 0);
                    break;
                case R.id.layout_print:
                    popWinShare.dismiss();
                    print();
                    break;
                default:
                    popWinShare.dismiss();
                    break;
            }
        }
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
                        if (dayEndFlag) {
                            Builder dialog1 = new AlertDialog.Builder(GiftActivity.this, AlertDialog.THEME_HOLO_LIGHT);
                            dialog1.setTitle("系统提示");
                            dialog1.setMessage("当前店铺已日结，无法新增赠品单！");
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
                    Toast.makeText(GiftActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 打印小票
     */
    private void print() {
        try {
            if (dataList.size() == 0) {
                Toast.makeText(GiftActivity.this, "当前没有小票数据", Toast.LENGTH_SHORT).show();
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
                sb.append("赠品 " + goodsName);
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
            sb.append(tv_amount.getText().toString());
            sb.append("\n");
            if (vipCode != null && !"".equals(vipCode) && !"null".equalsIgnoreCase(vipCode)) {
                sb.append("VIP卡号 : ");
                sb.append(vipCode);
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
            Toast.makeText(GiftActivity.this, "正在打印小票", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(GiftActivity.this, "打印发生错误，请检查打印参数是否正确并稍后重试", Toast.LENGTH_LONG).show();
            Logger.e(TAG, e.getMessage());
        }
    }
}
