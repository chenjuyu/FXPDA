package com.fuxi.vo;

public class RandomColor {

    public float r;
    public float g;
    public float b;

    public RandomColor(float r, float g, float b) {
        super();
        this.r = r * 256;
        this.g = g * 256;
        this.b = b * 256;
    }

    public RandomColor() {
        super();
    }

    public float getR() {
        return r;
    }

    public void setR(float r) {
        this.r = r;
    }

    public float getG() {
        return g;
    }

    public void setG(float g) {
        this.g = g;
    }

    public float getB() {
        return b;
    }

    public void setB(float b) {
        this.b = b;
    }

}
