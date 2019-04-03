package com.example.johndoe.najamstanova;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class MyClusterItem implements ClusterItem {
    private final LatLng mPosition;
    private String mUIDstana = "";
    private String mTitle = "";
    private String mPovrsinaStana = "";
    private String mNazivStana = "";
    private String mSnippet = "";
    private String mCijena = "";
    private String mGlavnaSlika = "";
    private boolean shouldCluster = true;

    public MyClusterItem(double lat, double lng, String title, String cijena, String glavnaSlika, String uidStana, String povrsinaStana, String nazivStana) {
        mPosition = new LatLng(lat, lng);
        mTitle = title;
        mCijena = cijena;
        mGlavnaSlika = glavnaSlika;
        mUIDstana = uidStana;
        mPovrsinaStana = povrsinaStana;
        mNazivStana = nazivStana;
    }

    public MyClusterItem(double lat, double lng, String title, String snippet, String cijena, String glavnaSlika, String uidStana, String povrsinaStana, String nazivStana) {
        mPosition = new LatLng(lat, lng);
        mTitle = title;
        mSnippet = snippet;
        mCijena = cijena;
        mGlavnaSlika = glavnaSlika;
        mUIDstana = uidStana;
        mPovrsinaStana = povrsinaStana;
        mNazivStana = nazivStana;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    @Override
    public String getSnippet() {
        return mSnippet;
    }

    public String getUIDstana() {
        return mUIDstana;
    }

    public String getCijenaStana() {
        return mCijena;
    }

    public String getGlavnaSlika() {
        return mGlavnaSlika;
    }

    public String getPovrsinaStana() {
        return mPovrsinaStana;
    }

    public String getNazivStana() {
        return mNazivStana;
    }

    public void setMarkersToCluster(boolean toCluster)
    {
        this.shouldCluster = toCluster;
    }

}