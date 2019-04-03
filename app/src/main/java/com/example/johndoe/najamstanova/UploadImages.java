package com.example.johndoe.najamstanova;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class UploadImages extends AppCompatActivity {

    private static final int RESULT_LOAD_IMAGE = 1;
    private StorageReference mStorage;
    private List<Uri> fileNameList;
    private List<String> fileDoneList;
    private List<String> fileURLList;
    private RecyclerView recyclerView;
    private UploadImagesAdapter UploadImagesAdapter;
    private String key;
    private static final FirebaseDatabase DATABASE = FirebaseDatabase.getInstance();
    private DatabaseReference mDatabase;
    String sveSlike = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prikaz_slika_upload);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance().getReference();

        key = DATABASE.getReference().push().getKey();

        fileNameList = new ArrayList<>();
        fileDoneList = new ArrayList<>();
        fileURLList = new ArrayList<>();

        UploadImagesAdapter = new UploadImagesAdapter(this, fileNameList, fileDoneList, fileURLList);

        //RecyclerView
        recyclerView = findViewById(R.id.recycler);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(UploadImagesAdapter);
        // add pager behavior
        PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);

        ImageButton odaberiSlike = findViewById(R.id.select_btn);
        odaberiSlike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Select Picture"), RESULT_LOAD_IMAGE);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK){

            if(data.getClipData() != null){     // znaci da je odabrano vise od 1 slike
                int totalItemsSelected = data.getClipData().getItemCount();

                if (fileURLList.size() <= 0) {      // korisnik prvi puta dodaje slike

                    for (int i = 0; i < totalItemsSelected; i++) {
                        Uri fileUri = data.getClipData().getItemAt(i).getUri();
                        fileNameList.add(fileUri);

                        final StorageReference fileToUpload = mStorage.child(new StringBuilder("Images/").append(UUID.randomUUID().toString()).toString());
                        UploadTask uploadTask = fileToUpload.putFile(fileUri);

                        Task<Uri> urlTask = uploadTask.continueWithTask(task -> {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }
                            return fileToUpload.getDownloadUrl();
                        }).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Uri downloadUri = task.getResult();
                                fileURLList.add(downloadUri.toString());
                                sveSlike = sveSlike + downloadUri.toString() + "$";   // sve slike sprema u jedan string URL-ova razdvojen znakom $
                                UploadImagesAdapter.notifyDataSetChanged();

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
                                Toast.makeText(UploadImages.this, "Pogreska uploada!", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(UploadImages.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                } else {    // korisnik je vec odabrao slike, dodaje nove uz postojece

                    for (int i = fileURLList.size(); i <  fileURLList.size() + totalItemsSelected ; i++) {
                        Log.d("logos2", String.valueOf(i - fileURLList.size()));
                        Uri fileUri = data.getClipData().getItemAt(i - fileURLList.size()).getUri();
                        fileNameList.add(fileUri);

                        final StorageReference fileToUpload = mStorage.child(new StringBuilder("Images/").append(UUID.randomUUID().toString()).toString());
                        UploadTask uploadTask = fileToUpload.putFile(fileUri);

                        final int finalI = i;
                        Task<Uri> urlTask = uploadTask.continueWithTask(task -> {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }
                            return fileToUpload.getDownloadUrl();
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {
                                    Uri downloadUri = task.getResult();
                                    fileURLList.add(downloadUri.toString());
                                    sveSlike = sveSlike + downloadUri.toString() + "$";   // sve slike sprema u jedan string URL-ova razdvojen znakom $
                                    UploadImagesAdapter.notifyDataSetChanged();

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
                                    Toast.makeText(UploadImages.this, "Pogreska uploada!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(UploadImages.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
                //Toast.makeText(MainActivity.this, "Selected Multiple Files", Toast.LENGTH_SHORT).show();
            } else if (data.getData() != null){
                Toast.makeText(UploadImages.this, "Selected Single File", Toast.LENGTH_SHORT).show();
            }

        }
    }


    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

}
