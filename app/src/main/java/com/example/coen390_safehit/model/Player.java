package com.example.coen390_safehit.model;

import com.example.coen390_safehit.controller.DatabaseHelper;

// Model for the players
public class Player extends Person {

    // Attributes

    private String pid; // Player ID
    private int number; // Number
    private String position; // Position of the field
    private String team; // Team name
    private String status;
    private String suggestion;

    private String mac; //should replace the id

    // Constructors

    public Player() {
        super();
    }

    public Player(String email, String firstName, String lastName, String pid, int number, String position, String team, String status, String suggestion, String mac) {
        super(email, firstName, lastName);
        this.pid = pid;
        this.number = number;
        this.position = position;
        this.team = team;
        this.status = status;
        this.suggestion = suggestion;
        this.mac = mac;
    }

    // Getters and setters

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        DatabaseHelper.updatePlayerMac(mac, pid);
        this.mac = mac;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getTeams() {
        return team;
    }

    public void setTeams(String teams) {
        this.team = teams;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }

}
