package Master;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import Worker.Worker;
import models.MasterMessage;
import models.Player;
import models.WorkerInfo;
import utils.ConfigLoader;

public class Master{

    // static variables for network communication
    private static int masterPort;
    private static int reducerListenerPort;

    public static void main(String []args) {

        ConfigLoader.loadConfig("src/main/resources/system_network_config.conf");

        masterPort = ConfigLoader.getPort("MASTER");
        reducerListenerPort = ConfigLoader.getPort("MASTER_REDUCER_LISTENER");


        new Master().openServer();
    }

    final ArrayList<WorkerInfo> workersList = new ArrayList<>();
    final HashMap<Integer, ResultBox> sharedResults = new HashMap<>();
    final HashMap<String, Player> registeredPlayers = new HashMap<>();

    void openServer() {

        ArrayList<WorkerInfo> loadedWorkers = ConfigLoader.getAllWorkers();
        if (loadedWorkers != null) {
            this.workersList.addAll(loadedWorkers);
        }


        startReducerListener(reducerListenerPort);

        try(ServerSocket providerSocket = new ServerSocket(masterPort, 50);){

            System.out.println("Listening for Clients on port 43210...");
            while(true) {

                Socket connection = providerSocket.accept();
                Thread t = new MasterHandler(connection, workersList , sharedResults,registeredPlayers);
                t.start();
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }


    private void startReducerListener(int port){

        new Thread(()-> {
            try(ServerSocket reducerSocket = new ServerSocket(port)) {

                System.out.println("Master is listening for Reducer results on port " + port + "...");
                while(true){

                    Socket reducerConnection = reducerSocket.accept();
                    try(ObjectInputStream in = new ObjectInputStream(reducerConnection.getInputStream()) ;){

                        MasterMessage incomingMessage = (MasterMessage) in.readObject();
                        int mapId = incomingMessage.getMapId();

                        ResultBox targetBox;

                        synchronized (sharedResults){
                            targetBox = sharedResults.remove(mapId);
                        }

                        if(targetBox != null){
                            synchronized(targetBox){
                                targetBox.setFinalMessage(incomingMessage);
                                targetBox.notify();
                            }
                        }
                        reducerConnection.close();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }catch (IOException e) {
                System.err.println("Reducer Listener Error: " + e.getMessage());
            }

        }).start();
    }

}
