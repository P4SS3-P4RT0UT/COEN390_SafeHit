package com.example.coen390_safehit.view;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.example.coen390_safehit.R;
import com.example.coen390_safehit.controller.DatabaseHelper;
import com.google.firebase.auth.FirebaseAuth;

public class SplashScreenActivity extends AppCompatActivity {
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                SharedPreferences sharedPreferences = getSharedPreferences("checkbox", MODE_PRIVATE);
                mAuth = FirebaseAuth.getInstance();
                if (sharedPreferences.getString("remember", "").equals("true")) {
                    signIn(sharedPreferences.getString("email", ""), sharedPreferences.getString("password", ""), true);
                }
                else{
                        Intent intent = new Intent(SplashScreenActivity.this, SignIn.class);
                        startActivity(intent);
                        finish();
                    }
                }


        },2000);
    }
    void signIn(String emailText, String passwordText, boolean remembered) {
        mAuth.signInWithEmailAndPassword(emailText, passwordText)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Toast.makeText(SplashScreenActivity.this, "Sign in successful", Toast.LENGTH_SHORT).show();
                        checkDatabase(emailText);
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
                Toast.makeText(SplashScreenActivity.this, "Person not found in the database", Toast.LENGTH_SHORT).show();
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
}