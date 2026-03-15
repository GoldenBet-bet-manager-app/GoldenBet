package Master;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import Worker.Worker;

public class Master{

    public static void main(String []args) {
        new Master().openServer();
    }


    ServerSocket providerSocket;
    Socket connection = null;
    ArrayList<Worker> workersList = new ArrayList<>();

    void openServer() {

        loadWorkersConfig("workers.conf");

        try {
            providerSocket = new ServerSocket(43210, 10);

            while (true) {
                connection = providerSocket.accept();

                Thread t = new MasterHandler(connection,workersList);
                t.start();

            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            try {
                providerSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }


    private void loadWorkersConfig(String fileName) {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#") || line.trim().isEmpty()) continue;

                String[] parts = line.split(":");
                workersList.add(new Worker(parts[0], Integer.parseInt(parts[1])));
            }
            System.out.println(workersList.size() + " Workers were initialized");
        } catch (IOException e) {
            System.err.println("Critical Error: Could not read config file!");
        }
    }

}
