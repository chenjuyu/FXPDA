package com.fuxi.util;

import java.util.Random;
import com.fuxi.vo.RandomColor;

public class RandomColorUtil {

    private static Random random = new Random();
    private float k = random.nextFloat() * 360;

    public RandomColor randomColor() {
        return HSVtoRGB(randomH(), 0.8F, 0.8F);
    }

    private float randomH() {
        k += (1 - 0.618f) * 360;
        if (k > 360)
            k -= 360;
        return k;
    }

    private RandomColor HSVtoRGB(float h, float s, float v) {
        float f, p, q, t;
        if (s == 0) {
            return makeColor(v, v, v);
        }

        h /= 60;
        int i = (int) Math.floor(h);
        f = h - i;
        p = v * (1 - s);
        q = v * (1 - s * f);
        t = v * (1 - s * (1 - f));
        switch (i) {
            case 0:
                return makeColor(v, t, p);
            case 1:
                return makeColor(q, v, p);
            case 2:
                return makeColor(p, v, t);
            case 3:
                return makeColor(p, q, v);
            case 4:
                return makeColor(t, p, v);
            default:
                return makeColor(v, p, q);
        }
    }

    private RandomColor makeColor(float r, float g, float b) {
        return new RandomColor(r, g, b);
    }

}
