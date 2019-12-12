package com.fuxi.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ListView;
import com.fuxi.adspter.NegativeStockAdapter;
import com.fuxi.main.R;

/**
 * Title: GoodsBoxBarcodeActivity Description: 箱条码显示活动界面
 * 
 * @author LYJ
 * 
 */
public class NegativeStockActivity extends BaseWapperActivity {

    private static final String TAG = "NegativeStockActivity";

    private ListView lv_datas;

    private NegativeStockAdapter adapter;
    private List<HashMap<String, Object>> tempDatas;
    private List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();

    @Override
    public void onClick(View v) {}

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    protected void loadViewLayout() {
        setContentView(R.layout.activity_negative_stock);
        setTitle("负库存货品明细");
    }

    /**
     * 获取箱条码显示的数据
     * 
     * @param datas
     */
    private void getData(List<HashMap<String, Object>> datas) {
        for (int i = 0; i < datas.size(); i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            HashMap<String, Object> hashMap = datas.get(i);
            map.put("GoodsCode", hashMap.get("GoodsCode"));
            map.put("Color", hashMap.get("Color"));
            map.put("Size", hashMap.get("Size"));
            map.put("Quantity", hashMap.get("Quantity"));
            map.put("Qty", hashMap.get("StockQty"));
            dataList.add(map);
        }
    }

    /**
     * 点击返回时
     */
    private void back() {
        finish();
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

    @SuppressWarnings("unchecked")
    @Override
    protected void processLogic() {
        Bundle bundle = this.getIntent().getExtras();
        if (null != bundle) {
            tempDatas = (ArrayList<HashMap<String, Object>>) getIntent().getSerializableExtra("tempDatas");
        }
        // 清空集合
        dataList.clear();
        getData(tempDatas);
        // 显示获取到的数据
        adapter.refresh();
        // 弹出操作提示框
        showPromptMsg();
    }

    private void showPromptMsg() {
        Builder dialog = new AlertDialog.Builder(NegativeStockActivity.this, AlertDialog.THEME_HOLO_LIGHT);
        dialog.setTitle("提示");
        dialog.setMessage("请先处理单据中存在负库存的货品记录，再重新保存单据");
        // 相当于点击确认按钮
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {}
        });
        dialog.setCancelable(false);
        dialog.create();
        dialog.show();
    }

    @Override
    protected void setListener() {
        adapter = new NegativeStockAdapter(this, dataList);
        lv_datas.setAdapter(adapter);
    }

    @Override
    protected void findViewById() {
        lv_datas = (ListView) findViewById(R.id.lv_datas);
    }

}
