package com.manorllc.beerRate.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.joda.time.DateTime;

import com.manorllc.beerRate.model.Beer;
import com.manorllc.beerRate.model.Rating;
import com.manorllc.beerRate.model.Team;
import com.manorllc.beerRate.util.Utils;

/**
 * Simple implementation of the database in memory. Note: Not thread safe! Not
 * optimized. Many queries require long iterations of entire database. Ok for
 * small number of items.
 *
 */
public class Database {

    // Model objects
    private ConcurrentMap<UUID, DbCategory> categories = new ConcurrentHashMap<>();
    private ConcurrentMap<UUID, DbBeer> beers = new ConcurrentHashMap<>();
    private ConcurrentMap<UUID, DbTeam> teams = new ConcurrentHashMap<>();
    private ConcurrentMap<UUID, DbUser> users = new ConcurrentHashMap<>();
    private ConcurrentMap<UUID, DbRating> ratings = new ConcurrentHashMap<>();
    private DateTime gameMarker = null;

    // Model relationships

    // Team ID -> user ID of captain
    private ConcurrentMap<UUID, UUID> captains = new ConcurrentHashMap<>();

    // Category ID -> Collection of beer IDs
    private ConcurrentMap<UUID, Collection<UUID>> beersByCategory = new ConcurrentHashMap<>();

    // User ID -> Beer ID -> Rating ID
    private ConcurrentMap<UUID, Map<UUID, UUID>> userRatings = new ConcurrentHashMap<>();

    // Team ID -> Collection of User IDs
    private ConcurrentMap<UUID, Set<UUID>> usersByTeam = new ConcurrentHashMap<>();

