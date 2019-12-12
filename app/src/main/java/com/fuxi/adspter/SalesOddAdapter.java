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
 * Title: SalesOddAdapter Description: 有单价显示的ListView适配器
 * 
 * @author LYJ
 * 
 */
public class SalesOddAdapter extends BaseAdapter {

    private List<Map<String, Object>> data;
    private LayoutInflater layoutInflater;
    private Context context;

    public SalesOddAdapter(Context context, List<Map<String, Object>> data) {
        this.context = context;
        this.data = data;
        this.layoutInflater = LayoutInflater.from(context);
    }

    /**
     * 组件集合，对应activity_sales_odd.xml中的控件
     */
    public final class Odd {
        public TextView product;
        public TextView color;
        public TextView size;
        public TextView qty;
        public TextView price;
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
            convertView = layoutInflater.inflate(R.layout.activity_sales_odd, null);
            odd.product = (TextView) convertView.findViewById(R.id.product);
            odd.color = (TextView) convertView.findViewById(R.id.color);
            odd.size = (TextView) convertView.findViewById(R.id.size);
            odd.qty = (TextView) convertView.findViewById(R.id.qty);
            odd.price = (TextView) convertView.findViewById(R.id.price);
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
        // 单价
        String price = String.valueOf(data.get(position).get("DiscountPrice"));
        int leng1 = Tools.length(price);
        if (leng1 > 5) {
            odd.price.setTextSize(17 - (leng1 - 5));
        } else {
            odd.price.setTextSize(17);
        }
        odd.price.setText(price);
        odd.qty.setText(String.valueOf(data.get(position).get("Quantity")));
        return convertView;
    }

    /**
     * 根据控件宽度自动调整字体大小
     * 
     * @param text
     * @param textViewWidth
     * @param textView
     */
    private void refitText(String text, int textViewWidth, TextView textView) {
        if (text == null || textViewWidth <= 0)
            return;
        Paint mTextPaint = new Paint();
        mTextPaint.set(textView.getPaint());
        int availableTextViewWidth = textView.getWidth() - textView.getPaddingLeft() - textView.getPaddingRight();
        float[] charsWidthArr = new float[text.length()];
        Rect boundsRect = new Rect();
        mTextPaint.getTextBounds(text, 0, text.length(), boundsRect);
        int textWidth = boundsRect.width();
        float mTextSize = textView.getTextSize();
        while (textWidth > availableTextViewWidth) {
            mTextSize -= 1;
            mTextPaint.setTextSize(mTextSize);
            textWidth = mTextPaint.getTextWidths(text, charsWidthArr);
        }
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
    }

}
