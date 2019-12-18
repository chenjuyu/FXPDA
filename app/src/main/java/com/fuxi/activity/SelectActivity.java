package com.fuxi.activity;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONObject;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.fuxi.adspter.SelectAdapter;
import com.fuxi.adspter.SelectEditAdapter;
import com.fuxi.main.R;
import com.fuxi.util.Logger;
import com.fuxi.util.NetUtil;
import com.fuxi.vo.RequestVo;
import com.fuxi.widget.OnRefreshListener;
import com.fuxi.widget.RefreshListView;

/**
 * Title: SelectActivity Description: 选择活动界面
 * 
 * @author LYJ
 * 
 */
public class SelectActivity extends BaseWapperActivity implements OnRefreshListener {

    private static final String TAG = "SelectActivity";

    private EditText et_param;
    private LinearLayout ll_select;
    private LinearLayout ll_parent;

    // 实例化对象
    private Handler handler = new Handler();
    private RefreshListView lvDatas; // ListView
    private SelectAdapter adapter;
    private SelectEditAdapter editAdapter;
    private List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();

    private int currPage = 1;
    private int type;
    private int count;
    private String selectType = null;
    private String param = null;
    private boolean orderGoodsQuery = false;

    /**
     * 根据不同的参数设置选择界面的显示标题
     */
    private void setTitle() {
        if ("selectCustomer".equals(selectType)) {
            setTitle("选择客户");
        } else if ("selectDepartment".equals(selectType)) {
            setTitle("选择部门");
        } else if ("selectWarehouse".equals(selectType)) {
            setTitle("选择部门(仓库)");
        } else if ("selectWarehouseIn".equals(selectType)) {
            setTitle("选择部门(转进仓库)");
        } else if ("selectEmployee".equals(selectType)) {
            setTitle("选择经手人");
        } else if ("type".equals(selectType)) {
            setTitle("选择类型");
        } else if ("purchaseType".equals(selectType)) {
            setTitle("选择类型");
        } else if ("selectShop".equals(selectType)) {
            setTitle("选择店铺");
        } else if ("selectProduct".equals(selectType)) {
            setTitle("选择货号");
        } else if ("selectCategory".equals(selectType)) {
            setTitle("选择类别");
        } else if ("selectYear".equals(selectType)) {
            setTitle("选择年份");
        } else if ("selectBrand".equals(selectType)) {
            setTitle("选择品牌");
        } else if ("selectBrandByPower".equals(selectType)) {
            setTitle("选择品牌(品牌权限)");
        } else if ("selectSeason".equals(selectType)) {
            setTitle("选择季节");
        } else if ("selectVip".equals(selectType)) {
            setTitle("选择会员");
        } else if ("salesPriceType".equals(selectType)) {
            setTitle("选择批发价类型");
        } else if ("customerType".equals(selectType)) {
            setTitle("选择客户类别");
        } else if ("selectStorage".equals(selectType)) {
            setTitle("选择仓位");
        } else if ("selectStorageByInner".equals(selectType)) {
            setTitle("选择仓位");
        } else if ("selectBarcode".equals(selectType)) {
            setTitle("选择货品条码");
        } else if ("selectPresentGoods".equals(selectType)) {
            setTitle("选择赠品");
        } else if ("selectWarehouseHasStorage".equals(selectType)) {
            setTitle("选择部门(含仓位)");
        } else if ("selectWarehouseHasStorageHasInit".equals(selectType)) {
            setTitle("选择部门(含仓位)");
        } else if ("selectGoodsInfo".equals(selectType)) {
            setTitle("查找货号");
        } else if ("selectGoodsSubType".equals(selectType)) {
            setTitle("选择货品子类别");
        } else if ("selectGoodsBrandSerial".equals(selectType)) {
            setTitle("选择货品品牌系列");
        } else if ("selectGoodsSupplier".equals(selectType)) {
            setTitle("选择货品厂商");
        } else if ("selectGoodsColor".equals(selectType)) {
            setTitle("选择货品颜色");
        } else if ("selectPaymentType".equals(selectType)) {
            setTitle("选择结算方式");
        } else if ("selectColorByGoodsCode".equals(selectType)) {
            setTitle("选择货品颜色(关联货号)");
        } else if ("selectSizeByGoodsCode".equals(selectType)) {
            setTitle("选择货品尺码(关联货号)");
        } else if ("selectDocTypeIn".equals(selectType)) {
            setTitle("选择上架单据类别");
        } else if ("selectDocTypeOut".equals(selectType)) {
            setTitle("选择下架单据类别");
        } else if ("selectStockTypeIn".equals(selectType)) {
            setTitle("选择进仓单据类别");
        } else if ("selectStockTypeOut".equals(selectType)) {
            setTitle("选择出仓单据类别");
        } else if ("selectSalesOrderNo".equals(selectType)) {
            setTitle("选择销售订单单号");
        } else if ("selectSalesNo".equals(selectType)) {
            setTitle("选择销售发货单单号");
        } else if ("selectSalesReturnNo".equals(selectType)) {
            setTitle("选择销售退货单单号");
        } else if ("selectPurchaseNo".equals(selectType)) {
            setTitle("选择采购收货单单号");
        } else if ("selectPurchaseReturnNo".equals(selectType)) {
            setTitle("选择采购退货单单号");
        } else if ("selectStocktakingNo".equals(selectType)) {
            setTitle("选择盘点单单号");
        } else if ("selectStockMoveNo".equals(selectType)) {
            setTitle("选择转仓单单号");
        } else if ("selectPackingBoxSalesNo".equals(selectType)) {
            setTitle("选择发货单单号(装箱扫描)");
        } else if ("selectPackingBoxStockOutNo".equals(selectType)) {
            setTitle("选择出仓单单号(装箱扫描)");
        } else if ("selectPackingBoxSalesNoOfComplete".equals(selectType)) {
            setTitle("选择发货单单号(装箱扫描)");
        } else if ("selectPackingBoxStockOutNoOfComplete".equals(selectType)) {
            setTitle("选择出仓单单号(装箱扫描)");
        } else if ("selectBarcodePrintType".equals(selectType)) {
            setTitle("选择条码打印类型");
        } else if ("selectCustomerByUserNo".equals(selectType)) {
            setTitle("选择订货会客户");
        } else if("selectPurchaseOrderNo".equals(selectType)){
            setTitle("选择采购订单号");
        }
        else {
            setTitle("选择");
        }
    }

