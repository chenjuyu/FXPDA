package com.fuxi.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
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
import com.fuxi.adspter.BarcodeAdapter;
import com.fuxi.dao.DepartmentDao;
import com.fuxi.dao.OutLineStocktakingDao;
import com.fuxi.main.R;
import com.fuxi.util.Logger;
import com.fuxi.util.NetUtil;
import com.fuxi.util.Tools;
import com.fuxi.vo.Department;
import com.fuxi.vo.OutLineStocktaking;
import com.fuxi.vo.RequestVo;

public class InventorySheetOutLineDetailActivity extends BaseWapperActivity implements OnItemLongClickListener {

    // 静态常量
    private static final String TAG = "InventorySheetOutLineActivity";
    private static final String sendDatasToGenerateText = "/common.do?generateText";

    private List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
    private OutLineStocktakingDao stocktakingDao = new OutLineStocktakingDao(this);
    private DepartmentDao departmentDao = new DepartmentDao(this);
    private BarcodeAdapter adapter;
    private AlertDialog alertDialog;

    private LinearLayout llScanning;
    private LinearLayout llTitle;
    private EditText etBarcode;
    private EditText etQty;
    private EditText etDepartment;
    private EditText etShelvesNo;
    private TextView tvQtySum;
    private TextView tvSave;
    private TextView tvBegin;
    private ListView lvDatas;

    private String departmentId;
    private String departmentName;
    private String departmentCode;
    private String docShelvesNo;

    private boolean saveFlag = false;
    private boolean hasOutLineDept = false;

