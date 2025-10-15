package com.jspider.service;

import com.jspider.model.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.StreamSupport;


public final class JsonParser {

    private static final Logger LOGGER = Logger.getLogger(JsonParser.class.getName());

    public static List<Sport> parseSports(final String json) {
        try {
            return StreamSupport.stream(new JSONArray(json).spliterator(), false)
                    .map(item -> (JSONObject) item)
                    .map(JsonParser::toSport)
                    .toList();
        } catch (JSONException e) {
            LOGGER.warning("Could not parse sports JSON. Returning empty list.");
            return Collections.emptyList();
        }
    }

    public static List<Long> parseEvents(final String json) {
        try {
            return StreamSupport.stream(new JSONObject(json).getJSONArray("events").spliterator(), false)
                    .map(item -> ((JSONObject) item).getLong("id"))
                    .toList();
        } catch (JSONException e) {
            LOGGER.warning("Could not parse events JSON. Returning empty list.");
            return Collections.emptyList();
        }
    }

    public static Event parseEventDetails(final String json) {
        try {
            final JSONObject eventObj = new JSONObject(json);
            final List<Market> markets = StreamSupport.stream(eventObj.getJSONArray("markets").spliterator(), false)
                    .map(item -> (JSONObject) item)
                    .map(JsonParser::toMarket)
                    .toList();
            return new Event(eventObj.getLong("id"), eventObj.getString("name"), eventObj.getLong("kickoff"), markets);
        } catch (JSONException e) {
            LOGGER.warning("Could not parse event details. Returning empty event.");
            return new Event(0, "Parse Error", 0, Collections.emptyList());
        }
    }

    private static Sport toSport(final JSONObject obj) {
        final List<Region> regions = StreamSupport.stream(obj.getJSONArray("regions").spliterator(), false)
                .map(item -> (JSONObject) item)
                .map(JsonParser::toRegion)
                .toList();
        return new Sport(obj.getString("name"), obj.getString("family"), regions);
    }

    private static Region toRegion(final JSONObject obj) {
        final List<League> leagues = StreamSupport.stream(obj.getJSONArray("leagues").spliterator(), false)
                .map(item -> (JSONObject) item)
                .map(leagueObj -> new League(leagueObj.getLong("id"), leagueObj.getString("name"), leagueObj.optBoolean("top")))
                .toList();
        return new Region(leagues);
    }

    private static Market toMarket(final JSONObject obj) {
        final List<Runner> runners = StreamSupport.stream(obj.getJSONArray("runners").spliterator(), false)
                .map(item -> (JSONObject) item)
                .map(runnerObj -> new Runner(runnerObj.getString("name"), runnerObj.getDouble("price"), runnerObj.getLong("id")))
                .toList();
        return new Market(obj.getString("name"), runners);
    }
}