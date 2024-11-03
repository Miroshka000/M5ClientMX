package miroshka.model;

import com.fazecast.jSerialComm.SerialPort;

import java.util.ArrayList;
import java.util.List;

public class SerialPortUtils {
    public static List<String> getAvailablePorts() {
        List<String> ports = new ArrayList<>();

        SerialPort[] serialPorts = SerialPort.getCommPorts();

        for (SerialPort port : serialPorts) {
            ports.add(port.getSystemPortName());
        }

        return ports;
    }
}
