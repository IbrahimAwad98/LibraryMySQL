package se.kth.awad.bookdatabase.model;
import java.util.Date;

public class Review {
    private String text;
    private Date date;

    public Review(String text){
        this.text = text;
        this.date = new Date();
    }

    public Date getDate() {
        return date;
    }

    public String getText() {
        return text;
    }

    public void setDate(Date date) {
        this.date = new Date();
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "Review{" +
                "text='" + text + '\'' +
                ", date=" + date +
                '}';
    }
}
