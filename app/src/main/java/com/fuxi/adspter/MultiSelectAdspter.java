package com.fuxi.adspter;

import java.util.List;
import java.util.Map;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;
import com.fuxi.main.R;
import com.fuxi.widget.FontTextView;

/**
 * Title: MultiSelectAdspter Description: 多颜色尺码录入ListView适配器
 * 
 * @author LYJ
 * 
 */
public class MultiSelectAdspter extends BaseAdapter {

    private List<Map<String, Object>> data;
    private LayoutInflater layoutInflater;
    private Context context;

    public MultiSelectAdspter(Context context, List<Map<String, Object>> data) {
        this.context = context;
        this.data = data;
        this.layoutInflater = LayoutInflater.from(context);
    }

    /**
     * 组件集合，对应list_multi_select.xml中的控件
     */
    public final class Zujian {
        public TextView size;
        public FontTextView reduce;
        public FontTextView add;
        public EditText count;
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
            convertView = layoutInflater.inflate(R.layout.list_multi_select, null);
            zujian.size = (TextView) convertView.findViewById(R.id.tv_size);
            zujian.reduce = (FontTextView) convertView.findViewById(R.id.reduce);
            zujian.add = (FontTextView) convertView.findViewById(R.id.add);
            zujian.count = (EditText) convertView.findViewById(R.id.et_count);
            convertView.setTag(zujian);
        } else {
            zujian = (Zujian) convertView.getTag();
        }
        // 绑定数据
        zujian.size.setText(String.valueOf(data.get(position).get("Name")));
        zujian.count.setText(String.valueOf(data.get(position).get("Quantity")));
        zujian.count.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ((ViewGroup) v.getParent()).setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
                return false;
            }
        });

        convertView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ((ViewGroup) v).setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
                return false;
            }
        });
        return convertView;
    }

}
