package com.ganesus.facevision;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainMenu extends AppCompatActivity {


    public void pilihAktivitas(View v){
        int kode = Integer.parseInt(v.getTag().toString());
        Intent i = null;

        switch (kode){
            case 1:
                i = new Intent(this, FaceEdgeActivity.class);
                break;
            case 2:
                i = new Intent(this, SimpleFace.class);
                break;
            case 3:
                i = new Intent(this, FaceCounter.class);
                break;
            case 4:
                i = new Intent(this, FFTActivity.class);
                break;

        }

        startActivity(i);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);


        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

}
