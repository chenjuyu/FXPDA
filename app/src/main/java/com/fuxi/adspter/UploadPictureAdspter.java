package com.fuxi.adspter;

import java.util.List;
import java.util.Map;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import com.fuxi.main.R;

/**
 * Title: UploadPictureAdspter Description:图片上传ListView适配器
 * 
 * @author LYJ
 * 
 */
public class UploadPictureAdspter extends BaseAdapter {

    private List<Map<String, Object>> data;
    private LayoutInflater layoutInflater;
    private Context context;

    public UploadPictureAdspter(Context context, List<Map<String, Object>> data) {
        this.context = context;
        this.data = data;
        this.layoutInflater = LayoutInflater.from(context);
    }

    /**
     * 组件集合，对应list_upload_picture.xml中的控件
     */
    public final class Zujian {
        public TextView code;
        public TextView date;
        public CheckBox check;
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
            convertView = layoutInflater.inflate(R.layout.list_upload_picture, null);
            zujian.code = (TextView) convertView.findViewById(R.id.code);
            zujian.date = (TextView) convertView.findViewById(R.id.date);
            zujian.check = (CheckBox) convertView.findViewById(R.id.check);
            convertView.setTag(zujian);
        } else {
            zujian = (Zujian) convertView.getTag();
        }
        // 绑定数据
        zujian.code.setText(String.valueOf(data.get(position).get("GoodsCode")));
        zujian.date.setText(String.valueOf(data.get(position).get("Date")));
        zujian.check.setChecked(Boolean.valueOf(String.valueOf(data.get(position).get("Select"))));
        return convertView;
    }

}
