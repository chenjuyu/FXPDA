package com.fuxi.util.print.template;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import android.content.Context;
import com.fuxi.application.ECApplication;
import com.fuxi.dao.ParamerDao;
import com.fuxi.util.LoginParameterUtil;
import com.fuxi.util.ParamterFileUtil;
import com.fuxi.util.Tools;
import com.fuxi.util.print.NetPrinter;
import com.fuxi.vo.Customer;
import com.fuxi.vo.Paramer;

public class GeneratPrintTemplate {

    private NetPrinter printer = new NetPrinter();

    private Paramer printPIp;
    private Paramer printPPort;

    private String strPrintIp;
    private String strPrintPort;

    private String packingBoxNo;
    private String customer;
    private String no;
    private List<Map<String, Object>> list;
    private String printContent = "";

    public GeneratPrintTemplate(String packingBoxNo, String customer, String no, List<Map<String, Object>> list) {
        this.packingBoxNo = packingBoxNo;
        this.customer = customer;
        this.no = no;
        this.list = list;
    }

    /**
     * 普通客户的装箱单打印模板
     */
    public void packingBoxTemplateHasPrice(Context context) {
        String sb = null;
        sb = "单号：" + packingBoxNo + "\t\t\t客户：" + customer + "\t\t\t日期：" + Tools.dateToString(new Date());
        sb = sb + "\n|\t\t|\t\t|\t\t|";
        sb = sb + "\n|\t\t箱号\t\t|\t\t型号\t\t|\t\t商品名称\t\t|\t\t数量\t\t|\t\t合计\t\t|";
        sb = sb + "\n|\tQ0918-01\t|\t3A1313223-01\t|\t钱包\t|\t210\t|\t24990\t|";
        printContent = sb.toString();
        print(context);
    }

    /**
     * 特殊客户的装箱单打印模板
     */
    public void packingBoxTemplateNoPrice(Context context) {
        packingBoxTemplateHasPrice(context);
    }

    /**
     * 唯品会装箱单打印模板
     */
    public void packingBoxTemplateVip(Context context) {
        packingBoxTemplateHasPrice(context);
    }

    private void print(Context context) {
        ParamerDao paramerDao = new ParamerDao(context);
        // 获取显示值
        printPIp = paramerDao.find("printIp");
        printPPort = paramerDao.find("printPort");
        // 非空判断并绑定显示值
        if (null != printPIp) {
            strPrintIp = printPIp.getValue();
        } else {
            Properties userInfo = ParamterFileUtil.getIpInfo(context);
            if (null != userInfo) {
                strPrintIp = userInfo.getProperty("printIp");
            }
        }
        if (null != printPPort) {
            strPrintPort = printPPort.getValue();
        } else {
            Properties userInfo = ParamterFileUtil.getIpInfo(context);
            if (null != userInfo) {
                strPrintPort = userInfo.getProperty("printPort");
            }
        }
        // 打印
        new Thread(new Runnable() {

            @Override
            public void run() {
                printer.Open(strPrintIp, Integer.parseInt(strPrintPort));
                printer.Set();
                printer.PrintText("装箱单", 1, 9, 0);
                printer.PrintNewLines(1);
                printer.PrintText(printContent, 1, 0, 0);
            }
        }).start();

    }

}
