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
 * Title: SalesTicketAdapter Description: 销售小票ListView适配器
 * 
 * @author LYJ
 * 
 */
public class SalesTicketAdapter extends BaseAdapter {

    private List<Map<String, Object>> data;
    private LayoutInflater layoutInflater;
    private Context context;

    public SalesTicketAdapter(Context context, List<Map<String, Object>> data) {
        this.context = context;
        this.data = data;
        this.layoutInflater = LayoutInflater.from(context);
    }

    /**
     * 组件集合，对应activity_sales_ticket_odd.xml中的控件
     */
    public final class Odd {
        public TextView product;
        public TextView color;
        public TextView size;
        public TextView qty;
        public TextView discount;
        public TextView price;
        public TextView amount;
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
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Odd odd = null;
        if (convertView == null) {
            odd = new Odd();
            // 获得组件，实例化组件
            convertView = layoutInflater.inflate(R.layout.activity_sales_ticket_odd, null);
            odd.product = (TextView) convertView.findViewById(R.id.product);
            odd.color = (TextView) convertView.findViewById(R.id.color);
            odd.size = (TextView) convertView.findViewById(R.id.size);
            odd.qty = (TextView) convertView.findViewById(R.id.qty);
            odd.discount = (TextView) convertView.findViewById(R.id.discount);
            odd.price = (TextView) convertView.findViewById(R.id.price);
            odd.amount = (TextView) convertView.findViewById(R.id.amount);
            convertView.setTag(odd);
        } else {
            odd = (Odd) convertView.getTag();
        }
        // 绑定数据
        // 显示货品编码(控制长度)
        String goodsCode = String.valueOf(data.get(position).get("GoodsCode"));
        int length = Tools.length(goodsCode);
        if (length > 26) {
            odd.product.setTextSize(13);
        } else {
            odd.product.setTextSize(17);
        }
        odd.product.setText(goodsCode);
        odd.color.setText(String.valueOf(data.get(position).get("Color")));
        odd.size.setText(String.valueOf(data.get(position).get("Size")));
        odd.discount.setText(String.valueOf(data.get(position).get("DiscountRate")));
        // 单价
        String price = String.valueOf(data.get(position).get("UnitPrice"));
        int leng = Tools.length(price);
        if (leng > 5) {
            odd.price.setTextSize(17 - (leng - 5));
        } else {
            odd.price.setTextSize(17);
        }
        odd.price.setText(price);
        // 金额
        String amount = String.valueOf(data.get(position).get("Amount"));
        int len = Tools.length(amount);
        if (len > 5) {
            odd.amount.setTextSize(17 - (len - 5));
        } else {
            odd.amount.setTextSize(17);
        }
        odd.amount.setText(amount);
        odd.qty.setText(String.valueOf(data.get(position).get("Quantity")));
        return convertView;
    }

}
