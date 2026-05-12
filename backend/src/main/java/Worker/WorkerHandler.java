package Worker;



import Models.*;
import utils.ConfigLoader;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;

import static Worker.Worker.backupPlayersProfits;
import static Worker.Worker.primaryPlayersProfits;
import static Models.SHA256Generator.computeSHA256;

public class WorkerHandler extends Thread {

    ObjectInputStream in;
    ObjectOutputStream out;
    private final HashMap<String, Game> myGames;
    private final int workerId;
    private final HashMap<String, BigDecimal> gamesProfits;


    public WorkerHandler (Socket connection, HashMap<String, Game> myGames, HashMap<String, BigDecimal> profits, int workerid) {

        try {
            out = new ObjectOutputStream(connection.getOutputStream());
            in = new ObjectInputStream(connection.getInputStream());

            // Sending acknowledgement that the stream between master and this worker
            // has been successfully established
            out.writeObject("ACK");
            out.flush();

            // Whether to include backup data in MapReduce queries
            Worker.setIncludeBackUps(in.readBoolean());

        } catch (IOException e) {
            e.printStackTrace();
        }

        this.myGames = myGames;
        this.gamesProfits = profits;
        this.workerId = workerid;

    }

    @Override
    public void run(){


        try {
            MasterMessage message = (MasterMessage) in.readObject();
            MasterMessageType type = message.getType();
            Object response = null;

            try {
                switch (type) {
                    case PLAY:
                        response = CalculateProfit((Bet)message.getPayload());
                        break;

                    case ADD_GAME:
                        response = executeAddGame((Game) message.getPayload());
                        break;

                    case RATE:
                        response = averageRate((Rating) message.getPayload());
                        break;

                    case REMOVE_GAME:
                        response = executeRemoveGame((Game) message.getPayload());
                        break;

                    case PROVIDER_GAME_PROFITS:
                        executeProviderGamesProfits(message);
                        break;
                    case GAMES_PROFITS:
                        executeGamesProfits(message);
                        break;
                    case PLAYERS_PROFITS:
                        executePlayersProfits(message);
                        break;
                    case MODIFY_GAME:
                        response = executeModifyGame((Game) message.getPayload());
                        break;

                    case SEARCH, GET_ACTIVE_GAMES, GET_ALL_GAMES:
                        executeMapReduceTask(message);
                        break;
                    case GET_HISTORY:
                        executeHistoryMapReduceTask(message);
                        break;
                    case PLAYBACKUP:
                        PlayBackUp backUpPayout=(PlayBackUp) message.getPayload ();
                        handleTransactionStorage(backUpPayout.getBet(), backUpPayout.getPayout(), true);
                        updateProfits ( backUpPayout.getBet (),backUpPayout.getPayout () );
                        break;
                    case WORKER_FAILURE:
                        Integer failedId =  (Integer) message.getPayload();
                        System.out.println("Worker " + workerId + " received failure notice for Worker " + failedId);
                        sendToReducer(message.getType(), failedId, message.getMapId());
                        break;
                    case PING:
                        response = "The connection between Master and the worker with id " + workerId + " holds.";
                        break;
                }

            }catch (IllegalArgumentException | IllegalStateException e) {
                response =  e.getMessage();
            } catch (IOException | ClassNotFoundException e) {
                response = "INTERNAL COMMUNICATION ERROR (SRG/Reducer): " + e.getMessage();
                System.err.println("Worker " + workerId + " internal network error: " + e.getMessage());
            }

            if ( response != null ) {
                out.writeObject ( response );
                out.flush ( );
            }


        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Master communication error in WorkerHandler " + workerId + ": " + e.getMessage());
        } finally {
            try {
                in.close();
                out.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    private String executeAddGame(Game gameToAdd) throws IOException{

        String gameDataType = gameToAdd.isBackUp() ? "Back Up":"Primary";

        synchronized (myGames){
            //-------When the game is first added into the app------
            if (!myGames.containsKey(gameToAdd.getGameName())){
                myGames.put(gameToAdd.getGameName(), gameToAdd);
                gamesProfits.put(gameToAdd.getGameName(),BigDecimal.ZERO);

                if(!gameToAdd.isBackUp()) {

                    // DYNAMIC SRG CONNECTION
                    String srgIp = ConfigLoader.getIp("SRG");
                    int srgPort = ConfigLoader.getPort("SRG");

                    try (Socket s = new Socket(srgIp, srgPort);
                         ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
                         ObjectInputStream in = new ObjectInputStream(s.getInputStream())) {

                        out.writeByte(1); //register new game case
                        out.writeObject(gameToAdd.getGameName());
                        out.writeObject(gameToAdd.getHashKey());
                        out.flush();

                    } catch (IOException e) {
                        System.err.println("SRG Registration Error: " + e.getMessage());
                    }
                }

                System.out.println ( "The ("+ gameDataType +") game "+gameToAdd.getGameName()+" was added to worker "+ workerId);
                return "The game '" + gameToAdd.getGameName() + "' has been added successfully!";

            }
            //------When the game already exists in the app and becomes visible to the players again-----
            else if(!myGames.get(gameToAdd.getGameName()).isActive()){

                myGames.get(gameToAdd.getGameName()).setActive(true);
                System.out.println ( "The ("+ gameDataType +") game "+gameToAdd.getGameName()+" is visible on worker "+ workerId);
                return "The game '" + gameToAdd.getGameName() + "' is visible again for players!";
            }
            // When the game already exists in the app and is visible to the players
            else{
                return "The game '" + gameToAdd.getGameName() + "' is already visible to the players!";
            }
        }
    }

    private String executeRemoveGame(Game gameToDelete){

        String gameDataType;

        synchronized (myGames) {
            myGames.get(gameToDelete.getGameName()).setActive(false);
            gameDataType = myGames.get(gameToDelete.getGameName()).isBackUp()? "Back Up":"Primary";
        }
        System.out.println ( "The ("+ gameDataType +") game "+gameToDelete.getGameName()+" was disabled on worker "+ workerId);
        return "The game '" + gameToDelete.getGameName() + "' has been removed successfully!";
    }

    private String executeModifyGame(Game gameUpdate){

        String gameDataType;

        synchronized (myGames){

            Game existingGame = myGames.get(gameUpdate.getGameName());

            if (existingGame != null) {

                existingGame.setProviderName(gameUpdate.getProviderName());
                existingGame.setGameLogoPath(gameUpdate.getGameLogoPath());
                existingGame.setMaxBet(gameUpdate.getMaxBet());

                existingGame.setRiskLevel(gameUpdate.getRiskLevel());
                existingGame.setMinBet(gameUpdate.getMinBet());

                return "The game '" + existingGame.getGameName() + "' was modified successfully!";
            } else {
                throw new IllegalArgumentException("Error: Game not found on this worker.");
            }
        }
    }

    private void executeMapReduceTask(MasterMessage message) throws IOException {

        ArrayList<Game> gamesToReducer = new ArrayList<>();

        if (message.getType() == MasterMessageType.SEARCH) {
            gamesToReducer = searchGames((SearchFilters) message.getPayload());
        } else {
            synchronized (myGames) {
                for (Game g : myGames.values()) {
                    if(!g.isBackUp() || (g.isBackUp() && Worker.includeBackUps()))
                        if (g.isActive() || (!g.isActive() && message.getType() == MasterMessageType.GET_ALL_GAMES))
                            gamesToReducer.add(g);
                }
            }
        }
        sendToReducer(message.getType(), gamesToReducer, message.getMapId());
    }


    private void executeHistoryMapReduceTask(MasterMessage message) throws IOException {
        HistoryFilters filters = (HistoryFilters) message.getPayload();
        ArrayList<Transaction> matchedTxs = new ArrayList<>();

        synchronized (Worker.primaryTransactions) {
            ArrayList<Transaction> primary = Worker.primaryTransactions.get(filters.getUsername());
            if (primary != null) {
                matchedTxs.addAll(applyHistoryFilters(primary, filters));
            }
        }

        if (Worker.includeBackUps()) {
            synchronized (Worker.backupTransactions) {
                ArrayList<Transaction> backup = Worker.backupTransactions.get(filters.getUsername());
                if (backup != null) {
                    matchedTxs.addAll(applyHistoryFilters(backup, filters));
                }
            }
        }

        sendToReducer(message.getType(), matchedTxs, message.getMapId());

        System.out.println("Worker " + workerId + " sent " + matchedTxs.size() + " filtered transactions to Reducer.");
    }

    private ArrayList<Transaction> applyHistoryFilters(ArrayList<Transaction> list, HistoryFilters filters) {
        ArrayList<Transaction> result = new ArrayList<>();
        ArrayList<String> selected = filters.getSelectedTypes();
        String query = (filters.getSearchQuery() != null) ? filters.getSearchQuery().toLowerCase() : "";

        for (Transaction tx : list) {
            boolean typeMatch = false;

            // Filters logic
            if (selected.contains("All")) {
                typeMatch = true;
            } else {

                // Check for Wins
                if (selected.contains("Wins") && tx.getType().equals("GAME_RESULT")
                        && tx.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                    typeMatch = true;
                }

                // Check for Bets
                if (selected.contains("Bets") && tx.getType().equals("GAME_BET")) {
                    typeMatch = true;
                }

            }

            // Search Query Logic
            boolean searchMatch = query.isEmpty() || tx.getDescription().toLowerCase().contains(query);

            if (typeMatch && searchMatch) {
                result.add(tx);
            }
        }
        return result;
    }

    private void executeProviderGamesProfits(MasterMessage message){
        HashMap<String,BigDecimal> providerGamesProfits = new HashMap<>();
        String providerName = (String) message.getPayload();

        synchronized (myGames) {
            for (String gameName : myGames.keySet()) {
                Game curGame = myGames.get(gameName);
                BigDecimal curGameProfit = gamesProfits.get(gameName)!=null?gamesProfits.get(gameName):BigDecimal.ZERO;

                // If the game is not backUp
                // OR it is backup and the master wants this worker to include backup games
                if (!curGame.isBackUp() || (curGame.isBackUp() && Worker.includeBackUps())) {

                    // Provider check
                    if (curGame.getProviderName().equalsIgnoreCase(providerName)) {
                        providerGamesProfits.put(gameName, curGameProfit);
                    }
                }

            }
        }

        sendToReducer(message.getType(),providerGamesProfits,message.getMapId());
    }

    private void executeGamesProfits(MasterMessage message){
        HashMap<String, BigDecimal> filteredProfits = new HashMap<>();


        synchronized (myGames) {
            synchronized (gamesProfits) {
                for (String gameName : gamesProfits.keySet()) {
                    Game curGame = myGames.get(gameName);

                    // If the game is not backUp
                    // OR it is backup and the master wants this worker to include backup games
                    if (!curGame.isBackUp() || (curGame.isBackUp() && Worker.includeBackUps())) {
                        filteredProfits.put(gameName, gamesProfits.get(gameName));
                    }
                }
            }
        }

        sendToReducer(message.getType(), filteredProfits, message.getMapId());
    }

    private void executePlayersProfits(MasterMessage message){
        HashMap<String, BigDecimal> finalStatsToSend = new HashMap<>();

        synchronized (primaryPlayersProfits) {
            // Primary data are always sent
            finalStatsToSend.putAll(primaryPlayersProfits);
        }

        if (Worker.includeBackUps()) {
            synchronized (backupPlayersProfits) {
                // Backup data are sent if and only if the corresponding worker has failed
                for (String playerId : backupPlayersProfits.keySet()) {
                    BigDecimal backupVal = backupPlayersProfits.get(playerId);
                    BigDecimal currentVal = finalStatsToSend.getOrDefault(playerId, BigDecimal.ZERO);
                    finalStatsToSend.put(playerId, currentVal.add(backupVal));
                }
            }
        }

        sendToReducer(message.getType(), finalStatsToSend, message.getMapId());
    }

    private <T> void sendToReducer(MasterMessageType type, T payload, int mapId)  {
        String redIp = ConfigLoader.getIp("REDUCER");
        int redPort = ConfigLoader.getPort("REDUCER");

        try (Socket reducerSocket = new Socket(redIp, redPort);
             ObjectOutputStream reducerOut = new ObjectOutputStream(reducerSocket.getOutputStream())) {

            MasterMessage msg = new MasterMessage(type, payload);
            msg.setMapId(mapId);

            reducerOut.writeObject(msg);
            reducerOut.flush();
            System.out.println("Worker " + workerId + " sent results to reducer for MapId: " + mapId);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }




    private BigDecimal CalculateProfit (Bet bet) throws IOException, ClassNotFoundException {
        BigDecimal payout = BigDecimal.ZERO;
        Game game = myGames.get ( bet.getGameName ( ) );
        if ( bet.getAmount ( ).compareTo ( game.getMinBet ( ) ) < 0 || bet.getAmount ( ).compareTo ( game.getMaxBet ( ) ) > 0 ) {
            throw new IllegalArgumentException ( "Cancelled: The bet must be between "
                    + game.getMinBet ( ) + " and " + game.getMaxBet ( ) + " tokens." );
        }

        int index;
        BigDecimal[] gameMultiplies;
        int random = getSecuredRandomNumber ( game.getGameName ( ) );
        if ( random == -1 )
            throw new IllegalStateException ( "Technical Issue: Bet cancelled due to failure to generate a secure random number." );

        if ( random % 100 == 0 ) {
            payout = bet.getAmount ( ).multiply ( game.getJackpot ( ) );
        } else {
            index = random % 10;
            gameMultiplies = game.getMultipliers ( );
            payout = bet.getAmount ( ).multiply ( gameMultiplies[ index ] );
        }

        // Save primary Transactions
        handleTransactionStorage(bet, payout, false);

        updateProfits ( bet, payout );
        System.out.print ( payout );
        return payout;
    }

    private void updateProfits(Bet bet,BigDecimal payout) {
        Game game = myGames.get ( bet.getGameName ( ) );
        BigDecimal  gameprofit = BigDecimal.ZERO;


        try {
            if (game.isBackUp()) {
                synchronized (backupPlayersProfits) { // Κλειδώνεις απευθείας το πεδίο της κλάσης
                    updatePlayerProfitsMap(backupPlayersProfits, bet, payout);
                }
            } else {
                synchronized (primaryPlayersProfits) { // Κλειδώνεις απευθείας το πεδίο της κλάσης
                    updatePlayerProfitsMap(primaryPlayersProfits, bet, payout);
                }
            }

            synchronized (gamesProfits) {
                gameprofit = gamesProfits.getOrDefault ( game.getGameName ( ), BigDecimal.ZERO );
                gameprofit = gameprofit.add ( bet.getAmount ( ) );
                gameprofit = gameprofit.subtract ( payout );
                gamesProfits.put ( game.getGameName ( ), gameprofit );
            }

        } catch (Exception e) {
            System.err.println ( "Error while updating profit HashMaps: " + e.getMessage ( ) );
        }
    }

    // Auxiliary method to save 2 Transaction types (Bet & Payout)
    private void handleTransactionStorage(Bet bet, BigDecimal payout, boolean isBackup) {
        HashMap<String, ArrayList<Transaction>> targetMap = isBackup ? Worker.backupTransactions : Worker.primaryTransactions;

        synchronized (targetMap) {
            ArrayList<Transaction> userTxs = targetMap.computeIfAbsent(bet.getPlayerid(), k -> new ArrayList<>());

            userTxs.add(new Transaction(
                    bet.getPlayerid(),
                    "GAME_BET",
                    bet.getAmount().negate(),
                    "Bet on " + bet.getGameName()
            ));

            userTxs.add(new Transaction(
                    bet.getPlayerid(),
                    "GAME_RESULT",
                    payout,
                    "Payout from " + bet.getGameName()
            ));
        }
    }

    // Auxiliary method to update player profits maps after play
    private void updatePlayerProfitsMap(HashMap<String, BigDecimal> map, Bet bet, BigDecimal payout) {
        BigDecimal playerProfit = map.getOrDefault(bet.getPlayerid (), BigDecimal.ZERO);
        playerProfit = playerProfit.subtract(bet.getAmount()).add(payout);
        map.put(bet.getPlayerid (), playerProfit);
    }


    private int getSecuredRandomNumber(String gameName) throws IOException, ClassNotFoundException, IllegalArgumentException{

        if ( gameName == null || gameName.trim ( ).isEmpty ( ) ) {
            throw new IllegalArgumentException ( "The game name for SRG cannot be empty." );
        }

        Game g = myGames.get ( gameName );
        String secret = g.getHashKey ( );


        String srgIp = ConfigLoader.getIp("SRG");
        int srgPort = ConfigLoader.getPort("SRG");


        try(Socket requestSocket = new Socket ( srgIp, srgPort );
            ObjectOutputStream requestOut = new ObjectOutputStream(requestSocket.getOutputStream());
            ObjectInputStream requestIn = new ObjectInputStream(requestSocket.getInputStream());) {

            requestOut.writeByte(0); //generate number case
            requestOut.writeObject(gameName);
            requestOut.flush();

            int randomNumber = requestIn.readInt();
            String receivedHash = (String) requestIn.readObject();
            if (randomNumber == -1) {
                throw new IllegalStateException("SRG Error: SRGServer responded that it did not find the game '" + gameName);
            }
            if (!(computeSHA256(randomNumber + secret)).equals(receivedHash)) {
                throw new IllegalStateException("SECURE Error: The two hashes do not match. Possible SRG breach!");

            }
            return randomNumber;
        }
    }


    private ArrayList<Game> searchGames(SearchFilters filters) {

        if (filters == null) throw new IllegalArgumentException("Search filters cannot be null.");

        ArrayList<Game> matchedGames = new ArrayList<>();
        for (Game game : myGames.values()) {

            if(!game.isBackUp() || (game.isBackUp() && Worker.includeBackUps())){
                if ( game.isActive ( ) ) {
                    boolean condition1 = game.getStars() < filters.getMinstars() || game.getStars() > filters.getMaxstars();

                    boolean condition2= false;
                    if (filters.getRiskLevel() != null && !filters.getRiskLevel().trim().isEmpty()) {
                        condition2 = true;
                        String[] risks = filters.getRiskLevel().split(",");
                        for (String risk : risks) {
                            if (game.getRiskLevel().equalsIgnoreCase(risk.trim())) {
                                condition2 = false;
                                break;
                            }
                        }
                    }
                    boolean condition3=false;
                    if(filters.getBetCategory ()!=null && !filters.getBetCategory ().trim ().isEmpty ()){
                        condition3=true;
                        String[] bets=filters.getBetCategory ().split ( "," );
                        for(String b:bets){
                            if (game.getBetCategory ().equalsIgnoreCase(b.trim())) {
                                condition3 = false;
                                break;
                            }
                        }
                    }

                    if(!condition1 && !condition2 && !condition3){
                        matchedGames.add ( game );
                    }
                }
            }

        }
        return matchedGames;
    }

    private float averageRate(Rating rating) {

        if (rating == null) throw new IllegalArgumentException("The rating cannot be null.");
        if (rating.getGameName() == null || rating.getGameName().trim().isEmpty()) {
            throw new IllegalArgumentException("The game name cannot be empty.");
        }
        if (rating.getStars() < 1 || rating.getStars() > 5) {
            throw new IllegalArgumentException("The rating must be from 1 to 5 stars.");
        }

        Game game = myGames.get(rating.getGameName());

        if (game != null) {
            float currentStars = game.getStars();
            int currentVotes = game.getVotes();
            float newStarRating = rating.getStars();
            float totalStars = (currentStars * currentVotes) + newStarRating;
            int newVotes = currentVotes + 1;
            float average = totalStars / newVotes;
            game.setStars(average);
            game.setVotes(newVotes);
            return Math.round(average * 2) / 2.0f;

        } else {
            throw new IllegalArgumentException("The game was not found on this Worker.");
        }
    }
}