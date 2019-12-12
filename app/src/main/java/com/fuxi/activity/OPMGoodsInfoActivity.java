package com.fuxi.activity;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import com.fuxi.activity.BaseWapperActivity.DataCallback;
import com.fuxi.main.R;
import com.fuxi.util.Logger;
import com.fuxi.util.OPMLoginParameterUtil;
import com.fuxi.vo.RequestVo;

/**
 * Title: OPMMultiSelectActivity Description: 订货会系统货品资料显示界面
 * 
 * @author LYJ
 * 
 */
public class OPMGoodsInfoActivity extends BaseWapperActivity implements OnItemClickListener {

    private static final String TAG = "OPMGoodsInfoActivity";
    private static final String goodsInfoQueryPathMethod = "/OPMManager.do?goodsInfoQueryPath";

    private EditText etGoodsCode;
    private EditText etGoodsName;
    private EditText etGoodsType;
    private EditText etGoodsSubType;
    private EditText etAge;
    private EditText etSeason;
    private EditText etBrandSerial;
    private EditText etStyle;
    private EditText etSex;
    private EditText etKind;
    private EditText etModel;
    private EditText etRetailSales;

    private String goodsId;

    @Override
    public void onClick(View v) {

    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    protected void loadViewLayout() {
        setContentView(R.layout.activity_opm_goods_info);
        setTitle("订 货 会 货 品 资 料");
        setHeadRightVisibility(View.INVISIBLE);
    }

    @Override
    protected void setListener() {

    }

    @Override
    protected void processLogic() {
        Bundle bundle = this.getIntent().getExtras();
        if (null != bundle) {
            goodsId = bundle.getString("goodsId");
            Map<String, Object> map = (Map<String, Object>) readProduct("GoodsInfoCache", goodsId);
            if (map != null) {
                showGoodsInfo(map);
            } else {
                queryGoodsInfo();
            }
        }
    }

    @Override
    protected void findViewById() {
        etGoodsCode = (EditText) findViewById(R.id.goods_code);
        etGoodsName = (EditText) findViewById(R.id.goods_name);
        etGoodsType = (EditText) findViewById(R.id.goods_type);
        etGoodsSubType = (EditText) findViewById(R.id.goods_sub_type);
        etAge = (EditText) findViewById(R.id.age);
        etSeason = (EditText) findViewById(R.id.season);
        etBrandSerial = (EditText) findViewById(R.id.brand_serial);
        etStyle = (EditText) findViewById(R.id.style);
        etSex = (EditText) findViewById(R.id.sex);
        etKind = (EditText) findViewById(R.id.kind);
        etModel = (EditText) findViewById(R.id.model);
        etRetailSales = (EditText) findViewById(R.id.retail_sales);
    }

    @Override
    protected void onHeadLeftButton(View v) {
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 读取缓存中的对象
     * 
     * @param docName
     * @param objName
     * @return
     */
    public Object readProduct(String docName, String objName) {
        Object obj = null;
        SharedPreferences preferences = getSharedPreferences(docName, MODE_PRIVATE);
        String customerBase64 = preferences.getString(objName, "");
        if (customerBase64 == "") {
            return obj;
        }
        // 读取字节
        byte[] base64 = Base64.decodeBase64(customerBase64.getBytes());
        // 封装到字节流
        ByteArrayInputStream bais = new ByteArrayInputStream(base64);
        try {
            // 再次封装
            ObjectInputStream bis = new ObjectInputStream(bais);
            try {
                // 读取对象
                obj = bis.readObject();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return obj;
    }

    /**
     * 显示货品资料信息
     * 
     * @param map
     */
    private void showGoodsInfo(Map<String, Object> map) {
        etGoodsCode.setText((String) map.get("GoodsCode"));
        etGoodsName.setText((String) map.get("GoodsName"));
        etGoodsType.setText((String) map.get("GoodsType"));
        etGoodsSubType.setText((String) map.get("SubType"));
        etAge.setText((String) map.get("Age"));
        etSeason.setText((String) map.get("Season"));
        etBrandSerial.setText((String) map.get("BrandSerial"));
        etStyle.setText((String) map.get("Style"));
        etSex.setText((String) map.get("Sex"));
        etKind.setText((String) map.get("Kind"));
        etModel.setText((String) map.get("Model"));
        etRetailSales.setText((String) map.get("RetailSales"));
    }

    /**
     * 查询并显示货品资料信息
     */
    private void queryGoodsInfo() {
        if (null == goodsId || "".equals(goodsId) || "null".equalsIgnoreCase(goodsId)) {
            Toast.makeText(OPMGoodsInfoActivity.this, "未指定要查询的货品", Toast.LENGTH_SHORT).show();
            return;
        }
        RequestVo vo = new RequestVo();
        vo.requestUrl = goodsInfoQueryPathMethod;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("goodsId", goodsId);
        vo.requestDataMap = map;
        super.getDataFromServer(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    if (retObj.getBoolean("success")) {
                        JSONObject jsonObject = retObj.getJSONArray("obj").getJSONObject(0);
                        etGoodsCode.setText(jsonObject.getString("GoodsCode"));
                        etGoodsName.setText(jsonObject.getString("GoodsName"));
                        etGoodsType.setText(jsonObject.getString("GoodsType"));
                        etGoodsSubType.setText(jsonObject.getString("SubType"));
                        etAge.setText(jsonObject.getString("Age"));
                        etSeason.setText(jsonObject.getString("Season"));
                        etBrandSerial.setText(jsonObject.getString("BrandSerial"));
                        etStyle.setText(jsonObject.getString("Style"));
                        etSex.setText(jsonObject.getString("Sex"));
                        etKind.setText(jsonObject.getString("Kind"));
                        etModel.setText(jsonObject.getString("Model"));
                        etRetailSales.setText(jsonObject.getString("RetailSales"));
                    } else {
                        Toast.makeText(OPMGoodsInfoActivity.this, retObj.getString("msg"), Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(OPMGoodsInfoActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

}
