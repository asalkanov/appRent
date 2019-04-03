package com.example.johndoe.najamstanova;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class StanoviLiked extends AppCompatActivity  {

    StanoviLikedAdapter adapter;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    ArrayList listaStanUID = new ArrayList();
    ArrayList listaCijena = new ArrayList();
    ArrayList listaPovrsina = new ArrayList();
    ArrayList listaUdaljenost = new ArrayList();
    ArrayList listaTrajanje = new ArrayList();
    ArrayList listaBrojLajkova = new ArrayList();
    ArrayList listaBrojKomentara = new ArrayList();
    ArrayList listaGlavnaSlika = new ArrayList();
    ArrayList<Stanovi> listaStanova = new ArrayList<>();
    Boolean postojiBaremJedan = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stanovi_liked);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mDatabase.child("Lajkovi").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Integer brojacLajkovaStana = (int) dataSnapshot.getChildrenCount();
                Integer brojacPetlje = 0;
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    brojacPetlje++;
                    if (Objects.requireNonNull(data.child("userID").getValue(String.class)).equals(mAuth.getUid())) {
                        postojiBaremJedan = true;
                        String stanID = data.child("stanID").getValue(String.class);
                        listaStanUID.add(stanID);
                        mDatabase.child("Stanovi").child(stanID).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Stanovi stan = dataSnapshot.getValue(Stanovi.class);
                                listaStanova.add(stan);
                                Log.d("comon", stan.getBrojSoba());
                                RecyclerView recyclerView = findViewById(R.id.recyclerView);
                                recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                                adapter = new StanoviLikedAdapter(StanoviLiked.this, listaStanova);
                                recyclerView.setAdapter(adapter);
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });
                        Log.d("radiovo", (String) data.child("stanID").getValue(String.class));
                    } else {
                        if (brojacPetlje == brojacLajkovaStana) {
                            if (listaStanova.size() == 0 && !postojiBaremJedan) {
                                Snackbar.make(findViewById(android.R.id.content), "Nema stanova koji Vam se sviđaju!", Snackbar.LENGTH_LONG)
                                        .setActionTextColor(Color.RED)
                                        .show();
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void onBackPressed() {
        Intent listaStanova = new Intent(StanoviLiked.this, ListaStanova.class);
        startActivity(listaStanova);
        super.onBackPressed();
    }
}