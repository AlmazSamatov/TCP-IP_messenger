package com.example.almaz.messenger;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 * Created by Almaz on 31.10.2017.
 */

public class ServerMessengerActivity extends AppCompatActivity {

    private static final int READ_REQUEST_CODE = 42;
    private ListView listView;
    private ArrayList<String> arrayList;
    private MyCustomAdapter adapter;
    private Server server;
    private int serverPort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.messenger_activity);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        arrayList = new ArrayList<String>();

        final EditText editText = (EditText) findViewById(R.id.messageText);
        ImageButton send = (ImageButton)findViewById(R.id.send);
        ImageButton attach = (ImageButton)findViewById(R.id.attach);

        serverPort = getIntent().getIntExtra("Port", 4444);

        //relate the listView from java to the one created in xml
        listView = (ListView)findViewById(R.id.listView);
        adapter = new MyCustomAdapter(this, arrayList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String path = arrayList.get(position).substring(8);
                File file = new File(path);

                if(file.exists()){
                    open_file(file, view);
                }
            }
        });

        new connectTask().execute("");

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String message = editText.getText().toString();

                //add the text in the arrayList
                arrayList.add("server: " + message);

                //sends the message to the client
                if (server != null) {
                    try {
                        server.sendMessage(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                //refresh the list
                adapter.notifyDataSetChanged();
                editText.setText("");
            }
        });

        attach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
                // browser.
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

                // Filter to only show results that can be "opened", such as a
                // file (as opposed to a list of contacts or timezones)
                intent.addCategory(Intent.CATEGORY_OPENABLE);

                // Filter to show all files
                intent.setType("*/*");

                startActivityForResult(intent, READ_REQUEST_CODE);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                String nameOfFile = uri.getLastPathSegment().substring(uri.getLastPathSegment().
                        lastIndexOf('/')+1);
                String path = Environment.getExternalStoragePublicDirectory
                        (Environment.DIRECTORY_DOWNLOADS) + "/" + nameOfFile;

                try {
                    InputStream inputStream = getContentResolver().openInputStream(uri);
                    FileOutputStream fileOutputStream = new FileOutputStream(path);
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    //read from is to buffer
                    try {
                        while((bytesRead = inputStream.read(buffer)) > 0){
                            fileOutputStream.write(buffer, 0, bytesRead);
                        }
                        inputStream.close();
                        //flush OutputStream to write any buffered data to file
                        fileOutputStream.flush();
                        fileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                //add the text in the arrayList
                arrayList.add("server: " + path);

                try {
                    File file = new File(path);
                    server.sendFile(file);
                } catch (IOException e) {
                    Log.e("TCP Server", "S: Error while sending files");
                    e.printStackTrace();
                }

                //refresh the list
                adapter.notifyDataSetChanged();
            }
        }
    }


    public class connectTask extends AsyncTask<String,String,Client> {

        @Override
        protected Client doInBackground(String... message) {

            //we create a TCPClient object and
            try {
                server = new Server(new Server.OnMessageReceived() {
                    @Override
                    //here the messageReceived method is implemented
                    public void messageReceived(String message) {
                        //this method calls the onProgressUpdate
                        publishProgress(message);
                    }
                }, serverPort);
            } catch (IOException e) {
                e.printStackTrace();
            }
            server.run();

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            //in the arrayList we add the messaged received from server
            arrayList.add("client: " + values[0]);
            // notify the adapter that the data set has changed. This means that new message received
            // from server was added to the list
            adapter.notifyDataSetChanged();
        }
    }

    private void open_file(File file, View v) {

        // Get URI and MIME type of file
        Uri uri = Uri.fromFile(file);
        String mime = v.getContext().getContentResolver().getType(uri);

        // Open file with user selected app
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, mime);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        v.getContext().startActivity(intent);
    }
}
