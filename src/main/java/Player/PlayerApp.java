package Player;

import Client.Client;
import Client.PlayClient;
import Client.SearchClient;
import models.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import Client.RatingClient;

public class PlayerApp {
    private Player currentPlayer;
    private BigDecimal tokenBalance;
    private  Boolean projectisactive;
    private HashMap<String, Boolean> gamePlayedStatus =new HashMap<>();// true an exeis paikei esto kai mia fora ena paixnidi paixnidi

    public synchronized  Boolean getProjectisactive ( ) {
        return projectisactive;
    }

    public PlayerApp(Player currentPlayer) {
        this.currentPlayer = currentPlayer;
        this.tokenBalance = BigDecimal.ZERO;
    }

    public BigDecimal getTokenBalance() {
        return tokenBalance;
    }

    public void setTokenBalance(BigDecimal tokenBalance) {
        this.tokenBalance = tokenBalance;
    }


    public void search(String playerId, ArrayList<String> categories, int stars, String betLimit, String riskLevel) {
        SearchFilters filters = new SearchFilters(playerId, categories, stars, betLimit, riskLevel);
        SearchClient searchClient = new SearchClient("127.0.0.1", 43210, new MasterMessage(MasterMessageType.SEARCH, filters),this);
        searchClient.start();
    }


