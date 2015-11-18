package com.ganesus.facevision.engine;

/**
 * Created by kevinyu on 11/18/15.
 */
public class CbCrMap {

    boolean [][] map;

    static final int SIZE = 256;
    static final float[][] transformMat;
    static final float[] transformSum;

    static {
        transformMat = new float[3][];
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

        transformSum = new float[3];
        transformSum[0] = 16;
        transformSum[1] = 128;
        transformSum[2] = 128;
    }

    public CbCrMap() {
        map = new boolean[SIZE+1][];
        for (int i=0;i<SIZE+1;i++) {
            map[i] = new boolean[SIZE+1];
        }
    }

    public void reset() {
        for (int i=0;i<=SIZE;i++) {
            for (int j=0;j<=SIZE;i++) {
                map[i][j] = false
            }
        }
    }

    private int countCb(NativeBitmap.RGB rgb) {
        int Cb = 0;
        Cb += transformMat[1][0] * rgb.red;
        Cb += transformMat[1][1] * rgb.green;
        Cb += transformMat[1][2] * rgb.blue;
        Cb += transformSum[1];
        return Cb;
    }

    private int countCr(NativeBitmap.RGB rgb) {
        int Cr = 0;
        Cr += transformMat[2][0] * rgb.red;
        Cr += transformMat[2][1] * rgb.green;
        Cr += transformMat[2][2] * rgb.blue;
        Cr += transformSum[2];
        return Cr;

    }

    public void train(NativeBitmap bitmap) {
        NativeBitmap.RGB[][] rgbMap = bitmap.getRGBMap();

        int Cb;
        int Cr;

        for (int i=0;i<rgbMap.length;i++) {
            for (int j=0;j<rgbMap[i].length;j++) {
                NativeBitmap.RGB currentRGB = rgbMap[i][j];

                Cb = countCb(currentRGB);
                Cr = countCr(currentRGB);

                map[Cb][Cr] = true;
            }
        }
    }

    public boolean[][] segment(NativeBitmap bitmap) {
        NativeBitmap.RGB[][] rgbMap = bitmap.getRGBMap();

        boolean[][] result = new boolean[rgbMap.length][];

        for (int i=0;i<result.length;i++) {
            result[i] = new boolean[rgbMap[i].length];
        }

        int Cb;
        int Cr;
        for (int i=0;i<rgbMap.length;i++) {
            for (int j=0;j<rgbMap[i].length;i++) {

                NativeBitmap.RGB currentRGB = rgbMap[i][j];

                Cb = countCb(currentRGB);
                Cr = countCr(currentRGB);

                result[i][j] = map[Cb][Cr];
            }
        }

        return result;
    }

}
