package com.example.coen390_safehit.model;


import java.util.List;

public class Coach extends Person {

    // Attributes
    int cid; // Coach ID
    String title; // Official title
    List<Team> teams; // Associated team(s)

    // Constructors

    public Coach() {
        super();
    }

    public Coach(String email, String firstName, String lastName, int cid, String title, List<Team> teams) {
        super(email, firstName, lastName);
        this.cid = cid;
        this.title = title;
        this.teams = teams;
    }

    // Getters and setters

    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Team> getTeams() {
        return teams;
    }

    public void setTeams(List<Team> teams) {
        this.teams = teams;
    }
}
