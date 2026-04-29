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
    private BigDecimal tokenBalance;
    private Boolean projectIsActive;
    private HashMap<String, Boolean> gamePlayedStatus = new HashMap<> ( );
    private ArrayList<Game> searchedGames=new ArrayList<> ();
    private static PlayerApp instance;


    public static void initializeApp(Player player) {
        instance = new PlayerApp(player);
    }

    public static PlayerApp getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Προσοχή: Ο παίκτης δεν έχει συνδεθεί ή έχει κάνει logout!");
        }
        return instance;
    }


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


    public void search ( float minStars, float maxStars, String betLimit, String riskLevel) {

        if ( minStars < 1.0f || minStars > 5.0f || maxStars < 1.0f || maxStars > 5.0f ) {
            throw new IllegalArgumentException("Warning: Stars must be between 1.0 and 5.0.");
        }
        if ( minStars > maxStars ) {
            throw new IllegalArgumentException("Warning: The minimum star limit cannot be greater than the maximum.");
        }

        if ( betLimit != null && !betLimit.isEmpty ( ) ) {
            String[] limits = betLimit.split ( "," );
            for ( String limit : limits ) {
                if ( !limit.trim ( ).equals ( "$" ) && !limit.trim ( ).equals ( "$$" ) && !limit.trim ( ).equals ( "$$$" ) ) {
                    throw new IllegalArgumentException ( "Warning: Bet limits must be $, $$, or $$$." );
                }
            }
        }

        if ( riskLevel != null && !riskLevel.trim ( ).isEmpty ( ) ) {
            String[] risks = riskLevel.split ( "," );
            for ( String risk : risks ) {
                if ( !risk.trim ( ).equalsIgnoreCase ( "low" ) && !risk.trim ( ).equalsIgnoreCase ( "medium" ) && !risk.trim ( ).equalsIgnoreCase ( "high" ) ) {
                    throw new IllegalArgumentException ( "Warning: Risk level must be low, medium, or high." );
                }
            }
        }

        SearchFilters filters = new SearchFilters ( minStars, maxStars, betLimit, riskLevel );
        SearchClient searchClient = new SearchClient ( masterIp, masterPort, new MasterMessage ( MasterMessageType.SEARCH, filters ), this );
        searchClient.start ( );
    }


    public BigDecimal play (String gameName, BigDecimal amount) throws InterruptedException {
        BigDecimal payout=BigDecimal.ZERO;
        if ( gameName == null || gameName.trim ( ).isEmpty ( ) )
            throw new IllegalArgumentException ( "Warning: Invalid game name." );
        if ( amount.compareTo ( BigDecimal.ZERO ) <= 0 )
            throw new IllegalArgumentException ( "Warning: The bet cannot be negative or zero." );
        if ( amount.compareTo ( this.tokenBalance ) > 0 )
            throw new IllegalArgumentException ( "Warning: You don't have enough FUN." );

        Bet bet = new Bet ( amount, currentPlayer.getPlayerId(), gameName );
        Client playClient = new Client ( masterIp, masterPort, new MasterMessage ( MasterMessageType.PLAY, bet ) );
        playClient.start ( );
        playClient.join ();

        Object response = playClient.getResponse ( );
        System.out.println ( "\n============ Game Result ==============" );

        if ( response instanceof String )
            System.out.println ( response );
        else if ( response instanceof BigDecimal ) {
            changeGamePlayedStatus ( bet.getGameName( ) );
            payout = (BigDecimal) response;
            this.tokenBalance = this.tokenBalance.subtract ( amount );
            System.out.println ( "Deducted: " + bet.getAmount ( ) +" FUN" );

            if ( payout.compareTo ( BigDecimal.ZERO ) > 0 ) {
                System.out.println ( "Congratulations! You won " + payout + " FUN!" );
                addBalance ( payout );
            } else
                System.out.println ( "Unfortunately you didn't win this time. Good luck on your next bet!" );
        } else
            System.out.println ( "Warning: No response received from the Master." );

        return payout;
    }

    public void addBalance (BigDecimal amount) {
        if ( amount.compareTo ( BigDecimal.ZERO ) <= 0 )
            throw new IllegalArgumentException ( "Warning: The amount of FUN cannot be negative or zero." );
        this.tokenBalance = this.tokenBalance.add ( amount );
        System.out.println ( "Successful deposit!\nNew available balance: " + this.tokenBalance + " FUN." );
    }




    public void changeGamePlayedStatus (String gameName) {
        this.gamePlayedStatus.put ( gameName, true );
    }

    public void rate (String gameName, float stars) throws InterruptedException {
        if ( gameName == null || gameName.trim ( ).isEmpty ( ) )
            throw new IllegalArgumentException ( "Warning: Invalid game name." );
        if ( stars < 1.0f || stars > 5.0f )
            throw new IllegalArgumentException("Warning: Rating must be from 1.0 to 5.0, in half-star increments (e.g., 1.0, 1.5, 2.0, 2.5...).");
        if ( gamePlayedStatus.getOrDefault ( gameName, false ) ) {
            Rating rating = new Rating ( currentPlayer.getPlayerId ( ), gameName, stars );

            Client ratingClient = new Client ( masterIp, masterPort, new MasterMessage ( MasterMessageType.RATE, rating ) );
            ratingClient.start ( );
            ratingClient.join ();

            Object response = ratingClient.getResponse ( );
            if ( response instanceof String )
                System.out.println ( response );
            else if ( response instanceof Float )
                System.out.println ( "Successful rating! The new average rating for the game is: " + response + " stars!" );
            else
                System.out.println ( "Error: No response received from the Master." );
        } else {
            throw new IllegalArgumentException ( "Warning: You cannot rate the game " + gameName + " because you haven't played it yet!" );
        }
    }
    public static void regist(Player player) throws InterruptedException {
        Client registClient=new Client ( masterIp,masterPort,new MasterMessage ( MasterMessageType.REGIST,player ) );
        registClient.start ();
    }
    public static Player login(String username,String password) throws InterruptedException {
        String[] credentials = new String[]{username, password};
        Client loginClient = new Client ( masterIp, masterPort, new MasterMessage ( MasterMessageType.LOGIN, credentials ) );
        loginClient.start ( );
        loginClient.join ( );
        Object response = loginClient.getResponse ( );

        if ( response instanceof Player ) {
            return (Player) response;
        } else return null;

    }
    public void logout(String username,String password) throws InterruptedException {
        String[] credentials = new String[]{username, password};
        Client loginClient = new Client ( masterIp, masterPort, new MasterMessage ( MasterMessageType.LOGOUT, credentials ) );
        loginClient.start ( );
        loginClient.join ( );
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
            throw new IllegalArgumentException("Warning: The selected game is not in the current search results.");
        return contain;
    }


    private void projectavailablegames (Scanner sc, PlayerApp currentplayer, Player myPlayer) {
        projectIsActive = true;
        currentplayer.search (  1.0f,5.0f, "","" );
    }



    public static void main (String[] args) {

        // load servers configurations
        ConfigLoader.loadConfig("src/main/resources/system_network_config.conf");

        // Get Master's ip and port
        masterIp = ConfigLoader.getIp("MASTER");
        masterPort = ConfigLoader.getPort("MASTER");



    }
}