package com.manorllc.beerRate;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.manorllc.beerRate.db.Database;
import com.manorllc.beerRate.db.DatabaseQueries;
import com.manorllc.beerRate.db.DbTeam;
import com.manorllc.beerRate.db.DbUser;
import com.manorllc.beerRate.model.Beer;
import com.manorllc.beerRate.model.Generation;
import com.manorllc.beerRate.model.Stats;
import com.manorllc.beerRate.model.Team;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
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

        List<DbUser> users = sortUsers(db.getUsersByTeam().values()
                .stream()
                .flatMap(teamCollection -> teamCollection.stream())
                .collect(Collectors.toList()));
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

    public void summary(final RoutingContext ctx) {
        templateEngine.render(ctx, "templates/summary.html", res -> {
            if (res.succeeded()) {
                ctx.response().end(res.result());
            } else {
                ctx.fail(res.cause());
            }
        });
    }

    public void summaryBody(final RoutingContext ctx) {
        Map<String, Object> overallMap = new HashMap<>();
        Stats overallStats = queries.getStatsForAll();
        overallMap.put("stats", overallStats);

        Map<String, List<Beer>> beersByCategory = db.getBeersByCategory();
        ctx.put("beersByCategory", beersByCategory);

        Map<String, Stats> beerStats = new HashMap<>();
        Collection<Beer> beers = beersByCategory.values().stream().flatMap(b -> b.stream())
                .collect(Collectors.toList());
        for (Beer beer : beers) {
            beerStats.put(beer.getName(), queries.getStatsForBeer(beer.getName()).get());
        }
        ctx.put("beerStats", beerStats);

        templateEngine.render(ctx, "templates/summaryBody.html", res -> {
            if (res.succeeded()) {
                ctx.response().end(res.result());
            } else {
                ctx.fail(res.cause());
            }
        });
    }

    public void host(final RoutingContext ctx) {

        ctx.put("users", sortUsers(db.getUsers()));
        ctx.put("teams", db.getTeams());
        ctx.put("generations", Generation.values());

        System.out.println(Json.encodePrettily(db.getTeams()));

        templateEngine.render(ctx, "templates/host.html", res -> {
            if (res.succeeded()) {
                ctx.response().end(res.result());
            } else {
                ctx.fail(res.cause());
            }
        });
    }

    public void hostFormSuccess(final RoutingContext ctx) {
        templateEngine.render(ctx, "templates/hostFormSuccess.html", res -> {
            if (res.succeeded()) {
                ctx.response().end(res.result());
            } else {
                ctx.fail(res.cause());
            }
        });
    }

    public void teamStats(final RoutingContext ctx) {
        HttpServerResponse response = ctx.response();

        try {
            String teamName = URLDecoder.decode(ctx.request().getParam(HttpConstants.PARAM_TEAM),
                    HttpConstants.ENCODING);

            Optional<Team> teamOpt = db.getTeam(teamName);
            if (!teamOpt.isPresent()) {
                writeResponse(response, HttpResponseStatus.BAD_REQUEST);
                response.end(String.format("Team %s does not exist", teamName));
            } else {
                Team team = teamOpt.get();
                ctx.put("team", team);

                List<DbUser> users = sortUsers(db.getUsersForTeam(team.getName()));
                ctx.put("users", users);

                // TODO: Get the desired stats for the team

                templateEngine.render(ctx, "templates/teamStats.html", res -> {
                    if (res.succeeded()) {
                        ctx.response().end(res.result());
                    } else {
                        ctx.fail(res.cause());
                    }
                });

            }
        } catch (UnsupportedEncodingException e) {
            writeResponse(response, HttpResponseStatus.INTERNAL_SERVER_ERROR);
            response.end();
        }

    }

    private void writeResponse(final HttpServerResponse response, final HttpResponseStatus status) {
        response.setStatusCode(status.code());
        response.setStatusMessage(status.reasonPhrase());
    }

    private List<DbUser> sortUsers(final Collection<DbUser> userCollection) {
        List<DbUser> users = userCollection
                .stream()
                .collect(Collectors.toList());
        users.sort((u1, u2) -> {
            String full1 = u1.getLastName() + u1.getFirstName();
            String full2 = u2.getLastName() + u2.getFirstName();
            return (full1.compareTo(full2));
        });
        return users;
    }

    private List<DbTeam> sortTeams(final Collection<DbTeam> teamCollection) {
        List<DbTeam> teams = teamCollection
                .stream()
                .collect(Collectors.toList());
        teams.sort((t1, t2) -> {
            return (t1.getName().compareTo(t2.getName()));
        });
        return teams;
    }

}
