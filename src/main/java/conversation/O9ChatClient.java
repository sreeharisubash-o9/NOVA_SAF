package conversation;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Modernized API client for o9SAF, utilizing Java's HttpClient for 
 * robust, asynchronous, and memory-efficient communication.
 */
public class O9ChatClient {

    private static final Logger LOGGER = Logger.getLogger(O9ChatClient.class.getName());
    
    // Configurable timeouts for enterprise resilience
    private static final Duration CONNECTION_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration REQUEST_TIMEOUT = Duration.ofMinutes(2);

    private final String apiUrl;
    private final String authToken;
    private final HttpClient httpClient;

    /**
     * Initializes the client with a shared HttpClient instance.
     * Shared clients are more efficient for connection pooling.
     */
    public O9ChatClient(String apiUrl, String authToken) {
        this.apiUrl = apiUrl;
        this.authToken = authToken;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(CONNECTION_TIMEOUT)
                .build();
    }

    /**
     * Executes the POST request and pipes the response stream to the parser.
     * * @param jsonPayload    The JSON body constructed by PayloadHandler.
     * @param outputFileName The destination for the logged response.
     */
    public void runO9SafScript(String jsonPayload, String outputFileName) throws Exception {
        LOGGER.info("Initiating o9SAF API request...");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .timeout(REQUEST_TIMEOUT)
                .header("Authorization", authToken)
                .header("Accept", "text/event-stream")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        // Send request and handle response as an InputStream for memory efficiency
        HttpResponse<java.io.InputStream> response = httpClient.send(
                request, 
                HttpResponse.BodyHandlers.ofInputStream()
        );

        int statusCode = response.statusCode();
        if (statusCode != 200) {
            LOGGER.log(Level.SEVERE, "API request failed with status: {0}", statusCode);
            throw new RuntimeException("HTTP Error: " + statusCode);
        }

        // Use the refactored StreamParser with a functional callback
        StreamParser.parseAndProcess(response.body(), (assistantContent, userPrompt) -> {
            // Decoupled logic: Log to file and then attempt JSON extraction
            ResponseLogger.saveResponseToFile(assistantContent, userPrompt, outputFileName);
            
            MarkdownJsonExtractor.extract(assistantContent).ifPresent(json -> {
                LOGGER.info("Valid JSON block extracted from response.");
                // Additional processing logic for the extracted JSON can go here
            });
        });

        LOGGER.info("o9SAF Transaction completed successfully.");
    }
}