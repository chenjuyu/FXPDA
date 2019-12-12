package com.fuxi.vo;

/**
 * Title: Register Description: Register对象(设备注册)
 * 
 * @author LYJ
 * 
 */
public class Register {

    private String secret;
    private int type;
    private String madeDate;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getMadeDate() {
        return madeDate;
    }

    public void setMadeDate(String madeDate) {
        this.madeDate = madeDate;
    }

    public Register() {
        super();
        // TODO Auto-generated constructor stub
    }

    public Register(String secret, int type, String madeDate) {
        super();
        this.secret = secret;
        this.type = type;
        this.madeDate = madeDate;
    }


}
