package com.example.johndoe.najamstanova;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.robertlevonyan.views.customfloatingactionbutton.FloatingActionLayout;

import java.security.Permission;
import java.security.Permissions;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import im.delight.android.location.SimpleLocation;


public class ListaStanova extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private static final int FROM_GALLERY = 1;
    RecyclerView mRecyclerView;
    ImageView slika;
    ProgressBar progressDialog;
    FirebaseRecyclerAdapter<Stanovi, StanoviHolder> FirebaseAdapter;
    private String key;
    private static final FirebaseDatabase DATABASE = FirebaseDatabase.getInstance();
    private static final FirebaseStorage STORAGE = FirebaseStorage.getInstance();
    FloatingActionLayout mapFAB;
    int spremljenaPozicija = 0;
    ArrayList<String> listaUIDstanova = new ArrayList<>();
    ArrayList<String> listaLatitude = new ArrayList<>();
    ArrayList<String> listaLongitude = new ArrayList<>();
    PlaceAutocompleteFragment mjesta;
    private FirebaseAuth mAuth;
    private SimpleLocation location;



    /*
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.glavni_lista_mapa_stanova);

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        myToolbar.setBackgroundColor(getResources().getColor(R.color.GlavnaBoja));
        setSupportActionBar(myToolbar);

        ViewPager viewPager = findViewById(R.id.pager);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        // Add Fragments to adapter one by one
        adapter.addFragment(new FragmentListaStanova(), "FRAG1");
        adapter.addFragment(new FragmentMapaStanova(), "FRAG2");
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        progressDialog = findViewById(R.id.spin_kit);
        progressDialog.bringToFront();
        progressDialog.setVisibility(View.VISIBLE);

        // iz prikaza Liste Stanova u prikaz Mape Stanova
        mapFAB = findViewById(R.id.mapFAB);
        mapFAB.setEnabled(false);
        mapFAB.setOnClickListener(v -> {
            Bundle podaci = new Bundle();
            podaci.putStringArrayList("LISTASTANUID", listaUIDstanova);
            podaci.putStringArrayList("LISTALATITUDE", listaLatitude);
            podaci.putStringArrayList("LISTALONGITUDE", listaLongitude);
            Intent listaStanovaNaMapi = new Intent(ListaStanova.this, ListaStanovaMapa.class);
            listaStanovaNaMapi.putExtras(podaci);
            startActivity(listaStanovaNaMapi, ActivityOptions.makeSceneTransitionAnimation(ListaStanova.this).toBundle());
        });

        // Material Drawer
        PrimaryDrawerItem item1 = new PrimaryDrawerItem().withIdentifier(1).withName("Dodaj stan");
        SecondaryDrawerItem item2 = new SecondaryDrawerItem().withIdentifier(2).withName("Poruke");
        SecondaryDrawerItem item3 = new SecondaryDrawerItem().withIdentifier(3).withName("Moji stanovi");
        SecondaryDrawerItem item4 = new SecondaryDrawerItem().withIdentifier(4).withName("Omiljeno");

        Drawer ladica = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(myToolbar)
                .withDisplayBelowStatusBar(false)
                .withTranslucentStatusBar(false)
                .withActionBarDrawerToggle(true)
                .withActionBarDrawerToggleAnimated(true)
                .withCloseOnClick(true)
                .addDrawerItems(
                        new DividerDrawerItem(),
                        new DividerDrawerItem(),
                        item1,
                        new DividerDrawerItem(),
                        item2,
                        new DividerDrawerItem(),
                        item3,
                        new DividerDrawerItem(),
                        item4,
                        new DividerDrawerItem()
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        if (drawerItem.getIdentifier() == 1) {
                            Intent dodajStan = new Intent(ListaStanova.this, DodavanjeStanova.class);
                            startActivity(dodajStan);
                        } else if(drawerItem.getIdentifier() == 2) {
                            Intent chatUsersList = new Intent(ListaStanova.this, ChatUsersList.class);
                            startActivity(chatUsersList);
                        } else if(drawerItem.getIdentifier() == 3) {
                            Intent vlastitiStanovi = new Intent(ListaStanova.this, StanoviVlasnika.class);
                            startActivity(vlastitiStanovi);
                        } else if(drawerItem.getIdentifier() == 4) {

                        }
                        return false;
                    }
                })
                .build();

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mRecyclerView = findViewById(R.id.mRecView);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());

        mRecyclerView.setLayoutManager(linearLayoutManager);
        RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(getApplicationContext()) {
            @Override protected int getVerticalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_START;
            }
        };

        key = DATABASE.getReference().push().getKey();

        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("Stanovi")
                .limitToLast(50);

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
                holder.setCijena(stan.getCijena());
                holder.setSlika(stan.getGlavnaSlika(), getApplicationContext());       // učitavanje slike u Listi Stanova -- pomoću StanoviHolder

                listaUIDstanova.add(stan.getStanUID());
                listaLatitude.add(stan.getLatitude());
                Log.d("duzina", stan.getLatitude());
                listaLongitude.add(stan.getLongitude());

                // kada korisnik odabere neku od kartica iz Liste Stanova
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        spremljenaPozicija = holder.getAdapterPosition();

                        Bundle podaci = new Bundle();
                        podaci.putString("UIDSTAN", stan.getStanUID());
                        podaci.putString("UIDVLASNIK", stan.getVlasnik());
                        podaci.putString("SVESLIKE", stan.getSlike());
                        Intent detaljiStana = new Intent(ListaStanova.this, DetaljiStanova.class);
                        detaljiStana.putExtras(podaci);
                        startActivity(detaljiStana, ActivityOptions.makeSceneTransitionAnimation(ListaStanova.this).toBundle());

                    }
                });
            }
            @Override
            public void onDataChanged() {       // poziva se kada FirebaseAdapter zavrsi ucitavati podatke
                mapFAB.setEnabled(true);
                progressDialog.setVisibility(View.GONE);
                //mRecyclerView.scrollToPosition(spremljenaPozicija);   // skrola na kliknutu poziciju stana
                linearLayoutManager.scrollToPositionWithOffset(spremljenaPozicija, (int) getResources().getDimension(R.dimen.offset_recyclerview));
            }

        };
        mRecyclerView.setAdapter(FirebaseAdapter);
    }


    @Override
    protected void onStart() {
        progressDialog.setVisibility(View.VISIBLE);
        super.onStart();
        FirebaseAdapter.startListening();
    }

    @Override
    public void onResume(){
        super.onResume();
        //progressDialog.setVisibility(View.VISIBLE);
        // restore RecyclerView state
        //if (recyclerViewState != null) {
        //   mRecyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
        //}
    }

    @Override
    protected void onStop() {
        super.onStop();
        //recyclerViewState = mRecyclerView.getLayoutManager().onSaveInstanceState();
        FirebaseAdapter.stopListening();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            Uri odabranaSlika = data.getData();
            Glide.with(this).load(odabranaSlika).into(slika);
            StorageReference ref = STORAGE.getReference().child(key);
            ref.putFile(odabranaSlika).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.d("TAG", "Slika uploadana!");
                }
            });
        }
    }


    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);

        // Save list state
        //recyclerViewState = mRecyclerView.getLayoutManager().onSaveInstanceState();
        //mBundleRecyclerViewState.putParcelable(KEY_RECYCLER_STATE, recyclerViewState);
    }
}
*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.glavni_lista_mapa_stanova);

        if (!postojiMreza()) {
            DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        Intent ponovnoPokreni = new Intent(ListaStanova.this, ListaStanova.class);
                        startActivity(ponovnoPokreni);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        finishAffinity();
                        System.exit(0);
                        break;
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(ListaStanova.this, android.R.style.Theme_Material_Light_Dialog_Alert);
            builder.setMessage("Provjerite povezanost na internet.").setPositiveButton("Internet je uključen!", dialogClickListener)
                    .setNegativeButton("Izađi iz aplikacije", dialogClickListener).show();
        }


        if (ActivityCompat.checkSelfPermission(ListaStanova.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //ActivityCompat.requestPermissions(ListaStanova.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 123);
            //return;
        } else {
            //Intent ponovnoPokreni = new Intent(ListaStanova.this, ListaStanova.class);
            //startActivity(ponovnoPokreni);
        }

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Korisnici");

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        myToolbar.setBackgroundColor(getResources().getColor(R.color.GlavnaBoja));
        myToolbar.setTitle("Najam stanova");
        setSupportActionBar(myToolbar);


        resetirajOdabirSlika();


        // Material Drawer
        PrimaryDrawerItem item1 = new PrimaryDrawerItem().withIdentifier(1).withName("Dodaj stan");
        SecondaryDrawerItem item2 = new SecondaryDrawerItem().withIdentifier(2).withName("Poruke");
        SecondaryDrawerItem item3 = new SecondaryDrawerItem().withIdentifier(3).withName("Moji stanovi");
        SecondaryDrawerItem item4 = new SecondaryDrawerItem().withIdentifier(4).withName("Sviđa mi se");
        SecondaryDrawerItem item5 = new SecondaryDrawerItem().withIdentifier(5).withName("Odjava");

        Drawer ladica = new DrawerBuilder()
                .withActivity(ListaStanova.this)
                .withToolbar(myToolbar)
                .withDisplayBelowStatusBar(false)
                .withTranslucentStatusBar(false)
                .withActionBarDrawerToggle(true)
                .withActionBarDrawerToggleAnimated(true)
                .withCloseOnClick(true)
                .addDrawerItems(
                        new DividerDrawerItem(),
                        new DividerDrawerItem(),
                        item1,
                        new DividerDrawerItem(),
                        item2,
                        new DividerDrawerItem(),
                        item3,
                        new DividerDrawerItem(),
                        item4,
                        new DividerDrawerItem(),
                        item5,
                        new DividerDrawerItem()
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        if (drawerItem.getIdentifier() == 1) {
                            Intent dodajStan = new Intent(ListaStanova.this, DodavanjeStanova.class);
                            startActivity(dodajStan);
                        } else if(drawerItem.getIdentifier() == 2) {
                            Intent chatUsersList = new Intent(ListaStanova.this, ChatUsersList.class);
                            startActivity(chatUsersList);
                        } else if(drawerItem.getIdentifier() == 3) {
                            Intent vlastitiStanovi = new Intent(ListaStanova.this, StanoviVlasnika.class);
                            startActivity(vlastitiStanovi);
                        } else if(drawerItem.getIdentifier() == 4) {
                                Intent vlastitiStanovi = new Intent(ListaStanova.this, StanoviLiked.class);
                                startActivity(vlastitiStanovi);
                        } else if(drawerItem.getIdentifier() == 5) {        // ODJAVA KORISNIKA
                            mDatabase.child("Korisnici").child(Objects.requireNonNull(mAuth.getUid())).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    dataSnapshot.getRef().removeValue();
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    // postavi Display Name na null kada se korisnik odjavljuje iz aplikacije
                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(null).build();
                                    if (user != null) {
                                        user.updateProfile(profileUpdates);     // postavi DisplayName za korisnika
                                    }
                                    mAuth.signOut();
                                    Toast.makeText(getApplicationContext(), "Odjavljeni ste!", Toast.LENGTH_SHORT).show();
                                    Intent pocetniEkran = new Intent(ListaStanova.this, MainActivity.class);
                                    startActivity(pocetniEkran);
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                        return false;
                    }
                })
                .build();
                //.buildForFragment();      // -- ako je NavigationDrawer u Fragmentu

        ViewPager viewPager = findViewById(R.id.pager);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        // dodavanja Fragmenata u Adapter, jedan po jedan
        adapter.addFragment(new FragmentListaStanova(), "Lista stanova");
        adapter.addFragment(new FragmentMapaStanova(), "Mapa stanova");
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 1) {
                    // postavlja Search za adresu i grad u Toolbar
                    if ((getSupportActionBar() != null)) {
                        mjesta = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
                        Objects.requireNonNull(mjesta.getView()).setVisibility(View.VISIBLE);
                    }
                } else {
                    mjesta = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
                    Objects.requireNonNull(mjesta.getView()).setVisibility(View.GONE);
                }
                /*
                Log.d("postavio", listaLatitude.get(0));
                FragmentMapaStanova fragmentMapaStanova = new FragmentMapaStanova();
                Bundle podaci = new Bundle();
                podaci.putStringArrayList("LISTASTANUID", listaUIDstanova);
                podaci.putStringArrayList("LISTALATITUDE", listaLatitude);
                podaci.putStringArrayList("LISTALONGITUDE", listaLongitude);
                fragmentMapaStanova.setArguments(podaci);
                */
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {

            case 123: // Allowed was selected so Permission granted
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Snackbar s = Snackbar.make(findViewById(android.R.id.content),"Lokacije dopuštene!",Snackbar.LENGTH_LONG);
                    View snackbarView = s.getView();
                    TextView textView = snackbarView.findViewById(android.support.design.R.id.snackbar_text);
                    textView.setTextColor(Color.WHITE);
                    textView.setTextSize(18);
                    textView.setMaxLines(6);
                    s.show();

                    Intent ponovnoPokreni = new Intent(ListaStanova.this, ListaStanova.class);
                    startActivity(ponovnoPokreni);

                    // do your work here

                } else if (Build.VERSION.SDK_INT >= 23 && !shouldShowRequestPermissionRationale(permissions[0])) {
                    // User selected the Never Ask Again Option Change settings in app settings manually
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ListaStanova.this, android.R.style.Theme_Material_Light_Dialog_Alert);
                    alertDialogBuilder.setTitle("Promijenite dopuštenje u postavkama");
                    alertDialogBuilder
                            .setMessage("Potrebna su lokacijska dopuštenja, otvorite postavke!")
                            .setCancelable(false)
                            .setPositiveButton("Otvori postavke", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                                    intent.setData(uri);
                                    startActivityForResult(intent, 1000);
                                }
                            });

                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();

                } else {
                    // User selected Deny Dialog to EXIT App ==> OR <== RETRY to have a second chance to Allow Permissions
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {

                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ListaStanova.this, android.R.style.Theme_Material_Light_Dialog_Alert);
                        alertDialogBuilder.setTitle("Pokušajte ponovno!");
                        alertDialogBuilder
                                .setMessage("Potrebna su lokacijska dopuštenja, pokušajte ponovno!")
                                .setCancelable(false)
                                .setPositiveButton("Pokušajte ponovno", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        //ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, Integer.parseInt(WRITE_EXTERNAL_STORAGE));
                                        Intent i = new Intent(ListaStanova.this, ListaStanova.class);
                                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(i);
                                    }
                                });
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    }
                }
                break;
        }};


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1000) {
            if (ActivityCompat.checkSelfPermission(ListaStanova.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(ListaStanova.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 123);
            } else {
                Intent ponovnoPokreni = new Intent(ListaStanova.this, ListaStanova.class);
                startActivity(ponovnoPokreni);
            }
        }
    }


    // izbriši sve odabrane slike u OdabirSlike kada se aplikacija pokrene prvi puta
    private void resetirajOdabirSlika(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ListaStanova.this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("LISTASVESLIKEVELICINA", 0);
        editor.remove("SPREMLJENILATITUDE");
        editor.remove("SPREMLJENILONGITUDE");
        editor.remove("SPREMLJENAULICA");
        editor.putString("SPREMLJENILATITUDE", null);
        editor.putString("SPREMLJENILONGITUDE", null);
        editor.putString("SPREMLJENAULICA", null);
        editor.apply();
        editor.putInt("LISTASVESLIKEVELICINA", 0);
        int velicina = preferences.getInt("LISTASVESLIKEVELICINA", 0);
        for(int i=0; i<velicina; i++) {
            editor.remove("LISTASVESLIKE_" + i);
            editor.putString("LISTASVESLIKE_" + i, null);
            editor.apply();
            Log.d("pobrisao", "jesam!");
        }
    }


    public boolean postojiMreza(){
        ConnectivityManager cm = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        assert cm != null;
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }


    // Adapter for the viewpager using FragmentPagerAdapter
    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

}
