package com.fuxi.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.R.color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.fuxi.adspter.InvoicingDistributionAdspter;
import com.fuxi.main.R;
import com.fuxi.util.Logger;
import com.fuxi.util.LoginParameterUtil;
import com.fuxi.vo.RequestVo;

/**
 * Title: InvoicingDistributionActivity Description: 库存分布活动界面
 * 
 * @author LYJ
 * 
 */
public class InvoicingDistributionActivity extends BaseWapperActivity {

    private static final String TAG = "InvoicingDistributionActivity";
    private static String queryPath = "/invoicing.do?queryDistribution";

    private ListView lvDatas; // ListView
    private TextView tvGoodsName;

    private List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
    private List<String> colorList = new ArrayList<String>();
    private InvoicingDistributionAdspter adapter;

    private String goodsId;
    private String colorId;
    private String sizeId;
    private String departmentId;
    private boolean preciseQueryStock;

    @Override
    public void onClick(View v) {}

    /**
     * 店铺货品库存明细信息
     */
    private void query() {
        RequestVo vo = new RequestVo();
        vo.requestUrl = queryPath;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("goodsId", goodsId);
        map.put("colorId", colorId);
        map.put("sizeId", sizeId);
        map.put("preciseQueryStock", String.valueOf(preciseQueryStock));
        map.put("departmentId", departmentId);
        vo.requestDataMap = map;
        super.getDataFromServer(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj.getBoolean("success")) {
                        JSONObject rs = retObj.getJSONObject("attributes");
                        JSONArray jsonArray = rs.getJSONArray("dataList");
                        JSONArray colorArray = rs.getJSONArray("colorList");
                        for (int i = 0; i < colorArray.length(); i++) {
                            JSONObject json = colorArray.getJSONObject(i);
                            String colorId = json.getString("ColorID");
                            colorList.add(colorId);
                        }
                        for (int i = 0; i < jsonArray.length(); i++) {
                            Map<String, Object> temp = new HashMap<String, Object>();
                            JSONObject json = jsonArray.getJSONObject(i);
                            Iterator ite = json.keys();
                            while (ite.hasNext()) {
                                String key = ite.next().toString();
                                String value = json.getString(key);
                                temp.put(key, value);
                            }
                            dataList.add(temp);
                        }
                        // 按颜色分类进行小计
                        subtotalDataList();
                        adapter.refresh(dataList);
                    } else {
                        Toast.makeText(InvoicingDistributionActivity.this, "此部门暂无货品库存明细信息", Toast.LENGTH_LONG).show();
                        Logger.e(TAG, "此部门暂无货品库存明细信息");
                    }
                } catch (JSONException e) {
                    Toast.makeText(InvoicingDistributionActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }

        });
    }

    /**
     * 按颜色分类进行小计
     */
    private void subtotalDataList() {
        List<Map<String, Object>> tempList = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < colorList.size(); i++) {
            String colorId = colorList.get(i);
            int sum = 0;
            for (int j = 0; j < dataList.size(); j++) {
                Map<String, Object> map = dataList.get(j);
                String tColorId = (String) map.get("ColorID");
                if (colorId.equals(tColorId)) {
                    int qty = Integer.parseInt(String.valueOf(map.get("Quantity")));
                    sum += qty;
                    tempList.add(map);
                    dataList.remove(map);
                    j--;
                }
            }
            Map<String, Object> temp = new HashMap<String, Object>();
            temp.put("Color", "小计");
            temp.put("Size", "");
            temp.put("Quantity", String.valueOf(sum));
            tempList.add(temp);
        }
        dataList.clear();
        dataList.addAll(tempList);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    protected void setListener() {
        adapter = new InvoicingDistributionAdspter(this, dataList);
        lvDatas.setAdapter(adapter);
    }

    @Override
    protected void loadViewLayout() {
        setContentView(R.layout.activity_invoicing_distribution);
        setTitle("部门货品库存明细");
    }

    @Override
    protected void processLogic() {
        preciseQueryStock = LoginParameterUtil.preciseToQueryStock;
        Bundle bundle = this.getIntent().getExtras();
        if (null != bundle) {
            tvGoodsName.setText(bundle.getString("goodsName"));
            goodsId = bundle.getString("goodsId");
            colorId = bundle.getString("colorId");
            sizeId = bundle.getString("sizeId");
            departmentId = bundle.getString("departmentId");
            query();
        }
    }

    @Override
    protected void findViewById() {
        tvGoodsName = (TextView) findViewById(R.id.goods_name);
        lvDatas = (ListView) findViewById(R.id.lv_datas);
    }

    // 重置
    @Override
    protected void onHeadRightButton(View v) {}

}
