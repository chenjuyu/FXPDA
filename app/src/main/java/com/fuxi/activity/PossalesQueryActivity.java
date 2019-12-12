package com.fuxi.activity;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import com.fuxi.adspter.PossalesAdspter;
import com.fuxi.adspter.PossalesDetailAdspter;
import com.fuxi.main.R;
import com.fuxi.util.Logger;
import com.fuxi.util.LoginParameterUtil;
import com.fuxi.util.Tools;
import com.fuxi.vo.RequestVo;
import com.fuxi.widget.CheckBoxTextView;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemLongClickListener;

/**
 * Title: PossalesQueryActivity Description: 零售管理销售查询活动界面
 * 
 * @author LYJ
 * 
 */
public class PossalesQueryActivity extends BaseWapperActivity implements OnItemLongClickListener  {

    private static final String TAG = "PossalesQueryActivity";
    private static String queryPossalesDetailPath = "/salesTicket.do?getPossalesDetail";
    private static String queryPossalesPath = "/salesTicket.do?getPossales";
    private static String getPossalesDetailByNo = "/salesTicket.do?getPossalesDetailByNo";

    private LinearLayout llPossalesDetail;
    private LinearLayout llPossales;
    private CheckBoxTextView cbtvPossalesDetail;
    private CheckBoxTextView cbtvPossales;
    private EditText etBegindate;
    private EditText etEnddate;
    private TextView tvSearch;
    private TextView tvQuantitySum;
    private TextView tvDiscount;
    private TextView tvAmount;
    private TextView tvTotalCount;
    private TextView tvTotalAmount;
    private TextView tvTotalFact;
    private ListView lvPossalesDetailDatas;
    private ListView lvPossalesDatas;

    private String selectDate;
    private String beginDate;
    private String endDate;
    private int viewId;

    private List<Map<String, Object>> possalesDatas = new ArrayList<Map<String, Object>>();
    private List<Map<String, Object>> possalesDetailDatas = new ArrayList<Map<String, Object>>();
    private TouchListener tl = new TouchListener();
    private PossalesDetailAdspter possalesDetailAdspter;
    private PossalesAdspter possalesAdspter;
    private Intent printIntent = new Intent("COM.QSPDA.PRINTTEXT");

