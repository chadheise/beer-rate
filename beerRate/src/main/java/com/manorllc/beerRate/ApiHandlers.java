package com.manorllc.beerRate;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.manorllc.beerRate.db.Database;
import com.manorllc.beerRate.db.DatabaseQueries;
import com.manorllc.beerRate.db.DbBeer;
import com.manorllc.beerRate.db.DbUser;
import com.manorllc.beerRate.model.Beer;
import com.manorllc.beerRate.model.Generation;
import com.manorllc.beerRate.model.Rating;
import com.manorllc.beerRate.model.Stats;
import com.manorllc.beerRate.model.Team;
import com.manorllc.beerRate.util.Utils;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class ApiHandlers {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiHandlers.class);

    private static final double MIN_RATING = 0;
    private static final double MAX_RATING = 5;

    private final Database db;
    private final DatabaseQueries queries;

    public ApiHandlers(final Database db, final DatabaseQueries queries) {
        this.db = db;
        this.queries = queries;
    }

    public void putCategory(final RoutingContext ctx) {
        HttpServerResponse response = ctx.response();
        try {
            String categoryName = URLDecoder.decode(ctx.request().getParam(HttpConstants.PARAM_CATEGORY),
                    HttpConstants.ENCODING);

            if (db.categoryExists(categoryName)) {
                writeResponse(response, HttpResponseStatus.BAD_REQUEST);
                response.end(String.format("Category %s already exist", categoryName));
            } else {
                db.addCategory(categoryName);
                writeResponse(response, HttpResponseStatus.CREATED);
                response.end();
            }
        } catch (UnsupportedEncodingException e) {
            writeResponse(response, HttpResponseStatus.INTERNAL_SERVER_ERROR);
            response.end();
        }
    }

    public void getAllBeers(final RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();
        writeResponse(response, HttpResponseStatus.OK);
        response.putHeader(HttpConstants.HEADER_KEY_CONTENT_TYPE, HttpConstants.HEADER_VALUE_JSON);
        response.end(Json.encodePrettily(db.getBeersByCategory()));
    }

    public void putBeer(final RoutingContext ctx) {
        HttpServerResponse response = ctx.response();

        try {
            String category = URLDecoder.decode(ctx.request().getParam(HttpConstants.PARAM_CATEGORY),
                    HttpConstants.ENCODING);
            if (!db.categoryExists(category)) {
                writeResponse(response, HttpResponseStatus.BAD_REQUEST);
                response.end(String.format("Category %s does not exist", category));
            } else {
                DbBeer newBeer = Json.decodeValue(ctx.getBodyAsJson().toString(), DbBeer.class);

                if (db.beerExists(newBeer.getName())) {
                    writeResponse(response, HttpResponseStatus.BAD_REQUEST);
                    response.end(String.format("Beer %s already exist", newBeer.getName()));
                } else {
                    db.addBeer(category, newBeer);
                    writeResponse(response, HttpResponseStatus.CREATED);
                    response.end();
                }
            }
        } catch (UnsupportedEncodingException e) {
            writeResponse(response, HttpResponseStatus.INTERNAL_SERVER_ERROR);
            response.end();
        }
    }

    public void getAllUsers(final RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();
        writeResponse(response, HttpResponseStatus.OK);
        Collection<JsonObject> userCollection = new HashSet<>();
        db.getUsers().forEach(u -> {
            JsonObject j = new JsonObject();
            j.put("firstName", u.getFirstName());
            j.put("lastName", u.getLastName());
            j.put("generation", u.getGeneration());
            Optional<Team> teamOpt = db.getTeamForUser(u.getFirstName(), u.getLastName());
            if (teamOpt.isPresent()) {
                j.put("team", teamOpt.get().getName());
            } else {
                j.put("team", "None");
            }
            userCollection.add(j);
        });
        response.putHeader(HttpConstants.HEADER_KEY_CONTENT_TYPE, HttpConstants.HEADER_VALUE_JSON);
        response.end(Json.encodePrettily(userCollection));
    }

    public void putUser(final RoutingContext ctx) {
        HttpServerResponse response = ctx.response();

        DbUser user = Json.decodeValue(ctx.getBodyAsJson().toString(), DbUser.class);
        if (db.userExists(user.getFirstName(), user.getLastName())) {
            writeResponse(response, HttpResponseStatus.BAD_REQUEST);
            response.end(String.format("User %s %s already exist", user.getFirstName(), user.getLastName()));
        } else {
            db.addUser(user);
            writeResponse(response, HttpResponseStatus.CREATED);
            response.end();
        }
    }

    public void getTeams(final RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();
        writeResponse(response, HttpResponseStatus.OK);
        response.putHeader(HttpConstants.HEADER_KEY_CONTENT_TYPE, HttpConstants.HEADER_VALUE_JSON);
        response.end(Json.encodePrettily(db.getTeams()));
    }

    public void putTeam(final RoutingContext ctx) {
        HttpServerResponse response = ctx.response();
        try {
            String teamName = URLDecoder.decode(ctx.request().getParam(HttpConstants.PARAM_TEAM),
                    HttpConstants.ENCODING);

            if (db.teamExists(teamName)) {
                writeResponse(response, HttpResponseStatus.BAD_REQUEST);
                response.end(String.format("Team %s already exist", teamName));
            } else {
                db.addTeam(teamName);
                writeResponse(response, HttpResponseStatus.CREATED);
                response.end();
            }
        } catch (UnsupportedEncodingException e) {
            writeResponse(response, HttpResponseStatus.INTERNAL_SERVER_ERROR);
            response.end();
        }
    }

    public void addUserToTeam(final RoutingContext ctx) {
        HttpServerResponse response = ctx.response();

        try {
            String teamName = URLDecoder.decode(ctx.request().getParam(HttpConstants.PARAM_TEAM),
                    HttpConstants.ENCODING);

            if (!db.teamExists(teamName)) {
                writeResponse(response, HttpResponseStatus.BAD_REQUEST);
                response.end(String.format("Team %s does not exist", teamName));
            } else {
                String firstName = URLDecoder.decode(ctx.request().getParam(HttpConstants.PARAM_FIRST_NAME),
                        HttpConstants.ENCODING);
                String lastName = URLDecoder.decode(ctx.request().getParam(HttpConstants.PARAM_LAST_NAME),
                        HttpConstants.ENCODING);

                if (!db.userExists(firstName, lastName)) {
                    writeResponse(response, HttpResponseStatus.BAD_REQUEST);
                    response.end(String.format("User %s %s does not exist", firstName, lastName));
                } else {
                    if (db.getTeamForUser(firstName, lastName).isPresent()) {
                        db.removeUserFromTeam(firstName, lastName);
                    }
                    db.addUserToTeam(teamName, firstName, lastName);
                    writeResponse(response, HttpResponseStatus.CREATED);
                    response.end();
                }
            }
        } catch (UnsupportedEncodingException e) {
            writeResponse(response, HttpResponseStatus.INTERNAL_SERVER_ERROR);
            response.end();
        }

    }

    public void getRatings(final RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();

        String beerName = routingContext.request().getParam(HttpConstants.PARAM_BEER);

        Optional<Stats> stats = queries.getStatsForBeer(beerName);
        if (stats.isPresent()) {
            writeResponse(response, HttpResponseStatus.OK);
            response.putHeader(HttpConstants.HEADER_KEY_CONTENT_TYPE, HttpConstants.HEADER_VALUE_JSON);
            response.end(Json.encodePrettily(stats.get()));
        } else {
            writeResponse(response, HttpResponseStatus.OK);
            response.putHeader(HttpConstants.HEADER_KEY_CONTENT_TYPE, HttpConstants.HEADER_VALUE_JSON);
            response.end("{}");
        }
    }

    public void getAllRatings(final RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();
        writeResponse(response, HttpResponseStatus.OK);
        response.putHeader(HttpConstants.HEADER_KEY_CONTENT_TYPE, HttpConstants.HEADER_VALUE_JSON);
        List<Rating> ratings = db.getRatings();
        response.end(Json.encodePrettily(ratings));
    }

    public void putRating(final RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();

        JsonObject bodyJson = routingContext.getBodyAsJson();

        String firstName = routingContext.request().getParam(HttpConstants.PARAM_FIRST_NAME);
        String lastName = routingContext.request().getParam(HttpConstants.PARAM_LAST_NAME);

        if (!bodyJson.containsKey("beer") || !bodyJson.containsKey("rating")) {
            writeResponse(response, HttpResponseStatus.BAD_REQUEST);
            response.end();
        } else {
            double rating = bodyJson.getDouble("rating");
            if (MIN_RATING > rating || rating > MAX_RATING) {
                writeResponse(response, HttpResponseStatus.BAD_REQUEST);
                response.putHeader(HttpConstants.HEADER_KEY_CONTENT_TYPE, HttpConstants.HEADER_VALUE_TEXT);
                response.end("Rating must be between 0 and 5");
            } else {
                db.addRating(firstName, lastName, bodyJson.getString("beer"), rating);

                writeResponse(response, HttpResponseStatus.CREATED);
                response.end();
            }
        }
    }

    public void getFirstRatingByCategory(final RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();

        // Category -> First rating in that category
        Map<String, Rating> firstRatings = new HashMap<>();
        Map<String, String> beerToCategory = db.getBeerToCategory();
        Set<String> firstUsers = new HashSet<>();
        List<Rating> ratings = db.getRatings();

        for (Rating rating : ratings) {
            String categoryName = beerToCategory.get(rating.getBeerName());
            if (!firstRatings.containsKey(categoryName) && !firstUsers.contains(rating.getUserName())) {
                firstRatings.put(categoryName, rating);
                firstUsers.add(rating.getUserName());
            }
        }

        writeResponse(response, HttpResponseStatus.OK);
        response.putHeader(HttpConstants.HEADER_KEY_CONTENT_TYPE, HttpConstants.HEADER_VALUE_JSON);
        response.end(Json.encodePrettily(firstRatings));
    }

    public void postRatingFromForm(final RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();

        routingContext.request().setExpectMultipart(true);
        routingContext.request().endHandler(v -> {
            String userName = routingContext.request().formAttributes().get("user");
            String firstName = userName.split(",")[1].trim();
            String lastName = userName.split(",")[0].trim();

            String beerName = routingContext.request().formAttributes().get("beer");
            double rating = Double.valueOf(routingContext.request().formAttributes().get("rating"));

            if (MIN_RATING > rating || rating > MAX_RATING) {
                writeResponse(response, HttpResponseStatus.BAD_REQUEST);
                response.putHeader(HttpConstants.HEADER_KEY_CONTENT_TYPE, HttpConstants.HEADER_VALUE_TEXT);
                response.end("Rating must be between 0 and 5");
            } else {
                // Put rating in DB
                db.addRating(firstName, lastName, beerName, rating);

                // Redirect to stats page
                routingContext.response().setStatusCode(HttpResponseStatus.FOUND.code());
                String redirectUrl = String.format("/ui/stats/%s/%s", beerName, rating);
                routingContext.response().headers().add("Location", redirectUrl);
                routingContext.response().end();
            }
        });
    }

    public void addTeamFromForm(final RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();

        routingContext.request().setExpectMultipart(true);
        routingContext.request().endHandler(v -> {
            String teamName = routingContext.request().formAttributes().get("teamName");

            if (db.teamExists(teamName)) {
                writeResponse(response, HttpResponseStatus.BAD_REQUEST);
                response.putHeader(HttpConstants.HEADER_KEY_CONTENT_TYPE, HttpConstants.HEADER_VALUE_TEXT);
                response.end(String.format("Team \"%s\" already exists", teamName));
            } else {
                db.addTeam(teamName);
                writeResponse(response, HttpResponseStatus.FOUND);
                response.putHeader("Location", "/ui/hostFormSuccess");
                response.end();
            }
        });
    }

    public void addUserFromForm(final RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();

        routingContext.request().setExpectMultipart(true);
        routingContext.request().endHandler(v -> {
            String firstName = routingContext.request().formAttributes().get("firstName");
            String lastName = routingContext.request().formAttributes().get("lastName");
            String generationString = routingContext.request().formAttributes().get("generation");
            Generation gen = Generation.valueOf(generationString);

            if (db.userExists(firstName, lastName)) {
                writeResponse(response, HttpResponseStatus.BAD_REQUEST);
                response.putHeader(HttpConstants.HEADER_KEY_CONTENT_TYPE, HttpConstants.HEADER_VALUE_TEXT);
                response.end(String.format("USer %s %s already exists", firstName, lastName));
            } else {
                db.addUser(new DbUser(firstName, lastName, gen));
                writeResponse(response, HttpResponseStatus.FOUND);
                response.putHeader("Location", "/ui/hostFormSuccess");
                response.end();
            }
        });
    }

    public void changeTeamFromForm(final RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();
        routingContext.request().setExpectMultipart(true);
        routingContext.request().endHandler(v -> {
            String userName = routingContext.request().formAttributes().get("user");
            String firstName = userName.split(",")[1].trim();
            String lastName = userName.split(",")[0].trim();
            String teamName = routingContext.request().formAttributes().get("team");

            if (db.getTeamForUser(firstName, lastName).isPresent()) {
                db.removeUserFromTeam(firstName, lastName);
            }
            db.addUserToTeam(teamName, firstName, lastName);
            writeResponse(response, HttpResponseStatus.FOUND);
            response.putHeader("Location", "/ui/hostFormSuccess");
            response.end();
        });
    }

    public void teamStatsFromForm(final RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();
        routingContext.request().setExpectMultipart(true);
        routingContext.request().endHandler(v -> {
            try {
                String teamName = routingContext.request().formAttributes().get("team");
                writeResponse(response, HttpResponseStatus.FOUND);
                response.putHeader("Location",
                        "/ui/host/teamStats/" + URLEncoder.encode(teamName, HttpConstants.ENCODING));
                response.end();
            } catch (UnsupportedEncodingException e) {
                writeResponse(response, HttpResponseStatus.INTERNAL_SERVER_ERROR);
                response.end();
            }

        });
    }

    public void setCaptainFromForm(final RoutingContext ctx) {
        HttpServerResponse response = ctx.response();
        ctx.request().setExpectMultipart(true);
        ctx.request().endHandler(v -> {
            String userName = ctx.request().formAttributes().get("user");
            String firstName = Utils.getFirstName(userName);
            String lastName = Utils.getLastName(userName);
            db.setTeamCaptain(firstName, lastName);

            writeResponse(response, HttpResponseStatus.FOUND);
            response.putHeader("Location", "/ui/hostFormSuccess");
            response.end();
        });
    }

    public void getOverallStats(final RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();
        writeResponse(response, HttpResponseStatus.OK);
        response.putHeader(HttpConstants.HEADER_KEY_CONTENT_TYPE, HttpConstants.HEADER_VALUE_JSON);
        response.end(Json.encodePrettily(queries.getStatsForAll()));
    }

    public void getAllStats(final RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();
        writeResponse(response, HttpResponseStatus.OK);
        response.putHeader(HttpConstants.HEADER_KEY_CONTENT_TYPE, HttpConstants.HEADER_VALUE_JSON);

        Map<String, Object> overallMap = new HashMap<>();
        Stats overallStats = queries.getStatsForAll();
        overallMap.put("stats", overallStats);

        Map<String, List<Beer>> beersByCategory = db.getBeersByCategory();
        for (Entry<String, List<Beer>> entry : beersByCategory.entrySet()) {
            Map<String, Object> categoryMap = new HashMap<>();
            categoryMap.put("stats", queries.getStatsForCategory(entry.getKey()).get());
            List<Beer> beerList = new ArrayList<Beer>(entry.getValue());
            beerList.sort((o1, o2) -> {
                return o1.getName().compareTo(o2.getName());
            });
            for (Beer dbBeer : beerList) {
                categoryMap.put(dbBeer.getName(), queries.getStatsForBeer(dbBeer.getName()).get());
            }
            overallMap.put(entry.getKey(), categoryMap);
        }

        response.end(Json.encodePrettily(overallMap));
    }

    private void writeResponse(final HttpServerResponse response, final HttpResponseStatus status) {
        response.setStatusCode(status.code());
        response.setStatusMessage(status.reasonPhrase());
    }

}
