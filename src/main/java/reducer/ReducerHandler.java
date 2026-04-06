package reducer;

import models.Game;
import models.MasterMessage;
import models.MasterMessageType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import static reducer.Reducer.numberOfWorkers;

public class ReducerHandler extends Thread{

    private final HashMap<Integer, AggregationBox> resultsBuffer;
    private final Socket connection;

    private final String masterIp;
    private final int masterPort;


    public ReducerHandler(Socket connection, HashMap<Integer, AggregationBox> resultsBuffer,
                          String masterIp, int masterPort){

        this.resultsBuffer = resultsBuffer;
        this.connection = connection;
        this.masterIp = masterIp;
        this.masterPort = masterPort;
    }


    @Override
    public void run(){

        try(ObjectInputStream in = new ObjectInputStream(connection.getInputStream())){

            MasterMessage message = (MasterMessage) in.readObject();

            if(message.getType() != MasterMessageType.WORKER_FAILURE)
                processAggregation(message);
            else{

                synchronized (numberOfWorkers){
                    numberOfWorkers--;
                }
                System.out.println("The number of workers decreased to " +  numberOfWorkers +
                        " after the failure of worker " + (Integer) message.getPayload());

            }


        }catch (Exception e) {
            System.err.println("Σφάλμα κατά την ανάγνωση από Worker: " + e.getMessage());
        } finally {
            try {
                connection.close();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }


    private void processAggregation(MasterMessage message){

        int currentMapId = message.getMapId();
        MasterMessageType type = message.getType();
        MasterMessage newMessage = null;

        synchronized(resultsBuffer){
            AggregationBox currentBox;

            if (resultsBuffer.containsKey(currentMapId)) {

                currentBox = resultsBuffer.get(currentMapId);
                currentBox.addGamesFromWorker(message.getPayload());
                currentBox.addReceivedResponses();

            }else{

                switch (type){
                    case PROVIDER_GAME_PROFITS,GAMES_PROFITS,PLAYERS_PROFITS:
                        currentBox = new StatsMapBox((HashMap<String, BigDecimal>) message.getPayload());
                        break;
                    default:
                        currentBox = new GameListBox((ArrayList<Game>) message.getPayload());
                        break;
                }

                resultsBuffer.put(currentMapId, currentBox);
            }

            synchronized (numberOfWorkers) {
                if (currentBox.getReceivedResponses() == numberOfWorkers) {

                    Object finalPayload = currentBox.getGamesFromWorkers();
                    newMessage = new MasterMessage(finalPayload, currentMapId);

                    resultsBuffer.remove(currentMapId);
                }
            }
        }

        if (newMessage != null) {
            sendGamesToMaster(newMessage);
        }
    }


    private void sendGamesToMaster(MasterMessage message){

        try(Socket s = new Socket(masterIp, masterPort);
            ObjectOutputStream outMaster = new ObjectOutputStream(s.getOutputStream());){

            outMaster.writeObject(message);
            outMaster.flush();
            System.out.println("Sending final result back to Master completed successfully");

        }catch (Exception e){
            System.err.println("Σφάλμα αποστολής στον Master: " + e.getMessage());
        }
    }

}
