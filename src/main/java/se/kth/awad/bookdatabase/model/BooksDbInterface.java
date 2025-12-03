package se.kth.awad.bookdatabase.model;

import java.util.ArrayList;

/**
 * Interface för databaskommunikation med LibraryDB.
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