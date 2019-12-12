package com.fuxi.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.json.JSONArray;
import org.json.JSONObject;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebSettings.TextSize;
import android.webkit.WebSettings.ZoomDensity;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.fuxi.adspter.PackingBoxAdspter;
import com.fuxi.dao.GeneralPackingBoxNoDao;
import com.fuxi.dao.ParamerDao;
import com.fuxi.main.R;
import com.fuxi.util.Logger;
import com.fuxi.util.LoginParameterUtil;
import com.fuxi.util.ParamterFileUtil;
import com.fuxi.vo.Paramer;
import com.fuxi.vo.RequestVo;
import com.fuxi.widget.OnRefreshListener;
import com.fuxi.widget.RefreshListView;

/**
 * Title: PackingBoxSelectPrintActivity Description: 装箱单选择打印界面(已完成的装箱单列表)
 * 
 * @author LYJ
 * 
 */
public class PackingBoxSelectPrintActivity extends BaseWapperActivity implements OnItemClickListener, OnRefreshListener, OnItemLongClickListener {

    private static final String TAG = "PackingBoxSelectPrintActivity";
    private static final String url = "/packing.do?getCompletePackingList";
    private static final String packingBoxNoList = "/packing.do?getCompletePackingBoxNoList";
    private static final String deleteAlreadyPackingBoxNo = "/packing.do?deleteAlreadyPackingBoxNo";
    private static String strIp;

    private RefreshListView lvDatas; // ListView
    private EditText etNo;
    private EditText etCustomer;
    private TextView tvTotalCount;
    private WebView mWebView;

    private ParamerDao paramerDao = new ParamerDao(this);
    private GeneralPackingBoxNoDao boxNoDao = new GeneralPackingBoxNoDao(this);
    private List<String> boxNos = new ArrayList<String>(); // 存储箱号
    private List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
    private PackingBoxAdspter adapter;
    private AlertDialog alertDialog;

    private String no;
    private String customerId;
    private String relationType;
    private String printIp; // 打印机IP
    private String printPort; // 打印机端口
    private String printer; // 打印机
    private boolean refresh;
    private int type;
    private int currPage = 1;

    private Paramer printPIp;
    private Paramer printPPort;
    private Paramer pPrinter;

