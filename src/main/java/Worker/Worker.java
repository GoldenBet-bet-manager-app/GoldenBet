package Worker;

import models.Game;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;


public class Worker implements Runnable {
    private String ip;
    private int port;
    private final int workerid;
    private HashMap<String, BigDecimal> profits = new HashMap<> ( );
    private HashMap<String, Game> myGames = new HashMap<> ( );
    private static int counter = 0;

    public Worker (String ip, int port) {
        this.ip = ip;
        this.port = port;
        this.workerid = counter++;
    }

    public String getIp ( ) {
        return ip;
    }

    public int getPort ( ) {
        return port;
    }


    ServerSocket providerSocket;
    Socket connection = null;

    @Override
    public void run ( ) {
        try {
            providerSocket = new ServerSocket ( this.port, 10 );
            System.out.println ( "Ο Worker " + workerid + " άνοιξε και ακούει στην πόρτα: " + this.port );

            while (true) {
                connection = providerSocket.accept ( );
                Thread workerHandler = new WorkerHandler ( connection, myGames, profits );
                workerHandler.start ( );

            }
        } catch (IOException ioException) {
            ioException.printStackTrace ( );
        } finally {
            try {
                providerSocket.close ( );
            } catch (IOException ioException) {
                ioException.printStackTrace ( );
            }
        }
    }
}
