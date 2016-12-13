package com.manorllc.beerRate.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.math3.stat.descriptive.SynchronizedSummaryStatistics;

import com.manorllc.beerRate.model.Rating;
import com.manorllc.beerRate.model.Stats;

public class DBQueries {

    private final InMemoryBeerRatingDatabase db;

    public DBQueries(final InMemoryBeerRatingDatabase db) {
        this.db = db;
    }

    public Optional<Stats> getStats(final String beerName) {
        if (!db.beerExists(beerName)) {
            return Optional.empty();
        } else {
            Stats stats = new Stats();
            Collection<Rating> beerRatings = db.getRatingsForBeer(beerName).values();
            stats.setCount(beerRatings.size());

            List<Double> doubleRatings = new ArrayList<>();
            beerRatings.forEach(r -> doubleRatings.add(r.getRating()));
            int midPoint = doubleRatings.size() / 2;
            double median = doubleRatings.get(midPoint);
            stats.setMedian(median);

            SynchronizedSummaryStatistics summaryStats = new SynchronizedSummaryStatistics();
            beerRatings.forEach(r -> summaryStats.addValue((double) r.getRating()));

            stats.setMin((int) summaryStats.getMin());
            stats.setMax((int) summaryStats.getMax());
            stats.setMean(summaryStats.getMean());
            return Optional.of(stats);
        }
    }

    // TODO: Make this categorize by style
    public Map<String, Stats> getAllStats() {
        Map<String, Stats> allStats = new HashMap<>();
        beers.values().forEach(beer -> {
            String beerName = beer.getName();
            allStats.put(beerName, getStats(beerName).get());
        });
        return allStats;
    }

}
