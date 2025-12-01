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
import java.util.List;
import java.util.Optional;

import static javafx.scene.control.Alert.AlertType.*;

/**
 * The controller is responsible for handling user requests and update the view
 * (and in some cases the model).
 * Acts as the mediator between View and Model following MVC pattern.
 *
 * @author anderslm@kth.se
 */
public class BookDBController {

    private final BooksPane booksView; // view
    private final BooksDbInterface booksDb; // model

    public BookDBController(BooksDbInterface booksDb, BooksPane booksView) {
        this.booksDb = booksDb;
        this.booksView = booksView;
    }

    public void onSearchSelected(String searchFor, SearchMode mode) {
        try {
            if (searchFor != null && !searchFor.isEmpty()) {

                List<Book> result = null;
                switch (mode) {
                    case Title:
                        result = booksDb.searchBooksByTitle(searchFor);
                        break;
                    case ISBN:
                        result = booksDb.searchBooksByISBN(searchFor);
                        break;
                    case Author:
                        result = booksDb.searchBooksByAuthor(searchFor);
                        break;
                    case Grade:
                        result = booksDb.searchBooksByGrade(searchFor);
                        break;
                    case Genre:
                        result = booksDb.searchBooksByGenre(searchFor);
                        break;
                    default:
                        result= new ArrayList<>();
                }

                if (result == null || result.isEmpty()) {
                    booksView.showAlertAndWait(
                            "No results found.", INFORMATION);
                } else {
                    booksView.displayBooks(result);
                }

            } else {
                booksView.showAlertAndWait(
                        "Enter a search string!", WARNING);
            }
        } catch (Exception e) {
            booksView.showAlertAndWait("Database error: " + e.getMessage(), ERROR);
            e.printStackTrace();
        }
    }

    public void connectToDb() {
        try{
            boolean isConnected = booksDb.connect("bookdb");
            if(isConnected){
                List <Book> books = new ArrayList<>();
                try{
                    books = booksDb.retrieveBookTable();
                    booksView.displayBooks(books);
                    booksView.showAlertAndWait("Successfully connected to database 'bookdb'", CONFIRMATION);
                } catch (Exception e) {
                    e.printStackTrace();
                    booksView.showAlertAndWait("Error fetching table: " + e.getMessage(), ERROR);
                }
            } else {
                booksView.showAlertAndWait("Database 'bookdb' not found", WARNING);
            }
        } catch (Exception e) {
            booksView.showAlertAndWait("Connection failed: " + e.getMessage(), ERROR);
            e.printStackTrace();
        }
    }

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

    public void addBook() {
        AddBookDialog dialog = new AddBookDialog();
        try {
            Optional<Book> result = dialog.showAndWait();
            if (result.isPresent()) {
                Book book = result.get();
                booksDb.insertBook(book);
                refreshBookList();
                booksView.showAlertAndWait("Book '" + book.getTitle() + "' added successfully", CONFIRMATION);
            } else {
                booksView.showAlertAndWait("Book addition cancelled", INFORMATION);
            }
        } catch (Exception e) {
            booksView.showAlertAndWait("Error adding book: " + e.getMessage(), ERROR);
            e.printStackTrace();
        }
    }

    public void removeBook() {
        RemoveBookDialog dialog = new RemoveBookDialog();

        try {
            Optional<String> titleResult = dialog.showAndWait();
            if (titleResult.isPresent() && !titleResult.get().isEmpty()) {
                String title = titleResult.get();

                // Search for the book
                List<Book> books = booksDb.searchBooksByTitle(title);

                if (!books.isEmpty()) {
                    Book book = books.get(0);
                    booksDb.deleteBook(book);
                    refreshBookList();
                    booksView.showAlertAndWait("Book '" + title + "' deleted successfully", CONFIRMATION);
                } else {
                    booksView.showAlertAndWait("No book found with title: " + title, WARNING);
                }
            } else {
                booksView.showAlertAndWait("Book deletion cancelled", INFORMATION);
            }
        } catch (Exception e) {
            booksView.showAlertAndWait("Error removing book: " + e.getMessage(), ERROR);
            e.printStackTrace();
        }
    }
    public void updateGrade() {
        UpdateGradeDialog dialog = new UpdateGradeDialog();

        try {
            Optional<UpdateGradeDialog.GradeUpdate> result = dialog.showAndWait();
            if (result.isPresent()) {
                UpdateGradeDialog.GradeUpdate gradeUpdate = result.get();
                String title = gradeUpdate.getTitle();
                int newGrade = gradeUpdate.getGrade();

                // Search for the book
                List<Book> books = booksDb.searchBooksByTitle(title);

                if (!books.isEmpty()) {
                    Book book = books.get(0);
                    book.setGrade(newGrade);
                    booksDb.updateBook(book);
                    refreshBookList();
                    booksView.showAlertAndWait("Grade for '" + title + "' updated to " + newGrade, CONFIRMATION);
                } else {
                    booksView.showAlertAndWait("No book found with title: " + title, WARNING);
                }
            } else {
                booksView.showAlertAndWait("Grade update cancelled", INFORMATION);
            }
        } catch (Exception e) {
            booksView.showAlertAndWait("Error updating grade: " + e.getMessage(), ERROR);
            e.printStackTrace();
        }
    }

    /**
     * Helper method to refresh the book list in the view
     */
    private void refreshBookList() throws BooksDbException {
        List<Book> books = booksDb.retrieveBookTable();
        booksView.displayBooks(books);
    }
}
