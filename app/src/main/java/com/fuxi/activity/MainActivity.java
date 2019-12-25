package com.fuxi.activity;

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
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;
import com.fuxi.dao.DepartmentDao;
import com.fuxi.main.R;
import com.fuxi.util.AppManager;
import com.fuxi.util.Logger;
import com.fuxi.util.LoginParameterUtil;
import com.fuxi.util.NetUtil;
import com.fuxi.util.Tools;
import com.fuxi.util.gridview.MyGridAdapter;
import com.fuxi.util.gridview.MyGridView;
import com.fuxi.vo.Department;
import com.fuxi.vo.RequestVo;

/**
 * Title: MainActivity Description: 主界面
 * 
 * @author LYJ
 * 
 */
public class MainActivity extends BaseWapperActivity implements OnItemClickListener {

    private static String TAG = "MainActivity";
    private static String logout = "/login.do?logout";
    private static String getWarehouse = "/common.do?getWarehouse";
    private MyGridView gridview;
    private Intent intent = null;
    private DepartmentDao departmentDao = new DepartmentDao(this);
    // 其它属性
    private long exitTime = 0; // 记录按回退键的时间间隔
    private String[] img_text = null; // 显示的文字
    private String[] img_activity = null; // 图标对应的activity
    private Integer[] imgs = null; // 图标

