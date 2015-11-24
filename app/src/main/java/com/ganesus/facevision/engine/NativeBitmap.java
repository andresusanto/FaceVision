package com.ganesus.facevision.engine;

import android.graphics.Bitmap;
import android.util.Log;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andre on 10/13/2015.
 */
public class NativeBitmap {
    public static final int FIRST_ORDER_SQUARE_ROOT = 1;
    public static final int FIRST_ORDER_MAX = 2;
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

    public static int convertArgbToInt(RGB argb) {
        return 0xFF000000 | (argb.red << 16) | (argb.green << 8) | (argb.blue);
    }

    public static RGB convertIntToArgb(int pixel){
        RGB ret = new RGB();

        ret.red = ((pixel >> 16) & 0xff);
        ret.green = ((pixel >> 8) & 0xff);
        ret.blue = ((pixel) & 0xff);

        return ret;
    }

    public void drawRects(List<Rectangle> rects){
        int warna = convertArgbToInt(new RGB(255, 0, 0));

        for (int i = 0 ; i < rects.size(); i ++){
            Rectangle rectangle = rects.get(i);

            for (int w = rectangle.lt.y; w <= rectangle.rb.y; w++){
                pixels[getPos(w, rectangle.lt.x)] = warna;
                pixels[getPos(w, rectangle.lt.x + 1)] = warna;
                pixels[getPos(w, rectangle.lt.x + 2)] = warna;


                pixels[getPos(w, rectangle.rb.x)] = warna;
                pixels[getPos(w, rectangle.rb.x - 1)] = warna;
                pixels[getPos(w, rectangle.rb.x - 2)] = warna;
            }

            for (int h = rectangle.lt.x; h <= rectangle.rb.x; h++){
                pixels[getPos(rectangle.lt.y, h)] = warna;
                pixels[getPos(rectangle.lt.y + 1, h)] = warna;
                pixels[getPos(rectangle.lt.y + 2, h)] = warna;

                pixels[getPos(rectangle.rb.y, h)] = warna;
                pixels[getPos(rectangle.rb.y - 1, h)] = warna;
                pixels[getPos(rectangle.rb.y - 2, h)] = warna;
            }

        }
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

    private int getPos(int i,int j){
        return i * width + j;
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
                        RGB currentRGB = convertIntToArgb(pixels[neighbor.getY() * width + neighbor.getX()]);
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

                        int matRow = 1+Point.direction[k].getY();
                        int matCol = 1+Point.direction[k].getX();
                        redAccum += currentRGB.red * mask_matrix[matRow][matCol];
                        greenAccum += currentRGB.green * mask_matrix[matRow][matCol];
                        blueAccum += currentRGB.blue * mask_matrix[matRow][matCol];
                    }
                }

                RGB rgbAccum = new RGB();
                Log.d("TAG",redAccum+" "+greenAccum+" "+blueAccum);
                rgbAccum.red = Math.abs((int) redAccum);
                rgbAccum.green = Math.abs((int)greenAccum);
                rgbAccum.blue = Math.abs((int)blueAccum);

