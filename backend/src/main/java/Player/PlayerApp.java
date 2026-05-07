package Player;

import Client.Client;
import Client.SearchClient;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import Models.*;
import utils.ConfigLoader;


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


    public void search (String playerId, float minStars, float maxStars, String betLimit, String riskLevel) {
        if ( playerId == null || playerId.trim().isEmpty() ) {
            throw new IllegalArgumentException("Warning: The Player ID cannot be empty.");
        }
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

        SearchFilters filters = new SearchFilters ( playerId, minStars, maxStars, betLimit, riskLevel );
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

        Bet bet = new Bet ( amount, currentPlayer.getSurname ( ),currentPlayer.getPlayerId(), gameName );
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
            Rating rating = new Rating ( gameName, stars );

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
    // =========================================================================================
    // STATIC METHODS ΓΙΑ ΤΟ ΜΕΝΟΥ
    // =========================================================================================

    private void handleDeposit (Scanner sc, PlayerApp currentplayer) {
        System.out.print ( "Enter deposit amount: " );
        String depositAmount = sc.nextLine ( );
        currentplayer.addBalance ( new BigDecimal ( Integer.parseInt ( depositAmount ) ) );
    }

    private void handlePlay (Scanner sc, PlayerApp currentplayer) throws InterruptedException {
        ArrayList<Game> availableGames = searchedGames;
        if ( availableGames == null || availableGames.isEmpty ( ) ) {
            throw new IllegalArgumentException ( "No available games! Please perform a Search (Option 2) or View Games (Option 3) first." );
        }

        System.out.print ( "Enter the name of the game: " );
        String gameToPlay = sc.nextLine ( );
        isavailableSelectedgame(gameToPlay,availableGames);
        System.out.print ( "Enter the bet amount: " );
        String betAmount = sc.nextLine ( );
        currentplayer.play ( gameToPlay, new BigDecimal (  betAmount  ) );
    }


    private void handleRate (Scanner sc, PlayerApp currentplayer) throws InterruptedException {
        System.out.print ( "Enter the name of the game you want to rate: " );
        String gameToRate = sc.nextLine ( );
        System.out.print ( "Enter a rating from 1.0 to 5.0, in half-star increments (e.g., 1.0, 1.5, 2.0, 2.5...): ");
        float starsRating = Float.parseFloat ( sc.nextLine ( ) );
        currentplayer.rate ( gameToRate, starsRating );
    }

    private void addsearchfilters (Scanner sc, PlayerApp currentplayer, Player myPlayer) {
        projectIsActive = false;
        System.out.println ( "\n--- Search Criteria ---" );
        float minSearchStars = 1.0f;
        float maxSearchStars = 5.0f;

        System.out.print ( "Enter Stars (e.g., 4) or Range (e.g., 3-4.5) or press Enter for no filter: " );
        String searchStars = sc.nextLine ( ).trim ( );

        if ( !searchStars.isEmpty ( ) ) {
            try {
                if ( searchStars.contains ( "-" ) ) {
                    String[] parts = searchStars.split ( "-" );
                    minSearchStars = Float.parseFloat ( parts[ 0 ].trim ( ) );
                    maxSearchStars = Float.parseFloat ( parts[ 1 ].trim ( ) );
                } else {
                    minSearchStars = Float.parseFloat ( searchStars );
                    maxSearchStars = Float.parseFloat ( searchStars );
                }
            } catch (NumberFormatException e) {
                System.out.println ( "Error: Invalid number format. Please enter numbers (e.g., '4' or '3.5-5').\n" );
            }
        }

        System.out.println ( "\n--- Bet Limits ---" );
        System.out.println ( "Minimum bet 0.1 FUN -> $" );
        System.out.println ( "Minimum bet 1 FUN -> $$" );
        System.out.println ( "Minimum bet 5 FUN -> $$$" );
        System.out.print ( "Bet Limit ($, $$, $$$) [Separate with commas for multiple, e.g., $, $$] or press Enter: " );
        String searchBetLimit = sc.nextLine ( ).trim ( );

        System.out.print ( "Risk Level (low, medium, high) [Separate with commas for multiple, e.g., low, medium] or press Enter: " );
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
            System.out.println ( "\n============= MAIN MENU =============" );
            System.out.println ( "Balance: " + currentplayer.getTokenBalance ( ) + " FUN" );
            System.out.println ( "1. Add Balance (Deposit)" );
            System.out.println ( "2. Search Games (By Criteria)" );
            System.out.println ( "3. View Available Games" );
            System.out.println ( "4. Play Game" );
            System.out.println ( "5. Rate Game" );
            System.out.println ( "6. Logout" );
            System.out.print ( "Select an action (1-6): " );

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
                        System.out.println ( "Logging out player... Thank you for playing!" );
                        running = false;
                        break;
                    default:
                        System.out.println ( "Invalid choice! Please enter a number from 1 to 6." );
                }
            } catch (IllegalArgumentException e) {
                System.out.println ( e.getMessage ( ) );
            } catch (Exception e) {
                System.out.println ( "Error in input. Please try again." );
            }
        }
    }

    public static void main (String[] args) {

        // load servers configurations
        ConfigLoader.loadConfig("src/main/resources/system_network_config.conf");

        // Get Master's ip and port
        masterIp = ConfigLoader.getIp("MASTER");
        masterPort = ConfigLoader.getPort("MASTER");


        Scanner sc = new Scanner ( System.in );
        System.out.println ( "============== Player Registration / Login ==========" );
        System.out.print ( "Name: " );
        String name = sc.nextLine ( );
        System.out.print ( "Surname: " );
        String surname = sc.nextLine ( );
        System.out.print ( "Username: " );
        String username = sc.nextLine ( );
        System.out.print ( "Password: " );
        String password = sc.nextLine ( );
        Player myPlayer = new Player ( name, surname, username, password);
        PlayerApp currentplayer = new PlayerApp ( myPlayer );
        System.out.println ( "\nSuccessful login! Welcome to GoldenBet, " + myPlayer.getUsername ( ) + "!!!" );
        startmenu ( myPlayer, currentplayer );
    }
}