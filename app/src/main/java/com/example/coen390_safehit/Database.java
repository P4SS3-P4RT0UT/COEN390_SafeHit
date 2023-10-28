package com.example.coen390_safehit;

import static android.content.ContentValues.TAG;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class Database {
    private static Database databaseInstance = null;
    public HashMap<String, String> playerList = new HashMap<>();
    public HashMap<String, String> teamsList = new HashMap<>();

    public static String personID;

    public static synchronized Database getInstance() {
        if (databaseInstance == null)
            databaseInstance = new Database();

        return databaseInstance;
    }

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
                Map<String, String> player = new HashMap<>();
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
        Map<String, String> person = new HashMap<>();
        person.put("FirstName", firstName);
        person.put("LastName", lastName);
        person.put("Type", type);

        db.collection("people").add(person)
                .addOnSuccessListener(documentReference -> callback.onSuccess(documentReference.getId()))
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    public void addTeams(String teamName, String coachID, AddCallback callback) {
        Map<String, String> team = new HashMap<>();
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
    public void getTeamsFromCoachID(String coachID) {
        db.collection("Teams")
                .whereEqualTo("CoachID", coachID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                teamsList.put(document.getId(), document.getData().toString());
                                Log.d(TAG, document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    //Returns PlayerID
    public String getPlayersFromTeamID(String teamID) {
        db.collection("players")
                .whereEqualTo("TeamID", teamID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                playerList.put(document.getId(), document.getData().toString());
                                Log.d(TAG, document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

        return null;
    }

    //================================================================================
    // endregion
    //================================================================================
}
