package com.example.coen390_safehit.view;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.coen390_safehit.controller.DatabaseHelper;
import com.example.coen390_safehit.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignIn extends AppCompatActivity {

    // Layout elements
    TextInputEditText email, password;
    Button signIn, createAccount;
    ProgressBar progressBar;
    FirebaseAuth mAuth;
    CheckBox rememberMe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        // Setup layout elements
        setupEditTextFields();
        setupAuthentication();
        setupButtons();
        setupProgressBar();
    }

    @Override
    public void onStart() {
        super.onStart();
        SharedPreferences sharedPreferences = getSharedPreferences("checkbox", MODE_PRIVATE);
        FirebaseUser user = mAuth.getCurrentUser();
        if (sharedPreferences.getString("remember", "").equals("true"))
            signIn(sharedPreferences.getString("email", ""), sharedPreferences.getString("password", ""), true);
        if (user != null) {
            // Go to profile/main page
        }
    }

    void setupEditTextFields() {
        // Email
        email = findViewById(R.id.editThreshold);
        // Password
        password = findViewById(R.id.editTextSignInPassword);
    }

    void setupAuthentication() {
        mAuth = FirebaseAuth.getInstance();
    }

    void setupButtons() {
        // Sign in button
        rememberMe = findViewById(R.id.rememberme);
        signIn = findViewById(R.id.signInButton);
        signIn.setOnClickListener(view -> {
            if (validEmailAndPassword()) {
                progressBar.setVisibility(View.VISIBLE);
                email.setEnabled(false);
                password.setEnabled(false);
                signIn.setVisibility(View.GONE);
                createAccount.setVisibility(View.GONE);
                // Connect to account
                signIn(email.getText().toString().toLowerCase(), String.valueOf(password.getText()), false);
            }
        });
        // Create an account button
        createAccount = findViewById(R.id.createAnAccountButton);
        createAccount.setOnClickListener(view -> {
            goToSignUp();
        });
    }

    void signIn(String emailText, String passwordText, boolean remembered) {
        mAuth.signInWithEmailAndPassword(emailText, passwordText)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        if (remembered) {

                        } else if (rememberMe.isChecked()) {
                            // Save email and password
                            SharedPreferences sharedPreferences = getSharedPreferences("checkbox", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("remember", "true");
                            editor.putString("email", emailText);
                            editor.putString("password", passwordText);

                            editor.apply();
                        } else {
                            // Remove email and password
                            SharedPreferences sharedPreferences = getSharedPreferences("checkbox", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("remember", "false");
                            editor.putString("email", "");
                            editor.putString("password", "");
                            editor.apply();
                        }
                        // Sign in success, update UI with the signed-in user's information
                        Toast.makeText(SignIn.this, "Sign in successful", Toast.LENGTH_SHORT).show();
                        checkDatabase(emailText);
                    } else {
                        // If sign in fails, display a message to the user
                        signIn.setVisibility(View.VISIBLE);
                        createAccount.setVisibility(View.VISIBLE);
                        email.setEnabled(true);
                        password.setEnabled(true);
                        Toast.makeText(SignIn.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    void checkDatabase(String emailText) {
        DatabaseHelper db = DatabaseHelper.getInstance(this);
        db.getPersonFromEmail(emailText, new DatabaseHelper.FetchCallback() {
            @Override
            public void onComplete() {
                if (DatabaseHelper.personType.equals("Coach") || DatabaseHelper.personType.equals("Trainer"))
                    goToCoachProfile();
                else if (DatabaseHelper.personType.equals("Player"))
                    goToPlayerProfile();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(SignIn.this, "Person not found in the database", Toast.LENGTH_SHORT).show();
            }
        });
    }


    void goToCoachProfile() {
        Intent coachProfile = new Intent(getApplicationContext(), CoachProfileActivity.class);
        startActivity(coachProfile);
    }

    void goToPlayerProfile() {
        Intent playerProfile = new Intent(getApplicationContext(), PlayerProfileActivity.class);
        startActivity(playerProfile);
    }

    void setupProgressBar() {
        progressBar = findViewById(R.id.progressBar);
    }

    boolean validEmailAndPassword() {
        if (TextUtils.isEmpty(email.getText()) || TextUtils.isEmpty(password.getText())) {
            showToast();
            return false;
        } else return true;
    }

    void showToast() {
        Toast.makeText(SignIn.this, "Please fill out all required fields", Toast.LENGTH_SHORT).show();
    }

    void goToSignUp() {
        Intent signUp = new Intent(getApplicationContext(), SignUp.class);
        startActivity(signUp);
    }
}