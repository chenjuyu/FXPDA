package com.fuxi.activity;

import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.fuxi.adspter.PosReportAdapter;
import com.fuxi.adspter.PosReportGroupAdapter;
import com.fuxi.adspter.VipAdapter;
import com.fuxi.main.R;
import com.fuxi.util.Logger;
import com.fuxi.vo.RequestVo;
import com.fuxi.widget.OnRefreshListener;
import com.fuxi.widget.RefreshListView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class VipActivity extends BaseWapperActivity implements OnRefreshListener, AdapterView.OnItemClickListener {

    private static final  String TAG="VipActivity";
    private  String url="/select.do?getVip";
    String keyword="";
    private EditText param;
    private RefreshListView lvData;
    private int currPage = 1;
    private VipAdapter adapter;
    private boolean refresh;
    private  TxtWatcher tw=new TxtWatcher();

    private List<Map<String,Object>> dataList=new ArrayList<>();
    @Override
    protected void loadViewLayout() {
     setContentView(R.layout.activity_vip);
     setTitle("VIP查询");
    }

    @Override
    protected void setListener() {
        lvData.setOnRefreshListener(this);
        lvData.setOnItemClickListener(this);

        adapter =new VipAdapter(this,dataList);
        lvData.setAdapter(adapter);
        param.addTextChangedListener(tw);
    }

    @Override
    protected void processLogic() {
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
        param =(EditText) findViewById(R.id.param);
        lvData =(RefreshListView) findViewById(R.id.lv_datas);
    }

    @Override
    public void onDownPullRefresh() {
        currPage=1;
        refresh=true;
        getData();
    }

    @Override
    public void onLoadingMore() {
        currPage ++;
        refresh=false;
        getData();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        RefreshListView listView =(RefreshListView)parent;
        VipAdapter.Zujian tag =(VipAdapter.Zujian)view.getTag();
        Map<String,Object> map=(Map<String,Object>)  listView.getItemAtPosition(position);
        Intent in=new Intent(VipActivity.this,VipPointCzActivity.class);
        in.putExtra("vipId",String.valueOf(map.get("VIPID")));
        startActivityForResult(in,R.id.tv_code);
    }

   class TxtWatcher implements  TextWatcher{

       @Override
       public void beforeTextChanged(CharSequence s, int start, int count, int after) {

       }

       @Override
       public void onTextChanged(CharSequence s, int start, int before, int count) {

       }

       @Override
       public void afterTextChanged(Editable s) {

           keyword=String.valueOf(param.getText());
           currPage =1;
           getData();


       }
   }


    public void getData(){
        RequestVo vo = new RequestVo();
        vo.requestUrl = url;
        vo.context = this;

       if(String.valueOf(param.getText()).equals("") && param.getText()!=null)
       {
           keyword =String.valueOf(param.getText());
       }

        HashMap map = new HashMap();
        map.put("currPage",String.valueOf(currPage));
        map.put("param",keyword);
        vo.requestDataMap =map;
        super.getDataFromServer(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try{
                    if (retObj == null) {
                        return;
                    }
                    if (retObj.getBoolean("success")) {
                        //多次查询时
                        if (VipActivity.this.refresh) {
                            dataList.clear();
                        }

                        JSONArray array = retObj.getJSONArray("obj");
                        Log.v(TAG,"数据："+array.toString());
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

                        if (array.length() == 0) {
                            Toast.makeText(VipActivity.this, "已经到达最后一页", Toast.LENGTH_SHORT).show();
                        }

                        if(refresh){
                            lvData.hideHeaderView();
                        }else{
                            lvData.hideFooterView();
                        }

                        adapter.refresh(dataList);

                    } else {
                        Toast.makeText(VipActivity.this, "数据返回失败", Toast.LENGTH_LONG).show();
                    }

                }catch (Exception e){
                        Toast.makeText(VipActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                        Logger.e(TAG, e.getMessage());
                }




            }
        });


    }


}
