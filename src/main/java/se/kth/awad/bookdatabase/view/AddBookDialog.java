package se.kth.awad.bookdatabase.view;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import se.kth.awad.bookdatabase.model.*;
import java.sql.Date;

public class AddBookDialog extends Dialog<Book> {

    private final TextField titleField = new TextField();
    private final TextField isbnField = new TextField();
    private final DatePicker publishedField = new DatePicker();
    private final TextField authorField = new TextField();
    private final DatePicker birthDayField = new DatePicker();
    private final TextField genreField = new TextField();

    public AddBookDialog() {
        buildAddBookDialog();
    }

    private void buildAddBookDialog() {
        this.setTitle("Add a new book");
        this.setResizable(false);

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setPadding(new Insets(10, 10, 10, 10));
        
        grid.add(new Label("Title "), 1, 1);
        grid.add(titleField, 2, 1);
        grid.add(new Label("ISBN "), 1, 2);
        grid.add(isbnField, 2, 2);
        grid.add(new Label("Published Date "), 1, 3);
        grid.add(publishedField, 2, 3);
        grid.add(new Label("Genre(s) "), 1, 4);
        grid.add(genreField, 2, 4);
        genreField.setPromptText("Comma-separated (e.g., Fantasy, Young Adult)");
        grid.add(new Label("Author(s) "), 1, 5);
        grid.add(authorField, 2, 5);
        authorField.setPromptText("Comma-separated (e.g., J.K. Rowling, Stephen King)");
        grid.add(new Label("Author Birth Date "), 1, 6);
        grid.add(birthDayField, 2, 6);
        birthDayField.setPromptText("Optional (only for single author)");

        this.getDialogPane().setContent(grid);

        ButtonType buttonTypeOk = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
        this.getDialogPane().getButtonTypes().add(buttonTypeOk);
        ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        this.getDialogPane().getButtonTypes().add(buttonTypeCancel);

        this.setResultConverter(new Callback<ButtonType, Book>() {
            @Override
            public Book call(ButtonType b) {
                Book result = null;
                if (b == buttonTypeOk) {
                    if (isValidData()) {
                        Date publishedDate = publishedField.getValue() != null 
                            ? Date.valueOf(publishedField.getValue()) 
                            : null;
                        result = new Book(
                                isbnField.getText().trim(),
                                titleField.getText().trim(),
                                publishedDate
                        );
                        // Handle multiple authors (comma-separated)
                        String authorsText = authorField.getText().trim();
                        if (!authorsText.isEmpty()) {
                            String[] authorNames = authorsText.split(",");
                            Date authorBirthDate = null;
                            

                            if (authorNames.length == 1 && birthDayField.getValue() != null) {
                                authorBirthDate = Date.valueOf(birthDayField.getValue());
                            }
                            
                            for (String authorName : authorNames) {
                                authorName = authorName.trim();
                                if (!authorName.isEmpty()) {
                                    Author author = authorBirthDate != null 
                                        ? new Author(authorName, authorBirthDate)
                                        : new Author(authorName);
                                    result.addAuthor(author);
                                }
                            }
                        }

                        String genresText = genreField.getText().trim();
                        if (!genresText.isEmpty()) {
                            String[] genreNames = genresText.split(",");
                            for (String genreName : genreNames) {
                                genreName = genreName.trim();
                                if (!genreName.isEmpty()) {
                                    result.addGenre(new Genre(genreName));
                                }
                            }
                        }
                    }
                }
                clearFormData();
                return result;
            }
        });

        Button okButton = (Button) this.getDialogPane().lookupButton(buttonTypeOk);
        okButton.addEventFilter(ActionEvent.ACTION, new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!isValidData()) {
                    event.consume();
                    showErrorAlert("Form error", "Please fill in all required fields correctly.");
                }
            }
        });
    }

    private boolean isValidData() {
        if (titleField.getText().trim().isEmpty()) {
            return false;
        }
        if (isbnField.getText().trim().isEmpty()) {
            return false;
        }
        if (publishedField.getValue() == null) {
            return false;
        }
        // Check if at least one author is provided
        String authorsText = authorField.getText().trim();
        if (authorsText.isEmpty()) {
            return false;
        }
        // Validate that there's at least one non-empty author name
        String[] authorNames = authorsText.split(",");
        boolean hasValidAuthor = false;
        for (String name : authorNames) {
            if (!name.trim().isEmpty()) {
                hasValidAuthor = true;
                break;
            }
        }
        if (!hasValidAuthor) {
            return false;
        }

        String isbn = isbnField.getText().trim();
        if (isbn.isEmpty() || isbn.length() < 10) {
            return false;
        }
        
        return true;
    }

    private void clearFormData() {
        titleField.setText("");
        isbnField.setText("");
        publishedField.setValue(null);
        genreField.setText("");
        authorField.setText("");
        birthDayField.setValue(null);
    }

    private final Alert errorAlert = new Alert(Alert.AlertType.ERROR);

    private void showErrorAlert(String title, String info) {
        errorAlert.setTitle(title);
        errorAlert.setHeaderText(null);
        errorAlert.setContentText(info);
        errorAlert.show();
    }
}
