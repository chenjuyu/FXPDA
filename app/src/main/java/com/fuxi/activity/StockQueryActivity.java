package com.fuxi.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.dommy.qrcode.util.Constant;
import com.fuxi.adspter.DepartmentMenuAdapter;

import com.fuxi.adspter.StockQueryAdapter;

import com.fuxi.main.R;
import com.fuxi.util.Logger;
import com.fuxi.util.ScreenUtils;
import com.fuxi.vo.RequestVo;
import com.fuxi.widget.OnRefreshListener;
import com.fuxi.widget.RefreshListView;
import com.google.zxing.activity.CaptureActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/*
* 参考秦丝软件库存
* */

public class StockQueryActivity extends BaseWapperActivity implements AdapterView.OnItemClickListener, OnRefreshListener {


    private String url ="/salesTicket.do?stockquery";
    private String TAG ="StockQueryActivity";
    private int STATE_ASC =1; //货号升序
    private int Name_ASC =1; //销售额升序
    private int Code_ASC=1;//毛利润
    private int Qty_ASC=1;//数量
    private String orderField="Code"; //排序字段
    private String orderTitle="货品编码";
    private boolean descflag =false;
    private String typewareStr="";
    private String keyword="";

    private int currPage=1;
    private String DepartmentID="";
    private String season="";
    private String goodstypeid="";
    private String brandid="";

    private boolean refresh=false;
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

    private PopupWindow mPopupWindow;
    private ListView lvDepartment;
    private RefreshListView lvdatas;
    private StockQueryAdapter adapter;

    private  DepartmentMenuAdapter departmentMenuAdapter;
    private List<Map<String,Object>> departmentList =new ArrayList<>();
    private List<Map<String,Object>> dataList=new ArrayList<>();
    private List<Map<String,Object>> totalList =new ArrayList<>();

    SimpleDateFormat df =new SimpleDateFormat("yyyy-MM-dd");

    private String enddate  =df.format(new Date());
    public void getData(){
     RequestVo vo =new RequestVo();
     vo.context =this;
     vo.requestUrl =url;
     HashMap map =new HashMap();
     map.put("enddate",enddate);
     map.put("Code",keyword);
     map.put("DepartmentID",DepartmentID);
     map.put("season",season);
     map.put("GoodsTypeID",goodstypeid);
     map.put("brandid",brandid);
    vo.requestDataMap =map;

    super.getDataFromServerNoProcess(vo, new DataCallback<JSONObject>() {
        @Override
        public void processData(JSONObject retObj, boolean paramBoolean) {
            try {
                if(retObj==null){
                    return;
                }
               if(retObj.getBoolean("success")){
                 if(StockQueryActivity.this.refresh){
                     dataList.clear();
                     totalList.clear();
                     lvdatas.hideHeaderView();
                 }
                JSONArray  array =retObj.getJSONArray("obj");
                  // array 转list
                for(int i=0;i<array.length();i++){
                    Map<String,Object> temp =new HashMap<>();
                    JSONObject json =array.getJSONObject(i);
                    Iterator ite= json.keys();
                    while (ite.hasNext()){
                        String key =ite.next().toString();
                        String value =json.getString(key);
                        temp.put(key,value);
                    }
                   totalList.add(temp);
                }
                   total();//合计
                   dataList =page(totalList,15,currPage);
                   if(totalList.size()==0){
                       Toast.makeText(StockQueryActivity.this, "暂无数据", Toast.LENGTH_SHORT).show();
                   }
                   adapter.refresh(dataList);
               }else{
                   Toast.makeText(StockQueryActivity.this,"数据异常",Toast.LENGTH_SHORT).show();
               }
               lvdatas.hideFooterView();
            }catch (Exception e){
             Toast.makeText(StockQueryActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
             Logger.e(TAG,e.getMessage());
            }
        }
    });

    }
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
     currPage ++;
     refresh =false;
    getData();
    }

