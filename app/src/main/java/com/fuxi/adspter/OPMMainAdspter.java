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
 * Title: OPMMainAdspter Description: 订货会系统主界面ListView适配器
 * 
 * @author LYJ
 * 
 */
public class OPMMainAdspter extends BaseAdapter {

    private List<Map<String, Object>> data;
    private LayoutInflater layoutInflater;
    private Context context;

    public OPMMainAdspter(Context context, List<Map<String, Object>> data) {
        this.context = context;
        this.data = data;
        this.layoutInflater = LayoutInflater.from(context);
    }

    /**
     * 组件集合，对应list_opm_main.xml中的控件
     */
    public final class Zujian {
        public TextView goodsCode;
        public TextView goodsName;
        public TextView count;
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
            convertView = layoutInflater.inflate(R.layout.list_opm_main, null);
            zujian.goodsCode = (TextView) convertView.findViewById(R.id.tv_goods_code);
            zujian.goodsName = (TextView) convertView.findViewById(R.id.tv_goods_name);
            zujian.count = (TextView) convertView.findViewById(R.id.tv_count);
            convertView.setTag(zujian);
        } else {
            zujian = (Zujian) convertView.getTag();
        }
        // 绑定数据
        zujian.goodsCode.setText(String.valueOf(data.get(position).get("GoodsCode")));
        zujian.goodsName.setText(String.valueOf(data.get(position).get("GoodsName")));
        zujian.count.setText(String.valueOf(data.get(position).get("Quantity")));
        return convertView;
    }

}
