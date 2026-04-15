package Worker;

import models.Game;
import models.WorkerInfo;
import utils.ConfigLoader;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;


public class Worker {

    public static void main(String []args){

        // Check if the Worker Index was provided as a command-line argument
        if (args.length < 1) {
            System.err.println("Usage Error: Please provide the Worker Index (0, 1, 2...) as an argument.");
            System.err.println("Example: java Worker.Worker 0");
            System.exit(1);
        }

        try {
            int index = Integer.parseInt(args[0]);

            // Load the central configuration file
            ConfigLoader.loadConfig("src/main/resources/system_network_config.conf");

            // Initialize the Worker instance
            Worker worker = new Worker();
            worker.setupFromConfig(index);

            // Start the server to listen for Master/Client requests
            worker.openServer();

        } catch (NumberFormatException e) {
            System.err.println("Error: The argument must be a valid integer index.");
        }

    }


    private  HashMap<String, BigDecimal> gamesProfits = new HashMap<> ();
    private  HashMap<String, Game> myGames = new HashMap<>();
    static HashMap<String, BigDecimal> primaryPlayersProfits = new HashMap<>();
    static HashMap<String, BigDecimal> backupPlayersProfits = new HashMap<>();


    private int workerPort;
    private int workerId;
    private static boolean includeBackUps;


    public void openServer(){

        try( ServerSocket providerSocket = new ServerSocket ( this.workerPort, 50 )){

            System.out.println ( "Worker " + workerId + " opened and listens to port: " + this.workerPort );
            while(true) {

                Socket connection = providerSocket.accept ( );
                Thread workerHandler = new WorkerHandler ( connection, myGames, gamesProfits, workerId);
                workerHandler.start ( );

            }

        } catch (IOException ioException) {
            ioException.printStackTrace ( );
        }
    }


    private void setupFromConfig(int index) {
        ArrayList<WorkerInfo> allWorkers = ConfigLoader.getAllWorkers();

        // Validate that the index exists in the loaded configuration
        if (index < 0 || index >= allWorkers.size()) {
            System.err.println("Configuration Error: Index " + index + " not found in system_network_config.conf");
            System.exit(1);
        }

        // Retrieve the pre-configured WorkerInfo from the loader
        WorkerInfo myInfo = allWorkers.get(index);
        this.workerId = myInfo.getWorkerId();     // ID assigned by ConfigLoader (currentWorkerId++)
        this.workerPort = myInfo.getWorkerPort(); // Port assigned in the .conf file

        System.out.println("==================================================");
        System.out.println("   WORKER NODE #" + this.workerId + " INITIALIZED");
        System.out.println("   Listening on Port: " + this.workerPort);
        System.out.println("==================================================");
    }


    public synchronized static boolean includeBackUps() {
        return includeBackUps;
    }

    public synchronized static void setIncludeBackUps(boolean includeBackUps) {
        Worker.includeBackUps = includeBackUps;
    }

}

