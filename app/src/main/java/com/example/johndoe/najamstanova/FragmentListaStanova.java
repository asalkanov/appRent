package com.example.johndoe.najamstanova;

import android.Manifest;
import android.app.ActivityOptions;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.gson.Gson;
import com.robertlevonyan.views.customfloatingactionbutton.FloatingActionLayout;
import com.yarolegovich.lovelydialog.LovelyCustomDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

import im.delight.android.location.SimpleLocation;


public class FragmentListaStanova extends Fragment {

    private DatabaseReference mDatabase;
    private static final int FROM_GALLERY = 1;
    RecyclerView mRecyclerView;
    LinearLayoutManager linearLayoutManager;
    ImageView slika;
    ProgressBar progressDialog;
    FirebaseRecyclerAdapter<Stanovi, StanoviHolder> FirebaseAdapter;
    private String key;
    private static final FirebaseDatabase DATABASE = FirebaseDatabase.getInstance();
    private static final FirebaseStorage STORAGE = FirebaseStorage.getInstance();
    private FirebaseAuth mAuth;
    FloatingActionLayout postavkeFAB;
    int spremljenaPozicija = 0;
    ArrayList<String> listaUIDstanova = new ArrayList<>();
    ArrayList<String> tempListaUIDstanova = new ArrayList<>();
    ArrayList<String> listaLatitude = new ArrayList<>();
    ArrayList<String> listaLongitude = new ArrayList<>();
    ArrayList<String> listaUdaljenosti = new ArrayList<>();
    ArrayList<String> listaTrajanja = new ArrayList<>();
    ArrayList<String> onlineDistanceArray = new ArrayList<>();
    ArrayList<String> onlineDurationArray = new ArrayList<>();
    ArrayList<String> listaNizeCijene = new ArrayList<>();
    ArrayList<String> listaViseCijene = new ArrayList<>();
    public static String API_KEY = "AIzaSyCi-OxElbcjbcBOAALV3dRSFxvnzGlVmhE";
    private final String KEY_RECYCLER_STATE = "recycler_state";
    private static Bundle mBundleRecyclerViewState;
    PlaceAutocompleteFragment mjesta;
    //Dialog dialogPostavkePretrage;
    String odabranaNizaCijena, odabranaVisaCijena;
    Button spremiPostavke;
    Dialog dialogKomentar;
    Integer odabraniIndeksNizaCijena = 0;
    Integer odabraniIndeksVisaCijena = 0;
    Bundle podaci;
    Query query;
    private SimpleLocation location;
    Boolean permissionGranted = false;
    public static final String TAG = "VolleyTAG";
    String odabraniLatitude = "0.0";
    String odabraniLongitude = "0.0";
    String odabranaAdresa;
    String checkboxCijeneAktiviran = "false";
    String checkboxLokacijeAktiviran = "false";
    String checkboxPovrsinaAktiviran = "false";
    SharedPreferences preferences;
    String odabranoSortiranje = "rastuce";
    String nizaCijena, visaCijena, odabraniQueryCijene, nizaPovrsina, visaPovrsina;
    AlertDialog alertDialog;
    AlertDialog.Builder builder1;
    AlertDialog alert11;


    public FragmentListaStanova() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intentExtras = getActivity().getIntent();
        podaci = intentExtras.getExtras();

