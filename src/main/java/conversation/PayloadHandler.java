package conversation;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Objects;

/**
 * Handles the construction of API request payloads using standard JSON libraries.
 * Ensures structural integrity and proper character escaping.
 */
public class PayloadHandler {

    /**
     * Constructs a valid JSON payload for the o9SAF API.
     * Use of org.json ensures all special characters in the prompt are safely escaped.
     *
     * @param userPrompt The raw text input from the user.
     * @param fileId     The unique identifier for the file (defaults to empty string if null).
     * @return A minified JSON string ready for the POST request.
     */
    public String createJsonPayload(String userPrompt, String fileId) {
        // Defensive check for mandatory field
        String message = Objects.requireNonNullElse(userPrompt, "").trim();
        
        // Build the root object
        JSONObject payload = new JSONObject();
        payload.put("message", message);
        payload.put("thread_context", JSONObject.NULL);
        payload.put("tags", JSONObject.NULL);

        // Build the attachment object
        JSONObject attachment = new JSONObject();
        attachment.put("file_name", JSONObject.NULL);
        attachment.put("file_id", Objects.requireNonNullElse(fileId, ""));

        // Add attachment to a JSONArray
        JSONArray attachments = new JSONArray();
        attachments.put(attachment);
        
        payload.put("attachments", attachments);

        return payload.toString();
    }
}