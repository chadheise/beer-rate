package com.manorllc.beerRate.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.math3.stat.descriptive.SynchronizedSummaryStatistics;

import com.manorllc.beerRate.model.Rating;
import com.manorllc.beerRate.model.Stats;

/**
 * Simple implementation of the database in memory. Note: Not thread safe!
 *
 */
public class InMemoryBeerRatingDatabase implements BeerRatingDatabase {

    Map<String, List<Rating>> ratings = new HashMap<>();

    @Override
    public void putRating(Rating rating) {
        if (!ratings.containsKey(rating.getBeer())) {
            ratings.put(rating.getBeer(), new ArrayList<>());
        }
        ratings.get(rating.getBeer()).add(rating);
    }

    @Override
    public Optional<Stats> getStats(String beerName) {
        if (!ratings.containsKey(beerName)) {
            return Optional.empty();
        } else {
            Stats stats = new Stats();
            List<Rating> beerRatings = ratings.get(beerName);
            stats.setCount(beerRatings.size());

            List<Integer> integerRatings = new ArrayList<>();
            beerRatings.forEach(r -> integerRatings.add(r.getRating()));
            int midPoint = integerRatings.size() / 2;
            int median = integerRatings.get(midPoint);
            stats.setMedian(median);

            SynchronizedSummaryStatistics summaryStats = new SynchronizedSummaryStatistics();
            beerRatings.forEach(r -> summaryStats.addValue((double) r.getRating()));

            stats.setMin((int) summaryStats.getMin());
            stats.setMax((int) summaryStats.getMax());
            stats.setMean(summaryStats.getMean());
            return Optional.of(stats);
        }
    }

    @Override
    public Map<String, Stats> getAllStats() {
        Map<String, Stats> allStats = new HashMap<>();
        ratings.keySet().forEach(r -> allStats.put(r, getStats(r).get()));
        return allStats;
    }

}
