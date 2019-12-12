package com.fuxi.vo;

/**
 * Title: Paramer Description: Paramer对象(系统参数)
 * 
 * @author LYJ
 * 
 */
public class Paramer {
    private int id;
    private String name;
    private String value;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Paramer(String name, String value) {
        super();
        this.name = name;
        this.value = value;
    }

    public Paramer() {
        super();
    }

}
