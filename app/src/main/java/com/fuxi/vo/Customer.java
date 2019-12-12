package com.fuxi.vo;

import java.io.Serializable;

/**
 * Title: Customer Description: Customer对象
 * 
 * @author LYJ
 * 
 */
@SuppressWarnings("serial")
public class Customer implements Serializable {

    // 登录参数
    private String userId; // 登录用户的ID
    private String userName; // 登录用户名
    private String userPwd; // 登录密码
    // 系统设置参数
    private String ip; // 服务器地址
    private String accountUserName; // 账套用户名
    private String accountPassword; // 账套密码
    private String versionNumber; // 版本号

    private String printIp; // 打印机IP
    private String printPort; // 打印机Port
    private String printer; // 打印机Port

    private String printBarcodeHeight; // 条码打印高度
    private String printBarcodeWidth; // 条码打印宽度
    private String printBarcodeType; // 条码打印类别

    public String getPrintBarcodeHeight() {
        return printBarcodeHeight;
    }

    public void setPrintBarcodeHeight(String printBarcodeHeight) {
        this.printBarcodeHeight = printBarcodeHeight;
    }

    public String getPrintBarcodeWidth() {
        return printBarcodeWidth;
    }

    public void setPrintBarcodeWidth(String printBarcodeWidth) {
        this.printBarcodeWidth = printBarcodeWidth;
    }

    public String getPrintBarcodeType() {
        return printBarcodeType;
    }

    public void setPrintBarcodeType(String printBarcodeType) {
        this.printBarcodeType = printBarcodeType;
    }

    // 封装属性
    public String getUserName() {
        return userName;
    }

    public String getPrinter() {
        return printer;
    }

    public void setPrinter(String printer) {
        this.printer = printer;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPwd() {
        return userPwd;
    }

    public void setUserPwd(String userPwd) {
        this.userPwd = userPwd;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getAccountUserName() {
        return accountUserName;
    }

    public void setAccountUserName(String accountUserName) {
        this.accountUserName = accountUserName;
    }

    public String getAccountPassword() {
        return accountPassword;
    }

    public void setAccountPassword(String accountPassword) {
        this.accountPassword = accountPassword;
    }

    public String getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(String versionNumber) {
        this.versionNumber = versionNumber;
    }

    public String getPrintIp() {
        return printIp;
    }

    public void setPrintIp(String printIp) {
        this.printIp = printIp;
    }

    public String getPrintPort() {
        return printPort;
    }

    public void setPrintPort(String printPort) {
        this.printPort = printPort;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    // 构造方法
    public Customer() {
        super();
        // TODO Auto-generated constructor stub
    }

    public Customer(String userId, String userName, String userPwd, String ip, String accountUserName, String accountPassword, String versionNumber, String printIp, String printPort, String printer) {
        super();
        this.userId = userId;
        this.userName = userName;
        this.userPwd = userPwd;
        this.ip = ip;
        this.accountUserName = accountUserName;
        this.accountPassword = accountPassword;
        this.versionNumber = versionNumber;
        this.printIp = printIp;
        this.printPort = printPort;
        this.printer = printer;
    }

    public Customer(String userId, String userName, String userPwd, String ip, String printIp, String printPort, String printer) {
        super();
        this.userId = userId;
        this.userName = userName;
        this.userPwd = userPwd;
        this.printIp = printIp;
        this.printPort = printPort;
        this.printer = printer;
        this.ip = ip;
    }

    public Customer(String userId, String userName, String userPwd, String ip) {
        super();
        this.userId = userId;
        this.userName = userName;
        this.userPwd = userPwd;
        this.ip = ip;
    }

}