        mAuth = FirebaseAuth.getInstance();

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getContext()), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1987);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
        }

        listaLatitude.clear();
        listaLongitude.clear();

        preferences = PreferenceManager.getDefaultSharedPreferences(getContext());      // učitaj podatak je li checkbox s odabirom lokacije za sortiranje ranije označen ili nije
        checkboxCijeneAktiviran = preferences.getString("CHECKBOXCIJENE", "false");
        checkboxLokacijeAktiviran = preferences.getString("CHECKBOXLOKACIJA", "false");
        checkboxPovrsinaAktiviran = preferences.getString("CHECKBOXPOVRSINA", "false");
        odabranoSortiranje = preferences.getString("SORTIRANJE", "rastuce");            // // učitaj podatak je li nčin sortiranja ranije odabran ili nije
        nizaCijena = preferences.getString("nizaCijena", "Bilo koja");
        visaCijena = preferences.getString("visaCijena", "Bilo koja");
        nizaPovrsina = preferences.getString("nizaPovrsina", "0");
        visaPovrsina = preferences.getString("visaPovrsina", "0");
        odabraniQueryCijene = preferences.getString("QUERY", null);
        odabraniLatitude = preferences.getString("ODABRANILATITUDE", "0.0");
        odabraniLongitude = preferences.getString("ODABRANILONGITUDE", "0.0");
        odabranaAdresa = preferences.getString("ODABRANAADRESA", "");

        Log.d("kakosamsort", odabranoSortiranje + "   cijene: " + checkboxCijeneAktiviran + "    lokacija: " + checkboxLokacijeAktiviran  +  "  povrsina: " +  checkboxPovrsinaAktiviran +  "    "  + odabraniQueryCijene + "  " + odabraniLatitude);

        // ako je korisnik sortirao i filtrirao podatke u SortAndFilterPostavke aktivnosti
        if (podaci != null && !podaci.isEmpty()) {
            Log.d("sortiranje", "iz sortiranja");
            if (podaci.containsKey("SORTANDFILTER")) {
                String odabirCijene = podaci.getString("QUERY");
                String nizaCijena = podaci.getString("NIZACIJENA");
                String visaCijena = podaci.getString("VISACIJENA");
                String nizaPovrsina = podaci.getString("NIZAPOVRSINA");
                String visaPovrsina = podaci.getString("VISAPOVRSINA");
                odabraniLatitude = podaci.getString("ODABRANILATITUDE", "0.0");
                odabraniLongitude = podaci.getString("ODABRANILONGITUDE", "0.0");
                odabraniLongitude = podaci.getString("ODABRANILONGITUDE", "0.0");
                checkboxCijeneAktiviran = podaci.getString("CHECKBOXCIJENE", "false");
                checkboxLokacijeAktiviran = podaci.getString("CHECKBOXLOKACIJA", "false");
                checkboxPovrsinaAktiviran = podaci.getString("CHECKBOXPOVRSINA", "false");
                odabranaAdresa = podaci.getString("ADRESA");
                if (odabirCijene != null && checkboxCijeneAktiviran.equals("true")) {
                    Log.d("kakosamsort", "sortiranje po CIJENI " + nizaCijena + "  " + visaCijena);
                    odabranoSortiranje = podaci.getString("SORTIRANJE");
                    if (odabirCijene.equals("1")) {
                        Log.d("kakosamsort", "1. slucaj");
                        query = FirebaseDatabase.getInstance()
                                .getReference()
                                .child("Stanovi")
                                .orderByChild("cijena")
                                .startAt(Integer.valueOf(nizaCijena))
                                .limitToLast(50);
                    } else if (odabirCijene.equals("2")) {
                        Log.d("kakosamsort", "2. slucaj");
                        query = FirebaseDatabase.getInstance()
                                .getReference()
                                .child("Stanovi")
                                .orderByChild("cijena")
                                .endAt(Integer.valueOf(visaCijena))
                                .limitToLast(50);
                    } else if (odabirCijene.equals("3")) {
                        Log.d("kakosamsort", "3. slucaj: " + nizaCijena + " - " + visaCijena);
                        query = FirebaseDatabase.getInstance()
                                .getReference()
                                .child("Stanovi")
                                .orderByChild("cijena")
                                .startAt(Integer.valueOf(nizaCijena))
                                .endAt(Integer.valueOf(visaCijena))
                                .limitToLast(50);
                    } else {
                        Log.d("kakosamsort", "4. slucaj");
                        query = FirebaseDatabase.getInstance()
                                .getReference()
                                .child("Stanovi")
                                .orderByChild("cijena")
                                .limitToLast(50);
                    }
                } else if (checkboxLokacijeAktiviran.equals("true")) {
                    Log.d("kakosamsort", "sortiranje po LOKACIJI");
                    query = FirebaseDatabase.getInstance()
                            .getReference()
                            .child("Stanovi")
                            .orderByChild("udaljenost")
                            .limitToLast(50);
                } else if (checkboxPovrsinaAktiviran.equals("true")) {
                    Log.d("kakosamsort", "sortiranje po POVRSINI 1:  " + nizaPovrsina  + "  " + visaPovrsina);
                    query = FirebaseDatabase.getInstance()
                            .getReference()
                            .child("Stanovi")
                            .orderByChild("povrsina")
                            .startAt(Integer.valueOf(nizaPovrsina))
                            .endAt(Integer.valueOf(visaPovrsina))
                            .limitToLast(50);
                } else {
                    // općeniti Query koji dohvaća sve podatke o stanovima s Firebasea  -- slučaj kada niti jedan CheckBox u SortAndFilter nije aktiviran
                    Log.d("kakosamsort", "opceniti NE iz sort");
                    query = FirebaseDatabase.getInstance()
                            .getReference()
                            .child("Stanovi")
                            .orderByChild("cijena")
                            .limitToLast(50);
                }
            }
        } else if (odabraniQueryCijene != null && checkboxCijeneAktiviran.equals("true")) {         // ako su podaci učitani iz SharedPreferences, a ne iz SortAndFilterActivity
            Log.d("kakosamsort", "sortiranje po CIJENI spremljeno");
            //odabranoSortiranje = podaci.getString("SORTIRANJE");
            if (odabraniQueryCijene.equals("1")) {
                Log.d("kakosamsort", "1. slucaj");
                query = FirebaseDatabase.getInstance()
                        .getReference()
                        .child("Stanovi")
                        .orderByChild("cijena")
                        .startAt(Integer.valueOf(nizaCijena))
                        .limitToLast(50);
            } else if (odabraniQueryCijene.equals("2")) {
                Log.d("kakosamsort", "2. slucaj");
                query = FirebaseDatabase.getInstance()
                        .getReference()
                        .child("Stanovi")
                        .orderByChild("cijena")
                        .endAt(Integer.valueOf(visaCijena))
                        .limitToLast(50);
            } else if (odabraniQueryCijene.equals("3")) {
                Log.d("kakosamsort", "3. slucaj");
                query = FirebaseDatabase.getInstance()
                        .getReference()
                        .child("Stanovi")
                        .orderByChild("cijena")
                        .startAt(Integer.valueOf(nizaCijena))
                        .endAt(Integer.valueOf(visaCijena))
                        .limitToLast(50);
            } else {
                Log.d("kakosamsort", "4. slucaj");
                query = FirebaseDatabase.getInstance()
                        .getReference()
                        .child("Stanovi")
                        .orderByChild("cijena")
                        .limitToLast(50);
            }
        } else if (checkboxLokacijeAktiviran.equals("true")) {
        Log.d("kakosamsort", "sortiranje po LOKACIJI spremljeno");
        query = FirebaseDatabase.getInstance()
                .getReference()
                .child("Stanovi")
                .orderByChild("udaljenost")
                .limitToLast(50);
        }  else if (checkboxPovrsinaAktiviran.equals("true")) {
            Log.d("kakosamsort", "sortiranje po POVRSINI 2:  " + nizaPovrsina  + "  " + visaPovrsina);
            query = FirebaseDatabase.getInstance()
                    .getReference()
                    .child("Stanovi")
                    .orderByChild("povrsina")
                    .startAt(Integer.valueOf(nizaPovrsina))
                    .endAt(Integer.valueOf(visaPovrsina))
                    .limitToLast(50);
        } else {                                                   // nema podataka za sortiranje za učitati iz SP niti iz SortAndFilter activitya
            Log.d("kakosamsort", "NE iz sortiranja");
            // općeniti Query koji dohvaća sve podatke o stanovima s Firebasea
            query = FirebaseDatabase.getInstance()
                    .getReference()
                    .child("Stanovi")
                    .orderByChild("cijena")
                    .limitToLast(50);
        }


        Log.d("kakosamosort", String.valueOf(query));

        // prvo dohvati sve latitude i longitude za stanove, kako bi potom mogao izračunati udaljenost i trajanje za svaki stan
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Stanovi");
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    tempListaUIDstanova.add(Objects.requireNonNull(singleSnapshot.child("stanUID").getValue()).toString());
                    listaLatitude.add(Objects.requireNonNull(singleSnapshot.child("latitude").getValue()).toString());
                    listaLongitude.add(Objects.requireNonNull(singleSnapshot.child("longitude").getValue()).toString());
                }
                izracunajUdaljenostITrajanje();     // tek kada su dohvaćeni svi podaci stanova o latitude i longitude, izračunaj udaljenost i trajanje do stana pomoću Distance Matrix API-ja
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_lista_stanova, container, false);
        Toolbar myToolbar = view.findViewById(R.id.my_toolbar);
        myToolbar.setBackgroundColor(getResources().getColor(R.color.GlavnaBoja));
        myToolbar.setTitle("Najam stanova");
        //setSupportActionBar(myToolbar);

        //ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1234);

        permissionGranted = true;
        Context context = getContext();
        boolean requireFineGranularity = true;
        boolean passiveMode = false;
        long updateIntervalInMilliseconds = 10 * 60 * 1000;
        boolean requireNewLocation = false;
        getLocation(context, requireFineGranularity, passiveMode, updateIntervalInMilliseconds, requireNewLocation);

        izracunajUdaljenostITrajanje();

        if (((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar() != null) {
            Objects.requireNonNull(((AppCompatActivity) getActivity()).getSupportActionBar()).setCustomView(R.layout.search_address);
            Objects.requireNonNull(((AppCompatActivity) getActivity()).getSupportActionBar()).setDisplayShowCustomEnabled(true);
            Objects.requireNonNull(((AppCompatActivity) getActivity()).getSupportActionBar()).setDisplayShowTitleEnabled(false);    // sakrij Naslov u Toolbaru
            mjesta = (PlaceAutocompleteFragment) getActivity().getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
            Objects.requireNonNull(mjesta.getView()).setVisibility(View.GONE);
        }

        progressDialog = view.findViewById(R.id.spin_kit);
        progressDialog.bringToFront();
        progressDialog.setVisibility(View.VISIBLE);

        // iz prikaza Liste Stanova u prikaz Mape Stanova
        postavkeFAB = view.findViewById(R.id.postavkeFAB);
        postavkeFAB.setEnabled(false);
        postavkeFAB.setOnClickListener(v -> {
            Intent sortAndFilter = new Intent(getContext(), SortAndFilterPostavke.class);
            startActivity(sortAndFilter);
            getActivity().overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_to_bottom);
        });


        mDatabase = FirebaseDatabase.getInstance().getReference();

        mRecyclerView = view.findViewById(R.id.mRecView);


        /*
        if (odabranoSortiranje.equals("rastuce")) {
            linearLayoutManager = new LinearLayoutManager(getContext());
            mRecyclerView.setLayoutManager(linearLayoutManager);
        } else if (odabranoSortiranje.equals("padajuce")) {
            linearLayoutManager = new LinearLayoutManager(getContext());
            linearLayoutManager.setReverseLayout(true);
            linearLayoutManager.setStackFromEnd(true);
            mRecyclerView.setLayoutManager(linearLayoutManager);
        } else {
            linearLayoutManager = new LinearLayoutManager(getContext());
            mRecyclerView.setLayoutManager(linearLayoutManager);
        }
        */

        linearLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(linearLayoutManager);




        RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(getContext()) {
            @Override protected int getVerticalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_START;
            }
        };

        key = DATABASE.getReference().push().getKey();

        /*
        // općeniti Query koji dohvaća sve podatke o stanovima s Firebasea
        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("Stanovi")
                .limitToLast(50);
         */

        //dohvatiPodatke(query);

        return view;
    }


    /* Glavna metoda za dobivanje podataka o stanovima s Firebasea */
    public void dohvatiPodatke(Query query) {
        FirebaseRecyclerOptions<Stanovi> opcijeFirebaseAdaptera =
                new FirebaseRecyclerOptions.Builder<Stanovi>()
                        .setQuery(query, Stanovi.class)
                        .build();

        FirebaseAdapter = new FirebaseRecyclerAdapter<Stanovi, StanoviHolder>(opcijeFirebaseAdaptera) {
            @NonNull
            @Override
            public StanoviHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.lista_stanova_izgled, parent, false);
                return new StanoviHolder(view);
            }
            @Override
            protected void onBindViewHolder(@NonNull StanoviHolder holder, int position, @NonNull final Stanovi stan) {
                holder.setNaziv(stan.getNaziv());
                holder.setCijena(stan.getCijena() + " kn");
                holder.setPovrsina(stan.getPovrsina() + " m\u00B2");
                //holder.setUdaljenost(stan.getUdaljenost() + " km");
                List<String> udaljenosti;
                //udaljenosti = stan.getUdaljenosti(mAuth.getUid());

                GenericTypeIndicator<HashMap<String, Object>> objectsGTypeInd = new GenericTypeIndicator<HashMap<String, Object>>() {};
                HashMap<String, Object> objectHashMap = stan.getUdaljenosti(mAuth.getUid());
                //ArrayList<Object> objectArrayList = new ArrayList<Object>(Integer.parseInt(objectHashMap.get(0)));

                Object[] array = objectHashMap.values().toArray();
                for (Object anArray : array) {
                    String tempString = anArray.toString();
                    String udaljenost = tempString.substring(tempString.lastIndexOf("=")+1, tempString.lastIndexOf("}"));
                    String trajanje = tempString.substring(tempString.indexOf("=")+1, tempString.indexOf(","));
                    Log.d("fsdfd", udaljenost + " <<<>>>> " + trajanje);
                    holder.setUdaljenost(udaljenost + " km");
                    holder.setTrajanje(trajanje);
                }


                holder.setSlika(stan.getGlavnaSlika(), getContext());       // učitavanje slike u Listi Stanova -- pomoću StanoviHolder
                holder.setBrojLajkova(String.valueOf(stan.getBrojLajkova()).replace(".0", ""));
                holder.setBrojKomentara(String.valueOf(stan.getBrojKomentara()).replace(".0", ""));


                holder.likeImage.setImageDrawable(getResources().getDrawable(R.drawable.already_liked));
                // mijenja ikonu za Lajk ako je korisnik (dis)lajkao stan
                mDatabase.child("Lajkovi").child(mAuth.getUid() + "**" + stan.getStanUID()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            //if (stan.getVlasnik().equals(mAuth.getUid())) {         // korisnik čiji je stan ne može lajkati niti komentirati vlastiti stan
                                holder.likeImage.setImageDrawable(getResources().getDrawable(R.drawable.thumb));
                             //   holder.likeImage.setEnabled(false);
                              //  Log.d("ugasio", "njegov je, ne moze lajkat");
                            //} else {
                             //   holder.likeImage.setImageDrawable(getResources().getDrawable(R.drawable.thumb));
                             //   holder.likeImage.setEnabled(true);
                                Log.d("ugasio", "njegov nije, MOZE lajkat");
                            //}
                        } else {
                            holder.likeImage.setImageDrawable(getResources().getDrawable(R.drawable.already_liked));
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });

                holder.commentImage.setImageDrawable(getResources().getDrawable(R.drawable.already_commented));
                // mijenja ikonu za Comment ako je korisnik već komentirao ili tek komentirao stan
                mDatabase.child("Komentari").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot ds : dataSnapshot.getChildren()) {
                            if (Objects.requireNonNull(ds.child("stanID").getValue(String.class)).equals(stan.getStanUID()) &&
                                    Objects.requireNonNull(ds.child("userID").getValue(String.class)).equals(mAuth.getUid()) ) {       // dohvati samo komentare za detalje stana kojeg korisnik trenutno pregledava
                                holder.commentImage.setImageDrawable(getResources().getDrawable(R.drawable.comment));
                                break;
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });

                // ImageButton za lajkanje/dislajkanje Stana
                holder.likeImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!stan.getVlasnik().equals(mAuth.getUid())) {
                            spremljenaPozicija = holder.getAdapterPosition();
                            zapisiLajkove(mAuth.getUid(), stan.getStanUID(), stan.getVlasnik());
                            Log.d("lajk", mAuth.getUid() + "->" + stan.getStanUID());
                        } else {
                            Snackbar.make(Objects.requireNonNull(getActivity()).findViewById(android.R.id.content), "Ne možete lajkati vlastiti stan!", Snackbar.LENGTH_SHORT)
                                    .setActionTextColor(Color.RED)
                                    .show();
                        }
                    }
                });

                // ImageButton za komentiranje Stana
                holder.commentImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!stan.getVlasnik().equals(mAuth.getUid())) {
                            spremljenaPozicija = holder.getAdapterPosition();
                            dialogKomentar = new LovelyCustomDialog(getContext())
                                    .setView(R.layout.dialog_komentar)
                                    .setTopColorRes(R.color.GlavnaBoja)
                                    .setTitle("Ostavite komentar")
                                    .setMessage("Ostavite komentar na ovaj stan.")
                                    .setIcon(R.drawable.filter)
                                    .show();
                            EditText komentarEditText = dialogKomentar.findViewById(R.id.komentar);
                            Button spremiKomentar = dialogKomentar.findViewById(R.id.spremiKomentar);
                            spremiKomentar.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (!komentarEditText.getText().toString().equals("")) {
                                        zapisiKomentare(mAuth.getUid(), stan.getStanUID(), komentarEditText.getText().toString(), stan.getVlasnik());      // komentar iz Dialoga
                                        Log.d("lajk", mAuth.getUid() + "->" + stan.getStanUID() + " " + komentarEditText.getText());
                                        dialogKomentar.dismiss();
                                    } else {
                                        Toast.makeText(getContext(), "Unesite tekst komentara!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            Snackbar.make(Objects.requireNonNull(getActivity()).findViewById(android.R.id.content), "Ne možete komentirati vlastiti stan!", Snackbar.LENGTH_SHORT)
                                    .setActionTextColor(Color.RED)
                                    .show();
                        }
                    }
                });


                listaUIDstanova.add(stan.getStanUID());
                // Defining the comparator
                Comparator compare = Collections.reverseOrder();

