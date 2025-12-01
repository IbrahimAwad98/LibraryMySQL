package se.kth.awad.bookdatabase;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import se.kth.awad.bookdatabase.Controller.BookDBController;
import se.kth.awad.bookdatabase.model.BooksDb;
import se.kth.awad.bookdatabase.view.BooksPane;

/**
 * Application start up.
 * Follows MVC pattern: Creates Model, View, and Controller independently,
 * then wires them together via dependency injection.
 *
 * @author anderslm@kth.se
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {

        // === MVC SETUP - Dependency Injection Pattern ===

        // 1. Create Model (Data layer - handles database operations)
        BooksDb booksDb = new BooksDb();

        // 2. Create View (UI layer - displays data)
        BooksPane booksPane = new BooksPane();

        // 3. Create Controller (Business logic - mediates between Model and View)
        BookDBController controller = new BookDBController(booksDb, booksPane);

        // 4. Inject Controller into View and initialize UI
        booksPane.setController(controller);

        // === JavaFX SETUP ===

        // 5. Create and configure the scene
        Scene scene = new Scene(booksPane, 800, 600);
        primaryStage.setTitle("Books Database Client");

        // 6. Setup exit handler to properly disconnect from database
        primaryStage.setOnCloseRequest(event -> {
            try {
                booksDb.disconnect();
                System.out.println("Application closed, database disconnected");
            } catch (Exception e) {
                System.err.println("Error during shutdown: " + e.getMessage());
            }
        });

        // 7. Show the application
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}