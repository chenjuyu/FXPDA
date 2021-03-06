package com.fuxi.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.TabLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.fuxi.adspter.VipDetailAdapter;
import com.fuxi.main.R;
import com.fuxi.util.CircularImageView;
import com.fuxi.util.Logger;
import com.fuxi.vo.RequestVo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;


public class VipPointCzActivity extends BaseWapperActivity {

    private static final String TAG="VipPointCzActivity";

    private static final  String url="/select.do?VipDetail";

    private ImageView PhotoUrl;
    private TextView tvname;
    private TextView tvpoint;
    private TextView tvviptype;
    private TextView tvcz;
    private TextView tvamount;
    private TextView tvcount;
    private TextView tvdate;
    private TextView tvdepartment;
    private TextView tvprice;
    private TextView tvjprice;
    private TextView tvldlv;


    private ListView listView;
   // private TabHost tabHost;
    private  String flag,vipId;//1.消费,3表示积分，2表示储值

    private VipDetailAdapter adapter;
    private  List<Map<String,Object>> dataList=new ArrayList<Map<String,Object>>();
    private  List<Map<String,Object>> dataList2=new ArrayList<Map<String,Object>>();
    private  List<Map<String,Object>> dataList3=new ArrayList<Map<String,Object>>();
    @Override
    protected void loadViewLayout() {
     setContentView(R.layout.activity_vip_person);
    }

    @Override
    protected void setListener() {

    }



    @Override
    protected void processLogic() {
        super.sethead_layout();//改变颜色
        Bundle bundle = this.getIntent().getExtras();
        if(bundle !=null){
           // flag = bundle.getString("flag");
            vipId =bundle.getString("vipId");
        }

      getData();
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }


    @Override
    protected void findViewById() {
        PhotoUrl =(ImageView) findViewById(R.id.iv_photo);
        tvname =(TextView) findViewById(R.id.tv_name);
        tvviptype =(TextView) findViewById(R.id.tv_viptype);
        tvpoint =(TextView) findViewById(R.id.tv_point);
        tvcz =(TextView) findViewById(R.id.tv_cz);
        tvamount =(TextView) findViewById(R.id.tv_amount);
        tvcount =(TextView) findViewById(R.id.tv_count);
        tvdate =(TextView) findViewById(R.id.tv_date);
        tvdepartment =(TextView) findViewById(R.id.tv_department);
        tvprice =(TextView) findViewById(R.id.tv_price);
        tvjprice =(TextView) findViewById(R.id.tv_jprice);
        tvldlv =(TextView) findViewById(R.id.tv_ldlv);

        listView =(ListView) findViewById(R.id.lv_datas);

      /*  for(int i=0;i<30;i++){
            Map<String,Object> map =new HashMap<>();
            map.put("No","SM0000001");
            map.put("Department","广州店");
            map.put("Date","2020-01-11");
            map.put("ThisSalesPoint","20000");
            map.put("DepositAmount","200.00");
            map.put("Memo","测试备注测试备注");
            dataList.add(map);
        } */




        adapter =new VipDetailAdapter(this,dataList,1);
        listView.setAdapter(adapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        ArrayList<String> tabList = new ArrayList<String>();
       // for (int i = 0; i < 3; i++) {
        //    tabList.add("选项" + i);
       // }
          tabList.add("交易记录");
          tabList.add("储值记录");
          tabList.add("积分明细");
        for (int i = 0; i < tabList.size(); i++) {
            tabLayout.addTab(tabLayout.newTab().setText(tabList.get(i)));
        }//TabLayout.MODE_SCROLLABLE
        tabLayout.setTabMode(TabLayout.MODE_FIXED);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                //Toast.makeText(VipPointCzActivity.this,String.valueOf(tab.getPosition()),Toast.LENGTH_SHORT).show();
                if(tab.getPosition()==0){
                  adapter =new VipDetailAdapter(VipPointCzActivity.this,dataList,1);
                  listView.setAdapter(adapter);
                }else if(tab.getPosition()==1){

                    if(dataList2.size()==0){
                        Toast.makeText(VipPointCzActivity.this,"暂无储值记录",Toast.LENGTH_SHORT).show();
                        listView.setAdapter(null);
                    }else {
                        adapter =new VipDetailAdapter(VipPointCzActivity.this,dataList2,2);
                        listView.setAdapter(adapter);
                    }
                }else if(tab.getPosition()==2){
                    if(dataList3.size()==0){
                        Toast.makeText(VipPointCzActivity.this,"暂无积分记录",Toast.LENGTH_SHORT).show();
                        listView.setAdapter(null);
                    }else {
                        adapter =new VipDetailAdapter(VipPointCzActivity.this,dataList3,3);
                        listView.setAdapter(adapter);
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
               // Toast.makeText(VipPointCzActivity.this,"第二点击的："+String.valueOf(tab.getPosition()),Toast.LENGTH_SHORT).show();

            }
        });


    /*
        tabHost=(TabHost)findViewById(android.R.id.tabhost);//获取tabHost对象
        tabHost.setup();//初始化TabHost组件

        LayoutInflater inflater=LayoutInflater.from(this);//声明并实例化一个LayoutInflater对象
        //关于LayoutInflater详细，请看我的另外一篇转载的总结
        inflater.inflate(R.layout.tab1, tabHost.getTabContentView());
        inflater.inflate(R.layout.tab2, tabHost.getTabContentView());
        inflater.inflate(R.layout.tab3, tabHost.getTabContentView());

        tabHost.addTab(tabHost.newTabSpec("tab01")
                .setIndicator("标签页一")
                .setContent(R.id.linearLayout1));//添加第一个标签页
        tabHost.addTab(tabHost.newTabSpec("tab02")
                .setIndicator("标签页二")
                .setContent(R.id.linearLayout2));//添加第二个标签页
        tabHost.addTab(tabHost.newTabSpec("tab03")
                .setIndicator("标签页三")
                .setContent(R.id.linearLayout3));//添加第三个标签页
        tabHost.setCurrentTab(0);   //防止重复点击 产生错误

        tabHost.getTabWidget().getChildAt(0).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               // tabHost.setCurrentTab(0);
                Log.i(TAG,"当前索引值:"+String.valueOf(tabHost.getCurrentTab()));
            }
        });
        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                Log.i(TAG,"当前tabId索引值:"+tabId);
                Log.i(TAG,"当前索引值:"+String.valueOf(tabHost.getCurrentTab()));
            }
        });
     */
    }


