package Manager;

import java.util.Scanner;

public class ManagerApp {

    public static void startConsoleMenu() {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println ( "\n=========== ΜΕΝΟΥ ΔΙΑΧΕΙΡΙΣΗΣ ===========" );
            System.out.println ( "1. Προσθήκη Παιχνιδιών" );
            System.out.println ( "2. Αφαίρεση Παιχνιδιού" );
            System.out.println ( "3. Τροποποίηση Παιχνιδιού" );
            System.out.println ( "4. Εμφάνιση κερδών/ζημιών ανά Παιχνίδι" );
            System.out.println ( "5. Εμφάνιση κερδών/ζημιών ανά Παίκτη" );
            System.out.println ( "0. Έξοδος" );
            System.out.print ( "Επιλέξτε μια ενέργεια: " );
            String choice = scanner.nextLine ( );


            switch (choice){
                case "0":
                    running=false;
                case "1":

                case "2":
                case "3":
                case "4":
                case "5":

            }
        }
        scanner.close();
    }
    public static void main(String[] args) {
        startConsoleMenu ();

    }
}
