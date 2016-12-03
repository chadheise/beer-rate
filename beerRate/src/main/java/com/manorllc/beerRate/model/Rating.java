package com.manorllc.beerRate.model;

import org.joda.time.DateTime;

/**
 * Pojo representing a beer rating.
 */
public class Rating {

    private String beer;
    private double rating;
    private DateTime timestamp;

    public String getBeer() {
        return beer;
    }

    public void setBeer(String beer) {
        this.beer = beer;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public DateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(DateTime timestamp) {
        this.timestamp = timestamp;
    }

}
