package Worker;

import models.Bet;
import models.Game;
import models.Rating;
import models.SearchFilters;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class WorkerHandler extends Thread {
    private HashMap<String, Game> myGames = new HashMap<> ( );
    private HashMap<String, BigDecimal> profits = new HashMap<> ( );
    private HashMap<String, BigDecimal> playerProfits = new HashMap<> ( );


    @Override
    public void run ( ) {
        CalculateProfit ( null );
    }

    private BigDecimal CalculateProfit (Bet bet) { //edo mpainei h parametros ths genitrias
        BigDecimal playerProfit, systemProfitFromThisBet, playerPayout = BigDecimal.ZERO;
        int index;
        BigDecimal[] gamemultiplies;
        Random gen = new Random ( );
        int random = gen.nextInt ( 1000 ); // Βγάζει έναν ακέραιο από 0 έως 999
        Game game = myGames.get ( bet.getGamename ( ) );
        try {
            if ( random % 100 == 0 )
                playerPayout = bet.getAmount ( ).multiply ( game.getJackpot ( ) );
            else {
                index = random % 10;
                gamemultiplies = game.getMultipliers ( );
                playerPayout = bet.getAmount ( ).multiply ( gamemultiplies[ index ] );
            }
            systemProfitFromThisBet = bet.getAmount ( ).subtract ( playerPayout );
            playerProfit = playerPayout.subtract ( bet.getAmount ( ) );

            synchronized (profits) {
                BigDecimal currentProfit = profits.getOrDefault ( game.getGameName ( ), BigDecimal.ZERO );
                profits.put ( game.getGameName ( ), currentProfit.add ( systemProfitFromThisBet ) );
            }
        } catch (Exception e) {
            System.err.println ( "Σφάλμα κατά τον υπολογισμό κέρδους: " + e.getMessage ( ) );
        }
        return playerPayout;
    }

    private ArrayList<Game> searchgames (SearchFilters filters) {
        ArrayList<Game> matchedGames = new ArrayList<> ( );

        for ( Game game : myGames.values ( ) ) {
            boolean matching = true;

            if ( game.getStars ( ) < filters.getStars ( ) )
                matching = false;
            if ( !game.getRiskLevel ( ).equalsIgnoreCase ( filters.getRiskLevel ( ) ) )
                matching = false;
            if ( !game.getBetCategory ( ).equals ( filters.getBetLimit ( ) ) )
                matching = false;
            if ( !filters.getCategories ( ).contains ( game.getCategory ( ) ) )
                matching = false;

            if ( matching )
                matchedGames.add ( game );
        }
        return matchedGames;
    }

    private int averageRate (Rating rating) {
        int average;
        Game game = myGames.get ( rating.getGameName ( ) );
        if ( game != null ) {
            int currentStars = game.getStars ( );
            int currentVotes = game.getVotes ( );
            int newStarRating = rating.getStars ( );
            int totalstars = (currentStars * currentVotes) + newStarRating;
            int newVotes = currentVotes + 1;
            average = totalstars / newVotes;
            game.setStars ( totalstars );
            game.setVotes ( newVotes );
            return average;
        } else {
            System.err.println ( "Το παιχνίδι για βαθμολογία δεν βρέθηκε σε αυτόν τον Worker." );
            return 0;

        }

    }
}