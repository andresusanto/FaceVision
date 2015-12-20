package com.ganesus.facevision;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

import com.ganesus.facevision.engine.NativeBitmap;

import java.util.ArrayList;
import java.util.List;

public class FaceEdgeActivity extends AppCompatActivity {
    private static final int RESULT_LOAD_IMAGE = 121;

    private static final int PREWETT_ALGO = 0;
    private static final int ROBINSON_ALGO = 1;
    private static final int ROBERT_ALGO = 2;
    private static final int KIRSCH_ALGO = 3;
    private static final int SOBEL_ALGO = 4;
    private int algorithmChoice;

    private List<Double[][]> maskMatrixes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_edge);

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.algorithm, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                switch(pos) {
                    case PREWETT_ALGO:
                        choosePrewitt();
                        algorithmChoice = PREWETT_ALGO;
                        break;
                    case ROBINSON_ALGO:
                        chooseRobinson();
                        algorithmChoice = ROBINSON_ALGO;
                        break;
                    case ROBERT_ALGO:
                        chooseRobert();
                        algorithmChoice = ROBERT_ALGO;
                        break;
                    case KIRSCH_ALGO:
                        chooseKirch();
                        algorithmChoice = KIRSCH_ALGO;
                        break;
                    case SOBEL_ALGO:
                        chooseSobel();
                        algorithmChoice = SOBEL_ALGO;
                        break;

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                choosePrewitt();
            }
        });
    }

    public void prosesImage(View v){
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RESULT_LOAD_IMAGE);
    }

    public void choosePrewitt() {
        maskMatrixes.clear();
        Double mask[][] = new Double[3][];
        Double masky[][] = new Double[3][];

        mask[0] = new Double[]{-1., 0., 1.};
        mask[1] = new Double[]{-1., 0., 1.};
        mask[2] = new Double[]{-1., 0., 1.};

        masky[0] = new Double[]{-1.,-1.,-1.};
        masky[1] = new Double[]{0., 0., 0.};
        masky[2] = new Double[]{ 1., 1., 1.};
        maskMatrixes.add(mask);
        maskMatrixes.add(masky);
    }

    public void chooseRobinson() {

    }

    public void chooseSobel() {
        maskMatrixes.clear();
        Double mask[][] = new Double[3][];
        Double masky[][] = new Double[3][];

        mask[0] = new Double[]{-1., 0., 1.};
        mask[1] = new Double[]{-2., 0., 2.};
        mask[2] = new Double[]{-1., 0., 1.};

        masky[0] = new Double[]{-1.,-2.,-1.};
        masky[1] = new Double[]{0., 0., 0.};
        masky[2] = new Double[]{ 1., 2., 1.};
        maskMatrixes.add(mask);
        maskMatrixes.add(masky);
    }

    public void chooseKirch() {
        Double kirch[][] = new Double[3][];
        kirch[0] = new Double[]{+5.,+5.,+5.};
        kirch[1] = new Double[]{-3.,+0.,-3.};
        kirch[2] = new Double[]{-3.,-3.,-3.};
        maskMatrixes.add(kirch);
    }

    public void chooseRobert() {
        Double mask[][] = new Double[3][];
        Double masky[][] = new Double[3][];
        mask[0] = new Double[]{+1., 0., 0.};
        mask[1] = new Double[]{ 0.,-1., 0.};
        mask[2] = new Double[]{ 0., 0., 0.};

        masky[0] = new Double[]{ 0.,+1.,0.};
        masky[1] = new Double[]{-1., 0.,0.};
        masky[2] = new Double[]{ 0., 0.,0.};
        maskMatrixes.add(mask);
        maskMatrixes.add(masky);
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

            nativeBitmap1.grayscaleBitmap();
            nativeBitmap2.grayscaleBitmap();

            for(int i = 0; i < 4; i++)
                nativeBitmap2.smooth();

            if (algorithmChoice == KIRSCH_ALGO){
                try {
                    nativeBitmap1.applySecondOrder(maskMatrixes.get(0));
                    nativeBitmap2.applySecondOrder(maskMatrixes.get(0));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                nativeBitmap1.applyFirstOrderMax(maskMatrixes);
                nativeBitmap2.applyFirstOrderMax(maskMatrixes);
            }


            praProses.setImageBitmap(bmp);
            postProses1.setImageBitmap(nativeBitmap1.draw());
            postProses2.setImageBitmap(nativeBitmap2.draw());



        }
    }

}