    /**
     * 查询销售明细表
     */
    private void queryPossalesDetailReport() {
        if (null == beginDate || "".equals(beginDate) || "null".equalsIgnoreCase(beginDate)) {
            Toast.makeText(PossalesQueryActivity.this, "开始日期不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (null == endDate || "".equals(endDate) || "null".equalsIgnoreCase(endDate)) {
            Toast.makeText(PossalesQueryActivity.this, "结束日期不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        RequestVo vo = new RequestVo();
        vo.requestUrl = queryPossalesDetailPath;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("beginDate", beginDate);
        map.put("endDate", endDate);
        vo.requestDataMap = map;
        super.getDataFromServer(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    if (retObj.getBoolean("success")) {
                        possalesDetailDatas.clear();
                        JSONArray array = retObj.getJSONArray("obj");
                        for (int i = 0; i < array.length(); i++) {
                            Map temp = new HashMap();
                            JSONObject json = array.getJSONObject(i);
                            Iterator ite = json.keys();
                            while (ite.hasNext()) {
                                String key = ite.next().toString();
                                String value = json.getString(key);
                                temp.put(key, value);
                            }
                            possalesDetailDatas.add(temp);
                        }
                        countPossalesDetail();
                    } else {
                        Toast.makeText(PossalesQueryActivity.this, retObj.getString("msg"), Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(PossalesQueryActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    private void countPossalesDetail() {
        int qtysum = 0;
        double discountSum = 0;
        double amountSum = 0;
        for (int i = 0; i < possalesDetailDatas.size(); i++) {
            Map<String, Object> map = possalesDetailDatas.get(i);
            int quantity = Integer.parseInt(String.valueOf(map.get("Quantity")));
            double discount = Double.parseDouble(String.valueOf(map.get("Discount")));
            double amount = Double.parseDouble(String.valueOf(map.get("Amount")));
            qtysum += quantity;
            discountSum += discount;
            amountSum += amount;
        }
        tvQuantitySum.setText(String.valueOf(qtysum));
        tvDiscount.setText(String.valueOf(new BigDecimal(discountSum).setScale(2, BigDecimal.ROUND_HALF_UP)));
        tvAmount.setText(String.valueOf(new BigDecimal(amountSum).setScale(2, BigDecimal.ROUND_HALF_UP)));
        possalesDetailAdspter.refresh(possalesDetailDatas);
    }

    /**
     * 查询销售单
     */
    private void queryPossalesReport() {
        if (null == beginDate || "".equals(beginDate) || "null".equalsIgnoreCase(beginDate)) {
            Toast.makeText(PossalesQueryActivity.this, "开始日期不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (null == endDate || "".equals(endDate) || "null".equalsIgnoreCase(endDate)) {
            Toast.makeText(PossalesQueryActivity.this, "结束日期不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        RequestVo vo = new RequestVo();
        vo.requestUrl = queryPossalesPath;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("beginDate", beginDate);
        map.put("endDate", endDate);
        vo.requestDataMap = map;
        super.getDataFromServer(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    if (retObj.getBoolean("success")) {
                        possalesDatas.clear();
                        JSONArray array = retObj.getJSONArray("obj");
                        for (int i = 0; i < array.length(); i++) {
                            Map temp = new HashMap();
                            JSONObject json = array.getJSONObject(i);
                            Iterator ite = json.keys();
                            while (ite.hasNext()) {
                                String key = ite.next().toString();
                                String value = json.getString(key);
                                temp.put(key, value);
                            }
                            possalesDatas.add(temp);
                        }
                        countPossales();
                    } else {
                        Toast.makeText(PossalesQueryActivity.this, retObj.getString("msg"), Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(PossalesQueryActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }
    
    /**
     * 查询销售单明细(重打小票)
     */
    private void getPOSsalesDetails(final String no, final String vipCode) {
        RequestVo vo = new RequestVo();
        vo.requestUrl = getPossalesDetailByNo;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("no", no);
        vo.requestDataMap = map;
        super.getDataFromServer(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    if (retObj.getBoolean("success")) {
                        JSONObject jsonMap = retObj.getJSONObject("obj");
                        JSONObject jsonObject = jsonMap.getJSONObject("possales");
                        JSONArray jsonArray = jsonMap.getJSONArray("possalesDetail");
                        print(no, vipCode, jsonObject, jsonArray);
                    }
                } catch (Exception e) {
                    Toast.makeText(PossalesQueryActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    private void countPossales() {
        int qtysum = 0;
        double discountSum = 0;
        double amountSum = 0;
        for (int i = 0; i < possalesDatas.size(); i++) {
            Map<String, Object> map = possalesDatas.get(i);
            int quantity = Integer.parseInt(String.valueOf(map.get("QuantitySum")));
            double discount = Double.parseDouble(String.valueOf(map.get("AmountSum")));
            double amount = Double.parseDouble(String.valueOf(map.get("FactAmt")));
            qtysum += quantity;
            discountSum += discount;
            amountSum += amount;
        }
        tvTotalCount.setText(String.valueOf(qtysum));
        tvTotalAmount.setText(String.valueOf(new BigDecimal(discountSum).setScale(2, BigDecimal.ROUND_HALF_UP)));
        tvTotalFact.setText(String.valueOf(new BigDecimal(amountSum).setScale(2, BigDecimal.ROUND_HALF_UP)));
        possalesAdspter.refresh(possalesDatas);
    }

    @Override
    public void onClick(View v) {
        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
        }
        switch (v.getId()) {
            case R.id.cbtv_possales_detail:
                cbtvPossales.setChecked(false);
                cbtvPossalesDetail.setChecked(true);
                llPossales.setVisibility(View.INVISIBLE);
                llPossalesDetail.setVisibility(View.VISIBLE);
                queryPossalesDetailReport();
                break;
            case R.id.cbtv_possales:
                cbtvPossales.setChecked(true);
                cbtvPossalesDetail.setChecked(false);
                llPossales.setVisibility(View.VISIBLE);
                llPossalesDetail.setVisibility(View.INVISIBLE);
                queryPossalesReport();
                break;
            case R.id.search:
                if (cbtvPossalesDetail.isChecked()) {
                    queryPossalesDetailReport();
                } else {
                    queryPossalesReport();
                }
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
    protected void loadViewLayout() {
        setContentView(R.layout.activity_possales_query);
        setTitle("零售小票查询");
    }

    @Override
    protected void setListener() {
        cbtvPossalesDetail.setOnClickListener(this);
        cbtvPossales.setOnClickListener(this);
        tvSearch.setOnClickListener(this);
        etBegindate.setOnTouchListener(tl);
        etEnddate.setOnTouchListener(tl);
        possalesDetailAdspter = new PossalesDetailAdspter(this, possalesDetailDatas);
        lvPossalesDetailDatas.setAdapter(possalesDetailAdspter);
        possalesAdspter = new PossalesAdspter(this, possalesDatas);
        lvPossalesDatas.setAdapter(possalesAdspter);
        lvPossalesDatas.setOnItemLongClickListener(this);
    }

    @Override
    protected void processLogic() {
        String date = Tools.dateToString(new Date());
        beginDate = date;
        endDate = date;
        etBegindate.setText(beginDate);
        etEnddate.setText(endDate);
        // 设置默认选中
        cbtvPossalesDetail.callOnClick();

    }

    @Override
    protected void findViewById() {

        cbtvPossalesDetail = (CheckBoxTextView) findViewById(R.id.cbtv_possales_detail);
        cbtvPossales = (CheckBoxTextView) findViewById(R.id.cbtv_possales);
        etBegindate = (EditText) findViewById(R.id.begindate);
        etEnddate = (EditText) findViewById(R.id.enddate);
        tvSearch = (TextView) findViewById(R.id.search);

        llPossalesDetail = (LinearLayout) findViewById(R.id.ll_possales_detail);
        lvPossalesDetailDatas = (ListView) findViewById(R.id.lv_possales_detail_datas);
        tvQuantitySum = (TextView) findViewById(R.id.quantity_sum);
        tvDiscount = (TextView) findViewById(R.id.discount);
        tvAmount = (TextView) findViewById(R.id.amount);

        llPossales = (LinearLayout) findViewById(R.id.ll_possales);
        lvPossalesDatas = (ListView) findViewById(R.id.lv_possales_datas);
        tvTotalCount = (TextView) findViewById(R.id.total_count);
        tvTotalAmount = (TextView) findViewById(R.id.total_amount);
        tvTotalFact = (TextView) findViewById(R.id.total_fact);
    }

    /**
     * DatePickerDialog的选择监听事件
     */
    private DatePickerDialog.OnDateSetListener dateListener = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {
            selectDate = year + "-" + (month + 1) + "-" + day;
            try {
                selectDate = Tools.dateStringToDate(selectDate);
            } catch (ParseException e) {
                Toast.makeText(PossalesQueryActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                Logger.e(TAG, e.getMessage());
            }
            setDate(viewId);
        }

    };

    /**
     * 动态显示日期的值
     * 
     * @param viewId
     */
    private void setDate(int viewId) {
        switch (viewId) {
            case R.id.begindate:
                beginDate = selectDate;
                etBegindate.setText(selectDate);
                break;
            case R.id.enddate:
                endDate = selectDate;
                etEnddate.setText(selectDate);
                break;
            default:
                break;
        }
    }

    private class TouchListener implements OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (v.getId()) {
                case R.id.begindate:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        viewId = R.id.begindate;
                        DatePickerDialog pickerDialog1 = new DatePickerDialog(PossalesQueryActivity.this, AlertDialog.THEME_HOLO_LIGHT, dateListener, Tools.year, Tools.month, Tools.day);
                        pickerDialog1.setCancelable(false);
                        pickerDialog1.show();
                    }
                    break;
                case R.id.enddate:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        viewId = R.id.enddate;
                        DatePickerDialog pickerDialog2 = new DatePickerDialog(PossalesQueryActivity.this, AlertDialog.THEME_HOLO_LIGHT, dateListener, Tools.year, Tools.month, Tools.day);
                        pickerDialog2.setCancelable(false);
                        pickerDialog2.show();
                    }
                    break;
                default:
                    break;
            }
            return false;
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        Map<String, Object> map = possalesDatas.get(position);
        final String no = String.valueOf(map.get("No"));
        final String vipCode = String.valueOf(map.get("VIPCode"));
        Builder dialog = new AlertDialog.Builder(PossalesQueryActivity.this, AlertDialog.THEME_HOLO_LIGHT);
        dialog.setTitle("提示");
        dialog.setMessage("是否重新打印单号为 " + no + " 的小票？");
        // 相当于点击确认按钮
        dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                boolean browseRight = LoginParameterUtil.posSalesRightMap.get("BrowseRight");
                if(!browseRight){
                    Toast.makeText(PossalesQueryActivity.this, "当前暂无打印销售小票的权限", Toast.LENGTH_SHORT).show();
                    return;
                }
                getPOSsalesDetails(no,vipCode);
            }
        });
        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {}
        });
        dialog.setCancelable(false);
        dialog.create();
        dialog.show();
        return true;
    }
    
    /**
     * 打印小票
     */
    private void print(String posSalesNo, String vipCode,JSONObject jsonObject, JSONArray jsonArray) {
        try {
            if (jsonArray.length() == 0) {
                Toast.makeText(PossalesQueryActivity.this, "当前没有小票数据", Toast.LENGTH_SHORT).show();
                return;
            }
            // 封装打印数据
            StringBuilder sb = new StringBuilder();
            if (LoginParameterUtil.possalesTile != null && !"".equals(LoginParameterUtil.possalesTile) && !"null".equalsIgnoreCase(LoginParameterUtil.possalesTile)) {
                int len = Tools.countStringLength(LoginParameterUtil.possalesTile);
                len = Double.valueOf(Math.floor((32 - len) / 2)).intValue();
                String emptyCharacter = Tools.generalEmptyCharacter(len);
                sb.append(emptyCharacter + LoginParameterUtil.possalesTile + emptyCharacter);
            }
            if (LoginParameterUtil.possalesParam1 != null && !"".equals(LoginParameterUtil.possalesParam1) && !"null".equalsIgnoreCase(LoginParameterUtil.possalesParam1)) {
                int len = Tools.countStringLength(LoginParameterUtil.possalesParam1);
                len = Double.valueOf(Math.floor((32 - len) / 2)).intValue();
                String emptyCharacter = Tools.generalEmptyCharacter(len);
                sb.append(emptyCharacter + LoginParameterUtil.possalesParam1 + emptyCharacter);
            }
            if (LoginParameterUtil.possalesParam2 != null && !"".equals(LoginParameterUtil.possalesParam2) && !"null".equalsIgnoreCase(LoginParameterUtil.possalesParam2)) {
                int len = Tools.countStringLength(LoginParameterUtil.possalesParam2 + "   " + LoginParameterUtil.deptName);
                len = Double.valueOf(Math.floor((32 - len) / 2)).intValue();
                String emptyCharacter = Tools.generalEmptyCharacter(len);
                sb.append(emptyCharacter + LoginParameterUtil.possalesParam2 + "   " + LoginParameterUtil.deptName + emptyCharacter);
            }
            sb.append("\n");
            sb.append("开单 : ");
            sb.append(jsonObject.getString("Employee"));
            sb.append("\n");
            sb.append("日期 : ");
            sb.append(jsonObject.getString("Date"));
            sb.append("\n");
            sb.append("打印 : ");
            sb.append(Tools.dateTimeToString(new Date()));
            sb.append("\n");
            sb.append("票号 : ");
            sb.append(posSalesNo);
            sb.append("\n");
            sb.append("--------------------------------");
            sb.append("\n");
            sb.append("商品号");
            sb.append("\n");
            sb.append("商品名   数量*折扣*单价   实收");
            sb.append("\n");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonMap = jsonArray.getJSONObject(i);
                String barCode = jsonMap.getString("Barcode");
                String goodsName = jsonMap.getString("GoodsName");
                String unitPrice = jsonMap.getString("UnitPrice");
                String quantity = jsonMap.getString("Quantity");
                String discountRate = jsonMap.getString("DiscountRate");
                String amount = jsonMap.getString("Amount");
                sb.append(barCode);
                sb.append("\n");
                sb.append(goodsName);
                sb.append("    ");
                sb.append(quantity);
                sb.append("*");
                sb.append(discountRate);
                sb.append("*");
                sb.append(unitPrice);
                sb.append("    ");
                sb.append(amount);
                sb.append("\n");
            }
            sb.append("--------------------------------");
            sb.append("合计 : ");
            sb.append(jsonObject.getString("AmountSum"));
            sb.append("\n");
            sb.append("收银 : ");
            sb.append(jsonObject.getString("FactAmountSum"));
            sb.append("\n");
            if (vipCode != null && !"".equals(vipCode) && !"null".equalsIgnoreCase(vipCode)) {
                sb.append("VIP卡号 : ");
                sb.append(vipCode);
                sb.append("\n");
            }
            sb.append("已售商品数量 : ");
            sb.append(jsonObject.getString("QuantitySum"));
            sb.append("\n");
            sb.append("\n");
            if (LoginParameterUtil.possalesParam3 != null && !"".equals(LoginParameterUtil.possalesParam3) && !"null".equalsIgnoreCase(LoginParameterUtil.possalesParam3)) {
                int len = Tools.countStringLength(LoginParameterUtil.possalesParam3);
                len = Double.valueOf(Math.floor((32 - len) / 2)).intValue();
                String emptyCharacter = Tools.generalEmptyCharacter(len);
                sb.append(emptyCharacter + LoginParameterUtil.possalesParam3 + emptyCharacter);
            }
            if (LoginParameterUtil.possalesParam4 != null && !"".equals(LoginParameterUtil.possalesParam4) && !"null".equalsIgnoreCase(LoginParameterUtil.possalesParam4)) {
                int len = Tools.countStringLength(LoginParameterUtil.possalesParam4);
                len = Double.valueOf(Math.floor((32 - len) / 2)).intValue();
                String emptyCharacter = Tools.generalEmptyCharacter(len);
                sb.append(emptyCharacter + LoginParameterUtil.possalesParam4 + emptyCharacter);
            }
            if (LoginParameterUtil.possalesParam5 != null && !"".equals(LoginParameterUtil.possalesParam5) && !"null".equalsIgnoreCase(LoginParameterUtil.possalesParam5)) {
                int len = Tools.countStringLength(LoginParameterUtil.possalesParam5);
                len = Double.valueOf(Math.floor((32 - len) / 2)).intValue();
                String emptyCharacter = Tools.generalEmptyCharacter(len);
                sb.append(emptyCharacter + LoginParameterUtil.possalesParam5 + emptyCharacter);
            }
            sb.append("\n");
            sb.append("\n");
            sb.append("\n");
            printIntent.putExtra("text", sb.toString());
            sendBroadcast(printIntent);
            Toast.makeText(PossalesQueryActivity.this, "正在打印小票", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(PossalesQueryActivity.this, "打印发生错误，请检查打印参数是否正确并稍后重试", Toast.LENGTH_LONG).show();
            Logger.e(TAG, e.getMessage());
        }
    }

}
