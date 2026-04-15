package generator;

import utils.ConfigLoader;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class SRGServer {

    private int srgPort;

    public static void main(String [] args){

        // Load the centralized configuration
        ConfigLoader.loadConfig("src/main/resources/system_network_config.conf");


        new SRGServer().openServer();
    }

    private void openServer(){

        this.srgPort = ConfigLoader.getPort("SRG");

        Map<String, RandomNumberGenerator> generators = new HashMap<>();
        Map<String, String> secrets = new HashMap<>();

        try {

            ServerSocket serverSocket = new ServerSocket(this.srgPort);
            System.out.printf("SRG Server running and listening on port %d...\n",this.srgPort);

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
