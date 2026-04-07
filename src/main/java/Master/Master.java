package Master;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import Worker.Worker;
import models.MasterMessage;
import models.WorkerInfo;

public class Master{

    public static void main(String []args) {

        new Master().openServer();
    }

    final ArrayList<WorkerInfo> workersList = new ArrayList<>();
    final HashMap<Integer, ResultBox> sharedResults = new HashMap<>();

    void openServer() {

        loadWorkersConfig("workers.conf");

        startReducerListener(43211);

        try(ServerSocket providerSocket = new ServerSocket(43210, 50);){

            System.out.println("Listening for Clients on port 43210...");
            while(true) {

                Socket connection = providerSocket.accept();
                Thread t = new MasterHandler(connection, workersList , sharedResults);
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


    private void loadWorkersConfig(String fileName) {

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {

            String line;
            int currentWorkerId = 0;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#") || line.trim().isEmpty()) continue;

                String[] parts = line.split(":");
                String ip = parts[0];
                int port = Integer.parseInt(parts[1]);

                WorkerInfo info = new WorkerInfo(ip, port, currentWorkerId);
                workersList.add(info);
                currentWorkerId += 1;
            }
            System.out.println(workersList.size() + " Workers were initialized");

        } catch (IOException e) {
            System.err.println("Critical Error: Could not read config file!");
        }
    }


}
