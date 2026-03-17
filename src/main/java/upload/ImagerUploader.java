package upload;

import java.io.File;
import java.io.IOException;

import java.util.Map;
import java.util.UUID;

import org.json.JSONObject;

/**
 * Service class for handling image persistence. 
 * Extracts file_id from server responses for downstream use.
 */
public class ImagerUploader {

    private final O9HttpClient client;

    // API Configuration Constants
    public ImagerUploader(String apiUrlFileUpload, String authToken, String fileFieldName) {
        String API_URL_FILE_UPLOAD = apiUrlFileUpload;
        String AUTH_TOKEN = authToken;
        String FILE_FIELD_NAME = fileFieldName;
        String BOUNDARY = "Boundary-" + UUID.randomUUID().toString().replace("-", "");
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