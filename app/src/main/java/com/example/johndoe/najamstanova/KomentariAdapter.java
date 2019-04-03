package com.example.johndoe.najamstanova;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ThrowOnExtraProperties;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

public class KomentariAdapter extends RecyclerView.Adapter<KomentariAdapter.ViewHolder> {

    Context mContext;
    private List<String> listaIDkomentara;
    private List<String> listaIDstana;
    private List<String> listaIDkorisnika;
    private List<String> listaBrojKomentara;
    private List<String> listaKomentara;
    private List<String> listaImeKorisnikaKomentar;
    private List<String> listaDatumKomentiranja;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseStanovi;
    private DatabaseReference mDatabaseKomentari;

    // data is passed into the constructor
    KomentariAdapter(Context context, ArrayList<String> komentarID, ArrayList<String> stanID, ArrayList<String> korisnikID, ArrayList<String> brojKomentara, List<String> komentari, ArrayList<String> imenaKorisnika, ArrayList<String> datumiKomentiranja) {
        this.mInflater = LayoutInflater.from(context);
        this.mContext = context;
        this.listaIDkomentara = komentarID;
        this.listaIDstana = stanID;
        this.listaIDkorisnika = korisnikID;
        this.listaBrojKomentara = brojKomentara;
        this.listaKomentara = komentari;
        this.listaImeKorisnikaKomentar = imenaKorisnika;
        this.listaDatumKomentiranja = datumiKomentiranja;
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("LajkoviKomentara");
        mDatabaseStanovi = FirebaseDatabase.getInstance().getReference().child("Stanovi");
        mDatabaseKomentari = FirebaseDatabase.getInstance().getReference().child("Komentari");
    }



    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView komentariTV, imenaKorisnikaTV, datumiKomentiranjaTV, komentarIDTV;
        ImageButton komentarLike;
        TextView komentarBroj;

