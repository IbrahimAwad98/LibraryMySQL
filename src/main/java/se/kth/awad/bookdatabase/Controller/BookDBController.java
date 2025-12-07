package se.kth.awad.bookdatabase.Controller;

import se.kth.awad.bookdatabase.model.Book;
import se.kth.awad.bookdatabase.model.BooksDbException;
import se.kth.awad.bookdatabase.model.BooksDbInterface;
import se.kth.awad.bookdatabase.model.SearchMode;
import se.kth.awad.bookdatabase.view.AddBookDialog;
import se.kth.awad.bookdatabase.view.BooksPane;
import se.kth.awad.bookdatabase.view.RemoveBookDialog;
import se.kth.awad.bookdatabase.view.UpdateGradeDialog;
import java.util.ArrayList;
import javafx.concurrent.Task;
import java.util.List;
import java.util.Optional;
import static javafx.scene.control.Alert.AlertType.*;

/**
 * Controller i MVC-mönstret.
 * Hanterar all logik mellan View (BooksPane) och Model (BooksDb).
 */
public class BookDBController {

    private final BooksPane booksView;
    private final BooksDbInterface booksDb;

    public BookDBController(BooksDbInterface booksDb, BooksPane booksView) {
        this.booksDb = booksDb;
        this.booksView = booksView;
    }

    //Anslut till databasen och ladda alla böcker.
    public void connectToDb() {
        try{
            boolean isConnected = booksDb.connect("LibraryDB");
            if(isConnected){
                List <Book> books = new ArrayList<>();
                try{
                    books = booksDb.retrieveBookTable();
                    booksView.displayBooks(books);
                    booksView.showAlertAndWait("Successfully connected to database 'LibraryDB'", CONFIRMATION);
                } catch (Exception e) {
                    e.printStackTrace();
                    booksView.showAlertAndWait("Error fetching table: " + e.getMessage(), ERROR);
                }
            } else {
                booksView.showAlertAndWait("Database 'LibraryDB' not found", WARNING);
            }
        } catch (Exception e) {
            booksView.showAlertAndWait("Connection failed: " + e.getMessage(), ERROR);
            e.printStackTrace();
        }
    }

    // Koppla ned från databasen och töm tabellen.
    public void disconnect() {
        List<Book> books = new ArrayList<>();
        try {
            boolean isDisconnected = booksDb.disconnect();
            if (isDisconnected) {
                booksView.showAlertAndWait("Successfully disconnected from database", INFORMATION);
                booksView.displayBooks(books);
            } else {
                booksView.showAlertAndWait("Not connected to any database", WARNING);
            }
        } catch (Exception e) {
            booksView.showAlertAndWait("Error disconnecting: " + e.getMessage(), ERROR);
            e.printStackTrace();
        }
    }

    //Sök efter böcker baserat på vald söktyp.
    public void onSearchSelected(String searchFor, SearchMode mode) {
        if (searchFor == null || searchFor.isEmpty()) {
            booksView.showAlertAndWait("Enter a search string!", WARNING);
            return;
        }
        Task<List<Book>> task = new Task<List<Book>>() {
            @Override
            protected List<Book> call() throws Exception {
                switch (mode){
                    case Title:
                        return booksDb.searchBooksByTitle(searchFor);
                    case ISBN:
                        return booksDb.searchBooksByISBN(searchFor);
                    case Author:
                        return booksDb.searchBooksByAuthor(searchFor);
                    case Rating:
                        int minRating = (int) Double.parseDouble(searchFor);
                        return booksDb.searchBooksByRating(minRating);
                    case Genre:
                        return booksDb.searchBooksByGenre(searchFor);
                    default:
                        return new ArrayList<>();
                }
            }
        };
        task.setOnSucceeded(e->{
            List<Book> result = task.getValue();
            if(result == null || result.isEmpty()){
                booksView.showAlertAndWait("No results found.", INFORMATION);
            }else{
                booksView.displayBooks(result); // Visa resultaten i TableView
            }
        });
        //misslyckas ! då visa fel
        task.setOnFailed(e->{
            Throwable ex = task.getException();
            booksView.showAlertAndWait("Database error: " + ex.getMessage(), ERROR);
            ex.printStackTrace();
        });
        //starta tråden för att inte frysa ui
        Thread thr = new Thread(task);
        thr.setDaemon(true);
        thr.start();
    }

