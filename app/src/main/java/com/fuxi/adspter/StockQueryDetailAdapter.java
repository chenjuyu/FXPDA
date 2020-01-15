package com.fuxi.adspter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fuxi.main.R;

import java.util.List;
import java.util.Map;

public class StockQueryDetailAdapter extends BaseAdapter {

    private List<Map<String,Object>> data;
    private Context context;
    private LayoutInflater layoutInflater;

    public StockQueryDetailAdapter(Context context,List<Map<String,Object>> data){
        this.context=context;
        this.data=data;
        this.layoutInflater=LayoutInflater.from(context);
    }

    public final class Zujian{
        public TextView tvdepartment;
        public TextView tvcolor;
        public TextView tvdeptqty;
        public TextView tvcolorqty;
        public LinearLayout llsizetitle;
        public LinearLayout llsizeqty;

    }


    public void refresh(List<Map<String,Object>> data){
        notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Zujian odd=null;
        if(convertView ==null){
            odd =new Zujian();
            convertView =layoutInflater.inflate(R.layout.activity_stock_detail_item,null);
            odd.tvdepartment=(TextView) convertView.findViewById(R.id.tv_department);
            odd.tvcolor=(TextView) convertView.findViewById(R.id.tv_color);
            odd.tvdeptqty=(TextView) convertView.findViewById(R.id.tv_deptqty);
            odd.tvcolorqty=(TextView) convertView.findViewById(R.id.tv_colorqty);
            odd.llsizetitle=(LinearLayout) convertView.findViewById(R.id.ll_sizetitle);
            odd.llsizeqty=(LinearLayout) convertView.findViewById(R.id.ll_sizeqty);

            LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(10,0,0,0);
            layoutParams.weight=1;
            final List<String> sizetitle=(List<String>) data.get(position).get("sizetitle");
            for(int i=0;i<sizetitle.size();i++) {
                String  map =sizetitle.get(i);
                TextView textView = new TextView(context);
                textView.setId(i);
                textView.setText(map);
                textView.setLayoutParams(layoutParams);
                odd.llsizetitle.addView(textView);
            }
            //数据
            List<Integer> sizeqty=(List<Integer>)  data.get(position).get("sizeqty");
            for(int j=0;j<sizeqty.size();j++) {
                int map =sizeqty.get(j);
               // LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
             //   layoutParams.setMargins(5,0,0,0);
                TextView textView1 = new TextView(context);
                //textView.setId(i);
                textView1.setText(String.valueOf(map));
                textView1.setLayoutParams(layoutParams);
                odd.llsizeqty.addView(textView1);
            }

            //要在settag前，不然会有重复生成的问题
            convertView.setTag(odd);
        }else{
            odd=(Zujian)convertView.getTag();
        }

        //邦定数据
       //

       try {
           odd.tvdepartment.setText(String.valueOf(data.get(position).get("Department")));
           odd.tvcolor.setText(String.valueOf(data.get(position).get("Color")));
           odd.tvdeptqty.setText(String.valueOf(data.get(position).get("Quantity")));
           odd.tvcolorqty.setText(String.valueOf(data.get(position).get("ColorQty")));

       }catch (Exception e){
           Toast.makeText(context,e.getMessage(),Toast.LENGTH_SHORT).show();
       }



        return convertView;
    }
}


