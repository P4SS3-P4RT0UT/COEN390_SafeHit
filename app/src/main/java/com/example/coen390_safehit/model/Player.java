package com.example.coen390_safehit.model;

import com.google.type.DateTime;

import java.util.List;

// Model for the players
public class Player extends Person {

    // Attributes

    private String pid; // Player ID
    private int number; // Number
    private Enum<Position> position; // Position of the field
    private List<Team> teams; // Team name
    private DateTime dateOfBirth; // Date of birth
    private float height; // Player height
    private float weight; // Player weight

    // Constructors

    public Player() {
        super();
    }

    public Player(String email, String firstName, String lastName, String pid, int number, Enum<Position> position, List<Team> teams, DateTime dateOfBirth, float height, float weight) {
        super(email, firstName, lastName);
        this.pid = pid;
        this.number = number;
        this.position = position;
        this.teams = teams;
        this.dateOfBirth = dateOfBirth;
        this.height = height;
        this.weight = weight;
    }

    // Getters and setters

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public Enum<Position> getPosition() {
        return position;
    }

    public void setPosition(Enum<Position> position) {
        this.position = position;
    }

    public List<Team> getTeams() {
        return teams;
    }

    public void setTeams(List<Team> teams) {
        this.teams = teams;
    }

    public DateTime getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(DateTime dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }
}
