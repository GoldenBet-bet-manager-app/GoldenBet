package Master;


import gr.goldenbet.domain.*;
import org.json.JSONObject;
import utils.Address;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.net.Socket;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import static java.util.UUID.randomUUID;


public class MasterHandler extends Thread {


    ObjectInputStream in;
    ObjectOutputStream out;
    private final ArrayList<WorkerInfo> workers;
    private final HashMap<Integer, ResultBox> sharedResults;
    private HashMap<String, Player> registedPlayers;

    public MasterHandler (Socket connection, ArrayList<WorkerInfo> workers, HashMap<Integer, ResultBox> sharedResults, HashMap<String, Player> registeredPlayers) {

        this.workers = workers;
        this.sharedResults = sharedResults;
        this.registedPlayers=registeredPlayers;
        try {
            out = new ObjectOutputStream ( connection.getOutputStream ( ) );
            in = new ObjectInputStream ( connection.getInputStream ( ) );
        } catch (IOException e) {
            e.printStackTrace ( );
        }
    }


    @Override
    public void run ( ) {


        try {
            MasterMessage message = (MasterMessage) in.readObject ( );
            MasterMessageType type = message.getType ( );



//            MasterMessage message = null ;
//            // 1. Διάβασμα του String από το Socket
//            String receivedJsonString = in.readUTF();
//            JSONObject masterJson = new JSONObject(receivedJsonString);
//
//// 2. Ξετύλιγμα του MasterMessage (Εξωτερικό επίπεδο)
//            String typeStr = masterJson.getString("type");
//            MasterMessageType type = MasterMessageType.valueOf(typeStr);
//            int mapId = masterJson.getInt("mapId");
//
//// 3. Ξετύλιγμα του Player (Μεσαίο επίπεδο)
//            if (masterJson.has("payload")) {
//                JSONObject playerJson = masterJson.getJSONObject("payload");
//
//                // Λήψη στοιχείων Player
//                String name = playerJson.getString("name");
//                String surname = playerJson.getString("surname");
//                String username = playerJson.getString("username");
//                String password = playerJson.getString("password");
//                String email = playerJson.getString("email");
//                String phone = playerJson.getString("phone");
//                String playerId = playerJson.getString("playerId");
//                boolean isLoggedIn = playerJson.getBoolean("isLoggedIn");
//
//                // 4. Ξετύλιγμα της Address (Εσωτερικό επίπεδο)
//                JSONObject addressJson = playerJson.getJSONObject("address");
//                Address address = new Address(
//                        addressJson.getString("street"),
//                        addressJson.getString("streetNumber"),
//                        addressJson.getString("city"),
//                        addressJson.getString("zipCode")
//                );
//
//                // 5. Ανασύνθεση των Java Objects
//                Player player = new Player(name, surname, username, password, email, phone, address);
//                player.setPlayerId(playerId);
//                player.setLoggedIn(isLoggedIn);
//
//                message = new MasterMessage(type, player);
//                message.setMapId(mapId);
//
//                // Τώρα το 'message' είναι έτοιμο για χρήση στο switch/case σας!
//            }


            switch (type) {
                case ADD_GAME:
                    Game gameToAdd = (Game) message.getPayload ( );
                    routeToSingleWorker ( message, gameToAdd.getGameName ( ), "add" );
                    break;

                case REMOVE_GAME:
                    Game gameToDelete = (Game) message.getPayload ( );
                    routeToSingleWorker ( message, gameToDelete.getGameName ( ), "remove" );
                    break;

                case MODIFY_GAME:
                    Game gameToModify = (Game) message.getPayload ( );
                    routeToSingleWorker ( message, gameToModify.getGameName ( ), "modify" );
                    break;

                case SEARCH, GET_ACTIVE_GAMES,GET_ALL_GAMES, PROVIDER_GAME_PROFITS, GAMES_PROFITS, PLAYERS_PROFITS:
                    executeMapReducePhase ( message );
                    break;

                case PLAY:
                    Bet bet = (Bet) message.getPayload ( );
                    routeToSingleWorker ( message, bet.getGameName ( ), "play" );
                    break;

                case RATE:
                    Rating rating = (Rating) message.getPayload ( );
                    routeToSingleWorker ( message, rating.getGameName ( ), "rate" );
                    break;
                case REGIST:
                    System.out.println("Hello from MASTER REGIST");
                    Player player = (Player) message.getPayload();
                    Object regResponse = registplayers(player);

                    JSONObject responseJson = new JSONObject();

                    if (regResponse instanceof Player) {
                        // Successful registration attempt
                        responseJson.put("status", "SUCCESS");
                        responseJson.put("message", "Registration successful for: " + ((Player) regResponse).getUsername());
                    } else {
                        // Failed registration attempt
                        responseJson.put("status", "FAILED");
                        responseJson.put("message", (String) regResponse);
                    }

                    // Αποστολή του String μέσω του TCP Socket
                    out.writeUTF(responseJson.toString());
                    out.flush();
                    break;
                case LOGIN:
                    Object loginResponse = loginplayers(message.getPayload());
                    out.writeObject(loginResponse);
                    out.flush();
                    break;
                case LOGOUT:
                    logoutplayers(message.getPayload ());
                    break;
            }

        } catch (IOException e) {
            System.err.println ( "Client Communication Error (IO exception): " + e.getMessage() );
        }
        catch (ClassNotFoundException e){
            System.err.println ( "Client Communication Error (Class not found): " + e.getMessage() );

        }
        finally {
            try {
                in.close ( );
                out.close ( );
            } catch (IOException ioException) {
                ioException.printStackTrace ( );
            }
        }
    }

