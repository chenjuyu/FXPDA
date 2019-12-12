package com.fuxi.vo;

public class GeneralPackingBoxNo {

    private int id;
    private String boxNo;
    private String customerId;
    private String madeDate;
    private int type;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBoxNo() {
        return boxNo;
    }

    public void setBoxNo(String boxNo) {
        this.boxNo = boxNo;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getMadeDate() {
        return madeDate;
    }

    public void setMadeDate(String madeDate) {
        this.madeDate = madeDate;
    }

    public GeneralPackingBoxNo() {
        super();
        // TODO Auto-generated constructor stub
    }

    public GeneralPackingBoxNo(String boxNo, String customerId, String madeDate, int type) {
        super();
        this.boxNo = boxNo;
        this.customerId = customerId;
        this.madeDate = madeDate;
        this.type = type;
    }

}
