package com.example.coen390_safehit.model;

// Model for person in database
public class Person {

    // Attributes
    private String email;
    private String firstName;
    private String LastName;

    // Constructors
    public Person() {
        super();
    }
    public Person(String email, String firstName, String lastName) {
        this.email = email;
        this.firstName = firstName;
        LastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return LastName;
    }

    public void setLastName(String lastName) {
        LastName = lastName;
    }
}
