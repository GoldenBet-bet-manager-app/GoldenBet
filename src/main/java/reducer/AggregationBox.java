package reducer;

import models.Game;

import java.util.ArrayList;

public class AggregationBox {

    private int receivedResponses;
    private final ArrayList<Game> gamesFromWorkers;

    public AggregationBox(ArrayList<Game> gamesFromWorker){

        this.receivedResponses = 1;
        this.gamesFromWorkers = new ArrayList<>(gamesFromWorker);
    }

    public synchronized int getReceivedResponses() {
        return receivedResponses;
    }

    public synchronized void addReceivedResponses(){
        receivedResponses ++;
    }

    public ArrayList<Game> getGamesFromWorkers() {
        return gamesFromWorkers;
    }

    public synchronized void addGamesFromWorker(ArrayList<Game> gamesFromWorker){
        gamesFromWorkers.addAll(gamesFromWorker);
    }
}
