package com.example.johndoe.najamstanova;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ImageListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;
import java.util.TimeZone;
import java.util.UUID;

public class SazetakInformacija extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private StorageReference mStorage;
    private FirebaseAuth mAuth;
    private static final FirebaseDatabase DATABASE = FirebaseDatabase.getInstance();
    Integer brojacUploadnihSlika = 0;
    ProgressBar uploadLoadingBar;
    private String key;
    Button predajOglas;
    Bundle podaci;
    String glavnaSlika = "";
    String sveSlike = "";
    String latitude, longitude;
    ArrayList<String> tempUriPreostaleSlike = new ArrayList<>();     // URL slika iz Firebase Database
    TextView vlasnik, naziv, cijena, povrsina, prijenosStatus, brojSoba, brojKupaonica;
    ArrayList<String> listaSlika = new ArrayList<>();
    Button pogledajLokaciju;
    CarouselView carouselView;
    String tv, klima, rublje, hladnjak, posude, stednjak;
    Boolean izmjenaPodatakaStana = false;
    String stanUIDzaIzmjenu;
    Boolean vecZapisan = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sazetak_informacija);

        izmjenaPodatakaStana = false;

        vlasnik = findViewById(R.id.cijenaStana);
        povrsina = findViewById(R.id.povrsina);
        naziv = findViewById(R.id.naziv);
        cijena = findViewById(R.id.cijenaCL);
        brojSoba = findViewById(R.id.brojSoba);
        brojKupaonica = findViewById(R.id.brojKupaonica);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        key = DATABASE.getReference().push().getKey();

        uploadLoadingBar = findViewById(R.id.progressBar);
        prijenosStatus = findViewById(R.id.prijenosStatus);
        uploadLoadingBar.bringToFront();
        prijenosStatus.bringToFront();

        carouselView = findViewById(R.id.carouselView);

        Intent intentExtras = getIntent();
        podaci = intentExtras.getExtras();

        vecZapisan = false;

        if (podaci != null && !podaci.isEmpty()) {

            if (podaci.containsKey("IZMJENASTANA")) {       // mijenja li korisnik podatke ili dodavanje novi stan
                izmjenaPodatakaStana = true;
            } else {
                izmjenaPodatakaStana = false;
            }

            //izmjenaPodatakaStana = podaci.containsKey("IZMJENASTANA") && Objects.requireNonNull(podaci.getString("IZMJENASTANA")).equals("IZMJENAPODATAKASTANA");

            stanUIDzaIzmjenu = podaci.getString("UIDSTAN");

            listaSlika = podaci.getStringArrayList("LISTASLIKA");

            glavnaSlika = listaSlika.get(0).replace("#&~*%#", "");
            sveSlike = podaci.getString("SVESLIKE");
            carouselView.setPageCount(listaSlika.size());
            carouselView.setImageListener(imageListener);

            povrsina.setText(podaci.getString("POVRSINA") + " m\u00B2");
            naziv.setText(podaci.getString("OPIS"));
            cijena.setText(podaci.getString("CIJENA") + " kn");
            vlasnik.setText(mAuth.getCurrentUser().getDisplayName());
            brojSoba.setText(podaci.getString("BROJSOBA"));
            brojKupaonica.setText(podaci.getString("KUPAONICA"));

            latitude = podaci.getString("LATITUDE");
            longitude = podaci.getString("LONGITUDE");

            tv = podaci.getString("TELEVIZIJA");
            klima = podaci.getString("KLIMA");
            rublje = podaci.getString("RUBLJE");
            hladnjak = podaci.getString("HLADNJAK");
            posude = podaci.getString("POSUDE");
            stednjak = podaci.getString("STEDNJAK");
        }

        predajOglas = findViewById(R.id.predajOglas);
        if (izmjenaPodatakaStana) {
            predajOglas.setText("Izmijeni oglas");
        }
        predajOglas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listaSlika.size() == 1) {
                    prijenosStatus.setVisibility(View.VISIBLE);
                    prijenosStatus.setText("Prijenos 1/1 slike...");
                    if (listaSlika.get(0).contains("firebasestorage.googleapis.com")) {
                        Calendar kalendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Zagreb"));
                        String datumDodavanja = DateFormat.format("dd.MM.yyyy HH:mm", kalendar).toString();
                        zapisiStanove(stanUIDzaIzmjenu, mAuth.getUid(), podaci.getString("OPIS"), Long.valueOf(podaci.getString("CIJENA")), Long.valueOf(podaci.getString("POVRSINA")), 0.0, 0.0, podaci.getString("BROJSOBA"), podaci.getString("KUPAONICA"),
                                glavnaSlika, sveSlike, podaci.getString("LATITUDE"), podaci.getString("LONGITUDE"), datumDodavanja, tv, klima, rublje, hladnjak, posude, stednjak);
                    } else {
                        uploadajGlavnuSliku(listaSlika);
                    }
                } else {
                    if (listaSlika.get(0).contains("firebasestorage.googleapis.com")) {
                        Log.d("ddaddada", "TU " + listaSlika.get(0));
                        Calendar kalendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Zagreb"));
                        String datumDodavanja = DateFormat.format("dd.MM.yyyy HH:mm", kalendar).toString();
                        zapisiStanove(stanUIDzaIzmjenu, mAuth.getUid(), podaci.getString("OPIS"), Long.valueOf(podaci.getString("CIJENA")), Long.valueOf(podaci.getString("POVRSINA")), 0.0, 0.0, podaci.getString("BROJSOBA"), podaci.getString("KUPAONICA"),
                                glavnaSlika, sveSlike, podaci.getString("LATITUDE"), podaci.getString("LONGITUDE"), datumDodavanja, tv, klima, rublje, hladnjak, posude, stednjak);
                    } else {
                        Log.d("ddaddada", "IPAK TU " + listaSlika.get(0));
                        DiscreteScrollAdapter discreteAdapter = new DiscreteScrollAdapter(listaSlika);
                        //uploadajPreostaleSlikeUBazuIStorage(discreteAdapter.vratiListuSlikuIzAdaptera());
                        uploadajPreostaleSlikeUBazuIStorage(listaSlika);
                    }

                }
            }
        });

        pogledajLokaciju = findViewById(R.id.pogledajLokaciju);
        pogledajLokaciju.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog dialog = new Dialog(SazetakInformacija.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                Objects.requireNonNull(dialog.getWindow()).clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                dialog.setContentView(R.layout.map_dialog);
                dialog.show();

                ImageButton closeMapsDialog = dialog.findViewById(R.id.closeMapsDialog);
                closeMapsDialog.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });

                MapView mMapView = dialog.findViewById(R.id.mapView);
                MapsInitializer.initialize(getApplicationContext());

                mMapView =  dialog.findViewById(R.id.mapView);
                mMapView.onCreate(dialog.onSaveInstanceState());
                mMapView.onResume();// needed to get the map to display immediately
                mMapView.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(final GoogleMap googleMap) {
                        LatLng lokacijaStana = new LatLng(Double.valueOf(latitude), Double.valueOf(longitude)); ////your lat lng
                        googleMap.addMarker(new MarkerOptions().position(lokacijaStana).title(podaci.getString("OPIS")));
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lokacijaStana, 15));
                        googleMap.getUiSettings().setZoomControlsEnabled(true);
                        //googleMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
                    }
                });
            }
        });
    }


    // učitava sve slike stana u Carousel View
    ImageListener imageListener = new ImageListener() {
        @Override
        public void setImageForPosition(int position, ImageView imageView) {
            Glide.with(getApplicationContext()).load(listaSlika.get(position)).apply((new RequestOptions().transforms(new FitCenter()))).into(imageView);
            //Picasso.get().load(sveSlike.get(position)).fit().centerInside().into(imageView);
        }
    };


    // zapisuje Stanove u Firebase Database
    private void zapisiStanove(final String randomKljucStana, String vlasnik, String naziv, Long cijena, Long povrsina, Double brojLajkova, Double brojKomentara, String brojSoba, String brojKupaonica, String glavnaSlika, String slike,
                               String latitude, String longitude, String datumDodavanja, String tv, String klima, String rublje, String hladnjak, String posude, String stednjak) {
            if (!vecZapisan) {
                Stanovi stan = new Stanovi(randomKljucStana, naziv, vlasnik, cijena, povrsina, brojLajkova, brojKomentara, brojSoba, brojKupaonica, glavnaSlika, slike, latitude, longitude, 0.0, "0", datumDodavanja, tv, klima, rublje, hladnjak, posude, stednjak, new HashMap<>());
                mDatabase.child("Stanovi").child(randomKljucStana).setValue(stan)     // setValue(stan) se sastoji od naziv, cijena, slike... - linija iznad
                        .addOnSuccessListener(aVoid -> {             // mora biti push() jer vlasnik može imati više stanova, a vlasnik svakog stana je zapisan pod polje 'vlasnik'
                            uploadLoadingBar.setVisibility(View.GONE);
                            vecZapisan = true;
                            Toast.makeText(getApplicationContext(), "Oglas uspješno postavljen!", Toast.LENGTH_LONG).show();
                            Intent pocetniEkran = new Intent(SazetakInformacija.this, ListaStanova.class);
                            startActivity(pocetniEkran);
                        })
                        .addOnFailureListener(e -> {
                            // Write failed
                            // ...
                        });
            }
    }


    // uploadaj samo glavnu (prvu) sliku
    public void uploadajGlavnuSliku(ArrayList listaURIsveSlike) {
        uploadLoadingBar.setVisibility(View.VISIBLE);
        StorageMetadata metadataImeSlike = new StorageMetadata.Builder()
                .setCustomMetadata("IMESLIKE", String.valueOf(listaURIsveSlike.get(0)))      // dodaje naziv slike u Metapodatke, za očuvanje redolijeda prilikom uploada
                .build();
        final StorageReference prvaSlika = mStorage.child(new StringBuilder("Images/").append(UUID.randomUUID().toString()).toString());
        //final StorageReference prvaSlika = mStorage.child(new StringBuilder("Images/").append(listaURIsveSlike.get(0)).toString());
        UploadTask uploadTaskPrvaSlika = prvaSlika.putFile(Uri.parse("file://" + listaURIsveSlike.get(0)), metadataImeSlike);
        Task<Uri> urlTaskPrvaSlika = uploadTaskPrvaSlika.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw Objects.requireNonNull(task.getException());
            }
            return prvaSlika.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                prijenosStatus.setVisibility(View.INVISIBLE);
                Uri downloadUri = task.getResult();
                glavnaSlika = downloadUri.toString();        // prvu sliku sprema u posaeban string
                Calendar kalendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Zagreb"));
                String datumDodavanja = DateFormat.format("dd.MM.yyyy HH:mm", kalendar).toString();
                if (!izmjenaPodatakaStana) {
                    String randomKljucStana = mDatabase.child("Stanovi").push().getKey();       // random ključ Stana --- identičan se dodaje Stanu kao stanUID
                    zapisiStanove(randomKljucStana, mAuth.getUid(), podaci.getString("OPIS"), Long.valueOf(podaci.getString("CIJENA")), Long.valueOf(podaci.getString("POVRSINA")), 0.0, 0.0, podaci.getString("BROJSOBA"), podaci.getString("KUPAONICA"),
                            glavnaSlika, glavnaSlika, podaci.getString("LATITUDE"), podaci.getString("LONGITUDE"), datumDodavanja, tv, klima, rublje, hladnjak, posude, stednjak);     // kada se uploadaju slike, spremi sve info. o stanu na Firebase
                } else {
                    zapisiStanove(stanUIDzaIzmjenu, mAuth.getUid(), podaci.getString("OPIS"), Long.valueOf(podaci.getString("CIJENA")), Long.valueOf(podaci.getString("POVRSINA")), 0.0, 0.0, podaci.getString("BROJSOBA"), podaci.getString("KUPAONICA"),
                            glavnaSlika, glavnaSlika, podaci.getString("LATITUDE"), podaci.getString("LONGITUDE"), datumDodavanja, tv, klima, rublje, hladnjak, posude, stednjak);     // kada se uploadaju slike, spremi sve info. o stanu na Firebase
                }
            } else {
                Snackbar.make(findViewById(android.R.id.content), "Pogreška prilikom prijenosa slika!", Snackbar.LENGTH_SHORT)
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
                Toast.makeText(SazetakInformacija.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    // uploada odabrane slike na Firebase Storage i sprema ih kao zapis za odabrani stan u Firebase Database
    public void uploadajPreostaleSlikeUBazuIStorage(ArrayList listaURIsveSlike) {
        uploadLoadingBar.setVisibility(View.VISIBLE);
        prijenosStatus.setVisibility(View.VISIBLE);
        brojacUploadnihSlika = 0;
        sveSlike = "";
        tempUriPreostaleSlike.clear();
        for (int nula=0; nula<listaURIsveSlike.size(); nula++) {      // potrebno zbog inicijalizacije tempUriPreostaleSlike, kako bi se slike mogle postaviti na ispravnu lokaciju ( u .set(j, downloadUri) )
            tempUriPreostaleSlike.add("0");
        }
        if (listaURIsveSlike.size() > 0) {
            // uploadaj sve preostale slike, osim glavne
            for (int i = 0; i < listaURIsveSlike.size(); i++) {
                StorageMetadata metadataImeSlike = new StorageMetadata.Builder()
                        .setCustomMetadata("IMESLIKE", String.valueOf(listaURIsveSlike.get(i)))       // dodaje naziv slike u Metapodatke, za očuvanje redolijeda prilikom uploada
                        .build();
                final StorageReference preostaleSlike = mStorage.child(new StringBuilder("Images/").append(UUID.randomUUID().toString()).toString());
                //final StorageReference preostaleSlike = mStorage.child(new StringBuilder("Images/").append(listaURIsveSlike.get(i)).toString());
                UploadTask uploadTaskPreostale = preostaleSlike.putFile(Uri.parse("file://" + listaURIsveSlike.get(i).toString()), metadataImeSlike);
                Task<Uri> urlTaskPreostale = uploadTaskPreostale.continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }
                    return preostaleSlike.getDownloadUrl();
                }).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        prijenosStatus.setText("Prijenos " + brojacUploadnihSlika + "/" + listaURIsveSlike.size() + " slika...");
                        Uri downloadUri = task.getResult();
                        preostaleSlike.getMetadata().addOnSuccessListener(storageMetadata -> {        // dobivanje Metapodataka uploadane slike za usporedbu naziva kako bi se sačuvao redoslijed
                            Log.d("fkdskgfds", "veličina: " + listaURIsveSlike.size());
                            for (int j=0; j<listaURIsveSlike.size(); j++) {
                                Log.d("fkdskgfds", "veličina: " + listaURIsveSlike.get(j));
                                if (storageMetadata.getCustomMetadata("IMESLIKE").equals(listaURIsveSlike.get(j))) {
                                    tempUriPreostaleSlike.set(j, String.valueOf(downloadUri));        // dodaje URI slike po redoslijedu kako su odabrane
                                    Log.d("fkdskgfds", tempUriPreostaleSlike.get(j) + "  " + downloadUri);
                                }
                            }
                            StringBuilder sb = new StringBuilder();
                            for (String s : tempUriPreostaleSlike) {
                                sb.append(s);
                                sb.append("#&~*%#");        // sve slike sprema u jedan string URL-ova razdvojen znakovima #$%&/?"
                            }
                            sveSlike = sb.toString();
                            glavnaSlika = tempUriPreostaleSlike.get(0);
                            brojacUploadnihSlika++;
                            if (brojacUploadnihSlika.equals(listaURIsveSlike.size()) && !vecZapisan) {
                                prijenosStatus.setVisibility(View.INVISIBLE);
                                Calendar kalendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Zagreb"));
                                String datumDodavanja = DateFormat.format("dd.MM.yyyy HH:mm", kalendar).toString();
                                if (!izmjenaPodatakaStana && !vecZapisan) {
                                    Log.d("izmjenaPodasdsa", izmjenaPodatakaStana + "  prvi put  " + glavnaSlika);
                                    String randomKljucStana = mDatabase.child("Stanovi").push().getKey();       // random ključ Stana --- identičan se dodaje Stanu kao stanUID
                                    zapisiStanove(randomKljucStana, mAuth.getUid(), podaci.getString("OPIS"), Long.valueOf(podaci.getString("CIJENA")), Long.valueOf(podaci.getString("POVRSINA")), 0.0, 0.0, podaci.getString("BROJSOBA"), podaci.getString("KUPAONICA"),
                                            glavnaSlika, sveSlike, podaci.getString("LATITUDE"), podaci.getString("LONGITUDE"), datumDodavanja, tv, klima, rublje, hladnjak, posude, stednjak);     // kada se uploadaju slike, spremi sve info. o stanu na Firebase
                                } else if(!vecZapisan) {
                                    Log.d("izmjenaPodasdsa", izmjenaPodatakaStana + "  drugi put");
                                    zapisiStanove(stanUIDzaIzmjenu, mAuth.getUid(), podaci.getString("OPIS"), Long.valueOf(podaci.getString("CIJENA")), Long.valueOf(podaci.getString("POVRSINA")), 0.0, 0.0, podaci.getString("BROJSOBA"), podaci.getString("KUPAONICA"),
                                            glavnaSlika, sveSlike, podaci.getString("LATITUDE"), podaci.getString("LONGITUDE"), datumDodavanja, tv, klima, rublje, hladnjak, posude, stednjak);     // kada se uploadaju slike, spremi sve info. o stanu na Firebase
                                }
                            }
                        });
                    } else {
                        Snackbar.make(findViewById(android.R.id.content), "Pogreška prilikom prijenosa slika!", Snackbar.LENGTH_SHORT)
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
                        Toast.makeText(SazetakInformacija.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else {
            Snackbar.make(findViewById(android.R.id.content), "Odaberite slike za upload!", Snackbar.LENGTH_SHORT)
                    .setActionTextColor(Color.RED)
                    .show();
        }
    }



    /*

    @Override
    public void onBackPressed()
    {
        if (uploadLoadingBar.getVisibility() != View.VISIBLE) {
            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(SazetakInformacija.this, android.R.style.Theme_Material_Dialog_Alert);
            } else {
                builder = new AlertDialog.Builder(SazetakInformacija.this);
            }
            builder.setTitle("Želite li odustati?")
                    .setMessage("Svi uneseni podaci će biti izbrisani.")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with delete
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            Intent listaStanova = new Intent(SazetakInformacija.this, ListaStanova.class);
            startActivity(listaStanova);
        }
    }
    */


}