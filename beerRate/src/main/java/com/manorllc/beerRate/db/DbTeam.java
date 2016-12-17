package com.manorllc.beerRate.db;

public class DbTeam {

    private String name;

    public DbTeam(final String name) {
        this.name = name;
    }

    public DbTeam() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
