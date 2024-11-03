package miroshka.model;

@FunctionalInterface
public interface ProgressCallback {
    void updateProgress(double bytesRead, double totalBytes);
}
