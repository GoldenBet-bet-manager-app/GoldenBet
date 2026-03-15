package Worker;

import models.Bet;
import models.Game;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Random;


public class Worker {
    private String ip;
    private int port;
    private final int workerid;
    private int counter=0;

    public Worker(String ip, int port) {
        this.ip = ip;
        this.port = port;
        this.workerid = counter++;
    }
    public String getIp() { return ip; }
    public int getPort() { return port; }



    public static void main(String args[]) {
        if (args.length < 1) {
            System.out.println("Error: Please provide a port number.");
            System.out.println("Usage: java Worker.Worker <port>");
            return;
        }

        int port = Integer.parseInt(args[0]);
        String ip = "127.0.0.1";

        Worker w = new Worker(ip, port);
        w.openServer();
    }



    ServerSocket providerSocket;
    Socket connection = null;

    void openServer() {
        try {
            providerSocket = new ServerSocket(4321, 10);

            while (true) {
                connection = providerSocket.accept();

                //Thread workerHandler = new WorkerHandler(connection);
                //workerHandler.start();

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

}
