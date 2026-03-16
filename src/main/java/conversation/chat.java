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
    public static final Dotenv dotenv = Dotenv.load();
    
    private static final String API_URL = dotenv.get("O9_API_URL");
    private static final String AUTH_TOKEN = dotenv.get("O9_AUTH_TOKEN");
    private static final String OUTPUT_FILE = dotenv.get("OUTPUT_FILE_NAME");

    public static void main(String[] args) {
        validateEnvironment();

        O9ChatClient chatClient = new O9ChatClient(API_URL, AUTH_TOKEN);
        ImagerUploader uploader = new ImagerUploader();
        PayloadHandler payloadHandler = new PayloadHandler();

        try (Scanner scanner = new Scanner(System.in)) {
            LOGGER.info("o9SAF Orchestrator Active.");

            System.out.println("Enter image path to attach (or press Enter to skip):");
            String pathInput = scanner.nextLine().trim();
            
            String fileId = "69b7e18f9a3b23ded66c7b9a";
            String fileName = null;

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
                    LOGGER.info("Successfully linked File ID: " + fileId);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Upload failed, proceeding without attachment: {0}", e.getMessage());
                }
            }

            // Step 2: Proceed to Chat
            System.out.print("\nMessage > ");
            String prompt = scanner.nextLine();

            String jsonPayload = payloadHandler.createJsonPayload(prompt, fileName, fileId);
            chatClient.runO9SafScript(jsonPayload, OUTPUT_FILE);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Fatal orchestration error: {0}", e.getMessage());
        }
    }

    private static void validateEnvironment() {
        if (AUTH_TOKEN == null || AUTH_TOKEN.isBlank()) {
            throw new IllegalStateException("O9_AUTH_TOKEN not found in environment.");
        }
    }
}