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
 * Title: BaseInformationActivity Description: 基本资料活动界面
 * 
 * @author LYJ
 * 
 */
public class BaseInformationActivity extends BaseWapperActivity implements OnItemClickListener {

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
        img_activity = new String[] {"com.fuxi.activity.GoodsInformationActivity", "com.fuxi.activity.AddCustomerActivity", "com.fuxi.activity.PictureActivity"};
        img_text = new String[] {"货品资料", "新增客户", "货品图片"};
        imgs = new Integer[] {R.string.goods_info, R.string.add_customer, R.string.picture};
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
        setTitle("基 本 资 料");
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
            intent = new Intent(BaseInformationActivity.this, Class.forName(img_activity[position]));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        startActivity(intent);
    }
}
