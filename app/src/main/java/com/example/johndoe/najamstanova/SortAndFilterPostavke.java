package com.example.johndoe.najamstanova;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.polyak.iconswitch.IconSwitch;

import org.angmarch.views.NiceSpinner;

import java.util.ArrayList;


public class SortAndFilterPostavke extends AppCompatActivity {

    NiceSpinner spinnerNizaCijena, spinnerVisaCijena;
    ArrayList<String> listaNizeCijene = new ArrayList<>();
    ArrayList<String> listaViseCijene = new ArrayList<>();
    Integer odabraniIndeksNizaCijena = 0;
    Integer odabraniIndeksVisaCijena = 0;
    String odabranaNizaCijena, odabranaVisaCijena;
    IconSwitch padajuceRastuceSwitch;
    SharedPreferences preferences;
    Button odabirLokacije;
    Double odabraniLatitude, odabraniLongitude;
    String odabranaAdresa, nizaPovrsina, visaPovrsina;
    String odabranoSortiranje = "rastuce";
    Bundle podaci;
    TextView adresa, odabrana;
    CheckBox checkBoxLokacija, checkboxCijene, checkBoxPovrsina;
    ConstraintLayout sakrijPokaziCL, cijenaCL, povrsinaCL;
    EditText odPovrsina, doPovrsina;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sort_and_filter_postavke);

        TextView padajuceTextView = findViewById(R.id.padajuceTextView);
        TextView rastuceTextView = findViewById(R.id.rastuceTextView);
        odabirLokacije = findViewById(R.id.odabirLokacije);
        adresa = findViewById(R.id.adresa);
        odabrana = findViewById(R.id.odabrana);
        adresa.setVisibility(View.GONE);
        odabrana.setVisibility(View.GONE);

        sakrijPokaziCL = findViewById(R.id.sakrijPokaziCL);
        cijenaCL = findViewById(R.id.cijenaCL);
        sakrijPokaziCL.setVisibility(View.GONE);
        cijenaCL.setVisibility(View.GONE);
        povrsinaCL = findViewById(R.id.povrsinaCL);
        povrsinaCL.setVisibility(View.GONE);

        checkBoxLokacija = findViewById(R.id.checkBoxLokacija);
        checkboxCijene = findViewById(R.id.checkBoxCijena);
        checkBoxPovrsina = findViewById(R.id.checkBoxPovrsina);

        odPovrsina = findViewById(R.id.odPovrsina);
        doPovrsina = findViewById(R.id.doPovrsina);

        // učitaj Postavke Pretrage (padajuce ili rastuce u Switchu) ako je korisnik već mijenjao postavke za Padajuce ili Rastuce Sortiranje
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String checkboxAktiviran = preferences.getString("CHECKBOXLOKACIJA", "false");
        String checkboxCijeneAktiviran = preferences.getString("CHECKBOXCIJENE", "false");
        String checkboxPovrsinaAktiviran = preferences.getString("CHECKBOXPOVRSINA", "false");
        odabranaAdresa = preferences.getString("odabranaAdresa", "");
        nizaPovrsina = preferences.getString("nizaPovrsina", "");
        visaPovrsina = preferences.getString("visaPovrsina", "");
        odabraniLatitude = Double.valueOf(preferences.getString("odabraniLatitude", "0.0"));
        odabraniLongitude = Double.valueOf(preferences.getString("odabraniLongitude", "0.0"));

        Log.d("pocetni", checkboxAktiviran + "  ovo je loadao: " + odabranaAdresa + " -> " + odabraniLatitude );

        if (!nizaPovrsina.equals("")) {
            odPovrsina.setText(nizaPovrsina);
        }
        if (!visaPovrsina.equals("")) {
            doPovrsina.setText(visaPovrsina);
        }
        if (!odabranaAdresa.equals("")) {
            adresa.setVisibility(View.VISIBLE);
            odabrana.setVisibility(View.VISIBLE);
            adresa.setText(odabranaAdresa);
        }

        checkBoxLokacija.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkBoxLokacija.isChecked()) {
                    checkboxCijene.setEnabled(false);
                    sakrijPokaziCL.setVisibility(View.VISIBLE);
                    checkBoxLokacija.setText("Sortiranje po udaljenosti od odabrane lokacije");
                    preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("CHECKBOXLOKACIJA", String.valueOf(checkBoxLokacija.isChecked()));
                    editor.apply();
                    Log.d("loadaosam",  String.valueOf(checkBoxLokacija.isChecked()));
                } else if (!checkBoxLokacija.isChecked()) {
                    checkboxCijene.setEnabled(true);
                    checkboxCijene.setVisibility(View.VISIBLE);
                    sakrijPokaziCL.setVisibility(View.GONE);
                    checkBoxLokacija.setText("Sortiranje po udaljenosti od trenutne lokacije");
                    preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("CHECKBOXLOKACIJA", String.valueOf(checkBoxLokacija.isChecked()));
                    editor.putString("ODABRANILATITUDE", "0.0");
                    editor.putString("ODABRANILONGITUDE", "0.0");
                    Log.d("loadaosam",  String.valueOf(checkBoxLokacija.isChecked()));
                    editor.apply();


                }
            }
        });


        checkboxCijene.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkboxCijene.isChecked()) {
                    checkBoxLokacija.setEnabled(false);
                    checkBoxPovrsina.setEnabled(false);
                    cijenaCL.setVisibility(View.VISIBLE);
                    preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("CHECKBOXCIJENE", String.valueOf(checkboxCijene.isChecked()));
                    editor.apply();
                    Log.d("loadaosam",  String.valueOf(checkboxCijene.isChecked()));
                } else if (!checkboxCijene.isChecked()) {
                    checkBoxLokacija.setEnabled(true);
                    checkBoxPovrsina.setEnabled(true);
                    checkBoxLokacija.setVisibility(View.VISIBLE);
                    cijenaCL.setVisibility(View.GONE);
                    preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("CHECKBOXCIJENE", String.valueOf(checkboxCijene.isChecked()));
                    Log.d("loadaosam",  String.valueOf(checkboxCijene.isChecked()));
                    editor.apply();
                }
            }
        });

        checkBoxPovrsina.setOnClickListener(view -> {
            if (checkBoxPovrsina.isChecked()) {
                checkBoxLokacija.setEnabled(false);
                checkboxCijene.setEnabled(false);
                povrsinaCL.setVisibility(View.VISIBLE);
                preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("CHECKBOXPOVRSINA", String.valueOf(checkBoxPovrsina.isChecked()));
                editor.apply();
                Log.d("loadaosam",  String.valueOf(checkboxCijene.isChecked()));
            } else if (!checkBoxPovrsina.isChecked()) {
                checkBoxLokacija.setEnabled(true);
                checkboxCijene.setEnabled(true);
                checkBoxPovrsina.setVisibility(View.VISIBLE);
                povrsinaCL.setVisibility(View.GONE);
                preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("CHECKBOXPOVRSINA", String.valueOf(checkBoxPovrsina.isChecked()));
                Log.d("loadaosam",  String.valueOf(checkBoxPovrsina.isChecked()));
                editor.apply();
            }
        });


        Intent intentExtras = getIntent();
        podaci = intentExtras.getExtras();

        if (podaci != null && !podaci.isEmpty()) {
            odabraniLatitude = podaci.getDouble("ODABRANILATITUDE", 0.0);
            odabraniLongitude = podaci.getDouble("ODABRANILONGITUDE", 0.0);
            odabranaAdresa = podaci.getString("ODABRANAADRESA", "");
            adresa.setText(odabranaAdresa);
            adresa.setVisibility(View.VISIBLE);
            odabrana.setVisibility(View.VISIBLE);
            Log.d("pocetni", " ovo je odabrao user:  " +  odabraniLatitude + ":" + odabraniLongitude + " --> " + odabranaAdresa);
        }


        // lokacija
        if (checkboxAktiviran.equals("true")) {
            sakrijPokaziCL.setVisibility(View.VISIBLE);
            checkBoxLokacija.setText("Sortiranje po udaljenosti od odabrane lokacije");
            checkBoxLokacija.setChecked(true);
            odabrana.setVisibility(View.VISIBLE);
            adresa.setVisibility(View.VISIBLE);
            checkBoxPovrsina.setEnabled(false);
            checkboxCijene.setEnabled(false);
            adresa.setText(odabranaAdresa);
            checkBoxLokacija.setVisibility(View.GONE);
        } else if (!checkboxAktiviran.equals("true")) {
            sakrijPokaziCL.setVisibility(View.GONE);
            checkBoxLokacija.setText("Sortiranje po udaljenosti od trenutne lokacije");
            checkBoxLokacija.setChecked(false);
            odabrana.setVisibility(View.GONE);
            adresa.setVisibility(View.GONE);
            adresa.setText("");
            checkBoxLokacija.setVisibility(View.VISIBLE);
            checkBoxPovrsina.setEnabled(true);
            checkboxCijene.setEnabled(true);
        }


        if (checkboxCijeneAktiviran.equals("true")) {
            cijenaCL.setVisibility(View.VISIBLE);
            ///checkBoxLokacija.setVisibility(View.GONE);
            checkboxCijene.setChecked(true);
            checkBoxPovrsina.setEnabled(false);
            checkBoxLokacija.setEnabled(false);
        } else if (!checkboxCijeneAktiviran.equals("true")) {
            cijenaCL.setVisibility(View.GONE);
            checkBoxLokacija.setVisibility(View.VISIBLE);
            checkboxCijene.setChecked(false);
            checkBoxPovrsina.setEnabled(true);
            checkBoxLokacija.setEnabled(true);
        }


        if (checkboxPovrsinaAktiviran.equals("true")) {
            povrsinaCL.setVisibility(View.VISIBLE);
            //checkBoxPovrsina.setVisibility(View.GONE);
            checkBoxPovrsina.setChecked(true);
            checkboxCijene.setEnabled(false);
            checkBoxLokacija.setEnabled(false);
        } else if (!checkboxCijeneAktiviran.equals("true")) {
            povrsinaCL.setVisibility(View.GONE);
            checkBoxLokacija.setVisibility(View.VISIBLE);
            checkboxCijene.setVisibility(View.VISIBLE);
            checkboxCijene.setChecked(false);
            checkboxCijene.setEnabled(true);
            checkBoxLokacija.setEnabled(true);
        }


        odabirLokacije.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent odabirLokacijeMapa = new Intent(SortAndFilterPostavke.this, MapsOdabirLokacijePretrage.class);
                startActivity(odabirLokacijeMapa);
            }
        });

        // učitaj Postavke Pretrage (padajuce ili rastuce u Switchu) ako je korisnik već mijenjao postavke za Padajuce ili Rastuce Sortiranje
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String padajuceRastuce = preferences.getString("SORTIRANJE", "rastuce");

        padajuceRastuceSwitch = findViewById(R.id.padajuceRastuceSwitch);

        padajuceTextView.setTypeface(null, Typeface.BOLD);
        if (!padajuceRastuce.equals("nemaPrethodnogOdabira")) {
            if (padajuceRastuce.equals("padajuce")) {
                padajuceRastuceSwitch.setChecked(IconSwitch.Checked.LEFT);
                padajuceTextView.setTypeface(null, Typeface.BOLD);
                rastuceTextView.setTypeface(null, Typeface.NORMAL);
            } else if (padajuceRastuce.equals("rastuce")) {
                padajuceRastuceSwitch.setChecked(IconSwitch.Checked.RIGHT);
                padajuceTextView.setTypeface(null, Typeface.NORMAL);
                rastuceTextView.setTypeface(null, Typeface.BOLD);
            }
        } else {
            padajuceRastuceSwitch.setChecked(IconSwitch.Checked.RIGHT);      // po defaultu postavi Rastuce sortiranje
        }

        padajuceRastuceSwitch.setCheckedChangeListener(new IconSwitch.CheckedChangeListener() {
            @Override
            public void onCheckChanged(IconSwitch.Checked current) {
                if (current.toString().equals("LEFT")) {
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("SORTIRANJE", "padajuce");
                    editor.apply();
                    padajuceTextView.setTypeface(null, Typeface.BOLD);
                } else {
                    padajuceTextView.setTypeface(null, Typeface.NORMAL);
                }
                if (current.toString().equals("RIGHT")) {
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("SORTIRANJE", "rastuce");
                    editor.apply();
                    rastuceTextView.setTypeface(null, Typeface.BOLD);
                } else {
                    rastuceTextView.setTypeface(null, Typeface.NORMAL);
                }
                preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("PADAJUCERASTUCE", current.toString());
                editor.apply();
            }
        });

        spinnerNizaCijena = findViewById(R.id.spinnerNizaCijena);
        spinnerVisaCijena = findViewById(R.id.spinnerVisaCijena);

        odabranaNizaCijena = "Bilo koja";

        listaNizeCijene.clear();
        listaViseCijene.clear();

        listaNizeCijene.add("Bilo koja");
        listaViseCijene.add("Bilo koja");

        for (Integer i=500; i<=5000; i+=500) {
            listaNizeCijene.add(String.valueOf(i));
        }
        for (Integer i=1000; i<=10000; i+=500) {
            listaViseCijene.add(String.valueOf(i));
        }

        spinnerNizaCijena.attachDataSource(listaNizeCijene);
        spinnerVisaCijena.attachDataSource(listaViseCijene);

        // učitaj Postavke Pretrage ako je korisnik već mijenjao postavke
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String nizaCijena = preferences.getString("nizaCijena", "Bilo koja");
        String visaCijena = preferences.getString("visaCijena", "Bilo koja");

        for (int i=0; i<listaNizeCijene.size(); i++) {
            if (listaNizeCijene.get(i).equals(nizaCijena)) {
                //nizaCijenaSpinner.setSelectedIndex(i);
                spinnerNizaCijena.setSelectedIndex(i);
                Log.d("rjesenje1", String.valueOf(i) + " " + nizaCijena);
                break;
            }
        }
        for (int i=0; i<listaViseCijene.size(); i++) {
            if (listaViseCijene.get(i).equals(visaCijena)) {
                //visaCijenaSpinner.setSelectedIndex(i);
                spinnerVisaCijena.setSelectedIndex(i);
                Log.d("rjesenje2", String.valueOf(i) + " " + visaCijena);
                break;
            }
        }


        spinnerNizaCijena.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                odabraniIndeksNizaCijena = spinnerNizaCijena.getSelectedIndex();
                odabraniIndeksVisaCijena = spinnerVisaCijena.getSelectedIndex();
                String odabranaVisaVrijednost = listaViseCijene.get(odabraniIndeksVisaCijena);
                Log.d("cijena", odabraniIndeksNizaCijena + "**" + odabraniIndeksVisaCijena);
                odabranaNizaCijena = listaNizeCijene.get(spinnerNizaCijena.getSelectedIndex());
                listaViseCijene.clear();
                listaViseCijene.add("Bilo koja");
                Log.d("odabir", odabranaNizaCijena);
                if (odabranaNizaCijena.equals("Bilo koja")) {
                    for (Integer j=1000; j<=10000; j+=500) {
                        listaViseCijene.add(String.valueOf(j));
                    }
                } else {
                    for (Integer j=Integer.valueOf(odabranaNizaCijena)+500; j<=10000; j+=500) {
                        listaViseCijene.add(String.valueOf(j));
                    }
                }

               spinnerVisaCijena.attachDataSource(listaViseCijene);

                for (int j=0; j<listaViseCijene.size(); j++) {
                    if (listaViseCijene.get(j).equals(odabranaVisaVrijednost)) {
                        spinnerVisaCijena.setSelectedIndex(j);
                        Log.d("rjesenje", String.valueOf(j) + " " + visaCijena);
                        break;
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        spinnerVisaCijena.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                odabraniIndeksNizaCijena = spinnerNizaCijena.getSelectedIndex();
                odabraniIndeksVisaCijena = spinnerVisaCijena.getSelectedIndex();
                String odabranaNizaVrijednost = listaNizeCijene.get(odabraniIndeksNizaCijena);
                odabranaVisaCijena = listaViseCijene.get(spinnerVisaCijena.getSelectedIndex());
                listaNizeCijene.clear();
                listaNizeCijene.add("Bilo koja");
                Log.d("odabir", odabranaVisaCijena);
                if (odabranaVisaCijena.equals("Bilo koja")) {
                    for (Integer i=500; i<=5000; i+=500) {
                        listaNizeCijene.add(String.valueOf(i));
                    }
                } else {
                    for (Integer i=500; i<Integer.valueOf(odabranaVisaCijena); i+=500) {
                        listaNizeCijene.add(String.valueOf(i));
                    }
                }

                spinnerNizaCijena.attachDataSource(listaNizeCijene);

                for (int i=0; i<listaNizeCijene.size(); i++) {
                    if (listaNizeCijene.get(i).equals(odabranaNizaVrijednost)) {
                        spinnerNizaCijena.setSelectedIndex(i);
                        Log.d("rjesenje", String.valueOf(i) + " " + nizaCijena);
                        break;
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        Button spremiPostavke = findViewById(R.id.spremiPostavke);
        spremiPostavke.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String odabir;
                String nizaCijena = listaNizeCijene.get(spinnerNizaCijena.getSelectedIndex());
                String visaCijena = listaViseCijene.get(spinnerVisaCijena.getSelectedIndex());
                Log.d("cijena", nizaCijena + " <> " + visaCijena);
                if (!nizaCijena.equals("Bilo koja") && visaCijena.equals("Bilo koja")) {
                    Log.d("cijena", "1. slucaj");
                    odabir = "1";
                } else if (!visaCijena.equals("Bilo koja") && nizaCijena.equals("Bilo koja")) {
                    Log.d("cijena", "2. slucaj");
                    odabir = "2";
                } else if (!nizaCijena.equals("Bilo koja") && !visaCijena.equals("Bilo koja")) {
                    Log.d("cijena", "3. slucaj");
                    odabir = "3";
                } else {
                    Log.d("cijena", "4. slucaj");
                    odabir = "4";
                }

                String nizaPovrsina = odPovrsina.getText().toString();
                String visaPovrsina = doPovrsina.getText().toString();

                preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("nizaCijena", odabranaNizaCijena);
                editor.putString("visaCijena", odabranaVisaCijena);
                editor.putString("nizaPovrsina", nizaPovrsina);
                editor.putString("visaPovrsina", visaPovrsina);
                editor.putString("odabranaAdresa", odabranaAdresa);
                editor.putString("odabraniLatitude", String.valueOf(odabraniLatitude));
                editor.putString("odabraniLongitude", String.valueOf(odabraniLongitude));
                editor.putBoolean("checkboxStatus", checkBoxLokacija.isChecked());
                editor.apply();

                //Intent fragmentListaStanovaIntent = new Intent(SortAndFilterPostavke.this, FragmentListaStanova.class);
                //fragmentListaStanovaIntent.putExtra("QUERY", (Serializable) query);
                //startActivity(fragmentListaStanovaIntent);


                Bundle noviPodaci = new Bundle();
                noviPodaci.putString("SORTANDFILTER", "sortandfilter");
                switch (odabir) {
                    case "1":
                        noviPodaci.putString("QUERY", "1");
                        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        SharedPreferences.Editor editorTemp1 = preferences.edit();
                        editorTemp1.putString("QUERY", "1");
                        editorTemp1.apply();
                        break;
                    case "2":
                        noviPodaci.putString("QUERY", "2");
                        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        SharedPreferences.Editor editorTemp2 = preferences.edit();
                        editorTemp2.putString("QUERY", "2");
                        editorTemp2.apply();
                        break;
                    case "3":
                        noviPodaci.putString("QUERY", "3");
                        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        SharedPreferences.Editor editorTemp3 = preferences.edit();
                        editorTemp3.putString("QUERY", "3");
                        editorTemp3.apply();
                        break;
                    default:
                        noviPodaci.putString("QUERY", "4");
                        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        SharedPreferences.Editor editorTemp4 = preferences.edit();
                        editorTemp4.putString("QUERY", "4");
                        editorTemp4.apply();
                        break;
                }


                preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editorCijena = preferences.edit();
                noviPodaci.putString("NIZACIJENA", nizaCijena);
                noviPodaci.putString("VISACIJENA", visaCijena);
                editorCijena.putString("nizaCijena", nizaCijena);
                editorCijena.putString("visaCijena", visaCijena);
                noviPodaci.putString("NIZAPOVRSINA", nizaPovrsina);
                noviPodaci.putString("VISAPOVRSINA", visaPovrsina);
                editorCijena.putString("nizaPovrsina", nizaPovrsina);
                editorCijena.putString("visaPovrsina", visaPovrsina);
                noviPodaci.putString("ADRESA", odabranaAdresa);
                editorCijena.putString("ODABRANAADRESA", odabranaAdresa);
                noviPodaci.putString("ODABRANILATITUDE", String.valueOf(odabraniLatitude));
                noviPodaci.putString("ODABRANILONGITUDE", String.valueOf(odabraniLongitude));
                editorCijena.putString("ODABRANILATITUDE", String.valueOf(odabraniLatitude));
                editorCijena.putString("ODABRANILONGITUDE", String.valueOf(odabraniLongitude));
                editorCijena.apply();
                noviPodaci.putString("CHECKBOXCIJENE", String.valueOf(checkboxCijene.isChecked()));
                noviPodaci.putString("CHECKBOXLOKACIJA", String.valueOf(checkBoxLokacija.isChecked()));
                noviPodaci.putString("CHECKBOXPOVRSINA", String.valueOf(checkBoxPovrsina.isChecked()));

                String odabirSortiranja = padajuceRastuceSwitch.getChecked().toString();
                if (odabirSortiranja.equals("LEFT")) {
                    editor.putString("SORTIRANJE", "padajuce");
                    noviPodaci.putString("SORTIRANJE", "padajuce");
                } else if (odabirSortiranja.equals("RIGHT")) {
                    editor.putString("SORTIRANJE", "rastuce");
                    noviPodaci.putString("SORTIRANJE", "rastuce");
                }


                if (checkBoxPovrsina.isChecked()) {
                    if (!nizaPovrsina.equals("") && !visaPovrsina.equals("")) {
                        Intent fragmentListaStanova = new Intent(SortAndFilterPostavke.this, ListaStanova.class);
                        fragmentListaStanova.putExtras(noviPodaci);
                        //startActivity(fragmentListaStanova, ActivityOptions.makeSceneTransitionAnimation(SortAndFilterPostavke.this).toBundle());
                        startActivity(fragmentListaStanova);
                        overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_to_bottom);
                    } else {
                        Toast.makeText(getApplicationContext(), "Unesite vrijednosti površina!", Toast.LENGTH_SHORT).show();
                    }
                }

                if (!checkBoxPovrsina.isChecked() && !checkboxCijene.isChecked() && !checkBoxLokacija.isChecked()) {
                    Intent fragmentListaStanova = new Intent(SortAndFilterPostavke.this, ListaStanova.class);
                    fragmentListaStanova.putExtras(noviPodaci);
                    //startActivity(fragmentListaStanova, ActivityOptions.makeSceneTransitionAnimation(SortAndFilterPostavke.this).toBundle());
                    startActivity(fragmentListaStanova);
                    overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_to_bottom);
                }


                Intent fragmentListaStanova = new Intent(SortAndFilterPostavke.this, ListaStanova.class);
                fragmentListaStanova.putExtras(noviPodaci);
                //startActivity(fragmentListaStanova, ActivityOptions.makeSceneTransitionAnimation(SortAndFilterPostavke.this).toBundle());
                startActivity(fragmentListaStanova);
                overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_to_bottom);

                //FragmentListaStanova fragmentListaStanova = new FragmentListaStanova();
               //fragmentListaStanova.dohvatiPodatke(query);


            }
        });
    }
}
