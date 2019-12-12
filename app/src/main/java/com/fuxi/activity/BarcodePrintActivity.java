package com.fuxi.activity;

import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.os.Handler;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import com.fuxi.main.R;
import com.fuxi.util.AppManager;
import com.fuxi.util.BarcodeCreater;
import com.fuxi.util.Logger;
import com.fuxi.util.LoginParameterUtil;
import com.fuxi.util.NetUtil;
import com.fuxi.util.Tools;
import com.fuxi.vo.Customer;
import com.fuxi.vo.RequestVo;
import com.fuxi.widget.FontTextView;
import com.google.zxing.BarcodeFormat;

/**
 * Title: BarcodePrintActivity Description: 条码打印活动界面
 * 
 * @author LYJ
 * 
 */
public class BarcodePrintActivity extends BaseWapperActivity {

    private static final String TAG = "BarcodePrintActivity";
    private static String goodsQueryPath = "/select.do?analyticalBarcode";
    private static String goodsQueryOnlyPath = "/select.do?getGoodsByCode";
    private static String queryBarcodePath = "/barcodePrint.do?queryBarcode";

    private static Intent bitmapIntent = new Intent("COM.QSPDA.PRINTBITMAP");
    private static Intent printIntent = new Intent("COM.QSPDA.PRINTTEXT");

    private AlertDialog alertDialog;
    private FontTextView ftv_goodsCodeIcon;
    private EditText et_goodsCode;
    private EditText et_colorCode;
    private EditText et_sizeCode;
    private EditText et_goodsName;
    private EditText et_retailSales;
    private EditText et_goodsBarcode;
    private EditText et_supplierBarcode;
    private TextView tv_printGoodsBarcode;
    private TextView tv_printSupplierBarcode;

    private String goodsId;
    private String colorId;
    private String sizeId;
    private String goodsCode;
    private String colorCode;
    private String sizeCode;
    private String goodsName;
    private String colorName;
    private String sizeName;
    private String retailSales;


