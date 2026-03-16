package upload;

import java.io.File;
import java.io.IOException;

import java.util.Map;

import org.json.JSONObject;
import io.github.cdimascio.dotenv.Dotenv;

/**
 * Service class for handling image persistence. 
 * Extracts file_id from server responses for downstream use.
 */
public class ImagerUploader {

    private final O9HttpClient client;

    // API Configuration Constants
    public ImagerUploader() {
        Dotenv dotenv = Dotenv.load();
        String API_URL_FILE_UPLOAD = dotenv.get("O9_API_URL_FILE_UPLOAD");
        String AUTH_TOKEN = dotenv.get("O9_AUTH_TOKEN");
        String FILE_FIELD_NAME = dotenv.get("O9_FILE_FIELD_NAME");
        String FILE_PATH = dotenv.get("O9_FILE_PATH");
        String BOUNDARY = "---PureJavaBoundary123456789";
        this.client = new O9HttpClient(API_URL_FILE_UPLOAD, AUTH_TOKEN, BOUNDARY, FILE_FIELD_NAME);
    }

    /**
     * Uploads the file and returns the generated File ID.
     */
    public String uploadAndGetFileId(File file, Map<String, String> metadata) throws IOException {
        if (!file.exists()) {
            throw new IOException("Target file does not exist: " + file.getAbsolutePath());
        }
        
        String responseBody = client.uploadFileAndReturnBody(file, metadata);
        JSONObject json = new JSONObject(responseBody);

        if (!json.has("file_id")) {
            throw new IOException("Upload succeeded but file_id was missing from response: " + responseBody);
        }

        return json.getString("file_id");
    }
}