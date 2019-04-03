package com.example.johndoe.najamstanova;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;


import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    PlaceAutocompleteFragment mjesta;
    EditText pretrazite;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
    private boolean mLocationPermissionGranted;
    private Location mLastKnownLocation;
    private CameraPosition mCameraPosition;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private PlaceDetectionClient mPlaceDetectionClient;
    LatLng odabranaPozicija = null;
    Bundle podaci;
    Boolean izmjenaPodatakaStana = false;
    String stanUID, vlasnikUID, sveSlike, naziv, cijena, povrsina, latitude, longitude, brojSoba, kupaonica, tv, klima, rublje, hladnjak, posude, stednjak;
    String odabranaAdresa;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        this.setTitle("3.) Lokacija:");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapsActivity.this);

        Intent intentExtras = getIntent();
        podaci = intentExtras.getExtras();

        if (podaci != null && !podaci.isEmpty()) {
            izmjenaPodatakaStana = podaci.containsKey("IZMJENALOKACIJE") && Objects.requireNonNull(podaci.getString("IZMJENALOKACIJE")).equals("IZMJENASTANA");
            stanUID = podaci.getString("UIDSTAN");
            vlasnikUID = podaci.getString("UIDVLASNIK");
            sveSlike = podaci.getString("SVESLIKE");
            naziv = podaci.getString("NAZIV");
            cijena = podaci.getString("CIJENA");
            povrsina = podaci.getString("POVRSINA");
            latitude = podaci.getString("LATITUDE");
            longitude = podaci.getString("LONGITUDE");
            brojSoba = podaci.getString("BROJSOBA");
            kupaonica = podaci.getString("KUPAONICA");
            tv = podaci.getString("TV");
            klima = podaci.getString("KLIMA");
            rublje = podaci.getString("RUBLJE");
            hladnjak = podaci.getString("HLADNJAK");
            posude = podaci.getString("POSUDE");
            stednjak = podaci.getString("STEDNJAK");
        }

        mPlaceDetectionClient = Places.getPlaceDetectionClient(this);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // dobiva lokaciju i poziciju kamere iz spremljenog instance statea
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        // postavlja Search za adresu i grad u Toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setCustomView(R.layout.search_address);
            getSupportActionBar().setDisplayShowCustomEnabled(true);
        }

        // Za trazenje adrese u Toolbaru (samo HR i adrese ulica)
        mjesta = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        pretrazite = Objects.requireNonNull(mjesta.getView()).findViewById(R.id.place_autocomplete_search_input);
        ImageView pretraziteIkona = (ImageView)((LinearLayout) mjesta.getView()).getChildAt(0);
        pretrazite.setHintTextColor(getResources().getColor(R.color.white));
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
                dodajMarker(odabranaPozicija.latitude, odabranaPozicija.longitude, Objects.requireNonNull(place.getAddress()).toString());
            }
            @Override
            public void onError(Status status) {
                Toast.makeText(getApplicationContext(),status.toString(),Toast.LENGTH_SHORT).show();

            }
        });

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Pitaj korisnika za dozvole
        getLocationPermission();
        // Dobiva trenutnu lokaciju korisnika i prikazuje ju na karti
        getDeviceLocation();

        // vraća prethodno odabranu lokaciju u slučaju kada se korisnik vratio u prethodnu aktivnost
        vratiOdabranuLokaciju();

        // postavlja Marker na poziciju mape gdje je korisnik kliknuo
        mMap.setOnMapClickListener(latLng -> {
            String adresa = pretvoriLokacijuUAdresuUlice(latLng);
            dodajMarker(latLng.latitude, latLng.longitude, adresa);
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
                LatLng pozicija = new LatLng(marker.getPosition().latitude, marker.getPosition().longitude);
                String adresa = pretvoriLokacijuUAdresuUlice(pozicija);
                dodajMarker(marker.getPosition().latitude, marker.getPosition().longitude, adresa);
            }
        });

        // long click na mapu brise sve Markere
        mMap.setOnMapLongClickListener(latLng -> mMap.clear());
    }


    // dodaje marker na mapu
    public void dodajMarker(Double latitude, Double longitude, String adresa) {
        MarkerOptions markerOptions = new MarkerOptions();
        LatLng pozicija = new LatLng(latitude, longitude);
        markerOptions.position(pozicija);
        markerOptions.title(adresa);
        markerOptions.visible(true);
        markerOptions.draggable(true);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_lista));
        mMap.clear();
        Marker mMarker = mMap.addMarker(markerOptions);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pozicija, 15.0f));
        mjesta.setText(adresa);
        odabranaAdresa = adresa;
        odabranaPozicija = pozicija;    // ukoliko pozicija nije odabrana pomoću PlaceAutocompleteFragmenta, nego klikom na Mapu
        Log.d("stasad", pozicija.latitude + " " + pozicija.longitude + "   " + adresa);

    }


    // Dobivanje latitude i longitude iz adrese ulice
    public String pretvoriLokacijuUAdresuUlice(LatLng pozicijaKlika) {
        List<Address> addresses = null;
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        Address adresa;
        String pronadjenaAdresa = null;

        try {
            addresses = geocoder.getFromLocation(
                    pozicijaKlika.latitude,
                    pozicijaKlika.longitude,
                    1);     // vrati samo prvu adresu iz mjesta na mapi na koje je korisnik kliknuo
        } catch (IOException ioException) {
            Toast.makeText(getApplicationContext(), "Adresa nije pronađena. Označite drugo mjesto.", Toast.LENGTH_SHORT).show();
        }

        if (addresses == null || addresses.size() == 0) {   // ako adresa nije pronađena
            Toast.makeText(getApplicationContext(), "Adresa nije pronađena. Označite drugo mjesto.", Toast.LENGTH_SHORT).show();
        } else {
            adresa = addresses.get(0);
            pronadjenaAdresa = adresa.getAddressLine(0);    // vrati samo adresu iz svih ostalih podataka
            odabranaAdresa = String.valueOf(pronadjenaAdresa);
            //String street = pronadjenaAdresa.getThoroughfare(); ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        }

        return String.valueOf(pronadjenaAdresa);
    }


    // sprema stanje mape na onPause()
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }


    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            mMap.setMyLocationEnabled(true);    // button za trenutnu lokaciju
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
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
                locationResult.addOnCompleteListener(this, task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        // postavi kameru na trenutnu lokaciju korisnika
                        mLastKnownLocation = (Location) task.getResult();
                        if (mLastKnownLocation.getLatitude() != 0 && mLastKnownLocation.getLongitude() != 0) {
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), 15.0f));
                            MarkerOptions markerOptions = new MarkerOptions();
                            LatLng trenutnaLokacija = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
                            //mjesta.setText(pretvoriLokacijuUAdresuUlice(trenutnaLokacija));
                            markerOptions.position(trenutnaLokacija);
                            mMap.clear();
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(trenutnaLokacija, 15.0f));
                            //mMap.addMarker(markerOptions);
                        }
                    } else {
                        LatLng mDefaultLocation = new LatLng(45.838979, 15.979779);      // default lokacija grad Zagreb
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, 10.0f));
                        mMap.getUiSettings().setMyLocationButtonEnabled(false);
                    }
                });
            }
        } catch(SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }


    // kreira Toolbar s Buttonom za nastavak na sljedeci Activity (MapsActivity) prilikom dodavanja novog Stana
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_next, menu);
        return true;
    }

    // Toolbar button Next - za nastavak na sljedeci Activity
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.next_activity:
                EditText pretrazite = mjesta.getView().findViewById(R.id.place_autocomplete_search_input);
                if (odabranaPozicija != null && !pretrazite.getText().equals("") && !izmjenaPodatakaStana) {
                    podaci.putString("LATITUDE", String.valueOf(odabranaPozicija.latitude));
                    podaci.putString("LONGITUDE", String.valueOf(odabranaPozicija.longitude));
                    Intent odaberiLokaciju = new Intent(MapsActivity.this, SazetakInformacija.class);
                    odaberiLokaciju.putExtras(podaci);
                    startActivity(odaberiLokaciju, ActivityOptions.makeSceneTransitionAnimation(MapsActivity.this).toBundle());
                } else if (odabranaPozicija != null && !pretrazite.getText().equals("") && izmjenaPodatakaStana) {                  // vrati se na IzmijenaStana activity
                    Bundle podaci = new Bundle();
                    podaci.putString("IZMJENALOKACIJE", "IZMJENASTANA");
                    podaci.putString("UIDSTAN", stanUID);
                    podaci.putString("UIDVLASNIK", vlasnikUID);
                    podaci.putString("SVESLIKE",  sveSlike);
                    podaci.putString("NAZIV", naziv);
                    podaci.putString("CIJENA", cijena);
                    podaci.putString("POVRSINA", povrsina);
                    podaci.putString("LATITUDE", latitude);
                    podaci.putString("LONGITUDE", longitude);
                    podaci.putString("ADRESA", odabranaAdresa);
                    podaci.putString("BROJSOBA", brojSoba);
                    podaci.putString("KUPAONICA", kupaonica);
                    podaci.putString("TV", tv);
                    podaci.putString("KLIMA", klima);
                    podaci.putString("RUBLJE", rublje);
                    podaci.putString("HLADNJAK", hladnjak);
                    podaci.putString("POSUDE", posude);
                    podaci.putString("STEDNJAK", stednjak);
                    podaci.putString("LATITUDE", String.valueOf(odabranaPozicija.latitude));
                    podaci.putString("LONGITUDE", String.valueOf(odabranaPozicija.longitude));
                    Intent izmjenaLokacije = new Intent(MapsActivity.this, IzmijeniStan.class);
                    izmjenaLokacije.putExtras(podaci);
                    startActivity(izmjenaLokacije);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onBackPressed() {
        spremiOdabranuLokaciju();
        super.onBackPressed();
    }

    private void spremiOdabranuLokaciju(){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove("SPREMLJENILATITUDE");
        editor.remove("SPREMLJENILONGITUDE");
        editor.remove("SPREMLJENAULICA");
        editor.putString("SPREMLJENILATITUDE", String.valueOf(odabranaPozicija.latitude));
        editor.putString("SPREMLJENILONGITUDE", String.valueOf(odabranaPozicija.longitude));
        editor.putString("SPREMLJENAULICA", pretrazite.getText().toString());
        editor.apply();
    }


    private void vratiOdabranuLokaciju() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MapsActivity.this);

        Double odabraniLatitude = null, odabraniLongitude = null;
        String odabranaUlica = null;

        String odabraniLatString = preferences.getString("SPREMLJENILATITUDE", null);
        String odabraniLonString = preferences.getString("SPREMLJENILONGITUDE", null);
        String odabranaUlicaString = preferences.getString("SPREMLJENAULICA", null);

        if (odabraniLatString != null && odabraniLonString != null && odabranaUlicaString != null) {
            odabraniLatitude = Double.valueOf(odabraniLatString);
            odabraniLongitude = Double.valueOf(odabraniLonString);
            odabranaUlica = odabranaUlicaString;
            //pretrazite.setText(odabranaUlica);
            dodajMarker(odabraniLatitude, odabraniLongitude, odabranaUlica);
        }

    }

            /*
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(odabraniLatitude, odabraniLongitude), 15.0f));
            MarkerOptions markerOptions = new MarkerOptions();
            LatLng trenutnaLokacija = new LatLng(odabraniLatitude, odabraniLatitude);
            //mjesta.setText(pretvoriLokacijuUAdresuUlice(trenutnaLokacija));
            markerOptions.position(trenutnaLokacija);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(trenutnaLokacija, 15.0f));
            */



}