    @Override
    public void onDownPullRefresh() {
        currPage =1;
        refresh =true;
        getData();
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

        edkeyword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                keyword =s.toString();
            }
        });

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
    protected void onHeadTileButton(View v) {
        showPopupWindow(v);
    }
    //不带箭头
    private void showPopupWindow(View v) {
        View contentView = getPopupWindowContentView();
        mPopupWindow = new PopupWindow(contentView,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        // 如果不设置PopupWindow的背景，有些版本就会出现一个问题：无论是点击外部区域还是Back键都无法dismiss弹框
        // new ColorDrawable().setColor(this.getResources().getColorStateList(R.color.pbackgroup)
        //getResources().getDrawable(R.color.pbackgroup)
        mPopupWindow.setBackgroundDrawable(new ColorDrawable());
        backgroundAlpha(1f);
        // 设置好参数之后再show
        // popupWindow.showAsDropDown(mButton2);  // 默认在mButton2的左下角显示
        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int xOffset = v.getWidth() / 2 - contentView.getMeasuredWidth() / 2;
        mPopupWindow.showAsDropDown(v, xOffset, 10);    //0 在mButton2的中间显示
    }

    /**
     * 设置添加屏幕的背景透明度
     * @param bgAlpha
     */
    public void backgroundAlpha(float bgAlpha)
    {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.alpha = bgAlpha; //0.0-1.0
        getWindow().setAttributes(lp);
    }
  //带箭头
    public PopupWindow showTipPopupWindow5(final View anchorView) {
        final View contentView = createPopupContentView(anchorView.getContext());
        final int pos[] = new int[2];
        anchorView.getLocationOnScreen(pos);
        int windowHeight = ScreenUtils.getScreenHeight(getApplicationContext()) - pos[1];
        // focus参数传入false，好验证update方法对Gravity的影响
        final PopupWindow popupWindow = new PopupWindow(contentView,
                ViewGroup.LayoutParams.WRAP_CONTENT, windowHeight, false);
        // 使用 showAtLocation 方法：anchorView 下面即使空间不够也还是会显示在 anchorView 下面
        popupWindow.showAtLocation(anchorView, Gravity.BOTTOM, 0, 0);
        return popupWindow;
    }

    private View createPopupContentView(Context context) {
        final View contentView = LayoutInflater.from(context).inflate(R.layout.popup_empty_content_layout, null);
       // contentView.setOnClickListener(mClickContentCancelListener);
        return contentView;
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
                        descflag=true;
                        orderField="Quantity";
                        orderTitle="默认";
                        tvdefault.setText("↓"+orderTitle);
                        tvname.setText("名称");
                        tvcode.setText("货号");
                        tvstockqty.setText("库存量");
                        sortList();


                    }else{
                        descflag=false;
                        STATE_ASC=1;
                        //按提交时间降序--工具类写法
                        //按提交时间降序--工具类写法
                        orderField ="Quantity";
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
                        descflag=true;
                        orderField="Name";
                        orderTitle="名称";
                        tvname.setText("↓"+orderTitle);
                        tvdefault.setText("默认");
                        tvcode.setText("货号");
                        tvstockqty.setText("库存量");
                        sortList();


                    }else{
                        descflag=false;
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
                    descflag=true;
                    tvcode.setText("↑"+orderTitle);
                    tvname.setText("名称");
                    tvdefault.setText("默认");
                    tvstockqty.setText("库存量");
                    sortList();
                }else{
                    descflag=false;
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
                     descflag=true;
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
                     descflag=false;
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
                 if(!descflag){
                    return String.valueOf(o2.get(orderField)).compareTo(String.valueOf(o1.get(orderField)));
                }else {
                    return String.valueOf(o1.get(orderField)).compareTo(String.valueOf(o2.get(orderField)));
                }
              //  return  0;//加一个默认返回
            }
        });
        adapter.refresh(dataList);
    }


    //合计
    public void total(){

        //处理小数位

        int totalQty=0; //可用库存
        int stockqty =0; //库存量

        BigDecimal totalAmt =new BigDecimal("0.00");
        BigDecimal totalCostAmt =new BigDecimal("0.00");
        BigDecimal totalML =new BigDecimal("0.00");
        BigDecimal totalMLLV =new BigDecimal("0.00");
        try {
            for (int i = 0; i < totalList.size(); i++) {
                Map<String, Object> map = totalList.get(i);
             /*   if (map.containsKey("Amount") && map.get("Amount") != null && !"null".equals(map.get("Amount"))) {
                    map.put("Amount", new BigDecimal(String.valueOf(map.get("Amount"))).setScale(2, BigDecimal.ROUND_DOWN));
                    totalAmt = totalAmt.add(new BigDecimal(String.valueOf(map.get("Amount"))).setScale(2, BigDecimal.ROUND_DOWN));
                }
                if (map.containsKey("ML") && map.get("ML") != null && !"null".equals(map.get("ML"))) {
                    map.put("ML", new BigDecimal(String.valueOf(map.get("ML"))).setScale(2, BigDecimal.ROUND_DOWN));
                    totalML = totalML.add(new BigDecimal(String.valueOf(map.get("ML"))).setScale(2, BigDecimal.ROUND_DOWN));
                }  */
                if (map.containsKey("CostAmt") && map.get("CostAmt") != null && !"null".equals(map.get("CostAmt"))) {
                    map.put("CostAmt", new BigDecimal(String.valueOf(map.get("CostAmt"))).setScale(2, BigDecimal.ROUND_DOWN));
                    totalCostAmt = totalCostAmt.add(new BigDecimal(String.valueOf(map.get("CostAmt"))).setScale(2, BigDecimal.ROUND_DOWN));
                }
                //可用库存
                if(map.get("Quantity") !=null && !"null".equals(map.get("Quantity"))) {
                    totalQty = totalQty + Integer.parseInt(String.valueOf(map.get("Quantity")));
                }
                //库存量
                if(map.get("stockqty") !=null && !"null".equals(map.get("stockqty"))) {
                    stockqty = stockqty + Integer.parseInt(String.valueOf(map.get("stockqty")));
                }



            }

            if(totalAmt.compareTo(BigDecimal.ZERO) !=0 && totalAmt !=null) {
                totalMLLV = totalML.divide(totalAmt,2,BigDecimal.ROUND_DOWN).multiply(new BigDecimal("100"));
            }
            tvgoodscount.setText(String.valueOf(totalList.size()));
            tvstocktotalqty.setText(String.valueOf(stockqty));
            tvstockcost.setText("￥"+String.valueOf(totalCostAmt));
          //  tvtotalMLV.setText(String.valueOf(totalMLLV));

        }catch (Exception e){
            Toast.makeText(StockQueryActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            Logger.e(TAG, e.getMessage());
        }

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
        lvDepartment= (ListView)contentView.findViewById(R.id.lv_viptype);


        departmentMenuAdapter =new DepartmentMenuAdapter(StockQueryActivity.this,departmentList);
        lvDepartment.setAdapter(departmentMenuAdapter);
        getDepartment();

        lvDepartment.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListView  ls =(ListView) parent;
                Map<String,Object> m=(Map<String,Object>)ls.getItemAtPosition(position);
                //  Toast.makeText(VipActivity.this,"得到位数："+String.valueOf(position),Toast.LENGTH_SHORT).show();
                if(String.valueOf(m.get("Name")).equals("全部仓库")){
                    setTitle("全部仓库 ▼");
                    DepartmentID="";
                    currPage=1;
                    keyword="";
                    StockQueryActivity.this.refresh=true;
                }else {
                    setTitle(String.valueOf(m.get("Name"))+" ▼");
                    DepartmentID =String.valueOf(m.get("DepartmentID"));
                    currPage=1;
                    keyword="";
                    StockQueryActivity.this.refresh=true;
                }
                if (mPopupWindow != null) {
                    mPopupWindow.dismiss();
                }
             //   getData();
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

    /**
     * 循环截取某页列表进行分页
     * @param dataList 分页数据
     * @param pageSize  页面大小
     * @param currentPage   当前页面
     */
    public static List<Map<String,Object>> page(List<Map<String,Object>> dataList, int pageSize,int currentPage) {
        List<Map<String,Object>> currentPageList = new ArrayList<>();
        if (dataList != null && dataList.size() > 0) {
            int currIdx = (currentPage > 1 ? (currentPage - 1) * pageSize : 0);
            for (int i = 0; i < pageSize && i < dataList.size() - currIdx; i++) {
                Map<String,Object> data = dataList.get(currIdx + i);
                currentPageList.add(data);
            }
        }
        return currentPageList;
    }

 public  void getDepartment(){
     RequestVo vo = new RequestVo();
     vo.requestUrl = "/select.do?getDepartment";
     vo.context = this;


     HashMap map = new HashMap();
     map.put("param", "");
     map.put("currPage", String.valueOf(currPage));
     vo.requestDataMap = map;
    super.getDataFromServerNoProcess(vo, new DataCallback<JSONObject>() {
        @Override
        public void processData(JSONObject retObj, boolean paramBoolean) {
            try {
                if (retObj == null) {
                    return;
                }
                if (retObj.getBoolean("success")) {
                    //多次查询时
                    if(departmentList.size()>0) departmentList.clear();
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
                        departmentList.add(temp);
                    }
                //    total();//合计
              //         dataList =page(totalList,15,currPage);

                    if(departmentList.size()==0){
                        Toast.makeText(StockQueryActivity.this, "暂无数据", Toast.LENGTH_SHORT).show();
                    }else{
                        Map<String,Object> m=new HashMap<>();
                        m.put("DepartmentID","");
                        m.put("Name","全部仓库");
                        departmentList.add(0,m);
                        departmentMenuAdapter.refresh(departmentList);
                    }


                } else {
                    Toast.makeText(StockQueryActivity.this, "数据返回失败", Toast.LENGTH_LONG).show();
                }

            } catch (Exception e) {
                Toast.makeText(StockQueryActivity.this, "查询数据异常", Toast.LENGTH_LONG).show();
                Logger.e(TAG, e.getMessage());
            }
        }
    });


 }

}
