package models;

import java.io.Serializable;

public class Rating implements Serializable {
    private String playerId;
    private String gameName;
    private float stars;


    public Rating (String playerId, String gameName, float stars) {
        this.playerId = playerId;
        this.gameName = gameName;
        this.stars = stars;
    }

    public String getGameName ( ) {
        return gameName;
    }

    public void setGameName (String gameName) {
        this.gameName = gameName;
    }

    public float getStars ( ) {
        return stars;
    }

    public void setStars (float stars) {
        this.stars = stars;
    }

    public String getPlayerId ( ) {
        return playerId;
    }

    public void setPlayerId (String playerId) {
        this.playerId = playerId;
    }
}
