package com.ganesus.facevision;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.ganesus.facevision.engine.NativeBitmap;

public class SimpleFace extends AppCompatActivity {
    private static final int RESULT_LOAD_IMAGE = 121;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_face);
    }

    public void prosesImage(View v){
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RESULT_LOAD_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            ImageView praProses = (ImageView) findViewById(R.id.praProses);
            ImageView postProses = (ImageView) findViewById(R.id.postProses);

            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            Bitmap bmp = BitmapFactory.decodeFile(picturePath);
            NativeBitmap nativeBitmap = new NativeBitmap(bmp);
            //nativeBitmap.smooth();
            nativeBitmap.grayscaleBitmap();

            int w = bmp.getWidth(); int h = bmp.getHeight();

            float mask[][] = new float[3][];

            mask[0] = new float[]{-1, 0, 1};
            mask[1] = new float[]{-1, 0, 1};
            mask[2] = new float[]{-1, 0, 1};

            nativeBitmap.applyDivergence();

            praProses.setImageBitmap(bmp);
            postProses.setImageBitmap(nativeBitmap.draw());

            boolean[][] boolImage = nativeBitmap.convertToBoolmage();


        }
    }

}
