package conversation;

import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Main class to configure and run the o9SAF API client.
 */
public class chat {

    private static final Logger LOGGER = Logger.getLogger(chat.class.getName());

    // API Configuration Constants
    public static final String API_URL = System.getenv("O9_API_URL");
    public static final String AUTH_TOKEN = System.getenv("O9_AUTH_TOKEN");

    private static final String OUTPUT_FILE_NAME = "agent_response.md";

    public static void main(String[] args) {

        try {
            // Instantiate and run the client
            O9ChatClient client = new O9ChatClient(API_URL, AUTH_TOKEN);

            while (true) {
                String USER_PROMPT = new PayloadHandler().getUserPrompt();
                String jsonPayload = new PayloadHandler().createJsonPayload(USER_PROMPT, null, "699464aa3766958798d894fd");

                try {
                    client.runO9SafScript(jsonPayload, OUTPUT_FILE_NAME);
                    
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "An error occurred during script execution: " + e.getMessage(), e);
                    break;
                }
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "An error occurred during script execution: " + e.getMessage(), e);
        }
    }
}
