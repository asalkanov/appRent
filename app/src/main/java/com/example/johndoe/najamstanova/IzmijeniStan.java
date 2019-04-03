package com.example.johndoe.najamstanova;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.veinhorn.scrollgalleryview.MediaInfo;
import com.veinhorn.scrollgalleryview.ScrollGalleryView;
import com.yarolegovich.discretescrollview.DiscreteScrollView;
import com.yarolegovich.discretescrollview.transform.Pivot;
import com.yarolegovich.discretescrollview.transform.ScaleTransformer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class IzmijeniStan extends AppCompatActivity {

    Bundle podaci;
    String vlasnik, stanUID, vlasnikUID, odabranaAdresa, sveSlike;
    EditText naziv, cijena, povrsina, prijenosStatus;
    CheckBox tvCB, klimaCB, rubljeCB, hladnjakCB, posudeCB, stednjakCB;
    String tv, klima, rublje, hladnjak, posude, stednjak;
    Spinner brojSoba, brojKupaonica;
    private ScrollGalleryView scrollGalleryView;
    Button izmijeniLokaciju, izmijeniSlike;
    TextView adresaTV;
    DiscreteScrollView discreteScrollView;
    DiscreteScrollAdapter discreteAdapter;
    ArrayList<String> listaSlika = new ArrayList<>();
    List<MediaInfo> infoSlike = new ArrayList<>();
    String latitude, longitude, soba, kupaonica;
    String odabraneNoveSlike = "false";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_izmijeni_stan);

        discreteScrollView = findViewById(R.id.discreteScroll);
        discreteAdapter = new DiscreteScrollAdapter(listaSlika);
        discreteScrollView.setItemTransformer(new ScaleTransformer.Builder()
                .setMaxScale(1.05f)
                .setMinScale(0.8f)
                .setPivotX(Pivot.X.CENTER) // CENTER is a default one
                .setPivotY(Pivot.Y.BOTTOM) // CENTER is a default one
                .build());

        // resetiraj sve kada se dodaju nove slike
        infoSlike.clear();

        scrollGalleryView = findViewById(R.id.scroll_gallery_view);
        scrollGalleryView
                .setThumbnailSize(100)
                .setZoom(true)
                .withHiddenThumbnails(true)
                .setFragmentManager(getSupportFragmentManager());

        povrsina = findViewById(R.id.povrsina);
        naziv = findViewById(R.id.naziv);
        cijena = findViewById(R.id.cijenaCL);
        brojSoba = findViewById(R.id.brojSoba);
        brojKupaonica = findViewById(R.id.brojKupaonica);
        tvCB = findViewById(R.id.tvCB);
        klimaCB = findViewById(R.id.klimaCB);
        rubljeCB = findViewById(R.id.rubljeCB);
        hladnjakCB = findViewById(R.id.hladnjakCB);
        posudeCB = findViewById(R.id.posudeCB);
        stednjakCB = findViewById(R.id.stednjakCB);

        adresaTV = findViewById(R.id.adresa);

        Intent intentExtras = getIntent();
        podaci = intentExtras.getExtras();

        // podaci dobiveni iz prethodnog Activitya -- Liste Stanova -- ili -- FragmentMapaStanova
        if (podaci != null && !podaci.isEmpty()) {
            stanUID = podaci.getString("UIDSTAN");
            vlasnikUID = podaci.getString("UIDVLASNIK");
            naziv.setText(podaci.getString("NAZIV"));
            listaSlika = podaci.getStringArrayList("LISTASLIKA");
            cijena.setText(podaci.getString("CIJENA"));
            povrsina.setText(podaci.getString("POVRSINA"));
            brojSoba.setSelection(dohvatiIndeks(brojSoba, podaci.getString("BROJSOBA")));
            brojKupaonica.setSelection(dohvatiIndeks(brojKupaonica, podaci.getString("KUPAONICA")));
            latitude = podaci.getString("LATITUDE");
            longitude = podaci.getString("LONGITUDE");
            soba = podaci.getString("BROJSOBA");
            kupaonica = podaci.getString("KUPAONICA");
            tv = podaci.getString("TV");
            klima = podaci.getString("KLIMA");
            rublje = podaci.getString("RUBLJE");
            hladnjak = podaci.getString("HLADNJAK");
            posude = podaci.getString("POSUDE");
            stednjak = podaci.getString("STEDNJAK");
            odabraneNoveSlike = podaci.getString("IZMJENANOVESLIKE", "false");

            if (odabraneNoveSlike.equals("true")) {
                listaSlika = podaci.getStringArrayList("LISTASLIKE");
                Log.d("dsajgkdf", " !!!!!!!! " + listaSlika.size() + "  ");
            } else {
                sveSlike = podaci.getString("SVESLIKE");
                listaSlika = new ArrayList<>();
                Log.d("dsajgkdf", "%%%%%%%%%%%%! " +  sveSlike);

                String nerazdvojeneSlike = podaci.getString("SVESLIKE");
                String[] tempSveSlike = new String[]{};
                if (nerazdvojeneSlike != null) {
                    Pattern p = Pattern.compile("#&~*%#", Pattern.LITERAL);
                    tempSveSlike = p.split(nerazdvojeneSlike);
                }
                for (int i = 0; i < tempSveSlike.length; i++) {
                    if (!tempSveSlike[i].equals("")) {
                        listaSlika.add(tempSveSlike[i]);
                    }
                }
            }

            infoSlike.clear();
            scrollGalleryView.clearGallery();

            if (listaSlika != null && listaSlika.size() > 0) {
                for (String uri : listaSlika) {
                    Log.d("sadasdgggg", uri);
                    infoSlike.add(MediaInfo.mediaLoader(new PicassoImageLoader(uri)));
                    scrollGalleryView.addMedia(MediaInfo.mediaLoader(new PicassoImageLoader(uri)));
                }
                discreteAdapter = new DiscreteScrollAdapter(listaSlika);
                discreteScrollView.setAdapter(discreteAdapter);
                discreteAdapter.notifyDataSetChanged();
            }

            if (podaci.containsKey("ADRESA")) {
                String tempAdresa = podaci.getString("ADRESA");
                Log.d("fndjfds", tempAdresa);
                if (!tempAdresa.equals("null") && !tempAdresa.equals("")) {
                    adresaTV.setVisibility(View.VISIBLE);
                    adresaTV.setText(tempAdresa);
                } else {
                    adresaTV.setVisibility(View.GONE);
                }
            }

            if (Objects.requireNonNull(podaci.getString("TV")).equals("true")) {
                tvCB.setChecked(true);
            } else {
                tvCB.setChecked(false);
            }
            if (Objects.requireNonNull(podaci.getString("KLIMA")).equals("true")) {
                klimaCB.setChecked(true);
            } else {
                klimaCB.setChecked(false);
            }
            if (Objects.requireNonNull(podaci.getString("RUBLJE")).equals("true")) {
                rubljeCB.setChecked(true);
            } else {
                rubljeCB.setChecked(false);
            }
            if (Objects.requireNonNull(podaci.getString("HLADNJAK")).equals("true")) {
                hladnjakCB.setChecked(true);
            } else {
                hladnjakCB.setChecked(false);
            }
            if (Objects.requireNonNull(podaci.getString("POSUDE")).equals("true")) {
                posudeCB.setChecked(true);
            } else {
                posudeCB.setChecked(false);
            }
            if (Objects.requireNonNull(podaci.getString("STEDNJAK")).equals("true")) {
                stednjakCB.setChecked(true);
            } else {
                stednjakCB.setChecked(false);
            }
        }


        izmijeniLokaciju = findViewById(R.id.izmijeniLokaciju);
        izmijeniLokaciju.setOnClickListener(view -> {
            Bundle tempPodaci = new Bundle();
            tempPodaci.putString("IZMJENALOKACIJE", "IZMJENASTANA");
            tempPodaci.putString("UIDSTAN", podaci.getString("UIDSTAN"));
            tempPodaci.putString("UIDVLASNIK", podaci.getString("UIDVLASNIK"));
            tempPodaci.putString("SVESLIKE", podaci.getString("SVESLIKE"));
            tempPodaci.putString("NAZIV", naziv.getText().toString());
            tempPodaci.putString("CIJENA", cijena.getText().toString());
            tempPodaci.putString("POVRSINA", povrsina.getText().toString());
            tempPodaci.putString("LATITUDE", podaci.getString("LATITUDE"));
            tempPodaci.putString("LONGITUDE", podaci.getString("LONGITUDE"));
            tempPodaci.putString("ADRESA", adresaTV.getText().toString());
            tempPodaci.putString("BROJSOBA", brojSoba.getSelectedItem().toString());
            tempPodaci.putString("KUPAONICA", brojKupaonica.getSelectedItem().toString());
            tempPodaci.putString("TV", String.valueOf(tvCB.isChecked()));
            tempPodaci.putString("KLIMA", String.valueOf(klimaCB.isChecked()));
            tempPodaci.putString("RUBLJE", String.valueOf(rubljeCB.isChecked()));
            tempPodaci.putString("HLADNJAK", String.valueOf(hladnjakCB.isChecked()));
            tempPodaci.putString("POSUDE", String.valueOf(posudeCB.isChecked()));
            tempPodaci.putString("STEDNJAK", String.valueOf(stednjakCB.isChecked()));
            Intent izmjenaLokacije = new Intent(IzmijeniStan.this, MapsActivity.class);
            izmjenaLokacije.putExtras(tempPodaci);
            startActivity(izmjenaLokacije);
        });



        izmijeniSlike = findViewById(R.id.izmijeniSlike);
        izmijeniSlike.setOnClickListener(view -> {
            Bundle tempPodaci = new Bundle();
            tempPodaci.putString("IZMJENALOKACIJE", "IZMJENASTANA");
            tempPodaci.putString("UIDSTAN", podaci.getString("UIDSTAN"));
            tempPodaci.putString("UIDVLASNIK", podaci.getString("UIDVLASNIK"));
            tempPodaci.putString("SVESLIKE", podaci.getString("SVESLIKE"));
            tempPodaci.putString("NAZIV", naziv.getText().toString());
            tempPodaci.putString("CIJENA", cijena.getText().toString());
            tempPodaci.putString("POVRSINA", povrsina.getText().toString());
            tempPodaci.putString("LATITUDE", podaci.getString("LATITUDE"));
            tempPodaci.putString("LONGITUDE", podaci.getString("LONGITUDE"));
            tempPodaci.putString("ADRESA", adresaTV.getText().toString());
            tempPodaci.putString("BROJSOBA", brojSoba.getSelectedItem().toString());
            tempPodaci.putString("KUPAONICA", brojKupaonica.getSelectedItem().toString());
            tempPodaci.putString("TV", String.valueOf(tvCB.isChecked()));
            tempPodaci.putString("KLIMA", String.valueOf(klimaCB.isChecked()));
            tempPodaci.putString("RUBLJE", String.valueOf(rubljeCB.isChecked()));
            tempPodaci.putString("HLADNJAK", String.valueOf(hladnjakCB.isChecked()));
            tempPodaci.putString("POSUDE", String.valueOf(posudeCB.isChecked()));
            tempPodaci.putString("STEDNJAK", String.valueOf(stednjakCB.isChecked()));
            Intent izmjenaSlika = new Intent(IzmijeniStan.this, OdabirSlika.class);
            izmjenaSlika.putExtras(tempPodaci);
            startActivity(izmjenaSlika);
        });


    }



    private int dohvatiIndeks(Spinner spinner, String myString){
        for (int i=0;i<spinner.getCount();i++){
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)){
                return i;
            }
        }
        return 0;
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
                //uploadajSlikeUBazuIStorage(listaURIsveSlike);   /*  OVO JE BILO PRIJE!!!!!!!!!!!!!!!!!!! NEMOJ ODMAH UPLOADAT SLIKE NEGO KASNIJE KADA SE SKUPE SVI PODACI !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                if (listaSlika.size() > 0) {
                    Bundle podaci = new Bundle();
                    podaci.putStringArrayList("LISTASLIKA", discreteAdapter.vratiListuSlikuIzAdaptera());     // pošalji u sljedeći Activity onakav redoslijed slika kakav je u Discrete Adapteru
                    podaci.putString("IZMJENASTANA", "IZMJENAPODATAKASTANA");
                    podaci.putString("UIDSTAN", stanUID);
                    podaci.putString("UIDVLASNIK", vlasnikUID);
                    podaci.putString("SVESLIKE",  sveSlike);
                    podaci.putStringArrayList("LISTASLIKA", discreteAdapter.vratiListuSlikuIzAdaptera());
                    podaci.putString("OPIS", naziv.getText().toString());
                    podaci.putString("CIJENA", cijena.getText().toString());
                    podaci.putString("POVRSINA", povrsina.getText().toString());
                    podaci.putString("LATITUDE", latitude);
                    podaci.putString("LONGITUDE", longitude);
                    podaci.putString("ADRESA", odabranaAdresa);
                    podaci.putString("BROJSOBA", brojSoba.getSelectedItem().toString());
                    podaci.putString("KUPAONICA", brojKupaonica.getSelectedItem().toString());
                    podaci.putString("TELEVIZIJA", String.valueOf(tvCB.isChecked()));
                    podaci.putString("KLIMA", String.valueOf(klimaCB.isChecked()));
                    podaci.putString("RUBLJE", String.valueOf(rubljeCB.isChecked()));
                    podaci.putString("HLADNJAK", String.valueOf(hladnjakCB.isChecked()));
                    podaci.putString("POSUDE", String.valueOf(posudeCB.isChecked()));
                    podaci.putString("STEDNJAK", String.valueOf(stednjakCB.isChecked()));
                    Intent izmijeniPodatkeStana = new Intent(IzmijeniStan.this, SazetakInformacija.class);
                    izmijeniPodatkeStana.putExtras(podaci);
                    startActivity(izmijeniPodatkeStana);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(IzmijeniStan.this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(IzmijeniStan.this);
        }
        builder.setTitle("Prekinuti izmjenu podataka?")
                .setMessage("Svi uneseni podaci bit će obrisani.")
                .setPositiveButton("Prekini izmjenu podataka", (dialog, which) -> {
                    Toast.makeText(getApplicationContext(), "Izmjena podataka prekinuta!", Toast.LENGTH_SHORT).show();
                    Intent pocetniEkran = new Intent(IzmijeniStan.this, ListaStanova.class);
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
