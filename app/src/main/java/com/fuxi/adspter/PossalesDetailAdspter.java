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

/**
 * Title: PossalesDetailAdspter Description: 零售销售明细表查询ListView适配器
 * 
 * @author LYJ
 * 
 */
public class PossalesDetailAdspter extends BaseAdapter {

    private List<Map<String, Object>> data;
    private LayoutInflater layoutInflater;
    private Context context;

    public PossalesDetailAdspter(Context context, List<Map<String, Object>> data) {
        this.context = context;
        this.data = data;
        this.layoutInflater = LayoutInflater.from(context);
    }

    /**
     * 组件集合，对应list_possales_detail_datas.xml中的控件
     */
    public final class Zujian {
        public TextView no;
        public TextView type;
        public TextView vip;
        public TextView goods_code;
        public TextView goods_name;
        public TextView unit;
        public TextView color;
        public TextView size;
        public TextView quantity;
        public TextView price;
        public TextView discount_rate;
        public TextView discount;
        public TextView amount;
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
            convertView = layoutInflater.inflate(R.layout.list_possales_detail_datas, null);
            zujian.no = (TextView) convertView.findViewById(R.id.tv_no);
            zujian.type = (TextView) convertView.findViewById(R.id.tv_type);
            zujian.vip = (TextView) convertView.findViewById(R.id.tv_vip);
            zujian.goods_code = (TextView) convertView.findViewById(R.id.tv_goods_code);
            zujian.goods_name = (TextView) convertView.findViewById(R.id.tv_goods_name);
            zujian.unit = (TextView) convertView.findViewById(R.id.tv_unit);
            zujian.color = (TextView) convertView.findViewById(R.id.tv_color);
            zujian.size = (TextView) convertView.findViewById(R.id.tv_size);
            zujian.quantity = (TextView) convertView.findViewById(R.id.tv_quantity);
            zujian.price = (TextView) convertView.findViewById(R.id.tv_price);
            zujian.discount_rate = (TextView) convertView.findViewById(R.id.tv_discount_rate);
            zujian.discount = (TextView) convertView.findViewById(R.id.tv_discount);
            zujian.amount = (TextView) convertView.findViewById(R.id.tv_amount);
            convertView.setTag(zujian);
        } else {
            zujian = (Zujian) convertView.getTag();
        }
        // 绑定数据
        zujian.no.setText(String.valueOf(data.get(position).get("No")));
        zujian.type.setText(String.valueOf(data.get(position).get("Type")));
        zujian.vip.setText(String.valueOf(data.get(position).get("VIPCode")));
        zujian.goods_code.setText(String.valueOf(data.get(position).get("Code")));
        zujian.goods_name.setText(String.valueOf(data.get(position).get("Name")));
        zujian.unit.setText(String.valueOf(data.get(position).get("Unit")));
        zujian.color.setText(String.valueOf(data.get(position).get("Color")));
        zujian.size.setText(String.valueOf(data.get(position).get("Size")));
        zujian.quantity.setText(String.valueOf(data.get(position).get("Quantity")));
        zujian.price.setText(String.valueOf(data.get(position).get("UnitPrice")));
        zujian.discount_rate.setText(String.valueOf(data.get(position).get("DiscountRate")));
        zujian.discount.setText(String.valueOf(data.get(position).get("Discount")));
        zujian.amount.setText(String.valueOf(data.get(position).get("Amount")));
        return convertView;
    }

}
