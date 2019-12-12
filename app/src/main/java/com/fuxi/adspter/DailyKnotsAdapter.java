package com.fuxi.adspter;

import java.util.List;
import java.util.Map;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.fuxi.main.R;
import com.fuxi.util.Tools;

/**
 * Title: SalesOddAdapter Description: 有单价,折扣,折扣价显示的ListView适配器
 * 
 * @author LYJ
 * 
 */
public class DailyKnotsAdapter extends BaseAdapter {

    private List<Map<String, Object>> data;
    private LayoutInflater layoutInflater;
    private Context context;

    public DailyKnotsAdapter(Context context, List<Map<String, Object>> data) {
        this.context = context;
        this.data = data;
        this.layoutInflater = LayoutInflater.from(context);
    }

    /**
     * 组件集合，对应activity_daily_knots_odd.xml中的控件
     */
    public final class Odd {
        public TextView product;
        public TextView color;
        public TextView size;
        public TextView qty;
        public TextView price;
        public TextView discountRate;
        public TextView discount;
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
            convertView = layoutInflater.inflate(R.layout.activity_daily_knots_odd, null);
            odd.product = (TextView) convertView.findViewById(R.id.product);
            odd.color = (TextView) convertView.findViewById(R.id.color);
            odd.size = (TextView) convertView.findViewById(R.id.size);
            odd.qty = (TextView) convertView.findViewById(R.id.qty);
            odd.price = (TextView) convertView.findViewById(R.id.price);
            odd.discountRate = (TextView) convertView.findViewById(R.id.discountRate);
            odd.discount = (TextView) convertView.findViewById(R.id.discount);
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
        // 折扣显示
        String discountRate = String.valueOf(data.get(position).get("DiscountRate"));
        if (discountRate == null || "".equals(discountRate) || "null".equals(discountRate) || "10".equals(discountRate)) {
            odd.discountRate.setText("");
        } else {
            odd.discountRate.setText(discountRate);
        }
        // 单价
        String price = String.valueOf(data.get(position).get("UnitPrice"));
        int leng1 = Tools.length(price);
        if (leng1 > 5) {
            odd.price.setTextSize(17 - (leng1 - 5));
        } else {
            odd.price.setTextSize(17);
        }
        odd.price.setText(price);
        // 折扣额
        String discountPrice = String.valueOf(data.get(position).get("Discount"));
        int leng2 = Tools.length(discountPrice);
        if (leng2 > 5) {
            odd.price.setTextSize(17 - (leng2 - 5));
        } else {
            odd.price.setTextSize(17);
        }
        odd.discount.setText(discountPrice);
        // 金额
        String amount = String.valueOf(data.get(position).get("Amount"));
        int leng3 = Tools.length(amount);
        if (leng3 > 5) {
            odd.amount.setTextSize(17 - (leng3 - 5));
        } else {
            odd.amount.setTextSize(17);
        }
        odd.amount.setText(amount);
        odd.qty.setText(String.valueOf(data.get(position).get("Quantity")));
        return convertView;
    }

}
