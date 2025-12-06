module BookDatabase {
    requires java.sql;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;
    
    // MySQL Connector/J driver is loaded automatically via ServiceLoader mechanism
    // No explicit requires needed - the driver is on module path via Maven dependency

    opens se.kth.awad.bookdatabase to javafx.base, javafx.graphics, javafx.fxml;
    opens se.kth.awad.bookdatabase.model to javafx.base;

    exports se.kth.awad.bookdatabase;
}