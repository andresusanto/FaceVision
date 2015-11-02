package com.ganesus.facevision.engine;

import android.graphics.Bitmap;

/**
 * Created by Andre on 10/13/2015.
 */
public class NativeBitmap {
    public int pixels[];
    public int width;
    public int height;


    public static class RGB{
        int alpha, red, green, blue;
        public RGB(){ red = 0; green = 0; blue = 0;}
        public RGB( int _red, int _green, int _blue) {
            red = _red;
            green = _green;
            blue = _blue;
        }
    };

    public NativeBitmap(int width, int height) {
        pixels = new int[width * height];
        this.width = width;
        this.height = height;
    }

    private int convertArgbToInt(RGB argb) {
        return 0xFF000000 | (argb.red << 16) | (argb.green << 8) | (argb.blue);
    }

    private RGB convertIntToArgb(int pixel){
        RGB ret = new RGB();

        ret.red = ((pixel >> 16) & 0xff);
        ret.green = ((pixel >> 8) & 0xff);
        ret.blue = ((pixel) & 0xff);

        return ret;
    }

    public void grayscaleBitmap(){
        int nBitmapSize = width * height;

        for (int i = 0; i < nBitmapSize; i++){
            RGB bitmapColor = convertIntToArgb(pixels[i]);

            int grayscaleColor = (int)(0.2989f * bitmapColor.red + 0.5870f * bitmapColor.green + 0.1141 * bitmapColor.blue);

            bitmapColor.red = grayscaleColor;
            bitmapColor.green = grayscaleColor;
            bitmapColor.blue = grayscaleColor;

            pixels[i] = convertArgbToInt(bitmapColor);
        }

    }

    public int[] createHistogram(){
        int result[] = new int[256];
        int nBitmapSize = width * height;

        for (int i = 0; i < nBitmapSize; i++){
            RGB bitmapColor = convertIntToArgb(pixels[i]);
            result[bitmapColor.red]++;
        }

        return result;
    }

    public float generateOtsu(int histogram[], int total) {
        int sum = 0;
        for (int i=1;i<256; ++i) sum+= i * histogram[i];

        int sumB = 0;
        int wB = 0;
        int wF = 0;
        int mB = 0;
        int mF = 0;
        float max = 0.0f;
        float between = 0.0f;
        float threshold1 = 0.0f;
        float threshold2 = 0.0f;

        for (int i=0;i<256;++i) {
            wB += histogram[i];
            if (wB == 0) continue;
            wF = total - wB;
            if (wF == 0) break;

            sumB += i * histogram[i];

            mB = sumB / wB;
            mF = (sum - sumB) /wF;

            between = wB * wF * (mB - mF) * (mB - mF);
            if (between >= max) {
                threshold1 = i;
                if ( between > max ) {
                    threshold2 = i;
                }
                max = between;
            }
        }

        return (threshold1 + threshold2) / 2.0f;
    }

    public int[][] convertToBinary(){
        int image[][] = new int[height][];
        int nBitmapSize = width * height;

        int histogram[] = createHistogram();

        float otsu = generateOtsu(histogram, nBitmapSize);


        for (int i=0;i< height;i++) {
            image[i] = new int[width];

            for (int j=0;j< width;j++) {
                RGB warna = convertIntToArgb(pixels[i * width + j]);
                if (warna.red > otsu){
                    image[i][j] = 1;
                }
            }
        }

        // create border
        for (int i=0;i<width;i++) {
            image[0][i] = 0;
            image[height-1][i] = 0;
        }

        for (int i=0;i<height;i++) {
            image[i][0] = 0;
            image[i][width-1] = 0;
        }

        return image;
    }

    public boolean[][] convertToBoolmage(){ // syarat harus grayscale
        boolean image[][] = new boolean[height][];
        int nBitmapSize = width * height;

        int histogram[] = createHistogram();

        float otsu = generateOtsu(histogram, nBitmapSize);


        for (int i=0;i< height;i++) {
            image[i] = new boolean[width];

            for (int j=0;j< width;j++) {
                RGB warna = convertIntToArgb(pixels[i * width + j]);
                image[i][j] = (warna.red > otsu);
            }
        }

        // create border
        for (int i=0;i<width;i++) {
            image[0][i] = false;
            image[height-1][i] = false;
        }

        for (int i=0;i<height;i++) {
            image[i][0] = false;
            image[i][width-1] = false;
        }

        return image;
    }

