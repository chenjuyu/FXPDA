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

public class StockQueryAdapter extends BaseAdapter {

    private List<Map<String,Object>> data;
    private Context context;
    private LayoutInflater layoutInflater;

    public StockQueryAdapter(Context context,List<Map<String,Object>> data){
        this.context=context;
        this.data=data;
        this.layoutInflater=LayoutInflater.from(context);
    }

    public final class Zujian{
        public ImageView ivimg;
        public TextView tvcode;
        public TextView tvname;
        public TextView tvqty;
        public TextView tvinqty;
        public TextView tvoutqty;
        public TextView tvstockqty;
        public TextView tvpurchaseprice;
        public TextView tvpf;
        public TextView tvretailsales;

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
        if(convertView==null){
            odd =new Zujian();
            convertView =layoutInflater.inflate(R.layout.activity_stock_query_item,null);
            odd.ivimg=(ImageView) convertView.findViewById(R.id.iv_img);
            odd.tvcode =(TextView) convertView.findViewById(R.id.tv_code);
            odd.tvname=(TextView)convertView.findViewById(R.id.tv_name);
            odd.tvqty=(TextView)convertView.findViewById(R.id.tv_qty);
            odd.tvinqty=(TextView) convertView.findViewById(R.id.tv_inqty);
            odd.tvoutqty=(TextView) convertView.findViewById(R.id.tv_outqty);
            odd.tvstockqty=(TextView) convertView.findViewById(R.id.tv_stockqty);
            odd.tvpurchaseprice=(TextView) convertView.findViewById(R.id.tv_purchaseprice);
            odd.tvpf=(TextView)convertView.findViewById(R.id.tv_pf);
            odd.tvretailsales=(TextView)convertView.findViewById(R.id.tv_retailsales);
            convertView.setTag(odd);
        }else{
            odd =(Zujian)convertView.getTag();
        }

        //绑定数据
        RequestOptions options = new RequestOptions();
        options.override(60, 70); //设置加载的图片大小

        if(!data.get(position).get("img").equals("")){
            Glide.with(context)
                    .load(String.valueOf(data.get(position).get("img")))//图片的地址
                    //.placeholder(R.drawable.bar_bg)//图片加载出来前，显示的图片
                    //.error(R.mipmap.unfind)//图片加载失败后，显示的图片
                    .apply(options)
                    .apply(bitmapTransform(new RoundedCornersTransformation(10, 0, RoundedCornersTransformation.CornerType.ALL)))
                    .into(odd.ivimg);
        }else{
            Glide.with(context)
                    .load(R.drawable.a)//图片的地址
                    //.placeholder(R.drawable.bar_bg)//图片加载出来前，显示的图片
                    //.error(R.mipmap.unfind)//图片加载失败后，显示的图片
                    .apply(options)
                    .apply(bitmapTransform(new RoundedCornersTransformation(10, 0, RoundedCornersTransformation.CornerType.ALL)))
                    .into(odd.ivimg);
        }
        odd.tvcode.setText(String.valueOf(data.get(position).get("Code")));
        odd.tvname.setText(String.valueOf(data.get(position).get("Name")));
        odd.tvqty.setText(String.valueOf(data.get(position).get("Quantity")));
        odd.tvinqty.setText(String.valueOf(data.get(position).get("inqty")));
        odd.tvoutqty.setText(String.valueOf(data.get(position).get("outqty")));
        odd.tvstockqty.setText(String.valueOf(data.get(position).get("stockqty")));
        odd.tvpurchaseprice.setText(String.valueOf(data.get(position).get("purchaseprice")));
        odd.tvpf.setText(String.valueOf(data.get(position).get("pfprice")));
        odd.tvretailsales.setText(String.valueOf(data.get(position).get("retailsales")));
        return convertView;
    }
}
