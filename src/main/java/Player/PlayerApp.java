package Player;

import Client.PlayClient;
import Client.SearchClient;
import models.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Scanner;
import Client.RatingClient;

public class PlayerApp {
    private Player currentPlayer;
    private BigDecimal tokenBalance;

    public PlayerApp (Player currentPlayer) {
        this.currentPlayer = currentPlayer;
        this.tokenBalance = BigDecimal.ZERO;
    }

    public BigDecimal getTokenBalance ( ) {
        return tokenBalance;
    }

    public void setTokenBalance (BigDecimal tokenBalance) {
        this.tokenBalance = tokenBalance;
    }


    public void search (String playerId, ArrayList<String> categories, int stars, String betLimit, String riskLevel) {//να βαλω exceptions edo
        SearchFilters filters = new SearchFilters ( playerId, categories, stars, betLimit, riskLevel );
        SearchClient searchClient = new SearchClient ( "127.0.0.1", 4321, filters );
        searchClient.start ( );
        System.out.println ( "Αποστολή αιτήματος αναζήτησης στον Master... Παρακαλώ περιμένετε τα αποτελέσματα." );
    }


    public void play (String gameName, BigDecimal amount) {
        if ( gameName == null || gameName.trim ( ).isEmpty ( ) )
            throw new IllegalArgumentException ( "Σφάλμα: Μη έγκυρο όνμα παιχνιδιού" );
        if ( amount.compareTo ( BigDecimal.ZERO ) <= 0 )
            throw new IllegalArgumentException ( "Σφάλμα: Το πονάρισμα δεν μπορεί να είναι αρνητικός ή το μηδέν" );
        if ( amount.compareTo ( this.tokenBalance ) > 0 )
            throw new IllegalArgumentException ( "Σφάλμα: Δεν έχετε αρκετά tokens." );
        this.tokenBalance = this.tokenBalance.subtract ( amount );
        Bet bet = new Bet ( amount, currentPlayer.getName ( ), gameName );
        PlayClient playClient = new PlayClient ( "127.0.0.1", 4321, bet );
        playClient.start ( );
        System.out.println ( "Το ποντάρισμα για το παιχνίδι " + gameName + " στάλθηκε..." );
    }

    public void addBalance (BigDecimal amount) {
        if ( amount.compareTo ( BigDecimal.ZERO ) <= 0 )
            throw new IllegalArgumentException ( "Σφάλμα: Το πλήθος των tokens δεν μπορεί να είναι αρνητικός ή το μηδέν" );
        this.tokenBalance = this.tokenBalance.add ( amount );
        System.out.println ( "Επιτυχής κατάθεση!\nΝέο διαθέσιμο υπόλοιπο: " + this.tokenBalance + " tokens." );
    }

    public void rate (String gameName, int stars) {
        if ( gameName == null || gameName.trim ( ).isEmpty ( ) )
            throw new IllegalArgumentException ( "Σφάλμα: Μη έγκυρο όνμα παιχνιδιού" );
        if ( stars < 1 || stars > 5 )
            throw new IllegalArgumentException ( "Σφάλμα: Η βαθμολογία του κάθε παιχνιδιού πρέπει να είναι από 1 έως 5 αστέρια." );

        System.out.println ( "Αποστολή βαθμολογίας (" + stars + " αστέρια) για το " + gameName + "..." );
        Rating rating = new Rating ( currentPlayer.getPlayerId ( ), gameName, stars );
        RatingClient ratingClient = new RatingClient ( "127.0.0.1", 4321, rating );
        ratingClient.start ( );
    }


    private static String formatGameName (String input) {
        if ( input == null || input.trim ( ).isEmpty ( ) ) {
            return "";
        }
        String[] words = input.trim ( ).toLowerCase ( ).split ( "\\s+" );
        StringBuilder formattedName = new StringBuilder ( );

        for ( String word : words ) {
            if ( word.length ( ) > 0 ) {
                // Κάνουμε κεφαλαίο το πρώτο γράμμα και κολλάμε την υπόλοιπη λέξη
                formattedName.append ( Character.toUpperCase ( word.charAt ( 0 ) ) )
                        .append ( word.substring ( 1 ) )
                        .append ( " " );
            }
        }
        return formattedName.toString ( ).trim ( );
    }


