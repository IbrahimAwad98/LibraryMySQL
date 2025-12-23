package se.kth.awad.bookdatabase.model;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
/**
 * JDBC-implementation av BooksDbInterface.
 * Denna klass kommunicerar direkt med LibraryDB i MySQL.
 */
public class BooksDb implements BooksDbInterface {

    //skapa konstanter för den gemensamma SQL-frågor
    private static final String BASE_BOOK_QUERY =
            "SELECT B.bookID, B.ISBN, B.title, B.publishedDate, " +
                    "B.addedByUserID, U.username AS addedByUsername, " +
                    "GROUP_CONCAT(DISTINCT A.name SEPARATOR ', ') AS authors, " +
                    "GROUP_CONCAT(DISTINCT G.genreName SEPARATOR ', ') AS genres, " +
                    "AVG(R.ratingValue) AS averageRating, " +
                    "COUNT(DISTINCT R.ratingID) AS numRatings " +
                    "FROM T_Book AS B " +
                    "LEFT JOIN T_User AS U ON B.addedByUserID = U.userID " +
                    "LEFT JOIN T_Book_Author AS BA ON B.bookID = BA.bookID " +
                    "LEFT JOIN T_Author AS A ON BA.authorID = A.authorID " +
                    "LEFT JOIN T_Book_Genre AS BG ON B.bookID = BG.bookID " +
                    "LEFT JOIN T_Genre AS G ON BG.genreID = G.genreID " +
                    "LEFT JOIN T_Rating AS R ON B.bookID = R.bookID ";

    private static final String GROUP_BY_CLAUSE =
            "GROUP BY B.bookID, B.ISBN, B.title, B.publishedDate, B.addedByUserID, U.username";

    private final DatabaseConnection dbConnection;

    // Anslutnings Hantering
    public BooksDb() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    @Override
    public boolean connect(String database) throws BooksDbException {
        return dbConnection.connect(database);
    }

    @Override
    public boolean disconnect() throws BooksDbException {
        return dbConnection.disconnect();
    }

