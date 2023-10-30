package com.example.coen390_safehit;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignIn extends AppCompatActivity {

    // Layout elements
    TextInputEditText email, password;
    Button signIn, createAccount;
    ProgressBar progressBar;
    FirebaseAuth mAuth;

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
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Go to profile/main page
        }
    }

    void setupEditTextFields() {
        // Email
        email = findViewById(R.id.editTextSignInEmail);
        // Password
        password = findViewById(R.id.editTextSignInPassword);
    }

    void setupAuthentication() {
        mAuth = FirebaseAuth.getInstance();
    }

    void setupButtons() {
        // Sign in button
        signIn = findViewById(R.id.signInButton);
        signIn.setOnClickListener(view -> {
            if (validEmailAndPassword()) {
                progressBar.setVisibility(View.VISIBLE);
                // Connect to account
                mAuth.signInWithEmailAndPassword(String.valueOf(email.getText()), String.valueOf(password.getText()))
                        .addOnCompleteListener(this, task -> {
                            progressBar.setVisibility(View.GONE);
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Toast.makeText(SignIn.this, "Sign in successful", Toast.LENGTH_SHORT).show();
                                checkDatabase();
                            } else {
                                // If sign in fails, display a message to the user.
                                Toast.makeText(SignIn.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
        // Create an account button
        createAccount = findViewById(R.id.createAnAccountButton);
        createAccount.setOnClickListener(view -> {
            goToSignUp();
        });
    }

    void checkDatabase() {
        Database db = Database.getInstance(this);
        db.getPersonFromEmail(email.getText().toString(), new Database.FetchCallback() {
            @Override
            public void onComplete() {
                if (Database.personType.equals("Coach") || Database.personType.equals("Trainer"))
                    goToCoachProfile();
                else if (Database.personType.equals("Player"))
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