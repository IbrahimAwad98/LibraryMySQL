package se.kth.awad.bookdatabase.model;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Representerar en bok från T_Book tabellen i LibraryDB.
 * Den har relation till Genre, author och rating
 */
public class Book {
    private int bookID;
    private String ISBN, title;
    private Date publishedDate;
    //ArrayList för (M:N) relationer.
    private ArrayList<Author> authors;
    private ArrayList<Genre> genres;
    //Betyginfo från T_Rating
    private double averageRating;
    private int numRatings;

    // Konstruktor för att läsa från databasen (med ID).
    public Book(int bookID,
                String ISBN,
                String title,
                Date publishedDate){
        this.bookID = bookID;
        this.ISBN = ISBN;
        this.title = title;
        this.publishedDate = publishedDate;
        this.authors = new ArrayList<>();
        this.genres = new ArrayList<>();
        this.averageRating = 0;
        this.numRatings = 0;
    }

     // Konstruktor för att skapa ny bok (utan ID).
    public Book(String ISBN, String title, Date publishedDate){
        this(-1, ISBN, title, publishedDate);
    }

    public int getBookID(){
        return bookID;
    }

    public String getISBN(){
        return ISBN;
    }

    public String getTitle(){
        return title;
    }

    public Date getPublishedDate(){
        return publishedDate;
    }

    /**
     *
     * @return direkt referens
     * den fungerar eftersom vi behöver ändra listor direkt
     * ingen säkerhets risk.
     */
    public ArrayList<Author> getAuthors(){
        return authors;
    }

    public ArrayList<Genre> getGenres(){
        return genres;
    }

    public double getAverageRating(){
        return averageRating;
    }

    public int getNumRatings(){
        return numRatings;
    }

    public void setBookID(int newID){
        this.bookID = newID;
    }

    public void setISBN(String newISBN){
        this.ISBN = newISBN;
    }

    public void setTitle(String newTitle){
        this.title = newTitle;
    }

    public void setPublishedDate(Date newDate){
        this.publishedDate = newDate;
    }

    public void setAuthors(ArrayList<Author> newAuthors){
        this.authors = newAuthors;
    }

    public void setGenres(ArrayList<Genre> genres) {
        this.genres = genres;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public void setNumRatings(int numRatings) {
        this.numRatings = numRatings;
    }

    // Metoder
    public void addAuthor(Author author){
        if(!this.authors.contains(author)){ //inga dubbletter
            this.authors.add(author);
        }
    }

    public void addGenre(Genre genre){
        if(!this.genres.contains(genre)){ //dubbletter
            this.genres.add(genre);
        }
    }

    // Används för att visa författare i TableView.
    public String getAuthorsAsString() {
        if (authors.isEmpty()) {
            return "Unknown";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < authors.size(); i++) {
            sb.append(authors.get(i).getName());
            if (i < authors.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    public String getGenresAsString() {
        if (genres.isEmpty()) {
            return "Unknown";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < genres.size(); i++) {
            sb.append(genres.get(i).getGenreName());
            if (i < genres.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    public String getPublishedYear(){
        if(publishedDate == null){
            return "N/A";
        }
        return String.valueOf(publishedDate.toLocalDate().getYear());
    }

    // Object metoder
    public boolean equals(Object obj){
        if(this == obj){
            return true;
        }
        if(obj == null || getClass() != obj.getClass()){
            return false;
        }
        Book book = (Book) obj;
        return bookID == book.bookID;
    }

    public int hashCode(){
        return Objects.hash(bookID);
    }
}