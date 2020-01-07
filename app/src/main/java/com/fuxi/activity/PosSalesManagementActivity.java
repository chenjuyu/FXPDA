package com.fuxi.activity;

import android.content.Intent;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import com.fuxi.main.R;
import com.fuxi.util.LoginParameterUtil;
import com.fuxi.util.gridview.MyGridAdapter;
import com.fuxi.util.gridview.MyGridView;

/**
 * Title: PurchaseManagementActivity Description: 零售管理活动界面
 * 
 * @author LYJ
 * 
 */
public class PosSalesManagementActivity extends BaseWapperActivity implements OnItemClickListener {

    private MyGridView gridview;
    private Intent intent = null;
    // 其它属性
    private String[] img_text = null; // 显示的文字
    private String[] img_activity = null; // 图标对应的activity
    private Integer[] imgs = null; // 图标

    /**
     * 初始化数据
     */
    private void getData() {
        if (LoginParameterUtil.showGiftMenuFlag) {
            img_activity = new String[] {"com.fuxi.activity.SalesTicketActivity", "com.fuxi.activity.GiftActivity", "com.fuxi.activity.PosSalesInventoryQueryActivity", "com.fuxi.activity.PossalesQueryActivity", "com.fuxi.activity.DailyKnotsActivity","com.fuxi.activity.PosReportActivity"};
            img_text = new String[] {"销售小票", "赠品单", "零售库存查询", "销售单查询", "日结","零售报表"};
            imgs = new Integer[] {R.string.sales_ticket, R.string.gift, R.string.inventory_query, R.string.noQuery, R.string.daily_knots,R.string.query};
        } else {
            img_activity = new String[] {"com.fuxi.activity.SalesTicketActivity", "com.fuxi.activity.PosSalesInventoryQueryActivity", "com.fuxi.activity.PossalesQueryActivity", "com.fuxi.activity.DailyKnotsActivity","com.fuxi.activity.PosReportActivity"};
            img_text = new String[] {"销售小票", "零售库存查询", "销售单查询", "日结","零售报表"};
            imgs = new Integer[] {R.string.sales_ticket, R.string.inventory_query, R.string.noQuery, R.string.daily_knots,R.string.query};
        }
    }

    @Override
    public void onClick(View v) {}

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    protected void loadViewLayout() {
        setContentView(R.layout.activity_purchase_management);
        setTitle("零 售 管 理");
        setHeadLeftVisibility(View.INVISIBLE);
    }

    @Override
    protected void setListener() {
        gridview.setOnItemClickListener(this);
    }

    @Override
    protected void processLogic() {
        // 显示按钮
        setHeadLeftVisibility(View.VISIBLE);
        // setHeadRightText(R.string.exit);
    }

    @Override
    protected void findViewById() {
        gridview = (MyGridView) findViewById(R.id.gv_gridview);
        // 获取数据
        getData();
        // 设置适配器
        gridview.setAdapter(new MyGridAdapter(img_text, img_activity, imgs, this));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try {
            intent = new Intent(PosSalesManagementActivity.this, Class.forName(img_activity[position]));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        startActivity(intent);
    }
}
