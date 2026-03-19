package reducer;

import models.Game;
import models.MasterMessage;
import models.MasterMessageType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

public class ReducerHandler extends Thread{

    private HashMap<Integer, AggregationBox> resultsBuffer;
    private Socket connection;


    public ReducerHandler(Socket connection, HashMap<Integer, AggregationBox> resultsBuffer){

        this.resultsBuffer = resultsBuffer;
        this.connection = connection;
    }


    @Override
    public void run(){

        int numberOfWorkers = 3;
        int currentMapId = -1;
        MasterMessage newMessage = null;


        try(ObjectInputStream in = new ObjectInputStream(connection.getInputStream())){

            MasterMessage message = (MasterMessage) in.readObject();
            MasterMessageType type = message.getType();

            switch (type){
                case SEARCH:
                    currentMapId = message.getMapId();

                    synchronized (resultsBuffer){
                        if(resultsBuffer.containsKey(currentMapId)){

                            AggregationBox currentBox = resultsBuffer.get(currentMapId);
                            currentBox.addGamesFromWorker((ArrayList<Game>)message.getPayload());
                            currentBox.addReceivedResponses();

                        }else{
                            AggregationBox newBox = new AggregationBox((ArrayList<Game>)message.getPayload());
                            resultsBuffer.put(currentMapId, newBox);
                        }
                        AggregationBox currentBox = resultsBuffer.get(currentMapId);
                        if(currentBox.getReceivedResponses() == numberOfWorkers){

                            ArrayList<Game> allGames = currentBox.getGamesFromWorkers();
                            newMessage = new MasterMessage(allGames, currentMapId);

                            resultsBuffer.remove(currentMapId);
                        }
                    }
                    if(newMessage != null){
                        sendGamesToMaster(newMessage);
                    }

                    break;

                case REMOVE_GAME:
                    currentMapId = message.getMapId();


                    synchronized (resultsBuffer){
                        if(resultsBuffer.containsKey(currentMapId)){

                            AggregationBox currentBox = resultsBuffer.get(currentMapId);
                            currentBox.addGamesFromWorker((ArrayList<Game>)message.getPayload());
                            currentBox.addReceivedResponses();

                        }else{
                            AggregationBox newBox = new AggregationBox((ArrayList<Game>)message.getPayload());
                            resultsBuffer.put(currentMapId, newBox);
                        }
                        AggregationBox currentBox = resultsBuffer.get(currentMapId);
                        if(currentBox.getReceivedResponses() == numberOfWorkers){

                            ArrayList<Game> allGames = currentBox.getGamesFromWorkers();
                            newMessage = new MasterMessage(allGames, currentMapId);

                            resultsBuffer.remove(currentMapId);
                        }
                    }

                    if(newMessage != null){
                        sendGamesToMaster(newMessage);
                    }

                    break;
                case GET_ACTIVE_GAMES:
                    currentMapId = message.getMapId();


                    synchronized (resultsBuffer){
                        if(resultsBuffer.containsKey(currentMapId)){

                            AggregationBox currentBox = resultsBuffer.get(currentMapId);
                            currentBox.addGamesFromWorker((ArrayList<Game>)message.getPayload());
                            currentBox.addReceivedResponses();

                        }else{
                            AggregationBox newBox = new AggregationBox((ArrayList<Game>)message.getPayload());
                            resultsBuffer.put(currentMapId, newBox);
                        }
                        AggregationBox currentBox = resultsBuffer.get(currentMapId);
                        if(currentBox.getReceivedResponses() == numberOfWorkers){

                            ArrayList<Game> allGames = currentBox.getGamesFromWorkers();
                            newMessage = new MasterMessage(allGames, currentMapId);

                            resultsBuffer.remove(currentMapId);
                        }
                    }

                    if(newMessage != null){
                        sendGamesToMaster(newMessage);
                    }
                    break;
                case STATS:
                    break;
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


    private void sendGamesToMaster(MasterMessage message){

        try(Socket s = new Socket("127.0.0.1", 43211);
            ObjectOutputStream outMaster = new ObjectOutputStream(s.getOutputStream());){

            outMaster.writeObject(message);
            outMaster.flush();
            System.out.println("Sending final result back to Master completed successfully");

        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
