package reducer;

import Master.Master;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Reducer {

    public static void main(String []args){

        new Reducer().openServer();
    }

    final HashMap<Integer, AggregationBox> resultsBuffer = new HashMap<>();

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


}
