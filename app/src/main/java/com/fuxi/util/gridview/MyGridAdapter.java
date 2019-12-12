package com.fuxi.util.gridview;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.fuxi.activity.BaseInformationActivity;
import com.fuxi.activity.MainActivity;
import com.fuxi.activity.PosSalesManagementActivity;
import com.fuxi.activity.PositionManagementActivity;
import com.fuxi.activity.PurchaseManagementActivity;
import com.fuxi.activity.SalesManagementActivity;
import com.fuxi.activity.WarehouseManagementActivity;
import com.fuxi.main.R;
import com.fuxi.util.Logger;
import com.fuxi.widget.FontTextView;

/**
 * GridView的Adapter
 */
public class MyGridAdapter extends BaseAdapter {
    private Context mContext;
    private String[] img_text; // 图片标题
    private String[] img_activity; // 图片控件id
    private Integer[] imgs; // 图片

    public MyGridAdapter(String[] img_text, String[] img_activity, Integer[] imgs, Context mContext) {
        super();
        this.img_text = img_text;
        this.imgs = imgs;
        this.img_activity = img_activity;
        this.mContext = mContext;
    }

    public MyGridAdapter(Context mContext) {
        super();
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        return img_text.length;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.grid_item, parent, false);
        }
        RelativeLayout rl_main = BaseViewHolder.get(convertView, R.id.rl_main);
        TextView tv = BaseViewHolder.get(convertView, R.id.tv_item);
        FontTextView iv = BaseViewHolder.get(convertView, R.id.iv_item);
        iv.setText(imgs[position]);
        tv.setText(img_text[position]);
        try {
            Activity activity = (Activity) mContext;
            if (activity instanceof MainActivity) {
                rl_main.setBackgroundColor(Color.WHITE);
                if ("零售管理".equals(img_text[position])) {
                    // TextPaint paint = iv.getPaint();
                    // paint.setFakeBoldText(true);
                    iv.setTextSize(44);
                }
                if ("设置中心".equals(img_text[position])) {
                    // TextPaint paint = iv.getPaint();
                    // paint.setFakeBoldText(true);
                    iv.setTextSize(38);
                }
                if ("订货中心".equals(img_text[position])) {
                    // TextPaint paint = iv.getPaint();
                    // paint.setFakeBoldText(true);
                    iv.setTextSize(38);
                }
                if (position == 3 || position == 7 || position == 9) {
                    iv.setTextColor(activity.getResources().getColor(R.color.icon_blue));
                } else if (position == 1 || position == 2 || position == 4 || position == 10 || position == 11 || position == 6) {
                    iv.setTextColor(activity.getResources().getColor(R.color.icon_red));
                }
            }
            if (activity instanceof PositionManagementActivity) {
                if (position == 2 || position == 3 || position == 4 || position == 7) {
                    // TextPaint paint = iv.getPaint();
                    // paint.setFakeBoldText(true);
                    iv.setTextSize(46);
                }
                if (position == 1 || position == 2 || position == 6) {
                    iv.setTextColor(activity.getResources().getColor(R.color.icon_blue));
                } else if (position == 0 || position == 3 || position == 7) {
                    iv.setTextColor(activity.getResources().getColor(R.color.icon_red));
                }
            }
            if (activity instanceof BaseInformationActivity) {
                if (position == 1) {
                    iv.setTextColor(activity.getResources().getColor(R.color.icon_blue));
                } else if (position == 2) {
                    iv.setTextColor(activity.getResources().getColor(R.color.icon_red));
                }
            }
            if (activity instanceof SalesManagementActivity) {
                if (position == 1) {
                    iv.setTextColor(activity.getResources().getColor(R.color.icon_blue));
                } else if (position == 2) {
                    iv.setTextColor(activity.getResources().getColor(R.color.icon_red));
                }
            }
            if (activity instanceof PurchaseManagementActivity) {
                if (position == 0) {
                    iv.setTextColor(activity.getResources().getColor(R.color.icon_blue));
                } else if (position == 1) {
                    iv.setTextColor(activity.getResources().getColor(R.color.icon_red));
                }
            }
            if (activity instanceof WarehouseManagementActivity) {
                if (position == 0 || position == 3 || position == 4) {
                    iv.setTextColor(activity.getResources().getColor(R.color.icon_blue));
                } else if (position == 2 || position == 5) {
                    iv.setTextColor(activity.getResources().getColor(R.color.icon_red));
                }
                if (position == 4 || position == 5 || position == 3) {
                    iv.setTextSize(48);
                }
                if (position == 6) {
                    iv.setTextSize(44);
                }
            }
            if (activity instanceof PosSalesManagementActivity) {
                if (position == 0 || position == 4) {
                    iv.setTextColor(activity.getResources().getColor(R.color.icon_blue));
                } else if (position == 1) {
                    iv.setTextColor(activity.getResources().getColor(R.color.icon_red));
                }
            }
        } catch (Exception e) {
            Logger.e("系统错误", e.getMessage());
        }
        return convertView;
    }

}
