package se.kth.awad.bookdatabase.model;

import java.util.ArrayList;
import java.sql.Date;

public class Author {
    private int authorId;
    private String name;
    private Date birthdate;
    private String biography;
    private ArrayList<Book> books;

    public Author(int authorId, String name, Date birthdate, String biography) {
        this.authorId = authorId;
        this.name = name;
        this.birthdate = birthdate;
        this.biography = biography;
        this.books = new ArrayList<>();
    }
    public Author(String name, Date birthDate, String biography){
        this(-1,name,birthDate,biography);
    }
    public Author(String name){
        this(name,null,null);
    }
    public int getAuthorId() {
        return authorId;
    }
    public String getName() {
        return name;
    }
    public String getBiography() {
        return biography;
    }
    public Date getBirthdate(){ return birthdate; }

    public ArrayList<Book> getBooks() {
        return new ArrayList<>(books);
    }

    @Override
    public String toString() {
        return name;
    }
}
