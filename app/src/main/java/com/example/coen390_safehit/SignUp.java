package com.example.coen390_safehit;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;


import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUp extends AppCompatActivity {

    TextInputEditText email, password;
    Button signUp;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        setupAuthentication();
        setupEditTextFields();
        setupButtons();
    }

    void setupEditTextFields() {
        email = findViewById(R.id.editTextSignUpEmail);
        password = findViewById(R.id.editTextSignUpPassword);
    }

    void setupButtons() {
        signUp = findViewById(R.id.signUpButton);
        signUp.setOnClickListener(view -> {
            if(validEmailAndPassword()){
                mAuth.createUserWithEmailAndPassword(String.valueOf(email.getText()), String.valueOf(password.getText()))
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                FirebaseUser user = mAuth.getCurrentUser();
                                Toast.makeText(SignUp.this, "Account created successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                // If sign in fails, display a message to the user.
                                Toast.makeText(SignUp.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    void setupAuthentication() {
        mAuth = FirebaseAuth.getInstance();
    }

    boolean validEmailAndPassword() {
        if (TextUtils.isEmpty(email.getText()) || TextUtils.isEmpty(password.getText())){
            showToast();
            return false;
        } else return true;
    }

    void showToast() {
        Toast.makeText(SignUp.this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
    }
}