// Sorting the list taking into account the comparator
                Collections.sort(listaUIDstanova, compare);
                //listaLatitude.add(stan.getLatitude());
                //listaLongitude.add(stan.getLongitude());

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
                        Intent detaljiStana = new Intent(getContext(), DetaljiStanova.class);
                        detaljiStana.putExtras(podaci);
                        startActivity(detaljiStana, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
                    }
                });
            }
            @Override
            public void onDataChanged() {       // poziva se kada FirebaseAdapter zavrsi ucitavati podatke
                //if (postavkeFAB != null) {
                    postavkeFAB.setEnabled(true);

                    //calculateDistanceOnline();
                //}
                //if (progressDialog != null) {
                   progressDialog.setVisibility(View.GONE);
                //}


                  mRecyclerView.scrollToPosition(spremljenaPozicija);   // skrola na kliknutu poziciju stana
                  linearLayoutManager.scrollToPosition(spremljenaPozicija);

                //linearLayoutManager.scrollToPositionWithOffset(spremljenaPozicija, (int) getResources().getDimension(R.dimen.offset_recyclerview));
                //if (linearLayoutManager != null) {

                //}
                FragmentMapaStanova fragmentMapaStanova = new FragmentMapaStanova();
                Bundle podaci = new Bundle();
                podaci.putStringArrayList("LISTASTANUID", listaUIDstanova);
                podaci.putStringArrayList("LISTALATITUDE", listaLatitude);
                podaci.putStringArrayList("LISTALONGITUDE", listaLongitude);
                fragmentMapaStanova.setArguments(podaci);

                mRecyclerView.setAdapter(FirebaseAdapter);
                //FirebaseAdapter.notifyDataSetChanged();
                if (FirebaseAdapter.getItemCount() == 0) {
                    Snackbar.make(Objects.requireNonNull(getActivity()).findViewById(android.R.id.content), "Nema stanova koji odgovaraju uvjetima.", Snackbar.LENGTH_SHORT)
                            .setActionTextColor(Color.RED)
                            .show();
                }

                Log.d("cijena", "osvjezio!");
            }
            @Override
            public Stanovi getItem(int position) {              //  ZA SORTIRANJE  --  PADAJUĆE ILI RASTUĆE SORITRANJE U FIREBASE DATABSE !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                if (odabranoSortiranje.equals("padajuce")) {
                    return super.getItem(getItemCount() - 1 - position);            /******    PADAJUĆE SORTIRANJE    ******/
                } else {
                    return super.getItem(position);                                 /******     RASTUĆE SORITRANJE    ******/
                }
            }
        };
        FirebaseAdapter.startListening();
    }



    /* Calculate Road Distance when user is Online */
    public void izracunajUdaljenostITrajanje() {

        final int[] brojZahtjeva = {0};
        final int ukupnoZahtjeva = listaLatitude.size();
        double startLatitude = 0, startLongitude = 0, endLatitude = 0, endLongitude = 0;

        if (checkboxLokacijeAktiviran.equals("true") && !odabraniLatitude.equals("0.0") && !odabraniLongitude.equals("0.0")) {      // ako je korisnik odabrao lokaciju za sortiranje, učitaj nju - inače uzmi trenutnu lokaciju
            startLatitude = Double.valueOf(odabraniLatitude);
            startLongitude = Double.valueOf(odabraniLongitude);
            Log.d("pocetni", " odabran je :  "  + startLatitude + "," + startLongitude);
        } else {
            startLatitude = location.getLatitude();
            startLongitude = location.getLongitude();
            Log.d("pocetni", " dobiven je :  "  + startLatitude + "," + startLongitude);
        }



        for (int i=0; i < listaLatitude.size(); i++) {

            endLatitude = Double.parseDouble(listaLatitude.get(i));
            endLongitude = Double.parseDouble(listaLongitude.get(i));
            Log.d("brojZahtjeva", startLatitude + ":" + startLongitude + "  " + endLatitude + ":" + endLongitude);

            // Instantiate the RequestQueue.
            RequestQueue requestQueue = Volley.newRequestQueue(Objects.requireNonNull(getContext()));
            String url = "https://maps.googleapis.com/maps/api/distancematrix/json?units=metric&origins=" + startLatitude + "," + startLongitude + "&destinations=" +
                    endLatitude + "," + endLongitude + "&key=" + API_KEY;

            Log.d("brojZahtjeva", url);

            int finalI = i;
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url, response -> {
                try {
                    JSONObject reponseObject = new JSONObject(response);
                    JSONArray jsonObject1 = (JSONArray) reponseObject.get("rows");
                    JSONObject jsonObject2 = (JSONObject) jsonObject1.get(0);
                    JSONArray jsonObject3 = (JSONArray) jsonObject2.get("elements");
                    JSONObject elementObj = (JSONObject) jsonObject3.get(0);
                    JSONObject distanceObj = (JSONObject) elementObj.get("distance");        // Calculate Road Distance when user is Online
                    JSONObject durationObj = (JSONObject) elementObj.get("duration");
                    String distance1 = distanceObj.getString("text");
                    String duration = durationObj.getString("text");
                    Float distanceOnly = Float.valueOf(distance1.replaceAll("[^0-9\\.]",""));
                    onlineDistanceArray.add(String.valueOf(distanceOnly) + " km");
                    onlineDurationArray.add(duration);

                    mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            int pozicijaDestinations = url.lastIndexOf("&destinations=");           // potrebno je prvo pronaći ispravnu poziciju stana za koji je potrebno upisati udaljenosti i trajanje, jer Responses dolaze u random redoslijedu
                            int pozicijaKey = url.lastIndexOf("&key=");
                            String latitudeAndLongitude = url.substring(pozicijaDestinations + 14, pozicijaKey).trim();
                            String samoLatitude = latitudeAndLongitude.substring(0, latitudeAndLongitude.indexOf(","));
                            String samoLongitude = latitudeAndLongitude.substring(latitudeAndLongitude.indexOf(",")+1, latitudeAndLongitude.length());
                            Log.d("dobarsi", url + "   " + pozicijaDestinations + " "  + pozicijaKey + "  " + latitudeAndLongitude + "  " + samoLatitude + ":" + samoLongitude);
                            int ispravnaPozicijaUdaljenostTrajanje = 0;
                            for (int j=0; j < listaLatitude.size(); j++) {
                                if (listaLatitude.get(j).equals(samoLatitude) && listaLongitude.get(j).equals(samoLongitude)) {
                                    ispravnaPozicijaUdaljenostTrajanje = j;     // ispravna pozicija stana za koji je pronađena udaljenost i trajanje
                                    break;
                                }
                            }

                            Map<String, Object> trenutnaUdaljenost = new HashMap<>();
                            String tempUdaljenost = onlineDistanceArray.get(ispravnaPozicijaUdaljenostTrajanje).replace(" km", "").trim();
                            String tempTrajanje = onlineDurationArray.get(ispravnaPozicijaUdaljenostTrajanje);
                            Double tempDoubleUdaljenost = Double.parseDouble(tempUdaljenost);
                            //long tempInteger = (int) Math.round(tempDouble);
                            long longUdaljenost = (long) Double.parseDouble(tempUdaljenost);
                            //Long longTrajanje = (long) Double.parseDouble(tempTrajanje);
                            trenutnaUdaljenost.put("udaljenost", tempDoubleUdaljenost);
                            trenutnaUdaljenost.put("trajanje", tempTrajanje);

                            Log.d("dobarsi", tempDoubleUdaljenost + "  " +  longUdaljenost);

                            if (mAuth != null) {
                                if (mAuth.getCurrentUser() != null) {
                                    mDatabase.child("Stanovi").child(tempListaUIDstanova.get(ispravnaPozicijaUdaljenostTrajanje)).child("udaljenosti").
                                            child(mAuth.getCurrentUser().getUid()).updateChildren(trenutnaUdaljenost);      // ažuriraj 'Stanovi' node u bazi s novodobivenom lokacijom za trenutnog korisnika
                                }
                            }

                            //Toast.makeText(getContext(), trenutnaUdaljenost.toString(), Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    //Stanovi stanDistanceDuration = beachesArray.get(finalI);
                    //stanDistanceDuration.setDistance(distanceOnly);
                    //stanDistanceDuration.setDuration(duration);
                    brojZahtjeva[0]++;
                    if (brojZahtjeva[0] == ukupnoZahtjeva) {      // sve Road Distances su pronađene Online
                        dohvatiPodatke(query);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                }
            });

            // Set the tag on the request.
            stringRequest.setTag(TAG);
            // Add the request to the RequestQueue.
            requestQueue.add(stringRequest);

        }

    }


    // zapisuje Lajkove/Dislajkove u Firebase Database
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
                                Toast.makeText(getContext(), "Sviđa Vam se!", Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(getContext(), "Ne sviđa Vam se!", Toast.LENGTH_SHORT).show();
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


    // zapisuje Komentare u Firebase Database
    private void zapisiKomentare(String userID, String stanID, String komentar, String vlasnik) {
            mDatabase.child("Komentari").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    //if (!dataSnapshot.exists()) {
                        mDatabase.child("Stanovi").child(stanID).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                String trenutniBrojLKomentara = Objects.requireNonNull(dataSnapshot.child("brojKomentara").getValue()).toString();
                                Log.d("svidanja", trenutniBrojLKomentara);
                                Integer povecaniBrojKomentara = Integer.valueOf(trenutniBrojLKomentara) + 1;

                                Map<String, Object> dodajKomentareUserIDStanID = new HashMap<>();
                                dodajKomentareUserIDStanID.put("userID", userID);
                                dodajKomentareUserIDStanID.put("stanID", stanID);
                                dodajKomentareUserIDStanID.put("brojKomentara", Double.valueOf(povecaniBrojKomentara));
                                dodajKomentareUserIDStanID.put("brojLajkovaKomentara", 0);
                                dodajKomentareUserIDStanID.put("komentar", komentar);
                                dodajKomentareUserIDStanID.put("imeKorisnika", Objects.requireNonNull(mAuth.getCurrentUser()).getDisplayName());
                                Calendar kalendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Zagreb"));
                                String datumDodavanja = DateFormat.format("dd.MM.yyyy HH:mm", kalendar).toString();
                                dodajKomentareUserIDStanID.put("datum", datumDodavanja);
                                String komentarID = mDatabase.child("Komentari").push().getKey();                // prvo dobij ID novog komentara kako bi ga mogao zapisati u node 'Komentari'
                                dodajKomentareUserIDStanID.put("komentarID", komentarID);
                                if (komentarID != null) {
                                    mDatabase.child("Komentari").child(komentarID).setValue(dodajKomentareUserIDStanID);      // zapisi u 'Komentari' node u bazi koji je user komentirao koji stan i broj komentara
                                }

                                Map<String, Object> dodajKomentarZaStanID = new HashMap<>();
                                dodajKomentarZaStanID.put("brojKomentara", Double.valueOf(povecaniBrojKomentara));
                                mDatabase.child("Stanovi").child(stanID).updateChildren(dodajKomentarZaStanID);      // ažuriraj 'Stanovi' node u bazi kada korisnik lajka stan
                                Toast.makeText(getContext(), "Komentar spremljen!", Toast.LENGTH_SHORT).show();
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }

            });

    }


    public void getLocation(Context context, Boolean requireFineGranularity, Boolean passiveMode, Long updateIntervalInMilliseconds, Boolean requireNewLocation) {
        location = new SimpleLocation(context, requireFineGranularity, passiveMode, updateIntervalInMilliseconds, requireNewLocation);
        location.setListener(new SimpleLocation.Listener() {

            public void onPositionChanged() {
                // new location data has been received and can be accessed
                //location = new SimpleLocation(context, requireFineGranularity, passiveMode, updateIntervalInMilliseconds, requireNewLocation);
            }

        });
        Log.d("poz", location.getLatitude() + ":" + location.getLongitude());
        // if we can't access the location yet
        if (!location.hasLocationEnabled()) {
            // ask the user to enable location access
            SimpleLocation.openSettings(context);
        }

        // Location changed listener
        location.setListener(new SimpleLocation.Listener() {
            public void onPositionChanged() {
                Log.d("poz2", location.getLatitude() + ":" + location.getLongitude());
            }

        });

        final double latitude = location.getLatitude();
        final double longitude = location.getLongitude();

        Log.d("poz", latitude + ":" + longitude);


    }


    public void pripremiPodatkeZaSpinnerNizeCijene(String odabranaNizaCijena, String odabranaVisaVrijednost) {
        listaViseCijene.clear();
        listaViseCijene.add("Bilo koja");
        if (odabranaNizaCijena.equals("Bilo koja")) {
            for (Integer i=1000; i<=10000; i+=500) {
                listaViseCijene.add(String.valueOf(i));
            }
        } else {
            for (Integer i=Integer.valueOf(odabranaNizaCijena)+500; i<=10000; i+=500) {
                listaViseCijene.add(String.valueOf(i));
            }
        }
        //visaCijenaSpinner.attachDataSource(listaViseCijene);
        Integer pozicijaOdabiraVisaCijena = listaViseCijene.indexOf(odabranaVisaVrijednost);
        //visaCijenaSpinner.setSelectedIndex(pozicijaOdabiraVisaCijena);
    }



    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 9876: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("dopustenje", "granted");
                    permissionGranted = true;
                    Context context = getContext();
                    boolean requireFineGranularity = true;
                    boolean passiveMode = false;
                    long updateIntervalInMilliseconds = 10 * 60 * 1000;
                    boolean requireNewLocation = false;
                    getLocation(context, requireFineGranularity, passiveMode, updateIntervalInMilliseconds, requireNewLocation);
                    Log.d("pocetni", "tu sam!");
                    Intent ponovnoPokreni = new Intent(getContext(), ListaStanova.class);
                    startActivity(ponovnoPokreni);
                } else {
                    ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()), new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, 1234);
                    if (ActivityCompat.shouldShowRequestPermissionRationale(Objects.requireNonNull(getActivity()), Manifest.permission.ACCESS_FINE_LOCATION)) {
                        Log.d("pocetni", "drugi slucaj");
                        showMessageOKCancel("Potrebna su lokacijska dopuštenja za korištenje ove aplikacije!", new DialogInterface.OnClickListener() {      // permission denied, but not permanent
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1234);
                            }
                        });
                        return;
                    } else {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1234);
                        Log.d("pocetni", "treci slucaj");
                        showMessageOKCancel("Potrebna su lokacijska dopuštenja za korištenje ove aplikacije!", new DialogInterface.OnClickListener() {      // permission denied, permanent!
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent settings = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", getActivity().getPackageName(), null));
                                startActivityForResult(settings, 1);
                            }
                        });
                        return;
                    }
                }
                return;
            }
            case 1987: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Log.d("dopustenje", "granted");
                    permissionGranted = true;
                    Context context = getContext();
                    boolean requireFineGranularity = true;
                    boolean passiveMode = false;
                    long updateIntervalInMilliseconds = 10 * 60 * 1000;
                    boolean requireNewLocation = false;
                    getLocation(context, requireFineGranularity, passiveMode, updateIntervalInMilliseconds, requireNewLocation);
                    Log.d("pocetni", "tu sam!");
                    Intent ponovnoPokreni = new Intent(getContext(), ListaStanova.class);
                    startActivity(ponovnoPokreni);
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }


    // za provjeru je li korisnik omogucio Lokaciju u postavkama ili nije
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1234) {
            if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getContext()), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(getContext(), "Uključite lokaciju u postavkama!", Toast.LENGTH_SHORT).show();

                // show a dialog
                AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
                builder1.setMessage("Lokacija nije uključena");
                builder1.setCancelable(true);

                builder1.setPositiveButton(
                        "Morate uključiti lokaciju!",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + Objects.requireNonNull(getActivity()).getPackageName()));
                                myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
                                myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivityForResult(myAppSettings, 1234);
                            }
                        });

                AlertDialog alert11 = builder1.create();
                alert11.show();

            } else {
                Intent ponovnoPokreni = new Intent(getContext(), ListaStanova.class);
                startActivity(ponovnoPokreni);
            }
        }
    }


    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(Objects.requireNonNull(getActivity()))
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }



    @Override
    public void onStart() {
        progressDialog.setVisibility(View.VISIBLE);
        super.onStart();
        if (FirebaseAdapter != null) {
            FirebaseAdapter.startListening();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        // restore RecyclerView state
        if (mBundleRecyclerViewState != null) {
            //Parcelable listState = mBundleRecyclerViewState.getParcelable(KEY_RECYCLER_STATE);
            //mRecyclerView.getLayoutManager().onRestoreInstanceState(listState);
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        // save RecyclerView state
        mBundleRecyclerViewState = new Bundle();
        Parcelable listState = mRecyclerView.getLayoutManager().onSaveInstanceState();
        mBundleRecyclerViewState.putParcelable(KEY_RECYCLER_STATE, listState);
    }

    @Override
    public void onStop() {
        super.onStop();
        //recyclerViewState = mRecyclerView.getLayoutManager().onSaveInstanceState();
        if (FirebaseAdapter != null) {
            FirebaseAdapter.stopListening();
        }
    }
}