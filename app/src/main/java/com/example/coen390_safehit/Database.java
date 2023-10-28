package com.example.coen390_safehit;

import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Database {
    public interface AddCallback {
        void onSuccess(String documentId);

        void onFailure(Exception e);
    }

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    //================================================================================
    // region Add Person, Player, Team and Coach to database
    //================================================================================
    public void addCoach(String firstName, String lastName, String teamName) {
        addPerson(firstName, lastName, "Coach", new AddCallback() {
            @Override
            public void onSuccess(String personID) {
                addTeams(teamName, personID, new AddCallback() {
                    @Override
                    public void onSuccess(String teamID) {
                        Toast.makeText(null, "Team added successfully", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(null, "Failed to add team", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(null, "Failed to add coach", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void addPlayer(String firstName, String lastName, String position, String number, String teamID) {
        addPerson(firstName, lastName, "Player", new AddCallback() {
            @Override
            public void onSuccess(String personID) {
                Map<String, Object> player = new HashMap<>();
                player.put("PID", personID);
                player.put("Number", number);
                player.put("Position", position);
                player.put("TeamID", teamID);
                db.collection("players").add(player);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(null, "Failed to add player", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void addPerson(String firstName, String lastName, String type, AddCallback callback) {
        Map<String, Object> person = new HashMap<>();
        person.put("FirstName", firstName);
        person.put("LastName", lastName);
        person.put("Type", type);

        db.collection("people").add(person)
                .addOnSuccessListener(documentReference -> callback.onSuccess(documentReference.getId()))
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    public void addTeams(String teamName, String coachID, AddCallback callback) {
        Map<String, Object> team = new HashMap<>();
        team.put("TeamName", teamName);
        team.put("CoachID", coachID);

        db.collection("teams")
                .add(team)
                .addOnSuccessListener(documentReference -> {
                    System.out.println("Team added with ID: " + documentReference.getId());
                    callback.onSuccess(documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    System.out.println("Error adding team: " + e.getMessage());
                    callback.onFailure(e);
                });
    }
    //================================================================================
    // endregion
    //================================================================================

    //================================================================================
    // region Get Person, Player, Team and Coach from database
    //================================================================================

    //Returns TeamID and TeamName
    public Map<String, String> getTeamsFromCoachID() {
        return null;
    }

    //Returns PlayerID
    public String getPlayersFromTeamID() {
        return null;
    }

    //================================================================================
    // endregion
    //================================================================================
}
