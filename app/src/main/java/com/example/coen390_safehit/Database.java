package com.example.coen390_safehit;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Database {
    private static Database databaseInstance = null;
    public ArrayList<DocumentSnapshot> playerDocumentList = new ArrayList<>();
    public HashMap<String, String> teamsList = new HashMap<>();

    public static String personID;
    public static String personType;
    public static String currentTeamID;
    public static String currentTeamName;
    private static Context currentContext;
    public TextView playerStatus;
    public TextView coachSuggestion;
    public TextView playerName;
    public static String email;

    public static synchronized Database getInstance(Context context) {
        if (databaseInstance == null)
            databaseInstance = new Database();
        currentContext = context;
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
            public void onSuccess(String pID) {
                personID = pID;
                currentTeamName = teamName;
                addTeams(teamName, personID, new AddCallback() {
                    @Override
                    public void onSuccess(String teamID) {
                        currentTeamID = teamID;
                        Toast.makeText(currentContext, "Team added successfully", Toast.LENGTH_SHORT).show();
                        Intent coachProfile = new Intent(currentContext, CoachProfileActivity.class);
                        currentContext.startActivity(coachProfile);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(currentContext, "Failed to add team", Toast.LENGTH_SHORT).show();
                        db.collection("Person").document(personID).delete();
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(currentContext, "Failed to add coach", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void addTrainer(String firstName, String lastName, String teamName) {
        addPerson(firstName, lastName, "Trainer", new AddCallback() {
            @Override
            public void onSuccess(String personID) {
                addTeams(teamName, personID, new AddCallback() {
                    @Override
                    public void onSuccess(String teamID) {
                        Toast.makeText(currentContext, "Team added successfully", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(currentContext, "Failed to add team", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(currentContext, "Failed to add Trainer", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void addPlayer(String firstName, String lastName, String position, String number, String teamID) {
        addPerson(firstName, lastName, "Player", new AddCallback() {
            @Override
            public void onSuccess(String pID) {
                Map<String, String> player = new HashMap<>();
                player.put("PID", pID);
                player.put("Number", number);
                player.put("Position", position);
                player.put("TeamID", teamID);
                personID = pID;
                db.collection("Players").add(player)
                        .addOnSuccessListener(documentReference -> {
                            Toast.makeText(currentContext, "Player added successfully", Toast.LENGTH_SHORT).show();
                            // TODO: Go to PlayerProfileActivity
                        })
                        .addOnFailureListener(
                                e -> {
                                    Toast.makeText(currentContext, "Failed to add player", Toast.LENGTH_SHORT).show();
                                    db.collection("Person").document(pID).delete();
                                });
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(currentContext, "Failed to add person", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addPerson(String firstName, String lastName, String type, AddCallback callback) {
        Map<String, String> person = new HashMap<>();
        person.put("FirstName", firstName);
        person.put("LastName", lastName);
        person.put("Email", email);
        person.put("Type", type);

        db.collection("Person").add(person)
                .addOnSuccessListener(documentReference -> callback.onSuccess(documentReference.getId()))
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    private void addTeams(String teamName, String coachID, AddCallback callback) {
        Map<String, String> team = new HashMap<>();
        team.put("TeamName", teamName);
        team.put("CoachID", coachID);

        db.collection("Teams")
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
    public interface FetchCallback {
        void onComplete();

        void onError(Exception e);
    }

    //Returns TeamID and TeamName
    public void getTeamsFromCoachID(String coachID, FetchCallback callback) {
        db.collection("Teams")
                .whereEqualTo("CoachID", coachID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (!teamsList.containsKey(document.getId())) {
                                    teamsList.put(document.getData().get("TeamName").toString(), document.getId());
                                    Log.d(TAG, document.getId() + " => " + document.getData());
                                }
                            }
                            callback.onComplete();
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                            callback.onError(task.getException());
                        }
                    }
                });
    }

    public void getPersonFromPlayerID(String playerID, OnCompleteListener<DocumentSnapshot> listener) {
        db.collection("Person").document(playerID).get().addOnCompleteListener(listener);
    }


    //Returns PlayerID
    public String getPlayersFromTeamID(String teamID, FetchCallback callback) {
        db.collection("Players")
                .whereEqualTo("TeamID", teamID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if (!playerDocumentList.contains(document)) {
                                playerDocumentList.add(document);
                                Log.d(TAG, document.getId() + " => " + document.getData());
                            }
                        }
                        callback.onComplete();
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                        callback.onError(task.getException());
                    }
                });

        return null;
    }

    public List<String> getTeams() {
        List<String> teams = new ArrayList<>();
        db.collection("Teams")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (!teamsList.containsKey(document.getId()))
                                    teamsList.put(document.getData().get("TeamName").toString(), document.getId());

                                teams.add(document.getData().get("TeamName").toString());
                                Log.d(TAG, document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

        return teams;
    }


    public void getPersonFromEmail(String email, FetchCallback callback) {
        db.collection("Person")
                .whereEqualTo("Email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            personID = task.getResult().getDocuments().get(0).getId();
                            personType = task.getResult().getDocuments().get(0).get("Type").toString();
                            Log.d(TAG, personID + " => " + task.getResult().getDocuments().get(0).getData());
                            callback.onComplete();
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                        callback.onError(task.getException());
                    }
                });
    }

    public String getStatus(String personID) {
        db.collection("Players")
                .whereEqualTo("PID", personID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                playerStatus.setText(document.getString("Status"));
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

        return null;
    }

    public String getSuggestion(String personID) {
        db.collection("Players")
                .whereEqualTo("PID", personID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                coachSuggestion.setText(document.getString("Suggestion"));
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

        return null;
    }

    public String getPlayerNameFromPlayerID(String personID) {
        db.collection("Person")
                .document(personID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            Log.d(TAG, document.getId() + " => " + document.getData());
                            playerName.setText((document.getString("FirstName") + " " + document.getString("LastName")));
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
