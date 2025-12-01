package se.kth.awad.bookdatabase.model;

import java.util.ArrayList;

public class User {
    private String username;
    private String password;
    private String email;
    private ArrayList<Review> reviews;

    public User (String username, String password, String email){
        this.username = username;
        this.password = password;
        this.email = email;
        this.reviews = new ArrayList<>();
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public ArrayList<Review> getReviews() {
        return reviews;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setReviews(ArrayList<Review> reviews) {
        this.reviews = reviews;
    }
    public void setReview(Review review){
        this.reviews.add(review);
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", reviews=" + reviews +
                '}';
    }
}
