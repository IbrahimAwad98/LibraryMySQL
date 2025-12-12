package se.kth.awad.bookdatabase.model;

import java.time.LocalDate;

public class Review {
    private int reviewID;
    private int bookID;
    private int userID;
    private String reviewText;
    private LocalDate reviewDate;


    public Review(int reviewID, int bookID, int userID, String reviewText, LocalDate reviewDate){
        this.reviewID = reviewID;
        this.bookID = bookID;
        this.userID = userID;
        this.reviewText = reviewText;
        this.reviewDate = reviewDate;
    }

    public Review(int bookID, int userID, String reviewText, LocalDate reviewDate){
        this(-1,bookID,userID,reviewText,reviewDate);
    }

    public int getReviewID(){
        return reviewID;
    }

    public int getBookID(){
        return bookID;
    }

    public int getUserID(){
        return userID;
    }

    public String getReviewText(){
        return reviewText;
    }

    public LocalDate getReviewDate(){
        return reviewDate;
    }

    public void  setReviewID(int reviewID){
        this.reviewID = reviewID;
    }

    @Override
    public String toString() {
        return "Review{" +
                "reviewID=" + reviewID +
                ", bookID=" + bookID +
                ", userID=" + userID +
                ", reviewText='" + reviewText + '\'' +
                ", reviewDate=" + reviewDate +
                '}';
    }

}
