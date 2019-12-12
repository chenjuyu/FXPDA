package com.fuxi.vo;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Title: SalesOrder Description: SalesOrder对象
 * 
 * @author LYJ
 * 
 */
public class SalesOrder {
    private int ID;
    private String SalesOrderID;
    private String No;
    private Date Date;
    private String CustomerID;
    private String Customer;
    private String WarehouseID;
    private String WarehouseName;
    private BigDecimal QuantitySum;
    private BigDecimal DiscountSum;
    private BigDecimal AmountSum;
    private String Madeby;
    private String Memo;

    public int getID() {
        return ID;
    }

    public void setID(int iD) {
        ID = iD;
    }

    public String getSalesOrderID() {
        return SalesOrderID;
    }

    public void setSalesOrderID(String salesOrderID) {
        SalesOrderID = salesOrderID;
    }

    public String getNo() {
        return No;
    }

    public void setNo(String no) {
        No = no;
    }

    public Date getDate() {
        return Date;
    }

    public void setDate(Date date) {
        Date = date;
    }

    public String getCustomerID() {
        return CustomerID;
    }

    public void setCustomerID(String customerID) {
        CustomerID = customerID;
    }

    public String getCustomer() {
        return Customer;
    }

    public void setCustomer(String customer) {
        Customer = customer;
    }

    public String getWarehouseID() {
        return WarehouseID;
    }

    public void setWarehouseID(String warehouseID) {
        WarehouseID = warehouseID;
    }

    public String getWarehouseName() {
        return WarehouseName;
    }

    public void setWarehouseName(String warehouseName) {
        WarehouseName = warehouseName;
    }

    public BigDecimal getQuantitySum() {
        return QuantitySum;
    }

    public void setQuantitySum(BigDecimal quantitySum) {
        QuantitySum = quantitySum;
    }

    public BigDecimal getDiscountSum() {
        return DiscountSum;
    }

    public void setDiscountSum(BigDecimal discountSum) {
        DiscountSum = discountSum;
    }

    public BigDecimal getAmountSum() {
        return AmountSum;
    }

    public void setAmountSum(BigDecimal amountSum) {
        AmountSum = amountSum;
    }

    public String getMadeby() {
        return Madeby;
    }

    public void setMadeby(String madeby) {
        Madeby = madeby;
    }

    public String getMemo() {
        return Memo;
    }

    public void setMemo(String memo) {
        Memo = memo;
    }

}
