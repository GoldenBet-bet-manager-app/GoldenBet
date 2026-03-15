package models;

import java.io.Serializable;
import java.math.BigDecimal;

public class Bet implements Serializable {
    BigDecimal amount;
    String playerid,gamename;


    public Bet (BigDecimal amount, String playerid, String gamename) {
        this.amount = amount;
        this.playerid = playerid;
        this.gamename = gamename;
    }

    public BigDecimal getAmount () {
        return amount;
    }

    public void setAmount (BigDecimal amount) {
        this.amount = amount;
    }

    public String getPlayername() {
        return playerid;
    }

    public void setPlayername (String playername) {
        this.playerid = playername;
    }

    public String getGamename(){
        return gamename;
    }

    public void setGamename (String gamename) {
        this.gamename = gamename;
    }
}
