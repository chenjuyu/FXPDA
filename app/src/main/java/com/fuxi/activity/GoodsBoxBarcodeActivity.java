package com.fuxi.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import com.fuxi.adspter.GoodsBoxBarcodeAdapter;
import com.fuxi.dao.GoodsBoxBarcodeRecordDao;
import com.fuxi.main.R;
import com.fuxi.vo.GoodsBoxBarcodeRecord;

/**
 * Title: GoodsBoxBarcodeActivity Description: 箱条码显示活动界面
 * 
 * @author LYJ
 * 
 */
public class GoodsBoxBarcodeActivity extends BaseWapperActivity {

    private static final String TAG = "GoodsBoxBarcodeActivity";

    private ListView lv_datas;
    private TextView tv_qtysum;

    private GoodsBoxBarcodeAdapter adapter;
    private GoodsBoxBarcodeRecordDao recordDao = new GoodsBoxBarcodeRecordDao(this);
    private List<HashMap<String, Object>> datas;
    private List<HashMap<String, Object>> dataList = new ArrayList<HashMap<String, Object>>();

    private String billId;

    @Override
    public void onClick(View v) {}

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    protected void loadViewLayout() {
        setContentView(R.layout.activity_goods_box_barcode);
        setTitle("箱 条 码 明 细");
    }

    /**
     * 获取箱条码显示的数据
     * 
     * @param datas
     */
    private void getData(List<HashMap<String, Object>> datas) {
        // 新增单据时
        if (datas.size() == 0) {
            List<GoodsBoxBarcodeRecord> list = recordDao.getList(billId);
            for (int i = 0; i < list.size(); i++) {
                HashMap<String, Object> hm = new HashMap<String, Object>();
                hm.put("Barcode", list.get(i).getGoodsBoxBarcode());
                hm.put("BoxQty", list.get(i).getBoxQty());
                hm.put("SizeStr", list.get(i).getSizeStr());
                hm.put("GoodsID", list.get(i).getGoodsId());
                hm.put("ColorID", list.get(i).getColorId());
                dataList.add(hm);
            }
        } else {
            dataList.addAll(datas);
        }
    }

    /**
     * 点击返回时
     */
    private void back() {
        finish();
    }

    /**
     * 计算并显示总数量和价格
     */
    private void countTotal() {
        int sum = 0;
        for (int j = 0; j < dataList.size(); j++) {
            int num = Integer.parseInt(String.valueOf(dataList.get(j).get("BoxQty")));
            sum += num;
        }
        tv_qtysum.setText(String.valueOf(sum));
        adapter.refresh();
        adapter.notifyDataSetInvalidated();
    }

    @Override
    protected void onHeadLeftButton(View v) {
        back();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            back();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void processLogic() {
        Bundle bundle = this.getIntent().getExtras();
        if (null != bundle) {
            billId = bundle.getString("billId");
            datas = (ArrayList<HashMap<String, Object>>) getIntent().getSerializableExtra("datas");
        }
        getData(datas);
        // 计算数量
        countTotal();
        // 显示获取到的数据
        adapter.refresh();
    }

    @Override
    protected void setListener() {
        adapter = new GoodsBoxBarcodeAdapter(this, dataList);
        lv_datas.setAdapter(adapter);
    }

    @Override
    protected void findViewById() {
        lv_datas = (ListView) findViewById(R.id.lv_datas);
        tv_qtysum = (TextView) findViewById(R.id.qtysum);
    }

}
