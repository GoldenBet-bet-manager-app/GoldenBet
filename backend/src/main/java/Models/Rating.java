package Models;

import java.io.Serializable;

public class Rating implements Serializable {
    private String gameName;
    private float stars;


    public Rating ( String gameName, float stars) {
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


}
