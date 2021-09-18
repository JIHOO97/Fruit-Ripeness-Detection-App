package org.tensorflow.lite.examples.detection.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import org.tensorflow.lite.examples.detection.R;
import org.tensorflow.lite.examples.detection.StartingActivity;
import org.tensorflow.lite.examples.detection.adapter.ViewPagerAdapter;

import java.util.ArrayList;
import java.util.List;

import me.relex.circleindicator.CircleIndicator3;

public class InformationFragment extends Fragment {

    private List<String> titlesList;
    private List<String> detailsList;
    ViewPager2 viewPager2;
    ImageView imageView;

    String[] titles = {"Welcome!", "Center!", "Focus on the objects", "Notice!", "Update!"};
    String[] details = {"Thank you for using fripe.\nfripe helps you to identify 4 kinds of\nfruits about their ripeness",
                        "Take centered, well-lit photos of apple, mango,\norange, and tomato!",
                        "Make sure that the focus is on the fruit itself\nand not on the background of the image",
                        "Don't snap fruits that are too far of frame or\nfruit not belonging to the desired species\n(pot,ruler,etc.)",
                        "Peach, Lemon, Strawberry, Tomato, and Watermelon\nare coming soon. Even more fruits will be updated\nin the future."};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.info_fragment_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        titlesList = new ArrayList<>();
        detailsList = new ArrayList<>();

        addToList(titles, details);

        viewPager2 = getView().findViewById(R.id.view_pager2);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(titlesList, detailsList);
        viewPager2.setAdapter(viewPagerAdapter);
        CircleIndicator3 indicator3 = getView().findViewById(R.id.indicator);
        indicator3.setViewPager(viewPager2);
    }

    private void addToList(String[] titles, String[] descriptions) {
        for(int i = 0; i < titles.length; ++i) {
            titlesList.add(titles[i]);
            detailsList.add(descriptions[i]);
        }
    }

}
