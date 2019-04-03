package com.example.johndoe.najamstanova;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.synnapps.carouselview.CarouselView;

public class MessageHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    final TextView chatBubbleLeft;
    final TextView chatBubbleRight;
    final TextView vrijemeLijevo;
    final TextView vrijemeDesno;
    final ImageView slikaLijevo;
    final ImageView slikaDesno;
    private final Context context;
    final CarouselView carouselView;

    public MessageHolder(View itemView, Context context) {
        super(itemView);
        this.context = context;
        itemView.setOnClickListener(this);

        // korisnikova poruka je na desno, poruka od druge osobe je lijevo
        chatBubbleLeft = itemView.findViewById(R.id.text_view_chat_bubble_left);
        chatBubbleRight = itemView.findViewById(R.id.text_view_chat_bubble_right);
        vrijemeLijevo = itemView.findViewById(R.id.vrijeme_lijevo);
        vrijemeDesno = itemView.findViewById(R.id.vrijeme_desno);
        slikaLijevo = itemView.findViewById(R.id.slika_lijevo);
        slikaDesno = itemView.findViewById(R.id.slika_desno);
        carouselView = itemView.findViewById(R.id.carouselView);
    }

    @Override
    public void onClick(View v) {

    }
}