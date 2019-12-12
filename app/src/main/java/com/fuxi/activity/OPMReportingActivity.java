package com.fuxi.activity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import com.fuxi.adspter.OPMReportingInfoDatasAdspter;
import com.fuxi.adspter.OPMReportingRankingListDatasAdspter;
import com.fuxi.adspter.OPMReportingSubTypeDatasAdspter;
import com.fuxi.adspter.OPMReportingTypeDatasAdspter;
import com.fuxi.main.R;
import com.fuxi.util.Logger;
import com.fuxi.util.RandomColorUtil;
import com.fuxi.vo.RandomColor;
import com.fuxi.vo.RequestVo;
import com.fuxi.widget.CheckBoxTextView;
import com.fuxi.widget.FontTextView;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Legend.LegendPosition;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;

/**
 * Title: OPMReportingActivity Description: 订货会系统报表查询界面
 * 
 * @author LYJ
 * 
 */
public class OPMReportingActivity extends BaseWapperActivity {

    private static final String TAG = "OPMReportingActivity";
    private static final String getOrderGoodsInfoReportMethod = "/OPMManager.do?getOrderGoodsInfo";
    private static final String getOrderGoodsTypeReportMethod = "/OPMManager.do?getOrderGoodsType";
    private static final String getOrderRankingListReportMethod = "/OPMManager.do?getOrderRankingList";

    private CheckBoxTextView cbtvGoodsInfo;
    private CheckBoxTextView cbtvGoodsSubType;
    private CheckBoxTextView cbtvGoodsType;
    private CheckBoxTextView cbtvRankingList;
    private CheckBoxTextView cbtvGraphical;

    // 货品查询
    private RelativeLayout rlGoodsInfo;
    private EditText etGoods;
    private EditText etGoodsName;
    private FontTextView ftvToggle;
    private ListView lvInfoDatas;
    private TextView tvInfoTotalCount;
    private TextView tvInfoTotalAmount;
    // 子类别
    private LinearLayout llGoodsSubType;
    private EditText etGoodsType1;
    private ListView lvSubTypeDatas;
    private TextView tvSubTypeGoodsCount;
    private TextView tvSubTypeColorCount;
    private TextView tvSubTypeTotalCount;
    private TextView tvSubTypeCountProportion;
    private TextView tvSubTypeTotalAmount;
    private TextView tvSubTypeAmountProportion;
    // 货品类别
    private LinearLayout llGoodsType;
    private ListView lvGoodsTypeDatas;
    private TextView tvGoodsTypeTotalCount;
    private TextView tvGoodsTypeCountProportion;
    private TextView tvGoodsTypeTotalAmount;
    private TextView tvGoodsTypeAmountProportion;
    // 排行榜
    private LinearLayout llRankingList;
    private EditText etGoodsType2;
    private ListView lvRankingListDatas;
    private TextView tvRankingListTotalCount;
    // 图像
    private LinearLayout llGraphical;
    private PieChart pieChart;

    private TouchListener tl = new TouchListener();
    private List<Map<String, Object>> infoDatas = new ArrayList<Map<String, Object>>();
    private List<Map<String, Object>> subTypeDatas = new ArrayList<Map<String, Object>>();
    private List<Map<String, Object>> goodsTypeDatas = new ArrayList<Map<String, Object>>();
    private List<Map<String, Object>> rankingListDatas = new ArrayList<Map<String, Object>>();
    private List<Map<String, Object>> graphicalListDatas = new ArrayList<Map<String, Object>>();
    private OPMReportingInfoDatasAdspter infoDatasAdspter;
    private OPMReportingSubTypeDatasAdspter subTypeDatasAdspter;
    private OPMReportingTypeDatasAdspter goodsTypeDatasAdspter;
    private OPMReportingRankingListDatasAdspter rankingListDatasAdspter;
    private AlertDialog alertDialog;

