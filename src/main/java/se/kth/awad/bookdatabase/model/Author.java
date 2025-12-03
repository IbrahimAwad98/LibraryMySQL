package se.kth.awad.bookdatabase.model;

import java.sql.Date;
import java.util.Objects;

/**
 * Representerar en författare från T_Author tabellen i LibraryDB.
 */
public class Author {
    private int authorID;      // Matchar T_Author.authorID
    private String name;       // Matchar T_Author.name
    private Date birthDate;    // Matchar T_Author.birthDate

    // Konstruktor för att läsa från databasen (med ID)
    public Author(int authorID, String name, Date birthDate) {
        this.authorID = authorID;
        this.name = name;
        this.birthDate = birthDate;
    }

    // Konstruktor för att skapa ny författare (utan ID)
    public Author(String name, Date birthDate) {
        this(-1, name, birthDate);
    }

    // Enkel konstruktor (endast namn)
    public Author(String name) {
        this(name, null);
    }


    public int getAuthorID() {
        return authorID;
    }

    public String getName() {
        return name;
    }

    public Date getBirthDate() {
        return birthDate;
    }


    public void setAuthorID(int authorID) {
        this.authorID = authorID;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    @Override
    public String toString() {
        if (birthDate != null) {
            return name + " (born: " + birthDate + ")";
        }
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Author author = (Author) o;
        return authorID == author.authorID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(authorID);
    }
}