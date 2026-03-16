package upload;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.Map;
import java.util.Vector;
import java.util.function.Supplier;

/**
 * Enterprise-grade utility to construct multipart/form-data streams.
 * Optimized for memory efficiency using a Streaming/Sequence approach 
 * to prevent OutOfMemoryErrors on large file uploads.
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
     * Builds a Supplier of InputStreams representing the multipart body.
     * This allows the HttpClient to stream the request without loading the file into memory.
     *
     * @param file       The file to be uploaded.
     * @param formFields Map of additional text parameters.
     * @return A Supplier providing a combined InputStream of the entire request body.
     */
    public Supplier<InputStream> buildLazyBody(File file, Map<String, String> formFields) {
        return () -> {
            Vector<InputStream> streams = new Vector<>();

            // 1. Add Text Fields
            formFields.forEach((name, value) -> {
                String fieldPart = HYPHENS + boundary + CRLF
                        + "Content-Disposition: form-data; name=\"" + name + "\"" + CRLF + CRLF
                        + value + CRLF;
                streams.add(new ByteArrayInputStream(fieldPart.getBytes(StandardCharsets.UTF_8)));
            });

            // 2. Add File Header
            String fileHeader = HYPHENS + boundary + CRLF
                    + "Content-Disposition: form-data; name=\"" + fileFieldName + "\"; filename=\"" + file.getName() + "\"" + CRLF
                    + "Content-Type: " + probeContentType(file) + CRLF + CRLF;
            streams.add(new ByteArrayInputStream(fileHeader.getBytes(StandardCharsets.UTF_8)));

            // 3. Add File Data (Streamed)
            try {
                streams.add(new BufferedInputStream(new FileInputStream(file)));
            } catch (IOException e) {
                throw new RuntimeException("Failed to open file stream for upload: " + file.getAbsolutePath(), e);
            }

            // 4. Add Footer/Closing Boundary
            String footer = CRLF + HYPHENS + boundary + HYPHENS + CRLF;
            streams.add(new ByteArrayInputStream(footer.getBytes(StandardCharsets.UTF_8)));

            Enumeration<InputStream> en = streams.elements();
            return new SequenceInputStream(en);
        };
    }

    /**
     * Calculates the total content length without loading the data into memory.
     */
    public long calculateContentLength(File file, Map<String, String> formFields) {
        long length = 0;
        for (Map.Entry<String, String> entry : formFields.entrySet()) {
            length += (HYPHENS + boundary + CRLF
                    + "Content-Disposition: form-data; name=\"" + entry.getKey() + "\"" + CRLF + CRLF
                    + entry.getValue() + CRLF).getBytes(StandardCharsets.UTF_8).length;
        }

        length += (HYPHENS + boundary + CRLF
                + "Content-Disposition: form-data; name=\"" + fileFieldName + "\"; filename=\"" + file.getName() + "\"" + CRLF
                + "Content-Type: " + probeContentType(file) + CRLF + CRLF).getBytes(StandardCharsets.UTF_8).length;

        length += file.length();
        length += (CRLF + HYPHENS + boundary + HYPHENS + CRLF).getBytes(StandardCharsets.UTF_8).length;

        return length;
    }

    /**
     * Uses Java NIO to probe the content type, falling back to octet-stream.
     */
    private String probeContentType(File file) {
        try {
            String type = Files.probeContentType(file.toPath());
            return (type != null) ? type : "application/octet-stream";
        } catch (IOException e) {
            return "application/octet-stream";
        }
    }
}