    /**
     * 初始化数据
     */
    private void getData() {
        if (Boolean.valueOf(this.getString(R.string.use_position))) { //&& "1".equals(LoginParameterUtil.customer.getUserId())
            if (LoginParameterUtil.customer != null ) {
                if (LoginParameterUtil.usingOrderGoodsModule) {
                    img_activity =
                            new String[] {"com.fuxi.activity.BaseInformationActivity", "com.fuxi.activity.SalesManagementActivity", "com.fuxi.activity.PurchaseManagementActivity", "com.fuxi.activity.WarehouseManagementActivity", "com.fuxi.activity.PositionManagementActivity",
                                    "com.fuxi.activity.PosSalesManagementActivity", "com.fuxi.activity.PackingBoxActivity", "com.fuxi.activity.BarcodePrintActivity", "com.fuxi.activity.OPMLoginActivity", "com.fuxi.activity.SettingActivity"};
                    img_text = new String[] {"基本资料", "分销管理", "采购管理", "库存管理", "仓储管理", "零售管理", "装箱扫描", "条码打印", "订货中心", "设置中心"};
                    imgs = new Integer[] {R.string.base_info, R.string.sales_manage, R.string.purchase_manage, R.string.warehouse_manage, R.string.position_manage, R.string.posSales_manage, R.string.packing_box, R.string.barcode_print, R.string.order_placing_meeting, R.string.system_setting};
                } else {
                    img_activity =
                            new String[] {"com.fuxi.activity.BaseInformationActivity", "com.fuxi.activity.SalesManagementActivity", "com.fuxi.activity.PurchaseManagementActivity", "com.fuxi.activity.WarehouseManagementActivity", "com.fuxi.activity.PositionManagementActivity",
                                    "com.fuxi.activity.PosSalesManagementActivity", "com.fuxi.activity.PackingBoxActivity", "com.fuxi.activity.BarcodePrintActivity", "com.fuxi.activity.SettingActivity"};
                    img_text = new String[] {"基本资料", "分销管理", "采购管理", "库存管理", "仓储管理", "零售管理", "装箱扫描", "条码打印", "设置中心"};
                    imgs = new Integer[] {R.string.base_info, R.string.sales_manage, R.string.purchase_manage, R.string.warehouse_manage, R.string.position_manage, R.string.posSales_manage, R.string.packing_box, R.string.barcode_print, R.string.system_setting};
                }
            } else {
                if (LoginParameterUtil.usingOrderGoodsModule) {
                    img_activity =
                            new String[] {"com.fuxi.activity.BaseInformationActivity", "com.fuxi.activity.SalesManagementActivity", "com.fuxi.activity.PurchaseManagementActivity", "com.fuxi.activity.WarehouseManagementActivity", "com.fuxi.activity.PositionManagementActivity",
                                    "com.fuxi.activity.PosSalesManagementActivity", "com.fuxi.activity.PackingBoxActivity", "com.fuxi.activity.BarcodePrintActivity", "com.fuxi.activity.OPMLoginActivity"};
                    img_text = new String[] {"基本资料", "分销管理", "采购管理", "库存管理", "仓储管理", "零售管理", "装箱扫描", "条码打印", "订货中心"};
                    imgs = new Integer[] {R.string.base_info, R.string.sales_manage, R.string.purchase_manage, R.string.warehouse_manage, R.string.position_manage, R.string.posSales_manage, R.string.packing_box, R.string.barcode_print, R.string.order_placing_meeting};
                } else {
                    img_activity =
                            new String[] {"com.fuxi.activity.BaseInformationActivity", "com.fuxi.activity.SalesManagementActivity", "com.fuxi.activity.PurchaseManagementActivity", "com.fuxi.activity.WarehouseManagementActivity", "com.fuxi.activity.PositionManagementActivity",
                                    "com.fuxi.activity.PosSalesManagementActivity", "com.fuxi.activity.PackingBoxActivity", "com.fuxi.activity.BarcodePrintActivity"};
                    img_text = new String[] {"基本资料", "分销管理", "采购管理", "库存管理", "仓储管理", "零售管理", "装箱扫描", "条码打印"};
                    imgs = new Integer[] {R.string.base_info, R.string.sales_manage, R.string.purchase_manage, R.string.warehouse_manage, R.string.position_manage, R.string.posSales_manage, R.string.packing_box, R.string.barcode_print};
                }
            }
        } else { // && "1".equals(LoginParameterUtil.customer.getUserId())
            if (LoginParameterUtil.customer != null ) {
                if (LoginParameterUtil.usingOrderGoodsModule) {
                    img_activity =
                            new String[] {"com.fuxi.activity.BaseInformationActivity", "com.fuxi.activity.SalesManagementActivity", "com.fuxi.activity.PurchaseManagementActivity", "com.fuxi.activity.WarehouseManagementActivity", "com.fuxi.activity.PosSalesManagementActivity",
                                    "com.fuxi.activity.PackingBoxActivity", "com.fuxi.activity.BarcodePrintActivity", "com.fuxi.activity.OPMLoginActivity", "com.fuxi.activity.SettingActivity"};
                    img_text = new String[] {"基本资料", "分销管理", "采购管理", "库存管理", "零售管理", "装箱扫描", "条码打印", "订货中心", "设置中心"};
                    imgs = new Integer[] {R.string.base_info, R.string.sales_manage, R.string.purchase_manage, R.string.warehouse_manage, R.string.posSales_manage, R.string.packing_box, R.string.barcode_print, R.string.order_placing_meeting, R.string.system_setting,};

                } else {
                    img_activity =
                            new String[] {"com.fuxi.activity.BaseInformationActivity", "com.fuxi.activity.SalesManagementActivity", "com.fuxi.activity.PurchaseManagementActivity", "com.fuxi.activity.WarehouseManagementActivity", "com.fuxi.activity.PosSalesManagementActivity",
                                    "com.fuxi.activity.PackingBoxActivity", "com.fuxi.activity.BarcodePrintActivity", "com.fuxi.activity.SettingActivity"};
                    img_text = new String[] {"基本资料", "分销管理", "采购管理", "库存管理", "零售管理", "装箱扫描", "条码打印", "设置中心"};
                    imgs = new Integer[] {R.string.base_info, R.string.sales_manage, R.string.purchase_manage, R.string.warehouse_manage, R.string.posSales_manage, R.string.packing_box, R.string.barcode_print, R.string.system_setting,};

                }
            } else {
                if (LoginParameterUtil.usingOrderGoodsModule) {
                    img_activity =
                            new String[] {"com.fuxi.activity.BaseInformationActivity", "com.fuxi.activity.SalesManagementActivity", "com.fuxi.activity.PurchaseManagementActivity", "com.fuxi.activity.WarehouseManagementActivity", "com.fuxi.activity.PosSalesManagementActivity",
                                    "com.fuxi.activity.PackingBoxActivity", "com.fuxi.activity.BarcodePrintActivity", "com.fuxi.activity.OPMLoginActivity"};
                    img_text = new String[] {"基本资料", "分销管理", "采购管理", "库存管理", "零售管理", "装箱扫描", "条码打印", "订货中心"};
                    imgs = new Integer[] {R.string.base_info, R.string.sales_manage, R.string.purchase_manage, R.string.warehouse_manage, R.string.posSales_manage, R.string.packing_box, R.string.barcode_print, R.string.order_placing_meeting};

                } else {
                    img_activity =
                            new String[] {"com.fuxi.activity.BaseInformationActivity", "com.fuxi.activity.SalesManagementActivity", "com.fuxi.activity.PurchaseManagementActivity", "com.fuxi.activity.WarehouseManagementActivity", "com.fuxi.activity.PosSalesManagementActivity",
                                    "com.fuxi.activity.PackingBoxActivity", "com.fuxi.activity.BarcodePrintActivity"};
                    img_text = new String[] {"基本资料", "分销管理", "采购管理", "库存管理", "零售管理", "装箱扫描", "条码打印"};
                    imgs = new Integer[] {R.string.base_info, R.string.sales_manage, R.string.purchase_manage, R.string.warehouse_manage, R.string.posSales_manage, R.string.packing_box, R.string.barcode_print};

                }
            }
        }
    }

