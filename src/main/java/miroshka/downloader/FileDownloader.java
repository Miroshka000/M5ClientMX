package miroshka.downloader;

import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public class FileDownloader {
    public static void downloadFile(String url, File destination) throws IOException {
        FileUtils.copyURLToFile(new URL(url), destination);
    }
}
