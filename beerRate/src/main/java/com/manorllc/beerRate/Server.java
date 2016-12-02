package com.manorllc.beerRate;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class Server extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

    private final int port;
    private final Handlers handlers;

    public Server(final Vertx vertx, final int port, final Handlers handlers) {
        this.vertx = vertx;
        this.port = port;
        this.handlers = handlers;
    }

    public void start() {
        HttpServer server = vertx.createHttpServer();

        Router router = Router.router(vertx);
        LOGGER.debug("Registering route handlers");
        registerHandlers(router);
        LOGGER.debug("Finished registering route handlers");

        LOGGER.info("Starting server on port " + port);
        server.requestHandler(router::accept).listen(port);
        LOGGER.info("Server started on port " + port);

    }

    public void registerHandlers(final Router router) {
        // Pass all PUT requests through a body handler for easier parsing of
        // the body
        router.route()
                .method(HttpMethod.PUT)
                .handler(BodyHandler.create());

        router.get("/ui")
                .handler(handlers::mainUi);

        router.route()
                .method(HttpMethod.GET)
                .path("/ratings")
                .handler(handlers::getAllRatings);

        router.route()
                .method(HttpMethod.GET)
                .path("/ratings/:" + HttpConstants.PARAM_BEER)
                .handler(handlers::getRatings);

        router.route()
                .method(HttpMethod.GET)
                .path("/ratings/new/:" + HttpConstants.PARAM_BEER + "/:" + HttpConstants.PARAM_RATING)
                .handler(handlers::putRatingViaGet);

        router.route()
                .method(HttpMethod.PUT)
                .path("/ratings")
                .consumes(HttpConstants.HEADER_VALUE_JSON)
                .produces(HttpConstants.HEADER_VALUE_JSON)
                .handler(handlers::putRating);
    }
}
