package org.tensorflow.lite.examples.detection.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import org.tensorflow.lite.examples.detection.R;
import org.tensorflow.lite.examples.detection.StartingActivity;
import org.tensorflow.lite.examples.detection.adapter.MyFruitAdapter;

import java.util.ArrayList;
import java.util.List;

public class MyFruitFragment extends Fragment {

    private static final int NUM_COLUMNS = 2;

    List<Bitmap> bitmapList;
    private RecyclerView myFruitRecyclerView;
    private TextView noFruitText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.myfruit_fragment_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        myFruitRecyclerView = getView().findViewById(R.id.myfruit_recyclerview);
        noFruitText = getView().findViewById(R.id.no_detected_fruit_text);
        bitmapList = new ArrayList<>();

        String bitmaps = StartingActivity.sharedPreferences.getString("bitmaps", "");
        splitAndSaveBitmapString(bitmaps);

        if(bitmapList != null) {
            StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(NUM_COLUMNS, LinearLayoutManager.VERTICAL);
            myFruitRecyclerView.setLayoutManager(staggeredGridLayoutManager);
            MyFruitAdapter myFruitAdapter = new MyFruitAdapter(bitmapList, getActivity());
            myFruitRecyclerView.setAdapter(myFruitAdapter);
        }
    }

    private void splitAndSaveBitmapString(String bitmaps) {
        String[] stringBitmapList = bitmaps.split("DES01");
        if(!stringBitmapList[0].equals("")) {
            noFruitText.setText("");
            for(int i = 0; i < stringBitmapList.length; ++i) {
                byte[] decode = Base64.decode(stringBitmapList[i].getBytes(), 1);
                Bitmap bit = BitmapFactory.decodeByteArray(decode, 0, decode.length);
                bitmapList.add(bit);
            }
        }
    }
}
