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

import com.manorllc.beerRate.model.Stats;

public class DatabaseQueries {

    private final Database db;

    public DatabaseQueries(final Database db) {
        this.db = db;
    }

    public Optional<Stats> getStatsForBeer(final String beerName) {
        if (!db.beerExists(beerName)) {
            return Optional.empty();
        } else {
            Collection<DbRating> ratings = db.getRatingsForBeer(beerName).values();
            Stats stats = getStats(ratings);
            return Optional.of(stats);
        }
    }

    public Optional<Stats> getStatsForCategory(final String categoryName) {
        if (!db.categoryExists(categoryName)) {
            return Optional.empty();
        } else {
            Collection<DbBeer> beers = db.getBeersForCategory(categoryName);
            Collection<DbRating> ratings = new HashSet<>();
            beers.forEach(beer -> {
                ratings.addAll(db.getRatingsForBeer(beer.getName()).values());
            });
            Stats stats = getStats(ratings);
            return Optional.of(stats);
        }
    }

    public Stats getStatsForAll() {
        Collection<DbBeer> beers = new HashSet<>();
        db.getBeersByCategory().values().forEach(col -> {
            beers.addAll(col);
        });

        Collection<DbRating> ratings = new HashSet<>();
        beers.forEach(beer -> {
            ratings.addAll(db.getRatingsForBeer(beer.getName()).values());
        });

        Stats stats = getStats(ratings);
        return stats;

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
                if (!rating.getUserLastName().equalsIgnoreCase("Heise")) {
                    String fullName = rating.getUserFirstName() + " " + rating.getUserLastName();
                    // Only add them as a captain if they are not captain of
                    // another team
                    if (!captainSet.contains(fullName)) {
                        captains.put(categoryName, fullName);
                        captainSet.add(fullName);
                    }
                }
            }

        });
        return captains;
    }

    private Stats getStats(final Collection<DbRating> ratings) {
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
        stats.setMean(summaryStats.getMean());

        return stats;
    }

}
