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
import java.util.function.Supplier;

/**
 * Modern HTTP Client implementation using java.net.http.HttpClient.
 * Features non-blocking capabilities, automatic timeout handling, and 
 * native streaming for multipart uploads.
 */
public class O9HttpClient {

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
     * Executes the file upload using a streaming BodyPublisher.
     * Memory usage remains constant regardless of file size.
     */
    public void uploadFile(File file, Map<String, String> formFields) throws IOException {
        // Prepare the streaming body supplier
        Supplier<InputStream> bodySupplier = formBuilder.buildLazyBody(file, formFields);
        long contentLength = formBuilder.calculateContentLength(file, formFields);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .timeout(Duration.ofMinutes(5)) // Higher timeout for large file uploads
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .header("Authorization", authToken)
                // ofInputStream requires a Supplier to allow for request retries
                .POST(HttpRequest.BodyPublishers.ofInputStream(bodySupplier))
                .build();

        try {
            System.out.println("Sending request (" + (contentLength / 1024) + " KB)...");
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            handleResponse(response);

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

        System.out.println("\n--- Server Response ---");
        System.out.println("Status: " + status);
        if (status >= 200 && status < 300) {
            System.out.println("Success: Upload completed.");
        } else {
            System.err.println("Failure: Server returned an error.");
        }
        System.out.println("Body: " + body);
        System.out.println("-----------------------");
    }
}