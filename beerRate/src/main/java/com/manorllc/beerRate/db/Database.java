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

/**
 * Simple implementation of the database in memory. Note: Not thread safe! Not
 * optimized. Many queries require long iterations of entire database. Ok for
 * small number of items.
 *
 */
public class Database {

    // Model objects
    private Map<UUID, DbCategory> categories = new HashMap<>();
    private Map<UUID, DbBeer> beers = new HashMap<>();
    private Map<UUID, DbTeam> teams = new HashMap<>();
    private Map<UUID, DbUser> users = new HashMap<>();
    private Map<UUID, DbRating> ratings = new HashMap<>();

    // Model relationships

    // Category ID -> Collection of beer IDs
    private Map<UUID, Collection<UUID>> beersByCategory = new HashMap<>();

    // User ID -> Beer ID -> Rating ID
    private Map<UUID, Map<UUID, UUID>> userRatings = new HashMap<>();

    // Team ID -> Collection of User IDs
    private Map<UUID, Set<UUID>> usersByTeam = new HashMap<>();

    public Optional<UUID> getCategoryId(final String categoryName) {
        for (Entry<UUID, DbCategory> categoryEntry : categories.entrySet()) {
            UUID id = categoryEntry.getKey();
            DbCategory category = categoryEntry.getValue();
            if (category.getName().equals(categoryName)) {
                return Optional.of(id);
            }
        }
        return Optional.empty();
    }

    public void addCategory(final String categoryName) {
        if (categoryExists(categoryName)) {
            throw new RuntimeException(String.format("Category %s already exists", categoryName));
        }
        UUID id = UUID.randomUUID();
        categories.put(id, new DbCategory(categoryName));
        beersByCategory.put(id, new HashSet<>());
    }

    public Optional<UUID> getBeerId(final String beerName) {
        for (Entry<UUID, DbBeer> beerEntry : beers.entrySet()) {
            UUID id = beerEntry.getKey();
            DbBeer beer = beerEntry.getValue();
            if (beer.getName().equals(beerName)) {
                return Optional.of(id);
            }
        }
        return Optional.empty();
    }

    public void addBeer(final String categoryName, final DbBeer beer) {
        Optional<UUID> categoryOpt = getCategoryId(categoryName);
        if (!categoryOpt.isPresent()) {
            throw new RuntimeException(String.format("Category %s does not exists", categoryName));
        }
        UUID categoryId = categoryOpt.get();

        if (beerExists(beer.getName())) {
            // TODO: Consider overwriting
            throw new RuntimeException(String.format("Beer %s already exists", beer.getName()));
        } else {
            UUID beerId = UUID.randomUUID();
            beers.put(beerId, beer);
            beersByCategory.get(categoryId).add(beerId);
        }
    }

    public Optional<DbBeer> getBeer(final String beerName) {
        Optional<UUID> beerIdOpt = getBeerId(beerName);
        if (beerIdOpt.isPresent()) {
            return Optional.of(beers.get(beerIdOpt.get()));
        }
        return Optional.empty();
    }

    /**
     * Returns a map of beer category -> collection of beers in that category
     * 
     * @return a map of beer category -> collection of beers in that category
     */
    public Map<String, Collection<DbBeer>> getBeersByCategory() {
        Map<String, Collection<DbBeer>> beerMap = new HashMap<>();
        for (Entry<UUID, Collection<UUID>> entry : beersByCategory.entrySet()) {
            UUID categoryId = entry.getKey();
            String categoryName = categories.get(categoryId).getName();
            beerMap.put(categoryName, getBeersForCategory(categoryName));
        }

        return beerMap;
    }

    public Collection<DbBeer> getBeersForCategory(final String categoryName) {
        Optional<UUID> categoryOpt = getCategoryId(categoryName);
        if (!categoryOpt.isPresent()) {
            throw new RuntimeException(String.format("Category %s does not exists", categoryName));
        }
        UUID categoryId = categoryOpt.get();

        Collection<DbBeer> beerCollection = new HashSet<>();
        beersByCategory.get(categoryId).forEach(beerId -> {
            beerCollection.add(beers.get(beerId));
        });

        return beerCollection;
    }

    public Optional<UUID> getTeamId(final String teamName) {
        for (Entry<UUID, DbTeam> teamEntry : teams.entrySet()) {
            UUID id = teamEntry.getKey();
            DbTeam team = teamEntry.getValue();
            if (team.getName().equals(teamName)) {
                return Optional.of(id);
            }
        }
        return Optional.empty();
    }

    public Optional<DbTeam> getTeam(final String teamName) {
        Optional<UUID> teamIdOpt = getTeamId(teamName);
        if (teamIdOpt.isPresent()) {
            return Optional.of(teams.get(teamIdOpt.get()));
        }
        return Optional.empty();
    }

    public void addTeam(final String teamName) {
        if (teamExists(teamName)) {
            throw new RuntimeException(String.format("Team %s already exists", teamName));
        }
        UUID id = UUID.randomUUID();
        teams.put(id, new DbTeam(teamName));
        usersByTeam.put(id, new HashSet<>());
    }

    // Assumes first and last name combination is unique
    public Optional<UUID> getUserId(final String firstName, final String lastName) {
        for (Entry<UUID, DbUser> userEntry : users.entrySet()) {
            UUID id = userEntry.getKey();
            DbUser user = userEntry.getValue();
            if (user.getFirstName().equals(firstName) && user.getLastName().equals(lastName)) {
                return Optional.of(id);
            }
        }
        return Optional.empty();
    }

