package com.fuxi.activity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import com.fuxi.activity.BaseWapperActivity.DataCallback;
import com.fuxi.adspter.OPMMainAdspter;
import com.fuxi.main.R;
import com.fuxi.util.AppManager;
import com.fuxi.util.Logger;
import com.fuxi.util.LoginParameterUtil;
import com.fuxi.util.NetUtil;
import com.fuxi.util.OPMLoginParameterUtil;
import com.fuxi.vo.RequestVo;
import com.fuxi.widget.FontTextView;

public class OPMMainActivity extends BaseWapperActivity implements OnItemClickListener, OnItemLongClickListener {

    private static final String TAG = "OPMMainActivity";
    private static final String goodsQueryPathMethod = "/OPMManager.do?goodsQueryPath";
    private static final String getOPMDataMethod = "/OPMManager.do?getOPMDataMethod";
    private static final String saveOPMDataMethod = "/salesOrder.do?saveSalesOrder";
    private static final String deleteMethod = "/salesOrder.do?deleteItem";

    private EditText etGoods;
    private TextView tvTotalCount;
    private TextView tvTotalAmount;
    private TextView tvReport;
    private TextView tvInfo;
    private TextView tvReset;
    private TextView tvLogout;
    private FontTextView ftvToggle;
    private ListView lvDatas;

    private String goodsId;
    private String goodsCode;
    private String goodsName;
    private String typeName = "订货";
    private String salesOrderId; // 销售订单单号
    private BigDecimal unitPrice; // 货品单价
    private long exitTime = 0; // 记录按回退键的时间间隔
    private boolean inputFlag; // 输入框是否可用的标志

    private List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
    private List<Map<String, Object>> delList = new ArrayList<Map<String, Object>>();
    private List<Map<String, Object>> tempList = new ArrayList<Map<String, Object>>();
    private TouchListener tl = new TouchListener();
    private OPMMainAdspter opmMainAdspter;
    private AlertDialog alertDialog;

