package com.manorllc.beerRate.util;

import java.io.IOException;

import com.manorllc.beerRate.db.Database;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class DatabaseInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseInitializer.class);

    private static final String ROOT_DIR = "src/main/resources/data/";

    public static void init(final Database db) throws IOException {

        LOGGER.info("Initializing database with user data");
        Parsers.parseUsers(ROOT_DIR + "users.csv").forEach(user -> {
            db.addUser(user);
        });

        LOGGER.info("Initializing database with team data");
        Parsers.parseTeams(ROOT_DIR + "teams.csv").forEach(team -> {
            db.addTeam(team);
        });

        LOGGER.info("Initializing database with team membership");
        Parsers.parseTeamMembership(ROOT_DIR + "teamMembership.json", db);

        LOGGER.info("Initializing database with beers");
        Parsers.parseBeers(ROOT_DIR + "beers.json", db);

    }

}
