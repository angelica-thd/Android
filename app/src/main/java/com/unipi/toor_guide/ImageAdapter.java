package com.unipi.toor_guide;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.makeramen.roundedimageview.RoundedImageView;

import java.util.List;


public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageAdapterHolder> {
    private List<Bitmap> images;
    private ViewPager2 viewPager2;

    public ImageAdapter(List<Bitmap> images, ViewPager2 viewPager2) {
        this.images = images;
        this.viewPager2 = viewPager2;
    }

    @NonNull
    @Override
    public ImageAdapterHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ImageAdapterHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.slide_img_container,parent,false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ImageAdapterHolder holder, int position) {
        holder.setImage(images.get(position));
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    class ImageAdapterHolder extends RecyclerView.ViewHolder{
       private RoundedImageView imageView;

       ImageAdapterHolder(@NonNull View itemView) {
           super(itemView);
           imageView = itemView.findViewById(R.id.imageslide);
       }

       void setImage(Bitmap img){
           imageView.setImageBitmap(img);
       }

   }
}
