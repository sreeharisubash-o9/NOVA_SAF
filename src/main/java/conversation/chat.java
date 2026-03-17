package conversation;

import upload.ImagerUploader;
import java.io.File;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import io.github.cdimascio.dotenv.Dotenv;

/**
 * Orchestrator class that manages the end-to-end workflow: 
 * Image Upload -> File ID Extraction -> Chat Request.
 */
public class Chat {

    private static final Logger LOGGER = Logger.getLogger(Chat.class.getName());
    private static final Dotenv dotenv = Dotenv.load();
    
    private static final String API_URL_FILE_UPLOAD = dotenv.get("O9_API_URL_FILE_UPLOAD");
    private static final String API_URL = dotenv.get("O9_API_URL");
    private static final String AUTH_TOKEN = dotenv.get("O9_AUTH_TOKEN");
    private static final String OUTPUT_FILE = dotenv.get("OUTPUT_FILE_NAME");
    private static final String O9_FILE_FIELD_NAME = dotenv.get("O9_FILE_FIELD_NAME");

    public static void main(String[] args) {
        validateEnvironment();

        O9ChatClient chatClient = new O9ChatClient(API_URL, AUTH_TOKEN);
        ImagerUploader uploader = new ImagerUploader(API_URL_FILE_UPLOAD, AUTH_TOKEN, O9_FILE_FIELD_NAME);
        PayloadHandler payloadHandler = new PayloadHandler();

        try (Scanner scanner = new Scanner(System.in)) {
            LOGGER.info("o9SAF Orchestrator Active.");

            System.out.println("Enter image path to attach (or press Enter to skip):");
            
            String pathInput = scanner.nextLine().trim();
            String fileId = null;

            // Step 1: Sequential Upload if a path is provided
            if (!pathInput.isEmpty()) {
                try {
                    File file = new File(pathInput);
                    LOGGER.info("Initiating file upload...");
                    Map<String, String> metadata = Map.of(
                    "source_type", "thread", // This matches the field the server is complaining about
                    "description", "Automated upload from Java Orchestrator"
                    );
                    
                    fileId = uploader.uploadAndGetFileId(file, metadata);
                    LOGGER.log(Level.INFO, "Successfully linked File ID: {0}", fileId);

                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Upload failed, proceeding without attachment: {0}", e.getMessage());
                }
            }

            // Step 2: Proceed to Chat
            System.out.print("\nMessage > ");
            String prompt = scanner.nextLine();

            String jsonPayload = payloadHandler.createJsonPayload(prompt, fileId);
            chatClient.runO9SafScript(jsonPayload, OUTPUT_FILE);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Fatal orchestration error: {0}", e.getMessage());
        }
    }

    private static void validateEnvironment() {
        String[] requiredKeys = {
            "O9_AUTH_TOKEN",
            "O9_API_URL",
            "O9_API_URL_FILE_UPLOAD",
            "OUTPUT_FILE_NAME",
            "O9_FILE_FIELD_NAME"
        };

        for (String key : requiredKeys) {
            String value = dotenv.get(key);
            if (value == null || value.isBlank()) {
                String description = switch (key) {
                    case "O9_AUTH_TOKEN"          -> "API authentication token";
                    case "O9_API_URL"             -> "chat API endpoint URL";
                    case "O9_API_URL_FILE_UPLOAD" -> "file upload API endpoint URL";
                    case "OUTPUT_FILE_NAME"       -> "output file path";
                    case "O9_FILE_FIELD_NAME"     -> "file field name for multipart upload";
                    default                       -> "required configuration value";
                };
                throw new IllegalStateException(
                    "Missing environment variable [" + key + "]: " + description
                );
            }
        }
    }
}