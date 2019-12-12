package com.fuxi.activity;

import java.util.ArrayList;
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
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.fuxi.adspter.ShelvesOutAdspter;
import com.fuxi.adspter.ShelvesOutAdspter.Zujian;
import com.fuxi.main.R;
import com.fuxi.main.R.color;
import com.fuxi.util.Logger;
import com.fuxi.util.LoginParameterUtil;
import com.fuxi.vo.RequestVo;

/**
 * Title: ShelvesOutActivity Description: 仓位下架活动界面
 * 
 * @author LYJ
 * 
 */
public class ShelvesOutActivity extends BaseWapperActivity implements OnItemClickListener {

    private static final String TAG = "StorageOutActivity";
    private static final String PATH = "/shelvesOut.do?getStorageOutDetail";
    private static final String StockDetail = "/shelvesIn.do?getStockDetail";
    private static final String ReleasingResources = "/shelvesOut.do?releasingResources";
    private static final String COMPLETEPATH = "/shelvesOut.do?completeShelvesOut";
    private static final String SHELVESOUT = "/shelvesOut.do?shelvesOut";
    private static final String quickShelvesOut = "/shelvesOut.do?quickShelvesOut";

    private EditText etStockOutNo;
    private TextView tvComplete;
    private TextView tvQuickComplete;
    private EditText etStorage;

    private ListView lvDatas; // ListView
    private ShelvesOutAdspter adapter;

    private boolean refresh;
    private boolean storageOnTouch = false;// 是否有选择仓位
    private boolean isEmpty = false;// 库架上没有出仓单内的货品
    private boolean flag = false;
    private String stockId;
    private String stockNo;
    private String departmentId; // 出仓单对应的部门ID
    private String newStorageId;// 仓位ID
    private String newStorage;// 仓位
    private String type;// 类型

    private List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();

