package se.kth.awad.bookdatabase.view;

import java.sql.Date;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import se.kth.awad.bookdatabase.Controller.BookDBController;
import se.kth.awad.bookdatabase.model.*;


/**
 * The main pane for the view, extending VBox and including the menus. An
 * internal BorderPane holds the TableView for books and a search utility.
 *
 * @author anderslm@kth.se
 */
public class BooksPane extends VBox {

    private TableView<Book> booksTable;
    private ObservableList<Book> booksInTable; // the data backing the table view
    private ComboBox<SearchMode> searchModeBox;
    private TextField searchField;
    private Button searchButton;
    private MenuBar menuBar;

    /**
     * Constructor for dependency injection pattern.
     * Creates the view - controller should be set via setController() before use.
     */
    public BooksPane() {
        // UI will be initialized when controller is set
    }

    /**
     * Sets the controller and initializes the UI.
     * The controller is used during initialization to wire up event handlers,
     * but is not stored as a field - following proper MVC separation.
     *
     * @param controller the controller to handle user actions
     */
    public void setController(BookDBController controller) {
        this.init(controller);
    }

    /**
     * Display a new set of books, e.g. from a database select, in the
     * booksTable table view.
     *
     * @param books the books to display
     */
    public void displayBooks(List<Book> books) {
        booksInTable.clear();
        booksInTable.addAll(books);
    }

    /**
     * Notify user on input error or exceptions.
     * Called by Controller to display messages to the user.
     *
     * @param msg the message
     * @param type types: INFORMATION, WARNING et c.
     */
    public void showAlertAndWait(String msg, Alert.AlertType type) {
        Alert alert = new Alert(type, msg);
        alert.showAndWait();
    }

    private void init(BookDBController controller) {

        booksInTable = FXCollections.observableArrayList();
        BorderPane mainPane = new BorderPane();

        // init views and event handlers
        initBooksTable();
        initSearchView(controller);
        initMenus(controller);

        FlowPane bottomPane = new FlowPane();
        bottomPane.setHgap(10);
        bottomPane.setPadding(new Insets(10, 10, 10, 10));
        bottomPane.getChildren().addAll(searchModeBox, searchField, searchButton);

        mainPane.setCenter(booksTable);
        mainPane.setBottom(bottomPane);
        mainPane.setPadding(new Insets(10, 10, 10, 10));

        this.getChildren().addAll(menuBar, mainPane);
        VBox.setVgrow(mainPane, Priority.ALWAYS);
    }

    private void initBooksTable() {
        booksTable = new TableView<>();
        booksTable.setEditable(false); // don't allow user updates (yet)
        booksTable.setPlaceholder(new Label("No rows to display"));

        // define columns
        TableColumn<Book, String> titleCol = new TableColumn<>("Title");
        TableColumn<Book, String> isbnCol = new TableColumn<>("ISBN");
        TableColumn<Book, Date> publishedCol = new TableColumn<>("Published");
        TableColumn<Book, String> genreCol = new TableColumn<>("Genre");
        TableColumn<Book, Integer> gradeCol = new TableColumn<>("Grade");
        TableColumn<Book, Integer> nameCol = new TableColumn<>("Name");


        booksTable.getColumns().addAll(titleCol, isbnCol, publishedCol, genreCol, gradeCol,nameCol);
        // give title column some extra space
        titleCol.prefWidthProperty().bind(booksTable.widthProperty().multiply(0.5));
        nameCol.prefWidthProperty().bind(booksTable.widthProperty().multiply(0.3));


        // define how to fill data for each cell,
        // get values from Book properties
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        isbnCol.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        publishedCol.setCellValueFactory(new PropertyValueFactory<>("published"));
        genreCol.setCellValueFactory(new PropertyValueFactory<>("genre"));
        gradeCol.setCellValueFactory(new PropertyValueFactory<>("grade"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("authors"));

        booksTable.setItems(booksInTable);
    }

    private void initSearchView(BookDBController controller) {
        searchField = new TextField();
        searchField.setPromptText("Search for...");
        searchModeBox = new ComboBox<>();
        searchModeBox.getItems().addAll(SearchMode.values());
        searchModeBox.setValue(SearchMode.Title);
        searchButton = new Button("Search");

        // event handling (dispatch to controller)
        searchButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                SearchMode mode = searchModeBox.getValue();
                if(mode == SearchMode.Grade) {
                    int searchFor = Integer.parseInt(searchField.getText().trim());
                    controller.onSearchSelected(String.valueOf(searchFor), mode);
                    searchField.setPromptText("Search for...");

                } else {
                    String searchFor = searchField.getText();
                    controller.onSearchSelected(searchFor, mode);
                    searchField.setPromptText("Search for...");
                }

            }
        });
    }

    private void initMenus(BookDBController controller) {

        Menu fileMenu = new Menu("File");
        MenuItem exitItem = new MenuItem("Exit");
        MenuItem connectItem = new MenuItem("Connect to Db");
        MenuItem disconnectItem = new MenuItem("Disconnect");
        fileMenu.getItems().addAll(exitItem, connectItem, disconnectItem);

        connectItem.setOnAction(event -> {
            controller.connectToDb();
        });
        disconnectItem.setOnAction(event -> {
            controller.disconnect();
        });

        Menu manageMenu = new Menu("Manage");
        MenuItem addItem = new MenuItem("Add");

        addItem.setOnAction(event ->{
            controller.addBook();
        });

        MenuItem removeItem = new MenuItem("Remove");

        removeItem.setOnAction(event-> {
            controller.removeBook();
        });

        MenuItem updateItem = new MenuItem("Update");
        manageMenu.getItems().addAll(addItem, removeItem, updateItem);
        updateItem.setOnAction(event -> {
            controller.updateGrade();
        });

        menuBar = new MenuBar();
        menuBar.getMenus().addAll(fileMenu, manageMenu);
    }
}
