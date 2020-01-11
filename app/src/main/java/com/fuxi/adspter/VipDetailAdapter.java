package com.fuxi.adspter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.fuxi.main.R;

import java.util.List;
import java.util.Map;

public class VipDetailAdapter extends BaseAdapter {
    private List<Map<String, Object>> data;
    private LayoutInflater layoutInflater;
    private Context context;
    private int disType;//1显示小票记录，2是储值记录
    public VipDetailAdapter(Context context, List<Map<String, Object>> data,int disType){
        this.context =context;
        this.data=data;
        this.disType =disType;
        this.layoutInflater=LayoutInflater.from(context);


    }    /**
     * 组件集合，对应list_storage_lock_detail.xml中的控件
     */
    public final class Zujian {
        public TextView tvno;
        public TextView tvdepartment;
        public TextView tvdate;
        public TextView tvpoint;
        public TextView tvcz;
        public TextView tvmemo;
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
            convertView = layoutInflater.inflate(R.layout.activity_vip_point_item, null);
            zujian.tvno = (TextView) convertView.findViewById(R.id.tv_no);
            zujian.tvdepartment = (TextView) convertView.findViewById(R.id.tv_department);
            zujian.tvdate = (TextView) convertView.findViewById(R.id.tv_date);
            zujian.tvpoint = (TextView) convertView.findViewById(R.id.tv_point);
            zujian.tvcz = (TextView) convertView.findViewById(R.id.tv_cz);
            zujian.tvmemo=(TextView) convertView.findViewById(R.id.tv_memo);
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

        if(disType==2) zujian.tvdepartment.setVisibility(View.VISIBLE);

        zujian.tvno.setText(String.valueOf(data.get(position).get("No")));

        if(disType==2)
        zujian.tvdepartment.setText(String.valueOf(data.get(position).get("Department")));

        zujian.tvdate.setText(String.valueOf(data.get(position).get("Date")));

        if(disType==1)
        zujian.tvpoint.setText(String.valueOf(data.get(position).get("ThisSalesPoint")));

        if(disType==2)
        zujian.tvcz.setText(String.valueOf(data.get(position).get("DepositAmount")));

        zujian.tvmemo.setText(String.valueOf(data.get(position).get("Memo")));
        return convertView;
    }
}
