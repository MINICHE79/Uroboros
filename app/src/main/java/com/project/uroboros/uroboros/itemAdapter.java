package com.project.uroboros.uroboros;



import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by INSPIRON on 15/02/2018.
 */

public class itemAdapter extends ArrayAdapter {
    private Context context;
    private int LayoutR;
    private ArrayList<item> items;
    private int layout ;


    public itemAdapter(@NonNull Context context, int resource, ArrayList<item> items) {
        super(context, resource, items);
        this.LayoutR = resource;
        this.context = context;
        this.items = items;
        this.layout = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View item     = convertView;
        item user     = (item)getItem(position);
        if (item == null ){
            item= LayoutInflater.from(getContext()).inflate(this.layout, parent, false);
        }
        TextView tvName = (TextView) item.findViewById(R.id.thread);
        ImageView image = (ImageView) item.findViewById(R.id.image);
        // Populate the data into the template view using the data object
        tvName.setText(user.getName());
        image.setImageBitmap(user.getImage());

        // Return the completed view to render on screen
        return item;
    }

}