package com.fuxi.widget;

/**
 * Title: OnRefreshListener Description: 刷新监听接口
 * 
 * @author LJ
 * 
 */
public interface OnRefreshListener {

    /**
     * 下拉刷新
     */
    void onDownPullRefresh();

    /**
     * 上拉加载更多
     */
    void onLoadingMore();
}
