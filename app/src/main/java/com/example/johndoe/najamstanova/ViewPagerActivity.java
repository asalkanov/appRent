package com.example.johndoe.najamstanova;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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
import com.veinhorn.scrollgalleryview.MediaInfo;
import com.veinhorn.scrollgalleryview.ScrollGalleryView;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;


public class ViewPagerActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    private ScrollGalleryView scrollGalleryView;
    List<MediaInfo> infoSlike = new ArrayList<>();
    ArrayList<String> listaURItrenutnoOdabraneSlike = new ArrayList<>();    // slike koje je korisnik trenutno odabrao
    ArrayList<String> listaURIsveSlike = new ArrayList<>();     // ukoliko korisnik doda nove slike, uz vec postojece
    ArrayList<String> URLslikeBaza = new ArrayList<>();     // URL slika iz Firebase Database
    String sveSlike = "";
    private String key;
    private static final FirebaseDatabase DATABASE = FirebaseDatabase.getInstance();
    private DatabaseReference mDatabase;
    private StorageReference mStorage;
    Integer brojacUploadnihSlika = 0;
    ProgressBar uploadLoadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_odabir_slika);

        uploadLoadingBar = findViewById(R.id.progressBar);
        uploadLoadingBar.bringToFront();

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance().getReference();

        key = DATABASE.getReference().push().getKey();

        // FAB za dodavanje novih slika
        findViewById(R.id.fabAdd).setOnClickListener((View view) -> {
            Pix.start(ViewPagerActivity.this, 100, 5);
        });

        scrollGalleryView = findViewById(R.id.scroll_gallery_view);

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
                     new MaterialDialog.Builder(ViewPagerActivity.this)
                            .title("Potvrda brisanja")
                            .content("Želite li zaista ukloniti sliku?")
                            .positiveText("Ukloni")
                            .negativeText("Odustani")
                            .onPositive((dialog, which) -> {
                                listaURIsveSlike.remove(scrollGalleryView.getCurrentItem());
                                scrollGalleryView.clearGallery();
                                for (String url : listaURIsveSlike) {
                                    infoSlike.add(MediaInfo.mediaLoader(new PicassoImageLoader(url)));
                                    scrollGalleryView.addMedia(MediaInfo.mediaLoader(new PicassoImageLoader(url)));
                                }
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
            case (100): {
                if (resultCode == Activity.RESULT_OK) {
                    listaURItrenutnoOdabraneSlike = data.getStringArrayListExtra(Pix.IMAGE_RESULTS);
                    listaURIsveSlike.addAll(listaURItrenutnoOdabraneSlike);

                    scrollGalleryView
                            .setThumbnailSize(100)
                            .setZoom(true)
                            .setFragmentManager(getSupportFragmentManager());

                    scrollGalleryView.clearGallery();

                    for (String url : listaURIsveSlike) {
                        infoSlike.add(MediaInfo.mediaLoader(new PicassoImageLoader(url)));
                        scrollGalleryView.addMedia(MediaInfo.mediaLoader(new PicassoImageLoader(url)));
                    }

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
                    Pix.start(ViewPagerActivity.this, 100, 5);
                } else {
                    Toast.makeText(ViewPagerActivity.this, "Potrebna je dozvola za pristup fotografijama.", Toast.LENGTH_LONG).show();
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
                        Toast.makeText(ViewPagerActivity.this, "Pogreska prilikom prijenosa slika!", Toast.LENGTH_SHORT).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ViewPagerActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else {
            Snackbar.make(findViewById(android.R.id.content), "Odaberite slike za upload!", Snackbar.LENGTH_SHORT)
                    .setActionTextColor(Color.RED)
                    .show();
        }
    }


    // kreira Toolbar s Buttonom za poslati odabrane slike na Firebase
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_next, menu);
        return true;
    }

    // Toolbar button Send - salje odabrane slike na Firebase
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.next_activity:
                uploadajSlikeUBazuIStorage(listaURIsveSlike);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}