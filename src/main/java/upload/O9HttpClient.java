package upload;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.io.File;
import java.util.Map;
import java.util.logging.Logger;
import java.util.function.Supplier;

/**
 * Modern HTTP Client implementation using java.net.http.HttpClient.
 * Features non-blocking capabilities, automatic timeout handling, and 
 * native streaming for multipart uploads.
 */
public class O9HttpClient {

    private static final Logger LOGGER = Logger.getLogger(O9HttpClient.class.getName());    
    private final String apiUrl;
    private final String authToken;
    private final String boundary;
    private final MultipartFormBuilder formBuilder;
    private final HttpClient httpClient;

    public O9HttpClient(String apiUrl, String authToken, String boundary, String fileFieldName) {
        this.apiUrl = apiUrl;
        this.authToken = authToken;
        this.boundary = boundary;
        this.formBuilder = new MultipartFormBuilder(boundary, fileFieldName);
        
        // Configure a centralized HttpClient with a connection timeout
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    /**
     * Executes the file upload and returns the response body as a String.
     * Uses BodyPublishers.ofInputStream to keep memory usage constant.
     */
    public String uploadFileAndReturnBody(File file, Map<String, String> formFields) throws IOException {
        Supplier<InputStream> bodySupplier = formBuilder.buildLazyBody(file, formFields);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .timeout(Duration.ofMinutes(5))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .header("Authorization", authToken)
                .POST(HttpRequest.BodyPublishers.ofInputStream(bodySupplier))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                handleResponse(response);
                return response.body();
            } else {
                throw new IOException("Server returned status " + response.statusCode() + ": " + response.body());
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Upload interrupted", e);
        }
    }

    /**
     * Handles the HTTP response logic and status code validation.
     */
    private void handleResponse(HttpResponse<String> response) {
        int status = response.statusCode();
        String body = response.body();

        LOGGER.info("\n--- Server Response ---");
        LOGGER.info("Status: " + status);
        if (status >= 200 && status < 300) {
            LOGGER.info("Success: Upload completed.");
        } else {
            LOGGER.severe("Failure: Server returned an error.");
        }
        LOGGER.info("Body: " + body);
        LOGGER.info("-----------------------");
    }
}