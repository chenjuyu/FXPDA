package com.fuxi.activity;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import com.fuxi.adspter.InvoicingAdspter;
import com.fuxi.main.R;
import com.fuxi.util.AppManager;
import com.fuxi.util.Logger;
import com.fuxi.util.LoginParameterUtil;
import com.fuxi.util.NetUtil;
import com.fuxi.util.Tools;
import com.fuxi.vo.RequestVo;

/**
 * Title: InvoicingActivity Description: 进销存查询活动界面
 * 
 * @author LYJ
 * 
 */
public class InvoicingActivity extends BaseWapperActivity {

    private static final String TAG = "InvoicingActivity";
    private static String queryPath = "/invoicing.do?queryInvoicing";

    private List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();

    private LinearLayout ll_purchase_price;// 参考进价
    private LinearLayout ll_arrival_days;// 到货天数
    private LinearLayout ll_brand;// 品牌
    private LinearLayout ll_supplier_code;// 厂商货品编码
    private LinearLayout ll_supplier_phone;// 厂商电话
    private LinearLayout ll_retail_sales;// 零售价
    private LinearLayout ll_purchased_date;// 首采期
    private LinearLayout ll_last_purchased_date;// 末采期
    private EditText et_product; // 货品
    private EditText et_retailSales; // 零售价
    private EditText et_salesPrice; // 现售价
    private EditText et_goodsName;
    private EditText et_goodsCode;
    private EditText et_supplierCode;
    private EditText et_supplierPhone;
    private EditText et_purchasePrice;
    private EditText et_arrivalDays;
    private EditText et_brand;
    private EditText et_purchasedDate;
    private EditText et_lastPurchasedDate;
    private TextView tv_purchaseTotal;
    private TextView tv_salesTotal;
    private TextView tv_stockTotal;
    private ListView lv_detail; // 添加的货品集合