    @Override
    public void onClick(View v) {
        InputMethodManager im = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (im.isActive()) {
            im.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
        }
        switch (v.getId()) {
            case R.id.save:
                save();
                break;
            case R.id.begin:
                begin();
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
     * 开始盘点
     */
    private void begin() {
        String shelvesNo = etShelvesNo.getText().toString();
        if (!hasOutLineDept) {
            departmentId = etDepartment.getText().toString().trim();
            departmentCode = departmentId;
        }
        if (null == departmentId || "".equals(departmentId)) {
            Toast.makeText(InventorySheetOutLineDetailActivity.this, "请先选择仓库", Toast.LENGTH_SHORT).show();
            reset();
            return;
        }
        if (null == shelvesNo || "".equals(shelvesNo)) {
            Toast.makeText(InventorySheetOutLineDetailActivity.this, "请先输入货架号", Toast.LENGTH_SHORT).show();
            reset();
            return;
        }
        // 获取单号
        String no = departmentCode + shelvesNo;
        // 判断数据库中是否有相应单号的数据
        List<OutLineStocktaking> list = stocktakingDao.find(no);
        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            OutLineStocktaking lineStocktaking = list.get(i);
            map.put("Barcode", lineStocktaking.getBarcode());
            map.put("Quantity", lineStocktaking.getQuantity());
            dataList.add(map);
        }
        tvBegin.setVisibility(View.GONE);
        llScanning.setVisibility(View.VISIBLE);
        llTitle.setVisibility(View.VISIBLE);
        etBarcode.requestFocus();
        // 计算数量
        countTotal();
    }

    /**
     * 删除缓存中的数据
     * 
     * @param no
     */
    private void delete(String no) {
        List<OutLineStocktaking> list = stocktakingDao.find(no);
        for (int i = 0; i < list.size(); i++) {
            OutLineStocktaking lineStocktaking = list.get(i);
            stocktakingDao.delete(String.valueOf(lineStocktaking.getId()));
        }
    }

    /**
     * 保存盘点数据
     */
    private void save() {
        String shelvesNo = etShelvesNo.getText().toString();
        departmentName = etDepartment.getText().toString();
        // 获取单号
        String no = departmentCode + shelvesNo;
        Date date = new Date();
        // 删除原来的盘点记录
        delete(no);
        // 将数据存入缓存本地数据库中
        for (int i = 0; i < dataList.size(); i++) {
            Map<String, Object> map = dataList.get(i);
            String barcode = String.valueOf(map.get("Barcode"));
            int quantity = Integer.parseInt(String.valueOf(map.get("Quantity")));
            OutLineStocktaking lineStocktaking = new OutLineStocktaking(no, departmentId, barcode, shelvesNo, departmentCode, departmentName, quantity, Tools.dateTimeToString(date));
            stocktakingDao.insert(lineStocktaking);
        }
        // 保存成功提示
        saveSuccess();
    }

    /**
     * 保存条码信息
     */
    private void addBarcode() {
        String barcodeStr = etBarcode.getText().toString();
        String qtyStr = etQty.getText().toString();
        String shelvesNo = etShelvesNo.getText().toString();
        if (null == departmentId || "".equals(departmentId)) {
            Toast.makeText(InventorySheetOutLineDetailActivity.this, "请先选择仓库", Toast.LENGTH_SHORT).show();
            reset();
            return;
        }
        if (null == shelvesNo || "".equals(shelvesNo)) {
            Toast.makeText(InventorySheetOutLineDetailActivity.this, "请先输入货架号", Toast.LENGTH_SHORT).show();
            reset();
            return;
        }
        if (null == barcodeStr || "".equals(barcodeStr)) {
            Toast.makeText(InventorySheetOutLineDetailActivity.this, R.string.barcode_null, Toast.LENGTH_SHORT).show();
            return;
        }
        if (qtyStr == null || "".equals(qtyStr)) {
            Toast.makeText(InventorySheetOutLineDetailActivity.this, R.string.qty_null, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Tools.isNumeric(qtyStr)) {
            Toast.makeText(InventorySheetOutLineDetailActivity.this, "数量非法，请修改数量", Toast.LENGTH_SHORT).show();
            etQty.requestFocus();
            etQty.selectAll();
            return;
        }
        // 保存提示
        saveFlag = true;
        // 添加条码
        boolean isNew = true;
        for (int i = 0; i < dataList.size(); i++) {
            Map<String, Object> map = dataList.get(i);
            String barcode = String.valueOf(map.get("Barcode"));
            if (barcodeStr.equals(barcode)) {
                int quantity = Integer.parseInt(String.valueOf(map.get("Quantity")));
                map.put("Quantity", String.valueOf(quantity + Integer.parseInt(qtyStr)));
                isNew = false;
            }
        }
        if (isNew) {
            Map<String, Object> temp = new HashMap<String, Object>();
            temp.put("Barcode", barcodeStr);
            temp.put("Quantity", qtyStr);
            dataList.add(temp);
        }
        // 计算合计
        countTotal();
        // 重置扫码区
        reset();
        // 刷新ListView
        adapter.refresh();
    }

    /**
     * 计算并显示总数量
     */
    private void countTotal() {
        int sum = 0;
        for (int j = 0; j < dataList.size(); j++) {
            int num = Integer.parseInt(String.valueOf(dataList.get(j).get("Quantity")));
            sum += num;
        }
        tvQtySum.setText(String.valueOf(sum));
        if (sum > 0) {
            tvSave.setVisibility(View.VISIBLE);
            // 开始盘点后仓库和货架不可变
            etDepartment.setEnabled(false);
            etShelvesNo.setEnabled(false);
        }
    }

    /**
     * 重置扫码区
     */
    private void reset() {
        etBarcode.setText(null);
        etQty.setText("1");
        etBarcode.requestFocus();
    }

    @Override
    protected void loadViewLayout() {
        setContentView(R.layout.activity_inventory_sheet_out_line_detail);
        setTitle("离 线 盘 点");
        // 显示右上角按钮
        setHeadRightVisibility(View.VISIBLE);
        setHeadRightText(R.string.export);
    }

    @Override
    protected void processLogic() {
        // 获取用户操作权限(新增)
        // boolean addRight = LoginParameterUtil.stocktakingRightMap.get("AddRight");
        // if (!addRight) {
        // Builder dialog = new AlertDialog.Builder(
        // InventorySheetOutLineDetailActivity.this, AlertDialog.THEME_HOLO_LIGHT);
        // dialog.setTitle("提示");
        // dialog.setMessage("当前暂无新增盘点单的操作权限");
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
        Bundle bundle = this.getIntent().getExtras();
        if (null != bundle) {
            docShelvesNo = bundle.getString("ShelvesNo");
            departmentId = bundle.getString("DepartmentID");
            departmentCode = bundle.getString("DepartmentCode");
            departmentName = bundle.getString("DepartmentName");
            etDepartment.setText(departmentName);
            etShelvesNo.setText(docShelvesNo);
            begin();
        } else {
            llScanning.setVisibility(View.GONE);
            llTitle.setVisibility(View.GONE);
            tvSave.setVisibility(View.GONE);
        }
        // 扫描区获取焦点
        etBarcode.requestFocus();

    }

    @Override
    protected void setListener() {
        TouchListener tl = new TouchListener();

        // 根据离线仓库数据判断仓库是否手动输入
        Department findFirst = departmentDao.findFirst();
        if (findFirst != null) {
            hasOutLineDept = true;
            etDepartment.setFocusableInTouchMode(false);
            etDepartment.setFocusable(false);
            etDepartment.setOnTouchListener(tl);
        } else {
            etDepartment.setCompoundDrawables(null, null, null, null);
        }
        tvSave.setOnClickListener(this);
        tvBegin.setOnClickListener(this);
        adapter = new BarcodeAdapter(this, dataList);
        lvDatas.setAdapter(adapter);
        lvDatas.setOnItemLongClickListener(this);
        etBarcode.setOnEditorActionListener(new BarcodeActionListener());
        etQty.setOnEditorActionListener(new QtyBarcodeActionListener());
    }

    private class TouchListener implements OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (v.getId()) {
                case R.id.department:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Intent intent = new Intent(InventorySheetOutLineDetailActivity.this, OutLineSelectActivity.class);
                        intent.putExtra("selectType", "selectWarehouseOutLine");
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
            case R.id.department:
                if (resultCode == 1) {
                    etDepartment.setText(data.getStringExtra("Name"));
                    departmentId = data.getStringExtra("DepartmentID");
                    departmentCode = data.getStringExtra("DepartmentCode");
                }
                // 设置扫码区自动获取焦点
                etBarcode.requestFocus();
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
            if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm.isActive()) {
                    imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
                }
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    addBarcode();
                } else if (event != null && event.getAction() == KeyEvent.ACTION_DOWN) {
                    addBarcode();
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
                etBarcode.requestFocus();
                return true;
            }
            return false;
        }

    }

    /**
     * 单选提示框选择条码检验导出方式 1:导出到设备 2.导出到服务器
     * 
     * @param view
     */
    private void showSingleAlertDialog(View view) {
        final String[] items = {"导出到本地", "导出到服务器"};
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
        alertBuilder.setTitle("选择盘点数据导出方式");
        alertBuilder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int index) {
                if (index == 0) {
                    // 导出到本地
                    exportToLocal();
                } else if (index == 1) {
                    if (NetUtil.hasNetwork(getApplicationContext())) {
                        // 导出到服务器
                        exportToServer();
                    } else {
                        // 无网络
                        Builder dialog = new AlertDialog.Builder(InventorySheetOutLineDetailActivity.this, AlertDialog.THEME_HOLO_LIGHT);
                        dialog.setTitle("系统提示");
                        dialog.setMessage("当前无网络连接，请连接网络后重试！");
                        // 相当于点击确认按钮
                        dialog.setPositiveButton("我知道了", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {}
                        });
                        dialog.create();
                        dialog.show();
                    }
                }
                // 关闭提示框
                alertDialog.dismiss();
            }
        });
        alertDialog = alertBuilder.create();
        alertDialog.show();
    }

    /**
     * 导出到本地
     */
    private void exportToLocal() {
        String shelvesNo = etShelvesNo.getText().toString();
        // 导出Text文件
        final String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "fxpda" + File.separator + "export_file";
        File direct = new File(path);
        if (!direct.exists()) {
            direct.mkdirs();
        }
        String quantitySum = tvQtySum.getText().toString();
        String exportPath = path + File.separator + departmentCode + "_" + shelvesNo + "_" + quantitySum + ".txt";
        String showPath = "fxpda" + File.separator + "export_file" + File.separator + departmentCode + "_" + shelvesNo + "_" + quantitySum + ".txt";
        // 格式化盘点内容
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < dataList.size(); i++) {
            Map<String, Object> map = dataList.get(i);
            String barcode = String.valueOf(map.get("Barcode"));
            int quantity = Integer.parseInt(String.valueOf(map.get("Quantity")));
            sb.append(barcode + "," + quantity + "\n");
        }
        Tools.contentToTxt(exportPath, sb.toString());
        // 提示
        Builder dialog = new AlertDialog.Builder(InventorySheetOutLineDetailActivity.this, AlertDialog.THEME_HOLO_LIGHT);
        dialog.setTitle("提示");
        dialog.setMessage("离线盘点文件已导出到 " + showPath);
        // 相当于点击确认按钮
        dialog.setPositiveButton("返回", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 删除原来的盘点记录
                String shelvesNo = etShelvesNo.getText().toString();
                String no = departmentCode + shelvesNo;
                // delete(no);
                back();
            }
        });
        dialog.setCancelable(false);
        dialog.create();
        dialog.show();
    }

    /**
     * 导出到服务器
     */
    private void exportToServer() {
        String quantitySum = tvQtySum.getText().toString();
        String shelvesNo = etShelvesNo.getText().toString();
        String no = departmentCode + shelvesNo;
        sendDatasToServer(no, departmentCode, shelvesNo, quantitySum);
    }

    /**
     * 发送盘点数据到服务端生成Text
     * 
     * @param no
     * @param listTitle
     * @param objs
     */
    private void sendDatasToServer(final String no, String departmentCode, String shelvesNo, String quantitySum) {
        RequestVo vo = new RequestVo();
        vo.requestUrl = sendDatasToGenerateText;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("quantitySum", quantitySum);
        map.put("departmentCode", departmentCode);
        map.put("shelvesNo", shelvesNo);
        net.sf.json.JSONArray jsons = net.sf.json.JSONArray.fromObject(dataList);
        map.put("dataList", jsons.toString());
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
                        Builder dialog = new AlertDialog.Builder(InventorySheetOutLineDetailActivity.this, AlertDialog.THEME_HOLO_LIGHT);
                        dialog.setTitle("提示");
                        dialog.setMessage("离线盘点文件已导出到服务器 " + exportPath);
                        // 相当于点击确认按钮
                        dialog.setPositiveButton("返回", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 删除原来的盘点记录
                                // delete(no);
                                back();
                            }
                        });
                        dialog.setCancelable(false);
                        dialog.create();
                        dialog.show();
                    }
                } catch (Exception e) {
                    etBarcode.requestFocus();
                    etBarcode.selectAll();
                    Toast.makeText(InventorySheetOutLineDetailActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    @Override
    protected void onHeadRightButton(View v) {
        if (dataList.size() < 1) {
            Toast.makeText(InventorySheetOutLineDetailActivity.this, "当前暂无盘点数据，请先进行盘点后再导出数据", Toast.LENGTH_LONG).show();
        } else {
            finishOrNot(v);
        }
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
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 点击返回时的操作
     */
    private void back() {
        Intent intent = new Intent(InventorySheetOutLineDetailActivity.this, InventorySheetOutLineActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * 点击导出按钮时提示
     */
    private void finishOrNot(final View v) {
        // 询问是否删除
        Builder dialog = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
        dialog.setTitle("提示");
        dialog.setMessage("确定结束盘点并导出盘点数据吗？");
        // 相当于点击确认按钮
        dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 选择导出方式
                showSingleAlertDialog(v);
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
     * 点击返回时询问是否保存当前信息
     */
    private void saveOrNot() {
        if (saveFlag) {
            // 询问是否删除
            Builder dialog = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
            dialog.setTitle("保存提示");
            dialog.setMessage("离线盘点数据尚未保存，确定要返回吗，返回将丢失盘点数据？");
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
     * 保存成功提示
     */
    private void saveSuccess() {
        Builder dialog = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
        dialog.setTitle("提示");
        dialog.setMessage("盘点数据保存成功");
        // 相当于点击确认按钮
        dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                back();
            }
        });
        dialog.create();
        dialog.show();
    }

    @Override
    protected void findViewById() {
        llScanning = (LinearLayout) findViewById(R.id.ll_scanning);
        llTitle = (LinearLayout) findViewById(R.id.ll_title);
        etBarcode = (EditText) findViewById(R.id.barcode);
        etQty = (EditText) findViewById(R.id.qty);
        etDepartment = (EditText) findViewById(R.id.department);
        etShelvesNo = (EditText) findViewById(R.id.shelvesNo);
        tvQtySum = (TextView) findViewById(R.id.qtysum);
        tvSave = (TextView) findViewById(R.id.save);
        tvBegin = (TextView) findViewById(R.id.begin);
        lvDatas = (ListView) findViewById(R.id.datas);
    }

    /**
     * 长按选中ListView的项
     */
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        final Map<String, Object> map = dataList.get(position);
        String barcode = String.valueOf(map.get("Barcode"));
        String quantity = String.valueOf(map.get("Quantity"));
        // 隐藏控件
        View v = (LinearLayout) getLayoutInflater().inflate(R.layout.menu_inventory, null);
        View view1 = v.findViewById(R.id.view1);
        final EditText et_count = (EditText) v.findViewById(R.id.et_count);
        LinearLayout ll_meno = (LinearLayout) v.findViewById(R.id.ll_meno);
        view1.setVisibility(View.GONE);
        ll_meno.setVisibility(View.GONE);
        et_count.setText(quantity);
        et_count.setSelection(quantity.length());
        Builder dialog = new AlertDialog.Builder(InventorySheetOutLineDetailActivity.this, AlertDialog.THEME_HOLO_LIGHT);
        dialog.setView(v);
        dialog.setTitle("提示");
        dialog.setMessage("修改条码" + barcode + "的数量 :");
        // 相当于点击确认按钮
        dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String countStr = et_count.getText().toString();
                if ("".equals(countStr.trim())) {
                    countStr = "1";
                }
                int count = Integer.parseInt(countStr);
                if (count < 1) {
                    dataList.remove(map);
                } else {
                    map.put("Quantity", String.valueOf(count));
                }
                // 动态更新ListView
                adapter.notifyDataSetChanged();
                // 计算价格
                countTotal();
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
