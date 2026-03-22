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

        int numOfWorkers = workers.size();
        int gameWorkerIndex;
        Worker target;
        Object response;
        int currentMapId;
        ResultBox myBox;
        MasterMessage finalResultMsg;
        ArrayList<Game> finalGames;

        try {

            MasterMessage message = (MasterMessage) in.readObject();
            MasterMessageType type = message.getType();

            switch (type){
                case ADD_GAME:
                    Game gameToAdd = (Game) message.getPayload();
                    gameWorkerIndex = hashGameToWorker(gameToAdd.getGameName(),numOfWorkers);
                    target = workers.get(gameWorkerIndex);

                    System.out.println("Master is trying to add game: " + gameToAdd.getGameName() + "...");
                    response = sendRequestToWorker(target, message); // response returned to master

                    if (response == null) {
                        System.out.println("Error: Could not communicate with Worker.");
                        out.writeObject("Error: Could not communicate with Worker.");
                    }else{
                        System.out.println(response);
                        out.writeObject(response); // response returned to manager
                    }
                    out.flush();
                    break;

                case REMOVE_GAME:
                    Game gameToDelete = (Game) message.getPayload();
                    gameWorkerIndex = hashGameToWorker(gameToDelete.getGameName(),numOfWorkers);
                    target = workers.get(gameWorkerIndex);

                    System.out.println("Master is trying to remove game: " + gameToDelete.getGameName() + "...");
                    response = sendRequestToWorker(target, message); // response returned to master

                    if (response == null) {
                        System.out.println("Error: Could not communicate with Worker.");
                        out.writeObject("Error: Could not communicate with Worker.");
                    }else{
                        System.out.println(response);
                        out.writeObject(response); // response returned to manager
                    }
                    out.flush();
                    break;

                case MODIFY_GAME:
                    Game gameToModify = (Game) message.getPayload();
                    gameWorkerIndex = hashGameToWorker(gameToModify.getGameName(),numOfWorkers);
                    target = workers.get(gameWorkerIndex);

                    System.out.println("Master is trying to modify game: " + gameToModify.getGameName() + "...");
                    response = sendRequestToWorker(target, message); // response returned to master


                    if (response == null) {
                        System.out.println("Error: Could not communicate with Worker.");
                        out.writeObject("Error: Could not communicate with Worker.");
                    }else{
                        System.out.println(response);
                        out.writeObject(response); // response returned to manager
                    }
                    out.flush();
                    break;
                case GET_ACTIVE_GAMES:
                    currentMapId = Math.abs(randomUUID().hashCode());
                    message.setMapId(currentMapId);

                    myBox = new ResultBox();

                    synchronized(sharedResults){
                        sharedResults.put(currentMapId, myBox);
                    }

                    for(Worker w : workers){
                        new Thread(() -> {
                            sendMapRequestToWorker(w, message);
                        }).start();
                    }

                    synchronized (myBox){
                        while(myBox.getFinalMessage() == null) {
                            try {
                                myBox.wait(); // MasterHandler sleeps until the arrival of the final results
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    finalResultMsg = myBox.getFinalMessage();
                    finalGames = (ArrayList<Game>) finalResultMsg.getPayload();

                    out.writeObject(finalGames);
                    out.flush();
                    break;

                case SEARCH:

                    currentMapId = Math.abs(randomUUID().hashCode());
                    message.setMapId(currentMapId);

                    myBox = new ResultBox();

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
                                myBox.wait(); // MasterHandler sleeps until the arrival of the final results
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    finalResultMsg = myBox.getFinalMessage();
                    finalGames = (ArrayList<Game>) finalResultMsg.getPayload();

                    out.writeObject(finalGames);
                    out.flush();
                    break;

                case PLAY:
                    Bet bet = (Bet) message.getPayload ();
                    gameWorkerIndex = hashGameToWorker( bet.getGamename ( ),numOfWorkers);
                    target = workers.get(gameWorkerIndex);
                    response = sendRequestToWorker(target, message); // response returned to master
                    out.writeObject(response); // response returned to manager
                    out.flush();
                    break;

                case PROVIDER_GAME_PROFITS:
                    currentMapId = Math.abs(randomUUID().hashCode());
                    message.setMapId(currentMapId);

                    myBox = new ResultBox();

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
                                myBox.wait(); // MasterHandler sleeps until the arrival of the final results
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    break;

                case RATE:
                    Rating rating = (Rating) message.getPayload ();
                    gameWorkerIndex = hashGameToWorker(rating.getGameName ( ),numOfWorkers);
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


            // We wait for acknowledgement message before proceeding to send the real one
            // To ensure that the connection has been established
            Object ack = inWorker.readObject();

            if ("ACK".equals(ack)) {
                outWorker.writeObject(message);
                outWorker.flush();
            }


            // returns a message of success/failure of the requested operation
            return inWorker.readObject();

        } catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
        } catch (IOException e) {
            System.err.println("Worker Connection Error: " + e.getMessage());
            e.printStackTrace();
        }
        catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    private void sendMapRequestToWorker(Worker w, MasterMessage message){
        try(Socket requestSocket = new Socket(w.getIp(), w.getPort());
            ObjectOutputStream outWorker = new ObjectOutputStream(requestSocket.getOutputStream());
            ObjectInputStream inWorker = new ObjectInputStream(requestSocket.getInputStream());){

            // We wait for acknowledgement message before proceeding to send the real one
            // To ensure that the connection has been established
            Object ack = inWorker.readObject();

            if ("ACK".equals(ack)) {
                outWorker.writeObject(message);
                outWorker.flush();
            }



        }catch (IOException e) {
            System.err.println("Worker Map Phase Connection Error: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    private int hashGameToWorker(String gameName,int numbersOfWorkers){

        int hash = Math.abs ( gameName.hashCode () );
        return hash % numbersOfWorkers;
    }
}
