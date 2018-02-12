package com.example.almaz.messenger;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Locale;

/**
 * Created by Almaz on 31.10.2017.
 */

public class ServerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.server_mode);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        WifiManager wifiMan = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInf = wifiMan.getConnectionInfo();
        int ipAddress = wifiInf.getIpAddress();
        String ip = String.format(Locale.ENGLISH, "%d.%d.%d.%d", (ipAddress & 0xff),(ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff),(ipAddress >> 24 & 0xff));

        TextView ipAddressValue = (TextView) findViewById(R.id.ip_address_value_server);
        ipAddressValue.setText(ip);
    }

    public void openOnClick(View v) {
        Intent intent = new Intent(getBaseContext(), ServerMessengerActivity.class);
        EditText port = (EditText) findViewById(R.id.port_value_server);
        intent.putExtra("Port", port.getText().toString());
        startActivity(intent);
    }
}
