package com.fuxi.activity;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.fuxi.adspter.PosReportAdapter;
import com.fuxi.adspter.PosReportGroupAdapter;
import com.fuxi.adspter.VipAdapter;
import com.fuxi.adspter.VipTypeMenuAdapter;
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
    private  String keyword="";
    private  String typecountStr;
    private  String VipTypeID;


    private EditText param;
    private RefreshListView lvData;
    private int currPage = 1;
    private VipAdapter adapter;
    private VipTypeMenuAdapter vipTypeMenuAdapter;
    private boolean refresh;
    private  TxtWatcher tw=new TxtWatcher();
    private PopupWindow mPopupWindow;

    ListView viptype;

    private List<Map<String,Object>> dataList=new ArrayList<>();
    private List<Map<String,Object>> vipTypeList=new ArrayList<>();
    @Override
    protected void loadViewLayout() {
     setContentView(R.layout.activity_vip);

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
   //点击标题
    @Override
    protected void onHeadTileButton(View v) {
        showPopupWindow(v);
    }

    private void showPopupWindow(View v) {
        View contentView = getPopupWindowContentView();
        mPopupWindow = new PopupWindow(contentView,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        // 如果不设置PopupWindow的背景，有些版本就会出现一个问题：无论是点击外部区域还是Back键都无法dismiss弹框
        mPopupWindow.setBackgroundDrawable(new ColorDrawable());
        // 设置好参数之后再show
        // popupWindow.showAsDropDown(mButton2);  // 默认在mButton2的左下角显示
        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int xOffset = v.getWidth() / 2 - contentView.getMeasuredWidth() / 2;
        mPopupWindow.showAsDropDown(v, xOffset, 10);    //0 在mButton2的中间显示
    }

    private View getPopupWindowContentView() {
        // 一个自定义的布局，作为显示的内容
        int layoutId = R.layout.popup_content_layout;   // 布局ID
        View contentView = LayoutInflater.from(this).inflate(layoutId, null);
        View.OnClickListener menuItemOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "Click " + ((TextView) v).getText(), Toast.LENGTH_SHORT).show();
                if (mPopupWindow != null) {
                    mPopupWindow.dismiss();
                }
            }
        };
        viptype= (ListView)contentView.findViewById(R.id.lv_viptype);

        vipTypeMenuAdapter =new VipTypeMenuAdapter(VipActivity.this,vipTypeList);
        viptype.setAdapter(vipTypeMenuAdapter);
        getVipType();
        viptype.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListView  ls =(ListView) parent;
                 Map<String,Object> m=(Map<String,Object>)ls.getItemAtPosition(position);
               //  Toast.makeText(VipActivity.this,"得到位数："+String.valueOf(position),Toast.LENGTH_SHORT).show();
                 if(String.valueOf(m.get("Name")).equals("全部分类")){
                     setTitle("全部分类("+typecountStr+") ▼");
                     VipTypeID="";
                     currPage=1;
                     keyword="";
                     VipActivity.this.refresh=true;
                 }else {
                     setTitle(String.valueOf(m.get("Name"))+" ▼");
                     VipTypeID =String.valueOf(m.get("VipTypeID"));
                     currPage=1;
                     keyword="";
                     VipActivity.this.refresh=true;
                 }
                if (mPopupWindow != null) {
                    mPopupWindow.dismiss();
                }
                getData();
            }
        });

      /*  contentView.findViewById(R.id.menu_item1).setOnClickListener(menuItemOnClickListener);
        contentView.findViewById(R.id.menu_item2).setOnClickListener(menuItemOnClickListener);
        contentView.findViewById(R.id.menu_item3).setOnClickListener(menuItemOnClickListener);
        contentView.findViewById(R.id.menu_item4).setOnClickListener(menuItemOnClickListener);
        contentView.findViewById(R.id.menu_item5).setOnClickListener(menuItemOnClickListener);

       */
        return contentView;
    }


   public void getVipType(){

       RequestVo vo = new RequestVo();
       vo.requestUrl = "/select.do?getVipType";
       vo.context = this;

       HashMap map = new HashMap();
      // map.put("currPage",String.valueOf(1));
      // map.put("param","");
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
                       if (vipTypeList.size()>0) {
                           vipTypeList.clear();
                       }

                       JSONArray array = retObj.getJSONArray("obj");
                      // Log.v(TAG,"数据："+array.toString());
                       for (int i = 0; i < array.length(); i++) {
                           Map temp = new HashMap();
                           JSONObject json = array.getJSONObject(i);
                           Iterator ite = json.keys();
                           while (ite.hasNext()) {
                               String key = ite.next().toString();
                               String value = json.getString(key);
                               temp.put(key, value);
                           }
                           vipTypeList.add(temp);
                       }

                       if (array.length() == 0) {
                           Toast.makeText(VipActivity.this, "已经到达最后一页", Toast.LENGTH_SHORT).show();
                       }else {
                           Map<String,Object> allmap =new HashMap<>();
                           allmap.put("VipTypeID","0");
                           allmap.put("Name","全部分类");
                           vipTypeList.add(0,allmap);
                       }

                       vipTypeMenuAdapter.refresh(vipTypeList);

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

       if(!String.valueOf(keyword).equals("") && keyword!=null)
       {
           keyword =String.valueOf(param.getText());
       }

        HashMap map = new HashMap();
        map.put("currPage",String.valueOf(currPage));
        map.put("VipTypeID",VipTypeID);
        map.put("param",keyword);
        vo.requestDataMap =map;
        super.getDataFromServerNoProcess(vo, new DataCallback<JSONObject>() {
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


                        JSONObject typecount=retObj.getJSONObject("attributes");
                        typecountStr =typecount.getString("typecount");
                        setTitle("全部分类("+typecountStr+") ▼");

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
