package se.kth.awad.bookdatabase.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DatabaseConnection {

    private static DatabaseConnection instance = null;
    private Connection connection;
    private String currentDatabase;

    // Database configuration
    private static final String SERVER_URL = "jdbc:mysql://localhost:3306/";
    private static final String USERNAME = "ahmed";
    private static final String PASSWORD = "Ahmed-1993";
    private static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";

    /**
     * Private constructor to prevent direct instantiation (Singleton pattern)
     */
    private DatabaseConnection() {
        this.connection = null;
        this.currentDatabase = null;
    }

    /**
     * Get the single instance of DatabaseConnection (Singleton pattern)
     * @return the DatabaseConnection instance
     */
    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Connect to a specific MySQL database
     * @param database the name of the database to connect to
     * @return true if connection was successful
     * @throws BooksDbException if connection fails
     */
    public boolean connect(String database) throws BooksDbException {
        try {
            // Close existing connection if any
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }

            // Load MySQL JDBC driver
            Class.forName(DRIVER_CLASS);

            // Build connection URL
            String fullUrl = SERVER_URL + database + "?UseClientEnc=UTF8";

            // Establish connection
            connection = DriverManager.getConnection(fullUrl, USERNAME, PASSWORD);
            currentDatabase = database;

            System.out.println("Successfully connected to database: " + database);
            return true;

        } catch (ClassNotFoundException e) {
            throw new BooksDbException("MySQL JDBC Driver not found", e);
        } catch (SQLException e) {
            throw new BooksDbException("Failed to connect to database: " + database, e);
        }
    }

    /**
     * Disconnect from the current database
     * @return true if disconnection was successful, false if not connected
     * @throws BooksDbException if disconnection fails
     */
    public boolean disconnect() throws BooksDbException {
        try {
            if (connection != null && !connection.isClosed()) {
                String dbName = currentDatabase;
                connection.close();
                connection = null;
                currentDatabase = null;
                System.out.println("Disconnected from database: " + dbName);
                return true;
            }
            return false;

        } catch (SQLException e) {
            throw new BooksDbException("Error disconnecting from database", e);
        }
    }

    /**
     * Get the active database connection
     * @return the active Connection object
     * @throws BooksDbException if not connected to any database
     */
    public Connection getConnection() throws BooksDbException {
        try {
            if (connection == null || connection.isClosed()) {
                throw new BooksDbException("Not connected to any database. Call connect() first.");
            }
            return connection;

        } catch (SQLException e) {
            throw new BooksDbException("Error checking connection status", e);
        }
    }

    /**
     * Check if currently connected to a database
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Get the name of the current database
     * @return database name, or null if not connected
     */
    public String getCurrentDatabase() {
        return currentDatabase;
    }
}
