module BookDatabase {
    requires java.sql;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;

    opens se.kth.awad.bookdatabase to javafx.base, javafx.graphics, javafx.fxml;
    opens se.kth.awad.bookdatabase.model to javafx.base;
    opens se.kth.awad.bookdatabase.Controller to javafx.base;
    opens se.kth.awad.bookdatabase.view to javafx.base;

    exports se.kth.awad.bookdatabase;
    exports se.kth.awad.bookdatabase.Controller;
    exports se.kth.awad.bookdatabase.model;
    exports se.kth.awad.bookdatabase.view;
}