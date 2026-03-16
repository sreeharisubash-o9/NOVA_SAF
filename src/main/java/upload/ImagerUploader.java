package upload;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import io.github.cdimascio.dotenv.Dotenv;


/**
 * Main application class to configure and run the multipart file upload client.
 */
public class ImagerUploader {

    // API Configuration Constants
    public static Dotenv dotenv = Dotenv.load();
    private static final String API_URL = dotenv.get("O9_API_URL_FILE_UPLOAD");
    private static final String AUTH_TOKEN = dotenv.get("O9_AUTH_TOKEN");
    private static final String FILE_FIELD_NAME = dotenv.get("O9_FILE_FIELD_NAME");
    private static final String FILE_PATH = dotenv.get("O9_FILE_PATH");

    // Boundary constant shared across the client and builder
    public static final String BOUNDARY = "---PureJavaBoundary123456789";

    public static void main(String[] args) {
        // NOTE: The file path MUST exist.
        File imageFile = new File(FILE_PATH);

        if (!imageFile.exists()) {
            System.err.println("Error: Image file not found at " + FILE_PATH);
            System.err.println("Please verify the file path and accessibility.");
            return;
        }

        // Define the additional form fields
        Map<String, String> formFields = new LinkedHashMap<>();
        formFields.put("thread_id", "456");
        formFields.put("description", "A test upload from Java");
        formFields.put("message", "read the contents of this image.");
        formFields.put("source_type", "thread");

        System.out.println("Attempting to upload file: " + imageFile.getName());

        try {
            O9HttpClient client = new O9HttpClient(API_URL, AUTH_TOKEN, BOUNDARY, FILE_FIELD_NAME);
            client.uploadFile(imageFile, formFields);
        } catch (IOException e) {
            System.err.println("Upload failed due to connection error or server issue.");
            e.printStackTrace();
        }
    }
}