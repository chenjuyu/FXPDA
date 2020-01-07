package com.fuxi.adspter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.fuxi.main.R;

import java.util.List;
import java.util.Map;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

public class PosReportGroupAdapter extends BaseAdapter {

    private  Context context;
    private LayoutInflater layoutInflater;
    private List<Map<String,Object>> dataList;
    private  String groupstr;

   public PosReportGroupAdapter(Context context, List<Map<String,Object>> dataList,String groupstr){
        this.context=context;
        this.dataList =dataList;
        this.layoutInflater =LayoutInflater.from(context);
        this.groupstr=groupstr;
    }

    public class ZuJian{
       public TextView tvcodeTitle;
       public TextView tvcode;
       public TextView tvAmt;
       public TextView tvqty;
       public TextView tvML;
        public TextView tvMLLV;
       public TextView costTitle;
       public TextView costAmt;
    }

  //刷新
    public  void refresh(){
       notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return dataList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
       ZuJian odd;
       if(convertView ==null){
           convertView =layoutInflater.inflate(R.layout.activity_posreport_group,null);
           odd =new ZuJian();
           odd.tvAmt =(TextView) convertView.findViewById(R.id.tv_Amt);
           odd.tvcode =(TextView)convertView.findViewById(R.id.tv_code);
           odd.tvcodeTitle =(TextView) convertView.findViewById(R.id.tv_codeTitle);
           odd.costTitle =(TextView) convertView.findViewById(R.id.costTitle);
           odd.costAmt=(TextView)convertView.findViewById(R.id.costAmt);
           odd.tvqty =(TextView) convertView.findViewById(R.id.tv_qty);
           odd.tvML=(TextView) convertView.findViewById(R.id.tv_ML);
           odd.tvMLLV =(TextView)   convertView.findViewById(R.id.tv_MLLV);
           convertView.setTag(odd);
       }else{
         odd=(ZuJian) convertView.getTag();
       }

         odd.tvcode.setText(String.valueOf(dataList.get(position).get("Code")));

         odd.tvcodeTitle.setText(groupstr+": ");


        odd.costTitle.setText("采购成本");
         odd.tvqty.setText(String.valueOf(dataList.get(position).get("Quantity")));
         odd.tvML.setText(String.valueOf(dataList.get(position).get("ML")));
         odd.tvAmt.setText(String.valueOf(dataList.get(position).get("Amount")));
         odd.tvMLLV.setText(String.valueOf(dataList.get(position).get("MLLV")));
         odd.costAmt.setText(String.valueOf(dataList.get(position).get("CostAmt")));
        return convertView;
    }
}
