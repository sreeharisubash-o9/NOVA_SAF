package conversation;

import java.util.Scanner;

public class PayloadHandler {
    public String getUserPrompt() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter your prompt for the o9SAF agent:");
        String userPrompt = scanner.nextLine();
        // Don't close scanner to keep System.in open for subsequent calls
        return userPrompt;
    }

    public String createJsonPayload(String userPrompt, String fileName, String fileId) {
        if (fileName == null) {
            fileName = " ";
        }

        if (fileId == null) {
            fileId = " ";
        }

        // Escape backslashes first, then double quotes
    String escapedPrompt = userPrompt
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r");

    return "{"
            + "\"message\": \"" + escapedPrompt + "\","
            + "\"fileId\": \"\","
            + "\"attachments\": ["
            + "    {"
            + "        \"file_name\": \"" + fileName + "\","
            + "        \"file_id\": \"" + fileId + "\""
            + "    }"
            + "],"
            + "\"thread_context\": null,"
            + "\"tags\": null"
            + "}";
    }
}
