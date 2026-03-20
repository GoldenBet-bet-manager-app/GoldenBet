package Worker;

import models.Game;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;


public class Worker  {
    private String ip;
    private int port;
    private final int workerid;
    private HashMap<String, BigDecimal> gamesprofits = new HashMap<> ( );
    private HashMap<String, Game> myGames = new HashMap<> ( );

    public int getWorkerid ( ) {
        return workerid;
    }

    public Worker (String ip, int port,int workerid) {
        this.ip = ip;
        this.port = port;
        this.workerid =workerid;
    }

    public String getIp ( ) {
        return ip;
    }

    public int getPort ( ) {
        return port;
    }


    ServerSocket providerSocket;
    Socket connection = null;


    public void openServer ( ) {
        try {
            providerSocket = new ServerSocket ( this.port, 10 );
            System.out.println ( "Ο Worker " + workerid + " άνοιξε και ακούει στην πόρτα: " + this.port );

            while (true) {
                connection = providerSocket.accept ( );
                Thread workerHandler = new WorkerHandler ( connection, myGames, gamesprofits,workerid );
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
    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Σφάλμα: Πρέπει να δώσετε IP και Port και id ως ορίσματα.");
            return;
        }
        String ip = args[0];
        int port,workerid;
        try {
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.out.println("Σφάλμα: Η πόρτα πρέπει να είναι ακέραιος αριθμός.");
            return;
        }
        Worker worker = new Worker(ip, port,Integer.parseInt ( args[ 2 ] ));
        worker.openServer();

    }
}