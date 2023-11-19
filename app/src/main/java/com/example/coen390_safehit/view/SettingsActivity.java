package com.example.coen390_safehit.view;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.coen390_safehit.R;

public class SettingsActivity extends AppCompatActivity {

    // Person id to identify user
    private String uid;
    private String type;
    // Toolbar for back navigation
    private Toolbar toolbar;

    private Button scan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        // Setup the toolbar
        setupToolBar();
        // Fetch the id from the previous activity
        uid = getIntent().getStringExtra("pid");
        type= getIntent().getStringExtra("type");
        scan = findViewById(R.id.scan);

        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAttachDeviceClicked();
            }
        });


    }

    public void onLogOutClicked(View view) {
        goToSignIn();
    }

    public void onDeleteAccountClicked(View view) {
        // Delete account from database
    }
    public void onAttachDeviceClicked(){
        Intent intent  = new Intent(getApplicationContext(), ScanningActivity.class);
        startActivity(intent);
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
        // Redirect user back to their main page
        toolbar.setNavigationOnClickListener(view -> goBackToMainPage(type, uid));
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

    public void goBackToMainPage(String type, String pid) {
        switch (type) {
            // Go back to coach main page
            case "Coach":
                Intent coachProfile = new Intent(getApplicationContext(), CoachProfileActivity.class);
                startActivity(coachProfile);
                break;
                // Go back to player main page
            case "Player":
                Intent playerProfile = new Intent(getApplicationContext(), PlayerProfileActivity.class);
                startActivity(playerProfile);
                break;
            case "Trainer":
                // Empty
                break;
            default:
                Log.d("SETTINGS EXCEPTION", "Error trying to go back to user main page");
        }
    }
}
