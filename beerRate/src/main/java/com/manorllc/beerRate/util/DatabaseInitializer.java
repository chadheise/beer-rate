package com.manorllc.beerRate.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.file.Paths;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.Json;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class DatabaseInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseInitializer.class);

     private static final String HOST = "localhost";
     private static final int PORT = 8080;

//    private static final String HOST = "beerychristmas.us-west-2.elasticbeanstalk.com";
//    private static final int PORT = 80;

    private static final String ROOT_DIR = "/src/main/resources/data/";
    private static final Vertx VERTX = Vertx.factory.vertx();

    public static void main(final String[] args) throws IOException, URISyntaxException {

        String filePath = Paths.get(LOGGER.getClass().getResource("/").toURI()).toString();
        filePath = filePath.replaceAll("/target/classes", "");
        LOGGER.info("File path: " + filePath);

        HttpClient client = VERTX.createHttpClient();

        LOGGER.info("Initializing database with user data");
        Parsers.parseUsers(filePath + ROOT_DIR + "users.csv").forEach(user -> {
            String body = Json.encode(user);
            Buffer buffer = Buffer.buffer(body);

            client.put(PORT, HOST, "/users")
                    .putHeader("Content-Length", Integer.toString(buffer.length()))
                    .handler(resp -> {
                        LOGGER.debug(resp.statusCode());
                    })
                    .write(buffer)
                    .end();
        });

        LOGGER.info("Initializing database with team data");
        Parsers.parseTeams(filePath + ROOT_DIR + "teams.csv").forEach(team -> {
            try {
                String encodedTeam = URLEncoder.encode(team, "UTF-8");
                client.put(PORT, HOST, "/teams/" + encodedTeam)
                        .putHeader("Content-Length", Integer.toString(0))
                        .handler(resp -> {
                            LOGGER.debug(resp.statusCode());
                        })
                        .end();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        });

        LOGGER.info("Initializing database with team membership");
        Parsers.parseTeamMembership(filePath + ROOT_DIR +
                "teamMembership.json", client, PORT, HOST);

        LOGGER.info("Initializing database with beers");
        Parsers.parseBeers(filePath + ROOT_DIR + "beers.json", client, PORT, HOST);

        client.close();
    }

}