    private void logoutplayers (Object payload) {
        String[] credentials = (String[]) payload;
        for ( Player p : registedPlayers.values ( ) ) {
            if ( p.getUsername ( ).equals ( credentials[ 0 ] ) && p.getPassword ( ).equals ( credentials[ 1 ] )
                    && p.isLoggedIn ()){
                p.setLoggedIn ( false );
            }
        }
    }

    private Object loginplayers (Object payload) {

        String[] credentials = (String[]) payload;

        synchronized (registedPlayers) {
            for (Player p : registedPlayers.values()) {

                if (p.getUsername().equals(credentials[0]) && p.getPassword().equals(credentials[1])
                        && !p.isLoggedIn()) {
                    p.setLoggedIn(true);
                    return p;
                }

            }
        }

        return null;
    }

    private Object registplayers (Player player) {

        synchronized (registedPlayers) {
            if ( registedPlayers.containsKey( player.getUsername()) ) {
                return "The player with this username already exists.";
            } else {
                registedPlayers.put (player.getUsername(), player);
            }
        }

        return player;
    }


    private void routeToSingleWorker (MasterMessage message, String gameName, String actionLog) throws IOException, ClassNotFoundException {

        int numOfWorkers = workers.size ( );
        int gameWorkerIndex = hashGameToWorker ( gameName, numOfWorkers );
        WorkerInfo target = workers.get ( gameWorkerIndex );
        WorkerInfo backupTarget=getBackupWorker ( gameWorkerIndex );
        int backupIndex=backupTarget.getWorkerId ();
        System.out.println ( "Master is trying to " + actionLog + " game: " + gameName + "..." );
        System.out.println ( "Primary: Worker " + gameWorkerIndex + " | Backup: Worker " + backupIndex );
        Object response = sendRequestToWorker ( target, message, true, false);
        System.out.println(response!=null);

        if (response == null) {
            System.out.println("Primary Worker with id: " + gameWorkerIndex + " failed!!! Routing to Backup Worker with id: " + backupIndex);

            // If we try to re add game on backup worker not to consider it primary
            if ( message.getType ( ).equals ( MasterMessageType.ADD_GAME ) )
                ((Game) message.getPayload ( )).setBackUp ( true );

            response = sendRequestToWorker(backupTarget, message, true, true);
            out.writeObject(response);
            out.flush();
            return;
        }

        if ( message.getType ( ).equals ( MasterMessageType.ADD_GAME ) )
            ((Game) message.getPayload ( )).setBackUp ( true );

        if ( message.getType().equals (MasterMessageType.RATE ) && response instanceof String)
            System.out.println ( "Error message returned during rating. Canceling Backup Data." );

        else if ( !message.getType ( ).equals ( MasterMessageType.PLAY ) ) {
            System.out.println ( "Synchronizing data with Backup Worker " + backupIndex + "..." );
            sendRequestToWorker ( backupTarget, message, false, false );
        }
        else if ( response instanceof BigDecimal ) {
            BigDecimal payout = (BigDecimal) response;
            Bet originalBet = (Bet) message.getPayload ( );
            PlayBackUp playBackUp = new PlayBackUp ( payout, originalBet );
            sendRequestToWorker ( backupTarget, new MasterMessage ( MasterMessageType.PLAYBACKUP, playBackUp ), false, false );
        }
        else if (message.getType ( ).equals ( MasterMessageType.PLAY ) && response instanceof String){
            System.out.println ( "Error message returned from betting. Canceling Backup Data." );
        }

        out.writeObject ( response );
        out.flush ( );
    }


