package Worker;

import models.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import static models.SHA256Generator.computeSHA256;

public class WorkerHandler extends Thread {

    ObjectInputStream in;
    ObjectOutputStream out;
    private final HashMap<String, Game> myGames;
    private final int workerid;
    private HashMap<String, BigDecimal> gamesprofits,playersProfits,providersprofits;

    public WorkerHandler (Socket connection, HashMap<String, Game> myGames, HashMap<String, BigDecimal> profits, HashMap<String, BigDecimal> playersprofits, HashMap<String, BigDecimal> providersprofits, int workerid) {
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
        this.gamesprofits = profits;
        this.workerid=workerid;
        this.playersProfits=playersprofits;
        this.providersprofits=providersprofits;
    }

    @Override
    public void run() {
        Object response = null;
        MasterMessage msgToReducer;

        try {
            MasterMessage message = (MasterMessage) in.readObject ( );
            MasterMessageType type = message.getType ( );
            switch (type) {
                case PLAY:
                    Bet bet = (Bet) message.getPayload ( );
                    try {
                        response = CalculateProfit ( bet );
                    } catch (IllegalArgumentException | IllegalStateException e) {
                        response = "ΣΦΑΛΜΑ: " + e.getMessage ( );
                    }
                    break;

                case ADD_GAME:
                    Game gameToAdd = (Game) message.getPayload ( );

                    // When the game is first added into the app
                    synchronized (myGames){
                        if (!myGames.containsKey(gameToAdd.getGameName())) {
                            myGames.put(gameToAdd.getGameName(), gameToAdd);
                            response = "The game '" + gameToAdd.getGameName() + "' has been added successfully!";
                            System.out.println ( "The game "+gameToAdd.getGameName()+" was added to worker "+workerid);
                        }
                        // When the game already exists in the app and becomes visible to the players again
                        else if(!myGames.get(gameToAdd.getGameName()).isActive()){
                            myGames.get(gameToAdd.getGameName()).setActive(true);
                            response = "The game '" + gameToAdd.getGameName() + "' is visible again for players!";
                            System.out.println ( "The game "+gameToAdd.getGameName()+" is visible on worker "+workerid);

                        }
                        // When the game already exists in the app and is visible to the players
                        // No action is needed, just an information response to the manager that
                        // the game is already visible to the players
                        else{
                            response = "The game '" + gameToAdd.getGameName() + "' is already visible to the players!";
                        }
                    }
                    break;

                case RATE:
                    Rating rating = (Rating) message.getPayload ( );
                    try {
                        response = averageRate ( rating );
                    } catch (IllegalArgumentException e) {
                        response = null;
                    }
                    break;

                case REMOVE_GAME:
                    Game gameToDelete = (Game) message.getPayload ( );

                    // No check for the existence or the activity state of the game needs to be done
                    // as they have been checked automatically when the manager submitted the query
                    synchronized (myGames) {
                        myGames.get(gameToDelete.getGameName()).setActive(false);
                    }

                    response = "The game '" + gameToDelete.getGameName() + "' has been removed successfully!";
                    System.out.println ( "The game "+gameToDelete.getGameName()+" was disabled on worker "+workerid);

                    break;

                case MODIFY_GAME:
                    Game gameUpdate = (Game) message.getPayload();
                    synchronized (myGames) {
                        Game existingGame = myGames.get(gameUpdate.getGameName());
                        if (existingGame != null) {
                            // Ενημέρωση απλών πεδίων
                            existingGame.setProviderName(gameUpdate.getProviderName());
                            existingGame.setGameLogo(gameUpdate.getGameLogo());
                            existingGame.setMaxBet(gameUpdate.getMaxBet());


                            existingGame.setRiskLevel(gameUpdate.getRiskLevel());
                            existingGame.setMinBet(gameUpdate.getMinBet());

                            response = "The game '" + existingGame.getGameName() + "' was modified successfully!";
                        } else {
                            response = "Error: Game not found on this worker.";
                        }
                    }

                    break;
                case GET_ACTIVE_GAMES:

                    ArrayList<Game> activeGames = new ArrayList<>();
                    synchronized (myGames) {
                        for (String gameName : myGames.keySet()) {
                            Game curGame = myGames.get(gameName);
                            if (curGame.isActive())
                                activeGames.add(curGame);
                        }
                    }

                    try (Socket reducerSocket = new Socket("127.0.0.1", 43500);
                         ObjectOutputStream reducerOut = new ObjectOutputStream(reducerSocket.getOutputStream())) {

                        msgToReducer = new MasterMessage(MasterMessageType.GET_ACTIVE_GAMES, activeGames);

                        msgToReducer.setMapId(message.getMapId());

                        reducerOut.writeObject(msgToReducer);
                        reducerOut.flush();
                        System.out.println("Worker sent its results to reducer for MapId: " + message.getMapId());

                    } catch (IOException e) {
                        System.err.println("Communication error with reducer: " + e.getMessage());
                    }
                    break;


                case SEARCH:
                    SearchFilters filters = (SearchFilters) message.getPayload ( );
                    ArrayList<Game> matchedGames;
                    try {
                        matchedGames = searchgames ( filters );
                    } catch (IllegalArgumentException e) {
                        System.err.println ( "Λάθος στα φίλτρα αναζήτησης: " + e.getMessage ( ) );
                        matchedGames = new ArrayList<> ( );
                    }

                    try (Socket reducerSocket = new Socket ( "127.0.0.1", 43500 );
                         ObjectOutputStream reducerOut = new ObjectOutputStream ( reducerSocket.getOutputStream ( ) )) {

                        msgToReducer = new MasterMessage ( MasterMessageType.SEARCH, matchedGames );
                        msgToReducer.setMapId ( message.getMapId ( ) );

                        reducerOut.writeObject ( msgToReducer );
                        reducerOut.flush ( );
                        System.out.println ( "Worker έστειλε αποτελέσματα στον Reducer για MapId: " + message.getMapId ( ) );

                    } catch (IOException e) {
                        System.err.println ( "Σφάλμα επικοινωνίας με Reducer: " + e.getMessage ( ) );
                    }

                    response = null;
                    break;

                case PROVIDER_GAME_PROFITS:
                    String requestedProvider = (String) message.getPayload();
                    BigDecimal localProviderProfit = BigDecimal.ZERO;

                    synchronized (providersprofits) {
                        // profits of the requested provider
                        localProviderProfit = providersprofits.getOrDefault(requestedProvider, BigDecimal.ZERO);
                    }

                    try (Socket reducerSocket = new Socket("127.0.0.1", 43500);
                         ObjectOutputStream reducerOut = new ObjectOutputStream(reducerSocket.getOutputStream())) {


                        msgToReducer = new MasterMessage(MasterMessageType.PROVIDER_GAME_PROFITS, localProviderProfit);
                        msgToReducer.setMapId(message.getMapId());

                        reducerOut.writeObject(msgToReducer);
                        reducerOut.flush();
                    } catch (IOException e) {
                        System.err.println("Σφάλμα στην αποστολή stats στον Reducer: " + e.getMessage());
                    }


                    break;
            }

            if ( response != null ) {
                out.writeObject ( response );
                out.flush ( );
            }

        } catch (IOException | ClassNotFoundException e) {
            System.err.println ( "Σφάλμα επικοινωνίας στον WorkerHandler: " + e.getMessage ( ) );
        } finally {
            try {
                in.close();
                out.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }


    private BigDecimal CalculateProfit (Bet bet) {
        if ( bet == null ) throw new IllegalArgumentException ( "Το ποντάρισμα Bet δεν μπορεί να είναι null." );
        if ( bet.getGamename ( ) == null || bet.getGamename ( ).trim ( ).isEmpty ( ) ) {
            throw new IllegalArgumentException ( "Το όνομα του παιχνιδιού στο ποντάρισμα δεν μπορεί να είναι κενό." );
        }

        Game game = myGames.get ( bet.getGamename ( ) );
        if ( game == null ) {
            throw new IllegalArgumentException ( "Το παιχνίδι '" + bet.getGamename ( ) + "' δεν βρέθηκε σε αυτόν τον Worker." );
        }

        if ( bet.getAmount ( ).compareTo ( game.getMinBet ( ) ) < 0 || bet.getAmount ( ).compareTo ( game.getMaxBet ( ) ) > 0 ) {
            throw new IllegalArgumentException ( "Ακυρώθηκε: Το ποντάρισμα πρέπει να είναι μεταξύ "
                    + game.getMinBet ( ) + " και " + game.getMaxBet ( ) + " tokens." );
        }

        BigDecimal playerProfit,gameprofit,providerprofit,payout=BigDecimal.ZERO;
        int index;
        BigDecimal[] gamemultiplies;
        int random = getSecuredRandomNumber ( game.getGameName ( ) );
        if ( random == -1 )
            throw new IllegalStateException ( "Τεχνικό Πρόβλημα: Ακυρώθηκε το ποντάρισμα λόγω αδυναμίας παραγωγής ασφαλούς τυχαίου αριθμού." );        try {
            if ( random % 100 == 0 ) {
                payout = bet.getAmount ( ).multiply ( game.getJackpot ( ) );
            } else {
                index = random % 10;
                gamemultiplies = game.getMultipliers ( );
                payout = bet.getAmount ( ).multiply ( gamemultiplies[ index ] );
            }
            synchronized (playersProfits) {
                playerProfit=playersProfits.getOrDefault ( bet.getPlayerid (),BigDecimal.ZERO );
                playerProfit=playerProfit.subtract ( bet.getAmount ( ) );
                playerProfit=playerProfit.add ( payout );
                playersProfits.put ( bet.getPlayerid (), playerProfit);
            }
            synchronized (gamesprofits) {
                gameprofit=gamesprofits.getOrDefault ( game.getGameName (),BigDecimal.ZERO );
                gameprofit=gameprofit.add ( bet.getAmount ( ) );
                gameprofit=gameprofit.subtract ( payout );
                gamesprofits.put ( game.getGameName (), gameprofit);
            }
            synchronized (providersprofits){
                providerprofit=providersprofits.getOrDefault (game.getProviderName (),BigDecimal.ZERO  );
                providerprofit=providerprofit.add ( bet.getAmount () );
                providerprofit=providerprofit.subtract ( payout );
                providersprofits.put ( game.getProviderName (),providerprofit );
            }

        } catch (Exception e) {
            System.err.println ( "Σφάλμα κατά τον υπολογισμό κέρδους: " + e.getMessage ( ) );
        }
        return payout;
    }


    private int getSecuredRandomNumber (String gameName) {
        if ( gameName == null || gameName.trim ( ).isEmpty ( ) ) {
            throw new IllegalArgumentException ( "Το όνομα του παιχνιδιού για τον SRG δεν μπορεί να είναι κενό." );
        }
        Game g = myGames.get ( gameName );
        String secret = g.getHashKey ( );
        Socket requestSocket = null;
        ObjectOutputStream requestOut = null;
        ObjectInputStream requestIn = null;
        int randomNumber;
        String  receivedhash;

        try {
            requestSocket = new Socket ( "127.0.0.1", 5000 );
            requestOut = new ObjectOutputStream ( requestSocket.getOutputStream ( ) );
            requestIn = new ObjectInputStream ( requestSocket.getInputStream ( ) );

            requestOut.writeObject ( gameName );
            requestOut.flush ( );

            randomNumber = requestIn.readInt ( );
            receivedhash = (String) requestIn.readObject ( );
            if ( randomNumber == -1 ) {
                System.err.println ( "ΣΦΑΛΜΑ SRG: Ο SRGServer απάντησε ότι δεν βρήκε το παιχνίδι '" + gameName + "'." );                return -1;
            }
            if ( !(computeSHA256 ( randomNumber + secret )).equals (receivedhash) ) {
                System.err.println ( "ΣΦΑΛΜΑ ΑΣΦΑΛΕΙΑΣ: Τα δύο hashes δεν είναι ίδια για το παιχνίδι " + gameName + ". Πιθανή παραβίαση SRG!" );
                return -1;
            }
        } catch (UnknownHostException e) {
            throw new RuntimeException ( "Άγνωστος Host για τον SRG: " + e.getMessage ( ), e );
        } catch (IOException e) {
            throw new RuntimeException ( "Σφάλμα I/O με τον SRG: " + e.getMessage ( ), e );
        } catch (ClassNotFoundException e) {
            throw new RuntimeException ( e );
        } finally {
            try {
                requestIn.close ( );
                requestOut.close ( );
                requestSocket.close ( );
            } catch (IOException ioException) {
                ioException.printStackTrace ( );
            }
        }
        return randomNumber;
    }


    private ArrayList<Game> searchgames(SearchFilters filters) {
        if (filters == null) throw new IllegalArgumentException("Τα φίλτρα αναζήτησης δεν μπορεί να είναι null.");
        ArrayList<Game> matchedGames = new ArrayList<>();
        for (Game game : myGames.values()) {
            boolean condition1,condition2,condition3;
            if ( game.isActive ( ) ) {
                condition1=game.getStars ( ) < filters.getStars ( );

                condition2=filters.getRiskLevel ( ) != null && !filters.getRiskLevel ( ).trim ( ).isEmpty ( ) &&
                        !game.getRiskLevel ( ).equalsIgnoreCase ( filters.getRiskLevel ( ) );

                condition3=filters.getBetCategory ( ) != null && !filters.getBetCategory ( ).trim ( ).isEmpty ( ) &&
                        !game.getBetCategory ( ).equals ( filters.getBetCategory ( ) );



                if(!(condition1 || condition2 || condition3)){
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

            float totalstars = (currentStars * currentVotes) + newStarRating;
            int newVotes = currentVotes + 1;
            float average = totalstars / newVotes;
            game.setStars(average);
            game.setVotes(newVotes);
            return average;

        } else {
            throw new IllegalArgumentException("Το παιχνίδι δεν βρέθηκε σε αυτόν τον Worker.");
        }
    }
}