    @Override
    public void onClick(View v) {
        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
        }
        switch (v.getId()) {
            case R.id.printGoodsBarcode:
                // 打印货品条码
                String goodsBarcode = et_goodsBarcode.getText().toString();
                if (goodsBarcode == null || "".equals(goodsBarcode) || "null".equalsIgnoreCase(goodsBarcode)) {
                    Toast.makeText(BarcodePrintActivity.this, "当前货品条码为空，无法进行条码打印", Toast.LENGTH_SHORT).show();
                    return;
                }
                // 打印
                Toast.makeText(BarcodePrintActivity.this, "正在打印货品条码", Toast.LENGTH_SHORT).show();
                print(goodsBarcode);
                break;
            case R.id.printSupplierBarcode:
                // 打印厂商条码
                String supplierBarcode = et_supplierBarcode.getText().toString();
                if (supplierBarcode == null || "".equals(supplierBarcode) || "null".equalsIgnoreCase(supplierBarcode)) {
                    Toast.makeText(BarcodePrintActivity.this, "当前厂商条码为空，无法进行条码打印", Toast.LENGTH_SHORT).show();
                    return;
                }
                // 打印
                Toast.makeText(BarcodePrintActivity.this, "正在打印厂商条码", Toast.LENGTH_SHORT).show();
                print(supplierBarcode);
                break;
            default:
                break;
        }
    }

    /**
     * 通过条码查询货品信息
     */
    private void queryGoodsCode() {
        final String productId = et_goodsCode.getText().toString();
        if (null == productId || "".equals(productId) || "null".equalsIgnoreCase(productId)) {
            et_goodsCode.setText(null);
            Toast.makeText(BarcodePrintActivity.this, "条码或货号不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        RequestVo vo = new RequestVo();
        vo.requestUrl = goodsQueryPath;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("BarCode", productId);
        vo.requestDataMap = map;
        super.getDataFromServer(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    if (retObj.getBoolean("success")) {
                        JSONObject jsonObject = retObj.getJSONObject("obj");
                        goodsId = jsonObject.getString("GoodsID");
                        colorId = jsonObject.getString("ColorID");
                        sizeId = jsonObject.getString("SizeID");
                        goodsName = jsonObject.getString("GoodsName");
                        retailSales = jsonObject.getString("RetailSales");
                        goodsCode = jsonObject.getString("GoodsCode");
                        colorName = jsonObject.getString("ColorName");
                        colorCode = jsonObject.getString("ColorCode");
                        sizeCode = jsonObject.getString("SizeCode");
                        sizeName = jsonObject.getString("SizeName");
                        // 设置显示值
                        if ("null".equalsIgnoreCase(goodsCode) || "null".equalsIgnoreCase(colorCode) || "null".equalsIgnoreCase(sizeCode)) {
                            // 单条码无法识别时执行货品信息查询(忽略颜色和尺码)
                            queryGoodsCodeOnly(productId);
                        } else {
                            et_goodsCode.setText(goodsCode);
                            et_colorCode.setText(colorCode);
                            et_sizeCode.setText(sizeCode);
                            et_goodsName.setText(goodsName);
                            et_retailSales.setText(retailSales);
                            // 生成条码
                            generalBarcode();
                        }
                    } else {
                        // 忽略货品颜色尺码信息
                        queryGoodsCodeOnly(productId);
                    }
                } catch (Exception e) {
                    Toast.makeText(BarcodePrintActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 忽略颜色尺码获取货品信息
     * 
     * @param barcode
     */
    private void queryGoodsCodeOnly(String barcode) {
        RequestVo vo = new RequestVo();
        vo.requestUrl = goodsQueryOnlyPath;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("goodsCode", barcode);
        vo.requestDataMap = map;
        super.getDataFromServer(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    if (retObj.getBoolean("success")) {
                        JSONObject jsonObject = retObj.getJSONObject("obj");
                        goodsId = jsonObject.getString("GoodsID");
                        goodsName = jsonObject.getString("GoodsName");
                        retailSales = jsonObject.getString("RetailSales");
                        goodsCode = jsonObject.getString("GoodsCode");
                        // 获取货品信息后自动弹出颜色选择框
                        // 设置扫码区自动获取焦点
                        et_colorCode.requestFocus();
                        et_colorCode.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, et_colorCode.getLeft() + 5, et_colorCode.getTop() + 5, 0));
                        et_colorCode.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, et_colorCode.getLeft() + 5, et_colorCode.getTop() + 5, 0));
                        // 设置显示值
                        if ("null".equalsIgnoreCase(goodsCode) || "null".equalsIgnoreCase(colorCode) || "null".equalsIgnoreCase(sizeCode)) {
                            et_goodsCode.requestFocus();
                            et_goodsCode.selectAll();
                            Toast.makeText(BarcodePrintActivity.this, "条码或货号错误", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        et_goodsCode.requestFocus();
                        et_goodsCode.selectAll();
                        Toast.makeText(BarcodePrintActivity.this, "条码或货号错误", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(BarcodePrintActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 根据货品信息查询生成条码
     */
    private void generalBarcode() {
        if (null == goodsId || "".equals(goodsId) || "null".equalsIgnoreCase(goodsId) || null == colorId || "".equals(colorId) || "null".equalsIgnoreCase(colorId) || null == sizeId || "".equals(sizeId) || "null".equalsIgnoreCase(sizeId)) {
            return;
        }
        RequestVo vo = new RequestVo();
        vo.requestUrl = queryBarcodePath;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("goodsId", goodsId);
        map.put("colorId", colorId);
        map.put("sizeId", sizeId);
        vo.requestDataMap = map;
        super.getDataFromServer(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    et_goodsBarcode.setText(goodsCode + colorCode + sizeCode);
                    if (retObj.getBoolean("success")) {
                        String barcode = retObj.getString("obj");
                        // 设置显示值
                        et_supplierBarcode.setText(barcode);
                    } else {
                        et_goodsCode.selectAll();
                        et_supplierBarcode.setText(null);
                        Toast.makeText(BarcodePrintActivity.this, "厂商条码生成失败", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(BarcodePrintActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }

    private class TouchListener implements OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (v.getId()) {
                case R.id.goodsCodeIcon:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Intent intent = new Intent(BarcodePrintActivity.this, SelectActivity.class);
                        intent.putExtra("selectType", "selectProduct");
                        startActivityForResult(intent, R.id.goodsCodeIcon);
                        overridePendingTransition(R.anim.activity_open, 0);
                    }
                    break;
                case R.id.colorCode:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        if (goodsId == null || "".equals(goodsId) || "null".equalsIgnoreCase(goodsId)) {
                            Toast.makeText(BarcodePrintActivity.this, "请先输入货品编码", Toast.LENGTH_SHORT).show();
                            break;
                        }
                        Intent intent = new Intent(BarcodePrintActivity.this, SelectActivity.class);
                        intent.putExtra("selectType", "selectColorByGoodsCode");
                        intent.putExtra("param", goodsId);
                        startActivityForResult(intent, R.id.colorCode);
                        overridePendingTransition(R.anim.activity_open, 0);
                    }
                    break;
                case R.id.sizeCode:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        if (goodsId == null || "".equals(goodsId) || "null".equalsIgnoreCase(goodsId)) {
                            Toast.makeText(BarcodePrintActivity.this, "请先输入货品编码", Toast.LENGTH_SHORT).show();
                            break;
                        }
                        Intent intent = new Intent(BarcodePrintActivity.this, SelectActivity.class);
                        intent.putExtra("selectType", "selectSizeByGoodsCode");
                        intent.putExtra("param", goodsId);
                        startActivityForResult(intent, R.id.sizeCode);
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
            case R.id.goodsCodeIcon:
                if (resultCode == 1) {
                    goodsCode = data.getStringExtra("Code");
                    et_goodsCode.setText(goodsCode);
                    goodsId = data.getStringExtra("GoodsID");
                    goodsName = data.getStringExtra("GoodsName");
                    et_goodsName.setText(data.getStringExtra("GoodsName"));
                    retailSales = data.getStringExtra("RetailSales");
                    et_retailSales.setText(data.getStringExtra("RetailSales"));
                    // 设置扫码区自动获取焦点
                    et_colorCode.requestFocus();
                    et_colorCode.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, et_colorCode.getLeft() + 5, et_colorCode.getTop() + 5, 0));
                    et_colorCode.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, et_colorCode.getLeft() + 5, et_colorCode.getTop() + 5, 0));
                }
                generalBarcode();
                break;
            case R.id.colorCode:
                if (resultCode == 1) {
                    et_colorCode.setText(data.getStringExtra("Name"));
                    et_colorCode.setSelection(et_colorCode.getText().toString().length());
                    colorId = data.getStringExtra("ColorID");
                    colorCode = data.getStringExtra("ColorCode");
                    colorName = data.getStringExtra("ColorName");
                    // 设置扫码区自动获取焦点
                    et_sizeCode.requestFocus();
                    et_sizeCode.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, et_sizeCode.getLeft() + 5, et_sizeCode.getTop() + 5, 0));
                    et_sizeCode.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, et_sizeCode.getLeft() + 5, et_sizeCode.getTop() + 5, 0));
                }
                generalBarcode();
                break;
            case R.id.sizeCode:
                if (resultCode == 1) {
                    et_sizeCode.setText(data.getStringExtra("Name"));
                    et_sizeCode.setSelection(et_sizeCode.getText().toString().length());
                    sizeId = data.getStringExtra("SizeID");
                    sizeCode = data.getStringExtra("SizeCode");
                    sizeName = data.getStringExtra("SizeName");
                    // 设置扫码区自动获取焦点
                    et_goodsCode.requestFocus();
                }
                generalBarcode();
                break;
            default:
                break;
        }
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
                queryGoodsCode();
            }
            return false;
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            backOrNot();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onHeadRightButton(View v) {
        showRightDialog();
    }

    /**
     * 询问是否返回
     */
    private void backOrNot() {
        Builder dialog = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
        dialog.setTitle("提示");
        dialog.setMessage("退出条码打印功能并返回？");
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
    protected void onHeadLeftButton(View v) {
        // 点击返回时
        backOrNot();
    }

    /**
     * 清空界面信息
     */
    private void reset() {
        et_goodsCode.setText(null);
        et_colorCode.setText(null);
        et_sizeCode.setText(null);
        et_goodsName.setText(null);
        et_retailSales.setText(null);
        et_goodsBarcode.setText(null);
        et_supplierBarcode.setText(null);
        goodsId = null;
        colorId = null;
        sizeId = null;
        goodsCode = null;
        colorCode = null;
        sizeCode = null;
        goodsName = null;
        colorName = null;
        sizeName = null;
        retailSales = null;
        et_goodsCode.requestFocus();
    }

    @Override
    protected void loadViewLayout() {
        setContentView(R.layout.activity_barcode_print);
        setTitle("条码打印");
    }

    @Override
    protected void processLogic() {
        if (NetUtil.hasNetwork(getApplicationContext())) {
            if (LoginParameterUtil.online) {
                // 显示右上角功能
                setHeadRightVisibility(View.VISIBLE);
                setHeadRightText(R.string.reset);
                et_goodsCode.requestFocus();
            } else {
                AlertDialog.Builder dialog = new AlertDialog.Builder(BarcodePrintActivity.this, AlertDialog.THEME_HOLO_LIGHT);
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
            AlertDialog.Builder dialog = new AlertDialog.Builder(BarcodePrintActivity.this, AlertDialog.THEME_HOLO_LIGHT);
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

    /**
     * 选择操作方式
     * 
     * @param view
     */
    private void showRightDialog() {
        final String[] items = {"重新录入", "重新生成"};
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
        alertBuilder.setTitle("选择操作方式");
        alertBuilder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int index) {
                if (index == 0) {
                    // 清空
                    reset();
                } else if (index == 1) {
                    if (null == goodsId || "".equals(goodsId) || "null".equalsIgnoreCase(goodsId) || null == colorId || "".equals(colorId) || "null".equalsIgnoreCase(colorId) || null == sizeId || "".equals(sizeId) || "null".equalsIgnoreCase(sizeId)) {
                        Toast.makeText(BarcodePrintActivity.this, "请检查货品编码，颜色编码，尺码编码是否为空", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // 点击重新生成条码
                    generalBarcode();
                }
                // 关闭提示框
                alertDialog.dismiss();
            }
        });
        alertDialog = alertBuilder.create();
        alertDialog.show();
    }

    /**
     * 打印条码
     * 
     * @param barcode
     */
    private void print(String barcode) {
        StringBuilder sb = new StringBuilder();
        try {
            if (LoginParameterUtil.showGoodsCode) {
                sb.append("   货品：").append(goodsCode).append("\n");
            }
            if (LoginParameterUtil.showGoodsName) {
                sb.append("   品名：").append(goodsName).append("\n");
            }
            if (LoginParameterUtil.showColor && LoginParameterUtil.showSize) {
                sb.append("   颜色：").append(colorName).append("   ");
                sb.append("   尺码：").append(sizeName).append("\n");
            } else {
                if (LoginParameterUtil.showColor) {
                    sb.append("   颜色：").append(colorName).append("\n");
                }
                if (LoginParameterUtil.showSize) {
                    sb.append("   尺码：").append(sizeName).append("\n");
                }
            }
            if (LoginParameterUtil.showRetailSales) {
                sb.append("   售价：").append(retailSales).append("\n");
            }
            String content = sb.toString();
            if (!content.trim().equals("")) {
                printText(sb.toString());
            }
            // 打印条码
            String barcodeType = null;
            int barcodeHeight = 100;
            int barcodeWidth = 300;
            // 判断条码打印参数
            Customer customer = LoginParameterUtil.customer;
            if (customer != null) {
                if (customer.getPrintBarcodeHeight() != null && !"".equals(customer.getPrintBarcodeHeight()) && !"null".equalsIgnoreCase(customer.getPrintBarcodeHeight())) {
                    barcodeHeight = Integer.parseInt(customer.getPrintBarcodeHeight());
                }
                if (customer.getPrintBarcodeHeight() != null && !"".equals(customer.getPrintBarcodeHeight()) && !"null".equalsIgnoreCase(customer.getPrintBarcodeHeight())) {
                    barcodeWidth = Integer.parseInt(customer.getPrintBarcodeWidth());
                }
                if (customer.getPrintBarcodeHeight() != null && !"".equals(customer.getPrintBarcodeHeight()) && !"null".equalsIgnoreCase(customer.getPrintBarcodeHeight())) {
                    barcodeType = customer.getPrintBarcodeType();
                }
            }
            BarcodeFormat format = Tools.getBarcodeType(barcodeType);
            // 打印
            printBitmap(barcode, barcodeWidth, barcodeHeight, true, format);
        } catch (Exception e) {
            Toast.makeText(BarcodePrintActivity.this, "请检查条码打印参数是否正确设置" + e, Toast.LENGTH_LONG).show();
            Logger.e(TAG, e.getMessage());
        }
    }

    /**
     * 打印文本
     * 
     * @param content
     */
    private void printText(String content) {
        printIntent.putExtra("text", content);
        sendBroadcast(printIntent);
    }

    /**
     * 打印图片
     * 
     * @param barcode
     * @param barcodeWidth
     * @param barcodeHeight
     * @param showText
     * @param format
     */
    private void printBitmap(final String barcode, final int barcodeWidth, final int barcodeHeight, final boolean showText, final BarcodeFormat format) {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                Bitmap codeBitmap = BarcodeCreater.creatBarcode(getApplicationContext(), barcode, barcodeWidth, barcodeHeight, showText, format);
                bitmapIntent.putExtra("bitmap", codeBitmap);
                sendBroadcast(bitmapIntent);
                printWrap();
            }
        }, 2000);
    }

    private void printWrap() {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                printText("\n\n\n");
            }
        }, 2000);
    }

    @Override
    protected void setListener() {
        TouchListener tl = new TouchListener();
        et_goodsCode.setOnEditorActionListener(new BarcodeActionListener());
        ftv_goodsCodeIcon.setOnTouchListener(tl);
        et_colorCode.setOnTouchListener(tl);
        et_sizeCode.setOnTouchListener(tl);
        tv_printGoodsBarcode.setOnClickListener(this);
        tv_printSupplierBarcode.setOnClickListener(this);
    }

    @Override
    protected void findViewById() {
        ftv_goodsCodeIcon = (FontTextView) findViewById(R.id.goodsCodeIcon);
        et_goodsCode = (EditText) findViewById(R.id.goodsCode);
        et_colorCode = (EditText) findViewById(R.id.colorCode);
        et_sizeCode = (EditText) findViewById(R.id.sizeCode);
        et_goodsName = (EditText) findViewById(R.id.goodsName);
        et_retailSales = (EditText) findViewById(R.id.retailSales);
        et_goodsBarcode = (EditText) findViewById(R.id.goodsBarcode);
        et_supplierBarcode = (EditText) findViewById(R.id.supplierBarcode);
        tv_printGoodsBarcode = (TextView) findViewById(R.id.printGoodsBarcode);
        tv_printSupplierBarcode = (TextView) findViewById(R.id.printSupplierBarcode);
    }

}