    /**
     * 获取已经装箱的装箱单列表
     * 
     * @param refresh
     * @param type
     */
    private void getData(boolean refresh, int type) {
        RequestVo vo = new RequestVo();
        vo.requestUrl = url;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("no", no);
        map.put("currPage", String.valueOf(currPage));
        map.put("customerId", customerId);
        vo.requestDataMap = map;
        this.refresh = refresh;
        this.type = type;
        super.getDataFromServerNoProcess(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    if (retObj.getBoolean("success")) {
                        if (PackingBoxSelectPrintActivity.this.refresh) {
                            dataList.clear();
                        }
                        JSONArray array = retObj.getJSONArray("obj");
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
                        if (PackingBoxSelectPrintActivity.this.type == 1) {
                            lvDatas.hideFooterView();
                            if (array.length() == 0) {
                                Toast.makeText(PackingBoxSelectPrintActivity.this, "已经到达最后一页", Toast.LENGTH_SHORT).show();
                            }
                        } else if (PackingBoxSelectPrintActivity.this.type == 2) {
                            lvDatas.hideHeaderView();
                        }
                        countTotal();
                        adapter.refresh(dataList);
                    } else {
                    }
                } catch (Exception e) {
                    Toast.makeText(PackingBoxSelectPrintActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 计算并显示总数量
     */
    private void countTotal() {
        int sum = 0;
        for (int j = 0; j < dataList.size(); j++) {
            int num = Integer.parseInt(String.valueOf(dataList.get(j).get("QuantitySum")));
            sum += num;
        }
        tvTotalCount.setText(String.valueOf(sum));
        if (dataList.size() == 0) { // 提示没有要打印的数据
            prompt();
        }
    }

    /**
     * 无离线盘点数据时提示
     */
    private void prompt() {
        Builder dialog = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
        dialog.setTitle("系统提示");
        dialog.setMessage("当前没有要打印的单据！");
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

    @Override
    public void onClick(View v) {}

    /**
     * 重置筛选条件
     */
    private void reset() {
        etNo.setText(null);
        etCustomer.setText(null);
        no = null;
        customerId = null;
    }

    private class TouchListener implements OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (v.getId()) {
                case R.id.no:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        reset();
                        Intent intent = new Intent(PackingBoxSelectPrintActivity.this, SelectActivity.class);
                        if ("sales".equals(relationType)) {
                            intent.putExtra("selectType", "selectPackingBoxSalesNoOfComplete");
                        } else if ("stockOut".equals(relationType)) {
                            intent.putExtra("selectType", "selectPackingBoxStockOutNoOfComplete");
                        }
                        startActivityForResult(intent, R.id.no);
                        overridePendingTransition(R.anim.activity_open, 0);
                    }
                    break;
                case R.id.customer:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        reset();
                        Intent intent = new Intent(PackingBoxSelectPrintActivity.this, SelectActivity.class);
                        intent.putExtra("selectType", "selectCustomer");
                        startActivityForResult(intent, R.id.customer);
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
            case R.id.no:
                if (resultCode == 1) {
                    no = data.getStringExtra("Name");
                    etNo.setText(data.getStringExtra("Name"));
                    // 刷新数据
                    getData(true, type);;
                }
                break;
            case R.id.customer:
                if (resultCode == 1) {
                    customerId = data.getStringExtra("CustomerID");
                    etCustomer.setText(data.getStringExtra("Name"));
                    // 刷新数据
                    getData(true, type);
                }
                break;
            default:
                break;
        }
    }

    // @Override
    // protected void onHeadRightButton(View v) {
    // // 获取用户操作权限(新增)
    // boolean addRight = LoginParameterUtil.salesRightMap.get("AddRight");
    // if (addRight) {
    // // 新增销售发货单(散件)
    // addItem();
    // } else {
    // Builder dialog = new AlertDialog.Builder(PackingBoxActivity.this,
    // AlertDialog.THEME_HOLO_LIGHT);
    // dialog.setTitle("提示");
    // dialog.setMessage("当前暂无新增销售发货单的操作权限");
    // // 相当于点击确认按钮
    // dialog.setPositiveButton("确认",
    // new DialogInterface.OnClickListener() {
    // @Override
    // public void onClick(DialogInterface dialog, int which) {
    // }
    // });
    // dialog.setCancelable(false);
    // dialog.create();
    // dialog.show();
    // }
    // }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    protected void onHeadRightButton(View v) {

    }

    @Override
    protected void loadViewLayout() {
        setContentView(R.layout.activity_packing_box_select_print);
        setTitle("已 装 箱 单 据");
        setHeadRightVisibility(View.INVISIBLE);
    }

    @Override
    protected void setListener() {
        // ListView设置
        adapter = new PackingBoxAdspter(this, dataList);
        lvDatas.setAdapter(adapter);
        lvDatas.setOnItemClickListener(this);
        lvDatas.setOnItemLongClickListener(this);
        TouchListener tl = new TouchListener();
        etNo.setOnTouchListener(tl);
        etCustomer.setOnTouchListener(tl);
    }

    @Override
    protected void processLogic() {
        // 根据参数获取关联类别
        if (LoginParameterUtil.packingBoxRelationType == 0) {
            relationType = "sales";
        } else {
            relationType = "stockOut";
        }
        // 获取服务器连接IP
        getRequestPath();
        // 获取打印机信息
        getPrintMsg();
        // 加载数据
        getData(true, type);
    }

    @Override
    protected void findViewById() {
        etNo = (EditText) findViewById(R.id.no);
        etCustomer = (EditText) findViewById(R.id.customer);
        tvTotalCount = (TextView) findViewById(R.id.totalCount);
        lvDatas = (RefreshListView) findViewById(R.id.lv_datas);
        mWebView = (WebView) findViewById(R.id.myWeb);
        lvDatas.setOnRefreshListener(this);
    }

    /**
     * ListView点击事件
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        RefreshListView listView = (RefreshListView) parent;
        HashMap<String, String> map = (HashMap<String, String>) listView.getItemAtPosition(position);
        String packingBoxId = (String) map.get("PackingBoxID");
        String customer = (String) map.get("Customer");
        // print(packingBoxId,customer);
        // 选择打印模板
        showPrintTempleteDialog(view, packingBoxId, customer);
    }

    /**
     * 返回时释放装箱单据
     */
    private void selectPrint(String packingBoxId, String customer, String printBoxNo) {
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

    private void print(final String printPath) {
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

    /**
     * 单选提示框选择已经生成的箱号
     * 
     * @param view
     */
    private void showBoxListAlertDialog(final String packingBoxId, final String customer, View view) {
        final String[] items = boxNos.toArray(new String[boxNos.size()]);
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
        alertBuilder.setTitle("选择箱号");
        alertBuilder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int index) {
                String boxNo = items[index];
                selectPrint(packingBoxId, customer, boxNo);
                // 关闭提示框
                alertDialog.dismiss();
            }
        });
        alertDialog = alertBuilder.create();
        alertDialog.show();
    }

