package Client;

import models.Game;
import models.Rating;

public class RatingClient extends Client {

    public RatingClient (String ip, int port, Object object) {
        super ( ip, port, object );
    }
    @Override
    public void run() {
        super.run();
        Object response = getResponse();
        if(response!=null) {
            Rating rating = (Rating) response;
            System.out.println ( "Το παιχνίδι '" + rating.getGameName ( ) + "' έχει πλέον " + response + " αστέρια" );
        }else{
            System.out.println("Σφάλμα: Δεν λάβαμε απάντηση από τον Master.");
        }
    }
}