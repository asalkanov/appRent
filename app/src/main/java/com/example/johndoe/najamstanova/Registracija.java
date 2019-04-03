package com.example.johndoe.najamstanova;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class Registracija extends AppCompatActivity {

    TextView korisnik;
    EditText ime, prezime, email;
    Button registracija;
    ProgressBar registracijaProgressBar;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    String tokenZaMessagingChat = "nemaTokena";        // sluzi za slanje Push Notifickacije kada korisnik primi novu poruku u Chatu

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registracija);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        korisnik = findViewById(R.id.korisnik);
        korisnik.setText(mAuth.getUid());

        ime = findViewById(R.id.opis);
        prezime = findViewById(R.id.prezime);
        email = findViewById(R.id.email);

        registracijaProgressBar = findViewById(R.id.progressBar);
        registracijaProgressBar.bringToFront();

        // registrira novog korisnika u Firebase Database
        registracija = findViewById(R.id.registracija);
        registracija.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // registriraj novog korisnika u Firebase Database
                FirebaseUser user = mAuth.getCurrentUser();
                // dobivanje tokena za Messaging, odnosno Chat, koji sluzi za slanje Push Notifikacija kada korisnik dobije novu poruku
                FirebaseInstanceId.getInstance().getInstanceId()
                        .addOnCompleteListener(task -> {
                            if (!task.isSuccessful()) {     // token za Messaging Chat nije dobiven
                                Snackbar.make(findViewById(android.R.id.content), "Pogreška registracije tokena.", Snackbar.LENGTH_LONG)
                                        .setActionTextColor(Color.RED)
                                        .show();
                            } else {
                                // token za Push Notifikaciju za novu poruku u Chatu je uspjesno dohvacen
                                tokenZaMessagingChat = Objects.requireNonNull(task.getResult()).getToken();     // token za Push Notifikaciju za notifikaciju o novoj poruci u Chatu
                                Log.d("MyFirebase", tokenZaMessagingChat);
                                if (mAuth.getUid() != null && !tokenZaMessagingChat.equals("nemaTokena") && ime.getText() != null && !ime.getText().toString().equals("") && prezime.getText() != null && !prezime.getText().toString().equals("") && !"".equals(Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber())) {
                                    registracijaProgressBar.bringToFront();
                                    registracijaProgressBar.setVisibility(View.VISIBLE);
                                    String datumReg = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
                                    Korisnici noviKorisnik = new Korisnici(mAuth.getUid(), tokenZaMessagingChat, ime.getText().toString(), prezime.getText().toString(),
                                            Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber(), email.getText().toString(), datumReg);
                                    mDatabase.child("Korisnici").child(mAuth.getUid()).setValue(noviKorisnik)
                                            .addOnSuccessListener(aVoid -> {
                                                // postavi Display Name (ime korisnika) koji se registrira tek kada je unesen u Firebase Database
                                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(ime.getText() + " " + prezime.getText()).build();
                                                if (user != null) {
                                                    user.updateProfile(profileUpdates);     // postavi DisplayName za korisnika
                                                }
                                                registracijaProgressBar.setVisibility(View.GONE);
                                                Log.d("MyFire", "Korisnik dodan!");
                                                Intent listaStanova = new Intent(Registracija.this, ListaStanova.class);
                                                startActivity(listaStanova);
                                                /* KADA KORISNIK BUDE DODAVAO STANOVE, SVAKI STAN ĆE IMATI POLJE "VLASNIK" GDJE ĆE BITI ZAPISAN UID VLASNIKA ZA SVAKI POJEDINI STAN! */
                                            })
                                            .addOnFailureListener(e -> Snackbar.make(findViewById(android.R.id.content), "Pogreška prilikom registracije.", Snackbar.LENGTH_LONG)
                                                    .setActionTextColor(Color.RED)
                                                    .show());
                                }

                            }
                        });
            }
        });
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(Registracija.this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(Registracija.this);
        }
        builder.setTitle("Prekinuti registraciju?")
                .setMessage("Svi uneseni podaci bit će obrisani.")
                .setPositiveButton("Prekini registraciju", (dialog, which) -> {
                    FirebaseUser user = mAuth.getCurrentUser();
                    // postavi Display Name na null ako korisnik prekine registraciju
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(null).build();
                    if (user != null) {
                        user.updateProfile(profileUpdates);     // postavi DisplayName za korisnika
                    }
                    mAuth.signOut();
                    Toast.makeText(getApplicationContext(), "Registracija prekinuta!", Toast.LENGTH_SHORT).show();
                    Intent pocetniEkran = new Intent(Registracija.this, MainActivity.class);
                    startActivity(pocetniEkran);
                })
                .setNegativeButton("Zatvori", (dialog, which) -> {
                    // do nothing
                })
                .setIcon(android.R.drawable.ic_menu_close_clear_cancel)
                .show();
        //super.onBackPressed();
    }
}