    private void executeMapReducePhase (MasterMessage message) throws IOException, ClassNotFoundException {


        // Workers Health check before map reduce query
        for (WorkerInfo w : workers) {
            if (w.isActive()) {
                // If ping fails, isActive of the corresponding worker will become false
                pingWorker(w);
            }
        }

        int currentMapId = Math.abs ( randomUUID ( ).hashCode ( ) );
        message.setMapId ( currentMapId );

        ResultBox myBox = new ResultBox ( );

        synchronized (sharedResults) {
            sharedResults.put ( currentMapId, myBox );
        }

        for ( WorkerInfo w : workers ) {

            if(w.isActive()) {
                boolean includeBackUps = false;

                // Check if there is a dead worker and the current one keeps backup data of it
                for (WorkerInfo potentialDeadWorker: workers){
                    if(!potentialDeadWorker.isActive() && isBackupOf(w,potentialDeadWorker)){
                        // Send message to the worker to use its backup data
                        includeBackUps = true;
                        break;
                    }
                }
                sendRequestToWorker(w, message, false, includeBackUps);
            }
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

    private Object sendRequestToWorker(WorkerInfo target, MasterMessage message, boolean expectResponse, boolean includeBackUps) throws IOException, ClassNotFoundException {

        // In case worker is already failed
        // not to exhaust all maxRetries and occur delays
        if(!target.isActive()){
            System.out.println("The worker "+ target.getWorkerId() + " is already inactive");
            return null;
        }


        int maxRetries = 3;
        int attempt = 0;

        // maxRetries attempts are given to the worker to respond
        while (attempt < maxRetries){
            try (Socket requestSocket = new Socket(target.getWorkerIp(), target.getWorkerPort());
                 ObjectOutputStream outWorker = new ObjectOutputStream(requestSocket.getOutputStream());
                 ObjectInputStream inWorker = new ObjectInputStream(requestSocket.getInputStream());) {

                // We wait for acknowledgement message before proceeding to send the real one
                // To ensure that the connection has been established
                Object ack = inWorker.readObject();

                if ("ACK".equals(ack)) {
                    // If access to worker's backup data is needed
                    outWorker.writeBoolean(includeBackUps);
                    outWorker.flush();

                    outWorker.writeObject(message);
                    outWorker.flush();
                }

                // returns a message of success/failure of the requested operation
                if (expectResponse) {
                    return inWorker.readObject();
                }
                else{ break; }

            } catch (IOException | ClassNotFoundException  e) {
                attempt++;

                if (attempt < maxRetries) {
                    try {
                        // Wait for 1 second before the next try
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        // Restore the interrupted status to ensure the thread can terminate properly.
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    // If there are no more tries, target worker is marked as inactive
                    markWorkerAsFailed(target);
                    System.err.println("Worker " + target.getWorkerId() + " marked as INACTIVE.");
                }
            }

        }

        return null;
    }

    private int hashGameToWorker(String gameName,int numbersOfWorkers){

        int hash = Math.abs(gameName.hashCode());
        return hash % numbersOfWorkers;
    }
    private WorkerInfo getBackupWorker(int gameWorkerIndex){
        int backupIndex = (gameWorkerIndex + 1) % workers.size ();
        WorkerInfo backupTarget = workers.get ( backupIndex );
        return backupTarget;
    }

    private boolean isBackupOf(WorkerInfo backup, WorkerInfo primary) {
        return backup.getWorkerId() == (primary.getWorkerId() + 1) % workers.size();
    }

    private boolean pingWorker(WorkerInfo w) throws IOException, ClassNotFoundException {
        int maxRetries = 3;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try{
                // send dummy request to the worker, to check if the connection holds
                String response = (String) sendRequestToWorker(w, new MasterMessage(MasterMessageType.PING, null), true, false);

                return response != null;
            } catch (IOException e) {
                System.err.println("Ping attempt " + attempt + " failed for Worker " + w.getWorkerId());

                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        // If all maxTries attempts are over
        markWorkerAsFailed(w);

        return false;
    }


    private void markWorkerAsFailed(WorkerInfo failedWorker) throws IOException, ClassNotFoundException {

        // In case worker was already failed
        // not to send message to reducer again and decrease its workers counter furthermore
        if(!failedWorker.isActive()){
            System.out.println("The worker "+ failedWorker.getWorkerId() + " is already inactive");
            return;
        }

        // change Worker info active status
        failedWorker.setActive(false);
        int failedId = failedWorker.getWorkerId();

        LocalTime time = LocalTime.now();

        System.err.println("["+ time +"] !!! ALERT: Worker " + failedId + " is down. Notifying the backup node...");


        int backupIndex = (failedId + 1) % workers.size();
        WorkerInfo backupWorker = getBackupWorker(failedId);

        // Notification to the backup worker of the failed one to inform the reducer about it
        if (backupWorker.isActive()) {
            System.out.println("Directing failure notification to Backup Worker: " + backupIndex);

            // failure message construction
            MasterMessage failureNotice = new MasterMessage(MasterMessageType.WORKER_FAILURE, Integer.valueOf(failedId));

            // Message sending to the backup worker to inform reducer for the newly found failure
            sendRequestToWorker(backupWorker, failureNotice, false, false);
        } else {
            System.err.println("Critical: Backup Worker " + backupIndex + " is also inactive!");
        }


    }
}