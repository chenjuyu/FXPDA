package com.fuxi.activity;

import java.io.File;
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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import com.fuxi.adspter.InventoryQueryAdspter;
import com.fuxi.main.R;
import com.fuxi.task.ImageTask;
import com.fuxi.util.AppManager;
import com.fuxi.util.Logger;
import com.fuxi.util.LoginParameterUtil;
import com.fuxi.util.NetUtil;
import com.fuxi.util.Tools;
import com.fuxi.vo.BarCode;
import com.fuxi.vo.RequestVo;

/**
 * Title: InventoryQueryActivity Description: 库存查询活动界面
 * 
 * @author LYJ
 * 
 */
public class InventoryQueryActivity extends BaseWapperActivity {

    private static final String TAG = "InventoryQueryActivity";
    private static String queryPath = "/inventoryQuery.do?queryStock";

    private List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
    private LinearLayout ll_supplier_code;
    private LinearLayout ll_brand;
    private LinearLayout ll_retail_sales;
    private LinearLayout ll_trade_price;
    private LinearLayout ll_purchased_date;
    private LinearLayout ll_last_purchased_date;
    private EditText et_department; // 店铺
    private EditText et_product; // 货品
    private EditText et_retailSales; // 零售价
    private EditText et_tradePrice; // 批发价
    private EditText et_goodsName;
    private EditText et_supplierCode;
    private EditText et_brand;
    private EditText et_purchasedDate;
    private EditText et_lastPurchasedDate;
    private ImageView iv_pic; // 货品
    private TextView tv_totalCount; // 合计
    private ListView lv_detail; // 添加的货品集合

