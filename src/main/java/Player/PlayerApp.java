package Player;

import Client.Client;
import Client.SearchClient;
import models.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class PlayerApp {

    private Player currentPlayer;
    private BigDecimal tokenBalance;
    private Boolean projectIsActive;
    private HashMap<String, Boolean> gamePlayedStatus = new HashMap<> ( );
    private ArrayList<Game> searchedGames=new ArrayList<> ();

    public void setSearchedGames (ArrayList<Game> searchedGames) {
        this.searchedGames = searchedGames;
    }

    public synchronized Boolean getProjectIsActive( ) {
        return projectIsActive;
    }

    public PlayerApp (Player currentPlayer) {
        this.currentPlayer = currentPlayer;
        this.tokenBalance = BigDecimal.ZERO;
    }

    public BigDecimal getTokenBalance ( ) {
        return tokenBalance;
    }


    public void search (String playerId, float minStars, float maxStars, String betLimit, String riskLevel) {
        if ( playerId == null || playerId.trim().isEmpty() ) {
            throw new IllegalArgumentException("Προσοχή: Το Player ID δεν μπορεί να είναι κενό.");
        }
        if ( minStars < 1.0f || minStars > 5.0f || maxStars < 1.0f || maxStars > 5.0f ) {
            throw new IllegalArgumentException("Προσοχή: Τα αστέρια πρέπει να είναι μεταξύ 1.0 και 5.0.");
        }
        if ( minStars > maxStars ) {
            throw new IllegalArgumentException("Προσοχή: Το ελάχιστο όριο αστεριών δεν μπορεί να είναι μεγαλύτερο από το μέγιστο.");
        }

        if ( betLimit != null && !betLimit.trim().isEmpty() ) {
            if ( !betLimit.equals("$") && !betLimit.equals("$$") && !betLimit.equals("$$$") ) {
                throw new IllegalArgumentException("Προσοχή: Το όριο πονταρίσματος πρέπει να είναι $, $$ ή $$$.");
            }
        }

        if ( riskLevel != null && !riskLevel.trim().isEmpty() ) {
            if ( !riskLevel.equalsIgnoreCase("low") && !riskLevel.equalsIgnoreCase("medium") && !riskLevel.equalsIgnoreCase("high") ) {
                throw new IllegalArgumentException("Προσοχή: Το επίπεδο ρίσκου πρέπει να είναι low, medium ή high.");
            }
        }

        SearchFilters filters = new SearchFilters ( playerId, minStars, maxStars, betLimit, riskLevel );
        SearchClient searchClient = new SearchClient ( "127.0.0.1", 43210, new MasterMessage ( MasterMessageType.SEARCH, filters ), this );
        searchClient.start ( );
    }


    public void play (String gameName, BigDecimal amount) throws InterruptedException {
        if ( gameName == null || gameName.trim ( ).isEmpty ( ) )
            throw new IllegalArgumentException ( "Προσοχή: Μη έγκυρο όνομα παιχνιδιού" );
        if ( amount.compareTo ( BigDecimal.ZERO ) <= 0 )
            throw new IllegalArgumentException ( "Προσοχή: Το ποντάρισμα δεν μπορεί να είναι αρνητικό ή μηδέν" );
        if ( amount.compareTo ( this.tokenBalance ) > 0 )
            throw new IllegalArgumentException ( "Προσοχή: Δεν έχετε αρκετά FUN." );

        Bet bet = new Bet ( amount, currentPlayer.getPlayerId(), gameName );
        Client playClient = new Client ( "127.0.0.1", 43210, new MasterMessage ( MasterMessageType.PLAY, bet ) );
        playClient.start ( );
        playClient.join ();

        Object response = playClient.getResponse ( );
        System.out.println ( "\n============ Αποτέλεσμα Παιχνιδιού ==============" );

        if ( response instanceof String )
            System.out.println ( response );
        else if ( response instanceof BigDecimal ) {
            changeGamePlayedStatus ( bet.getGameName( ) );
            BigDecimal payout = (BigDecimal) response;
            this.tokenBalance = this.tokenBalance.subtract ( amount );
            System.out.println ( "Αφαιρέθηκαν: " + bet.getAmount ( ) +" FUN" );
            if ( payout.compareTo ( BigDecimal.ZERO ) > 0 ) {
                System.out.println ( "Συγχαρητήρια! Κερδίσατε " + payout + " FUN!" );
                addBalance ( payout );
            } else
                System.out.println ( "Δυστυχώς δεν κερδίσατε αυτή τη φορά. Καλή τύχη στο επόμενο ποντάρισμα!" );
        } else
            System.out.println ( "Προσοχή: Δεν λάβαμε απάντηση από τον Master." );
    }

    public void addBalance (BigDecimal amount) {
        if ( amount.compareTo ( BigDecimal.ZERO ) <= 0 )
            throw new IllegalArgumentException ( "Προσοχή: Το πλήθος των FUN δεν μπορεί να είναι αρνητικό ή μηδέν" );
        this.tokenBalance = this.tokenBalance.add ( amount );
        System.out.println ( "Επιτυχής κατάθεση!\nΝέο διαθέσιμο υπόλοιπο: " + this.tokenBalance + " FUN." );
    }




    public void changeGamePlayedStatus (String gameName) {
        this.gamePlayedStatus.put ( gameName, true );
    }

    public void rate (String gameName, float stars) throws InterruptedException {
        if ( gameName == null || gameName.trim ( ).isEmpty ( ) )
            throw new IllegalArgumentException ( "Προσοχή: Μη έγκυρο όνομα παιχνιδιού" );
        if ( stars < 1.0f || stars > 5.0f )
            throw new IllegalArgumentException("Προσοχή: Η βαθμολογία πρέπει να είναι από 1.0 έως 5.0, με βήμα μισού αστεριού (π.χ. 1.0, 1.5, 2.0, 2.5...).");
        if ( gamePlayedStatus.getOrDefault ( gameName, false ) ) {
            Rating rating = new Rating ( currentPlayer.getPlayerId ( ), gameName, stars );
            Client ratingClient = new Client ( "127.0.0.1", 43210, new MasterMessage ( MasterMessageType.RATE, rating ) );
            ratingClient.start ( );
            ratingClient.join ();
            Object response = ratingClient.getResponse ( );
            if ( response instanceof String )
                System.out.println ( response );
            else if ( response instanceof Float )
                System.out.println ( "Επιτυχής αξιολόγηση! Ο νέος μέσος όρος του παιχνιδιού είναι: " + response + " αστέρια!" );
            else
                System.out.println ( "Σφάλμα: Δεν λάβαμε απάντηση από τον Master." );
        } else {
            throw new IllegalArgumentException ( "Προσοχή: Δεν μπορείτε να βαθμολογήσετε το παιχνίδι '" + gameName + "' γιατί δεν το έχετε παίξει ακόμα!" );
        }
    }

    public Boolean isavailableSelectedgame(String gameName,ArrayList<Game> availableGames){
        boolean contain=false;
        for(Game g: availableGames) {
            if ( g.getGameName ( ).equalsIgnoreCase ( gameName ) ) {
                contain = true;
                break;
            }
        }
        if (!contain )
            throw new IllegalArgumentException("Προσοχή: Το παιχνίδι που επιλέξατε δεν ανήκει στα αποτελέσματα της τρέχουσας αναζήτησης.");
        return contain;
    }
    // =========================================================================================
    // STATIC METHODS ΓΙΑ ΤΟ ΜΕΝΟΥ
    // =========================================================================================

    private void handleDeposit (Scanner sc, PlayerApp currentplayer) {
        System.out.print ( "Πληκτρολογήστε ποσό κατάθεσης: " );
        String depositAmount = sc.nextLine ( );
        currentplayer.addBalance ( new BigDecimal ( Integer.parseInt ( depositAmount ) ) );
    }

    private void handlePlay (Scanner sc, PlayerApp currentplayer) throws InterruptedException {
        ArrayList<Game> availableGames = searchedGames;
        if ( availableGames == null || availableGames.isEmpty ( ) ) {
            throw new IllegalArgumentException ( "Δεν υπάρχουν διαθέσιμα παιχνίδια! Παρακαλώ κάντε πρώτα Αναζήτηση (Επιλογή 2) ή Προβολή (Επιλογή 3)." );
        }

        System.out.print ( "Πληκτρολογήστε το όνομα του παιχνιδιού: " );
        String gameToPlay = sc.nextLine ( );
        isavailableSelectedgame(gameToPlay,availableGames);
        System.out.print ( "Πληκτρολογήστε το ποσό του πονταρίσματος: " );
        String betAmount = sc.nextLine ( );
        currentplayer.play ( gameToPlay, new BigDecimal (  betAmount  ) );
    }


    private void handleRate (Scanner sc, PlayerApp currentplayer) throws InterruptedException {
        ArrayList<Game> availableGames = searchedGames;
        System.out.print ( "Πληκτρολογήστε το όνομα του παιχνιδιού που θέλετε να βαθμολογήσετε: " );
        String gameToRate = sc.nextLine ( );
        System.out.print ( "Δώστε βαθμολογία από 1.0 έως 5.0, με βήμα μισού αστεριού (π.χ. 1.0, 1.5, 2.0, 2.5...): ");
        float starsRating = Float.parseFloat ( sc.nextLine ( ) );
        currentplayer.rate ( gameToRate, starsRating );
    }

    private void addsearchfilters (Scanner sc, PlayerApp currentplayer, Player myPlayer) {
        projectIsActive = false;
        System.out.println ( "\n--- Κριτήρια Αναζήτησης ---" );
        System.out.print ( "Δώστε Αστέρια (π.χ. 4) ή Εύρος (π.χ. 3-4.5) ή Enter για κανένα φίλτρο: " );
        String searchStars = sc.nextLine ( ).trim ( );
        float minSearchStars = 1.0f;
        float maxSearchStars = 5.0f;

        if ( !searchStars.isEmpty ( ) ) {
            try {
                if ( searchStars.contains ( "-" ) ) {
                    String[] parts = searchStars.split ( "-" );
                    minSearchStars = Float.parseFloat ( parts[ 0 ].trim () );
                    maxSearchStars = Float.parseFloat ( parts[ 1 ].trim () );
                } else {
                    minSearchStars = Float.parseFloat ( searchStars );
                    maxSearchStars = Float.parseFloat ( searchStars );
                }
            } catch (NumberFormatException e) {
                System.out.println ( "Μη έγκυρη μορφή αστεριών. Το φίλτρο δεν θα εφαρμοστεί." );
                minSearchStars = 1.0f;
                maxSearchStars = 5.0f;
            }
        }

        System.out.println ( "\n--- Όρια Πονταρίσματος ---" );
        System.out.println ( "Ελάχιστο ποντάρισμα 0.1 FUN -> $" );
        System.out.println ( "Ελάχιστο ποντάρισμα 1 FUN -> $$" );
        System.out.println ( "Ελάχιστο ποντάρισμα 5 FUN -> $$$" );
        System.out.print ( "Όριο Πονταρίσματος ($, $$, $$$) ή πατήστε Enter για κανένα φίλτρο: " );
        String searchBetLimit = sc.nextLine ( ).trim ( );

        System.out.print ( "Επίπεδο Ρίσκου (low, medium, high) ή πατήστε Enter για κανένα φίλτρο: " );
        String searchRiskLevel = sc.nextLine ( ).trim ( );

        currentplayer.search ( myPlayer.getPlayerId ( ), minSearchStars, maxSearchStars, searchBetLimit, searchRiskLevel );
    }

    private void projectavailablegames (Scanner sc, PlayerApp currentplayer, Player myPlayer) {
        projectIsActive = true;
        currentplayer.search ( myPlayer.getPlayerId ( ), 1.0f,5.0f, "","" );
    }


    private static void startmenu (Player myPlayer, PlayerApp currentplayer) {
        Scanner sc=new Scanner ( System.in );
        boolean running = true;
        while (running) {
            System.out.println ( "\n============= ΜΕΝΟΥ ΕΠΙΛΟΓΩΝ =============" );
            System.out.println ( "Υπόλοιπο: " + currentplayer.getTokenBalance ( ) + " FUN" );
            System.out.println ( "1. Προσθήκη Υπολοίπου (Κατάθεση)" );
            System.out.println ( "2. Αναζήτηση Παιχνιδιών (Βάσει Κριτηρίων)" );
            System.out.println ( "3. Προβολή διαθέσιμων Παιχνιδιών" );
            System.out.println ( "4. Παίξε Παιχνίδι" );
            System.out.println ( "5. Βαθμολόγησε Παιχνίδι" );
            System.out.println ( "6. Αποσύνδεση" );
            System.out.print ( "Επιλέξτε ενέργεια (1-5): " );

            String choice = sc.nextLine ( );

            try {
                switch (choice) {
                    case "1":
                        currentplayer.handleDeposit ( sc, currentplayer );
                        break;
                    case "2":
                        currentplayer.addsearchfilters ( sc, currentplayer, myPlayer );
                        Thread.sleep ( 800 );
                        break;
                    case "3":
                        currentplayer.projectavailablegames ( sc, currentplayer, myPlayer );
                        Thread.sleep ( 800 );
                        break;
                    case "4":
                        currentplayer.handlePlay ( sc, currentplayer );
                        break;

                    case "5":
                        currentplayer.handleRate ( sc, currentplayer );
                        break;

                    case "6":
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

    public static void main (String[] args) {
        Scanner sc = new Scanner ( System.in );
        System.out.println ( "============== Εγγραφή / Είσοδος Παίκτη ==========" );
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
        System.out.println ( "\nΕπιτυχής είσοδος! Καλωσήρθες, στο GoldenBet " + myPlayer.getUsername ( ) + "!!!" );
        startmenu ( myPlayer, currentplayer );
    }
}