    public void addUser(final DbUser user) {
        if (userExists(user.getFirstName(), user.getLastName())) {
            // TODO: Consider overwriting
            throw new RuntimeException(
                    String.format("User %s %s already exists", user.getFirstName(), user.getFirstName()));
        } else {
            UUID userId = UUID.randomUUID();
            users.put(userId, user);
        }
    }

    public Optional<DbUser> getUser(final String firstName, final String lastName) {
        Optional<UUID> userIdOpt = getUserId(firstName, lastName);
        if (userIdOpt.isPresent()) {
            return Optional.of(users.get(userIdOpt.get()));
        }
        return Optional.empty();
    }

    public void addUserToTeam(final String teamName, final String firstName, final String lastName) {
        Optional<UUID> userIdOpt = getUserId(firstName, lastName);
        if (!userIdOpt.isPresent()) {
            throw new RuntimeException(String.format("User %s %s does not exists", firstName, lastName));
        }
        UUID userId = userIdOpt.get();
        Optional<UUID> teamIdOpt = getTeamId(teamName);
        if (!teamIdOpt.isPresent()) {
            throw new RuntimeException(String.format("Team %s does not exists", teamName));
        }
        UUID teamId = teamIdOpt.get();

        // Check if user belongs to another team
        Optional<String> teamOpt = getTeamForUser(firstName, lastName);
        if (teamOpt.isPresent()) {
            throw new RuntimeException(String.format("User %s %s already belongs to team %s", firstName,
                    lastName, teamOpt.get()));
        }

        usersByTeam.get(teamId).add(userId);
    }

    public void removeUserFromTeam(final String firstName, final String lastName) {
        Optional<String> teamOpt = getTeamForUser(firstName, lastName);
        if (!teamOpt.isPresent()) {
            throw new RuntimeException(String.format("User %s %s is not on a team", firstName,
                    lastName));
        }
        UUID teamId = getTeamId(teamOpt.get()).get();
        UUID thisUserId = getUserId(firstName, lastName).get();

        usersByTeam.get(teamId).forEach(userId -> {
            if (userId.equals(thisUserId)) {
                usersByTeam.get(teamId).remove(thisUserId);
            }
        });
    }

    public Collection<DbUser> getUsers() {
        return users.values();
    }

    public Optional<String> getTeamForUser(final String firstName, final String lastName) {
        Optional<UUID> userIdOpt = getUserId(firstName, lastName);
        if (!userIdOpt.isPresent()) {
            throw new RuntimeException(String.format("User %s %s does not exists", firstName, lastName));
        }
        UUID userId = userIdOpt.get();

        for (Entry<UUID, Set<UUID>> entry : usersByTeam.entrySet()) {
            UUID currentTeamId = entry.getKey();
            for (UUID currentUserId : entry.getValue()) {
                if (currentUserId.equals(userId)) {
                    return Optional.of(teams.get(currentTeamId).getName());
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Returns a map of team -> collection of users on that team
     * 
     * @return a map of team -> collection of users on that team
     */
    public Map<String, Collection<DbUser>> getUsersByTeam() {
        Map<String, Collection<DbUser>> userMap = new HashMap<>();
        for (Entry<UUID, Set<UUID>> entry : usersByTeam.entrySet()) {
            UUID teamId = entry.getKey();
            String teamName = teams.get(teamId).getName();
            userMap.put(teamName, getUsersForTeam(teamName));
        }

        return userMap;
    }

    public Collection<DbUser> getUsersForTeam(final String teamName) {
        Optional<UUID> teamOpt = getTeamId(teamName);
        if (!teamOpt.isPresent()) {
            throw new RuntimeException(String.format("Team %s does not exists", teamName));
        }
        UUID teamId = teamOpt.get();

        Collection<DbUser> userCollection = new HashSet<>();
        usersByTeam.get(teamId).forEach(userId -> {
            userCollection.add(users.get(userId));
        });

        return userCollection;
    }

    public Collection<DbTeam> getTeams() {
        return teams.values();
    }

    public void addRating(final String firstName, final String lastName, final String beerName,
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
            DbRating rating = new DbRating(ratingValue, DateTime.now());
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
    public Map<String, DbRating> getRatingsForBeer(final String beerName) {
        Optional<UUID> beerIdOpt = getBeerId(beerName);
        if (!beerIdOpt.isPresent()) {
            throw new RuntimeException(String.format("Beer %s does not exist", beerName));
        }
        UUID beerId = beerIdOpt.get();

        Map<String, DbRating> ratingMap = new HashMap<>();
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
    public Map<String, DbRating> getRatingsForUser(final String firstName, final String lastName) {
        Optional<UUID> userIdOpt = getUserId(firstName, lastName);
        if (!userIdOpt.isPresent()) {
            throw new RuntimeException(String.format("User %s %s does not exist", firstName, lastName));
        }
        UUID userId = userIdOpt.get();

        Map<String, DbRating> ratingMap = new HashMap<>();
        for (Entry<UUID, UUID> beerRating : userRatings.get(userId).entrySet()) {
            UUID beerId = beerRating.getKey();
            UUID ratingId = beerRating.getValue();
            ratingMap.put(beers.get(beerId).getName(), ratings.get(ratingId));
        }
        return ratingMap;
    }

    public boolean categoryExists(final String categoryName) {
        return getCategoryId(categoryName).isPresent();
    }

    public boolean beerExists(final String beerName) {
        return getBeerId(beerName).isPresent();
    }

    public boolean teamExists(final String teamName) {
        return getTeamId(teamName).isPresent();
    }

    public boolean userExists(final String firstName, final String lastName) {
        return getUserId(firstName, lastName).isPresent();
    }

}
