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
 * Title: nvoicingAdspter Description: 进销存查询ListView适配器
 * 
 * @author LYJ
 * 
 */
public class InvoicingAdspter extends BaseAdapter {

    private List<Map<String, Object>> data;
    private LayoutInflater layoutInflater;
    private Context context;

    public InvoicingAdspter(Context context, List<Map<String, Object>> data) {
        this.context = context;
        this.data = data;
        this.layoutInflater = LayoutInflater.from(context);
    }

    /**
     * 组件集合，对应list_invoicing.xml中的控件
     */
    public final class Zujian {
        public TextView department;
        public TextView purchaseCount;
        public TextView salesCount;
        public TextView stockCount;
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
            convertView = layoutInflater.inflate(R.layout.list_invoicing, null);
            zujian.department = (TextView) convertView.findViewById(R.id.tv_department);
            zujian.purchaseCount = (TextView) convertView.findViewById(R.id.tv_purchase_count);
            zujian.salesCount = (TextView) convertView.findViewById(R.id.tv_sales_count);
            zujian.stockCount = (TextView) convertView.findViewById(R.id.tv_stock_count);
            convertView.setTag(zujian);
        } else {
            zujian = (Zujian) convertView.getTag();
        }
        // 绑定数据
        // 显示货品编码(控制长度)
        String goodsCode = String.valueOf(data.get(position).get("Department"));
        int length = Tools.length(goodsCode);
        if (length > 26) {
            zujian.department.setTextSize(13);
        } else {
            zujian.department.setTextSize(17);
        }
        zujian.department.setText(goodsCode);
        zujian.purchaseCount.setText(String.valueOf(data.get(position).get("PurchaseCount")));
        zujian.salesCount.setText(String.valueOf(data.get(position).get("SalesCount")));
        zujian.stockCount.setText(String.valueOf(data.get(position).get("StockCount")));
        return convertView;
    }

}
