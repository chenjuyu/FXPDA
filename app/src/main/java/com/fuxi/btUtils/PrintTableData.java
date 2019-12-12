package com.fuxi.btUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 列表数据 Created by Administrator on 2016/12/14.
 */
public class PrintTableData {
    private List<HeaderLable> headers;// 表头
    private List<Map<String, String>> dataList = new ArrayList<>();// 数据
    private String newLineKey;// 提行显示的key

    public List<HeaderLable> getHeaders() {
        return headers;
    }

    public void setHeaders(List<HeaderLable> headers) {
        this.headers = headers;
    }

    public List<Map<String, String>> getDataList() {
        return dataList;
    }

    public void setDataList(List<Map<String, String>> dataList) {
        this.dataList = dataList;
    }

    public String getNewLineKey() {
        return newLineKey;
    }

    public void setNewLineKey(String newLineKey) {
        this.newLineKey = newLineKey;
    }

    public static class HeaderLable {
        private String name;
        private String key;
        private int width;

        public HeaderLable() {}

        public HeaderLable(String name, String key, int width) {
            this.name = name;
            this.key = key;
            this.width = width;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }
    }
}
