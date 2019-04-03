package com.example.johndoe.najamstanova;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;



public class UsersListHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    final TextView imeKorisnika;
    final TextView tekstPoruke;
    private final Context context;

    public UsersListHolder(View itemView, Context context) {
        super(itemView);
        this.context = context;
        itemView.setOnClickListener(this);

        imeKorisnika = itemView.findViewById(R.id.imeKorisnika);
        tekstPoruke = itemView.findViewById(R.id.tekstPoruke);
    }

    @Override
    public void onClick(View v) {

    }
}