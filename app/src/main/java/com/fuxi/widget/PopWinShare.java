package com.fuxi.widget;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import com.fuxi.main.R;

/**
 * Title: PopWinShare Description: 自定义弹出菜单
 * 
 * @author LYJ
 * 
 */
public class PopWinShare extends PopupWindow {
    public View mainView, view, view_2, view_3, view_4, view_5, view_6, view_7, view_8, view_9, view_10, view_11;
    public LinearLayout layoutSwitch, layoutRemark, ll_parent, layoutPrint, layout_price, layoutAddCustomer, layoutBarcodeVerification, layoutGoodsBoxBarcode, layoutSwitchInputMode, layoutSingleDiscount, layoutBarcodeVerificationDifferent, layoutExportExcel, layoutOtherOptions;

    public TextView tvToggle, tvInput, tvOtherOptions;
    public CheckBox cb_last_price;
    public FontTextView ftvToggle, ftvInput, ftvOtherOptions;

    public PopWinShare(Activity paramActivity, View.OnClickListener paramOnClickListener) {
        super(paramActivity);
        // 窗口布局
        mainView = LayoutInflater.from(paramActivity).inflate(R.layout.popwin_share, null);
        // 切换(装箱/散件)布局
        layoutSwitch = ((LinearLayout) mainView.findViewById(R.id.layout_switch));
        // 添加备注布局
        layoutRemark = (LinearLayout) mainView.findViewById(R.id.layout_remark);
        // 打印布局
        layoutPrint = (LinearLayout) mainView.findViewById(R.id.layout_print);
        // 是否使用最近一次价格布局
        layout_price = (LinearLayout) mainView.findViewById(R.id.layout_price);
        // 父布局
        ll_parent = (LinearLayout) mainView.findViewById(R.id.ll_parent);
        // 使用最近一次价格
        cb_last_price = (CheckBox) mainView.findViewById(R.id.last_price);
        // 新增客户
        layoutAddCustomer = (LinearLayout) mainView.findViewById(R.id.layout_add_customer);
        // 条码校验
        layoutBarcodeVerification = (LinearLayout) mainView.findViewById(R.id.layout_barcode_verification);
        // 箱条码记录
        layoutGoodsBoxBarcode = (LinearLayout) mainView.findViewById(R.id.layout_goods_box_barcode);
        // 条码录入方式
        layoutSwitchInputMode = (LinearLayout) mainView.findViewById(R.id.layout_switch_input_mode);
        // 条码校验差异
        layoutBarcodeVerificationDifferent = (LinearLayout) mainView.findViewById(R.id.layout_barcode_verification_different);
        // 导出条码校验Excel
        layoutExportExcel = (LinearLayout) mainView.findViewById(R.id.layout_export_excel);
        // 整单折扣
        layoutSingleDiscount = (LinearLayout) mainView.findViewById(R.id.layout_single_discount);
        // 整单折扣
        layoutOtherOptions = (LinearLayout) mainView.findViewById(R.id.layout_other_options);
        // 分割线
        view = (View) mainView.findViewById(R.id.view);
        view_2 = (View) mainView.findViewById(R.id.view_2);
        view_3 = (View) mainView.findViewById(R.id.view_3);
        view_4 = (View) mainView.findViewById(R.id.view_4);
        view_5 = (View) mainView.findViewById(R.id.view_5);
        view_6 = (View) mainView.findViewById(R.id.view_6);
        view_7 = (View) mainView.findViewById(R.id.view_7);
        view_8 = (View) mainView.findViewById(R.id.view_8);
        view_9 = (View) mainView.findViewById(R.id.view_9);
        view_10 = (View) mainView.findViewById(R.id.view_10);
        view_11 = (View) mainView.findViewById(R.id.view_11);
        // 切换(装箱/散件)的文字
        tvToggle = (TextView) mainView.findViewById(R.id.toggle);
        tvInput = (TextView) mainView.findViewById(R.id.input);
        ftvToggle = (FontTextView) mainView.findViewById(R.id.ftv_toggle);
        ftvInput = (FontTextView) mainView.findViewById(R.id.ftv_input);
        // 其它选择的文字
        tvOtherOptions = (TextView) mainView.findViewById(R.id.other_options);
        ftvOtherOptions = (FontTextView) mainView.findViewById(R.id.ftv_other_options);

        // 设置每个子布局的事件监听器
        if (paramOnClickListener != null) {
            layoutSwitch.setOnClickListener(paramOnClickListener);
            layoutRemark.setOnClickListener(paramOnClickListener);
            layoutPrint.setOnClickListener(paramOnClickListener);
            layout_price.setOnClickListener(paramOnClickListener);
            layoutAddCustomer.setOnClickListener(paramOnClickListener);
            cb_last_price.setOnClickListener(paramOnClickListener);
            layoutBarcodeVerification.setOnClickListener(paramOnClickListener);
            layoutGoodsBoxBarcode.setOnClickListener(paramOnClickListener);
            layoutSwitchInputMode.setOnClickListener(paramOnClickListener);
            layoutBarcodeVerificationDifferent.setOnClickListener(paramOnClickListener);
            layoutExportExcel.setOnClickListener(paramOnClickListener);
            layoutSingleDiscount.setOnClickListener(paramOnClickListener);
            layoutOtherOptions.setOnClickListener(paramOnClickListener);
        }
        setContentView(mainView);
        // 设置宽度
        setWidth(260);
        // 设置高度
        setHeight(LayoutParams.WRAP_CONTENT);
        // 设置显示隐藏动画
        setAnimationStyle(R.style.AnimTools);
        // 设置背景透明
        setBackgroundDrawable(new ColorDrawable(0));
    }
}
