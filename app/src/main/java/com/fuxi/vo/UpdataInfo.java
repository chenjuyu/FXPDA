package com.fuxi.vo;

/**
 * Title: UpdataInfo Description: UpdataInfo对象(设备升级时的对象封装)
 * 
 * @author LYJ
 * 
 */
public class UpdataInfo {

    private String version;
    private String url;
    private String description;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
