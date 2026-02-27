package conversation;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import java.net.HttpURLConnection;


import org.json.JSONObject;


/**
 * Utility class to handle crude parsing of concatenated JSON strings
 * without using external JSON libraries.
 */

public class StreamParser {

    private StreamParser() {
        // Private constructor to prevent instantiation of a utility class
    }



    /**
     * Implements bracket-counting logic to split a string containing
     * multiple concatenated JSON objects, typical of SSE streams.
     * @param data The raw streaming response text.
     * @return A list of individual, top-level JSON object strings.
     */

    public static void parseStreamResponse(int status, HttpURLConnection connection, String outputFileName) throws Exception {
        final Logger LOGGER = Logger.getLogger(O9ChatClient.class.getName());

        LOGGER.info("Response received from o9-QA-SAF agent: " + status);
        LOGGER.info("Processing response stream...");

        // Read the entire streaming response into a single buffer
        StringBuilder agentResponseText = new StringBuilder();
        StringBuilder userRequestText = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // SSE responses often have "data: " prefix. Remove it if present.
                String processedLine = line.startsWith("data: ") ? line.substring(6).trim() : line.trim();
                
                if (!processedLine.isEmpty() && !processedLine.equalsIgnoreCase("[DONE]")) {
                    JSONObject mainObject = new JSONObject(processedLine);
                    if (mainObject.has("content")) {
                        String type = mainObject.getString("type");
                        if ("assistant".equals(type)) {
                            agentResponseText.append(mainObject.getString("content"));
                        } else if ("user".equals(type)) {
                            userRequestText.append(mainObject.getString("content"));
                        }
                    }
                }
            }
        }
        

        // Save the response to a file
        ResponseLogger.saveResponseToFile(agentResponseText.toString(), userRequestText.toString(), outputFileName);

    }

}

