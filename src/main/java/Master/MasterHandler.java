package Master;

import models.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import Worker.Worker;

import static java.util.UUID.randomUUID;


public class MasterHandler extends Thread{


    ObjectInputStream in;
    ObjectOutputStream out;
    private ArrayList<Worker> workers;
    private HashMap<Integer, ResultBox> sharedResults;

    public MasterHandler(Socket connection,ArrayList<Worker> workers, HashMap<Integer, ResultBox> sharedResults ) {

        this.workers = workers;
        this.sharedResults = sharedResults;
        try {
            out = new ObjectOutputStream(connection.getOutputStream());
            in = new ObjectInputStream(connection.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void run(){

        int numOfWorker = workers.size();
        int gameWorkerIndex;
        Worker target;
        Object response;
        try {

            MasterMessage message = (MasterMessage) in.readObject();
            MasterMessageType type = message.getType();

            switch (type){
                case ADD_GAME:

                    Game addedGame = (Game) message.getPayload();
                    gameWorkerIndex = hashGameToWorker(addedGame.getGameName(),numOfWorker);
                    target = workers.get(gameWorkerIndex);

                    response = sendRequestToWorker(target, message); // response returned to master

                    if (response == null) {
                        out.writeObject("Error: Could not communicate with Worker.");
                    }else{
                        out.writeObject(response); // response returned to manager
                        out.flush();
                    }
                    break;

                case REMOVE_GAME:
                case SEARCH:

                    int currentMapId = Math.abs(randomUUID().hashCode());
                    message.setMapId(currentMapId);

                    ResultBox myBox = new ResultBox();

                    synchronized(sharedResults){
                        sharedResults.put(currentMapId, myBox);
                    }

                    for(Worker w: workers){
                        new Thread(() -> {
                            sendMapRequestToWorker(w, message);
                        }).start();
                    }

                    synchronized (myBox){
                        while(myBox.getFinalMessage() == null) {
                            try {
                                myBox.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    MasterMessage finalResultMsg = myBox.getFinalMessage();
                    ArrayList<Game> finalGames = (ArrayList<Game>) finalResultMsg.getPayload();

                    out.writeObject(finalGames);
                    out.flush();
                    break;

                case PLAY:
                    Bet bet = (Bet) message.getPayload ();
                    gameWorkerIndex = hashGameToWorker( bet.getGamename ( ),numOfWorker);
                    target = workers.get(gameWorkerIndex);
                    response = sendRequestToWorker(target, message); // response returned to master
                    out.writeObject(response); // response returned to manager
                    out.flush();
                    break;

                case ADD_BALANCE:
                case STATS:
                case RATE:
                    Rating rating = (Rating) message.getPayload ();
                    gameWorkerIndex = hashGameToWorker(rating.getGameName ( ),numOfWorker);
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

        try(Socket requestSocket = new Socket(target.getIp(), target.getPort());
            ObjectOutputStream outWorker = new ObjectOutputStream(requestSocket.getOutputStream());
            ObjectInputStream inWorker = new ObjectInputStream(requestSocket.getInputStream());) {

            outWorker.writeObject(message);
            outWorker.flush();

            return inWorker.readObject();

        } catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Worker Connection Error: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private void sendMapRequestToWorker(Worker w, MasterMessage message){
        try(Socket requestSocket = new Socket(w.getIp(), w.getPort());
            ObjectOutputStream outWorker = new ObjectOutputStream(requestSocket.getOutputStream());){

            outWorker.writeObject(message);
            outWorker.flush();

        }catch (IOException e) {
            System.err.println("Worker Map Phase Connection Error: " + e.getMessage());
        }
    }


    private int hashGameToWorker(String gameName,int numbersOfWorkers){

        int hash = Math.abs ( gameName.hashCode () );
        return hash % numbersOfWorkers;
    }
}