    private boolean inputFlag; // 输入框是否可用的标志
    private int graphicalType = 1;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cbtv_goods_info:
                cbtvGoodsInfo.setChecked(true);
                cbtvGoodsSubType.setChecked(false);
                cbtvGoodsType.setChecked(false);
                cbtvRankingList.setChecked(false);
                cbtvGraphical.setChecked(false);
                rlGoodsInfo.setVisibility(View.VISIBLE);
                llGoodsSubType.setVisibility(View.GONE);
                llGoodsType.setVisibility(View.GONE);
                llRankingList.setVisibility(View.GONE);
                llGraphical.setVisibility(View.GONE);
                etGoods.requestFocus();
                setHeadRightVisibility(View.INVISIBLE);
                break;
            case R.id.cbtv_goods_sub_type:
                cbtvGoodsInfo.setChecked(false);
                cbtvGoodsSubType.setChecked(true);
                cbtvGoodsType.setChecked(false);
                cbtvRankingList.setChecked(false);
                cbtvGraphical.setChecked(false);
                rlGoodsInfo.setVisibility(View.GONE);
                llGoodsSubType.setVisibility(View.VISIBLE);
                llGoodsType.setVisibility(View.GONE);
                llRankingList.setVisibility(View.GONE);
                llGraphical.setVisibility(View.GONE);
                setHeadRightVisibility(View.INVISIBLE);
                break;
            case R.id.cbtv_goods_type:
                cbtvGoodsInfo.setChecked(false);
                cbtvGoodsSubType.setChecked(false);
                cbtvGoodsType.setChecked(true);
                cbtvRankingList.setChecked(false);
                cbtvGraphical.setChecked(false);
                rlGoodsInfo.setVisibility(View.GONE);
                llGoodsSubType.setVisibility(View.GONE);
                llGoodsType.setVisibility(View.VISIBLE);
                llRankingList.setVisibility(View.GONE);
                llGraphical.setVisibility(View.GONE);
                queryGoodsTypeReport();
                setHeadRightVisibility(View.INVISIBLE);
                break;
            case R.id.cbtv_ranking_list:
                cbtvGoodsInfo.setChecked(false);
                cbtvGoodsSubType.setChecked(false);
                cbtvGoodsType.setChecked(false);
                cbtvRankingList.setChecked(true);
                cbtvGraphical.setChecked(false);
                rlGoodsInfo.setVisibility(View.GONE);
                llGoodsSubType.setVisibility(View.GONE);
                llGoodsType.setVisibility(View.GONE);
                llRankingList.setVisibility(View.VISIBLE);
                llGraphical.setVisibility(View.GONE);
                setHeadRightVisibility(View.INVISIBLE);
                break;
            case R.id.cbtv_graphical:
                cbtvGoodsInfo.setChecked(false);
                cbtvGoodsSubType.setChecked(false);
                cbtvGoodsType.setChecked(false);
                cbtvRankingList.setChecked(false);
                cbtvGraphical.setChecked(true);
                rlGoodsInfo.setVisibility(View.GONE);
                llGoodsSubType.setVisibility(View.GONE);
                llGoodsType.setVisibility(View.GONE);
                llRankingList.setVisibility(View.GONE);
                llGraphical.setVisibility(View.VISIBLE);
                showPieChartReport();
                setHeadRightVisibility(View.VISIBLE);
                break;
            case R.id.toggle:
                if (inputFlag) {
                    etGoods.setText(null);
                    etGoods.setFocusable(false);
                    etGoods.setOnTouchListener(tl);
                    Drawable drawable = getResources().getDrawable(R.drawable.input_bg);
                    drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight()); // 此为必须写的
                    etGoods.setCompoundDrawables(null, null, drawable, null);
                    SpannableString s = new SpannableString("点击选择货品");
                    etGoods.setHint(s);
                } else {
                    etGoods.setText(null);
                    etGoods.setFocusable(true);
                    etGoods.setFocusableInTouchMode(true);
                    etGoods.setClickable(true);
                    etGoods.setOnTouchListener(null);
                    etGoods.setCompoundDrawables(null, null, null, null);
                    SpannableString s = new SpannableString("输入条码/货号");
                    etGoods.setHint(s);
                    etGoods.requestFocus();
                }
                inputFlag = !inputFlag;
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onHeadRightButton(View v) {
        final String[] items = {"数量占比", "金额占比"};
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
        alertBuilder.setTitle("选择数据显示类型");
        alertBuilder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int index) {
                if (index == 0) {
                    graphicalType = 1;
                } else if (index == 1) {
                    graphicalType = 2;
                }
                // 重新查询数据
                showPieChartReport();
                // 关闭提示框
                alertDialog.dismiss();
            }
        });
        alertDialog = alertBuilder.create();
        alertDialog.show();
    }

    @Override
    protected void loadViewLayout() {
        setContentView(R.layout.activity_opm_reporting);
        setTitle("订 货 会 报 表 查 询");
    }

    @Override
    protected void setListener() {
        // 模块按钮
        cbtvGoodsInfo.setOnClickListener(this);
        cbtvGoodsSubType.setOnClickListener(this);
        cbtvGoodsType.setOnClickListener(this);
        cbtvRankingList.setOnClickListener(this);
        cbtvGraphical.setOnClickListener(this);
        // 货品查询
        etGoods.setOnEditorActionListener(new BarcodeActionListener());
        ftvToggle.setOnClickListener(this);
        infoDatasAdspter = new OPMReportingInfoDatasAdspter(this, infoDatas);
        lvInfoDatas.setAdapter(infoDatasAdspter);
        // 子类别
        etGoodsType1.setOnTouchListener(tl);
        subTypeDatasAdspter = new OPMReportingSubTypeDatasAdspter(this, subTypeDatas);
        lvSubTypeDatas.setAdapter(subTypeDatasAdspter);
        // 类别
        goodsTypeDatasAdspter = new OPMReportingTypeDatasAdspter(this, goodsTypeDatas);
        lvGoodsTypeDatas.setAdapter(goodsTypeDatasAdspter);
        // 排行
        etGoodsType2.setOnTouchListener(tl);
        rankingListDatasAdspter = new OPMReportingRankingListDatasAdspter(this, rankingListDatas);
        lvRankingListDatas.setAdapter(rankingListDatasAdspter);
    }

    @Override
    protected void processLogic() {
        // 显示右上角功能
        setHeadRightVisibility(View.VISIBLE);
        setHeadRightText(R.string.setting);
        inputFlag = true;
        cbtvGoodsInfo.callOnClick();
    }

    @Override
    protected void findViewById() {
        // 模块按钮
        cbtvGoodsInfo = (CheckBoxTextView) findViewById(R.id.cbtv_goods_info);
        cbtvGoodsSubType = (CheckBoxTextView) findViewById(R.id.cbtv_goods_sub_type);
        cbtvGoodsType = (CheckBoxTextView) findViewById(R.id.cbtv_goods_type);
        cbtvRankingList = (CheckBoxTextView) findViewById(R.id.cbtv_ranking_list);
        cbtvGraphical = (CheckBoxTextView) findViewById(R.id.cbtv_graphical);
        // 货品查询
        rlGoodsInfo = (RelativeLayout) findViewById(R.id.rl_goods_info);
        etGoods = (EditText) findViewById(R.id.goods);
        etGoodsName = (EditText) findViewById(R.id.goods_name);
        ftvToggle = (FontTextView) findViewById(R.id.toggle);
        lvInfoDatas = (ListView) findViewById(R.id.info_datas);
        tvInfoTotalCount = (TextView) findViewById(R.id.info_total_count);
        tvInfoTotalAmount = (TextView) findViewById(R.id.info_total_amount);
        // 子类别
        llGoodsSubType = (LinearLayout) findViewById(R.id.ll_goods_sub_type);
        etGoodsType1 = (EditText) findViewById(R.id.goods_type1);
        lvSubTypeDatas = (ListView) findViewById(R.id.sub_type_datas);
        tvSubTypeGoodsCount = (TextView) findViewById(R.id.sub_type_goods_count);
        tvSubTypeColorCount = (TextView) findViewById(R.id.sub_type_color_count);
        tvSubTypeTotalCount = (TextView) findViewById(R.id.sub_type_total_count);
        tvSubTypeCountProportion = (TextView) findViewById(R.id.sub_type_count_proportion);
        tvSubTypeTotalAmount = (TextView) findViewById(R.id.sub_type_total_amount);
        tvSubTypeAmountProportion = (TextView) findViewById(R.id.sub_type_amount_proportion);
        // 货品类别
        llGoodsType = (LinearLayout) findViewById(R.id.ll_goods_type);
        lvGoodsTypeDatas = (ListView) findViewById(R.id.goods_type_datas);
        tvGoodsTypeTotalCount = (TextView) findViewById(R.id.goods_type_total_count);
        tvGoodsTypeCountProportion = (TextView) findViewById(R.id.goods_type_count_proportion);
        tvGoodsTypeTotalAmount = (TextView) findViewById(R.id.goods_type_total_amount);
        tvGoodsTypeAmountProportion = (TextView) findViewById(R.id.goods_type_amount_proportion);
        // 排行榜
        llRankingList = (LinearLayout) findViewById(R.id.ll_ranking_list);
        etGoodsType2 = (EditText) findViewById(R.id.goods_type2);
        lvRankingListDatas = (ListView) findViewById(R.id.ranking_list_datas);
        tvRankingListTotalCount = (TextView) findViewById(R.id.ranking_list_total_count);
        // 图像
        llGraphical = (LinearLayout) findViewById(R.id.ll_graphical);
        pieChart = (PieChart) findViewById(R.id.pieChart);

    }

    /**
     * Title: BarcodeActionListener Description: 条码扫描完成触发Enter事件
     */
    class BarcodeActionListener implements OnEditorActionListener {

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if ((actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_UNSPECIFIED) && ((event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER))) {
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm.isActive()) {
                    imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
                }
                String productId = etGoods.getText().toString().trim();
                queryGoodsCodeReport(productId);
            }
            return false;
        }

    }

    /**
     * 通过条码查询货品信息
     */
    private void queryGoodsCodeReport(String productId) {
        if (null == productId || "".equals(productId) || "null".equalsIgnoreCase(productId)) {
            etGoods.setText(null);
            Toast.makeText(OPMReportingActivity.this, "条码或货号不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        RequestVo vo = new RequestVo();
        vo.requestUrl = getOrderGoodsInfoReportMethod;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("barcode", productId);
        vo.requestDataMap = map;
        super.getDataFromServer(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    if (retObj.getBoolean("success")) {
                        infoDatas.clear();
                        JSONArray jsonArray = retObj.getJSONArray("obj");
                        String goodsName = null;
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            goodsName = jsonObject.getString("GoodsName");
                            Map<String, Object> map = new HashMap<String, Object>();
                            map.put("Color", jsonObject.getString("Color"));
                            map.put("Qty", jsonObject.get("Qty"));
                            map.put("Amt", jsonObject.get("Amt"));
                            infoDatas.add(map);
                        }
                        etGoodsName.setText(goodsName);
                        countInfoDatas();
                    } else {
                        Toast.makeText(OPMReportingActivity.this, retObj.getString("msg"), Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(OPMReportingActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 通过货品类别查询货品订购信息
     */
    private void queryGoodsSubTypeReport(String goodsType) {
        if (null == goodsType || "".equals(goodsType) || "null".equalsIgnoreCase(goodsType)) {
            etGoods.setText(null);
            Toast.makeText(OPMReportingActivity.this, "请选择货品类别", Toast.LENGTH_SHORT).show();
            return;
        }
        RequestVo vo = new RequestVo();
        vo.requestUrl = getOrderGoodsTypeReportMethod;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("goodsType", goodsType);
        vo.requestDataMap = map;
        super.getDataFromServer(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    if (retObj.getBoolean("success")) {
                        subTypeDatas.clear();
                        JSONArray jsonArray = retObj.getJSONArray("obj");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            Map<String, Object> map = new HashMap<String, Object>();
                            map.put("SubType", jsonObject.get("SubType"));
                            map.put("Goods", jsonObject.get("Goods"));
                            map.put("Color", jsonObject.get("Color"));
                            map.put("Quantity", jsonObject.get("Quantity"));
                            map.put("Qty_Rate", jsonObject.get("Qty_Rate"));
                            map.put("Amount", jsonObject.get("Amount"));
                            map.put("Amt_Rate", jsonObject.get("Amt_Rate"));
                            subTypeDatas.add(map);
                        }
                        countSubTypeDatas();
                    } else {
                        Toast.makeText(OPMReportingActivity.this, retObj.getString("msg"), Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(OPMReportingActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 通过货品类别查询货品订购信息
     */
    private void queryGoodsTypeReport() {
        RequestVo vo = new RequestVo();
        vo.requestUrl = getOrderGoodsTypeReportMethod;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("goodsType", "");
        vo.requestDataMap = map;
        super.getDataFromServer(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    if (retObj.getBoolean("success")) {
                        goodsTypeDatas.clear();
                        JSONArray jsonArray = retObj.getJSONArray("obj");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            Map<String, Object> map = new HashMap<String, Object>();
                            map.put("SubType", jsonObject.get("SubType"));
                            map.put("Goods", jsonObject.get("Goods"));
                            map.put("Color", jsonObject.get("Color"));
                            map.put("Quantity", jsonObject.get("Quantity"));
                            map.put("Qty_Rate", jsonObject.get("Qty_Rate"));
                            map.put("Amount", jsonObject.get("Amount"));
                            map.put("Amt_Rate", jsonObject.get("Amt_Rate"));
                            goodsTypeDatas.add(map);
                        }
                        countGoodsTypeDatas();
                    } else {
                        Toast.makeText(OPMReportingActivity.this, retObj.getString("msg"), Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(OPMReportingActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 通过货品类别查询货品排行榜
     */
    private void queryRankingListReport(String goodsType) {
        if (null == goodsType || "".equals(goodsType) || "null".equalsIgnoreCase(goodsType)) {
            etGoods.setText(null);
            Toast.makeText(OPMReportingActivity.this, "请选择货品类别", Toast.LENGTH_SHORT).show();
            return;
        }
        RequestVo vo = new RequestVo();
        vo.requestUrl = getOrderRankingListReportMethod;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("goodsType", goodsType);
        vo.requestDataMap = map;
        super.getDataFromServer(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    if (retObj.getBoolean("success")) {
                        rankingListDatas.clear();
                        JSONArray jsonArray = retObj.getJSONArray("obj");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            Map<String, Object> map = new HashMap<String, Object>();
                            map.put("Code", jsonObject.get("Code"));
                            map.put("Name", jsonObject.get("Name"));
                            map.put("Color", jsonObject.get("Color"));
                            map.put("SN", jsonObject.get("SN"));
                            map.put("Quantity", jsonObject.get("Quantity"));
                            rankingListDatas.add(map);
                        }
                        countRankingListDatas();
                    } else {
                        Toast.makeText(OPMReportingActivity.this, retObj.getString("msg"), Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(OPMReportingActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 通过货品类别查询货品订购信息(用于图形报表显示)
     */
    private void showPieChartReport() {
        RequestVo vo = new RequestVo();
        vo.requestUrl = getOrderGoodsTypeReportMethod;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("goodsType", "");
        vo.requestDataMap = map;
        super.getDataFromServer(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    if (retObj.getBoolean("success")) {
                        graphicalListDatas.clear();
                        JSONArray jsonArray = retObj.getJSONArray("obj");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            Map<String, Object> map = new HashMap<String, Object>();
                            map.put("SubType", jsonObject.get("SubType"));
                            map.put("Qty_Rate", jsonObject.get("Qty_Rate"));
                            map.put("Amt_Rate", jsonObject.get("Amt_Rate"));
                            graphicalListDatas.add(map);
                        }
                        // 使用获取的数据显示饼图
                        PieData pieData = getPieData(graphicalListDatas);
                        showChart(pieChart, pieData);
                    } else {
                        Toast.makeText(OPMReportingActivity.this, retObj.getString("msg"), Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(OPMReportingActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 统计货品查询数据
     */
    private void countInfoDatas() {
        int qtyTotal = 0;
        double amtTotal = 0;
        for (int i = 0; i < infoDatas.size(); i++) {
            Map<String, Object> map = infoDatas.get(i);
            int qty = (Integer) map.get("Qty");
            String amtStr = String.valueOf(map.get("Amt"));
            if (amtStr == null || "".equals(amtStr) || "null".equalsIgnoreCase(amtStr)) {
                amtStr = "0";
            }
            double amt = Double.parseDouble(amtStr);
            qtyTotal += qty;
            amtTotal += amt;
        }
        tvInfoTotalCount.setText(String.valueOf(qtyTotal));
        tvInfoTotalAmount.setText(String.valueOf(new BigDecimal(amtTotal).setScale(2, BigDecimal.ROUND_HALF_UP)));
        infoDatasAdspter.refresh(infoDatas);
    }

    /**
     * 统计货品子类别查询数据
     */
    private void countSubTypeDatas() {
        int goodsTotal = 0, colorTotal = 0, quantityTotal = 0;
        double amountTotal = 0;
        for (int i = 0; i < subTypeDatas.size(); i++) {
            Map<String, Object> map = subTypeDatas.get(i);
            int goods = (Integer) map.get("Goods");
            int color = (Integer) map.get("Color");
            int quantity = (Integer) map.get("Quantity");
            String amountStr = String.valueOf(map.get("Amount"));
            if (amountStr == null || "".equals(amountStr) || "null".equalsIgnoreCase(amountStr)) {
                amountStr = "0";
            }
            double amount = Double.parseDouble(amountStr);
            goodsTotal += goods;
            colorTotal += color;
            quantityTotal += quantity;
            amountTotal += amount;
        }
        tvSubTypeGoodsCount.setText(String.valueOf(goodsTotal));
        tvSubTypeColorCount.setText(String.valueOf(colorTotal));
        tvSubTypeTotalCount.setText(String.valueOf(quantityTotal));
        tvSubTypeTotalAmount.setText(String.valueOf(new BigDecimal(amountTotal).setScale(2, BigDecimal.ROUND_HALF_UP)));
        subTypeDatasAdspter.refresh(subTypeDatas);
    }

    /**
     * 统计货品类别查询数据
     */
    private void countGoodsTypeDatas() {
        int quantityTotal = 0;
        double amountTotal = 0;
        for (int i = 0; i < goodsTypeDatas.size(); i++) {
            Map<String, Object> map = goodsTypeDatas.get(i);
            int quantity = (Integer) map.get("Quantity");
            String amountStr = String.valueOf(map.get("Amount"));
            if (amountStr == null || "".equals(amountStr) || "null".equalsIgnoreCase(amountStr)) {
                amountStr = "0";
            }
            double amount = Double.parseDouble(amountStr);
            quantityTotal += quantity;
            amountTotal += amount;
        }
        tvGoodsTypeTotalCount.setText(String.valueOf(quantityTotal));
        tvGoodsTypeTotalAmount.setText(String.valueOf(new BigDecimal(amountTotal).setScale(2, BigDecimal.ROUND_HALF_UP)));
        goodsTypeDatasAdspter.refresh(goodsTypeDatas);
    }

    /**
     * 统计货品类别数据排行榜
     */
    private void countRankingListDatas() {
        int quantityTotal = 0;
        for (int i = 0; i < rankingListDatas.size(); i++) {
            Map<String, Object> map = rankingListDatas.get(i);
            int quantity = (Integer) map.get("Quantity");
            quantityTotal += quantity;
        }
        tvRankingListTotalCount.setText(String.valueOf(quantityTotal));
        rankingListDatasAdspter.refresh(rankingListDatas);
    }

    private class TouchListener implements OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (v.getId()) {
                case R.id.goods:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Intent intent = new Intent(OPMReportingActivity.this, SelectActivity.class);
                        intent.putExtra("selectType", "selectProduct");
                        startActivityForResult(intent, R.id.goods);
                        overridePendingTransition(R.anim.activity_open, 0);
                    }
                    break;
                case R.id.goods_type1:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Intent intent = new Intent(OPMReportingActivity.this, SelectActivity.class);
                        intent.putExtra("selectType", "selectCategory");
                        startActivityForResult(intent, R.id.goods_type1);
                        overridePendingTransition(R.anim.activity_open, 0);
                    }
                    break;
                case R.id.goods_type2:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Intent intent = new Intent(OPMReportingActivity.this, SelectActivity.class);
                        intent.putExtra("selectType", "selectCategory");
                        startActivityForResult(intent, R.id.goods_type2);
                        overridePendingTransition(R.anim.activity_open, 0);
                    }
                    break;
                default:
                    break;
            }
            return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case R.id.goods:
                if (resultCode == 1) {
                    String goodsCode = data.getStringExtra("Code");
                    queryGoodsCodeReport(goodsCode);
                }
                break;
            case R.id.goods_type1:
                if (resultCode == 1) {
                    String goodsType = data.getStringExtra("Name");
                    etGoodsType1.setText(goodsType);
                    queryGoodsSubTypeReport(goodsType);
                }
                break;
            case R.id.goods_type2:
                if (resultCode == 1) {
                    String goodsType = data.getStringExtra("Name");
                    etGoodsType2.setText(goodsType);
                    queryRankingListReport(goodsType);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void showChart(PieChart pieChart, PieData pieData) {
        pieChart.setHoleColorTransparent(true);
        pieChart.setHoleRadius(60f); // 半径
        pieChart.setTransparentCircleRadius(64f); // 半透明圈
        // pieChart.setHoleRadius(0) //实心圆
        pieChart.setDescription(null);
        // mChart.setDrawYValues(true);
        pieChart.setDrawCenterText(true); // 饼状图中间可以添加文字
        pieChart.setDrawHoleEnabled(true);
        pieChart.setRotationAngle(90); // 初始旋转角度
        // draws the corresponding description value into the slice
        // mChart.setDrawXValues(true);
        // enable rotation of the chart by touch
        pieChart.setRotationEnabled(true); // 可以手动旋转
        // display percentage values
        pieChart.setUsePercentValues(true); // 显示成百分比
        // mChart.setUnit(" €");
        // mChart.setDrawUnitsInChart(true);
        // add a selection listener
        // mChart.setOnChartValueSelectedListener(this);
        // mChart.setTouchEnabled(false);
        // mChart.setOnAnimationListener(this);
        pieChart.setCenterText("货品类别订购比例"); // 饼状图中间的文字
        // 设置数据
        pieChart.setData(pieData);
        // undo all highlights
        // pieChart.highlightValues(null);
        // pieChart.invalidate();
        Legend mLegend = pieChart.getLegend(); // 设置比例图
        mLegend.setPosition(LegendPosition.RIGHT_OF_CHART); // 最右边显示
        // mLegend.setForm(LegendForm.LINE); //设置比例图的形状，默认是方形
        mLegend.setXEntrySpace(7f);
        mLegend.setYEntrySpace(5f);
        pieChart.animateXY(1000, 1000); // 设置动画
        // mChart.spin(2000, 0, 360);
    }

    /**
     * 分成几部分
     * 
     * @param count
     * @param range
     * @return
     */
    private PieData getPieData(List<Map<String, Object>> listDatas) {
        RandomColorUtil d = new RandomColorUtil();
        // 饼图标题
        ArrayList<String> xValues = new ArrayList<String>(); // xVals用来表示每个饼块上的内容
        // 饼图数据
        ArrayList<Entry> yValues = new ArrayList<Entry>(); // yVals用来表示封装每个饼块的实际数据
        // 饼图颜色
        ArrayList<Integer> colors = new ArrayList<Integer>();
        for (int i = 0; i < listDatas.size(); i++) {
            Map<String, Object> map = listDatas.get(i);
            String subType = String.valueOf(map.get("SubType"));
            xValues.add(subType);
            Double qtyRate = (Double) map.get("Qty_Rate");
            Double amtRate = (Double) map.get("Amt_Rate");
            if (graphicalType == 1) {
                yValues.add(new Entry(qtyRate.floatValue(), i));
            } else {
                yValues.add(new Entry(amtRate.floatValue(), i));
            }
            RandomColor rc = d.randomColor();
            colors.add(Color.rgb(((int) rc.r), ((int) rc.g), ((int) rc.b)));
        }
        // y轴的集合
        PieDataSet pieDataSet = new PieDataSet(yValues, "");
        pieDataSet.setSliceSpace(0f); // 设置个饼状图之间的距离
        pieDataSet.setColors(colors);
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float px = 5 * (metrics.densityDpi / 160f);
        pieDataSet.setSelectionShift(px); // 选中态多出的长度
        PieData pieData = new PieData(xValues, pieDataSet);
        return pieData;
    }

}
