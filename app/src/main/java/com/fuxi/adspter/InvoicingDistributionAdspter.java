package com.fuxi.adspter;

import java.util.List;
import java.util.Map;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.fuxi.main.R;
import com.fuxi.util.Tools;

/**
 * Title: InvoicingDistributionAdspter Description: 进销存部门货品尺码库存分布ListView适配器
 * 
 * @author LYJ
 * 
 */
public class InvoicingDistributionAdspter extends BaseAdapter {

    private List<Map<String, Object>> data;
    private LayoutInflater layoutInflater;
    private Context context;

    public InvoicingDistributionAdspter(Context context, List<Map<String, Object>> data) {
        this.context = context;
        this.data = data;
        this.layoutInflater = LayoutInflater.from(context);
    }

    /**
     * 组件集合，对应list_invoicing_distribution.xml中的控件
     */
    public final class Zujian {
        public TextView color;
        public TextView size;
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
            convertView = layoutInflater.inflate(R.layout.list_invoicing_distribution, null);
            zujian.color = (TextView) convertView.findViewById(R.id.tv_color);
            zujian.size = (TextView) convertView.findViewById(R.id.tv_size);
            zujian.count = (TextView) convertView.findViewById(R.id.tv_count);
            convertView.setTag(zujian);
        } else {
            zujian = (Zujian) convertView.getTag();
        }
        // 绑定数据
        String color = String.valueOf(data.get(position).get("Color"));
        zujian.color.setText(color);
        zujian.size.setText(String.valueOf(data.get(position).get("Size")));
        zujian.count.setText(String.valueOf(data.get(position).get("Quantity")));
        // 设置背景颜色
        Activity activity = (Activity) context;
        if ("小计".equals(color)) {
            convertView.setBackgroundColor(activity.getResources().getColor(R.color.pink));
            zujian.color.setTextColor(Color.BLACK);
        } else {
            convertView.setBackgroundColor(Color.WHITE);
            zujian.color.setTextColor(activity.getResources().getColor(R.color.list_text_color));
        }
        return convertView;
    }

}
