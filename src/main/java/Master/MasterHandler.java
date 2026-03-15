package Master;

import models.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import Worker.Worker;

public class MasterHandler extends Thread{
    ObjectInputStream in;
    ObjectOutputStream out;
    private ArrayList<Worker> workers;

    public MasterHandler(Socket connection,ArrayList<Worker> workers) {
        this.workers = workers;
        try {
            out = new ObjectOutputStream(connection.getOutputStream());
            in = new ObjectInputStream(connection.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void run() {
        int numofworkes = workers.size();
        int gameWorkerIndex;
        Worker target;
        Object response;
        try {

            MasterMessage message = (MasterMessage) in.readObject();
            MasterMessageType type = message.getType();

            switch (type){
                case ADD_GAME:
                    Game addedGame = (Game) message.getPayload();
                     gameWorkerIndex = hashGameToWorker(addedGame.getGameName(),numofworkes);
                     target = workers.get(gameWorkerIndex);

                     response = sendRequestToWorker(target, message); // response returned to master

                    out.writeObject(response); // response returned to manager
                    out.flush();


                    break;
                case REMOVE_GAME:
                case SEARCH:
                    ArrayList<Game> allMatchedGames = new ArrayList<> ();
                    for(Worker w:workers) {
                        response = sendRequestToWorker ( w, message );
                        if ( response instanceof ArrayList<?> ) {
                            allMatchedGames.addAll ( (ArrayList<Game>) response );
                        }

                    }
                    out.writeObject(allMatchedGames); // response returned to manager
                    out.flush();
                    break;
                case PLAY:
                    Bet bet = (Bet) message.getPayload ();
                    gameWorkerIndex = hashGameToWorker( bet.getGamename ( ),numofworkes);
                    target = workers.get(gameWorkerIndex);
                    response = sendRequestToWorker(target, message); // response returned to master
                    out.writeObject(response); // response returned to manager
                    out.flush();
                    break;

                case ADD_BALANCE:
                case STATS:
                case RATE:
                    Rating rating = (Rating) message.getPayload ();
                    gameWorkerIndex = hashGameToWorker(rating.getGameName ( ),numofworkes);
                    target = workers.get(gameWorkerIndex);
                    response = sendRequestToWorker(target, message); // response returned to master
                    out.writeObject(response); // response returned to manager
                    out.flush();
                    break;

            }


        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
                out.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }


    }


    private Object sendRequestToWorker(Worker target, MasterMessage message) {

        Socket requestSocket = null;
        ObjectOutputStream outWorker = null;
        ObjectInputStream inWorker = null;

        try {
            requestSocket = new Socket ( target.getIp ( ), target.getPort ( ) );
            outWorker = new ObjectOutputStream ( requestSocket.getOutputStream ( ) );
            inWorker = new ObjectInputStream ( requestSocket.getInputStream ( ) );

            outWorker.writeObject ( message );
            outWorker.flush ( );

            return inWorker.readObject ( );

        } catch (UnknownHostException unknownHost) {
            System.err.println ( "You are trying to connect to an unknown host!" );
        } catch (IOException ioException) {
            ioException.printStackTrace ( );
        } catch (ClassNotFoundException e) {
            e.printStackTrace ( );
        } finally {
            try {

                in.close ( );
                out.close ( );
                requestSocket.close ( );

            } catch (IOException ioException) {
                ioException.printStackTrace ( );
            }
        }
        return null;
    }


    private int hashGameToWorker(String gameName,int numbersofWorkers){
        int hash=Math.abs ( gameName.hashCode () );
        return hash % numbersofWorkers;
    }
}
