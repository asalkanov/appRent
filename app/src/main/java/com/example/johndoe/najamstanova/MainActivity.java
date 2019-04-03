package com.example.johndoe.najamstanova;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.Marker;
import com.google.firebase.FirebaseError;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hbb20.CountryCodePicker;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    CountryCodePicker countryCodePicker;
    String countryCode;
    private static final String TAG = "PhoneAuthActivity";

    private static final String KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress";

    private static final int STATE_INITIALIZED = 1;
    private static final int STATE_CODE_SENT = 2;
    private static final int STATE_VERIFY_FAILED = 3;
    private static final int STATE_VERIFY_SUCCESS = 4;
    private static final int STATE_SIGNIN_FAILED = 5;
    private static final int STATE_SIGNIN_SUCCESS = 6;

    private FirebaseAuth mAuth;

    private boolean mVerificationInProgress = false;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    private DatabaseReference mDatabase;

    private ViewGroup mPhoneNumberViews;
    private ViewGroup mSignedInViews;

    private TextView mStatusText;
    private TextView mDetailText;

    private EditText mPhoneNumberField;
    private EditText mVerificationField;

    private Button mStartButton;
    private Button mVerifyButton;
    private Button mResendButton;
    private Button mSignOutButton;

    Boolean prekinutaRegistracija = false;

    Bundle podaci;

    SharedPreferences stanjeActivitya;      // Notifikacije se ne prikazuju ako je korisnik otvorio PrivateChat u Foregroundu
    SharedPreferences.Editor prefEditor;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            // vrati stanje instance
            if (savedInstanceState != null) {
                onRestoreInstanceState(savedInstanceState);
            }

            Intent intentExtras = getIntent();
            podaci = intentExtras.getExtras();

            if (podaci != null && !podaci.isEmpty() && podaci.containsKey("PREKINIREGISTRACIJU")) {
                if (Objects.requireNonNull(podaci.getString("PREKINIREGISTRACIJU"), "").equals("prekid")) {
                    prekinutaRegistracija = true;
                }
            } else {
                prekinutaRegistracija = false;
            }

            Log.d("prekiunta", String.valueOf(prekinutaRegistracija));

            // na početku aplikacije, korisnik nije ulazio u Private Chat -- PrivateChat nije u Foregroundu -- kako se Notifikacija ne bi poslala kada korisnik primi novu poruku u Chat, ako korisnik ima otvoren PrivateChat
            stanjeActivitya = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            prefEditor = stanjeActivitya.edit();
            prefEditor.putBoolean("isInForeground", false);
            prefEditor.apply();

            countryCodePicker = findViewById(R.id.countryPicker);
            countryCodePicker.setOnCountryChangeListener(() -> countryCode = countryCodePicker.getSelectedCountryCode());

            mStatusText = findViewById(R.id.status);
            mDetailText = findViewById(R.id.detail);

            mPhoneNumberField = findViewById(R.id.phoneNumber);
            mVerificationField = findViewById(R.id.verificationNumber);

            mStartButton = findViewById(R.id.register);
            mVerifyButton = findViewById(R.id.verify);
            mResendButton = findViewById(R.id.resend);
            mSignOutButton = findViewById(R.id.signout);

            mStartButton.setOnClickListener(this);
            mVerifyButton.setOnClickListener(this);
            mResendButton.setOnClickListener(this);
            mSignOutButton.setOnClickListener(this);

            mAuth = FirebaseAuth.getInstance();

            //signOut();

            mDatabase = FirebaseDatabase.getInstance().getReference();

            mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                @Override
                public void onVerificationCompleted(PhoneAuthCredential credential) {
                    // This callback will be invoked in two situations:
                    // 1 - Instant verification. In some cases the phone number can be instantly
                    //     verified without needing to send or enter a verification code.
                    // 2 - Auto-retrieval. On some devices Google Play services can automatically
                    //     detect the incoming verification SMS and perform verification without
                    //     user action.
                    Log.d(TAG, "onVerificationCompleted:" + credential);
                    mVerificationInProgress = false;

                    updateUI(STATE_VERIFY_SUCCESS, credential);     // ažuriraj UI i pokušaj se prijaviti s brojem mobitela
                    signInWithPhoneAuthCredential(credential);
                }

                @Override
                public void onVerificationFailed(FirebaseException e) {     // ako verifikacija nije valjana, primjerice broj mobitela je u pogrešnom formatu
                    Log.d(TAG, "onVerificationFailed", e);
                    mVerificationInProgress = false;

                    Toast.makeText(getApplicationContext(), "Neispravan broj mobitela!", Toast.LENGTH_SHORT).show();

                    if (e instanceof FirebaseAuthInvalidCredentialsException) {
                        mPhoneNumberField.setError("Neispravan broj mobitela.");
                    } else if (e instanceof FirebaseTooManyRequestsException) {     // SMS Quota je premašena
                        Toast.makeText(getApplicationContext(), "Registacijska kvota prekoračena!", Toast.LENGTH_SHORT).show();
                    }
                    updateUI(STATE_VERIFY_FAILED);      // ažuriraj UI i pokaži grešku za verifikaicju
                }

                @Override
                public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {     // poslan je SMS s verif. brojem, pitaj korisnika da unese verif. kôd, izgradi credential iz kôda + verif. ID-a
                    Log.d(TAG, "onCodeSent:" + verificationId);

                    Toast.makeText(getApplicationContext(), "Verifikacijski broj poslan!", Toast.LENGTH_SHORT).show();

                    mVerificationId = verificationId;   // spremi verification ID i ponovno pošalji token za kasniju uporabu
                    mResendToken = token;

                    updateUI(STATE_CODE_SENT);
                }
            };
        }


        @Override
        public void onStart() {
            super.onStart();

            FirebaseUser currentUser = mAuth.getCurrentUser();      // provjeri je li korisnik Signed-in (non-null) i ažuriraj UI
            updateUI(currentUser);

            if (mVerificationInProgress && validatePhoneNumber()) {
                startPhoneNumberVerification(countryCodePicker.getSelectedCountryCodeWithPlus() + mPhoneNumberField.getText().toString());
                Toast.makeText(getApplicationContext(), countryCodePicker.getSelectedCountryCodeWithPlus() + mPhoneNumberField.getText().toString(), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putBoolean(KEY_VERIFY_IN_PROGRESS, mVerificationInProgress);
        }

        @Override
        protected void onRestoreInstanceState(Bundle savedInstanceState) {
            super.onRestoreInstanceState(savedInstanceState);
            mVerificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS);
        }

        private void startPhoneNumberVerification(String phoneNumber) {
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    phoneNumber,        // broj mobitela
                    60,                 // Timeout
                    TimeUnit.SECONDS,
                    this,
                    mCallbacks);        // OnVerificationStateChangedCallbacks

            mVerificationInProgress = true;
        }

        private void verifyPhoneNumberWithCode(String verificationId, String code) {
            try {
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);     // provjeri kôd
                signInWithPhoneAuthCredential(credential);
            } catch (Exception e) {
                Log.d("ne valja", e.toString());
                Toast.makeText(getApplicationContext(), "Pogrešan verifikacijski broj!", Toast.LENGTH_SHORT).show();
            }
        }


        private void resendVerificationCode(String phoneNumber, PhoneAuthProvider.ForceResendingToken token) {      // ponovno pošalji verifikacijski kôd
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    phoneNumber,
                    60,
                    TimeUnit.SECONDS,
                    this,
                    mCallbacks,
                    token);

        }


        private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {        // prijavi se pomoću broja mobitela
            mAuth.signInWithCredential(credential)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {       // Sign-in uspio, ažuriraj UI s informacijama prijavljenog korisnika
                            Log.d(TAG, "signInWithCredential:success");
                            //Toast.makeText(getApplicationContext(), "signInWithCredential:success", Toast.LENGTH_SHORT).show();
                            FirebaseUser user = task.getResult().getUser();
                            updateUI(STATE_SIGNIN_SUCCESS, user);
                        } else {
                            // Sign-in nije uspio, pokaži grešku i ažuriraj UI
                            Log.d(TAG, "signInWithCredential:failure", task.getException());
                            //Toast.makeText(getApplicationContext(), "signInWithCredential:failure", Toast.LENGTH_SHORT).show();
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {        // verif. kôd nije valjan
                                mVerificationField.setError("Invalid code.");
                            }
                            updateUI(STATE_SIGNIN_FAILED);
                        }
                    });
        }


        private void signOut() {
            mAuth.signOut();
            updateUI(STATE_INITIALIZED);
        }

        private void updateUI(int uiState) {
            updateUI(uiState, mAuth.getCurrentUser(), null);
        }

        private void updateUI(FirebaseUser user) {
            if (user != null) {
                updateUI(STATE_SIGNIN_SUCCESS, user);
            } else {
                updateUI(STATE_INITIALIZED);
            }
        }

        private void updateUI(int uiState, FirebaseUser user) {
            updateUI(uiState, user, null);
        }

        private void updateUI(int uiState, PhoneAuthCredential cred) {
            updateUI(uiState, null, cred);
        }

        private void updateUI(int uiState, FirebaseUser user, PhoneAuthCredential cred) {
            switch (uiState) {
                case STATE_INITIALIZED:   // faza inicijalizacije, pokaži samo EditText za unos broja mobitela
                    mDetailText.setText(null);
                    break;
                case STATE_CODE_SENT:  // poslan je verif. kôd, pokaži EdiText za unos verif. kôda
                    break;
                case STATE_VERIFY_FAILED:         // verif. nije uspjela, pokaži sve opcije
                    break;
                case STATE_VERIFY_SUCCESS:        // verif. je uspjela, nastavi na Firebase Sign-in
                    if (cred != null) {         // postavi verif. tekst u EditText, ovisno o credentialu
                        if (cred.getSmsCode() != null) {
                            mVerificationField.setText(cred.getSmsCode());
                        } else {
                        }
                    }
                    break;
                case STATE_SIGNIN_FAILED:
                    break;
                case STATE_SIGNIN_SUCCESS:
                    break;
            }

            /* PROVJERA JE LI KORISNIK VEC RESGISTRIRAN ILI NIJE - AKO JEST, POSALJI GA NA REGISTRACIJU (NAJMODAVAC ILI NAJMOPRIMAC) */
            if (user == null) {     // korisnik je Signed-out
                //Toast.makeText(getApplicationContext(), "Signed out", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Signed-out!");
            } else {    // korisnik je Signed-in
                mPhoneNumberField.setText(null);
                mVerificationField.setText(null);
                mDetailText.setText( user.getUid());
                mStatusText.setText("Signed-in");
                Log.d(TAG, "Signed-in!");

                if (user.getDisplayName() != null && !user.getDisplayName().equals("") && !user.getDisplayName().trim().equals("")) {      // ako user nema DisplayName, znaci da još NIJE REGISTRIRAN - posalji ga da se registrira!
                    Toast.makeText(getApplicationContext(), "Dobrodošli, " + user.getDisplayName(), Toast.LENGTH_LONG).show();
                    Log.d(TAG, "Ima ime: " + user.getDisplayName());
                    Intent listaStanova = new Intent(MainActivity.this, ListaStanova.class);
                    startActivity(listaStanova);
                } else {
                    Toast.makeText(getApplicationContext(), "Niste registrirani!", Toast.LENGTH_SHORT).show();
                    Log.d("prekiunta", String.valueOf(prekinutaRegistracija));
                    Log.d(TAG, "Nema ime");
                   // if (!prekinutaRegistracija) {
                        Intent registracija = new Intent(MainActivity.this, Registracija.class);
                        startActivity(registracija);
                   // }
                }
            }
        }

        private boolean validatePhoneNumber() {
            String phoneNumber = countryCodePicker.getSelectedCountryCodeWithPlus() + mPhoneNumberField.getText().toString();
            if (TextUtils.isEmpty(phoneNumber)) {
                mPhoneNumberField.setError("Broj nije valjan.");
                return false;
            }
            return true;
        }



        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.register:
                    if (mPhoneNumberField.getText().toString().equals("")) {
                        Toast.makeText(getApplicationContext(), "Unesite broj mobitela!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!validatePhoneNumber()) {
                        return;
                    }
                    startPhoneNumberVerification(countryCodePicker.getSelectedCountryCodeWithPlus() + mPhoneNumberField.getText().toString());
                    //Toast.makeText(getApplicationContext(), countryCodePicker.getSelectedCountryCodeWithPlus() + mPhoneNumberField.getText().toString(), Toast.LENGTH_SHORT).show();
                    break;
                case R.id.verify:
                    String code = mVerificationField.getText().toString();
                    if (TextUtils.isEmpty(code)) {
                        mVerificationField.setError("Ne može biti prazno.");
                        return;
                    }
                    verifyPhoneNumberWithCode(mVerificationId, code);
                    break;
                case R.id.resend:
                    resendVerificationCode(countryCodePicker.getSelectedCountryCodeWithPlus() + mPhoneNumberField.getText().toString(), mResendToken);
                    break;
                case R.id.signout:
                    signOut();
                    break;
            }
        }


    }

