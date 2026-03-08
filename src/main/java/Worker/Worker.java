package Worker;

import models.Game;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Random;

public class Worker {
    private HashMap<String, Game> myGames = new HashMap<> ( );
    private HashMap<String, BigDecimal> profits = new HashMap<> ( );

    public BigDecimal CalculateProfit (BigDecimal bet, Game game) { //edo mpainei h parametros ths genitrias
        BigDecimal playerPayout = BigDecimal.ZERO;
        BigDecimal systemProfitFromThisBet = BigDecimal.ZERO;
        int index;
        BigDecimal[] gamemultiplies;
        Random gen = new Random ( );
        int random = gen.nextInt ( 1000 ); // Βγάζει έναν ακέραιο από 0 έως 999
        try {
            if ( random % 100 == 0 )
                playerPayout = bet.multiply ( game.getJackpot ( ) );
            else {
                index = random % 10;
                gamemultiplies = game.getMultipliers ( );
                playerPayout = bet.multiply ( gamemultiplies[ index ] );
            }
            systemProfitFromThisBet = bet.subtract ( playerPayout );
            //edo thelei sigxronismso
        } catch (Exception e) {
            System.err.println ( "Σφάλμα κατά τον υπολογισμό κέρδους: " + e.getMessage ( ) );
        }

        return playerPayout;
    }
}
