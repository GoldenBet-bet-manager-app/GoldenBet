package Client;

import Player.PlayerApp;
import models.Bet;
import models.MasterMessage;
import models.Player;

import java.math.BigDecimal;

public class PlayClient extends Client {
    private PlayerApp playerapp;

    public PlayClient (String ip, int port, Object object, PlayerApp playerapp) {
        super ( ip, port, object );
        this.playerapp=playerapp;
    }

    @Override
    public void run ( ) {
        super.run ( );
        Object response = getResponse ( );
        Object request = getObject ();
        MasterMessage originalMessage = (MasterMessage) request;
        Bet bet = (Bet) originalMessage.getPayload();

        System.out.println ( "\n======= Αποτέλεσμα Παιχνιδιού =======" );
        if ( response instanceof String )
            System.out.println ( response );
        else if ( response instanceof BigDecimal ) {
            playerapp.changeGamePlayedStatus ( bet.getGamename ( ) );
            BigDecimal payout = (BigDecimal) response;
            playerapp.subBalance ( bet.getAmount () );
            System.out.println ( "Αφαιρέθηκαν: " + bet.getAmount ( ) +" FUN" );
            if ( payout.compareTo ( BigDecimal.ZERO ) > 0 ) {
                System.out.println ( "Συγχαρητήρια! Κερδίσατε " + payout + " FUN!" );
                playerapp.addBalance ( payout );
            } else
                System.out.println ( "Δυστυχώς δεν κερδίσατε αυτή τη φορά. Καλή τύχη στο επόμενο ποντάρισμα!" );
        } else {
            System.out.println ( "Σφάλμα: Δεν λάβαμε απάντηση από τον Master." );
        }
    }
}