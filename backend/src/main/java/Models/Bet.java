package Models;

import java.io.Serializable;
import java.math.BigDecimal;

public class Bet implements Serializable {
    BigDecimal amount;
    String playerId, gameName;


    public Bet (BigDecimal amount, String playerId, String gameName) {
        this.amount = amount;
        this.playerId = playerId;
        this.gameName = gameName;
    }

    public BigDecimal getAmount () {
        return amount;
    }

    public String getPlayerId( ) {
        return playerId;
    }

    public String getGameName(){
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }
}
