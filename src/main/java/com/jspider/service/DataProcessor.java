package com.jspider.service;

import com.jspider.config.Config;
import com.jspider.model.Event;
import com.jspider.model.League;
import com.jspider.model.Sport;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

public class DataProcessor {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss 'UTC'")
            .withZone(ZoneId.of("UTC"));

    private final ApiService apiService;
    private final ExecutorService executor;
    private final Config config;

    public DataProcessor(
            final ApiService apiService,
            final ExecutorService executor,
            final Config config
    ) {
        this.apiService = apiService;
        this.executor = executor;
        this.config = config;
    }

    public CompletableFuture<Void> processSport(final Sport sport) {
        final List<CompletableFuture<Void>> leagueFutures = sport.regions().stream()
                .flatMap(region -> region.leagues().stream())
                .filter(League::isTop)
                .map(league -> processLeague(sport, league))
                .toList();
        return CompletableFuture.allOf(leagueFutures.toArray(new CompletableFuture[0]));
    }

    public CompletableFuture<Void> processLeague(final Sport sport, final League league) {
        return apiService.fetchEventsForLeague(league.id(), executor)
                .thenApply(JsonParser::parseEvents)
                .thenCompose(eventIds -> {
                    final List<CompletableFuture<Event>> eventDetailFutures = eventIds.stream()
                            .limit(config.getEventsPerLeagueLimit())
                            .map(eventId -> apiService.fetchEventDetails(eventId, executor)
                                    .thenApply(JsonParser::parseEventDetails))
                            .toList();

                    return CompletableFuture.allOf(eventDetailFutures.toArray(new CompletableFuture[0]))
                            .thenApply(v -> eventDetailFutures.stream().map(CompletableFuture::join).collect(Collectors.toList()));
                })
                .thenAccept(events -> printLeagueWithEvents(sport, league, events));
    }

    private void printLeagueWithEvents(final Sport sport, final League league, final List<Event> events) {
        System.out.printf("\n%s, %s%n", sport.name(), league.name());
        events.forEach(event -> {
            final String startTime = DATE_FORMATTER.format(Instant.ofEpochMilli(event.kickoff()));
            System.out.printf("%s%s, %s, %d%n", " ".repeat(4), event.name(), startTime, event.id());
            event.markets().stream()
                    .limit(config.getMarketsPerEventLimit())
                    .forEach(market -> {
                        System.out.printf("%s%s%n", " ".repeat(8), market.name());
                        market.runners().forEach(runner -> System.out.printf("%s%s, %.2f, %d%n",
                                " ".repeat(12), runner.name(), runner.price(), runner.id()));
                    });
        });
    }
}