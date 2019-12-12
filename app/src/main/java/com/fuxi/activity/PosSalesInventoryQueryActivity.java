package com.fuxi.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.text.SpannableString;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import com.fuxi.adspter.InventoryQueryAdspter;
import com.fuxi.main.R;
import com.fuxi.task.ImageTask;
import com.fuxi.util.AppManager;
import com.fuxi.util.Logger;
import com.fuxi.util.LoginParameterUtil;
import com.fuxi.util.NetUtil;
import com.fuxi.util.Tools;
import com.fuxi.vo.BarCode;
import com.fuxi.vo.RequestVo;
import com.fuxi.widget.FontTextView;

/**
 * Title: PosSalesInventoryQueryActivity Description: 零售管理库存查询活动界面
 * 
 * @author LYJ
 * 
 */
public class PosSalesInventoryQueryActivity extends BaseWapperActivity {

    private static final String TAG = "PosSalesInventoryQueryActivity";
    private static String queryPath = "/inventoryQuery.do?posSalesQueryStock";

    private List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
    private TouchListener tl = new TouchListener();
    private EditText et_product; // 货品
    private EditText et_color; // 店铺
    private FontTextView ftv_toggle; // 店铺
    private ImageView iv_pic; // 货品
    private TextView tv_totalCount; // 合计
    private ListView lv_detail; // 添加的货品集合

    private InventoryQueryAdspter adspter;
    private String colorId;
    private String goodsId;
    private String goodsCode;
    private boolean inputFlag; // 输入框是否可用的标志

