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
 * Title: InventoryDistributionAdspter Description: 仓库库存分布ListView适配器
 * 
 * @author LYJ
 * 
 */
public class InventoryDistributionAdspter extends BaseAdapter {

    private List<Map<String, Object>> data;
    private LayoutInflater layoutInflater;
    private Context context;

    public InventoryDistributionAdspter(Context context, List<Map<String, Object>> data) {
        this.context = context;
        this.data = data;
        this.layoutInflater = LayoutInflater.from(context);
    }

    /**
     * 组件集合，对应activity_inventory_distribution.xml中的控件
     */
    public final class Zujian {
        public TextView name;
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
            convertView = layoutInflater.inflate(R.layout.list_inventory_distribution, null);
            zujian.name = (TextView) convertView.findViewById(R.id.tv_name);
            zujian.count = (TextView) convertView.findViewById(R.id.tv_count);
            convertView.setTag(zujian);
        } else {
            zujian = (Zujian) convertView.getTag();
        }
        // 绑定数据
        String deptName = String.valueOf(data.get(position).get("DeptName"));
        int length = Tools.length(deptName);
        if (length > 24) {
            zujian.name.setTextSize(16);
        } else {
            zujian.name.setTextSize(17);
        }
        zujian.name.setText(deptName);
        zujian.count.setText(String.valueOf(data.get(position).get("Qty")));
        return convertView;
    }

}
