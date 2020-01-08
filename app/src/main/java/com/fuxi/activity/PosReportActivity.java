package com.fuxi.activity;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fuxi.adspter.PosReportAdapter;
import com.fuxi.adspter.PosReportGroupAdapter;
import com.fuxi.main.R;
import com.fuxi.switchbutton.widget.SwitchButton;
import com.fuxi.switchbutton.widget.SwitchButton.OnChangeListener;
import com.fuxi.util.Logger;
import com.fuxi.util.LoginParameterUtil;
import com.fuxi.vo.RequestVo;
import com.fuxi.widget.OnRefreshListener;
import com.fuxi.widget.RefreshListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PosReportActivity extends BaseWapperActivity implements OnRefreshListener {

    private final  String TAG="PosReportActivity";

    private final  String url="/salesTicket.do?Rpt301F";

    private  SwitchButton mSwitchButton;
    private LinearLayout llQ;
    private LinearLayout llA;
    private LinearLayout llC;
    private LinearLayout llD;
    private RefreshListView lvData;
    private PosReportAdapter adapter;
    private PosReportGroupAdapter groupAdapter;
    static Toast mToast;
    private int STATE_ASC =1; //货号升序
    private int Amt_ASC =1; //销售额升序
    private int ML_ASC=1;//毛利润
    private int Qty_ASC=1;//数量
    private String orderField="Code"; //排序字段
    private String orderTitle="货品编码";
    private String barcode="";
    private  TextView tvtotalQty;// 合计数量
    private  TextView tvtotalAmt;//  合计金额
    private  TextView tvtotalML;
    private  TextView tvtotalMLV;


    private TextView tvCodeTitle;
    private TextView tvAmtTitle;
    private TextView tvMLTitle;
    private TextView tvother;

    private TextView tv_all;
    private TextView tv_department;
    private TextView tv_department2;
    private TextView tv_goodstype;
    private TextView tv_subtype;
    private TextView tv_name;
    private TextView tv_brand;
    private TextView tv_employee;
    private TextView tv_madeby;
    private TextView tv_model;
    private Dialog mCameraDialog;

    private TouchListener tl =new TouchListener();

    SimpleDateFormat df =new SimpleDateFormat("yyyy-MM-dd");
    Calendar c = Calendar.getInstance();

    private  String beginDate =df.format(new java.util.Date());
    private  String endDate=df.format(new java.util.Date());
    private  String groupField;
    private  String departmentid;
    private  String vipid;
    private  String goodstypeid;
    private  String employeeid;

    private int screenWidth;//dp为单位
    private int screenHeight;
    private int currPage = 1;
    private List<Map<String,Object>> dataList =new ArrayList<Map<String,Object>>();
    private List<Map<String,Object>> totalList=new ArrayList<Map<String,Object>>();;

    @Override
    protected void loadViewLayout() {
       setContentView(R.layout.activity_posreport);
       setTitle("零售报表");
        setHeadRightVisibility(View.VISIBLE);
        setHeadRightText(R.string.other_option);
        //getHeadRightBtn().setTextColor(getResources().getColor(R.color.icon_blue));
       // getHeadRightBtn().setLayoutParams(new LinearLayout.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,ActionBar.LayoutParams.WRAP_CONTENT).);
        getHeadRightBtn().setPadding(20,0,10,0);
        setHeadSearchVisibility(View.VISIBLE);
    }

    @Override
    protected void setListener() {

      /*
        totalList =new ArrayList<>();

        for (int i=0;i<30;i++){ //图片暂时写死
          Map<String,Object> map =new HashMap<>();
          map.put("Code","货品名称(test"+i+")");
          map.put("Color","蓝色"+i);
          map.put("Size","M");
          map.put("Quantity",2);
          map.put("Amount",300.55);
          map.put("UnitPrice","150.00");
          map.put("Discount","9");
            totalList.add(map);
        }

        dataList =page(totalList,10,currPage);
     */
        tvMLTitle.setOnTouchListener(tl);
        tvAmtTitle.setOnTouchListener(tl);
        tvCodeTitle.setOnTouchListener(tl);
        tvother.setOnTouchListener(tl);

     //   adapter =new PosReportAdapter(this,dataList);

    //    lvData.setAdapter(adapter);
        lvData.setOnRefreshListener(this);

        mSwitchButton.setOnChangeListener(new OnChangeListener() {

            @Override
            public void onChange(int position) {
              //  toast(position);
              switch (position){
                  case 0:
                  beginDate =df.format(new Date());
                  endDate =df.format(new Date());
                  getData();
                  break;
                  case 1:
                      c.setTime(new Date());
                      c.add(Calendar.DAY_OF_MONTH,-1);
                      beginDate =df.format(c.getTime());
                      endDate =df.format(c.getTime());
                      getData();
                  break;
                  case 2:
                      c.setTime(new Date());
                      c.add(Calendar.DAY_OF_MONTH,7);
                      beginDate =df.format(new Date());
                      endDate =df.format(c.getTime());
                      getData();
                  break;
                  case 3:
                      c.setTime(new Date());
                      c.add(Calendar.DAY_OF_MONTH,30);
                      beginDate =df.format(new Date());
                      endDate =df.format(c.getTime());
                      getData();
                  break;
              }
            }
        });
    }

    @Override
    protected void processLogic() {
        setSwitchButton();
     /*   getAndroiodScreenProperty();
        LinearLayout.LayoutParams layoutParams= new LinearLayout.LayoutParams((screenWidth)/4, ViewGroup.LayoutParams.WRAP_CONTENT,1);
        layoutParams.gravity = Gravity.CENTER;
        llQ.setLayoutParams(layoutParams);
        LinearLayout.LayoutParams layoutParamsA= new LinearLayout.LayoutParams((screenWidth)/4, ViewGroup.LayoutParams.WRAP_CONTENT,1);
        layoutParamsA.gravity=Gravity.CENTER;
        llA.setLayoutParams(layoutParamsA);

        LinearLayout.LayoutParams layoutParamsC= new LinearLayout.LayoutParams((screenWidth)/4, ViewGroup.LayoutParams.WRAP_CONTENT,1);
        layoutParamsC.gravity=Gravity.CENTER;
        llC.setLayoutParams(layoutParamsC);

        LinearLayout.LayoutParams layoutParamsD= new LinearLayout.LayoutParams((screenWidth)/4, ViewGroup.LayoutParams.WRAP_CONTENT,1);
        layoutParamsD.gravity=Gravity.CENTER;
        llD.setLayoutParams(layoutParamsD);
     */
        getData();//获取数据

    }

    @Override
    protected void findViewById() {
        mSwitchButton =(SwitchButton)findViewById(R.id.switchButton);
        llQ =(LinearLayout)findViewById(R.id.llQ);
        llA =(LinearLayout)findViewById(R.id.llA);
        llC =(LinearLayout)findViewById(R.id.llC);
        llD =(LinearLayout)findViewById(R.id.llD);

        tvtotalQty =(TextView) findViewById(R.id.totalQty);
        tvtotalAmt =(TextView) findViewById(R.id.totalAmt);
        tvtotalML =(TextView) findViewById(R.id.totalML);
        tvtotalMLV =(TextView) findViewById(R.id.totalMLV);

        tvCodeTitle =(TextView) findViewById(R.id.tvCodeTitle);
        tvAmtTitle =(TextView) findViewById(R.id.tvAmtTitle);
        tvMLTitle=(TextView) findViewById(R.id.tvMLTitle);
        tvother =(TextView) findViewById(R.id.tvother);

        lvData=(RefreshListView) findViewById(R.id.lv_datas);
    }

    @Override
    public void onClick(View v) {

    }


    @Override
    protected void onHeadRightButton(View v) {

        setDialog();

    }

    @Override
    protected void onHeadSearchButton(View v) {
        //Toast.makeText(PosReportActivity.this,"搜索提示",Toast.LENGTH_SHORT).show();
        Intent intent=new Intent(PosReportActivity.this,PosReportSearchActivity.class);
        intent.putExtra("begindate",beginDate);
        startActivityForResult(intent,R.id.head_search);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    public void onDownPullRefresh() {
         currPage =1;
         dataList =new ArrayList<>(page(totalList,10,currPage));
        if(!orderTitle.equals("货品编码")){
            groupAdapter.refresh();
        }else {
            adapter.refresh();
        }
        lvData.hideHeaderView();
    }

    @Override
    public void onLoadingMore() {
        currPage ++;
        List<Map<String,Object>> tmp =page(totalList,10,currPage);//new ArrayList<>();
        if(tmp.size()>0) {
            dataList.addAll(tmp);//new ArrayList<>(tmp)
            if(!orderTitle.equals("货品编码")){
                groupAdapter.refresh();
            }else {
                adapter.refresh();
            }

        }else if(tmp.size()==0){
            Toast.makeText(PosReportActivity.this, "已经到达最后一页", Toast.LENGTH_SHORT).show();
        }
        System.out.println("dataList值："+String.valueOf(dataList));

        lvData.hideFooterView();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case R.id.head_search:
                 if(resultCode==1){
                  beginDate =data.getStringExtra("begindate");
                  endDate =data.getStringExtra("enddate");
                    vipid =data.getStringExtra("vipid");
                  if(vipid !=null && !"".equals(vipid)){
                   StringBuffer sb= new StringBuffer(vipid);
                      sb.insert(0,"0'");
                      //  sb.insert(1,"'''");
                      sb.insert(sb.length(),"'");
                      vipid =sb.toString();
                  }
                    goodstypeid=data.getStringExtra("goodstypeid");
                    employeeid =data.getStringExtra("employeeid");
                  departmentid =data.getStringExtra("departmentid");
                   barcode =data.getStringExtra("barcode");
                     if(goodstypeid !=null && !"".equals(goodstypeid)){
                         StringBuffer sb= new StringBuffer(goodstypeid);
                         sb.insert(0,"0'");
                         //  sb.insert(1,"'''");
                         sb.insert(sb.length(),"'");
                         goodstypeid =sb.toString();
                     }

                     if(employeeid !=null && !"".equals(employeeid)){
                         StringBuffer sb= new StringBuffer(employeeid);
                         sb.insert(0,"0'");
                         //  sb.insert(1,"'''");
                         sb.insert(sb.length(),"'");
                         employeeid =sb.toString();
                     }
                     if(departmentid !=null && !"".equals(departmentid)){
                         StringBuffer sb= new StringBuffer(departmentid);
                         sb.insert(0,"0'");
                       //  sb.insert(1,"'''");
                         sb.insert(sb.length(),"'");
                         departmentid =sb.toString();
                     }
                     Log.v(TAG,"barcode:"+barcode);
                     Log.v(TAG,"beginDate的值："+beginDate);
                     Log.v(TAG,"endDate的值："+endDate);
                     Log.v(TAG,"vipid的值："+vipid);
                     Log.v(TAG,"goodstypeid的值："+goodstypeid);
                     Log.v(TAG,"employeeid的值："+employeeid);
                     Log.v(TAG,"departmentid的值："+departmentid);
                     mSwitchButton.clearCheck();
                     getData();
                 }
            break;


        }
    }

    public void setSwitchButton() {
        String[] mTexts = new String[mSwitchButton.getSwitchCount()];
        mTexts[0] = "今日";
        mTexts[1] = "昨日";
        mTexts[2] = "近7天";
        mTexts[3] = "近30天";

        mSwitchButton.setTextArray(mTexts);
        mSwitchButton.notifyDataSetChange();
    }

    private void toast(int position) {
        String msg = "点击了第" + position + "项目";
        if (mToast == null)
            mToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        else {
            mToast.setText(msg);
            mToast.setDuration(Toast.LENGTH_SHORT);
        }
        mToast.show();
    }

    //屏幕宽度,高度
    public void getAndroiodScreenProperty() {
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;         // 屏幕宽度（像素）
        int height = dm.heightPixels;       // 屏幕高度（像素）
        float density = dm.density;         // 屏幕密度（0.75 / 1.0 / 1.5）
        int densityDpi = dm.densityDpi;     // 屏幕密度dpi（120 / 160 / 240）
        // 屏幕宽度算法:屏幕宽度（像素）/屏幕密度
       // int screenWidth = (int) (width / density);  // 屏幕宽度(dp)
        //int screenHeight = (int) (height / density);// 屏幕高度(dp)
        screenWidth = (int) (width / density);  // 屏幕宽度(dp)
        screenHeight = (int) (height / density);// 屏幕高度(dp)
        Log.d("h_bl", "屏幕宽度（像素）：" + width);
        Log.d("h_bl", "屏幕高度（像素）：" + height);
        Log.d("h_bl", "屏幕密度（0.75 / 1.0 / 1.5）：" + density);
        Log.d("h_bl", "屏幕密度dpi（120 / 160 / 240）：" + densityDpi);
        Log.d("h_bl", "屏幕宽度（dp）：" + screenWidth);
        Log.d("h_bl", "屏幕高度（dp）：" + screenHeight);
    }

    class  TouchListener implements View.OnTouchListener{
        @Override
        public boolean onTouch(View v, MotionEvent event) {
           switch (v.getId()){
              case  R.id.tvCodeTitle :
                 if(STATE_ASC==1){
                     STATE_ASC =2;// 升序
                     //按提交时间降序--工具类写法

                     sortList();
                     tvCodeTitle.setText("↓"+orderTitle);


                 }else{
                     STATE_ASC=1;
                     //按提交时间降序--工具类写法
                     //按提交时间降序--工具类写法
                     //orderField ="Code";
                     sortList();
                     tvCodeTitle.setText("↑"+orderTitle);
                 }
                 break;
               case  R.id.tvAmtTitle:
                   if(Amt_ASC==1){ // 升序
                       Amt_ASC =2;

                       sortList();
                       tvAmtTitle.setText("↓销售金额");
                   }else{
                       Amt_ASC=1;
                       orderField ="Amount";
                       sortList();

                       tvAmtTitle.setText("↑销售金额");
                   }

                 break;
               case R.id.tvMLTitle:
                   if(ML_ASC==1){
                       ML_ASC =2;// 降序

                  /*     Collections.sort(dataList, new Comparator<Map<String, Object>>() {

                           @Override
                           public int compare(Map<String, Object> o1, Map<String, Object> o2) {

                               return String.valueOf(o1.get("Amount")).compareTo(String.valueOf(o2.get("Amount")));
                           }
                       });
                       adapter.refresh();
                        */
                       tvMLTitle.setText("↓毛利润");
                   }else{
                       ML_ASC=1;
                       tvMLTitle.setText("↑毛利润");
                   }
                 break;
              case R.id.tvother:
                   if(Qty_ASC==1){
                       Qty_ASC=2;
                       orderField ="Quantity";
                       sortList();
                       tvother.setText("↓数量");
                   }else {
                       Qty_ASC=1;
                       orderField ="Quantity";
                       sortList();
                       tvother.setText("↑数量");
                   }
                 break;
              //底部框点击
              case R.id.tv_all:
                  setTitle("零售报表");
                  orderField ="Code";
                  orderTitle="货品编码";
                  tvCodeTitle.setText(orderTitle);
                  getData();
               //   adapter =new PosReportAdapter(PosReportActivity.this,dataList);
               //   lvData.setAdapter(adapter);
                Log.i(TAG,tv_all.getText().toString());
                  mCameraDialog.dismiss();
                 break;
              case R.id.tv_department :
                  setTitle("店铺汇总");
                  orderField ="Department";
                  orderTitle="店铺";
                  tvCodeTitle.setText(orderTitle);
                  getData();
               //   groupAdapter =new PosReportGroupAdapter(PosReportActivity.this,dataList,"店铺");
               //   lvData.setAdapter(groupAdapter);
                   Log.i(TAG,tv_department.getText().toString());
                  mCameraDialog.dismiss();
                 break;
              case R.id.tv_department2:
                  setTitle("柜组汇总");
                  orderField ="TiWei";
                  orderTitle="柜组";
                  tvCodeTitle.setText(orderTitle);
                  getData();
              //    groupAdapter =new PosReportGroupAdapter(PosReportActivity.this,dataList,"柜组");
               //   lvData.setAdapter(groupAdapter);
                   Log.i(TAG,tv_department2.getText().toString());
                  mCameraDialog.dismiss();
                 break;
              case R.id.tv_goodstype:
                  setTitle("货品类别汇总");
                  orderField ="GoodsType";
                  orderTitle="货品类别";
                  tvCodeTitle.setText(orderTitle);
                  getData();
               //   groupAdapter =new PosReportGroupAdapter(PosReportActivity.this,dataList,"货品类别");
              //    lvData.setAdapter(groupAdapter);
                  mCameraDialog.dismiss();
                  break;
              case R.id.tv_subtype:
                  setTitle("货品子类别汇总");
                  orderField ="SubType";
                  orderTitle="货品子类别";
                  tvCodeTitle.setText(orderTitle);
                  getData();
                //  groupAdapter =new PosReportGroupAdapter(PosReportActivity.this,dataList,"货品子类别");
               //   lvData.setAdapter(groupAdapter);
                  mCameraDialog.dismiss();
                  break;
              case R.id.tv_name:
                  setTitle("货品名称汇总");
                  orderField ="Name";
                  orderTitle="货品名称";
                  tvCodeTitle.setText(orderTitle);
                  getData();
                //  groupAdapter =new PosReportGroupAdapter(PosReportActivity.this,dataList,"货品名称");
                //  lvData.setAdapter(groupAdapter);
                  mCameraDialog.dismiss();
                  break;
              case R.id.tv_brand:
                  setTitle("品牌汇总");
                  orderField ="Brand";
                  orderTitle="品牌";
                  tvCodeTitle.setText(orderTitle);
                  getData();
              //    groupAdapter =new PosReportGroupAdapter(PosReportActivity.this,dataList,"品牌");
              //    lvData.setAdapter(groupAdapter);
                  mCameraDialog.dismiss();
                  break;
              case R.id.tv_employee:
                  setTitle("售货员汇总");
                  orderField ="GoodsEmployee";
                  orderTitle="货品售货员";
                  tvCodeTitle.setText(orderTitle);
                  getData();
                  groupAdapter =new PosReportGroupAdapter(PosReportActivity.this,dataList,"货品售货员");
                  lvData.setAdapter(groupAdapter);
                  mCameraDialog.dismiss();
                  break;
              case R.id.tv_madeby:
                  setTitle("制单汇总");
                  orderField ="MadeBy";
                  orderTitle="制单";
                  tvCodeTitle.setText(orderTitle);
                  getData();
                //  groupAdapter =new PosReportGroupAdapter(PosReportActivity.this,dataList,"制单");
                //  lvData.setAdapter(groupAdapter);
                  mCameraDialog.dismiss();
                  break;
               case R.id.tv_model:
                   setTitle("型号规格汇总");
                   orderField ="Model";
                   orderTitle="型号规格";
                   tvCodeTitle.setText(orderTitle);
                   getData();
               //    groupAdapter =new PosReportGroupAdapter(PosReportActivity.this,dataList,"型号规格");
                //   lvData.setAdapter(groupAdapter);
                   mCameraDialog.dismiss();
                  break;
             default:
                 break;
           }



            return false;
        }
    }

    private void setDialog(){
         mCameraDialog = new Dialog(this, R.style.BottomDialog);
        LinearLayout root = (LinearLayout) LayoutInflater.from(this).inflate(
                R.layout.bottom_dialog, null);
        //初始化视图
      //  root.findViewById(R.id.btn_choose_img).setOnClickListener(this);
      //  root.findViewById(R.id.btn_open_camera).setOnClickListener(this);
      //  root.findViewById(R.id.btn_cancel).setOnClickListener(this);

        tv_all =(TextView) root.findViewById(R.id.tv_all);
        tv_department =(TextView) root.findViewById(R.id.tv_department);
        tv_department2 =(TextView) root.findViewById(R.id.tv_department2);
        tv_goodstype =(TextView) root.findViewById(R.id.tv_goodstype);
        tv_subtype =(TextView) root.findViewById(R.id.tv_subtype);
        tv_name =(TextView) root.findViewById(R.id.tv_name);
        tv_brand=(TextView) root.findViewById(R.id.tv_brand);
        tv_employee=(TextView) root.findViewById(R.id.tv_employee);
        tv_madeby =(TextView) root.findViewById(R.id.tv_madeby);
        tv_model =(TextView) root.findViewById(R.id.tv_model);

        tv_all.setOnTouchListener(tl);
        tv_department.setOnTouchListener(tl);
        tv_department2.setOnTouchListener(tl);
        tv_goodstype.setOnTouchListener(tl);
        tv_subtype.setOnTouchListener(tl);
        tv_name.setOnTouchListener(tl);
        tv_brand.setOnTouchListener(tl);
        tv_employee.setOnTouchListener(tl);
        tv_madeby.setOnTouchListener(tl);
        tv_model.setOnTouchListener(tl);

        mCameraDialog.setContentView(root);
        Window dialogWindow = mCameraDialog.getWindow();
        dialogWindow.setGravity(Gravity.BOTTOM);
//        dialogWindow.setWindowAnimations(R.style.dialogstyle); // 添加动画
        WindowManager.LayoutParams lp = dialogWindow.getAttributes(); // 获取对话框当前的参数值
        lp.x = 0; // 新位置X坐标
        lp.y = 0; // 新位置Y坐标
        lp.width = (int) getResources().getDisplayMetrics().widthPixels; // 宽度
        root.measure(0, 0);
        lp.height = root.getMeasuredHeight();

        lp.alpha = 9f; // 透明度
        dialogWindow.setAttributes(lp);
        mCameraDialog.show();

    }

   //排序
    private  void sortList(){

        Collections.sort(dataList, new Comparator<Map<String, Object>>() {

            @Override
            public int compare(Map<String, Object> o1, Map<String, Object> o2) {

                if(STATE_ASC==2 || Amt_ASC==2 || ML_ASC==2 || Qty_ASC==2){
                    return String.valueOf(o2.get(orderField)).compareTo(String.valueOf(o1.get(orderField)));
                }else if(STATE_ASC==1 || Amt_ASC==1 || ML_ASC==1 || Qty_ASC==1){
                    return String.valueOf(o1.get(orderField)).compareTo(String.valueOf(o2.get(orderField)));
                }
               return  0;//加一个默认返回
            }
        });
        adapter.refresh();
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

 public void getData(){
   try {
       RequestVo vo = new RequestVo();
       vo.requestUrl = url;
       vo.context = this;


       HashMap map = new HashMap();
       map.put("begindate", beginDate);
       map.put("enddate", endDate);
       map.put("DeptTypeID",departmentid);
       map.put("GoodsCode", barcode);
       map.put("GoodsName", "");
       map.put("GoodsTypeID", goodstypeid);
       map.put("EmployeeID", employeeid);
       map.put("VipID", vipid);
       map.put("SumStr", orderTitle.equals("货品编码") ? "全部" : orderTitle);
       vo.requestDataMap = map;
       super.getDataFromServer(vo, new DataCallback<JSONObject>() {
           @Override
           public void processData(JSONObject retObj, boolean paramBoolean) {
               try {
                   if (retObj == null) {
                       return;
                   }
                   if (retObj.getBoolean("success")) {
                       //多次查询时
                       if(totalList.size()>0) totalList.clear();
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
                           totalList.add(temp);
                       }
                       total();//合计
                       dataList =page(totalList,15,currPage);
                       if(!orderTitle.equals("货品编码")){
                           groupAdapter=new PosReportGroupAdapter(PosReportActivity.this,dataList,orderTitle);
                           lvData.setAdapter(groupAdapter);
                       }else{
                           adapter=new PosReportAdapter(PosReportActivity.this,dataList);
                           lvData.setAdapter(adapter);
                       }
                       if(totalList.size()==0){
                           Toast.makeText(PosReportActivity.this, "暂无数据", Toast.LENGTH_SHORT).show();
                       }


                   } else {
                       Toast.makeText(PosReportActivity.this, "数据返回失败", Toast.LENGTH_LONG).show();
                   }

               } catch (Exception e) {
                   Toast.makeText(PosReportActivity.this, "查询数据异常", Toast.LENGTH_LONG).show();
                   Logger.e(TAG, e.getMessage());
               }
           }
       });

   }catch (Exception e){
       Toast.makeText(PosReportActivity.this, "系统错误", Toast.LENGTH_LONG).show();
       Logger.e(TAG, e.getMessage());
   }
 }

 //合计
 public void total(){

  //处理小数位

  int totalQty=0;
  BigDecimal totalAmt =new BigDecimal("0.00");
  BigDecimal totalCostAmt =new BigDecimal("0.00");
  BigDecimal totalML =new BigDecimal("0.00");
  BigDecimal totalMLLV =new BigDecimal("0.00");
  try {
      for (int i = 0; i < totalList.size(); i++) {
          Map<String, Object> map = totalList.get(i);
          if (map.containsKey("Amount") && map.get("Amount") != null && !"null".equals(map.get("Amount"))) {
              map.put("Amount", new BigDecimal(String.valueOf(map.get("Amount"))).setScale(2, BigDecimal.ROUND_DOWN));
              totalAmt = totalAmt.add(new BigDecimal(String.valueOf(map.get("Amount"))).setScale(2, BigDecimal.ROUND_DOWN));
          }
          if (map.containsKey("ML") && map.get("ML") != null && !"null".equals(map.get("ML"))) {
              map.put("ML", new BigDecimal(String.valueOf(map.get("ML"))).setScale(2, BigDecimal.ROUND_DOWN));
              totalML = totalML.add(new BigDecimal(String.valueOf(map.get("ML"))).setScale(2, BigDecimal.ROUND_DOWN));
          }
          if (map.containsKey("CostAmt") && map.get("CostAmt") != null && !"null".equals(map.get("CostAmt"))) {
              map.put("CostAmt", new BigDecimal(String.valueOf(map.get("CostAmt"))).setScale(2, BigDecimal.ROUND_DOWN));
              totalCostAmt = totalCostAmt.add(new BigDecimal(String.valueOf(map.get("CostAmt"))).setScale(2, BigDecimal.ROUND_DOWN));
          }
          if(map.get("Quantity") !=null && !"null".equals(map.get("Quantity"))) {
              totalQty = totalQty + Integer.parseInt(String.valueOf(map.get("Quantity")));
          }

      }

      if(totalAmt.compareTo(BigDecimal.ZERO) !=0 && totalAmt !=null) {
          totalMLLV = totalML.divide(totalAmt,2,BigDecimal.ROUND_DOWN).multiply(new BigDecimal("100"));
      }
      tvtotalQty.setText(String.valueOf(totalQty));
      tvtotalAmt.setText("￥"+String.valueOf(totalAmt));
      tvtotalML.setText("￥"+String.valueOf(totalML));
      tvtotalMLV.setText(String.valueOf(totalMLLV));

  }catch (Exception e){
      Toast.makeText(PosReportActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
      Logger.e(TAG, e.getMessage());
  }



 }


}
