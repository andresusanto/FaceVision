package com.ganesus.facevision;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
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
    private ImageView afterImage;
    private ImageView afterFFT;

    NativeBitmap nativeBitmap;
    NativeBitmap fftBitmap;
    FFT.FFTMap fftMap;

    FFT fft;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fft);

        previousImage = (ImageView) findViewById(R.id.previousImage);
        previousFFT = (ImageView) findViewById(R.id.previousFFT);
        afterImage = (ImageView) findViewById(R.id.afterImage);
        afterFFT = (ImageView) findViewById(R.id.afterFFT);

        fft = new FFT();

    }

    public void klikBuka(View v){
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RESULT_LOAD_IMAGE);
    }

    public void clickLowPass(View v) {
        if (nativeBitmap != null) {
            previousImage.setImageBitmap(nativeBitmap.draw());
            fft.FFTMapToPixels(fftMap, fftBitmap);
            previousFFT.setImageBitmap(fftBitmap.draw());


            fft.lowPass(fftMap);

            short[] pixels = fft.inverseTransform(fftMap);
            NativeBitmap nativeBitmap2 = new NativeBitmap(pixels, fftMap.width, fftMap.height);
            afterImage.setImageBitmap(nativeBitmap2.draw());

            fft.FFTMapToPixels(fftMap, fftBitmap);
            afterFFT.setImageBitmap(fftBitmap.draw());

            nativeBitmap = nativeBitmap2;
        }

    }

    public void clickHighPass(View v) {

        if (nativeBitmap != null) {
            previousImage.setImageBitmap(nativeBitmap.draw());
            fft.FFTMapToPixels(fftMap, fftBitmap);
            previousFFT.setImageBitmap(fftBitmap.draw());


            fft.highPass(fftMap);

            short[] pixels = fft.inverseTransform(fftMap);
            NativeBitmap nativeBitmap2 = new NativeBitmap(pixels, fftMap.width, fftMap.height);
            afterImage.setImageBitmap(nativeBitmap2.draw());

            fft.FFTMapToPixels(fftMap, fftBitmap);
            afterFFT.setImageBitmap(fftBitmap.draw());

            nativeBitmap = nativeBitmap2;
        }
    }

    public void clickSharpening(View v) {
        if (nativeBitmap != null) {
            previousImage.setImageBitmap(nativeBitmap.draw());
            fft.FFTMapToPixels(fftMap, fftBitmap);
            previousFFT.setImageBitmap(fftBitmap.draw());


            fft.sharpening(fftMap);

            short[] pixels = fft.inverseTransform(fftMap);
            NativeBitmap nativeBitmap2 = new NativeBitmap(pixels, fftMap.width, fftMap.height);
            afterImage.setImageBitmap(nativeBitmap2.draw());

            fft.FFTMapToPixels(fftMap, fftBitmap);
            afterFFT.setImageBitmap(fftBitmap.draw());

            nativeBitmap = nativeBitmap2;
        }
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

            afterImage.setImageBitmap(null);
            afterFFT.setImageBitmap(null);

        }
    }
}
