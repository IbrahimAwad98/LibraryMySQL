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
    private int averageRating;
    private int numRatings;
    //VG attribut
    private int addedBy;
    private ArrayList<Review> reviews;
    private int addedByUserID;
    private String addedByUsername;


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
        this.addedBy = -1; //inte satt en så länge
        this.reviews = new ArrayList<>();
    }

     // Konstruktor för att skapa ny bok (utan ID).
    public Book(String ISBN, String title, Date publishedDate){
        this(-1, ISBN, title, publishedDate);
    }

    public Book(int bookID,
                String ISBN,
                String title,
                Date publishedDate,
                int addedBy){
        this(bookID, ISBN, title, publishedDate);
        this.addedBy = addedBy;
    }

    // Konstruktor för att skapa ny bok med användare (VG)
    public Book(String ISBN, String title, Date publishedDate, int addedBy){
        this(-1, ISBN, title, publishedDate, addedBy);
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

    public int getAddedByUserID(){
        return addedByUserID;
    }

    public String getAddedByUsername() {
        return addedByUsername;
    }

    /**
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

    public int getAverageRating(){
        return averageRating;
    }

    public int getNumRatings(){
        return numRatings;
    }

    public int getAddedBy(){
        return addedBy;
    }

    public ArrayList<Review> getReviews(){
        return reviews;
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

    public void setAverageRating(int averageRating) {
        this.averageRating = averageRating;
    }

    public void setNumRatings(int numRatings) {
        this.numRatings = numRatings;
    }

    public void setAddedBy(int newAddedBy){
        this.addedBy = newAddedBy;
    }

    public void setReviews(ArrayList<Review> newReviews){
        this.reviews = newReviews;
    }

    public void setAddedByUserID(int addedByUserID) {
        this.addedByUserID = addedByUserID;
    }

    public void setAddedByUsername(String addedByUsername) {
        this.addedByUsername = addedByUsername;
    }

    // Metoder
    public void addAuthor(Author author){
        boolean exists = false;
        for (Author existingAuthor : this.authors) {
            if (existingAuthor.getName().equalsIgnoreCase(author.getName())) {
                exists = true;
                break;
            }
        }
        if (!exists) {
            this.authors.add(author);
        }
    }

    public void addGenre(Genre genre){
        boolean exists = false; //un
        for (Genre existingGenre : this.genres) {
            if (existingGenre.getGenreName().equalsIgnoreCase(genre.getGenreName())) {
                exists = true;
                break;
            }
        }
        if (!exists) {
            this.genres.add(genre);
        }
    }

    public void addReview(Review review){
        boolean exists = false; //undvik dubbletter
        for(Review existingReview: this.reviews){
            if(existingReview.getReviewID() == review.getReviewID()){
                exists = true;
                break;
            }
        }
        if(!exists){
            this.reviews.add(review);
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

    public String getReviewsAsString(){
        if(reviews.isEmpty()){
            return "No reviews yet";
        }
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i <reviews.size();i++){
            Review r = reviews.get(i);
            sb.append(r.getReviewDate()).append(": ");
            sb.append(r.getReviewText().substring(0, Math.min(50, r.getReviewText().length())));
            if(r.getReviewText().length() > 50){
                sb.append("...");
            }
            if(i < reviews.size() - 1){
                sb.append("\n");
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

    //antal recensioner för denna bok
    public int getNumReviews(){
        return reviews.size();
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

    public String toString(){
        return "Book{" +
                "bookID=" + bookID +
                ", ISBN='" + ISBN + '\'' +
                ", title='" + title + '\'' +
                ", avgRating=" + averageRating +
                ", numRatings=" + numRatings +
                ", addedBy=" + addedBy +
                ", numReviews=" + reviews.size() +
                ", authors=" + authors.size() +
                ", genres=" + genres.size() +
                '}';
    }
}