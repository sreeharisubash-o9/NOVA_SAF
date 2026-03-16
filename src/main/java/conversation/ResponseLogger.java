package conversation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility for persisting agent responses to the local filesystem.
 * Uses Java NIO for atomic writes and explicit UTF-8 encoding.
 */
public final class ResponseLogger {

    private static final Logger LOGGER = Logger.getLogger(ResponseLogger.class.getName());
    private static final String BORDER = "=".repeat(40);

    private ResponseLogger() {
        // Prevent instantiation of utility class
    }

    /**
     * Writes the interaction context and agent response to a Markdown file.
     *
     * @param content  The text received from the assistant.
     * @param prompt   The user's original input prompt.
     * @param fileName The destination path (e.g., "output.md").
     */
    public static void saveResponseToFile(String content, String prompt, String fileName) {
        if (content == null || content.isBlank()) {
            LOGGER.warning("Attempted to log empty content. Skipping file write.");
            return;
        }

        printToConsole(content);

        String fileContent = formatMarkdown(prompt, content);

        try {
            Path path = Path.of(fileName);
            // Explicitly use UTF-8 and overwrite/create file
            Files.writeString(path, fileContent, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, 
                    StandardOpenOption.TRUNCATE_EXISTING);
            
            LOGGER.info(() -> "Response successfully persisted to: " + path.toAbsolutePath());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e, () -> "Failed to write response to " + fileName);
        }
    }

    private static void printToConsole(String content) {
        System.out.println("\n" + BORDER);
        System.out.println(" EXTRACTED AGENT RESPONSE CONTENT ");
        System.out.println(BORDER);
        System.out.println(content);
        System.out.println(BORDER + "\n");
    }

    private static String formatMarkdown(String prompt, String content) {
        return """
               ## User Prompt
               `%s`
               
               ---
               
               ## Agent Response
               %s
               """.formatted(prompt, content);
    }
}