package com.fuxi.util;

import java.util.Arrays;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Log;

public class BitmapTools {

    public static Bitmap resizeImage(Bitmap bitmap, int w, int h) {
        Bitmap BitmapOrg = bitmap;
        int width = BitmapOrg.getWidth();
        int height = BitmapOrg.getHeight();
        int newWidth = w;
        int newHeight = h;

        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleWidth);
        Bitmap resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, width, height, matrix, true);
        return resizedBitmap;
    }

    public void PrintImageNew(Bitmap bitmapCode) {
        // TODO Auto-generated method stub
        int w = bitmapCode.getWidth();
        int h = bitmapCode.getHeight();
        // byte[] sendbuf = StartBmpToPrintCode(bitmapCode);

        // write(sendbuf);

    }


    public static Bitmap convertToBlackWhite(Bitmap bmp) {
        int width = bmp.getWidth(); // ��ȡλͼ�Ŀ�
        int height = bmp.getHeight(); // ��ȡλͼ�ĸ�
        int[] pixels = new int[width * height]; // ͨ��λͼ�Ĵ�С�������ص�����
        byte b;
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        int alpha = 0xFF << 24;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int grey = pixels[width * i + j];

                int red = ((grey & 0x00FF0000) >> 16);
                int green = ((grey & 0x0000FF00) >> 8);
                int blue = (grey & 0x000000FF);

                grey = (int) (red * 0.3 + green * 0.59 + blue * 0.11);
                // grey = alpha | (grey << 16) | (grey << 8) | grey;
                if (grey < 128) {
                    b = 1;
                } else {
                    b = 0;
                }
                // pixels[width * i + j] = grey;
                pixels[width * i + j] = b;
            }
        }
        Bitmap newBmp = Bitmap.createBitmap(width, height, Config.RGB_565);
        newBmp.setPixels(pixels, 0, width, 0, 0, width, height);
        return newBmp;
    }

    public Bitmap bitmap2Gray(Bitmap bmSrc) {
        // �õ�ͼƬ�ĳ��Ϳ�
        int width = bmSrc.getWidth();
        int height = bmSrc.getHeight();
        // ����Ŀ��Ҷ�ͼ��
        Bitmap bmpGray = null;
        bmpGray = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        // ��������
        Canvas c = new Canvas(bmpGray);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmSrc, 0, 0, paint);
        return bmpGray;
    }

    // �ú���ʵ�ֶ�ͼ����ж�ֵ������
    public static Bitmap gray2Binary(Bitmap graymap) {
        // �õ�ͼ�εĿ�Ⱥͳ���
        int width = graymap.getWidth();
        int height = graymap.getHeight();
        // ������ֵ��ͼ��
        Bitmap binarymap = null;
        binarymap = graymap.copy(Config.ARGB_8888, true);
        // ����ѭ������ͼ������ؽ��д���
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                // �õ���ǰ���ص�ֵ
                int col = binarymap.getPixel(i, j);
                // �õ�alphaͨ����ֵ
                int alpha = col & 0xFF000000;
                // �õ�ͼ�������RGB��ֵ
                int red = (col & 0x00FF0000) >> 16;
                int green = (col & 0x0000FF00) >> 8;
                int blue = (col & 0x000000FF);
                // �ù�ʽX = 0.3��R+0.59��G+0.11��B�����X����ԭ����RGB
                int gray = (int) ((float) red * 0.3 + (float) green * 0.59 + (float) blue * 0.11);
                // ��ͼ����ж�ֵ������
                if (gray <= 95) {
                    gray = 0;
                } else {
                    gray = 255;
                }
                // �µ�ARGB
                int newColor = alpha | (gray << 16) | (gray << 8) | gray;
                // ������ͼ��ĵ�ǰ����ֵ
                binarymap.setPixel(i, j, newColor);
            }
        }
        return binarymap;
    }

    public static Bitmap binarization(Bitmap img) {
        int width = img.getWidth();
        int height = img.getHeight();
        int area = width * height;
        int gray[][] = new int[width][height];
        int average = 0;// �Ҷ�ƽ��ֵ
        int graysum = 0;
        int graymean = 0;
        int grayfrontmean = 0;
        int graybackmean = 0;
        int pixelGray;
        int front = 0;
        int back = 0;
        int[] pix = new int[width * height];
        img.getPixels(pix, 0, width, 0, 0, width, height);
        for (int i = 1; i < width; i++) { // ����߽��к��У�Ϊ����Խ��
            for (int j = 1; j < height; j++) {
                int x = j * width + i;
                int r = (pix[x] >> 16) & 0xff;
                int g = (pix[x] >> 8) & 0xff;
                int b = pix[x] & 0xff;
                pixelGray = (int) (0.3 * r + 0.59 * g + 0.11 * b);// ����ÿ�������ĻҶ�
                gray[i][j] = (pixelGray << 16) + (pixelGray << 8) + (pixelGray);
                graysum += pixelGray;
            }
        }
        graymean = (int) (graysum / area);// ����ͼ�ĻҶ�ƽ��ֵ
        average = graymean;
        // Log.i(TAG,"Average:"+average);
        for (int i = 0; i < width; i++) // ��������ͼ�Ķ�ֵ����ֵ
        {
            for (int j = 0; j < height; j++) {
                if (((gray[i][j]) & (0x0000ff)) < graymean) {
                    graybackmean += ((gray[i][j]) & (0x0000ff));
                    back++;
                } else {
                    grayfrontmean += ((gray[i][j]) & (0x0000ff));
                    front++;
                }
            }
        }
        int frontvalue = (int) (grayfrontmean / front);// ǰ������
        int backvalue = (int) (graybackmean / back);// ��������
        float G[] = new float[frontvalue - backvalue + 1];// ��������
        int s = 0;
        // Log.i(TAG,"Front:"+front+"**Frontvalue:"+frontvalue+"**Backvalue:"+backvalue);
        for (int i1 = backvalue; i1 < frontvalue + 1; i1++)// ��ǰ�����ĺͱ�������Ϊ������ô���㷨��OTSU�㷨��
        {
            back = 0;
            front = 0;
            grayfrontmean = 0;
            graybackmean = 0;
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    if (((gray[i][j]) & (0x0000ff)) < (i1 + 1)) {
                        graybackmean += ((gray[i][j]) & (0x0000ff));
                        back++;
                    } else {
                        grayfrontmean += ((gray[i][j]) & (0x0000ff));
                        front++;
                    }
                }
            }
            grayfrontmean = (int) (grayfrontmean / front);
            graybackmean = (int) (graybackmean / back);
            G[s] = (((float) back / area) * (graybackmean - average) * (graybackmean - average) + ((float) front / area) * (grayfrontmean - average) * (grayfrontmean - average));
            s++;
        }
        float max = G[0];
        int index = 0;
        for (int i = 1; i < frontvalue - backvalue + 1; i++) {
            if (max < G[i]) {
                max = G[i];
                index = i;
            }
        }

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int in = j * width + i;
                if (((gray[i][j]) & (0x0000ff)) < (index + backvalue)) {
                    pix[in] = Color.rgb(0, 0, 0);
                } else {
                    pix[in] = Color.rgb(255, 255, 255);
                }
            }
        }

        Bitmap temp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        temp.setPixels(pix, 0, width, 0, 0, width, height);
        return temp;
        // image.setImageBitmap(temp);
    }



    public static void encodeYUV420SP(byte[] yuv420sp, int[] rgba, int width, int height) {
        int frameSize = width * height;

        int[] U = new int[frameSize];
        int[] V = new int[frameSize];
        int uvwidth = width / 2;

        int bits = 8;
        int index = 0;
        int f = 0;
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                int r = (rgba[index] & 0xFF000000) >> 24;
                int g = (rgba[index] & 0xFF0000) >> 16;
                int b = (rgba[index] & 0xFF00) >> 8;

                int y = (66 * r + 129 * g + 25 * b + 128 >> 8) + 16;
                int u = (-38 * r - 74 * g + 112 * b + 128 >> 8) + 128;
                int v = (112 * r - 94 * g - 18 * b + 128 >> 8) + 128;

                byte temp = (byte) (y > 255 ? 255 : y < 0 ? 0 : y);
                yuv420sp[(index++)] = (byte) (temp > 0 ? 1 : 0);


                // {
                // if (f == 0) {
                // yuv420sp[index++] = 0;
                // f = 1;
                // } else {
                // yuv420sp[index++] = 1;
                // f = 0;
                // }
                // }

            }

        }

        f = 0;
    }



    public static byte[] bitmap2PrinterBytes(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        // Log.v("hello", "height?:"+height);
        int startX = 0;
        int startY = 0;
        int offset = 0;
        int scansize = width;
        int writeNo = 0;
        int rgb = 0;
        int colorValue = 0;
        int[] rgbArray = new int[offset + (height - startY) * scansize + (width - startX)];
        bitmap.getPixels(rgbArray, offset, scansize, startX, startY, width, height);

        int iCount = (height % 8);
        if (iCount > 0) {
            iCount = (height / 8) + 1;
        } else {
            iCount = (height / 8);
        }

        byte[] mData = new byte[iCount * width];

        // Log.v("hello", "myiCount?:"+iCoun t);
        for (int l = 0; l <= iCount - 1; l++) {
            // Log.v("hello", "iCount?:"+l);
            // Log.d("hello", "l?:"+l);
            for (int i = 0; i < width; i++) {
                int rowBegin = l * 8;
                // Log.v("hello", "width?:"+i);
                int tmpValue = 0;
                int leftPos = 7;
                int newheight = ((l + 1) * 8 - 1);
                // Log.v("hello", "newheight?:"+newheight);
                for (int j = rowBegin; j <= newheight; j++) {
                    // Log.v("hello", "width?:"+i+"  rowBegin?:"+j);
                    if (j >= height) {
                        colorValue = 0;
                    } else {
                        rgb = rgbArray[offset + (j - startY) * scansize + (i - startX)];
                        if (rgb == -1) {
                            colorValue = 0;
                        } else {
                            colorValue = 1;
                        }
                    }
                    // Log.d("hello", "rgbArray?:"+(offset + (j - startY)
                    // * scansize + (i - startX)));
                    // Log.d("hello", "colorValue?:"+colorValue);
                    tmpValue = (tmpValue + (colorValue << leftPos));
                    leftPos = leftPos - 1;

                }
                mData[writeNo] = (byte) tmpValue;
                writeNo++;
            }
        }

        return mData;
    }



}
