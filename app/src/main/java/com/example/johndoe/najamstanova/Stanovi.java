package com.example.johndoe.najamstanova;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.gson.internal.ObjectConstructor;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@IgnoreExtraProperties
public class Stanovi {

    public String stanUID;
    public String naziv;
    public String vlasnik;
    public Long cijena;
    public Long povrsina;
    public Double brojLajkova;
    public Double brojKomentara;
    public String brojSoba;
    public String brojKupaonica;
    public String glavnaSlika;
    public String slike;
    public String latitude;
    public String longitude;
    public Double udaljenost;
    public String trajanje;
    public String datumDodavanja;
    public String tv, klima, rublje, hladnjak, posude, stednjak;
    public HashMap<String, Object> udaljenosti = new HashMap<>();

    public Stanovi() {
        // Default constructor required for calls to DataSnapshot.getValue(Korisnici.class)
    }

    public Stanovi(String stanUID, String naziv, String vlasnik, Long cijena, Long povrsina, Double brojLajkova, Double brojKomentara, String brojSoba, String brojKupaonica, String glavnaSlika, String slike, String latitude, String longitude,
                   Double udaljenost, String trajanje, String datumDodavanja, String tv, String klima, String rublje, String hladnjak, String posude, String stednjak, HashMap<String, Object> udaljenosti) {
        this.stanUID = stanUID;
        this.naziv = naziv;
        this.vlasnik = vlasnik;
        this.cijena = cijena;
        this.povrsina = povrsina;
        this.brojLajkova = brojLajkova;
        this.brojKomentara = brojKomentara;
        this.brojSoba = brojSoba;
        this.brojKupaonica = brojKupaonica;
        this.glavnaSlika = glavnaSlika;
        this.slike = slike;
        this.latitude = latitude;
        this.longitude = longitude;
        this.udaljenost = udaljenost;
        this.trajanje = trajanje;
        this.datumDodavanja = datumDodavanja;
        this.tv = tv;
        this.klima = klima;
        this.rublje = rublje;
        this.hladnjak = hladnjak;
        this.posude = posude;
        this.stednjak = stednjak;
        this.udaljenosti = udaljenosti;
    }

    public String getStanUID() { return stanUID; }

    public String getNaziv() {
        return naziv;
    }

    public String getVlasnik() { return vlasnik; }

    public Long getCijena() {
        return cijena;
    }

    public Long getPovrsina() {
        return povrsina;
    }

    public Double getUdaljenost() {
        return udaljenost;
    }

    public HashMap<String, Object> getUdaljenosti(String userID) {
        return udaljenosti;
    }

    public String getTrajanje() {
        return trajanje;
    }

    public Double getBrojLajkova() {
        return brojLajkova;
    }

    public Double getBrojKomentara() {
        return brojKomentara;
    }

    public String getBrojSoba() { return brojSoba; };

    public String getBrojKupaonica() { return brojKupaonica; };

    public String getGlavnaSlika() {
        return glavnaSlika;
    }

    public String getSlike() {
        return slike;
    }

    public String getLatitude() {
        return  latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getDatumDodavanja() {
        return datumDodavanja;
    }

    public String getTV() {
        return tv;
    }

    public String getKlima() {
        return klima;
    }

    public String getRublje() {
        return rublje;
    }

    public String getHladnjak() {
        return hladnjak;
    }

    public String getPosude() {
        return posude;
    }

    public String getStednjak() {
        return stednjak;
    }


    public void setUdaljenost(Double mUdaljenost) {
       this.udaljenost = mUdaljenost;
    }

    public void setTrajanje(String mTrajanje) {
        this.trajanje = mTrajanje;
    }
}