    public static void main (String[] args) {
        Scanner sc = new Scanner ( System.in );
        //EDO MALLON THA XREIASTO LISTA ME TOYS PLAYERS OSTE NA KSERO AN PREPEI NA GINETAI EGGRAFH H EISODOS
        System.out.println ( "============ Εγγραφή / Είσοδος Παίκτη ========" );
        System.out.print ( "Όνομα: " );
        String name = sc.nextLine ( );
        System.out.print ( "Επίθετο: " );
        String surname = sc.nextLine ( );
        System.out.print ( "Username: " );
        String username = sc.nextLine ( );
        System.out.print ( "Password: " );
        String password = sc.nextLine ( );
        System.out.print ( "Player ID: " );
        String id = sc.nextLine ( );

        Player myPlayer = new Player ( name, surname, username, password, id );
        PlayerApp currentplayer = new PlayerApp ( myPlayer );

        // ================== ΔΙΑΔΡΑΣΤΙΚΟ ΜΕΝΟΥ ==================
        boolean running = true;
        while (running) {
            System.out.println ( "\n============= ΜΕΝΟΥ ΕΠΙΛΟΓΩΝ =============" );
            System.out.println ( "Υπόλοιπο: " + currentplayer.getTokenBalance ( ) + " tokens" );
            System.out.println ( "1. Προσθήκη Υπολοίπου (Κατάθεση)" );
            System.out.println ( "2. Αναζήτηση Παιχνιδιών (Βάσει Κριτηρίων)" );
            System.out.println ( "3. Παίξε Παιχνίδι" );
            System.out.println ( "4. Βαθμολόγησε Παιχνίδι" );
            System.out.println ( "5. Αποσύνδεση" );
            System.out.print ( "Επιλέξτε ενέργεια (1-5): " );

            String choice = sc.nextLine ( );

            try {
                switch (choice) {
                    case "1":
                        System.out.print ( "Πληκτρολογήστε ποσό κατάθεσης: " );
                        String depositAmount = sc.nextLine ( );
                        currentplayer.addBalance ( new BigDecimal ( Integer.parseInt ( depositAmount ) ) );
                        break;

                    case "2":

                        System.out.println ( "\n--- Κριτήρια Αναζήτησης ---" );
                        // 1. Κατηγορίες (Διαχωρισμός με κόμμα αν είναι πολλές)
                        System.out.print ( "Εισάγετε κατηγορίες (π.χ. Cards,Slots) ή πατήστε Enter για να μην βάλετε φίλτρο: " );
                        String catInput = sc.nextLine ( );
                        ArrayList<String> searchCategories = new ArrayList<> ( );

                        // Αν ο χρήστης δεν πάτησε απλά Enter, χωρίζουμε τις λέξεις με βάση το κόμμα
                        if ( !catInput.trim ( ).isEmpty ( ) ) {
                            String[] cats = catInput.split ( "," );
                            for ( String c : cats ) {
                                searchCategories.add ( c.trim ( ) ); // Το trim() καθαρίζει τα περιττά κενά
                            }
                        }

                        // 2. Αστέρια
                        System.out.print ( "Ελάχιστα αστέρια (1-5) ή πατήστε 0 για κανένα φίλτρο: " );
                        int searchStars = 0;
                        try {
                            searchStars = Integer.parseInt ( sc.nextLine ( ) );
                        } catch (NumberFormatException e) {
                            System.out.println ( "Δεν δώσατε έγκυρο αριθμό. Το φίλτρο αστεριών δεν θα εφαρμοστεί." );
                        }

                        // 3. Όριο Πονταρίσματος
                        System.out.print ( "Όριο Πονταρίσματος ($, $$, $$$) ή πατήστε Enter για κανένα φίλτρο: " );
                        String searchBetLimit = sc.nextLine ( ).trim ( );

                        // 4. Επίπεδο Ρίσκου
                        System.out.print ( "Επίπεδο Ρίσκου (low, medium, high) ή πατήστε Enter για κανένα φίλτρο: " );
                        String searchRiskLevel = sc.nextLine ( ).trim ( );

                        System.out.println ( "Αναζήτηση στα παιχνίδια του GoldenBet..." );
                        // currentplayer.search(myPlayer.getPlayerId(), searchCategories, searchStars, searchBetLimit, searchRiskLevel);
                        // Μικρή παύση για να έρθει η απάντηση από τον Server
                        Thread.sleep ( 1500 );
                        break;

                    case "3":
                        System.out.print ( "Πληκτρολογήστε το όνομα του παιχνιδιού: " );
                        String rawGameInput = sc.nextLine ( );
                        String gameToPlay = formatGameName ( rawGameInput );
                        System.out.print ( "Πληκτρολογήστε το ποσό του πονταρίσματος: " );
                        String betAmount = sc.nextLine ( );

                        currentplayer.play ( gameToPlay, new BigDecimal ( Integer.parseInt ( betAmount ) ) );

                        // Μικρή παύση για να προλάβει να έρθει το αποτέλεσμα (Νίκη/Ήττα)
                        Thread.sleep ( 1500 );
                        break;

                    case "4":
                        System.out.print ( "Πληκτρολογήστε το όνομα του παιχνιδιού που θέλετε να βαθμολογήσετε: " );
                        String gameInput = sc.nextLine ( );
                        String gameToRate = formatGameName ( gameInput );
                        System.out.print ( "Δώστε βαθμολογία (1 έως 5 αστέρια): " );
                        int starsRating = Integer.parseInt ( sc.nextLine ( ) );

                        currentplayer.rate ( gameToRate, starsRating );
                        break;

                    case "5":
                        System.out.println ( "Αποσύνδεση παίκτη... Ευχαριστούμε που παίξατε!" );
                        running = false;
                        break;

                    default:
                        System.out.println ( "Μη έγκυρη επιλογή! Παρακαλώ εισάγετε έναν αριθμό από το 1 έως το 5." );
                }
            } catch (IllegalArgumentException e) {
                System.out.println ( e.getMessage ( ) );
            } catch (Exception e) {
                System.out.println ( "Σφάλμα στην εισαγωγή στοιχείων. Προσπαθήστε ξανά." );
            }
        }

    }
}


