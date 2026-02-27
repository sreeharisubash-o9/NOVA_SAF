package upload;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Utility class to manually construct the complete multipart/form-data request body.
 */
public class MultipartFormBuilder {

    private static final String CRLF = "\r\n";
    private static final String HYPHENS = "--";
    private final String boundary;
    private final String fileFieldName;

    public MultipartFormBuilder(String boundary, String fileFieldName) {
        this.boundary = boundary;
        this.fileFieldName = fileFieldName;
    }

    /**
     * Manually constructs the complete multipart/form-data request body by combining
     * text fields and the file's binary data.
     */
    public byte[] buildBody(File file, Map<String, String> formFields) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        // --- 1. Text/Data Fields ---
        for (Map.Entry<String, String> entry : formFields.entrySet()) {
            String fieldName = entry.getKey();
            String fieldValue = entry.getValue();
            
            String fieldHeader = HYPHENS + boundary + CRLF
                + "Content-Disposition: form-data; name=\"" + fieldName + "\"" + CRLF + CRLF
                + fieldValue + CRLF;
            
            outputStream.write(fieldHeader.getBytes(StandardCharsets.UTF_8));
        }

        // --- 2. File Part (The Image) ---
        String fileHeader = HYPHENS + boundary + CRLF
            + "Content-Disposition: form-data; name=\"" + fileFieldName + "\"; filename=\"" + file.getName() + "\"" + CRLF
            + "Content-Type: " + getContentType(file.getName()) + CRLF + CRLF;
        // System.out.println(fileHeader);
        outputStream.write(fileHeader.getBytes(StandardCharsets.UTF_8));
        
        // Write the actual file data
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
        
        // Add the CRLF that precedes the closing boundary.
        outputStream.write(CRLF.getBytes(StandardCharsets.UTF_8));
        
        // --- 3. Closing Boundary ---
        String closingBoundary = HYPHENS + boundary + HYPHENS + CRLF;
        outputStream.write(closingBoundary.getBytes(StandardCharsets.UTF_8));
        
        return outputStream.toByteArray();
    }
    
    /**
     * Helper to determine MIME type based on file extension.
     */
    private static String getContentType(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            String extension = fileName.substring(dotIndex + 1).toLowerCase();
            return switch (extension) {
                case "png" -> "image/png";
                case "jpg", "jpeg" -> "image/jpeg";
                case "pdf" -> "application/pdf";
                default -> "application/octet-stream";
            };
        }
        return "application/octet-stream";
    }
}