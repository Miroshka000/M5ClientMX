module miroshka {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;
    requires com.fazecast.jSerialComm;
    requires org.apache.commons.io;
    requires org.json;

    requires java.net.http;
    requires java.desktop;
    requires java.logging;

    opens miroshka.controller to javafx.fxml;

    exports miroshka;
    exports miroshka.model;
    opens miroshka.installer to javafx.fxml;
}
