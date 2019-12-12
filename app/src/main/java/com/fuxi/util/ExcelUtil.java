package com.fuxi.util;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.format.UnderlineStyle;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

/**
 * 使用JXL实现Excel导入导出通用类
 * 
 */
public class ExcelUtil {
    // 导出到EXCEL(导出路径，标题数组，内容数组（全String类型）)
    public static void exportExcel(String filePath, String title, List<String> listTitle, List<Object[]> listContent) throws Exception {
        // 构建Workbook对象, 只读Workbook对象
        // Method 1：创建可写入的Excel工作薄
        File tempFile = new File(filePath);
        WritableWorkbook workbook = Workbook.createWorkbook(tempFile);
        // 创建Excel工作表 (名称,位置) ,该方法需要两个参数，一个是工作表的名称，另一个是工作表在工作薄中的位置
        WritableSheet sheet = workbook.createSheet("条码校验差异", 0);
        // 1.添加带有字型Formatting的对象
        WritableFont headerTitlt1 = new WritableFont(WritableFont.ARIAL, 16, WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE, jxl.format.Colour.BLACK); // 字体
        WritableFont headerTitlt2 = new WritableFont(WritableFont.ARIAL, 14, WritableFont.NO_BOLD, false, UnderlineStyle.NO_UNDERLINE, jxl.format.Colour.BLACK); // 字体
        WritableFont headerTitltFont = new WritableFont(WritableFont.ARIAL, 11, WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE, jxl.format.Colour.BLACK); // 字体
        WritableFont headerFont = new WritableFont(WritableFont.ARIAL, 10, WritableFont.NO_BOLD, false, UnderlineStyle.NO_UNDERLINE, jxl.format.Colour.BLACK); // 字体
        // 2.Format
        WritableCellFormat titltFormat1 = new WritableCellFormat(headerTitlt1); // Format
        WritableCellFormat titltFormat2 = new WritableCellFormat(headerTitlt2); // Format
        WritableCellFormat headerTitleFormat = new WritableCellFormat(headerTitltFont); // Format
        WritableCellFormat headerFormatLeft = new WritableCellFormat(headerFont); // Format
        WritableCellFormat headerFormatRight = new WritableCellFormat(headerFont); // Format
        titltFormat1.setAlignment(jxl.format.Alignment.CENTRE);// 单元格中的内容水平方向居中
        titltFormat2.setAlignment(jxl.format.Alignment.CENTRE);// 单元格中的内容水平方向居中
        headerTitleFormat.setAlignment(jxl.format.Alignment.CENTRE);// 单元格中的内容水平方向居中
        headerFormatLeft.setAlignment(jxl.format.Alignment.LEFT);// 单元格中的内容水平方向居中
        headerFormatRight.setAlignment(jxl.format.Alignment.RIGHT);// 单元格中的内容水平方向居中
        // 设置边框
        headerTitleFormat.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);
        headerFormatLeft.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);
        headerFormatRight.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);
        // 设置主标题
        sheet.mergeCells(0, 0, 7, 0);
        sheet.addCell(new jxl.write.Label(0, 0, title, titltFormat1));
        sheet.setColumnView(0, 50);
        sheet.setRowView(0, 400, false); // 设置行高
        sheet.mergeCells(0, 1, 7, 1);
        sheet.setColumnView(0, 50);
        sheet.setRowView(1, 400, false); // 设置行高
        sheet.addCell(new jxl.write.Label(0, 1, "条码校验差异", titltFormat2));
        // 3.加入标签 (列,行,"内容",字体)
        // 打印标题(0为第一行）
        for (int i = 0; i < listTitle.size(); i++) {
            if (i == 0) {
                sheet.setColumnView(i, 15 + listTitle.get(i).length());
            } else {
                sheet.setColumnView(i, 15);
            }
            sheet.setRowView(3, 400, false); // 设置行高
            sheet.addCell(new jxl.write.Label(i, 3, listTitle.get(i), headerTitleFormat));
        }

        // 循环打印数据(i+1为第(i+1)行）
        for (int i = 0; i < listContent.size(); i++) {
            for (int j = 0; j < listContent.get(i).length; j++) {
                if (j > 4) {
                    sheet.addCell(new jxl.write.Label(j, i + 4, listContent.get(i)[j].toString(), headerFormatRight));
                } else {
                    sheet.addCell(new jxl.write.Label(j, i + 4, listContent.get(i)[j].toString(), headerFormatLeft));
                }
            }
            // 4.加入工作表
        }
        // 设置列的宽度 (哪列,宽度数字)
        // sheet.setColumnView(0, 40);
        // 设置第2行的高度
        // sheet.setRowView(1,400,false);
        // 写入Exel工作表
        workbook.write();
        // 关闭Excel工作薄对象
        workbook.close();
        // System.out.println("exportExcelsuccess");
    }

    // 从Excel中导入数据(excel表的路径)实现打印
    public static String importExcel(String filePath) {
        File file = new File(filePath);
        StringBuffer result = new StringBuffer();
        try {
            // 创建流
            FileInputStream fis = new FileInputStream(file);
            StringBuilder sb = new StringBuilder();
            // 获得第一个工作表对象
            jxl.Workbook rwb = Workbook.getWorkbook(fis);
            // 创建单元格数组
            Sheet[] sheet = rwb.getSheets();
            // 循环读取excel中的单元格
            for (int i = 0; i < sheet.length; i++) {
                Sheet rs = rwb.getSheet(i);
                for (int j = 0; j < rs.getRows(); j++) {
                    Cell[] cells = rs.getRow(j);
                    for (int k = 0; k < cells.length; k++) {
                        sb.append(cells[k].getContents() + "\t");
                    }
                    // 输出换行
                    sb.append("\n");
                }
            }
            fis.close();
            result.append(sb);
            System.out.println("importExcelsuccess");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString();

    }

}
