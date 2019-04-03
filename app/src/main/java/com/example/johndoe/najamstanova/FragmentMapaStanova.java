package com.example.johndoe.najamstanova;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.app.ActivityOptions;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Property;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.MarkerManager;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;
import com.yarolegovich.lovelydialog.LovelyCustomDialog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class FragmentMapaStanova extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    Bundle podaci;
    ArrayList<String> listaUIDstanova = new ArrayList<>();
    ArrayList<String> listaNazivaStanova = new ArrayList<>();
    ArrayList<String> listaLatitude = new ArrayList<>();
    ArrayList<String> listaLongitude = new ArrayList<>();
    ArrayList<String> glavneSlikeStanova = new ArrayList<>();
    ArrayList<String> svelikeStanova = new ArrayList<>();
    ArrayList<String> cijeneStanova = new ArrayList<>();
    ArrayList<String> povrsineStanova = new ArrayList<>();
    ArrayList<String> listaVlasnika = new ArrayList<>();
    ArrayList<String> listaBrojSoba = new ArrayList<>();
    ArrayList<String> listaBrojKupaonica = new ArrayList<>();
    ArrayList<String> listaTV = new ArrayList<>();
    ArrayList<String> listaKlima = new ArrayList<>();
    ArrayList<String> listaRublje = new ArrayList<>();
    ArrayList<String> listaHladnjak = new ArrayList<>();
    ArrayList<String> listaPosude = new ArrayList<>();
    ArrayList<String> listaStednjak = new ArrayList<>();
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private ClusterManager<MyClusterItem> mClusterManager;      // varijabla za marker Cluster Manager
    BottomSheetBehavior bottomSheetBehavior;
    ImageView glavnaSlikaImageView;
    ImageButton directionsButton;
    TextView cijenaTextView, stanUIDTextView, stanNazivTextView, povrsinaStanaTextView;
    TextView vlasnikUIDTextView;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
    private Location mLastKnownLocation;
    private boolean mLocationPermissionGranted;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    Integer trenutnaPozicijaKlikaMarker = 0;
    PlaceAutocompleteFragment mjesta;
    LatLng odabranaPozicija = null;
    double mAmplitude = 1;
    double mFrequency = 10;
    Marker mMarker = null;
    Circle radijusKrugStanova = null;
    FloatingActionButton postavkePretrageFAB;
    Dialog dialogPostavkePretrage;
    IndicatorSeekBar seekBarRadijusKrug;
    Button spremiPostavke;
    int odabraniRadijusKruga = 500;
    Boolean nemojAnimiratiMarker = false;
    MarkerManager.Collection normalMarkersCollection;
    String trenutnaAdresa = "";
    EditText pretraziteAutocomplete;
    ProgressBar loadingBar;



    public FragmentMapaStanova() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Stanovi");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.lista_stanova_mapa_glavni, container, false);

        // loading bar za long click na mapu, kada se dohvaća adresa iz latitude i longitude
        loadingBar = view.findViewById(R.id.progressBar);
        loadingBar.bringToFront();
        loadingBar.setVisibility(View.GONE);

        // otvara Postavke Pretrage stanova na Mapi klikom na FAB
        postavkePretrageFAB = view.findViewById(R.id.postavkePretrage);
        postavkePretrageFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // učitaj Postavke Pretrage ako je korisnik već mijenjao postavke
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                odabraniRadijusKruga = preferences.getInt("odabraniRadijusKruga", 1000);

                dialogPostavkePretrage = new LovelyCustomDialog(getContext())
                        .setView(R.layout.dialog_postavke_pretrage)
                        .setTopColorRes(R.color.GlavnaBoja)
                        .setTitle("Postavke pretrage")
                        .setMessage("Odaberite postavke za pretraživanje stanova unutar željenog područja.")
                        .setIcon(R.drawable.filter)
                        .show();
                ConstraintLayout seekBarShowHide = dialogPostavkePretrage.findViewById(R.id.seekBarShowHide);
                seekBarRadijusKrug = dialogPostavkePretrage.findViewById(R.id.seekRadijusKrug);
                seekBarRadijusKrug.setProgress(odabraniRadijusKruga);
                if (radijusKrugStanova == null) {       // ako nema Kruga na Mapi, nemoj pokazati SeekBar za odabir Radijusa Kruga u Dialogu postvaki
                    seekBarShowHide.setVisibility(View.GONE);
                } else {
                    seekBarShowHide.setVisibility(View.VISIBLE);
                }
                seekBarRadijusKrug.setIndicatorTextFormat("${PROGRESS} m");
                seekBarRadijusKrug.setOnSeekChangeListener(new OnSeekChangeListener() {
                    @Override
                    public void onSeeking(SeekParams seekParams) {
                    }
                    @Override
                    public void onStartTrackingTouch(IndicatorSeekBar seekBar) {
                    }
                    @Override
                    public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
                        if (radijusKrugStanova != null) {
                            odabraniRadijusKruga = seekBar.getProgress();
                            nemojAnimiratiMarker = true;    // nemoj pirkazati animaciju markera dok korisnik povećavaju ili smanjuje radijus kruga
                            postaviKrugNaMapu(odabranaPozicija.latitude, odabranaPozicija.longitude, seekBar.getProgress(), trenutnaAdresa);
                        }
                    }
                });
                // sprema Postavke Pretrage stanova
                spremiPostavke = dialogPostavkePretrage.findViewById(R.id.spremiPostavke);
                spremiPostavke.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putInt("odabraniRadijusKruga", odabraniRadijusKruga);
                        editor.apply();
                        dialogPostavkePretrage.dismiss();
                    }
                });

            }
        });


        // postavlja Search za adresu i grad u Toolbar
        if (((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar() != null) {
            mjesta = (PlaceAutocompleteFragment) getActivity().getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
            Objects.requireNonNull(mjesta.getView()).setVisibility(View.GONE);
        }

        // Za trazenje adrese u Toolbaru (samo HR i adrese ulica)
        mjesta = (PlaceAutocompleteFragment) getActivity().getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        pretraziteAutocomplete = Objects.requireNonNull(mjesta.getView()).findViewById(R.id.place_autocomplete_search_input);
        pretraziteAutocomplete.setHint("Pretražite...");
        pretraziteAutocomplete.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        ImageView pretraziteIkona = (ImageView) ((LinearLayout) mjesta.getView()).getChildAt(0);
        pretraziteAutocomplete.setTextColor(getResources().getColor(R.color.white));
        pretraziteAutocomplete.setHintTextColor(getResources().getColor(R.color.white));
        pretraziteIkona.setColorFilter(getResources().getColor(R.color.white));

        AutocompleteFilter filterMjesta = new AutocompleteFilter.Builder()
                .setCountry("HR")
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                .build();
        mjesta.setFilter(filterMjesta);
        mjesta.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                odabranaPozicija = place.getLatLng();
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                //dodajMarkerPlaceAutocomplete(odabranaPozicija.latitude, odabranaPozicija.longitude, place.getAddress().toString());
                //dodajMarkerKlikMape(odabranaPozicija.latitude, odabranaPozicija.longitude, Objects.requireNonNull(place.getAddress()).toString());
                postaviKrugNaMapu(odabranaPozicija.latitude, odabranaPozicija.longitude, odabraniRadijusKruga, Objects.requireNonNull(place.getAddress()).toString());
            }
            @Override
            public void onError(Status status) {
                Toast.makeText(getContext(), status.toString(), Toast.LENGTH_SHORT).show();

            }
        });

        // Bottom Sheet view
        ConstraintLayout clBottomSheet = view.findViewById(R.id.bottom_sheet);

        glavnaSlikaImageView = clBottomSheet.findViewById(R.id.glavnaSlika);
        cijenaTextView = clBottomSheet.findViewById(R.id.cijenaStana);
        stanUIDTextView = clBottomSheet.findViewById(R.id.stanUID);
        stanNazivTextView = clBottomSheet.findViewById(R.id.nazivStana);
        povrsinaStanaTextView = clBottomSheet.findViewById(R.id.povrsinaStana);

        bottomSheetBehavior = BottomSheetBehavior.from(clBottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheetBehavior.setPeekHeight(0);
        bottomSheetBehavior.setHideable(true);

        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
                    postavkePretrageFAB.setVisibility(View.VISIBLE);
                } else {
                    postavkePretrageFAB.setVisibility(View.GONE);
                }
            }
            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });

        // klik na Bottom Sheet otvara Detalje Stana
        clBottomSheet.setOnClickListener(v -> {
            Bundle podaci = new Bundle();
            podaci.putString("UIDSTAN", listaUIDstanova.get(trenutnaPozicijaKlikaMarker));
            podaci.putString("SVESLIKE", svelikeStanova.get(trenutnaPozicijaKlikaMarker));
            podaci.putString("UIDVLASNIK", listaVlasnika.get(trenutnaPozicijaKlikaMarker));
            podaci.putString("NAZIV", listaNazivaStanova.get(trenutnaPozicijaKlikaMarker));
            podaci.putString("CIJENA", cijeneStanova.get(trenutnaPozicijaKlikaMarker));
            podaci.putString("POVRSINA", povrsineStanova.get(trenutnaPozicijaKlikaMarker));
            podaci.putString("LATITUDE", listaLatitude.get(trenutnaPozicijaKlikaMarker));
            podaci.putString("LONGITUDE", listaLongitude.get(trenutnaPozicijaKlikaMarker));
            podaci.putString("BROJSOBA", listaBrojSoba.get(trenutnaPozicijaKlikaMarker));
            podaci.putString("KUPAONICA", listaBrojKupaonica.get(trenutnaPozicijaKlikaMarker));
            podaci.putString("TV", listaBrojKupaonica.get(trenutnaPozicijaKlikaMarker));
            podaci.putString("KLIMA", listaBrojKupaonica.get(trenutnaPozicijaKlikaMarker));
            podaci.putString("RUBLJE", listaBrojKupaonica.get(trenutnaPozicijaKlikaMarker));
            podaci.putString("HLADNJAK", listaBrojKupaonica.get(trenutnaPozicijaKlikaMarker));
            podaci.putString("POSDUDE", listaBrojKupaonica.get(trenutnaPozicijaKlikaMarker));
            podaci.putString("STEDNJAK", listaBrojKupaonica.get(trenutnaPozicijaKlikaMarker));
            Intent detaljiStanova = new Intent(getContext(), DetaljiStanova.class);
            detaljiStanova.putExtras(podaci);
            startActivity(detaljiStanova, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
        });


        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        return view;

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setMapToolbarEnabled(false);   // iskljuci Map Toolbat kada korisnik klikne na Marker

        // postavlja Cluter Manager za one markere koji su u Clusteru
        mClusterManager = new ClusterManager<>(Objects.requireNonNull(getContext()), mMap);
        mClusterManager.setRenderer(new OwnIconRendered(getContext(), mMap, mClusterManager));
        // postavlja Marker Manager za normalne markere koji nisu u Clusteru
        normalMarkersCollection = mClusterManager.getMarkerManager().newCollection();


        // zatvara Bottom Sheet detalja stanova ako korisnik klikne na Mapu
        mMap.setOnMapClickListener(latLng -> {
            if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        });


        // postavlja Marker na poziciju mape gdje je korisnik kliknuo
        mMap.setOnMapLongClickListener(latLng -> {
            if (mMarker != null) {
                normalMarkersCollection.clear();
                Log.d("jebote", "ima ih: " + normalMarkersCollection.getMarkers().size());
                mMarker.remove();
            }
            //loadingBar.setVisibility(View.VISIBLE);
            String adresa = pretvoriLokacijuUAdresuUlice(latLng);
            //dodajMarkerKlikMape(latLng.latitude, latLng.longitude, adresa);
            postaviKrugNaMapu(latLng.latitude, latLng.longitude, odabraniRadijusKruga, adresa);
            //loadingBar.setVisibility(View.GONE);
        });


        mMap.setOnInfoWindowClickListener(marker -> {       // umjesto toga, prikaži samo Ikonu koja otvara Directions do mjesta, ili Ikonu za obrisati marker ako je Crveni Marker (normalni, ne u Clusteru)
            if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
            if (marker.getSnippet().equals("crveniMarker")) {
                Snackbar.make(Objects.requireNonNull(getActivity()).findViewById(android.R.id.content), "Postavke pretrage uklonjene.", Snackbar.LENGTH_SHORT)
                        .setActionTextColor(Color.RED)
                        .show();
                marker.remove();
                radijusKrugStanova.remove();
                radijusKrugStanova = null;
                resetirajClusterMarkere();
            } else {
                Intent directionsIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.google.com/maps/search/?api=1&query=" + marker.getPosition().latitude + "," + marker.getPosition().longitude));
                startActivity(directionsIntent);
            }
        });


        mMap.setOnInfoWindowCloseListener(new GoogleMap.OnInfoWindowCloseListener() {
            @Override
            public void onInfoWindowClose(Marker marker) {
                // zatvara BottomSheet kada korisnik klikne na Normalni Marker (crveni)
                /*
                if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED && marker.getZIndex() == 1) {
                    Log.d("dajga", "sakrio");
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                }
                */
            }
        });


        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            public View getInfoWindow(Marker arg0) {
                View markerInfoWindow = null;
                if (arg0.getSnippet() != null) {
                    if (arg0.getSnippet().equals("crveniMarker")) {
                        markerInfoWindow = getLayoutInflater().inflate(R.layout.marker_info_window_normal, null);
                    } else {
                        markerInfoWindow = getLayoutInflater().inflate(R.layout.marker_info_window, null);
                    }
                }
                return markerInfoWindow;
            }
            public View getInfoContents(Marker arg0) {

                return null;
            }
        });

        // listener kada korisnik pomice Marker po mapi
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
            }
            @Override
            public void onMarkerDrag(Marker marker) {
            }
            @Override
            public void onMarkerDragEnd(Marker marker) {
                marker.setVisible(false);
                LatLng pozicija = new LatLng(marker.getPosition().latitude, marker.getPosition().longitude);
                String adresa = pretvoriLokacijuUAdresuUlice(pozicija);
                //dodajMarkerKlikMape(marker.getPosition().latitude, marker.getPosition().longitude, adresa);
                postaviKrugNaMapu(marker.getPosition().latitude, marker.getPosition().longitude, odabraniRadijusKruga, adresa);
            }
        });

        LatLng mDefaultLocation = new LatLng(45.333805, 14.429187);     // samo za test
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, 15.0f));


        // prvo dohvati sve Latitude i Longitude stanova s Firebase Database, a tek onda dodaj Markere na Mapi
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listaUIDstanova.clear();
                listaLatitude.clear();
                listaLongitude.clear();
                for (DataSnapshot podaciOstanu : dataSnapshot.getChildren()) {
                    Log.d("postavio", podaciOstanu.child("stanUID").getValue(String.class));
                    listaUIDstanova.add(podaciOstanu.child("stanUID").getValue(String.class));
                    listaNazivaStanova.add(podaciOstanu.child("naziv").getValue(String.class));
                    listaLatitude.add(podaciOstanu.child("latitude").getValue(String.class));
                    listaLongitude.add(podaciOstanu.child("longitude").getValue(String.class));
                    glavneSlikeStanova.add(podaciOstanu.child("glavnaSlika").getValue(String.class));
                    svelikeStanova.add(podaciOstanu.child("slike").getValue(String.class));
                    cijeneStanova.add(String.valueOf(podaciOstanu.child("cijena").getValue(Long.class)));
                    povrsineStanova.add(String.valueOf(podaciOstanu.child("povrsina").getValue(Long.class)));
                    listaVlasnika.add(podaciOstanu.child("vlasnik").getValue(String.class));
                    listaBrojSoba.add(podaciOstanu.child("brojSoba").getValue(String.class));
                    listaBrojKupaonica.add(podaciOstanu.child("brojKupaonica").getValue(String.class));
                    listaTV.add(podaciOstanu.child("tv").getValue(String.class));
                    listaKlima.add(podaciOstanu.child("klima").getValue(String.class));
                    listaRublje.add(podaciOstanu.child("rublje").getValue(String.class));
                    listaHladnjak.add(podaciOstanu.child("hladnjak").getValue(String.class));
                    listaPosude.add(podaciOstanu.child("posude").getValue(String.class));
                    listaStednjak.add(podaciOstanu.child("stednjak").getValue(String.class));

                    Log.d("velicina1", "ucitao podatke, " + listaUIDstanova.size() + " ima stanova");
                }
                // kada su se učitali svi podaci s Firebasea, pokaži Cluster Markere na Mapi
                if (listaUIDstanova.size() > 0) {
                    postaviMarkerClusterer(listaUIDstanova, listaLatitude, listaLongitude);
                    //resetirajClusterMarkere();
                    Log.d("velicina2", mClusterManager.getAlgorithm().getItems().size() + " ovoliko pak ima Clustera");
                } else {
                    mClusterManager.clearItems();
                    mClusterManager.cluster();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    /*
    // dodaje marker na mapu
    public void dodajMarkerPlaceAutocomplete(Double latitude, Double longitude, String adresa) {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        if (mMarker != null) {
            mMarker.remove();       // izbriši prethodni Marker
        }
        LatLng pozicija = new LatLng(latitude, longitude);
        mMarker = mMap.addMarker(new MarkerOptions().position(pozicija)
                .anchor(0.5f, 0.5f).visible(false));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pozicija, 15.0f), new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                dropAnimateMarker(mMarker, mMap);       // animacija Markera tek kada završi Animacija Kamere
            }

            @Override
            public void onCancel() {
            }
        });
        if (adresa != null) {
            mjesta.setText(adresa);
        } else {
            mjesta.setText("");
        }
        odabranaPozicija = pozicija;    // ukoliko pozicija nije odabrana pomoću PlaceAutocompleteFragmenta, nego klikom na Mapu
    }
    */

    /*
    // dodaje marker na mapu kada korisnik klikne na nju
    public void dodajMarkerKlikMape(Double latitude, Double longitude, String adresa) {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        if (mMarker != null) {
            mMarker.remove();
        }
        if (radijusKrugStanova != null) {
            radijusKrugStanova.remove();
        }

        LatLng pozicija = new LatLng(latitude, longitude);


        MarkerOptions opcijeCrvenogMarkera = new MarkerOptions().position(pozicija)
                .draggable(true)
                .zIndex(0)
                .snippet("crveni")
                .visible(false);

        mMarker = mMap.addMarker(opcijeCrvenogMarkera);

/*
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pozicija, 15.0f), new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                //dropAnimateMarker(mMarker, mMap);       // animacija Markera tek kada završi Animacija Kamere
            }
            @Override
            public void onCancel() {
            }
        });

        odabranaPozicija = pozicija;
        if (adresa != null) {
            mjesta.setText(adresa);
        } else {
            mjesta.setText("");
        }

        postaviKrugNaMapu(latitude, longitude, 1000, adresa);

    }
    */


    public void postaviKrugNaMapu(Double latitude, Double longitude, Integer radijusKruga, String adresa) {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        if (mMarker != null) {
            mMarker.setVisible(false);
            mMarker.remove();
        }
        if (radijusKrugStanova != null) {
            radijusKrugStanova.remove();
        }

        LatLng pozicija = new LatLng(latitude, longitude);
        odabranaPozicija = pozicija;

        // prikaži adresu u PlaceAutocomplete EdiTextu
        if (adresa != null) {
            pretraziteAutocomplete.setText(adresa);
        } else {
            pretraziteAutocomplete.setText("");
        }

        MarkerOptions opcijeCrvenogMarkera = new MarkerOptions().position(pozicija)
                .draggable(true)
                .zIndex(0)
                .snippet("crveniMarker")
                .visible(false);

        mMarker = mMap.addMarker(opcijeCrvenogMarkera);

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pozicija, 15.0f), new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                dropAnimateMarker(mMarker, mMap, opcijeCrvenogMarkera);       // animacija Markera tek kada završi Animacija Kamere
            }
            @Override
            public void onCancel() {
            }
        });

        odabranaPozicija = pozicija;

        if (radijusKrugStanova != null) {
            radijusKrugStanova.remove();
        }

        if (mClusterManager.getAlgorithm().getItems().size() > 0) {
            mClusterManager.clearItems();
            mClusterManager.cluster();
        }

        CircleOptions circleOptions = new CircleOptions()
                .center(new LatLng(latitude, longitude))
                .radius(radijusKruga)   // odabrani radijus kruga u metrima
                .strokeWidth(0)
                .fillColor(Color.argb(32, 255, 0, 0))
                .clickable(false);

        radijusKrugStanova = mMap.addCircle(circleOptions);

        postaviClusterMarkereUnutarKruga();

    }


    // postavlja Cluster Markere na Mapu (zelene markere), samo one koji se nalaze unutar radijusa odabranog Kruga
    public void postaviClusterMarkereUnutarKruga() {
        for (int i = 0; i < listaLatitude.size(); i++) {
            float[] distance = new float[2];

            Location.distanceBetween(Double.parseDouble(listaLatitude.get(i)), Double.parseDouble(listaLongitude.get(i)),
                    radijusKrugStanova.getCenter().latitude, radijusKrugStanova.getCenter().longitude, distance);

            Log.d("odgovara", i + " < > " + listaUIDstanova.get(i) + " < > " + listaLatitude.get(i));

            if (distance[0] < radijusKrugStanova.getRadius()) {
                MyClusterItem markerCluster = new MyClusterItem(Double.parseDouble(listaLatitude.get(i)), Double.parseDouble(listaLongitude.get(i)), listaUIDstanova.get(i), cijeneStanova.get(i), glavneSlikeStanova.get(i), listaUIDstanova.get(i), povrsineStanova.get(i), listaNazivaStanova.get(i));
                mClusterManager.addItem(markerCluster);
            }
        }
        if (mClusterManager.getAlgorithm().getItems().size() == 0) {
            mClusterManager.clearItems();
            mClusterManager.cluster();
            Snackbar.make(Objects.requireNonNull(getActivity()).findViewById(android.R.id.content), "Nema pronađenih stanova u blizini ove lokacije.", Snackbar.LENGTH_SHORT)
                    .setActionTextColor(Color.RED)
                    .show();
        } else {
            mClusterManager.cluster();
            Integer brojPronadenihStanova = mClusterManager.getAlgorithm().getItems().size();
            String porukaPronadenoStanova = "";
            if (brojPronadenihStanova == 1) {
                porukaPronadenoStanova = "Pronađen 1 stan.";
            } else if (brojPronadenihStanova == 2 || brojPronadenihStanova == 3 || brojPronadenihStanova == 4) {
                porukaPronadenoStanova = "Pronađena " + brojPronadenihStanova + " stana.";
            } else {
                porukaPronadenoStanova = "Pronađeno " + brojPronadenihStanova + " stanova.";
            }
            Snackbar.make(Objects.requireNonNull(getActivity()).findViewById(android.R.id.content), porukaPronadenoStanova, Snackbar.LENGTH_SHORT)
                    .setActionTextColor(Color.RED)
                    .show();
        }
    }


    // nanovo postavlja sve Cluster Markere, bez uvjeta da se moraju nalaziti unutar odabranog radijusa Kruga (resetira filtere)
    public void resetirajClusterMarkere() {
        if (mClusterManager.getAlgorithm().getItems().size() > 0) {
            mClusterManager.clearItems();
            mClusterManager.cluster();
        }
        for (int i = 0; i < listaLatitude.size(); i++) {
            MyClusterItem markerCluster = new MyClusterItem(Double.parseDouble(listaLatitude.get(i)), Double.parseDouble(listaLongitude.get(i)), listaUIDstanova.get(i), cijeneStanova.get(i), glavneSlikeStanova.get(i), listaUIDstanova.get(i), povrsineStanova.get(i), listaNazivaStanova.get(i));
            mClusterManager.addItem(markerCluster);

            if (mClusterManager.getAlgorithm().getItems().size() == 0) {
                mClusterManager.clearItems();
                mClusterManager.cluster();
            } else {
                mClusterManager.cluster();
            }
        }
    }


    // za animaciju Markera na Mapu kada korisnik unese adresu u blizini gdje traži stan
    void dropAnimateMarker(Marker marker, GoogleMap map, MarkerOptions opcijeCrvenogMarkera) {
        final LatLng finalPosition = new LatLng(marker.getPosition().latitude, marker.getPosition().longitude);
        Projection projection = map.getProjection();
        Point startPoint = projection.toScreenLocation(finalPosition);
        startPoint.y = 0;
        final LatLng startLatLng = projection.fromScreenLocation(startPoint);
        final Interpolator interpolator = new MyBounceInterpolator(0.11, 4.6);

        TypeEvaluator<LatLng> typeEvaluator = new TypeEvaluator<LatLng>() {
            @Override
            public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
                float t = interpolator.getInterpolation(fraction);
                double lng = t * finalPosition.longitude + (1 - t) * startLatLng.longitude;
                double lat = t * finalPosition.latitude + (1 - t) * startLatLng.latitude;
                return new LatLng(lat, lng);
            }
        };
        Property<Marker, LatLng> property = Property.of(Marker.class, LatLng.class, "position");
        ObjectAnimator animator = ObjectAnimator.ofObject(marker, property, typeEvaluator, finalPosition);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                marker.setVisible(true);
            }
            // kada završi animacija Crvenog Markera, taj Marker sakrij i dodaj ga u Cluster Markera, a onda njega učini vidljivim
            @Override
            public void onAnimationEnd(Animator animation) {
                //marker.setVisible(false);
                //marker.remove();
                mMarker.remove();
                opcijeCrvenogMarkera.visible(true);
                normalMarkersCollection.clear();
                normalMarkersCollection.addMarker(opcijeCrvenogMarkera);
            }
            @Override
            public void onAnimationCancel(Animator animation) {
            }
            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        animator.setDuration(500);
        if (!nemojAnimiratiMarker) {
            animator.start();
        } else {
            nemojAnimiratiMarker = false;
        }
    }


    private void postaviMarkerClusterer(ArrayList listaUIDstanova, ArrayList listaLatitude, ArrayList listaLongitude) {

        Integer provjeraBrisanjaMarkeraPrije = mClusterManager.getAlgorithm().getItems().size();     // za provjeru je li Stan dodan ili je izbrisan - onda sakrij Bottom Sheet, tj. detalje tog stana koji je izbrisan

        // kada korisnik klikne na jedan od markera u Clasteru (zeleni marker)
        mClusterManager.setOnClusterItemClickListener(myClusterItem -> {
            postavkePretrageFAB.setVisibility(View.GONE);
            /*
            trenutnaPozicijaKlikaMarker = listaUIDstanova.indexOf(myClusterItem.getTitle());                OVO SAM ZAMIJENIO !!!!!!!!!!!
            String glavnaSlika = glavneSlikeStanova.get(trenutnaPozicijaKlikaMarker);
            Glide.with(Objects.requireNonNull(getContext())).load(glavnaSlika).apply((new RequestOptions().transforms(new FitCenter()))).transition(DrawableTransitionOptions.withCrossFade()).into(glavnaSlikaImageView);
            stanUIDTextView.setText(listaUIDstanova.get(trenutnaPozicijaKlikaMarker).toString());
            cijenaTextView.setText(cijeneStanova.get(trenutnaPozicijaKlikaMarker));
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            return false;
            */
            Glide.with(Objects.requireNonNull(getContext())).load(myClusterItem.getGlavnaSlika()).apply((new RequestOptions().transforms(new FitCenter()))).transition(DrawableTransitionOptions.withCrossFade()).into(glavnaSlikaImageView);
            String stanUID = myClusterItem.getUIDstana();
            stanUIDTextView.setText(stanUID);
            for (int indeks=0; indeks<listaUIDstanova.size(); indeks++) {
                if (listaUIDstanova.get(indeks).equals(stanUID)) {
                    trenutnaPozicijaKlikaMarker = indeks;
                    break;
                }
            }
            stanNazivTextView.setText(myClusterItem.getNazivStana());
            cijenaTextView.setText(myClusterItem.getCijenaStana() + " kn");
            povrsinaStanaTextView.setText(myClusterItem.getPovrsinaStana() + " m\u00B2");
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            return false;
        });

        mMap.setOnCameraIdleListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager.getMarkerManager());      // umjesto mMap.setOnMarkerClickListener(mClusterManager);  ide ova linija, zbog klika na Normalni Marker koji nije u Clusteru

        // klik na Normalni Marker zatvara Bottom Sheet
        normalMarkersCollection.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                }
                return false;
            }
        });

        normalMarkersCollection.setOnInfoWindowClickListener(Marker::remove);

        mClusterManager.clearItems();
        mClusterManager.cluster();

        if (listaUIDstanova.size() > 0) {
            for (int i = 0; i < listaUIDstanova.size(); i++) {
                MyClusterItem markerCluster = new MyClusterItem(Double.parseDouble(listaLatitude.get(i).toString()), Double.parseDouble(listaLongitude.get(i).toString()), listaUIDstanova.get(i).toString(), cijeneStanova.get(i), glavneSlikeStanova.get(i), listaUIDstanova.get(i).toString(), povrsineStanova.get(i), listaNazivaStanova.get(i));
                mClusterManager.addItem(markerCluster);
                //mClusterManager.cluster();
            }
        }

        mClusterManager.cluster();

        Integer provjeraBrisanjaMarkeraPoslije = mClusterManager.getAlgorithm().getItems().size();

        if (provjeraBrisanjaMarkeraPoslije < provjeraBrisanjaMarkeraPrije) {       // znaci da ima manje markera nego prije, odnosno da je stan obrisan - onda sakrij Bottom Sheet, tj. detalje tog stana koji je izbrisan
            if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        }

        Log.d("velicina3", " Ostalo ih je: " + mClusterManager.getAlgorithm().getItems().size());

    }


    // Dobivanje latitude i longitude iz adrese ulice
    public String pretvoriLokacijuUAdresuUlice(LatLng pozicijaKlika) {
        List<Address> addresses = null;
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        Address adresa;
        String pronadjenaAdresa = null;
        try {
            addresses = geocoder.getFromLocation(
                    pozicijaKlika.latitude,
                    pozicijaKlika.longitude,
                    1);     // vrati samo prvu adresu iz mjesta na mapi na koje je korisnik kliknuo
        } catch (IOException ioException) {
            Toast.makeText(getContext(), "Adresa ove lokacija nije pronađena.", Toast.LENGTH_SHORT).show();
            pretraziteAutocomplete.setText("");
        }

        if (addresses == null || addresses.size() == 0) {   // ako adresa nije pronađena
            Toast.makeText(getContext(), "Adresa ove lokacije nije pronađena.", Toast.LENGTH_SHORT).show();
            pretraziteAutocomplete.setText("");
        } else {
            adresa = addresses.get(0);
            pronadjenaAdresa = adresa.getAddressLine(0);    // vrati samo adresu iz svih ostalih podataka
            //String street = pronadjenaAdresa.getThoroughfare(); ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        }
        trenutnaAdresa = String.valueOf(pronadjenaAdresa);
        return trenutnaAdresa;
    }


    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getContext()), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            mMap.setMyLocationEnabled(true);    // button za trenutnu lokaciju
        } else {
            ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }


    // sprema stanje mape na onPause()
    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // Ako je zahtjev otkazan, rezultirajuci array je prazan
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                    getDeviceLocation();
                }
            }
        }
    }


    private void getDeviceLocation() {
        try {
            if (mLocationPermissionGranted) {
                Task locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(Objects.requireNonNull(getActivity()), task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        mLastKnownLocation = (Location) task.getResult();         // postavi kameru na trenutnu lokaciju korisnika
                        if (mLastKnownLocation.getLatitude() != 0 && mLastKnownLocation.getLongitude() != 0) {
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), 15.0f));
                            MarkerOptions markerOptions = new MarkerOptions();
                            LatLng trenutnaLokacija = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
                            markerOptions.position(trenutnaLokacija);
                            mMap.clear();
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(trenutnaLokacija, 15.0f));
                        }
                    } else {
                        LatLng mDefaultLocation = new LatLng(45.838979, 15.979779);      // default lokacija grad Zagreb
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, 10.0f));
                        mMap.getUiSettings().setMyLocationButtonEnabled(false);
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }
}


// stvaranje vlastitih Opcija Markera u Clusteru, poput ikone Cluster Markera, zIndexa itd.
class OwnIconRendered extends DefaultClusterRenderer<MyClusterItem> {
    OwnIconRendered(Context context, GoogleMap map, ClusterManager<MyClusterItem> clusterManager) {
        super(context, map, clusterManager);
    }
    @Override
    protected boolean shouldRenderAsCluster(Cluster<MyClusterItem> cluster) {
        return cluster.getSize() > 1;        // počni clustering markera ako se barem 2 markera preklapaju
    }

    @Override
    protected void onBeforeClusterItemRendered(MyClusterItem item, MarkerOptions markerOptions) {
            markerOptions.visible(true);
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_lista));
            markerOptions.zIndex(1);      // Ikona Cluster Markera se nalazi ispred ikone Markera na klik mape ili upisom u PlaceAutocomplete
        super.onBeforeClusterItemRendered(item, markerOptions);
    }
}


