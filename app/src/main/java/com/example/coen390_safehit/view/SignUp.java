package com.example.coen390_safehit.view;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;


import com.example.coen390_safehit.controller.DatabaseHelper;
import com.example.coen390_safehit.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUp extends AppCompatActivity {
    TextInputEditText email, password;
    Button signUp, backButton;
    FirebaseAuth mAuth;
    ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        setupEditTextFields();
        setupAuthentication();
        setupButtons();
        setupProgressBar();
    }

    @Override
    public void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
        signUp.setVisibility(View.VISIBLE);

    }

    void setupEditTextFields() {
        email = findViewById(R.id.editTextSignUpEmail);
        password = findViewById(R.id.editTextSignUpPassword);
    }

    void setupButtons() {
        backButton = findViewById(R.id.backButton2);
        backButton.setOnClickListener(v -> finish());

        signUp = findViewById(R.id.signUpButton);
        signUp.setOnClickListener(view -> {
            if (validEmailAndPassword()) {
                progressBar.setVisibility(View.VISIBLE);
                signUp.setVisibility(View.GONE);
                mAuth.createUserWithEmailAndPassword(String.valueOf(email.getText()), String.valueOf(password.getText()))
                        .addOnCompleteListener(this, task -> {
                            progressBar.setVisibility(View.GONE);
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                FirebaseUser user = mAuth.getCurrentUser();
                                Toast.makeText(SignUp.this, "Account created successfully", Toast.LENGTH_SHORT).show();
                                //Go to the next page for sign up information
                                goToSignUpInformation();
                            } else {
                                signUp.setVisibility(View.VISIBLE);
                                // If sign in fails, display a message to the user.
                                Toast.makeText(SignUp.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    void goToSignUpInformation() {
        Intent signUpInformation = new Intent(getApplicationContext(), SignUpInformation.class);
        DatabaseHelper.getInstance(this);
        DatabaseHelper.email = String.valueOf(email.getText());
        startActivity(signUpInformation);
    }


    void setupProgressBar() {
        progressBar = findViewById(R.id.progressBar);
    }

    void setupAuthentication() {
        mAuth = FirebaseAuth.getInstance();
    }

    boolean validEmailAndPassword() {
        if (TextUtils.isEmpty(email.getText()) || TextUtils.isEmpty(password.getText())) {
            showToast();
            return false;
        } else return true;
    }

    void showToast() {
        Toast.makeText(SignUp.this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
    }

}