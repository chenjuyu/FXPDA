package com.fuxi.btUtils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.widget.Toast;
import com.zj.btsdk.BluetoothService;

public class BluePrint {
    private String BluetoothName = "BLU58";

    public Activity thirdActivity;

    private static BluePrint instance = null;

    public static BluePrint getInstance() {
        if (instance == null) {
            instance = new BluePrint();

        }
        return instance;
    }

    public void initBlueTooth() {

        // 注册广播监听搜索设备
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        thirdActivity.registerReceiver(discoverReceiver, filter);

        // 注册广播监听搜索结束
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        thirdActivity.registerReceiver(discoverReceiver, filter);

        processLogic();

    }

    public Handler handler = new Handler();
    BluetoothService btService;
    private List<BluetoothDevice> bluetoothDevices;

    protected void processLogic() {
        if (btService != null) {
            btService.stop();
        }

        btService = new BluetoothService(thirdActivity, handler);
        bluetoothDevices = new ArrayList<>();

        if (btService.isAvailable() == false) {// 蓝牙不可用
            Toast.makeText(thirdActivity, "蓝牙不可用", Toast.LENGTH_LONG).show();
            thirdActivity.finish();
        }
        if (btService.isBTopen() == false) {// 蓝牙未打开，打开蓝牙
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            thirdActivity.startActivityForResult(enableIntent, 101);
        }
        Set<BluetoothDevice> pairedDevices = btService.getPairedDev();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                bluetoothDevices.add(device);
                if (device.getName().equals(BluetoothName)) {
                    btService.connect(device);
                    return;
                }
            }
        }
    }

    private final BroadcastReceiver discoverReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {// 发现一个设备
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (!bluetoothDevices.contains(device)) {
                    bluetoothDevices.add(device);

                }
                if (bluetoothDevices.size() > 0) {
                    for (BluetoothDevice bluetoothDevice : bluetoothDevices) {
                        if (null != bluetoothDevice) {
                            if (bluetoothDevice.getName().equals(BluetoothName)) {
                                btService.connect(bluetoothDevice);
                                return;
                            }
                        }
                    }
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {// 搜索结束

            }
        }
    };

    public void print() {
        BtPrintUtil printUtil = new BtPrintUtil(btService);
        printUtil.addSeparate(1);
        printUtil.addTitle("测试药店", true);
        printUtil.addSeparate(1);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss ");
        Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
        String str = formatter.format(curDate);
        printUtil.addLineText("销售时间:" + str);
        printUtil.addLineText("操作人员：益丰药房");
        printUtil.addLineText("单号：1561462314566116546146");
        printUtil.addSplitLine();
        List<SaleBean> list = new ArrayList<>();

        for (int i = 0; i < 2; i++) {
            SaleBean saleBean = new SaleBean();
            saleBean.setBatch_num(i + "9");
            saleBean.setName(i + "哈哈");
            saleBean.setSpecifications(i + "*9袋/盒");
            saleBean.setPrice(i + "5.00");
            list.add(saleBean);
        }
        double totalPrice = 0;
        DecimalFormat df = new DecimalFormat("##0.00");

        for (int i = 0; i < list.size(); i++) {
            printUtil.addLineText("序号: " + i);
            printUtil.addLineText("通用名称: " + list.get(i).getName());
            printUtil.addLineText("规格: " + list.get(i).getSpecifications());
            printUtil.addLineText("批号: " + list.get(i).getBatch_num());
            printUtil.addLineText("单价: " + list.get(i).getPrice());
            totalPrice += Double.parseDouble(list.get(i).getPrice());
            printUtil.addSplitLine();
        }

        printUtil.addLineText("共计: " + list.size() + "件" + "     合计:￥" + df.format(totalPrice));
        printUtil.addSeparate(2);
        printUtil.print();
    }

    public void unregistActivity() {
        try {
            if (null != discoverReceiver) {
                thirdActivity.unregisterReceiver(discoverReceiver);
            }
        } catch (Exception e) {

        }
    }

}
