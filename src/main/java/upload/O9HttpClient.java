package upload;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Handles the core HTTP connection and request/response process.
 */
public class O9HttpClient {
    
    private final String apiUrl;
    private final String authToken;
    private final String boundary;
    private final MultipartFormBuilder formBuilder;

    public O9HttpClient(String apiUrl, String authToken, String boundary, String fileFieldName) {
        this.apiUrl = apiUrl;
        this.authToken = authToken;
        this.boundary = boundary;
        this.formBuilder = new MultipartFormBuilder(boundary, fileFieldName);
    }

    /**
     * Sets up the connection, sends the multipart request, and prints the response.
     */
    public void uploadFile(File file, Map<String, String> formFields) throws IOException {
        URI uri = null;
        try {
            uri = new URI(apiUrl);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        URL url = uri.toURL();
        HttpURLConnection connection = null;

        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);

            // 1. Build the multipart request body
            byte[] requestBody = formBuilder.buildBody(file, formFields);

            // 2. Set required headers
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            connection.setRequestProperty("Authorization", authToken);
            connection.setRequestProperty("Content-Length", String.valueOf(requestBody.length));

            // 3. Write the body
            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(requestBody);
                outputStream.flush();
            }

            // 4. Get the response code and body
            int status = connection.getResponseCode();

            // Determine whether to read from the input stream (success) or error stream (failure)
            InputStream responseStream = (status >= 200 && status < 300) 
                                         ? connection.getInputStream() 
                                         : connection.getErrorStream();

            String responseBody = readResponse(responseStream);

            System.out.println("\n--- Server Response ---");
            System.out.println("Status: " + status);
            System.out.println("Body: " + responseBody);
            System.out.println("-----------------------");

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
    
    /**
     * Reads the entire content from an InputStream into a single String.
     */
    private String readResponse(InputStream inputStream) throws IOException {
        if (inputStream == null) return "No response stream available.";
        
        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }
        return response.toString();
    }
}