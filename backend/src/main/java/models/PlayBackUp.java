package models;

import java.io.Serializable;
import java.math.BigDecimal;

public class PlayBackUp implements Serializable {
    private BigDecimal payout;
    private Bet bet;

    public PlayBackUp (BigDecimal payout, Bet bet) {
        this.payout = payout;
        this.bet = bet;
    }

    public BigDecimal getPayout ( ) {
        return payout;
    }

    public Bet getBet ( ) {
        return bet;
    }
}
