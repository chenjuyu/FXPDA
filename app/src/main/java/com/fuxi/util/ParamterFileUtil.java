package com.fuxi.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import android.content.Context;
import android.os.Environment;

public class ParamterFileUtil {

    private static final File directory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "fxpda" + File.separator + "info");

    /**
     * 使用属性文件保存用户的信息
     * 
     * @param context 上下文
     * @param username 用户名
     * @param password 密码
     * @return
     */
    public static boolean saveIpInfo(String name, String path, Context context) {
        try {
            if (!directory.exists()) {
                directory.mkdirs();
            }
            File file = new File(directory.getAbsolutePath() + File.separator + "info.properties");
            if (!file.exists()) {
                file.createNewFile();
            }

            Properties pps = new Properties();
            InputStream in = new FileInputStream(file);
            // 从输入流中读取属性列表（键和元素对）
            pps.load(in);
            // 调用 HashTable 的方法 put。使用 getProperty 方法提供并行性。
            // 强制要求为属性的键和值使用字符串。返回值是 HashTable 调用 put 的结果。
            OutputStream out = new FileOutputStream(file);
            pps.setProperty(name, path);
            // 以适合使用 load 方法加载到 Properties 表中的格式，
            // 将此 Properties 表中的属性列表（键和元素对）写入输出流
            pps.store(out, "info.properties");
            in.close();
            out.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            CommonUtil.showInfoDialog(context, "用户配置信息保存失败");
            return false;
        }
    }

    /**
     * 返回属性文件对象
     * 
     * @param context 上下文
     * @return
     */
    public static Properties getIpInfo(Context context) {
        try {
            if (!directory.exists()) {
                directory.mkdirs();
            }
            File file = new File(directory.getAbsolutePath() + File.separator + "info.properties");
            if (!file.exists()) {
                file.createNewFile();
                saveIpInfo("", "", context);
            }
            // 创建FileIutputStream 对象
            FileInputStream fis = new FileInputStream(file);
            // 创建属性对象
            Properties pro = new Properties();
            // 加载文件
            pro.load(fis);
            // 关闭输入流对象
            fis.close();
            return pro;
        } catch (Exception e) {
            e.printStackTrace();
            CommonUtil.showInfoDialog(context, "用户配置信息读取失败");
        }
        return null;
    }
}
