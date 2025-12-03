module BookDatabase {
    requires java.sql;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;
    //requires javafx.web;

    /*
    // Extra UI libraries
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    */

    //requires com.mysql.cj.jdbc;

    opens se.kth.awad.bookdatabase to javafx.base, javafx.graphics, javafx.fxml;
    opens se.kth.awad.bookdatabase.model to javafx.base;

    exports se.kth.awad.bookdatabase;
}