    public Bitmap draw(boolean[][] source){
        int width = source[0].length; int height = source.length;
        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        Bitmap canvas = Bitmap.createBitmap(width, height, conf);
        int[] _pixels = new int[width * height];

        int warna = convertArgbToInt(new RGB(128, 128, 128));

        for (int i=0;i<height;i++) {
            for (int j = 0; j < width; j++) {
                if (source[i][j]){
                    _pixels[i * width + j] = warna;
                }
            }
        }

        canvas.setPixels(_pixels, 0, width, 0, 0, width, height);

        return canvas;
    }

    public Bitmap draw(){
        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        Bitmap canvas = Bitmap.createBitmap(width, height, conf);
        canvas.setPixels(this.pixels, 0, width, 0, 0, width, height);

        return canvas;

    }

    public NativeBitmap(Bitmap bitmap){
        width = bitmap.getWidth();
        height = bitmap.getHeight();
        pixels = new int[bitmap.getWidth() * bitmap.getHeight()];

        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
    }

    public void smooth() {
        int _pixels[] = new int[width * height];

        for (int i=0;i<height;i++) {
            for (int j=0;j<width;j++) {
                Point currentPoint = new Point(j,i);
                int nNeighbor = 0;
                RGB rgbAccum = new RGB();
                for (int k=0;k<8;k++) {
                    Point neighbor = currentPoint.add(Point.direction[k]);
                    if ((neighbor.x > 0) && (neighbor.y > 0) && (neighbor.x < width) && (neighbor.y < height)) {
                        nNeighbor++;
                        RGB currentRGB = convertIntToArgb(pixels[i * width + j]);
                        rgbAccum.red += currentRGB.red;
                        rgbAccum.green += currentRGB.green;
                        rgbAccum.blue += currentRGB.blue;
                    }
                }
                rgbAccum.red /= nNeighbor;
                rgbAccum.green /= nNeighbor;
                rgbAccum.blue /= nNeighbor;
                _pixels[i * width + j] = convertArgbToInt(rgbAccum);
            }
        }
        this.pixels = _pixels;
    }

    public void applyMask(float[][] mask_matrix){
        int _pixels[] = new int[width * height];
        for (int i=0;i<height;i++) {
            for (int j=0;j<width;j++) {
                Point currentPoint = new Point(j,i);
                int nNeighbor = 0;
                double redAccum = 0, greenAccum = 0, blueAccum = 0;
                RGB currentRGB = convertIntToArgb(pixels[i * width + j]);

                redAccum += currentRGB.red * mask_matrix[1][1];
                greenAccum += currentRGB.green * mask_matrix[1][1];
                blueAccum += currentRGB.blue * mask_matrix[1][1];

                for (int k=0;k<8;k++) {
                    Point neighbor = currentPoint.add(Point.direction[k]);
                    if ((neighbor.x > 0) && (neighbor.y > 0) && (neighbor.x < width) && (neighbor.y < height)) {
                        nNeighbor++;
                        int currentRow = neighbor.getY();
                        int currentCol = neighbor.getX();
                        currentRGB = convertIntToArgb(pixels[currentRow * width + currentCol]);

                        int matRow = k/3;
                        int matCol = k%3;
                        redAccum += currentRGB.red * mask_matrix[matRow][matCol];
                        greenAccum += currentRGB.green * mask_matrix[matRow][matCol];
                        blueAccum += currentRGB.blue * mask_matrix[matRow][matCol];
                    }
                }

                RGB rgbAccum = new RGB();

                rgbAccum.red = Math.abs((int) redAccum);
                rgbAccum.green = Math.abs((int)greenAccum);
                rgbAccum.blue = Math.abs((int)blueAccum);

                _pixels[i * width + j] = convertArgbToInt(rgbAccum);
            }
        }
        this.pixels = _pixels;
    }

    public void applyHomogeneous(){
        int _pixels[] = new int[width * height];
        for (int i=0;i<height;i++) {
            for (int j=0;j<width;j++) {
                Point currentPoint = new Point(j,i);
                int nNeighbor = 0;
                double redAccum = 0, greenAccum = 0, blueAccum = 0;
                RGB currentRGB = convertIntToArgb(pixels[i * width + j]);

                RGB newRGB = new RGB();
                for (int k=0;k<8;k++) {
                    Point neighbor = currentPoint.add(Point.direction[k]);
                    if ((neighbor.x > 0) && (neighbor.y > 0) && (neighbor.x < width) && (neighbor.y < height)) {
                        nNeighbor++;
                        int currentRow = neighbor.getY();
                        int currentCol = neighbor.getX();
                        RGB neighborRGB = convertIntToArgb(pixels[currentRow * width + currentCol]);

                        if (newRGB.red < Math.abs(currentRGB.red - neighborRGB.red)) {
                            newRGB.red = Math.abs(currentRGB.red - neighborRGB.red);
                        }
                        if (newRGB.green < Math.abs(currentRGB.green - neighborRGB.green)) {
                            newRGB.green = Math.abs(currentRGB.green - neighborRGB.green);
                        }
                        if (newRGB.blue < Math.abs(currentRGB.blue - neighborRGB.blue)) {
                            newRGB.blue = Math.abs(currentRGB.blue - neighborRGB.blue);
                        }

                    }
                }

                _pixels[i * width + j] = convertArgbToInt(newRGB);
            }
        }
        this.pixels = _pixels;
    }

