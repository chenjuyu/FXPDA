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
 * Title: ErrorAdapter Description: 条码校验错误信息输出ListView适配器
 * 
 * @author LYJ
 * 
 */
public class ErrorAdapter extends BaseAdapter {

    private List<Map<String, Object>> data;
    private LayoutInflater layoutInflater;
    private Context context;

    public ErrorAdapter(Context context, List<Map<String, Object>> data) {
        this.context = context;
        this.data = data;
        this.layoutInflater = LayoutInflater.from(context);
    }

    /**
     * 组件集合，对应list_errors.xml中的控件
     */
    public final class Odd {
        public TextView error;
        public TextView no;
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
            convertView = layoutInflater.inflate(R.layout.list_errors, null);
            odd.error = (TextView) convertView.findViewById(R.id.tv_error);
            odd.no = (TextView) convertView.findViewById(R.id.tv_no);
            convertView.setTag(odd);
        } else {
            odd = (Odd) convertView.getTag();
        }
        // 绑定数据
        odd.no.setText("错误" + (position + 1) + ": ");
        odd.error.setText(String.valueOf(data.get(position).get("Error")));
        return convertView;
    }

}
