package com.fuxi.receiver;

import java.io.File;
import com.fuxi.util.Tools;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;

/**
 * Title: InitApkBroadCastReceiver Description: 应用程序发生变化时的监听广播(用于程序替换,更新)
 * 
 * @author LYJ
 * 
 */
public class InitApkBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())) {
            System.out.println("监听到系统广播添加");
        }

        if (Intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction())) {
            System.out.println("监听到系统广播移除");
        }

        if (Intent.ACTION_PACKAGE_REPLACED.equals(intent.getAction())) {
            System.out.println("监听到系统广播替换");
            // 获取更目录路径
            File file = Environment.getExternalStorageDirectory();
            Tools.deleteFile(file);
        }

    }
}
