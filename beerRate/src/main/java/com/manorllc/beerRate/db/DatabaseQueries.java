package com.manorllc.beerRate.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.SynchronizedSummaryStatistics;
import org.joda.time.DateTime;

import com.manorllc.beerRate.model.Beer;
import com.manorllc.beerRate.model.Rating;
import com.manorllc.beerRate.model.Stats;
import com.manorllc.beerRate.util.Utils;

public class DatabaseQueries {

    private final Database db;

    public DatabaseQueries(final Database db) {
        this.db = db;
    }

    public Optional<Stats> getStatsForBeer(final String beerName) {
        if (!db.beerExists(beerName)) {
            return Optional.empty();
        } else {
            Collection<Rating> ratings = db.getRatingsForBeer(beerName);
            Stats stats = getStats(ratings);
            return Optional.of(stats);
        }
    }

    public Optional<Stats> getStatsForCategory(final String categoryName) {
        if (!db.categoryExists(categoryName)) {
            return Optional.empty();
        } else {
            Collection<Beer> beers = db.getBeersForCategory(categoryName);
            Collection<Rating> ratings = new HashSet<>();
            beers.forEach(beer -> {
                ratings.addAll(db.getRatingsForBeer(beer.getName()));
            });
            Stats stats = getStats(ratings);
            return Optional.of(stats);
        }
    }

    public Stats getStatsForAll() {
        Collection<Beer> beers = new HashSet<>();
        db.getBeersByCategory().values().forEach(col -> {
            beers.addAll(col);
        });

        Collection<Rating> ratings = new HashSet<>();
        beers.forEach(beer -> {
            ratings.addAll(db.getRatingsForBeer(beer.getName()));
        });

        Stats stats = getStats(ratings);
        return stats;
    }

    public Map<String, Integer> getGameOneStats() {
        // user name -> number of checkins
        Map<String, Integer> checkinCount = new HashMap<>();

        DateTime gameMarker = db.getGameMarker();

        // Initialize all users to 0
        for (DbUser dbUser : db.getUsers()) {
            checkinCount.put(Utils.getFullName(dbUser.getFirstName(), dbUser.getLastName()), 0);
        }

        for (Rating rating : db.getRatings()) {
            if (gameMarker != null) {
                if (rating.getCreated().isBefore(gameMarker)) {
                    int currentCount = checkinCount.get(rating.getUserName());
                    checkinCount.put(rating.getUserName(), currentCount + 1);
                }
            } else {
                int currentCount = checkinCount.get(rating.getUserName());
                checkinCount.put(rating.getUserName(), currentCount + 1);
            }

        }

        return checkinCount;
    }

    public Map<String, Integer> getGameTwoStats() {
        // user name -> number of checkins
        Map<String, Integer> checkinCount = new HashMap<>();

        DateTime gameMarker = db.getGameMarker();

        // Initialize all users to 0
        for (DbUser dbUser : db.getUsers()) {
            checkinCount.put(Utils.getFullName(dbUser.getFirstName(), dbUser.getLastName()), 0);
        }

        for (Rating rating : db.getRatings()) {
            if (gameMarker != null) {
                if (rating.getCreated().isAfter(gameMarker)) {
                    int currentCount = checkinCount.get(rating.getUserName());
                    checkinCount.put(rating.getUserName(), currentCount + 1);
                }
            }
        }

        return checkinCount;
    }

    /**
     * Returns the first person to check in a beer from each category
     * 
     * @return
     */
    public Map<String, String> getTeamCaptains() {
        Map<String, String> captains = new HashMap<>();
        Set<String> captainSet = new HashSet<>();

        Map<String, String> beerToCategory = db.getBeerToCategory();
        db.getRatings().forEach(rating -> {
            String categoryName = beerToCategory.get(rating.getBeerName());

            if (!captains.containsKey(categoryName)) {
                // Ignore members of the Heise family as they should not be
                // captains
                if (!Utils.getFirstName(rating.getUserName()).equalsIgnoreCase("Heise")) {
                    // Only add them as a captain if they are not captain of
                    // another team
                    if (!captainSet.contains(rating.getUserName())) {
                        captains.put(categoryName, rating.getUserName());
                        captainSet.add(rating.getUserName());
                    }
                }
            }

        });
        return captains;
    }

    private Stats getStats(final Collection<Rating> ratings) {
        Stats stats = new Stats();
        stats.setCount(ratings.size());

        List<Double> doubleRatings = new ArrayList<>();
        ratings.forEach(r -> doubleRatings.add(r.getRating()));
        if (!doubleRatings.isEmpty()) {
            int midPoint = doubleRatings.size() / 2;
            double median = doubleRatings.get(midPoint);
            stats.setMedian(median);
        }

        SynchronizedSummaryStatistics summaryStats = new SynchronizedSummaryStatistics();
        ratings.forEach(r -> summaryStats.addValue((double) r.getRating()));

        stats.setMin((int) summaryStats.getMin());
        stats.setMax((int) summaryStats.getMax());
        double roundedMean = Math.round(summaryStats.getMean() * 100.0) / 100.0;
        stats.setMean(roundedMean);

        return stats;
    }

}