    // Visa dialog och lägg till ny bok.
    public void addBook() {
        AddBookDialog dialog = new AddBookDialog();
        Optional<Book> result = dialog.showAndWait();
        if(result.isEmpty()) {
            booksView.showAlertAndWait("Book addition cancelled", INFORMATION);
            return;
        }
        Book book = result.get();

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                booksDb.insertBook(book);
                return null;
            }
        };

        task.setOnSucceeded(e->{
            try {
                refreshBookList();
                booksView.showAlertAndWait("Book '" + book.getTitle() + "' added successfully", CONFIRMATION);
            }catch (BooksDbException ex) {
                booksView.showAlertAndWait("Error refreshing list: " + ex.getMessage(), ERROR);
            }
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            booksView.showAlertAndWait("Error adding book: " + ex.getMessage(), ERROR);
            ex.printStackTrace();
        });

        Thread thr = new Thread(task);
        thr.setDaemon(true);
        thr.start();
    }

    //Visa dialog och ta bort bok.
    public void removeBook() {
        RemoveBookDialog dialog = new RemoveBookDialog();
        Optional<String> titleResult = dialog.showAndWait();

        if (titleResult.isEmpty() || titleResult.get().isBlank()) {
            booksView.showAlertAndWait("Book deletion cancelled", INFORMATION);
            return;
        }
        String title = titleResult.get().trim();

        Task<Void>task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                List<Book> books = booksDb.searchBooksByTitle(title);
                if (books.isEmpty()){
                    throw new BooksDbException("No book found with title: "+ title);
                }
                Book book = books.get(0);
                booksDb.deleteBook(book);

                return null;
            }
        };

        task.setOnSucceeded(e -> {
            try{
                refreshBookList();
                booksView.showAlertAndWait("Book '" + title + "' deleted successfully", CONFIRMATION);


            }catch(BooksDbException ex) {
                booksView.showAlertAndWait("Error refreshing list: "+ ex.getMessage(), ERROR);
                ex.printStackTrace();
            }
        });

        task.setOnFailed(e->{
            Throwable ex = task.getException();
            if (ex instanceof BooksDbException) {
                booksView.showAlertAndWait(ex.getMessage(),WARNING);
            }else {
                booksView.showAlertAndWait("Error removing book: " + ex.getMessage(),ERROR);
            }
        });

        Thread thr = new Thread(task);
        thr.setDaemon(true);
        thr.start();

    }
    //Visa dialog och lägg till betyg på bok.
    public void updateGrade() {
        UpdateGradeDialog dialog = new UpdateGradeDialog();
        Optional<UpdateGradeDialog.GradeUpdate> result = dialog.showAndWait();

        if(!result.isPresent()){
            booksView.showAlertAndWait("Grade Update Cancelled!!!",INFORMATION);
            return;
        }

        UpdateGradeDialog.GradeUpdate gradeUpdate = result.get();
        String title = gradeUpdate.getTitle();
        int newGrade = gradeUpdate.getGrade();

        if (title == null || title.trim().isEmpty()) {
            booksView.showAlertAndWait("Title cannot be empty", WARNING);
            return;
        }
        String trimmedTitle = title.trim();

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                List<Book> books = booksDb.searchBooksByTitle(trimmedTitle);
                if(books.isEmpty()){
                    throw new BooksDbException("No book found with title: " + trimmedTitle);
                }
                Book book = books.get(0);
                booksDb.addRating(book.getBookID(), newGrade);
                return null;
            }
        };

        task.setOnSucceeded(e->{
            try {
                refreshBookList();
                booksView.showAlertAndWait(
                        "Grade for '" + trimmedTitle + "' updated to " + newGrade,
                        CONFIRMATION
                );
            } catch (BooksDbException ex) {
                booksView.showAlertAndWait(
                        "Error refreshing list: " + ex.getMessage(),
                        ERROR
                );
                ex.printStackTrace();
            }

        });

        task.setOnFailed(e->{
            Throwable ex = task.getException();
            if (ex instanceof BooksDbException) {
                booksView.showAlertAndWait(ex.getMessage(), WARNING);
            } else {
                booksView.showAlertAndWait(
                        "Error updating grade: " + ex.getMessage(),
                        ERROR
                );
            }
            ex.printStackTrace();
        });

        Thread thr = new Thread(task);
        thr.setDaemon(true);
        thr.start();

    }

    /**
     * Hjälpmetod för att ladda om alla böcker från databasen.
     */
    private void refreshBookList() throws BooksDbException {
        List<Book> books = booksDb.retrieveBookTable();
        booksView.displayBooks(books);
    }
}