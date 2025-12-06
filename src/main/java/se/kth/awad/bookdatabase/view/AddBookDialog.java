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
        grid.add(new Label("Author Name "), 1, 5);
        grid.add(authorField, 2, 5);
        grid.add(new Label("Author Birth Date "), 1, 6);
        grid.add(birthDayField, 2, 6);
        birthDayField.setPromptText("Optional");

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
                        String authorName = authorField.getText().trim();
                        if (!authorName.isEmpty()) {
                            Date authorBirthDate = birthDayField.getValue() != null 
                                ? Date.valueOf(birthDayField.getValue()) 
                                : null;
                            Author author = authorBirthDate != null 
                                ? new Author(authorName, authorBirthDate)
                                : new Author(authorName);
                            result.addAuthor(author);
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
        if (authorField.getText().trim().isEmpty()) {
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
