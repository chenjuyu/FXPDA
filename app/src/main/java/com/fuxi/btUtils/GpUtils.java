package com.fuxi.btUtils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Environment;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

public class GpUtils {

    private static int[] p0 = {0, 128};
    private static int[] p1 = {0, 64};
    private static int[] p2 = {0, 32};
    private static int[] p3 = {0, 16};
    private static int[] p4 = {0, 8};
    private static int[] p5 = {0, 4};
    private static int[] p6 = {0, 2};

    private static int[][] Floyd16x16 = { {0, 128, 32, 160, 8, 136, 40, 168, 2, 130, 34, 162, 10, 138, 42, 170}, {192, 64, 224, 96, 200, 72, 232, 104, 194, 66, 226, 98, 202, 74, 234, 106}, {48, 176, 16, 144, 56, 184, 24, 152, 50, 178, 18, 146, 58, 186, 26, 154},
            {240, 112, 208, 80, 248, 120, 216, 88, 242, 114, 210, 82, 250, 122, 218, 90}, {12, 140, 44, 172, 4, 132, 36, 164, 14, 142, 46, 174, 6, 134, 38, 166}, {204, 76, 236, 108, 196, 68, 228, 100, 206, 78, 238, 110, 198, 70, 230, 102},
            {60, 188, 28, 156, 52, 180, 20, 148, 62, 190, 30, 158, 54, 182, 22, 150}, {252, 124, 220, 92, 244, 116, 212, 84, 254, 126, 222, 94, 246, 118, 214, 86}, {3, 131, 35, 163, 11, 139, 43, 171, 1, 129, 33, 161, 9, 137, 41, 169},
            {195, 67, 227, 99, 203, 75, 235, 107, 193, 65, 225, 97, 201, 73, 233, 105}, {51, 179, 19, 147, 59, 187, 27, 155, 49, 177, 17, 145, 57, 185, 25, 153}, {243, 115, 211, 83, 251, 123, 219, 91, 241, 113, 209, 81, 249, 121, 217, 89},
            {15, 143, 47, 175, 7, 135, 39, 167, 13, 141, 45, 173, 5, 133, 37, 165}, {207, 79, 239, 111, 199, 71, 231, 103, 205, 77, 237, 109, 197, 69, 229, 101}, {63, 191, 31, 159, 55, 183, 23, 151, 61, 189, 29, 157, 53, 181, 21, 149},
            {254, 127, 223, 95, 247, 119, 215, 87, 253, 125, 221, 93, 245, 117, 213, 85}};

    private static int[][] Floyd8x8 = { {0, 32, 8, 40, 2, 34, 10, 42}, {48, 16, 56, 24, 50, 18, 58, 26}, {12, 44, 4, 36, 14, 46, 6, 38}, {60, 28, 52, 20, 62, 30, 54, 22}, {3, 35, 11, 43, 1, 33, 9, 41}, {51, 19, 59, 27, 49, 17, 57, 25}, {15, 47, 7, 39, 13, 45, 5, 37},
            {63, 31, 55, 23, 61, 29, 53, 21}};
    public static final int ALGORITHM_DITHER_16x16 = 16;
    public static final int ALGORITHM_DITHER_8x8 = 8;
    public static final int ALGORITHM_TEXTMODE = 2;
    public static final int ALGORITHM_GRAYTEXTMODE = 1;

    public static Bitmap resizeImage(Bitmap bitmap, int w, int h) {
        Bitmap BitmapOrg = bitmap;

        int width = BitmapOrg.getWidth();
        int height = BitmapOrg.getHeight();
        int newWidth = w;
        int newHeight = h;

        float scaleWidth = newWidth / width;
        float scaleHeight = newHeight / height;
        Matrix matrix = new Matrix();

        matrix.postScale(scaleWidth, scaleHeight);

        Bitmap resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, width, height, matrix, true);

