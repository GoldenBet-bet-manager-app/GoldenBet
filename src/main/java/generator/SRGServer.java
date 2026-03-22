package generator;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class SRGServer {

    public static void main(String [] args){

        new SRGServer().openServer();
    }

    private void openServer(){

        Map<String, RandomNumberGenerator> generators = new HashMap<>();
        Map<String, String> secrets = new HashMap<>();

        try {

            ServerSocket serverSocket = new ServerSocket(5000);
            System.out.println("SRG Server running...");

            while(true) {

                Socket socket = serverSocket.accept();
                GeneratorHandler handler = new GeneratorHandler(socket, generators, secrets);
                handler.start();
            }

        }catch(Exception e){
            System.err.println("Server Error: " + e.getMessage());
        }

    }
}
