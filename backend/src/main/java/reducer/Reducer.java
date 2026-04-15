package reducer;

import utils.ConfigLoader;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;

public class Reducer {

    public static void main(String []args){
        // Load the centralized configuration file
        ConfigLoader.loadConfig("src/main/resources/system_network_config.conf");


        Reducer reducer = new Reducer();
        reducer.initFromConfig();
        reducer.openServer();
    }

    final HashMap<Integer, AggregationBox> resultsBuffer = new HashMap<>();

    private static Integer numberOfWorkers = -1;

    private int masterListenerPort = -1;
    private String masterListenerIp = null;
    private int reducerPort;




    
    void openServer(){

        try(ServerSocket providerSocket = new ServerSocket(this.reducerPort)){

            System.out.printf("Listening for Workers on port %d...\n", reducerPort);
            while(true){

                Socket connection = providerSocket.accept();
                Thread t = new ReducerHandler(connection, resultsBuffer, masterListenerIp, masterListenerPort);
                t.start();
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }



    private void initFromConfig() {
        // Get Reducer's port
        this.reducerPort = ConfigLoader.getPort("REDUCER");

        // Get Master's listener info for sending back final results
        this.masterListenerIp = ConfigLoader.getIp("MASTER_REDUCER_LISTENER");
        this.masterListenerPort = ConfigLoader.getPort("MASTER_REDUCER_LISTENER");

        // Automatically determine number of workers from the config list
        Reducer.numberOfWorkers = ConfigLoader.getAllWorkers().size();

        System.out.println("==================================================");
        System.out.println("   REDUCER INITIALIZED");
        System.out.println("   Listening on Port: " + this.reducerPort);
        System.out.println("   Expecting Results From: " + numberOfWorkers + " Workers");
        System.out.println("   Target Master Listener: " + masterListenerIp + ":" + masterListenerPort);
        System.out.println("==================================================");
    }


    public static synchronized int getNumberOfWorkers() {
        return numberOfWorkers;
    }

    public static synchronized void setNumberOfWorkers(int count) {
        Reducer.numberOfWorkers = count;
    }

}
