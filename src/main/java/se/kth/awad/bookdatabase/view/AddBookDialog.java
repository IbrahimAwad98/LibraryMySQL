package se.kth.awad.bookdatabase.view;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.Event;
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
    private final ComboBox <Integer> gradeField = new ComboBox<>();
    private final TextField authorField = new TextField();
    private final DatePicker birthDayField = new DatePicker();
    private final TextField biographyField = new TextField();

    private final ComboBox<Genre> genreChoice = new ComboBox<>(FXCollections
            .observableArrayList(Genre.values()));

    public AddBookDialog() {
        buildAddBookDialog();
    }

    private void buildAddBookDialog() {

        this.setTitle("Add a new book");
        this.setResizable(false); // really?
        gradeField.getItems().addAll(1, 2, 3, 4, 5);

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.add(new Label("Title "), 1, 1);
        grid.add(titleField, 2, 1);
        grid.add(new Label("Isbn "), 1, 2);
        grid.add(isbnField, 2, 2);
        grid.add(new Label("Genre "), 1, 3);
        grid.add(genreChoice, 2, 3);
        grid.add(new Label("Published "), 1, 4);
        grid.add(publishedField, 2, 4);
        grid.add(new Label("Grade "), 1, 5);
        grid.add(gradeField, 2, 5);
        grid.add(new Label("Name "), 1, 6);
        grid.add(authorField,  2, 6);
        grid.add(new Label("Birth Date "), 1, 7);
        grid.add(birthDayField,2,7);
        grid.add(new Label("Biography "),1,8);
        grid.add(biographyField,2,8);


        this.getDialogPane().setContent(grid);

        ButtonType buttonTypeOk
                = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
        this.getDialogPane().getButtonTypes().add(buttonTypeOk);
        ButtonType buttonTypeCancel
                = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        this.getDialogPane().getButtonTypes().add(buttonTypeCancel);

        // this callback returns the result from our dialog, via
        // Optional<FooBook> result = dialog.showAndWait();
        // FooBook book = result.get();
        // see DialogExample, line 31-34
        this.setResultConverter(new Callback<ButtonType, Book>() {
            @Override
            public Book call(ButtonType b) {
                Book result = null;
                Author author = null;
                if (b == buttonTypeOk) {
                    if (isValidData()) {
                        result = new Book(
                                isbnField.getText(),
                                titleField.getText(),
                                Date.valueOf(publishedField.getValue()),
                                genreChoice.getValue(),
                                gradeField.getValue()
                        );
                        author = new Author(
                                authorField.getText(),
                                Date.valueOf(birthDayField.getValue()),
                                biographyField.getText()
                        );
                        result.setAuthor(author);

                    }
                }
                clearFormData();
                return result;
            }
        });

        // add an event filter to keep the dialog active if validation fails
        // (yes, this is ugly in FX)
        Button okButton
                = (Button) this.getDialogPane().lookupButton(buttonTypeOk);
        okButton.addEventFilter(ActionEvent.ACTION, new EventHandler() {
            @Override
            public void handle(Event event) {
                if (!isValidData()) {
                    event.consume();
                    showErrorAlert("Form error", "Invalid input");
                }
            }
        });
    }


    private boolean isValidData() {
        if (genreChoice.getValue() == null) {
            return false;
        }
        if (!Book.isValidIsbn(isbnField.getText())) {
            System.out.println(isbnField.getText());
            return false;
        }
        if(!Book.isValidGrade(gradeField.getValue())){
            System.out.println(gradeField.getValue());
            return false;
        }
        return true;
    }

    private void clearFormData() {
        titleField.setText("");
        isbnField.setText("");
        genreChoice.setValue(null);
        publishedField.setValue(null);
        gradeField.setValue(1);
        authorField.setText("");
        biographyField.setText("");
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

