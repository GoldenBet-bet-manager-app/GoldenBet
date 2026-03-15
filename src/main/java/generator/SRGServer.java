package generator;

import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class SRGServer {

    public static void main(String [] args){

        Map<String, RandomNumberGenerator> generators = new HashMap<>();
        Map<String, String> secrets = new HashMap<>();

        try {

            File folder = new File("games");
            File[] files = folder.listFiles();

            if(files == null) {
                System.out.println("Games folder not found.");
                return;
            }

            for(File f : files) {

                if(!f.getName().endsWith(".json"))
                    continue;

                String content = Files.readString(f.toPath());

                String[] parts = content.split("\"GameName\"\\s*:\\s*\"");
                String rightPart = parts[1];
                String gameName = rightPart.split("\"")[0];

                String[] parts2 = content.split("\"HashKey\"\\s*:\\s*\"");
                String rightPart2 = parts2[1];
                String hashKey = rightPart2.split("\"")[0];

                RandomNumberGenerator gen = new RandomNumberGenerator(20);

                gen.start();

                generators.put(gameName, gen);
                secrets.put(gameName, hashKey);

                System.out.println("Loaded game: " + gameName);
            }

            ServerSocket serverSocket = new ServerSocket(5000);

            System.out.println("SRG Server running...");

            int i = 1;
            while(true) {

                Socket socket = serverSocket.accept();

                WorkerRequestHandler handler = new WorkerRequestHandler(socket, generators, secrets);
                System.out.println("i called generator " + i);
                i+=1;
                handler.start();
            }

        }catch(Exception e){
            System.err.println("Server Error: " + e.getMessage());
        }
    }
}
