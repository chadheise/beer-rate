package com.manorllc.beerRate;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;

public class Handlers {

    private static final Logger LOGGER = LoggerFactory.getLogger(Handlers.class);

    public void getBeers(final RoutingContext routingContext) {

        HttpServerResponse response = routingContext.response();
        response.putHeader("content-type", "text/plain");

        // Write to the response and end it
        response.end("Hello World!");

    }

    public void putRating(final RoutingContext routingContext) {
        JsonObject bodyJson = routingContext.getBodyAsJson();
        System.out.println(bodyJson);
        LOGGER.info(bodyJson);

        HttpServerResponse response = routingContext.response();
        response.putHeader(HttpConstants.HEADER_KEY_CONTENT_TYPE, HttpConstants.HEADER_VALUE_JSON);

        response.end(bodyJson.encodePrettily());
    }

}
