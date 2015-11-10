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

import java.util.ArrayList;
import java.util.List;

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
            ImageView postProses1 = (ImageView) findViewById(R.id.postProses1);
            ImageView postProses2 = (ImageView) findViewById(R.id.postProses2);

            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            Bitmap bmp = BitmapFactory.decodeFile(picturePath);
            NativeBitmap nativeBitmap1 = new NativeBitmap(bmp);
            NativeBitmap nativeBitmap2 = new NativeBitmap(bmp);

            int w = bmp.getWidth(); int h = bmp.getHeight();

            Double mask[][] = new Double[3][];
            Double masky[][] = new Double[3][];

            mask[0] = new Double[]{-1., 0., 1.};
            mask[1] = new Double[]{-1., 0., 1.};
            mask[2] = new Double[]{-1., 0., 1.};

            masky[0] = new Double[]{-1.,-1.,-1.};
            masky[1] = new Double[]{0., 0., 0.};
            masky[2] = new Double[]{ 1., 1., 1.};

            Double kirch[][] = new Double[3][];
            kirch[0] = new Double[]{+5.,+5.,+5.};
            kirch[1] = new Double[]{-3.,+0.,-3.};
            kirch[2] = new Double[]{-3.,-3.,-3.};

            List<Double[][]> masks = new ArrayList<>();
            masks.add(mask); masks.add(masky);
            //nativeBitmap.applyDivergence();
            //nativeBitmap.applyMask(mask);
            nativeBitmap1.grayscaleBitmap();
            nativeBitmap2.grayscaleBitmap();

            nativeBitmap1.applyFirstOrder(masks);
            try {
                nativeBitmap2.applySecondOrder(kirch);
            } catch (Exception e) {
                e.printStackTrace();
            }


            praProses.setImageBitmap(bmp);
            postProses1.setImageBitmap(nativeBitmap1.draw());
            postProses2.setImageBitmap(nativeBitmap2.draw());



        }
    }

}
