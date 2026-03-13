package models;

import java.io.Serializable;
import java.math.BigDecimal;

public class Bet implements Serializable {
    BigDecimal amount;
    String playername,gamename;


    public Bet (BigDecimal amount, String playername, String gamename) {
        this.amount = amount;
        this.playername = playername;
        this.gamename = gamename;
    }

    public BigDecimal getAmount ( ) {
        return amount;
    }

    public void setAmount (BigDecimal amount) {
        this.amount = amount;
    }

    public String getPlayername ( ) {
        return playername;
    }

    public void setPlayername (String playername) {
        this.playername = playername;
    }

    public String getGamename ( ) {
        return gamename;
    }

    public void setGamename (String gamename) {
        this.gamename = gamename;
    }
}
