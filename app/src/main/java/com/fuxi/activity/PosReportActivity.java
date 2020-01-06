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
import com.fuxi.main.R;
import com.fuxi.switchbutton.widget.SwitchButton;
import com.fuxi.switchbutton.widget.SwitchButton.OnChangeListener;
import com.fuxi.widget.OnRefreshListener;
import com.fuxi.widget.RefreshListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PosReportActivity extends BaseWapperActivity implements OnRefreshListener {

    private final  String TAG="PosReportActivity";

    private  SwitchButton mSwitchButton;
    private LinearLayout llQ;
    private LinearLayout llA;
    private LinearLayout llC;
    private LinearLayout llD;
    private RefreshListView lvData;
    private PosReportAdapter adapter;
    static Toast mToast;
    private int STATE_ASC =1; //货号升序
    private int Amt_ASC =1; //销售额升序
    private int ML_ASC=1;//毛利润
    private int Qty_ASC=1;//数量

    private  TextView totalQty;// 合计数量
    private  TextView totalAmt;//  合计金额
    private  TextView totalML;
    private  TextView totalMLV;


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




    private int screenWidth;//dp为单位
    private int screenHeight;
    private List<Map<String,Object>> dataList =new ArrayList<Map<String,Object>>();

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

        if(dataList.size() >0) dataList.clear();
        for (int i=0;i<30;i++){ //图片暂时写死
          Map<String,Object> map =new HashMap<>();
          map.put("Code","货品名称(test"+i+")");
          map.put("Color","蓝色"+i);
          map.put("Size","M");
          map.put("Quantity",2);
          map.put("Amount",300.55);
          map.put("UnitPrice","150.00");
          map.put("Discount","9");
          dataList.add(map);
        }
        tvMLTitle.setOnTouchListener(tl);
        tvAmtTitle.setOnTouchListener(tl);
        tvCodeTitle.setOnTouchListener(tl);
        tvother.setOnTouchListener(tl);

        adapter =new PosReportAdapter(this,dataList);
        lvData.setAdapter(adapter);
        lvData.setOnRefreshListener(this);
        mSwitchButton.setOnChangeListener(new OnChangeListener() {

            @Override
            public void onChange(int position) {
                toast(position);
            }
        });
    }

    @Override
    protected void processLogic() {
        setSwitchButton();
        getAndroiodScreenProperty();
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



    }

    @Override
    protected void findViewById() {
        mSwitchButton =(SwitchButton)findViewById(R.id.switchButton);
        llQ =(LinearLayout)findViewById(R.id.llQ);
        llA =(LinearLayout)findViewById(R.id.llA);
        llC =(LinearLayout)findViewById(R.id.llC);
        llD =(LinearLayout)findViewById(R.id.llD);

        totalQty =(TextView) findViewById(R.id.totalQty);
        totalAmt =(TextView) findViewById(R.id.totalAmt);
        totalML =(TextView) findViewById(R.id.totalML);
        totalMLV =(TextView) findViewById(R.id.totalMLV);

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

        startActivityForResult(intent,R.id.head_search);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    public void onDownPullRefresh() {

    }

    @Override
    public void onLoadingMore() {

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case R.id.head_search:
                 if(resultCode==1){

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
                     Collections.sort(dataList, new Comparator<Map<String, Object>>() {

                         @Override
                         public int compare(Map<String, Object> o1, Map<String, Object> o2) {

                             return String.valueOf(o1.get("Code")).compareTo(String.valueOf(o2.get("Code")));
                         }
                     });
                     adapter.refresh();
                     tvCodeTitle.setText("↓货品编码");


                 }else{
                     STATE_ASC=1;
                     //按提交时间降序--工具类写法
                     //按提交时间降序--工具类写法
                     Collections.sort(dataList, new Comparator<Map<String, Object>>() {

                         @Override
                         public int compare(Map<String, Object> o1, Map<String, Object> o2) {

                             return String.valueOf(o2.get("Code")).compareTo(String.valueOf(o1.get("Code")));
                         }
                     });
                     adapter.refresh();
                     tvCodeTitle.setText("↑货品编码");
                 }
                 break;
               case  R.id.tvAmtTitle:
                   if(Amt_ASC==1){ // 升序
                       Amt_ASC =2;

                       Collections.sort(dataList, new Comparator<Map<String, Object>>() {

                           @Override
                           public int compare(Map<String, Object> o1, Map<String, Object> o2) {

                               return String.valueOf(o1.get("Amount")).compareTo(String.valueOf(o2.get("Amount")));
                           }
                       });
                       adapter.refresh();

                       tvAmtTitle.setText("↓销售金额");
                   }else{
                       Amt_ASC=1;
                       Collections.sort(dataList, new Comparator<Map<String, Object>>() {

                           @Override
                           public int compare(Map<String, Object> o1, Map<String, Object> o2) {

                               return String.valueOf(o2.get("Amount")).compareTo(String.valueOf(o1.get("Amount")));
                           }
                       });
                       adapter.refresh();

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

                       tvother.setText("↓数量");
                   }else {
                       Qty_ASC=1;

                       tvother.setText("↑数量");
                   }
                 break;
              //底部框点击
              case R.id.tv_all:

                Log.i(TAG,tv_all.getText().toString());
                  mCameraDialog.dismiss();
                 break;
              case R.id.tv_department :
                   Log.i(TAG,tv_department.getText().toString());
                  mCameraDialog.dismiss();
                 break;
              case R.id.tv_department2:
                   Log.i(TAG,tv_department2.getText().toString());
                  mCameraDialog.dismiss();
                 break;
              case R.id.tv_goodstype:
                  mCameraDialog.dismiss();
                  break;
              case R.id.tv_subtype:
                  mCameraDialog.dismiss();
                  break;
              case R.id.tv_name:
                  mCameraDialog.dismiss();
                  break;
              case R.id.tv_brand:
                  mCameraDialog.dismiss();
                  break;
              case R.id.tv_employee:
                  mCameraDialog.dismiss();
                  break;
              case R.id.tv_madeby:
                  mCameraDialog.dismiss();
                  break;
               case R.id.tv_model:
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





}
