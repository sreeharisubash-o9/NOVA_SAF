package conversation; 

import java.io.FileWriter;
import java.io.IOException;


/**
 * Utility class for logging the agent's response to a file.
 */
public class ResponseLogger {

    private ResponseLogger() {
        // Private constructor to prevent instantiation of a utility class
    }

    /**
     * Writes the given content to a file.
     * * @param content The extracted text content from the agent.
     * @param prompt The original user message for context.
     * @param fileName The name of the file (e.g., "output.md").
     */
    public static void saveResponseToFile(String content, String prompt, String fileName) {
        // Output the final result
        System.out.println("\n" + "=".repeat(40));
        System.out.println(" EXTRACTED AGENT RESPONSE CONTENT ");
        System.out.println("=".repeat(40));
        System.out.println(content);
        System.out.println("=".repeat(40));
        
        // Prepend the prompt and a separator to the output file for context
        String fileContent = "## User Prompt\n"
                           + "`" + prompt + "`\n"
                           + "\n---\n\n"
                           + "## Agent Response\n"
                           + content;

        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write(fileContent);
            System.out.println("\nResponse saved successfully to: " + fileName);
        } catch (IOException e) {
            System.err.println("\nError writing response to file " + fileName + ": " + e.getMessage());
        }
    }
}