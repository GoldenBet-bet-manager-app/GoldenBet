package reducer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;

public class Reducer {

    public static void main(String []args){

        Reducer reducer = new Reducer();
        reducer.setupConfiguration();
        reducer.openServer();
    }

    final HashMap<Integer, AggregationBox> resultsBuffer = new HashMap<>();

    int numberOfWorkers = -1;
    int MasterPort = -1;
    String MasterIp = null;



    void openServer(){

        try(ServerSocket providerSocket = new ServerSocket(43500)){

            System.out.println("Listening for Workers on port 43500...");
            while(true){

                Socket connection = providerSocket.accept();
                Thread t = new ReducerHandler(connection, resultsBuffer);
                t.start();
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }




    private void setupConfiguration(){

        Scanner scanner = new Scanner(System.in);
        boolean flag = false;

        while(!flag){

            System.out.print("Enter number of Workers: ");

            if(scanner.hasNextInt()){
                this.numberOfWorkers = scanner.nextInt();

                if(numberOfWorkers > 1) {
                    scanner.nextLine();
                    flag = true;
                }else{
                    System.out.println("Error: Invalid value. Please enter a positive integer");
                }
            }else{
                System.out.println("Error: Invalid input. Only numeric values are accepted");
                scanner.next();
            }
        }

        flag = false;

        while(!flag){

            System.out.print("Enter Master IP and Port (format IP:PORT): ");
            String input = scanner.nextLine().trim();

            String[] parts = input.split(":");
            if(parts.length != 2){
                System.out.println("Error: Invalid format. Expected format is IP:PORT");
                continue;
            }


            this.MasterIp = parts[0];
            String portString = parts[1];

            //--------ελεγχος Port--------
            try{
                MasterPort = Integer.parseInt(portString);
                if(MasterPort < 1 || MasterPort > 65535){
                    System.out.println("Error: Port out of range. Allowed range is 1-65535");
                    continue;
                }
            }catch(Exception e){
                System.out.println("Error: Port must be a numeric value.");
                continue;
            }

            //-------ελεγχος IP--------
            String[] splitsIp = MasterIp.split("\\.");
            if(splitsIp.length != 4){
                System.out.println("Error: Invalid IP format. Must contain exactly 4 parts separated by dots");
                continue;
            }

            flag = true;
            for(String s: splitsIp){
                try {
                    int ipNumber = Integer.parseInt(s);
                    if(ipNumber < 0 || ipNumber > 255){
                        flag = false;
                        break;
                    }
                }catch(Exception e){
                    flag = false;
                    break;
                }
            }

            if (!flag) {
                System.out.println("Error: Invalid IP address. Each part must be a number between 0 and 255.");
                continue;
            }
        }

        System.out.println("Configuration saved successfully");    }

}