    public void play(String gameName, BigDecimal amount) {
        if (gameName == null || gameName.trim().isEmpty())
            throw new IllegalArgumentException("Σφάλμα: Μη έγκυρο όνομα παιχνιδιού");
        if (amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Σφάλμα: Το ποντάρισμα δεν μπορεί να είναι αρνητικό ή μηδέν");
        if (amount.compareTo(this.tokenBalance) > 0)
            throw new IllegalArgumentException("Σφάλμα: Δεν έχετε αρκετά tokens.");

        Bet bet = new Bet(amount, currentPlayer.getName(), gameName);
        PlayClient playClient = new PlayClient("127.0.0.1", 43210, new MasterMessage(MasterMessageType.PLAY, bet),this);
        playClient.start();
    }

    public void addBalance(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Σφάλμα: Το πλήθος των tokens δεν μπορεί να είναι αρνητικό ή μηδέν");
        this.tokenBalance = this.tokenBalance.add(amount);
        System.out.println("Επιτυχής κατάθεση!\nΝέο διαθέσιμο υπόλοιπο: " + this.tokenBalance + " tokens.");
    }
    public void subBalance(BigDecimal amount){
        if (amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Σφάλμα: Το πλήθος των tokens δεν μπορεί να είναι αρνητικό ή μηδέν");
        this.tokenBalance = this.tokenBalance.subtract (amount);
    }

    public void changeGamePlayedStatus(String gameName){
        this.gamePlayedStatus.put ( gameName,true );
    }

    public void rate(String gameName, int stars) {
        if ( gameName == null || gameName.trim ( ).isEmpty ( ) )
            throw new IllegalArgumentException ( "Σφάλμα: Μη έγκυρο όνομα παιχνιδιού" );
        if ( stars < 1 || stars > 5 )
            throw new IllegalArgumentException ( "Σφάλμα: Η βαθμολογία του κάθε παιχνιδιού πρέπει να είναι από 1 έως 5 αστέρια." );
        if ( gamePlayedStatus.getOrDefault ( gameName ,false) ) {
            Rating rating = new Rating ( currentPlayer.getPlayerId ( ), gameName, stars );
            RatingClient ratingClient = new RatingClient ( "127.0.0.1", 43210, new MasterMessage ( MasterMessageType.RATE, rating ) );
            ratingClient.start ( );
        }else{
            throw new IllegalArgumentException ( "Σφάλμα: Δεν μπορείτε να βαθμολογήσετε το παιχνίδι '" + gameName + "' γιατί δεν το έχετε παίξει ακόμα!" );
        }
    }


    // =========================================================================================
    // STATIC METHODS ΓΙΑ ΤΟ ΜΕΝΟΥ
    // =========================================================================================

    private  void handleDeposit(Scanner sc, PlayerApp currentplayer) {
        System.out.print("Πληκτρολογήστε ποσό κατάθεσης: ");
        String depositAmount = sc.nextLine();
        currentplayer.addBalance(new BigDecimal(Integer.parseInt(depositAmount)));
    }

    private  void handlePlay(Scanner sc, PlayerApp currentplayer) {
        System.out.print("Πληκτρολογήστε το όνομα του παιχνιδιού: ");
        String gameToPlay = sc.nextLine();
        System.out.print("Πληκτρολογήστε το ποσό του πονταρίσματος: ");
        String betAmount = sc.nextLine();
        currentplayer.play(gameToPlay, new BigDecimal(Integer.parseInt(betAmount)));
    }

    private  void handleRate(Scanner sc, PlayerApp currentplayer) {
        System.out.print("Πληκτρολογήστε το όνομα του παιχνιδιού που θέλετε να βαθμολογήσετε: ");
        String gameToRate = sc.nextLine();
        System.out.print("Δώστε βαθμολογία (1 έως 5 αστέρια): ");
        int starsRating = Integer.parseInt(sc.nextLine());
        currentplayer.rate(gameToRate, starsRating);
    }

    private  void addsearchfilters(Scanner sc, PlayerApp currentplayer, Player myPlayer) {
        projectisactive=false;
        System.out.println("\n--- Κριτήρια Αναζήτησης ---");
        System.out.print("Εισάγετε κατηγορίες (π.χ. Cards,Slots) ή πατήστε Enter για να μην βάλετε φίλτρο: ");
        String catInput = sc.nextLine();
        ArrayList<String> searchCategories = new ArrayList<>();

        if (!catInput.trim().isEmpty()) {
            String[] cats = catInput.split(",");
            for (String c : cats) {
                searchCategories.add(c.trim());
            }
        }

        System.out.print("Ελάχιστα αστέρια (1-5) ή πατήστε 0 για κανένα φίλτρο: ");
        int searchStars = 0;
        try {
            searchStars = Integer.parseInt(sc.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Δεν δώσατε έγκυρο αριθμό. Το φίλτρο αστεριών δεν θα εφαρμοστεί.");
        }

        System.out.println("\n--- Όρια Πονταρίσματος ---");
        System.out.println("Ελάχιστο ποντάρισμα 0.1 FUN -> $");
        System.out.println("Ελάχιστο ποντάρισμα 1 FUN -> $$");
        System.out.println("Ελάχιστο ποντάρισμα 5 FUN -> $$$");
        System.out.print("Όριο Πονταρίσματος ($, $$, $$$) ή πατήστε Enter για κανένα φίλτρο: ");
        String searchBetLimit = sc.nextLine().trim();

        System.out.print("Επίπεδο Ρίσκου (low, medium, high) ή πατήστε Enter για κανένα φίλτρο: ");
        String searchRiskLevel = sc.nextLine().trim();

        currentplayer.search(myPlayer.getPlayerId(), searchCategories, searchStars, searchBetLimit, searchRiskLevel);
    }
    private  void projectavailablegames (Scanner sc, PlayerApp currentplayer, Player myPlayer) {
        projectisactive=true;
        currentplayer.search ( myPlayer.getPlayerId (),new ArrayList<String> (),0,"","" );
    }

    // =========================================================================================
    // MAIN
    // =========================================================================================

    public static void main (String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("============ Εγγραφή / Είσοδος Παίκτη ========");
        System.out.print("Όνομα: ");
        String name = sc.nextLine();
        System.out.print("Επίθετο: ");
        String surname = sc.nextLine();
        System.out.print("Username: ");
        String username = sc.nextLine();
        System.out.print("Password: ");
        String password = sc.nextLine();
        System.out.print("Player ID: ");
        String id = sc.nextLine();

        Player myPlayer = new Player(name, surname, username, password, id);
        PlayerApp currentplayer = new PlayerApp(myPlayer);

        // ================== ΔΙΑΔΡΑΣΤΙΚΟ ΜΕΝΟΥ ==================
        boolean running = true;
        while (running) {
            System.out.println("\n============= ΜΕΝΟΥ ΕΠΙΛΟΓΩΝ =============");
            System.out.println("Υπόλοιπο: " + currentplayer.getTokenBalance() + " FUN");
            System.out.println("1. Προσθήκη Υπολοίπου (Κατάθεση)");
            System.out.println("2. Αναζήτηση Παιχνιδιών (Βάσει Κριτηρίων)");
            System.out.println("3. Προβολή διαθέσιμων Παιχνιδιών");
            System.out.println("4. Παίξε Παιχνίδι");
            System.out.println("5. Βαθμολόγησε Παιχνίδι");
            System.out.println("6. Αποσύνδεση");
            System.out.print("Επιλέξτε ενέργεια (1-5): ");

            String choice = sc.nextLine();

            try {
                switch (choice) {
                    case "1":
                        currentplayer.handleDeposit(sc, currentplayer);
                        break;

                    case "2":
                        currentplayer.addsearchfilters(sc, currentplayer, myPlayer);
                        Thread.sleep(1500);
                        break;
                    case "3":
                        currentplayer.projectavailablegames ( sc,currentplayer,myPlayer );
                        Thread.sleep(1500);
                        break;
                    case "4":
                        currentplayer.handlePlay(sc, currentplayer);
                        Thread.sleep(1500);
                        break;

                    case "5":
                        currentplayer.handleRate(sc, currentplayer);
                        Thread.sleep(1500);
                        break;

                    case "6":
                        System.out.println("Αποσύνδεση παίκτη... Ευχαριστούμε που παίξατε!");
                        running = false;
                        break;

                    default:
                        System.out.println("Μη έγκυρη επιλογή! Παρακαλώ εισάγετε έναν αριθμό από το 1 έως το 5.");
                }
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            } catch (Exception e) {
                System.out.println("Σφάλμα στην εισαγωγή στοιχείων. Προσπαθήστε ξανά.");
            }
        }
    }


}