package com.example.coen390_safehit.model;

import java.util.List;

public class Team {

    // Attributes
    private int tid; // Team ID
    private String name; // Team name
    private List<Player> players; // List of players in the team
    private List<Coach> coaches; // List of coaches for the team

    // Constructors

    public Team() {
    }

    public Team(int tid, String name, List<Player> players, List<Coach> coaches) {
        this.tid = tid;
        this.name = name;
        this.players = players;
        this.coaches = coaches;
    }

    // Getters and setters

    public int getTid() {
        return tid;
    }

    public void setTid(int tid) {
        this.tid = tid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public List<Coach> getCoaches() {
        return coaches;
    }

    public void setCoaches(List<Coach> coaches) {
        this.coaches = coaches;
    }
}
