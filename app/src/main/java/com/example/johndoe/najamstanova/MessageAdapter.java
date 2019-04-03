package com.example.johndoe.najamstanova;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.ceylonlabs.imageviewpopup.ImagePopup;
import com.google.firebase.auth.FirebaseAuth;
import com.synnapps.carouselview.ImageListener;

import java.util.List;

import static com.example.johndoe.najamstanova.GlideOptions.fitCenterTransform;


public class MessageAdapter extends RecyclerView.Adapter<MessageHolder> {
    private final List<MessageItemGetter> conversationThread;
    private final Context context;
    private FirebaseAuth mAuth;


    public MessageAdapter(List<MessageItemGetter> conversationThread, Context context) {
        this.conversationThread = conversationThread;
        this.context = context;
        mAuth = FirebaseAuth.getInstance();
    }


    @Override
    public MessageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_bubbles, null);
        return new MessageHolder(view, context);
    }

    @Override
    public void onBindViewHolder(MessageHolder holder, int position) {
        if (conversationThread != null)
            if (conversationThread.get(holder.getAdapterPosition()).getSenderUid().equals(mAuth.getUid())) {
                // vlastita poruka korisnika
                if (conversationThread.get(holder.getAdapterPosition()).getMessageBody().startsWith("https://firebasestorage.googleapis.com/")) {      // znaci da je poruka slika, jer tekst poruke počinje ovako
                    holder.slikaLijevo.setVisibility(View.INVISIBLE);
                    holder.slikaDesno.setVisibility(View.VISIBLE);
                    Glide.with(context).load(conversationThread.get(holder.getAdapterPosition()).getMessageBody()).into(holder.slikaDesno);
                } else {
                    holder.chatBubbleLeft.setVisibility(View.INVISIBLE);
                    holder.chatBubbleRight.setVisibility(View.VISIBLE);
                    holder.chatBubbleRight.setText(conversationThread.get(holder.getAdapterPosition()).getMessageBody());
                }
            } else {
                // poruka dobivena od druge osobe
                if (conversationThread.get(holder.getAdapterPosition()).getMessageBody().startsWith("https://firebasestorage.googleapis.com/")) {      // znaci da je poruka slika, jer tekst poruke počinje ovako
                    holder.slikaDesno.setVisibility(View.INVISIBLE);
                    holder.slikaLijevo.setVisibility(View.VISIBLE);
                    Glide.with(context).load(conversationThread.get(holder.getAdapterPosition()).getMessageBody()).into(holder.slikaLijevo);
                } else {
                    holder.chatBubbleRight.setVisibility(View.INVISIBLE);
                    holder.chatBubbleLeft.setVisibility(View.VISIBLE);
                    holder.chatBubbleLeft.setText(conversationThread.get(holder.getAdapterPosition()).getMessageBody());
                }
            }

            // slika u Chatu se poveća kada korisnik klikne na nju -- slika desno
            holder.slikaDesno.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Dialog slikaDialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen) {
                        @Override
                        public boolean onTouchEvent(MotionEvent event) {
                            this.dismiss();     // zatvara slikaDialog klikom bilo gdje na ekran
                            return true;
                        }
                    };
                    slikaDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    slikaDialog.setCancelable(true);
                    slikaDialog.setContentView(R.layout.image_chat_dialog);
                    ImageView slikaZaDialog = slikaDialog.findViewById(R.id.slikaDialog);
                    Glide.with(context).load(conversationThread.get(holder.getAdapterPosition()).getMessageBody()).into(slikaZaDialog);
                    slikaDialog.show();
                }
            });

        // slika u Chatu se poveća kada korisnik klikne na nju -- slika lijevo
        holder.slikaLijevo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog slikaDialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen) {
                    @Override
                    public boolean onTouchEvent(MotionEvent event) {
                        this.dismiss();     // zatvara slikaDialog klikom bilo gdje na ekran
                        return true;
                    }
                };
                slikaDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                slikaDialog.setCancelable(true);
                slikaDialog.setContentView(R.layout.image_chat_dialog);
                ImageView slikaZaDialog = slikaDialog.findViewById(R.id.slikaDialog);
                Glide.with(context).load(conversationThread.get(holder.getAdapterPosition()).getMessageBody()).into(slikaZaDialog);
                slikaDialog.show();
            }
        });


            // prikaz vremena slanja poruke -- vrijeme lijevo
            holder.chatBubbleLeft.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (holder.vrijemeLijevo.getVisibility() == View.GONE) {       // ako vrijeme slanja poruke nije prikazano, prikaži ga
                        holder.vrijemeDesno.setVisibility(View.GONE);
                        holder.vrijemeLijevo.setVisibility(View.VISIBLE);
                        holder.vrijemeLijevo.setText(conversationThread.get(holder.getAdapterPosition()).getMessageTimestamp());
                        holder.vrijemeLijevo.startAnimation(AnimationUtils.loadAnimation(context, R.anim.vrijeme_chat_in));
                    } else {
                        holder.vrijemeLijevo.startAnimation(AnimationUtils.loadAnimation(context, R.anim.vrijeme_chat_out));
                        holder.vrijemeLijevo.setVisibility(View.GONE);
                    }
                }
            });
            // prikaz vremena slanja poruke -- vrijeme desno
            holder.chatBubbleRight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (holder.vrijemeDesno.getVisibility() == View.GONE) {
                        holder.vrijemeLijevo.setVisibility(View.GONE);
                        holder.vrijemeDesno.setVisibility(View.VISIBLE);
                        holder.vrijemeDesno.setText(conversationThread.get(holder.getAdapterPosition()).getMessageTimestamp());
                        holder.vrijemeDesno.startAnimation(AnimationUtils.loadAnimation(context, R.anim.vrijeme_chat_in));
                    } else {
                        holder.vrijemeDesno.startAnimation(AnimationUtils.loadAnimation(context, R.anim.vrijeme_chat_out));
                        holder.vrijemeDesno.setVisibility(View.GONE);
                    }
                }
            });
    }

    @Override
    public int getItemCount() {
        return conversationThread.size();
    }
}