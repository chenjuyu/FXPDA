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

/**
 * Title: ShelvesOutAdspter Description: 仓位下架ListView适配器(仓库管理)
 * 
 * @author LYJ
 * 
 */
public class ShelvesOutAdspter extends BaseAdapter {

    private List<Map<String, Object>> data;
    private LayoutInflater layoutInflater;
    private Context context;
    private String type;

    public ShelvesOutAdspter(Context context, List<Map<String, Object>> data, String type) {
        this.context = context;
        this.data = data;
        this.type = type;
        this.layoutInflater = LayoutInflater.from(context);
    }

    /**
     * 组件集合，对应list_storage_out.xml中的控件
     */
    public final class Zujian {
        public TextView code;
        public TextView supplierCode;
        public TextView storage;
        public TextView color;
        public TextView size;
        public TextView qty;
        public TextView shelves_out;
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
            convertView = layoutInflater.inflate(R.layout.list_storage_out, null);
            zujian.code = (TextView) convertView.findViewById(R.id.code);
            zujian.supplierCode = (TextView) convertView.findViewById(R.id.supplierCode);
            zujian.storage = (TextView) convertView.findViewById(R.id.storage);
            zujian.color = (TextView) convertView.findViewById(R.id.color);
            zujian.size = (TextView) convertView.findViewById(R.id.size);
            zujian.qty = (TextView) convertView.findViewById(R.id.qty);
            zujian.shelves_out = (TextView) convertView.findViewById(R.id.shelves_out);
            convertView.setTag(zujian);
        } else {
            zujian = (Zujian) convertView.getTag();
        }
        if (convertView != null) {
            // 绑定数据
            zujian.code.setText(String.valueOf(data.get(position).get("Code")));
            zujian.supplierCode.setText(String.valueOf(data.get(position).get("SupplierCode")));
            zujian.storage.setText(String.valueOf(data.get(position).get("Storage")));
            zujian.color.setText(String.valueOf(data.get(position).get("Color")));
            zujian.size.setText(String.valueOf(data.get(position).get("Size")));
            zujian.qty.setText(String.valueOf(data.get(position).get("Quantity")));
            Activity activity = (Activity) context;
            if ("下架".equals(type)) {
                zujian.shelves_out.setText("下架");
                if ("无".equals(String.valueOf(data.get(position).get("Storage")))) {
                    zujian.shelves_out.setEnabled(false);
                    zujian.shelves_out.setBackgroundColor(Color.GRAY);
                } else {
                    zujian.shelves_out.setEnabled(true);
                    zujian.shelves_out.setBackground(activity.getResources().getDrawable(R.drawable.upload_button));
                }
            } else {
                zujian.shelves_out.setText("上架");
            }
        }
        return convertView;
    }

}
