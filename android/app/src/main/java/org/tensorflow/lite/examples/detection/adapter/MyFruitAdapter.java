package org.tensorflow.lite.examples.detection.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.tensorflow.lite.examples.detection.R;
import org.tensorflow.lite.examples.detection.StartingActivity;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class MyFruitAdapter extends RecyclerView.Adapter<MyFruitAdapter.CustomViewHolder> {

    List<Bitmap> bitmapList;
    Activity mActivity;

    public MyFruitAdapter(List<Bitmap> bitmapList, Activity activity) {
        this.bitmapList = bitmapList;
        this.mActivity = activity;
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.myfruit_item_list, parent,false);

        return new MyFruitAdapter.CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {
        holder.savedFruitImage.setImageBitmap(this.bitmapList.get(position));

        holder.deleteImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeItem(holder.getAdapterPosition());
                Log.d("myFruitAdapter", "removing position is: " + holder.getAdapterPosition());
            }
        });

        holder.savedFruitImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("myFruitAdapter", "selectedPosition is: " + position);
                if(position < bitmapList.size()) {
                    openMyFruitDialog(bitmapList.get(position));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return bitmapList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {

        // initialize variable
        ImageView savedFruitImage, deleteImg;

        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);
            // assign variables
            savedFruitImage = itemView.findViewById(R.id.saved_fruit_image);
            deleteImg = itemView.findViewById(R.id.delete_icon_img);
        }
    }

    private void removeItem(int position) {
        try {
            bitmapList.remove(position);

            String newSharedPreferenceString = "";
            // add new bitmap strings
            for(int i = 0; i < bitmapList.size(); ++i) {
                // convert bitmap to string
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmapList.get(i).compress(Bitmap.CompressFormat.JPEG, 99, byteArrayOutputStream);
                String stringBitmap = new String(Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT));

                if(i != bitmapList.size() - 1) {
                    newSharedPreferenceString += (stringBitmap + "DES01");
                } else {
                    newSharedPreferenceString += stringBitmap;
                }
            }

            SharedPreferences.Editor editor = StartingActivity.sharedPreferences.edit();
            editor.putString("bitmaps", newSharedPreferenceString);
            editor.apply();

            notifyItemRemoved(position);
        } catch(IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    private void openMyFruitDialog(Bitmap bitmap) {
        final Dialog dialog = new Dialog(mActivity);
        dialog.setContentView(R.layout.myfruit_display_dialog);
        makeDialogBackgroundTransparent(dialog);
        ImageView myFruitImage = dialog.findViewById(R.id.myfruit_image);
        myFruitImage.setImageBitmap(bitmap);
        dialog.show();
    }

    private void makeDialogBackgroundTransparent(Dialog dialog) {
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // this is optional
        }
    }
}
