package com.example.almaz.messenger;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

/**
 * Created by Almaz on 31.10.2017.
 */

public class ClientActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.client_mode);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    public void connectOnClick(View v) {
        Intent intent = new Intent(getBaseContext(), ClientMessengerActivity.class);
        EditText ip = (EditText) findViewById(R.id.ip_address_value_client);
        EditText port = (EditText) findViewById(R.id.port_value_client);
        intent.putExtra("IP", ip.getText().toString());
        intent.putExtra("Port", port.getText().toString());
        startActivity(intent);
    }
}
