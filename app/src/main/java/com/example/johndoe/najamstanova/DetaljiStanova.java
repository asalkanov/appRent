package com.example.johndoe.najamstanova;

import android.Manifest;
import android.app.ActivityOptions;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ImageClickListener;
import com.synnapps.carouselview.ImageListener;
import com.veinhorn.scrollgalleryview.MediaInfo;
import com.veinhorn.scrollgalleryview.ScrollGalleryView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;


public class DetaljiStanova extends AppCompatActivity {

    Bundle podaci;
    String nerazdvojeneSlike;
    CarouselView carouselView;
    ArrayList<String> sveSlike = new ArrayList<>();
    ArrayList<String> listaIDkomentara = new ArrayList<>();
    ArrayList<String> listaIDstana = new ArrayList<>();
    ArrayList<String> listaIDkorisnika = new ArrayList<>();
    ArrayList<String> listaBrojKomentara = new ArrayList<>();
    ArrayList<String> listaKomentara = new ArrayList<>();
    ArrayList<String> listaImeKorisnikaKomentar = new ArrayList<>();
    ArrayList<String> listaDatumKomentiranja = new ArrayList<>();
    com.robertlevonyan.views.customfloatingactionbutton.FloatingActionLayout chatFAB, callFAB;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase, mDatabaseKomentari;
    public static final int PERMISSION_REQUEST_CODE = 123;       // za poziv
    TextView vlasnikTextView, vlasnikUIDTextView, brojMobitelaTextView;
    Button pregledajLokaciju;
    String vlasnik, stanUID, vlasnikUID, brojMobitelaVlasnikaStana;
    private ScrollGalleryView scrollGalleryView;
    List<MediaInfo> infoSlike = new ArrayList<>();
    Integer pozicijaOdabraneSlike = 0;
    String latitude, longitude;
    TextView povuciteLijevoDesno;
    TextView naziv, cijena, povrsina, prijenosStatus, brojSoba, brojKupaonica;
    CheckBox tv, klima, rublje, hladnjak, posude, stednjak;
    KomentariAdapter komentariAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalji_stanova);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Korisnici");
        mDatabaseKomentari = FirebaseDatabase.getInstance().getReference().child("Komentari");

        vlasnikTextView = findViewById(R.id.cijenaStana);
        vlasnikUIDTextView = findViewById(R.id.stanUID);
        brojMobitelaTextView = findViewById(R.id.brojMobitela);
        chatFAB = findViewById(R.id.postavkeFAB);    // chat FAB
        callFAB = findViewById(R.id.callFAB);    // call FAB
        carouselView = findViewById(R.id.carouselView);

        povrsina = findViewById(R.id.povrsina);
        naziv = findViewById(R.id.naziv);
        cijena = findViewById(R.id.cijenaCL);
        brojSoba = findViewById(R.id.brojSoba);
        brojKupaonica = findViewById(R.id.brojKupaonica);
        povuciteLijevoDesno = findViewById(R.id.povuciteLijevoDesno);
        povuciteLijevoDesno.setVisibility(View.GONE);

        pregledajLokaciju = findViewById(R.id.pregledajLokaciju);
        pregledajLokaciju.setEnabled(false);

        tv = findViewById(R.id.tvCB);
        klima = findViewById(R.id.klimaCB);
        rublje = findViewById(R.id.rubljeCB);
        hladnjak = findViewById(R.id.hladnjakCB);
        posude = findViewById(R.id.posudeCB);
        stednjak = findViewById(R.id.stednjakCB);

        scrollGalleryView = findViewById(R.id.scroll_gallery_view);
        scrollGalleryView
                .setThumbnailSize(150)
                .setZoom(true)
                .withHiddenThumbnails(true)
                .setFragmentManager(getSupportFragmentManager());
        // za spremanje pozicije scrollane slike kada se pojavi ScrollGalleryView
        scrollGalleryView.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                pozicijaOdabraneSlike = position;       // spremljena pozicija scrollane slike
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        Intent intentExtras = getIntent();
        podaci = intentExtras.getExtras();

        // podaci dobiveni iz prethodnog Activitya -- Liste Stanova -- ili -- FragmentMapaStanova
        if (podaci != null && !podaci.isEmpty()) {
            stanUID = podaci.getString("UIDSTAN");
            vlasnikUID = podaci.getString("UIDVLASNIK");
            vlasnikUIDTextView.setText(podaci.getString("UIDVLASNIK"));
            naziv.setText(podaci.getString("NAZIV"));
            cijena.setText(podaci.getString("CIJENA") + " kn");
            povrsina.setText(podaci.getString("POVRSINA") + " m\u00B2");
            brojSoba.setText(podaci.getString("BROJSOBA"));
            brojKupaonica.setText(podaci.getString("KUPAONICA"));

            Log.d("koment", vlasnikUID + "**" + stanUID);

            // dohvaća sve komentare za stan s UID-em za koji korisnik gleda detalje
            mDatabaseKomentari.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot ds : dataSnapshot.getChildren()) {
                        if (Objects.requireNonNull(ds.child("stanID").getValue(String.class)).equals(stanUID)) {       // dohvati samo komentare za detalje stana kojeg korisnik trenutno pregledava
                            listaIDkomentara.add(ds.child("komentarID").getValue(String.class));
                            listaIDstana.add(ds.child("stanID").getValue(String.class));
                            listaIDkorisnika.add(ds.child("userID").getValue(String.class));
                            listaBrojKomentara.add(Objects.requireNonNull(ds.child("brojKomentara").getValue(Double.class)).toString());
                            listaKomentara.add(Objects.requireNonNull(ds.child("komentar").getValue(String.class)));
                            listaImeKorisnikaKomentar.add(ds.child("imeKorisnika").getValue(String.class));
                            listaDatumKomentiranja.add(ds.child("datum").getValue(String.class));
                        }
                    }
                    RecyclerView recyclerView = findViewById(R.id.recyclerView);
                    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(DetaljiStanova.this, LinearLayoutManager.HORIZONTAL, false);
                    SnapHelper snapHelper = new PagerSnapHelper();
                    recyclerView.setLayoutManager(layoutManager);
                    snapHelper.attachToRecyclerView(recyclerView);
                    recyclerView.setLayoutManager(layoutManager);
                    komentariAdapter = new KomentariAdapter(DetaljiStanova.this, listaIDkomentara, listaIDstana, listaIDkorisnika, listaBrojKomentara, listaKomentara, listaImeKorisnikaKomentar, listaDatumKomentiranja);
                    recyclerView.setAdapter(komentariAdapter);
                    if (listaKomentara.size() > 1) {
                        povuciteLijevoDesno.setText("Povucite lijevo za prikaz " + listaKomentara.size() + " komentara:");
                        povuciteLijevoDesno.setVisibility(View.VISIBLE);
                    } else {
                        povuciteLijevoDesno.setVisibility(View.GONE);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });

            latitude = podaci.getString("LATITUDE");
            longitude = podaci.getString("LONGITUDE");

            pregledajLokaciju.setEnabled(true);
            pregledajLokaciju.setOnClickListener(view -> {
                Dialog dialog = new Dialog(DetaljiStanova.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                Objects.requireNonNull(dialog.getWindow()).clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                dialog.setContentView(R.layout.map_dialog);
                dialog.show();

                ImageButton closeMapsDialog = dialog.findViewById(R.id.closeMapsDialog);
                closeMapsDialog.setOnClickListener(view1 -> dialog.dismiss());

                MapView mMapView = dialog.findViewById(R.id.mapView);
                MapsInitializer.initialize(getApplicationContext());

                mMapView =  dialog.findViewById(R.id.mapView);
                mMapView.onCreate(dialog.onSaveInstanceState());
                mMapView.onResume();// needed to get the map to display immediately
                mMapView.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(final GoogleMap googleMap) {
                        LatLng lokacijaStana = new LatLng(Double.valueOf(latitude), Double.valueOf(longitude));
                        googleMap.addMarker(new MarkerOptions().position(lokacijaStana).title(podaci.getString("OPIS")));
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lokacijaStana, 15));
                        googleMap.getUiSettings().setZoomControlsEnabled(true);
                        //googleMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
                    }
                });
            });

            if (podaci.containsKey("TV")) {
                if (Objects.requireNonNull(podaci.getString("TV")).equals("true")) {
                    tv.setChecked(true);
                } else {
                    tv.setChecked(false);
                }
            }
            if (podaci.containsKey("KLIMA")) {
                if (Objects.requireNonNull(podaci.getString("KLIMA")).equals("true")) {
                    klima.setChecked(true);
                } else {
                    klima.setChecked(false);
                }
            }
            if (podaci.containsKey("RUBLJE")) {
                if (Objects.requireNonNull(podaci.getString("RUBLJE")).equals("true")) {
                    rublje.setChecked(true);
                } else {
                    rublje.setChecked(false);
                }
            }
            if (podaci.containsKey("HLADNJAK")) {
                if (Objects.requireNonNull(podaci.getString("HLADNJAK")).equals("true")) {
                    hladnjak.setChecked(true);
                } else {
                    hladnjak.setChecked(false);
                }
            }
            if (podaci.containsKey("POSUDE")) {
                if (Objects.requireNonNull(podaci.getString("POSUDE")).equals("true")) {
                    posude.setChecked(true);
                } else {
                    posude.setChecked(false);
                }
            }
            if (podaci.containsKey("STEDNJAK")) {
                if (Objects.requireNonNull(podaci.getString("STEDNJAK")).equals("true")) {
                    stednjak.setChecked(true);
                } else {
                    stednjak.setChecked(false);
                }
            }

            if (podaci.getString("UIDVLASNIK").equals(mAuth.getUid())) {        // nemoj prikazati FAB gumb za Chat korisniku čiji je taj stan
                chatFAB.setVisibility(View.GONE);
                callFAB.setVisibility(View.GONE);
            } else {
                chatFAB.setVisibility(View.VISIBLE);
                callFAB.setVisibility(View.VISIBLE);
            }
            nerazdvojeneSlike = podaci.getString("SVESLIKE");
            String[] tempSveSlike = new String[]{};
            if (nerazdvojeneSlike != null) {
                Pattern p = Pattern.compile("#&~*%#", Pattern.LITERAL);
                tempSveSlike = p.split(nerazdvojeneSlike);
            }
            for (int i = 0; i < tempSveSlike.length; i++) {
                if (!tempSveSlike[i].equals("")) {
                    sveSlike.add(tempSveSlike[i]);
                }
            }
            infoSlike.clear();
            scrollGalleryView.clearGallery();
            for (String uri : sveSlike) {       // dodaj sve slike u Scroll Gallery tako da se odmah učitaju kada korisnik klikne na Carousel View
                infoSlike.add(MediaInfo.mediaLoader(new PicassoImageLoader(uri)));
            }
            scrollGalleryView.addMedia(infoSlike);


            if (podaci.getString("UIDVLASNIK") != null) {
                Query query = mDatabase.child(Objects.requireNonNull(podaci.getString("UIDVLASNIK"))).orderByChild("uid");
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            vlasnikTextView.setText(dataSnapshot.child("ime").getValue(String.class));
                            brojMobitelaVlasnikaStana = dataSnapshot.child("brojMobitela").getValue(String.class);
                            brojMobitelaTextView.setText(dataSnapshot.child("brojMobitela").getValue(String.class));
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }


        // pozivanje telefonskog broja vlasnika stana
        callFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               pozivanjeVlasnikaStana();
            }
        });


        // otvaranje Chata s vlasnikom odabranog Stana
        chatFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (podaci.getString("UIDVLASNIK") != null) {
                    Bundle noviPodaci = new Bundle();
                    noviPodaci.putString("RECEIVERUID", podaci.getString("UIDVLASNIK"));
                    noviPodaci.putString("DETALJISTANOVA", "DETALJISTANOVA");
                    Intent privateChat = new Intent(DetaljiStanova.this, PrivateChat.class);
                    privateChat.putExtras(noviPodaci);
                    startActivity(privateChat, ActivityOptions.makeSceneTransitionAnimation(DetaljiStanova.this).toBundle());
                }
            }
        });


        // klik na CarouselView povećava slike u fullscreen Galeriju
        if (!(sveSlike.size() == 0)) {
            carouselView.setPageCount(sveSlike.size());
            carouselView.setImageListener(imageListener);
            // klik na CarouselView otvara Galeriju
            carouselView.setImageClickListener(new ImageClickListener() {
                @Override
                public void onClick(int position) {
                    infoSlike.clear();
                    scrollGalleryView.clearGallery();
                    for (String uri : sveSlike) {       // dodaj sve slike u Scroll Gallery tako da se odmah učitaju kada korisnik klikne na Carousel View
                        infoSlike.add(MediaInfo.mediaLoader(new PicassoImageLoader(uri)));
                    }
                    scrollGalleryView.addMedia(infoSlike);
                    scrollGalleryView.setCurrentItem(position);
                    scrollGalleryView.setVisibility(View.VISIBLE);
                }
            });
        }
    }


    // učitava sve slike stana u Carousel View
    ImageListener imageListener = new ImageListener() {
        @Override
        public void setImageForPosition(int position, ImageView imageView) {
            Glide.with(getApplicationContext()).load(sveSlike.get(position)).apply((new RequestOptions().transforms(new FitCenter()))).into(imageView);
            //Picasso.get().load(sveSlike.get(position)).fit().centerInside().into(imageView);
        }
    };


    // ponovno učitava slike u ScrollGaleryView kada korisnik promijeni orijentaciju uređaja, za prikaz ispravne veličine slike kod rotacije
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (scrollGalleryView.getVisibility() == View.VISIBLE) {
                infoSlike.clear();
                scrollGalleryView.clearGallery();
                for (String uri : sveSlike) {       // dodaj sve slike u Scroll Gallery tako da se odmah učitaju kada korisnik klikne na Carousel View
                    infoSlike.add(MediaInfo.mediaLoader(new PicassoImageLoader(uri)));
                }
                scrollGalleryView.addMedia(infoSlike);
                scrollGalleryView.setCurrentItem(pozicijaOdabraneSlike);
                scrollGalleryView.setVisibility(View.VISIBLE);
            }
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            if (scrollGalleryView.getVisibility() == View.VISIBLE) {
                infoSlike.clear();
                scrollGalleryView.clearGallery();
                for (String uri : sveSlike) {       // dodaj sve slike u Scroll Gallery tako da se odmah učitaju kada korisnik klikne na Carousel View
                    infoSlike.add(MediaInfo.mediaLoader(new PicassoImageLoader(uri)));
                }
                scrollGalleryView.addMedia(infoSlike);
                scrollGalleryView.setCurrentItem(pozicijaOdabraneSlike);
                scrollGalleryView.setVisibility(View.VISIBLE);
            }
        }
    }



    private void zatraziDopustenjePoziva() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(DetaljiStanova.this, Manifest.permission.CALL_PHONE)) {
        } else {
            ActivityCompat.requestPermissions(DetaljiStanova.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},PERMISSION_REQUEST_CODE);
        }
    }


    // za dopušenje poziva telefenskog broja
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pozivanjeVlasnikaStana();
                }
                break;
        }
    }


    public void pozivanjeVlasnikaStana() {
        if (brojMobitelaVlasnikaStana != null) {
            Intent pozivanje = new Intent(Intent.ACTION_DIAL);
            pozivanje.setData(Uri.parse("tel:" + brojMobitelaVlasnikaStana));
            Log.d("poziv", brojMobitelaVlasnikaStana);
            int result = ContextCompat.checkSelfPermission(DetaljiStanova.this, Manifest.permission.CALL_PHONE);
            if (result == PackageManager.PERMISSION_GRANTED) {
                startActivity(pozivanje);
            } else {
                zatraziDopustenjePoziva();
            }
        } else {
            Snackbar.make(findViewById(android.R.id.content), "Korisnik nije dodao broj za kontakt.", Snackbar.LENGTH_LONG)
                    .setActionTextColor(Color.RED)
                    .show();
        }
    }


    @Override
    public void onBackPressed() {
        if (scrollGalleryView.getVisibility() == View.VISIBLE) {        // znači da korisnik gleda slike u fullscreenu u Scroll Gallery View
            scrollGalleryView.setVisibility(View.GONE);
        } else {
            super.onBackPressed();      // normalna akcija - zavrsi trenutnu aktivnost, vrati se nazad na prethodnu
        }

    }

}