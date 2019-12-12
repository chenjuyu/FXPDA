package com.fuxi.activity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.fuxi.adspter.DailyKnotsAdapter;
import com.fuxi.adspter.SalesOrderOddAdapter;
import com.fuxi.main.R;
import com.fuxi.util.AppManager;
import com.fuxi.util.Logger;
import com.fuxi.util.LoginParameterUtil;
import com.fuxi.util.NetUtil;
import com.fuxi.util.Tools;
import com.fuxi.vo.RequestVo;

/**
 * Title: DailyKnotsActivity Description: 日结活动界面
 * 
 * @author LYJ
 * 
 */
public class DailyKnotsActivity extends BaseWapperActivity {

    private static final String TAG = "DailyKnotsActivity";
    private static final String queryPossalesDetail = "/dailyknots.do?getPossalesDetail";
    private static final String doDailyKnots = "/dailyknots.do?dailyKnots";
    private static final String checkDailyKnots = "/dailyknots.do?checkDailyKnots";
    private EditText etDate;
    private ListView lvData;
    private TextView tvQtySum;
    private TextView tvAmount;

    private String date;
    private String departmentId;
    private String factAmountSum;
    // 日结标志, 默认未日结
    private boolean dayEndFlag = false;
    // 用户权限
    private boolean addRight;
    private boolean modifyRight;
    private boolean browseRight;

    private List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
    private DailyKnotsAdapter salesOddAdapter;
    private TouchListener tl = new TouchListener();
    private Intent printIntent = new Intent("COM.QSPDA.PRINTTEXT");

