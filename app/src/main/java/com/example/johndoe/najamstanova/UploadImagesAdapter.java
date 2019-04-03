package com.example.johndoe.najamstanova;

import android.content.Context;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Task;

import java.util.List;

public class UploadImagesAdapter extends RecyclerView.Adapter<UploadImagesAdapter.ViewHolder>{

    Context context;
    public List<Uri> fileNameList;
    public List<String> fileDoneList;
    public List<String> fileURLList;


    public UploadImagesAdapter(Context context, List<Uri> fileNameList, List<String> fileDoneList, List<String> fileURLList){

        this.context = context;
        this.fileDoneList = fileDoneList;
        this.fileNameList = fileNameList;
        this.fileURLList = fileURLList;

    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public TextView fileNameView;
        public ImageView fullscreen_content;

        public ViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            //uploadIcon = mView.findViewById(R.id.upload_icon);
            //fullscreen_image = mView.findViewById(R.id.fullscreen_content);
            fullscreen_content = mView.findViewById(R.id.view_pager);

        }

    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.upload_images_izgled, parent, false);
        return new ViewHolder(v);

    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        //String fileName = fileNameList.get(holder.getAdapterPosition());

        //String fileDone = fileDoneList.get(holder.getAdapterPosition());

        //if(fileDone.equals("uploading")){

            //Glide.with(context).load(R.drawable.common_full_open_on_phone).into(holder.fullscreen_content);
            //Glide.with(context).load(R.drawable.common_google_signin_btn_icon_dark).into(holder.uploadIcon);


        //} else {

        Glide.with(context).load(fileNameList.get(holder.getAdapterPosition())).into(holder.fullscreen_content);

            //Glide.with(context).load(fileURLList.get(holder.getAdapterPosition())).into(holder.fullscreen_content);
            //Glide.with(context).load(fileURLList.get(position)).into(holder.uploadIcon);
            Log.d("slika", fileURLList.get(holder.getAdapterPosition()));    // holder.getAdapterPosition() umjesto int position !

        //}

    }

    @Override
    public int getItemCount() {
        return fileNameList.size();
    }


}
