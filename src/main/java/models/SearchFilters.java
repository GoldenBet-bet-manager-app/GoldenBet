package models;

import java.io.Serializable;
import java.util.ArrayList;

public class SearchFilters implements Serializable {
    private String playerId;
    private float minstars,maxstars;
    private String betCategory;  // "$", "$$", "$$$"
    private String riskLevel;

    public SearchFilters (String playerId, float minstars,float maxstars ,String betCategory, String riskLevel) {
        this.playerId = playerId;
        this.minstars = minstars;
        this.maxstars=maxstars;
        this.betCategory = betCategory;
        this.riskLevel = riskLevel;
    }

    public String getPlayerId ( ) {
        return playerId;
    }

    public float getMinstars ( ) {
        return minstars;
    }

    public float getMaxstars ( ) {
        return maxstars;
    }

    public String getBetCategory ( ) {
        return betCategory;
    }
    
    public String getRiskLevel ( ) {
        return riskLevel;
    }

}