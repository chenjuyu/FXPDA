package com.fuxi.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dommy.qrcode.util.Constant;
import com.fuxi.adspter.StockQueryAdapter;
import com.fuxi.main.R;
import com.fuxi.widget.OnRefreshListener;
import com.fuxi.widget.RefreshListView;
import com.google.zxing.activity.CaptureActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
* 参考秦丝软件库存
* */

public class StockQueryActivity extends BaseWapperActivity implements AdapterView.OnItemClickListener, OnRefreshListener {

    String TAG ="StockQueryActivity";
    private int STATE_ASC =1; //货号升序
    private int Name_ASC =1; //销售额升序
    private int Code_ASC=1;//毛利润
    private int Qty_ASC=1;//数量
    private String orderField="Code"; //排序字段
    private String orderTitle="货品编码";
    private String typewareStr="";
    private TouchListener tl=new TouchListener();

    private TextView tvscan;
    private EditText edkeyword;
    private TextView tvsearch;
    private TextView tvgoodscount;
    private TextView tvstocktotalqty;
    private TextView tvstockcost;
    private TextView tvstockwarn;
    private TextView tvdefault;
    private TextView tvname;
    private TextView tvcode;
    private TextView tvstockqty;


    private RefreshListView lvdatas;
    private StockQueryAdapter adapter;
    private List<Map<String,Object>> dataList=new ArrayList<>();

    @Override
    protected void loadViewLayout() {
       setContentView(R.layout.activity_stock_query);

        setTitle("全部仓库 ▼");
    }

    @Override
    protected void setListener() {
        lvdatas.setOnRefreshListener(this);
        lvdatas.setOnItemClickListener(this);
        tvsearch.setOnTouchListener(tl);
        tvscan.setOnTouchListener(tl);
        tvdefault.setOnTouchListener(tl);
        tvname.setOnTouchListener(tl);
        tvcode.setOnTouchListener(tl);
        tvstockqty.setOnTouchListener(tl);
    }


    @Override
    public void onLoadingMore() {

    }

    @Override
    public void onDownPullRefresh() {

    }

    @Override
    protected void processLogic() {

        //添加数据
       for(int i=0;i<30;i++){
       Map<String,Object> map =new HashMap<>();
       map.put("GoodsID",i);
       map.put("img","");
       map.put("Code","SB0001"+i);
       map.put("Name","货品名称"+i);
       map.put("Quantity",i);
       map.put("inqty",i);
       map.put("outqty",i);
       map.put("stockqty",i);
       map.put("purchaseprice","100"+i);
       map.put("pfprice","200"+i);
       map.put("retailsales","300"+i);
       dataList.add(map);
       }
        Log.v(TAG,"列表数据:"+dataList.toString());
        adapter =new StockQueryAdapter(this,dataList);
        lvdatas.setAdapter(adapter);




    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
       RefreshListView listView =(RefreshListView)  parent;

      Map<String,Object> map =(Map<String,Object>) listView.getItemAtPosition(position) ;
        Intent in=new Intent(StockQueryActivity.this,StockQueryDetailActivity.class);
        in.putExtra("GoodsID",String.valueOf(map.get("GoodsID")));
        in.putExtra("Code",String.valueOf(map.get("Code")));
      startActivityForResult(in,100);

    }

    @Override
    protected void findViewById() {
          tvscan =(TextView) findViewById(R.id.tv_scan);
          edkeyword=(EditText) findViewById(R.id.ed_keyword);
          tvsearch =(TextView) findViewById(R.id.tv_search);
          tvgoodscount =(TextView) findViewById(R.id.tv_goodscount);
          tvstocktotalqty =(TextView) findViewById(R.id.tv_stocktotalqty);
         tvstockcost =(TextView) findViewById(R.id.tv_stockcost);
         tvstockwarn =(TextView) findViewById(R.id.tv_stockwarn);

        tvdefault =(TextView) findViewById(R.id.tv_default);
        tvname =(TextView) findViewById(R.id.tv_name);
        tvcode =(TextView) findViewById(R.id.tv_code);
        tvstockqty =(TextView) findViewById(R.id.tv_stockqty);
        lvdatas =(RefreshListView)findViewById(R.id.lv_datas);

    }

    @Override
    public void onClick(View v) {

    }

