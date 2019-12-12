package com.fuxi.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.util.Log;
import com.fuxi.application.ECApplication;
import com.google.zxing.BarcodeFormat;

public class Tools extends Activity {

    private final static String imgPath = "image";
    public static int year, month, day;

    static {
        final Calendar ca = Calendar.getInstance();
        year = ca.get(Calendar.YEAR);
        month = ca.get(Calendar.MONTH);
        day = ca.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * 获取图片存储位置
     * 
     * @return
     */
    public static File getImagePath(Context context) {
        File dir = context.getDir(imgPath, Context.MODE_PRIVATE);
        return dir;
    }

    public static File getImage(Context context, String goodsCode) {
        // File dir = getImagePath(context);
        // String path = dir.getAbsolutePath();
        File path = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "fxpda" + File.separator + "images");
        if (!path.exists()) {
            path.mkdirs();
        }
        String imagePath = path + File.separator + goodsCode + ".jpg";
        File imageFile = new File(imagePath);
        return imageFile;
    }

    // public static String formatDecimal(String data){
    // DecimalFormat df = new DecimalFormat("###,###0.00");
    // double d = 0.0;
    // d = Double.parseDouble(data);
    // String formatData = df.format(d);
    // return formatData;
    // }

    /**
     * 金额格式化
     * 
     * @param s 金额
     * @param len 小数位数
     * @return 格式后的金额
     */
    public static String formatDecimal(String s) {
        if (s == null || s.length() < 1 || "null".equals(s)) {
            s = "0";
        }
        NumberFormat formater = null;
        double num = Double.parseDouble(s);
        StringBuffer buff = new StringBuffer();
        buff.append("#,###,##0.00");
        for (int i = 0; i < 2; i++) {
            buff.append("#");
        }
        formater = new DecimalFormat(buff.toString());

        return formater.format(num);
    }

    /**
     * MD5加密
     * 
     * @param data
     * @return
     * @throws Exception
     */
    public static byte[] encryptMD5(byte[] data) throws Exception {

        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(data);

        return md5.digest();

    }

    /**
     * 装换为字符串
     * 
     * @param data
     * @return
     */
    public static String getByteToString(byte[] data) {
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        int j = data.length;
        char str[] = new char[j * 2];
        int k = 0;
        for (int i = 0; i < j; i++) {
            byte byte0 = data[i];
            str[k++] = hexDigits[byte0 >>> 4 & 0xf];
            str[k++] = hexDigits[byte0 & 0xf];
        }
        String outPut = new String(str);
        return outPut;
    }

    /**
     * 删除指定的文件
     * 
     * @param file
     */
    public static void deleteFile(File file) {
        try {
            if (file.exists()) { // 判断文件是否存在
                if (file.isFile()) {// 判断是否是文件
                    String fileName = file.getName();
                    String prefix = fileName.substring(fileName.lastIndexOf(".") + 1);
                    if (prefix.equalsIgnoreCase("apk") && fileName.contains("PDA_1")) {
                        // 保留最新版本的APK文件
                        String apkName = "fuxiPDA_" + getVersionName(ECApplication.getInstance()) + ".apk";
                        if (!apkName.equals(fileName)) {
                            file.delete();
                        }
                    }
                } else if (file.isDirectory()) { // 否则如果它是一个目录
                    File files[] = file.listFiles(); // 声明目录下所有的文件 files[];
                    if (files == null || files.length == 0) {
                        file.delete();
                    } else {
                        for (int i = 0; i < files.length; i++) { // 遍历目录下所有的文件
                            deleteFile(files[i]); // 把每个文件 用这个方法进行迭代
                        }
                    }
                }
            }
        } catch (Exception e) {
            Logger.e("Tools", e);
        }
    }

    /**
     * 获取当前应用程序的版本号
     * 
     * @param context
     * @return
     * @throws Exception
     */
    public static String getVersionName(Context context) {
        String version = "";
        try {
            // 获取PackageManager的实例
            PackageManager packageManager = context.getPackageManager();
            // getPackageName()是你当前类的包名
            PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            version = packInfo.versionName;
        } catch (NameNotFoundException e) {
            Log.i("获取应用程序版本号失败", e.toString());
        }
        return version;
    }

