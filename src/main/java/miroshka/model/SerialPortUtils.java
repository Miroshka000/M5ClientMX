package miroshka.model;

import com.fazecast.jSerialComm.SerialPort;
import java.util.ArrayList;
import java.util.List;

public class SerialPortUtils {

    private static List<String> availablePorts = new ArrayList<>();
    private static SerialPortListUpdateListener listener;
    private static volatile boolean monitoringActive = false;
    private static Thread monitoringThread;

    public static void setListener(SerialPortListUpdateListener listener) {
        SerialPortUtils.listener = listener;
    }

    public static List<String> getAvailablePorts() {
        return new ArrayList<>(availablePorts);
    }

    public static void startMonitoringPorts() {
        updateAvailablePorts();

        monitoringActive = true;
        monitoringThread = new Thread(() -> {
            while (monitoringActive) {
                SerialPort[] serialPorts = SerialPort.getCommPorts();
                List<String> currentPorts = new ArrayList<>();
                for (SerialPort port : serialPorts) {
                    currentPorts.add(port.getSystemPortName());
                }

                if (!currentPorts.equals(availablePorts)) {
                    availablePorts = currentPorts;
                    if (listener != null) {
                        listener.onPortListUpdated(availablePorts);
                    }
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        });

        monitoringThread.start();
    }

    public static void stopMonitoringPorts() {
        monitoringActive = false;
        if (monitoringThread != null && monitoringThread.isAlive()) {
            monitoringThread.interrupt();
        }
    }

    private static void updateAvailablePorts() {
        SerialPort[] serialPorts = SerialPort.getCommPorts();
        availablePorts.clear();
        for (SerialPort port : serialPorts) {
            availablePorts.add(port.getSystemPortName());
        }
    }

    public interface SerialPortListUpdateListener {
        void onPortListUpdated(List<String> updatedPorts);
    }
}
