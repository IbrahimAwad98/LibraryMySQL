package se.kth.awad.bookdatabase.model;


import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A mock implementation of the BooksDBInterface interface to demonstrate how to
 * use it together with the user interface.
 * <p>
 * Your implementation must access a real database.
 *
 * @author anderslm@kth.se
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
    public List<Book> retrieveBookTable() throws BooksDbException {
        List<Book> result = new ArrayList<>();
        String sql = "SELECT B.*, GROUP_CONCAT(A.authorName SEPARATOR ', ') AS authors " +
                "FROM Book AS B " +
                "JOIN BookAuthor AS BA ON B.bookId = BA.bookId " +
                "JOIN Author AS A ON BA.authorId = A.authorId " +
                "GROUP BY B.bookId;";

        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = dbConnection.getConnection().createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                // Create a Book object using retrieved data
                Book book = new Book(rs.getString("isbn"), rs.getString("title"),
                        rs.getDate("published"), rs.getString("genre"),
                        rs.getInt("grade"));

                // Get the concatenated authors and set them to the book
                String authors = rs.getString("authors");
                if (authors != null && !authors.isEmpty()) {
                    for (String authorName : authors.split(", ")) {
                        book.setAuthor(new Author(authorName)); // Assuming addAuthor method exists
                    }
                }

                // Add the book to the result list
                result.add(book);
            }
            return result;
        } catch (SQLException e) {
            throw new BooksDbException("Error in fetching book table", e);
        } finally {
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
    public boolean disconnect() throws BooksDbException {
        return dbConnection.disconnect();
    }

    @Override
    public List<Book> searchBooksByTitle(String searchTitle)
            throws BooksDbException {

        List<Book> result = new ArrayList<>();
        searchTitle = searchTitle.toLowerCase();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try{
            String sql = "SELECT B.*, GROUP_CONCAT(A.authorName SEPARATOR ', ') AS authors " +
                    "FROM Book AS B " +
                    "JOIN BookAuthor BA ON BA.bookId = B.bookId " +
                    "JOIN Author A ON A.authorId = BA.authorId " +
                    "WHERE B.title = ? " +
                    "GROUP BY B.bookId;";
            pstmt = dbConnection.getConnection().prepareStatement(sql);
            pstmt.setString(1, searchTitle);
            rs = pstmt.executeQuery();
            while(rs.next()){
                Book book = new Book(rs.getString("isbn"),
                        rs.getString("title"),rs.getDate("published"),
                        rs.getString("genre"),rs.getInt("grade"));


                String[] authorNames = rs.getString("authors").split(", ");
                for (String authorName : authorNames) {
                    Author author = new Author(authorName);
                    book.setAuthor(author);
                }
                result.add(book);
            }
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new BooksDbException("Error searching books by title", e);
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
    public List<Book> searchBooksByISBN(String searchIsbn)
            throws BooksDbException {

        List<Book> result = new ArrayList<>();
        searchIsbn = searchIsbn.toLowerCase();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String sql = "SELECT B.*, GROUP_CONCAT(A.authorName SEPARATOR ', ') AS authors " +
                    "FROM Book AS B " +
                    "JOIN BookAuthor BA ON BA.bookId = B.bookId " +
                    "JOIN Author A ON A.authorId = BA.authorId " +
                    "WHERE B.isbn = ? " +
                    "GROUP BY B.bookId;";
            pstmt = dbConnection.getConnection().prepareStatement(sql);
            pstmt.setString(1, searchIsbn);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Book book = new Book(rs.getString("isbn"),
                        rs.getString("title"), rs.getDate("published"),
                        rs.getString("genre"), rs.getInt("grade"));
                String[] authorNames = rs.getString("authors").split(",");
                for (String authorName : authorNames) {
                    Author author = new Author(authorName);
                    book.setAuthor(author);
                }
                result.add(book);
            }

            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new BooksDbException("Error searching books by title", e);
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
    public List<Book> searchBooksByAuthor(String searchName)
            throws BooksDbException {

        List<Book> result = new ArrayList<>();
        searchName = searchName.toLowerCase();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try{
            String sql =
                    "SELECT B.*, GROUP_CONCAT(A.authorName SEPARATOR ', ') AS authors " +
                            "FROM Author AS A " +
                            "JOIN BookAuthor BA ON BA.authorId = A.authorId " +
                            "JOIN Book B ON B.bookId = BA.bookId " +
                            "WHERE A.authorName = ? " +
                            "GROUP BY B.bookId;";
            pstmt = dbConnection.getConnection().prepareStatement(sql);
            pstmt.setString(1, searchName);
            rs = pstmt.executeQuery();
            while(rs.next()){
                Book book = new Book(rs.getString("isbn"),
                        rs.getString("title"),rs.getDate("published"),
                        rs.getString("genre"),rs.getInt("grade"));


                String[] authorNames = rs.getString("authors").split(", ");
                for (String authorName : authorNames) {
                    Author author = new Author(authorName);
                    book.setAuthor(author);
                }
                result.add(book);
            }
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new BooksDbException("Error searching books by title", e);
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
    public List<Book> searchBooksByGrade(String searchGrade)
            throws BooksDbException {

        List<Book> result = new ArrayList<>();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try{
            String sql = "SELECT B.*, GROUP_CONCAT(A.authorName SEPARATOR ', ') AS authors " +
                    "FROM Book AS B " +
                    "JOIN BookAuthor BA ON BA.bookId = B.bookId " +
                    "JOIN Author A ON A.authorId = BA.authorId " +
                    "WHERE B.grade = ? " +
                    "GROUP BY B.bookId;";
            pstmt = dbConnection.getConnection().prepareStatement(sql);
            pstmt.setString(1, searchGrade);
            rs = pstmt.executeQuery();
            while(rs.next()){
                Book book = new Book(rs.getString("isbn"),
                        rs.getString("title"),rs.getDate("published"),
                        rs.getString("genre"),rs.getInt("grade"));


                String[] authorNames = rs.getString("authors").split(", ");
                for (String authorName : authorNames) {
                    Author author = new Author(authorName);
                    book.setAuthor(author);
                }
                result.add(book);
            }
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new BooksDbException("Error searching books by title", e);
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
            String sql = "SELECT B.*, GROUP_CONCAT(A.authorName SEPARATOR ', ') AS authors " +
                    "FROM Book AS B " +
                    "JOIN BookAuthor BA ON BA.bookId = B.bookId " +
                    "JOIN Author A ON A.authorId = BA.authorId " +
                    "WHERE B.genre = ? " +
                    "GROUP BY B.bookId;";
            pstmt = dbConnection.getConnection().prepareStatement(sql);
            pstmt.setString(1, searchGenre);
            rs = pstmt.executeQuery();
            while(rs.next()){
                Book book = new Book(rs.getString("isbn"),
                        rs.getString("title"),rs.getDate("published"),
                        rs.getString("genre"),rs.getInt("grade"));


                String[] authorNames = rs.getString("authors").split(", ");
                for (String authorName : authorNames) {
                    Author author = new Author(authorName);
                    book.setAuthor(author);
                }
                result.add(book);
            }
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new BooksDbException("Error searching books by title", e);
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
    public void insertBook(Book book) throws BooksDbException{
        PreparedStatement pstmtBook = null;
        PreparedStatement pstmtAuthor = null;
        PreparedStatement pstmtBookAuthor = null;
        Connection con = null;
        try{
            con = dbConnection.getConnection();
            con.setAutoCommit(false);

            String bookSql = "INSERT INTO Book (isbn, title, published, genre, grade) VALUES (?, ?, ?, ?, ?)";
            String authorSql = "INSERT INTO Author (authorName, birthdate, biography) VALUES (?, ?, ?)";
            String bookAuthorSql = "INSERT INTO BookAuthor (bookId, authorId) VALUES (?, ?)";
            pstmtBook = con.prepareStatement(bookSql,Statement.RETURN_GENERATED_KEYS);
            pstmtAuthor = con.prepareStatement(authorSql,Statement.RETURN_GENERATED_KEYS);
            pstmtBookAuthor = con.prepareStatement(bookAuthorSql);

            pstmtBook.setString(1,book.getIsbn());
            pstmtBook.setString(2,book.getTitle());
            pstmtBook.setDate(3,book.getPublished());
            pstmtBook.setString(4,book.getStringGenre(book.getGenre()));
            pstmtBook.setInt(5,book.getGrade());

            int nBook = pstmtBook.executeUpdate();
            if (nBook == 0){
                throw new SQLException("Inserting book failed, no rows affected.");
            }

            Author author = book.getAuthor(0);
            pstmtAuthor.setString(1,author.getName());
            pstmtAuthor.setDate(2,author.getBirthdate());
            pstmtAuthor.setString(3,author.getBiography());

            int nAuthor = pstmtAuthor.executeUpdate();
            if (nAuthor == 0){
                throw new SQLException("Inserting author failed, no rows affected.");
            }

            ResultSet resultBG = pstmtBook.getGeneratedKeys();
            resultBG.next();
            int bookId = resultBG.getInt(1);
            pstmtBookAuthor.setInt(1,bookId);
            ResultSet resultAG = pstmtAuthor.getGeneratedKeys();
            resultAG.next();
            int authorId = resultAG.getInt(1);

            pstmtBookAuthor.setInt(1,bookId);
            pstmtBookAuthor.setInt(2,authorId);
            pstmtBookAuthor.executeUpdate();
            con.commit();

        } catch (SQLException e) {
            try {
                if (con != null) {
                    con.rollback(); // Roll back the transaction if an exception occurs
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            e.printStackTrace();
            throw new BooksDbException("SQL is wrong", e);
        } finally {
            // Close resources in the finally block
            try {
                if (pstmtBook != null) {
                    pstmtBook.close();
                }
                if (pstmtAuthor != null) {
                    pstmtAuthor.close();
                }
                if (pstmtBookAuthor != null) {
                    pstmtBookAuthor.close();
                }
                if (con != null) {
                    con.setAutoCommit(true); // Reset auto-commit to true
                    //con.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    public void deleteBook(Book book) throws BooksDbException {
        String bookAuthorSql = "DELETE FROM BookAuthor WHERE bookId = ?";
        String bookSql = "DELETE FROM Book WHERE title = ?";
        String authorCheckSql = "SELECT COUNT(*) FROM BookAuthor WHERE authorId = ?";
        String authorSql = "DELETE FROM Author WHERE authorId = ?";

        Connection con = null;
        try {
            con = dbConnection.getConnection();
            PreparedStatement pstmtBookAuthor = con.prepareStatement(bookAuthorSql);
            PreparedStatement pstmtBook = con.prepareStatement(bookSql);
            PreparedStatement pstmtAuthorCheck = con.prepareStatement(authorCheckSql);
            PreparedStatement pstmtAuthor = con.prepareStatement(authorSql);

            // Disable auto-commit for transaction
            con.setAutoCommit(false);

            // Step 1: Delete from BookAuthor
            pstmtBookAuthor.setInt(1, book.getBookId());
            pstmtBookAuthor.executeUpdate();

            // Step 2: Check and delete authors (if they are not associated with other books)
            for (Author author : book.getAuthores()) { // Assuming book.getAuthors() returns a List of Author objects
                pstmtAuthorCheck.setInt(1, author.getAuthorId());
                try (ResultSet rs = pstmtAuthorCheck.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) { // No other books reference this author
                        pstmtAuthor.setInt(1, author.getAuthorId());
                        pstmtAuthor.executeUpdate();
                    }
                }
            }

            // Step 3: Delete the book itself
            pstmtBook.setString(1, book.getTitle());
            pstmtBook.executeUpdate();

            // Commit the transaction
            con.commit();

            // Close PreparedStatements
            pstmtBookAuthor.close();
            pstmtBook.close();
            pstmtAuthorCheck.close();
            pstmtAuthor.close();

        } catch (SQLException e) {
            // Roll back the transaction in case of an error
            try {
                if (con != null) con.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            e.printStackTrace();
            throw new BooksDbException("Failed to delete book by title", e);
        } finally {
            try {
                if (con != null) con.setAutoCommit(true); // Reset auto-commit
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    public void updateBook(Book book) throws BooksDbException{
        PreparedStatement pstmtGradeUpdate = null;
        Connection con = null;
        try{
            con = dbConnection.getConnection();
            con.setAutoCommit(false);

            String sql = "UPDATE Book SET grade = ? WHERE title = ? ";

            pstmtGradeUpdate = con.prepareStatement(sql);

            pstmtGradeUpdate.setInt(1,book.getGrade());
            pstmtGradeUpdate.setString(2,book.getTitle());
            pstmtGradeUpdate.executeUpdate();
            //System.out.println("SQL Statement: " + sql);
            //System.out.println("Grade: " + book.getGrade());
            //System.out.println("Bookid " + book.getBookId());

            con.commit();
        }catch (SQLException e) {
            try {
                if (con != null) {
                    con.rollback(); // Roll back the transaction if an exception occurs
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            e.printStackTrace();
            throw new BooksDbException("SQL is wrong", e);
        }finally {

            try {
                if (pstmtGradeUpdate != null) pstmtGradeUpdate.close();
                if (con != null) {
                    con.setAutoCommit(true); // Reset auto-commit to true
                    //con.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
}
