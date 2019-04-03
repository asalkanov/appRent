package com.example.johndoe.najamstanova;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.firebase.FirebaseError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ChatUsersList extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private RecyclerView recyclerView;
    private FirebaseAuth mAuth;
    List<UsersListGetter> listaKorisnika = new ArrayList<>();
    SharedPreferences stanjeActivitya;      // Notifikacije se ne prikazuju ako je korisnik otvorio PrivateChat u Foregroundu
    SharedPreferences.Editor prefEditor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_users_list);

        recyclerView = findViewById(R.id.recycler_view_users_list);
        recyclerView.setAdapter(new UsersListAdapter(listaKorisnika, ChatUsersList.this));
        listaKorisnika.clear();

        // kada se korisnik vrati u ChatUsersList, PrivateChat više nije u Foregroundu
        stanjeActivitya = PreferenceManager.getDefaultSharedPreferences(ChatUsersList.this);
        prefEditor = stanjeActivitya.edit();
        prefEditor.putBoolean("isInForeground", false);
        prefEditor.apply();

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Chat");

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());

        // automatski osvjezi RecyclerView kada se podaci na serveru promijene
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                   listaKorisnika.clear();
                   for (DataSnapshot dijete : dataSnapshot.getChildren()) {
                       DatabaseReference usersListDatabase = FirebaseDatabase.getInstance().getReference().child("Chat").child(dijete.getKey());
                       usersListDatabase.limitToLast(1).addValueEventListener(new ValueEventListener() {
                           @Override
                           public void onDataChange(@NonNull DataSnapshot dataSnapshotNew) {
                               for (DataSnapshot poruka : dataSnapshotNew.getChildren()) {
                                   if (poruka.child("sender_uid").getValue().equals(mAuth.getUid()) || poruka.child("receiver_uid").getValue().equals(mAuth.getUid())) {        // poruke gdje je trenutni korisnik ili posiljatelj ili primatelj
                                       Log.d("korisbnk", mAuth.getUid());
                                       listaKorisnika.add(
                                               new UsersListGetter(
                                                       // DOBIVANJE VEC POSLANIH PORUKA U RECYCLERVIEW
                                                       poruka.child("message_id").getValue().toString(),
                                                       poruka.child("sender_uid").getValue().toString(),      // sender uid
                                                       poruka.child("sender_name").getValue().toString(),     // sender name
                                                       poruka.child("receiver_uid").getValue().toString(),    // receiver uid
                                                       poruka.child("receiver_name").getValue().toString(),    // receiver name
                                                       poruka.child("message_body").getValue().toString(),
                                                       poruka.child("message_read").getValue().toString()
                                               ));
                                   }
                                   if (recyclerView != null) {
                                       recyclerView.setLayoutManager(linearLayoutManager);
                                       recyclerView.setAdapter(new UsersListAdapter(listaKorisnika, ChatUsersList.this));
                                   }
                               }
                           }
                           @Override
                           public void onCancelled(@NonNull DatabaseError databaseError) {
                           }
                       });
                   }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent chatUsersList = new Intent(ChatUsersList.this, MainActivity.class);
        startActivity(chatUsersList);
    }
}
