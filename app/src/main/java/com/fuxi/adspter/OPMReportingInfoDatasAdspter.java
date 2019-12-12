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
 * Title: OPMReportingInfoDatasAdspter Description: 订货会系统报表查询货品查询ListView适配器
 * 
 * @author LYJ
 * 
 */
public class OPMReportingInfoDatasAdspter extends BaseAdapter {

    private List<Map<String, Object>> data;
    private LayoutInflater layoutInflater;
    private Context context;

    public OPMReportingInfoDatasAdspter(Context context, List<Map<String, Object>> data) {
        this.context = context;
        this.data = data;
        this.layoutInflater = LayoutInflater.from(context);
    }

    /**
     * 组件集合，对应list_opm_reporting_info_datas.xml中的控件
     */
    public final class Zujian {
        public TextView color;
        public TextView quantity;
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
            convertView = layoutInflater.inflate(R.layout.list_opm_reporting_info_datas, null);
            zujian.color = (TextView) convertView.findViewById(R.id.tv_color);
            zujian.quantity = (TextView) convertView.findViewById(R.id.tv_quantity);
            zujian.amount = (TextView) convertView.findViewById(R.id.tv_amount);
            convertView.setTag(zujian);
        } else {
            zujian = (Zujian) convertView.getTag();
        }
        // 绑定数据
        zujian.color.setText(String.valueOf(data.get(position).get("Color")));
        zujian.quantity.setText(String.valueOf(data.get(position).get("Qty")));
        zujian.amount.setText(String.valueOf(data.get(position).get("Amt")));
        return convertView;
    }

}
