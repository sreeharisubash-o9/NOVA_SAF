package conversation;

import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Main class to configure and run the o9SAF API client.
 */
public class chat {

    private static final Logger LOGGER = Logger.getLogger(chat.class.getName());

    // API Configuration Constants
    public static final String API_URL = "https://mygenaidev.o9solutions.com/api/ai/chat?agent=qa-saf-failures";
    public static final String AUTH_TOKEN = "Basic vnzuau2qfp834pzc6g7at1ee";

    private static final String OUTPUT_FILE_NAME = "agent_response.md";

    public static void main(String[] args) {

        try {
            // Instantiate and run the client
            O9ChatClient client = new O9ChatClient(API_URL, AUTH_TOKEN);

            while (true) {
                String USER_PROMPT = new PayloadHandler().getUserPrompt();
                String jsonPayload = new PayloadHandler().createJsonPayload(USER_PROMPT, null,
                        "69b3a1ca0d7046b5488da6e4");

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
