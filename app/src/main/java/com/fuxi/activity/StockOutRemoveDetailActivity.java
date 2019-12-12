package com.fuxi.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.fuxi.adspter.ShelvesOutAdspter;
import com.fuxi.adspter.ShelvesOutAdspter.Zujian;
import com.fuxi.dao.StockRemoveDao;
import com.fuxi.main.R;
import com.fuxi.util.Logger;
import com.fuxi.vo.StockRemove;

/**
 * Title: StockOutRemoveDetailActivity Description: 货架移位活动界面(仓位管理) 移出下架,移进上架
 * 
 * @author LYJ
 * 
 */
public class StockOutRemoveDetailActivity extends BaseWapperActivity implements OnItemClickListener {

    private static final String TAG = "StockOutRemoveDetailActivity";

    private RelativeLayout rlMain;
    private LinearLayout llSon;
    private LinearLayout llBottom;
    private LinearLayout llSplit;
    private ListView lvDatas; // ListView
    private TextView tvComplete;
    private TextView tvQuickComplete;
    private TextView tvStorage;

    private ShelvesOutAdspter adapter;
    private StockRemoveDao stockRemoveDao = new StockRemoveDao(this);

    private List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();

    private String type;

    @Override
    public void onClick(View v) {}

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    protected void loadViewLayout() {
        setContentView(R.layout.activity_shelves_out);
        setTitle("移 进 上 架 列 表");
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        Zujian tag = (Zujian) view.getTag();
        TextView tvso = tag.shelves_out;
        tvso.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Map map = dataList.get(position);
                    String goodsId = (String) map.get("goodsId");
                    String colorId = (String) map.get("colorId");
                    String sizeId = (String) map.get("sizeId");
                    String barcode = (String) map.get("Barcode");
                    String supplierCode = (String) map.get("SupplierCode");
                    String goodsName = (String) map.get("Name");
                    String goodsCode = (String) map.get("Code");
                    String color = (String) map.get("Color");
                    String size = (String) map.get("Size");
                    String qty = (String) map.get("Quantity");
                    String deptId = (String) map.get("deptId");
                    String deptName = (String) map.get("deptName");
                    String storageId = (String) map.get("storageId");
                    // 初始化
                    Intent intent = new Intent(StockOutRemoveDetailActivity.this, MoveShelvesActivity.class);
                    intent.putExtra("type", "移入");
                    intent.putExtra("goodsId", goodsId);
                    intent.putExtra("colorId", colorId);
                    intent.putExtra("sizeId", sizeId);
                    intent.putExtra("barcode", barcode);
                    intent.putExtra("SupplierCode", supplierCode);
                    intent.putExtra("goodsName", goodsName);
                    intent.putExtra("goodsCode", goodsCode);
                    intent.putExtra("deptId", deptId);
                    intent.putExtra("deptName", deptName);
                    intent.putExtra("storageId", storageId);
                    intent.putExtra("color", color);
                    intent.putExtra("size", size);
                    intent.putExtra("qty", qty);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    Toast.makeText(StockOutRemoveDetailActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    @Override
    protected void onHeadLeftButton(View v) {
        back();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            back();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 点击返回时的操作
     */
    private void back() {
        Intent intent = new Intent(StockOutRemoveDetailActivity.this, MoveShelvesActivity.class);
        intent.putExtra("type", "移出");
        startActivity(intent);
        finish();
    }

    @Override
    protected void processLogic() {
        Bundle bundle = this.getIntent().getExtras();
        if (null != bundle) {
            type = bundle.getString("type");
        }
        // 加载数据
        List<StockRemove> list = stockRemoveDao.getList();
        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            StockRemove sr = list.get(i);
            map.put("Barcode", sr.getBarcode());
            map.put("SupplierCode", sr.getSupplierCode());
            map.put("Code", sr.getGoodsCode());
            map.put("Name", sr.getGoodsName());
            map.put("Color", sr.getColor());
            map.put("Size", sr.getSize());
            map.put("Storage", sr.getStorage());
            map.put("storageId", sr.getStorageId());
            map.put("goodsId", sr.getGoodsId());
            map.put("colorId", sr.getColorId());
            map.put("sizeId", sr.getSizeId());
            map.put("Quantity", String.valueOf(sr.getQuantity()));
            map.put("deptId", sr.getDeptId());
            map.put("deptName", sr.getDeptName());
            dataList.add(map);
        }
        adapter.refresh(dataList);
        // 其它处理
        tvStorage.setText("下架仓位");
        rlMain.removeView(llSon);
        rlMain.removeView(llSplit);
        rlMain.removeView(llBottom);
        tvComplete.setVisibility(View.GONE);
        tvQuickComplete.setVisibility(View.GONE);
        if (list.size() < 1) {
            Intent intent = new Intent(StockOutRemoveDetailActivity.this, PositionManagementActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void setListener() {
        // ListView设置
        adapter = new ShelvesOutAdspter(this, dataList, "上架");
        lvDatas.setAdapter(adapter);
        lvDatas.setOnItemClickListener(this);
    }

    @Override
    protected void findViewById() {
        rlMain = (RelativeLayout) findViewById(R.id.main);
        llBottom = (LinearLayout) findViewById(R.id.ll_bottom);
        llSon = (LinearLayout) findViewById(R.id.ll_stock_out_no);
        llSplit = (LinearLayout) findViewById(R.id.split2);
        lvDatas = (ListView) findViewById(R.id.lv_datas);
        tvComplete = (TextView) findViewById(R.id.complete);
        tvQuickComplete = (TextView) findViewById(R.id.quickComplete);
        tvStorage = (TextView) findViewById(R.id.storage);
    }

}