    private InventoryQueryAdspter adspter;
    private String departmentId;
    private String productId;
    private String goodsId;
    private String goodsCode;
    private String productCode;

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
            case R.id.pic:
                InputMethodManager im = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (im.isActive()) {
                    im.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
                }
                if (null != goodsCode && !goodsCode.isEmpty()) {
                    Intent inten = new Intent(InventoryQueryActivity.this, PictureActivity.class);
                    inten.putExtra("goodsCode", goodsCode);
                    startActivity(inten);
                } else {
                    Toast.makeText(InventoryQueryActivity.this, "当前图片无对应的货品编码", Toast.LENGTH_SHORT).show();
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
        // 非空判断
        if (null == departmentId || "".equals(departmentId)) {
            return;
        }
        if (null == productId || "".equals(productId)) {
            et_product.setText(null);
            Toast.makeText(InventoryQueryActivity.this, "条码或货号不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        RequestVo vo = new RequestVo();
        vo.requestUrl = queryPath;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("deptId", departmentId);
        map.put("productId", productId);
        map.put("goodsId", goodsId);
        map.put("preciseQueryStock", String.valueOf(preciseQueryStock));
        vo.requestDataMap = map;
        // 初始化
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
                        BarCode code = new BarCode();
                        JSONArray array = rs.getJSONArray("list");
                        boolean hasStock = rs.getBoolean("hasStock");
                        if (hasStock) {// 有库存 ==> 显示货品价格+库存信息
                            for (int i = 0; i < array.length(); i++) {
                                Map<String, Object> temp = new HashMap<String, Object>();
                                JSONObject json = array.getJSONObject(i);
                                code.setGoodsid(json.getString("GoodsID"));
                                goodsCode = json.getString("GoodsCode");
                                et_goodsName.setText(json.getString("GoodsName"));
                                et_supplierCode.setText(json.getString("SupplierCode"));
                                et_brand.setText(json.getString("Brand"));
                                if (json.isNull("PurchasedDate")) {
                                    et_purchasedDate.setText(null);
                                } else {
                                    et_purchasedDate.setText(Tools.dateToString(new Date(json.getLong("PurchasedDate"))));
                                }
                                if (json.isNull("LastPurchasedDate")) {
                                    et_lastPurchasedDate.setText(null);
                                } else {
                                    et_lastPurchasedDate.setText(Tools.dateToString(new Date(json.getLong("LastPurchasedDate"))));
                                }
                                code.setGoodscode(json.getString("GoodsCode"));
                                et_tradePrice.setText(Tools.formatDecimal(json.getString("TradePrice")));
                                et_retailSales.setText(Tools.formatDecimal(json.getString("RetailSales")));
                                Iterator ite = json.keys();
                                while (ite.hasNext()) {
                                    String key = ite.next().toString();
                                    String value = json.getString(key);
                                    temp.put(key, value);
                                }
                                dataList.add(temp);
                            }
                        } else {// 无库存 ==> 显示货品价格
                            for (int i = 0; i < array.length(); i++) {
                                Map<String, Object> temp = new HashMap<String, Object>();
                                JSONObject json = array.getJSONObject(i);
                                code.setGoodsid(json.getString("GoodsID"));
                                goodsCode = json.getString("GoodsCode");
                                code.setGoodscode(json.getString("GoodsCode"));
                                et_goodsName.setText(json.getString("GoodsName"));
                                et_supplierCode.setText(json.getString("SupplierCode"));
                                et_brand.setText(json.getString("Brand"));
                                if (json.isNull("PurchasedDate")) {
                                    et_purchasedDate.setText(null);
                                } else {
                                    et_purchasedDate.setText(Tools.dateToString(new Date(json.getLong("PurchasedDate"))));
                                }
                                if (json.isNull("LastPurchasedDate")) {
                                    et_lastPurchasedDate.setText(null);
                                } else {
                                    et_lastPurchasedDate.setText(Tools.dateToString(new Date(json.getLong("LastPurchasedDate"))));
                                }
                                et_tradePrice.setText(Tools.formatDecimal(json.getString("TradePrice")));
                                et_retailSales.setText(Tools.formatDecimal(json.getString("RetailSales")));
                                Iterator ite = json.keys();
                                while (ite.hasNext()) {
                                    String key = ite.next().toString();
                                    String value = json.getString(key);
                                    temp.put(key, value);
                                }
                                dataList.add(temp);
                            }
                            if (null == goodsCode || "null".equalsIgnoreCase(goodsCode) || "".equals(goodsCode) || goodsCode.isEmpty()) {
                                goodsCode = productCode;
                            }
                            Toast.makeText(InventoryQueryActivity.this, "此仓库暂无货品 " + goodsCode + " 的库存", Toast.LENGTH_LONG).show();
                            Logger.e(TAG, "暂无库存数据");
                        }
                        // 刷新ListView
                        adspter.refresh(dataList);
                        // 计算价格
                        countTotal();
                        // 加载图片
                        if (hasStock && goodsCode != null && !goodsCode.isEmpty()) {
                            LoadImage(code);
                        }
                    } else {
                        if ("条码或货号错误".equals(retObj.getString("msg"))) {
                            et_product.selectAll();
                            Toast.makeText(InventoryQueryActivity.this, retObj.getString("msg"), Toast.LENGTH_SHORT).show();
                            Logger.e(TAG, retObj.getString("msg"));
                        } else {
                            Toast.makeText(InventoryQueryActivity.this, "暂无库存数据", Toast.LENGTH_SHORT).show();
                            Logger.e(TAG, "暂无库存数据");
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(InventoryQueryActivity.this, "系统错误", Toast.LENGTH_LONG).show();
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
        et_product.setText(null);
        et_tradePrice.setText(null);
        et_retailSales.setText(null);
        iv_pic.setImageResource(R.drawable.unfind);
    }

    /**
     * 计算并显示总数量和价格
     */
    private void countTotal() {
        int sum = 0;
        for (int j = 0; j < dataList.size(); j++) {
            int num = Integer.parseInt(String.valueOf(dataList.get(j).get("Qty")));
            sum += num;
        }
        tv_totalCount.setText(String.valueOf(sum));
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    protected void setListener() {
        TouchListener tl = new TouchListener();
        adspter = new InventoryQueryAdspter(this, dataList);
        lv_detail.setAdapter(adspter);
        et_product.setOnEditorActionListener(new BarcodeActionListener());
        et_product.setOnClickListener(this);
        iv_pic.setOnClickListener(this);
        et_product.setOnTouchListener(tl);
        et_department.setOnTouchListener(tl);
        lv_detail.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                ListView listView = (ListView) arg0;
                HashMap<String, String> map = (HashMap<String, String>) listView.getItemAtPosition(arg2);
                String goodsId = (String) map.get("GoodsID");
                String colorId = (String) map.get("ColorID");
                String sizeId = (String) map.get("SizeID");
                Intent intent = new Intent(InventoryQueryActivity.this, InventoryDistributionActivity.class);
                intent.putExtra("goodsId", goodsId);
                intent.putExtra("colorId", colorId);
                intent.putExtra("sizeId", sizeId);
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
                        Intent intent = new Intent(InventoryQueryActivity.this, SelectActivity.class);
                        intent.putExtra("selectType", "selectProduct");
                        startActivityForResult(intent, R.id.product);
                        overridePendingTransition(R.anim.activity_open, 0);
                    }
                    break;
                case R.id.department:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Intent intent = new Intent(InventoryQueryActivity.this, SelectActivity.class);
                        intent.putExtra("selectType", "selectWarehouse");
                        startActivityForResult(intent, R.id.department);
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
            case R.id.department:
                if (resultCode == 1) {
                    et_department.setText(data.getStringExtra("Name"));
                    departmentId = data.getStringExtra("DepartmentID");
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

    /**
     * 根据货号从服务端加载图片
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
        }
        // 显示图片
        setImage(imageFile);
    }

    /**
     * 设置显示图片
     * 
     * @param imageFile
     */
    private void setImage(File imageFile) {
        setImage(imageFile.getAbsolutePath());
    }

    /**
     * 设置显示图片
     * 
     * @param imagePath
     */
    private void setImage(String imagePath) {
        Bitmap bm = BitmapFactory.decodeFile(imagePath);
        if (null == bm) {
            bm = BitmapFactory.decodeFile(imagePath);
            // 显示默认图片
            if (null == bm) {
                iv_pic.setImageResource(R.drawable.unfind);
                return;
            }
        }
        iv_pic.setImageBitmap(bm);
    }

    @Override
    protected void loadViewLayout() {
        setContentView(R.layout.activity_inventory_query);
        setTitle("货品库存查询");
    }

    @Override
    protected void processLogic() {
        if (NetUtil.hasNetwork(getApplicationContext())) {
            if (LoginParameterUtil.online) {
                // 库存查询方式
                preciseQueryStock = LoginParameterUtil.preciseToQueryStock;
                // 获取用户操作权限
                boolean browseRight = LoginParameterUtil.stocktakingQueryRightMap.get("BrowseRight");
                if (browseRight) {
                    if (LoginParameterUtil.isWarehouse) {
                        departmentId = LoginParameterUtil.deptId;
                        et_department.setText(LoginParameterUtil.deptName);
                    }
                    // 设置扫码区自动获取焦点
                    et_product.requestFocus();
                    // 货品厂商编码字段权限
                    if (!LoginParameterUtil.supplierCodeRight) {
                        ll_supplier_code.setVisibility(View.GONE);
                    }
                    // 货品品牌字段权限
                    if (!LoginParameterUtil.brandRight) {
                        ll_brand.setVisibility(View.GONE);
                    }
                    // 货品零售价字段权限
                    if (!LoginParameterUtil.retailSalesRight) {
                        ll_retail_sales.setVisibility(View.GONE);
                    }
                    // 货品批发价字段权限
                    if (!LoginParameterUtil.tradePriceRight) {
                        ll_trade_price.setVisibility(View.GONE);
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
                    Builder dialog = new AlertDialog.Builder(InventoryQueryActivity.this, AlertDialog.THEME_HOLO_LIGHT);
                    dialog.setTitle("提示");
                    dialog.setMessage("当前暂无库存查询的操作权限");
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
                AlertDialog.Builder dialog = new AlertDialog.Builder(InventoryQueryActivity.this, AlertDialog.THEME_HOLO_LIGHT);
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
            AlertDialog.Builder dialog = new AlertDialog.Builder(InventoryQueryActivity.this, AlertDialog.THEME_HOLO_LIGHT);
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
        ll_supplier_code = (LinearLayout) findViewById(R.id.ll_supplier_code);
        ll_brand = (LinearLayout) findViewById(R.id.ll_brand);
        ll_retail_sales = (LinearLayout) findViewById(R.id.ll_retail_sales);
        ll_trade_price = (LinearLayout) findViewById(R.id.ll_trade_price);
        ll_purchased_date = (LinearLayout) findViewById(R.id.ll_purchased_date);
        ll_last_purchased_date = (LinearLayout) findViewById(R.id.ll_last_purchased_date);
        lv_detail = (ListView) findViewById(R.id.lv_datas);
        et_department = (EditText) findViewById(R.id.department);
        et_product = (EditText) findViewById(R.id.product);
        et_goodsName = (EditText) findViewById(R.id.goodsName);
        et_supplierCode = (EditText) findViewById(R.id.supplierCode);
        et_brand = (EditText) findViewById(R.id.brand);
        et_purchasedDate = (EditText) findViewById(R.id.purchasedDate);
        et_lastPurchasedDate = (EditText) findViewById(R.id.lastPurchasedDate);
        et_retailSales = (EditText) findViewById(R.id.retailSales);
        et_tradePrice = (EditText) findViewById(R.id.tradePrice);
        iv_pic = (ImageView) findViewById(R.id.pic);
        tv_totalCount = (TextView) findViewById(R.id.totalCount);
    }

}