    /**
     * 获取对应日期的店铺小票明细
     */
    private void getData() {
        RequestVo vo = new RequestVo();
        vo.requestUrl = queryPossalesDetail;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("date", date);
        map.put("departmentId", departmentId);
        vo.requestDataMap = map;
        super.getDataFromServer(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    if (retObj.getBoolean("success")) {
                        dataList.clear();
                        JSONObject jsonMap = retObj.getJSONObject("attributes");
                        factAmountSum = jsonMap.getString("factAmountSum");
                        JSONArray jsonArray = retObj.getJSONArray("obj");
                        if (jsonArray.length() == 0) {
                            Toast.makeText(DailyKnotsActivity.this, "未查询到 " + date + " 的销售记录", Toast.LENGTH_SHORT).show();
                            Logger.e(TAG, "未查询到 " + date + " 的销售记录");
                        }
                        for (int i = 0; i < jsonArray.length(); i++) {
                            Map<String, Object> map = new HashMap<String, Object>();
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            map.put("GoodsCode", jsonObject.getString("GoodsCode"));
                            map.put("GoodsName", jsonObject.getString("GoodsName"));
                            map.put("Color", jsonObject.getString("Color"));
                            map.put("Size", jsonObject.getString("Size"));
                            map.put("Quantity", jsonObject.getInt("Quantity"));
                            map.put("Amount", jsonObject.getString("Amount"));
                            map.put("DiscountRate", jsonObject.getString("DiscountRate"));
                            map.put("UnitPrice", jsonObject.getString("UnitPrice"));
                            map.put("Discount", jsonObject.getString("Discount"));
                            dataList.add(map);
                        }
                        // 刷新ListView
                        salesOddAdapter.refresh();
                        // 计算合计
                        countTotal();
                        // 检查当天是否日结
                        checkDailyKnots();
                    }
                } catch (Exception e) {
                    Toast.makeText(DailyKnotsActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 日结
     */
    private void dailyKnots() {
        RequestVo vo = new RequestVo();
        vo.requestUrl = doDailyKnots;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("date", date);
        map.put("departmentId", departmentId);
        vo.requestDataMap = map;
        super.getDataFromServer(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    if (retObj.getBoolean("success")) {
                        boolean unAuditFlag = retObj.getBoolean("obj");
                        if (!unAuditFlag) {
                            // 日结结果
                            Builder dialog1 = new AlertDialog.Builder(DailyKnotsActivity.this, AlertDialog.THEME_HOLO_LIGHT);
                            dialog1.setTitle("日结结果");
                            dialog1.setMessage("日结失败！\n“" + date + "”前存在未审核的小票！");
                            // 相当于点击确认按钮
                            dialog1.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {}
                            });
                            dialog1.create();
                            dialog1.show();
                        } else {
                            // 日结结果
                            Builder dialog1 = new AlertDialog.Builder(DailyKnotsActivity.this, AlertDialog.THEME_HOLO_LIGHT);
                            dialog1.setTitle("日结结果");
                            dialog1.setMessage("日结成功！\n是否打印日结小票？");
                            // 相当于点击确认按钮
                            dialog1.setPositiveButton("打印", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dayEndFlag = true;
                                    setHeadRightVisibility(View.VISIBLE);
                                    setHeadRightText(R.string.print);
                                    print();
                                }
                            });
                            // 相当于点击取消按钮
                            dialog1.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {}
                            });
                            dialog1.create();
                            dialog1.show();
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(DailyKnotsActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 检查是否日结
     */
    private void checkDailyKnots() {
        RequestVo vo = new RequestVo();
        vo.requestUrl = checkDailyKnots;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("date", date);
        map.put("departmentId", departmentId);
        vo.requestDataMap = map;
        super.getDataFromServer(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    if (retObj.getBoolean("success")) {
                        dayEndFlag = retObj.getBoolean("obj");
                        if (dayEndFlag) {
                            // 已经日结
                            setHeadRightVisibility(View.VISIBLE);
                            setHeadRightText(R.string.print);
                        } else {
                            // 未日结
                            setHeadRightVisibility(View.VISIBLE);
                            setHeadRightText(R.string.day_end);
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(DailyKnotsActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 合计
     */
    private void countTotal() {
        int sum = 0;
        double amount = 0.0;
        for (int j = 0; j < dataList.size(); j++) {
            Map<String, Object> map = dataList.get(j);
            // 计算合计数量和金额
            int num = Integer.parseInt(String.valueOf(map.get("Quantity")));
            double discount = Double.parseDouble(String.valueOf(map.get("Discount") == null ? "0" : map.get("Discount")));
            double unitPrice = Double.parseDouble(String.valueOf(map.get("UnitPrice") == null ? "0" : map.get("UnitPrice")));
            double discountRate = Double.parseDouble(String.valueOf(map.get("DiscountRate") == null ? "0" : map.get("DiscountRate")));
            double tamount = Double.parseDouble(String.valueOf(map.get("Amount") == null ? "0" : map.get("Amount")));
            map.put("UnitPrice", new BigDecimal(unitPrice).setScale(2, BigDecimal.ROUND_HALF_UP));
            map.put("Discount", new BigDecimal(discount).setScale(2, BigDecimal.ROUND_HALF_UP));
            map.put("DiscountRate", new BigDecimal(discountRate).setScale(2, BigDecimal.ROUND_HALF_UP));
            map.put("Amount", new BigDecimal(tamount).setScale(2, BigDecimal.ROUND_HALF_UP));
            amount += tamount;
            sum += num;
        }
        // 合计总数
        tvQtySum.setText(String.valueOf(sum));
        // 合计金额
        tvAmount.setText(new BigDecimal(amount).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
    }

    @Override
    public void onClick(View v) {}

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }

    private class TouchListener implements OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (v.getId()) {
                case R.id.date:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        DatePickerDialog pickerDialog = new DatePickerDialog(DailyKnotsActivity.this, AlertDialog.THEME_HOLO_LIGHT, dateListener, Tools.year, Tools.month, Tools.day);
                        pickerDialog.setCancelable(false);
                        pickerDialog.show();
                    }
                    break;
                default:
                    break;
            }
            return false;
        }
    }

    /**
     * DatePickerDialog的选择监听事件
     */
    private DatePickerDialog.OnDateSetListener dateListener = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {
            String selectDate = year + "-" + (month + 1) + "-" + day;
            date = selectDate;
            etDate.setText(selectDate);
            // 获取新数据
            getData();
        }

    };

    @Override
    protected void onHeadRightButton(View v) {
        if (dayEndFlag) {
            // 重新打印日结小票
            print();
        } else {
            // 日结前提示
            Builder dialog1 = new AlertDialog.Builder(DailyKnotsActivity.this, AlertDialog.THEME_HOLO_LIGHT);
            dialog1.setTitle("日结提示");
            dialog1.setMessage("日结后不能再录入当日的销售小票和赠品单，是否对店铺截止至 “" + date + "” 进行日结？");
            // 相当于点击确认按钮
            dialog1.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (addRight && modifyRight) {
                        dailyKnots();
                    } else {
                        Builder dialog2 = new AlertDialog.Builder(DailyKnotsActivity.this, AlertDialog.THEME_HOLO_LIGHT);
                        dialog2.setTitle("提示");
                        dialog2.setMessage("当前暂无日结权限");
                        // 相当于点击确认按钮
                        dialog2.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {}
                        });
                        dialog2.setCancelable(false);
                        dialog2.create();
                        dialog2.show();
                    }
                }
            });
            // 相当于点击取消按钮
            dialog1.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {}
            });
            dialog1.create();
            dialog1.show();
        }
    }

    @Override
    protected void loadViewLayout() {
        setContentView(R.layout.activity_daily_knots);
        setTitle("日 结");
        setHeadRightVisibility(View.VISIBLE);
        // 显示右上角功能
        setHeadRightVisibility(View.VISIBLE);
        setHeadRightText(R.string.day_end);
    }

    @Override
    protected void setListener() {
        salesOddAdapter = new DailyKnotsAdapter(this, dataList);
        lvData.setAdapter(salesOddAdapter);
        etDate.setOnTouchListener(tl);
    }

    @Override
    protected void processLogic() {
        if (NetUtil.hasNetwork(getApplicationContext())) {
            if (LoginParameterUtil.online) {
                // 获取用户操作权限(新增)
                addRight = LoginParameterUtil.dailyKnotsMenuRight.get("AddRight");
                modifyRight = LoginParameterUtil.dailyKnotsMenuRight.get("ModifyRight");
                browseRight = LoginParameterUtil.dailyKnotsMenuRight.get("BrowseRight");
                if (addRight || modifyRight || browseRight) {
                    // 初始化
                    departmentId = LoginParameterUtil.deptId;
                    date = Tools.dateToString(new Date());
                    etDate.setText(date);
                    // 获取销售明细记录
                    getData();
                } else {
                    Builder dialog = new AlertDialog.Builder(DailyKnotsActivity.this, AlertDialog.THEME_HOLO_LIGHT);
                    dialog.setTitle("提示");
                    dialog.setMessage("当前暂无日结的操作权限");
                    // 相当于点击确认按钮
                    dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
                    dialog.setCancelable(false);
                    dialog.create();
                    dialog.show();
                }
                // 检查当前店铺是否日结
                checkDailyKnots();
            } else {
                AlertDialog.Builder dialog = new AlertDialog.Builder(DailyKnotsActivity.this, AlertDialog.THEME_HOLO_LIGHT);
                dialog.setTitle("系统提示");
                dialog.setMessage("当前用户未登录，操作非法！");
                // 相当于点击确认按钮
                dialog.setPositiveButton("退出", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AppManager.getAppManager().AppExit(getApplicationContext());
                    }
                });
                dialog.setCancelable(false);
                dialog.create();
                dialog.show();
            }
        } else {
            AlertDialog.Builder dialog = new AlertDialog.Builder(DailyKnotsActivity.this, AlertDialog.THEME_HOLO_LIGHT);
            dialog.setTitle("系统提示");
            dialog.setMessage("当前为离线状态，此功能不可用！");
            // 相当于点击确认按钮
            dialog.setPositiveButton("返回", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            dialog.setCancelable(false);
            dialog.create();
            dialog.show();
        }
    }

    @Override
    protected void findViewById() {
        etDate = (EditText) findViewById(R.id.date);
        tvQtySum = (TextView) findViewById(R.id.qtysum);
        tvAmount = (TextView) findViewById(R.id.amount);
        lvData = (ListView) findViewById(R.id.sales_detail);
    }

    /**
     * 打印小票
     */
    private void print() {
        try {
            if (!browseRight) {
                Toast.makeText(DailyKnotsActivity.this, "当前暂无打印日结小票权限", Toast.LENGTH_SHORT).show();
                return;
            }
            if (dataList.size() == 0) {
                Toast.makeText(DailyKnotsActivity.this, "当前没有小票数据", Toast.LENGTH_SHORT).show();
                return;
            }
            StringBuilder sb = new StringBuilder();
            if (LoginParameterUtil.possalesTile != null && !"".equals(LoginParameterUtil.possalesTile) && !"null".equalsIgnoreCase(LoginParameterUtil.possalesTile)) {
                int len = Tools.countStringLength(LoginParameterUtil.possalesTile);
                len = Double.valueOf(Math.floor((32 - len) / 2)).intValue();
                String emptyCharacter = Tools.generalEmptyCharacter(len);
                sb.append(emptyCharacter + LoginParameterUtil.possalesTile + emptyCharacter);
            }

            int len = Tools.countStringLength("销售汇总");
            len = Double.valueOf(Math.floor((32 - len) / 2)).intValue();
            String emptyCharacter = Tools.generalEmptyCharacter(len);
            sb.append(emptyCharacter + "销售汇总" + emptyCharacter);

            sb.append("\n");
            sb.append("日期 : ");
            sb.append(date);
            sb.append("\n");
            sb.append("店铺 : ");
            sb.append(LoginParameterUtil.deptName);
            sb.append("\n");
            sb.append("打印时间 : ");
            sb.append(Tools.dateTimeToString(new Date()));
            sb.append("\n");
            sb.append("货号   名称");
            sb.append("\n");
            sb.append("颜色   尺码   数量   实收");
            sb.append("\n");
            sb.append("--------------------------------");
            sb.append("\n");
            for (int i = 0; i < dataList.size(); i++) {
                Map<String, Object> map = dataList.get(i);
                String goodsCode = String.valueOf(map.get("GoodsCode"));
                String goodsName = String.valueOf(map.get("GoodsName"));
                String color = String.valueOf(map.get("Color"));
                String size = String.valueOf(map.get("Size"));
                String amount = String.valueOf(map.get("Amount"));
                String quantity = String.valueOf(map.get("Quantity"));
                sb.append(goodsCode);
                sb.append("    ");
                sb.append(goodsName);
                sb.append("\n");
                sb.append(color);
                sb.append("    ");
                sb.append(size);
                sb.append("    ");
                sb.append(quantity);
                sb.append("    ");
                sb.append(amount);
                sb.append("\n");
            }
            sb.append("--------------------------------");
            sb.append("总数量 : ");
            sb.append(tvQtySum.getText().toString());
            sb.append("\n");
            sb.append("现   金 : ");
            sb.append(factAmountSum);
            sb.append("\n");
            sb.append("总金额 : ");
            sb.append(tvAmount.getText().toString());
            sb.append("\n");
            sb.append("\n");
            sb.append("\n");
            printIntent.putExtra("text", sb.toString());
            sendBroadcast(printIntent);
            Toast.makeText(DailyKnotsActivity.this, "正在打印小票", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(DailyKnotsActivity.this, "打印发生错误，请检查打印参数是否正确并稍后重试", Toast.LENGTH_LONG).show();
            Logger.e(TAG, e.getMessage());
        }
    }

}
