package com.jspider.service;

import com.jspider.config.Config;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ApiService {

    private static final Logger LOGGER = Logger.getLogger(ApiService.class.getName());

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Config config;

    public ApiService(final Config config) {
        this.config = config;
    }

    public CompletableFuture<String> fetchSports(final ExecutorService executor) {
        return sendRequest(config.getSportsUrl(), executor);
    }

    public CompletableFuture<String> fetchEventsForLeague(final long leagueId, final ExecutorService executor) {
        return sendRequest(String.format(config.getLeagueEventsUrlTemplate(), leagueId), executor);
    }

    public CompletableFuture<String> fetchEventDetails(final long eventId, final ExecutorService executor) {
        return sendRequest(String.format(config.getEventDetailsUrlTemplate(), eventId), executor);
    }

    private CompletableFuture<String> sendRequest(final String url, final ExecutorService executor) {
        final HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApplyAsync(HttpResponse::body, executor)
                .exceptionally(e -> {
                    LOGGER.log(Level.WARNING, "Failed to fetch data from URL: " + url, e);
                    return "{}";
                });
    }
}