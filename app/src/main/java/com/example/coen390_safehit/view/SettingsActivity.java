package com.example.coen390_safehit.view;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.coen390_safehit.R;

public class SettingsActivity extends AppCompatActivity {

    // Person id to identify user
    private String uid;
    private String type;
    // Toolbar for back navigation
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        // Setup the toolbar
        setupToolBar();
        // Fetch the id from the previous activity
        uid = getIntent().getStringExtra("pid");
        type= getIntent().getStringExtra("type");
    }

    public void onLogOutClicked(View view) {
        goToSignIn();
    }
    public void onDeleteAccountClicked(View view) {
        // Delete account from database
    }
    public void onUpdateInfoClicked(View view) {
        goToPersonalInformation();
    }
    private void setupToolBar() {
        toolbar = findViewById(R.id.toolbarSettings);
        toolbar.setTitle("Settings");
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        //Return to previous page
        toolbar.setNavigationOnClickListener(v -> finish());
    }
    public void goToSignIn() {
        Intent intent = new Intent(getApplicationContext(), SignIn.class);
        startActivity(intent);
    }
    public void goToPersonalInformation() {
        Intent intent = new Intent(getApplicationContext(), UpdateInformationActivity.class);
        intent.putExtra("pid", uid);
        intent.putExtra("type",type);
        startActivity(intent);
    }
}
