package com.example.johndoe.najamstanova;

import android.app.Activity;
import android.app.Application;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.TimeZone;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PrivateChat extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseKorisnici;
    private RecyclerView recyclerView;
    private FirebaseAuth mAuth;
    private StorageReference mStorage;
    EditText tekstPoruke;
    ImageButton posaljiPoruku, dodajSliku;
    String RECEIVER = "";
    Bundle podaci;
    public static final int ODABIR_SLIKE = 420;
    public static final String LEGACY_SERVER_KEY = "INSERT_GOOGLE_API_KEY_HERE";
    SharedPreferences stanjeActivitya;      // Notifikacije se ne prikazuju ako je korisnik otvorio PrivateChat u Foregroundu
    SharedPreferences.Editor prefEditor;
    Boolean dolazimIzDetaljiStanova = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_activity);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Chat");
        mDatabaseKorisnici = FirebaseDatabase.getInstance().getReference().child("Korisnici");
        mStorage = FirebaseStorage.getInstance().getReference();

        tekstPoruke = findViewById(R.id.tekstPoruke);
        posaljiPoruku = findViewById(R.id.posaljiChatPoruku);

        Log.d("MyFire", "onCreate");

        dolazimIzDetaljiStanova = false;

        // spremanje stanja (ove) aktivnosti PrivatChat, kako se Notifikacije o novoj poruci u Chatu ne bi prikazale ako je PrivateChat u Foregroundu
        stanjeActivitya = PreferenceManager.getDefaultSharedPreferences(PrivateChat.this);
        prefEditor = stanjeActivitya.edit();
        prefEditor.putBoolean("isInForeground", true);
        prefEditor.apply();

        Intent intentExtras = getIntent();
        podaci = intentExtras.getExtras();

        // ako korisnik dolazi iz DetaljiStanova, ne iz ChatUsersList
        if (podaci != null && !podaci.isEmpty() && !podaci.containsKey("LISTAKORISNIKA") && podaci.containsKey("DETALJISTANOVA")) {     // nemoj uci u ovaj IF ako korisnik dolazi iz ChatUsersList, samo ako dolazi iz DetaljiStanova
            if (podaci.getString("RECEIVERUID") != null) {
                RECEIVER = podaci.getString("RECEIVERUID");
                Globals.trenutniRECEIVER = RECEIVER;
                dolazimIzDetaljiStanova = true;
            }
        }

        // ako korisnik klikne na Notifikaciju o novoj poruci u Chatu -- korisnik dolazi iz MyFirebaseMessagingService jer je kliknuo na Notifikaciju
        if (podaci != null && !podaci.isEmpty() && podaci.containsKey("FIREBASEMESSAGINGSERVICE") && !podaci.containsKey("DETALJISTANOVA") && !podaci.containsKey("LISTAKORISNIKA")) {     // samo ako korisnik dolazi iz Notifikacije, tj. FirebaseMessagingService
            if (podaci.getString("RECEIVERUID") != null) {
                RECEIVER = podaci.getString("RECEIVERUID");
                Globals.trenutniRECEIVER = RECEIVER;
                Log.d("posiljateljImePrezime", RECEIVER + "____" + "dosao iz Notif.");
            }
        }

        // POTREBNO -- za Chat, odnosno Listu Korisnika s kojima je user ranije komunicirao -- ako korisnik dolazi iz ChatUsersList
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey("LISTAKORISNIKA")) {       // uđi u IF samo ako korisnik dolazi iz ChatUsersList, a ne iz DetaljiStanova
            String POSILJATELJ = extras.getString("POSILJATELJ");
            RECEIVER = extras.getString("PRIMATELJ");
            Globals.trenutniRECEIVER = RECEIVER;
            Log.d("PRIM1",  POSILJATELJ + "_" + RECEIVER);
            Log.d("PRIM2",  mAuth.getUid());
        }


        // ako je korisnik ušao u PrivateChat, sve poruke u tom Chatu označi kao pročitane
        oznaciPorukeKaoProcitane();

        dodajSliku = findViewById(R.id.dodajSliku);
        dodajSliku.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_PICK);
                startActivityForResult(Intent.createChooser(intent, "Odaberite sliku:"), ODABIR_SLIKE);
            }
        });

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView = findViewById(R.id.recycler_view_user_messages);


        // automatski osvjezi RecyclerView kada se podaci na serveru promijene -- dohvaćanje svih poruka s Firebasea
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<MessageItemGetter> razgovor = new ArrayList<>();
                for (DataSnapshot messageSnapshot : dataSnapshot.child(mAuth.getUid() + "_" + RECEIVER).getChildren()) {
                    Log.d("procitano", messageSnapshot.child("message_read").getValue().toString());
                    //messageSnapshot.getRef().child("message_read").setValue("true");
                    Log.d("procitano", messageSnapshot.child("message_read").getValue().toString());
                    razgovor.add(
                                new MessageItemGetter(
                                        /* DOBIVANJE VEC POSLANIH PORUKA U RECYCLERVIEW */
                                        messageSnapshot.child("message_id").getValue().toString(),
                                        messageSnapshot.child("sender_uid").getValue().toString(),       // sender uid
                                        messageSnapshot.child("sender_name").getValue().toString(),      // sender name
                                        messageSnapshot.child("receiver_uid").getValue().toString(),     // receiver uid
                                        messageSnapshot.child("receiver_name").getValue().toString(),    // receiver name
                                        messageSnapshot.child("message_body").getValue().toString(),
                                        messageSnapshot.child("message_timestamp").getValue().toString(),
                                        messageSnapshot.child("message_read").getValue().toString()
                                ));
                   // }
                }

                /* DRUGI SLUCAJ -- AKO JE DRUGA OSOBA PRVA POSLALA PORUKU -- ONDA CE U BAZI BIZI ZAPISANO RECEIVER_ID +"_" + mAuth.getUid() */
                for (DataSnapshot messageSnapshot : dataSnapshot.child(RECEIVER + "_" + mAuth.getUid()).getChildren()) {
                    Log.d("procitano", messageSnapshot.child("message_read").getValue().toString());
                    //messageSnapshot.getRef().child("message_read").setValue("true");
                    Log.d("procitano", messageSnapshot.child("message_read").getValue().toString());
                    razgovor.add(
                            new MessageItemGetter(
                                    /* DOBIVANJE VEC POSLANIH PORUKA U RECYCLERVIEW */
                                    messageSnapshot.child("message_id").getValue().toString(),
                                    messageSnapshot.child("sender_uid").getValue().toString(),       // sender uid
                                    messageSnapshot.child("sender_name").getValue().toString(),      // sender name
                                    messageSnapshot.child("receiver_uid").getValue().toString(),     // receiver uid
                                    messageSnapshot.child("receiver_name").getValue().toString(),    // receiver name
                                    messageSnapshot.child("message_body").getValue().toString(),
                                    messageSnapshot.child("message_timestamp").getValue().toString(),
                                    messageSnapshot.child("message_read").getValue().toString()
                            ));
                    // }
                }

                if (recyclerView != null) {
                    recyclerView.setLayoutManager(linearLayoutManager);
                    recyclerView.scrollToPosition(razgovor.size() - 1);
                    recyclerView.setAdapter(new MessageAdapter(razgovor, PrivateChat.this));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //Log.v(Constants.LOG_CATCH_EXCEPTION_VERBOSE, databaseError.getMessage());
            }
        });


        // posalji novu poruku na Firebase
        if (posaljiPoruku != null && tekstPoruke != null) {
            posaljiPoruku.setOnClickListener(v -> {

                DatabaseReference korisnici = FirebaseDatabase.getInstance().getReference().child("Korisnici");
                korisnici.orderByChild("uid").equalTo(RECEIVER).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String imePrimatelja =  dataSnapshot.child(RECEIVER).child("ime").getValue(String.class) + " " + dataSnapshot.child(RECEIVER).child("prezime").getValue(String.class);
                        posaljiPorukuChat(imePrimatelja, "samoTekst");
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            });
        }
    }


    // kada korisnik želi poslati sliku u Chat
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == ODABIR_SLIKE) {
            if (resultCode == RESULT_OK) {
                    //Get ImageURi and load with help of picasso
                Uri file_uri = Uri.parse(data.getData().toString()); // parse to Uri if your videoURI is string
                String real_path = file_uri.getPath(); // will return real file path
                Uri uriOdabraneSlike = data.getData();
                Log.d("ODABRANA1", uriOdabraneSlike.toString() + "--" + file_uri + "___" + real_path);
                posaljiSliku(uriOdabraneSlike);


            }
        }
    }


    public void posaljiSliku(Uri uriSlike) {
        final StorageReference fileToUpload = mStorage.child(new StringBuilder("Images/").append(UUID.randomUUID().toString()).toString());
        UploadTask uploadTask = fileToUpload.putFile(Uri.parse(String.valueOf(uriSlike)));
        Log.d("ODABRANA2", uriSlike.toString());
        Task<Uri> urlTask = uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw Objects.requireNonNull(task.getException());
            }
            return fileToUpload.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Uri uriUploadaneSlike = task.getResult();
                posaljiPorukuChat(RECEIVER, String.valueOf(uriUploadaneSlike));     // poziv za poslati sliku iz Chata na Firebase Database
            } else {
                Snackbar.make(findViewById(android.R.id.content), "Pogreška prilikom prijenosa slike!", Snackbar.LENGTH_SHORT)
                        .setActionTextColor(Color.RED)
                        .show();
            }
        }).addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(PrivateChat.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    public void posaljiPorukuChat(String imePrimatelja, String porukaTekst) {

        if (porukaTekst.equals("samoTekst")) {       // znači da korisik šalje samo tekst
            porukaTekst = tekstPoruke.getText().toString();
            Log.d("testiranje", "samoTekst je");
        }

        Log.d("testiranje", "samoTekst nije " + porukaTekst);


        posaljiNotifikacijuZaNovuChatPoruku(porukaTekst);       // slanje Push Notifikacije u Background i Foreground

        Calendar kalendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Zagreb"));
        String datum = DateFormat.format("dd.MM.yyyy HH:mm", kalendar).toString();

        /*  PEVI SLUCAJ  -- OVISI TKO JE PRVI POSLAO PORUKU -- NAĐI KEY PORUKE I U TAJ KEY DODAJ NOVE PORUKE  */
        final String[] finalPorukaTekst = {porukaTekst};

        mDatabase.child(mAuth.getUid() + "_" + RECEIVER).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.d("EGZISTIRA", "prvi slucaj " + mAuth.getUid() + " " + tekstPoruke.getText().toString());
                    //if ((!tekstPoruke.getText().toString().equals("") && !mAuth.getUid().equals("") && !finalPorukaTekst.startsWith("https://firebasestorage.googleapis.com/")) || (finalPorukaTekst.startsWith("https://firebasestorage.googleapis.com/"))) {
                    if (!tekstPoruke.getText().toString().equals("") && !mAuth.getUid().equals("")) {
                        mDatabase.child(mAuth.getUid() + "_" + RECEIVER).push().setValue(
                                new MessageItemSetter(String.valueOf(mDatabase.push().hashCode()),
                                        mAuth.getUid(),                     // sender uid
                                        mAuth.getCurrentUser().getDisplayName(),
                                        RECEIVER,     // receiver uid  -->   TU TREBA DODATI STVARNI UID KORISNIKA ČIJI JE STAN, TJ. ONOM KOJEM JE PORUKA NAMIJENJENA, KOME SE TREBA POSLATI
                                        imePrimatelja,
                                        finalPorukaTekst[0],
                                        datum,
                                        "false"
                                ));
                    }
                    // za prikaz Slike u Chatu
                    if (finalPorukaTekst[0].startsWith("https://firebasestorage.googleapis.com/") && !mAuth.getUid().equals("")) {          // znaci da je poslana slika, a ne tekst u Chatu
                        Log.d("EGZISTIRA2", finalPorukaTekst[0] + " " + mAuth.getUid());
                        mDatabase.child(mAuth.getUid() + "_" + RECEIVER).push().setValue(
                                new MessageItemSetter(String.valueOf(mDatabase.push().hashCode()),
                                        mAuth.getUid(),                     // sender uid
                                        mAuth.getCurrentUser().getDisplayName(),
                                        RECEIVER,     // receiver uid  -->   TU TREBA DODATI STVARNI UID KORISNIKA ČIJI JE STAN, TJ. ONOM KOJEM JE PORUKA NAMIJENJENA, KOME SE TREBA POSLATI
                                        imePrimatelja,
                                        finalPorukaTekst[0],
                                        datum,
                                        "false"
                                ));
                        finalPorukaTekst[0] = "";
                    }
                    tekstPoruke.setText(null);
                }   else {
                    mDatabase.child(RECEIVER + "_" + mAuth.getUid()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                Log.d("EGZISTIRA3", "drugi slucaj " + finalPorukaTekst[0]);
                                if (!tekstPoruke.getText().toString().equals("") && !mAuth.getUid().equals("")) {
                                //if ((!tekstPoruke.getText().toString().equals("") && !mAuth.getUid().equals("") && !finalPorukaTekst.startsWith("https://firebasestorage.googleapis.com/")) || (finalPorukaTekst.startsWith("https://firebasestorage.googleapis.com/"))) {
                                    mDatabase.child(RECEIVER + "_" + mAuth.getUid()).push().setValue(
                                            new MessageItemSetter(String.valueOf(mDatabase.push().hashCode()),
                                                    mAuth.getUid(),                     // sender uid
                                                    mAuth.getCurrentUser().getDisplayName(),
                                                    RECEIVER,     // receiver uid  -->   TU TREBA DODATI STVARNI UID KORISNIKA ČIJI JE STAN, TJ. ONOM KOJEM JE PORUKA NAMIJENJENA, KOME SE TREBA POSLATI
                                                    imePrimatelja,
                                                    finalPorukaTekst[0],
                                                    datum,
                                                    "false"
                                            ));
                                }
                                // za prikaz Slike u Chatu
                                if (finalPorukaTekst[0].startsWith("https://firebasestorage.googleapis.com/") && !mAuth.getUid().equals("")) {          // znaci da je poslana slika, a ne tekst u Chatu
                                    Log.d("EGZISTIRA3", finalPorukaTekst[0] + " " + mAuth.getUid());
                                    mDatabase.child(RECEIVER + "_" + mAuth.getUid()).push().setValue(
                                            new MessageItemSetter(String.valueOf(mDatabase.push().hashCode()),
                                                    mAuth.getUid(),                     // sender uid
                                                    mAuth.getCurrentUser().getDisplayName(),
                                                    RECEIVER,     // receiver uid  -->   TU TREBA DODATI STVARNI UID KORISNIKA ČIJI JE STAN, TJ. ONOM KOJEM JE PORUKA NAMIJENJENA, KOME SE TREBA POSLATI
                                                    imePrimatelja,
                                                    finalPorukaTekst[0],
                                                    datum,
                                                    "false"
                                            ));
                                    finalPorukaTekst[0] = "";
                                }
                                tekstPoruke.setText(null);
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


        /*  DRUGI SLUCAJ  -- OVISI TKO JE PRVI POSLAO PORUKU -- NAĐI KEY PORUKE I U TAJ KEY DODAJ NOVE PORUKE  */
        mDatabase.child(RECEIVER + "_" + mAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.d("EGZISTIRA", "treci slucaj " + finalPorukaTekst[0]);
                    if (!tekstPoruke.getText().toString().equals("") && !mAuth.getUid().equals("")) {
                    //if ((!tekstPoruke.getText().toString().equals("") && !mAuth.getUid().equals("") && !finalPorukaTekst.startsWith("https://firebasestorage.googleapis.com/")) || (finalPorukaTekst.startsWith("https://firebasestorage.googleapis.com/"))) {
                        mDatabase.child(RECEIVER + "_" + mAuth.getUid()).push().setValue(
                                new MessageItemSetter(String.valueOf(mDatabase.push().hashCode()),
                                        mAuth.getUid(),                     // sender uid
                                        mAuth.getCurrentUser().getDisplayName(),
                                        RECEIVER,     // receiver uid  -->   TU TREBA DODATI STVARNI UID KORISNIKA ČIJI JE STAN, TJ. ONOM KOJEM JE PORUKA NAMIJENJENA, KOME SE TREBA POSLATI
                                        imePrimatelja,
                                        //tekstPoruke.getText().toString(),
                                        finalPorukaTekst[0],
                                        datum,
                                        "false"
                                ));
                    }
                    // za prikaz Slike u Chatu
                    if (finalPorukaTekst[0].startsWith("https://firebasestorage.googleapis.com/") && !mAuth.getUid().equals("")) {          // znaci da je poslana slika, a ne tekst u Chatu
                        Log.d("EGZISTIRA3", finalPorukaTekst[0] + " " + mAuth.getUid());
                        mDatabase.child(RECEIVER + "_" + mAuth.getUid()).push().setValue(
                                new MessageItemSetter(String.valueOf(mDatabase.push().hashCode()),
                                        mAuth.getUid(),                     // sender uid
                                        mAuth.getCurrentUser().getDisplayName(),
                                        RECEIVER,     // receiver uid  -->   TU TREBA DODATI STVARNI UID KORISNIKA ČIJI JE STAN, TJ. ONOM KOJEM JE PORUKA NAMIJENJENA, KOME SE TREBA POSLATI
                                        imePrimatelja,
                                        finalPorukaTekst[0],
                                        datum,
                                        "false"
                                ));
                        finalPorukaTekst[0] = "";
                    }
                    tekstPoruke.setText(null);
                } else {
                    mDatabase.child(mAuth.getUid() + "_" + RECEIVER).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (!tekstPoruke.getText().toString().equals("") && !mAuth.getUid().equals("")) {
                            //if ((!tekstPoruke.getText().toString().equals("") && !mAuth.getUid().equals("") && !finalPorukaTekst.startsWith("https://firebasestorage.googleapis.com/")) || (finalPorukaTekst.startsWith("https://firebasestorage.googleapis.com/"))) {
                                mDatabase.child(mAuth.getUid() + "_" + RECEIVER).push().setValue(
                                        new MessageItemSetter(String.valueOf(mDatabase.push().hashCode()),
                                                mAuth.getUid(),                     // sender uid
                                                mAuth.getCurrentUser().getDisplayName(),
                                                RECEIVER,     // receiver uid  -->   TU TREBA DODATI STVARNI UID KORISNIKA ČIJI JE STAN, TJ. ONOM KOJEM JE PORUKA NAMIJENJENA, KOME SE TREBA POSLATI
                                                imePrimatelja,
                                                finalPorukaTekst[0],
                                                datum,
                                                "false"
                                        ));
                            }
                            // za prikaz Slike u Chatu
                            if (finalPorukaTekst[0].startsWith("https://firebasestorage.googleapis.com/") && !mAuth.getUid().equals("")) {          // znaci da je poslana slika, a ne tekst u Chatu
                                Log.d("EGZISTIRA3", finalPorukaTekst[0] + " " + mAuth.getUid());
                                mDatabase.child(mAuth.getUid() + "_" + RECEIVER).push().setValue(
                                        new MessageItemSetter(String.valueOf(mDatabase.push().hashCode()),
                                                mAuth.getUid(),                     // sender uid
                                                mAuth.getCurrentUser().getDisplayName(),
                                                RECEIVER,     // receiver uid  -->   TU TREBA DODATI STVARNI UID KORISNIKA ČIJI JE STAN, TJ. ONOM KOJEM JE PORUKA NAMIJENJENA, KOME SE TREBA POSLATI
                                                imePrimatelja,
                                                finalPorukaTekst[0],
                                                datum,
                                                "false"
                                        ));
                                finalPorukaTekst[0] = "";
                            }
                            tekstPoruke.setText(null);
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


    // slanje Notifikacije o novoj poruci u Chatu -- koristeći Library "FCM-AndroidToOtherDevice"
    public void posaljiNotifikacijuZaNovuChatPoruku(String poruka) {
        /*
        FirebasePush firebasePush = new FirebasePush(LEGACY_SERVER_KEY);        // dodan gore, viljdiv u Firebase -> Project settings -> Cloud Messaaging
        firebasePush.setAsyncResponse(new PushNotificationTask.AsyncResponse() {
            @Override
            public void onFinishPush(@NotNull String ouput) {
                Log.e("OUTPUT", ouput);
            }
        });
        firebasePush.setNotification(new Notification("naslovPoruke","tijeloPoruke"));
        */
        // dobivanje Tokena za Messaging Chat kako bi se korisnik poslala Notifikacija o novoj poruci
        mDatabaseKorisnici.child(RECEIVER).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String chatTokenPrimatelja = dataSnapshot.child("tokenZaMessagingChat").getValue(String.class);
                if (chatTokenPrimatelja != null && chatTokenPrimatelja.length() > 0) {
                    if (!poruka.startsWith("https://firebasestorage.googleapis.com/")) {        // poruka nije slika
                        posaljiPushNotifikacijuPOST(chatTokenPrimatelja, mAuth.getUid(), Objects.requireNonNull(mAuth.getCurrentUser()).getDisplayName(), RECEIVER, poruka);
                    } else {
                        posaljiPushNotifikacijuPOST(chatTokenPrimatelja, mAuth.getUid(), Objects.requireNonNull(mAuth.getCurrentUser()).getDisplayName(), RECEIVER, "Primljena je fotografija.");
                    }
                }
                //firebasePush.sendToToken(chatTokenPrimatelja);    // -- šalje poruku odgovarajućem primatelju (dobiveni chatTokenPrimatelj)
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    // Šalje Push Notifikaciju na drugi uređaj -- kada aplikacija radi u Backgroundu i u Foregroundu (jer se šalju Data Messages, a ne Notification Messages)
    // Data messages are handled in onMessageReceived whether the app is in the foreground or background.
    public void posaljiPushNotifikacijuPOST(final String primatelj, final String posiljateljUID, final String posiljateljImePrezime, final String primateljUID, final String poruka) {
        if (primatelj.length() > 0) {
            pripremiPorukuParametri parametri = new pripremiPorukuParametri(primatelj, posiljateljUID, posiljateljImePrezime, primateljUID, poruka);
            pripremiPoruku pripremiPoruku = new pripremiPoruku();
            pripremiPoruku.execute(parametri);
        }
    }
    private static class pripremiPorukuParametri {
        String primatelj;
        String posiljateljUID;
        String posiljateljImePrezime;
        String primateljUID;
        String poruka;

        pripremiPorukuParametri(String primatelj, String posiljateljUID, String posiljateljImePrezime, String primateljUID, String poruka) {
            this.primatelj = primatelj;
            this.posiljateljUID = posiljateljUID;
            this.posiljateljImePrezime = posiljateljImePrezime;
            this.primateljUID = primateljUID;
            this.poruka = poruka;
        }
    }
    private static class pripremiPoruku extends AsyncTask<pripremiPorukuParametri, String, String> {
        @Override
        protected String doInBackground(pripremiPorukuParametri... pripremiPorukuParametri) {
            final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            try {
                OkHttpClient client = new OkHttpClient();
                JSONObject json = new JSONObject();
                JSONObject dataJson = new JSONObject();
                dataJson.put("title", "Nova poruka");
                dataJson.put("body", pripremiPorukuParametri[0].poruka);
                dataJson.put("priority", "high");
                dataJson.put("posiljateljUID", pripremiPorukuParametri[0].posiljateljUID);
                dataJson.put("primateljUID", pripremiPorukuParametri[0].primateljUID);
                dataJson.put("posiljateljImePrezime", pripremiPorukuParametri[0].posiljateljImePrezime);
                int notification_id = new Random().nextInt();
                dataJson.put("IDporuke", notification_id);
                json.put("data", dataJson);
                json.put("to", pripremiPorukuParametri[0].primatelj);    // primatelj, ovdje ide token za Chat (chatTokenPrimatelja)
                RequestBody body = RequestBody.create(JSON, json.toString());
                Request request = new Request.Builder()
                        .header("Authorization","key="+LEGACY_SERVER_KEY)
                        .url("https://fcm.googleapis.com/fcm/send")
                        .post(body)
                        .build();
                Response response = client.newCall(request).execute();
                String finalResponse = response.body().string();
            }catch (Exception e){
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
        }
    }


    @Override
    protected void onStart() {
        Log.d("MyFire", "onStart");
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((mMessageReceiver), new IntentFilter("MyData"));
        stanjeActivitya = PreferenceManager.getDefaultSharedPreferences(PrivateChat.this);
        prefEditor = stanjeActivitya.edit();
        prefEditor.putBoolean("isInForeground", true);
        prefEditor.apply();
    }

    @Override
    protected void onResume() {
        Log.d("MyFire", "onResume");
        super.onResume();
        stanjeActivitya = PreferenceManager.getDefaultSharedPreferences(PrivateChat.this);
        prefEditor = stanjeActivitya.edit();
        prefEditor.putBoolean("isInForeground", true);
        prefEditor.apply();
    }

    @Override
    protected void onRestart() {
        Log.d("MyFire", "onRestart");
        super.onRestart();
        stanjeActivitya = PreferenceManager.getDefaultSharedPreferences(PrivateChat.this);
        prefEditor = stanjeActivitya.edit();
        prefEditor.putBoolean("isInForeground", true);
        prefEditor.apply();
    }

    @Override
    protected void onPause() {
        Log.d("MyFire", "onPause");
        super.onPause();
        stanjeActivitya = PreferenceManager.getDefaultSharedPreferences(PrivateChat.this);
        prefEditor = stanjeActivitya.edit();
        prefEditor.putBoolean("isInForeground", false);
        prefEditor.apply();
    }

    // ako je korisnik kliknuo na Notifikaciju, onda je PrivateChat u Foregroundu
    @Override
    protected void onStop() {
        Log.d("MyFire", "onStop");
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        Bundle podaciNotifikacija;
        Intent intentExtras = getIntent();
        podaciNotifikacija = intentExtras.getExtras();
        // znači da dolazi iz Notifikacije, tj. korisnik je kliknuo na notifikaciju
        if (podaciNotifikacija != null && !podaciNotifikacija.isEmpty() && podaciNotifikacija.containsKey("FIREBASEMESSAGINGSERVICE") && !podaciNotifikacija.containsKey("DETALJISTANOVA") && !podaciNotifikacija.containsKey("LISTAKORISNIKA")) {
            stanjeActivitya = PreferenceManager.getDefaultSharedPreferences(PrivateChat.this);
            prefEditor = stanjeActivitya.edit();
            prefEditor.putBoolean("isInForeground", true);
            prefEditor.apply();
            Log.d("MyFire", "onStop " + "true");
        } else {
            stanjeActivitya = PreferenceManager.getDefaultSharedPreferences(PrivateChat.this);
            prefEditor = stanjeActivitya.edit();
            prefEditor.putBoolean("isInForeground", false);
            prefEditor.apply();
            Log.d("MyFire", "onStop " + "false");
        }
    }

    // ako je korisnik kliknuo na Notifikaciju, onda je PrivateChat u Foregroundu
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Bundle podaciNotifikacija;
        Intent intentExtras = getIntent();
        podaciNotifikacija = intentExtras.getExtras();
        // znači da dolazi iz Notifikacije, tj. korisnik je kliknuo na notifikaciju
        if (podaciNotifikacija != null && !podaciNotifikacija.isEmpty() && podaciNotifikacija.containsKey("FIREBASEMESSAGINGSERVICE") && !podaciNotifikacija.containsKey("DETALJISTANOVA") && !podaciNotifikacija.containsKey("LISTAKORISNIKA")) {
            stanjeActivitya = PreferenceManager.getDefaultSharedPreferences(PrivateChat.this);
            prefEditor = stanjeActivitya.edit();
            prefEditor.putBoolean("isInForeground", true);
            prefEditor.apply();
            Log.d("MyFire", "onDestroy " + "true");
        } else {
            stanjeActivitya = PreferenceManager.getDefaultSharedPreferences(PrivateChat.this);
            prefEditor = stanjeActivitya.edit();
            prefEditor.putBoolean("isInForeground", false);
            prefEditor.apply();
            Log.d("MyFire", "onDestroy " + "false");
        }
    }


    // poziva se kada korisnik otvori PrivatChat klikom na Notifikaciju, onda treba isto zapisati da se privateChat odvija u Foregroundu.
    @Override
    protected void onNewIntent(Intent intent) {
        Log.d("MyFire", "onNewIntent");
        super.onNewIntent(intent);
        Bundle podaciNotifikacija;
        podaciNotifikacija = intent.getExtras();
        if (podaciNotifikacija != null && !podaciNotifikacija.isEmpty() && podaciNotifikacija.containsKey("FIREBASEMESSAGINGSERVICE") && !podaciNotifikacija.containsKey("DETALJISTANOVA") && !podaciNotifikacija.containsKey("LISTAKORISNIKA")) {
            stanjeActivitya = PreferenceManager.getDefaultSharedPreferences(PrivateChat.this);
            prefEditor = stanjeActivitya.edit();
            prefEditor.putBoolean("isInForeground", true);
            prefEditor.apply();
        }
    }


    // označava poruke kao pročitane kada korisnik odabere nepročitanu kporuku u ChatUsersList i uđe u PrivateChat aktivnost
    public void oznaciPorukeKaoProcitane() {
        ukloniNotifikaciju();
        Log.d("procitano", RECEIVER + "<>" + mAuth.getUid());
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot messageSnapshot : dataSnapshot.child(mAuth.getUid() + "_" + RECEIVER).getChildren()) {
                    Log.d("procitano", messageSnapshot.child("message_read").getValue().toString());
                    messageSnapshot.getRef().child("message_read").setValue("true");
                    Log.d("procitano", messageSnapshot.child("message_read").getValue().toString());
                }
                /* DRUGI SLUCAJ */
                for (DataSnapshot messageSnapshot : dataSnapshot.child(RECEIVER + "_" + mAuth.getUid()).getChildren()) {
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

    // ako je PrivateChat u Foregroundu i trenutni korisnik primi poruku, označi ih sve kao pročitane -- ovo se poziva iz MyFirebaseMessagingService
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            oznaciPorukeKaoProcitane();
        }
    };


    public void ukloniNotifikaciju() {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            Log.d("UKLANJAM", "uklonio: <> " + RECEIVER + " <> " + mAuth.getUid());
            //notificationManager.cancel(RECEIVER, 0);
            notificationManager.cancel(mAuth.getUid(), 0);
        }
    }

    @Override
    public void onBackPressed() {
        if (!dolazimIzDetaljiStanova) {     // nemoj se vratiti u ChatUsersList ako korisnik dolazi iz DetaljiStanova
            super.onBackPressed();
            Intent chatUsersList = new Intent(PrivateChat.this, ChatUsersList.class);
            startActivity(chatUsersList);
        } else {
            super.onBackPressed();
        }
    }
}


