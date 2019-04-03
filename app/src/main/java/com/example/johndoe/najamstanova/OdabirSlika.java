package com.example.johndoe.najamstanova;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.fxn.pix.Pix;
import com.fxn.utility.PermUtil;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.theartofdev.edmodo.cropper.CropImage;
import com.veinhorn.scrollgalleryview.MediaInfo;
import com.veinhorn.scrollgalleryview.ScrollGalleryView;
import com.yarolegovich.discretescrollview.DiscreteScrollView;
import com.yarolegovich.discretescrollview.transform.Pivot;
import com.yarolegovich.discretescrollview.transform.ScaleTransformer;


import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;


public class OdabirSlika extends AppCompatActivity {
    RecyclerView recyclerView;
    private ScrollGalleryView scrollGalleryView;
    List<MediaInfo> infoSlike = new ArrayList<>();
    ArrayList<String> listaURItrenutnoOdabraneSlike = new ArrayList<>();    // slike koje je korisnik trenutno odabrao
    ArrayList<String> listaURIsveSlike = new ArrayList<>();     // ukoliko korisnik doda nove slike, uz vec postojece
    ArrayList<String> listaURIvratiSlike = new ArrayList<>();     // ukoliko korisnik doda nove slike, uz vec postojece
    ArrayList<String> tempListaURIsveSlike = new ArrayList<>();     // ukoliko korisnik doda nove slike, uz vec postojece
    ArrayList<String> URLslikeBaza = new ArrayList<>();     // URL slika iz Firebase Database
    String sveSlike = "";
    private String key;
    private static final FirebaseDatabase DATABASE = FirebaseDatabase.getInstance();
    private DatabaseReference mDatabase;
    private StorageReference mStorage;
    Integer brojacUploadnihSlika = 0;
    ProgressBar uploadLoadingBar;
    Bundle podaci;
    Bundle spremiSlike;
    ImageButton glavnaSlika;
    DiscreteScrollView discreteScrollView;
    DiscreteScrollAdapter discreteAdapter;
    String odabraniBrojSoba;
    String odabraniBrojKupaonica;
    Boolean izmjenaPodatakaStana = false;
    Boolean odabraneNoveSlike = false;
    String stanUID, vlasnikUID, naziv, cijena, povrsina, latitude, longitude, adresa, brojSoba, kupaonica, tv, klima, rublje, hladnjak, posude, stednjak;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // omoguci animacije Activitya
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);

        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_odabir_slika);
        setContentView(R.layout.discrete_scroll);
        this.setTitle("2.) Fotografije:");

        discreteScrollView = findViewById(R.id.discreteScroll);
        discreteAdapter = new DiscreteScrollAdapter(listaURIsveSlike);
        discreteScrollView.setItemTransformer(new ScaleTransformer.Builder()
                .setMaxScale(1.05f)
                .setMinScale(0.8f)
                .setPivotX(Pivot.X.CENTER) // CENTER is a default one
                .setPivotY(Pivot.Y.BOTTOM) // CENTER is a default one
                .build());

        uploadLoadingBar = findViewById(R.id.progressBar);
        uploadLoadingBar.bringToFront();

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
            adresa = podaci.getString("ADRESA");
            brojSoba = podaci.getString("BROJSOBA");
            kupaonica = podaci.getString("KUPAONICA");
            tv = podaci.getString("TV");
            klima = podaci.getString("KLIMA");
            rublje = podaci.getString("RUBLJE");
            hladnjak = podaci.getString("HLADNJAK");
            posude = podaci.getString("POSUDE");
            stednjak = podaci.getString("STEDNJAK");
        }

        if (!izmjenaPodatakaStana) {
            vratiOdabraneSlike(listaURIsveSlike);
        }


        // resetiraj sve kada se dodaju nove slike
        infoSlike.clear();

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance().getReference();

        key = DATABASE.getReference().push().getKey();

        // FAB za dodavanje novih slika
        findViewById(R.id.fabAdd).setOnClickListener((View view) -> {
            Pix.start(OdabirSlika.this, 100, 5);
        });

        // postavlja trenutnu sliku na prvu (glavnu) sliku
        glavnaSlika = findViewById(R.id.glavnaSlika);
        glavnaSlika.bringToFront();
        glavnaSlika.setOnClickListener(v -> {
            glavnaSlika.setEnabled(false);
            if (listaURIsveSlike.size() > 1) {
                //String trenutniUri = listaURIsveSlike.get(scrollGalleryView.getCurrentItem());                /* AKO SE KORISTI scrollGalleryView */
                //listaURIsveSlike.set(scrollGalleryView.getCurrentItem(), listaURIsveSlike.get(0));

                discreteScrollView.smoothScrollToPosition(0);

                String trenutniUri = listaURIsveSlike.get(discreteScrollView.getCurrentItem());
                Integer trenutnaPozicija = discreteScrollView.getCurrentItem();
                String prviUri = listaURIsveSlike.get(0);

                for (String uri : listaURIsveSlike) {
                    Log.d("KOJE1", uri);
                }

                listaURIsveSlike.set(trenutnaPozicija, prviUri);
                listaURIsveSlike.set(0, trenutniUri);

                for (String uri : listaURIsveSlike) {
                    Log.d("KOJE2", uri);
                }

                scrollGalleryView.clearGallery();

                for (String uri : listaURIsveSlike) {
                    infoSlike.add(MediaInfo.mediaLoader(new PicassoImageLoader(uri)));
                    scrollGalleryView.addMedia(MediaInfo.mediaLoader(new PicassoImageLoader(uri)));
                }
                discreteAdapter = new DiscreteScrollAdapter(listaURIsveSlike);
                discreteScrollView.setAdapter(discreteAdapter);
                discreteAdapter.notifyDataSetChanged();

                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Slika postavljena kao glavna.", Snackbar.LENGTH_SHORT);
                snackbar.show();
                glavnaSlika.setEnabled(true);

            }

        });


        scrollGalleryView = findViewById(R.id.scroll_gallery_view);
        scrollGalleryView
                .setThumbnailSize(100)
                .setZoom(true)
                .withHiddenThumbnails(true)
                .setFragmentManager(getSupportFragmentManager());

        // kada korisnik klikne na sliku
        scrollGalleryView.addOnImageClickListener(new ScrollGalleryView.OnImageClickListener() {
            @Override
            public void onClick() {
                Log.d("klik", String.valueOf(scrollGalleryView.getCurrentItem()));
            }
        });

        // FAB za brisanje odabrane slike
        findViewById(R.id.fabDelete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listaURIsveSlike.size() > 0) {
                    new MaterialDialog.Builder(OdabirSlika.this)
                            .title("Potvrda brisanja")
                            .content("Želite li zaista ukloniti sliku?")
                            .positiveText("Ukloni")
                            .negativeText("Odustani")
                            .onPositive((dialog, which) -> {
                                tempListaURIsveSlike.addAll(listaURIsveSlike);
                                //tempListaURIsveSlike.remove(scrollGalleryView.getCurrentItem());
                                tempListaURIsveSlike.remove(discreteScrollView.getCurrentItem());
                                listaURIsveSlike.clear();
                                listaURIsveSlike.addAll(tempListaURIsveSlike);
                                tempListaURIsveSlike.clear();

                                scrollGalleryView.clearGallery();

                                for (String uri : listaURIsveSlike) {
                                    Log.d("SAD3", uri);
                                    infoSlike.add(MediaInfo.mediaLoader(new PicassoImageLoader(uri)));
                                    scrollGalleryView.addMedia(MediaInfo.mediaLoader(new PicassoImageLoader(uri)));
                                }

                                discreteAdapter = new DiscreteScrollAdapter(listaURIsveSlike);
                                discreteScrollView.setAdapter(discreteAdapter);
                                discreteAdapter.notifyDataSetChanged();

                            })
                            .onNegative((dialog, which) -> {
                                // do nothing
                            })
                            .show();
                }
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case (100): {   // odabir slike iz Galerije ili Kamera
                Log.d("SADA", "case 100");
                if (resultCode == Activity.RESULT_OK) {
                    odabraneNoveSlike = true;
                    listaURItrenutnoOdabraneSlike = data.getStringArrayListExtra(Pix.IMAGE_RESULTS);
                    for (String uri : listaURItrenutnoOdabraneSlike) {
                        listaURIsveSlike.add(uri);
                        infoSlike.add(MediaInfo.mediaLoader(new PicassoImageLoader(uri)));
                        scrollGalleryView.addMedia(MediaInfo.mediaLoader(new PicassoImageLoader(uri)));
                    }

                    for (String uri : listaURIsveSlike) {
                        Log.d("KOJE2", uri);
                    }

                    discreteScrollView.setAdapter(new DiscreteScrollAdapter(listaURIsveSlike));

                }
            }
            break;
            case (CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE): {   // za Croppanje slike
                Log.d("SADA", "case 200");
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {
                    Uri resultUri = result.getUri();
                    List<String> odvojeneSlike = new ArrayList<String>(Arrays.asList(resultUri.toString().split("file://")));

                    // cisti prazna mjesta u listi nakon split
                    for (int i=0; i<odvojeneSlike.size(); i++) {
                       if (odvojeneSlike.get(i).equals("")) {
                           odvojeneSlike.remove(i);
                       }
                    }

                    Log.d("SAD2", String.valueOf(result.getUri()) + " # " + odvojeneSlike.size());

                    //scrollGalleryView.clearGallery();

                    for (String uriCrop : odvojeneSlike) {
                        //listaURIsveSlike.add(uriCrop);
                        Log.d("SAD3", uriCrop);
                        listaURIsveSlike.add(uriCrop);
                        infoSlike.add(MediaInfo.mediaLoader(new PicassoImageLoader(uriCrop)));
                        scrollGalleryView.addMedia(MediaInfo.mediaLoader(new PicassoImageLoader(uriCrop)));
                    }

                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();
                }
            }
            break;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PermUtil.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                // Ako je zahtjev otkazan, array je prazan
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Pix.start(OdabirSlika.this, 100, 5);
                } else {
                    Toast.makeText(OdabirSlika.this, "Potrebna je dozvola za pristup fotografijama.", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }


    // uploada odabrane slike na Firebase Storage i sprema ih kao zapis za odabrani stan u Firebase Database
    public void uploadajSlikeUBazuIStorage(ArrayList listaURIsveSlike) {
        brojacUploadnihSlika = 0;
        if (listaURIsveSlike.size() > 0) {
            uploadLoadingBar.setVisibility(View.VISIBLE);
            for (int i = 0; i < listaURIsveSlike.size(); i++) {
                final StorageReference fileToUpload = mStorage.child(new StringBuilder("Images/").append(UUID.randomUUID().toString()).toString());
                UploadTask uploadTask = fileToUpload.putFile(Uri.parse("file://" + listaURIsveSlike.get(i).toString()));

                Task<Uri> urlTask = uploadTask.continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }
                    return fileToUpload.getDownloadUrl();
                }).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        brojacUploadnihSlika++;
                        if (brojacUploadnihSlika.equals(listaURIsveSlike.size())) {
                            uploadLoadingBar.setVisibility(View.GONE);
                            Snackbar.make(findViewById(android.R.id.content), "Slike uspješno prenesene!", Snackbar.LENGTH_SHORT)
                                    .setActionTextColor(Color.RED)
                                    .show();
                        }
                        Uri downloadUri = task.getResult();
                        URLslikeBaza.add(downloadUri.toString());
                        sveSlike = sveSlike + downloadUri.toString() + "$";   // sve slike sprema u jedan string URL-ova razdvojen znakom $

                        // Zapisi dodane slike za Stan u Firebase Database
                        mDatabase.child("Stanovi").child("13579").child("slike").setValue(sveSlike)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("TAG", "Slike " + sveSlike + " dodane!");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Write failed
                                        // ...
                                    }
                                });
                    } else {
                        Toast.makeText(OdabirSlika.this, "Pogreska prilikom prijenosa slika!", Toast.LENGTH_SHORT).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(OdabirSlika.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else {
            Snackbar.make(findViewById(android.R.id.content), "Odaberite slike za upload!", Snackbar.LENGTH_SHORT)
                    .setActionTextColor(Color.RED)
                    .show();
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
                //uploadajSlikeUBazuIStorage(listaURIsveSlike);   /*  OVO JE BILO PRIJE!!!!!!!!!!!!!!!!!!! NEMOJ ODMAH UPLOADAT SLIKE NEGO KASNIJE KADA SE SKUPE SVI PODACI !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                if (listaURIsveSlike.size() > 0 && !izmjenaPodatakaStana) {
                    podaci.putStringArrayList("LISTASLIKA", discreteAdapter.vratiListuSlikuIzAdaptera());     // pošalji u sljedeći Activity onakav redoslijed slika kakav je u Discrete Adapteru
                    for (String uri : listaURIsveSlike) {
                        Log.d("KOJE3", uri);
                    }
                    Intent odaberiLokaciju = new Intent(OdabirSlika.this, MapsActivity.class);
                    odaberiLokaciju.putExtras(podaci);

                    startActivity(odaberiLokaciju, ActivityOptions.makeSceneTransitionAnimation(OdabirSlika.this).toBundle());
                } else if (listaURIsveSlike.size() > 0 && izmjenaPodatakaStana) {
                Bundle podaci = new Bundle();
                podaci.putString("IZMJENALOKACIJE", "IZMJENASTANA");
                podaci.putString("IZMJENANOVESLIKE", String.valueOf(odabraneNoveSlike));
                podaci.putString("UIDSTAN", stanUID);
                podaci.putString("UIDVLASNIK", vlasnikUID);
                if (odabraneNoveSlike) {
                    podaci.putStringArrayList("LISTASLIKE", discreteAdapter.vratiListuSlikuIzAdaptera());
                    Log.d("dsak", "prvi " + discreteAdapter.vratiListuSlikuIzAdaptera().get(0));
                } else {
                    podaci.putString("SVESLIKE",  sveSlike);
                    Log.d("dsak", "drugfi");
                }
                podaci.putString("NAZIV", naziv);
                podaci.putString("CIJENA", cijena);
                podaci.putString("POVRSINA", povrsina);
                podaci.putString("LATITUDE", latitude);
                podaci.putString("LONGITUDE", longitude);
                podaci.putString("ADRESA", adresa);
                podaci.putString("BROJSOBA", brojSoba);
                podaci.putString("KUPAONICA", kupaonica);
                podaci.putString("TV", tv);
                podaci.putString("KLIMA", klima);
                podaci.putString("RUBLJE", rublje);
                podaci.putString("HLADNJAK", hladnjak);
                podaci.putString("POSUDE", posude);
                podaci.putString("STEDNJAK", stednjak);
                podaci.putString("LATITUDE", latitude);
                podaci.putString("LONGITUDE", longitude);
                Intent izmjenaSlika = new Intent(OdabirSlika.this, IzmijeniStan.class);
                izmjenaSlika.putExtras(podaci);
                startActivity(izmjenaSlika);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public Integer[] odrediSirinuVisinuSlike(String uriSLikePortrait) {
        int sirinaSlike = 0;
        int visinaSlike = 0;

        BitmapFactory.Options opcijeSlike = new BitmapFactory.Options();
        opcijeSlike.inJustDecodeBounds = true;
        Uri uriSlike = Uri.parse(uriSLikePortrait);
        BitmapFactory.decodeFile(new File(uriSlike.getPath()).getAbsolutePath(), opcijeSlike);
        sirinaSlike = opcijeSlike.outWidth;
        visinaSlike = opcijeSlike.outHeight;
        Log.d("VELICINA", String.valueOf(sirinaSlike + "x" + visinaSlike));


        Integer vratiVelicinuSlike[] = new Integer[2];
        vratiVelicinuSlike[0]= sirinaSlike;
        vratiVelicinuSlike[1] =  visinaSlike;
        return vratiVelicinuSlike;      // vraca dvije vrijednosti odjednom - sirinu i visinu slike
    }


    public int dobivanjeOrijentacijeSlike(String uriSlike){
        ExifInterface ei = null;
        int orijentacijaSlike = 0;
        try {
            ei = new ExifInterface(uriSlike);
            orijentacijaSlike = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
        } catch (IOException e) {
            e.printStackTrace();
        }
        switch(orijentacijaSlike) {

            case ExifInterface.ORIENTATION_ROTATE_90:
                orijentacijaSlike = 90;
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                orijentacijaSlike = 180;
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                orijentacijaSlike = 270;
                break;

            case ExifInterface.ORIENTATION_NORMAL:
            default:
                orijentacijaSlike = 0;
        }
        Log.d("SAD4", String.valueOf(orijentacijaSlike));
        return orijentacijaSlike;
    }



    // sprema stanje kada korisnik unese slike, primjerice nakon rotacije uređaja
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList("LISTASLIKA", listaURIsveSlike);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        spremiOdabraneSlike();
        super.onBackPressed();
    }

    private void spremiOdabraneSlike(){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("LISTASVESLIKEVELICINA", listaURIsveSlike.size());

        for(int i=0;i<listaURIsveSlike.size();i++) {
            editor.remove("LISTASVESLIKE_" + i);
            editor.putString("LISTASVESLIKE_" + i, listaURIsveSlike.get(i));
        }
        editor.apply();
    }


    private void vratiOdabraneSlike(ArrayList listaURIsveSlike){
        SharedPreferences preferences =  PreferenceManager.getDefaultSharedPreferences(OdabirSlika.this);
        listaURIsveSlike.clear();
        int velicina = preferences.getInt("LISTASVESLIKEVELICINA", 0);

        listaURIsveSlike.clear();

        for(int i=0; i<velicina; i++) {
            listaURIsveSlike.add(preferences.getString("LISTASVESLIKE_" + i, null));
        }

        discreteAdapter = new DiscreteScrollAdapter(listaURIsveSlike);
        discreteScrollView.setAdapter(discreteAdapter);
        discreteAdapter.notifyDataSetChanged();
    }
}