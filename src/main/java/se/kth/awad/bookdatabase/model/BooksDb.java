package se.kth.awad.bookdatabase.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *  JDBC-implementation av BooksDbInterface.
 *  Denna klass kommunicerar direkt med LibraryDB i MySQL.
 */
public class BooksDb implements BooksDbInterface {

    private final DatabaseConnection dbConnection;

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

    // Sql frågor
    @Override
    public List<Book> retrieveBookTable() throws BooksDbException {
        List<Book> result = new ArrayList<>();
        String sql = "SELECT B.bookID, B.ISBN, B.title, B.publishedDate, " +
                "GROUP_CONCAT(DISTINCT A.name SEPARATOR ', ') AS authors, " +
                "GROUP_CONCAT(DISTINCT G.genreName SEPARATOR ', ') AS genres, " +
                "AVG(R.ratingValue) AS averageRating, " +
                "COUNT(R.ratingID) AS numRatings " +
                "FROM T_Book AS B " +
                "LEFT JOIN T_Book_Author AS BA ON B.bookID = BA.bookID " +
                "LEFT JOIN T_Author AS A ON BA.authorID = A.authorID " +
                "LEFT JOIN T_Book_Genre AS BG ON B.bookID = BG.bookID " +
                "LEFT JOIN T_Genre AS G ON BG.genreID = G.genreID " +
                "LEFT JOIN T_Rating AS R ON B.bookID = R.bookID " +
                "GROUP BY B.bookID, B.ISBN, B.title, B.publishedDate;";

        Statement stmt = null;
        ResultSet rs = null;
        try {
            // Kör frågan
            stmt = dbConnection.getConnection().createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                int bookID = rs.getInt("bookID");
                String ISBN = rs.getString("ISBN");
                String title = rs.getString("title");
                Date publishedDate = rs.getDate("publishedDate");

                //skapa bok
                Book book = new Book(bookID, ISBN, title, publishedDate);
                // sätt betyg
                int avgRating = rs.getInt("averageRating");
                if (!rs.wasNull()) {
                    book.setAverageRating(avgRating);
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

                result.add(book);
            }
            return result;
        } catch (SQLException e) {
            throw new BooksDbException("Error in fetching book table", e);
        } finally {
            // Stäng resurser
            try {
                if (rs != null) rs.close();
            } catch (SQLException e) {
                System.err.println("Error closing ResultSet: " + e.getMessage());
            }
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                System.err.println("Error closing Statement: " + e.getMessage());
            }
        }
    }

    @Override
    public List<Book> searchBooksByTitle(String searchTitle) throws BooksDbException {
        List<Book> result = new ArrayList<>();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try{
            String sql = "SELECT B.bookID, B.ISBN, B.title, B.publishedDate, " +
                    "GROUP_CONCAT(DISTINCT A.name SEPARATOR ', ') AS authors, " +
                    "GROUP_CONCAT(DISTINCT G.genreName SEPARATOR ', ') AS genres, " +
                    "AVG(R.ratingValue) AS averageRating, " +
                    "COUNT(R.ratingID) AS numRatings " +
                    "FROM T_Book AS B " +
                    "LEFT JOIN T_Book_Author AS BA ON B.bookID = BA.bookID " +
                    "LEFT JOIN T_Author AS A ON BA.authorID = A.authorID " +
                    "LEFT JOIN T_Book_Genre AS BG ON B.bookID = BG.bookID " +
                    "LEFT JOIN T_Genre AS G ON BG.genreID = G.genreID " +
                    "LEFT JOIN T_Rating AS R ON B.bookID = R.bookID " +
                    "WHERE LOWER(B.title) LIKE ? " +
                    "GROUP BY B.bookID, B.ISBN, B.title, B.publishedDate;";
            pstmt = dbConnection.getConnection().prepareStatement(sql);
            pstmt.setString(1, "%" + searchTitle.toLowerCase() + "%");
            rs = pstmt.executeQuery();
            while(rs.next()){
                result.add(buildBookFromResultSet(rs));
            }
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new BooksDbException("Error searching books by title", e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public List<Book> searchBooksByISBN(String searchISBN)
            throws BooksDbException {

        List<Book> result = new ArrayList<>();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String sql = "SELECT B.bookID, B.ISBN, B.title, B.publishedDate, " +
                    "GROUP_CONCAT(DISTINCT A.name SEPARATOR ', ') AS authors, " +
                    "GROUP_CONCAT(DISTINCT G.genreName SEPARATOR ', ') AS genres, " +
                    "AVG(R.ratingValue) AS averageRating, " +
                    "COUNT(R.ratingID) AS numRatings " +
                    "FROM T_Book AS B " +
                    "LEFT JOIN T_Book_Author AS BA ON B.bookID = BA.bookID " +
                    "LEFT JOIN T_Author AS A ON BA.authorID = A.authorID " +
                    "LEFT JOIN T_Book_Genre AS BG ON B.bookID = BG.bookID " +
                    "LEFT JOIN T_Genre AS G ON BG.genreID = G.genreID " +
                    "LEFT JOIN T_Rating AS R ON B.bookID = R.bookID " +
                    "WHERE B.ISBN = ? " +
                    "GROUP BY B.bookID, B.ISBN, B.title, B.publishedDate;";
            pstmt = dbConnection.getConnection().prepareStatement(sql);
            pstmt.setString(1, searchISBN);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                result.add(buildBookFromResultSet(rs));
            }
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new BooksDbException("Error searching books by ISBN", e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public List<Book> searchBooksByAuthor(String searchName)
            throws BooksDbException {

        List<Book> result = new ArrayList<>();
        searchName = searchName.toLowerCase();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try{
            String sql = "SELECT B.bookID, B.ISBN, B.title, B.publishedDate, " +
                    "GROUP_CONCAT(DISTINCT A.name SEPARATOR ', ') AS authors, " +
                    "GROUP_CONCAT(DISTINCT G.genreName SEPARATOR ', ') AS genres, " +
                    "AVG(R.ratingValue) AS averageRating, " +
                    "COUNT(R.ratingID) AS numRatings " +
                    "FROM T_Author AS A " +
                    "JOIN T_Book_Author AS BA ON BA.authorID = A.authorID " +
                    "JOIN T_Book AS B ON B.bookID = BA.bookID " +
                    "LEFT JOIN T_Book_Genre AS BG ON B.bookID = BG.bookID " +
                    "LEFT JOIN T_Genre AS G ON BG.genreID = G.genreID " +
                    "LEFT JOIN T_Rating AS R ON B.bookID = R.bookID " +
                    "WHERE LOWER(A.name) LIKE ? " +
                    "GROUP BY B.bookID, B.ISBN, B.title, B.publishedDate;";
            pstmt = dbConnection.getConnection().prepareStatement(sql);
            pstmt.setString(1, "%" + searchName.toLowerCase() + "%");
            rs = pstmt.executeQuery();
            while(rs.next()){
                result.add(buildBookFromResultSet(rs));
            }
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new BooksDbException("Error searching books by author", e);
        } finally {
            // Close resources in a finally block
            try {
                if (rs != null) {
                    rs.close();
                }
                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (SQLException e) {
                e.printStackTrace(); // Handle or log the exception
            }
        }
    }
    @Override
    public List<Book> searchBooksByGenre(String searchGenre)
            throws BooksDbException {

        List<Book> result = new ArrayList<>();
        searchGenre = searchGenre.toLowerCase();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try{
            String sql = "SELECT B.bookID, B.ISBN, B.title, B.publishedDate, " +
                    "GROUP_CONCAT(DISTINCT A.name SEPARATOR ', ') AS authors, " +
                    "GROUP_CONCAT(DISTINCT G.genreName SEPARATOR ', ') AS genres, " +
                    "AVG(R.ratingValue) AS averageRating, " +
                    "COUNT(R.ratingID) AS numRatings " +
                    "FROM T_Genre AS G " +
                    "JOIN T_Book_Genre AS BG ON BG.genreID = G.genreID " +
                    "JOIN T_Book AS B ON B.bookID = BG.bookID " +
                    "LEFT JOIN T_Book_Author AS BA ON B.bookID = BA.bookID " +
                    "LEFT JOIN T_Author AS A ON BA.authorID = A.authorID " +
                    "LEFT JOIN T_Rating AS R ON B.bookID = R.bookID " +
                    "WHERE LOWER(G.genreName) LIKE ? " +
                    "GROUP BY B.bookID, B.ISBN, B.title, B.publishedDate;";
            pstmt = dbConnection.getConnection().prepareStatement(sql);
            pstmt.setString(1, "%" + searchGenre.toLowerCase() + "%");
            rs = pstmt.executeQuery();
            while(rs.next()){
                result.add(buildBookFromResultSet(rs));
            }
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new BooksDbException("Error searching books by genre", e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void insertBook(Book book) throws BooksDbException{
        PreparedStatement pstmtBook = null;
        PreparedStatement pstmtAuthor = null;
        PreparedStatement pstmtBookAuthor = null;
        PreparedStatement pstmtBookGenre = null;
        PreparedStatement pstmtGenreCheck = null;
        PreparedStatement pstmtGenre = null;
        Connection con = null;
        try{
            con = dbConnection.getConnection();
            con.setAutoCommit(false); //transaktion

            String bookSql = "INSERT INTO T_Book (ISBN, title, publishedDate) VALUES (?, ?, ?)";
            pstmtBook = con.prepareStatement(bookSql, Statement.RETURN_GENERATED_KEYS);
            pstmtBook.setString(1, book.getISBN());
            pstmtBook.setString(2, book.getTitle());
            pstmtBook.setDate(3, book.getPublishedDate());

            int nBook = pstmtBook.executeUpdate();
            if (nBook == 0){
                throw new SQLException("Inserting book failed, no rows affected.");
            }

            ResultSet bookKeys = pstmtBook.getGeneratedKeys();
            bookKeys.next();
            int bookID = bookKeys.getInt(1);
            book.setBookID(bookID);

            if (book.getAuthors() != null && !book.getAuthors().isEmpty()) {
                String authorCheckSql = "SELECT authorID FROM T_Author WHERE name = ?";
                String authorSql = "INSERT INTO T_Author (name, birthDate) VALUES (?, ?)";
                String bookAuthorSql = "INSERT INTO T_Book_Author (bookID, authorID) VALUES (?, ?)";
                
                pstmtAuthor = con.prepareStatement(authorCheckSql);
                
                for (Author author : book.getAuthors()) {
                    pstmtAuthor.setString(1, author.getName());
                    ResultSet authorCheck = pstmtAuthor.executeQuery();
                    
                    int authorID;
                    if (authorCheck.next()) {
                        authorID = authorCheck.getInt("authorID");
                    } else {
                        PreparedStatement pstmtNewAuthor = con.prepareStatement(authorSql, Statement.RETURN_GENERATED_KEYS);
                        pstmtNewAuthor.setString(1, author.getName());
                        pstmtNewAuthor.setDate(2, author.getBirthDate());
                        pstmtNewAuthor.executeUpdate();
                        
                        ResultSet authorKeys = pstmtNewAuthor.getGeneratedKeys();
                        authorKeys.next();
                        authorID = authorKeys.getInt(1);
                        author.setAuthorID(authorID);
                        pstmtNewAuthor.close();
                    }

                    pstmtBookAuthor = con.prepareStatement(bookAuthorSql);
                    pstmtBookAuthor.setInt(1, bookID);
                    pstmtBookAuthor.setInt(2, authorID);
                    pstmtBookAuthor.executeUpdate();
                    pstmtBookAuthor.close();
                }
            }

            if (book.getGenres() != null && !book.getGenres().isEmpty()) {
                String genreCheckSql = "SELECT genreID FROM T_Genre WHERE genreName = ?";
                String genreSql = "INSERT INTO T_Genre (genreName) VALUES (?)";
                String bookGenreSql = "INSERT INTO T_Book_Genre (bookID, genreID) VALUES (?, ?)";
                
                pstmtGenreCheck = con.prepareStatement(genreCheckSql);
                
                for (Genre genre : book.getGenres()) {

                    pstmtGenreCheck.setString(1, genre.getGenreName());
                    ResultSet genreCheck = pstmtGenreCheck.executeQuery();
                    
                    int genreID;
                    if (genreCheck.next()) {

                        genreID = genreCheck.getInt("genreID");
                    } else {

                        pstmtGenre = con.prepareStatement(genreSql, Statement.RETURN_GENERATED_KEYS);
                        pstmtGenre.setString(1, genre.getGenreName());
                        pstmtGenre.executeUpdate();
                        
                        ResultSet genreKeys = pstmtGenre.getGeneratedKeys();
                        genreKeys.next();
                        genreID = genreKeys.getInt(1);
                        genre.setGenreID(genreID);
                        pstmtGenre.close();
                    }

                    pstmtBookGenre = con.prepareStatement(bookGenreSql);
                    pstmtBookGenre.setInt(1, bookID);
                    pstmtBookGenre.setInt(2, genreID);
                    pstmtBookGenre.executeUpdate();
                    pstmtBookGenre.close();
                }
            }

            con.commit();

        } catch (SQLException e) {
            try {
                if (con != null) {
                    con.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            throw new BooksDbException("Error inserting book: " + e.getMessage(), e);
        } finally {
            try {
                if (pstmtBook != null) pstmtBook.close();
                if (pstmtAuthor != null) pstmtAuthor.close();
                if (pstmtBookAuthor != null) pstmtBookAuthor.close();
                if (pstmtBookGenre != null) pstmtBookGenre.close();
                if (pstmtGenreCheck != null) pstmtGenreCheck.close();
                if (pstmtGenre != null) pstmtGenre.close();
                if (con != null) {
                    con.setAutoCommit(true);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void deleteBook(Book book) throws BooksDbException {
        //cascade ta bort resten automatik
        String bookSql = "DELETE FROM T_Book WHERE bookID = ?";
        String authorCheckSql = "SELECT COUNT(*) FROM T_Book_Author WHERE authorID = ?";
        String authorSql = "DELETE FROM T_Author WHERE authorID = ?";

        Connection con = null;
        PreparedStatement pstmtBook = null;
        PreparedStatement pstmtAuthorCheck = null;
        PreparedStatement pstmtAuthor = null;
        
        try {
            con = dbConnection.getConnection();
            con.setAutoCommit(false);


            pstmtBook = con.prepareStatement(bookSql);
            pstmtBook.setInt(1, book.getBookID());
            int deletedRows = pstmtBook.executeUpdate();
            
            if (deletedRows == 0) {
                throw new SQLException("No book found with bookID: " + book.getBookID());
            }

            if (book.getAuthors() != null) {
                pstmtAuthorCheck = con.prepareStatement(authorCheckSql);
                pstmtAuthor = con.prepareStatement(authorSql);
                
                for (Author author : book.getAuthors()) {
                    if (author.getAuthorID() > 0) {
                        pstmtAuthorCheck.setInt(1, author.getAuthorID());
                try (ResultSet rs = pstmtAuthorCheck.executeQuery()) {
                            if (rs.next() && rs.getInt(1) == 0) {
                                pstmtAuthor.setInt(1, author.getAuthorID());
                        pstmtAuthor.executeUpdate();
                            }
                        }
                    }
                }
            }

            con.commit();

        } catch (SQLException e) {
            try {
                if (con != null) con.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            e.printStackTrace();
            throw new BooksDbException("Failed to delete book: " + e.getMessage(), e);
        } finally {
            try {
                if (pstmtBook != null) pstmtBook.close();
                if (pstmtAuthorCheck != null) pstmtAuthorCheck.close();
                if (pstmtAuthor != null) pstmtAuthor.close();
                if (con != null) con.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void updateBook(Book book) throws BooksDbException {
        PreparedStatement pstmtUpdate = null;
        Connection con = null;
        try{
            con = dbConnection.getConnection();
            con.setAutoCommit(false);

            String sql = "UPDATE T_Book SET ISBN = ?, title = ?, publishedDate = ? WHERE bookID = ?";
            pstmtUpdate = con.prepareStatement(sql);
            pstmtUpdate.setString(1, book.getISBN());
            pstmtUpdate.setString(2, book.getTitle());
            pstmtUpdate.setDate(3, book.getPublishedDate());
            pstmtUpdate.setInt(4, book.getBookID());

            int rowsAffected = pstmtUpdate.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No book found with bookID: " + book.getBookID());
            }

            con.commit();
        } catch (SQLException e) {
            try {
                if (con != null) {
                    con.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            throw new BooksDbException("Error updating book: " + e.getMessage(), e);
        } finally {
            try {
                if (pstmtUpdate != null) pstmtUpdate.close();
                if (con != null) {
                    con.setAutoCommit(true);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
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

        int avgRating = rs.getInt("averageRating");
        if (!rs.wasNull()) {
            book.setAverageRating(avgRating);
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


    @Override
    public List<Book> searchBooksByRating(int  minRating) throws BooksDbException {
        List<Book> result = new ArrayList<>();
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            // SQL-fråga som filtrerar böcker baserat på genomsnittligt betyg
            String sql = "SELECT B.bookID, B.ISBN, B.title, B.publishedDate, " +
                    "GROUP_CONCAT(DISTINCT A.name SEPARATOR ', ') AS authors, " +
                    "GROUP_CONCAT(DISTINCT G.genreName SEPARATOR ', ') AS genres, " +
                    "AVG(R.ratingValue) AS averageRating, " +
                    "COUNT(R.ratingID) AS numRatings " +
                    "FROM T_Book AS B " +
                    "LEFT JOIN T_Book_Author AS BA ON B.bookID = BA.bookID " +
                    "LEFT JOIN T_Author AS A ON BA.authorID = A.authorID " +
                    "LEFT JOIN T_Book_Genre AS BG ON B.bookID = BG.bookID " +
                    "LEFT JOIN T_Genre AS G ON BG.genreID = G.genreID " +
                    "LEFT JOIN T_Rating AS R ON B.bookID = R.bookID " +
                    "GROUP BY B.bookID, B.ISBN, B.title, B.publishedDate " +
                    "HAVING AVG(R.ratingValue) = ?;";

            pstmt = dbConnection.getConnection().prepareStatement(sql);
            pstmt.setInt(1, minRating);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                result.add(buildBookFromResultSet(rs));
            }

            return result;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new BooksDbException("Error searching books by rating", e);
        } finally {
            // Stäng resurser i finally-blocket
            try {
                if (rs != null) {
                    rs.close();
                }
                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public boolean addRating(int bookID, int ratingValue) throws BooksDbException {

        if (ratingValue < 1 || ratingValue > 10) {
            throw new BooksDbException("Rating value must be between 1 and 10");
        }

        PreparedStatement pstmt = null;
        Connection con = null;
        try {
            con = dbConnection.getConnection();
            con.setAutoCommit(false);
            String sql = "INSERT INTO T_Rating (bookID, ratingValue, ratingDate) VALUES (?, ?, CURDATE())";
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, bookID);
            pstmt.setInt(2, ratingValue);

            int rowsAffected = pstmt.executeUpdate();
            if(rowsAffected > 0){
                con.commit();
                System.out.println("[BooksDbImpl] ✓ Betyg tillagt");
                return true;
            }else {
                con.rollback();
                System.out.println("[BooksDbImpl] ✗ Inget betyg tillagt");
                return false;
            }
        } catch (SQLException e) {
            try {
                con.rollback();
                System.out.println("[BooksDbImpl] ✗ Rollback - Error while adding rating");
            } catch (SQLException rollbackEx) {
                throw new BooksDbException("Failed to rollback transaction", rollbackEx);
            }
            throw new BooksDbException("Failed to add rating for book ID " + bookID, e);
        } finally {
            try {
                con.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("[BooksDbImpl] Warning: Failed to reset auto-commit");
            }
        }
    }

    @Override
    public List<Author> getAuthorsForBook(int bookID) throws BooksDbException {
        List<Author> result = new ArrayList<>();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            String sql = "SELECT A.authorID, A.name, A.birthDate " +
                    "FROM T_Author AS A " +
                    "JOIN T_Book_Author AS BA ON A.authorID = BA.authorID " +
                    "WHERE BA.bookID = ?";
            pstmt = dbConnection.getConnection().prepareStatement(sql);
            pstmt.setInt(1, bookID);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                int authorID = rs.getInt("authorID");
                String name = rs.getString("name");
                Date birthDate = rs.getDate("birthDate");
                result.add(new Author(authorID, name, birthDate));
            }
            
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new BooksDbException("Error getting authors for book: " + e.getMessage(), e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public List<Author> getAllAuthors() throws BooksDbException {
        List<Author> result = new ArrayList<>();
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            String sql = "SELECT authorID, name, birthDate FROM T_Author ORDER BY name";
            stmt = dbConnection.getConnection().createStatement();
            rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                int authorID = rs.getInt("authorID");
                String name = rs.getString("name");
                Date birthDate = rs.getDate("birthDate");
                result.add(new Author(authorID, name, birthDate));
            }
            
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new BooksDbException("Error getting all authors: " + e.getMessage(), e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public List<Genre> getAllGenres() throws BooksDbException {
        List<Genre> result = new ArrayList<>();
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            String sql = "SELECT genreID, genreName FROM T_Genre ORDER BY genreName";
            stmt = dbConnection.getConnection().createStatement();
            rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                int genreID = rs.getInt("genreID");
                String genreName = rs.getString("genreName");
                result.add(new Genre(genreID, genreName));
            }
            
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new BooksDbException("Error getting all genres: " + e.getMessage(), e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public List<Genre> getGenresForBook(int bookID) throws BooksDbException {
        List<Genre> result = new ArrayList<>();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            String sql = "SELECT G.genreID, G.genreName " +
                    "FROM T_Genre AS G " +
                    "JOIN T_Book_Genre AS BG ON G.genreID = BG.genreID " +
                    "WHERE BG.bookID = ?";
            pstmt = dbConnection.getConnection().prepareStatement(sql);
            pstmt.setInt(1, bookID);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                int genreID = rs.getInt("genreID");
                String genreName = rs.getString("genreName");
                result.add(new Genre(genreID, genreName));
            }
            
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new BooksDbException("Error getting genres for book: " + e.getMessage(), e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}