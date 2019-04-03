package com.example.johndoe.najamstanova;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class StanoviLikedAdapter extends RecyclerView.Adapter<StanoviLikedAdapter.ViewHolder> {

    ArrayList listaStanUID = new ArrayList();
    ArrayList listaCijena = new ArrayList();
    ArrayList listaPovrsina = new ArrayList();
    ArrayList listaUdaljenost = new ArrayList();
    ArrayList listaTrajanje = new ArrayList();
    ArrayList listaBrojLajkova = new ArrayList();
    ArrayList listaBrojKomentara = new ArrayList();
    ArrayList listaGlavnaSlika = new ArrayList();
    Context mContext;
    private LayoutInflater mInflater;
    int spremljenaPozicija = 0;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    ArrayList<Stanovi> listaStanova = new ArrayList<>();


    public StanoviLikedAdapter(Context context, ArrayList<Stanovi> stanovi) {
        this.mContext = context;
        this.listaStanova = stanovi;
        this.mInflater = LayoutInflater.from(context);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView opis, cijenaCL, povrsina, udaljenost, trajanje, like, comment;
        ImageView slika;
        ImageButton likeImage;

        ViewHolder(View itemView) {
            super(itemView);
            opis = itemView.findViewById(R.id.opis);
            cijenaCL = itemView.findViewById(R.id.cijenaCL);
            povrsina = itemView.findViewById(R.id.povrsina);
            udaljenost = itemView.findViewById(R.id.udaljenost);
            trajanje = itemView.findViewById(R.id.trajanje);
            like = itemView.findViewById(R.id.like);
            comment = itemView.findViewById(R.id.comment);
            slika = itemView.findViewById(R.id.slika);
            likeImage = itemView.findViewById(R.id.likeImage);
        }


    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.lista_stanova_izgled, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Stanovi stan = (Stanovi) listaStanova.get(position);
        Log.d("zastoneradi", stan.getVlasnik());
        holder.opis.setText(stan.getNaziv());
        holder.cijenaCL.setText(stan.getCijena().toString());
        holder.povrsina.setText(stan.getPovrsina() + " m\u00B2");
        holder.udaljenost.setText(stan.getUdaljenost().toString());
        holder.trajanje.setText(stan.getTrajanje());
        holder.like.setText(stan.getBrojLajkova().toString().replace(".0", ""));
        holder.comment.setText(stan.getBrojKomentara().toString().replace(".0", ""));

        Glide.with(mContext).load(stan.getGlavnaSlika()).into(holder.slika);

        holder.likeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("stasadnevalja", mAuth.getUid()  + "  " +  stan.getStanUID());
                zapisiLajkove(mAuth.getUid(), stan.getStanUID(), stan.getVlasnik());
            }
        });

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
                Intent detaljiStana = new Intent(mContext, DetaljiStanova.class);
                detaljiStana.putExtras(podaci);
                mContext.startActivity(detaljiStana, ActivityOptions.makeSceneTransitionAnimation((Activity) mContext).toBundle());


            }
        });

    }

    // total number of rows
    @Override
    public int getItemCount() {
        return  listaStanova == null ? 0 : listaStanova.size();
    }



    // convenience method for getting data at click position
    String getItem(int id) {
        return listaStanUID.get(id).toString();
    }


    private void zapisiLajkove(String userID, String stanID, String vlasnik) {
        mDatabase.child("Lajkovi").child(userID + "**" + stanID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    mDatabase.child("Stanovi").child(stanID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String trenutniBrojLajkova = Objects.requireNonNull(dataSnapshot.child("brojLajkova").getValue()).toString();
                            Log.d("svidanja", trenutniBrojLajkova);
                            Double povecaniBrojLajkova = Double.valueOf(trenutniBrojLajkova) + 1;

                            Map<String, Object> dodajLajkoveUserIDStanID = new HashMap<>();
                            dodajLajkoveUserIDStanID.put("userID", userID);
                            dodajLajkoveUserIDStanID.put("stanID", stanID);
                            dodajLajkoveUserIDStanID.put("brojLajkova", povecaniBrojLajkova);
                            mDatabase.child("Lajkovi").child(userID + "**" + stanID).updateChildren(dodajLajkoveUserIDStanID);      // zapisi u 'Lajkovi' node u bazi koji je user lajkao koji stan i broj lajkova

                            Map<String, Object> dodajLajkoveZaStanID = new HashMap<>();
                            dodajLajkoveZaStanID.put("brojLajkova", povecaniBrojLajkova);
                            mDatabase.child("Stanovi").child(stanID).updateChildren(dodajLajkoveZaStanID);      // ažuriraj 'Stanovi' node u bazi kada korisnik lajka stan
                            Toast.makeText(mContext, "Sviđa Vam se!", Toast.LENGTH_SHORT).show();
                            Intent ponovnoUcitaj = new Intent(mContext, StanoviLiked.class);
                            mContext.startActivity(ponovnoUcitaj);
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                } else {
                    //Toast.makeText(getContext(), "Već Vam se sviđa!", Toast.LENGTH_SHORT).show();
                    mDatabase.child("Stanovi").child(stanID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String trenutniBrojLajkova = Objects.requireNonNull(dataSnapshot.child("brojLajkova").getValue()).toString();
                            Log.d("svidanja", trenutniBrojLajkova);
                            Double smanjeniBrojLajkova = Double.valueOf(trenutniBrojLajkova) - 1;

                            mDatabase.child("Lajkovi").child(userID + "**" + stanID).removeValue();     // izbrisi node u 'Lajkovi' gdje je korisnik lajkao stan s ovim ID-em

                            Map<String, Object> umanjiLajkZaStanID = new HashMap<>();
                            umanjiLajkZaStanID.put("brojLajkova", smanjeniBrojLajkova);
                            mDatabase.child("Stanovi").child(stanID).updateChildren(umanjiLajkZaStanID);      // ažuriraj 'Stanovi' node u bazi kada korisnik dislajka stan
                            Toast.makeText(mContext, "Ne sviđa Vam se!", Toast.LENGTH_SHORT).show();

                            Intent ponovnoUcitaj = new Intent(mContext, StanoviLiked.class);
                            mContext.startActivity(ponovnoUcitaj);
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
}