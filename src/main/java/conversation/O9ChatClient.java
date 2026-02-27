package conversation;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.json.JSONObject;

/**
 * Handles the HTTP connection, request sending, response streaming,
 * and delegates JSON parsing to StreamParser.
 */

public class O9ChatClient {

    private static final Logger LOGGER = Logger.getLogger(O9ChatClient.class.getName());
    private final String apiUrl;
    private final String authToken;

    public O9ChatClient(String apiUrl, String authToken) {
        this.apiUrl = apiUrl;
        this.authToken = authToken;
    }



    /**
     * Executes the API call, reads the streaming response, and extracts the content.
     * @param jsonPayload The JSON body for the POST request.
     * @return The concatenated extracted content from the assistant's response.
     */

    public void runO9SafScript(String jsonPayload, String outputFileName) throws Exception {
        HttpURLConnection connection = null;

        LOGGER.info("=== Starting o9SAF Automation ===");
        LOGGER.info("Sending request to o9SAF agent...");

        try {
            URI uri = new URI(apiUrl);
            URL url = uri.toURL();
            
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setDoInput(true);

            // Set required headers
            connection.setRequestProperty("Authorization", authToken);
            connection.setRequestProperty("Accept", "text/event-stream");
            connection.setRequestProperty("Content-Type", "application/json");

            // Write the JSON payload
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int status = connection.getResponseCode();
           
            if (status != 200) {
                String errorResponse = "";
                java.io.InputStream errorStream = connection.getErrorStream();
                if (errorStream != null) {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(errorStream, StandardCharsets.UTF_8))) {
                        errorResponse = br.lines().collect(Collectors.joining());
                    }
                }

                throw new Exception("API Error " + status + ": " + errorResponse);
            }

            StreamParser.parseStreamResponse(status, connection, outputFileName);

        } finally {
            if (connection != null) {
                connection.disconnect();
            }

            LOGGER.info("=== o9SAF Automation Completed ===");
        }
    }
}

