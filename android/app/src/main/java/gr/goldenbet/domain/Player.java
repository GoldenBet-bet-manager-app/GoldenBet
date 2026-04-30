package gr.goldenbet.domain;


import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

public class Player implements Serializable {
    private String name;
    private String surname;
    private String username;
    private String password;
    private String playerId;
    private boolean isLoggedIn;
    private static final AtomicInteger idCounter = new AtomicInteger (1);

    public Player (String name, String surname, String username, String password) {
        this.name = name;
        this.surname = surname;
        this.username = username;
        this.password = password;
        this.isLoggedIn=false;
        this.playerId="Player_"+idCounter.getAndIncrement ();
    }

    public String getName ( ) {
        return name;
    }

    public void setName (String name) {
        this.name = name;
    }

    public String getSurname ( ) {
        return surname;
    }

    public void setSurname (String surname) {
        this.surname = surname;
    }

    public String getUsername ( ) {
        return username;
    }

    public void setUsername (String username) {
        this.username = username;
    }

    public String getPassword ( ) {
        return password;
    }

    public void setPassword (String password) {
        this.password = password;
    }

    public String getPlayerId ( ) {
        return playerId;
    }

    public boolean isLoggedIn ( ) {
        return isLoggedIn;
    }

    public void setLoggedIn (boolean loggedIn) {
        isLoggedIn = loggedIn;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }
}
