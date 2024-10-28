package miroshka.serial;

import com.fazecast.jSerialComm.SerialPort;
import java.util.ArrayList;
import java.util.List;

public class SerialPortHandler {
    public static List<String> getAvailablePorts() {
        List<String> portList = new ArrayList<>();
        for (SerialPort port : SerialPort.getCommPorts()) {
            portList.add(port.getSystemPortName());
        }
        return portList;
    }
}
