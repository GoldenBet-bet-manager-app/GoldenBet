package gr.goldenbet.domain;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import gr.goldenbet.utils.ConfigLoader;


public class PlayerApp {

    // static variables for network communication
    private static String masterIp;
    private static int masterPort;

    private Player currentPlayer;
    private BigDecimal balance;
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
        this.balance = BigDecimal.ZERO;
    }

    public BigDecimal getBalance ( ) {
        return balance;
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

        if ( betLimit != null && !betLimit.isEmpty ( ) ) {
            String[] limits = betLimit.split ( "," );
            for ( String limit : limits ) {
                if ( !limit.trim ( ).equals ( "$" ) && !limit.trim ( ).equals ( "$$" ) && !limit.trim ( ).equals ( "$$$" ) ) {
                    throw new IllegalArgumentException ( "Προσοχή: Τα όρια πονταρίσματος πρέπει να είναι $, $$ ή $$$." );
                }
            }
        }

        if ( riskLevel != null && !riskLevel.trim ( ).isEmpty ( ) ) {
            String[] risks = riskLevel.split ( "," );
            for ( String risk : risks ) {
                if ( !risk.trim ( ).equalsIgnoreCase ( "low" ) && !risk.trim ( ).equalsIgnoreCase ( "medium" ) && !risk.trim ( ).equalsIgnoreCase ( "high" ) ) {
                    throw new IllegalArgumentException ( "Προσοχή: Το επίπεδο ρίσκου πρέπει να είναι low, medium ή high." );
                }
            }
        }

        SearchFilters filters = new SearchFilters ( playerId, minStars, maxStars, betLimit, riskLevel );
        SearchClient searchClient = new SearchClient ( masterIp, masterPort, new MasterMessage ( MasterMessageType.SEARCH, filters ), this );
        searchClient.start ( );
    }


    public BigDecimal play (String gameName, BigDecimal amount) throws InterruptedException {
        BigDecimal payout=BigDecimal.ZERO;
        if ( gameName == null || gameName.trim ( ).isEmpty ( ) )
            throw new IllegalArgumentException ( "Προσοχή: Μη έγκυρο όνομα παιχνιδιού" );
        if ( amount.compareTo ( BigDecimal.ZERO ) <= 0 )
            throw new IllegalArgumentException ( "Προσοχή: Το ποντάρισμα δεν μπορεί να είναι αρνητικό ή μηδέν" );
        if ( amount.compareTo ( this.balance ) > 0 )
            throw new IllegalArgumentException ( "Προσοχή: Δεν έχετε αρκετά FUN." );

        Bet bet = new Bet ( amount, currentPlayer.getPlayerId(), gameName );
        Client playClient = new Client ( masterIp, masterPort, new MasterMessage ( MasterMessageType.PLAY, bet ) );
        playClient.start ( );
        playClient.join ();

        Object response = playClient.getResponse ( );
        System.out.println ( "\n============ Αποτέλεσμα Παιχνιδιού ==============" );

        if ( response instanceof String )
            System.out.println ( response );
        else if ( response instanceof BigDecimal ) {
            changeGamePlayedStatus ( bet.getGameName( ) );
            payout = (BigDecimal) response;
            this.balance = this.balance.subtract ( amount );
            System.out.println ( "Αφαιρέθηκαν: " + bet.getAmount ( ) +" FUN" );

            if ( payout.compareTo ( BigDecimal.ZERO ) > 0 ) {
                System.out.println ( "Συγχαρητήρια! Κερδίσατε " + payout + " FUN!" );
                addBalance ( payout );
            } else
                System.out.println ( "Δυστυχώς δεν κερδίσατε αυτή τη φορά. Καλή τύχη στο επόμενο ποντάρισμα!" );
        } else
            System.out.println ( "Προσοχή: Δεν λάβαμε απάντηση από τον Master." );

        return payout;
    }

    public void addBalance (BigDecimal amount) {
        if ( amount.compareTo ( BigDecimal.ZERO ) <= 0 )
            throw new IllegalArgumentException ( "Προσοχή: Το πλήθος των FUN δεν μπορεί να είναι αρνητικό ή μηδέν" );
        this.balance = this.balance.add ( amount );
        System.out.println ( "Επιτυχής κατάθεση!\nΝέο διαθέσιμο υπόλοιπο: " + this.balance + " FUN." );
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

            Client ratingClient = new Client ( masterIp, masterPort, new MasterMessage ( MasterMessageType.RATE, rating ) );
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
            throw new IllegalArgumentException ( "Προσοχή: Δεν μπορείτε να βαθμολογήσετε το παιχνίδι " + gameName + " γιατί δεν το έχετε παίξει ακόμα!" );
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


    private void projectavailablegames ( PlayerApp currentplayer, Player myPlayer) {
        projectIsActive = true;
        currentplayer.search ( myPlayer.getPlayerId ( ), 1.0f,5.0f, "","" );
    }



}