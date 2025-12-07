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
import javafx.application.Platform;

/*
 * Huvudvyn för applikationen.
 */
public class BooksPane extends VBox {

    private TableView<Book> booksTable;
    private ObservableList<Book> booksInTable;
    private ComboBox<SearchMode> searchModeBox;
    private TextField searchField;
    private Button searchButton;
    private MenuBar menuBar;

    public BooksPane() {}

    public void setController(BookDBController controller) {
        this.init(controller);
    }

    public void displayBooks(List<Book> books) {
        booksInTable.clear();
        booksInTable.addAll(books);
    }

    public void showAlertAndWait(String msg, Alert.AlertType type) {
        Alert alert = new Alert(type, msg);
        alert.showAndWait();
    }

    private void init(BookDBController controller) {

        booksInTable = FXCollections.observableArrayList();
        BorderPane mainPane = new BorderPane();

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
        booksTable.setEditable(false);
        booksTable.setPlaceholder(new Label("No rows to display"));

        TableColumn<Book, String> titleCol = new TableColumn<>("Title");
        TableColumn<Book, String> isbnCol = new TableColumn<>("ISBN");
        TableColumn<Book, Date> publishedCol = new TableColumn<>("Published");
        TableColumn<Book, String> genreCol = new TableColumn<>("Genre");
        TableColumn<Book, Integer> ratingCol = new TableColumn<>("Rating");
        TableColumn<Book, Integer> nameCol = new TableColumn<>("Name");


        booksTable.getColumns().addAll(titleCol, isbnCol, publishedCol, genreCol, ratingCol,nameCol);
        titleCol.prefWidthProperty().bind(booksTable.widthProperty().multiply(0.25));
        isbnCol.prefWidthProperty().bind(booksTable.widthProperty().multiply(0.20));
        publishedCol.prefWidthProperty().bind(booksTable.widthProperty().multiply(0.15));
        genreCol.prefWidthProperty().bind(booksTable.widthProperty().multiply(0.15));
        ratingCol.prefWidthProperty().bind(booksTable.widthProperty().multiply(0.10));
        nameCol.prefWidthProperty().bind(booksTable.widthProperty().multiply(0.15));
        isbnCol.setMinWidth(140);
        ratingCol.setMinWidth(70);

        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        isbnCol.setCellValueFactory(new PropertyValueFactory<>("ISBN"));
        publishedCol.setCellValueFactory(new PropertyValueFactory<>("publishedDate"));
        genreCol.setCellValueFactory(new PropertyValueFactory<>("genresAsString"));
        ratingCol.setCellValueFactory(new PropertyValueFactory<>("averageRating"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("authorsAsString"));

        // rating med en decimal
        ratingCol.setCellFactory(column -> new TableCell<Book, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.valueOf(item));
                }
            }
        });

        booksTable.setItems(booksInTable);

        //välja en rad i tabellen och få upp detaljerad
        TableView.TableViewSelectionModel<Book> selectionModel = booksTable.getSelectionModel();
        selectionModel.setSelectionMode(SelectionMode.SINGLE);
        selectionModel.selectedItemProperty().addListener((obs, oldBook, newBook) -> {
            if (newBook != null) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Authors");
                alert.setHeaderText("Authors for: " + newBook.getTitle());
                alert.setContentText(newBook.getAuthorsAsString());
                alert.showAndWait();
            }
        });

    }

    private void initSearchView(BookDBController controller) {
        searchField = new TextField();
        searchField.setPromptText("Search for...");
        searchModeBox = new ComboBox<>();
        searchModeBox.getItems().addAll(SearchMode.values());
        searchModeBox.setValue(SearchMode.Title);
        searchButton = new Button("Search");


        searchButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                SearchMode mode = searchModeBox.getValue();
                if(mode == SearchMode.Rating) {
                    double searchFor = Double.parseDouble(searchField.getText().trim());
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
        MenuItem connectItem = new MenuItem("Connect");
        MenuItem disconnectItem = new MenuItem("Disconnect");
        MenuItem exitItem = new MenuItem("Exit");
        fileMenu.getItems().addAll(connectItem, disconnectItem, exitItem);

        connectItem.setOnAction(event -> {
            controller.connectToDb();
        });
        disconnectItem.setOnAction(event -> {
            controller.disconnect();
        });

        exitItem.setOnAction(event -> {
            Platform.exit();
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

        Menu helpMenu  = new Menu("Help");
        MenuItem aboutItem = new MenuItem("About");
        aboutItem.setOnAction(event -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("About");
            alert.setHeaderText("Book database application v1.0");
            alert.setContentText("Made by Ibrahim Awad & Ahmed El Yasini");
            alert.showAndWait();
        });
        helpMenu.getItems().add(aboutItem);
        menuBar.getMenus().add(helpMenu);



    }
}
