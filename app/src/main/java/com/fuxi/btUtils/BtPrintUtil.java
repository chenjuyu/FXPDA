package com.fuxi.btUtils;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.ArrayUtils;
import android.graphics.Bitmap;
import android.text.TextUtils;
import com.zj.btsdk.BluetoothService;

/**
 * 蓝牙打印工具类 Created by Administrator on 2016/5/20.
 */
public class BtPrintUtil {
    private List<Byte> contents;
    private BluetoothService btService;

    // 打印纸一行最大的字节
    private static final int LINE_BYTE_SIZE = 32;
    // 分隔符
    private static final String SEPARATOR = "$";

    private StringBuffer sb = new StringBuffer();

    public BtPrintUtil(BluetoothService btService) {
        this.contents = new ArrayList<>();
        this.btService = btService;
    }

    /**
     * 打印
     */
    public void print() {
        if (contents.size() > 0) {
            Byte[] Bytes = contents.toArray(new Byte[contents.size()]);
            byte[] bytes = ArrayUtils.toPrimitive(Bytes);
            btService.write(bytes);
            contents.clear();
            cutter();
        }
    }

    /**
     * 添加文本
     * 
     * @param text
     */
    public void addText(String text) {
        try {
            for (byte b : text.getBytes("GBK")) {
                contents.add((byte) b);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 添加标题（居中显示）
     * 
     * @param title 标题(大：最多8个字，小：最多16个字)
     * @param isBiger 是否加粗和倍高倍宽
     */
    public void addTitle(String title, boolean isBiger) {
        setAlign(1);
        if (isBiger) {// 设置加粗和倍高倍宽
            setFonts(true, true, true, false);
        }

        sb.delete(0, sb.length());
        sb.append(title + "\n");
        addText(sb.toString());

        if (isBiger) {
            setFonts(false, false, false, false);
        }

        setAlign(0);
    }

    /**
     * 添加一行文本
     * 
     * @param text
     */
    public void addLineText(String text) {
        sb.delete(0, sb.length());
        sb.append(text + "\n");
        addText(sb.toString());
    }

    /**
     * 添加分割线（样式---）
     */
    public void addSplitLine() {
        sb.delete(0, sb.length());
        for (int i = 0; i < LINE_BYTE_SIZE; i++) {
            sb.append("-");
        }
        sb.append("\n");

        addText(sb.toString());
    }

    /**
     * 添加分割线（自定义样式）
     */
    public void addSplitLine(String separator) {
        sb.delete(0, sb.length());
        for (int i = 0; i < LINE_BYTE_SIZE; i++) {
            sb.append(separator);
        }
        sb.append("\n");

        addText(sb.toString());
    }

    /**
     * 添加换行
     * 
     * @param num
     */
    public void addSeparate(int num) {
        sb.delete(0, sb.length());
        for (int i = 0; i < num; i++) {
            sb.append("\n");
        }
        addText(sb.toString());
    }

    /**
     * 添加图片
     * 
     * @param bitmap
     */
    public void addImage(Bitmap bitmap) {
        setAlign(1);

        int width = (bitmap.getWidth() + 7) / 8 * 8;
        int height = bitmap.getHeight() * width / bitmap.getWidth();
        Bitmap grayBitmap = GpUtils.toGrayscale(bitmap);
        Bitmap rszBitmap = GpUtils.resizeImage(grayBitmap, width, height);
        byte[] src = GpUtils.bitmapToBWPix(rszBitmap);
        byte[] command = new byte[8];
        height = src.length / width;

        contents.add((byte) 29);
        contents.add((byte) 118);
        contents.add((byte) 48);
        contents.add((byte) (0 & 0x1));
        contents.add((byte) (width / 8 % 256));
        contents.add((byte) (width / 8 / 256));
        contents.add((byte) (height % 256));
        contents.add((byte) (height / 256));

        GpUtils.addBitImage(src, contents);

        setAlign(0);
    }

    /**
     * 添加列表数据
     * 
     * @param tableData
     */
    public void addTableData(PrintTableData tableData) {
        List<PrintTableData.HeaderLable> headers = tableData.getHeaders();
        List<Map<String, String>> dataList = tableData.getDataList();
        int totalWidth = 0;
        for (PrintTableData.HeaderLable lable : headers) {
            totalWidth += lable.getWidth();
        }

        double weight = 1;// 权重计算
        if (totalWidth < (LINE_BYTE_SIZE - 2)) {
            weight = (double) (LINE_BYTE_SIZE - 2) / (double) totalWidth;
        }

        for (PrintTableData.HeaderLable lable : headers) {// 打印表头
            try {
                byte[] bytes = (lable.getName() + " ").getBytes("GBK");
                for (byte b : bytes) {
                    contents.add((byte) b);
                }
                for (int i = 0; i < (lable.getWidth() * weight - bytes.length); i++) {
                    addText(" ");
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        addSeparate(1);
        addSplitLine();

        for (Map<String, String> data : dataList) {// 打印数据

            for (PrintTableData.HeaderLable lable : headers) {
                try {
                    byte[] bytes = (data.get(lable.getKey()) + " ").getBytes("GBK");
                    for (byte b : bytes) {
                        contents.add((byte) b);
                    }
                    for (int i = 0; i < (lable.getWidth() * weight - bytes.length); i++) {
                        addText(" ");
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            addSeparate(1);
            if (!TextUtils.isEmpty(tableData.getNewLineKey())) {
                addLineText(data.get(tableData.getNewLineKey()));
            }
        }

        addSplitLine();

    }

    /**
     * 切纸
     */
    public void cutter() {
        btService.write(new byte[] {29, 86, 1});
    }

    /**
     * 设置对齐方式
     * 
     * @param align 0左对齐，1中间对齐，2右对齐
     */
    public void setAlign(int align) {
        contents.add((byte) 27);
        contents.add((byte) 97);
        contents.add((byte) align);
    }

    /**
     * 设置打印模式
     * 
     * @param bold 是否加粗
     * @param doubleHeight 是否倍高
     * @param doubleWidth 是否倍宽
     * @param underline 是否有下划线
     */
    public void setFonts(boolean bold, boolean doubleHeight, boolean doubleWidth, boolean underline) {
        contents.add((byte) 27);
        contents.add((byte) 33);
        contents.add((byte) ((bold ? 8 : 0) | (doubleHeight ? 16 : 0) | (doubleWidth ? 32 : 0) | (underline ? 128 : 0)));
    }

    /**
     * 获取最大长度
     */
    private int getMaxLength(Object[] msgs) {
        int max = 0;
        int tmp;
        for (Object oo : msgs) {
            tmp = getBytesLength(oo.toString());
            if (tmp > max) {
                max = tmp;
            }
        }
        return max;
    }

    /**
     * 获取数据长度
     */
    private int getBytesLength(String msg) {
        return msg.getBytes(Charset.forName("GBK")).length;
    }

}
