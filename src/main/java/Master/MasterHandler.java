package Master;

import models.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import static java.util.UUID.randomUUID;

/**
 *  Χειρίζεται την επικοινωνία του Master με τους πελάτες (Παίκτες ή Managers).
 */
public class MasterHandler extends Thread{


    ObjectInputStream in;
    ObjectOutputStream out;
    private final ArrayList<WorkerInfo> workers;
    private final HashMap<Integer, ResultBox> sharedResults;

    public MasterHandler(Socket connection,ArrayList<WorkerInfo> workers, HashMap<Integer, ResultBox> sharedResults ){

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

        try {
            MasterMessage message = (MasterMessage) in.readObject();
            MasterMessageType type = message.getType();

            switch (type){
                case ADD_GAME:
                    Game gameToAdd = (Game) message.getPayload();
                    routeToSingleWorker(message, gameToAdd.getGameName(), "add");
                    break;

                case REMOVE_GAME:
                    Game gameToDelete = (Game) message.getPayload();
                    routeToSingleWorker(message, gameToDelete.getGameName(), "remove");
                    break;

                case MODIFY_GAME:
                    Game gameToModify = (Game) message.getPayload();
                    routeToSingleWorker(message, gameToModify.getGameName(), "modify");
                    break;

                case SEARCH, GET_ACTIVE_GAMES, PROVIDER_GAME_PROFITS,GAMES_PROFITS,PLAYERS_PROFITS:
                    executeMapReducePhase(message);
                    break;

                case PLAY:
                    Bet bet = (Bet) message.getPayload ();
                    routeToSingleWorker(message, bet.getGameName(), "play");
                    break;

                case RATE:
                    Rating rating = (Rating) message.getPayload ();
                    routeToSingleWorker(message, rating.getGameName(), "rate");
                    break;
            }

        } catch (IOException | ClassNotFoundException e){
            System.err.println("Client Communication Error: " + e.getMessage());
        } finally {
            try {
                in.close();
                out.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    /**
     * Δρομολογεί ένα αίτημα σε έναν συγκεκριμένο Worker με βάση το όνομα του παιχνιδιού.
     * Περιμένει την απάντηση και την επιστρέφει απευθείας στον πελάτη.
     * @param message Το μήνυμα αναζήτησης
     * @param gameName Το όνομα του παιχνιδιού για να βρεθεί ο σωστός Worker
     * @param actionLog Περίπτωση ενέργειας
     */
    private void routeToSingleWorker(MasterMessage message, String gameName, String actionLog) throws IOException, ClassNotFoundException{

        int numOfWorkers = workers.size();
        int gameWorkerIndex = hashGameToWorker(gameName, numOfWorkers);
        WorkerInfo target = workers.get(gameWorkerIndex);

        System.out.println("Master is trying to " + actionLog + " game: " + gameName + "...");
        Object response = sendRequestToWorker(target, message, true);

        if (response == null) {
            System.out.println("Error: Could not communicate with Worker.");
            out.writeObject("Error: Could not communicate with Worker.");
        } else {
            System.out.println(response);
            out.writeObject(response);
        }
        out.flush();
    }

    /**
     * Εκτελεί τη διαδικασία MapReduce για αναζητήσεις, περιμένει μέχρι ο Reducer
     * να φέρει το τελικό αποτέλεσμα στο ResultBox.
     * @param message Το μήνυμα που εξυπηρετούμε
     */
    private void executeMapReducePhase(MasterMessage message) throws IOException, ClassNotFoundException {

        int currentMapId = Math.abs(randomUUID().hashCode());
        message.setMapId(currentMapId);

        ResultBox myBox = new ResultBox();

        synchronized(sharedResults){
            sharedResults.put(currentMapId, myBox);
        }

        for(WorkerInfo w: workers){
            sendRequestToWorker(w, message, false);
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

        MasterMessage finalResultMsg = myBox.getFinalMessage();

        Object finalGames = null;

        if(finalResultMsg.getPayload() instanceof ArrayList<?>)
            finalGames = (ArrayList<Game>) finalResultMsg.getPayload();
        else if (finalResultMsg.getPayload() instanceof HashMap<?,?>)
            finalGames = (HashMap<String, BigDecimal>) finalResultMsg.getPayload();


        out.writeObject(finalGames);
        out.flush();
    }

    /**
     * Ανοίγει σύνδεση με έναν Worker, περιμένει το ACK και του στέλνει το μήνυμα.
     * @param target Ο Worker που θα λάβει το μήνυμα
     * @param message Το μήνυμα που εξυπηρετούμε
     * @param expectResponse Διαχείριση επιστροφής
     * @return Την απάντηση του Worker ή null
     */
    private Object sendRequestToWorker(WorkerInfo target, MasterMessage message, boolean expectResponse) throws IOException, ClassNotFoundException {

        try (Socket requestSocket = new Socket(target.getWorkerIp(), target.getWorkerPort());
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
            if (expectResponse) {
                return inWorker.readObject();
            }
            return null;

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

    /**
     * Υπολογίζει σε ποιον Worker ανήκει ένα παιχνίδι κάνοντας hash το όνομά του.
     * @param gameName Το όνομα του παιχνιδιού
     * @param numbersOfWorkers Το συνολικό πλήθος των Workers
     * @return Το index του υπεύθυνου Worker
     */
    private int hashGameToWorker(String gameName,int numbersOfWorkers){

        int hash = Math.abs(gameName.hashCode());
        return hash % numbersOfWorkers;
    }
}
