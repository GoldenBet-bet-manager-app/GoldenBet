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

public class WorkerHandler extends Thread {


    ObjectInputStream in;
    ObjectOutputStream out;
    private HashMap<String, Game> myGames = new HashMap<> ( );
    private HashMap<String, BigDecimal> profits = new HashMap<> ( );
    private HashMap<String, BigDecimal> playerProfits = new HashMap<> ( );

    public WorkerHandler (Socket connection, HashMap<String, Game> myGames, HashMap<String, BigDecimal> profits) {

        try {
            out = new ObjectOutputStream ( connection.getOutputStream ( ) );
            in = new ObjectInputStream ( connection.getInputStream ( ) );
        } catch (IOException e) {
            e.printStackTrace ( );
        }
        this.myGames = myGames;
        this.profits = profits;
    }


    @Override
    public void run ( ) {

        ArrayList<Game> matchedGames = new ArrayList<>();
        MasterMessage msgToReducer;
        Object response = null;

        try{

            MasterMessage message = (MasterMessage) in.readObject();
            MasterMessageType type = message.getType();

            switch(type){
                case PLAY:

                    Bet bet = (Bet) message.getPayload ( );
                    response = CalculateProfit ( bet );
                    break;

                case ADD_GAME:

                    Game game = (Game) message.getPayload ( );
                    // When the game is first added into the app
                    if (!myGames.containsKey(game.getGameName())) {
                        myGames.put(game.getGameName(), game);
                        response = "The game '" + game.getGameName() + "' has been added successfully!";
                    }
                    // When the game already exists in the app and becomes visible to the players again
                    else if(!myGames.get(game.getGameName()).isActive()){
                        response = "The game '" + game.getGameName() + "' is visible again for players!";
                    }
                    // When the game already exists in the app and is visible to the players
                    // No action is needed, just an inforation response to the manager that
                    // the game is already visible to the players
                    else{
                        response = "The game '" + game.getGameName() + "' is already visible to the players!";
                    }

                    break;

                case RATE:

                    Rating rating = (Rating) message.getPayload ( );
                    response = averageRate ( rating );
                    break;

                case REMOVE_GAME:

                    break;

                case GET_ACTIVE_GAMES:

                    ArrayList<Game> activeGames = new ArrayList<>();
                    for (String gameName : myGames.keySet()){
                        Game curGame = myGames.get(gameName);
                        if(curGame.isActive())
                            activeGames.add(curGame);
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

                    SearchFilters filters = (SearchFilters) message.getPayload();
                    matchedGames = searchgames(filters);

                    try (Socket reducerSocket = new Socket("127.0.0.1", 43500);
                         ObjectOutputStream reducerOut = new ObjectOutputStream(reducerSocket.getOutputStream())) {

                        msgToReducer = new MasterMessage(MasterMessageType.SEARCH, matchedGames);

                        msgToReducer.setMapId(message.getMapId());

                        reducerOut.writeObject(msgToReducer);
                        reducerOut.flush();
                        System.out.println("Worker έστειλε αποτελέσματα στον Reducer για MapId: " + message.getMapId());

                    } catch (IOException e) {
                        System.err.println("Σφάλμα επικοινωνίας με Reducer: " + e.getMessage());
                    }

                    response = null;
                    break;

                case STATS:

                    break;


            }

            if (response != null) {
                out.writeObject(response);
                out.flush();
            }


        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Σφάλμα επικοινωνίας στον WorkerHandler: " + e.getMessage());
        } finally {
            try {
                if (in != null) in.close();
                if (out != null) out.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }


    private int getSecuredRandomNumber(String gameName){

        Socket requestSocket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        int randomNumber = 0;

        try {
            requestSocket = new Socket ( "127.0.0.1", 5000 );
            out = new ObjectOutputStream ( requestSocket.getOutputStream ( ) );
            in = new ObjectInputStream ( requestSocket.getInputStream ( ) );

            out.writeObject ( gameName );
            out.flush ( );

            randomNumber = in.readInt ( );
            String hash = (String) in.readObject ( );

            if(randomNumber == 0) System.out.println ( "ο SRG δεν βρήκε το παιχνίδι!!!" );

        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (requestSocket != null) requestSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
        return randomNumber;
    }

    private BigDecimal CalculateProfit(Bet bet){

        BigDecimal playerProfit, systemProfitFromThisBet, playerPayout = BigDecimal.ZERO;
        int index;
        BigDecimal[] gamemultiplies;
        Game game = myGames.get(bet.getGamename());
        int random = getSecuredRandomNumber(game.getGameName());

        try {
            if ( random % 100 == 0 )
                playerPayout = bet.getAmount().multiply(game.getJackpot());
            else {
                index = random % 10;
                gamemultiplies = game.getMultipliers ( );
                playerPayout = bet.getAmount ( ).multiply ( gamemultiplies[ index ] );
            }

            systemProfitFromThisBet = bet.getAmount ( ).subtract ( playerPayout );
            playerProfit = playerPayout.subtract ( bet.getAmount ( ) );

            synchronized (profits) {
                BigDecimal currentProfit = profits.getOrDefault ( game.getGameName ( ), BigDecimal.ZERO );
                profits.put ( game.getGameName ( ), currentProfit.add ( systemProfitFromThisBet ) );
            }

        } catch (Exception e) {
            System.err.println ( "Σφάλμα κατά τον υπολογισμό κέρδους: " + e.getMessage ( ) );
        }
        return playerPayout;
    }

    private ArrayList<Game> searchgames(SearchFilters filters){

        ArrayList<Game> matchedGames = new ArrayList<>();
        for(Game game : myGames.values()){

            boolean matching = true;

            if (game.getStars() < filters.getStars())
                matching = false;
            if (!filters.getRiskLevel().isEmpty() && !game.getRiskLevel().equalsIgnoreCase(filters.getRiskLevel()))
                matching = false;
            if (!filters.getBetCategory().isEmpty() && !game.getBetCategory().equals(filters.getBetCategory()))
                matching = false;
            if (!filters.getCategories().isEmpty() && !filters.getCategories().contains(game.getCategory()))
                matching = false;

            if(!game.isActive())
                matching = false;

            if (matching) matchedGames.add(game);

        }
        return matchedGames;
    }

    private float averageRate(Rating rating){

        float averagerate = 0.0F;
        Game game = myGames.get(rating.getGameName());

        if ( game != null ) {
            float currentStars = game.getStars ( );
            int currentVotes = game.getVotes ( );
            float newStarRating = rating.getStars ( );
            float totalstars = (currentStars * currentVotes) + newStarRating;
            int newVotes = currentVotes + 1;
            float average = totalstars / newVotes;
            game.setStars ( average );
            game.setVotes ( newVotes );
            return averagerate;

        } else {
            System.err.println ( "Το παιχνίδι για βαθμολογία δεν βρέθηκε σε αυτόν τον Worker." );
            return 0;
        }
    }


}