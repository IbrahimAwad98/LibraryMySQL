package se.kth.awad.bookdatabase.model;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Interface för databaskommunikation med LibraryDB.
 *
 * Detta är KRAV 3 i uppgiften:
 * "Koden som kommunicerar med databasen ska ha separata metoder för varje
 * specifik select/update/insert-sats(er) som klientapplikationen ska kunna
 * exekvera på servern."
 *
 * VIKTIGT: Kontrollern ska endast känna till detta interface, INTE
 * implementationen (BooksDbImpl)!
 */
public interface BooksDbInterface {

   //Sökning metoder
  ArrayList<Book> searchBooksByTitle(String searchString) throws Exception;
  Book searchBookByISBN(String ISBN) throws Exception;
  ArrayList<Book> searchBooksByAuthor(String authorName) throws Exception;
  ArrayList<Book> searchBooksByGenre(String genreName) throws Exception;
  ArrayList<Book> searchBooksByRating(double minRating) throws Exception;


  boolean addBook(Book book) throws Exception;
  boolean addRating(int bookID, int ratingValue) throws Exception;

  //Hämtar data för GUI
  ArrayList<Author> getAuthorsForBook(int bookID) throws Exception;
  ArrayList<Author> getAllAuthors() throws Exception;
  ArrayList<Genre> getAllGenres() throws Exception;
  ArrayList<Genre> getGenresForBook(int bookID) throws Exception;

    /**
     * Kopplar bort från databasen.
     * Måste anropas när applikationen stängs!
     */
  void disconnect() throws Exception;

}