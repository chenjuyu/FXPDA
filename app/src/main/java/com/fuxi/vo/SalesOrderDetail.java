package com.fuxi.vo;

import java.math.BigDecimal;

/**
 * Title: SalesOrderDetail Description: SalesOrderDetail对象
 * 
 * @author LYJ
 * 
 */
public class SalesOrderDetail {

    private Integer ID;
    private String RelationID;
    private String GoodsID;
    private String ColorID;
    private String SizeID;
    private Integer Quantity;
    private BigDecimal UnitPrice;
    private BigDecimal Amount;
    private String BarCode;
    private String SizeName;
    private String SizeCode;
    private String GoodsCode;
    private String ColorName;

    public Integer getID() {
        return ID;
    }

    public void setID(Integer iD) {
        ID = iD;
    }

    public String getRelationID() {
        return RelationID;
    }

    public void setRelationID(String relationID) {
        RelationID = relationID;
    }

    public String getGoodsID() {
        return GoodsID;
    }

    public void setGoodsID(String goodsID) {
        GoodsID = goodsID;
    }

    public String getColorID() {
        return ColorID;
    }

    public void setColorID(String colorID) {
        ColorID = colorID;
    }

    public String getSizeID() {
        return SizeID;
    }

    public void setSizeID(String sizeID) {
        SizeID = sizeID;
    }

    public Integer getQuantity() {
        return Quantity;
    }

    public void setQuantity(Integer quantity) {
        Quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return UnitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        UnitPrice = unitPrice;
    }

    public BigDecimal getAmount() {
        return Amount;
    }

    public void setAmount(BigDecimal amount) {
        Amount = amount;
    }

    public String getBarCode() {
        return BarCode;
    }

    public void setBarCode(String barCode) {
        BarCode = barCode;
    }

    public String getSizeName() {
        return SizeName;
    }

    public void setSizeName(String sizeName) {
        SizeName = sizeName;
    }

    public String getSizeCode() {
        return SizeCode;
    }

    public void setSizeCode(String sizeCode) {
        SizeCode = sizeCode;
    }

    public String getGoodsCode() {
        return GoodsCode;
    }

    public void setGoodsCode(String goodsCode) {
        GoodsCode = goodsCode;
    }

    public String getColorName() {
        return ColorName;
    }

    public void setColorName(String colorName) {
        ColorName = colorName;
    }

}
