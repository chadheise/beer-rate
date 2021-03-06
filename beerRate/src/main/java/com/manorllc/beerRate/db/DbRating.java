package com.manorllc.beerRate.db;

import org.joda.time.DateTime;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.manorllc.beerRate.util.CustomDateSerializer;

/**
 * Pojo representing a beer rating.
 */
public class DbRating {

    private double rating;

    @JsonSerialize(using = CustomDateSerializer.class)
    private DateTime created;

    @JsonSerialize(using = CustomDateSerializer.class)
    private DateTime updated;

    public DbRating(final double rating, final DateTime created) {
        this.rating = rating;
        this.created = created;
        this.updated = created;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(final double rating) {
        this.rating = rating;
    }

    public DateTime getCreated() {
        return created;
    }

    public DateTime getUpdated() {
        return updated;
    }

    public void setUpdated(final DateTime updated) {
        this.updated = updated;
    }

}
