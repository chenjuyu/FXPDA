package com.fuxi.util;

// import java.awt.Graphics2D;
// import java.awt.RenderingHints;
// import java.awt.geom.AffineTransform;
// import java.awt.image.BufferedImage;
// import java.awt.image.ColorModel;
// import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class FormatImagesUtil {

    /**
     * 根据网络路径下载图片并生成固定大小的图片
     * 
     * @param urlString 网络路径
     * @param targetWidth 图片目标宽度
     * @param targetHeight 图片目标高度
     * @param filename 图片名称
     * @param savePath 保存的路径
     * @throws Exception
     */
    public static void download(String urlString, int targetWidth, int targetHeight, String filename, String savePath) throws Exception {
        File file = new File(savePath);
        if (file.exists()) {
            file.delete();
        }
        if (!file.exists()) {
            file.createNewFile();
        }
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setReadTimeout(20000);
        if (conn.getResponseCode() == 200) {
            InputStream fis = conn.getInputStream();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] bytes = new byte[1024];
            int length = -1;
            while ((length = fis.read(bytes)) != -1) {
                bos.write(bytes, 0, length);
                bos.flush();
            }
            byte[] picByte = bos.toByteArray();
            bos.close();
            fis.close();
            Bitmap bitmap = BitmapFactory.decodeByteArray(picByte, 0, picByte.length);
            FileOutputStream os = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();
            os.close();
        }

    }

    /**
     * 压缩图片
     * 
     * @param source 要压缩的图片
     * @param targetW 图片目标宽度
     * @param targetH 图片目标高度
     * @return
     */
    // public static BufferedImage resize(BufferedImage source, int targetW, int
    // targetH) {
    // // targetW，targetH分别表示目标长和宽
    // int type = source.getType();
    // BufferedImage target = null;
    // double sx = (double) targetW / source.getWidth();
    // double sy = (double) targetH / source.getHeight();
    // // 这里想实现在targetW，targetH范围内实现等比缩放。如果不需要等比缩放
    // // 则将下面的if else语句注释即可
    // if (sx < sy) {
    // sy = sx;
    // targetH = (int) (sx * source.getHeight());
    // } else {
    // sx = sy;
    // targetW = (int) (sy * source.getWidth());
    // }
    // if (type == BufferedImage.TYPE_CUSTOM) { // handmade
    // ColorModel cm = source.getColorModel();
    // WritableRaster raster = cm.createCompatibleWritableRaster(targetW,
    // targetH);
    // boolean alphaPremultiplied = cm.isAlphaPremultiplied();
    // target = new BufferedImage(cm, raster, alphaPremultiplied, null);
    // } else
    // target = new BufferedImage(targetW, targetH, type);
    // Graphics2D g = target.createGraphics();
    // // smoother than exlax:
    // g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
    // RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    // g.drawRenderedImage(source, AffineTransform.getScaleInstance(sx, sy));
    // g.dispose();
    // return target;
    // }

    // public static void zoomImage(String srcFileName, String tagFileName, int
    // width, int height) {
    // try {
    // BufferedImage bi = ImageIO.read(new File(srcFileName));
    // BufferedImage tag = resize(bi, width, height);
    // ImageIO.write(tag, "jpg", new File(tagFileName));
    // } catch (IOException e) {
    // e.printStackTrace();
    // }
    // }

    public static Bitmap compressBitmap(Bitmap image, int maxkb) {
        // L.showlog(压缩图片);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 50, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        // Log.i(test,原始大小 + baos.toByteArray().length);
        while (baos.toByteArray().length / 1024 > maxkb) { // 循环判断如果压缩后图片是否大于(maxkb)50kb,大于继续压缩
            // Log.i(test,压缩一次!);
            baos.reset();// 重置baos即清空baos
            options -= 10;// 每次都减少10
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
        }
        // Log.i(test,压缩后大小 + baos.toByteArray().length);
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);// 把ByteArrayInputStream数据生成图片
        return bitmap;
    }

    /**
     * http://developer.android.com/training/displaying-bitmaps/load-bitmap.html 官网：获取压缩后的图片
     * 
     * @param res
     * @param resId
     * @param reqWidth 所需图片压缩尺寸最小宽度
     * @param reqHeight 所需图片压缩尺寸最小高度
     * @return
     */
    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    /**
     * 官网：获取压缩后的图片
     * 
     * @param res
     * @param resId
     * @param reqWidth 所需图片压缩尺寸最小宽度
     * @param reqHeight 所需图片压缩尺寸最小高度
     * @return
     */
    public static Bitmap decodeSampledBitmapFromFile(String filepath, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filepath, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filepath, options);
    }

    public static Bitmap decodeSampledBitmapFromBitmap(Bitmap bitmap, int reqWidth, int reqHeight) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, baos);
        byte[] data = baos.toByteArray();

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(data, 0, data.length, options);
    }

    /**
     * 计算压缩比例值(改进版 by touch_ping)
     * 
     * 原版2>4>8...倍压缩 当前2>3>4...倍压缩
     * 
     * @param options 解析图片的配置信息
     * @param reqWidth 所需图片压缩尺寸最小宽度O
     * @param reqHeight 所需图片压缩尺寸最小高度
     * @return
     */
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {

        final int picheight = options.outHeight;
        final int picwidth = options.outWidth;

        int targetheight = picheight;
        int targetwidth = picwidth;
        int inSampleSize = 1;

        if (targetheight > reqHeight || targetwidth > reqWidth) {
            while (targetheight >= reqHeight && targetwidth >= reqWidth) {
                inSampleSize += 1;
                targetheight = picheight / inSampleSize;
                targetwidth = picwidth / inSampleSize;
            }
        }
        return inSampleSize;
    }

}
