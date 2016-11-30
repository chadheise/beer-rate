package com.manorllc.beerRate;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;

public class Server extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

    private final int port;

    public Server(final int port) {
        this.port = port;
    }

    public void start() {
        Vertx vertx = Vertx.vertx();

        HttpServer server = vertx.createHttpServer();

        Router router = Router.router(vertx);
        Handlers handlers = new Handlers();

        router.route().handler(handlers::getBeers);

        LOGGER.info("Starting server on port " + port);
        server.requestHandler(router::accept).listen(port);
    }
}
