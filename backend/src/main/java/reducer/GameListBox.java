package reducer;

import gr.goldenbet.domain.Game;

import java.util.ArrayList;

public class GameListBox implements AggregationBox{

    private int receivedResponses;
    private final ArrayList<Game> gamesFromWorkers;

    public GameListBox(ArrayList<Game> gamesFromWorker){

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

    public synchronized void addGamesFromWorker(Object gamesFromWorker){
            gamesFromWorkers.addAll((ArrayList<Game>) gamesFromWorker);
    }
}
