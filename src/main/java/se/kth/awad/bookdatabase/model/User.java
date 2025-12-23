package se.kth.awad.bookdatabase.model;

import java.util.Objects;

public class User {
    private int userID;
    private String username;

    public User(int userID, String username){
        this.userID = userID;
        this.username = username;
    }
    // Konstruktor för att skapa ny user (utan ID)
    public User(String username, String password){
        this(-1, username);
    }

    public User(String username){
        this.username =username;
    }

    public int getUserID(){
        return userID;
    }

    public String getUsername(){
        return username;
    }

    public void setUserID(int userID){
        this.userID = userID;
    }

    public boolean equals(Object obj){
        if(this == obj){
            return true;
        }
        if(obj == null || getClass() != obj.getClass()){
            return false;
        }
        User other = (User) obj;
        return userID == other.userID;
    }

    public int hashCode(){
        return Objects.hash(userID);
    }

    @Override
    public String toString(){
        return "User{" +
                "userID=" + userID +
                ", username='" + username + '\'' +
                '}';
    }
}

