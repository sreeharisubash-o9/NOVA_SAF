package conversation;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.function.BiConsumer;
import java.util.logging.Logger;
import org.json.JSONObject;

/**
 * Enterprise-standard SSE (Server-Sent Events) Stream Parser.
 * Designed with a callback mechanism to decouple stream processing from data handling.
 */
public class StreamParser {

    private static final Logger LOGGER = Logger.getLogger(StreamParser.class.getName());
    private static final String DATA_PREFIX = "data: ";
    private static final String DONE_SIGNAL = "[DONE]";

    private StreamParser() {
        // Private constructor for utility class
    }

    /**
     * Parses the incoming stream line-by-line and triggers a callback for gathered content.
     * This implementation is memory-efficient as it processes data as it arrives.
     *
     * @param inputStream The raw stream from the HTTP connection.
     * @param onCompletion Callback receiving (FullAssistantContent, UserPrompt).
     */
    public static void parseAndProcess(InputStream inputStream, BiConsumer<String, String> onCompletion) throws Exception {
        StringBuilder assistantBuffer = new StringBuilder();
        StringBuilder userBuffer = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                processLine(line, assistantBuffer, userBuffer);
            }
        }

        String finalAssistantText = assistantBuffer.toString();
        String finalUserText = userBuffer.toString();

        // Pass the results to the callback (e.g., for logging or extraction)
        if (onCompletion != null) {
            onCompletion.accept(finalAssistantText, finalUserText);
        }
    }

    private static void processLine(String line, StringBuilder assistantBuffer, StringBuilder userBuffer) {
        String trimmed = line.trim();
        if (trimmed.isEmpty()) return;

        // Strip SSE "data: " prefix
        String jsonPayload = trimmed.startsWith(DATA_PREFIX) ? trimmed.substring(DATA_PREFIX.length()).trim() : trimmed;

        if (DONE_SIGNAL.equalsIgnoreCase(jsonPayload)) {
            LOGGER.info("Streaming response signaling completion.");
            return;
        }

        try {
            JSONObject json = new JSONObject(jsonPayload);
            if (json.has("content") && json.has("type")) {
                String type = json.getString("type");
                String content = json.getString("content");

                if ("assistant".equals(type)) {
                    assistantBuffer.append(content);
                } else if ("user".equals(type)) {
                    userBuffer.append(content);
                }
            }

        } catch (Exception e) {
            // Log and skip malformed lines typical in noisy streams
            LOGGER.warning("Skipping malformed stream line: " + e.getMessage());
        }
    }
}