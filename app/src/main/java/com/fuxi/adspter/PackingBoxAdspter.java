package com.fuxi.adspter;

import java.util.List;
import java.util.Map;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.fuxi.main.R;
import com.fuxi.util.Tools;

/**
 * Title: InventoryQueryAdspter Description: 装箱单列表ListView适配器
 * 
 * @author LYJ
 * 
 */
public class PackingBoxAdspter extends BaseAdapter {

    private List<Map<String, Object>> data;
    private LayoutInflater layoutInflater;
    private Context context;

    public PackingBoxAdspter(Context context, List<Map<String, Object>> data) {
        this.context = context;
        this.data = data;
        this.layoutInflater = LayoutInflater.from(context);
    }

    /**
     * 组件集合，对应list_inventory_query.xml中的控件
     */
    public final class Zujian {
        public TextView no;
        public TextView warehouse;
        public TextView customer;
        public TextView count;
    }

    public void refresh(List<Map<String, Object>> list) {
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return data.size();
    }

    /**
     * 获得某一位置的数据
     */
    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    /**
     * 获得唯一标识
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Zujian zujian = null;
        if (convertView == null) {
            zujian = new Zujian();
            // 获得组件，实例化组件
            convertView = layoutInflater.inflate(R.layout.list_packing_box, null);
            zujian.no = (TextView) convertView.findViewById(R.id.tv_no);
            zujian.warehouse = (TextView) convertView.findViewById(R.id.tv_warehouse);
            zujian.customer = (TextView) convertView.findViewById(R.id.tv_customer);
            zujian.count = (TextView) convertView.findViewById(R.id.tv_count);
            convertView.setTag(zujian);
        } else {
            zujian = (Zujian) convertView.getTag();
        }
        // 绑定数据
        // 显示货品编码(控制长度)
        String no = String.valueOf(data.get(position).get("No"));
        int length = Tools.length(no);
        if (length > 11) {
            zujian.no.setTextSize(15);
        } else {
            zujian.no.setTextSize(17);
        }
        zujian.no.setText(no);
        zujian.warehouse.setText(String.valueOf(data.get(position).get("Warehouse")));
        zujian.customer.setText(String.valueOf(data.get(position).get("Customer")));
        zujian.count.setText(String.valueOf(data.get(position).get("QuantitySum")));
        return convertView;
    }

}