    /**
     * 查找箱号集合
     * 
     * @param packingBoxId
     */
    private void showBoxNos(final String packingBoxId, final String customer, final View view) {
        RequestVo vo = new RequestVo();
        vo.requestUrl = packingBoxNoList;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("packingBoxId", packingBoxId);
        vo.requestDataMap = map;
        super.getDataFromServer(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    if (retObj.getBoolean("success")) {
                        boxNos.clear();
                        JSONArray array = retObj.getJSONArray("obj");
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject object = (JSONObject) array.get(i);
                            String boxNo = object.getString("BoxNo");
                            boxNos.add(boxNo);
                        }
                        showBoxListAlertDialog(packingBoxId, customer, view);
                    }
                } catch (Exception e) {
                    Toast.makeText(PackingBoxSelectPrintActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 删除已经装箱的单据(重新装箱)
     * 
     * @param packingBoxId
     */
    private void deleteAlreadyPackingBoxNo(String packingBoxId, final String packingBoxNo, final String tcustomerId) {
        RequestVo vo = new RequestVo();
        vo.requestUrl = deleteAlreadyPackingBoxNo;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("packingBoxId", packingBoxId);
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
                        boolean flag = jsonObject.getBoolean("flag");
                        if (flag) {
                            // String boxNo = jsonObject.getString("boxNo");
                            // boxNoDao.update(new GeneralPackingBoxNo(boxNo, tcustomerId,
                            // Tools.dateToString(new Date()), 0));
                            Toast.makeText(PackingBoxSelectPrintActivity.this, "装箱单 " + packingBoxNo + " 删除成功", Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(PackingBoxSelectPrintActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 选择打印模板(装箱单)
     * 
     * @param view
     */
    private void showPrintTempleteDialog(final View view, final String packingBoxId, final String customer) {
        final String[] items = {"整单打印", "选箱打印"};
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
        alertBuilder.setTitle("选择装箱单打印模板");
        alertBuilder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int index) {
                if (index == 0) {
                    selectPrint(packingBoxId, customer, "");
                } else if (index == 1) {
                    showBoxNos(packingBoxId, customer, view);
                }
                // 关闭提示框
                alertDialog.dismiss();
            }
        });
        alertDialog = alertBuilder.create();
        alertDialog.show();
    }

    @Override
    public void onLoadingMore() {
        currPage++;
        getData(true, 1);
    }

    @Override
    public void onDownPullRefresh() {
        currPage = 1;
        getData(true, 2);
    }

    /**
     * 长按选中ListView的项 长按删除
     */
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        final int location = position - 1;
        Map<String, Object> map = dataList.get(location);
        final String packingBoxNo = String.valueOf(map.get("No"));
        final String packingBoxId = String.valueOf(map.get("PackingBoxID"));
        final String customerId = String.valueOf(map.get("CustomerID"));
        Builder dialog = new AlertDialog.Builder(PackingBoxSelectPrintActivity.this, AlertDialog.THEME_HOLO_LIGHT);
        dialog.setTitle("提示");
        dialog.setMessage("确定要删除装箱单 " + packingBoxNo + " 并进行重新装箱吗？");
        // 相当于点击确认按钮
        dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dataList.remove(location);
                countTotal();
                adapter.notifyDataSetChanged();
                deleteAlreadyPackingBoxNo(packingBoxId, packingBoxNo, customerId);
            }
        });
        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {}
        });
        dialog.setCancelable(false);
        dialog.create();
        dialog.show();
        return true;
    }

}
