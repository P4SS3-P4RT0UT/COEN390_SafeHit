package com.example.coen390_safehit.view;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.example.coen390_safehit.R;
import com.example.coen390_safehit.controller.DatabaseHelper;
import com.example.coen390_safehit.model.Position;
import com.google.android.material.textfield.TextInputEditText;

public class UpdateInformationActivity extends AppCompatActivity {
    AutoCompleteTextView  teamDropdown, positionDropdown;
    LinearLayout coachLayout, playerLayout, trainerLayout;
    Button saveButton;
    TextInputEditText firstName, lastName;
    TextInputEditText teamName;
    TextInputEditText number;
    ProgressBar progressBar;
    DatabaseHelper db = DatabaseHelper.getInstance(this);

    String position;
    String uid;
    String type;

    private Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_information);
        uid = getIntent().getStringExtra("pid");
        type = getIntent().getStringExtra("type");
        firstName= findViewById(R.id.firstName);
        db.firstName = firstName;
        lastName = findViewById(R.id.lastName);
        db.lastName = lastName;
        setupToolBar();
        // Setup all user layouts (player, coach, trainer)
        setupUserLayouts();

        // For first name, last name, and type
        setupGeneralUserInfo();

        // Setup the progress bar
        progressBar = findViewById(R.id.progressBar);

        // Setup the sign up button
        setupSaveButton();
    }


    private void loadPersonInfo() {
        db.getPersonInfoFromPlayerID(uid);
    }

    private void loadPlayerInformation() {
        db.getPlayerInformationFromPlayerID(uid);
    }
    // To setup all possible user layouts
    public void setupUserLayouts() {
        // Layout of coach, player, and trainer
        coachLayout = findViewById(R.id.coach_layout);
        playerLayout = findViewById(R.id.player_layout);
        trainerLayout = findViewById(R.id.trainer_layout);
    }

    // To setup layout fields available to all users
    public void setupGeneralUserInfo() {
        // Input text for first name, last name

        loadPersonInfo();
        setupTypeLayout();
    }
    private void setupToolBar() {
        toolbar = findViewById(R.id.toolbarinfo);
        toolbar.setTitle("User Information");
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        //Return to previous page
        toolbar.setNavigationOnClickListener(v -> finish());

    }

    // To enable the user type selection
    private void setupTypeLayout() {
            // Set the layout visibility according to the type selection
            if (type.equals("Coach")) {
                // Show coach layout and enable fields specific to user type
                coachLayout.setVisibility(LinearLayout.VISIBLE);
                enableCoachReservedFields();
                // Hide other layouts
                playerLayout.setVisibility(LinearLayout.GONE);
                trainerLayout.setVisibility(LinearLayout.GONE);
            } else if (type.equals("Player")) {
                // Show player layout and enable fields specific to user type
                playerLayout.setVisibility(LinearLayout.VISIBLE);
                enablePlayerReservedFields();
                // Hide other layouts
                coachLayout.setVisibility(LinearLayout.GONE);
                trainerLayout.setVisibility(LinearLayout.GONE);
            } else if (type.equals("Trainer")) {
                // Show trainer layout and enable fields specific to user type
                trainerLayout.setVisibility(LinearLayout.VISIBLE);
                enableTrainerReservedFields();
                // Hide other layouts
                coachLayout.setVisibility(LinearLayout.GONE);
                playerLayout.setVisibility(LinearLayout.GONE);
            }
    }
    // To setup the sign up button
    public void setupSaveButton() {
        // Setup the sign up button
        saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(view -> {
            // Check for valid inputs
            if(validUserInput()) {
                // Show progress bar
                progressBar.setVisibility(View.VISIBLE);
                // Add the user to the database
                switch (type) {
                    case "Coach":
                        db.updateCoach(firstName.getText().toString(), lastName.getText().toString(), uid, teamName.getText().toString());
                        break;
                    case "Player":
                        db.teamsList.get(teamDropdown.getText().toString());
                        db.updatePerson(firstName.getText().toString(), lastName.getText().toString(), uid);
                        db.updatePlayer(number.getText().toString(), positionDropdown.getText().toString(), db.teamsList.get(teamDropdown.getText().toString()), uid);
                        break;
                    case "Trainer":
                        db.updatePerson(firstName.getText().toString(), lastName.getText().toString(), uid);
                        break;
                    default:
                        Log.d("SIGN UP EXCEPTION", "Error trying to determine user type (setupSignUpButton())");
                }
            }
        });
    }

    // To check if all user inputs are valid
    public boolean validUserInput() {
        // For all user types, check first name, last name, and user type
        if(TextUtils.isEmpty(firstName.getText()) || TextUtils.isEmpty(lastName.getText()) || TextUtils.isEmpty(type)) {
            showToast("Please fill all required fields");
            return false;
        }
        switch(type) {
            case "Coach":
                // Check for team name
                if(TextUtils.isEmpty(teamName.getText())) {
                    showToast("Please enter a team name");
                    return false;
                }
                break;
            case "Player":
                // Check for team name, position, and number
                if(TextUtils.isEmpty(teamDropdown.getText()) || TextUtils.isEmpty(positionDropdown.getText()) || TextUtils.isEmpty(number.getText())) {
                    showToast("Please fill all required fields");
                    return false;
                }
                break;
            case "Trainer":
                // Check for team name
                if(TextUtils.isEmpty(teamDropdown.getText())) {
                    showToast("Please select a team from the dropdown menu");
                    return false;
                }
                break;
            default:
                Log.d("SIGN UP EXCEPTION", "Error trying to determine user type (verifyInputs())");
                return false;
        }
        // No invalid inputs, return true
        return true;
    }

    // Show a toast message
    void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // Functions to manage extra fields specific to each user
    public void enablePlayerReservedFields() {
        // Dropdown menu to select the player's team
        setupTeamDropdown();
        // Dropdown menu to select the player's position
        setupPositionDropdown();
        // To enter the player's number
        number = findViewById(R.id.number_field);
    }

    public void enableCoachReservedFields() {
        // To enter team name
        teamName = findViewById(R.id.teamname_field);
    }

    public void enableTrainerReservedFields() {
        // Dropdown menu to select the trainer's team
        setupTeamDropdown();
    }

    // To setup the dropdown for a player's position (players only)
    private void setupPositionDropdown() {
        positionDropdown = findViewById(R.id.position_field_dropdown);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, Position.getPositionList());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        positionDropdown.setAdapter(adapter);
        positionDropdown.setOnItemClickListener((adapterView, view, i, l) -> {
            position = adapterView.getItemAtPosition(i).toString();
        });
    }

    // To setup the dropdown menu for teams (players and trainers only)
    private void setupTeamDropdown() {
        teamDropdown = findViewById(R.id.teamnamePlayer_dropdown);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, db.getTeams());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        teamDropdown.setAdapter(adapter);
    }
}


