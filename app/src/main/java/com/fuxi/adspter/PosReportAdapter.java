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

import org.w3c.dom.Text;

import java.util.List;
import java.util.Map;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

public class PosReportAdapter extends BaseAdapter {

    private  Context context;
    private LayoutInflater layoutInflater;
    private List<Map<String,Object>> dataList;

   public PosReportAdapter(Context context,List<Map<String,Object>> dataList){
        this.context=context;
        this.dataList =dataList;
        this.layoutInflater =LayoutInflater.from(context);
    }

    public class ZuJian{
       public ImageView ivpic;
       public TextView tvcode;
       public TextView tvcolor;
       public TextView tvsize;
       public TextView tvAmt;
       public TextView tvunitprice;
       public TextView tvqty;
       public TextView tvdiscount;
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
           convertView =layoutInflater.inflate(R.layout.activity_posreport_item,null);
           odd =new ZuJian();
           odd.ivpic =(ImageView) convertView.findViewById(R.id.ivpic);
           odd.tvAmt =(TextView) convertView.findViewById(R.id.tv_Amt);
           odd.tvcode =(TextView)convertView.findViewById(R.id.tv_code);
           odd.tvcolor =(TextView) convertView.findViewById(R.id.tv_color);
           odd.tvsize =(TextView) convertView.findViewById(R.id.tv_size);
           odd.tvunitprice=(TextView)convertView.findViewById(R.id.tv_unitprice);
           odd.tvqty =(TextView) convertView.findViewById(R.id.tv_qty);
           odd.tvdiscount=(TextView) convertView.findViewById(R.id.tv_discount);
           convertView.setTag(odd);
       }else{
         odd=(ZuJian) convertView.getTag();
       }
        RequestOptions options = new RequestOptions();
        options.override(100, 100); //设置加载的图片大小
        Glide.with(context)
                .load("https://cdn2.jianshu.io/assets/default_avatar/2-9636b13945b9ccf345bc98d0d81074eb.jpg")//图片的地址
                //.placeholder(R.drawable.bar_bg)//图片加载出来前，显示的图片
                //.error(R.mipmap.unfind)//图片加载失败后，显示的图片
                .apply(options)
                .apply(bitmapTransform(new RoundedCornersTransformation(10, 0, RoundedCornersTransformation.CornerType.ALL)))
                .into(odd.ivpic);
         odd.tvcode.setText(String.valueOf(dataList.get(position).get("Code")));
         odd.tvcolor.setText(String.valueOf(dataList.get(position).get("Color")));
         odd.tvsize.setText(String.valueOf(dataList.get(position).get("Size")));
         odd.tvqty.setText(String.valueOf(dataList.get(position).get("Quantity")));
         odd.tvunitprice.setText(String.valueOf(dataList.get(position).get("UnitPrice")));
         odd.tvAmt.setText(String.valueOf(dataList.get(position).get("Amount")));
         odd.tvdiscount.setText(String.valueOf(dataList.get(position).get("Discount")));
        return convertView;
    }
}
