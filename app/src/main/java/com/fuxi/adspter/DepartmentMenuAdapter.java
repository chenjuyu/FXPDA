package com.fuxi.adspter;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fuxi.main.R;

import java.util.List;
import java.util.Map;

public class DepartmentMenuAdapter extends BaseAdapter {
    private List<Map<String, Object>> data;
    private LayoutInflater layoutInflater;
    private Context context;

    public DepartmentMenuAdapter(Context context, List<Map<String, Object>> data){
        this.context =context;
        this.data=data;
        this.layoutInflater=LayoutInflater.from(context);

    }    /**
     * 组件集合，对应list_storage_lock_detail.xml中的控件
     */
    public final class Zujian {
        public TextView tvname;

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
            convertView = layoutInflater.inflate(R.layout.popup_content_item_layout, null);
           // convertView.setLayoutParams(new LinearLayout.LayoutParams(120,50));
            zujian.tvname = (TextView) convertView.findViewById(R.id.menu_item1);
            zujian.tvname.setTextColor(context.getResources().getColorStateList(R.color.icon_blue));
            LinearLayout.LayoutParams layoutParams= new LinearLayout.LayoutParams(120, 80);
            layoutParams.gravity= Gravity.CENTER;
            zujian.tvname.setLayoutParams(layoutParams);
            convertView.setTag(zujian);
        } else {
            zujian = (Zujian) convertView.getTag();
        }
        // 绑定数据
      /*  String MobilePhone="";
        String UsableDepositAmount ="";
        if(data.get(position).get("MobilePhone") !=null && !"null".equals(data.get(position).get("MobilePhone"))){
            MobilePhone =String.valueOf(data.get(position).get("MobilePhone"));
        }
        if(data.get(position).get("UsableDepositAmount") !=null && !"null".equals(data.get(position).get("UsableDepositAmount"))){
            UsableDepositAmount =String.valueOf(data.get(position).get("UsableDepositAmount"));
        } */

        zujian.tvname.setText(String.valueOf(data.get(position).get("Name")));


        return convertView;
    }
}
