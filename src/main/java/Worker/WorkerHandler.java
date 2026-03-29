package Worker;

import models.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import static models.SHA256Generator.computeSHA256;

public class WorkerHandler extends Thread {

    ObjectInputStream in;
    ObjectOutputStream out;
    private final HashMap<String, Game> myGames;
    private final int workerId;
    private final HashMap<String, BigDecimal> gamesProfits,playersProfits, providersProfits;

    public WorkerHandler (Socket connection, HashMap<String, Game> myGames, HashMap<String, BigDecimal> profits, HashMap<String, BigDecimal> playersProfits, HashMap<String, BigDecimal> providersProfits, int workerid) {

        try {
            out = new ObjectOutputStream(connection.getOutputStream());
            in = new ObjectInputStream(connection.getInputStream());
            // Sending acknowledgement that the stream between master and this worker
            // has been successfully established
            out.writeObject("ACK");
            out.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }

        this.myGames = myGames;
        this.gamesProfits = profits;
        this.workerId = workerid;
        this.playersProfits = playersProfits;
        this.providersProfits = providersProfits;
    }

    @Override
    public void run(){


        try {
            MasterMessage message = (MasterMessage) in.readObject();
            MasterMessageType type = message.getType();
            Object response = null;

            //-----Προστατεύει απο σφάλματα των μεθόδων execute-----
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

                    case SEARCH, GET_ACTIVE_GAMES:
                        executeMapReduceTask(message);
                        break;
                }

            }catch (IllegalArgumentException | IllegalStateException e) {
                //------Σφάλματα λογικής (π.χ. λάθος ποντάρισμα, το παιχνίδι δεν υπάρχει)----
                response = "ΣΦΑΛΜΑ ΛΟΓΙΚΗΣ: " + e.getMessage();
            } catch (IOException | ClassNotFoundException e) {
                //-----Σφάλματα δικτύου με τον SRG ή τον Reducer-----
                response = "ΣΦΑΛΜΑ ΕΣΩΤΕΡΙΚΗΣ ΕΠΙΚΟΙΝΩΝΙΑΣ (SRG/Reducer): " + e.getMessage();
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

        synchronized (myGames){
            //-------When the game is first added into the app------
            if (!myGames.containsKey(gameToAdd.getGameName())){

                myGames.put(gameToAdd.getGameName(), gameToAdd);
                gamesProfits.put(gameToAdd.getGameName(),BigDecimal.ZERO);

                try (Socket s = new Socket("127.0.0.1", 5000);
                     ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
                     ObjectInputStream in = new ObjectInputStream(s.getInputStream())){

                    out.writeByte(1); //register new game case
                    out.writeObject(gameToAdd.getGameName());
                    out.writeObject(gameToAdd.getHashKey());
                    out.flush();

                } catch (IOException e) {
                    System.err.println("SRG Registration Error: " + e.getMessage());
                }
                System.out.println ( "The game "+gameToAdd.getGameName()+" was added to worker "+ workerId);
                return "The game '" + gameToAdd.getGameName() + "' has been added successfully!";

            }
            //------When the game already exists in the app and becomes visible to the players again-----
            else if(!myGames.get(gameToAdd.getGameName()).isActive()){

                myGames.get(gameToAdd.getGameName()).setActive(true);
                System.out.println ( "The game "+gameToAdd.getGameName()+" is visible on worker "+ workerId);
                return "The game '" + gameToAdd.getGameName() + "' is visible again for players!";
            }
            // When the game already exists in the app and is visible to the players
            else{
                return "The game '" + gameToAdd.getGameName() + "' is already visible to the players!";
            }
        }
    }

    private String executeRemoveGame(Game gameToDelete){

        synchronized (myGames) {
            myGames.get(gameToDelete.getGameName()).setActive(false);
        }
        System.out.println ( "The game "+gameToDelete.getGameName()+" was disabled on worker "+ workerId);
        return "The game '" + gameToDelete.getGameName() + "' has been removed successfully!";
    }

    private String executeModifyGame(Game gameUpdate){

        synchronized (myGames){

            Game existingGame = myGames.get(gameUpdate.getGameName());
            if (existingGame != null) {

                existingGame.setProviderName(gameUpdate.getProviderName());
                existingGame.setGameLogo(gameUpdate.getGameLogo());
                existingGame.setMaxBet(gameUpdate.getMaxBet());

                existingGame.setRiskLevel(gameUpdate.getRiskLevel());
                existingGame.setMinBet(gameUpdate.getMinBet());

                return "The game '" + existingGame.getGameName() + "' was modified successfully!";
            } else {
                throw new IllegalArgumentException("Error: Game not found on this worker.");
            }
        }
    }

