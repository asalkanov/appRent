package com.example.johndoe.najamstanova;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class UsersListAdapter extends RecyclerView.Adapter<UsersListHolder> {
    private final List<UsersListGetter> usersList;
    private final Context context;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;


    public UsersListAdapter(List<UsersListGetter> usersList, Context context) {
        this.usersList = usersList;
        this.context = context;
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Chat");
    }


    @Override
    public UsersListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_list, null);
        return new UsersListHolder(view, context);
    }

    @Override
    public void onBindViewHolder(UsersListHolder holder, int position) {
        if (usersList != null) {
            if (usersList.get(position).getSenderUid().equals(mAuth.getUid())) {
                // vlastita poruka korisnika
                holder.imeKorisnika.setText(usersList.get(holder.getAdapterPosition()).getReceiverName());
                if (!usersList.get(holder.getAdapterPosition()).getMessageBody().startsWith("https://firebasestorage.googleapis.com/")) {
                    holder.tekstPoruke.setText(usersList.get(holder.getAdapterPosition()).getMessageBody());
                } else {
                    holder.tekstPoruke.setText("Poslana je fotografija.");
                }
            } else {
                // poruka dobivena od druge osobe
                if (!usersList.get(holder.getAdapterPosition()).getMessageBody().startsWith("https://firebasestorage.googleapis.com/")) {
                    holder.imeKorisnika.setText(usersList.get(holder.getAdapterPosition()).getSenderName());
                    if (usersList.get(holder.getAdapterPosition()).getMessageRead().equals("false")) {      // znači da ima nepročitanih poruka
                        holder.tekstPoruke.setTypeface(null, Typeface.BOLD);
                        holder.imeKorisnika.setTypeface(null, Typeface.BOLD);
                        holder.tekstPoruke.setTextColor(ContextCompat.getColor(context, R.color.browser_actions_text_color));
                        holder.imeKorisnika.setTextColor(ContextCompat.getColor(context, R.color.browser_actions_text_color));
                    } else {
                        holder.tekstPoruke.setTypeface(null, Typeface.NORMAL);
                        holder.imeKorisnika.setTypeface(null, Typeface.NORMAL);
                        holder.tekstPoruke.setTextColor(ContextCompat.getColor(context, R.color.browser_actions_title_color));
                        holder.imeKorisnika.setTextColor(ContextCompat.getColor(context, R.color.browser_actions_title_color));
                    }
                    holder.tekstPoruke.setText(usersList.get(holder.getAdapterPosition()).getMessageBody());
                } else {
                    holder.tekstPoruke.setText("Primljena je fotografija.");
                }
            }
        }
        // kada korisnik klikne na jedan od razgovora u listi
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chat = new Intent(context, PrivateChat.class);
                if (usersList.get(holder.getAdapterPosition()).getReceiverUid().equals(mAuth.getUid())) {      // znaci da je trenutni korisnik bio primatelj, pa onda posalji UID od posiljatelja
                    chat.putExtra("PRIMATELJ", usersList.get(holder.getAdapterPosition()).getSenderUid());
                    chat.putExtra("LISTAKORISNIKA", "LISTAKORISNIKA");      // kako bi PrivateChat znao da li korisnik dolazi iz ChatUsersList ili iz DetaljiStanova
                } else {
                    chat.putExtra("PRIMATELJ", usersList.get(holder.getAdapterPosition()).getReceiverUid());
                    chat.putExtra("LISTAKORISNIKA", "LISTAKORISNIKA");
                }
                //oznaciPorukeKaoProcitane(holder.getAdapterPosition());
                chat.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                context.startActivity(chat);
            }
        });
    }

    @Override
    public int getItemCount() {
        return usersList == null ? 0 : usersList.size();
    }


    // označava poruke kao pročitane kada korisnik odabere nepročitanu kporuku u ChatUsersList i uđe u PrivateChat aktivnost
    public void oznaciPorukeKaoProcitane(Integer pozicija) {
        Log.d("procitano", usersList.get(pozicija).getSenderUid() + "<>" + mAuth.getUid() + " <> " + pozicija);
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot messageSnapshot : dataSnapshot.child(mAuth.getUid() + "_" + usersList.get(pozicija).getSenderUid()).getChildren()) {
                    Log.d("procitano", messageSnapshot.child("message_read").getValue().toString());
                    messageSnapshot.getRef().child("message_read").setValue("true");
                    Log.d("procitano", messageSnapshot.child("message_read").getValue().toString());
                }

                /* DRUGI SLUCAJ */
                for (DataSnapshot messageSnapshot : dataSnapshot.child(usersList.get(pozicija).getSenderUid() + "_" + mAuth.getUid()).getChildren()) {
                    Log.d("procitano", messageSnapshot.child("message_read").getValue().toString());
                    messageSnapshot.getRef().child("message_read").setValue("true");
                    Log.d("procitano", messageSnapshot.child("message_read").getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}