package com.fuxi.activity;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.fuxi.adspter.UploadPictureAdspter;
import com.fuxi.dao.ImagesDao;
import com.fuxi.dao.ParamerDao;
import com.fuxi.main.R;
import com.fuxi.util.Logger;
import com.fuxi.util.LoginParameterUtil;
import com.fuxi.util.Tools;
import com.fuxi.vo.Images;
import com.fuxi.vo.Paramer;

/**
 * Title: UploadPictureActivity Description: 货品图片上传活动界面
 * 
 * @author LYJ
 * 
 */
public class UploadPictureActivity extends BaseWapperActivity {

    private static final String TAG = "UploadPictureActivity";
    private static final int UPLOAD_ERROR = -1;
    private static final int UPLOAD_SUCCESS = 1;
    private static final String path = "/common.do?uploadImages";

    private static String strIp;

    private List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
    private List<Map<String, Object>> upLoadList = new ArrayList<Map<String, Object>>();
    private UploadPictureAdspter picAdspter;
    private ImagesDao imagesDao = new ImagesDao(this);
    private ParamerDao paramerDao = new ParamerDao(this);

    private ListView lv_pics;
    private TextView tv_upload;
    private TextView tv_delete;
    private CheckBox cb_check;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        // 点击全选
            case R.id.check:
                selectAll();
                break;
            case R.id.delete:
                if (dataList.size() > 0) {
                    if (hasUploadFile()) {
                        // 询问是否错误操作
                        Builder dialog = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
                        dialog.setTitle("删除提示");
                        dialog.setMessage("确定要删除勾选的货品图片吗？");
                        // 相当于点击确认按钮
                        dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                delete();
                                // 刷新ListView集合
                                picAdspter.refresh(dataList);
                            }
                        });
                        // 相当于点击取消按钮
                        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {}
                        });
                        dialog.create();
                        dialog.show();
                    } else {
                        Toast.makeText(getApplicationContext(), "请选择要移除的货品图片", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "当前没有可移除的货品图片", Toast.LENGTH_SHORT).show();
                    cb_check.setChecked(false);
                    return;
                }
                break;
            // 上传图片到服务器
            case R.id.upload:
                if (dataList.size() > 0) {
                    if (hasUploadFile()) {
                        showProgressDialog(null, "图片正在上传中,请稍后...");
                        getUploadItems();
                        upload();
                        // 刷新ListView集合
                        picAdspter.refresh(dataList);
                    } else {
                        Toast.makeText(getApplicationContext(), "请选择要上传的货品图片", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "当前没有可上传的货品图片", Toast.LENGTH_SHORT).show();
                    cb_check.setChecked(false);
                    return;
                }
                break;
            default:
                break;
        }
    }

    /**
     * 判断当前是否有选中ListView中的项
     * 
     * @return
     */
    private boolean hasUploadFile() {
        boolean flag = false;
        for (int i = 0; i < dataList.size(); i++) {
            Map<String, Object> map = dataList.get(i);
            boolean check = Boolean.parseBoolean(String.valueOf(map.get("Select")));
            if (check) {
                flag = true;
            }
        }
        return flag;
    }

    /**
     * 删除选中的项
     */
    private void delete() {
        for (int i = 0; i < dataList.size(); i++) {
            Map<String, Object> map = dataList.get(i);
            boolean check = Boolean.parseBoolean(String.valueOf(map.get("Select")));
            String goodsCode = String.valueOf(map.get("GoodsCode"));
            if (check) {
                dataList.remove(i);
                i--;
                imagesDao.delete(goodsCode);
                File imageFile = Tools.getImage(getApplicationContext(), goodsCode);
                if (imageFile.exists()) {
                    imageFile.delete();
                }
            }
            // 刷新ListView集合
            picAdspter.refresh(dataList);
        }
        cb_check.setChecked(false);
        Toast.makeText(getApplicationContext(), "删除成功", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    protected void loadViewLayout() {
        setContentView(R.layout.activity_upload_picture);
        setTitle("货品图片上传");
    }

    @Override
    protected void processLogic() {
        // 获取要上传的图片数据
        getData();
        // 获取服务器IP
        getRequestPath();
    }

    @Override
    protected void setListener() {
        picAdspter = new UploadPictureAdspter(this, dataList);
        tv_upload.setOnClickListener(this);
        tv_delete.setOnClickListener(this);
        cb_check.setOnClickListener(this);
        lv_pics.setAdapter(picAdspter);
        lv_pics.setOnItemClickListener(new ListViewItemClick());
    }

    @Override
    protected void findViewById() {
        lv_pics = (ListView) findViewById(R.id.pic_detail);
        tv_upload = (TextView) findViewById(R.id.upload);
        tv_delete = (TextView) findViewById(R.id.delete);
        cb_check = (CheckBox) findViewById(R.id.check);
    }

    @Override
    protected void onHeadLeftButton(View v) {
        // 点击返回时 结束线程
        finish();
    }

    /**
     * 获取要上传到服务器的图片
     */
    private void getData() {
        try {
            List<Images> list = imagesDao.getList();
            for (int i = 0; i < list.size(); i++) {
                Images images = list.get(i);
                Map<String, Object> map = new HashMap<String, Object>();
                if (images.getGoodsCode() == null || "null".equalsIgnoreCase(images.getGoodsCode())) {
                    imagesDao.delete("null");
                    continue;
                }
                map.put("GoodsCode", images.getGoodsCode());
                map.put("Date", images.getMadeDate());
                map.put("Select", false);
                dataList.add(map);
            }
            picAdspter.refresh(dataList);
        } catch (Exception e) {
            Toast.makeText(UploadPictureActivity.this, "系统错误", Toast.LENGTH_LONG).show();
            Logger.e(TAG, e.getMessage());
        }
    }

    /**
     * 全选或全不选
     */
    private void selectAll() {
        if (cb_check.isChecked()) {
            for (int i = 0; i < dataList.size(); i++) {
                Map<String, Object> map = dataList.get(i);
                map.put("Select", true);
            }
        } else {
            for (int i = 0; i < dataList.size(); i++) {
                Map<String, Object> map = dataList.get(i);
                map.put("Select", false);
            }
        }
        // 刷新ListView集合
        picAdspter.refresh(dataList);
    }

    /**
     * 获取要上传的图片集合
     */
    private void getUploadItems() {
        for (int i = 0; i < dataList.size(); i++) {// upLoadList
            Map<String, Object> map = dataList.get(i);
            boolean flag = Boolean.parseBoolean(String.valueOf(map.get("Select")));
            if (flag) {
                upLoadList.add(map);
            }
        }
    }

    /**
     * 批量上传图片到服务器
     */
    private void upload() {
        for (int i = 0; i < upLoadList.size(); i++) {
            if (dataList.size() > 0) {
                Map<String, Object> map = dataList.get(i);
                String goodsCode = String.valueOf(map.get("GoodsCode"));
                File file = Tools.getImage(getApplicationContext(), goodsCode);
                if (null != file && file.length() != 0) {
                    // 上传图片到服务器
                    uploadFile(i, upLoadList.size(), goodsCode, file);
                }
            }
        }
    }

    /**
     * 上传文件至服务器的方法
     * 
     * @param index
     * @param total
     * @param goodsCode
     * @param file
     */
    private void uploadFile(final int index, final int total, final String goodsCode, final File file) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String end = "\r\n";
                String twoHyphens = "--";
                String boundary = "*****";
                try {
                    URL url = new URL(strIp + path);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setConnectTimeout(20000);
                    con.setReadTimeout(15000);
                    /* 允许Input、Output，不使用Cache */
                    con.setDoInput(true);
                    con.setDoOutput(true);
                    con.setUseCaches(false);
                    /* 设置传送的method=POST */
                    con.setRequestMethod("POST");
                    /* setRequestProperty */
                    con.setRequestProperty("Connection", "Keep-Alive");
                    con.setRequestProperty("Charset", "UTF-8");
                    con.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                    /* 设置DataOutputStream */
                    DataOutputStream ds = new DataOutputStream(con.getOutputStream());
                    ds.write((twoHyphens + boundary + end).getBytes());
                    ds.write(("Content-Disposition:form-data;name=\"file\";filename=\"" + file.getName() + "\"" + end).getBytes());
                    ds.write(end.getBytes());
                    /* 取得文件的FileInputStream */
                    FileInputStream fStream = new FileInputStream(file);
                    /* 设置每次写入1024bytes */
                    int bufferSize = 1024;
                    byte[] buffer = new byte[bufferSize];
                    int length = -1;
                    /* 从文件读取数据至缓冲区 */
                    while ((length = fStream.read(buffer)) != -1) {
                        /* 将资料写入DataOutputStream中 */
                        ds.write(buffer, 0, length);
                    }
                    ds.write(end.getBytes());
                    ds.write((twoHyphens + boundary + twoHyphens + end).getBytes());
                    /* close streams */
                    fStream.close();
                    ds.flush();
                    /* 取得Response内容 */
                    InputStream is = con.getInputStream();
                    int ch;
                    StringBuffer b = new StringBuffer();
                    while ((ch = is.read()) != -1) {
                        b.append((char) ch);
                    }
                    /* 将Response显示于Dialog */
                    Log.e(TAG, "上传成功" + b.toString().trim());
                    /* 关闭DataOutputStream */
                    ds.close();
                    if (index == (total - 1)) {
                        Message msg = new Message();
                        msg.what = UPLOAD_SUCCESS;
                        handler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "上传失败" + e);
                    Message msg = new Message();
                    msg.what = UPLOAD_ERROR;
                    handler.sendMessage(msg);
                }
            }
        }).start();;
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPLOAD_ERROR:
                    closeProgressDialog();
                    cb_check.setChecked(false);
                    // 刷新ListView集合
                    picAdspter.refresh(dataList);
                    // 对话框通知
                    Toast.makeText(getApplicationContext(), "网络连接超时,图片上传失败", Toast.LENGTH_SHORT).show();
                    break;
                case UPLOAD_SUCCESS:
                    // 返回图片上传结果
                    for (int j = 0; j < upLoadList.size(); j++) {
                        Map<String, Object> map = upLoadList.get(j);
                        String goodsCode = String.valueOf(map.get("GoodsCode"));
                        for (int i = 0; i < dataList.size(); i++) {
                            Map<String, Object> temp = dataList.get(i);
                            String gcode = String.valueOf(temp.get("GoodsCode"));
                            if (goodsCode.equals(gcode)) {
                                imagesDao.delete(goodsCode);
                                dataList.remove(i);
                            }
                        }
                    }
                    // 刷新ListView集合
                    picAdspter.refresh(dataList);
                    upLoadList.clear();
                    cb_check.setChecked(false);
                    closeProgressDialog();
                    // 图片上传成功的提示框
                    Builder dialog = new AlertDialog.Builder(UploadPictureActivity.this, AlertDialog.THEME_HOLO_LIGHT);
                    dialog.setTitle("提示");
                    dialog.setMessage("货品图片已成功上传至服务器 " + LoginParameterUtil.imagePath + " 目录下");
                    // 相当于点击确认按钮
                    dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
                    // 相当于点击取消按钮
                    dialog.setCancelable(false);
                    dialog.create();
                    dialog.show();
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 获取用户连接服务器的IP地址
     */
    private void getRequestPath() {
        Paramer ip = paramerDao.find("ip");
        if (null != ip) {
            strIp = ip.getValue();
        }
    }

    /**
     * Title: ListViewItemClick Description: ListView的点击事件
     */
    class ListViewItemClick implements OnItemClickListener {

        @SuppressWarnings("unchecked")
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ListView listView = (ListView) parent;
            HashMap<String, String> map = (HashMap<String, String>) listView.getItemAtPosition(position);
            boolean select = Boolean.parseBoolean(String.valueOf(map.get("Select")));
            Map<String, Object> dataMap = dataList.get(position);
            if (select) {
                dataMap.put("Select", false);
            } else {
                dataMap.put("Select", true);
            }
            // 判断是否已经全选
            int i = 0;
            for (; i < dataList.size(); i++) {
                Map<String, Object> temp = dataList.get(i);
                if ((Boolean) temp.get("Select")) {
                    continue;
                } else {
                    break;
                }
            }
            if (i == dataList.size()) {
                cb_check.setChecked(true);
            } else {
                cb_check.setChecked(false);
            }
            // 刷新ListView集合
            picAdspter.refresh(dataList);
        }

    }

}
