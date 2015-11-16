package com.ganesus.facevision.engine;

public class Rectangle {
    Point lt;
    Point rb;

    public Point getLt() {
        return lt;
    }

    public void setLt(Point lt) {
        this.lt = lt;
    }

    public Point getRb() {
        return rb;
    }

    public void setRb(Point rb) {
        this.rb = rb;
    }

    public  boolean isBigEnough(){
        if ((rb.x - lt.x) * (rb.y - lt.y) > 3000)
            return true;
        return  false;
    }

    public double getScale(){
        if (rb.x - lt.x == 0) return 999;
        return (double)(rb.y - lt.y) / (double)( rb.x - lt.x );
    }
}
