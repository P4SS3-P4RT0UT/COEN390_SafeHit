package com.example.coen390_safehit;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

public class SignIn extends AppCompatActivity {

    // Layout elements
    TextInputEditText email, password;
    Button signIn, createAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        // Setup layout elements
        setupEditTextFields();
        setupButtons();
    }

    void setupEditTextFields(){
        // Email
        email = findViewById(R.id.editTextSignInEmail);
        // Password
        password = findViewById(R.id.editTextSignInPassword);
    }

    void setupButtons(){
        // Sign in button
        signIn = findViewById(R.id.signInButton);
        signIn.setOnClickListener(view -> {
            if(validEmailAndPassword()){
                // Do something
            }
        });
        // Create an account button
        createAccount = findViewById(R.id.createAnAccountButton);
        createAccount.setOnClickListener(view -> {

        });
    }

    boolean validEmailAndPassword() {
        if(TextUtils.isEmpty(email.getText()) || TextUtils.isEmpty(password.getText())) {
            showToast();
            return false;
        } else return true;
    }

    void showToast() {
        Toast.makeText(SignIn.this, "Please fill out all required fields", Toast.LENGTH_SHORT).show();
    }
}