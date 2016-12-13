package com.manorllc.beerRate.db;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.joda.time.DateTime;

import com.manorllc.beerRate.model.Beer;
import com.manorllc.beerRate.model.BeerCategory;
import com.manorllc.beerRate.model.Generation;
import com.manorllc.beerRate.model.Rating;
import com.manorllc.beerRate.model.User;

/**
 * Simple implementation of the database in memory. Note: Not thread safe! Not
 * optimized. Many queries require long iterations of entire database. Ok for
 * small number of items.
 *
 */
public class InMemoryBeerRatingDatabase {

    // Rating ID -> Rating
    private Map<UUID, Rating> ratings = new HashMap<>();

    // Beer ID -> Beer
    private Map<UUID, Beer> beers = new HashMap<>();

    // User ID -> User
    private Map<UUID, User> users = new HashMap<>();

    // User ID -> Beer ID -> Rating IDs
    private Map<UUID, Map<UUID, UUID>> userRatings = new HashMap<>();

    // Team ID -> Collection of User IDs
    private Map<UUID, Collection<UUID>> teamMembers = new HashMap<>();
    // Team ID -> name
    private Map<UUID, String> teamNames = new HashMap<>();

    public Optional<UUID> getBeerId(final String beerName) {
        for (Entry<UUID, Beer> beerEntry : beers.entrySet()) {
            UUID id = beerEntry.getKey();
            Beer beer = beerEntry.getValue();
            if (beer.getName().equals(beerName)) {
                return Optional.of(id);
            }
        }
        return Optional.empty();
    }

    public void putBeer(final Beer beer) {
        if (beerExists(beer.getName())) {
            // TODO: Consider overwriting
            throw new RuntimeException("Beer already exists");
        } else {
            beers.put(UUID.randomUUID(), beer);
        }
    }

    public Optional<Beer> getBeer(final String beerName) {
        for (Beer beer : beers.values()) {
            if (beer.getName().equals(beerName)) {
                return Optional.of(beer);
            }
        }
        return Optional.empty();
    }

    /**
     * Returns a map of beer category -> collection of beers in that category
     * 
     * @return a map of beer category -> collection of beers in that category
     */
    public Map<BeerCategory, Collection<Beer>> getBeers() {
        Map<BeerCategory, Collection<Beer>> beerMap = new HashMap<>();
        for (BeerCategory category : BeerCategory.values()) {
            beerMap.put(category, new HashSet<>());
        }
        for (Beer beer : beers.values()) {
            beerMap.get(beer.getCategory()).add(beer);
        }
        return beerMap;
    }

    public Collection<Beer> getBeers(final BeerCategory category) {
        Set<Beer> beerSet = new HashSet<>();
        for (Beer beer : beers.values()) {
            if (beer.getCategory().equals(category)) {
                beerSet.add(beer);
            }
        }
        return beerSet;
    }

    // Assumes first and last name combination is unique
    public Optional<UUID> getUserId(final String firstName, final String lastName) {
        for (Entry<UUID, User> userEntry : users.entrySet()) {
            UUID id = userEntry.getKey();
            User user = userEntry.getValue();
            if (user.getFirstName().equals(firstName) && user.getLastName().equals(lastName)) {
                return Optional.of(id);
            }
        }
        return Optional.empty();
    }

    public void putUser(final User user) {
        if (userExists(user.getFirstName(), user.getLastName())) {
            // TODO: Consider overwriting
            throw new RuntimeException("User already exists");
        } else {
            users.put(UUID.randomUUID(), user);
        }
    }

    public Optional<User> getUser(final String firstName, final String lastName) {
        for (User user : users.values()) {
            if (user.getFirstName().equals(firstName) && user.getLastName().equals(lastName)) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    public Collection<User> getUsers(final Generation generation) {
        Set<User> userSet = new HashSet<>();
        for (User user : users.values()) {
            if (user.getGeneration().equals(generation)) {
                userSet.add(user);
            }
        }
        return userSet;
    }

    public void putRating(final String firstName, final String lastName, final String beerName,
            final double ratingValue) {
        Optional<UUID> userIdOpt = getUserId(firstName, lastName);
        if (!userIdOpt.isPresent()) {
            throw new RuntimeException(String.format("User %s %s does not exist", firstName, lastName));
        }
        Optional<UUID> beerIdOpt = getBeerId(beerName);
        if (!beerIdOpt.isPresent()) {
            throw new RuntimeException(String.format("Beer %s does not exist", beerName));
        }

        UUID userId = userIdOpt.get();
        UUID beerId = beerIdOpt.get();

        if (!userRatings.containsKey(userId)) {
            userRatings.put(userId, new HashMap<>());
        }
        if (userRatings.get(userId).containsKey(beerId)) {
            // Update the rating if it already exists
            UUID ratingId = userRatings.get(userId).get(beerId);
            ratings.get(ratingId).setUpdated(DateTime.now());
            ratings.get(ratingId).setRating(ratingValue);
        } else {
            // Create a new rating if it doesn't exist yet
            UUID ratingId = UUID.randomUUID();
            userRatings.get(userId).put(beerId, ratingId);
            Rating rating = new Rating(ratingValue, DateTime.now());
            ratings.put(ratingId, rating);
        }
    }

    /**
     * Returns a map of "firstName lastName" -> rating.
     * 
     * @param beerName
     *            the name of the beer to get ratings for
     * @return a map of user name to its rating
     */
    public Map<String, Rating> getRatingsForBeer(final String beerName) {
        Optional<UUID> beerIdOpt = getBeerId(beerName);
        if (!beerIdOpt.isPresent()) {
            throw new RuntimeException(String.format("Beer %s does not exist", beerName));
        }
        UUID beerId = beerIdOpt.get();

        Map<String, Rating> ratingMap = new HashMap<>();
        for (Entry<UUID, Map<UUID, UUID>> userRating : userRatings.entrySet()) {
            UUID userId = userRating.getKey();
            for (Entry<UUID, UUID> beerRating : userRating.getValue().entrySet()) {
                UUID thisBeerId = beerRating.getKey();
                UUID ratingId = beerRating.getValue();
                if (beerId.equals(thisBeerId)) {
                    String fullName = users.get(userId).getFirstName() + " " + users.get(userId).getLastName();
                    ratingMap.put(fullName, ratings.get(ratingId));
                }
            }
        }
        return ratingMap;
    }

    /**
     * Returns a map of beer name to rating.
     * 
     * @param firstName
     *            the user's first name
     * @param lastName
     *            the user's last name
     * @return a map of beer name to its rating
     */
    public Map<String, Rating> getRatingsForUser(final String firstName, final String lastName) {
        Optional<UUID> userIdOpt = getUserId(firstName, lastName);
        if (!userIdOpt.isPresent()) {
            throw new RuntimeException(String.format("User %s %s does not exist", firstName, lastName));
        }
        UUID userId = userIdOpt.get();

        Map<String, Rating> ratingMap = new HashMap<>();
        for (Entry<UUID, UUID> beerRating : userRatings.get(userId).entrySet()) {
            UUID beerId = beerRating.getKey();
            UUID ratingId = beerRating.getValue();
            ratingMap.put(beers.get(beerId).getName(), ratings.get(ratingId));
        }
        return ratingMap;
    }

    public boolean beerExists(final String beerName) {
        return getBeerId(beerName).isPresent();
    }

    public boolean userExists(final String firstName, final String lastName) {
        return getUserId(firstName, lastName).isPresent();
    }

}
