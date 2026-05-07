package gr.goldenbet.domain;

import java.io.Serializable;
import java.math.BigDecimal;

public class Bet implements Serializable {
    BigDecimal amount;
    String playerusername, gameName,playerid;


    public Bet(BigDecimal amount, String playerusername, String playerid, String gameName) {
        this.amount = amount;
        this.playerusername = playerusername;
        this.playerid = playerid;
        this.gameName = gameName;
    }

    public BigDecimal getAmount ( ) {
        return amount;
    }

    public void setAmount (BigDecimal amount) {
        this.amount = amount;
    }

    public String getPlayerusername ( ) {
        return playerusername;
    }

    public void setPlayerusername (String playerusername) {
        this.playerusername = playerusername;
    }

    public String getGameName ( ) {
        return gameName;
    }

    public void setGameName (String gameName) {
        this.gameName = gameName;
    }

    public String getPlayerid ( ) {
        return playerid;
    }

    public void setPlayerid (String playerid) {
        this.playerid = playerid;
    }
}
