package com.manorllc.beerRate.db;

import java.util.Map;
import java.util.Optional;

import com.manorllc.beerRate.model.Rating;
import com.manorllc.beerRate.model.Stats;

public interface BeerRatingDatabase {

    public void putRating(final Rating rating);

    public Optional<Stats> getStats(final String beerName);

    public Map<String, Stats> getAllStats();

}
