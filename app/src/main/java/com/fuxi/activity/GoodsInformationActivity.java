package com.fuxi.activity;

import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONObject;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.fuxi.main.R;
import com.fuxi.util.AppManager;
import com.fuxi.util.Logger;
import com.fuxi.util.LoginParameterUtil;
import com.fuxi.util.NetUtil;
import com.fuxi.vo.RequestVo;
import com.fuxi.widget.CheckBoxTextView;
import com.fuxi.widget.FontTextView;

/**
 * Title: GoodsInformationActivity Description: 货品资料活动界面
 * 
 * @author LYJ
 * 
 */
public class GoodsInformationActivity extends BaseWapperActivity {

    private static final String TAG = "GoodsInformationActivity";
    private static final String queryPath = "/goodsInfo.do?getGoodsInfo";
    private static final String savePath = "/goodsInfo.do?saveGoodsInfo";
    private static final String updatePath = "/goodsInfo.do?updateGoodsInfo";

    private LinearLayout llGoodsInfo;
    private LinearLayout llGoodsPrice;
    private LinearLayout llGoodsColor;
    private LinearLayout llGoodsPic;
    private LinearLayout llGoodsSubType;
    private LinearLayout llBrand;
    private LinearLayout llBrandSerial;
    private LinearLayout llKind;
    private LinearLayout llAge;
    private LinearLayout llSeason;
    private LinearLayout llSupplier;
    private LinearLayout llSupplierCode;
    private LinearLayout llPurchasePrice;
    private LinearLayout llRetailSales;
    private LinearLayout llTradePrice;
    private LinearLayout llRetailSales1;
    private LinearLayout llSalesPrice1;
    private LinearLayout llRetailSales2;
    private LinearLayout llSalesPrice2;
    private LinearLayout llRetailSales3;
    private LinearLayout llSalesPrice3;
    private CheckBoxTextView tvGoodsInfo;
    private CheckBoxTextView tvGoodsPrice;
    private CheckBoxTextView tvGoodsColor;
    private CheckBoxTextView tvGoodsPic;
    private TextView tvAdd;
    private TextView tvUpdate;
    private TextView tvSave;
    private TextView tvFind;
    private TextView tvPurPrice;
    private EditText etGoodsCode;
    private EditText etGoodsName;
    private EditText etGoodsType;
    private EditText etGoodsSubType;
    private EditText etBrand;
    private EditText etBrandSerial;
    private EditText etKind;
    private EditText etAge;
    private EditText etSeason;
    private EditText etSupplier;
    private EditText etSupplierCode;
    private EditText etPurchasePrice;
    private EditText etRetailSales;
    private EditText etTradePrice;
    private EditText etRetailSales1;
    private EditText etRetailSales2;
    private EditText etRetailSales3;
    private EditText etSalesPrice1;
    private EditText etSalesPrice2;
    private EditText etSalesPrice3;
    private EditText etGoodsColor1;
    private EditText etGoodsColor2;
    private EditText etGoodsColor3;
    private EditText etGoodsColor4;
    private EditText etGoodsColor5;
    private EditText etGoodsColor6;
    private EditText etGoodsColor7;
    private EditText etGoodsColor8;
    private FontTextView seasonIcon;
    private FontTextView ageIcon;

