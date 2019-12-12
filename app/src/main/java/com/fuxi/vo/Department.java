package com.fuxi.vo;

public class Department {

    private int id;
    private String departmentId;
    private String department;
    private int mustExistsGoodsFlag;
    private String departmentCode;
    private String madeDate;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getMustExistsGoodsFlag() {
        return mustExistsGoodsFlag;
    }

    public void setMustExistsGoodsFlag(int mustExistsGoodsFlag) {
        this.mustExistsGoodsFlag = mustExistsGoodsFlag;
    }

    public String getDepartmentCode() {
        return departmentCode;
    }

    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
    }

    public String getMadeDate() {
        return madeDate;
    }

    public void setMadeDate(String madeDate) {
        this.madeDate = madeDate;
    }

    public Department() {
        super();
        // TODO Auto-generated constructor stub
    }

    public Department(int id, String departmentId, String department, int mustExistsGoodsFlag, String departmentCode, String madeDate) {
        super();
        this.id = id;
        this.departmentId = departmentId;
        this.department = department;
        this.mustExistsGoodsFlag = mustExistsGoodsFlag;
        this.departmentCode = departmentCode;
        this.madeDate = madeDate;
    }

    public Department(String departmentId, String department, int mustExistsGoodsFlag, String departmentCode, String madeDate) {
        super();
        this.departmentId = departmentId;
        this.department = department;
        this.mustExistsGoodsFlag = mustExistsGoodsFlag;
        this.departmentCode = departmentCode;
        this.madeDate = madeDate;
    }



}
