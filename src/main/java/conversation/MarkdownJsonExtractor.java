package conversation;

import java.util.Optional;
import java.util.logging.Logger;

/**
 * Specifically designed to extract JSON content from LLM Markdown fences.
 * Handles variations in tagging (e.g., ```json vs ```).
 */
public class MarkdownJsonExtractor {

    private static final Logger LOGGER = Logger.getLogger(MarkdownJsonExtractor.class.getName());
    private static final String JSON_MARKER = "```json";
    private static final String PLAIN_MARKER = "```";

    private MarkdownJsonExtractor() {}

    public static Optional<String> extract(String content) {
        if (content == null || content.isBlank()) {
            return Optional.empty();
        }

        // 1. Try to find the start of a JSON block (case-insensitive)
        int start = content.toLowerCase().indexOf(JSON_MARKER);
        int contentStartIndex;

        if (start != -1) {
            contentStartIndex = start + JSON_MARKER.length();
        } else {
            // 2. Fallback: Look for generic triple backticks
            start = content.indexOf(PLAIN_MARKER);
            if (start != -1) {
                contentStartIndex = start + PLAIN_MARKER.length();
            } else {
                // 3. Final Fallback: Is it just raw JSON?
                String trimmed = content.trim();
                return (trimmed.startsWith("{") && trimmed.endsWith("}")) 
                        ? Optional.of(trimmed) : Optional.empty();
            }
        }

        // 4. Find the closing marker
        int end = content.indexOf(PLAIN_MARKER, contentStartIndex);
        if (end == -1) {
            LOGGER.warning("Detected start of code block but no closing backticks.");
            return Optional.empty();
        }

        return Optional.of(content.substring(contentStartIndex, end).trim());
    }
}