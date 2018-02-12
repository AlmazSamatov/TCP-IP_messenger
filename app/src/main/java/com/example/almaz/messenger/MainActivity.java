package com.example.almaz.messenger;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * Created by Almaz on 31.10.2017.
 */

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    public void clientOnClick(View v) {
        Intent intent = new Intent(getBaseContext(), ClientActivity.class);
        startActivity(intent);
    }

    public void serverOnClick(View v) {
        Intent intent = new Intent(getBaseContext(), ServerActivity.class);
        startActivity(intent);
    }
}
