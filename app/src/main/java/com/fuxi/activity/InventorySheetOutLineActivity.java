package com.fuxi.activity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.fuxi.adspter.InventorySheetOutLineAdspter;
import com.fuxi.dao.OutLineStocktakingDao;
import com.fuxi.main.R;
import com.fuxi.util.CodeUtils;
import com.fuxi.util.Logger;
import com.fuxi.util.LoginParameterUtil;
import com.fuxi.util.NetUtil;
import com.fuxi.util.Tools;
import com.fuxi.vo.OutLineStocktaking;
import com.fuxi.vo.RequestVo;

/**
 * Title: InventorySheetOutLineActivity Description: 离线盘点列表界面
 * 
 * @author LYJ
 * 
 */
public class InventorySheetOutLineActivity extends BaseWapperActivity {

    private static final String TAG = "InventorySheetOutLineActivity";
    private static final String sendDatasToGenerateText = "/common.do?generateText";
    private static final String sendDatasMulitToGenerateText = "/common.do?generateMulitText";

    private List<Map<String, Object>> mulitList = new ArrayList<Map<String, Object>>();
    private List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
    private InventorySheetOutLineAdspter adspter;
    private AlertDialog alertDialog;
    private OutLineStocktakingDao lineStocktakingDao = new OutLineStocktakingDao(this);
    private OutLineStocktakingDao stocktakingDao = new OutLineStocktakingDao(this);

    private LinearLayout llBottom;
    private ListView lvDatas;
    private CheckBox cbCheckAll;
    private TextView tvExport;
    private TextView tvDelete;
    private TextView tvTotal;
    private TextView tvOperation;
    private ImageView image;

    // 是否多选
    private CodeUtils codeUtils;
    private boolean multiSelectFlag = false;

