package com.manorllc.beerRate.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.apache.commons.math3.stat.descriptive.SynchronizedSummaryStatistics;

import com.manorllc.beerRate.model.Stats;

public class DbQueries {

    private final Database db;

    public DbQueries(final Database db) {
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

    private Stats getStats(final Collection<DbRating> ratings) {
        Stats stats = new Stats();
        stats.setCount(ratings.size());

        List<Double> doubleRatings = new ArrayList<>();
        ratings.forEach(r -> doubleRatings.add(r.getRating()));
        int midPoint = doubleRatings.size() / 2;
        double median = doubleRatings.get(midPoint);
        stats.setMedian(median);

        SynchronizedSummaryStatistics summaryStats = new SynchronizedSummaryStatistics();
        ratings.forEach(r -> summaryStats.addValue((double) r.getRating()));

        stats.setMin((int) summaryStats.getMin());
        stats.setMax((int) summaryStats.getMax());
        stats.setMean(summaryStats.getMean());

        return stats;
    }

}
