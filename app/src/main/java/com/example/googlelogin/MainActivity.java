package com.example.googlelogin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    static final int GOOGLE_SIGN_IN = 123;
    FirebaseAuth mAuth;
    Button login, logout;
    TextView textView;
    ImageView imageView;
    ProgressBar progressBar;
    GoogleSignInClient mGoogleSigninClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        login = findViewById(R.id.loginbutton);
        logout = findViewById(R.id.logoutbutton);
        imageView = findViewById(R.id.imageview);
        progressBar = findViewById(R.id.progress_circular);
        textView = findViewById(R.id.datas);

        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions
                .Builder()
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSigninClient = GoogleSignIn.getClient(this,googleSignInOptions);


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                signInGoogle();
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Userlogout();
            }
        });

        // below used for check user login or not if login we show login information only
        if (mAuth.getCurrentUser() != null)
        {
            FirebaseUser user = mAuth.getCurrentUser();
            updateUI(user);
        }

    }

    void signInGoogle()
    {
        progressBar.setVisibility(View.VISIBLE);
        Intent signInIntent = mGoogleSigninClient.getSignInIntent();
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GOOGLE_SIGN_IN)
        {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try
            {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null)
                {
                    // call method from below
                    firebaseAuthWithGoogle(account);
                }

            }
            catch (ApiException e)
            {
                e.printStackTrace();
            }

        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {

        AuthCredential credential = GoogleAuthProvider
                .getCredential(account.getIdToken(),null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful())
                {
                    progressBar.setVisibility(View.GONE);
                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                    // this below method created for maintain UI by user current id
                    updateUI(firebaseUser);

                }
                else
                {
                    progressBar.setVisibility(View.GONE);
                    updateUI(null);
                }
            }
        });

    }

    private void updateUI(FirebaseUser firebaseUser) {

        if (firebaseUser!= null)
        {
            String name = firebaseUser.getDisplayName();
            String email = firebaseUser.getEmail();
            String photo = String.valueOf(firebaseUser.getPhotoUrl());

            textView.append("Information"+"\n\n");
            textView.append(name +"\n");
            textView.append(email +"\n");
            Picasso.get().load(photo).into(imageView);
            login.setVisibility(View.GONE);
            logout.setVisibility(View.VISIBLE);
        }
        else
        {
            textView.setText("Firebase Login");
            Picasso.get().load(R.mipmap.ic_launcher_round).into(imageView);
            login.setVisibility(View.VISIBLE);
            logout.setVisibility(View.GONE);
        }

    }

    void Userlogout()
    {
        FirebaseAuth.getInstance().signOut();
        // THIS below method will be used for maintain ui if logout
        mGoogleSigninClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                // here i call my update ui method & set null to remove all user information fetch from google account
                updateUI(null);
            }
        });
    }
}
