package Client;

import java.math.BigDecimal;

public class PlayClient extends Client {

    public PlayClient(String ip, int port, Object object) {
        super(ip, port, object);
    }

    @Override
    public void run() {
        super.run();
        Object response = getResponse();
        BigDecimal payout = (BigDecimal) response;
        System.out.println("\n======= Αποτέλεσμα Παιχνιδιού =======");
        if (response != null) {
            if(payout.compareTo(BigDecimal.ZERO) > 0){
                System.out.println("Συγχαρητήρια! Κερδίσατε " + payout + " tokens!");
            } else {
                System.out.println ( "Δυστυχώς δεν κερδίσατε αυτή τη φορά. Καλή τύχη στο επόμενο ποντάρισμα!" );
            }
        } else {
            System.out.println("Σφάλμα: Δεν λάβαμε απάντηση από τον Master.");
        }

    }
}