    /**
     * 判断是否是英文字符
     * 
     * @param c
     * @return
     */
    public static boolean isLetter(char c) {
        int k = 0x80;
        return c / k == 0 ? true : false;
    }

    /**
     * 得到一个字符串的长度,显示的长度,一个汉字或日韩文长度为2,英文字符长度为1
     * 
     * @param String s 需要得到长度的字符串
     * @return int 得到的字符串长度
     */
    public static int length(String s) {
        if (s == null)
            return 0;
        char[] c = s.toCharArray();
        int len = 0;
        for (int i = 0; i < c.length; i++) {
            len++;
            if (!isLetter(c[i])) {
                len++;
            }
        }
        return len;
    }

    /**
     * 截取指定长度的字符串
     * 
     * @param s
     * @param length
     * @return
     */
    public static String fixedLengthStr(String s, int length) {
        if (s == null)
            return "";
        char[] c = s.toCharArray();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            sb.append(c[i]);
            if (!isLetter(c[i])) {
                length--;
            }
        }
        return sb.toString();
    }

    /**
     * 格式化日期
     * 
     * @param time
     * @return
     */
    public static String dateToString(Date time) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String ctime = formatter.format(time);
        return ctime;
    }

    /**
     * 格式化日期
     * 
     * @param time
     * @return
     */
    public static String dateTimeToString(Date time) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String ctime = formatter.format(time);
        return ctime;
    }

    /**
     * 格式化日期
     * 
     * @param time
     * @return
     * @throws ParseException
     */
    public static Date dateStringToTime(String time) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date ctime = formatter.parse(time);
        return ctime;
    }

    /**
     * 格式化日期
     * 
     * @param time
     * @return
     * @throws ParseException
     */
    public static String dateStringToDate(String time) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date ctime = formatter.parse(time);
        String ntime = formatter.format(ctime);
        return ntime;
    }

    /**
     * 计算两个日期之间相差的天数
     * 
     * @param date1
     * @param date2
     * @return
     */
    public static int daysBetween(Date date1, Date date2) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date1);
        long time1 = cal.getTimeInMillis();
        cal.setTime(date2);
        long time2 = cal.getTimeInMillis();
        long between_days = (time2 - time1) / (1000 * 3600 * 24);
        return Integer.parseInt(String.valueOf(between_days));
    }

    /**
     * 生成下一个编号
     */
    public static synchronized String generaterNextNumber(String sno) {
        String nno = null;
        StringBuffer sb = new StringBuffer();
        if (sno == null || "".equals(sno) || "null".equalsIgnoreCase(sno)) {
            return nno;
        }
        String baseStr = sno.substring(0, sno.indexOf("-") + 1);
        String noStr = sno.substring(sno.indexOf("-") + 1, sno.length());
        int no = Integer.parseInt(noStr);
        no++;
        if (String.valueOf(no).length() < 2) {
            nno = sb.append(baseStr).append("00").append(no).toString();
        } else if (String.valueOf(no).length() < 3) {
            nno = sb.append(baseStr).append("0").append(no).toString();
        } else {
            nno = sb.append(baseStr).append(no).toString();
        }
        return nno;
    }

    /**
     * 创建文件
     * 
     * @param fileName
     * @return
     */
    public static boolean createFile(File fileName) throws Exception {
        boolean flag = false;
        try {
            if (!fileName.exists()) {
                fileName.createNewFile();
                flag = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 读TXT文件内容
     * 
     * @param fileName
     * @return
     */
    public static String readTxtFile(File fileName) throws Exception {
        String result = null;
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        try {
            fileReader = new FileReader(fileName);
            bufferedReader = new BufferedReader(fileReader);
            try {
                String read = null;
                while ((read = bufferedReader.readLine()) != null) {
                    result = result + read + "\r\n";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (fileReader != null) {
                fileReader.close();
            }
        }
        return result;
    }

    /**
     * 将内容写入文件
     * 
     * @param content
     * @param fileName
     * @return
     * @throws Exception
     */
    public static boolean writeTxtFile(String content, File fileName) throws Exception {
        RandomAccessFile mm = null;
        boolean flag = false;
        FileOutputStream o = null;
        try {
            o = new FileOutputStream(fileName);
            o.write(content.getBytes("UTF-8"));
            o.close();
            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mm != null) {
                mm.close();
            }
        }
        return flag;
    }

    /**
     * 保存内容写入文件
     * 
     * @param filePath
     * @param content
     */
    public static void contentToTxt(String filePath, String content) {
        try {
            File f = new File(filePath);
            if (f.exists()) {
                System.out.print("文件存在");
            } else {
                System.out.print("文件不存在");
                f.createNewFile();// 不存在则创建
            }
            OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(f));
            BufferedWriter writer = new BufferedWriter(write);
            writer.write(content);
            writer.flush();
            write.close();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    /**
     * 判断是否是数字
     * 
     * @param str
     * @return
     */
    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    /**
     * 计算字符串的长度
     * 
     * @param str
     * @return
     * @throws UnsupportedEncodingException
     */
    public static int countStringLength(String str) throws UnsupportedEncodingException {
        int len = 0;
        str = new String(str.getBytes("gb2312"), "iso-8859-1");
        len = str.length();
        return len;
    }

    /**
     * 根据传入数值生成对应的空字符
     * 
     * @param len
     * @return
     */
    public static String generalEmptyCharacter(int len) {
        String str = " ";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append(str);
        }
        return sb.toString();
    }

    /**
     * 获取条码类别
     * 
     * @param type
     * @return
     */
    public static BarcodeFormat getBarcodeType(String type) {
        BarcodeFormat barcodeFormat = null;
        if ("CODE_128".equals(type)) {
            barcodeFormat = BarcodeFormat.CODE_128;
        } else if ("CODE_39".equals(type)) {
            barcodeFormat = BarcodeFormat.CODE_39;
        } else if ("CODE_93".equals(type)) {
            barcodeFormat = BarcodeFormat.CODE_93;
        } else if ("EAN_8".equals(type)) {
            barcodeFormat = BarcodeFormat.EAN_8;
        } else if ("EAN_13".equals(type)) {
            barcodeFormat = BarcodeFormat.EAN_13;
        } else if ("RSS_14".equals(type)) {
            barcodeFormat = BarcodeFormat.RSS_14;
        } else if ("UPC_A".equals(type)) {
            barcodeFormat = BarcodeFormat.UPC_A;
        } else if ("UPC_E".equals(type)) {
            barcodeFormat = BarcodeFormat.UPC_E;
        } else if ("AZTEC".equals(type)) {
            barcodeFormat = BarcodeFormat.AZTEC;
        } else if ("CODABAR".equals(type)) {
            barcodeFormat = BarcodeFormat.CODABAR;
        } else if ("ITF".equals(type)) {
            barcodeFormat = BarcodeFormat.ITF;
        } else if ("DATA_MATRIX".equals(type)) {
            barcodeFormat = BarcodeFormat.DATA_MATRIX;
        } else if ("MAXICODE".equals(type)) {
            barcodeFormat = BarcodeFormat.MAXICODE;
        } else if ("PDF_417".equals(type)) {
            barcodeFormat = BarcodeFormat.PDF_417;
        } else if ("RSS_EXPANDED".equals(type)) {
            barcodeFormat = BarcodeFormat.RSS_EXPANDED;
        } else if ("UPC_EAN_EXTENSION".equals(type)) {
            barcodeFormat = BarcodeFormat.UPC_EAN_EXTENSION;
        } else if ("QR_CODE".equals(type)) {
            barcodeFormat = BarcodeFormat.QR_CODE;
        }
        return barcodeFormat;
    }

}
