package com.dev_marinov.wnfxtest2;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

class AdapterListPhoto extends RecyclerView.Adapter<AdapterListPhoto.HolderListPhoto>{

    Context context;
    HashMap<Integer, ObjectListPhoto> hashMapListPhoto;

    public AdapterListPhoto(Context context, HashMap<Integer, ObjectListPhoto> hashMapListPhoto) {
        this.context = context;
        this.hashMapListPhoto = hashMapListPhoto;
    }

    @NonNull
    @Override
    public AdapterListPhoto.HolderListPhoto onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_list_photo,parent,false);
        AdapterListPhoto.HolderListPhoto holderListPhoto = new AdapterListPhoto.HolderListPhoto(view);
        return holderListPhoto;
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterListPhoto.HolderListPhoto holderListPhoto, int position) {

//        Log.e("adapter","-position-" + position);
//        // вариант 1
//        String url_part = "https://lh3.googleusercontent.com/places/";
//        String url_hashMap = hashMapListPhoto.get(1).img_photo_reference;
//        Log.e("adapter","-url_hashMap-" + url_hashMap);
//        String itog = url_part + url_hashMap;
//        Log.e("adapter","-itog-" + itog);
//        // вариант 2
//
//
//       // String url_part1 = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=";
//        String apiKey_googleMap_marker = "AIzaSyDbjAPSWXxoup3RRkEYWb1RupO1fhUBBlg";
//        Picasso.get().load(url_part + hashMapListPhoto.get(1).img_photo_reference)
//                .memoryPolicy(MemoryPolicy.NO_CACHE).into(holderListPhoto.img_list_photo);
//
//        String str = String.valueOf(Picasso.get().load(url_part + hashMapListPhoto.get(position).img_photo_reference)
//                .memoryPolicy(MemoryPolicy.NO_CACHE));
//        Log.e("adapter","-str-" + str);

        String url_part1 = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=";
        String url_part2 = "&key=AIzaSyDbjAPSWXxoup3RRkEYWb1RupO1fhUBBlg";

        Picasso.get().load(url_part1 + hashMapListPhoto.get(position).img_photo_reference_1 + url_part2)
                .memoryPolicy(MemoryPolicy.NO_CACHE).into(holderListPhoto.img_list_photo_1);


    }

    @Override
    public int getItemCount() {
        Log.e("adapter","-hashMapListPhoto.size()-" + hashMapListPhoto.size());
        return hashMapListPhoto.size();
    }

    public class HolderListPhoto extends RecyclerView.ViewHolder {

        ImageView img_list_photo_1;

        public HolderListPhoto(@NonNull View itemView) {
            super(itemView);
            img_list_photo_1 = itemView.findViewById(R.id.img_list_photo_1);
        }
    }

}
