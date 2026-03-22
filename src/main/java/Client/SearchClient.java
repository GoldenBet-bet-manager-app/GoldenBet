package Client;

import Player.PlayerApp;
import models.Game;

import java.util.ArrayList;

public class SearchClient extends Client {
    private PlayerApp playerApp;

    public SearchClient (String ip, int port, Object object, PlayerApp playerApp) {
        super ( ip, port, object );
        this.playerApp = playerApp;
    }

    @Override
    public void run ( ) {
        super.run ( );
        Object response = getResponse ( );
        if ( !playerApp.getProjectIsActive( ) )
            System.out.println ( "\n===== Αποτελέσματα Αναζήτησης ======" );
        else
            System.out.println ( "\n===== Προβολή διαθέσιμων Παιχνίδιών ======" );

        if ( response != null ) {
            ArrayList<Game> foundgames = (ArrayList<Game>) response;
            int i = 0;
            if ( foundgames.isEmpty ( ) ) {
                System.out.println ( "Δεν βρέθηκαν παιχνίδια!!!!!!" );
            }
            for ( Game g : foundgames ) {
                i++;
                System.out.println ( i + ") " + g.getGameName ( ) + " | Αστέρια: " + g.getStars ( ) +
                        " | Ποντάρισμα από " + g.getMinBet ( ) + " FUN εώς " + g.getMaxBet ( ) + " FUN" +
                        " | Επίπεδο Ρίσκου: " + g.getRiskLevel ( ) );
            }
        } else
            System.out.println ( "Σφάλμα: Δεν λάβαμε απάντηση από τον Master." );

    }
}