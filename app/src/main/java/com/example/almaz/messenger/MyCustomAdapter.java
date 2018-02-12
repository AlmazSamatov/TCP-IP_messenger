package com.example.almaz.messenger;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import static android.support.v4.content.ContextCompat.startActivity;

public class MyCustomAdapter extends BaseAdapter {
    private ArrayList<String> listItems;
    private LayoutInflater layoutInflater;

    public MyCustomAdapter(Context context, ArrayList<String> arrayList){

        listItems = arrayList;

        //get the layout inflater
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        //getCount() represents how many items are in the list
        return listItems.size();
    }

    @Override
    //get the data of an item from a specific position
    //i represents the position of the item in the list
    public Object getItem(int i) {
        return null;
    }

    @Override
    //get the position id of the item from the list
    public long getItemId(int i) {
        return 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {

        //check to see if the reused view is null or not, if is not null then reuse it
        if (view == null) {
            view = layoutInflater.inflate(R.layout.list_item, null);
        }

        //get the string item from the position "position" from array list to put it on the TextView
        final String stringItem = listItems.get(position);
        if (stringItem != null) {

            final TextView itemName = (TextView) view.findViewById(R.id.list_item_text_view);

            if (itemName != null) {

                //set the item name on the TextView
                itemName.setText(stringItem);
            }
        }

        //this method must return the view corresponding to the data at the specified position.
        return view;

    }
}