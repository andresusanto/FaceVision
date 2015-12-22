package com.ganesus.facevision.engine;

/**
 * Created by kevinyu on 12/19/15.
 */
public class FFT {

    public static class FFTMap {

        public FFTMap(int width, int height) {
            this.width = width;
            this.height = height;
            real = new float[width * height];
            imaginer = new float[width * height];
        }

        public FFTMap(FFTMap fftMap) {
            this.width = fftMap.width;
            this.height = fftMap.height;

            real = new float[width * height];
            imaginer = new float[width * height];

            for (int i=0;i<height;i++) {
                for (int j=0;j<width;j++) {
                    real [i * width + j] = fftMap.real[i * width + j];
                    imaginer [i * width + j] = fftMap.imaginer[i * width + j];
                }
            }
        }

        public float[] real;
        public float[] imaginer;
        public int width;
        public int height;
    }

    public FFTMap transform(short[] image,int width, int height) {

        FFTMap fftMapTmp = new FFTMap(width,height);

        FFTMap fftMap = new FFTMap(width ,height);

        for (int i=0;i<height;i++) {
            for (int j=0;j<width;j++) {
                int m = j - (width/2);
                for (int k=0;k<width;k++) {
                    float frac = (float)(m * k) / width;
                    fftMapTmp.real[i * width + j] += (float)(image[i * width + k] * Math.cos( -2 * Math.PI * frac));
                    fftMapTmp.imaginer[i * width + j] += (float)(image[i * width + k] * Math.sin( -2 * Math.PI * frac));
                }
            }
        }

        for (int i=0;i<height;i++) {
            int n = i - (height/2);
            for (int j=0;j<width;j++) {
                for (int k=0;k<height;k++) {
                    float frac = (float)(n * k) / height;

                    float real = (float) Math.cos(-2 * Math.PI * frac);
                    float imaginer = (float) Math.sin(-2 * Math.PI * frac);

                    fftMap.real[i * width + j] += (fftMapTmp.real[k * width + j] * real -
                            fftMapTmp.imaginer[k * width + j] * imaginer);
                    fftMap.imaginer[i * width + j] += (fftMapTmp.real[k * width + j] * imaginer + fftMapTmp.imaginer[k * width + j] * real);
                }
            }
        }

        return fftMap;

    }

    public short[] inverseTransform(FFTMap fftMapIn) {

        int width = fftMapIn.width;
        int height = fftMapIn.height;

        short[] pixels = new short[fftMapIn.width * fftMapIn.height];

        float[] realValTmp = new float[fftMapIn.width * fftMapIn.height];
        float[] imaginerValTmp = new float[fftMapIn.width * fftMapIn.height];

        for (int i=0;i<height;i++) {
            for (int j=0;j<width;j++) {

                for (int k=0;k<height;k++) {

                    int n = k - (height / 2);

                    float frac1 = (float) (n * i) / height;

                    float real = (float) Math.cos(2 * Math.PI * frac1);
                    float imaginer = (float) Math.sin(2 * Math.PI * frac1);

                    float real2 = fftMapIn.real[k * width + j] * real - fftMapIn.imaginer[k * width + j] * imaginer;
                    float imaginer2 = fftMapIn.real[k * width + j] * imaginer + fftMapIn.imaginer[k * width + j] * real;

                    realValTmp[i * width + j] += real2;
                    imaginerValTmp[i * width + j] += imaginer2;

                }

            }

        }



        float[] realVal = new float[fftMapIn.width * fftMapIn.height];
        float[] imaginerVal = new float[fftMapIn.width * fftMapIn.height];


        float area = width * height;
        for (int i=0;i<height;i++) {
            for (int j=0;j<width;j++) {
                for (int l=0;l<width;l++) {

                    int m = l - (width/2);

                    float frac2 = (float) (m * j) / width;

                    float real = (float)Math.cos(2 * Math.PI * frac2);
                    float imaginer = (float)Math.sin(2 * Math.PI * frac2);

                    float real2 = realValTmp[i * width + l] * real - imaginerValTmp[i * width + l] * imaginer;
                    float imaginer2 = realValTmp[i * width + l] * imaginer + imaginerValTmp[i * width + l] * real;

                    realVal[i * width + j] += real2;
                    imaginerVal[i * width + j] += imaginer2;
                }

                realVal[i * width + j] /= area;
                pixels[i * width + j] = (short)Math.max(0, Math.min(255, realVal[i * width + j]));

            }

        }

        return pixels;
    }

    public void lowPass(FFTMap map) {
        int width = map.width;
        int height = map.height;

        int radiusThreshold = width/4;
        for (int i=0;i<height;i++) {
            for (int j=0;j<width;j++) {
                int n = i - (height/2);
                int m = j - (width/2);
                if (Math.sqrt(n * n + m * m) > radiusThreshold) {
                    map.real[i * width + j] = 0;
                    map.imaginer[i * width + j] = 0;
                }
            }
        }
    }

    public void highPass(FFTMap map) {
        int width = map.width;
        int height = map.height;

        float radiusThreshold = 10f;
        for (int i=0;i<height;i++) {
            for (int j=0;j<width;j++) {
                int n = i - (height/2);
                int m = j - (width/2);
                if (Math.sqrt(n * n + m * m) < radiusThreshold) {
                    map.real[i * width + j] = 0;
                    map.imaginer[i * width + j] = 0;
                }
            }
        }

        double total = 10;
        for (int i=0;i<height;i++) {
            for (int j=0;j<width;j++) {
                float real = map.real[i * width + j];
                float imaginer = map.real[i * width + j];

                total += Math.sqrt(real * real + imaginer * imaginer);
            }
        }
    }

    public void sharpening(FFTMap map) {
        int width = map.width;
        int height = map.height;

        float min = 0.5f;
        float max = 6;

        float maxFrequency = (float)Math.sqrt(width * width / 4 + height * height / 4);
        for (int i=0;i<height;i++) {
            for (int j=0;j<width;j++) {
                int n = i - (height/2);
                int m = j - (width/2);

                float distance = (float) Math.sqrt(n * n + m * m);
                float range = max - min;
                float multiplier = min + (float)(distance * range / maxFrequency);
                map.real[i * width + j] *= multiplier;
                map.imaginer[i * width + j] *= multiplier;
            }
        }
    }

    public void FFTMapToPixels(FFTMap mapIn, NativeBitmap nativeBitmapOut) {
        int width = mapIn.width;
        int height = mapIn.height;

        float pixels[] = new float[width * height];

        float max = 0;
        for (int i=0;i<height;i++) {
            for (int j=0;j<width;j++) {
                float real = mapIn.real[i * width + j];
                float imaginer = mapIn.imaginer[i * width + j];
                float amplitudo = real * real + imaginer * imaginer;
                pixels[i * width + j] = (float)Math.log(1 + Math.sqrt(amplitudo));

                if (max < amplitudo) {
                    max = amplitudo;
                }
            }
        }

        for (int i=0;i<height;i++) {
            for (int j=0;j<width;j++) {
                float greyValue = (float)(pixels[i * width + j] * 255) /(float)Math.log(1 + max);
                NativeBitmap.RGB rgb = new NativeBitmap.RGB((int)greyValue, (int)greyValue, (int)greyValue);
                nativeBitmapOut.pixels[ i * width + j] = NativeBitmap.convertArgbToInt(rgb);
            }
        }

    }

}
