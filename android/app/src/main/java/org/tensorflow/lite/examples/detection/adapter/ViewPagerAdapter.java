package org.tensorflow.lite.examples.detection.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.tensorflow.lite.examples.detection.R;

import java.util.List;

public class ViewPagerAdapter extends RecyclerView.Adapter<ViewPagerAdapter.Pager2ViewHolder> {

    List<String> headTexts, detailTexts;

    public ViewPagerAdapter(List<String> headers, List<String> details) {
        this.headTexts = headers;
        this.detailTexts = details;
    }


    @NonNull
    @Override
    public ViewPagerAdapter.Pager2ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_page, parent,false);

        return new Pager2ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewPagerAdapter.Pager2ViewHolder holder, int position) {
        holder.header.setText(headTexts.get(position));
        holder.detail.setText(detailTexts.get(position));
    }

    @Override
    public int getItemCount() {
        return headTexts.size();
    }

    public class Pager2ViewHolder extends RecyclerView.ViewHolder {

        // initialize variable
        TextView header, detail;

        public Pager2ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Assign variable
            header = itemView.findViewById(R.id.header_text);
            detail = itemView.findViewById(R.id.detail_text);
        }
    }
}
