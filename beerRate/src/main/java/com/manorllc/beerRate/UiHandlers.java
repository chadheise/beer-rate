package com.manorllc.beerRate;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.manorllc.beerRate.db.Database;
import com.manorllc.beerRate.db.DatabaseQueries;
import com.manorllc.beerRate.db.DbUser;
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

        List<DbUser> users = db.getUsersByTeam().values()
                .stream()
                .flatMap(teamCollection -> teamCollection.stream())
                .collect(Collectors.toList());
        users.sort((u1, u2) -> {
            String full1 = u1.getLastName() + u1.getFirstName();
            String full2 = u2.getLastName() + u2.getFirstName();
            return (full1.compareTo(full2));
        });
        routingContext.put("users", users);

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