    @Override
    public void onClick(View v) {
        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
        }
        switch (v.getId()) {
            case R.id.toggle:
                if (inputFlag) {
                    et_product.setText(null);
                    et_product.setFocusable(false);
                    et_product.setOnTouchListener(tl);
                    Drawable drawable = getResources().getDrawable(R.drawable.input_bg);
                    drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight()); // 此为必须写的
                    et_product.setCompoundDrawables(null, null, drawable, null);
                    SpannableString s = new SpannableString("点击选择货品");
                    et_product.setHint(s);
                } else {
                    et_product.setText(null);
                    et_product.setFocusable(true);
                    et_product.setFocusableInTouchMode(true);
                    et_product.setClickable(true);
                    et_product.setOnTouchListener(null);
                    et_product.setCompoundDrawables(null, null, null, null);
                    SpannableString s = new SpannableString("输入条码/货号");
                    et_product.setHint(s);
                    et_product.requestFocus();
                }
                inputFlag = !inputFlag;
                break;
            case R.id.pic:
                if (null != goodsCode && !goodsCode.isEmpty()) {
                    Intent inten = new Intent(PosSalesInventoryQueryActivity.this, PictureActivity.class);
                    inten.putExtra("goodsCode", goodsCode);
                    startActivity(inten);
                } else {
                    Toast.makeText(PosSalesInventoryQueryActivity.this, "当前图片无对应的货品编码", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    /**
     * 库存查询方法
     */
    private void createQuery() {
        String productId = et_product.getText().toString();
        RequestVo vo = new RequestVo();
        vo.requestUrl = queryPath;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("productId", productId);
        map.put("goodsId", goodsId);
        map.put("colorId", colorId);
        vo.requestDataMap = map;
        // 初始化
        initialData();
        super.getDataFromServer(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    if (retObj.getBoolean("success")) {
                        JSONArray jsonArray = retObj.getJSONArray("obj");
                        if (jsonArray.length() == 0) {
                            Toast.makeText(PosSalesInventoryQueryActivity.this, "暂无当前货品的实际库存数据", Toast.LENGTH_SHORT).show();
                            Logger.e(TAG, "暂无当前货品的实际库存数据");
                        }
                        for (int i = 0; i < jsonArray.length(); i++) {
                            Map<String, Object> map = new HashMap<String, Object>();
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            map.put("GoodsCode", jsonObject.getString("GoodsCode"));
                            map.put("Color", jsonObject.getString("Color"));
                            map.put("Size", jsonObject.getString("Size"));
                            map.put("Qty", jsonObject.getInt("Quantity"));
                            dataList.add(map);
                        }
                        // 计算合计
                        countTotal();
                    } else {
                        if ("条码或货号错误".equals(retObj.getString("msg"))) {
                            et_product.selectAll();
                            Toast.makeText(PosSalesInventoryQueryActivity.this, retObj.getString("msg"), Toast.LENGTH_SHORT).show();
                            Logger.e(TAG, retObj.getString("msg"));
                        } else {
                            Toast.makeText(PosSalesInventoryQueryActivity.this, "暂无当前货品的实际库存数据", Toast.LENGTH_SHORT).show();
                            Logger.e(TAG, "暂无当前货品的实际库存数据");
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(PosSalesInventoryQueryActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 初始化设置(界面信息重置)
     */
    private void initialData() {
        dataList.clear();
        goodsId = null;
        colorId = null;
        et_product.setText(null);
        iv_pic.setImageResource(R.drawable.unfind);
    }

    /**
     * 计算并显示总数量和价格
     */
    private void countTotal() {
        int sum = 0;
        for (int j = 0; j < dataList.size(); j++) {
            int num = Integer.parseInt(String.valueOf(dataList.get(j).get("Qty")));
            sum += num;
        }
        tv_totalCount.setText(String.valueOf(sum));
        adspter.refresh(dataList);
    }

    @Override
    protected void onHeadRightButton(View v) {
        // 执行查询
        createQuery();
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    protected void setListener() {
        adspter = new InventoryQueryAdspter(this, dataList);
        lv_detail.setAdapter(adspter);
        et_product.setOnEditorActionListener(new BarcodeActionListener());
        ftv_toggle.setOnClickListener(this);
        iv_pic.setOnClickListener(this);
        et_color.setOnTouchListener(tl);
    }

    private class TouchListener implements OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (v.getId()) {
                case R.id.product:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Intent intent = new Intent(PosSalesInventoryQueryActivity.this, SelectActivity.class);
                        intent.putExtra("selectType", "selectProduct");
                        startActivityForResult(intent, R.id.product);
                        overridePendingTransition(R.anim.activity_open, 0);
                    }
                    break;
                case R.id.color:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        if (goodsId == null || "".equals(goodsId) || "null".equalsIgnoreCase(goodsId)) {
                            Toast.makeText(PosSalesInventoryQueryActivity.this, "请先输入货品编码", Toast.LENGTH_SHORT).show();
                            break;
                        }
                        Intent intent = new Intent(PosSalesInventoryQueryActivity.this, SelectActivity.class);
                        intent.putExtra("selectType", "selectColorByGoodsCode");
                        intent.putExtra("param", goodsId);
                        startActivityForResult(intent, R.id.color);
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
            case R.id.product:
                if (resultCode == 1) {
                    et_product.setText(data.getStringExtra("Code"));
                    goodsId = data.getStringExtra("GoodsID");
                    et_product.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                    et_product.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
                }
                break;
            case R.id.color:
                if (resultCode == 1) {
                    et_color.setText(data.getStringExtra("Name"));
                    colorId = data.getStringExtra("ColorID");
                }
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
            }
            return false;
        }

    }

    /**
     * 根据货号从服务端加载图片
     * 
     * @param barcode
     */
    private void LoadImage(BarCode barcode) {
        // 加载图片
        String url = LoginParameterUtil.customer.getIp();
        url = url.concat("/common.do?image&code=").concat(barcode.getGoodscode());
        File imageFile = Tools.getImage(getApplicationContext(), barcode.getGoodscode());
        if (!imageFile.exists()) {
            ImageTask itTask = new ImageTask(imageFile, url, barcode, iv_pic);
            itTask.execute();
        }
        // 显示图片
        setImage(imageFile);
    }

    /**
     * 设置显示图片
     * 
     * @param imageFile
     */
    private void setImage(File imageFile) {
        setImage(imageFile.getAbsolutePath());
    }

    /**
     * 设置显示图片
     * 
     * @param imagePath
     */
    private void setImage(String imagePath) {
        Bitmap bm = BitmapFactory.decodeFile(imagePath);
        if (null == bm) {
            bm = BitmapFactory.decodeFile(imagePath);
            // 显示默认图片
            if (null == bm) {
                iv_pic.setImageResource(R.drawable.unfind);
                return;
            }
        }
        iv_pic.setImageBitmap(bm);
    }

    @Override
    protected void loadViewLayout() {
        setContentView(R.layout.activity_possales_inventory_query);
        setTitle("零售货品库存查询");
        setHeadRightVisibility(View.VISIBLE);
        setHeadRightText(R.string.query);
    }

    @Override
    protected void processLogic() {
        if (NetUtil.hasNetwork(getApplicationContext())) {
            if (LoginParameterUtil.online) {
                // 获取用户操作权限
                boolean browseRight = LoginParameterUtil.stocktakingQueryRightMap.get("BrowseRight");
                if (browseRight) {
                    // 设置扫码区自动获取焦点
                    et_product.requestFocus();
                    // 查询库存
                    createQuery();
                } else {
                    Builder dialog = new AlertDialog.Builder(PosSalesInventoryQueryActivity.this, AlertDialog.THEME_HOLO_LIGHT);
                    dialog.setTitle("提示");
                    dialog.setMessage("当前暂无库存查询的操作权限");
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
            } else {
                AlertDialog.Builder dialog = new AlertDialog.Builder(PosSalesInventoryQueryActivity.this, AlertDialog.THEME_HOLO_LIGHT);
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
            AlertDialog.Builder dialog = new AlertDialog.Builder(PosSalesInventoryQueryActivity.this, AlertDialog.THEME_HOLO_LIGHT);
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
        lv_detail = (ListView) findViewById(R.id.lv_datas);
        et_color = (EditText) findViewById(R.id.color);
        et_product = (EditText) findViewById(R.id.product);
        ftv_toggle = (FontTextView) findViewById(R.id.toggle);
        iv_pic = (ImageView) findViewById(R.id.pic);
        tv_totalCount = (TextView) findViewById(R.id.totalCount);
    }

}
