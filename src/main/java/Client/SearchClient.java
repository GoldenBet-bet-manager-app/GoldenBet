package Client;

import models.Game;

import java.util.ArrayList;

public class SearchClient extends Client {

    public SearchClient (String ip, int port, Object object) {

        super ( ip, port, object );
    }

    @Override
    public void run ( ) {
        super.run ( );
        Object response = getResponse ( );

        System.out.println ( "\n===== Αποτελέσματα Αναζήτησης από Master ======" );
        if ( response != null ) {
            ArrayList<Game> foundgames = (ArrayList<Game>) response;
            int i = 0;
            if ( foundgames.isEmpty ( ) ) {
                System.out.println ( "Δεν βρέθηκαν παιχνίδια με αυτά τα κριτήρια." );
            }
            for ( Game g : foundgames ) {
                i++;
                System.out.println ( i + ") " + g.getGameName ( ) + " | Αστέρια: " + g.getStars ( ) + " | Ποντάρισμα: " + g.getBetCategory ( ) );
            }
        } else {
            System.out.println("Σφάλμα: Δεν λάβαμε απάντηση από τον Master.");
        }
    }
}