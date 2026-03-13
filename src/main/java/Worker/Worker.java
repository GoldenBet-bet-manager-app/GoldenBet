package Worker;

import models.Bet;
import models.Game;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Random;

public class Worker {
    private final int workerid;
    private HashMap<String, Game> myGames = new HashMap<> ( );
    private HashMap<String, BigDecimal> profits = new HashMap<> ( );
    private HashMap<String, BigDecimal> playerProfits = new HashMap<>();

    public Worker (int workerid) {
        this.workerid = workerid;
    }

    public BigDecimal CalculateProfit (Bet bet/*kathe bet einai ena thread*/, Game game) { //edo mpainei h parametros ths genitrias
        BigDecimal playerProfit,systemProfitFromThisBet = BigDecimal.ZERO;
        BigDecimal playerPayout=BigDecimal.ZERO;
        int index;
        BigDecimal[] gamemultiplies;
        Random gen = new Random ( );
        int random = gen.nextInt ( 1000 ); // Βγάζει έναν ακέραιο από 0 έως 999
        try {
            if ( random % 100 == 0 )
                playerPayout = bet.getAmount ().multiply ( game.getJackpot ( ) );
            else {
                index = random % 10;
                gamemultiplies = game.getMultipliers ( );
                playerPayout = bet.getAmount ().multiply(gamemultiplies[index]);
            }
            systemProfitFromThisBet = bet.getAmount ().subtract ( playerPayout );
            playerProfit=playerPayout.subtract ( bet.getAmount () );

            synchronized (profits) {
                BigDecimal currentProfit = profits.getOrDefault(game.getGameName(), BigDecimal.ZERO);
                profits.put(game.getGameName (),currentProfit.add(systemProfitFromThisBet));
            }
        } catch (Exception e) {
            System.err.println ( "Σφάλμα κατά τον υπολογισμό κέρδους: " + e.getMessage ( ) );
        }
        return playerPayout;
    }
}
