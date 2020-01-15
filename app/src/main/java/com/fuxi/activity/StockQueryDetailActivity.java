package com.fuxi.activity;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.fuxi.adspter.StockQueryDetailAdapter;
import com.fuxi.main.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StockQueryDetailActivity extends BaseWapperActivity implements AdapterView.OnItemClickListener {

    String TAG ="StockQueryDetailActivity";

    private ListView lvData;
    private String goodsid="";
    private List<Map<String,Object>> dataList=new ArrayList<>();
    private StockQueryDetailAdapter adapter;
    private TabLayout tabLayout;

    @Override
    protected void loadViewLayout() {
        setContentView(R.layout.activity_stock_detail);
        setTitle("库存分布");
    }

    @Override
    protected void setListener() {

    }

    @Override
    protected void processLogic() {
        Bundle bundle =this.getIntent().getExtras();
        goodsid =bundle.getString("GoodsID");
        String Code=bundle.getString("Code");
        setTitle(Code+"的库存分布");

        List<String> sizetitle =new ArrayList<>();
        sizetitle.add("M");
        sizetitle.add("S");
        sizetitle.add("L");
        sizetitle.add("L");
        sizetitle.add("L");
        sizetitle.add("L");
        sizetitle.add("L");
        sizetitle.add("L");
        sizetitle.add("L");
        sizetitle.add("L");
        sizetitle.add("L");


        List<Integer> sizeqty =new ArrayList<>();
        sizeqty.add(1);
        sizeqty.add(1);
        sizeqty.add(1);
        sizeqty.add(1);
        sizeqty.add(1);
        sizeqty.add(1);
        sizeqty.add(1);
        sizeqty.add(1);
        sizeqty.add(1);
        sizeqty.add(1);
        sizeqty.add(1);

        for(int i=0;i<20;i++){
         Map<String,Object> map =new HashMap<>();
         map.put("Department","测试部门"+i);
         map.put("Color","颜色"+i);
         map.put("Quantity",i);
         map.put("ColorQty",i);
         map.put("sizetitle",sizetitle);
         map.put("sizeqty",sizeqty);
        dataList.add(map);
        }
        Log.v(TAG,"测试数据："+dataList.toString());
        adapter =new StockQueryDetailAdapter(this,dataList);
        lvData.setAdapter(adapter);


    }

    @Override
    protected void findViewById() {
        lvData=(ListView)findViewById(R.id.lv_datas);
        tabLayout =(TabLayout) findViewById(R.id.tabLayout);

        ArrayList<String> tabList = new ArrayList<String>();
        // for (int i = 0; i < 3; i++) {
        //    tabList.add("选项" + i);
        // }
        tabList.add("当前库存");
        tabList.add("可用库存");
        tabList.add("待出库");
        tabList.add("待入库");
        for (int i = 0; i < tabList.size(); i++) {
            tabLayout.addTab(tabLayout.newTab().setText(tabList.get(i)));
        }//TabLayout.MODE_SCROLLABLE
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
               if(tab.getPosition()==0){//当前库存

               }else if(tab.getPosition()==1){//可用库存

               }else if(tab.getPosition()==2){//待出库

               }else if(tab.getPosition()==3){//待入库

               }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                //这里是多次点击的

            }
        });
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ListView listView =(ListView)parent;
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }
}


