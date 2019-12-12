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
 * Title: PossalesAdspter Description: 零售销售单查询ListView适配器
 * 
 * @author LYJ
 * 
 */
public class PossalesAdspter extends BaseAdapter {

    private List<Map<String, Object>> data;
    private LayoutInflater layoutInflater;
    private Context context;

    public PossalesAdspter(Context context, List<Map<String, Object>> data) {
        this.context = context;
        this.data = data;
        this.layoutInflater = LayoutInflater.from(context);
    }

    /**
     * 组件集合，对应list_possales_datas.xml中的控件
     */
    public final class Zujian {
        public TextView no;
        public TextView date;
        public TextView vip;
        public TextView employee;
        public TextView quantitySum;
        public TextView amountSum;
        public TextView factAmt;
        public TextView memo;
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
            convertView = layoutInflater.inflate(R.layout.list_possales_datas, null);
            zujian.no = (TextView) convertView.findViewById(R.id.tv_no);
            zujian.date = (TextView) convertView.findViewById(R.id.tv_date);
            zujian.vip = (TextView) convertView.findViewById(R.id.tv_vip);
            zujian.employee = (TextView) convertView.findViewById(R.id.tv_employee);
            zujian.quantitySum = (TextView) convertView.findViewById(R.id.tv_quantity_sum);
            zujian.amountSum = (TextView) convertView.findViewById(R.id.tv_amount_sum);
            zujian.factAmt = (TextView) convertView.findViewById(R.id.tv_fact_amt);
            zujian.memo = (TextView) convertView.findViewById(R.id.tv_memo);
            convertView.setTag(zujian);
        } else {
            zujian = (Zujian) convertView.getTag();
        }
        // 绑定数据
        zujian.no.setText(String.valueOf(data.get(position).get("No")));
        zujian.date.setText(String.valueOf(data.get(position).get("Date")));
        zujian.vip.setText(String.valueOf(data.get(position).get("VIPCode")));
        zujian.employee.setText(String.valueOf(data.get(position).get("Employee")));
        zujian.quantitySum.setText(String.valueOf(data.get(position).get("QuantitySum")));
        zujian.amountSum.setText(String.valueOf(data.get(position).get("AmountSum")));
        zujian.factAmt.setText(String.valueOf(data.get(position).get("FactAmt")));
        zujian.memo.setText(String.valueOf(data.get(position).get("Memo")));
        return convertView;
    }

}
