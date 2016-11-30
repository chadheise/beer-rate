package com.manorllc.beerRate;

import java.util.Locale;
import java.util.Optional;

import org.apache.http.HttpStatus;
import org.apache.http.ReasonPhraseCatalog;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.manorllc.beerRate.db.BeerRatingDatabase;
import com.manorllc.beerRate.model.Rating;
import com.manorllc.beerRate.model.Stats;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class Handlers {

    private final ReasonPhraseCatalog reasonPhraseCatalog;
    private final Locale locale;
    private final BeerRatingDatabase db;

    public Handlers(final ReasonPhraseCatalog reasonPhraseCatalog,
            final Locale locale,
            final BeerRatingDatabase db) {
        this.reasonPhraseCatalog = reasonPhraseCatalog;
        this.locale = locale;
        this.db = db;
    }

    public void getRatings(final RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();

        String beerName = routingContext.request().getParam(HttpConstants.PARAM_BEER);

        Optional<Stats> stats = db.getStats(beerName);
        if (stats.isPresent()) {
            writeResponse(response, HttpStatus.SC_OK);
            response.putHeader(HttpConstants.HEADER_KEY_CONTENT_TYPE, HttpConstants.HEADER_VALUE_JSON);
            response.end(Json.encodePrettily(stats.get()));
        } else {
            writeResponse(response, HttpStatus.SC_OK);
            response.putHeader(HttpConstants.HEADER_KEY_CONTENT_TYPE, HttpConstants.HEADER_VALUE_JSON);
            response.end("{}");
        }
    }

    public void getAllRatings(final RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();
        writeResponse(response, HttpStatus.SC_OK);
        response.putHeader(HttpConstants.HEADER_KEY_CONTENT_TYPE, HttpConstants.HEADER_VALUE_JSON);
        response.end(Json.encodePrettily(db.getAllStats()));
    }

    public void putRating(final RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();

        JsonObject bodyJson = routingContext.getBodyAsJson();

        if (!bodyJson.containsKey("beer") || !bodyJson.containsKey("rating")) {
            writeResponse(response, HttpStatus.SC_BAD_REQUEST);
            response.end();
        } else {
            int rating = bodyJson.getInteger("rating");
            if (0 > rating || rating > 5) {
                writeResponse(response, HttpStatus.SC_BAD_REQUEST);
                response.putHeader(HttpConstants.HEADER_KEY_CONTENT_TYPE, HttpConstants.HEADER_VALUE_TEXT);
                response.end("Rating must be between 0 and 5");
            } else {
                Rating beerRating = new Rating();
                beerRating.setBeer(bodyJson.getString("beer"));
                beerRating.setRating(rating);
                beerRating.setTimestamp(new DateTime(DateTimeZone.UTC));

                db.putRating(beerRating);

                writeResponse(response, HttpStatus.SC_CREATED);
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
            writeResponse(response, HttpStatus.SC_BAD_REQUEST);
            response.putHeader(HttpConstants.HEADER_KEY_CONTENT_TYPE, HttpConstants.HEADER_VALUE_TEXT);
            response.end("Rating must be between 0 and 5");
        } else {
            Rating beerRating = new Rating();
            beerRating.setBeer(beerName);
            beerRating.setRating(rating);
            beerRating.setTimestamp(new DateTime(DateTimeZone.UTC));

            db.putRating(beerRating);

            writeResponse(response, HttpStatus.SC_CREATED);
            response.putHeader(HttpConstants.HEADER_KEY_CONTENT_TYPE, HttpConstants.HEADER_VALUE_JSON);
            response.end(Json.encodePrettily(beerRating));
        }
    }

    private void writeResponse(final HttpServerResponse response, final int status) {
        response.setStatusCode(status);
        response.setStatusMessage(reasonPhraseCatalog.getReason(status, locale));
    }

}
