package com.manorllc.beerRate;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import com.manorllc.beerRate.db.Database;
import com.manorllc.beerRate.db.DatabaseQueries;
import com.manorllc.beerRate.db.DbBeer;
import com.manorllc.beerRate.db.DbRating;
import com.manorllc.beerRate.db.DbUser;
import com.manorllc.beerRate.model.Stats;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class ApiHandlers {

    private static final double MIN_RATING = 0;
    private static final double MAX_RATING = 5;

    private final Database db;
    private final DatabaseQueries queries;

    public ApiHandlers(final Database db, final DatabaseQueries queries) {
        this.db = db;
        this.queries = queries;
    }

    public void getAllBeers(final RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();
        writeResponse(response, HttpResponseStatus.OK);
        response.putHeader(HttpConstants.HEADER_KEY_CONTENT_TYPE, HttpConstants.HEADER_VALUE_JSON);
        response.end(Json.encodePrettily(db.getBeersByCategory()));
    }

    public void putBeer(final RoutingContext ctx) {
        HttpServerResponse response = ctx.response();

        String category = ctx.request().getParam(HttpConstants.PARAM_CATEGORY);
        if (!db.categoryExists(category)) {
            writeResponse(response, HttpResponseStatus.BAD_REQUEST);
            response.write(String.format("Category %s does not exist", category));
            response.end();
        } else {
            DbBeer newBeer = Json.decodeValue(ctx.getBodyAsJson().toString(), DbBeer.class);

            if (db.beerExists(newBeer.getName())) {
                writeResponse(response, HttpResponseStatus.BAD_REQUEST);
                response.write(String.format("Beer %s already exist", newBeer.getName()));
                response.end();
            } else {
                db.addBeer(category, newBeer);
                writeResponse(response, HttpResponseStatus.CREATED);
                response.end();
            }
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
            Optional<String> teamOpt = db.getTeamForUser(u.getFirstName(), u.getLastName());
            if (teamOpt.isPresent()) {
                j.put("team", teamOpt.get());
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
            response.write(String.format("User %s %s already exist", user.getFirstName(), user.getLastName()));
            response.end();
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
        response.end(Json.encodePrettily(db.getUsersByTeam()));
    }

    public void putTeam(final RoutingContext ctx) {
        HttpServerResponse response = ctx.response();
        try {
            String teamName = URLDecoder.decode(ctx.request().getParam(HttpConstants.PARAM_TEAM),
                    HttpConstants.ENCODING);

            if (db.teamExists(teamName)) {
                writeResponse(response, HttpResponseStatus.BAD_REQUEST);
                response.write(String.format("Team %s already exist", teamName));
                response.end();
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
        String teamName = ctx.request().getParam(HttpConstants.PARAM_TEAM);

        if (!db.teamExists(teamName)) {
            writeResponse(response, HttpResponseStatus.BAD_REQUEST);
            response.write(String.format("Team %s does not exist", teamName));
            response.end();
        } else {
            String firstName = ctx.request().getParam(HttpConstants.PARAM_FIRST_NAME);
            String lastName = ctx.request().getParam(HttpConstants.PARAM_LAST_NAME);
            if (!db.userExists(firstName, lastName)) {
                writeResponse(response, HttpResponseStatus.BAD_REQUEST);
                response.write(String.format("User %s %s does not exist", firstName, lastName));
                response.end();
            } else {
                db.removeUserFromTeam(firstName, lastName);
                db.addUserToTeam(teamName, firstName, lastName);
                writeResponse(response, HttpResponseStatus.CREATED);
                response.end();
            }
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
        Collection<DbRating> ratings = new HashSet<>();
        db.getBeersByCategory().values()
                .stream()
                .flatMap(x -> x.stream())
                .collect(Collectors.toList())
                .forEach(beer -> {
                    ratings.addAll(db.getRatingsForBeer(beer.getName()).values()
                            .stream()
                            .collect(Collectors.toList()));
                });

        response.putHeader(HttpConstants.HEADER_KEY_CONTENT_TYPE, HttpConstants.HEADER_VALUE_JSON);
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

        Map<String, Collection<DbBeer>> beersByCategory = db.getBeersByCategory();
        for (Entry<String, Collection<DbBeer>> entry : beersByCategory.entrySet()) {
            Map<String, Object> categoryMap = new HashMap<>();
            categoryMap.put("stats", queries.getStatsForCategory(entry.getKey()).get());
            List<DbBeer> beerList = new ArrayList<DbBeer>(entry.getValue());
            beerList.sort((o1, o2) -> {
                return o1.getName().compareTo(o2.getName());
            });
            for (DbBeer dbBeer : beerList) {
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