    public void applyDivergence(){
        int _pixels[] = new int[width * height];
        for (int i=0;i<height;i++) {
            for (int j=0;j<width;j++) {
                Point currentPoint = new Point(j,i);
                int nNeighbor = 0;
                double redAccum = 0, greenAccum = 0, blueAccum = 0;
                RGB currentRGB = convertIntToArgb(pixels[i * width + j]);

                RGB newRGB = new RGB();
                if (i == 0 || j == 0 || i == height-1 || j == width-1) continue;
                RGB neighborRGB1 = convertIntToArgb(pixels[(i-1) * width + (j + 1)]);
                RGB neighborRGB2 = convertIntToArgb(pixels[(i+1) * width + (j - 1)]);
                if (newRGB.red < Math.abs(neighborRGB1.red - neighborRGB2.red)) {
                    newRGB.red = Math.abs(neighborRGB1.red -neighborRGB2.red);
                }
                if (newRGB.green < Math.abs(neighborRGB1.green - neighborRGB2.green)) {
                    newRGB.green = Math.abs(neighborRGB1.green -neighborRGB2.green);
                }
                if (newRGB.blue < Math.abs(neighborRGB1.blue - neighborRGB2.blue)) {
                    newRGB.blue = Math.abs(neighborRGB1.blue -neighborRGB2.blue);
                }

                neighborRGB1 = convertIntToArgb(pixels[(i) * width + (j + 1)]);
                neighborRGB2 = convertIntToArgb(pixels[(i) * width + (j - 1)]);
                if (newRGB.red < Math.abs(neighborRGB1.red - neighborRGB2.red)) {
                    newRGB.red = Math.abs(neighborRGB1.red -neighborRGB2.red);
                }
                if (newRGB.green < Math.abs(neighborRGB1.green - neighborRGB2.green)) {
                    newRGB.green = Math.abs(neighborRGB1.green -neighborRGB2.green);
                }
                if (newRGB.blue < Math.abs(neighborRGB1.blue - neighborRGB2.blue)) {
                    newRGB.blue = Math.abs(neighborRGB1.blue -neighborRGB2.blue);
                }

                neighborRGB1 = convertIntToArgb(pixels[(i+1) * width + (j - 1)]);
                neighborRGB2 = convertIntToArgb(pixels[(i-1) * width + (j + 1)]);
                if (newRGB.red < Math.abs(neighborRGB1.red - neighborRGB2.red)) {
                    newRGB.red = Math.abs(neighborRGB1.red -neighborRGB2.red);
                }
                if (newRGB.green < Math.abs(neighborRGB1.green - neighborRGB2.green)) {
                    newRGB.green = Math.abs(neighborRGB1.green -neighborRGB2.green);
                }
                if (newRGB.blue < Math.abs(neighborRGB1.blue - neighborRGB2.blue)) {
                    newRGB.blue = Math.abs(neighborRGB1.blue -neighborRGB2.blue);
                }

                neighborRGB1 = convertIntToArgb(pixels[(i+1) * width + (j)]);
                neighborRGB2 = convertIntToArgb(pixels[(i-1) * width + (j)]);
                if (newRGB.red < Math.abs(neighborRGB1.red - neighborRGB2.red)) {
                    newRGB.red = Math.abs(neighborRGB1.red -neighborRGB2.red);
                }
                if (newRGB.green < Math.abs(neighborRGB1.green - neighborRGB2.green)) {
                    newRGB.green = Math.abs(neighborRGB1.green -neighborRGB2.green);
                }
                if (newRGB.blue < Math.abs(neighborRGB1.blue - neighborRGB2.blue)) {
                    newRGB.blue = Math.abs(neighborRGB1.blue -neighborRGB2.blue);
                }

                _pixels[i * width + j] = convertArgbToInt(newRGB);
            }
        }
        this.pixels = _pixels;
    }

}