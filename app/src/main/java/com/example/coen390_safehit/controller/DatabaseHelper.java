package com.example.coen390_safehit.controller;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.coen390_safehit.model.Player;
import com.example.coen390_safehit.view.CoachDataOverviewActivity;
import com.example.coen390_safehit.view.CoachProfileActivity;
import com.example.coen390_safehit.view.PlayerProfileActivity;
import com.example.coen390_safehit.view.SettingsActivity;
import com.example.coen390_safehit.view.UpdateInformationActivity;
import com.example.coen390_safehit.view.SignIn;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import org.checkerframework.checker.units.qual.C;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseHelper {
    private static DatabaseHelper databaseHelperInstance = null;
    public ArrayList<DocumentSnapshot> playerDocumentList = new ArrayList<>();
    public HashMap<String, String> teamsList = new HashMap<>();

    public static String personID;
    public static String playerID;
    public static String personType;
    public static String currentTeamID;
    public static String currentTeamName;
    private static Context currentContext;

    public static String threshold;
    public static String macAddress;

    public TextView playerStatus;
    public TextView coachSuggestion;
    public TextView playerName;
    public TextView firstName;
    public TextView lastName;
    public TextView coachName;

    public static String userType;

    public TextView playerNumber;
    public TextView playerPosition;
    public TextView playerTeam;

    public static String email;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (databaseHelperInstance == null)
            databaseHelperInstance = new DatabaseHelper();
        currentContext = context;
        return databaseHelperInstance;
    }

    public interface AddCallback {
        void onSuccess(String documentId);

        void onFailure(Exception e);
    }

    static FirebaseFirestore db = FirebaseFirestore.getInstance();

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
                player.put("Suggestion", "None");
                player.put("Status", "None");
                personID = pID;
                db.collection("Players").add(player)
                        .addOnSuccessListener(documentReference -> {
                            Toast.makeText(currentContext, "Player added successfully", Toast.LENGTH_SHORT).show();
                            Intent playerProfile = new Intent(currentContext, PlayerProfileActivity.class);
                            currentContext.startActivity(playerProfile);
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

        if (type == "Coach") {
            person.put("Threshold", "8");
            threshold = "8";
        }

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
    // region Update Person, Player, Team and Coach from database
    //================================================================================
    public void updatePerson(String firstName, String lastName, String uid) {
        Map<String, String> person = new HashMap<>();
        person.put("FirstName", firstName);
        person.put("LastName", lastName);

        db.collection("Person")
                .document(uid)
                .update("FirstName", firstName,
                        "LastName", lastName)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                    }
                });

    }

    public void udpateCoachThreshold(String thresholdValue, String uid) {
        Map<String, String> person = new HashMap<>();
        person.put("Threshold", thresholdValue);

        db.collection("Person")
                .document(uid)
                .update("Threshold", thresholdValue)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(currentContext, "Threshold Updated successfully", Toast.LENGTH_SHORT).show();
                        Intent coachProfile = new Intent(currentContext, CoachProfileActivity.class);
                        currentContext.startActivity(coachProfile);
                        threshold = thresholdValue;

                    }
                });
    }

    public void updateCoach(String firstName, String lastName, String uid, String teamname, String previousTeamName) {
        Map<String, String> person = new HashMap<>();
        person.put("FirstName", firstName);
        person.put("LastName", lastName);
        String teamID = teamsList.get(previousTeamName);

        if (teamsList.containsKey(teamname)) {
            Toast.makeText(currentContext, "Team name already exists", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("Person")
                .document(uid)
                .update("FirstName", firstName,
                        "LastName", lastName)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        db.collection("Teams")
                                .document(teamID)
                                .update("TeamName", teamname)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(currentContext, "Information Updated successfully", Toast.LENGTH_SHORT).show();
                                        Intent coachProfile = new Intent(currentContext, CoachProfileActivity.class);
                                        currentContext.startActivity(coachProfile);
                                    }
                                });
                    }

                });


    }

    public void updatePlayer(String number, String position, String TeamID, String uid) {
        Map<String, String> person = new HashMap<>();
        person.put("Number", number);
        person.put("Position", position);
        person.put("TeamID", TeamID);
        db.collection("Players")
                .whereEqualTo("PID", uid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                            String documentID = documentSnapshot.getId();
                            db.collection("Players")
                                    .document(documentID)
                                    .update("Number", number,
                                            "Position", position,
                                            "TeamID", TeamID)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Toast.makeText(currentContext, "Information Updated successfully", Toast.LENGTH_SHORT).show();
                                            Intent playerProfile = new Intent(currentContext, PlayerProfileActivity.class);
                                            currentContext.startActivity(playerProfile);
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(currentContext, "Failed to update information and add team", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                });
    }

    public void updatePlayerData(Player player) {
        db.collection("Players")
                .whereEqualTo("PID", player.getPid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        String documentID = documentSnapshot.getId();
                        db.collection("Players")
                                .document(documentID)
                                .update("Suggestion", player.getSuggestion(),
                                        "Status", player.getStatus())
                                .addOnSuccessListener(unused -> Toast.makeText(currentContext, "Information Updated successfully", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(currentContext, "Failed to update information and add team", Toast.LENGTH_SHORT).show());
                    }
                });
    }

    public static void updatePlayerMac(String mac, String uid) {
        db.collection("Players")
                .whereEqualTo("PID", uid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        String documentID = documentSnapshot.getId();
                        db.collection("Players")
                                .document(documentID)
                                .update("mac", mac)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        macAddress = mac;
                                        Toast.makeText(currentContext, "Device connected", Toast.LENGTH_SHORT).show();
                                        SettingsActivity.resetLinkButton(currentContext);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(currentContext, "Failed to connect device", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });
    }


    //================================================================================
    // region Get Person, Player, Team and Coach from database
    //================================================================================
    public interface FetchCallback {
        void onComplete();

        void onEmpty();

        void onError(Exception e);
    }

    //Returns TeamID and TeamName
    public void getTeamsFromCoachID(String coachID, FetchCallback callback) {
        if (coachID == null)
            return;

        teamsList.clear();
        db.collection("Teams")
                .whereEqualTo("CoachID", coachID)
                .orderBy("TeamName")
                .get()
                .addOnCompleteListener(task -> {
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
                });
    }

    public void getPersonFromPlayerID(String playerID, OnCompleteListener<DocumentSnapshot> listener) {
        db.collection("Person")
                .document(playerID)
                .get()
                .addOnCompleteListener(listener);
    }


    //Returns PlayerID
    public String getPlayersFromTeamID(String teamID, FetchCallback callback) {
        if (teamID == null)
            return null;

        db.collection("Players")
                .whereEqualTo("TeamID", teamID)
                .orderBy("Number")
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
        teams.add("Select a team");
        db.collection("Teams")
                .orderBy("TeamName")
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
        if (email == null)
            return;

        db.collection("Person")
                .whereEqualTo("Email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            personID = task.getResult().getDocuments().get(0).getId();
                            personType = task.getResult().getDocuments().get(0).get("Type").toString();
                            if (personType.equals("Coach"))
                                threshold = task.getResult().getDocuments().get(0).get("Threshold").toString();
                            Log.d(TAG, personID + " => " + task.getResult().getDocuments().get(0).getData());
                            callback.onComplete();
                        } else {
                            callback.onEmpty();
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                        callback.onError(task.getException());
                    }
                });
    }

    public String getStatus(String personID) {
        if (personID == null)
            return null;

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
                                currentTeamID = task.getResult().getDocuments().get(0).get("TeamID").toString();
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

        return null;
    }

    public String getSuggestion(String personID) {
        if (personID == null)
            return null;

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

    public void getPlayerNameFromPlayerID(String personID, Context context) {
        if (personID == null)
            return;

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

        db.collection("Players")
                .whereEqualTo("PID", personID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            Log.d(TAG, document.getId() + " => " + document.getData());
                            playerID = document.getId();
                            currentTeamID = document.getString("TeamID");
                            macAddress = document.getString("mac");
                            if (macAddress == null)
                                // TODO: CHANGE TO NULL
                                macAddress = null;
                            getPlayerCoachThreshold();
                            PlayerProfileActivity.updateButton(context);
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }

    public void unlinkDevice(Context context, String uid) {
        if (playerID != null) {
            db.collection("Players")
                    .document(playerID)
                    .update("mac", null)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(context, "Device unlinked successfully", Toast.LENGTH_SHORT).show();
                            macAddress = null;
                            SettingsActivity.resetLinkButton(context);
                        }
                    });
        } else {
            db.collection("Players")
                    .whereEqualTo("PID", uid)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                // Update each document
                                db.collection("Players").document(documentSnapshot.getId())
                                        .update("mac", null)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(context, "Device unlinked successfully", Toast.LENGTH_SHORT).show();
                                                SettingsActivity.resetLinkButton(context);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(context, "Failed to unlink device", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Failed to unlink device", Toast.LENGTH_SHORT).show();
                        }
                    });
        }


    }

    public String getUserTypeFromPlayerID(String personID) {
        db.collection("Person")
                .document(personID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            Log.d(TAG, document.getId() + " => " + document.getData());
                            userType = document.getString("Type");
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

        return null;
    }

    public String getPersonInfoFromPlayerID(String personID) {
        if (personID == null)
            return null;

        db.collection("Person")
                .document(personID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            Log.d(TAG, document.getId() + " => " + document.getData());
                            if (firstName != null) {
                                firstName.setText(document.getString("FirstName"));
                                lastName.setText(document.getString("LastName"));
                            }
                            if (coachName != null) {
                                coachName.setText(document.getString("FirstName") + " " + document.getString("LastName"));
                            }

                            userType = (document.getString("Type"));
                            if (userType.equals("Coach")) {
                                threshold = (document.getString("Threshold"));
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

        return null;
    }

    public String getPlayerInformationFromPlayerID(String personID, Context context) {
        if (personID == null)
            return null;

        db.collection("Players")
                .whereEqualTo("PID", personID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {

                            String teamID = documentSnapshot.getString("TeamID");
                            String position = documentSnapshot.getString("Position");
                            String number = documentSnapshot.getString("Number");
                            // Update each document

                            int teamIndex = -1;
                            for (int i = 0; i < UpdateInformationActivity.teamDropdown.getAdapter().getCount(); i++) {
                                String item = UpdateInformationActivity.teamDropdown.getAdapter().getItem(i).toString();
                                if (teamsList.containsKey(item) && teamsList.get(item).equals(teamID)) {
                                    teamIndex = i;
                                    break;
                                }
                            }

                            if (teamIndex >= 0) {
                                UpdateInformationActivity.teamDropdown.setSelection(teamIndex);
                            }

                            int positionIndex = -1;
                            for (int i = 0; i < UpdateInformationActivity.positionDropdown.getAdapter().getCount(); i++) {
                                String item = UpdateInformationActivity.positionDropdown.getAdapter().getItem(i).toString();
                                if (item.equalsIgnoreCase(position)) {
                                    positionIndex = i;
                                    break;
                                }
                            }

                            if (positionIndex >= 0) {
                                UpdateInformationActivity.positionDropdown.setSelection(positionIndex);
                            }

                            UpdateInformationActivity.teamAdapter.notifyDataSetChanged();

                            UpdateInformationActivity.number.setText(number);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Failed to unlink device", Toast.LENGTH_SHORT).show();
                    }
                });

        return null;
    }

    public void getPlayerCoachThreshold() {
        if (currentTeamID == null)
            return;


        db.collection("Teams")
                .document(currentTeamID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        db.collection("Person")
                                .document(task.getResult().getString("CoachID"))
                                .get()
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        DocumentSnapshot document = task1.getResult();
                                        Log.d(TAG, document.getId() + " => " + document.getData());
                                        threshold = document.getString("Threshold");
                                    } else {
                                        Log.d(TAG, "Error getting documents: ", task.getException());
                                    }
                                });
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }

    //================================================================================
    // Delete person, player, team from database
    //================================================================================
    public void deleteAccount(String uid, String type) {

        String personsCollection = "Person";

        // Build the path to the user document in the persons collection
        String personDocumentPath = personsCollection + "/" + uid;

        // Get the reference to the user document
        DocumentReference personDocumentRef = db.document(personDocumentPath);

        // Delete the user document from the persons collection
        personDocumentRef
                .delete()
                .addOnCompleteListener(personTask -> {
                    if (personTask.isSuccessful()) {
                        // Document deleted successfully
                        if (type.equals("Player")) {
                            // If the user is a player, also delete their player document
                            deletePlayerDocument(uid);
                        } else if (type.equals("Coach")) {
                            // If the user is a coach, also delete their team and remove coach ID
                            deleteCoachTeam(uid);
                        } else {
                            Intent intent = new Intent(currentContext, SignIn.class);
                            currentContext.startActivity(intent);
                            Toast.makeText(currentContext, "Account deleted successfully", Toast.LENGTH_SHORT).show();
                        }
                        deleteUserFromFirebase();
                    } else {
                        // Handle the exception if deletion fails for the person document
                        Toast.makeText(currentContext, "Failed to delete account: " + personTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void deleteUserFromFirebase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null)
            return;

        user.delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "User account deleted.");
                    }
                });
    }

    private void deletePlayerDocument(String uid) {
        if (uid == null)
            return;

        // Delete from "Person" collection first
        db.collection("Person")
                .document(uid)
                .delete()
                .addOnCompleteListener(personTask -> {
                    if (personTask.isSuccessful()) {
                        // Person deleted successfully, now find and delete the player
                        findAndDeletePlayer(uid);
                    } else {
                        handleDeletionError(personTask.getException());
                    }
                });
    }

    private void findAndDeletePlayer(String personId) {
        // Assuming 'uid' is a field in the documents of the "Players" collection
        db.collection("Players")
                .whereEqualTo("PID", personId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            deletePlayer(document.getId());
                        }
                    } else {
                        handleDeletionError(task.getException());
                    }
                });
    }

    private void deletePlayer(String playerDocumentId) {
        db.collection("Players")
                .document(playerDocumentId)
                .delete()
                .addOnCompleteListener(playerTask -> {
                    if (playerTask.isSuccessful()) {
                        Toast.makeText(currentContext, "Player deleted successfully", Toast.LENGTH_SHORT).show();
                        // Redirect to SignIn or other appropriate action
                        Intent intent = new Intent(currentContext, SignIn.class);
                        currentContext.startActivity(intent);
                    } else {
                        handleDeletionError(playerTask.getException());
                    }
                });
    }

    private void handleDeletionError(Exception exception) {
        // Log the error or handle it appropriately
        Toast.makeText(currentContext, "Failed to delete account: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
    }

    // Method to delete a coach's team and remove coach ID from the team document
    private void deleteCoachTeam(String coachUid) {
        db.collection("Teams")
                .whereEqualTo("CoachID", coachUid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            String teamDocumentId = document.getId();
                            db.collection("Teams")
                                    .document(teamDocumentId)
                                    .delete()
                                    .addOnCompleteListener(teamTask -> {
                                        if (teamTask.isSuccessful()) {
                                            // Team document deleted successfully
                                            // Navigate to the login screen
                                            Intent intent = new Intent(currentContext, SignIn.class);
                                            currentContext.startActivity(intent);
                                            deleteTeamPlayers(teamDocumentId);
                                            Toast.makeText(currentContext, "Team deleted successfully", Toast.LENGTH_SHORT).show();
                                        } else {
                                            // Handle the exception if deletion fails for the team document
                                            Toast.makeText(currentContext, "Failed to delete team: " + teamTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        // Handle the exception if fetching the coach's team document fails
                        Toast.makeText(currentContext, "Failed to delete account: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteTeamPlayers(String teamID) {
        db.collection("Players")
                .whereEqualTo("TeamID", teamID)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        deleteAccount(documentSnapshot.getId(), "Player");
                    }
                });
    }
    //================================================================================
    // endregion
    //================================================================================
}


