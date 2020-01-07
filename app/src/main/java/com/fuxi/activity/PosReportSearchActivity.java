package com.fuxi.activity;

import android.app.Dialog;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.fuxi.main.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


import fule.com.mydatapicker.DataPickerDialog;
import fule.com.mydatapicker.DatePickerDialog;
import fule.com.mydatapicker.DateUtil;
import fule.com.mydatapicker.TimePickerDialog;
import fule.com.mywheelview.bean.AddressDetailsEntity;
import fule.com.mywheelview.bean.AddressModel;
import fule.com.mywheelview.weight.wheel.ChooseAddressWheel;
import fule.com.mywheelview.weight.wheel.OnAddressChangeListener;
import com.fuxi.util.JsonUtil;
import com.fuxi.util.Utils;
public class PosReportSearchActivity extends BaseWapperActivity {

    private EditText begindate;
    private EditText enddate;
    private EditText et_barcode;
    private EditText et_department;
    private EditText et_vip;
    private EditText et_goodstype;
    private EditText et_employee;

    private TextView tv_ret;
    private TextView tv_submit;

    private Dialog dateDialog;
    private Intent intent;
    private int datestr;
    SimpleDateFormat df =new SimpleDateFormat("yyyy-MM-dd");
    private  String departmentid;
    private  String vipId;
    private  String goodsTypeId;
    private  String employeeId;
    private  String barcodeStr;
    private  TouchListener tl=new TouchListener();

    @Override
    protected void loadViewLayout() {
     setContentView(R.layout.activity_posreport_search);
     setTitle("查询");
    }

    @Override
    protected void setListener() {
        begindate.setOnClickListener(this);
        enddate.setOnClickListener(this);
       // et_barcode.setOnClickListener(this);
        et_department.setOnClickListener(this);
        et_employee.setOnClickListener(this);
        et_goodstype.setOnClickListener(this);
        et_vip.setOnClickListener(this);

        et_barcode.setFocusable(true);
        et_barcode.setEnabled(true);
    }

    @Override
    protected void processLogic() {
        begindate.setText(df.format(new Date()));
        enddate.setText(df.format(new Date()));
    }

    @Override
    protected void findViewById() {
       begindate =(EditText) findViewById(R.id.begin_date);
       enddate =(EditText) findViewById(R.id.end_date);
       et_barcode =(EditText) findViewById(R.id.et_barcode);
       et_department =(EditText) findViewById(R.id.et_department);
       et_employee=(EditText) findViewById(R.id.et_employee);
       et_goodstype=(EditText) findViewById(R.id.et_goodstype);
       et_vip =(EditText) findViewById(R.id.et_vip);
       tv_ret =(TextView) findViewById(R.id.tv_ret);
       tv_submit=(TextView)findViewById(R.id.tv_submit);

       tv_ret.setOnTouchListener(tl);
       tv_submit.setOnTouchListener(tl);
    }

    @Override
    public void onClick(View v) {
      switch (v.getId()){
          case R.id.begin_date:
              datestr =1;
              showDateDialog(DateUtil.getDateForString( df.format(new Date())));
              break;
          case R.id.end_date:
              datestr=2;
              showDateDialog(DateUtil.getDateForString( df.format(new Date())));
              break;

          case R.id.et_department:
              intent =new Intent(PosReportSearchActivity.this,SelectActivity.class);
              intent.putExtra("selectType","selectDepartment");
              startActivityForResult(intent,R.id.et_department);
              break;
          case R.id.et_employee:
              intent =new Intent(PosReportSearchActivity.this,SelectActivity.class);
              intent.putExtra("selectType","selectEmployee");
              startActivityForResult(intent,R.id.et_employee);
              break;
          case R.id.et_goodstype:
              intent =new Intent(PosReportSearchActivity.this,SelectActivity.class);
              intent.putExtra("selectType","selectCategory");
              startActivityForResult(intent,R.id.et_goodstype);
              break;
          case R.id.et_vip:
              intent =new Intent(PosReportSearchActivity.this,SelectActivity.class);
              intent.putExtra("selectType","selectVip");
              startActivityForResult(intent,R.id.et_vip);
              break;
          default:
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
            case R.id.et_department:
                if(resultCode==1){
                    et_department.setText(data.getStringExtra("Name"));
                    departmentid = data.getStringExtra("DepartmentID");
                }
                break;
            case R.id.et_vip:
                if(resultCode==1){
                    et_vip.setText("("+data.getStringExtra("Code")+")"+data.getStringExtra("Name"));
                    vipId=data.getStringExtra("VIPID");
                }
                break;
            case R.id.et_goodstype:
                 goodsTypeId=data.getStringExtra("GoodsTypeID");
                 et_goodstype.setText(data.getStringExtra("Name"));
                break;
            case R.id.et_employee:
                 employeeId=data.getStringExtra("EmployeeID");
                 et_employee.setText(data.getStringExtra("Name"));
                break;
            default:
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
                if(datestr ==1) {
                    begindate.setText(String.format("%d-%s-%s", dates[0], dates[1] > 9 ? dates[1] : ("0" + dates[1]), dates[2] > 9 ? dates[2] : ("0" + dates[2])));
                }else if(datestr==2){
                    enddate.setText(String.format("%d-%s-%s", dates[0], dates[1] > 9 ? dates[1] : ("0" + dates[1]), dates[2] > 9 ? dates[2] : ("0" + dates[2])));
                }

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

    public  void ret(){
        et_barcode.setText("");
        et_department.setText("");
        et_vip.setText("");
        et_goodstype.setText("");
        et_employee.setText("");
        departmentid="";
        vipId="";
        goodsTypeId="";
        employeeId="";
        barcodeStr="";
    }

    public  class  TouchListener implements View.OnTouchListener{

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (v.getId()){
                case R.id.tv_ret:
                    if(event.getAction() ==MotionEvent.ACTION_DOWN){
                        ret();
                    }
                   break;
                case R.id.tv_submit:
                    if(event.getAction() ==MotionEvent.ACTION_DOWN){
                        barcodeStr =String.valueOf(et_barcode.getText());
                        Intent intent =new Intent();
                        intent.putExtra("begindate",String.valueOf(begindate.getText()));
                        intent.putExtra("enddate",String.valueOf(enddate.getText()));
                        intent.putExtra("departmentid",departmentid);
                        intent.putExtra("vipid",vipId);
                        intent.putExtra("goodstypeid",goodsTypeId);
                        intent.putExtra("employeeid",employeeId);
                        intent.putExtra("barcode",barcodeStr);
                       setResult(1,intent);
                       finish();
                    }
                    break;
                default:
                    break;
            }
            return false;
        }
    }




}
