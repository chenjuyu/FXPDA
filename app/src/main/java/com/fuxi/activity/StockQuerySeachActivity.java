package com.fuxi.activity;

import android.app.Dialog;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.fuxi.main.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import fule.com.mydatapicker.DatePickerDialog;
import fule.com.mydatapicker.DateUtil;

public class StockQuerySeachActivity extends BaseWapperActivity {

    final String TAG ="StockQuerySeachActivity";

    private EditText etenddate;
    private EditText etbarcode;
    private EditText etdepartment;
    private EditText etseason;
    private EditText etgoodstype;
    private EditText etbrand;


    private String barcode;
    private String department;
    private String season;
    private String goodstype;
    private String brand;
    private String departmentid;
    private String goodsTypeId;
    private String brandid;
    private TextView tvret;
    private TextView tvsubmit;

    private  Intent intent;
    private Dialog dateDialog;
    private SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd");
    private String enddate =df.format(new Date());
    private TouchListener tl=new TouchListener();

    @Override
    protected void loadViewLayout() {
        setContentView(R.layout.activity_stock_search);
        setTitle("查询条件");
    }

    @Override
    protected void setListener() {
         tvret.setOnTouchListener(tl);
         tvsubmit.setOnTouchListener(tl);
        etbarcode.setOnClickListener(this);
         etdepartment.setOnClickListener(this);
         etseason.setOnClickListener(this);
        etgoodstype.setOnClickListener(this);
        etbrand.setOnClickListener(this);
        etenddate.setOnClickListener(this);

        etbarcode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                barcode =s.toString();
             //   Toast.makeText(StockQuerySeachActivity.this,barcode,Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void processLogic() {

    }

    @Override
    protected void findViewById() {
        etenddate=(EditText) findViewById(R.id.end_date);
        etbarcode =(EditText) findViewById(R.id.et_barcode);
        etdepartment =(EditText) findViewById(R.id.et_department);
        etseason =(EditText) findViewById(R.id.et_season);
        etgoodstype=(EditText) findViewById(R.id.et_goodstype);
        etbrand=(EditText) findViewById(R.id.et_brand);

        tvret =(TextView) findViewById(R.id.tv_ret);
        tvsubmit=(TextView) findViewById(R.id.tv_submit);

    }

    @Override
    public void onClick(View v) {

       switch (v.getId()){
           case R.id.end_date:
               showDateDialog(DateUtil.getDateForString( df.format(new Date())));
           break;

           case R.id.et_barcode:
               intent =new Intent(StockQuerySeachActivity.this,SelectActivity.class);
               intent.putExtra("selectType","selectProduct");
               startActivityForResult(intent,R.id.et_barcode);
           break;
           case R.id.et_department:
               intent =new Intent(StockQuerySeachActivity.this,SelectActivity.class);
               intent.putExtra("selectType","selectDepartment");
               startActivityForResult(intent,R.id.et_department);
           break;
           case R.id.et_season:

               intent =new Intent(StockQuerySeachActivity.this,SelectActivity.class);
               intent.putExtra("selectType","selectSeason");
               startActivityForResult(intent,R.id.et_season);
               break;
           case R.id.et_goodstype:
               intent =new Intent(StockQuerySeachActivity.this,SelectActivity.class);
               intent.putExtra("selectType","selectCategory");
               startActivityForResult(intent,R.id.et_goodstype);
           break;
           case R.id.et_brand:
               intent =new Intent(StockQuerySeachActivity.this,SelectActivity.class);
               intent.putExtra("selectType","selectBrand");
               startActivityForResult(intent,R.id.et_brand);
           break;

       }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case R.id.et_barcode:
                if(resultCode==1){
                    etbarcode.setText(data.getStringExtra("Name"));
                    barcode = data.getStringExtra("GoodsID");
                }
            break;
            case R.id.et_department:
                if(resultCode==1){
                    etdepartment.setText(data.getStringExtra("Name"));
                    departmentid = data.getStringExtra("DepartmentID");
                }
            break;
            case R.id.et_goodstype:
                if(resultCode==1) {
                    goodsTypeId = data.getStringExtra("GoodsTypeID");
                    etgoodstype.setText(data.getStringExtra("Name"));
                }
            break;
            case R.id.et_season:
                if(resultCode==1) {
                   season =data.getStringExtra("Name");
                    etseason.setText(data.getStringExtra("Name"));
                }

            break;
            case R.id.et_brand:
                if(resultCode==1) {
                    brandid = data.getStringExtra("BrandID");
                    etbrand.setText(data.getStringExtra("Name"));
                }
            break;
        }


    }

    /**
     * 显示日期
     * @param date
     */
    private void showDateDialog(List<Integer> date) {
        DatePickerDialog.Builder builder = new DatePickerDialog.Builder(this);
        builder.setOnDateSelectedListener(new DatePickerDialog.OnDateSelectedListener() {
            @Override
            public void onDateSelected(int[] dates) {
                etenddate.setText(String.format("%d-%s-%s", dates[0], dates[1] > 9 ? dates[1] : ("0" + dates[1]), dates[2] > 9 ? dates[2] : ("0" + dates[2])));
                enddate =String.format("%d-%s-%s", dates[0], dates[1] > 9 ? dates[1] : ("0" + dates[1]), dates[2] > 9 ? dates[2] : ("0" + dates[2]));

            }

            @Override
            public void onCancel() {

            }
        })
                .setSelectYear(date.get(0) - 1)
                .setSelectMonth(date.get(1) - 1)
                .setSelectDay(date.get(2) - 1);
        builder.setMaxYear(DateUtil.getYear());
        builder.setMaxMonth(DateUtil.getDateForString(DateUtil.getToday()).get(1));
        builder.setMaxDay(DateUtil.getDateForString(DateUtil.getToday()).get(2));
        dateDialog = builder.create();
        dateDialog.show();
    }


    class  TouchListener  implements View.OnTouchListener{

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            switch (v.getId()){
                case R.id.tv_ret:
                    etbarcode.setText("");
                    etdepartment.setText("");
                    etseason.setText("");
                    etgoodstype.setText("");
                    etbrand.setText("");
                    barcode="";
                    department="";
                    season="";
                    goodstype="";
                    brand="";
                 break;
                case R.id.tv_submit:
                    Intent intent =new Intent();

                    intent.putExtra("enddate",enddate);

                    intent.putExtra("departmentid",departmentid);
                    intent.putExtra("goodstypeid",goodsTypeId);
                    intent.putExtra("season",season);
                    intent.putExtra("barcode",barcode);
                    intent.putExtra("brandid",brandid);


                    Log.v(TAG,"结束时间:"+enddate);
                    Log.v(TAG,"departmentid:"+departmentid);
                    Log.v(TAG,"brandid:"+brandid);
                    Log.v(TAG,"goodstypeid:"+goodsTypeId);
                    Log.v(TAG,"season:"+season);
                    Log.v(TAG,"barcode:"+barcode);
                    setResult(1,intent);
                    finish();

                 break;
            }
            return false;
        }
    }


}
