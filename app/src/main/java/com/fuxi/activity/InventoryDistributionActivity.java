package com.fuxi.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;
import com.fuxi.adspter.InventoryDistributionAdspter;
import com.fuxi.main.R;
import com.fuxi.util.Logger;
import com.fuxi.vo.RequestVo;
import com.fuxi.widget.RefreshListView;

/**
 * Title: InventoryDistributionActivity Description: 库存分布活动界面
 * 
 * @author LYJ
 * 
 */
public class InventoryDistributionActivity extends BaseWapperActivity {

    private static final String TAG = "InventoryDistributionActivity";
    private static String queryPath = "/inventoryQuery.do?selectDistribution";

    private List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
    private ListView lvDatas; // 刷新ListView
    private InventoryDistributionAdspter adapter;

    private String goodsId;
    private String colorId;
    private String sizeId;

    @Override
    public void onClick(View v) {}

    /**
     * 库存分布查询方法
     * 
     * @param refresh
     * @param type
     */
    private void query() {
        RequestVo vo = new RequestVo();
        vo.requestUrl = queryPath;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("goodsId", String.valueOf(goodsId));
        map.put("colorId", String.valueOf(colorId));
        map.put("sizeId", String.valueOf(sizeId));
        vo.requestDataMap = map;
        super.getDataFromServerNoProcess(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj.getBoolean("success")) {
                        dataList.clear();
                        JSONArray array = retObj.getJSONArray("obj");
                        if (array.length() < 1) {
                            Toast.makeText(InventoryDistributionActivity.this, "没有该货品的库存分布信息", Toast.LENGTH_LONG).show();
                        } else {
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
                            adapter.refresh(dataList);
                        }
                    } else {
                        Toast.makeText(InventoryDistributionActivity.this, "没有该货品的库存分布信息", Toast.LENGTH_LONG).show();
                        Logger.e(TAG, "查询记录为空");
                    }
                } catch (JSONException e) {
                    Toast.makeText(InventoryDistributionActivity.this, "系统错误", Toast.LENGTH_LONG).show();
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
    protected void setListener() {
        adapter = new InventoryDistributionAdspter(this, dataList);
        lvDatas.setAdapter(adapter);
    }

    @Override
    protected void loadViewLayout() {
        setContentView(R.layout.activity_inventory_distribution);
        setTitle("库存查询");
    }

    @Override
    protected void processLogic() {
        Bundle bundle = this.getIntent().getExtras();
        if (null != bundle) {
            // 根据StocktakingID显示具体明细
            goodsId = bundle.getString("goodsId");
            colorId = bundle.getString("colorId");
            sizeId = bundle.getString("sizeId");
            query();
        }
    }

    @Override
    protected void findViewById() {
        lvDatas = (ListView) findViewById(R.id.lv_datas);
    }

    // 重置
    @Override
    protected void onHeadRightButton(View v) {}

}
