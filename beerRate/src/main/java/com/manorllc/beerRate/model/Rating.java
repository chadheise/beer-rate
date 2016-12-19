package com.manorllc.beerRate.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.joda.time.DateTime;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.manorllc.beerRate.util.CustomDateSerializer;

/**
 * Pojo representing a beer rating.
 */
public class Rating {

    private double rating;

    @JsonSerialize(using = CustomDateSerializer.class)
    private DateTime created;

    @JsonSerialize(using = CustomDateSerializer.class)
    private DateTime updated;

    private String userFirstName;

    private String userLastName;

    private String beerName;

    @Override
    public int hashCode() {
        return new HashCodeBuilder(31, 41)
                .append(rating)
                .append(created)
                .append(userFirstName)
                .append(userLastName)
                .append(beerName)
                .toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        Rating rhs = (Rating) obj;
        return new EqualsBuilder()
                .appendSuper(super.equals(obj))
                .append(rating, rhs.rating)
                .append(created, rhs.created)
                .append(userFirstName, rhs.userFirstName)
                .append(userLastName, rhs.userLastName)
                .append(beerName, rhs.beerName)
                .isEquals();
    }

    public String getUserFirstName() {
        return userFirstName;
    }

    public void setUserFirstName(String userFirstName) {
        this.userFirstName = userFirstName;
    }

    public String getUserLastName() {
        return userLastName;
    }

    public void setUserLastName(String userLastName) {
        this.userLastName = userLastName;
    }

    public String getBeerName() {
        return beerName;
    }

    public void setBeerName(String beerName) {
        this.beerName = beerName;
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

    public void setCreated(DateTime created) {
        this.created = created;
    }

    public DateTime getUpdated() {
        return updated;
    }

    public void setUpdated(final DateTime updated) {
        this.updated = updated;
    }

}
