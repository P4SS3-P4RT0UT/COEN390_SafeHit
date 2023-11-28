package com.example.coen390_safehit.model;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum Position {
    NONE("Select a position"),
    QB("Quarterback"),
    RB("Running Back"),
    FB("Fullback"),
    WR("Wide Receiver"),
    TE("Tight End"),
    C("Center"),
    LG("Left Guard"),
    RG("Right Guard"),
    LT("Left Tackle"),
    RT("Right Tackle"),
    NT("Nose Tackle"),
    DT("Defensive Tackle"),
    DE("Defensive End"),
    CB("Corner-back"),
    MLB("Middle Linebacker"),
    OLB("Outside Linebacker"),
    FS("Free Safety"),
    SS("Strong Safety");

    private String position;

    // To get the string value of a position
    Position(String position) {
        this.position = position;
    }

    public String getPosition() {
        return position;
    }

    // To get all positions (as their string value)
    public static String[] getPositionList() {
        Position[] positions = Position.values();
        String[] positionsToString = new String[positions.length];
        for (int i = 0; i < positions.length; i++) {
            positionsToString[i] = positions[i].getPosition();
        }
        return positionsToString;
    }
}
