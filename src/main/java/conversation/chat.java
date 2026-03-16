package conversation;

import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import io.github.cdimascio.dotenv.Dotenv;

/**
//  * Enterprise entry point for the o9SAF API client.
//  * Adheres to 2026 standards for resource management and security.
//  */
public class Chat {

    private static final Logger LOGGER = Logger.getLogger(Chat.class.getName());
    
    // Configuration loaded from environment variables for security
    public static Dotenv dotenv = Dotenv.load();
    private static final String API_URL = dotenv.get("O9_API_URL");
    private static final String AUTH_TOKEN = dotenv.get("O9_AUTH_TOKEN");
    private static final String OUTPUT_FILE_NAME = dotenv.get("OUTPUT_FILE_NAME");

    public static void main(String[] args) {
        validateEnvironment();

        O9ChatClient client = new O9ChatClient(API_URL, AUTH_TOKEN);
        PayloadHandler payloadHandler = new PayloadHandler();

        try (Scanner scanner = new Scanner(System.in)) {
            LOGGER.info("o9SAF Chat Interface Initialized. Type 'exit' to quit.");
            
            while (true) {
                System.out.print("\nUser > ");
                String userPrompt = scanner.nextLine();

                if ("exit".equalsIgnoreCase(userPrompt.trim())) {
                    LOGGER.info("Shutting down client...");
                    break;
                }

                processRequest(client, payloadHandler, userPrompt);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Fatal application error: {0}", e.getMessage());
        }
    }

    /**
     * Orchestrates the payload creation and client execution.
     */
    private static void processRequest(O9ChatClient client, PayloadHandler handler, String prompt) {
        try {
            // fileId and fileName are currently hardcoded placeholders from original logic
            String jsonPayload = handler.createJsonPayload(prompt, null, "69b3a1ca0d7046b5488da6e4");
            client.runO9SafScript(jsonPayload, OUTPUT_FILE_NAME);
            
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Invalid input: {0}", e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Request failed: {0}", e.getMessage());
        }
    }

    /**
     * Ensures required security credentials are present before execution.
     */
    private static void validateEnvironment() {
        if (AUTH_TOKEN == null || AUTH_TOKEN.isBlank()) {
            LOGGER.severe("MISSING CONFIGURATION: O9_AUTH_TOKEN environment variable is not set.");
            System.exit(1);
        }
    }
}