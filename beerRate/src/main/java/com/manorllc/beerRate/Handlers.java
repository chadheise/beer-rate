package com.manorllc.beerRate;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

public class Handlers {

    public void getBeers(final RoutingContext routingContext) {

        HttpServerResponse response = routingContext.response();
        response.putHeader("content-type", "text/plain");

        // Write to the response and end it
        response.end("Hello World!");

    }

}
