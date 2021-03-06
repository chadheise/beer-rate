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

    private void registerHandlers(final Router router) {
        // Pass all PUT requests through a body handler for easier parsing of
        // the body
        router.route()
                .method(HttpMethod.PUT)
                .handler(BodyHandler.create());

        router.route("/static/*")
                .handler(StaticHandler.create("static"));

        router.get("/ui/rate/:" + HttpConstants.PARAM_BEER)
                .handler(uiHandlers::rateBeer);

        router.get("/ui/stats/:" + HttpConstants.PARAM_BEER + "/:" + HttpConstants.PARAM_RATING)
                .handler(uiHandlers::beerStats);

        router.get("/")
                .handler(uiHandlers::summary);

        router.get("/ui/summary/")
                .handler(uiHandlers::summary);

        router.get("/ui/summary/body")
                .handler(uiHandlers::summaryBody);

        router.get("/ui/draught/")
                .handler(uiHandlers::draught);

        router.get("/ui/draught/body")
                .handler(uiHandlers::draughtBody);

        router.get("/ui/host")
                .handler(uiHandlers::host);

        router.get("/ui/hostFormSuccess")
                .handler(uiHandlers::hostFormSuccess);

        router.get("/ui/host/teamStats/:" + HttpConstants.PARAM_TEAM)
                .handler(uiHandlers::teamStats);

        router.get("/ui/host/gameStats/")
                .handler(uiHandlers::gameStats);

        router.put("/categories/:" + HttpConstants.PARAM_CATEGORY)
                .handler(apiHandlers::putCategory);

        router.get("/beers")
                .handler(apiHandlers::getAllBeers);

        router.put("/beers/:" + HttpConstants.PARAM_CATEGORY)
                .handler(apiHandlers::putBeer);

        router.get("/users")
                .handler(apiHandlers::getAllUsers);

        router.put("/users")
                .handler(apiHandlers::putUser);

        router.get("/teams")
                .handler(apiHandlers::getTeams);

        router.put("/teams/:" + HttpConstants.PARAM_TEAM)
                .handler(apiHandlers::putTeam);

        router.put("/teams/addUser/:" + HttpConstants.PARAM_TEAM)
                .handler(apiHandlers::addUserToTeam);

        router.get("/ratings")
                .handler(apiHandlers::getAllRatings);

        router.get("/ratings/:" + HttpConstants.PARAM_BEER)
                .handler(apiHandlers::getRatings);

        router.put("/ratings")
                .consumes(HttpConstants.HEADER_VALUE_JSON)
                .produces(HttpConstants.HEADER_VALUE_JSON)
                .handler(apiHandlers::putRating);

        router.get("/firstRatings")
                .handler(apiHandlers::getFirstRatingByCategory);

        router.post("/forms/ratings")
                .handler(apiHandlers::postRatingFromForm);

        router.post("/forms/addUser")
                .handler(apiHandlers::addUserFromForm);

        router.post("/forms/addTeam")
                .handler(apiHandlers::addTeamFromForm);

        router.post("/forms/changeTeam")
                .handler(apiHandlers::changeTeamFromForm);

        router.post("/forms/teamStats")
                .handler(apiHandlers::teamStatsFromForm);

        router.post("/forms/gameStats")
                .handler(apiHandlers::gameStatsFromForm);

        router.post("/forms/captain")
                .handler(apiHandlers::setCaptainFromForm);

        router.post("/forms/gameMarker")
                .handler(apiHandlers::setGameMarkerFromForm);

        router.get("/stats")
                .handler(apiHandlers::getAllStats);
    }
}
