package com.ganesus.facevision.engine;

import android.os.Debug;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Created by Andre on 11/16/2015.
 */
public class FaceDetector {
    private static final double THRESHOLD_WARNA = 90.0;

    private NativeBitmap nativeBitmap;

    public FaceDetector(NativeBitmap nativeBitmap){
        this.nativeBitmap = nativeBitmap;
    }

    private int getPos(int i,int j){
        return i * this.nativeBitmap.width + j;
    }

    public List<Rectangle> detectFaces(){
        boolean[] visited = new boolean[this.nativeBitmap.width * this.nativeBitmap.height];

        List<Rectangle> result = new ArrayList<>();
        for (int i = 0; i< this.nativeBitmap.height; i++) {
            for (int j = 0; j < this.nativeBitmap.width; j++) {
                if (isSkin(NativeBitmap.convertIntToArgb(this.nativeBitmap.pixels[getPos(i,j)]))){
                    Rectangle rect = new Rectangle();
                    rect.rb = new Point(j, i);
                    rect.lt = new Point(j, i);

                    visited[getPos(i,j)] = true;
                    DFSFace(i, j, visited, rect);
                    if (rect.getScale() < 1.55 && rect.getScale() > 0.9 && rect.isBigEnough()) result.add(rect);
                }
            }
        }

        nativeBitmap.drawRects(result);

        return result;
    }

    public void DFSFace(int cur_i, int cur_j, boolean[] visited, Rectangle rect){
        Stack<Point> proses = new Stack<>();
        proses.add(new Point(cur_i, cur_j));


        do {
            Point point = proses.pop();
            int i = point.x; int j = point.y;

            visited[getPos(i,j)] = true;
            //this.nativeBitmap.pixels[getPos(i,j)] = NativeBitmap.convertArgbToInt(new NativeBitmap.RGB(255,255,255));

            if (rect.lt.y > i) rect.lt.y = i;
            if (rect.lt.x > j) rect.lt.x = j;

            if (rect.rb.y < i) rect.rb.y = i;
            if (rect.rb.x < j) rect.rb.x = j;

            if (imageRange(i - 1, j - 1) && !visited[getPos(i - 1, j - 1)] && isSkin(NativeBitmap.convertIntToArgb(nativeBitmap.pixels[getPos(i - 1, j - 1)]))) {
                proses.add(new Point(i - 1, j - 1));
            }

            if (imageRange(i, j - 1) && !visited[getPos(i, j - 1)] && isSkin(NativeBitmap.convertIntToArgb(nativeBitmap.pixels[getPos(i, j - 1)]))) {
                proses.add(new Point(i, j - 1));
            }

            if (imageRange(i + 1, j - 1) && !visited[getPos(i + 1, j - 1)] && isSkin(NativeBitmap.convertIntToArgb(nativeBitmap.pixels[getPos(i + 1, j - 1)]))) {
                proses.add(new Point(i + 1, j - 1));
            }

            if (imageRange(i - 1, j) && !visited[getPos(i - 1, j)] && isSkin(NativeBitmap.convertIntToArgb(nativeBitmap.pixels[getPos(i - 1, j)]))) {
                proses.add(new Point(i - 1, j));
            }

            if (imageRange(i + 1, j) && !visited[getPos(i + 1, j)] && isSkin(NativeBitmap.convertIntToArgb(nativeBitmap.pixels[getPos(i + 1, j)]))) {
                proses.add(new Point(i + 1, j));
            }

            if (imageRange(i - 1, j + 1) && !visited[getPos(i - 1, j + 1)] && isSkin(NativeBitmap.convertIntToArgb(nativeBitmap.pixels[getPos(i - 1, j + 1)]))) {
                proses.add(new Point(i - 1, j + 1));
            }

            if (imageRange(i, j + 1) && !visited[getPos(i, j + 1)] && isSkin(NativeBitmap.convertIntToArgb(nativeBitmap.pixels[getPos(i, j + 1)]))) {
                proses.add(new Point(i, j + 1));
            }

            if (imageRange(i + 1, j + 1) && !visited[getPos(i + 1, j + 1)] && isSkin(NativeBitmap.convertIntToArgb(nativeBitmap.pixels[getPos(i + 1, j + 1)]))) {
                proses.add(new Point(i + 1, j + 1));
            }
        }while(proses.size() > 0);

    }

    private boolean imageRange(int i, int j){
        if (i < nativeBitmap.height && j < nativeBitmap.width && j > 0 && i > 0) return true;
        return false;
    }

    private double colorDistance(NativeBitmap.RGB a, NativeBitmap.RGB b){
        return Math.pow(Math.pow(a.red - b.red, 2) + Math.pow(a.green - b.green, 2) + Math.pow(a.blue - b.blue, 2), 0.5);
    }

    public boolean isSkin(NativeBitmap.RGB pixel){
        if (pixel.red > pixel.green && pixel.green > pixel.blue){
            if (pixel.red > 180) return false;
            if (pixel.red < 85) return  false;
            if (pixel.green > 160) return  false;
            if (pixel.green < 70) return false;
            if (pixel.red > 120) return false;
            if (pixel.red < 65) return false;
            return true;
        }else{
            return false;
        }
        /*List<NativeBitmap.RGB> rgb = new ArrayList<>();

        rgb.add(new NativeBitmap.RGB(82,70,46));
        rgb.add(new NativeBitmap.RGB(100,90,65));
        rgb.add(new NativeBitmap.RGB(175,151,117));

        double curentMin = colorDistance(pixel, rgb.get(0));

        for (int i = 1 ; i < rgb.size(); i++){
            double currentDist = colorDistance(pixel, rgb.get(i));
            if (curentMin < currentDist) curentMin = currentDist;
        }

        if (curentMin < THRESHOLD_WARNA) return true;
        return false;*/
    }
}
