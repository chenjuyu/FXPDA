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
 * Title: OPMReportingSubTypeDatasAdspter Description: 订货会系统报表查询子类别查询ListView适配器
 * 
 * @author LYJ
 * 
 */
public class OPMReportingSubTypeDatasAdspter extends BaseAdapter {

    private List<Map<String, Object>> data;
    private LayoutInflater layoutInflater;
    private Context context;

    public OPMReportingSubTypeDatasAdspter(Context context, List<Map<String, Object>> data) {
        this.context = context;
        this.data = data;
        this.layoutInflater = LayoutInflater.from(context);
    }

    /**
     * 组件集合，对应list_opm_reporting_sub_type_datas.xml中的控件
     */
    public final class Zujian {
        public TextView subType;
        public TextView goodsQuantity;
        public TextView colorQuantity;
        public TextView quantity;
        public TextView qtyRate;
        public TextView amount;
        public TextView amtRate;
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
            convertView = layoutInflater.inflate(R.layout.list_opm_reporting_sub_type_datas, null);
            zujian.subType = (TextView) convertView.findViewById(R.id.tv_sub_type);
            zujian.goodsQuantity = (TextView) convertView.findViewById(R.id.tv_goods_quantity);
            zujian.colorQuantity = (TextView) convertView.findViewById(R.id.tv_color_quantity);
            zujian.quantity = (TextView) convertView.findViewById(R.id.tv_quantity);
            zujian.qtyRate = (TextView) convertView.findViewById(R.id.tv_qty_rate);
            zujian.amount = (TextView) convertView.findViewById(R.id.tv_amount);
            zujian.amtRate = (TextView) convertView.findViewById(R.id.tv_amt_rate);
            convertView.setTag(zujian);
        } else {
            zujian = (Zujian) convertView.getTag();
        }
        // 绑定数据
        zujian.subType.setText(String.valueOf(data.get(position).get("SubType")));
        zujian.goodsQuantity.setText(String.valueOf(data.get(position).get("Goods")));
        zujian.colorQuantity.setText(String.valueOf(data.get(position).get("Color")));
        zujian.quantity.setText(String.valueOf(data.get(position).get("Quantity")));
        zujian.qtyRate.setText(String.valueOf(data.get(position).get("Qty_Rate")));
        zujian.amount.setText(String.valueOf(data.get(position).get("Amount")));
        zujian.amtRate.setText(String.valueOf(data.get(position).get("Amt_Rate")));
        return convertView;
    }

}
