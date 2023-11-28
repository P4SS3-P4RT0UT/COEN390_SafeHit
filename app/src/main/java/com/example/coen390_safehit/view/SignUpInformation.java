package com.example.coen390_safehit.view;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.coen390_safehit.controller.DatabaseHelper;
import com.example.coen390_safehit.R;
import com.example.coen390_safehit.model.Position;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class SignUpInformation extends AppCompatActivity {
    Spinner typeDropdown, teamDropdown, positionDropdown, teamDropdownTrainer;

    LinearLayout coachLayout, playerLayout;
    Button signUpButton, cancelButton;
    TextInputEditText firstName, lastName;
    TextInputEditText teamName;
    TextInputEditText number;
    ProgressBar progressBar;
    DatabaseHelper db = DatabaseHelper.getInstance(this);

    String currentType;
    String position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_information);

        // Setup all user layouts (player, coach, trainer)
        setupUserLayouts();

        // For first name, last name, and type dropdown
        setupGeneralUserInfo();

        // Setup the progress bar
        progressBar = findViewById(R.id.progressBar);

        cancelButton = findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(view -> {
            db.deleteUserFromFirebase();
            finish();
        });

        // Setup the sign up button
        setupSignUpButton();
    }

    // To setup all possible user layouts
    public void setupUserLayouts() {
        // Layout of coach, player, and trainer
        coachLayout = findViewById(R.id.coach_layout);
        playerLayout = findViewById(R.id.player_layout);
    }

    // To setup layout fields available to all users
    public void setupGeneralUserInfo() {
        // Input text for first name, last name
        firstName = findViewById(R.id.firstname_field);
        lastName = findViewById(R.id.lastname_field);
        // Setup the type dropdown
        setupTypeDropdown();
    }

    // To enable the user type selection
    private void setupTypeDropdown() {
        // Create type dropdown menu
        typeDropdown = findViewById(R.id.type_dropdown);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.personType, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        typeDropdown.setAdapter(adapter);

        // Get user type selection
        typeDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String type = typeDropdown.getSelectedItem().toString();
                currentType = type;
                // Set the layout visibility according to the type selection
                if (type.equals("Select Type")) {
                    // Hide all layouts
                    coachLayout.setVisibility(LinearLayout.GONE);
                    playerLayout.setVisibility(LinearLayout.GONE);
                } else if (type.equals("Coach")) {
                    // Show coach layout and enable fields specific to user type
                    coachLayout.setVisibility(LinearLayout.VISIBLE);
                    enableCoachReservedFields();
                    // Hide other layouts
                    playerLayout.setVisibility(LinearLayout.GONE);
                } else if (type.equals("Player")) {
                    // Show player layout and enable fields specific to user type
                    playerLayout.setVisibility(LinearLayout.VISIBLE);
                    enablePlayerReservedFields();
                    // Hide other layouts
                    coachLayout.setVisibility(LinearLayout.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }
        });
    }

    // To setup the sign up button
    public void setupSignUpButton() {
        // Setup the sign up button
        signUpButton = findViewById(R.id.signUpButton);
        signUpButton.setOnClickListener(view -> {
            progressBar.setVisibility(View.VISIBLE);
            signUpButton.setVisibility(View.GONE);
            // Check for valid inputs
            if (validUserInput()) {
                // Show progress bar
                progressBar.setVisibility(View.VISIBLE);
                // Add the user to the database
                switch (currentType) {
                    case "Coach":
                        db.addCoach(firstName.getText().toString(), lastName.getText().toString(), teamName.getText().toString());
                        break;
                    case "Player":
                        db.teamsList.get(teamDropdown.getSelectedItem().toString());
                        db.addPlayer(firstName.getText().toString(), lastName.getText().toString(), positionDropdown.getSelectedItem().toString(), number.getText().toString(), db.teamsList.get(teamDropdown.getSelectedItem().toString()));
                        break;
                    case "Trainer":
                        db.addTrainer(firstName.getText().toString(), lastName.getText().toString(), db.teamsList.get(teamName.getText().toString()));
                        break;
                    default:
                        progressBar.setVisibility(View.GONE);
                        signUpButton.setVisibility(View.VISIBLE);
                        Log.d("SIGN UP EXCEPTION", "Error trying to determine user type (setupSignUpButton())");
                }
            } else {
                progressBar.setVisibility(View.GONE);
                signUpButton.setVisibility(View.VISIBLE);
            }
        });
    }

    // To check if all user inputs are valid
    public boolean validUserInput() {
        // For all user types, check first name, last name, and user type
        if (TextUtils.isEmpty(firstName.getText()) || TextUtils.isEmpty(lastName.getText()) || TextUtils.isEmpty(currentType)) {
            showToast("Please fill all required fields");
            return false;
        }
        switch (currentType) {
            case "Coach":
                // Check for team name
                if (TextUtils.isEmpty(teamName.getText())) {
                    showToast("Please enter a team name");
                    return false;
                }
                break;
            case "Player":
                // Check for team name, position, and number
                if (TextUtils.isEmpty(teamDropdown.getSelectedItem().toString()) || TextUtils.isEmpty(number.getText())) {
                    showToast("Please fill all required fields");
                    return false;
                } else if (teamDropdown.getSelectedItem().toString().equals("Select a team")) {
                    showToast("Please select a team");
                    return false;
                } else if (TextUtils.isEmpty(positionDropdown.getSelectedItem().toString()) || positionDropdown.getSelectedItem().toString().equals("Select a position")) {
                    showToast("Please select a position");
                    return false;
                }
                break;
            case "Trainer":
                // Check for team name
                if (TextUtils.isEmpty(teamDropdown.getSelectedItem().toString())) {
                    showToast("Please select a team");
                    return false;
                }
                break;
            default:
                showToast("Please select user type");
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
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, Position.getPositionList());
        adapter.setDropDownViewResource(R.layout.spinner_item);
        positionDropdown.setAdapter(adapter);
    }

    // To setup the dropdown menu for teams (players and trainers only)
    private void setupTeamDropdown() {
        teamDropdown = findViewById(R.id.teamnamePlayer_dropdown);

        // Fetch the team data
        List<String> teams = db.getTeams();

        // Create the adapter with the fetched data
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, teams);
        adapter.setDropDownViewResource(R.layout.spinner_item);

        // Set the adapter to the spinner
        teamDropdown.setAdapter(adapter);
    }

}