    private void executeMapReduceTask(MasterMessage message) throws IOException{

        ArrayList<Game> activeGames = new ArrayList<>();

        if (message.getType() == MasterMessageType.SEARCH) {
            activeGames = searchGames((SearchFilters) message.getPayload());
        } else {
            synchronized (myGames) {
                for (Game g : myGames.values()) {
                    if (g.isActive()) activeGames.add(g);
                }
            }
        }
        sendToReducer(message.getType(), activeGames, message.getMapId());
    }

    private void executeProviderGamesProfits(MasterMessage message){
        HashMap<String,BigDecimal> providerGamesProfits = new HashMap<>();
        String providerName = (String) message.getPayload();

        synchronized (myGames) {
            for (String gameName : myGames.keySet()) {
                Game curGame = myGames.get(gameName);
                BigDecimal curGameProfit = gamesProfits.get(gameName)!=null?gamesProfits.get(gameName):BigDecimal.ZERO;

                if (curGame.getProviderName().equalsIgnoreCase(providerName)) {
                    providerGamesProfits.put(gameName, curGameProfit);
                }
            }
        }

        sendToReducer(message.getType(),providerGamesProfits,message.getMapId());
    }

    private void executeGamesProfits(MasterMessage message){
        synchronized (gamesProfits) {
            sendToReducer(message.getType(), new HashMap<String, BigDecimal>(gamesProfits), message.getMapId());
        }
    }

    private void executePlayersProfits(MasterMessage message){
        synchronized (playersProfits) {
            sendToReducer(message.getType(), new HashMap<String, BigDecimal>(playersProfits), message.getMapId());
        }
    }

