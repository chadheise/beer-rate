package com.manorllc.beerRate.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;

import com.manorllc.beerRate.db.Database;
import com.manorllc.beerRate.db.DbUser;
import com.manorllc.beerRate.model.Generation;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class Parsers {

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
    public static void parseTeamMembership(final String filePath, final Database db) throws IOException {
        Path path = Paths.get(new File(filePath).toURI());
        byte[] bytes = Files.readAllBytes(path);
        String string = new String(bytes);
        JsonObject json = new JsonObject(string);
        json.stream().forEach(entry -> {
            String teamName = entry.getKey();
            JsonArray users = json.getJsonArray(teamName);
            users.forEach(obj -> {
                String userName = obj.toString();
                String firstName = userName.split(" ")[0];
                String lastName = userName.split(" ")[1];
                db.addUserToTeam(teamName, firstName, lastName);
            });
        });
    }

}