        ViewHolder(View itemView) {
            super(itemView);
            komentarLike = itemView.findViewById(R.id.komentarLajk);
            komentarBroj = itemView.findViewById(R.id.komentarBroj);
            komentariTV = itemView.findViewById(R.id.komentar);
            komentarIDTV = itemView.findViewById(R.id.komentarID);
            imenaKorisnikaTV = itemView.findViewById(R.id.korisnik);
            datumiKomentiranjaTV = itemView.findViewById(R.id.datum);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String stanUID = listaIDstana.get(position);
        String komentar = listaKomentara.get(position);
        String imeKorisnika = listaImeKorisnikaKomentar.get(position);
        String datumKomentiranja = listaDatumKomentiranja.get(position);
        String komentarID = listaIDkomentara.get(position);
        holder.komentariTV.setText(komentar);
        holder.imenaKorisnikaTV.setText(imeKorisnika);
        holder.datumiKomentiranjaTV.setText(datumKomentiranja);
        holder.komentarIDTV.setText(komentarID);

        // dohvati broj lajkova za određeni koemntar iz 'Komentari'
        mDatabaseKomentari.child(komentarID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                holder.komentarBroj.setText(Objects.requireNonNull(dataSnapshot.child("brojLajkovaKomentara").getValue()).toString());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        // ako je trenutni korisnik već lajako komentar, promijeni boju ikone za lajkanje
        mDatabase.child(mAuth.getUid()+"**"+komentarID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    holder.komentarLike.setImageDrawable(mContext.getResources().getDrawable(R.drawable.arrow_up_liked));
                } else {
                    holder.komentarLike.setImageDrawable(mContext.getResources().getDrawable(R.drawable.arrow_up));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        // kada korisnik klikne dugme za lajkanje komentara stanova
        holder.komentarLike.setOnClickListener(view ->
            mDatabase.child(mAuth.getUid()+"**"+komentarID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshotKomentara) {
                Log.d("svidanja", mAuth.getUid() + "**" + komentarID);
                if (!snapshotKomentara.exists()) {          // korisnik još nije lajkao stan
                    Log.d("svidana", "ne postoji");

                    mDatabaseKomentari.child(komentarID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshotStanovi) {
                            Log.d("hgjgh", dataSnapshotStanovi.child("userID").getValue(String.class) + "  " + mAuth.getUid());
                            if (Objects.requireNonNull(dataSnapshotStanovi.child("userID").getValue(String.class)).equals(mAuth.getUid())) {   // korisnik ne može lajkati vlastiti komentar
                                Toast.makeText(mContext, "Ne možete lajkati vlastiti komentar!", Toast.LENGTH_SHORT).show();
                            } else {
                                mDatabaseKomentari.child(komentarID).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        String trenutniBrojLajkovaLKomentara = Objects.requireNonNull(dataSnapshot.child("brojLajkovaKomentara").getValue()).toString();
                                        Log.d("svidanja", trenutniBrojLajkovaLKomentara);
                                        Integer povecaniBrojLajkova = Integer.valueOf(trenutniBrojLajkovaLKomentara) + 1;

                                        holder.komentarBroj.setText(String.valueOf(povecaniBrojLajkova));
                                        holder.komentarLike.setImageDrawable(mContext.getResources().getDrawable(R.drawable.arrow_up_liked));

                                        Map<String, Object> povecajBrojLajkovaKomentara = new HashMap<>();
                                        povecajBrojLajkovaKomentara.put("brojLajkovaKomentara", povecaniBrojLajkova);
                                        mDatabaseKomentari.child(komentarID).updateChildren(povecajBrojLajkovaKomentara);

                                        Map<String, Object> dodajLajkoveKomentar = new HashMap<>();
                                        dodajLajkoveKomentar.put("userID", mAuth.getUid());
                                        dodajLajkoveKomentar.put("stanID", listaIDstana.get(position));
                                        dodajLajkoveKomentar.put("brojLajkovaKomentara", povecaniBrojLajkova);
                                        mDatabase.child(mAuth.getUid()+"**"+komentarID).updateChildren(dodajLajkoveKomentar);
                                        Toast.makeText(mContext, "Komentar Vam se sviđa!", Toast.LENGTH_SHORT).show();
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


                    /*
                    mDatabase.child("LajkoviKomentara").child(mAuth.getUid()+"**"+komentarID).child("brojLajkovaKomentara").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {        // ako broj lajkova već postoji, znači da je netko već prije lajkao komentar
                                String trenutniBrojLajkova = Objects.requireNonNull(dataSnapshot.child("brojLajkovaKomentara").getValue()).toString();

                                Log.d("svidanja", trenutniBrojLajkova + " " + "prvi sam!");
                                Double povecaniBrojLajkova = Double.valueOf(trenutniBrojLajkova) + 1;

                                Map<String, Object> dodajLajkoveKomentar = new HashMap<>();
                                dodajLajkoveKomentar.put("userID", mAuth.getUid());
                                dodajLajkoveKomentar.put("stanID", listaIDstana.get(position));
                                dodajLajkoveKomentar.put("brojLajkovaKomentara", povecaniBrojLajkova);
                                mDatabase.child(mAuth.getUid()+"**"+komentarID).updateChildren(dodajLajkoveKomentar);      // zapisi u 'LajkoviKomentara' node u bazi koji je user lajkao koji komentar i broj lajkova
                                Toast.makeText(mContext, "Sviđa Vam se!", Toast.LENGTH_SHORT).show();
                            } else {        // nitko do sada nije lajkao ovaj komentar stana

                                Log.d("svidanja", "0" + " " + "drugi sam!");

                                Map<String, Object> dodajLajkoveKomentar = new HashMap<>();
                                dodajLajkoveKomentar.put("userID", mAuth.getUid());
                                dodajLajkoveKomentar.put("stanID", listaIDstana.get(position));
                                dodajLajkoveKomentar.put("brojLajkovaKomentara", 0);
                                mDatabase.child(mAuth.getUid()+"**"+komentarID).updateChildren(dodajLajkoveKomentar);      // zapisi u 'LajkoviKomentara' node u bazi koji je user lajkao koji komentar i broj lajkova
                                Toast.makeText(mContext, "Sviđa Vam se!", Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                    */
                } else {        // korisnik je već lajkao komentar, dislajkaj ga!
                    Log.d("svidana", "postoji");
                    mDatabaseKomentari.child(komentarID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String trenutniBrojLajkovaLKomentara = Objects.requireNonNull(dataSnapshot.child("brojLajkovaKomentara").getValue()).toString();
                            Log.d("svidanja", trenutniBrojLajkovaLKomentara);
                            Integer smanjeniBrojLajkova = Integer.valueOf(trenutniBrojLajkovaLKomentara) - 1;

                            holder.komentarBroj.setText(String.valueOf(smanjeniBrojLajkova));
                            holder.komentarLike.setImageDrawable(mContext.getResources().getDrawable(R.drawable.arrow_up));

                            Map<String, Object> povecajBrojLajkovaKomentara = new HashMap<>();
                            povecajBrojLajkovaKomentara.put("brojLajkovaKomentara", smanjeniBrojLajkova);
                            mDatabaseKomentari.child(komentarID).updateChildren(povecajBrojLajkovaKomentara);

                            mDatabase.child(mAuth.getUid()+"**"+komentarID).removeValue();
                            Toast.makeText(mContext, "Komentar Vam se ne sviđa!", Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                    /*
                    mDatabase.child("LajkoviKomentara").child(mAuth.getUid()+"**"+komentarID).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            String trenutniBrojLajkova = Objects.requireNonNull(dataSnapshot.child("brojLajkovaKomentara").getValue()).toString();
                            if (trenutniBrojLajkova == null) {
                                trenutniBrojLajkova = "0";
                            }
                            Log.d("svidanja", trenutniBrojLajkova + " " + "treci sam!");

                            Double smanjeniBrojLajkova = Double.valueOf(trenutniBrojLajkova) - 1;
                            mDatabase.child(mAuth.getUid()+"**"+komentarID).removeValue();     // izbrisi node u 'LajkoviKomentara' node gdje je korisnik lajkao komentar s ovim ID-em
                            Toast.makeText(mContext, "Ne sviđa Vam se!", Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                    */
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        }));
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.komentari_item, parent, false);
        return new ViewHolder(view);
    }



    // total number of rows
    @Override
    public int getItemCount() {
        return (listaKomentara == null) ? 0 : listaKomentara.size();
    }


    // convenience method for getting data at click position
    String getItem(int id) {
        return listaKomentara.get(id);
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
