package com.manorllc.beerRate;

import java.util.Optional;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.manorllc.beerRate.db.BeerRatingDatabase;
import com.manorllc.beerRate.model.Rating;
import com.manorllc.beerRate.model.Stats;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class Handlers {

    private final BeerRatingDatabase db;

    public Handlers(final BeerRatingDatabase db) {
        this.db = db;
    }

    public void getRatings(final RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();

        String beerName = routingContext.request().getParam(HttpConstants.PARAM_BEER);

        Optional<Stats> stats = db.getStats(beerName);
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
        response.end(Json.encodePrettily(db.getAllStats()));
    }

    public void putRating(final RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();

        JsonObject bodyJson = routingContext.getBodyAsJson();

        if (!bodyJson.containsKey("beer") || !bodyJson.containsKey("rating")) {
            writeResponse(response, HttpResponseStatus.BAD_REQUEST);
            response.end();
        } else {
            int rating = bodyJson.getInteger("rating");
            if (0 > rating || rating > 5) {
                writeResponse(response, HttpResponseStatus.BAD_REQUEST);
                response.putHeader(HttpConstants.HEADER_KEY_CONTENT_TYPE, HttpConstants.HEADER_VALUE_TEXT);
                response.end("Rating must be between 0 and 5");
            } else {
                Rating beerRating = new Rating();
                beerRating.setBeer(bodyJson.getString("beer"));
                beerRating.setRating(rating);
                beerRating.setTimestamp(new DateTime(DateTimeZone.UTC));

                db.putRating(beerRating);

                writeResponse(response, HttpResponseStatus.CREATED);
                response.putHeader(HttpConstants.HEADER_KEY_CONTENT_TYPE, HttpConstants.HEADER_VALUE_JSON);
                response.end(Json.encodePrettily(beerRating));
            }
        }
    }

    /**
     * Puts a rating by processing an http GET request.
     * 
     * TODO: Refactor to merge duplicate code with putRating
     * 
     * @param routingContext
     */
    public void putRatingViaGet(final RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();

        String beerName = routingContext.request().getParam(HttpConstants.PARAM_BEER);
        int rating = Integer.parseInt(routingContext.request().getParam(HttpConstants.PARAM_RATING));
        if (0 > rating || rating > 5) {
            writeResponse(response, HttpResponseStatus.BAD_REQUEST);
            response.putHeader(HttpConstants.HEADER_KEY_CONTENT_TYPE, HttpConstants.HEADER_VALUE_TEXT);
            response.end("Rating must be between 0 and 5");
        } else {
            Rating beerRating = new Rating();
            beerRating.setBeer(beerName);
            beerRating.setRating(rating);
            beerRating.setTimestamp(new DateTime(DateTimeZone.UTC));

            db.putRating(beerRating);

            Stats stats = db.getStats(beerName).get();

            JsonObject responseJson = new JsonObject();
            responseJson.put("beer", beerName);
            responseJson.put("yourRating", rating);
            responseJson.put("totalRatings", stats.getCount());
            responseJson.put("averageRating", stats.getMean());

            writeResponse(response, HttpResponseStatus.CREATED);
            response.putHeader(HttpConstants.HEADER_KEY_CONTENT_TYPE, HttpConstants.HEADER_VALUE_JSON);
            response.end(Json.encodePrettily(responseJson));
        }
    }

    private void writeResponse(final HttpServerResponse response, final HttpResponseStatus status) {
        response.setStatusCode(status.code());
        response.setStatusMessage(status.reasonPhrase());
    }

}
