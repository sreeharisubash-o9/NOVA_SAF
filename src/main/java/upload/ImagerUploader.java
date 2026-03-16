package upload;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Main application class to configure and run the multipart file upload client.
 */
public class ImagerUploader {

    // API Configuration Constants
    private static final String API_URL = "https://mygenaidev.o9solutions.com/api/ai/file_manager/upload_and_ingest";
    private static final String AUTH_TOKEN = "Basic vnzuau2qfp834pzc6g7at1ee";
    private static final String FILE_FIELD_NAME = "file";

    // Boundary constant shared across the client and builder
    public static final String BOUNDARY = "---PureJavaBoundary123456789";

    public static void main(String[] args) {
        // NOTE: The file path MUST exist.
        String filePath = "C:\\Users\\sreehari.subash\\Pictures\\Screenshots\\EkgFailureTest.png";
        File imageFile = new File(filePath);

        if (!imageFile.exists()) {
            System.err.println("Error: Image file not found at " + filePath);
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