package org.tensorflow.lite.examples.detection.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.tensorflow.lite.examples.detection.DetectorActivity;
import org.tensorflow.lite.examples.detection.R;
import org.tensorflow.lite.examples.detection.RipenessActivity;
import org.tensorflow.lite.examples.detection.adapter.FruitAdapter;
import org.tensorflow.lite.examples.detection.env.ImageUtils;
import org.tensorflow.lite.examples.detection.env.Utils;

import java.io.FileNotFoundException;
import java.io.IOException;

public class HomeFragment extends Fragment {
    public static final int GET_FROM_GALLERY = 3;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.home_fragment_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        FloatingActionButton realTimeImage = getView().findViewById(R.id.real_time);
        FloatingActionButton upload = getView().findViewById(R.id.gallery);
        realTimeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToDetectorActivity();
            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);
            }
        });

    }

    private void goToDetectorActivity() {
        Intent intent = new Intent(getActivity(), DetectorActivity.class);
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        //Detects request codes
        if(requestCode==GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImage);
                // make the size of the cropped image to 300x300 to fit our model
                Bitmap croppedBitmap = Utils.processBitmap(bitmap, 300);

                // output the image
//                int[] croppedFruitPixels = new int[300 * 300];
//                croppedBitmap.getPixels(croppedFruitPixels, 0, croppedBitmap.getWidth(), 0, 0, croppedBitmap.getWidth(), croppedBitmap.getHeight());
//
//                int pixel = 0;
//                int[][][][] ripenessInputArray = new int[1][300][300][3];
//
//                for (int i = 0; i < 300; ++i) {
//                    for (int j = 0; j < 300; ++j) {
//                        int index = 0;
//                        final int val = croppedFruitPixels[pixel++];
//                        ripenessInputArray[0][i][j][index++] = (((val >> 16) & 0xFF));
//                        ripenessInputArray[0][i][j][index++] = (((val >> 8) & 0xFF));
//                        ripenessInputArray[0][i][j][index++] = ((val & 0xFF));
//                        // Log.d("RipenessFruitArray", "[" + )
//                    }
//                }
//
//                int index = 0;
//                for(int i = 0; i < 300; i++)
//                {
//                    for(int j = 0; j < 300; j++)
//                    {
//                        for(int r = 0; r < 3; r++)
//                        {
//                            System.out.print(ripenessInputArray[0][i][j][r] + " ");
//                        }
//                        System.out.println();
//                    }
//                }

                ImageUtils.saveBitmap(bitmap, "screenshot.jpg");
                ImageUtils.saveBitmap(croppedBitmap, "croppedFruit.jpg");
                float[] temp = new float[2];
                temp[0] = 1;
                temp[1] = 1;

                String[] fruits = {"Apple",
                        "Mango",
                        "Orange",
                        "Tomato"};

                openFruitOptionDialog(fruits, temp);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void openFruitOptionDialog(String[] fruits, float[] croppedBoxRatios) {
        final Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.fruit_dialog_list_view);
        makeDialogBackgroundTransparent(dialog);
        ListView listView = dialog.findViewById(R.id.lv_assignment_users);
        TextView tv = dialog.findViewById(R.id.tv_popup_title);
        ArrayAdapter arrayAdapter = new FruitAdapter(getActivity(), R.layout.fruit_dialog_list_view, fruits);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener((adapterView, view, which, l) -> {
            Log.d("Dialog", "showAssignmentsList: " + fruits[which]);
            // send the fruitBitmap to the server
            goToRipenessActivity(fruits[which], croppedBoxRatios);
            dialog.dismiss();
        });
        dialog.show();
    }

    private void makeDialogBackgroundTransparent(Dialog dialog) {
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // this is optional
        }
    }

    private void goToRipenessActivity(String fruit, float[] croppedBoxRatios) {
        Intent intent = new Intent(getActivity(), RipenessActivity.class);
        intent.putExtra("fruitName", fruit);
        intent.putExtra("croppedBoxRatios", croppedBoxRatios);
        startActivity(intent);
    }

}
