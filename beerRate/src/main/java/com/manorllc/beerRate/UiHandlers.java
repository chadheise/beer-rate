package com.manorllc.beerRate;

import java.util.Optional;

import com.manorllc.beerRate.db.BeerRatingDatabase;
import com.manorllc.beerRate.model.Stats;

import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.templ.TemplateEngine;

public class UiHandlers {

    private final TemplateEngine templateEngine;
    private final BeerRatingDatabase db;

    public UiHandlers(final TemplateEngine templateEngine, final BeerRatingDatabase db) {
        this.templateEngine = templateEngine;
        this.db = db;
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

        System.out.println("beer: " + beerName);
        System.out.println("rating: " + rating);

        Optional<Stats> stats = db.getStats(beerName);
        if (stats.isPresent()) {
            System.out.println("stats is present");
            routingContext.put("averageRating", stats.get().getMean());
            routingContext.put("numberOfRatings", stats.get().getCount());
            routingContext.put("stats", stats.get());

        }
        System.out.println("here");

        templateEngine.render(routingContext, "templates/beerStats.html", res -> {
            if (res.succeeded()) {
                routingContext.response().end(res.result());
            } else {
                routingContext.fail(res.cause());
            }
        });

    }

}