    private <T> void sendToReducer(MasterMessageType type, T payload, int mapId)  {

        try (Socket reducerSocket = new Socket("127.0.0.1", 43500);
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

        if ( bet == null ) throw new IllegalArgumentException ( "Το ποντάρισμα Bet δεν μπορεί να είναι null." );
        if ( bet.getGameName() == null || bet.getGameName().trim().isEmpty()) {
            throw new IllegalArgumentException ( "Το όνομα του παιχνιδιού στο ποντάρισμα δεν μπορεί να είναι κενό." );
        }

        Game game = myGames.get ( bet.getGameName( ) );
        if ( game == null ) {
            throw new IllegalArgumentException ( "Το παιχνίδι '" + bet.getGameName( ) + "' δεν βρέθηκε σε αυτόν τον Worker." );
        }

        if ( bet.getAmount ( ).compareTo ( game.getMinBet ( ) ) < 0 || bet.getAmount ( ).compareTo ( game.getMaxBet ( ) ) > 0 ) {
            throw new IllegalArgumentException ( "Ακυρώθηκε: Το ποντάρισμα πρέπει να είναι μεταξύ "
                    + game.getMinBet ( ) + " και " + game.getMaxBet ( ) + " tokens." );
        }

        BigDecimal playerProfit,gameprofit,providerprofit,payout=BigDecimal.ZERO;
        int index;
        BigDecimal[] gameMultiplies;
        int random = getSecuredRandomNumber ( game.getGameName ( ) );
        if ( random == -1 )
            throw new IllegalStateException ( "Τεχνικό Πρόβλημα: Ακυρώθηκε το ποντάρισμα λόγω αδυναμίας παραγωγής ασφαλούς τυχαίου αριθμού." );        try {
            if ( random % 100 == 0 ) {
                payout = bet.getAmount ( ).multiply ( game.getJackpot ( ) );
            } else {
                index = random % 10;
                gameMultiplies = game.getMultipliers ( );
                payout = bet.getAmount ( ).multiply ( gameMultiplies[ index ] );
            }
            synchronized (playersProfits) {
                playerProfit=playersProfits.getOrDefault ( bet.getPlayerId(),BigDecimal.ZERO );
                playerProfit=playerProfit.subtract ( bet.getAmount ( ) );
                playerProfit=playerProfit.add ( payout );
                playersProfits.put ( bet.getPlayerId(), playerProfit);
            }
            synchronized (gamesProfits) {
                gameprofit= gamesProfits.getOrDefault ( game.getGameName (),BigDecimal.ZERO );
                gameprofit=gameprofit.add ( bet.getAmount ( ) );
                gameprofit=gameprofit.subtract ( payout );
                gamesProfits.put ( game.getGameName (), gameprofit);
            }
            synchronized (providersProfits){
                providerprofit= providersProfits.getOrDefault (game.getProviderName (),BigDecimal.ZERO  );
                providerprofit=providerprofit.add ( bet.getAmount () );
                providerprofit=providerprofit.subtract ( payout );
                providersProfits.put ( game.getProviderName (),providerprofit );
            }

        } catch (Exception e) {
            System.err.println ( "Σφάλμα κατά τον υπολογισμό κέρδους: " + e.getMessage ( ) );
        }
        return payout;
    }


    private int getSecuredRandomNumber(String gameName) throws IOException, ClassNotFoundException, IllegalArgumentException{

        if ( gameName == null || gameName.trim ( ).isEmpty ( ) ) {
            throw new IllegalArgumentException ( "Το όνομα του παιχνιδιού για τον SRG δεν μπορεί να είναι κενό." );
        }
        Game g = myGames.get ( gameName );
        String secret = g.getHashKey ( );

        try(Socket requestSocket = new Socket ( "127.0.0.1", 5000 );
            ObjectOutputStream requestOut = new ObjectOutputStream(requestSocket.getOutputStream());
            ObjectInputStream requestIn = new ObjectInputStream(requestSocket.getInputStream());) {

            requestOut.writeByte(0); //generate number case
            requestOut.writeObject(gameName);
            requestOut.flush();

            int randomNumber = requestIn.readInt();
            String receivedHash = (String) requestIn.readObject();
            if (randomNumber == -1) {
                throw new IllegalStateException("SRG Error: Ο SRGServer απάντησε ότι δεν βρήκε το παιχνίδι '" + gameName);
            }
            if (!(computeSHA256(randomNumber + secret)).equals(receivedHash)) {
                throw new IllegalStateException("SECURE Error: Τα δύο hashes δεν είναι ίδια. Πιθανή παραβίαση SRG!");

            }
            return randomNumber;
        }
    }


    private ArrayList<Game> searchGames(SearchFilters filters) {

        if (filters == null) throw new IllegalArgumentException("Τα φίλτρα αναζήτησης δεν μπορεί να είναι null.");

        ArrayList<Game> matchedGames = new ArrayList<>();
        for (Game game : myGames.values()) {

            if ( game.isActive ( ) ) {
                boolean condition1 = game.getStars() < filters.getMinstars() || game.getStars() > filters.getMaxstars();

                boolean condition2=filters.getRiskLevel() != null && !filters.getRiskLevel().trim ().isEmpty() &&
                        !game.getRiskLevel().equalsIgnoreCase(filters.getRiskLevel());

                boolean condition3=filters.getBetCategory() != null && !filters.getBetCategory().trim().isEmpty() &&
                        !game.getBetCategory().equals(filters.getBetCategory());



                if(!condition1 && !condition2 && !condition3){
                    matchedGames.add ( game );
                }
            }
        }
        return matchedGames;
    }

    private float averageRate(Rating rating) {

        if (rating == null) throw new IllegalArgumentException("Η αξιολόγηση  δεν μπορεί να είναι null.");
        if (rating.getGameName() == null || rating.getGameName().trim().isEmpty()) {
            throw new IllegalArgumentException("Το όνομα του παιχνιδιού δεν μπορεί να είναι κενό.");
        }
        if (rating.getStars() < 1 || rating.getStars() > 5) {
            throw new IllegalArgumentException("Η βαθμολογία πρέπει να είναι από 1 έως 5 αστέρια.");
        }

        Game game = myGames.get(rating.getGameName());

        if (game != null) {
            float currentStars = game.getStars();
            int currentVotes = game.getVotes();
            float newStarRating = rating.getStars();
            float totalStars = (currentStars * currentVotes) + newStarRating;
            int newVotes = currentVotes + 1;
            float average = Math.round((totalStars / newVotes) * 2) / 2.0f;
            game.setStars(average);
            game.setVotes(newVotes);
            return average;

        } else {
            throw new IllegalArgumentException("Το παιχνίδι δεν βρέθηκε σε αυτόν τον Worker.");
        }
    }
}