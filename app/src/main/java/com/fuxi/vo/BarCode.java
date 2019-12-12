package com.fuxi.vo;

import java.math.BigDecimal;

/**
 * Title: BarCode Description: BarCode对象
 * 
 * @author LJ,LYJ
 * 
 */
public class BarCode {
    private String goodsid;
    private String colorid;
    private String sizeid;
    private String barcode;
    private String goodsname;
    private String goodscode;
    private String colorname;
    private String colorcode;
    private String sizename;
    private String sizeGroupId;
    private Integer indexno;
    private String sizecode;
    private BigDecimal retailSales;
    private BigDecimal unitPrice;
    private BigDecimal discountPrice; // 折扣价
    private BigDecimal goodsDiscount; // 货品折扣
    private String goodsDiscountFlag; // 货品是否允许折上折
    private BigDecimal discountRate; // 折扣
    private String type; // 单价类型
    private String customerId; // 客户
    private Integer qty;
    private Integer boxQty;
    private Integer oneBoxQty; // 一箱中的数量
    private String sizeStr; // 箱条码的配码

    // 封装属性
    public String getGoodsid() {
        return goodsid;
    }

    public void setGoodsid(String goodsid) {
        this.goodsid = goodsid;
    }

    public String getColorid() {
        return colorid;
    }

    public void setColorid(String colorid) {
        this.colorid = colorid;
    }

    public String getSizeid() {
        return sizeid;
    }

    public void setSizeid(String sizeid) {
        this.sizeid = sizeid;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getGoodsname() {
        return goodsname;
    }

    public void setGoodsname(String goodsname) {
        this.goodsname = goodsname;
    }

    public String getGoodscode() {
        return goodscode;
    }

    public void setGoodscode(String goodscode) {
        this.goodscode = goodscode;
    }

    public String getColorname() {
        return colorname;
    }

    public void setColorname(String colorname) {
        this.colorname = colorname;
    }

    public String getColorcode() {
        return colorcode;
    }

    public void setColorcode(String colorcode) {
        this.colorcode = colorcode;
    }

    public String getSizename() {
        return sizename;
    }

    public void setSizename(String sizename) {
        this.sizename = sizename;
    }

    public Integer getIndexno() {
        return indexno;
    }

    public void setIndexno(Integer indexno) {
        this.indexno = indexno;
    }

    public String getSizecode() {
        return sizecode;
    }

    public void setSizecode(String sizecode) {
        this.sizecode = sizecode;
    }

    public BigDecimal getRetailSales() {
        return retailSales;
    }

    public void setRetailSales(BigDecimal retailSales) {
        this.retailSales = retailSales;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public Integer getQty() {
        return qty;
    }

    public void setQty(Integer qty) {
        this.qty = qty;
    }

    public Integer getBoxQty() {
        return boxQty;
    }

    public void setBoxQty(Integer boxQty) {
        this.boxQty = boxQty;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getSizeGroupId() {
        return sizeGroupId;
    }

    public void setSizeGroupId(String sizeGroupId) {
        this.sizeGroupId = sizeGroupId;
    }

    public BigDecimal getDiscountPrice() {
        return discountPrice;
    }

    public void setDiscountPrice(BigDecimal discountPrice) {
        this.discountPrice = discountPrice;
    }

    public Integer getOneBoxQty() {
        return oneBoxQty;
    }

    public void setOneBoxQty(Integer oneBoxQty) {
        this.oneBoxQty = oneBoxQty;
    }

    public String getSizeStr() {
        return sizeStr;
    }

    public void setSizeStr(String sizeStr) {
        this.sizeStr = sizeStr;
    }

    public BigDecimal getDiscountRate() {
        return discountRate;
    }

    public void setDiscountRate(BigDecimal discountRate) {
        this.discountRate = discountRate;
    }

    public BigDecimal getGoodsDiscount() {
        return goodsDiscount;
    }

    public void setGoodsDiscount(BigDecimal goodsDiscount) {
        this.goodsDiscount = goodsDiscount;
    }

    public String getGoodsDiscountFlag() {
        return goodsDiscountFlag;
    }

    public void setGoodsDiscountFlag(String goodsDiscountFlag) {
        this.goodsDiscountFlag = goodsDiscountFlag;
    }



}
