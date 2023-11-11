package com.example.coen390_safehit.view;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.coen390_safehit.R;

public class SettingsActivity extends AppCompatActivity {

    // Person id to identify user
    private int uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        // Fetch the id from the previous activity
        int defaultValue = -1;
        uid = getIntent().getIntExtra("pid", defaultValue);
    }

    public void onLogOutClicked(View view) {
        goToSignIn();
    }

    public void onDeleteAccountClicked(View view) {
        // Delete account from database
    }

    public void goToSignIn() {
        Intent intent = new Intent(getApplicationContext(), SignIn.class);
        startActivity(intent);
    }
}