package Worker;

import models.Game;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;


public class Worker {

    public static void main(String []args){

        Worker worker = new Worker();
        worker.setupConfiguration();
        worker.openServer();
    }


    private  HashMap<String, BigDecimal> gamesProfits = new HashMap<> ();
    private  HashMap<String, Game> myGames = new HashMap<>();
    private  HashMap<String, BigDecimal> playersProfits = new HashMap<>();

    private int workerPort;
    private int workerId;
    private static boolean includeBackUps;


    public void openServer(){

        try( ServerSocket providerSocket = new ServerSocket ( this.workerPort, 50 )){

            System.out.println ( "Ο Worker " + workerId + " άνοιξε και ακούει στην πόρτα: " + this.workerPort );
            while(true) {

                Socket connection = providerSocket.accept ( );
                Thread workerHandler = new WorkerHandler ( connection, myGames, gamesProfits, playersProfits, workerId);
                workerHandler.start ( );

            }

        } catch (IOException ioException) {
            ioException.printStackTrace ( );
        }
    }

    private void setupConfiguration(){

        Scanner scanner = new Scanner(System.in);
        boolean flag = false;

        //--------ελεγχος ID--------
        while(!flag){

            System.out.print("Enter Worker ID (non-negative integer): ");

            if(scanner.hasNextInt()){
                this.workerId = scanner.nextInt();
                if (workerId >= 0) {
                    this.workerId = workerId;
                    scanner.nextLine(); // Καθαρισμός του buffer μετά το nextInt()
                    flag = true;
                } else {
                    System.out.println("Error: ID must be a non-negative integer.");
                }
            }else{
                System.out.println("Error: Invalid input. Only numeric values are accepted");
                scanner.next();
            }
        }

        flag = false;

        //--------ελεγχος Port--------
        while(!flag){

            System.out.print("Enter Worker Port (1-65535): ");

            if (scanner.hasNextInt()) {
                this.workerPort = scanner.nextInt();

                if(workerPort >= 1 && workerPort <= 65535){
                    flag = true;
                }else {
                    System.out.println("Error: Port out of range. Allowed range is 1-65535.");
                }
            } else {
                System.out.println("Error: Port must be a numeric value.");
                scanner.next();
            }
        }

        System.out.println("Configuration saved successfully");
    }



    public synchronized static boolean includeBackUps() {
        return includeBackUps;
    }

    public static void setIncludeBackUps(boolean includeBackUps) {
        Worker.includeBackUps = includeBackUps;
    }

}