    // 开始扫码
    private void startQrCode() {
        // 申请相机权限
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // 申请权限
            ActivityCompat.requestPermissions(StockQueryActivity.this, new String[]{Manifest.permission.CAMERA}, Constant.REQ_PERM_CAMERA);
            return;
        }
        // 申请文件读写权限（部分朋友遇到相册选图需要读写权限的情况，这里一并写一下）
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // 申请权限
            ActivityCompat.requestPermissions(StockQueryActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, Constant.REQ_PERM_EXTERNAL_STORAGE);
            return;
        }
        // 二维码扫码
        Intent intent = new Intent(StockQueryActivity.this, CaptureActivity.class);
        startActivityForResult(intent, Constant.REQ_QR_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode){
            case Constant.REQ_QR_CODE:
                if(resultCode == RESULT_OK ){
                    Bundle bundle = data.getExtras();
                    String scanResult = bundle.getString(Constant.INTENT_EXTRA_KEY_QR_SCAN);
                    edkeyword.setText(scanResult);
                   // addBarCode();
                    // Toast.makeText(SalesDetailActivity.this,scanResult,Toast.LENGTH_LONG).show();
                }
        break;

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constant.REQ_PERM_CAMERA:
                // 摄像头权限申请
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 获得授权
                    startQrCode();
                } else {
                    // 被禁止授权
                    Toast.makeText(StockQueryActivity.this, "请至权限中心打开本应用的相机访问权限", Toast.LENGTH_LONG).show();
                }
                break;
            case Constant.REQ_PERM_EXTERNAL_STORAGE:
                // 文件读写权限申请
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 获得授权
                    startQrCode();
                } else {
                    // 被禁止授权
                    Toast.makeText(StockQueryActivity.this, "请至权限中心打开本应用的文件读写权限", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }

    class TouchListener implements View.OnTouchListener{
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (v.getId()){
                case R.id.tv_search:
                   Intent in=new Intent(StockQueryActivity.this,StockQuerySeachActivity.class);
                   startActivityForResult(in,R.id.tv_search);
                break;

                case R.id.tv_scan:
                    startQrCode();
                break;
                case R.id.tv_default:
                    if(STATE_ASC==1){
                        STATE_ASC =2;// 升序
                        //按提交时间降序--工具类写法

                        orderField="Code";
                        orderTitle="默认";
                        tvdefault.setText("↓"+orderTitle);
                        tvname.setText("名称");
                        tvcode.setText("货号");
                        tvstockqty.setText("库存量");
                        sortList();


                    }else{
                        STATE_ASC=1;
                        //按提交时间降序--工具类写法
                        //按提交时间降序--工具类写法
                        orderField ="Code";
                        orderTitle="默认";

                        tvdefault.setText("↑"+orderTitle);
                        tvname.setText("名称");
                        tvcode.setText("货号");
                        tvstockqty.setText("库存量");
                        sortList();
                    }

                 break;
                case R.id.tv_name:
                    if(Name_ASC==1){
                        Name_ASC =2;// 升序
                        //按提交时间降序--工具类写法

                        orderField="Name";
                        orderTitle="名称";
                        tvname.setText("↓"+orderTitle);
                        tvdefault.setText("默认");
                        tvcode.setText("货号");
                        tvstockqty.setText("库存量");
                        sortList();


                    }else{
                        Name_ASC=1;
                        //按提交时间降序--工具类写法
                        //按提交时间降序--工具类写法
                        orderField ="Name";
                        orderTitle="名称";

                        tvname.setText("↑"+orderTitle);
                        tvdefault.setText("默认");
                        tvcode.setText("货号");
                        tvstockqty.setText("库存量");
                        sortList();
                    }

                 break;
                case R.id.tv_code :
                if(Code_ASC==1){
                    Code_ASC =2;
                    //按提交时间降序--工具类写法
                    //按提交时间降序--工具类写法
                    orderField ="Code";
                    orderTitle="货号";

                    tvcode.setText("↑"+orderTitle);
                    tvname.setText("名称");
                    tvdefault.setText("默认");
                    tvstockqty.setText("库存量");
                    sortList();
                }else{
                    Code_ASC=1;
                    //按提交时间降序--工具类写法
                    //按提交时间降序--工具类写法
                    orderField ="Code";
                    orderTitle="货号";

                    tvcode.setText("↓"+orderTitle);
                    tvname.setText("名称");
                    tvdefault.setText("默认");
                    tvstockqty.setText("库存量");
                    sortList();

                }
                 break;
                case R.id.tv_stockqty :
                 if(Qty_ASC==1){
                     Qty_ASC=2;

                     //按提交时间降序--工具类写法
                     //按提交时间降序--工具类写法
                     orderField ="stockqty";
                     orderTitle="库存量";

                     tvstockqty.setText("↑"+orderTitle);
                     tvdefault.setText("默认");
                     tvcode.setText("货号");
                     tvname.setText("名称");
                     sortList();

                 }else{
                     Qty_ASC=1;
                     //按提交时间降序--工具类写法
                     //按提交时间降序--工具类写法
                     orderField ="stockqty";
                     orderTitle="库存量";

                     tvstockqty.setText("↓"+orderTitle);
                     tvdefault.setText("默认");
                     tvcode.setText("货号");
                     tvname.setText("名称");
                     sortList();
                 }

                 break;
            }

            return false;
        }
    }


    //排序
    private  void sortList(){

        Collections.sort(dataList, new Comparator<Map<String, Object>>() {

            @Override
            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
              //|| Amt_ASC==2 || Amt_ASC==1
                if(STATE_ASC==2  || Name_ASC==2 || Qty_ASC==2){
                    return String.valueOf(o2.get(orderField)).compareTo(String.valueOf(o1.get(orderField)));
                }else if(STATE_ASC==1  || Name_ASC==1 || Qty_ASC==1){
                    return String.valueOf(o1.get(orderField)).compareTo(String.valueOf(o2.get(orderField)));
                }
                return  0;//加一个默认返回
            }
        });
        adapter.refresh(dataList);
    }

}