    private Optional<UUID> getCategoryId(final String categoryName) {
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

    private Optional<UUID> getBeerId(final String beerName) {
        for (Entry<UUID, DbBeer> beerEntry : beers.entrySet()) {
            UUID id = beerEntry.getKey();
            DbBeer beer = beerEntry.getValue();
            if (beer.getName().equals(beerName)) {
                return Optional.of(id);
            }
        }
        return Optional.empty();
    }

    public Optional<Beer> getBeer(final String beerName) {
        Optional<UUID> beerIdOpt = getBeerId(beerName);
        if (beerIdOpt.isPresent()) {
            UUID beerId = beerIdOpt.get();
            DbBeer dbBeer = beers.get(beerId);

            DbCategory category = null;
            for (Entry<UUID, Collection<UUID>> entry : beersByCategory.entrySet()) {
                UUID categoryId = entry.getKey();
                if (entry.getValue().stream().collect(Collectors.toSet()).contains(beerId)) {
                    category = categories.get(categoryId);
                    break;
                }

            }
            return Optional.of(Utils.createBeer(dbBeer, category.getName()));
        }
        return Optional.empty();
    }

    public void addBeer(final String categoryName, final DbBeer dbBeer) {
        Optional<UUID> categoryOpt = getCategoryId(categoryName);
        if (!categoryOpt.isPresent()) {
            throw new RuntimeException(String.format("Category %s does not exists", categoryName));
        }
        UUID categoryId = categoryOpt.get();

        if (beerExists(dbBeer.getName())) {
            // TODO: Consider overwriting
            throw new RuntimeException(String.format("Beer %s already exists", dbBeer.getName()));
        } else {
            UUID beerId = UUID.randomUUID();
            beers.put(beerId, dbBeer);
            beersByCategory.get(categoryId).add(beerId);
        }
    }

    /**
     * Returns a map of beer category -> collection of beers in that category
     * 
     * @return a map of beer category -> collection of beers in that category
     */
    public Map<String, List<Beer>> getBeersByCategory() {
        Map<String, List<Beer>> beerMap = new HashMap<>();
        for (Entry<UUID, Collection<UUID>> entry : beersByCategory.entrySet()) {
            UUID categoryId = entry.getKey();
            String categoryName = categories.get(categoryId).getName();
            beerMap.put(categoryName, getBeersForCategory(categoryName));
        }

        return beerMap;
    }

    public Map<String, String> getBeerToCategory() {
        Map<String, String> beerToCategory = new HashMap<>();

        for (Entry<String, List<Beer>> entry : getBeersByCategory().entrySet()) {
            String categoryName = entry.getKey();
            for (Beer beer : entry.getValue()) {
                beerToCategory.put(beer.getName(), categoryName);
            }
        }

        return beerToCategory;
    }

    public List<Beer> getBeersForCategory(final String categoryName) {
        Optional<UUID> categoryOpt = getCategoryId(categoryName);
        if (!categoryOpt.isPresent()) {
            throw new RuntimeException(String.format("Category %s does not exists", categoryName));
        }
        UUID categoryId = categoryOpt.get();

        List<Beer> beerList = new ArrayList<>();
        beersByCategory.get(categoryId).forEach(beerId -> {
            beerList.add(Utils.createBeer(beers.get(beerId), categoryName));
        });

        beerList.sort((b1, b2) -> {
            return b1.getName().compareTo(b2.getName());
        });

        return beerList;
    }

    private Optional<UUID> getTeamId(final String teamName) {
        for (Entry<UUID, DbTeam> teamEntry : teams.entrySet()) {
            UUID id = teamEntry.getKey();
            DbTeam team = teamEntry.getValue();
            if (team.getName().equals(teamName)) {
                return Optional.of(id);
            }
        }
        return Optional.empty();
    }

    public Optional<Team> getTeam(final String teamName) {
        Optional<UUID> teamIdOpt = getTeamId(teamName);
        if (teamIdOpt.isPresent()) {
            Team team = new Team();
            team.setName(teams.get(teamIdOpt.get()).getName());

            if (captains.containsKey(teamIdOpt.get())) {
                DbUser captain = users.get(captains.get(teamIdOpt.get()));
                team.setCaptain(Utils.getFullName(captain));
            }

            usersByTeam.get(teamIdOpt.get()).forEach(userId -> {
                team.addMember(Utils.getFullName(users.get(userId)));

            });

            return Optional.of(team);
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
    private Optional<UUID> getUserId(final String firstName, final String lastName) {
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
        Optional<Team> teamOpt = getTeamForUser(firstName, lastName);
        if (teamOpt.isPresent()) {
            throw new RuntimeException(String.format("User %s %s already belongs to team %s", firstName,
                    lastName, teamOpt.get()));
        }

        usersByTeam.get(teamId).add(userId);
    }

    public void removeUserFromTeam(final String firstName, final String lastName) {
        Optional<Team> teamOpt = getTeamForUser(firstName, lastName);
        if (!teamOpt.isPresent()) {
            throw new RuntimeException(String.format("User %s %s is not on a team", firstName,
                    lastName));
        }
        UUID teamId = getTeamId(teamOpt.get().getName()).get();
        UUID thisUserId = getUserId(firstName, lastName).get();

        usersByTeam.get(teamId).remove(thisUserId);
    }

    public Collection<DbUser> getUsers() {
        return users.values();
    }

    public Optional<Team> getTeamForUser(final String firstName, final String lastName) {
        Optional<UUID> userIdOpt = getUserId(firstName, lastName);
        if (!userIdOpt.isPresent()) {
            throw new RuntimeException(String.format("User %s %s does not exists", firstName, lastName));
        }
        UUID userId = userIdOpt.get();

        for (Entry<UUID, Set<UUID>> entry : usersByTeam.entrySet()) {
            UUID currentTeamId = entry.getKey();
            for (UUID currentUserId : entry.getValue()) {
                if (currentUserId.equals(userId)) {
                    return getTeam(teams.get(currentTeamId).getName());
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

    public List<Team> getTeams() {
        List<Team> teamList = teams.values().stream().map((Function<DbTeam, Team>) dbTeam -> {
            return getTeam(dbTeam.getName()).get();
        }).collect(Collectors.toList());

        teamList.sort((t1, t2) -> {
            return t1.getName().compareTo(t2.getName());
        });

        return teamList;
    }

    /**
     * Returns all ratings sorted by creation time.
     */
    public List<Rating> getRatings() {
        List<Rating> allRatings = new ArrayList<>();

        for (Entry<UUID, Map<UUID, UUID>> entry : userRatings.entrySet()) {
            UUID userId = entry.getKey();
            for (Entry<UUID, UUID> beerIdToRating : entry.getValue().entrySet()) {
                UUID beerId = beerIdToRating.getKey();
                UUID ratingId = beerIdToRating.getValue();
                allRatings.add(createRating(beerId, ratingId, userId));
            }
        }

        allRatings.sort((r1, r2) -> {
            return r1.getCreated().compareTo(r2.getCreated());
        });

        return allRatings;
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
    public List<Rating> getRatingsForBeer(final String beerName) {
        Optional<UUID> beerIdOpt = getBeerId(beerName);
        if (!beerIdOpt.isPresent()) {
            throw new RuntimeException(String.format("Beer %s does not exist", beerName));
        }
        UUID beerId = beerIdOpt.get();

        List<Rating> beerRatings = new ArrayList<>();
        for (Entry<UUID, Map<UUID, UUID>> userRating : userRatings.entrySet()) {
            UUID userId = userRating.getKey();
            for (Entry<UUID, UUID> beerRating : userRating.getValue().entrySet()) {
                UUID thisBeerId = beerRating.getKey();
                UUID ratingId = beerRating.getValue();
                if (beerId.equals(thisBeerId)) {
                    beerRatings.add(createRating(thisBeerId, ratingId, userId));
                }
            }
        }

        beerRatings.sort((r1, r2) -> {
            return r1.getCreated().compareTo(r2.getCreated());
        });

        return beerRatings;
    }

    public void setTeamCaptain(final String firstName, final String lastName) {
        Optional<UUID> userIdOpt = getUserId(firstName, lastName);
        if (!userIdOpt.isPresent()) {
            throw new RuntimeException(String.format("User %s %s does not exist", firstName, lastName));
        }
        UUID userId = userIdOpt.get();

        Optional<Team> teamOpt = getTeamForUser(firstName, lastName);
        if (!teamOpt.isPresent()) {
            throw new RuntimeException(String.format("User %s %s does not belong to a team", firstName, lastName));
        }
        UUID teamId = getTeamId(teamOpt.get().getName()).get();

        captains.put(teamId, userId);
    }

    public void setGameMarker() {
        gameMarker = DateTime.now();
    }

    public DateTime getGameMarker() {
        return gameMarker;
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

    private Rating createRating(UUID beerId, UUID ratingId, UUID userId) {
        Rating rating = new Rating();
        rating.setBeerName(beers.get(beerId).getName());
        rating.setRating(ratings.get(ratingId).getRating());
        rating.setUpdated(ratings.get(ratingId).getUpdated());
        rating.setCreated(ratings.get(ratingId).getCreated());
        rating.setUserName(Utils.getFullName(users.get(userId)));
        return rating;
    }

}
