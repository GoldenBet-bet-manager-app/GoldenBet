package Client;

import models.Game;
import models.Rating;

public class RatingClient extends Client {

    public RatingClient (String ip, int port, Object object) {
        super ( ip, port, object );
    }

    @Override
    public void run ( ) {
        super.run ( );
        Object response = getResponse ( );
        if ( response != null ) {
            System.out.println ( "Επιτυχία! Ο νέος μέσος όρος του παιχνιδιού είναι: " + response + " αστέρια!" );
        } else {
            System.out.println ( "Σφάλμα: Δεν λάβαμε απάντηση από τον Master." );
        }
    }
}