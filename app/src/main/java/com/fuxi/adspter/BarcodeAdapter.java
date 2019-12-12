package com.fuxi.adspter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.fuxi.main.R;

/**
 * Title: BarcodeAdapter Description: 条码显示ListView适配器
 * 
 * @author LYJ
 * 
 */
public class BarcodeAdapter extends BaseAdapter {

    private List<Map<String, Object>> data;
    private LayoutInflater layoutInflater;
    private Context context;

    public BarcodeAdapter(Context context, List<Map<String, Object>> data) {
        this.context = context;
        this.data = data;
        this.layoutInflater = LayoutInflater.from(context);
    }

    /**
     * 组件集合，对应list_barcode.xml中的控件
     */
    public final class Zujian {
        public TextView barcode;
        public TextView qty;
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

    public void refresh() {
        this.notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Zujian zujian = null;
        if (convertView == null) {
            zujian = new Zujian();
            // 获得组件，实例化组件
            convertView = layoutInflater.inflate(R.layout.list_barcode, null);
            zujian.barcode = (TextView) convertView.findViewById(R.id.barcode);
            zujian.qty = (TextView) convertView.findViewById(R.id.qty);
            convertView.setTag(zujian);
        } else {
            zujian = (Zujian) convertView.getTag();
        }
        // 绑定数据
        zujian.barcode.setText(String.valueOf(data.get(position).get("Barcode")));
        zujian.qty.setText(String.valueOf(data.get(position).get("Quantity")));
        return convertView;
    }

}
