package com.fuxi.vo;

/**
 * Title: GoodsBoxBarcodeRecord Description: GoodsBoxBarcodeRecord对象(用于箱条码显示)
 * 
 * @author LYJ
 * 
 */
public class GoodsBoxBarcodeRecord {

    private int id;
    private String billId;
    private String goodsBoxBarcode;
    private String goodsId;
    private String colorId;
    private String sizeStr;
    private int boxQty;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBillId() {
        return billId;
    }

    public void setBillId(String billId) {
        this.billId = billId;
    }

    public String getGoodsBoxBarcode() {
        return goodsBoxBarcode;
    }

    public void setGoodsBoxBarcode(String goodsBoxBarcode) {
        this.goodsBoxBarcode = goodsBoxBarcode;
    }

    public int getBoxQty() {
        return boxQty;
    }

    public void setBoxQty(int boxQty) {
        this.boxQty = boxQty;
    }

    public String getSizeStr() {
        return sizeStr;
    }

    public void setSizeStr(String sizeStr) {
        this.sizeStr = sizeStr;
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

    public GoodsBoxBarcodeRecord() {
        super();
        // TODO Auto-generated constructor stub
    }

    public GoodsBoxBarcodeRecord(String billId, String goodsBoxBarcode, String sizeStr, String goodsId, String colorId, int boxQty) {
        super();
        this.billId = billId;
        this.goodsBoxBarcode = goodsBoxBarcode;
        this.sizeStr = sizeStr;
        this.goodsId = goodsId;
        this.colorId = colorId;
        this.boxQty = boxQty;
    }

}