    private InvoicingAdspter adspter;
    private String productId;
    private String goodsId;
    private String colorId;
    private String sizeId;
    private String goodsCode;
    private String goodsName;
    private String productCode;
    private String tgoodsId; // 临时存储的货品ID
    private boolean preciseQueryStock;


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.product:
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm.isActive()) {
                    imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
                }
                break;
            default:
                break;
        }
    }

    /**
     * 库存查询方法
     */
    private void createQuery() {
        productId = et_product.getText().toString();
        productCode = et_product.getText().toString();
        if (null == productId || "".equals(productId)) {
            et_product.setText(null);
            Toast.makeText(InvoicingActivity.this, "条码或货号不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        // 处理扫描货品ID时的情况
        if (tgoodsId != null && tgoodsId.equals(goodsId)) {
            goodsId = null;
        }
        RequestVo vo = new RequestVo();
        vo.requestUrl = queryPath;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("productId", productId);
        map.put("goodsId", goodsId);
        map.put("preciseQueryStock", String.valueOf(preciseQueryStock));
        vo.requestDataMap = map;
        initialData();
        super.getDataFromServer(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    if (retObj.getBoolean("success")) {
                        JSONObject rs = retObj.getJSONObject("attributes");
                        colorId = rs.getString("colorId");
                        sizeId = rs.getString("sizeId");
                        JSONArray goodsDetailed = rs.getJSONArray("goodsDetailed");
                        JSONArray invoicingDetail = rs.getJSONArray("invoicingDetail");
                        // 货品信息
                        JSONObject goodsJson = goodsDetailed.getJSONObject(0);
                        goodsId = goodsJson.getString("GoodsID");
                        tgoodsId = goodsJson.getString("GoodsID");
                        goodsCode = goodsJson.getString("GoodsCode");
                        goodsName = goodsJson.getString("GoodsName");
                        et_goodsCode.setText(goodsCode);
                        et_goodsName.setText(goodsName);
                        et_supplierCode.setText(goodsJson.getString("SupplierCode"));
                        et_supplierPhone.setText(goodsJson.getString("SupplierTel"));
                        et_brand.setText(goodsJson.getString("Brand"));
                        et_purchasePrice.setText(Tools.formatDecimal(goodsJson.getString("PurchasePrice")));
                        et_arrivalDays.setText(goodsJson.getString("ArrivalDays"));
                        if (goodsJson.isNull("PurchasedDate")) {
                            et_purchasedDate.setText(null);
                        } else {
                            et_purchasedDate.setText(Tools.dateToString(new Date(goodsJson.getLong("PurchasedDate"))));
                        }
                        if (goodsJson.isNull("LastPurchasedDate")) {
                            et_lastPurchasedDate.setText(null);
                        } else {
                            et_lastPurchasedDate.setText(Tools.dateToString(new Date(goodsJson.getLong("LastPurchasedDate"))));
                        }
                        et_salesPrice.setText(Tools.formatDecimal(goodsJson.getString("SalesPrice")));
                        et_retailSales.setText(Tools.formatDecimal(goodsJson.getString("RetailSales")));
                        if (null == goodsCode || "null".equalsIgnoreCase(goodsCode) || "".equals(goodsCode) || goodsCode.isEmpty()) {
                            goodsCode = productCode;
                        }
                        // 进销存记录
                        if (invoicingDetail != null && invoicingDetail.length() > 0) {
                            for (int i = 0; i < invoicingDetail.length(); i++) {
                                Map<String, Object> temp = new HashMap<String, Object>();
                                JSONObject json = invoicingDetail.getJSONObject(i);
                                Iterator ite = json.keys();
                                while (ite.hasNext()) {
                                    String key = ite.next().toString();
                                    String value = json.getString(key);
                                    temp.put(key, value);
                                }
                                dataList.add(temp);
                            }
                        } else {
                            Toast.makeText(InvoicingActivity.this, "暂无货品 " + goodsCode + " 的进销存记录", Toast.LENGTH_LONG).show();
                            Logger.e(TAG, "暂无货品进销存记录");
                        }
                        // 刷新ListView
                        adspter.refresh(dataList);
                        // 计算价格
                        countTotal();
                    } else {
                        if ("条码或货号错误".equals(retObj.getString("msg"))) {
                            et_product.selectAll();
                            Toast.makeText(InvoicingActivity.this, retObj.getString("msg"), Toast.LENGTH_SHORT).show();
                            Logger.e(TAG, retObj.getString("msg"));
                        } else {
                            Toast.makeText(InvoicingActivity.this, "暂无货品进销存记录", Toast.LENGTH_SHORT).show();
                            Logger.e(TAG, "暂无货品进销存记录");
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(InvoicingActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 初始化设置(界面信息重置)
     */
    private void initialData() {
        dataList.clear();
        productId = null;
        goodsId = null;
        et_product.setText(null); // 货品
        et_retailSales.setText(null);
        et_salesPrice.setText(null);
        et_goodsName.setText(null);
        et_goodsCode.setText(null);
        et_supplierCode.setText(null);
        et_supplierPhone.setText(null);
        et_purchasePrice.setText(null);
        et_arrivalDays.setText(null);
        et_brand.setText(null);
        et_purchasedDate.setText(null);
        et_lastPurchasedDate.setText(null);
        tv_purchaseTotal.setText("0");
        tv_salesTotal.setText("0");
        tv_stockTotal.setText("0");
    }

    /**
     * 计算并显示总数量
     */
    private void countTotal() {
        int purchaseSum = 0, salesSum = 0, stockSum = 0;
        for (int j = 0; j < dataList.size(); j++) {
            int purchaseNum = Integer.parseInt(String.valueOf(dataList.get(j).get("PurchaseCount")));
            int salesNum = Integer.parseInt(String.valueOf(dataList.get(j).get("SalesCount")));
            int stockNum = Integer.parseInt(String.valueOf(dataList.get(j).get("StockCount")));
            purchaseSum += purchaseNum;
            salesSum += salesNum;
            stockSum += stockNum;
        }
        tv_purchaseTotal.setText(String.valueOf(purchaseSum));
        tv_salesTotal.setText(String.valueOf(salesSum));
        tv_stockTotal.setText(String.valueOf(stockSum));
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    protected void setListener() {
        TouchListener tl = new TouchListener();
        adspter = new InvoicingAdspter(this, dataList);
        lv_detail.setAdapter(adspter);
        et_product.setOnEditorActionListener(new BarcodeActionListener());
        et_product.setOnClickListener(this);
        et_product.setOnTouchListener(tl);
        lv_detail.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                ListView listView = (ListView) arg0;
                HashMap<String, String> map = (HashMap<String, String>) listView.getItemAtPosition(arg2);
                String departmentId = (String) map.get("DepartmentID");
                Intent intent = new Intent(InvoicingActivity.this, InvoicingDistributionActivity.class);
                intent.putExtra("goodsName", (goodsName + "(" + goodsCode + ")"));
                intent.putExtra("goodsId", goodsId);
                intent.putExtra("colorId", colorId);
                intent.putExtra("sizeId", sizeId);
                intent.putExtra("departmentId", departmentId);
                startActivity(intent);
            }

        });
    }

    private class TouchListener implements OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (v.getId()) {
                case R.id.product:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Intent intent = new Intent(InvoicingActivity.this, SelectActivity.class);
                        intent.putExtra("selectType", "selectProduct");
                        startActivityForResult(intent, R.id.product);
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
            case R.id.product:
                if (resultCode == 1) {
                    et_product.setText(data.getStringExtra("Code"));
                    productId = data.getStringExtra("Code");
                    goodsId = data.getStringExtra("GoodsID");
                    et_product.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                    et_product.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
                }
                break;
            default:
                break;
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
                createQuery();
            }
            return false;
        }

    }

    @Override
    protected void loadViewLayout() {
        setContentView(R.layout.activity_invoicing);
        setTitle("进销存查询");
    }

    @Override
    protected void processLogic() {
        if (NetUtil.hasNetwork(getApplicationContext())) {
            if (LoginParameterUtil.online) {
                // 设置用户操作权限
                preciseQueryStock = LoginParameterUtil.preciseToQueryStock;
                boolean goodsBrowseRight = LoginParameterUtil.goodsRightMap.get("BrowseRight");
                boolean browseRight = LoginParameterUtil.stocktakingQueryRightMap.get("BrowseRight");
                if (goodsBrowseRight && browseRight) {
                    // 设置扫码区自动获取焦点
                    et_product.requestFocus();
                    // 参考进价字段权限
                    if (!LoginParameterUtil.purchasePriceRight) {
                        ll_purchase_price.setVisibility(View.GONE);
                    }
                    // 货品品牌字段权限
                    if (!LoginParameterUtil.brandRight) {
                        ll_brand.setVisibility(View.GONE);
                    }
                    // 货品厂商编码字段权限
                    if (!LoginParameterUtil.supplierCodeRight) {
                        ll_supplier_code.setVisibility(View.GONE);
                    }
                    // 货品厂商电话字段权限
                    if (!LoginParameterUtil.supplierPhoneRight) {
                        ll_supplier_phone.setVisibility(View.GONE);
                    }
                    // 货品零售价字段权限
                    if (!LoginParameterUtil.retailSalesRight) {
                        ll_retail_sales.setVisibility(View.GONE);
                    }
                    // 货品首采期字段权限
                    if (!LoginParameterUtil.purchasedDateRight) {
                        ll_purchased_date.setVisibility(View.GONE);
                    }
                    // 货品末采期字段权限
                    if (!LoginParameterUtil.lastPurchasedDateRight) {
                        ll_last_purchased_date.setVisibility(View.GONE);
                    }
                } else {
                    Builder dialog = new AlertDialog.Builder(InvoicingActivity.this, AlertDialog.THEME_HOLO_LIGHT);
                    dialog.setTitle("提示");
                    dialog.setMessage("当前暂无进销存查询的操作权限");
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
                AlertDialog.Builder dialog = new AlertDialog.Builder(InvoicingActivity.this, AlertDialog.THEME_HOLO_LIGHT);
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
            AlertDialog.Builder dialog = new AlertDialog.Builder(InvoicingActivity.this, AlertDialog.THEME_HOLO_LIGHT);
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
        ll_arrival_days = (LinearLayout) findViewById(R.id.ll_arrival_days);
        ll_brand = (LinearLayout) findViewById(R.id.ll_brand);
        ll_supplier_code = (LinearLayout) findViewById(R.id.ll_supplier_code);
        ll_purchase_price = (LinearLayout) findViewById(R.id.ll_purchase_price);
        ll_supplier_phone = (LinearLayout) findViewById(R.id.ll_supplier_phone);
        ll_retail_sales = (LinearLayout) findViewById(R.id.ll_retail_sales);
        ll_purchased_date = (LinearLayout) findViewById(R.id.ll_purchased_date);
        ll_last_purchased_date = (LinearLayout) findViewById(R.id.ll_last_purchased_date);
        lv_detail = (ListView) findViewById(R.id.lv_datas);
        et_product = (EditText) findViewById(R.id.product);
        et_goodsCode = (EditText) findViewById(R.id.goodsCode);
        et_goodsName = (EditText) findViewById(R.id.goodsName);
        et_supplierCode = (EditText) findViewById(R.id.supplierCode);
        et_supplierPhone = (EditText) findViewById(R.id.supplierPhone);
        et_purchasePrice = (EditText) findViewById(R.id.purchasePrice);
        et_arrivalDays = (EditText) findViewById(R.id.arrivalDays);
        et_brand = (EditText) findViewById(R.id.brand);
        et_purchasedDate = (EditText) findViewById(R.id.purchasedDate);
        et_lastPurchasedDate = (EditText) findViewById(R.id.lastPurchasedDate);
        et_retailSales = (EditText) findViewById(R.id.retailSales);
        et_salesPrice = (EditText) findViewById(R.id.salesPrice);
        tv_purchaseTotal = (TextView) findViewById(R.id.purchaseTotal);
        tv_salesTotal = (TextView) findViewById(R.id.salesTotal);
        tv_stockTotal = (TextView) findViewById(R.id.stockTotal);
    }

}
