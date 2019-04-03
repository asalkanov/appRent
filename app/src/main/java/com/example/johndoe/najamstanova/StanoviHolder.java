package com.example.johndoe.najamstanova;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;


public class StanoviHolder extends RecyclerView.ViewHolder {

    TextView naziv;
    TextView cijena;
    ImageView slika;
    TextView udaljenost;
    TextView trajanje;
    TextView povrsina;
    TextView like, comment;
    ImageButton likeImage, commentImage, izmijeniStan;


    public StanoviHolder(View itemView) {
        super(itemView);
        naziv = itemView.findViewById(R.id.opis);
        cijena = itemView.findViewById(R.id.cijenaCL);
        slika = itemView.findViewById(R.id.slika);
        udaljenost = itemView.findViewById(R.id.udaljenost);
        trajanje = itemView.findViewById(R.id.trajanje);
        povrsina = itemView.findViewById(R.id.povrsina);

        like = itemView.findViewById(R.id.like);
        comment = itemView.findViewById(R.id.comment);
        likeImage = itemView.findViewById(R.id.likeImage);
        commentImage = itemView.findViewById(R.id.commentImage);
        izmijeniStan = itemView.findViewById(R.id.izmijeniStan);
    }

    // ovo se koristi u ListaStanova u onBindViewHolder
    public void setNaziv(String nazivStana) {
        naziv.setText(nazivStana);
    }

    public void setCijena(String cijenaStana) {
        cijena.setText(cijenaStana);
    }

    public void setUdaljenost(String udaljenostStana) {
        udaljenost.setText(udaljenostStana);
    }

    public void setTrajanje(String trajanjeStana) {
        trajanje.setText(trajanjeStana);
    }

    public void setPovrsina(String povrsinaStana) {
        povrsina.setText(povrsinaStana);
    }

    public void setBrojLajkova(String lajkoviStana) {
        like.setText(lajkoviStana);
    }

    public void setBrojKomentara(String komentariStana) {
        comment.setText(komentariStana);
    }

    public void setSlika(String glavnaSlika, Context context) {
        //StorageReference storageReference = FirebaseStorage.getInstance().getReference(slike);
        /*
        String[] tempSveSlike = new String[]{};
        ArrayList<String> sveSlike = new ArrayList<>();

        if (slike != null) {
            Pattern p = Pattern.compile("#&~*%#", Pattern.LITERAL);
            tempSveSlike = p.split(slike);
        }
        for (int i=0; i<tempSveSlike.length; i++) {
            if (!tempSveSlike[i].equals("")) {
                sveSlike.add(tempSveSlike[i]);
            }
        }
        */

        Glide.with(context).load(glavnaSlika).into(slika);     // za ovo je potreban MyAppGlideModule kojeg automatski procesira Glide annotation processor -- učitaj samo prvu sliku stana
    }


}

