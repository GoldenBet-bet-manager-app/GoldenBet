package Client;

import models.MasterMessage;
import models.MasterMessageType;

public class PlayClient extends Client {

    public PlayClient(String ip, int port, Object object) {
        super(ip, port, object);
    }

    @Override
    public void run() {
        super.run();
        Object response = getResponse();
        System.out.println("\n===== Αποτέλεσμα Παιχνιδιού =====");
        if (response != null) {
            System.out.println(response.toString());// edo prepei na emfanizetai exases h kerdises p 50 token
        } else {
            System.out.println("Σφάλμα: Δεν λάβαμε απάντηση από τον Master.");
        }
        System.out.println("=============================\n");
    }
}