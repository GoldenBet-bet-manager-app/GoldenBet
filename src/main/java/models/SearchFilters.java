package models;

import java.io.Serializable;
import java.util.ArrayList;

public class SearchFilters implements Serializable {
    private String playerId;
    private int stars;
    private String betCategory;  // "$", "$$", "$$$"
    private String riskLevel;

    public SearchFilters (String playerId, int stars, String betCategory, String riskLevel) {
        this.playerId = playerId;
        this.stars = stars;
        this.betCategory = betCategory;
        this.riskLevel = riskLevel;
    }

    public String getPlayerId ( ) {
        return playerId;
    }

    public int getStars ( ) {
        return stars;
    }

    public void setStars (int stars) {
        this.stars = stars;
    }

    public String getBetCategory ( ) {
        return betCategory;
    }


    public String getRiskLevel ( ) {
        return riskLevel;
    }

}