    /**
     * 根据下架推荐算法获取下架推荐信息
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void getData() {
        RequestVo vo = new RequestVo();
        vo.requestUrl = PATH;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("stockNo", stockNo);
        map.put("departmentId", departmentId);
        map.put("storageOutType", String.valueOf(LoginParameterUtil.storageOutType));
        vo.requestDataMap = map;
        super.getDataFromServer(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    if (retObj.getBoolean("success")) {
                        if (ShelvesOutActivity.this.refresh) {
                            dataList.clear();
                        }
                        JSONArray array = retObj.getJSONArray("obj");
                        if (retObj.isNull("attributes")) {
                            AlertDialog.Builder dialog = new AlertDialog.Builder(ShelvesOutActivity.this, AlertDialog.THEME_HOLO_LIGHT);
                            dialog.setTitle("提示");
                            dialog.setMessage("操作员 " + array.getJSONObject(0).getString("UserName") + " 正在下架出仓单 " + stockNo + " 中的货品,系统已将此单据锁定,请选择其它单据进行操作");
                            // 相当于点击确认按钮
                            dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(ShelvesOutActivity.this, StockOutActivity.class);
                                    intent.putExtra("type", type);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                            dialog.setCancelable(false);
                            dialog.create();
                            dialog.show();
                        } else {
                            JSONObject rs = retObj.getJSONObject("attributes");
                            flag = rs.getBoolean("flag");
                            isEmpty = rs.getBoolean("isEmpty");
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
                            if (!flag || isEmpty) {
                                // Toast.makeText(ShelvesOutActivity.this,
                                // "仓位货品库存不足,部分货品的推荐仓位暂时无法显示",
                                // Toast.LENGTH_LONG).show();
                                tvQuickComplete.setEnabled(false);
                                tvQuickComplete.setBackgroundColor(Color.GRAY);
                                AlertDialog.Builder dialog = new AlertDialog.Builder(ShelvesOutActivity.this, AlertDialog.THEME_HOLO_LIGHT);
                                dialog.setTitle("提示");
                                if (!flag) {
                                    dialog.setMessage("仓位货品库存不足,不足货品的推荐仓位暂时无法显示");
                                    // 相当于点击确认按钮
                                    dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            tvQuickComplete.setVisibility(View.GONE);
                                        }
                                    });
                                    dialog.setCancelable(false);
                                    dialog.create();
                                    dialog.show();
                                } else {
                                    dialog.setMessage("单据 " + stockNo + " 待下架的货品加载失败或暂无下架货品,请稍后重试");
                                    // 相当于点击确认按钮
                                    dialog.setPositiveButton("稍后重试", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            finish();
                                        }
                                    });
                                    // 相当于点击取消按钮
                                    dialog.setNegativeButton("继续操作", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {}
                                    });
                                    dialog.setCancelable(false);
                                    dialog.create();
                                    dialog.show();
                                }
                            }
                            adapter.refresh(dataList);
                        }
                    } else {
                        tvComplete.setEnabled(false);
                        tvComplete.setBackgroundColor(color.gray);
                        Toast.makeText(ShelvesOutActivity.this, "系统错误,请稍后重试", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    tvComplete.setEnabled(false);
                    tvComplete.setBackgroundColor(color.gray);
                    Toast.makeText(ShelvesOutActivity.this, "系统错误,请稍后重试", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        // 关闭软键盘
        InputMethodManager im = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (im.isActive()) {
            im.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
        }
        switch (v.getId()) {
            case R.id.complete:
                // 完成下架
                completeOrNo();
                break;
            case R.id.quickComplete:
                // 完成下架
                quickShelvesOutOrNo();
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            back();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onHeadLeftButton(View v) {
        back();
    }

    /**
     * 点击返回时的操作
     */
    private void back() {
        // 点击返回时
        if (dataList.size() > 0) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(ShelvesOutActivity.this, AlertDialog.THEME_HOLO_LIGHT);
            dialog.setTitle("提示");
            dialog.setMessage("确定要退出下架并释放当前锁定的库位资源 ?");
            // 相当于点击确认按钮
            dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // 释放资源
                    releasingResources(stockNo);
                }
            });
            // 相当于点击取消按钮
            dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {}
            });
            dialog.setCancelable(false);
            dialog.create();
            dialog.show();
        } else {
            // 释放资源
            releasingResources(stockNo);
            finish();
        }
    }

    /**
     * 释放锁定的仓位资源
     * 
     * @param stockNo
     */
    private void releasingResources(String stockNo) {
        RequestVo vo = new RequestVo();
        vo.requestUrl = ReleasingResources;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("stockNo", stockNo);
        vo.requestDataMap = map;
        super.getDataFromServer(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    int count = retObj.getInt("obj");
                    if (retObj.getBoolean("success") && count > 0) {
                        Intent intent = new Intent(ShelvesOutActivity.this, StockOutActivity.class);
                        intent.putExtra("type", type);
                        startActivity(intent);
                        finish();
                    } else {
                        AlertDialog.Builder dialog = new AlertDialog.Builder(ShelvesOutActivity.this, AlertDialog.THEME_HOLO_LIGHT);
                        dialog.setTitle("提示");
                        dialog.setMessage("当前锁定资源无法释放或无可释放资源");
                        // 相当于点击确认按钮
                        dialog.setPositiveButton("点击刷新", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dataList.clear();
                                getData();
                            }
                        });
                        dialog.setCancelable(false);
                        dialog.create();
                        dialog.show();
                    }
                } catch (Exception e) {
                    Toast.makeText(ShelvesOutActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 询问是否完成下架操作
     */
    private void completeOrNo() {
        if (dataList.size() > 0) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(ShelvesOutActivity.this, AlertDialog.THEME_HOLO_LIGHT);
            dialog.setTitle("提示");
            dialog.setMessage("出仓单 " + stockNo + " 的货品尚未全部下架,确定要完成下架操作吗?完成下架操作后的单据将不再显示");
            // 相当于点击确认按钮
            dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
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
            dialog.setCancelable(false);
            dialog.create();
            dialog.show();
        } else {
            complete();
        }
    }

    /**
     * 快速下架操作
     */
    private void quickShelvesOutOrNo() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(ShelvesOutActivity.this, AlertDialog.THEME_HOLO_LIGHT);
        dialog.setTitle("提示");
        dialog.setMessage("确认要快速下架出仓单 " + stockNo + " 中的货品吗?");
        // 相当于点击确认按钮
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                quickShelvesOut();
            }
        });
        // 相当于点击取消按钮
        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {}
        });
        dialog.setCancelable(false);
        dialog.create();
        dialog.show();
    }

    /**
     * 完成下架操作
     */
    private void complete() {
        RequestVo vo = new RequestVo();
        vo.requestUrl = COMPLETEPATH;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("stockNo", stockNo);
        map.put("departmentId", departmentId);
        vo.requestDataMap = map;
        tvComplete.setEnabled(false);
        tvComplete.setBackgroundColor(Color.GRAY);
        super.getDataFromServer(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    int count = retObj.getInt("obj");
                    if (retObj.getBoolean("success") && count > 0) {
                        Toast.makeText(ShelvesOutActivity.this, "出仓单 " + stockNo + " 已完成下架", Toast.LENGTH_SHORT).show();
                        dataList.clear();
                        tvComplete.setEnabled(false);
                        tvComplete.setBackgroundColor(color.gray);
                        finish();
                    } else {
                        if (count == -1) {
                            tvComplete.setEnabled(false);
                            tvComplete.setBackgroundColor(color.gray);
                            AlertDialog.Builder dialog = new AlertDialog.Builder(ShelvesOutActivity.this, AlertDialog.THEME_HOLO_LIGHT);
                            dialog.setTitle("提示");
                            dialog.setMessage("出仓单 " + stockNo + " 存在未下架货品,部分货品因库存不足暂时无法下架,请进行库位调整或单据调整后再进行此操作");
                            // 相当于点击确认按钮
                            dialog.setPositiveButton("点击返回", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            });
                            dialog.setCancelable(false);
                            dialog.create();
                            dialog.show();
                        } else {
                            Toast.makeText(ShelvesOutActivity.this, "完成下架失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (Exception e) {
                    tvComplete.setEnabled(false);
                    tvComplete.setBackgroundColor(color.gray);
                    Toast.makeText(ShelvesOutActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 快速下架方法
     */
    private void quickShelvesOut() {
        RequestVo vo = new RequestVo();
        vo.requestUrl = quickShelvesOut;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("stockNo", stockNo);
        map.put("departmentId", departmentId);
        map.put("type", type);
        vo.requestDataMap = map;
        tvQuickComplete.setEnabled(false);
        tvQuickComplete.setBackgroundColor(Color.GRAY);
        super.getDataFromServer(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    if (retObj.getBoolean("success")) {
                        AlertDialog.Builder dialog = new AlertDialog.Builder(ShelvesOutActivity.this, AlertDialog.THEME_HOLO_LIGHT);
                        dialog.setTitle("提示");
                        dialog.setMessage("出仓单 " + stockNo + " 已完成快速下架");
                        // 相当于点击确认按钮
                        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                complete();
                            }
                        });
                        dialog.setCancelable(false);
                        dialog.create();
                        dialog.show();
                    } else {
                        Toast.makeText(ShelvesOutActivity.this, "快速下架操作失败", Toast.LENGTH_SHORT).show();
                        tvQuickComplete.setEnabled(true);
                        tvQuickComplete.setBackground(ShelvesOutActivity.this.getResources().getDrawable(R.drawable.upload_button));
                    }
                } catch (Exception e) {
                    tvComplete.setEnabled(false);
                    tvComplete.setBackgroundColor(color.gray);
                    Toast.makeText(ShelvesOutActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 单个货品下架
     * 
     * @param index
     * @param tempId
     * @param storageId
     * @param goodsId
     * @param colorId
     * @param sizeId
     * @param qty
     * @param total
     */
    private void shelvesOut(final int index, String tempId, String storageId, String goodsId, String colorId, String sizeId, final int qty, final int total) {
        RequestVo vo = new RequestVo();
        vo.requestUrl = SHELVESOUT;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("departmentId", departmentId);
        map.put("type", type);
        map.put("tempId", tempId);
        map.put("storageId", storageId);
        map.put("goodsId", goodsId);
        map.put("colorId", colorId);
        map.put("sizeId", sizeId);
        map.put("stockId", stockId);
        map.put("stockNo", stockNo);
        map.put("qty", String.valueOf(qty));
        vo.requestDataMap = map;
        super.getDataFromServer(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    int count = retObj.getInt("obj");
                    if (retObj.getBoolean("success") && count > 0) {
                        Toast.makeText(ShelvesOutActivity.this, "货品下架成功", Toast.LENGTH_SHORT).show();
                        if (qty == total) {
                            dataList.remove(index);
                        }
                        // 刷新dataList
                        adapter.refresh(dataList);
                    } else {
                        Toast.makeText(ShelvesOutActivity.this, "货品下架失败", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(ShelvesOutActivity.this, AlertDialog.THEME_HOLO_LIGHT);
                    dialog.setTitle("提示");
                    dialog.setMessage("系统错误,货品下架失败,请稍后重试");
                    // 相当于点击确认按钮
                    dialog.setPositiveButton("点击返回", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
                    dialog.setCancelable(false);
                    dialog.create();
                    dialog.show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    protected void loadViewLayout() {
        setContentView(R.layout.activity_shelves_out);
        setTitle("货 品 出 库 下 架 列 表");
    }

    @Override
    protected void setListener() {
        // ListView设置
        tvComplete.setOnClickListener(this);
        tvQuickComplete.setOnClickListener(this);
        adapter = new ShelvesOutAdspter(this, dataList, "下架");
        lvDatas.setAdapter(adapter);
        lvDatas.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        // RefreshListView listView = (RefreshListView)parent;
        // HashMap<String, String> map = (HashMap<String,
        // String>)listView.getItemAtPosition(position);
        Zujian tag = (Zujian) view.getTag();
        TextView tvso = tag.shelves_out;
        tvso.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                Map map = dataList.get(position);
                final String tempId = (String) map.get("TempID");
                final String storageId = (String) map.get("StorageID");
                final String goodsId = (String) map.get("GoodsID");
                final String colorId = (String) map.get("ColorID");
                final String sizeId = (String) map.get("SizeID");
                String storage = (String) map.get("Storage");
                String goodsName = (String) map.get("Name");
                String supplierCode = (String) map.get("SupplierCode");
                String color = (String) map.get("Color");
                String size = (String) map.get("Size");
                final String qty = (String) map.get("Quantity");
                View v = (LinearLayout) getLayoutInflater().inflate(R.layout.menu_shelves_out, null);
                // 初始化
                EditText etGoods = (EditText) v.findViewById(R.id.goods);
                EditText etSupplierCode = (EditText) v.findViewById(R.id.supplierCode);
                EditText etColor = (EditText) v.findViewById(R.id.color);
                EditText etSize = (EditText) v.findViewById(R.id.size);
                etStorage = (EditText) v.findViewById(R.id.storage);
                final EditText etQty = (EditText) v.findViewById(R.id.qty);
                // 其它绑定操作
                etStorage.setOnTouchListener(new TouchListener());
                // 显示数据
                etSupplierCode.setText(supplierCode);
                etGoods.setText(goodsName);
                etColor.setText(color);
                etSize.setText(size);
                etStorage.setText(storage);
                etQty.setText(qty);
                // 操作提示
                Builder dialog = new AlertDialog.Builder(ShelvesOutActivity.this, AlertDialog.THEME_HOLO_LIGHT);
                dialog.setCancelable(false);
                dialog.setMessage("确认货品下架信息并出库?");
                dialog.setView(v);
                // 相当于点击确认按钮
                dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String storage = etStorage.getText().toString().trim();
                        String qtyStr = etQty.getText().toString().trim();
                        if (storage.isEmpty() || qtyStr.isEmpty()) {
                            Toast.makeText(ShelvesOutActivity.this, "货架或数量不能为空,下架失败", Toast.LENGTH_SHORT).show();
                        } else {
                            int num = Integer.parseInt(qtyStr);
                            if (num == 0) {
                                num = 1;
                            }
                            if ((null == newStorage || newStorage.isEmpty()) && num > Integer.parseInt(qty)) {
                                Toast.makeText(ShelvesOutActivity.this, "修改数量不能大于此仓位的货品数量,下架失败", Toast.LENGTH_LONG).show();
                                return;
                            }
                            if (num < Integer.parseInt(qty)) {
                                Map map = dataList.get(position);
                                map.put("Quantity", String.valueOf(Integer.parseInt(qty) - num));
                            }
                            // 货品下架操作
                            if (storageOnTouch && null != newStorage && !newStorage.isEmpty()) {// 修改了仓位
                                shelvesOut((position), tempId, newStorageId, goodsId, colorId, sizeId, num, Integer.parseInt(qty));
                            } else {
                                shelvesOut((position), tempId, storageId, goodsId, colorId, sizeId, num, Integer.parseInt(qty));
                            }
                        }
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
            }
        });
    }

    /**
     * 关联单号时获取对应单号的数据
     */
    private void addData() {
        RequestVo vo = new RequestVo();
        vo.requestUrl = StockDetail;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("stockNo", stockNo);
        vo.requestDataMap = map;
        super.getDataFromServer(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    if (retObj.getBoolean("success")) {
                        getData();
                    }
                } catch (Exception e) {
                    Toast.makeText(ShelvesOutActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    private class TouchListener implements OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (v.getId()) {
                case R.id.storage:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        storageOnTouch = true;
                        Intent intent = new Intent(ShelvesOutActivity.this, SelectActivity.class);
                        intent.putExtra("selectType", "selectStorageByInner");
                        intent.putExtra("param", departmentId);
                        startActivityForResult(intent, R.id.storage);
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
            case R.id.storage:
                if (resultCode == 1) {
                    storageOnTouch = true;
                    newStorage = data.getStringExtra("Name");
                    newStorageId = data.getStringExtra("StorageID");
                    // 修改了仓位
                    etStorage.setText(newStorage);
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void processLogic() {
        Bundle bundle = this.getIntent().getExtras();
        if (null != bundle) {
            stockId = bundle.getString("stockId");
            stockNo = bundle.getString("stockNo");
            departmentId = bundle.getString("departmentId");
            type = bundle.getString("type");
            etStockOutNo.setText(stockNo);
        }
        // 加载数据
        addData();
    }

    @Override
    protected void findViewById() {
        lvDatas = (ListView) findViewById(R.id.lv_datas);
        etStockOutNo = (EditText) findViewById(R.id.stock_out_no);
        tvComplete = (TextView) findViewById(R.id.complete);
        tvQuickComplete = (TextView) findViewById(R.id.quickComplete);
    }

}
