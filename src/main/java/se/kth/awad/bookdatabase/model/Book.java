package se.kth.awad.bookdatabase.model;


import java.sql.Date;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * Representation of a book.
 *
 * @author anderslm@kth.se
 */
public class Book {

    private int bookId;
    private String isbn; // should check format
    private String title;
    private Date published;
    private String storyLine = "";
    private int grade;
    private ArrayList<Author> authors;
    private Genre genre;
    private static final Pattern ISBN_PATTERN =
            Pattern.compile("^(978|979)\\d{10}$");


    // TODO:
    // Add authors, as a separate class(!), and corresponding methods, to your implementation
    // as well, i.e. "private ArrayList<Author> authors;"

    public Book(int bookId, String isbn, String title, Date published,Genre genre ,int grade) {
        if (!isValidIsbn(isbn)) {
            throw new IllegalArgumentException("Invalid ISBN !!!");
        }else if(!isValidGrade(grade)){
            throw new IllegalArgumentException("Invalid Grade !!!");
        }
        this.bookId = bookId;
        this.isbn = isbn;
        this.title = title;
        this.published = published;
        this.genre = genre;
        this.grade = grade;
        this.authors = new ArrayList<>();
    }

    public Book(String isbn, String title, Date published, String genre,int grade) {
        this(-1, isbn, title, published, parseGenre(genre), grade);
    }
    public Book(String isbn, String title, Date published, Genre genre,int grade) {
        this(-1, isbn, title, published, genre, grade);
    }

    public int getBookId() { return bookId; }
    public String getIsbn() { return isbn; }
    public String getTitle() { return title; }
    public Date getPublished() { return published; }
    public String getStoryLine() { return storyLine; }
    public Genre getGenre() { return genre; }

    public static Genre parseGenre(String genreString) {
        return Genre.valueOf(genreString.toUpperCase());
    }
    public String getStringGenre (Genre genre){
        return String.valueOf(genre);
    }
    public int getGrade() { return grade; }
    public Author getAuthor(int n){
        return authors.get(n);
    }

    public ArrayList<Author> getAuthores() {
        return new ArrayList<>(authors);
    }
    public String getAuthors() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < authors.size(); i++) {
            if (i == 0) {
                sb.append(authors.get(i).getName());
            } else {
                sb.append(", ").append(authors.get(i).getName());
            }
        }
        return sb.toString();
    }

    public void setStoryLine(String storyLine) {
        this.storyLine = storyLine;
    }

    public void setAuthor(Author author) {
        this.authors.add(author);
    }
    public void setGrade(int grade){
        this.grade = grade;
    }

    public static boolean isValidIsbn(String isbn) {
        return ISBN_PATTERN.matcher(isbn).matches();
    }
    public static boolean isValidGrade(int grade){
        if (grade <= 0 || grade > 5){
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return title + ", " + isbn + ", " + published.toString();
    }
}