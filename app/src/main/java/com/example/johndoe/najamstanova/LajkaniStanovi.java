package com.example.johndoe.najamstanova;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.yarolegovich.lovelydialog.LovelyCustomDialog;

import java.util.ArrayList;
import java.util.Objects;

public class LajkaniStanovi extends AppCompatActivity {

    FirebaseRecyclerAdapter<Stanovi, StanoviHolder> FirebaseAdapter;
    private DatabaseReference mDatabase;
    private static final FirebaseDatabase DATABASE = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth;
    int spremljenaPozicija = 0;
    ArrayList<String> listaUIDstanova = new ArrayList<>();
    ArrayList<String> listaCijena= new ArrayList<>();
    ArrayList<String> listaLatitude = new ArrayList<>();
    ArrayList<String> listaLongitude = new ArrayList<>();
    RecyclerView mRecyclerView;
    ProgressBar progressDialog;
    ImageView emptyHouse;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stanovi_vlasnika);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mRecyclerView = findViewById(R.id.mRecView);

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        myToolbar.setBackgroundColor(getResources().getColor(R.color.GlavnaBoja));
        setSupportActionBar(myToolbar);

        emptyHouse = findViewById(R.id.emptyHouse);
        emptyHouse.setVisibility(View.GONE);

        progressDialog = findViewById(R.id.spin_kit);
        progressDialog.bringToFront();
        progressDialog.setVisibility(View.VISIBLE);

        listaUIDstanova.clear();
        listaCijena.clear();
        listaLatitude.clear();
        listaLongitude.clear();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());

        mRecyclerView.setLayoutManager(linearLayoutManager);
        RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(getApplicationContext()) {
            @Override protected int getVerticalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_START;
            }
        };



        Log.d("zastooo", "vel: " + String.valueOf(listaUIDstanova.size()));

        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("Stanovi")
                .orderByChild("brojLajkova")
                .startAt(1)
                .limitToLast(50);

        Log.d("zastooo", mAuth.getUid());

        FirebaseRecyclerOptions<Stanovi> opcijeFirebaseAdaptera =
                new FirebaseRecyclerOptions.Builder<Stanovi>()
                        .setQuery(query, Stanovi.class)
                        .build();

        FirebaseAdapter = new FirebaseRecyclerAdapter<Stanovi, StanoviHolder>(opcijeFirebaseAdaptera) {
            @NonNull
            @Override
            public StanoviHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.lista_stanova_izgled, parent, false);
                return new StanoviHolder(view);
            }
            @Override
            protected void onBindViewHolder(@NonNull StanoviHolder holder, int position, @NonNull final Stanovi stan) {
                holder.setNaziv(stan.getNaziv());
                holder.setCijena(String.valueOf(stan.getCijena()));
                holder.setSlika(stan.getGlavnaSlika(), getApplicationContext());       // učitavanje slike u Listi Stanova -- pomoću StanoviHolder

                listaUIDstanova.add(stan.getStanUID());
                listaCijena.add(String.valueOf(stan.getCijena()));
                listaLatitude.add(stan.getLatitude());
                listaLongitude.add(stan.getLongitude());

                Log.d("zastooo", stan.getStanUID());

                holder.setBrojLajkova(String.valueOf(stan.getBrojLajkova()).replace(".0", ""));
                holder.setBrojKomentara(String.valueOf(stan.getBrojKomentara()).replace(".0", ""));

                Log.d("ugasio", stan.getVlasnik() + "   " +  mAuth.getUid());


                holder.likeImage.setImageDrawable(getResources().getDrawable(R.drawable.thumb));
                holder.likeImage.setEnabled(false);

                holder.commentImage.setImageDrawable(getResources().getDrawable(R.drawable.comment));
                holder.commentImage.setEnabled(false);

                listaUIDstanova.add(stan.getStanUID());
                listaCijena.add(String.valueOf(stan.getCijena()));
                listaLatitude.add(stan.getLatitude());
                listaLongitude.add(stan.getLongitude());

                // kada korisnik odabere neku od kartica iz Liste Stanova
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        spremljenaPozicija = holder.getAdapterPosition();

                        Bundle podaci = new Bundle();
                        podaci.putString("UIDSTAN", stan.getStanUID());
                        podaci.putString("UIDVLASNIK", stan.getVlasnik());
                        podaci.putString("SVESLIKE", stan.getSlike());
                        podaci.putString("NAZIV", stan.getNaziv());
                        podaci.putString("CIJENA", String.valueOf(stan.getCijena()));
                        podaci.putString("POVRSINA", String.valueOf(stan.getPovrsina()));
                        podaci.putString("LATITUDE", stan.getLatitude());
                        podaci.putString("LONGITUDE", stan.getLongitude());
                        podaci.putString("BROJSOBA", stan.getBrojSoba());
                        podaci.putString("KUPAONICA", stan.getBrojKupaonica());
                        podaci.putString("TV", stan.getTV());
                        podaci.putString("KLIMA", stan.getKlima());
                        podaci.putString("RUBLJE", stan.getRublje());
                        podaci.putString("HLADNJAK", stan.getHladnjak());
                        podaci.putString("POSUDE", stan.getPosude());
                        podaci.putString("STEDNJAK", stan.getStednjak());
                        Intent detaljiStana = new Intent(LajkaniStanovi.this, DetaljiStanova.class);
                        detaljiStana.putExtras(podaci);
                        startActivity(detaljiStana, ActivityOptions.makeSceneTransitionAnimation(LajkaniStanovi.this).toBundle());

                    }
                });
            }
            @Override
            public void onDataChanged() {       // poziva se kada FirebaseAdapter zavrsi ucitavati podatke
                /*if (listaUIDstanova.size() > 0) {
                    emptyHouse.setVisibility(View.GONE);
                } else {
                    emptyHouse.setVisibility(View.VISIBLE);
                }*/
                if (FirebaseAdapter.getItemCount() == 0) {      // ako ne postoje stanovi vlasnika
                    Snackbar.make(findViewById(android.R.id.content), "Nema dodanih stanova!", Snackbar.LENGTH_LONG)
                            .setActionTextColor(Color.RED)
                            .show();
                }
                progressDialog.setVisibility(View.GONE);
                linearLayoutManager.scrollToPositionWithOffset(spremljenaPozicija, (int) getResources().getDimension(R.dimen.offset_recyclerview));
                Log.d("zastooo", String.valueOf(listaUIDstanova.size()) + " onValueChanged");

            }

        };
        mRecyclerView.setAdapter(FirebaseAdapter);

    }


    @Override
    protected void onStart() {
        progressDialog.setVisibility(View.VISIBLE);
        super.onStart();
        FirebaseAdapter.startListening();
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //recyclerViewState = mRecyclerView.getLayoutManager().onSaveInstanceState();
        FirebaseAdapter.stopListening();
    }
}
