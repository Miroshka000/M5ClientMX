package miroshka.model;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public class FileDownloader {

    public static void downloadFileWithProgress(String url, File destination, ProgressCallback progressCallback) throws IOException {
        System.setProperty("https.protocols", "TLSv1.2");
        URI downloadUri = URI.create(url);
        long totalBytes = downloadUri.toURL().openConnection().getContentLengthLong();

        try (InputStream inputStream = downloadUri.toURL().openStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            long totalBytesRead = 0;

            try (var outputStream = FileUtils.openOutputStream(destination)) {
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;

                    if (progressCallback != null && totalBytes > 0) {
                        double progress = (double) totalBytesRead / totalBytes;
                        progressCallback.updateProgress(progress, 1.0);
                    }
                }
            }
        } catch (IOException e) {
            throw new IOException("Error while downloading file: " + e.getMessage(), e);
        }
    }
}
