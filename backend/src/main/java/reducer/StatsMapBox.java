package reducer;

import java.math.BigDecimal;
import java.util.HashMap;

public class StatsMapBox implements AggregationBox {
    private int receivedResponses;
    private final HashMap<String,BigDecimal> gamesProfits;

    public StatsMapBox(HashMap<String,BigDecimal> curGameProfits){

        this.receivedResponses = 1;
        this.gamesProfits = new HashMap<>(curGameProfits);
    }

    @Override
    public synchronized int getReceivedResponses() { return receivedResponses; }

    @Override
    public synchronized void addReceivedResponses() { receivedResponses++; }

    @Override
    public synchronized HashMap<String, BigDecimal> getGamesFromWorkers() { return gamesProfits; }

    public synchronized void addGamesFromWorker(Object incomingMap) {

        HashMap<String,BigDecimal> incomingProfitsMap = (HashMap<String, BigDecimal>) incomingMap;
        for (String key : incomingProfitsMap.keySet()) {
            BigDecimal newValue = incomingProfitsMap.get(key);

            if (gamesProfits.containsKey(key)) {
                BigDecimal oldValue = gamesProfits.get(key);
                gamesProfits.put(key, oldValue.add(newValue));
            } else {
                gamesProfits.put(key, newValue);
            }
        }
    }

}