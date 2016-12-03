package com.manorllc.beerRate;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

public class Server extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

    private final int port;
    private final ApiHandlers apiHandlers;
    private final UiHandlers uiHandlers;

    public Server(final int port, final ApiHandlers apiHandlers, final UiHandlers uiHandlers) {
        this.port = port;
        this.apiHandlers = apiHandlers;
        this.uiHandlers = uiHandlers;
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

        // Configure to serve static content
        router.route("/static/*")
                .handler(StaticHandler.create("static"));

        router.get("/rate/:" + HttpConstants.PARAM_BEER)
                .handler(uiHandlers::mainUi);

        router.route()
                .method(HttpMethod.GET)
                .path("/ratings")
                .handler(apiHandlers::getAllRatings);

        router.route()
                .method(HttpMethod.GET)
                .path("/ratings/:" + HttpConstants.PARAM_BEER)
                .handler(apiHandlers::getRatings);

        router.route()
                .method(HttpMethod.GET)
                .path("/ratings/add/:" + HttpConstants.PARAM_BEER + "/:" + HttpConstants.PARAM_RATING)
                .handler(apiHandlers::putRatingViaGet);

        router.route()
                .method(HttpMethod.PUT)
                .path("/ratings")
                .consumes(HttpConstants.HEADER_VALUE_JSON)
                .produces(HttpConstants.HEADER_VALUE_JSON)
                .handler(apiHandlers::putRating);
    }
}
