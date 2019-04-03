package com.example.johndoe.najamstanova;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Korisnici {

    public String uid;
    public String tokenZaMessagingChat;
    public String ime;
    public String prezime;
    public String brojMobitela;
    public String email;
    String datumReg;

    public Korisnici() {
        // zadaci konstruktor potreban za poziv
        // DataSnapshot.getValue(Korisnici.class
    }

    public Korisnici(String uid, String tokenZaMessagingChat, String ime, String prezime, String brojMobitela, String email, String datumReg) {
        this.uid = uid;
        this.tokenZaMessagingChat = tokenZaMessagingChat;
        this.ime = ime;
        this.prezime = prezime;
        this.brojMobitela = brojMobitela;
        this.email = email;
        this.datumReg = datumReg;
    }

    public String getUid() { return uid; }

    public String getTokenZaMessagingChat() { return tokenZaMessagingChat; }

    public String getIme() { return ime; }

    public String getPrezime() { return prezime; }

    public String brojMobitela() { return brojMobitela; }

    public String getEmail() { return email; }

    public String getDatumReg() { return datumReg; }


}
