package org.tensorflow.lite.examples.detection.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.tensorflow.lite.examples.detection.R;

import java.util.List;

public class FruitAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final String[] userList;

    public FruitAdapter(@NonNull Context context, int resource, @NonNull String[] objects) {
        super(context, resource, objects);
        userList = objects;
        this.context = context;
    }

    @SuppressLint("ViewHolder")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.item_layout_dialog, parent, false);
        TextView fruitName = rowView.findViewById(R.id.fruit_name);
        fruitName.setTextColor(Color.WHITE);
        String fruit = userList[position];

        fruitName.setText(fruit);

        return rowView;
    }

}
