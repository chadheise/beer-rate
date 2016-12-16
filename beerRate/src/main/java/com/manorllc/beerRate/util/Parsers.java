package com.manorllc.beerRate.util;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.manorllc.beerRate.db.DbUser;
import com.manorllc.beerRate.model.Generation;

import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class Parsers {

    private static final Logger LOGGER = LoggerFactory.getLogger(Parsers.class);

    /**
     * Parses a csv file and returns a collection of DbUser objects.
     *
     */
    public static Collection<DbUser> parseUsers(final String filePath) throws IOException {
        Collection<DbUser> users = new HashSet<>();

        Path path = Paths.get(new File(filePath).toURI());

        Files.readAllLines(path).forEach(line -> {
            String lastName = line.split(",")[0];
            String firstName = line.split(",")[1];
            Generation gen = Generation.valueOf(line.split(",")[2]);
            users.add(new DbUser(firstName, lastName, gen));
        });

        return users;
    }

    /**
     * Parses a csv file and returns a collection of team names.
     *
     */
    public static Collection<String> parseTeams(final String filePath) throws IOException {
        Path path = Paths.get(new File(filePath).toURI());
        return Files.readAllLines(path);
    }

    /**
     * Parse a json file containing a list of team members for each team and add
     * them to a database.
     * 
     * @param filePath
     * @param db
     * @throws IOException
     */
    public static void parseTeamMembership(final String filePath, final HttpClient client, final int port,
            final String host)
            throws IOException {
        Path path = Paths.get(new File(filePath).toURI());
        byte[] bytes = Files.readAllBytes(path);
        String string = new String(bytes);
        JsonObject json = new JsonObject(string);
        json.stream().forEach(entry -> {
            String teamName = entry.getKey();
            JsonArray users = json.getJsonArray(teamName);
            users.forEach(obj -> {
                try {
                    String userName = obj.toString();

                    String firstName = userName.split(" ")[0];
                    String lastName = userName.split(" ")[1];

                    String uri = new StringBuilder("/teams/addUser/")
                            .append(URLEncoder.encode(teamName, "UTF-8"))
                            .append("?")
                            .append("firstName=" + URLEncoder.encode(firstName, "UTF-8"))
                            .append("&")
                            .append("lastName=" + URLEncoder.encode(lastName, "UTF-8"))
                            .toString();
                    client.put(port, host, uri)
                            .putHeader("Content-Length", Integer.toString(0))
                            .handler(resp -> {
                                LOGGER.debug(Integer.toString(resp.statusCode()));
                            })
                            .end();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            });
        });
    }

    public static void parseBeers(final String filePath, final HttpClient client, final int port, final String host)
            throws IOException {
        Path path = Paths.get(new File(filePath).toURI());
        byte[] bytes = Files.readAllBytes(path);
        String string = new String(bytes);
        JsonObject json = new JsonObject(string);
        json.stream().forEach(entry -> {
            String categoryName = entry.getKey();

            try {
                String uri = "/categories/" + URLEncoder.encode(categoryName, "UTF-8");
                client.put(port, host, uri)
                        .putHeader("Content-Length", Integer.toString(0))
                        .handler(resp -> {
                            LOGGER.debug(Integer.toString(resp.statusCode()));
                        })
                        .end();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            JsonArray beers = json.getJsonArray(categoryName);
            beers.forEach(obj -> {
                try {
                    String uri = "/beers/" + URLEncoder.encode(categoryName, "UTF-8");
                    client.put(port, host, uri)
                            .handler(resp -> {
                                LOGGER.debug(Integer.toString(resp.statusCode()));
                            })
                            .end(obj.toString());
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            });
        });
    }

}
