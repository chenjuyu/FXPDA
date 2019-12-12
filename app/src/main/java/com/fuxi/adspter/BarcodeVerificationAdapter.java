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
 * Title: BarcodeVerificationAdapter Description: 条码校验ListView适配器
 * 
 * @author LYJ
 * 
 */
public class BarcodeVerificationAdapter extends BaseAdapter {

    private List<Map<String, Object>> data;
    private LayoutInflater layoutInflater;
    private Context context;

    public BarcodeVerificationAdapter(Context context, List<Map<String, Object>> data) {
        this.context = context;
        this.data = data;
        this.layoutInflater = LayoutInflater.from(context);
    }

    /**
     * Title: Odd Description: 组件集合，对应list_barcode_verification.xml中的控件
     */
    public final class Odd {
        public TextView goods;
        public TextView color;
        public TextView size;
        public TextView qty;
        public TextView docQty;
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
            convertView = layoutInflater.inflate(R.layout.list_barcode_verification, null);
            odd.goods = (TextView) convertView.findViewById(R.id.goods);
            odd.color = (TextView) convertView.findViewById(R.id.color);
            odd.size = (TextView) convertView.findViewById(R.id.size);
            odd.docQty = (TextView) convertView.findViewById(R.id.docQty);
            odd.qty = (TextView) convertView.findViewById(R.id.qty);
            convertView.setTag(odd);
        } else {
            odd = (Odd) convertView.getTag();
        }
        // 绑定数据
        String goodsCode = String.valueOf(data.get(position).get("GoodsCode"));
        int length = Tools.length(goodsCode);
        if (length > 26) {
            odd.goods.setTextSize(13);
        } else {
            odd.goods.setTextSize(17);
        }
        odd.goods.setText(goodsCode);
        odd.color.setText(String.valueOf(data.get(position).get("Color")));
        odd.size.setText(String.valueOf(data.get(position).get("Size")));
        String quantity = String.valueOf(data.get(position).get("Quantity"));
        String qty = String.valueOf(data.get(position).get("Qty"));
        if (qty == null || qty.isEmpty() || "null".equalsIgnoreCase(qty)) {
            qty = "0";
        }
        odd.docQty.setText(quantity);
        odd.qty.setText(qty);
        // 判断校验数量,并改变颜色
        Activity activity = (Activity) context;
        if (Integer.parseInt(qty) == Integer.parseInt(quantity)) {
            convertView.setBackgroundColor(activity.getResources().getColor(R.color.green));
            odd.goods.setTextColor(Color.WHITE);
            odd.color.setTextColor(Color.WHITE);
            odd.size.setTextColor(Color.WHITE);
            odd.qty.setTextColor(Color.WHITE);
            odd.docQty.setTextColor(Color.WHITE);
        } else if (Integer.parseInt(quantity) == 0 && Integer.parseInt(qty) > 0) {
            convertView.setBackgroundColor(activity.getResources().getColor(R.color.red));
            odd.goods.setTextColor(Color.WHITE);
            odd.color.setTextColor(Color.WHITE);
            odd.size.setTextColor(Color.WHITE);
            odd.qty.setTextColor(Color.WHITE);
            odd.docQty.setTextColor(Color.WHITE);
        } else if (Integer.parseInt(qty) > Integer.parseInt(quantity)) {
            convertView.setBackgroundColor(activity.getResources().getColor(R.color.pink));
            odd.goods.setTextColor(Color.WHITE);
            odd.color.setTextColor(Color.WHITE);
            odd.size.setTextColor(Color.WHITE);
            odd.qty.setTextColor(Color.WHITE);
            odd.docQty.setTextColor(Color.WHITE);
        } else {
            convertView.setBackgroundColor(Color.WHITE);
            odd.goods.setTextColor(activity.getResources().getColor(R.color.list_text_color));
            odd.color.setTextColor(activity.getResources().getColor(R.color.list_text_color));
            odd.size.setTextColor(activity.getResources().getColor(R.color.list_text_color));
            odd.qty.setTextColor(activity.getResources().getColor(R.color.list_text_color));
            odd.docQty.setTextColor(activity.getResources().getColor(R.color.list_text_color));
        }
        return convertView;
    }

}
