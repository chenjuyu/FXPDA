package com.fuxi.vo;

/**
 * 
 * Title: Images Description: Images对象
 * 
 * @author LYJ
 * 
 */
public class Images {

    private String goodsCode;
    private String madeDate;

    public String getGoodsCode() {
        return goodsCode;
    }

    public void setGoodsCode(String goodsCode) {
        this.goodsCode = goodsCode;
    }

    public String getMadeDate() {
        return madeDate;
    }

    public void setMadeDate(String madeDate) {
        this.madeDate = madeDate;
    }

    public Images() {
        super();
    }

    public Images(String goodsCode, String madeDate) {
        super();
        this.goodsCode = goodsCode;
        this.madeDate = madeDate;
    }

}
