package com.fuxi.vo;

/**
 * Title: OutLineStocktaking Description: 离线盘点对象封装
 * 
 * @author LYJ
 * 
 */
public class OutLineStocktaking {

    private int id;
    private String no;
    private String departmentID;
    private String barcode;
    private String shelvesNo;
    private String departmentCode;
    private String departmentName;
    private int quantity;
    private String madeDate;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        this.no = no;
    }

    public String getDepartmentID() {
        return departmentID;
    }

    public void setDepartmentID(String departmentID) {
        this.departmentID = departmentID;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getShelvesNo() {
        return shelvesNo;
    }

    public void setShelvesNo(String shelvesNo) {
        this.shelvesNo = shelvesNo;
    }

    public String getDepartmentCode() {
        return departmentCode;
    }

    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getMadeDate() {
        return madeDate;
    }

    public void setMadeDate(String madeDate) {
        this.madeDate = madeDate;
    }

    public OutLineStocktaking() {
        super();
        // TODO Auto-generated constructor stub
    }

    public OutLineStocktaking(int id, String no, String departmentID, String barcode, String shelvesNo, String departmentCode, String departmentName, int quantity, String madeDate) {
        super();
        this.id = id;
        this.no = no;
        this.departmentID = departmentID;
        this.barcode = barcode;
        this.shelvesNo = shelvesNo;
        this.departmentCode = departmentCode;
        this.departmentName = departmentName;
        this.quantity = quantity;
        this.madeDate = madeDate;
    }

    public OutLineStocktaking(String no, String departmentID, String barcode, String shelvesNo, String departmentCode, String departmentName, int quantity, String madeDate) {
        super();
        this.no = no;
        this.departmentID = departmentID;
        this.barcode = barcode;
        this.shelvesNo = shelvesNo;
        this.departmentCode = departmentCode;
        this.departmentName = departmentName;
        this.quantity = quantity;
        this.madeDate = madeDate;
    }

}
