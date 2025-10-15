# JSpider - Asynchronous Sports Data Scraper

A multi-threaded Java application that asynchronously scrapes sports data from the Leon.bet public API.

## Configuration

    You can configure which sports to parse and other settings in the `src/main/resources/config.properties` file.

## Project Structure

*   `JSpider.java`: The main entry point that starts the application.
*   `config/Config.java`: Loads settings from `config.properties`.
*   `model/`: Contains simple `record` classes (DTOs) to hold the parsed data (e.g., `Sport`, `Event`).
*   `service/ApiService.java`: Manages all HTTP requests to the external API.
*   `service/JsonParser.java`: Parses JSON responses into the model objects.
*   `service/DataProcessor.java`: Contains the main logic for fetching and processing the data.