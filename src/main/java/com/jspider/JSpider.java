package com.jspider;

import com.jspider.config.Config;
import com.jspider.model.Sport;
import com.jspider.service.ApiService;
import com.jspider.service.DataProcessor;
import com.jspider.service.JsonParser;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JSpider {

    private static final Logger LOGGER = Logger.getLogger(JSpider.class.getName());

    public static void main(final String[] args) {
        final Config config = new Config("config.properties");
        final ExecutorService executor = Executors.newFixedThreadPool(config.getThreadPoolSize());
        final ApiService apiService = new ApiService(config);
        final DataProcessor dataProcessor = new DataProcessor(apiService, executor, config);

        LOGGER.info("Parser starting...");

        try {
            final CompletableFuture<Void> allDone = apiService.fetchSports(executor)
                    .thenApply(JsonParser::parseSports)
                    .thenCompose(sports -> {
                        final List<Sport> sportsToProcess = sports.stream()
                                .filter(sport -> config.getSportsToParse().contains(sport.family()))
                                .toList();

                        CompletableFuture<Void> resultChain = CompletableFuture.completedFuture(null);
                        for (Sport sport : sportsToProcess) {
                            resultChain = resultChain.thenComposeAsync(v -> dataProcessor.processSport(sport), executor);
                        }
                        return resultChain;
                    });

            allDone.get(120, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "A critical error occurred during parsing.", e);
        } finally {
            shutdownExecutor(executor);
        }

        LOGGER.info("Parser finished.");
    }

    private static void shutdownExecutor(final ExecutorService executor) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(120, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            } LOGGER.info("Executor has been shut down.");
        } catch (InterruptedException ex) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