    // bok sök-metoder
    @Override
    public List<Book> retrieveBookTable() throws BooksDbException {
        List<Book> result = new ArrayList<>();

        String sql = BASE_BOOK_QUERY + GROUP_BY_CLAUSE;

        try (Statement statement = dbConnection.getConnection().createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                result.add(buildBookFromResultSet(resultSet));
            }
            return result;

        } catch (SQLException e) {
            throw new BooksDbException("Error in fetching book table", e);
        }
    }

    @Override
    public List<Book> searchBooksByTitle(String searchTitle) throws BooksDbException {
        List<Book> result = new ArrayList<>();

        String sql = BASE_BOOK_QUERY + "WHERE LOWER(B.title) LIKE ? " + GROUP_BY_CLAUSE;

        //try with resources
        try (PreparedStatement preparedStatement = dbConnection.getConnection().prepareStatement(sql);) {
            preparedStatement.setString(1, "%" + searchTitle.toLowerCase() + "%");

            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    result.add(buildBookFromResultSet(rs));
                }
            }
            return result;

        } catch (SQLException e) {
            throw new BooksDbException("Error searching books by title", e);
        }
    }

    @Override
    public List<Book> searchBooksByISBN(String searchISBN) throws BooksDbException {
        List<Book> result = new ArrayList<>();

        String sql = BASE_BOOK_QUERY + "WHERE B.ISBN = ? " + GROUP_BY_CLAUSE;

        try (PreparedStatement preparedStatement = dbConnection.getConnection().prepareStatement(sql)) {
            preparedStatement.setString(1, searchISBN);

            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    result.add(buildBookFromResultSet(rs));
                }
            }
            return result;

        } catch (SQLException e) {
            throw new BooksDbException("Error searching books by ISBN", e);
        }
    }

    @Override
    public List<Book> searchBooksByAuthor(String searchName) throws BooksDbException {
        List<Book> result = new ArrayList<>();

        String sql = BASE_BOOK_QUERY + "WHERE LOWER(A.name) LIKE ? " + GROUP_BY_CLAUSE;

        try (PreparedStatement preparedStatement = dbConnection.getConnection().prepareStatement(sql)) {
            preparedStatement.setString(1, "%" + searchName.toLowerCase() + "%");

            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    result.add(buildBookFromResultSet(rs));
                }
            }
            return result;

        } catch (SQLException e) {
            throw new BooksDbException("Error searching books by author", e);
        }
    }

    @Override
    public List<Book> searchBooksByGenre(String searchGenre) throws BooksDbException {
        List<Book> result = new ArrayList<>();

        String sql = BASE_BOOK_QUERY + "WHERE LOWER(G.genreName) LIKE ? " + GROUP_BY_CLAUSE;

        try (PreparedStatement preparedStatement = dbConnection.getConnection().prepareStatement(sql)) {
            preparedStatement.setString(1, "%" + searchGenre.toLowerCase() + "%");

            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    result.add(buildBookFromResultSet(rs));
                }
            }
            return result;

        } catch (SQLException e) {
            throw new BooksDbException("Error searching books by genre", e);
        }
    }

    @Override
    public List<Book> searchBooksByRating(int  minRating) throws BooksDbException {
        List<Book> result = new ArrayList<>();

        // OBS: HAVING används eftersom vi filtrerar på aggregerad data
        String sql = BASE_BOOK_QUERY + GROUP_BY_CLAUSE + " HAVING MAX(R.ratingValue) = ?";

        try (PreparedStatement preparedStatement = dbConnection.getConnection().prepareStatement(sql)) {
            preparedStatement.setInt(1, minRating);

            try (ResultSet resultSets = preparedStatement.executeQuery()) {
                while (resultSets.next()) {
                    result.add(buildBookFromResultSet(resultSets));
                }
            }
            return result;

        } catch (SQLException e) {
            throw new BooksDbException("Error searching books by rating", e);
        }
    }

    @Override
    public void insertBook(Book book) throws BooksDbException {
        Connection connection = null;
        try {
            connection = dbConnection.getConnection();
            connection.setAutoCommit(false); // Starta transaktion

            // Sätt in bokförteckningen
            int bookID = insertBookRecord(connection, book);
            book.setBookID(bookID);

            // Hantera författare
            if (book.getAuthors() != null && !book.getAuthors().isEmpty()) {
                System.out.println("[BooksDb] Processing " + book.getAuthors().size() + " author(s) for book: " + book.getTitle());

                for (Author author : book.getAuthors()) {
                    System.out.println("[BooksDb] Processing author: " + author.getName());
                    int authorID = getOrCreateAuthor(connection, author);
                    linkBookAuthor(connection, bookID, authorID);
                }
            }

            // Hantera genre
            if (book.getGenres() != null && !book.getGenres().isEmpty()) {
                System.out.println("[BooksDb] Processing " + book.getGenres().size() + " genre(s) for book: " + book.getTitle());

                for (Genre genre : book.getGenres()) {
                    System.out.println("[BooksDb] Processing genre: " + genre.getGenreName());
                    int genreID = getOrCreateGenre(connection, genre);
                    linkBookGenre(connection, bookID, genreID);
                }
            }

            connection.commit();
            System.out.println("[BooksDb] Book inserted successfully with ID: " + bookID);

        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                    System.out.println("[BooksDb] Rollback - Error inserting book");
                } catch (SQLException rollbackEx) {
                    throw new BooksDbException("Rollback failed", rollbackEx);
                }
            }
            throw new BooksDbException("Error inserting book: " + e.getMessage(), e);
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void deleteBook(Book book) throws BooksDbException {
        Connection connection = null;
        try {
            connection = dbConnection.getConnection();
            connection.setAutoCommit(false);

            // Ta bort boken (kaskadfunktionen hanterar Book_Author och Book_Genre)
            deleteBookRecord(connection, book.getBookID());

            // Rensa bort författare
            if (book.getAuthors() != null) {
                for (Author author : book.getAuthors()) {
                    if (author.getAuthorID() > 0) {
                        deleteAuthorIfOrphaned(connection, author.getAuthorID());
                    }
                }
            }

            connection.commit();
            System.out.println("[BooksDb] Book deleted successfully (ID: " + book.getBookID() + ")");

        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                    System.out.println("[BooksDb] Rollback - Error deleting book");
                } catch (SQLException rollbackEx) {
                    throw new BooksDbException("Rollback failed", rollbackEx);
                }
            }
            throw new BooksDbException("Failed to delete book: " + e.getMessage(), e);
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void updateBook(Book book) throws BooksDbException {
        Connection connection = null;
        try {
            connection = dbConnection.getConnection();
            connection.setAutoCommit(false);

            String sql = "UPDATE T_Book SET ISBN = ?, title = ?, publishedDate = ? WHERE bookID = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, book.getISBN());
                pstmt.setString(2, book.getTitle());
                pstmt.setDate(3, book.getPublishedDate());
                pstmt.setInt(4, book.getBookID());

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("No book found with bookID: " + book.getBookID());
                }
            }

            connection.commit();
            System.out.println("[BooksDb] Book updated successfully (ID: " + book.getBookID() + ")");

        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                    System.out.println("[BooksDb] Rollback - Error updating book");
                } catch (SQLException rollbackEx) {
                    throw new BooksDbException("Rollback failed", rollbackEx);
                }
            }
            throw new BooksDbException("Error updating book: " + e.getMessage(), e);
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean addRating(int bookID, int ratingValue) throws BooksDbException {
        if (ratingValue < 1 || ratingValue > 10) {
            throw new BooksDbException("Rating value must be between 1 and 10");
        }

        Connection connection = null;
        try {
            connection = dbConnection.getConnection();
            connection.setAutoCommit(false);

            // ta bor befintliga betyg
            String deleteSql = "DELETE FROM T_Rating WHERE bookID = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(deleteSql)) {
                pstmt.setInt(1, bookID);
                pstmt.executeUpdate();
            }

            // Infoga nytt betyg
            String insertSql = "INSERT INTO T_Rating (bookID, ratingValue, ratingDate) VALUES (?, ?, CURDATE())";
            try (PreparedStatement pstmt = connection.prepareStatement(insertSql)) {
                pstmt.setInt(1, bookID);
                pstmt.setInt(2, ratingValue);

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    connection.commit();
                    System.out.println("[BooksDb] Rating updated (replaced previous rating)");
                    return true;
                } else {
                    connection.rollback();
                    System.out.println("[BooksDb] No rating added");
                    return false;
                }
            }

        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                    System.out.println("[BooksDb] Rollback - Error adding rating");
                } catch (SQLException rollbackEx) {
                    throw new BooksDbException("Failed to rollback transaction", rollbackEx);
                }
            }
            throw new BooksDbException("Failed to add rating for book ID " + bookID, e);
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException e) {
                    System.err.println("[BooksDb] Warning: Failed to reset auto-commit");
                }
            }
        }
    }

    //privata hjälp metoder
    private int insertBookRecord(Connection con, Book book) throws SQLException {
        String sql = "INSERT INTO T_Book (ISBN, title, publishedDate) VALUES (?, ?, ?)";

        try (PreparedStatement preparedStatement = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, book.getISBN());
            preparedStatement.setString(2, book.getTitle());
            preparedStatement.setDate(3, book.getPublishedDate());

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Inserting book failed, no rows affected.");
            }

            try (ResultSet keys = preparedStatement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
                throw new SQLException("Failed to get generated book ID");
            }
        }
    }

    private int getOrCreateAuthor(Connection con, Author author) throws SQLException {
        String checkSql = "SELECT authorID FROM T_Author WHERE name = ?";

        try (PreparedStatement preparedStatement = con.prepareStatement(checkSql)) {
            preparedStatement.setString(1, author.getName());

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("authorID");
                }
            }
        }
        //skapa ny
        String insertSql = "INSERT INTO T_Author (name, birthDate) VALUES (?, ?)";
        try (PreparedStatement preparedStatement = con.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, author.getName());
            preparedStatement.setDate(2, author.getBirthDate());
            preparedStatement.executeUpdate();

            try (ResultSet keys = preparedStatement.getGeneratedKeys()) {
                if (keys.next()) {
                    int authorID = keys.getInt(1);
                    author.setAuthorID(authorID);
                    return authorID;
                }
                throw new SQLException("Failed to get generated author ID");
            }
        }
    }

    private int getOrCreateGenre(Connection con, Genre genre) throws SQLException {
        String checkSql = "SELECT genreID FROM T_Genre WHERE genreName = ?";

        try (PreparedStatement preparedStatement = con.prepareStatement(checkSql)) {
            preparedStatement.setString(1, genre.getGenreName());

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("genreID");
                }
            }
        }
        //skapa ny
        String insertSql = "INSERT INTO T_Genre (genreName) VALUES (?)";
        try (PreparedStatement preparedStatement = con.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, genre.getGenreName());
            preparedStatement.executeUpdate();

            try (ResultSet keys = preparedStatement.getGeneratedKeys()) {
                if (keys.next()) {
                    int genreID = keys.getInt(1);
                    genre.setGenreID(genreID);
                    return genreID;
                }
                throw new SQLException("Failed to get generated genre ID");
            }
        }
    }

    /**
     * Länkar en bok till en författare i kopplingstabellen.
     */
    private void linkBookAuthor(Connection con, int bookID, int authorID) throws SQLException {
        String sql = "INSERT INTO T_Book_Author (bookID, authorID) VALUES (?, ?)";
        try (PreparedStatement preparedStatement = con.prepareStatement(sql)) {
            preparedStatement.setInt(1, bookID);
            preparedStatement.setInt(2, authorID);
            preparedStatement.executeUpdate();
        }
    }

    /**
     * Länkar en bok till en genre i kopplingstabellen.
     */
    private void linkBookGenre(Connection con, int bookID, int genreID) throws SQLException {
        String sql = "INSERT INTO T_Book_Genre (bookID, genreID) VALUES (?, ?)";
        try (PreparedStatement preparedStatement = con.prepareStatement(sql)) {
            preparedStatement.setInt(1, bookID);
            preparedStatement.setInt(2, genreID);
            preparedStatement.executeUpdate();
        }
    }

    private void deleteBookRecord(Connection con, int bookID) throws SQLException {
        String sql = "DELETE FROM T_Book WHERE bookID = ?";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, bookID);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No book found with bookID: " + bookID);
            }
        }
    }

    /**
     * Tar bort en författare om de inte har några andra böcker.
     */
    private void deleteAuthorIfOrphaned(Connection con, int authorID) throws SQLException {

        String checkSql = "SELECT COUNT(*) FROM T_Book_Author WHERE authorID = ?";
        try (PreparedStatement preparedStatement = con.prepareStatement(checkSql)) {
            preparedStatement.setInt(1, authorID);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next() && resultSet.getInt(1) == 0) {

                    String deleteSql = "DELETE FROM T_Author WHERE authorID = ?";
                    try (PreparedStatement deletePstmt = con.prepareStatement(deleteSql)) {
                        deletePstmt.setInt(1, authorID);
                        deletePstmt.executeUpdate();
                    }
                }
            }
        }
    }

    /**
     * Bygger ett Book-objekt från ResultSet.
     */
    private Book buildBookFromResultSet(ResultSet rs) throws SQLException {
        int bookID = rs.getInt("bookID");
        String ISBN = rs.getString("ISBN");
        String title = rs.getString("title");
        Date publishedDate = rs.getDate("publishedDate");

        Book book = new Book(bookID, ISBN, title, publishedDate);


        int addedByUserID = rs.getInt("addedByUserID");
        if (!rs.wasNull()) {
            book.setAddedByUserID(addedByUserID);
            String addedByUsername = rs.getString("addedByUsername");
            if (addedByUsername != null) {
                book.setAddedByUsername(addedByUsername);  // ← NY RAD!
            }
        }

        // Hämta det unika betyget (inte genomsnitt)
        int rating = rs.getInt("averageRating");
        if (!rs.wasNull()) {
            book.setAverageRating(rating);
        }
        int numRatings = rs.getInt("numRatings");
        book.setNumRatings(numRatings);

        String authors = rs.getString("authors");
        if (authors != null && !authors.isEmpty()) {
            for (String name : authors.split(", ")) {
                book.addAuthor(new Author(name.trim()));
            }
        }

        String genres = rs.getString("genres");
        if (genres != null && !genres.isEmpty()) {
            for (String genreName : genres.split(", ")) {
                book.addGenre(new Genre(genreName.trim()));
            }
        }

        return book;
    }

    /**
     * Bygger ett Author-objekt från den aktuella ResultSet-raden.
     */
    private Author buildAuthorFromResultSet(ResultSet rs) throws SQLException {
        int authorID = rs.getInt("authorID");
        String name = rs.getString("name");
        Date birthDate = rs.getDate("birthDate");
        return new Author(authorID, name, birthDate);
    }

    /**
     * Bygger ett Genere-objekt från den aktuella ResultSet-raden.
     */
    private Genre buildGenreFromResultSet(ResultSet rs) throws SQLException {
        int genreID = rs.getInt("genreID");
        String genreName = rs.getString("genreName");
        return new Genre(genreID, genreName);
    }
    @Override
    public List<Author> getAuthorsForBook(int bookID) throws BooksDbException {
        List<Author> result = new ArrayList<>();
        String sql = "SELECT A.authorID, A.name, A.birthDate " +
                "FROM T_Author AS A " +
                "JOIN T_Book_Author AS BA ON A.authorID = BA.authorID " +
                "WHERE BA.bookID = ?";

        try (PreparedStatement preparedStatement = dbConnection.getConnection().prepareStatement(sql)) {
            preparedStatement.setInt(1, bookID);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    result.add(buildAuthorFromResultSet(resultSet));
                }
            }
            return result;

        } catch (SQLException e) {
            throw new BooksDbException("Error getting authors for book: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Author> getAllAuthors() throws BooksDbException {
        List<Author> result = new ArrayList<>();

        String sql = "SELECT authorID, name, birthDate FROM T_Author ORDER BY name";

        try (Statement statement = dbConnection.getConnection().createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                result.add(buildAuthorFromResultSet(resultSet));
            }
            return result;

        } catch (SQLException e) {
            throw new BooksDbException("Error getting all authors: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Genre> getAllGenres() throws BooksDbException {
        List<Genre> result = new ArrayList<>();
        String sql = "SELECT genreID, genreName FROM T_Genre ORDER BY genreName";

        try (Statement statement = dbConnection.getConnection().createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                result.add(buildGenreFromResultSet(resultSet));
            }
            return result;

        } catch (SQLException e) {
            throw new BooksDbException("Error getting all genres: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Genre> getGenresForBook(int bookID) throws BooksDbException {
        List<Genre> result = new ArrayList<>();
        String sql = "SELECT G.genreID, G.genreName " +
                "FROM T_Genre AS G " +
                "JOIN T_Book_Genre AS BG ON G.genreID = BG.genreID " +
                "WHERE BG.bookID = ?";

        try (PreparedStatement preparedStatement = dbConnection.getConnection().prepareStatement(sql)) {
            preparedStatement.setInt(1, bookID);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    result.add(buildGenreFromResultSet(resultSet));
                }
            }
            return result;

        } catch (SQLException e) {
            throw new BooksDbException("Error getting genres for book: " + e.getMessage(), e);
        }
    }

    @Override
    public User login(String username, String password) throws BooksDbException {
        String sql = "SELECT userID, username FROM T_User " +
                "WHERE username = ? AND password = ?";

        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int userID = rs.getInt("userID");
                    String user = rs.getString("username");
                    return new User(userID, user);
                } else {
                    throw new BooksDbException("Fel användarnamn eller lösenord");
                }
            }
        } catch (SQLException e) {
            throw new BooksDbException("Databasfel vid inloggning: " + e.getMessage(), e);
        }
    }

    @Override
    public void createUser(String username, String password) throws BooksDbException {
        String sql = "INSERT INTO T_User (username, password) VALUES (?,?)";

        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new BooksDbException("Kunde inte skapa användare");
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                throw new BooksDbException("Användarnamnet '" + username + "' är redan taget");
            }
            throw new BooksDbException("Databasfel: " + e.getMessage(), e);
        }
    }

    @Override
    public void rateBookAsUser(int bookID, int userID, int ratingValue) throws BooksDbException {
        // Validering
        if (ratingValue < 1 || ratingValue > 10) {
            throw new BooksDbException("Betyg måste vara mellan 1 och 10");
        }

        String sql = "INSERT INTO T_Rating (bookID, userID, ratingValue, ratingDate) " +
                "VALUES (?, ?, ?, CURDATE())";

        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, bookID);
            pstmt.setInt(2, userID);
            pstmt.setInt(3, ratingValue);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry") ||
                    e.getMessage().contains("unique_user_book_rating")) {
                throw new BooksDbException(
                        "Du har redan betygsatt denna bok! Endast ett betyg per användare tillåts."
                );
            }
            throw new BooksDbException("Databasfel vid betygsättning: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean hasUserRatedBook(int bookID, int userID) throws BooksDbException {
        String sql = "SELECT COUNT(*) AS count FROM T_Rating WHERE bookID = ? AND userID = ?";

        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, bookID);
            pstmt.setInt(2, userID);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count") > 0;
                }
                return false;
            }
        } catch (SQLException e) {
            throw new BooksDbException("Databasfel: " + e.getMessage(), e);
        }
    }

    @Override
    public void addReview(int bookID, int userID, String reviewText) throws BooksDbException {
        if (reviewText == null || reviewText.trim().isEmpty()) {
            throw new BooksDbException("Recensionen kan inte vara tom");
        }

        String sql = "INSERT INTO T_Review (bookID, userID, reviewText, reviewDate) " +
                "VALUES (?, ?, ?, CURDATE())";

        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, bookID);
            pstmt.setInt(2, userID);
            pstmt.setString(3, reviewText.trim());

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new BooksDbException("Kunde inte lägga till recension");
            }
        } catch (SQLException e) {
            throw new BooksDbException("Databasfel: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Review> getReviewsForBook(int bookID) throws BooksDbException {
        List<Review> reviews = new ArrayList<>();

        String sql = "SELECT r.reviewID, r.bookID, r.reviewText, r.reviewDate, " +
                "       u.userID, u.username " +
                "FROM T_Review r " +
                "JOIN T_User u ON r.userID = u.userID " +
                "WHERE r.bookID = ? " +
                "ORDER BY r.reviewDate DESC";

        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, bookID);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int reviewID = rs.getInt("reviewID");
                    int book = rs.getInt("bookID");
                    String text = rs.getString("reviewText");
                    LocalDate date = rs.getDate("reviewDate").toLocalDate();

                    // Skapa User-objekt för recensenten
                    int userId = rs.getInt("userID");
                    String username = rs.getString("username");
                    User user = new User(userId, username);

                    // Skapa Review-objekt
                    Review review = new Review(reviewID, book, user, text, date);
                    reviews.add(review);
                }
            }
        } catch (SQLException e) {
            throw new BooksDbException("Databasfel: " + e.getMessage(), e);
        }

        return reviews;
    }

    @Override
    public void insertBookAsUser(Book book, List<Integer> authorIDs,
                                 List<String> genreNames, int userID)
            throws BooksDbException {
        Connection connection = null;
        try {
            connection = dbConnection.getConnection();
            connection.setAutoCommit(false);

            // Lägg till boken MED userID
            int bookID = insertBookRecordWithUser(connection, book, userID);
            book.setBookID(bookID);

            // Koppla författare
            if (authorIDs != null && !authorIDs.isEmpty()) {
                for (int authorID : authorIDs) {
                    linkBookAuthor(connection, bookID, authorID);
                }
            }

            // Koppla genrer
            if (genreNames != null && !genreNames.isEmpty()) {
                for (String genreName : genreNames) {
                    Genre genre = new Genre(genreName);
                    int genreID = getOrCreateGenre(connection, genre);
                    linkBookGenre(connection, bookID, genreID);
                }
            }

            connection.commit();

        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    throw new BooksDbException("Rollback misslyckades: " + rollbackEx.getMessage());
                }
            }
            throw new BooksDbException("Kunde inte lägga till bok: " + e.getMessage(), e);
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void insertAuthorAsUser(Author author, int userID) throws BooksDbException {
        String sql = "INSERT INTO T_Author (name, birthDate, addedByUserID) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, author.getName());

            if (author.getBirthDate() != null) {
                pstmt.setDate(2, author.getBirthDate());
            } else {
                pstmt.setNull(2, java.sql.Types.DATE);
            }

            pstmt.setInt(3, userID);

            pstmt.executeUpdate();

            // Sätt authorID på objektet
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    author.setAuthorID(rs.getInt(1));
                }
            }

        } catch (SQLException e) {
            throw new BooksDbException("Kunde inte lägga till författare: " + e.getMessage(), e);
        }
    }

    /**
     * VG Helper: Lägger till bokpost med användarID
     */
    private int insertBookRecordWithUser(Connection con, Book book, int userID) throws SQLException {
        String sql = "INSERT INTO T_Book (ISBN, title, publishedDate, addedByUserID) " +
                "VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, book.getISBN());
            pstmt.setString(2, book.getTitle());
            pstmt.setDate(3, book.getPublishedDate());
            pstmt.setInt(4, userID);

            pstmt.executeUpdate();

            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
                throw new SQLException("Failed to get generated book ID");
            }
        }
    }

    @Override
    public void deleteBookAsUser(int bookID, int userID) throws BooksDbException {
        Connection connection = null;
        try {
            connection = dbConnection.getConnection();
            connection.setAutoCommit(false);

            // Hämta författar-IDn direkt här
            List<Integer> authorIDs = new ArrayList<>();
            String getAuthorsSql = "SELECT authorID FROM T_Book_Author WHERE bookID = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(getAuthorsSql)) {
                pstmt.setInt(1, bookID);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        authorIDs.add(rs.getInt("authorID"));
                    }
                }
            }
            // Ta bort boken (CASCADE tar bort kopplingar automatiskt)
            deleteBookRecord(connection, bookID);

            // Ta bort "orphaned" författare
            for (int authorID : authorIDs) {
                deleteAuthorIfOrphaned(connection, authorID);
            }

            // userID används för logging/audit (kan sparas i audit-tabell om man vill)
            System.out.println("[AUDIT] Book " + bookID + " deleted by user " + userID);

            connection.commit();

        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    throw new BooksDbException("Rollback misslyckades: " + rollbackEx.getMessage());
                }
            }
            throw new BooksDbException("Kunde inte ta bort bok: " + e.getMessage(), e);
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}