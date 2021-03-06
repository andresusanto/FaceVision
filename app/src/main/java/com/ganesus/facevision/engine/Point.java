package com.ganesus.facevision.engine;

public class Point {

    public static Point[] direction = {new Point(1,0), new Point(1,1), new Point(0,1), new Point(-1,1),
            new Point(-1,0), new Point(-1,-1), new Point(0,-1), new Point(1,-1)};

    public static Point[] fourDirection = {new Point (1,0), new Point (0,1), new Point (0,-1), new Point (-1,0)};

    public int x;
    public int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Point(Point otherPoint) {
        this.x = otherPoint.x;
        this.y = otherPoint.y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setPoint(Point point) {
        this.x = point.x;
        this.y = point.y;
    }

    public void setPoint(int x,int y) {
        this.x = x;
        this.y = y;
    }

    Point add(Point other) {
        return new Point(x + other.x, y + other.y);
    }

    public boolean equals(Point point) {
        return x == point.x && y == point.y;
    }
}