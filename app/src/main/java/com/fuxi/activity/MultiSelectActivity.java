package com.fuxi.activity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.fuxi.adspter.MultiSelectAdspter;
import com.fuxi.adspter.MultiSelectAdspter.Zujian;
import com.fuxi.main.R;
import com.fuxi.util.Logger;
import com.fuxi.vo.RequestVo;

/**
 * Title: MultiSelectActivity Description: 多尺码多颜色选择界面
 * 
 * @author LYJ
 * 
 */
public class MultiSelectActivity extends BaseWapperActivity implements OnItemClickListener {

    private static final String TAG = "MultiSelectActivity";
    private static final String url = "/select.do?getColorAndSize";

    private List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
    private List<Map<String, Object>> colorList = new ArrayList<Map<String, Object>>();
    private List<Map<String, Object>> sizeList = new ArrayList<Map<String, Object>>();
    private List<String> colors = new ArrayList<String>(); // 存储显示的颜色
    private MultiSelectAdspter selectAdspter;
    private AlertDialog alertDialog;

    private EditText etColor;
    private TextView tvSave;
    private ListView lvDatas;

    private String goodsId;

    /**
     * 根据货品ID获取颜色和尺码
     * 
     * @param refresh
     * @param type
     */
    private void getData() {
        RequestVo vo = new RequestVo();
        vo.requestUrl = url;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("goodsId", goodsId);
        vo.requestDataMap = map;
        super.getDataFromServerNoProcess(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    if (retObj.getBoolean("success")) {
                        JSONObject objs = retObj.getJSONObject("obj");
                        // 尺码
                        JSONArray sizeListArray = objs.getJSONArray("sizeList");
                        // 颜色
                        JSONArray colorListArray = objs.getJSONArray("colorList");
                        for (int i = 0; i < colorListArray.length(); i++) {
                            Map temp = new HashMap();
                            JSONObject json = colorListArray.getJSONObject(i);
                            String colorName = json.getString("Name");
                            colors.add(colorName);
                            Iterator ite = json.keys();
                            while (ite.hasNext()) {
                                String key = ite.next().toString();
                                String value = json.getString(key);
                                temp.put(key, value);
                            }
                            List<Map<String, Object>> tsizeList = new ArrayList<Map<String, Object>>();
                            for (int j = 0; j < sizeListArray.length(); j++) {
                                Map tmap = new HashMap();
                                JSONObject jsonObject = sizeListArray.getJSONObject(j);
                                Iterator iterator = jsonObject.keys();
                                while (iterator.hasNext()) {
                                    String key = iterator.next().toString();
                                    String value = jsonObject.getString(key);
                                    tmap.put(key, value);
                                }
                                tmap.put("Quantity", "0");
                                tsizeList.add(tmap);
                            }
                            temp.put("sizeList", tsizeList);
                            colorList.add(temp);
                        }
                        // 默认选择第一个颜色
                        if (colors.size() > 0) {
                            String color = colors.get(0);
                            etColor.setText(color);
                            // 刷新尺码列表
                            changeSizeList(color);
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(MultiSelectActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 选择颜色时刷新对应的尺码及录入的数量
     */
    private void changeSizeList(String color) {
        for (int i = 0; i < colorList.size(); i++) {
            Map<String, Object> map = colorList.get(i);
            String tcolor = String.valueOf(map.get("Name"));
            if (tcolor.equals(color)) {// 获取对应的尺码信息
                sizeList.clear();
                List<Map<String, Object>> tsizeList = (List<Map<String, Object>>) map.get("sizeList");
                sizeList.addAll(tsizeList);
                break;
            }
        }
        selectAdspter.refresh(sizeList);
    }

    /**
     * 保存选取的颜色尺码记录
     */
    private void save() {
        for (int i = 0; i < colorList.size(); i++) {
            Map<String, Object> map = colorList.get(i);
            String tcolor = String.valueOf(map.get("Name"));
            List<Map<String, Object>> tsizeList = (List<Map<String, Object>>) map.get("sizeList");
            for (int j = 0; j < tsizeList.size(); j++) {
                Map<String, Object> tmap = tsizeList.get(j);
                int quantity = Integer.parseInt(String.valueOf(tmap.get("Quantity")));
                if (quantity > 0) {
                    Map<String, Object> temp = new HashMap<String, Object>();
                    temp.put("ColorID", map.get("ColorID"));
                    temp.put("ColorCode", map.get("ColorCode"));
                    temp.put("ColorName", map.get("ColorName"));
                    temp.put("GoodsID", goodsId);
                    temp.put("SizeID", tmap.get("SizeID"));
                    temp.put("SizeCode", tmap.get("SizeCode"));
                    temp.put("SizeName", tmap.get("SizeName"));
                    temp.put("Quantity", tmap.get("Quantity"));
                    dataList.add(temp);
                }
            }
        }
    }

    /**
     * 单选提示框选择已经生成的箱号
     * 
     * @param view
     */
    private void selectGoodsColorAlertDialog(View view) {
        final String[] items = colors.toArray(new String[colors.size()]);
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
        alertBuilder.setTitle("选择颜色");
        alertBuilder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int index) {
                String color = items[index];
                etColor.setText(color);
                // 刷新尺码列表
                changeSizeList(color);
                // 关闭提示框
                alertDialog.dismiss();
            }
        });
        alertDialog = alertBuilder.create();
        alertDialog.show();
    }

    @Override
    public void onClick(View v) {
        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
        }
        switch (v.getId()) {
            case R.id.save:
                // 保存录入的颜色尺码记录
                save();
                Intent inte = new Intent();
                inte.putExtra("datas", (Serializable) dataList);
                setResult(1, inte);
                finish();
                break;
            case R.id.color:
                // 选择货品颜色
                selectGoodsColorAlertDialog(v);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onHeadLeftButton(View v) {
        // 点击返回时返回上一页面
        saveOrNot();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            saveOrNot();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 点击返回后询问是否保存单据内容
     */
    private void saveOrNot() {
        // 询问是否保存修改的发货单数据
        Builder dialog = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
        dialog.setTitle("系统提示");
        dialog.setMessage("多颜色多尺码录入尚未完成,确定要返回吗？");
        // 相当于点击确认按钮
        dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        // 相当于点击取消按钮
        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {}
        });
        dialog.create();
        dialog.show();
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    protected void loadViewLayout() {
        setContentView(R.layout.activity_multi_select);
        setTitle("选择多颜色尺码");
        setHeadRightVisibility(View.INVISIBLE);
    }

    @Override
    protected void setListener() {
        selectAdspter = new MultiSelectAdspter(this, sizeList);
        lvDatas.setAdapter(selectAdspter);
        lvDatas.setOnItemClickListener(this);
        tvSave.setOnClickListener(this);
        etColor.setOnClickListener(this);
    }

    @Override
    protected void processLogic() {
        Bundle bundle = this.getIntent().getExtras();
        if (null != bundle) {
            goodsId = bundle.getString("goodsId");
        }
        // 加载数据
        getData();
    }

    @Override
    protected void findViewById() {
        etColor = (EditText) findViewById(R.id.color);
        lvDatas = (ListView) findViewById(R.id.lv_datas);
        tvSave = (TextView) findViewById(R.id.save);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        ListView listView = (ListView) parent;
        int childCount = listView.getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (i != position) {
                View childAt = listView.getChildAt(i);
                Zujian tag = (Zujian) childAt.getTag();
                EditText tvcount = tag.count;
                tvcount.setEnabled(false);
                tvcount.setFocusable(false);
            }
        }
        Zujian tag = (Zujian) view.getTag();
        final EditText tvcount = tag.count;
        tvcount.setEnabled(true);
        tvcount.setFocusable(true);
        tvcount.setFocusableInTouchMode(true);
        tvcount.requestFocus();
        tvcount.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // changeQuantity();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // changeQuantity();
            }

            @Override
            public void afterTextChanged(Editable s) {
                changeQuantity();
            }

            private void changeQuantity() {
                Map<String, Object> map = sizeList.get(position);
                String quantityStr = tvcount.getText().toString();
                if (!"".equals(quantityStr.trim())) {
                    int count = Integer.parseInt(quantityStr);
                    map.put("Quantity", String.valueOf(count));
                    // tvcount.setText(String.valueOf(count));
                }
            }

        });
        // 减
        final TextView reduce = tag.reduce;
        reduce.setOnTouchListener(new OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                String quantityStr = tvcount.getText().toString();
                if (!"".equals(quantityStr.trim())) {
                    int count = Integer.parseInt(quantityStr);
                    if (count <= 0) {
                        tvcount.setText("0");
                    } else {
                        count--;
                        tvcount.setText(String.valueOf(count));
                    }
                }
                return false;
            }
        });
        // 加
        TextView add = tag.add;
        add.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                String quantityStr = tvcount.getText().toString();
                if (!"".equals(quantityStr.trim())) {
                    int count = Integer.parseInt(quantityStr);
                    count++;
                    tvcount.setText(String.valueOf(count));
                }
                return false;
            }

        });

    }

}