                _pixels[i * width + j] = convertArgbToInt(rgbAccum);
            }
        }
        this.pixels = _pixels;
    }

    public void applyFirstOrder(List<Double[][]> mask_matrixes){
        int _pixels[] = new int[width * height];
        for (int i=0;i<height;i++) {
            for (int j=0;j<width;j++) {
                Point currentPoint = new Point(j,i);
                int nNeighbor = 0;
                double redAccum = 0, greenAccum = 0, blueAccum = 0;
                RGB currentRGB = convertIntToArgb(pixels[i * width + j]);

                for(int m = 0; m < mask_matrixes.size(); m++){
                    double redAccumTemp = 0, greenAccumTemp = 0, blueAccumTemp = 0;
                    Double mask_matrix[][] = mask_matrixes.get(m);
                    redAccumTemp += currentRGB.red * mask_matrix[1][1];
                    greenAccumTemp += currentRGB.green * mask_matrix[1][1];
                    blueAccumTemp += currentRGB.blue * mask_matrix[1][1];

                    for (int k=0;k<8;k++) {
                        Point neighbor = currentPoint.add(Point.direction[k]);
                        if ((neighbor.x > 0) && (neighbor.y > 0) && (neighbor.x < width) && (neighbor.y < height)) {
                            nNeighbor++;
                            int currentRow = neighbor.getY();
                            int currentCol = neighbor.getX();
                            currentRGB = convertIntToArgb(pixels[currentRow * width + currentCol]);

                            int matRow = 1+Point.direction[k].getY();
                            int matCol = 1+Point.direction[k].getX();
                            redAccumTemp += currentRGB.red * mask_matrix[matRow][matCol];
                            greenAccumTemp += currentRGB.green * mask_matrix[matRow][matCol];
                            blueAccumTemp += currentRGB.blue * mask_matrix[matRow][matCol];
                        }
                    }
                    redAccum+= redAccumTemp * redAccumTemp;
                    greenAccum += greenAccumTemp  * greenAccumTemp;
                    blueAccum += blueAccumTemp * blueAccumTemp;
                }

                RGB rgbAccum = new RGB();
                rgbAccum.red = (int) Math.sqrt(redAccum);
                rgbAccum.green = (int) Math.sqrt(greenAccum);
                rgbAccum.blue = (int) Math.sqrt(blueAccum);

                _pixels[i * width + j] = convertArgbToInt(rgbAccum);
            }
        }
        this.pixels = _pixels;
    }

    public void applyFirstOrderMax(List<Double[][]> mask_matrixes){
        int _pixels[] = new int[width * height];
        for (int i=0;i<height;i++) {
            for (int j=0;j<width;j++) {
                Point currentPoint = new Point(j,i);
                int nNeighbor = 0;
                double redAccum = 0, greenAccum = 0, blueAccum = 0;
                RGB currentRGB = convertIntToArgb(pixels[i * width + j]);

                for(int m = 0; m < mask_matrixes.size(); m++){
                    double redAccumTemp = 0, greenAccumTemp = 0, blueAccumTemp = 0;
                    Double mask_matrix[][] = mask_matrixes.get(m);
                    redAccumTemp += currentRGB.red * mask_matrix[1][1];
                    greenAccumTemp += currentRGB.green * mask_matrix[1][1];
                    blueAccumTemp += currentRGB.blue * mask_matrix[1][1];

                    for (int k=0;k<8;k++) {
                        Point neighbor = currentPoint.add(Point.direction[k]);
                        if ((neighbor.x > 0) && (neighbor.y > 0) && (neighbor.x < width) && (neighbor.y < height)) {
                            nNeighbor++;
                            int currentRow = neighbor.getY();
                            int currentCol = neighbor.getX();
                            currentRGB = convertIntToArgb(pixels[currentRow * width + currentCol]);

                            int matRow = 1+Point.direction[k].getY();
                            int matCol = 1+Point.direction[k].getX();
                            redAccumTemp += currentRGB.red * mask_matrix[matRow][matCol];
                            greenAccumTemp += currentRGB.green * mask_matrix[matRow][matCol];
                            blueAccumTemp += currentRGB.blue * mask_matrix[matRow][matCol];
                        }
                    }
                    redAccum = Math.max(redAccum,Math.abs(redAccumTemp));
                    greenAccum = Math.max(greenAccum,Math.abs(greenAccumTemp));
                    blueAccum = Math.max(blueAccum,Math.abs(blueAccumTemp));
                }

                RGB rgbAccum = new RGB();
                rgbAccum.red = (int)redAccum;
                rgbAccum.green = (int)greenAccum;
                rgbAccum.blue = (int)blueAccum;

                _pixels[i * width + j] = convertArgbToInt(rgbAccum);
            }
        }
        this.pixels = _pixels;
    }

    public void applySecondOrder(Double[][] mask_matrix) throws Exception{
        final int ni[] = {0,0,0,1,2,2,2,1};
        final int nj[] = {0,1,2,2,2,1,0,0};
        int result_pixels[] = new int [width * height];

        if (mask_matrix.length != 3 || (mask_matrix[0].length != 3)){
            throw new Exception("Ukuran mask matrik harus 3 x 3");
        }

        for(int __rotate = 0; __rotate < 9; __rotate++) {
            int _pixels[] = new int[width * height];
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    Point currentPoint = new Point(j, i);
                    int nNeighbor = 0;
                    double redAccum = 0, greenAccum = 0, blueAccum = 0;
                    RGB currentRGB = convertIntToArgb(pixels[i * width + j]);

                    double redAccumTemp = 0, greenAccumTemp = 0, blueAccumTemp = 0;
                    redAccumTemp += currentRGB.red * mask_matrix[1][1];
                    greenAccumTemp += currentRGB.green * mask_matrix[1][1];
                    blueAccumTemp += currentRGB.blue * mask_matrix[1][1];
                    for (int k = 0; k < 8; k++) {
                        Point neighbor = currentPoint.add(Point.direction[k]);
                        if ((neighbor.x > 0) && (neighbor.y > 0) && (neighbor.x < width) && (neighbor.y < height)) {
                            nNeighbor++;
                            int currentRow = neighbor.getY();
                            int currentCol = neighbor.getX();
                            currentRGB = convertIntToArgb(pixels[currentRow * width + currentCol]);

                            int matRow = 1 + Point.direction[k].getY();
                            int matCol = 1 + Point.direction[k].getX();
                            redAccumTemp += currentRGB.red * mask_matrix[matRow][matCol];
                            greenAccumTemp += currentRGB.green * mask_matrix[matRow][matCol];
                            blueAccumTemp += currentRGB.blue * mask_matrix[matRow][matCol];
                        }
                    }

                    RGB rgbAccum = new RGB();
                    rgbAccum.red = (int) Math.min(255,Math.max(0,redAccumTemp));
                    rgbAccum.green = (int) Math.min(255,Math.max(0,greenAccumTemp));
                    rgbAccum.blue = (int) Math.min(255,Math.max(0,blueAccumTemp));
                    _pixels[i * width + j] = convertArgbToInt(rgbAccum);
                }
            }
            if (__rotate == 0){
                for(int i = 0; i < width * height; i++){
                    result_pixels[i] = _pixels[i];
                }
            } else {
                for(int i = 0; i < width * height; i++){
                    result_pixels[i] = Math.max(result_pixels[i],_pixels[i]);
                }
            }
            //rotate the matrix
            Double temp = Double.valueOf(mask_matrix[ni[0]][nj[0]]);
            for(int t = 0; t < 8; t++){
                mask_matrix[ni[t]][nj[t]] = Double.valueOf(mask_matrix[ni[(t + 1) % 8]][nj[(t + 1) % 8]]);
            }
            mask_matrix[ni[7]][nj[7]] = temp;
        }
        this.pixels = result_pixels;
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

    public int[] convolution(List<Double[][]> mask,int maskWidth,int maskHeight) {
        int newHeight = (maskHeight - (height-1));
        int newWidth = (maskWidth - (width-1));
        int[] newImage = new int [(maskHeight - (height - 1)) * (maskWidth - (width - 1))];
        for (int i=0;i<newHeight;i++) {
            for (int j=0;j<newWidth;j++) {

                int redAccum = 0,greenAccum = 0,blueAccum = 0;

                for (int m=0;m<mask.size();m++) {
                    int currentMaskRed = 0, currentMaskGreen = 0, currentMaskBlue = 0;
                    for (int k = 0; k < maskHeight; k++) {
                        for (int l = 0; l < maskWidth; l++) {
                            RGB currentRGB = convertIntToArgb(pixels[(i+k) * width + (j+l)]);

                            currentMaskRed += currentRGB.red;
                            currentMaskBlue += currentRGB.blue;
                            currentMaskGreen += currentRGB.green;
                        }
                    }
                    redAccum += (currentMaskRed * currentMaskRed);
                    greenAccum += (currentMaskGreen * currentMaskGreen);
                    blueAccum += (currentMaskBlue * currentMaskBlue);
                }

                RGB newRGB = new RGB();
                newRGB.red = (int)Math.sqrt(redAccum);
                newRGB.green = (int)Math.sqrt(greenAccum);
                newRGB.blue = (int) Math.sqrt(blueAccum);

                newImage[i * newWidth + j] = convertArgbToInt(newRGB);
            }
        }

        return newImage;
    }

    //Gu Kernel 3x3
    public double[] createGuKernel(double dev){
        double max=0;
        DecimalFormat df = new DecimalFormat("#.##");
        double[] guKernel=new double[3*3];
        for(int i=-1;i<=1;i++)
            for(int j=-1;j<=1;j++){
                double power=Math.pow(Math.E,-(i*i+j*j)/(2*dev*dev));
                double val=power/(2*Math.PI*dev*dev);
                guKernel[3*j+i+4]=val;
                if(val>max)max=val;
            }
        for(int i=0;i<9;i++)
            guKernel[i]=Math.round(guKernel[i]/max * 100.0) / 100.0;
        return guKernel;
    }


    public int[] gaussian(){
        int size=width*height;
        double red,green,blue,alpha;
        int[] newImage=new int[size];
        RGB rgb=new RGB();
        double[] gukernel=createGuKernel(1.5);
        for(int it=0;it<size;it++){
            red=0.0;green=0.0;blue=0.0;alpha=0.0;
            int counter=0;
            for(int x=-1;x<=1;x++)
                for(int y=-1;y<=1;y++){
                    int pos=width*y+x+it;
                    int idx=-1;
                    if(!(x==0 && y==0) && pos>=0 && pos<size){
                        if(x==0 && y==1){
                            if(pos%width==it%width && pos/width==(it/width+1))
                                idx=pos;
                        }
                        else if(x==0 && y==-1){
                            if(pos%width==it%width && pos/width==(it/width-1))
                                idx=pos;
                        }
                        else if(x==1 && y==0){
                            if(pos/width==it/width && pos%width==(it+1)%width)
                                idx=pos;
                        }
                        else if (x==-1 && y==0){
                            if(pos/width==it/width && pos%width==(it-1)%width)
                                idx=pos;
                        }
                        else{
                            if(pos/width!=it/width && pos%width!=it%width)
                                if(it%width==0){
                                    if(pos%width!=width-1)
                                        idx=pos;
                                }
                                else if(it%width==width-1){
                                    if(pos%width!=0)
                                        idx=pos;
                                }
                                else
                                    idx=pos;
                        }
                    }
                    if(idx!=-1){
                        rgb=convertIntToArgb(pixels[idx]);
                        double factor=gukernel[3*y+x+4];
                        red+=(double)rgb.red*factor;
                        green+=(double)rgb.green*factor;
                        blue+=(double)rgb.blue*factor;
                        System.out.println(rgb.blue+"*"+factor);
                        alpha+=(double)rgb.alpha*factor;
                        counter++;
                    }
                }
            System.out.println(blue+"/"+counter);
            rgb.red=(int)red/counter;
            rgb.green=(int)green/counter;
            rgb.blue=(int)blue/counter;
            rgb.alpha=(int)alpha/counter;
            System.out.println("Red:"+rgb.red+"||Green:"+rgb.green+"||Blue:"+rgb.blue);
            newImage[it]=convertArgbToInt(rgb);
        }
        return newImage;
    }

    public void cluster(){
        List<Point> points = new ArrayList<>();
        List<Point> centroids = new ArrayList<>();

        //mata kiri
        centroids.add(new Point(width/4,height/2));
        //mata kanan
        centroids.add(new Point(3*width/4,height/2));
        //hidung
        centroids.add(new Point(width/2,(int) (0.65*height)));
        //mulut
        centroids.add(new Point(width / 2, (int) (0.75 * height)));

        //int[][] newPixels = convertToBinary();

        
        for (int i=0;i<height;i++) {
            for (int j = 0; j < width; j++) {
                Point currentPoint = new Point(j, i);
                if (convertIntToArgb(pixels[i * width + j]).red > 30){
                    points.add(new Point(j,i));
                }
            }
        }

        KMeans kMeans = new KMeans();
        List<KMeans.Cluster> clusters = kMeans.computeCluster(centroids, points, 100,height*8);
        for(int i = 0; i < clusters.size(); i++){
            KMeans.Cluster cluster = clusters.get(i);
            RGB rgb = new RGB();
            if (i == 0){
                rgb.red = 255;
            } else if (i == 1){
                rgb.green = 255;
            } else if (i == 2){
                rgb.blue = 255;
            } else if (i == 3){
                rgb.red = 128;
                rgb.green = 128;
            }

            for(Point point:cluster.pointList){
                pixels[point.getY() *width + point.getX()] = convertArgbToInt(rgb);
            }
        }
    }

    public RGB[][] getRGBMap() {
        RGB[][] rgbMap = new RGB[height][];
        for (int i=0;i<height;i++) {
            rgbMap[i] = new RGB[width];
        }

        for (int i=0;i<height;i++) {
            for (int j=0;j<width;j++) {
                rgbMap[i][j] = convertIntToArgb(pixels[i * width + j]);
            }
        }

        return rgbMap;
    }

    public YCbCr[][] getYCbCr() {
        YCbCr[][] yCbCr = new YCbCr[height][];

        for (int i=0;i<height;i++) {
            yCbCr[i] = new YCbCr[width];
        }

        float[][] transformMat = new float[3][];
        for (int i=0;i<3;i++) {
            transformMat[i] = new float[3];
        }

        transformMat[0][0] = 65.481f;
        transformMat[0][1] = 128.553f;
        transformMat[0][2] = 24.966f;

        transformMat[1][0] = - 37.797f;
        transformMat[1][1] = -74.203f;
        transformMat[1][2] = 112;

        transformMat[2][0] = 112;
        transformMat[2][1] = -93786f;
        transformMat[2][2] = -18.214f;

        float[] transformSum = new float[3];
        transformSum[0] = 16;
        transformSum[1] = 128;
        transformSum[2] = 128;

        for (int i=0;i<height;i++) {
            for (int j=0;j<width;j++) {
                RGB currentRGB = convertIntToArgb(pixels[i * width + j]);

                yCbCr[i][j].Y = 0;
                for (int k=0;k<3;k++) {
                    yCbCr[i][j].Y += transformMat[0][k] * currentRGB.red;
                }
                yCbCr[i][j].Y += transformSum[0];

                yCbCr[i][j].Cb = 0;
                for (int k=0;k<3;k++) {
                    yCbCr[i][j].Cb += transformMat[1][k] * currentRGB.green;
                }
                yCbCr[i][j].Cb += transformSum[1];

                yCbCr[i][j].Cr = 0;
                for (int k=0;k<3;k++) {
                    yCbCr[i][j].Cr += transformMat[2][k] * currentRGB.blue;
                }
                yCbCr[i][j].Cr += transformSum[2];

            }
        }

        return yCbCr;
    }

    public List<Point> getEyeCoordinate(Point start, Point end) {
        List<Point> points = new ArrayList<>();
        Point minPoint = new Point(end);
        Point maxPoint = new Point(start);
        for (int i=start.y;i<=end.y;i++) {
            for (int j=start.x;j<=end.x;j++) {
                RGB currentRGB = convertIntToArgb(pixels[i * width + j]);
                int totalDelta = Math.abs(currentRGB.red - currentRGB.green) +
                        Math.abs(currentRGB.green - currentRGB.blue);
                if (currentRGB. red > 50 && totalDelta < 25) {
                    if (j < minPoint.x) minPoint.setPoint(j, i);
                    if (j > maxPoint.x) maxPoint.setPoint(j, i);
                }
            }
        }
        points.add(minPoint);
        points.add(maxPoint);
        return points;
    }


}