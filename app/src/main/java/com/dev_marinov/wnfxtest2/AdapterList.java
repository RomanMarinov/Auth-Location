package com.dev_marinov.wnfxtest2;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.auth.User;

import java.util.HashMap;

class AdapterList extends RecyclerView.Adapter<AdapterList.HolderList> {
    Context context;
    HashMap<Integer, ObjectList> hashMapList;

    public AdapterList(Context context, HashMap<Integer, ObjectList> hashMapList) {
        this.context = context;
        this.hashMapList = hashMapList;
    }

    @NonNull
    @Override
    public HolderList onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.row_list_food,parent,false);
        HolderList holderList = new HolderList(view);
        return holderList;
    }

    @Override
    public void onBindViewHolder(@NonNull HolderList holderList, int position) {

        Log.e("onBindViewHolder","-myname-");

//        holderList.tv_name_1.setText(hashMapList.get(position).str_name_1);
//        holderList.tv_name_2.setText(hashMapList.get(position).str_name_2);
//        holderList.tv_name_3.setText(hashMapList.get(position).str_name_3);
//        holderList.tv_name_4.setText(hashMapList.get(position).str_name_4);

        ObjectList objectList = hashMapList.get(position);
        holderList.tv_name_1.setText(objectList.str_name_1);
        holderList.tv_name_2.setText(objectList.str_name_2);
        holderList.tv_name_3.setText(objectList.str_name_3);
        holderList.tv_name_4.setText(objectList.str_name_4);

    }



    @Override
    public int getItemCount() {
        Log.e("adapter","-hashMapList.size()-" + hashMapList.size());
        return hashMapList.size();
    }

    public class HolderList extends RecyclerView.ViewHolder {

            TextView tv_name_1, tv_name_2, tv_name_3, tv_name_4;

        public HolderList(@NonNull View itemView) {
            super(itemView);

            tv_name_1 = itemView.findViewById(R.id.tv_name_1);
            tv_name_2 = itemView.findViewById(R.id.tv_name_2);
            tv_name_3 = itemView.findViewById(R.id.tv_name_3);
            tv_name_4 = itemView.findViewById(R.id.tv_name_4);
        }
    }
}
