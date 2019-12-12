package com.fuxi.activity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import com.fuxi.dao.ImagesDao;
import com.fuxi.main.R;
import com.fuxi.task.ImageTask;
import com.fuxi.util.AppManager;
import com.fuxi.util.FormatImagesUtil;
import com.fuxi.util.Logger;
import com.fuxi.util.LoginParameterUtil;
import com.fuxi.util.NetUtil;
import com.fuxi.util.Tools;
import com.fuxi.vo.BarCode;
import com.fuxi.vo.Images;
import com.fuxi.vo.RequestVo;

/**
 * Title: PictureActivity Description: 货品图片活动界面
 * 
 * @author LYJ
 * 
 */
public class PictureActivity extends BaseWapperActivity {

    private static final String TAG = "PictureActivity";
    private static final String queryPath = "/select.do?getGoodsByCode";

    private LinearLayout ll_pic;
    private ImageView iv_photo;
    private EditText et_goodsCode;
    private EditText et_goodsName;
    private EditText et_retailSales;
    private EditText et_tradePrice;
    private TextView tv_save;
    private TextView tv_upload;
    private TextView tv_addPic;

    private ImagesDao imagesDao = new ImagesDao(this);

    private String goodsCode;
    private String picPath;// 图片存储路径
    private boolean saveFlag = false;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.goodsCode:
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm.isActive()) {
                    imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
                }
                break;
            case R.id.addPic:
            case R.id.photo:
                if (null == goodsCode || "".equals(goodsCode)) {
                    Toast.makeText(PictureActivity.this, "请先选择货品编号", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // intent.putExtra("goodsCode", goodsCode);
                // startActivityForResult(intent, R.id.addPic);
                Intent intent2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File f = Tools.getImage(getApplicationContext(), goodsCode);
                picPath = f.getAbsolutePath();
                Uri uri = Uri.fromFile(f);
                // 为拍摄的图片指定一个存储的路径
                intent2.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                startActivityForResult(intent2, R.id.addPic);
                break;
            // 保存图片
            case R.id.save:
                try {
                    int count = 0;
                    if (null == goodsCode || "".equals(goodsCode)) {
                        Toast.makeText(PictureActivity.this, "请先选择货品编号", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Images img = new Images(goodsCode, generateCurrentDate());
                    Images images = imagesDao.find(goodsCode);
                    if (!saveFlag) {
                        Toast.makeText(PictureActivity.this, "当前没有货品 " + goodsCode + " 要替换的图片", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (null == images || images.getGoodsCode().isEmpty()) {
                        count = imagesDao.insert(img);
                    } else {
                        Toast.makeText(PictureActivity.this, "已覆盖本地同货号的货品图片", Toast.LENGTH_SHORT).show();
                        count = imagesDao.update(img);
                    }
                    if (count > 0) {
                        Toast.makeText(PictureActivity.this, "图片已保存到本地", Toast.LENGTH_SHORT).show();
                        initialData();
                    } else {
                        Toast.makeText(PictureActivity.this, "图片保存失败,请稍后重试!", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(PictureActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
                break;
            // 上传图片到服务器
            case R.id.upload:
                File file = Tools.getImage(getApplicationContext(), goodsCode);
                if (null != goodsCode && !"".equals(goodsCode) && null != file && file.length() != 0) {
                    // 自动保存当前图片信息
                    tv_save.callOnClick();
                    // 上传图片
                    Intent inten = new Intent(PictureActivity.this, UploadPictureActivity.class);
                    startActivity(inten);
                    finish();
                } else {
                    Intent inten = new Intent(PictureActivity.this, UploadPictureActivity.class);
                    startActivity(inten);
                    finish();
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onHeadRightButton(View v) {
        // 重置
        initialData();
    }

    @Override
    protected void onHeadLeftButton(View v) {
        if (saveFlag) {
            // 点击返回时询问是否保存数据
            saveOrNot();
        } else {
            finish();
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    protected void loadViewLayout() {
        setContentView(R.layout.activity_picture);
        setTitle("货品图片拍照");
    }

    @Override
    protected void processLogic() {
        if (NetUtil.hasNetwork(getApplicationContext())) {
            if (LoginParameterUtil.online) {
                boolean goodsBrowseRight = LoginParameterUtil.goodsRightMap.get("BrowseRight");
                boolean goodsModifyRight = LoginParameterUtil.goodsRightMap.get("ModifyRight");
                if (goodsBrowseRight && goodsModifyRight) {
                    Bundle bundle = this.getIntent().getExtras();
                    if (null != bundle) {
                        goodsCode = bundle.getString("goodsCode");
                        et_goodsCode.setText(goodsCode);
                        query();
                    }
                    ll_pic.removeView(iv_photo);
                    // 设置扫码区自动获取焦点
                    et_goodsCode.requestFocus();
                    // 设置右上角图标
                    setHeadRightText(R.string.reset);
                    setHeadRightVisibility(View.VISIBLE);
                } else {
                    Builder dialog = new AlertDialog.Builder(PictureActivity.this, AlertDialog.THEME_HOLO_LIGHT);
                    dialog.setTitle("提示");
                    dialog.setMessage("当前暂无货品图片的操作权限");
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
                AlertDialog.Builder dialog = new AlertDialog.Builder(PictureActivity.this, AlertDialog.THEME_HOLO_LIGHT);
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
            AlertDialog.Builder dialog = new AlertDialog.Builder(PictureActivity.this, AlertDialog.THEME_HOLO_LIGHT);
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
    protected void setListener() {
        TouchListener tl = new TouchListener();
        et_goodsCode.setOnTouchListener(tl);
        et_goodsCode.setOnEditorActionListener(new BarcodeActionListener());
        iv_photo.setOnClickListener(this);
        tv_save.setOnClickListener(this);
        tv_upload.setOnClickListener(this);
        tv_addPic.setOnClickListener(this);
    }

    @Override
    protected void findViewById() {
        ll_pic = (LinearLayout) findViewById(R.id.ll_pic);
        iv_photo = (ImageView) findViewById(R.id.photo);
        et_goodsCode = (EditText) findViewById(R.id.goodsCode);
        et_goodsName = (EditText) findViewById(R.id.goodsName);
        et_retailSales = (EditText) findViewById(R.id.retailSales);
        et_tradePrice = (EditText) findViewById(R.id.tradePrice);
        tv_save = (TextView) findViewById(R.id.save);
        tv_upload = (TextView) findViewById(R.id.upload);
        tv_addPic = (TextView) findViewById(R.id.addPic);
    }

    /**
     * 返回时询问是否保存
     */
    private void saveOrNot() {
        // 询问是否保存修改的发货单数据
        Builder dialog = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
        dialog.setTitle("保存提示");
        dialog.setMessage("当前货品图片信息尚未保存,确定要返回吗？");
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
     * 货号条码查询方法
     */
    private void query() {
        goodsCode = et_goodsCode.getText().toString();
        // 非空判断
        if (null == goodsCode || "".equals(goodsCode)) {
            et_goodsCode.setText(null);
            Toast.makeText(PictureActivity.this, "条码或货号不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        RequestVo vo = new RequestVo();
        vo.requestUrl = queryPath;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("goodsCode", goodsCode);
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
                        JSONArray array = retObj.getJSONArray("obj");
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject json = array.getJSONObject(i);
                            goodsCode = json.getString("Code");
                            et_goodsCode.setText(goodsCode);
                            et_goodsCode.setEnabled(false);
                            et_goodsCode.setFocusable(false);
                            et_goodsName.setText(json.getString("Name"));
                            et_tradePrice.setText(Tools.formatDecimal(json.getString("TradePrice")));
                            et_retailSales.setText(Tools.formatDecimal(json.getString("RetailSales")));
                        }
                        // 加载图片
                        BarCode bc = new BarCode();
                        bc.setGoodscode(goodsCode);
                        LoadImage(bc);
                    } else {
                        if ("条码或货号错误".equals(retObj.getString("msg"))) {
                            et_goodsCode.selectAll();
                            Toast.makeText(PictureActivity.this, retObj.getString("msg"), Toast.LENGTH_SHORT).show();
                            Logger.e(TAG, retObj.getString("msg"));
                        }
                    }
                } catch (JSONException e) {
                    Toast.makeText(PictureActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 根据货品编码从服务器加载货品图片
     * 
     * @param barcode
     */
    private void LoadImage(BarCode barcode) {
        // 加载图片
        String url = LoginParameterUtil.customer.getIp();
        url = url.concat("/common.do?image&code=").concat(barcode.getGoodscode());
        File imageFile = Tools.getImage(getApplicationContext(), barcode.getGoodscode());
        if (!imageFile.exists()) {
            ImageTask itTask = new ImageTask(imageFile, url, barcode, iv_photo);
            itTask.execute();
        }
        // 显示图片
        ll_pic.removeView(tv_addPic);
        ll_pic.removeView(iv_photo);
        ll_pic.addView(iv_photo);
        setImage(imageFile);
    }

    /**
     * 设置显示货品图片
     * 
     * @param imageFile
     */
    private void setImage(File imageFile) {
        setImage(imageFile.getAbsolutePath());
    }

    /**
     * 设置显示货品图片
     * 
     * @param imagePath
     */
    private void setImage(String imagePath) {
        Bitmap bm = BitmapFactory.decodeFile(imagePath);
        if (null == bm) {
            bm = BitmapFactory.decodeFile(imagePath);
            if (null == bm) {
                ll_pic.removeView(tv_addPic);
                ll_pic.removeView(iv_photo);
                ll_pic.addView(tv_addPic);
                return;
            }
        }
        iv_photo.setImageBitmap(bm);
    }

    /**
     * 获取系统当前时间,用于记录照片的保存时间
     * 
     * @return
     */
    private String generateCurrentDate() {
        String code = null;
        SimpleDateFormat yyyymmddhhmmss = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        code = yyyymmddhhmmss.format(new Date());
        return code;
    }

    /**
     * 初始化界面信息(重置界面)
     */
    private void initialData() {
        goodsCode = null;
        et_goodsCode.setText(null);
        et_goodsCode.setEnabled(true);
        et_goodsCode.setFocusable(true);
        et_goodsCode.setFocusableInTouchMode(true);
        et_goodsCode.requestFocus();
        et_tradePrice.setText(null);
        et_retailSales.setText(null);
        et_goodsName.setText(null);
        ll_pic.removeView(tv_addPic);
        ll_pic.removeView(iv_photo);
        ll_pic.addView(tv_addPic);
        saveFlag = false;
    }

    private class TouchListener implements OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (v.getId()) {
                case R.id.goodsCode:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Intent intent = new Intent(PictureActivity.this, SelectActivity.class);
                        intent.putExtra("selectType", "selectProduct");
                        startActivityForResult(intent, R.id.goodsCode);
                        overridePendingTransition(R.anim.activity_open, 0);
                    }
                    break;
                default:
                    break;
            }
            return false;
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
                query();
            }
            return false;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case R.id.goodsCode:
                if (resultCode == 1) {
                    et_goodsCode.setText(data.getStringExtra("Code"));
                    et_goodsName.setText(data.getStringExtra("Name"));
                    et_tradePrice.setText(data.getStringExtra("TradePrice"));
                    et_retailSales.setText(data.getStringExtra("RetailSales"));
                    et_goodsCode.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                    et_goodsCode.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
                }
                break;
            case R.id.addPic:
            case R.id.photo:
                if (requestCode == Activity.DEFAULT_KEYS_DIALER || resultCode == RESULT_OK) {
                    // MainActivity接收Camera返回的消息，然后将已经写入的图片显示在ImageView内
                    // if (data.getData() != null || data.getExtras() != null){
                    // //防止没有返回结果
                    // Bundle bundle = data.getExtras();
                    // Bitmap bitmap = (Bitmap) bundle.get("data");//
                    // 获取相机返回的数据，并转换为Bitmap图片格式
                    // FileOutputStream b = null;
                    // File fileName = Tools.getImage(getApplicationContext(),
                    // goodsCode);
                    // try{
                    // if(!fileName.exists()){
                    // fileName.createNewFile();
                    // }
                    // }catch (Exception e){
                    // e.printStackTrace();
                    // }
                    // try {
                    // b = new FileOutputStream(fileName);
                    // bitmap.compress(Bitmap.CompressFormat.PNG, 100, b);// 把数据写入文件
                    // } catch (FileNotFoundException e) {
                    // e.printStackTrace();
                    // } finally {
                    // try {
                    // b.flush();
                    // b.close();
                    // } catch (IOException e) {
                    // e.printStackTrace();
                    // }
                    // }
                    // try
                    // {
                    // ll_pic.removeView(tv_addPic);
                    // ll_pic.removeView(iv_photo);
                    // ll_pic.addView(iv_photo);
                    // iv_photo.setImageBitmap(bitmap);// 将图片显示在ImageView里
                    // saveFlag = true;
                    // }catch(Exception e){
                    // Log.e("error", e.getMessage());
                    // }
                    // }
                    // 使用原图上传
                    try {
                        Bitmap bitmap = FormatImagesUtil.decodeSampledBitmapFromFile(picPath, 680, 800);
                        bitmap = FormatImagesUtil.compressBitmap(bitmap, 100);
                        ll_pic.removeView(tv_addPic);
                        ll_pic.removeView(iv_photo);
                        ll_pic.addView(iv_photo);
                        iv_photo.setImageBitmap(bitmap); // 将图片显示在ImageView里
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                        FileOutputStream out = new FileOutputStream(new File(picPath));
                        out.write(baos.toByteArray());
                        out.flush();
                        out.close();
                        saveFlag = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("error", e.getMessage());
                    }
                }
                break;
            default:
                break;
        }
    }

}
