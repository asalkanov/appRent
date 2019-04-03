package com.example.johndoe.najamstanova;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class ListaStanovaMapa extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Bundle podaci;
    ArrayList<String> listaUIDstanova = new ArrayList<>();
    ArrayList<String> listaLatitude = new ArrayList<>();
    ArrayList<String> listaLongitude = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_stanova_mapa);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        Intent intentExtras = getIntent();
        podaci = intentExtras.getExtras();

        if (podaci != null && !podaci.isEmpty()) {
            listaUIDstanova = podaci.getStringArrayList("LISTASTANUID");
            listaLatitude = podaci.getStringArrayList("LISTALATITUDE");
            listaLongitude = podaci.getStringArrayList("LISTALONGITUDE");
        }


    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        for(int i=0; i<listaLatitude.size(); i++) {
            Marker trenutniMarker = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_lista)).position(new LatLng(Double.parseDouble(listaLatitude.get(i)), Double.parseDouble(listaLongitude.get(i)))));
            trenutniMarker.setTitle(listaUIDstanova.get(i));
        }

        LatLng prviStan = new LatLng(Double.parseDouble(listaLatitude.get(0)), Double.parseDouble(listaLongitude.get(0)));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(prviStan, 15));

        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
}
