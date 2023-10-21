package com.example.coen390_safehit;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

public class SignUp extends AppCompatActivity {

    TextInputEditText email, password;
    Button signUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
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

            }
        });
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