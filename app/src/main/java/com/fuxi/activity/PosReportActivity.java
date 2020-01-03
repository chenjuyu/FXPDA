package com.fuxi.activity;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PosReportActivity extends BaseWapperActivity implements OnRefreshListener {


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
    private TextView tvCodeTitle;
    private TextView tvAmtTitle;
    private TextView tvMLTitle;
    private TextView tvother;
    private TouchListener tl =new TouchListener();

    private int screenWidth;//dp为单位
    private int screenHeight;
    private List<Map<String,Object>> dataList =new ArrayList<Map<String,Object>>();

    @Override
    protected void loadViewLayout() {
       setContentView(R.layout.activity_posreport);
       setTitle("零售报表");
        setHeadRightVisibility(View.VISIBLE);
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
    protected void setHeadRightText(int textid) {
        super.setHeadRightText(R.string.subtract);

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
                     STATE_ASC =2;// 降序
                     tvCodeTitle.setText("↓货品编码");
                 }else{
                     STATE_ASC=1;
                     tvCodeTitle.setText("↑货品编码");
                 }
                 break;
               case  R.id.tvAmtTitle:
                   if(Amt_ASC==1){
                       Amt_ASC =2;// 降序
                       tvAmtTitle.setText("↓销售金额");
                   }else{
                       Amt_ASC=1;
                       tvAmtTitle.setText("↑销售金额");
                   }

                 break;
               case R.id.tvMLTitle:
                   if(ML_ASC==1){
                       ML_ASC =2;// 降序
                       tvMLTitle.setText("↓毛利润");
                   }else{
                       ML_ASC=1;
                       tvMLTitle.setText("↑毛利润");
                   }
                 break;
               case R.id.tvother:

                 break;
             default:
                 break;
           }



            return false;
        }
    }



}
