package com.fuxi.vo;

/**
 * Title: StockRemove Description: 货架移位对象(仓位管理)
 * 
 * @author LYJ
 * 
 */
public class StockRemove {

    private int id;
    private String deptId;
    private String deptName;
    private String barcode;
    private String supplierCode;
    private String goodsCode;
    private String goodsName;
    private String color;
    private String size;
    private String storage;
    private String storageId;
    private String goodsId;
    private String colorId;
    private String sizeId;
    private int quantity;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDeptId() {
        return deptId;
    }

    public void setDeptId(String deptId) {
        this.deptId = deptId;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getGoodsCode() {
        return goodsCode;
    }

    public void setGoodsCode(String goodsCode) {
        this.goodsCode = goodsCode;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getStorage() {
        return storage;
    }

    public void setStorage(String storage) {
        this.storage = storage;
    }

    public String getStorageId() {
        return storageId;
    }

    public void setStorageId(String storageId) {
        this.storageId = storageId;
    }

    public String getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(String goodsId) {
        this.goodsId = goodsId;
    }

    public String getColorId() {
        return colorId;
    }

    public void setColorId(String colorId) {
        this.colorId = colorId;
    }

    public String getSizeId() {
        return sizeId;
    }

    public void setSizeId(String sizeId) {
        this.sizeId = sizeId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getSupplierCode() {
        return supplierCode;
    }

    public void setSupplierCode(String supplierCode) {
        this.supplierCode = supplierCode;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public StockRemove() {
        super();
        // TODO Auto-generated constructor stub
    }

    public StockRemove(String deptId, String deptName, String barcode, String supplierCode, String goodsCode, String goodsName, String color, String size, String storage, String storageId, String goodsId, String colorId, String sizeId, int quantity) {
        super();
        this.deptId = deptId;
        this.deptName = deptName;
        this.supplierCode = supplierCode;
        this.barcode = barcode;
        this.goodsCode = goodsCode;
        this.goodsName = goodsName;
        this.color = color;
        this.size = size;
        this.storage = storage;
        this.storageId = storageId;
        this.goodsId = goodsId;
        this.colorId = colorId;
        this.sizeId = sizeId;
        this.quantity = quantity;
    }

    public StockRemove(String deptId, String storageId, String goodsId, String colorId, String sizeId) {
        super();
        this.deptId = deptId;
        this.storageId = storageId;
        this.goodsId = goodsId;
        this.colorId = colorId;
        this.sizeId = sizeId;
    }

    public StockRemove(int id, int quantity) {
        super();
        this.id = id;
        this.quantity = quantity;
    }

}
