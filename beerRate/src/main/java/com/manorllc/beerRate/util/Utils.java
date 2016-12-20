package com.manorllc.beerRate.util;

import com.manorllc.beerRate.db.DbBeer;
import com.manorllc.beerRate.db.DbUser;
import com.manorllc.beerRate.model.Beer;

public class Utils {

    public static String getFullName(final String firstName, final String lastName) {
        return firstName + " " + lastName;
    }

    public static String getFullName(final DbUser dbUser) {
        return Utils.getFullName(dbUser.getFirstName(), dbUser.getLastName());
    }

    public static String getFirstName(final String fullName) {
        return fullName.split(" ")[0];
    }

    public static String getLastName(final String fullName) {
        return fullName.split(" ")[1];
    }

    public static Beer createBeer(final DbBeer dbBeer, final String categoryName) {
        Beer beer = new Beer();
        beer.setBrewery(dbBeer.getBrewery());
        beer.setCategory(categoryName);
        beer.setName(dbBeer.getName());
        beer.setStyle(dbBeer.getStyle());
        return beer;
    }

}
