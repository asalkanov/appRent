package com.example.johndoe.najamstanova;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

public class DodavanjeStanova extends AppCompatActivity  {

    EditText opis, cijena, povrsina;
    Spinner brojSoba, brojKupaonica;
    String odabraniBrojSoba = "Bez soba";
    String odabraniBrojKupaonica = "Bez kupaonica";
    CheckBox tv, klima, rublje, hladnjak, posude, stednjak;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // omoguci animacije Activitya
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dodavanje_stanova);

        opis = findViewById(R.id.opis);
        cijena = findViewById(R.id.cijenaCL);
        povrsina = findViewById(R.id.povrsina);

        tv = findViewById(R.id.tvCB);
        klima = findViewById(R.id.klimaCB);
        rublje = findViewById(R.id.rubljeCB);
        hladnjak = findViewById(R.id.hladnjakCB);
        posude = findViewById(R.id.posudeCB);
        stednjak = findViewById(R.id.stednjakCB);

        brojSoba = findViewById(R.id.brojSoba);
        brojSoba.setSelection(1);
        brojKupaonica = findViewById(R.id.brojKupaonica);
        brojKupaonica.setSelection(1);
        brojSoba.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                odabraniBrojSoba = adapterView.getItemAtPosition(i).toString();
                Log.d("sobe", odabraniBrojSoba);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                brojSoba.setSelection(1);
            }
        });
        brojKupaonica.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                odabraniBrojKupaonica = adapterView.getItemAtPosition(i).toString();
                Log.d("sobe", odabraniBrojKupaonica);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                brojKupaonica.setSelection(1);
            }
        });


    }


    // sprema uneseni tekst dok korisnik primjerice odabier slike za stan
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }


    // kreira Toolbar s Buttonom za nastavak na sljedeci Activity (OdabirSlika) prilikom dodavanja novog Stana
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
                Bundle podaci = new Bundle();
                podaci.putString("OPIS", opis.getText().toString());
                podaci.putString("CIJENA", cijena.getText().toString());
                podaci.putString("POVRSINA", povrsina.getText().toString());
                podaci.putString("BROJSOBA", odabraniBrojSoba);
                podaci.putString("KUPAONICA", odabraniBrojKupaonica);
                podaci.putString("TELEVIZIJA", String.valueOf(tv.isChecked()));
                podaci.putString("KLIMA", String.valueOf(klima.isChecked()));
                podaci.putString("RUBLJE", String.valueOf(rublje.isChecked()));
                podaci.putString("HLADNJAK", String.valueOf(hladnjak.isChecked()));
                podaci.putString("POSUDE", String.valueOf(posude.isChecked()));
                podaci.putString("STEDNJAK", String.valueOf(stednjak.isChecked()));
                Intent odabirSlika = new Intent(DodavanjeStanova.this, OdabirSlika.class);
                odabirSlika.putExtras(podaci);
                startActivity(odabirSlika, ActivityOptions.makeSceneTransitionAnimation(DodavanjeStanova.this).toBundle());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(DodavanjeStanova.this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(DodavanjeStanova.this);
        }
        builder.setTitle("Poništiti oglas?")
                .setMessage("Svi uneseni podaci bit će obrisani.")
                .setPositiveButton("Poništi oglas", (dialog, which) -> {
                    resetirajOdabirSlika();
                    finish();
                })
                .setNegativeButton("Zatvori", (dialog, which) -> {
                    // do nothing
                })
                .setIcon(android.R.drawable.ic_menu_delete)
                .show();
        //super.onBackPressed();
    }


    // izbriši sve odabrane slike u OdabirSlike kada se aplikacija pokrene prvi puta
    private void resetirajOdabirSlika(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(DodavanjeStanova.this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("LISTASVESLIKEVELICINA", 0);
        editor.remove("SPREMLJENILATITUDE");
        editor.remove("SPREMLJENILONGITUDE");
        editor.remove("SPREMLJENAULICA");
        editor.putString("SPREMLJENILATITUDE", null);
        editor.putString("SPREMLJENILONGITUDE", null);
        editor.putString("SPREMLJENAULICA", null);
        editor.apply();
        int velicina = preferences.getInt("LISTASVESLIKEVELICINA", 0);
        for(int i=0; i<velicina; i++) {
            editor.remove("LISTASVESLIKE_" + i);
            editor.putString("LISTASVESLIKE_" + i, null);
            editor.apply();
            Log.d("pobrisao", "jesam!");
        }
    }


}
