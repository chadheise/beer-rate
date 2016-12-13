package com.manorllc.beerRate.db;

import com.manorllc.beerRate.model.Generation;

public class DbUser {

    private String firstName;
    private String lastName;
    private Generation generation;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Generation getGeneration() {
        return generation;
    }

    public void setGeneration(Generation generation) {
        this.generation = generation;
    }

}