    /**
     * 点击新增离线盘点单据
     */
    private void addItem() {
        Intent intent = new Intent(InventorySheetOutLineActivity.this, InventorySheetOutLineDetailActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        // 点击全选
            case R.id.checkAll:
                selectAll();
                break;
            case R.id.export:
                if (hasSelected()) {
                    getMultiItems();
                    if (mulitList.size() == 0) {
                        Toast.makeText(InventorySheetOutLineActivity.this, "请先选择要导出的记录", Toast.LENGTH_SHORT).show();
                    } else {
                        // 选择导出方式
                        showSingleAlertDialog(v);
                    }
                } else {
                    Toast.makeText(InventorySheetOutLineActivity.this, "请先选择要导出的记录", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.delete:
                if (hasSelected()) {
                    getMultiItems();
                    if (mulitList.size() == 0) {
                        Toast.makeText(InventorySheetOutLineActivity.this, "请先选择要删除的记录", Toast.LENGTH_SHORT).show();
                    } else {
                        final View v1 = (LinearLayout) getLayoutInflater().inflate(R.layout.menu_password_input, null);
                        final EditText et_password = (EditText) v1.findViewById(R.id.et_password);
                        image = (ImageView) v1.findViewById(R.id.image);
                        image.setOnClickListener(new OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                codeUtils = CodeUtils.getInstance();
                                Bitmap bitmap = codeUtils.createBitmap();
                                image.setImageBitmap(bitmap);
                            }
                        });
                        // 生成验证码
                        codeUtils = CodeUtils.getInstance();
                        Bitmap bitmap = codeUtils.createBitmap();
                        image.setImageBitmap(bitmap);
                        // 弹出输入折让金额
                        Builder dialog = new AlertDialog.Builder(InventorySheetOutLineActivity.this, AlertDialog.THEME_HOLO_LIGHT);
                        dialog.setTitle("请输入验证码确认当前操作");
                        dialog.setView(v1);
                        // 相当于点击确认按钮
                        dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String password = et_password.getText().toString();
                                String code = codeUtils.getCode();
                                if (password.equalsIgnoreCase(code)) {
                                    for (int i = 0; i < mulitList.size(); i++) {
                                        Map<String, Object> map = mulitList.get(i);
                                        String no = String.valueOf(map.get("No"));
                                        for (int j = 0; j < dataList.size(); j++) {
                                            Map<String, Object> temp = dataList.get(j);
                                            String tno = String.valueOf(temp.get("No"));
                                            if (tno.equals(no)) {
                                                dataList.remove(j);
                                                j--;
                                            }
                                        }
                                        delete(no);
                                    }
                                    countTotal();
                                    // 刷新ListView集合
                                    adspter.refresh(dataList);
                                    mulitList.clear();
                                    initListView();
                                    Toast.makeText(InventorySheetOutLineActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(InventorySheetOutLineActivity.this, "验证码错误", Toast.LENGTH_SHORT).show();
                                }
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
                } else {
                    Toast.makeText(InventorySheetOutLineActivity.this, "请先选择要删除的记录", Toast.LENGTH_SHORT).show();
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
        setContentView(R.layout.activity_inventory_sheet_out_line);
        setTitle("离线盘点列表");
        // 显示右上角按钮
        setHeadRightVisibility(View.VISIBLE);
    }

    @Override
    protected void processLogic() {
        // 获取盘点后未导出的数据
        getData();
    }

    @Override
    protected void setListener() {
        cbCheckAll.setOnClickListener(this);
        tvExport.setOnClickListener(this);
        tvDelete.setOnClickListener(this);
        adspter = new InventorySheetOutLineAdspter(this, dataList);
        lvDatas.setAdapter(adspter);
        lvDatas.setOnItemClickListener(new ListViewItemClick());
        lvDatas.setOnItemLongClickListener(new ListViewItemLongClick());
    }

    @Override
    protected void findViewById() {
        llBottom = (LinearLayout) findViewById(R.id.ll_bottom);
        cbCheckAll = (CheckBox) findViewById(R.id.checkAll);
        tvExport = (TextView) findViewById(R.id.export);
        tvOperation = (TextView) findViewById(R.id.operation);
        tvDelete = (TextView) findViewById(R.id.delete);
        tvTotal = (TextView) findViewById(R.id.total);
        lvDatas = (ListView) findViewById(R.id.list_detail);
    }

    @Override
    protected void onHeadRightButton(View v) {
        addItem();
    }

    @Override
    protected void onHeadLeftButton(View v) {
        if (multiSelectFlag) {
            initListView();
        } else {
            finish();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (multiSelectFlag) {
                initListView();
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 获取历史离线盘点数据
     */
    private void getData() {
        try {
            List<OutLineStocktaking> list = lineStocktakingDao.getList();
            List<String> nolist = lineStocktakingDao.getNoList();
            for (int i = 0; i < nolist.size(); i++) {
                OutLineStocktaking lineStocktaking = null;
                String no = nolist.get(i);
                int total = 0;
                Map<String, Object> map = new HashMap<String, Object>();
                for (int j = 0; j < list.size(); j++) {
                    lineStocktaking = list.get(j);
                    if (no.equals(lineStocktaking.getNo())) {
                        total += lineStocktaking.getQuantity();
                        map.put("DepartmentCode", lineStocktaking.getDepartmentCode());
                        map.put("DepartmentID", lineStocktaking.getDepartmentID());
                        map.put("DepartmentName", lineStocktaking.getDepartmentName());
                        map.put("MadeDate", lineStocktaking.getMadeDate());
                        map.put("No", lineStocktaking.getNo());
                        map.put("ShelvesNo", lineStocktaking.getShelvesNo());
                        map.put("Select", false);
                    }
                }
                map.put("Quantity", total);
                dataList.add(map);
            }
            if (dataList.size() == 0) { // 提示没有盘点数据
                prompt();
            }
            adspter.refresh(dataList);
        } catch (Exception e) {
            Toast.makeText(InventorySheetOutLineActivity.this, "系统错误", Toast.LENGTH_LONG).show();
            Logger.e(TAG, e.getMessage());
        }
    }

    /**
     * 删除缓存中的数据
     * 
     * @param no
     */
    private void delete(String no) {
        List<OutLineStocktaking> list = lineStocktakingDao.find(no);
        for (int i = 0; i < list.size(); i++) {
            OutLineStocktaking lineStocktaking = list.get(i);
            lineStocktakingDao.delete(String.valueOf(lineStocktaking.getId()));
        }
    }

    /**
     * 无离线盘点数据时提示
     */
    private void prompt() {
        Builder dialog = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
        dialog.setTitle("系统提示");
        dialog.setMessage("当前没有离线盘点数据！");
        // 相当于点击确认按钮
        dialog.setPositiveButton("我知道了", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {}
        });
        dialog.create();
        dialog.show();
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
            if (multiSelectFlag) {
                // 批量操作
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
                    cbCheckAll.setChecked(true);
                } else {
                    cbCheckAll.setChecked(false);
                }
                countTotal();
                // 刷新ListView集合
                adspter.refresh(dataList);
            } else {
                // 查看明细
                String shelvesNo = String.valueOf(map.get("ShelvesNo"));
                String departmentId = String.valueOf(map.get("DepartmentID"));
                String departmentCode = String.valueOf(map.get("DepartmentCode"));
                String departmentName = String.valueOf(map.get("DepartmentName"));
                Intent intent = new Intent(InventorySheetOutLineActivity.this, InventorySheetOutLineDetailActivity.class);
                intent.putExtra("ShelvesNo", shelvesNo);
                intent.putExtra("DepartmentID", departmentId);
                intent.putExtra("DepartmentCode", departmentCode);
                intent.putExtra("DepartmentName", departmentName);
                startActivity(intent);
                finish();
            }
        }

    }

    class ListViewItemLongClick implements OnItemLongClickListener {

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            adspter.showCheckBox();
            multiSelectFlag = true;
            tvOperation.setVisibility(View.VISIBLE);
            llBottom.setVisibility(View.VISIBLE);
            // 刷新ListView集合
            adspter.refresh(dataList);
            return true;
        }

    }

    /**
     * 判断当前是否有选中ListView中的项
     * 
     * @return
     */
    private boolean hasSelected() {
        boolean flag = false;
        for (int i = 0; i < dataList.size(); i++) {
            Map<String, Object> map = dataList.get(i);
            boolean check = Boolean.parseBoolean(String.valueOf(map.get("Select")));
            if (check) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    /**
     * 获取要批量操作的集合
     */
    private void getMultiItems() {
        for (int i = 0; i < dataList.size(); i++) {
            Map<String, Object> map = dataList.get(i);
            boolean flag = Boolean.parseBoolean(String.valueOf(map.get("Select")));
            if (flag) {
                mulitList.add(map);
            }
        }
    }

    /**
     * 全选或全不选
     */
    private void selectAll() {
        if (cbCheckAll.isChecked()) {
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
        countTotal();
        // 刷新ListView集合
        adspter.refresh(dataList);
    }

    /**
     * 初始化ListView(恢复多选前的操作)
     */
    private void initListView() {
        multiSelectFlag = false;
        tvOperation.setVisibility(View.GONE);
        llBottom.setVisibility(View.GONE);
        adspter.hideCheckBox();
        // 刷新ListView集合
        adspter.refresh(dataList);
    }

    /**
     * 计算选中的数量
     */
    private void countTotal() {
        int sum = 0;
        int count = 0;
        for (int i = 0; i < dataList.size(); i++) {
            Map<String, Object> map = dataList.get(i);
            boolean flag = Boolean.parseBoolean(String.valueOf(map.get("Select")));
            if (flag) {
                int quantity = Integer.parseInt(String.valueOf(map.get("Quantity")));
                sum += quantity;
                count++;
            }
        }
        tvTotal.setText(String.valueOf(sum));
        tvExport.setText("导出（" + count + "）");
        tvDelete.setText("删除（" + count + "）");
    }

    /**
     * 单选提示框选择条码检验导出方式 1:导出到设备 2.导出到服务器
     * 
     * @param view
     */
    private void showSingleAlertDialog(View view) {
        final String[] items = {"导出到本地", "导出到服务器"};
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
        alertBuilder.setTitle("选择盘点数据导出方式");
        alertBuilder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int index) {
                if (index == 0) {
                    // 导出到本地
                    exportToLocal();
                } else if (index == 1) {
                    if (NetUtil.hasNetwork(getApplicationContext())) {
                        // 导出到服务器
                        exportToServer();
                    } else {
                        // 无网络
                        Builder dialog = new AlertDialog.Builder(InventorySheetOutLineActivity.this, AlertDialog.THEME_HOLO_LIGHT);
                        dialog.setTitle("系统提示");
                        dialog.setMessage("当前无网络连接，请连接网络后重试！");
                        // 相当于点击确认按钮
                        dialog.setPositiveButton("我知道了", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {}
                        });
                        dialog.create();
                        dialog.show();
                    }
                }
                // 关闭提示框
                alertDialog.dismiss();
            }
        });
        alertDialog = alertBuilder.create();
        alertDialog.show();
    }

    /**
     * 导出到本地
     */
    private void exportToLocal() {
        // 导出文件目录
        final String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "fxpda" + File.separator + "export_file";
        File direct = new File(path);
        if (!direct.exists()) {
            direct.mkdirs();
        }
        if (mulitList.size() > 1) {
            // 导出文件操作
            Builder dialog = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
            // 询问是否保存修改的发货单数据
            dialog = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
            dialog.setTitle("提示");
            dialog.setMessage("是否将盘点单数据进行合并？");
            // 相当于点击确认按钮
            dialog.setPositiveButton("合并", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    StringBuffer sb = new StringBuffer();
                    String quantitySum = tvTotal.getText().toString();
                    String departmentCode = null;
                    for (int i = 0; i < mulitList.size(); i++) {
                        Map<String, Object> map = mulitList.get(i);
                        String no = String.valueOf(map.get("No"));
                        departmentCode = String.valueOf(map.get("DepartmentCode"));
                        // 判断数据库中是否有相应单号的数据
                        List<OutLineStocktaking> list = stocktakingDao.find(no);
                        // 格式化盘点内容
                        for (int j = 0; j < list.size(); j++) {
                            OutLineStocktaking lineStocktaking = list.get(j);
                            String barcode = lineStocktaking.getBarcode();
                            int quantity = lineStocktaking.getQuantity();
                            sb.append(barcode + "," + quantity + "\n");
                        }
                    }
                    // 导出Text文件
                    String exportPath = path + File.separator + departmentCode + "_" + generateCurrentTimeCode() + "_" + quantitySum + ".txt";
                    Tools.contentToTxt(exportPath, sb.toString());
                    mulitList.clear();
                    // 导出完成后提示
                    Builder dialog2 = new AlertDialog.Builder(InventorySheetOutLineActivity.this, AlertDialog.THEME_HOLO_LIGHT);
                    dialog2.setTitle("提示");
                    dialog2.setMessage("离线盘点文件已导出到目录 " + exportPath);
                    // 相当于点击确认按钮
                    dialog2.setPositiveButton("我知道了", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            initListView();
                        }
                    });
                    dialog2.setCancelable(false);
                    dialog2.create();
                    dialog2.show();
                }
            });
            // 相当于点击取消按钮
            dialog.setNegativeButton("不合并", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    for (int i = 0; i < mulitList.size(); i++) {
                        Map<String, Object> map = mulitList.get(i);
                        String no = String.valueOf(map.get("No"));
                        String departmentCode = String.valueOf(map.get("DepartmentCode"));
                        String shelvesNo = String.valueOf(map.get("ShelvesNo"));
                        // 判断数据库中是否有相应单号的数据
                        List<OutLineStocktaking> list = stocktakingDao.find(no);
                        // 格式化盘点内容
                        StringBuffer sb = new StringBuffer();
                        int quantitySum = 0;
                        for (int j = 0; j < list.size(); j++) {
                            OutLineStocktaking lineStocktaking = list.get(j);
                            String barcode = lineStocktaking.getBarcode();
                            int quantity = lineStocktaking.getQuantity();
                            quantitySum += quantity;
                            sb.append(barcode + "," + quantity + "\n");
                        }
                        // 导出Text文件
                        String exportPath = path + File.separator + departmentCode + "_" + shelvesNo + "_" + quantitySum + ".txt";
                        Tools.contentToTxt(exportPath, sb.toString());
                    }
                    mulitList.clear();
                    // 导出完成后提示
                    Builder dialog2 = new AlertDialog.Builder(InventorySheetOutLineActivity.this, AlertDialog.THEME_HOLO_LIGHT);
                    dialog2.setTitle("提示");
                    dialog2.setMessage("离线盘点文件已导出到目录 " + path);
                    // 相当于点击确认按钮
                    dialog2.setPositiveButton("我知道了", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            initListView();
                        }
                    });
                    dialog2.setCancelable(false);
                    dialog2.create();
                    dialog2.show();
                }
            });
            dialog.create();
            dialog.show();
        } else {
            for (int i = 0; i < mulitList.size(); i++) {
                Map<String, Object> map = mulitList.get(i);
                String no = String.valueOf(map.get("No"));
                String departmentCode = String.valueOf(map.get("DepartmentCode"));
                String shelvesNo = String.valueOf(map.get("ShelvesNo"));
                // 判断数据库中是否有相应单号的数据
                List<OutLineStocktaking> list = stocktakingDao.find(no);
                // 格式化盘点内容
                StringBuffer sb = new StringBuffer();
                int quantitySum = 0;
                for (int j = 0; j < list.size(); j++) {
                    OutLineStocktaking lineStocktaking = list.get(j);
                    String barcode = lineStocktaking.getBarcode();
                    int quantity = lineStocktaking.getQuantity();
                    quantitySum += quantity;
                    sb.append(barcode + "," + quantity + "\n");
                }
                // 导出Text文件
                String exportPath = path + File.separator + departmentCode + "_" + shelvesNo + "_" + quantitySum + ".txt";
                Tools.contentToTxt(exportPath, sb.toString());
            }
            mulitList.clear();
            // 导出完成后提示
            Builder dialog2 = new AlertDialog.Builder(InventorySheetOutLineActivity.this, AlertDialog.THEME_HOLO_LIGHT);
            dialog2.setTitle("提示");
            dialog2.setMessage("离线盘点文件已导出到目录 " + path);
            // 相当于点击确认按钮
            dialog2.setPositiveButton("我知道了", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    initListView();
                }
            });
            dialog2.setCancelable(false);
            dialog2.create();
            dialog2.show();
        }
    }

    /**
     * 生成当前时间编码
     * 
     * @return
     */
    private String generateCurrentTimeCode() {
        String code = null;
        SimpleDateFormat yyyymmddhhmmss = new SimpleDateFormat("yyMMddHHmmss");
        code = yyyymmddhhmmss.format(new Date());
        return code;
    }

    /**
     * 导出到服务器
     */
    private void exportToServer() {
        if (mulitList.size() > 1) {
            // 导出文件操作
            Builder dialog = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
            // 询问是否保存修改的发货单数据
            dialog = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
            dialog.setTitle("提示");
            dialog.setMessage("是否将盘点单数据进行合并？");
            // 相当于点击确认按钮
            dialog.setPositiveButton("合并", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String departmentCode = null;
                    String quantitySum = tvTotal.getText().toString();
                    List<Map<String, Object>> tempList = new ArrayList<Map<String, Object>>();
                    for (int i = 0; i < mulitList.size(); i++) {
                        Map<String, Object> map = mulitList.get(i);
                        String no = String.valueOf(map.get("No"));
                        departmentCode = String.valueOf(map.get("DepartmentCode"));
                        // 判断数据库中是否有相应单号的数据
                        List<OutLineStocktaking> list = stocktakingDao.find(no);
                        // 格式化盘点内容
                        for (int j = 0; j < list.size(); j++) {
                            Map<String, Object> tmap = new HashMap<String, Object>();
                            OutLineStocktaking lineStocktaking = list.get(j);
                            tmap.put("Barcode", lineStocktaking.getBarcode());
                            tmap.put("Quantity", lineStocktaking.getQuantity());
                            tempList.add(tmap);
                        }
                    }
                    // 导出Text文件
                    sendDatasMulitToServer(departmentCode, quantitySum, tempList);
                    mulitList.clear();
                }
            });
            // 相当于点击取消按钮
            dialog.setNegativeButton("不合并", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    boolean showDialog = false;
                    for (int i = 0; i < mulitList.size(); i++) {
                        Map<String, Object> map = mulitList.get(i);
                        String no = String.valueOf(map.get("No"));
                        String departmentCode = String.valueOf(map.get("DepartmentCode"));
                        String shelvesNo = String.valueOf(map.get("ShelvesNo"));
                        // 判断数据库中是否有相应单号的数据
                        List<OutLineStocktaking> list = stocktakingDao.find(no);
                        // 格式化盘点内容
                        int quantitySum = 0;
                        List<Map<String, Object>> tempList = new ArrayList<Map<String, Object>>();
                        for (int j = 0; j < list.size(); j++) {
                            Map<String, Object> tmap = new HashMap<String, Object>();
                            OutLineStocktaking lineStocktaking = list.get(j);
                            tmap.put("Barcode", lineStocktaking.getBarcode());
                            tmap.put("Quantity", lineStocktaking.getQuantity());
                            quantitySum += lineStocktaking.getQuantity();
                            tempList.add(tmap);
                        }
                        if (i == mulitList.size() - 1) {
                            showDialog = true;
                        }
                        // 导出Text文件
                        sendDatasToServer(no, departmentCode, shelvesNo, String.valueOf(quantitySum), tempList, showDialog);
                    }
                    mulitList.clear();
                }
            });
            dialog.create();
            dialog.show();
        } else {
            boolean showDialog = false;
            for (int i = 0; i < mulitList.size(); i++) {
                Map<String, Object> map = mulitList.get(i);
                String no = String.valueOf(map.get("No"));
                String departmentCode = String.valueOf(map.get("DepartmentCode"));
                String shelvesNo = String.valueOf(map.get("ShelvesNo"));
                // 判断数据库中是否有相应单号的数据
                List<OutLineStocktaking> list = stocktakingDao.find(no);
                // 格式化盘点内容
                int quantitySum = 0;
                List<Map<String, Object>> tempList = new ArrayList<Map<String, Object>>();
                for (int j = 0; j < list.size(); j++) {
                    Map<String, Object> tmap = new HashMap<String, Object>();
                    OutLineStocktaking lineStocktaking = list.get(j);
                    tmap.put("Barcode", lineStocktaking.getBarcode());
                    tmap.put("Quantity", lineStocktaking.getQuantity());
                    quantitySum += lineStocktaking.getQuantity();
                    tempList.add(tmap);
                }
                if (i == mulitList.size() - 1) {
                    showDialog = true;
                }
                // 导出Text文件
                sendDatasToServer(no, departmentCode, shelvesNo, String.valueOf(quantitySum), tempList, showDialog);
            }
            mulitList.clear();
        }
    }

    /**
     * 发送盘点数据到服务端生成Text(不合并导出)
     * 
     * @param no
     * @param listTitle
     * @param objs
     */
    private void sendDatasToServer(final String no, String departmentCode, String shelvesNo, String quantitySum, List<Map<String, Object>> tempList, final boolean showDialog) {
        RequestVo vo = new RequestVo();
        vo.requestUrl = sendDatasToGenerateText;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("quantitySum", quantitySum);
        map.put("departmentCode", departmentCode);
        map.put("shelvesNo", shelvesNo);
        net.sf.json.JSONArray jsons = net.sf.json.JSONArray.fromObject(tempList);
        map.put("dataList", jsons.toString());
        vo.requestDataMap = map;
        super.getDataFromServer(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    if (retObj.getBoolean("success")) {
                        String exportPath = retObj.getString("obj");
                        if (showDialog) {
                            exportPath = exportPath.substring(0, exportPath.lastIndexOf("\\"));
                            Builder dialog = new AlertDialog.Builder(InventorySheetOutLineActivity.this, AlertDialog.THEME_HOLO_LIGHT);
                            dialog.setTitle("提示");
                            dialog.setMessage("离线盘点文件已导出到服务器目录 " + exportPath);
                            // 相当于点击确认按钮
                            dialog.setPositiveButton("我知道了", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    initListView();
                                }
                            });
                            dialog.setCancelable(false);
                            dialog.create();
                            dialog.show();
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(InventorySheetOutLineActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 发送盘点数据到服务端生成Text(合并导出)
     * 
     * @param departmentCode
     * @param quantitySum
     * @param tempList
     */
    private void sendDatasMulitToServer(String departmentCode, String quantitySum, List<Map<String, Object>> tempList) {
        RequestVo vo = new RequestVo();
        vo.requestUrl = sendDatasMulitToGenerateText;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("quantitySum", quantitySum);
        map.put("departmentCode", departmentCode);
        net.sf.json.JSONArray jsons = net.sf.json.JSONArray.fromObject(tempList);
        map.put("dataList", jsons.toString());
        vo.requestDataMap = map;
        super.getDataFromServer(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    if (retObj.getBoolean("success")) {
                        String exportPath = retObj.getString("obj");
                        Builder dialog = new AlertDialog.Builder(InventorySheetOutLineActivity.this, AlertDialog.THEME_HOLO_LIGHT);
                        dialog.setTitle("提示");
                        dialog.setMessage("离线盘点文件已导出到服务器 " + exportPath);
                        // 相当于点击确认按钮
                        dialog.setPositiveButton("我知道了", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                initListView();
                            }
                        });
                        dialog.setCancelable(false);
                        dialog.create();
                        dialog.show();
                    }
                } catch (Exception e) {
                    Toast.makeText(InventorySheetOutLineActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

}
