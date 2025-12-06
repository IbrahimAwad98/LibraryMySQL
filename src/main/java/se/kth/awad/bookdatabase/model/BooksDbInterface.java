package se.kth.awad.bookdatabase.model;

import java.util.List;

/**
 * Interface för databaskommunikation med LibraryDB.
 */
public interface BooksDbInterface {

    /**
     * Ansluter till en databas.
     * @param database databasnamnet att ansluta till
     * @return true om anslutningen lyckades, false annars
     * @throws BooksDbException om anslutningen misslyckas
     */
    boolean connect(String database) throws BooksDbException;

    /**
     * Kopplar bort från databasen.
     * Måste anropas när applikationen stängs!
     * @return true om nedkopplingen lyckades, false annars
     * @throws BooksDbException om nedkopplingen misslyckas
     */
    boolean disconnect() throws BooksDbException;

    /**
     * Hämtar alla böcker från databasen.
     * @return lista över alla böcker
     * @throws BooksDbException om hämtningen misslyckas
     */
    List<Book> retrieveBookTable() throws BooksDbException;

    // Sökning metoder
    /**
     * Söker efter böcker efter titel.
     * @param searchTitle titeln att söka efter
     * @return lista över matchande böcker
     * @throws BooksDbException om sökningen misslyckas
     */
    List<Book> searchBooksByTitle(String searchTitle) throws BooksDbException;

    /**
     * Söker efter böcker efter ISBN.
     * @param searchISBN ISBN att söka efter
     * @return lista över matchande böcker
     * @throws BooksDbException om sökningen misslyckas
     */
    List<Book> searchBooksByISBN(String searchISBN) throws BooksDbException;

    /**
     * Söker efter böcker efter författare.
     * @param searchName författarens namn att söka efter
     * @return lista över matchande böcker
     * @throws BooksDbException om sökningen misslyckas
     */
    List<Book> searchBooksByAuthor(String searchName) throws BooksDbException;

    /**
     * Söker efter böcker efter genre.
     * @param searchGenre genren att söka efter
     * @return lista över matchande böcker
     * @throws BooksDbException om sökningen misslyckas
     */
    List<Book> searchBooksByGenre(String searchGenre) throws BooksDbException;

    /**
     * Lägger till en ny bok i databasen.
     * @param book boken att lägga till
     * @throws BooksDbException om insättningen misslyckas
     */
    void insertBook(Book book) throws BooksDbException;

    /**
     * Tar bort en bok från databasen.
     * @param book boken att ta bort
     * @throws BooksDbException om borttagningen misslyckas
     */
    void deleteBook(Book book) throws BooksDbException;

    /**
     * Uppdaterar en bok i databasen.
     * @param book boken med uppdaterad information
     * @throws BooksDbException om uppdateringen misslyckas
     */
    void updateBook(Book book) throws BooksDbException;

    // TODO: Följande metoder är definierade i interfacet men inte implementerade ännu
    // De kan implementeras i framtiden för utökad funktionalitet

    /**
     * Söker efter böcker efter minimum rating/betyg.
     * @param minRating minimibetyget
     * @return lista över matchande böcker
     * @throws BooksDbException om sökningen misslyckas
     */
    List<Book> searchBooksByRating(double minRating) throws BooksDbException;

    /**
     * Lägger till ett betyg för en bok.
     * @param bookID bokens ID
     * @param ratingValue betygsvärdet (1-10)
     * @return true om betyget lades till, false annars
     * @throws BooksDbException om insättningen misslyckas
     */
    boolean addRating(int bookID, int ratingValue) throws BooksDbException;

    /**
     * Hämtar alla författare för en specifik bok.
     * @param bookID bokens ID
     * @return lista över författare
     * @throws BooksDbException om hämtningen misslyckas
     */
    List<Author> getAuthorsForBook(int bookID) throws BooksDbException;

    /**
     * Hämtar alla författare från databasen.
     * @return lista över alla författare
     * @throws BooksDbException om hämtningen misslyckas
     */
    List<Author> getAllAuthors() throws BooksDbException;

    /**
     * Hämtar alla genrer från databasen.
     * @return lista över alla genrer
     * @throws BooksDbException om hämtningen misslyckas
     */
    List<Genre> getAllGenres() throws BooksDbException;

    /**
     * Hämtar alla genrer för en specifik bok.
     * @param bookID bokens ID
     * @return lista över genrer
     * @throws BooksDbException om hämtningen misslyckas
     */
    List<Genre> getGenresForBook(int bookID) throws BooksDbException;
}