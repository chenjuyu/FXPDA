package com.fuxi.vo;

import java.math.BigDecimal;

/**
 * Title: LatestPrice Description: LatestPrice对象(最近价格)
 * 
 * @author LYJ
 * 
 */
public class LatestPrice {

    private int id;
    private String goodsId;
    private String customerId;
    private String type;
    private String barcodeType;
    private BigDecimal lastPrice;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(String goodsId) {
        this.goodsId = goodsId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getBarcodeType() {
        return barcodeType;
    }

    public void setBarcodeType(String barcodeType) {
        this.barcodeType = barcodeType;
    }

    public BigDecimal getLastPrice() {
        return lastPrice;
    }

    public void setLastPrice(BigDecimal lastPrice) {
        this.lastPrice = lastPrice;
    }

    public LatestPrice() {
        super();
        // TODO Auto-generated constructor stub
    }

    public LatestPrice(int id, String goodsId, String customerId, String type, String barcodeType, BigDecimal lastPrice) {
        super();
        this.id = id;
        this.goodsId = goodsId;
        this.customerId = customerId;
        this.type = type;
        this.barcodeType = barcodeType;
        this.lastPrice = lastPrice;
    }

    public LatestPrice(String goodsId, String customerId, String type, String barcodeType, BigDecimal lastPrice) {
        super();
        this.goodsId = goodsId;
        this.customerId = customerId;
        this.type = type;
        this.barcodeType = barcodeType;
        this.lastPrice = lastPrice;
    }

    public LatestPrice(String goodsId, String customerId, String type, String barcodeType) {
        super();
        this.goodsId = goodsId;
        this.customerId = customerId;
        this.type = type;
        this.barcodeType = barcodeType;
    }

}