    /**
     * 根据不同的参数设置不同选择界面的连接URL
     * 
     * @return
     */
    private String getUrl() {
        String url = null;
        if ("selectCustomer".equals(selectType)) {
            url = "/select.do?getCustomer";
        } else if ("selectDepartment".equals(selectType)) {
            url = "/select.do?getDepartment";
        } else if ("selectWarehouse".equals(selectType)) {
            url = "/select.do?getWarehouse";
        } else if ("selectWarehouseIn".equals(selectType)) {
            url = "/select.do?getWarehouseIn";
        } else if ("selectEmployee".equals(selectType)) {
            url = "/select.do?getEmployee";
        } else if ("selectShop".equals(selectType)) {
            url = "/select.do?getDepartment";
        } else if ("selectProduct".equals(selectType)) {
            url = "/select.do?getGoods";
        } else if ("selectPosSalesGoods".equals(selectType)) {
            url = "/select.do?getPosSalesGoods";
        } else if ("selectPresentGoods".equals(selectType)) {
            url = "/select.do?getPresentGoods";
        } else if ("selectCategory".equals(selectType)) {
            url = "/select.do?getGoodsType";
        } else if ("selectPaymentType".equals(selectType)) {
            url = "/select.do?getPaymentType";
        } else if ("selectYear".equals(selectType)) {
            url = "/select.do?getYear";
        } else if ("selectBrand".equals(selectType)) {
            url = "/select.do?getBrand";
        } else if ("selectBrandByPower".equals(selectType)) {
            url = "/select.do?getBrandByPower";
        } else if ("selectSeason".equals(selectType)) {
            url = "/select.do?getSeason";
        } else if ("selectVip".equals(selectType)) {
            url = "/select.do?getVip";
        } else if ("type".equals(selectType)) {
            getType();
        } else if ("purchaseType".equals(selectType)) {
            getPurchaseType();
        } else if ("salesPriceType".equals(selectType)) {
            getPriceType();
        } else if ("selectWarehouseOutLine".equals(selectType)) {
            getWarehouseOutLine();
        } else if ("selectBarcodePrintType".equals(selectType)) {
            getBarcodePrintType();
        } else if ("customerType".equals(selectType)) {
            url = "/select.do?getCustomerType";
        } else if ("selectStorage".equals(selectType)) {
            url = "/select.do?getStorage";
        } else if ("selectBarcode".equals(selectType)) {
            url = "/select.do?getBarcode";
        } else if ("selectStorageByInner".equals(selectType)) {
            url = "/select.do?getStorageByInner";
        } else if ("selectWarehouseHasStorage".equals(selectType)) {
            url = "/select.do?getWarehouseHasStorage";
        } else if ("selectWarehouseHasStorageHasInit".equals(selectType)) {
            url = "/select.do?getWarehouseHasStorageHasInit";
        } else if ("selectGoodsInfo".equals(selectType)) {
            url = "/select.do?getGoodsInfo";
        } else if ("selectGoodsSubType".equals(selectType)) {
            url = "/select.do?getGoodsSubType";
        } else if ("selectGoodsBrandSerial".equals(selectType)) {
            url = "/select.do?getGoodsBrandSerial";
        } else if ("selectGoodsSupplier".equals(selectType)) {
            url = "/select.do?getGoodsSupplier";
        } else if ("selectGoodsColor".equals(selectType)) {
            url = "/select.do?getGoodsColor";
        } else if ("selectColorByGoodsCode".equals(selectType)) {
            url = "/select.do?getColorByGoodsCode";
        } else if ("selectSizeByGoodsCode".equals(selectType)) {
            url = "/select.do?getSizeByGoodsCode";
        } else if ("selectDocTypeIn".equals(selectType)) {
            url = "/select.do?getDocTypeIn";
        } else if ("selectDocTypeOut".equals(selectType)) {
            url = "/select.do?getDocTypeOut";
        } else if ("selectStockTypeIn".equals(selectType)) {
            url = "/select.do?getStockTypeIn";
        } else if ("selectStockTypeOut".equals(selectType)) {
            url = "/select.do?getStockTypeOut";
        } else if ("selectSalesOrderNo".equals(selectType)) {
            url = "/select.do?getSalesOrderNo";
        } else if ("selectSalesNo".equals(selectType)) {
            url = "/select.do?getSalesNo";
        } else if ("selectSalesReturnNo".equals(selectType)) {
            url = "/select.do?getSalesReturnNo";
        } else if ("selectPurchaseNo".equals(selectType)) {
            url = "/select.do?getPurchaseNo";
        } else if ("selectPurchaseReturnNo".equals(selectType)) {
            url = "/select.do?getPurchaseReturnNo";
        } else if ("selectStocktakingNo".equals(selectType)) {
            url = "/select.do?getStocktakingNo";
        } else if ("selectStockMoveNo".equals(selectType)) {
            url = "/select.do?getStockMoveNo";
        } else if ("selectPackingBoxSalesNo".equals(selectType)) {
            url = "/select.do?getPackingBoxSalesNo";
        } else if ("selectPackingBoxStockOutNo".equals(selectType)) {
            url = "/select.do?getPackingBoxStockOutNo";
        } else if ("selectPackingBoxSalesNoOfComplete".equals(selectType)) {
            url = "/select.do?getPackingBoxSalesNoOfComplete";
        } else if ("selectPackingBoxStockOutNoOfComplete".equals(selectType)) {
            url = "/select.do?getPackingBoxStockOutNoOfComplete";
        } else if ("selectCustomerByUserNo".equals(selectType)) {
            url = "/select.do?getCustomerByUserNo";
        }else if("selectPurchaseOrderNo".equals(selectType)){
            url ="/select.do?getPurchaseOrderNo";
        }
        return url;
    }

