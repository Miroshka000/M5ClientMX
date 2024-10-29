package miroshka.serial;

import javax.swing.JTextArea;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class StickInfoReader {
    private JTextArea infoArea;
    private static final File ESPTOOL_EXECUTABLE = new File(System.getProperty("user.dir"), "esptool/esptool-win64/esptool.exe");

    public StickInfoReader(String port, JTextArea infoArea) {
        this.infoArea = infoArea;
        fetchDeviceInfo(port);
    }

    private void fetchDeviceInfo(String port) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    ESPTOOL_EXECUTABLE.getAbsolutePath(), "--port", port, "chip_id"
            );
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            process.waitFor();
            infoArea.setText(output.toString());
        } catch (Exception e) {
            infoArea.setText("Error running esptool: " + e.getMessage());
        }
    }
}
