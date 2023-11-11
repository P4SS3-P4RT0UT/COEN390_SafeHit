package com.example.coen390_safehit.view;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.coen390_safehit.controller.DatabaseHelper;
import com.example.coen390_safehit.R;
import com.example.coen390_safehit.model.Position;
import com.google.android.material.textfield.TextInputEditText;

public class SignUpInformation extends AppCompatActivity {

    AutoCompleteTextView typeDropdown, teamDropdown, positionDropdown;
    //Spinner positionDropdown;
    LinearLayout coachLayout, playerLayout, trainerLayout;
    Button signUpButton;
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
        setupTypeDropdown();
        coachLayout = findViewById(R.id.coach_layout);
        playerLayout = findViewById(R.id.player_layout);
        trainerLayout = findViewById(R.id.trainer_layout);

        firstName = findViewById(R.id.firstname_field);
        lastName = findViewById(R.id.lastname_field);
        teamName = findViewById(R.id.teamname_field);
        number = findViewById(R.id.number_field);
        progressBar = findViewById(R.id.progressBar);
        setupButton();
    }

    private void setupPositionDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, Position.getPositionList());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        positionDropdown.setAdapter(adapter);
        positionDropdown.setOnItemClickListener((adapterView, view, i, l) -> {
            position = adapterView.getItemAtPosition(i).toString();
        });
    }

    private void setupTypeDropdown() {
        typeDropdown = findViewById(R.id.type_dropdown);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.personType, android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeDropdown.setAdapter(adapter);

        typeDropdown.setOnItemClickListener((adapterView, view, i, l) -> {
            String type = adapterView.getItemAtPosition(i).toString();
            currentType = type;
            if (type.equals("Coach")) {
                coachLayout.setVisibility(LinearLayout.VISIBLE);
                playerLayout.setVisibility(LinearLayout.GONE);
                trainerLayout.setVisibility(LinearLayout.GONE);
            } else if (type.equals("Player")) {
                coachLayout.setVisibility(LinearLayout.GONE);
                playerLayout.setVisibility(LinearLayout.VISIBLE);
                trainerLayout.setVisibility(LinearLayout.GONE);
                teamDropdown = findViewById(R.id.teamnamePlayer_dropdown);
                positionDropdown = findViewById(R.id.position_field_dropdown);
                setupTeamDropdown();
                setupPositionDropdown();
            } else if (type.equals("Trainer")) {
                coachLayout.setVisibility(LinearLayout.GONE);
                playerLayout.setVisibility(LinearLayout.GONE);
                trainerLayout.setVisibility(LinearLayout.VISIBLE);
                teamDropdown = findViewById(R.id.teamnameTrainer_dropdown);
                setupTeamDropdown();
            }
        });
    }

    void setupButton() {
        signUpButton = findViewById(R.id.signUpButton);
        signUpButton.setOnClickListener(view -> {
            if (verifyInputs()) {
                progressBar.setVisibility(View.VISIBLE);
                if (currentType.equals("Coach")) {
                    db.addCoach(firstName.getText().toString(), lastName.getText().toString(), teamName.getText().toString());
                } else if (currentType.equals("Player")) {
                    db.teamsList.get(teamDropdown.getText().toString());
                    db.addPlayer(firstName.getText().toString(), lastName.getText().toString(), position, number.getText().toString(), db.teamsList.get(teamDropdown.getText().toString()));
                } else if (currentType.equals("Trainer")) {
                    db.addTrainer(firstName.getText().toString(), lastName.getText().toString(), db.teamsList.get(teamName.getText().toString()));
                }
            }
        });
    }

    private void setupTeamDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, db.getTeams());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        teamDropdown.setAdapter(adapter);
    }

    boolean verifyInputs() {
        if (TextUtils.isEmpty(firstName.getText()) ||
                TextUtils.isEmpty(lastName.getText()) ||
                (TextUtils.isEmpty(teamName.getText()) && TextUtils.isEmpty(teamDropdown.getText()) ||
                        (currentType.equals("Player") && position == null) ||
                        TextUtils.isEmpty(number.getText()))) {
            showToast();
            return false;
        } else return true;
    }

    void showToast() {
        Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
    }
}