    @Override
    public void onClick(View v) {
        // 隐藏软键盘
        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
        }
        switch (v.getId()) {
            case R.id.toggle:
                if (inputFlag) {
                    etGoods.setText(null);
                    etGoods.setFocusable(false);
                    etGoods.setOnTouchListener(tl);
                    Drawable drawable = getResources().getDrawable(R.drawable.input_bg);
                    drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight()); // 此为必须写的
                    etGoods.setCompoundDrawables(null, null, drawable, null);
                    SpannableString s = new SpannableString("点击选择货品");
                    etGoods.setHint(s);
                } else {
                    etGoods.setText(null);
                    etGoods.setFocusable(true);
                    etGoods.setFocusableInTouchMode(true);
                    etGoods.setClickable(true);
                    etGoods.setOnTouchListener(null);
                    etGoods.setCompoundDrawables(null, null, null, null);
                    SpannableString s = new SpannableString("输入条码/货号");
                    etGoods.setHint(s);
                    etGoods.requestFocus();
                }
                inputFlag = !inputFlag;
                break;
            case R.id.report:
                Intent intent = new Intent(OPMMainActivity.this, OPMReportingActivity.class);
                startActivity(intent);
                break;
            case R.id.reset:
                Builder dialog = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
                dialog.setTitle("系统提示");
                dialog.setMessage("确定要清空数据吗？");
                // 相当于点击确认按钮
                dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        delList.addAll(dataList);
                        delete();
                        dataList.clear();
                        etGoods.setText(null);
                        tvTotalCount.setText(null);
                        tvTotalAmount.setText(null);
                        opmMainAdspter.refresh(dataList);
                    }
                });
                // 相当于点击取消按钮
                dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {}
                });
                dialog.create();
                dialog.show();
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    protected void loadViewLayout() {
        setContentView(R.layout.activity_opm_main);
        setTitle("订 货 会 系 统 中 心");
        setHeadLeftVisibility(View.INVISIBLE);
        setHeadRightVisibility(View.INVISIBLE);
    }

    @Override
    protected void setListener() {
        ftvToggle.setOnClickListener(this);
        tvReport.setOnClickListener(this);
        tvInfo.setOnClickListener(this);
        tvLogout.setOnClickListener(this);
        tvReset.setOnClickListener(this);
        opmMainAdspter = new OPMMainAdspter(this, dataList);
        lvDatas.setAdapter(opmMainAdspter);
        lvDatas.setOnItemClickListener(this);
        lvDatas.setOnItemLongClickListener(this);
        etGoods.setOnEditorActionListener(new BarcodeActionListener());
    }

    @Override
    protected void processLogic() {
        // 显示按钮
        setHeadRightVisibility(View.VISIBLE);
        setHeadRightText(R.string.exit);
        // 扫描框获取焦点
        etGoods.requestFocus();
        inputFlag = true;
        // 获取用户的订货记录
        getOPMData();
        // 屏蔽按钮
        tvInfo.setVisibility(View.GONE);
        tvLogout.setVisibility(View.GONE);
    }

    @Override
    protected void findViewById() {
        etGoods = (EditText) findViewById(R.id.goods);
        ftvToggle = (FontTextView) findViewById(R.id.toggle);
        tvTotalCount = (TextView) findViewById(R.id.totalCount);
        tvTotalAmount = (TextView) findViewById(R.id.totalAmount);
        tvReport = (TextView) findViewById(R.id.report);
        tvInfo = (TextView) findViewById(R.id.info);
        tvReset = (TextView) findViewById(R.id.reset);
        tvLogout = (TextView) findViewById(R.id.logout);
        lvDatas = (ListView) findViewById(R.id.datas);
    }

    private class TouchListener implements OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (v.getId()) {
                case R.id.goods:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Intent intent = new Intent(OPMMainActivity.this, SelectActivity.class);
                        intent.putExtra("selectType", "selectProduct");
                        startActivityForResult(intent, R.id.goods);
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
            case R.id.goods:
                if (resultCode == 1) {
                    etGoods.setText(null);
                    goodsId = data.getStringExtra("GoodsID");
                    goodsCode = data.getStringExtra("Code");
                    goodsName = data.getStringExtra("GoodsName");
                    unitPrice = new BigDecimal(data.getStringExtra(OPMLoginParameterUtil.customerOrderField));
                    // 货品资料信息
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("GoodsCode", goodsCode);
                    map.put("GoodsName", goodsName);
                    map.put("GoodsType", data.getStringExtra("GoodsType"));
                    map.put("SubType", data.getStringExtra("SubType"));
                    map.put("Age", data.getStringExtra("Age"));
                    map.put("Season", data.getStringExtra("Season"));
                    map.put("BrandSerial", data.getStringExtra("BrandSerial"));
                    map.put("Style", data.getStringExtra("Style"));
                    map.put("Sex", data.getStringExtra("Sex"));
                    map.put("Kind", data.getStringExtra("Kind"));
                    map.put("Model", data.getStringExtra("Model"));
                    map.put("RetailSales", data.getStringExtra("RetailSales"));
                    saveProduct("GoodsInfoCache", goodsId, map);
                    // 根据货品ID选择颜色和尺码
                    if (goodsId == null || "".equals(goodsId) || "null".equalsIgnoreCase(goodsId)) {
                        Toast.makeText(getApplicationContext(), "请输入货品编码或选择一个货品", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // 选择多颜色和尺码
                    Intent intent = new Intent(OPMMainActivity.this, OPMMultiSelectActivity.class);
                    // 判断扫描的货品是否存在, 若存在则显示录入初始化信息
                    tempList.clear();
                    for (int i = 0; i < dataList.size(); i++) {
                        Map<String, Object> tmap = dataList.get(i);
                        String ttgoodsId = String.valueOf(tmap.get("GoodsID"));
                        if (goodsId.equals(ttgoodsId)) {
                            tempList.add(tmap);
                        }
                    }
                    if(tempList.size() > 0){
                        intent.putExtra("datas", (Serializable) tempList);
                    }
                    intent.putExtra("goodsId", goodsId);
                    intent.putExtra("goodsName", goodsName);
                    intent.putExtra("goodsCode", goodsCode);
                    intent.putExtra("unitPrice", String.valueOf(unitPrice));
                    startActivityForResult(intent, 10);
                    overridePendingTransition(R.anim.activity_open, 0);
                }
                break;
            case 10:
                if (resultCode == 1) {
                    Bundle bundle = data.getExtras();
                    List<Map<String, Object>> tdatas = (List<Map<String, Object>>) bundle.get("datas");
                    String tgoodsId = bundle.getString("goodsId");
                    for (int i = 0; i < dataList.size(); i++) {
                        Map<String, Object> map = dataList.get(i);
                        String ttgoodsId = String.valueOf(map.get("GoodsID"));
                        if (tgoodsId.equals(ttgoodsId)) {
                            dataList.remove(i);
                            i--;
                        }
                    }
                    dataList.addAll(tdatas);
                    // 重新计算数量和价格
                    countTotal();
                    // 异步保存到数据库
                    saveOPMData();
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onHeadRightButton(View v) {
        Builder dialog = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
        dialog.setTitle("系统提示");
        dialog.setMessage("确定要注销重新登录吗？");
        // 相当于点击确认按钮
        dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 重新登录
                Intent intent = new Intent(OPMMainActivity.this, OPMLoginActivity.class);
                startActivity(intent);
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 连续按两次回退键退出程序
     */
    private void exit() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(getApplicationContext(), "再按一次注销", Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            Intent intent = new Intent(OPMMainActivity.this, OPMLoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    /**
     * Title: BarcodeActionListener Description: 条码扫描完成触发Enter事件
     */
    class BarcodeActionListener implements OnEditorActionListener {

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if ((actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_UNSPECIFIED) && ((event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER))) {
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm.isActive()) {
                    imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
                }
                queryGoodsCode();
            }
            return false;
        }

    }

    /**
     * 通过条码查询货品信息
     */
    private void queryGoodsCode() {
        final String productId = etGoods.getText().toString();
        if (null == productId || "".equals(productId) || "null".equalsIgnoreCase(productId)) {
            etGoods.setText(null);
            Toast.makeText(OPMMainActivity.this, "条码或货号不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        RequestVo vo = new RequestVo();
        vo.requestUrl = goodsQueryPathMethod;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("barcode", productId);
        map.put("orderField", OPMLoginParameterUtil.customerOrderField);
        vo.requestDataMap = map;
        super.getDataFromServer(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    if (retObj.getBoolean("success")) {
                        JSONObject jsonObject = retObj.getJSONArray("obj").getJSONObject(0);
                        goodsId = jsonObject.getString("GoodsID");
                        goodsName = jsonObject.getString("GoodsName");
                        goodsCode = jsonObject.getString("GoodsCode");
                        unitPrice = new BigDecimal(jsonObject.getString("UnitPrice"));
                        if (goodsId == null || "".equals(goodsId) || "null".equalsIgnoreCase(goodsId)) {
                            Toast.makeText(getApplicationContext(), "请输入货品编码或选择一个货品", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        // 缓存货品资料信息
                        // 货品资料信息
                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put("GoodsCode", goodsCode);
                        map.put("GoodsName", goodsName);
                        map.put("GoodsType", jsonObject.getString("GoodsType"));
                        map.put("SubType", jsonObject.getString("SubType"));
                        map.put("Age", jsonObject.getString("Age"));
                        map.put("Season", jsonObject.getString("Season"));
                        map.put("BrandSerial", jsonObject.getString("BrandSerial"));
                        map.put("Style", jsonObject.getString("Style"));
                        map.put("Sex", jsonObject.getString("Sex"));
                        map.put("Kind", jsonObject.getString("Kind"));
                        map.put("Model", jsonObject.getString("Model"));
                        map.put("RetailSales", jsonObject.getString("RetailSales"));
                        saveProduct("GoodsInfoCache", goodsId, map);
                        // 选择多颜色和尺码
                        Intent intent = new Intent(OPMMainActivity.this, OPMMultiSelectActivity.class);
                        // 判断扫描的货品是否存在, 若存在则显示录入初始化信息
                        tempList.clear();
                        for (int i = 0; i < dataList.size(); i++) {
                            Map<String, Object> tmap = dataList.get(i);
                            String ttgoodsId = String.valueOf(tmap.get("GoodsID"));
                            if (goodsId.equals(ttgoodsId)) {
                                tempList.add(tmap);
                            }
                        }
                        if(tempList.size() > 0){
                            intent.putExtra("datas", (Serializable) tempList);
                        }
                        intent.putExtra("goodsId", goodsId);
                        intent.putExtra("goodsName", goodsName);
                        intent.putExtra("goodsCode", goodsCode);
                        intent.putExtra("unitPrice", String.valueOf(unitPrice));
                        startActivityForResult(intent, 10);
                        overridePendingTransition(R.anim.activity_open, 0);
                    } else {
                        Toast.makeText(OPMMainActivity.this, retObj.getString("msg"), Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(OPMMainActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 计算商品的数量和价格
     */
    private void countTotal() {
        int totalCount = 0;
        double totalAmount = 0;
        for (int i = 0; i < dataList.size(); i++) {
            Map<String, Object> map = dataList.get(i);
            int quantity = Integer.parseInt(String.valueOf(map.get("Quantity")));
            double unitPrice = Double.parseDouble(String.valueOf(map.get("UnitPrice")));
            totalCount += quantity;
            totalAmount += quantity * unitPrice;
        }
        tvTotalCount.setText(String.valueOf(totalCount));
        tvTotalAmount.setText(String.valueOf(new BigDecimal(totalAmount).setScale(2, BigDecimal.ROUND_HALF_UP)));
        opmMainAdspter.refresh(dataList);
        etGoods.setText(null);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        final int location = position;
        final Map<String, Object> map = dataList.get(location);
        final String tgoodsId = (String) map.get("GoodsID");
        final String tgoodsCode = (String) map.get("GoodsCode");
        final String tcolorCode = (String) map.get("ColorCode");
        final String tsizeCode = (String) map.get("SizeCode");
        final String tquantity = String.valueOf(map.get("Quantity"));
        // 询问操作方式
        final String[] items = {"查询货品", "删除货品"};
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
        alertBuilder.setTitle("选择货品操作方式");
        alertBuilder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int index) {
                if (index == 0) {
                    Intent inten = new Intent(OPMMainActivity.this, OPMGoodsInfoActivity.class);
                    inten.putExtra("goodsId", tgoodsId);
                    startActivity(inten);
                } else if (index == 1) {
                    Builder dialog = new AlertDialog.Builder(OPMMainActivity.this, AlertDialog.THEME_HOLO_LIGHT);
                    dialog.setTitle("提示");
                    dialog.setMessage("确定要删除的货品信息？");
                    EditText et = new EditText(OPMMainActivity.this);
                    et.setText("货品编码：" + tgoodsCode + "\n颜色编码：" + tcolorCode + "\n尺码编码：" + tsizeCode + "\n数量：" + tquantity);
                    et.setBackgroundColor(Color.TRANSPARENT);
                    et.setEnabled(false);
                    dialog.setView(et);
                    // 相当于点击确认按钮
                    dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            delList.add(map);
                            dataList.remove(map);
                            delete();
                        }
                    });
                    dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {}
                    });
                    dialog.setCancelable(false);
                    dialog.create();
                    dialog.show();
                }
                // 关闭提示框
                alertDialog.dismiss();
            }
        });
        alertDialog = alertBuilder.create();
        alertDialog.show();
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        tempList.clear();
        ListView listView = (ListView) parent;
        HashMap<String, Object> map = (HashMap<String, Object>) listView.getItemAtPosition(position);
        String tgoodsId = String.valueOf(map.get("GoodsID"));
        String tgoodsName = String.valueOf(map.get("GoodsName"));
        String tgoodsCode = String.valueOf(map.get("GoodsCode"));
        String tunitPrice = String.valueOf(map.get("UnitPrice"));
        for (int i = 0; i < dataList.size(); i++) {
            Map<String, Object> tmap = dataList.get(i);
            String ttgoodsId = String.valueOf(tmap.get("GoodsID"));
            if (tgoodsId.equals(ttgoodsId)) {
                tempList.add(tmap);
            }
        }
        // 传输
        Intent intent = new Intent(OPMMainActivity.this, OPMMultiSelectActivity.class);
        intent.putExtra("datas", (Serializable) tempList);
        intent.putExtra("goodsId", tgoodsId);
        intent.putExtra("goodsName", tgoodsName);
        intent.putExtra("goodsCode", tgoodsCode);
        intent.putExtra("unitPrice", tunitPrice);
        startActivityForResult(intent, 10);
        overridePendingTransition(R.anim.activity_open, 0);
    }

    /**
     * 获取客户的订货数据
     */
    private void getOPMData() {
        RequestVo vo = new RequestVo();
        vo.requestUrl = getOPMDataMethod;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("customerId", OPMLoginParameterUtil.customerId);
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
                        salesOrderId = obj.getString("salesOrderId");
                        JSONArray jsonArray = obj.getJSONArray("datas");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            Map<String, Object> temp = new HashMap<String, Object>();
                            temp.put("ColorID", jsonObject.get("ColorID"));
                            temp.put("ColorCode", jsonObject.get("ColorCode"));
                            temp.put("GoodsID", jsonObject.get("GoodsID"));
                            temp.put("GoodsCode", jsonObject.get("GoodsCode"));
                            temp.put("GoodsName", jsonObject.get("GoodsName"));
                            temp.put("UnitPrice", jsonObject.get("UnitPrice"));
                            temp.put("SizeID", jsonObject.get("SizeID"));
                            temp.put("SizeCode", jsonObject.get("SizeCode"));
                            temp.put("Quantity", jsonObject.get("Quantity"));
                            temp.put("SizeGroupID", null);
                            temp.put("DiscountPrice", null);
                            temp.put("RetailSales", unitPrice);
                            temp.put("meno", null);
                            temp.put("IndexNo", null);
                            temp.put("QuantitySum", null);
                            temp.put("DiscountRate", null);
                            temp.put("SizeStr", null);
                            temp.put("OneBoxQty", null);
                            temp.put("BoxQty", null);
                            dataList.add(temp);
                        }
                        // 重新计算数量和价格
                        countTotal();
                    }
                } catch (Exception e) {
                    Toast.makeText(OPMMainActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 获取客户的订货数据
     */
    private void saveOPMData() {
        if (dataList.size() == 0) {
            return;
        }
        RequestVo vo = new RequestVo();
        vo.requestUrl = saveOPMDataMethod;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("SalesOrderID", salesOrderId);
        map.put("customerid", OPMLoginParameterUtil.customerId);
        map.put("departmentid", null);
        map.put("brandId", null);
        map.put("type", typeName);
        map.put("typeEName", null);
        map.put("employeeId", null);
        map.put("discountRateSum", null);
        map.put("lastARAmount", null);
        map.put("preReceivalAmount", null);
        map.put("privilegeAmount", null);
        map.put("businessDeptId", OPMLoginParameterUtil.departmentId);
        map.put("paymentTypeId", null);
        map.put("memo", null);
        net.sf.json.JSONArray json = net.sf.json.JSONArray.fromObject(dataList);
        map.put("data", json.toString());
        vo.requestDataMap = map;
        super.getDataFromServerNoProcess(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    if (retObj.getBoolean("success")) {
                        JSONObject rs = retObj.getJSONObject("attributes");
                        salesOrderId = rs.getString("SalesOrderID");
                        Toast.makeText(OPMMainActivity.this, "保存成功", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(OPMMainActivity.this, "保存失败", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(OPMMainActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 保存对象到缓存
     * 
     * @param docName
     * @param objName
     * @param saveObj
     */
    public void saveProduct(String docName, String objName, Object saveObj) {
        SharedPreferences preferences = getSharedPreferences(docName, MODE_PRIVATE);
        // 创建字节输出流
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            // 创建对象输出流，并封装字节流
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            // 将对象写入字节流
            oos.writeObject(saveObj);
            // 将字节流编码成base64的字符窜
            String customerBase64 = new String(Base64.encodeBase64(baos.toByteArray()));
            Editor editor = preferences.edit();
            editor.putString(objName, customerBase64);
            editor.commit();
            Log.i("ok", "存储成功");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除销售订单明细
     */
    private void delete() {
        RequestVo vo = new RequestVo();
        vo.requestUrl = deleteMethod;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("SalesOrderID", salesOrderId);
        net.sf.json.JSONArray json = net.sf.json.JSONArray.fromObject(delList);
        map.put("data", json.toString());
        vo.requestDataMap = map;
        super.getDataFromServerNoProcess(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject paramObject, boolean paramBoolean) {
                try {
                    if (paramObject.getBoolean("success")) {
                        Logger.e(TAG, "删除货品成功");
                        Toast.makeText(OPMMainActivity.this, "删除货品成功", Toast.LENGTH_LONG).show();
                        delList.clear();
                        // 重新计算数量和价格
                        countTotal();
                    }
                } catch (JSONException e) {
                    Toast.makeText(OPMMainActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

}
