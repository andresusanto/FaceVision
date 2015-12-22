package com.ganesus.facevision;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.ImageView;

import com.ganesus.facevision.engine.FFT;
import com.ganesus.facevision.engine.NativeBitmap;


public class FFTActivity extends ActionBarActivity {

    private static final int RESULT_LOAD_IMAGE = 121;

    private ImageView previousImage;
    private ImageView previousFFT;
    private ImageView afterImageLowPass;
    private ImageView afterFFTLowPass;
    private ImageView afterImageHighPass;
    private ImageView afterFFTHighPass;
    private ImageView afterImageSharpening;
    private ImageView afterFFTSharpening;

    NativeBitmap nativeBitmap;
    NativeBitmap fftBitmap;
    FFT.FFTMap fftMap;

    FFT fft;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy tp = new StrictMode.ThreadPolicy.Builder().permitAll().penaltyLog().build();
        StrictMode.setThreadPolicy(tp);

        setContentView(R.layout.activity_fft);
        previousImage = (ImageView) findViewById(R.id.previousImage);
        previousFFT = (ImageView) findViewById(R.id.previousFFT);
        afterImageLowPass = (ImageView) findViewById(R.id.afterImageLowPass);
        afterFFTLowPass = (ImageView) findViewById(R.id.afterFFTLowPass);
        afterImageHighPass = (ImageView) findViewById(R.id.afterImageHighPass);
        afterFFTHighPass = (ImageView) findViewById(R.id.afterFFTHighPass);
        afterImageSharpening = (ImageView) findViewById(R.id.afterImageSharpening);
        afterFFTSharpening = (ImageView) findViewById(R.id.afterFFTSharpening);

        fft = new FFT();



    }

    public void klikBuka(View v){
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RESULT_LOAD_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {


            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            Bitmap bmp = BitmapFactory.decodeFile(picturePath);
            nativeBitmap = new NativeBitmap(bmp);

            FFT fft = new FFT();
            fftMap = fft.transform(nativeBitmap.getGrayValue(), nativeBitmap.width, nativeBitmap.height);

            fftBitmap = new NativeBitmap(nativeBitmap.width, nativeBitmap.height);
            fft.FFTMapToPixels(fftMap,fftBitmap);


            previousImage.setImageBitmap(bmp);
            previousFFT.setImageBitmap(fftBitmap.draw());

            FFT.FFTMap fftMap2 = new FFT.FFTMap(fftMap);
            FFT.FFTMap fftMap3 = new FFT.FFTMap(fftMap);

            fft.lowPass(fftMap);
            short[] pixels = fft.inverseTransform(fftMap);
            NativeBitmap nativeBitmap2 = new NativeBitmap(pixels, fftMap.width, fftMap.height);
            afterImageLowPass.setImageBitmap(nativeBitmap2.draw());

            fft.FFTMapToPixels(fftMap, fftBitmap);
            afterFFTLowPass.setImageBitmap(fftBitmap.draw());

            nativeBitmap = nativeBitmap2;

            fft.FFTMapToPixels(fftMap, fftBitmap);


            fft.highPass(fftMap2);

            pixels = fft.inverseTransform(fftMap2);
            nativeBitmap2 = new NativeBitmap(pixels, fftMap2.width, fftMap2.height);
            afterImageHighPass.setImageBitmap(nativeBitmap2.draw());

            fft.FFTMapToPixels(fftMap2, fftBitmap);
            afterFFTHighPass.setImageBitmap(fftBitmap.draw());

            nativeBitmap = nativeBitmap2;

            fft.FFTMapToPixels(fftMap2, fftBitmap);


            fft.sharpening(fftMap3);

            pixels = fft.inverseTransform(fftMap3);
            nativeBitmap2 = new NativeBitmap(pixels, fftMap3.width, fftMap3.height);
            afterImageSharpening.setImageBitmap(nativeBitmap2.draw());

            fft.FFTMapToPixels(fftMap3, fftBitmap);
            afterFFTSharpening.setImageBitmap(fftBitmap.draw());

            nativeBitmap = nativeBitmap2;

        }
    }

}