    /**
     * 选择类型
     */
    private void getType() {
        String[] values = new String[] {"批发", "订货", "配货", "补货"};
        String[] keys = new String[] {"PriceType", "OrderPriceType", "AllotPriceType", "ReplenishType"};
        for (int i = 0; i < values.length; i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("Name", values[i]);
            map.put("Type", keys[i]);
            dataList.add(map);
        }
    }

    /**
     * 选择类型(采购发货单)
     */
    private void getPurchaseType() {
        String[] values = new String[] {"采购", "生产", "订货", "配货", "补货"};
        String[] keys = new String[] {"", "", "", "", ""};
        for (int i = 0; i < values.length; i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("Name", values[i]);
            map.put("Type", keys[i]);
            dataList.add(map);
        }
    }

    /**
     * 价格类型
     */
    private void getPriceType() {
        String[] values = new String[] {"批发价", "批发价2", "批发价3", "批发价4", "批发价5", "批发价6", "批发价7", "批发价8", "批发价9", "零售价", "零售价2", "零售价3", "零售价4", "零售价5", "零售价6", "零售价7", "零售价8", "零售价9"};
        for (int i = 0; i < values.length; i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("Name", values[i]);
            dataList.add(map);
        }
    }

    /**
     * 系统默认仓库
     */
    private void getDefaultDepartment() {
        String[] values = new String[] {"仓库A", "仓库B", "仓库C", "仓库D", "仓库E", "仓库F", "仓库G", "仓库H", "仓库I", "仓库J", "仓库K", "仓库L", "仓库M", "仓库N", "仓库O", "仓库P", "仓库Q", "仓库R", "仓库S", "仓库T", "仓库U", "仓库V", "仓库W", "仓库X", "仓库Y", "仓库Z"};
        String[] keys = new String[] {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
        for (int i = 0; i < values.length; i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("Name", values[i]);
            map.put("DepartmentID", keys[i]);
            map.put("DepartmentCode", keys[i]);
            dataList.add(map);
        }
        if (orderGoodsQuery) {
            editAdapter.refresh();
        } else {
            adapter.refresh();
        }
    }

    /**
     * 根据登录权限获取仓库
     */
    private void getWarehouseOutLine() {
        Object obj = readProduct("base64WarehouseList", "warehouseList");
        if (null != obj) {
            List<Map<String, Object>> tempList = (List<Map<String, Object>>) obj;
            for (int i = 0; i < tempList.size(); i++) {
                Map<String, Object> temp = tempList.get(i);
                dataList.add(temp);
            }
        } else {
            AlertDialog.Builder dialog = new AlertDialog.Builder(SelectActivity.this, AlertDialog.THEME_HOLO_LIGHT);
            dialog.setTitle("系统提示");
            dialog.setMessage("当前无用户仓库可选！系统将使用设定的仓库数据");
            // 相当于点击确认按钮
            dialog.setPositiveButton("好的", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dataList.clear();
                    getDefaultDepartment();
                }
            });
            dialog.setCancelable(false);
            dialog.create();
            dialog.show();
        }
    }

    /**
     * 选择条码打印类型
     */
    private void getBarcodePrintType() {
        // String[] values = new String[] {"AZTEC", "CODE_39", "CODE_93", "CODABAR", "CODE_128",
        // "DATA_MATRIX",
        // "EAN_8", "EAN_13", "ITF", "MAXICODE", "RSS_14", "RSS_EXPANDED", "PDF_417", "UPC_A",
        // "UPC_E", "UPC_EAN_EXTENSION"};
        String[] values = new String[] {"CODE_39", "CODE_93", "CODABAR", "CODE_128", "EAN_13", "ITF", "PDF_417", "QR_CODE"};
        for (int i = 0; i < values.length; i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("Name", values[i]);
            dataList.add(map);
        }
    }

    /**
     * 读取缓存中的对象
     * 
     * @param docName
     * @param objName
     * @return
     */
    public Object readProduct(String docName, String objName) {
        Object obj = null;
        SharedPreferences preferences = getSharedPreferences(docName, MODE_PRIVATE);
        String customerBase64 = preferences.getString(objName, "");
        if (customerBase64 == "") {
            return obj;
        }
        // 读取字节
        byte[] base64 = Base64.decodeBase64(customerBase64.getBytes());
        // 封装到字节流
        ByteArrayInputStream bais = new ByteArrayInputStream(base64);
        try {
            // 再次封装
            ObjectInputStream bis = new ObjectInputStream(bais);
            try {
                // 读取对象
                obj = bis.readObject();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return obj;
    }

    /**
     * 根据参数获取显示数据
     * 
     * @param type
     */
    public void getData(int type) {
        RequestVo vo = new RequestVo();
        vo.requestUrl = getUrl();
        vo.context = this;
        HashMap map = new HashMap();
        if (null != param && !param.isEmpty()) {
            param = "@" + param + "@";
            et_param.setText(param);
        }
        map.put("currPage", String.valueOf(currPage));
        map.put("param", et_param.getText().toString());
        if (null != param && !param.isEmpty()) {
            param = null;
            et_param.setText(null);
        }
        vo.requestDataMap = map;
        this.type = type;
        super.getDataFromServerNoProcess(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    if (retObj.getBoolean("success")) {
                        if (SelectActivity.this.type == 1) {
                            dataList.clear();
                        }
                        JSONArray array = retObj.getJSONArray("obj");
                        if (array.length() == 0) {
                            Toast.makeText(SelectActivity.this, "无查询数据", Toast.LENGTH_SHORT).show();
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
                        if (SelectActivity.this.type == 2) {
                            lvDatas.hideFooterView();
                            if (array.length() == 0) {
                                Toast.makeText(SelectActivity.this, "已经到达最后一页", Toast.LENGTH_SHORT).show();
                            }
                        }
                        if (orderGoodsQuery) {
                            editAdapter.refresh();
                        } else {
                            adapter.refresh();
                        }
                    } else {
                        Toast.makeText(SelectActivity.this, retObj.getString("msg"), Toast.LENGTH_SHORT).show();
                        Logger.e(TAG, retObj.getString("msg"));
                    }
                } catch (Exception e) {
                    Toast.makeText(SelectActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    @Override
    public void onClick(View v) {}

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_ENTER) {
            InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm.isActive()) {
                imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
            }
            return true;
        }
        return false;
    }

    @Override
    protected void loadViewLayout() {
        setContentView(R.layout.select_activity);
        // set select page method
        Bundle bundle = this.getIntent().getExtras();
        if (null != bundle) {
            selectType = bundle.getString("selectType");
            param = bundle.getString("param");
            if (bundle.containsKey("orderGoodsQuery")) {
                orderGoodsQuery = bundle.getBoolean("orderGoodsQuery");
            }
        }
    }

    /**
     * 显示输入法
     */
    private void hideKeyBorder() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        boolean isOpen = imm.isActive();// isOpen若返回true，则表示输入法打开
        if (isOpen) {
            imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @Override
    protected void setListener() {
        et_param.addTextChangedListener(textWatcher);
        if (orderGoodsQuery) {
            editAdapter = new SelectEditAdapter(this, dataList);
            lvDatas.setAdapter(editAdapter);
        } else {
            adapter = new SelectAdapter(this, dataList);
            lvDatas.setAdapter(adapter);
        }
        lvDatas.setOnRefreshListener(this);
        lvDatas.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                RefreshListView listView = (RefreshListView) arg0;
                HashMap<String, String> map = (HashMap<String, String>) listView.getItemAtPosition(arg2);
                if (orderGoodsQuery) {
                    editAdapter.setSelectIndex(arg2);
                } else {
                    adapter.setSelectIndex(arg2);
                }
                Iterator iter = map.keySet().iterator();
                Intent intent = new Intent();
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    intent.putExtra(entry.getKey(), entry.getValue());
                }
                setResult(1, intent);
                finish();
            }

        });

    }

    @Override
    protected void processLogic() {
        if ("type".equals(selectType)) {
            // 移除搜索框
            ll_parent.removeView(ll_select);
            getType();
        } else if ("purchaseType".equals(selectType)) {
            // 移除搜索框
            ll_parent.removeView(ll_select);
            getPurchaseType();
        } else if ("salesPriceType".equals(selectType)) {
            // 移除搜索框
            ll_parent.removeView(ll_select);
            getPriceType();
        } else if ("selectWarehouseOutLine".equals(selectType)) {
            // 移除搜索框
            ll_parent.removeView(ll_select);
            getWarehouseOutLine();
        } else if ("selectBarcodePrintType".equals(selectType)) {
            // 移除搜索框
            ll_parent.removeView(ll_select);
            getBarcodePrintType();
        } else if ("selectDocTypeIn".equals(selectType) || "selectDocTypeOut".equals(selectType) || "selectSeason".equals(selectType) || "selectYear".equals(selectType) || "selectStockTypeOut".equals(selectType) || "selectStockTypeIn".equals(selectType) || "customerType".equals(selectType)) {
            // 移除搜索框
            ll_parent.removeView(ll_select);
            getData(1);
        } else if (!"type".equals(selectType)) {
            getData(1);
        }
        // set title
        setTitle();

    }

    @Override
    protected void findViewById() {
        lvDatas = (RefreshListView) findViewById(R.id.lv_datas);
        et_param = (EditText) findViewById(R.id.param);
        ll_select = (LinearLayout) findViewById(R.id.ll_select);
        ll_parent = (LinearLayout) findViewById(R.id.ll_parent);
        lvDatas.setUp(false);
    }

    @Override
    public void onDownPullRefresh() {}

    @Override
    public void onLoadingMore() {
        if ("type".equals(selectType) || "salesPriceType".equals(selectType) || "selectWarehouseOutLine".equals(selectType) || "selectBarcodePrintType".equals(selectType) || !NetUtil.hasNetwork(getApplicationContext())) {
            lvDatas.hideFooterView();
        } else {
            currPage++;
            getData(2);
        }
    }

    @Override
    public void finish() {
        hideKeyBorder();
        super.finish();
        // 关闭窗体动画显示
        this.overridePendingTransition(R.anim.activity_close, 0);
    }

    /**
     * 延迟线程，看是否还有下一个字符输入
     */
    private Runnable delayRun = new Runnable() {

        @Override
        public void run() {
            // 在这里调用服务器的接口，获取数据
            currPage = 1;
            getData(1);
        }
    };

    /**
     * 监听TextView的字符输入变化
     */
    private TextWatcher textWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void afterTextChanged(Editable s) {
            if (delayRun != null) {
                // 每次editText有变化的时候，则移除上次发出的延迟线程
                handler.removeCallbacks(delayRun);
            }
            if (s.length() == 0) {
                count = 0;
                if (count > 0) {
                    handler.removeCallbacks(delayRun);
                } else {
                    handler.postDelayed(delayRun, 500);
                    count++;
                }
            } else {
                // 延迟800ms，如果不再输入字符，则执行该线程的run方法
                handler.postDelayed(delayRun, 500);
            }
        }
    };

}
