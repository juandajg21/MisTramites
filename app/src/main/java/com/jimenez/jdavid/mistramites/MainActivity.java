package com.jimenez.jdavid.mistramites;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    TextView mStatusUser, tEstado;
    EditText eUser, ePass;
    Button signOutButton, bRegister;
    String TAG = "MainActivity";
    private static final int RC_SIGN_IN=9001;
    private GoogleApiClient mGoogleApiClient;
    CallbackManager callbackManager;

    String aux, aux2, nomface;

    private static final String FIREBASE_URL="https://mistramites.firebaseio.com/";
    Firebase firebd;
    private Firebase firedatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        tEstado = (TextView) findViewById(R.id.tEstado);
        mStatusUser = (TextView) findViewById(R.id.mStatusUser);
        eUser = (EditText) findViewById(R.id.eUser);
        ePass = (EditText) findViewById(R.id.ePass);
        bRegister = (Button)findViewById(R.id.bRegister);
        signOutButton = (Button) findViewById(R.id.id_sign_out_button);
        SignInButton signInButton = (SignInButton) findViewById(R.id.id_sign_in_button);


        Firebase.setAndroidContext(this);
        firedatabase = new Firebase(FIREBASE_URL);

        signOutButton.setOnClickListener(this);
        signInButton.setOnClickListener(this);
        bRegister.setOnClickListener(this);

        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {
                Profile perfil = com.facebook.Profile.getCurrentProfile();
                //String nomface2 = perfil.getName();
                //Toast.makeText(MainActivity.this, nomface2, Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onCancel() {
                Toast.makeText(MainActivity.this, "Se canceló", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }

        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.id_sign_in_button:
                aux=ePass.getText().toString();
                if((aux == null)||(aux.isEmpty())){
                    Toast.makeText(MainActivity.this, "Es obligatorio ingresar el código único", Toast.LENGTH_LONG).show();
                }
                else{
                    Sign_in();
                    eUser.setFocusable(false);
                }

                break;
            case R.id.id_sign_out_button:
                Sign_out();
                eUser.setFocusable(false);
                eUser.setText(null);
                ePass.setText(null);
                tEstado.setText(null);

                break;

            case R.id.bRegister:
                aux2=null;
                aux2=eUser.getText().toString();
                if((aux2 == null)||(aux2.isEmpty())){
                    Toast.makeText(MainActivity.this, "Es obligatorio ingresar el Nombre de Usuario", Toast.LENGTH_LONG).show();
                }
                else{
                    if((aux == null)||(aux.isEmpty())){
                        Toast.makeText(MainActivity.this, "Es obligatorio ingresar el código único", Toast.LENGTH_LONG).show();
                    }
                    else{
                        aux=ePass.getText().toString();
                        firebd = firedatabase.child(aux);
                        aux2=eUser.getText().toString();
                        firedatabase.addValueEventListener(new ValueEventListener(){
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.child(aux).exists()){
                                    tEstado.setText(dataSnapshot.child(aux).getValue().toString());

                                    Map<String, Object> nuevoTram = new HashMap<>();
                                    nuevoTram.put("Usuario", aux2);
                                    firebd.updateChildren(nuevoTram);
                                }
                                else{
                                    Toast.makeText(MainActivity.this, "Este código no ha sido registrado",Toast.LENGTH_SHORT).show();
                                }
                            }
                            @Override
                            public void onCancelled(FirebaseError firebaseError) {

                            }
                        });
                    }

                }
        }
    }

    private void Sign_in() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void Sign_out() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        updateUI(false);

                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        callbackManager.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {

        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();
            mStatusUser.setText(getString(R.string.signed_in_fmt, acct.getDisplayName()));
            eUser.setText(getString(R.string.signed_in_fmt2,acct.getDisplayName()));
            ePass.requestFocus();
            updateUI(true);
        }
        else {
            updateUI(false);
        }
    }

    private void updateUI(boolean signedIn) {
        if(signedIn){
            Toast.makeText(MainActivity.this, "In",Toast.LENGTH_SHORT).show();
            findViewById(R.id.id_sign_in_button).setVisibility(View.GONE);
            findViewById(R.id.id_sign_out_button).setVisibility(View.VISIBLE);
        }
        else {
            Toast.makeText(MainActivity.this, "Out",Toast.LENGTH_SHORT).show();
            findViewById(R.id.id_sign_in_button).setVisibility(View.VISIBLE);
            findViewById(R.id.id_sign_out_button).setVisibility(View.GONE);
            mStatusUser.setText("...");
        }

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG,"onConnectionFailed"+connectionResult);
    }

    @Override
    protected void onStart() {
        super.onStart();
        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if(opr.isDone()){
            Log.d(TAG, "Sign-In");
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        }
        else {
            Log.d(TAG, "Nadie logeado");
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                    handleSignInResult(googleSignInResult);
                }
            });
        }
    }



}