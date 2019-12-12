package com.fuxi.task;

import java.io.File;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import com.fuxi.util.FormatImagesUtil;
import com.fuxi.vo.BarCode;

/**
 * Title: ImageTask Description: 货品图片下载任务
 * 
 * @author LJ
 * 
 */
public class ImageTask extends AsyncTask<Object, Integer, String> {

    File imageFile;
    String url;
    BarCode barcode;
    ImageView iv_goods;

    public ImageTask(File image, String url, BarCode barcode, ImageView iv) {
        super();
        imageFile = image;
        this.url = url;
        this.barcode = barcode;
        this.iv_goods = iv;
    }

    public String doInBackground(Object... params) {
        try {
            FormatImagesUtil.download(url, 100, 100, barcode.getGoodscode() + ".jpg", imageFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            // doInBackground(null);
        }
        return null;
    }

    @Override
    public void onPostExecute(String result) {
        if (imageFile.exists()) {
            Bitmap bm = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
            iv_goods.setImageBitmap(bm);
        }
    }

}
