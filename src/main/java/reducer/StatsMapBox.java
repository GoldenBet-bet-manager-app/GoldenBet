package reducer;

import models.Game;

import java.math.BigDecimal;
import java.util.ArrayList;
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

    public synchronized void addGamesFromWorker(Object curGameProfits) {
        gamesProfits.putAll((HashMap<String, BigDecimal>) curGameProfits);
    }
}