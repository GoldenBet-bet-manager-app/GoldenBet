package Player;

import Client.PlayClient;
import Client.SearchClient;
import models.Bet;
import models.Player;
import models.Rating;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Scanner;
import Client.RatingClient;
import models.SearchFilters;

public class PlayerApp {
    private Player currentPlayer;
    private BigDecimal tokenBalance;

    public PlayerApp (Player currentPlayer ) {
        this.currentPlayer = currentPlayer;
        this.tokenBalance=BigDecimal.ZERO;
    }

    public BigDecimal getTokenBalance ( ) {
        return tokenBalance;
    }

    public void setTokenBalance (BigDecimal tokenBalance) {
        this.tokenBalance = tokenBalance;
    }


    public void search(String playerId, ArrayList<String> categories, int stars, String betLimit, String riskLevel){
        SearchFilters filters = new SearchFilters (playerId, categories, stars, betLimit, riskLevel);
        SearchClient searchClient=new SearchClient ( "127.0.0.1",4321,filters );
        searchClient.start ();
        System.out.println("Αποστολή αιτήματος αναζήτησης στον Master... Παρακαλώ περιμένετε τα αποτελέσματα.");
    }


    public void play(String gameName, BigDecimal amount){
        if(amount.compareTo(BigDecimal.ZERO)<=0)
            throw new IllegalArgumentException("Σφάλμα: Το πονάρισμα δεν μπορεί να είναι αρνητικός ή το μηδέν");
        if (amount.compareTo(this.tokenBalance) > 0)
            throw new IllegalArgumentException("Σφάλμα: Δεν έχετε αρκετά tokens.");
        this.tokenBalance=this.tokenBalance.subtract ( amount );
        Bet bet=new Bet ( amount, currentPlayer.getName ( ), gameName);
        PlayClient playClient=new PlayClient ( "127.0.0.1",4321,bet );
        playClient.start();
        System.out.println("Το ποντάρισμα για το παιχνίδι " + gameName + " στάλθηκε...");
    }
    public void addBalance(BigDecimal amount){
        if(amount.compareTo(BigDecimal.ZERO)<=0)
            throw new IllegalArgumentException("Σφάλμα: Το πλήθος των tokens δεν μπορεί να είναι αρνητικός ή το μηδέν");
       this.tokenBalance= this.tokenBalance.add ( amount );
        System.out.println("Επιτυχής κατάθεση!\nΝέο διαθέσιμο υπόλοιπο: " + this.tokenBalance + " tokens.");
    }

    public void rate(String gameName,int stars) {
        if ( stars < 1 || stars > 5 )
            throw new IllegalArgumentException ( "Σφάλμα: Η βαθμολογία του κάθε παιχνιδιού πρέπει να είναι από 1 έως 5 αστέρια." );

        System.out.println ( "Αποστολή βαθμολογίας (" + stars + " αστέρια) για το " + gameName + "..." );
        Rating rating=new Rating ( currentPlayer.getPlayerId ( ), gameName,stars );
        RatingClient ratingClient=new RatingClient("127.0.0.1",4321,rating);
        ratingClient.start ();
        }



    public static  void main(String [] args){
        Scanner sc=new Scanner ( System.in );
        //EDO MALLON THA XREIASTO LISTA ME TOYS PLAYERS OSTE NA KSERO AN PREPEI NA GINETAI EGGRAFH H EISODOS
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

        Player myPlayer=new Player ( name,surname,username,password,id );
        PlayerApp currentplayer=new PlayerApp (myPlayer);

        ArrayList<String> myCategories = new ArrayList<>();
        myCategories.add("Cards");
        myCategories.add("Slots");

        System.out.print("Πόσα tokens θέλετε να προσθέσετε: ");
        String ammount = sc.nextLine();
        currentplayer.addBalance ( new BigDecimal ( Integer.parseInt (ammount) ) );
        currentplayer.search ( myPlayer.getPlayerId ( ),myCategories ,4,"$$","medium");
    }
}
