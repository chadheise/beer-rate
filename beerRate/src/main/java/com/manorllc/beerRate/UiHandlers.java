package com.manorllc.beerRate;

import java.util.Optional;

import com.manorllc.beerRate.db.Database;
import com.manorllc.beerRate.db.DatabaseQueries;
import com.manorllc.beerRate.model.Stats;

import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.templ.TemplateEngine;

public class UiHandlers {

    private final TemplateEngine templateEngine;
    private final Database db;
    private final DatabaseQueries queries;

    public UiHandlers(final TemplateEngine templateEngine, final Database db, final DatabaseQueries queries) {
        this.templateEngine = templateEngine;
        this.db = db;
        this.queries = queries;
    }

    public void rateBeer(final RoutingContext routingContext) {
        String beerName = routingContext.request().getParam(HttpConstants.PARAM_BEER);
        // Add beer directly for easier access in template
        routingContext.put("beer", beerName);

        templateEngine.render(routingContext, "templates/rateBeer.html", res -> {
            if (res.succeeded()) {
                routingContext.response().end(res.result());
            } else {
                routingContext.fail(res.cause());
            }
        });
    }

    public void beerStats(final RoutingContext routingContext) {
        String beerName = routingContext.request().getParam(HttpConstants.PARAM_BEER);
        double rating = Double.valueOf(routingContext.request().getParam(HttpConstants.PARAM_RATING));
        routingContext.put("beer", beerName);
        routingContext.put("rating", rating);

        Optional<Stats> stats = queries.getStatsForBeer(beerName);
        if (stats.isPresent()) {
            routingContext.put("averageRating", stats.get().getMean());
            routingContext.put("numberOfRatings", stats.get().getCount());
            routingContext.put("stats", stats.get());

        }

        templateEngine.render(routingContext, "templates/beerStats.html", res -> {
            if (res.succeeded()) {
                routingContext.response().end(res.result());
            } else {
                routingContext.fail(res.cause());
            }
        });
    }

}
