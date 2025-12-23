package se.kth.awad.bookdatabase.model;

import java.sql.Date;
import java.util.Objects;

/**
 * Representerar en författare från T_Author tabellen i LibraryDB.
 */
public class Author {
    private int authorID;
    private String name;
    private Date birthDate;
    private int addedBy;

    // Konstruktor för att läsa från databasen (med ID)
    public Author(int authorID, String name, Date birthDate) {
        this.authorID = authorID;
        this.name = name;
        this.birthDate = birthDate;
        this.addedBy = -1;
    }

    public Author(int authorID, String name, Date birthDate, int addedBy) {
        this(authorID, name, birthDate);
        this.addedBy = addedBy;
    }

    // Konstruktor för att skapa ny författare (utan ID)
    public Author(String name, Date birthDate) {
        this(-1, name, birthDate);
    }

    public Author(String name, Date birthDate, int addedBy){
        this(-1,name,birthDate,addedBy);
    }

    public Author(String authorName) {
        this.name = authorName;
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

    public int getAddedBy(){
        return addedBy;
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

    public void setAddedBy(int newAddedBy){
        this.addedBy = newAddedBy;
    }

    public String getBirthYear() {
        if (birthDate == null) {
            return "N/A";
        }
        return String.valueOf(birthDate.toLocalDate().getYear());
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

    @Override
    public String toString() {
        return "Author{" +
                "authorID=" + authorID +
                ", name='" + name + '\'' +
                ", birthDate=" + birthDate +
                ", addedBy=" + addedBy +
                '}';
    }
}