        return resizedBitmap;
    }

    public static void saveMyBitmap(Bitmap mBitmap) {
        File f = new File(Environment.getExternalStorageDirectory().getPath(), "Btatotest.jpeg");
        try {
            f.createNewFile();
        } catch (IOException localIOException) {
        }
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(f);
            mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
        } catch (FileNotFoundException localFileNotFoundException) {
        } catch (IOException localIOException1) {
        }
    }

    public static Bitmap toGrayscale(Bitmap bmpOriginal) {
        int height = bmpOriginal.getHeight();
        int width = bmpOriginal.getWidth();
        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0.0F);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0.0F, 0.0F, paint);
        return bmpGrayscale;
    }

    static byte[] pixToEscRastBitImageCmd(byte[] src, int nWidth, int nMode) {
        int nHeight = src.length / nWidth;
        byte[] data = new byte[8 + src.length / 8];
        data[0] = 29;
        data[1] = 118;
        data[2] = 48;
        data[3] = (byte) (nMode & 0x1);
        data[4] = (byte) (nWidth / 8 % 256);
        data[5] = (byte) (nWidth / 8 / 256);
        data[6] = (byte) (nHeight % 256);
        data[7] = (byte) (nHeight / 256);
        int i = 8;
        for (int k = 0; i < data.length; i++) {
            data[i] = (byte) (p0[src[k]] + p1[src[(k + 1)]] + p2[src[(k + 2)]] + p3[src[(k + 3)]] + p4[src[(k + 4)]] + p5[src[(k + 5)]] + p6[src[(k + 6)]] + src[(k + 7)]);
            k += 8;
        }
        return data;
    }

    public static byte[] pixToEscRastBitImageCmd(byte[] src) {
        byte[] data = new byte[src.length / 8];
        int i = 0;
        for (int k = 0; i < data.length; i++) {
            data[i] = (byte) (p0[src[k]] + p1[src[(k + 1)]] + p2[src[(k + 2)]] + p3[src[(k + 3)]] + p4[src[(k + 4)]] + p5[src[(k + 5)]] + p6[src[(k + 6)]] + src[(k + 7)]);
            k += 8;
        }
        return data;
    }

    public static void addBitImage(byte[] src, List<Byte> contents) {
        int length = src.length / 8;
        int i = 0;
        for (int k = 0; i < length; i++) {
            byte b = (byte) (p0[src[k]] + p1[src[(k + 1)]] + p2[src[(k + 2)]] + p3[src[(k + 3)]] + p4[src[(k + 4)]] + p5[src[(k + 5)]] + p6[src[(k + 6)]] + src[(k + 7)]);
            contents.add(b);
            k += 8;
        }

    }

    static byte[] pixToEscNvBitImageCmd(byte[] src, int width, int height) {
        byte[] data = new byte[src.length / 8 + 4];
        data[0] = (byte) (width / 8 % 256);
        data[1] = (byte) (width / 8 / 256);
        data[2] = (byte) (height / 8 % 256);
        data[3] = (byte) (height / 8 / 256);
        int k = 0;
        for (int i = 0; i < width; i++) {
            k = 0;
            for (int j = 0; j < height / 8; j++) {
                data[(4 + j + i * height / 8)] = (byte) (p0[src[(i + k)]] + p1[src[(i + k + 1 * width)]] + p2[src[(i + k + 2 * width)]] + p3[src[(i + k + 3 * width)]] + p4[src[(i + k + 4 * width)]] + p5[src[(i + k + 5 * width)]] + p6[src[(i + k + 6 * width)]] + src[(i + k + 7 * width)]);
                k += 8 * width;
            }
        }
        return data;
    }

    public static byte[] pixToTscCmd(byte[] src) {
        byte[] data = new byte[src.length / 8];

        int k = 0;
        for (int j = 0; k < data.length; k++) {
            byte temp = (byte) (p0[src[j]] + p1[src[(j + 1)]] + p2[src[(j + 2)]] + p3[src[(j + 3)]] + p4[src[(j + 4)]] + p5[src[(j + 5)]] + p6[src[(j + 6)]] + src[(j + 7)]);
            data[k] = (byte) (temp ^ 0xFFFFFFFF);
            j += 8;
        }
        return data;
    }

    public static byte[] pixToTscCmd(int x, int y, int mode, byte[] src, int nWidth) {
        int height = src.length / nWidth;
        int width = nWidth / 8;
        String str = "BITMAP " + x + "," + y + "," + width + "," + height + "," + mode + ",";
        byte[] bitmap = null;
        try {
            bitmap = str.getBytes("GB2312");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        byte[] arrayOfByte = new byte[src.length / 8];

        int k = 0;
        for (int j = 0; k < arrayOfByte.length; k++) {
            byte temp = (byte) (p0[src[j]] + p1[src[(j + 1)]] + p2[src[(j + 2)]] + p3[src[(j + 3)]] + p4[src[(j + 4)]] + p5[src[(j + 5)]] + p6[src[(j + 6)]] + src[(j + 7)]);
            arrayOfByte[k] = (byte) (temp ^ 0xFFFFFFFF);
            j += 8;
        }
        byte[] data = new byte[bitmap.length + arrayOfByte.length];
        System.arraycopy(bitmap, 0, data, 0, bitmap.length);
        System.arraycopy(arrayOfByte, 0, data, bitmap.length, arrayOfByte.length);
        return data;
    }

    private static void format_K_dither16x16(int[] orgpixels, int xsize, int ysize, byte[] despixels) {
        int k = 0;
        for (int y = 0; y < ysize; y++)
            for (int x = 0; x < xsize; x++) {
                if ((orgpixels[k] & 0xFF) > Floyd16x16[(x & 0xF)][(y & 0xF)])
                    despixels[k] = 0;
                else {
                    despixels[k] = 1;
                }
                k++;
            }
    }

    public static byte[] bitmapToBWPix(Bitmap mBitmap) {
        int[] pixels = new int[mBitmap.getWidth() * mBitmap.getHeight()];
        byte[] data = new byte[mBitmap.getWidth() * mBitmap.getHeight()];
        Bitmap grayBitmap = toGrayscale(mBitmap);
        grayBitmap.getPixels(pixels, 0, mBitmap.getWidth(), 0, 0, mBitmap.getWidth(), mBitmap.getHeight());

        format_K_dither16x16(pixels, grayBitmap.getWidth(), grayBitmap.getHeight(), data);

        return data;
    }

    private static void format_K_dither16x16_int(int[] orgpixels, int xsize, int ysize, int[] despixels) {
        int k = 0;
        for (int y = 0; y < ysize; y++)
            for (int x = 0; x < xsize; x++) {
                if ((orgpixels[k] & 0xFF) > Floyd16x16[(x & 0xF)][(y & 0xF)])
                    despixels[k] = -1;
                else {
                    despixels[k] = -16777216;
                }
                k++;
            }
    }

    private static void format_K_dither8x8_int(int[] orgpixels, int xsize, int ysize, int[] despixels) {
        int k = 0;
        for (int y = 0; y < ysize; y++)
            for (int x = 0; x < xsize; x++) {
                if ((orgpixels[k] & 0xFF) >> 2 > Floyd8x8[(x & 0x7)][(y & 0x7)])
                    despixels[k] = -1;
                else {
                    despixels[k] = -16777216;
                }
                k++;
            }
    }

    public static int[] bitmapToBWPix_int(Bitmap mBitmap, int algorithm) {
        int[] pixels = new int[0];
        switch (algorithm) {
            case 8:
                Bitmap grayBitmap = toGrayscale(mBitmap);
                pixels = new int[grayBitmap.getWidth() * grayBitmap.getHeight()];
                grayBitmap.getPixels(pixels, 0, grayBitmap.getWidth(), 0, 0, grayBitmap.getWidth(), grayBitmap.getHeight());
                format_K_dither8x8_int(pixels, grayBitmap.getWidth(), grayBitmap.getHeight(), pixels);
                break;
            case 2:
                break;
            case 16:
            default:
                Bitmap grayBitmap2 = toGrayscale(mBitmap);
                pixels = new int[grayBitmap2.getWidth() * grayBitmap2.getHeight()];
                grayBitmap2.getPixels(pixels, 0, grayBitmap2.getWidth(), 0, 0, grayBitmap2.getWidth(), grayBitmap2.getHeight());
                format_K_dither16x16_int(pixels, grayBitmap2.getWidth(), grayBitmap2.getHeight(), pixels);
        }

        return pixels;
    }

    public static Bitmap toBinaryImage(Bitmap mBitmap, int nWidth, int algorithm) {
        int width = (nWidth + 7) / 8 * 8;
        int height = mBitmap.getHeight() * width / mBitmap.getWidth();
        Bitmap rszBitmap = resizeImage(mBitmap, width, height);

        int[] pixels = bitmapToBWPix_int(rszBitmap, algorithm);
        rszBitmap.setPixels(pixels, 0, width, 0, 0, width, height);

        return rszBitmap;
    }
}
