package com.ganesus.facevision;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainMenu extends AppCompatActivity {


    public void pilihAktivitas(View v){
        int kode = Integer.parseInt(v.getTag().toString());
        Intent i = null;

        switch (kode){
            case 1:
                i = new Intent(this, SimpleFace.class);
                break;
            case 2:
                i = new Intent(this, SimpleFace.class);
                break;
        }

        startActivity(i);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
    }

}