    private String goodsId;
    private String brandId;
    private String goodsTypeId;
    private String brandSerialId; // 系列
    private String supplierId; // 厂商
    private String colorId1;
    private String colorId2;
    private String colorId3;
    private String colorId4;
    private String colorId5;
    private String colorId6;
    private String colorId7;
    private String colorId8;
    private boolean saveFlag;// 保存标志
    private boolean goodsAddRight;
    private boolean goodsBrowseRight;
    private boolean goodsModifyRight;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.goods_info:
                llGoodsInfo.setVisibility(View.VISIBLE);
                llGoodsPrice.setVisibility(View.INVISIBLE);
                llGoodsColor.setVisibility(View.INVISIBLE);
                llGoodsPic.setVisibility(View.INVISIBLE);
                tvGoodsInfo.setChecked(true);
                tvGoodsPrice.setChecked(false);
                tvGoodsColor.setChecked(false);
                tvGoodsPic.setChecked(false);
                break;
            case R.id.goods_price:
                llGoodsPrice.setVisibility(View.VISIBLE);
                llGoodsInfo.setVisibility(View.INVISIBLE);
                llGoodsColor.setVisibility(View.INVISIBLE);
                llGoodsPic.setVisibility(View.INVISIBLE);
                tvGoodsPrice.setChecked(true);
                tvGoodsInfo.setChecked(false);
                tvGoodsColor.setChecked(false);
                tvGoodsPic.setChecked(false);
                break;
            case R.id.goods_color:
                llGoodsColor.setVisibility(View.VISIBLE);
                llGoodsInfo.setVisibility(View.INVISIBLE);
                llGoodsPrice.setVisibility(View.INVISIBLE);
                llGoodsPic.setVisibility(View.INVISIBLE);
                tvGoodsColor.setChecked(true);
                tvGoodsInfo.setChecked(false);
                tvGoodsPrice.setChecked(false);
                tvGoodsPic.setChecked(false);
                break;
            case R.id.goods_pic:
                llGoodsPic.setVisibility(View.VISIBLE);
                llGoodsColor.setVisibility(View.INVISIBLE);
                llGoodsInfo.setVisibility(View.INVISIBLE);
                llGoodsPrice.setVisibility(View.INVISIBLE);
                tvGoodsPic.setChecked(true);
                tvGoodsColor.setChecked(false);
                tvGoodsInfo.setChecked(false);
                tvGoodsPrice.setChecked(false);
                // 权限控制
                if (goodsModifyRight) {
                    String goodsCode = etGoodsCode.getText().toString().trim();
                    if (null != goodsCode && !goodsCode.isEmpty()) {
                        Intent inten = new Intent(GoodsInformationActivity.this, PictureActivity.class);
                        inten.putExtra("goodsCode", goodsCode);
                        startActivity(inten);
                    } else {
                        Toast.makeText(GoodsInformationActivity.this, "请先指定图片的货品编码", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(GoodsInformationActivity.this, "暂无权限操作货品图片", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.add:
                tvGoodsInfo.callOnClick();
                // 保存按钮可用
                saveFlag = true;
                tvSave.setEnabled(true);
                tvSave.setBackground(this.getResources().getDrawable(R.drawable.custom_button));
                // 添加,查询,拍照功能不可用
                tvAdd.setEnabled(false);
                tvAdd.setBackgroundColor(Color.GRAY);
                tvUpdate.setBackground(this.getResources().getDrawable(R.drawable.text_left_line));
                tvFind.setEnabled(false);
                tvFind.setBackgroundColor(Color.GRAY);
                tvGoodsPic.setVisibility(View.INVISIBLE);
                reset();
                break;
            case R.id.update:
                // 保存按钮可用(修改)
                saveFlag = false;
                tvSave.setEnabled(true);
                tvSave.setBackground(this.getResources().getDrawable(R.drawable.custom_button));
                // 添加,查询,功能不可用
                tvAdd.setEnabled(false);
                tvAdd.setBackgroundColor(Color.GRAY);
                tvFind.setEnabled(false);
                tvFind.setBackgroundColor(Color.GRAY);
                tvUpdate.setBackground(this.getResources().getDrawable(R.drawable.text_left_line));
                // 改变控件的只读状态
                seasonIcon.setEnabled(true);
                ageIcon.setEnabled(true);
                etGoodsSubType.setEnabled(true);
                etBrand.setEnabled(true);
                etBrandSerial.setEnabled(true);
                etKind.setEnabled(true);
                etAge.setEnabled(true);
                etSeason.setEnabled(true);
                etSupplier.setEnabled(true);
                etSupplierCode.setEnabled(true);
                etPurchasePrice.setEnabled(true);
                etTradePrice.setEnabled(true);
                etRetailSales.setEnabled(true);
                etSalesPrice1.setEnabled(true);
                etSalesPrice2.setEnabled(true);
                etSalesPrice3.setEnabled(true);
                etRetailSales1.setEnabled(true);
                etRetailSales2.setEnabled(true);
                etRetailSales3.setEnabled(true);
                if (etGoodsColor1.getText().toString().trim().isEmpty()) {
                    etGoodsColor1.setEnabled(true);
                }
                if (etGoodsColor2.getText().toString().trim().isEmpty()) {
                    etGoodsColor2.setEnabled(true);
                }
                if (etGoodsColor3.getText().toString().trim().isEmpty()) {
                    etGoodsColor3.setEnabled(true);
                }
                if (etGoodsColor4.getText().toString().trim().isEmpty()) {
                    etGoodsColor4.setEnabled(true);
                }
                if (etGoodsColor5.getText().toString().trim().isEmpty()) {
                    etGoodsColor5.setEnabled(true);
                }
                if (etGoodsColor6.getText().toString().trim().isEmpty()) {
                    etGoodsColor6.setEnabled(true);
                }
                if (etGoodsColor7.getText().toString().trim().isEmpty()) {
                    etGoodsColor7.setEnabled(true);
                }
                if (etGoodsColor8.getText().toString().trim().isEmpty()) {
                    etGoodsColor8.setEnabled(true);
                }
                break;
            case R.id.save:
                if (saveFlag) {
                    save();
                } else {
                    update();
                }
                break;
            default:
                break;
        }
    }

    /**
     * 保存货品资料信息
     */
    private void save() {
        // 保存时,货品编码,名称,类别,颜色(至少一个)不能为空
        String goodsCode = etGoodsCode.getText().toString().trim();
        String goodsName = etGoodsName.getText().toString().trim();
        String goodsType = etGoodsType.getText().toString().trim();
        String goodsSubType = etGoodsSubType.getText().toString().trim();
        String kind = etKind.getText().toString().trim();
        String age = etAge.getText().toString().trim();
        String season = etSeason.getText().toString().trim();
        String supplierCode = etSupplierCode.getText().toString().trim();
        String purchasePrice = etPurchasePrice.getText().toString().trim();
        String tradePrice = etTradePrice.getText().toString().trim();
        String retailSales = etRetailSales.getText().toString().trim();
        String retailSales1 = etRetailSales1.getText().toString().trim();
        String retailSales2 = etRetailSales2.getText().toString().trim();
        String retailSales3 = etRetailSales3.getText().toString().trim();
        String salesPrice1 = etSalesPrice1.getText().toString().trim();
        String salesPrice2 = etSalesPrice2.getText().toString().trim();
        String salesPrice3 = etSalesPrice3.getText().toString().trim();
        if (null == goodsCode || goodsCode.isEmpty() || "null".equalsIgnoreCase(goodsCode)) {
            Toast.makeText(GoodsInformationActivity.this, "货品编码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (null == goodsName || goodsName.isEmpty() || "null".equalsIgnoreCase(goodsName)) {
            Toast.makeText(GoodsInformationActivity.this, "货品名称不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (null == goodsType || null == goodsTypeId || goodsTypeId.isEmpty() || goodsType.isEmpty() || "null".equalsIgnoreCase(goodsType)) {
            Toast.makeText(GoodsInformationActivity.this, "货品类别不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if ((null == colorId1 || colorId1.isEmpty())) {
            Toast.makeText(GoodsInformationActivity.this, "货品颜色1不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        // 保存操作
        RequestVo vo = new RequestVo();
        vo.requestUrl = savePath;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("goodsCode", goodsCode);
        map.put("goodsName", goodsName);
        map.put("goodsTypeId", goodsTypeId);
        map.put("goodsSubType", goodsSubType);
        map.put("brandId", brandId);
        map.put("brandSerialId", brandSerialId);
        map.put("kind", kind);
        map.put("age", age);
        map.put("season", season);
        map.put("supplierId", supplierId);
        map.put("supplierCode", supplierCode);
        map.put("purchasePrice", purchasePrice);
        map.put("tradePrice", tradePrice);
        map.put("retailSales", retailSales);
        map.put("retailSales1", retailSales1);
        map.put("retailSales2", retailSales2);
        map.put("retailSales3", retailSales3);
        map.put("salesPrice1", salesPrice1);
        map.put("salesPrice2", salesPrice2);
        map.put("salesPrice3", salesPrice3);
        map.put("colorId1", colorId1);
        map.put("colorId2", colorId2);
        map.put("colorId3", colorId3);
        map.put("colorId4", colorId4);
        map.put("colorId5", colorId5);
        map.put("colorId6", colorId6);
        map.put("colorId7", colorId7);
        map.put("colorId8", colorId8);
        vo.requestDataMap = map;
        // 防止重复提交数据
        tvSave.setEnabled(false);
        tvSave.setBackgroundColor(Color.GRAY);
        super.getDataFromServer(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    goodsId = retObj.getString("obj");
                    if (retObj.getBoolean("success") && goodsId != null && !"".equals(goodsId) && !"null".equalsIgnoreCase(goodsId)) {
                        // 保存按钮不可用
                        tvSave.setEnabled(false);
                        tvSave.setBackgroundColor(Color.GRAY);
                        // 修改按钮可用,拍照功能可用
                        tvAdd.setEnabled(true);
                        tvAdd.setBackground(GoodsInformationActivity.this.getResources().getDrawable(R.drawable.custom_button));
                        tvUpdate.setEnabled(true);
                        tvUpdate.setBackground(GoodsInformationActivity.this.getResources().getDrawable(R.drawable.upload_button));
                        tvGoodsPic.setVisibility(View.VISIBLE);
                        tvFind.setEnabled(true);
                        tvFind.setBackground(GoodsInformationActivity.this.getResources().getDrawable(R.drawable.upload_button));
                        Toast.makeText(GoodsInformationActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                        saveFlag = false;
                        // 刷新保存的数据
                        getGoodsInfoById();
                    } else {
                        saveFlag = true;
                        tvSave.setEnabled(true);
                        tvSave.setBackground(GoodsInformationActivity.this.getResources().getDrawable(R.drawable.custom_button));
                        if ("货品编码重复".equals(retObj.getString("msg"))) {
                            Toast.makeText(GoodsInformationActivity.this, "货品编码重复,请修改货品编码", Toast.LENGTH_SHORT).show();
                            tvGoodsInfo.callOnClick();
                            etGoodsCode.requestFocus();
                            etGoodsCode.selectAll();
                        } else if ("货品颜色重复".equals(retObj.getString("msg"))) {
                            Toast.makeText(GoodsInformationActivity.this, "货品颜色不能重复,保存失败", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(GoodsInformationActivity.this, "保存失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (Exception e) {
                    tvSave.setEnabled(true);
                    tvSave.setBackground(GoodsInformationActivity.this.getResources().getDrawable(R.drawable.custom_button));
                    Toast.makeText(GoodsInformationActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 修改货品资料信息
     */
    private void update() {
        // 修改时,货品编码,名称,类别不能修改,颜色(至少一个)不能为空
        String goodsSubType = etGoodsSubType.getText().toString().trim();
        String kind = etKind.getText().toString().trim();
        String age = etAge.getText().toString().trim();
        String season = etSeason.getText().toString().trim();
        String supplierCode = etSupplierCode.getText().toString().trim();
        String purchasePrice = etPurchasePrice.getText().toString().trim();
        String tradePrice = etTradePrice.getText().toString().trim();
        String retailSales = etRetailSales.getText().toString().trim();
        String retailSales1 = etRetailSales1.getText().toString().trim();
        String retailSales2 = etRetailSales2.getText().toString().trim();
        String retailSales3 = etRetailSales3.getText().toString().trim();
        String salesPrice1 = etSalesPrice1.getText().toString().trim();
        String salesPrice2 = etSalesPrice2.getText().toString().trim();
        String salesPrice3 = etSalesPrice3.getText().toString().trim();
        if (null == goodsId || goodsId.isEmpty() || "null".equalsIgnoreCase(goodsId)) {
            Toast.makeText(GoodsInformationActivity.this, "请先选择要修改的货品", Toast.LENGTH_SHORT).show();
            return;
        }
        // if((null == colorId1 || colorId1.isEmpty()) && (null == colorId2 ||
        // colorId2.isEmpty()) && (null == colorId3 || colorId3.isEmpty()) &&
        // (null == colorId4 || colorId4.isEmpty())){
        // Toast.makeText(GoodsInformationActivity.this, "货品颜色要求至少选择一种",
        // Toast.LENGTH_SHORT).show();
        // return;
        // }
        // 保存操作
        RequestVo vo = new RequestVo();
        vo.requestUrl = updatePath;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("goodsId", goodsId);
        map.put("goodsSubType", goodsSubType);
        map.put("brandId", brandId);
        map.put("brandSerialId", brandSerialId);
        map.put("kind", kind);
        map.put("age", age);
        map.put("season", season);
        map.put("supplierId", supplierId);
        map.put("supplierCode", supplierCode);
        map.put("purchasePrice", purchasePrice);
        map.put("tradePrice", tradePrice);
        map.put("retailSales", retailSales);
        map.put("retailSales1", retailSales1);
        map.put("retailSales2", retailSales2);
        map.put("retailSales3", retailSales3);
        map.put("salesPrice1", salesPrice1);
        map.put("salesPrice2", salesPrice2);
        map.put("salesPrice3", salesPrice3);
        map.put("colorId1", colorId1);
        map.put("colorId2", colorId2);
        map.put("colorId3", colorId3);
        map.put("colorId4", colorId4);
        map.put("colorId5", colorId5);
        map.put("colorId6", colorId6);
        map.put("colorId7", colorId7);
        map.put("colorId8", colorId8);
        vo.requestDataMap = map;
        super.getDataFromServer(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    int count = retObj.getInt("obj");
                    if (retObj.getBoolean("success") && count > 0) {
                        tvAdd.setEnabled(true);
                        tvAdd.setBackground(GoodsInformationActivity.this.getResources().getDrawable(R.drawable.custom_button));
                        tvSave.setEnabled(false);
                        tvUpdate.setEnabled(true);
                        tvSave.setBackgroundColor(Color.GRAY);
                        tvSave.setBackground(GoodsInformationActivity.this.getResources().getDrawable(R.drawable.text_left_line));
                        tvUpdate.setBackground(GoodsInformationActivity.this.getResources().getDrawable(R.drawable.upload_button));
                        tvGoodsPic.setVisibility(View.VISIBLE);
                        tvFind.setEnabled(true);
                        tvFind.setBackground(GoodsInformationActivity.this.getResources().getDrawable(R.drawable.upload_button));
                        Toast.makeText(GoodsInformationActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
                        // 刷新保存的数据
                        getGoodsInfoById();
                    } else {
                        if ("货品颜色重复".equals(retObj.getString("msg"))) {
                            Toast.makeText(GoodsInformationActivity.this, "货品颜色不能重复,修改失败", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(GoodsInformationActivity.this, "修改失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(GoodsInformationActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    @Override
    protected void onHeadLeftButton(View v) {
        // 点击返回时返回上一页面并刷新
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
     * 点击返回时询问是否保存当前信息
     */
    private void saveOrNot() {
        // 询问是否保存修改的发货单数据
        Builder dialog = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
        dialog.setTitle("提示");
        dialog.setMessage("确定要返回吗?返回将退出货品资料编辑");
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

    /**
     * 重置货品资料界面
     */
    private void reset() {
        brandId = null;
        goodsTypeId = null;
        brandSerialId = null;
        supplierId = null;
        etGoodsCode.setText(null);
        etGoodsName.setText(null);
        etGoodsType.setText(null);
        etGoodsSubType.setText(null);
        etBrand.setText(null);
        etBrandSerial.setText(null);
        etKind.setText(null);
        etAge.setText(null);
        etSeason.setText(null);
        etSupplier.setText(null);
        etSupplierCode.setText(null);
        etPurchasePrice.setText(null);
        etTradePrice.setText(null);
        etRetailSales.setText(null);
        etSalesPrice1.setText(null);
        etSalesPrice2.setText(null);
        etSalesPrice3.setText(null);
        etRetailSales1.setText(null);
        etRetailSales2.setText(null);
        etRetailSales3.setText(null);
        // 其它设置
        seasonIcon.setEnabled(true);
        ageIcon.setEnabled(true);
        etGoodsCode.setEnabled(true);
        etGoodsCode.setFocusable(true);
        etGoodsCode.setFocusableInTouchMode(true);
        etGoodsName.setEnabled(true);
        etGoodsName.setFocusable(true);
        etGoodsName.setFocusableInTouchMode(true);
        etGoodsType.setEnabled(true);
        etGoodsType.setFocusable(true);
        etGoodsType.setFocusableInTouchMode(true);
        etGoodsSubType.setEnabled(true);
        etBrand.setEnabled(true);
        etBrandSerial.setEnabled(true);
        etKind.setEnabled(true);
        etAge.setEnabled(true);
        etSeason.setEnabled(true);
        etSupplier.setEnabled(true);
        etSupplierCode.setEnabled(true);
        etPurchasePrice.setEnabled(true);
        etTradePrice.setEnabled(true);
        etRetailSales.setEnabled(true);
        etSalesPrice1.setEnabled(true);
        etSalesPrice2.setEnabled(true);
        etSalesPrice3.setEnabled(true);
        etRetailSales1.setEnabled(true);
        etRetailSales2.setEnabled(true);
        etRetailSales3.setEnabled(true);
        // 颜色设置
        etGoodsColor1.setText(null);
        etGoodsColor2.setText(null);
        etGoodsColor3.setText(null);
        etGoodsColor4.setText(null);
        etGoodsColor5.setText(null);
        etGoodsColor6.setText(null);
        etGoodsColor7.setText(null);
        etGoodsColor8.setText(null);
        colorId1 = null;
        colorId2 = null;
        colorId3 = null;
        colorId4 = null;
        colorId5 = null;
        colorId6 = null;
        colorId7 = null;
        colorId8 = null;
        etGoodsColor1.setFocusable(true);
        etGoodsColor2.setFocusable(true);
        etGoodsColor3.setFocusable(true);
        etGoodsColor4.setFocusable(true);
        etGoodsColor5.setFocusable(true);
        etGoodsColor6.setFocusable(true);
        etGoodsColor7.setFocusable(true);
        etGoodsColor8.setFocusable(true);
        etGoodsColor1.setEnabled(true);
        etGoodsColor2.setEnabled(true);
        etGoodsColor3.setEnabled(true);
        etGoodsColor4.setEnabled(true);
        etGoodsColor5.setEnabled(true);
        etGoodsColor6.setEnabled(true);
        etGoodsColor7.setEnabled(true);
        etGoodsColor8.setEnabled(true);
        etGoodsColor1.setFocusableInTouchMode(true);
        etGoodsColor2.setFocusableInTouchMode(true);
        etGoodsColor3.setFocusableInTouchMode(true);
        etGoodsColor4.setFocusableInTouchMode(true);
        etGoodsColor5.setFocusableInTouchMode(true);
        etGoodsColor6.setFocusableInTouchMode(true);
        etGoodsColor7.setFocusableInTouchMode(true);
        etGoodsColor8.setFocusableInTouchMode(true);
    }

    /**
     * 设为只读(货品资料初始化默认设置)
     */
    private void readOnly() {
        seasonIcon.setEnabled(false);
        ageIcon.setEnabled(false);
        etGoodsCode.setEnabled(false);
        etGoodsName.setEnabled(false);
        etGoodsType.setEnabled(false);
        etGoodsSubType.setEnabled(false);
        etBrand.setEnabled(false);
        etBrandSerial.setEnabled(false);
        etKind.setEnabled(false);
        etAge.setEnabled(false);
        etSeason.setEnabled(false);
        etSupplier.setEnabled(false);
        etSupplierCode.setEnabled(false);
        etPurchasePrice.setEnabled(false);
        etTradePrice.setEnabled(false);
        etRetailSales.setEnabled(false);
        etSalesPrice1.setEnabled(false);
        etSalesPrice2.setEnabled(false);
        etSalesPrice3.setEnabled(false);
        etRetailSales1.setEnabled(false);
        etRetailSales2.setEnabled(false);
        etRetailSales3.setEnabled(false);
        etGoodsColor1.setEnabled(false);
        etGoodsColor2.setEnabled(false);
        etGoodsColor3.setEnabled(false);
        etGoodsColor4.setEnabled(false);
        etGoodsColor5.setEnabled(false);
        etGoodsColor6.setEnabled(false);
        etGoodsColor7.setEnabled(false);
        etGoodsColor8.setEnabled(false);
    }

    /**
     * 通过货品ID获取货品信息
     */
    private void getGoodsInfoById() {
        tvGoodsInfo.callOnClick();
        RequestVo vo = new RequestVo();
        vo.requestUrl = queryPath;
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
                        // 显示的货品资料设为只读
                        reset();
                        readOnly();
                        JSONObject rs = retObj.getJSONObject("attributes");
                        JSONArray list = (JSONArray) rs.get("list");
                        JSONArray data = (JSONArray) rs.get("data");
                        if (list.length() > 0 || data.length() > 0) {
                            if (list.length() > 0) {
                                JSONObject obj = list.getJSONObject(0);
                                brandId = obj.getString("BrandID");
                                goodsTypeId = obj.getString("GoodsTypeID");
                                brandSerialId = obj.getString("BrandSerialID");
                                supplierId = obj.getString("SupplierID");
                                etGoodsCode.setText(obj.getString("GoodsCode"));
                                etGoodsName.setText(obj.getString("GoodsName"));
                                etGoodsType.setText(obj.getString("GoodsType"));
                                etGoodsSubType.setText(obj.getString("SubType"));
                                etBrand.setText(obj.getString("Brand"));
                                etBrandSerial.setText(obj.getString("Serial"));
                                etKind.setText(obj.getString("Kind"));
                                etAge.setText(obj.getString("Age"));
                                etSeason.setText(obj.getString("Season"));
                                etSupplier.setText(obj.getString("Supplier"));
                                etSupplierCode.setText(obj.getString("SupplierCode"));
                                etPurchasePrice.setText(obj.getString("PurchasePrice"));
                                etTradePrice.setText(obj.getString("TradePrice"));
                                etRetailSales.setText(obj.getString("RetailSales"));
                                etSalesPrice1.setText(obj.getString("SalesPrice1"));
                                etSalesPrice2.setText(obj.getString("SalesPrice2"));
                                etSalesPrice3.setText(obj.getString("SalesPrice3"));
                                etRetailSales1.setText(obj.getString("RetailSales1"));
                                etRetailSales2.setText(obj.getString("RetailSales2"));
                                etRetailSales3.setText(obj.getString("RetailSales3"));
                                // 设置货品编码,名称,类别不可改
                                etGoodsCode.setEnabled(false);
                                etGoodsCode.setFocusable(false);
                                etGoodsName.setEnabled(false);
                                etGoodsName.setFocusable(false);
                                etGoodsType.setEnabled(false);
                                etGoodsType.setFocusable(false);
                                // 其它设置
                                etPurchasePrice.setSelection(obj.getString("PurchasePrice").length());
                            }
                            if (data.length() > 0) {
                                for (int i = 0; i < data.length(); i++) {
                                    JSONObject json = data.getJSONObject(i);
                                    if (i == 0) {
                                        etGoodsColor1.setText(json.getString("Color"));
                                        colorId1 = null;
                                        etGoodsColor1.setFocusable(false);
                                        etGoodsColor1.setEnabled(false);
                                    } else if (i == 1) {
                                        etGoodsColor2.setText(json.getString("Color"));
                                        colorId2 = null;
                                        etGoodsColor2.setFocusable(false);
                                        etGoodsColor2.setEnabled(false);
                                    } else if (i == 2) {
                                        etGoodsColor3.setText(json.getString("Color"));
                                        colorId3 = null;
                                        etGoodsColor3.setFocusable(false);
                                        etGoodsColor3.setEnabled(false);
                                    } else if (i == 3) {
                                        etGoodsColor4.setText(json.getString("Color"));
                                        colorId4 = null;
                                        etGoodsColor4.setFocusable(false);
                                        etGoodsColor4.setEnabled(false);
                                    } else if (i == 4) {
                                        etGoodsColor5.setText(json.getString("Color"));
                                        colorId5 = null;
                                        etGoodsColor5.setFocusable(false);
                                        etGoodsColor5.setEnabled(false);
                                    } else if (i == 5) {
                                        etGoodsColor6.setText(json.getString("Color"));
                                        colorId6 = null;
                                        etGoodsColor6.setFocusable(false);
                                        etGoodsColor6.setEnabled(false);
                                    } else if (i == 6) {
                                        etGoodsColor7.setText(json.getString("Color"));
                                        colorId7 = null;
                                        etGoodsColor7.setFocusable(false);
                                        etGoodsColor7.setEnabled(false);
                                    } else if (i == 7) {
                                        etGoodsColor8.setText(json.getString("Color"));
                                        colorId8 = null;
                                        etGoodsColor8.setFocusable(false);
                                        etGoodsColor8.setEnabled(false);
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(GoodsInformationActivity.this, "货品资料不完整,部分资料信息无法显示", Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(GoodsInformationActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    protected void loadViewLayout() {
        setContentView(R.layout.activity_goods_information);
        setTitle("货品资料");
    }

    @Override
    protected void processLogic() {
        if (NetUtil.hasNetwork(getApplicationContext())) {
            if (LoginParameterUtil.online) {
                // 隐藏其它选项
                llGoodsPrice.setVisibility(View.INVISIBLE);
                llGoodsColor.setVisibility(View.INVISIBLE);
                llGoodsPic.setVisibility(View.INVISIBLE);
                // 设置修改,保存按钮不可用
                tvSave.setEnabled(false);
                tvUpdate.setEnabled(false);
                tvSave.setBackgroundColor(Color.GRAY);
                tvSave.setBackground(this.getResources().getDrawable(R.drawable.text_left_line));
                tvUpdate.setBackgroundColor(Color.GRAY);
                // 默认控件为只读
                readOnly();
                // 读取用户的货品资料操作权限
                goodsAddRight = LoginParameterUtil.goodsRightMap.get("AddRight");
                goodsBrowseRight = LoginParameterUtil.goodsRightMap.get("BrowseRight");
                goodsModifyRight = LoginParameterUtil.goodsRightMap.get("ModifyRight");
                // 读取浏览货品资料的权限
                if (!goodsBrowseRight) {
                    tvFind.setEnabled(false);
                    tvFind.setBackgroundColor(Color.GRAY);
                    tvFind.setBackground(this.getResources().getDrawable(R.drawable.text_left_line));
                }
                // 读取新增货品资料的权限
                if (!goodsAddRight) {
                    tvAdd.setEnabled(false);
                    tvAdd.setBackgroundColor(Color.GRAY);
                    tvAdd.setBackground(this.getResources().getDrawable(R.drawable.text_right_line));
                }
                // 参考进价字段权限
                if (!LoginParameterUtil.purchasePriceRight) {
                    llPurchasePrice.setVisibility(View.GONE);
                }
                // 货品子类别权限
                if (!LoginParameterUtil.subTypeRight) {
                    llGoodsSubType.setVisibility(View.GONE);
                }
                // 货品品牌权限
                if (!LoginParameterUtil.brandRight) {
                    llBrand.setVisibility(View.GONE);
                }
                // 货品系列权限
                if (!LoginParameterUtil.brandSerialRight) {
                    llBrandSerial.setVisibility(View.GONE);
                }
                // 货品性质权限
                if (!LoginParameterUtil.kindRight) {
                    llKind.setVisibility(View.GONE);
                }
                // 货品年份权限
                if (!LoginParameterUtil.ageRight) {
                    llAge.setVisibility(View.GONE);
                }
                // 货品季节权限
                if (!LoginParameterUtil.seasonRight) {
                    llSeason.setVisibility(View.GONE);
                }
                // 货品厂商权限
                if (!LoginParameterUtil.supplierRight) {
                    llSupplier.setVisibility(View.GONE);
                }
                // 货品厂商编码权限
                if (!LoginParameterUtil.supplierCodeRight) {
                    llSupplierCode.setVisibility(View.GONE);
                }
                // 货品零售价权限
                if (!LoginParameterUtil.retailSalesRight) {
                    llRetailSales.setVisibility(View.GONE);
                }
                // 货品零售价2权限
                if (!LoginParameterUtil.retailSales1Right) {
                    llRetailSales1.setVisibility(View.GONE);
                }
                // 货品零售价3权限
                if (!LoginParameterUtil.retailSales2Right) {
                    llRetailSales2.setVisibility(View.GONE);
                }
                // 货品零售价4权限
                if (!LoginParameterUtil.retailSales3Right) {
                    llRetailSales3.setVisibility(View.GONE);
                }
                // 货品批发价权限
                if (!LoginParameterUtil.tradePriceRight) {
                    llTradePrice.setVisibility(View.GONE);
                }
                // 货品批发价2权限
                if (!LoginParameterUtil.salesPrice1Right) {
                    llSalesPrice1.setVisibility(View.GONE);
                }
                // 货品批发价3权限
                if (!LoginParameterUtil.salesPrice2Right) {
                    llSalesPrice2.setVisibility(View.GONE);
                }
                // 货品批发价4权限
                if (!LoginParameterUtil.salesPrice3Right) {
                    llSalesPrice3.setVisibility(View.GONE);
                }
            } else {
                AlertDialog.Builder dialog = new AlertDialog.Builder(GoodsInformationActivity.this, AlertDialog.THEME_HOLO_LIGHT);
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
            AlertDialog.Builder dialog = new AlertDialog.Builder(GoodsInformationActivity.this, AlertDialog.THEME_HOLO_LIGHT);
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

    private class TouchListener implements OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (v.getId()) {
                case R.id.find:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Intent intent = new Intent(GoodsInformationActivity.this, SelectActivity.class);
                        intent.putExtra("selectType", "selectProduct");
                        startActivityForResult(intent, R.id.find);
                        overridePendingTransition(R.anim.activity_open, 0);
                    }
                    break;
                case R.id.seasonIcon:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Intent intent = new Intent(GoodsInformationActivity.this, SelectActivity.class);
                        intent.putExtra("selectType", "selectSeason");
                        startActivityForResult(intent, R.id.seasonIcon);
                        overridePendingTransition(R.anim.activity_open, 0);
                    }
                    break;
                case R.id.ageIcon:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Intent intent = new Intent(GoodsInformationActivity.this, SelectActivity.class);
                        intent.putExtra("selectType", "selectYear");
                        startActivityForResult(intent, R.id.ageIcon);
                        overridePendingTransition(R.anim.activity_open, 0);
                    }
                    break;
                case R.id.goodsType:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Intent intent = new Intent(GoodsInformationActivity.this, SelectActivity.class);
                        intent.putExtra("selectType", "selectCategory");
                        startActivityForResult(intent, R.id.goodsType);
                        overridePendingTransition(R.anim.activity_open, 0);
                    }
                    break;
                case R.id.goodsSubType:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Intent intent = new Intent(GoodsInformationActivity.this, SelectActivity.class);
                        intent.putExtra("selectType", "selectGoodsSubType");
                        startActivityForResult(intent, R.id.goodsSubType);
                        overridePendingTransition(R.anim.activity_open, 0);
                    }
                    break;
                case R.id.brand:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Intent intent = new Intent(GoodsInformationActivity.this, SelectActivity.class);
                        intent.putExtra("selectType", "selectBrand");
                        startActivityForResult(intent, R.id.brand);
                        overridePendingTransition(R.anim.activity_open, 0);
                    }
                    break;
                case R.id.brandSerial:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Intent intent = new Intent(GoodsInformationActivity.this, SelectActivity.class);
                        intent.putExtra("selectType", "selectGoodsBrandSerial");
                        startActivityForResult(intent, R.id.brandSerial);
                        overridePendingTransition(R.anim.activity_open, 0);
                    }
                    break;
                case R.id.supplier:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Intent intent = new Intent(GoodsInformationActivity.this, SelectActivity.class);
                        intent.putExtra("selectType", "selectGoodsSupplier");
                        startActivityForResult(intent, R.id.supplier);
                        overridePendingTransition(R.anim.activity_open, 0);
                    }
                    break;
                case R.id.goodsColor1:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Intent intent = new Intent(GoodsInformationActivity.this, SelectActivity.class);
                        intent.putExtra("selectType", "selectGoodsColor");
                        startActivityForResult(intent, R.id.goodsColor1);
                        overridePendingTransition(R.anim.activity_open, 0);
                    }
                    break;
                case R.id.goodsColor2:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Intent intent = new Intent(GoodsInformationActivity.this, SelectActivity.class);
                        intent.putExtra("selectType", "selectGoodsColor");
                        startActivityForResult(intent, R.id.goodsColor2);
                        overridePendingTransition(R.anim.activity_open, 0);
                    }
                    break;
                case R.id.goodsColor3:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Intent intent = new Intent(GoodsInformationActivity.this, SelectActivity.class);
                        intent.putExtra("selectType", "selectGoodsColor");
                        startActivityForResult(intent, R.id.goodsColor3);
                        overridePendingTransition(R.anim.activity_open, 0);
                    }
                    break;
                case R.id.goodsColor4:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Intent intent = new Intent(GoodsInformationActivity.this, SelectActivity.class);
                        intent.putExtra("selectType", "selectGoodsColor");
                        startActivityForResult(intent, R.id.goodsColor4);
                        overridePendingTransition(R.anim.activity_open, 0);
                    }
                    break;
                case R.id.goodsColor5:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Intent intent = new Intent(GoodsInformationActivity.this, SelectActivity.class);
                        intent.putExtra("selectType", "selectGoodsColor");
                        startActivityForResult(intent, R.id.goodsColor5);
                        overridePendingTransition(R.anim.activity_open, 0);
                    }
                    break;
                case R.id.goodsColor6:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Intent intent = new Intent(GoodsInformationActivity.this, SelectActivity.class);
                        intent.putExtra("selectType", "selectGoodsColor");
                        startActivityForResult(intent, R.id.goodsColor6);
                        overridePendingTransition(R.anim.activity_open, 0);
                    }
                    break;
                case R.id.goodsColor7:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Intent intent = new Intent(GoodsInformationActivity.this, SelectActivity.class);
                        intent.putExtra("selectType", "selectGoodsColor");
                        startActivityForResult(intent, R.id.goodsColor7);
                        overridePendingTransition(R.anim.activity_open, 0);
                    }
                    break;
                case R.id.goodsColor8:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Intent intent = new Intent(GoodsInformationActivity.this, SelectActivity.class);
                        intent.putExtra("selectType", "selectGoodsColor");
                        startActivityForResult(intent, R.id.goodsColor8);
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
            case R.id.find:
                if (resultCode == 1) {
                    goodsId = data.getStringExtra("GoodsID");
                    if (null != goodsId && !goodsId.isEmpty() && !"null".equalsIgnoreCase(goodsId)) {
                        // 读取修改货品资料的权限
                        if (goodsModifyRight) {
                            tvUpdate.setEnabled(true);
                            tvUpdate.setBackground(GoodsInformationActivity.this.getResources().getDrawable(R.drawable.upload_button));
                        }
                        tvSave.setBackground(null);
                        tvSave.setBackgroundColor(Color.GRAY);
                        getGoodsInfoById();
                    }
                }
                break;
            case R.id.seasonIcon:
                if (resultCode == 1) {
                    etSeason.setText(data.getStringExtra("Name"));
                }
                break;
            case R.id.ageIcon:
                if (resultCode == 1) {
                    etAge.setText(data.getStringExtra("Name"));
                }
                break;
            case R.id.goodsType:
                if (resultCode == 1) {
                    etGoodsType.setText(data.getStringExtra("Name"));
                    goodsTypeId = data.getStringExtra("GoodsTypeID");
                }
                break;
            case R.id.goodsSubType:
                if (resultCode == 1) {
                    etGoodsSubType.setText(data.getStringExtra("Name"));
                }
                break;
            case R.id.brand:
                if (resultCode == 1) {
                    etBrand.setText(data.getStringExtra("Name"));
                    brandId = data.getStringExtra("BrandID");
                }
                break;
            case R.id.brandSerial:
                if (resultCode == 1) {
                    etBrandSerial.setText(data.getStringExtra("Name"));
                    brandSerialId = data.getStringExtra("BrandSerialID");
                }
                break;
            case R.id.supplier:
                if (resultCode == 1) {
                    etSupplier.setText(data.getStringExtra("Name"));
                    supplierId = data.getStringExtra("SupplierID");
                }
                break;
            case R.id.goodsColor1:
                if (resultCode == 1) {
                    etGoodsColor1.setText(data.getStringExtra("Name"));
                    colorId1 = data.getStringExtra("ColorID");
                    String colorStr1 = etGoodsColor1.getText().toString().trim();
                    String colorStr2 = etGoodsColor2.getText().toString().trim();
                    String colorStr3 = etGoodsColor3.getText().toString().trim();
                    String colorStr4 = etGoodsColor4.getText().toString().trim();
                    String colorStr5 = etGoodsColor5.getText().toString().trim();
                    String colorStr6 = etGoodsColor6.getText().toString().trim();
                    String colorStr7 = etGoodsColor7.getText().toString().trim();
                    String colorStr8 = etGoodsColor8.getText().toString().trim();
                    if (colorStr2.equals(colorStr1) || colorStr3.equals(colorStr1) || colorStr4.equals(colorStr1) || colorStr5.equals(colorStr1) || colorStr6.equals(colorStr1) || colorStr7.equals(colorStr1) || colorStr8.equals(colorStr1)) {
                        etGoodsColor1.setText(null);
                        colorId1 = null;
                        Toast.makeText(GoodsInformationActivity.this, "货品颜色不能相同", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.goodsColor2:
                if (resultCode == 1) {
                    etGoodsColor2.setText(data.getStringExtra("Name"));
                    colorId2 = data.getStringExtra("ColorID");
                    String colorStr1 = etGoodsColor1.getText().toString().trim();
                    String colorStr2 = etGoodsColor2.getText().toString().trim();
                    String colorStr3 = etGoodsColor3.getText().toString().trim();
                    String colorStr4 = etGoodsColor4.getText().toString().trim();
                    String colorStr5 = etGoodsColor5.getText().toString().trim();
                    String colorStr6 = etGoodsColor6.getText().toString().trim();
                    String colorStr7 = etGoodsColor7.getText().toString().trim();
                    String colorStr8 = etGoodsColor8.getText().toString().trim();
                    if (colorStr1.equals(colorStr2) || colorStr3.equals(colorStr2) || colorStr4.equals(colorStr2) || colorStr5.equals(colorStr2) || colorStr6.equals(colorStr2) || colorStr7.equals(colorStr2) || colorStr8.equals(colorStr2)) {
                        etGoodsColor2.setText(null);
                        colorId2 = null;
                        Toast.makeText(GoodsInformationActivity.this, "货品颜色不能相同", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.goodsColor3:
                if (resultCode == 1) {
                    etGoodsColor3.setText(data.getStringExtra("Name"));
                    colorId3 = data.getStringExtra("ColorID");
                    String colorStr1 = etGoodsColor1.getText().toString().trim();
                    String colorStr2 = etGoodsColor2.getText().toString().trim();
                    String colorStr3 = etGoodsColor3.getText().toString().trim();
                    String colorStr4 = etGoodsColor4.getText().toString().trim();
                    String colorStr5 = etGoodsColor5.getText().toString().trim();
                    String colorStr6 = etGoodsColor6.getText().toString().trim();
                    String colorStr7 = etGoodsColor7.getText().toString().trim();
                    String colorStr8 = etGoodsColor8.getText().toString().trim();
                    if (colorStr1.equals(colorStr3) || colorStr2.equals(colorStr3) || colorStr4.equals(colorStr3) || colorStr5.equals(colorStr3) || colorStr6.equals(colorStr3) || colorStr7.equals(colorStr3) || colorStr8.equals(colorStr3)) {
                        etGoodsColor3.setText(null);
                        colorId3 = null;
                        Toast.makeText(GoodsInformationActivity.this, "货品颜色不能相同", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.goodsColor4:
                if (resultCode == 1) {
                    etGoodsColor4.setText(data.getStringExtra("Name"));
                    colorId4 = data.getStringExtra("ColorID");
                    String colorStr1 = etGoodsColor1.getText().toString().trim();
                    String colorStr2 = etGoodsColor2.getText().toString().trim();
                    String colorStr3 = etGoodsColor3.getText().toString().trim();
                    String colorStr4 = etGoodsColor4.getText().toString().trim();
                    String colorStr5 = etGoodsColor5.getText().toString().trim();
                    String colorStr6 = etGoodsColor6.getText().toString().trim();
                    String colorStr7 = etGoodsColor7.getText().toString().trim();
                    String colorStr8 = etGoodsColor8.getText().toString().trim();
                    if (colorStr1.equals(colorStr4) || colorStr3.equals(colorStr4) || colorStr2.equals(colorStr4) || colorStr5.equals(colorStr4) || colorStr6.equals(colorStr4) || colorStr7.equals(colorStr4) || colorStr8.equals(colorStr4)) {
                        etGoodsColor4.setText(null);
                        colorId4 = null;
                        Toast.makeText(GoodsInformationActivity.this, "货品颜色不能相同", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.goodsColor5:
                if (resultCode == 1) {
                    etGoodsColor5.setText(data.getStringExtra("Name"));
                    colorId5 = data.getStringExtra("ColorID");
                    String colorStr1 = etGoodsColor1.getText().toString().trim();
                    String colorStr2 = etGoodsColor2.getText().toString().trim();
                    String colorStr3 = etGoodsColor3.getText().toString().trim();
                    String colorStr4 = etGoodsColor4.getText().toString().trim();
                    String colorStr5 = etGoodsColor5.getText().toString().trim();
                    String colorStr6 = etGoodsColor6.getText().toString().trim();
                    String colorStr7 = etGoodsColor7.getText().toString().trim();
                    String colorStr8 = etGoodsColor8.getText().toString().trim();
                    if (colorStr2.equals(colorStr5) || colorStr3.equals(colorStr5) || colorStr4.equals(colorStr5) || colorStr1.equals(colorStr5) || colorStr6.equals(colorStr5) || colorStr7.equals(colorStr5) || colorStr8.equals(colorStr5)) {
                        etGoodsColor5.setText(null);
                        colorId5 = null;
                        Toast.makeText(GoodsInformationActivity.this, "货品颜色不能相同", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.goodsColor6:
                if (resultCode == 1) {
                    etGoodsColor6.setText(data.getStringExtra("Name"));
                    colorId6 = data.getStringExtra("ColorID");
                    String colorStr1 = etGoodsColor1.getText().toString().trim();
                    String colorStr2 = etGoodsColor2.getText().toString().trim();
                    String colorStr3 = etGoodsColor3.getText().toString().trim();
                    String colorStr4 = etGoodsColor4.getText().toString().trim();
                    String colorStr5 = etGoodsColor5.getText().toString().trim();
                    String colorStr6 = etGoodsColor6.getText().toString().trim();
                    String colorStr7 = etGoodsColor7.getText().toString().trim();
                    String colorStr8 = etGoodsColor8.getText().toString().trim();
                    if (colorStr1.equals(colorStr6) || colorStr3.equals(colorStr6) || colorStr4.equals(colorStr6) || colorStr5.equals(colorStr6) || colorStr2.equals(colorStr6) || colorStr7.equals(colorStr6) || colorStr8.equals(colorStr6)) {
                        etGoodsColor6.setText(null);
                        colorId6 = null;
                        Toast.makeText(GoodsInformationActivity.this, "货品颜色不能相同", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.goodsColor7:
                if (resultCode == 1) {
                    etGoodsColor7.setText(data.getStringExtra("Name"));
                    colorId7 = data.getStringExtra("ColorID");
                    String colorStr1 = etGoodsColor1.getText().toString().trim();
                    String colorStr2 = etGoodsColor2.getText().toString().trim();
                    String colorStr3 = etGoodsColor3.getText().toString().trim();
                    String colorStr4 = etGoodsColor4.getText().toString().trim();
                    String colorStr5 = etGoodsColor5.getText().toString().trim();
                    String colorStr6 = etGoodsColor6.getText().toString().trim();
                    String colorStr7 = etGoodsColor7.getText().toString().trim();
                    String colorStr8 = etGoodsColor8.getText().toString().trim();
                    if (colorStr1.equals(colorStr7) || colorStr2.equals(colorStr7) || colorStr4.equals(colorStr7) || colorStr5.equals(colorStr7) || colorStr6.equals(colorStr7) || colorStr3.equals(colorStr7) || colorStr8.equals(colorStr7)) {
                        etGoodsColor7.setText(null);
                        colorId7 = null;
                        Toast.makeText(GoodsInformationActivity.this, "货品颜色不能相同", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.goodsColor8:
                if (resultCode == 1) {
                    etGoodsColor8.setText(data.getStringExtra("Name"));
                    colorId8 = data.getStringExtra("ColorID");
                    String colorStr1 = etGoodsColor1.getText().toString().trim();
                    String colorStr2 = etGoodsColor2.getText().toString().trim();
                    String colorStr3 = etGoodsColor3.getText().toString().trim();
                    String colorStr4 = etGoodsColor4.getText().toString().trim();
                    String colorStr5 = etGoodsColor5.getText().toString().trim();
                    String colorStr6 = etGoodsColor6.getText().toString().trim();
                    String colorStr7 = etGoodsColor7.getText().toString().trim();
                    String colorStr8 = etGoodsColor8.getText().toString().trim();
                    if (colorStr1.equals(colorStr8) || colorStr3.equals(colorStr8) || colorStr2.equals(colorStr8) || colorStr5.equals(colorStr8) || colorStr6.equals(colorStr8) || colorStr7.equals(colorStr8) || colorStr4.equals(colorStr8)) {
                        etGoodsColor8.setText(null);
                        colorId8 = null;
                        Toast.makeText(GoodsInformationActivity.this, "货品颜色不能相同", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void setListener() {
        TouchListener tl = new TouchListener();
        tvGoodsInfo.setOnClickListener(this);
        tvGoodsInfo.setChecked(true);
        tvGoodsPrice.setOnClickListener(this);
        tvGoodsColor.setOnClickListener(this);
        tvGoodsPic.setOnClickListener(this);
        tvAdd.setOnClickListener(this);
        tvUpdate.setOnClickListener(this);
        tvSave.setOnClickListener(this);
        // tvFind.setOnClickListener(this);
        tvFind.setOnTouchListener(tl);
        etGoodsType.setOnTouchListener(tl);
        etGoodsSubType.setOnTouchListener(tl);
        etBrand.setOnTouchListener(tl);
        etBrandSerial.setOnTouchListener(tl);
        etSupplier.setOnTouchListener(tl);
        etGoodsColor1.setOnTouchListener(tl);
        etGoodsColor2.setOnTouchListener(tl);
        etGoodsColor3.setOnTouchListener(tl);
        etGoodsColor4.setOnTouchListener(tl);
        etGoodsColor5.setOnTouchListener(tl);
        etGoodsColor6.setOnTouchListener(tl);
        etGoodsColor7.setOnTouchListener(tl);
        etGoodsColor8.setOnTouchListener(tl);
        seasonIcon.setOnTouchListener(tl);
        ageIcon.setOnTouchListener(tl);
    }

    @Override
    protected void findViewById() {
        llGoodsSubType = (LinearLayout) findViewById(R.id.ll_goodsSubType);
        llBrand = (LinearLayout) findViewById(R.id.ll_brand);
        llBrandSerial = (LinearLayout) findViewById(R.id.ll_brandSerial);
        llKind = (LinearLayout) findViewById(R.id.ll_kind);
        llAge = (LinearLayout) findViewById(R.id.ll_age);
        llSeason = (LinearLayout) findViewById(R.id.ll_season);
        llSupplier = (LinearLayout) findViewById(R.id.ll_supplier);
        llSupplierCode = (LinearLayout) findViewById(R.id.ll_supplierCode);
        llPurchasePrice = (LinearLayout) findViewById(R.id.ll_purchasePrice);
        llRetailSales = (LinearLayout) findViewById(R.id.ll_retailSales);
        llTradePrice = (LinearLayout) findViewById(R.id.ll_tradePrice);
        llRetailSales1 = (LinearLayout) findViewById(R.id.ll_retailSales1);
        llSalesPrice1 = (LinearLayout) findViewById(R.id.ll_salesPrice1);
        llRetailSales2 = (LinearLayout) findViewById(R.id.ll_retailSales2);
        llSalesPrice2 = (LinearLayout) findViewById(R.id.ll_salesPrice2);
        llRetailSales3 = (LinearLayout) findViewById(R.id.ll_retailSales3);
        llSalesPrice3 = (LinearLayout) findViewById(R.id.ll_salesPrice3);

        llGoodsInfo = (LinearLayout) findViewById(R.id.ll_goods_info);
        llGoodsPrice = (LinearLayout) findViewById(R.id.ll_goods_price);
        llGoodsColor = (LinearLayout) findViewById(R.id.ll_goods_color);
        llGoodsPic = (LinearLayout) findViewById(R.id.ll_goods_pic);
        tvGoodsInfo = (CheckBoxTextView) findViewById(R.id.goods_info);
        tvGoodsPrice = (CheckBoxTextView) findViewById(R.id.goods_price);
        tvGoodsColor = (CheckBoxTextView) findViewById(R.id.goods_color);
        tvGoodsPic = (CheckBoxTextView) findViewById(R.id.goods_pic);
        tvAdd = (TextView) findViewById(R.id.add);
        tvUpdate = (TextView) findViewById(R.id.update);
        tvSave = (TextView) findViewById(R.id.save);
        tvFind = (TextView) findViewById(R.id.find);
        tvPurPrice = (TextView) findViewById(R.id.purPrice);
        etGoodsCode = (EditText) findViewById(R.id.goodsCode);
        etGoodsName = (EditText) findViewById(R.id.goodsName);
        etGoodsType = (EditText) findViewById(R.id.goodsType);
        etGoodsSubType = (EditText) findViewById(R.id.goodsSubType);
        etBrand = (EditText) findViewById(R.id.brand);
        etBrandSerial = (EditText) findViewById(R.id.brandSerial);
        etKind = (EditText) findViewById(R.id.kind);
        etAge = (EditText) findViewById(R.id.age);
        etSeason = (EditText) findViewById(R.id.season);
        etSupplier = (EditText) findViewById(R.id.supplier);
        etSupplierCode = (EditText) findViewById(R.id.supplierCode);
        etPurchasePrice = (EditText) findViewById(R.id.purchasePrice);
        etRetailSales = (EditText) findViewById(R.id.retailSales);
        etTradePrice = (EditText) findViewById(R.id.tradePrice);
        etRetailSales1 = (EditText) findViewById(R.id.retailSales1);
        etRetailSales2 = (EditText) findViewById(R.id.retailSales2);
        etRetailSales3 = (EditText) findViewById(R.id.retailSales3);
        etSalesPrice1 = (EditText) findViewById(R.id.salesPrice1);
        etSalesPrice2 = (EditText) findViewById(R.id.salesPrice2);
        etSalesPrice3 = (EditText) findViewById(R.id.salesPrice3);
        etGoodsColor1 = (EditText) findViewById(R.id.goodsColor1);
        etGoodsColor2 = (EditText) findViewById(R.id.goodsColor2);
        etGoodsColor3 = (EditText) findViewById(R.id.goodsColor3);
        etGoodsColor4 = (EditText) findViewById(R.id.goodsColor4);
        etGoodsColor5 = (EditText) findViewById(R.id.goodsColor5);
        etGoodsColor6 = (EditText) findViewById(R.id.goodsColor6);
        etGoodsColor7 = (EditText) findViewById(R.id.goodsColor7);
        etGoodsColor8 = (EditText) findViewById(R.id.goodsColor8);
        seasonIcon = (FontTextView) findViewById(R.id.seasonIcon);
        ageIcon = (FontTextView) findViewById(R.id.ageIcon);

        // 提示文字的颜色
        etGoodsCode.setHintTextColor(this.getResources().getColor(R.color.white));
        etGoodsName.setHintTextColor(this.getResources().getColor(R.color.white));
        etGoodsType.setHintTextColor(this.getResources().getColor(R.color.white));
        etGoodsSubType.setHintTextColor(this.getResources().getColor(R.color.white));
        etBrand.setHintTextColor(this.getResources().getColor(R.color.white));
        etBrandSerial.setHintTextColor(this.getResources().getColor(R.color.white));
        etKind.setHintTextColor(this.getResources().getColor(R.color.white));
        etAge.setHintTextColor(this.getResources().getColor(R.color.white));
        etSeason.setHintTextColor(this.getResources().getColor(R.color.white));
        etSupplier.setHintTextColor(this.getResources().getColor(R.color.white));
        etSupplierCode.setHintTextColor(this.getResources().getColor(R.color.white));
        etPurchasePrice.setHintTextColor(this.getResources().getColor(R.color.white));
        etRetailSales.setHintTextColor(this.getResources().getColor(R.color.white));
        etTradePrice.setHintTextColor(this.getResources().getColor(R.color.white));
        etRetailSales1.setHintTextColor(this.getResources().getColor(R.color.white));
        etRetailSales2.setHintTextColor(this.getResources().getColor(R.color.white));
        etRetailSales3.setHintTextColor(this.getResources().getColor(R.color.white));
        etSalesPrice1.setHintTextColor(this.getResources().getColor(R.color.white));
        etSalesPrice2.setHintTextColor(this.getResources().getColor(R.color.white));
        etSalesPrice3.setHintTextColor(this.getResources().getColor(R.color.white));
        etGoodsColor1.setHintTextColor(this.getResources().getColor(R.color.white));
        etGoodsColor2.setHintTextColor(this.getResources().getColor(R.color.white));
        etGoodsColor3.setHintTextColor(this.getResources().getColor(R.color.white));
        etGoodsColor4.setHintTextColor(this.getResources().getColor(R.color.white));
        etGoodsColor5.setHintTextColor(this.getResources().getColor(R.color.white));
        etGoodsColor6.setHintTextColor(this.getResources().getColor(R.color.white));
        etGoodsColor7.setHintTextColor(this.getResources().getColor(R.color.white));
        etGoodsColor8.setHintTextColor(this.getResources().getColor(R.color.white));
    }

}