    /**
     * 连续按两次回退键退出程序
     */
    private void exit() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            logOut();
        }
    }

    /**
     * 退出系统并注销登录信息
     */
    public void logOut() {
        boolean flag = NetUtil.hasNetwork(getApplicationContext());
        if (flag) {
            // 连接服务器
            RequestVo vo = new RequestVo();
            vo.requestUrl = logout;
            vo.context = this;
            HashMap map = new HashMap();
            map.put("onLineId", LoginParameterUtil.onLineId);
            vo.requestDataMap = map;
            super.getDataFromServer(vo, new DataCallback<JSONObject>() {
                @Override
                public void processData(JSONObject paramObject, boolean paramBoolean) {
                    AppManager.getAppManager().AppExit(getApplicationContext());
                }
            });
        } else {
            AppManager.getAppManager().AppExit(getApplicationContext());
        }
    }

    /**
     * 根据登录权限获取部门信息(用于离线盘点时使用)
     */
    private void getWarehouseList() {
        RequestVo vo = new RequestVo();
        vo.requestUrl = getWarehouse;
        vo.context = this;
        HashMap map = new HashMap();
        vo.requestDataMap = map;
        super.getDataFromServer(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    if (retObj.getBoolean("success")) {
                        JSONArray array = retObj.getJSONArray("obj");
                        List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
                        for (int i = 0; i < array.length(); i++) {
                            Map temp = new HashMap();
                            JSONObject json = array.getJSONObject(i);
                            Iterator ite = json.keys();
                            while (ite.hasNext()) {
                                String key = ite.next().toString();
                                String value = json.getString(key);
                                temp.put(key, value);
                            }
                            dataList.add(temp);
                        }
                        // 存入缓存
                        saveDepartment(dataList);
                    }
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 保存部门
     * 
     * @param docName
     * @param objName
     * @param saveObj
     */
    public void saveDepartment(List<Map<String, Object>> dataList) {
        try {
            // 检查部门表里面是否有旧数据
            Department findFirst = departmentDao.findFirst();
            if (findFirst != null) {
                departmentDao.deleteAll();
            }
            // 插入新的部门记录
            String madeDate = Tools.dateToString(new Date());
            for (int i = 0; i < dataList.size(); i++) {
                Map<String, Object> map = dataList.get(i);
                String departmentId = String.valueOf(map.get("DepartmentID"));
                String department = String.valueOf(map.get("Name"));
                int mustExistsGoodsFlag = 0;
                String mustExistsGoodsFlagStr = String.valueOf(map.get("MustExistsGoodsFlag"));
                if ("true".equals(mustExistsGoodsFlagStr)) {
                    mustExistsGoodsFlag = 1;
                }
                String departmentCode = String.valueOf(map.get("DepartmentCode"));
                Department dept = new Department(departmentId, department, mustExistsGoodsFlag, departmentCode, madeDate);
                departmentDao.insert(dept);
            }
            Log.i("ok", "部门信息存储成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {}

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    protected void loadViewLayout() {
        setContentView(R.layout.activity_main);
        setTitle("主 菜 单");
        setHeadLeftVisibility(View.INVISIBLE);
    }

    @Override
    protected void setListener() {
        gridview.setOnItemClickListener(this);
    }

    @Override
    protected void processLogic() {
        // 显示按钮
        setHeadRightVisibility(View.VISIBLE);
        setHeadRightText(R.string.exit);
        if (NetUtil.hasNetwork(getApplicationContext())) {
            // 获取仓库信息
            getWarehouseList();
        }
    }

    @Override
    protected void onHeadRightButton(View v) {
        Builder dialog = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
        dialog.setTitle("系统提示");
        dialog.setMessage("确定要退出系统吗？");
        // 相当于点击确认按钮
        dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 退出
                logOut();
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
    protected void findViewById() {
        gridview = (MyGridView) findViewById(R.id.gv_gridview);
        // 获取数据
        getData();
        // 设置适配器
        gridview.setAdapter(new MyGridAdapter(img_text, img_activity, imgs, this));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try {
            intent = new Intent(MainActivity.this, Class.forName(img_activity[position]));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        startActivity(intent);
    }
}
