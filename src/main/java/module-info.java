module BookDatabase {
    requires java.sql;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;

    opens se.kth.awad.bookdatabase to javafx.base, javafx.graphics, javafx.fxml;
    opens se.kth.awad.bookdatabase.model to javafx.base;

    exports se.kth.awad.bookdatabase;
}