    public  void getData(){
        RequestVo vo=new RequestVo();
        vo.requestUrl=url;
        vo.context =this;
        HashMap map=new HashMap();
        map.put("VipID",vipId);
        vo.requestDataMap=map;
        super.getDataFromServer(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if(retObj ==null){
                        return;
                    }
                    if (retObj.getBoolean("success")) {
                        JSONObject attributes =retObj.getJSONObject("attributes");
                        if(attributes !=null){
                        JSONObject vipinf= attributes.getJSONObject("vipinf");
                          tvname.setText(vipinf.getString("Name"));
                          tvviptype.setText(vipinf.getString("VipType"));
                          tvpoint.setText(vipinf.getString("UsablePoint"));
                          tvcz.setText(vipinf.getString("UsableDepositAmount"));
                          tvamount.setText(attributes.getString("Amt"));
                          tvcount.setText(attributes.getString("CountNo"));
                          tvdate.setText(attributes.getString("Date"));
                          tvdepartment.setText(attributes.getString("salesDepartment"));
                          tvprice.setText(attributes.getString("price"));
                          tvjprice.setText(attributes.getString("jprice"));
                          tvldlv.setText(attributes.getString("ldlv"));
                          if(vipinf.get("PhotoUrl") !=null && !"null".equals(String.valueOf(vipinf.get("PhotoUrl"))) && !"".equals(vipinf.get("PhotoUrl"))){
                              //或者是通过glide框架
                              Glide.with(VipPointCzActivity.this).load(String.valueOf(vipinf.get("PhotoUrl")))
                                      .into(PhotoUrl);
                          }

                          JSONArray array  =attributes.getJSONArray("posRecord");

                          if(array.length() >0){
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

                          }
                            JSONArray array2  =attributes.getJSONArray("czRecord");
                        if(array2.length()>0){
                            for (int i = 0; i < array2.length(); i++) {
                                Map temp2 = new HashMap();
                                JSONObject json = array2.getJSONObject(i);
                                Iterator ite = json.keys();
                                while (ite.hasNext()) {
                                    String key = ite.next().toString();
                                    String value = json.getString(key);
                                    temp2.put(key, value);
                                }
                                dataList2.add(temp2);
                            }

                        }

                            JSONArray array3  =attributes.getJSONArray("jfRecord");
                            if(array3.length()>0){
                                for (int i = 0; i < array3.length(); i++) {
                                    Map temp3 = new HashMap();
                                    JSONObject json = array3.getJSONObject(i);
                                    Iterator ite = json.keys();
                                    while (ite.hasNext()) {
                                        String key = ite.next().toString();
                                        String value = json.getString(key);
                                        temp3.put(key, value);
                                    }
                                    dataList3.add(temp3);
                                }

                            }


                            if(dataList.size()>0) {
                                adapter.refresh(dataList);
                            }else{
                                Toast.makeText(VipPointCzActivity.this,"暂无小票记录",Toast.LENGTH_SHORT).show();
                            }



                        }


                    }else {
                        Toast.makeText(VipPointCzActivity.this, "数据返回异常", Toast.LENGTH_LONG).show();
                    }
                }catch (Exception e){
                    Toast.makeText(VipPointCzActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());

                }
            }
        });


    }



}
