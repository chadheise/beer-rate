package com.manorllc.beerRate;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;

import com.manorllc.beerRate.db.Database;
import com.manorllc.beerRate.db.DatabaseQueries;
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

    public void getAllUsers(final RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();
        writeResponse(response, HttpResponseStatus.OK);
        Collection<JsonObject> userCollection = new HashSet<>();
        db.getUsersByTeam().entrySet().forEach(entry -> {
            entry.getValue().forEach(u -> {
                JsonObject j = new JsonObject();
                j.put("firstName", u.getFirstName());
                j.put("lastName", u.getLastName());
                j.put("generation", u.getGeneration());
                j.put("team", entry.getKey());
                userCollection.add(j);
            });

        });
        response.putHeader(HttpConstants.HEADER_KEY_CONTENT_TYPE, HttpConstants.HEADER_VALUE_JSON);
        response.end(Json.encodePrettily(userCollection));
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
        response.end(Json.encodePrettily(queries.getStatsForAll()));
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
            String userName = routingContext.request().formAttributes().get("name");
            String firstName = userName.split(" ")[0];
            String lastName = userName.split(" ")[1];

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

    private void writeResponse(final HttpServerResponse response, final HttpResponseStatus status) {
        response.setStatusCode(status.code());
        response.setStatusMessage(status.reasonPhrase());
    }

}
