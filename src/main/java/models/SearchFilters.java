package models;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;

public class SearchFilters implements Serializable {
    private String playerId;
    private ArrayList<String> categories;
    private int stars;
    private String betLimit;  // "$", "$$", "$$$"
    private String riskLevel;

    public SearchFilters (String playerId, ArrayList<String> categories, int stars, String betLimit, String riskLevel) {
        this.playerId = playerId;
        this.categories = categories;
        this.stars = stars;
        this.betLimit = betLimit;
        this.riskLevel = riskLevel;
    }

    public String getPlayerId ( ) {
        return playerId;
    }

    public void setPlayerId (String playerId) {
        this.playerId = playerId;
    }

    public ArrayList<String> getCategories ( ) {
        return categories;
    }

    public void setCategories (ArrayList<String> categories) {
        this.categories = categories;
    }

    public int getStars ( ) {
        return stars;
    }

    public void setStars (int stars) {
        this.stars = stars;
    }

    public String getBetLimit ( ) {
        return betLimit;
    }

    public void setBetLimit (String betLimit) {
        this.betLimit = betLimit;
    }

    public String getRiskLevel ( ) {
        return riskLevel;
    }

    public void setRiskLevel (String riskLevel) {
        this.riskLevel = riskLevel;
    }
}