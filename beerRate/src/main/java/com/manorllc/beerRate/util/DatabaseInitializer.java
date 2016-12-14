package com.manorllc.beerRate.util;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import com.manorllc.beerRate.db.Database;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class DatabaseInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseInitializer.class);

    private static final String ROOT_DIR = "/src/main/resources/data/";

    public static void init(final Database db) throws IOException, URISyntaxException {

        String filePath = Paths.get(db.getClass().getResource("/").toURI()).toString();
        filePath = filePath.replaceAll("/target/classes", "");
        LOGGER.info("File path: " + filePath);

        LOGGER.info("Initializing database with user data");
        Parsers.parseUsers(filePath + ROOT_DIR + "users.csv").forEach(user -> {
            db.addUser(user);
        });

        LOGGER.info("Initializing database with team data");
        Parsers.parseTeams(filePath + ROOT_DIR + "teams.csv").forEach(team -> {
            db.addTeam(team);
        });

        LOGGER.info("Initializing database with team membership");
        Parsers.parseTeamMembership(filePath + ROOT_DIR + "teamMembership.json", db);

        LOGGER.info("Initializing database with beers");
        Parsers.parseBeers(filePath + ROOT_DIR + "beers.json", db);

    }

}
