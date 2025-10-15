package com.jspider.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles loading and providing access to application configuration.
 */
public class Config {

    private static final Logger LOGGER = Logger.getLogger(Config.class.getName());

    private final Properties properties = new Properties();

    public Config(final String fileName) {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (input == null) {
                LOGGER.log(Level.SEVERE, "Unable to find " + fileName);
                throw new RuntimeException("Configuration file not found: " + fileName);
            }
            properties.load(input);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Error loading configuration file.", ex);
            throw new RuntimeException(ex);
        }
    }

    public String getSportsUrl() {
        return properties.getProperty("api.sports.url");
    }

    public String getLeagueEventsUrlTemplate() {
        return properties.getProperty("api.league.events.url.template");
    }

    public String getEventDetailsUrlTemplate() {
        return properties.getProperty("api.event.details.url.template");
    }

    public List<String> getSportsToParse() {
        return Arrays.asList(properties.getProperty("parser.sports").split(","));
    }

    public int getEventsPerLeagueLimit() { return Integer.parseInt(properties.getProperty("parser.events.per.league.limit", "2")); }

    public int getMarketsPerEventLimit() { return Integer.parseInt(properties.getProperty("parser.markets.per.event.limit", "4")); }

    public int getThreadPoolSize() { return Integer.parseInt(properties.getProperty("parser.thread.pool.size", "3")); }

}