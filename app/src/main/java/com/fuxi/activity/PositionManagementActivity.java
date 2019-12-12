package com.fuxi.activity;

import android.content.Intent;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import com.fuxi.main.R;
import com.fuxi.util.gridview.MyGridAdapter;
import com.fuxi.util.gridview.MyGridView;

/**
 * Title: PositionManagementActivity Description: 仓位管理活动界面
 * 
 * @author LYJ
 * 
 */
public class PositionManagementActivity extends BaseWapperActivity implements OnItemClickListener {

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
        img_activity =
                new String[] {"com.fuxi.activity.ShelvesInActivity", "com.fuxi.activity.StockOutActivity", "com.fuxi.activity.StockOutActivity", "com.fuxi.activity.AllocationShelvesInActivity", "com.fuxi.activity.MoveShelvesActivity", "com.fuxi.activity.StorageQueryActivity",
                        "com.fuxi.activity.InitializeShelvesInActivity", "com.fuxi.activity.PositionSettingActivity"};
        img_text = new String[] {"上架", "下架", "调拨下架", "调拨上架", "移位", "仓位分布", "仓位初始化", "仓储设置"};
        imgs = new Integer[] {R.string.shelves_in, R.string.shelves_out, R.string.allocation_shelves_out, R.string.allocation_shelves_in, R.string.move_shelves, R.string.position_distribution, R.string.initial, R.string.position_setting};
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
        setContentView(R.layout.activity_position_management);
        setTitle("仓 位 管 理");
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
            intent = new Intent(PositionManagementActivity.this, Class.forName(img_activity[position]));
            if (position == 0) {
                intent.putExtra("type", "上架");
            } else if (position == 1) {
                intent.putExtra("type", "下架");
            } else if (position == 2) {
                intent.putExtra("type", "调出");
            } else if (position == 3) {
                intent.putExtra("type", "调入");
            } else if (position == 4) {
                intent.putExtra("type", "移出");
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        startActivity(intent);
    }
}
