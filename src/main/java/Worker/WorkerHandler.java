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
        MasterMessage message = null;
        try {
            message = (MasterMessage) in.readObject ( );
        } catch (IOException e) {
            throw new RuntimeException ( e );
        } catch (ClassNotFoundException e) {
            throw new RuntimeException ( e );
        }
        Object response = null;

        switch (message.getType ( )) {
            case PLAY:
                Bet bet = (Bet) message.getPayload ( );
                response = CalculateProfit ( bet );
                break;
            case ADD_GAME:

            case RATE:
                Rating rating = (Rating) message.getPayload ( );
                response = averageRate ( rating );
                break;
            case REMOVE_GAME:

            case SEARCH:
                SearchFilters filters = (SearchFilters) message.getPayload ( );
                response = searchgames ( filters );
                break;
            case STATS:

        }
    }

    private int getSecuredRandomNumber (String gameName) {
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

            if ( randomNumber == 0 )
                System.out.println ( "ο SRG δεν βρήκε το παιχνίδι!!!" );

        } catch (UnknownHostException e) {
            throw new RuntimeException ( e );
        } catch (IOException e) {
            throw new RuntimeException ( e );
        } catch (ClassNotFoundException e) {
            throw new RuntimeException ( e );
        } finally {
            try {
                in.close ( );
                out.close ( );
                requestSocket.close ( );
            } catch (IOException ioException) {
                ioException.printStackTrace ( );
            }
        }
        return randomNumber;
    }

    private BigDecimal CalculateProfit (Bet bet) {
        BigDecimal playerProfit, systemProfitFromThisBet, playerPayout = BigDecimal.ZERO;
        int index;
        BigDecimal[] gamemultiplies;
        Game game = myGames.get ( bet.getGamename ( ) );
        int random = getSecuredRandomNumber ( game.getGameName ( ) );
        try {
            if ( random % 100 == 0 )
                playerPayout = bet.getAmount ( ).multiply ( game.getJackpot ( ) );
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

    private ArrayList<Game> searchgames (SearchFilters filters) {
        ArrayList<Game> matchedGames = new ArrayList<> ( );

        for ( Game game : myGames.values ( ) ) {
            boolean matching = true;

            if ( game.getStars ( ) < filters.getStars ( ) )
                matching = false;
            if ( !game.getRiskLevel ( ).equalsIgnoreCase ( filters.getRiskLevel ( ) ) )
                matching = false;
            if ( !game.getBetCategory ( ).equals ( filters.getBetLimit ( ) ) )
                matching = false;
            if ( !filters.getCategories ( ).contains ( game.getCategory ( ) ) )
                matching = false;

            if ( matching )
                matchedGames.add ( game );
        }
        return matchedGames;
    }

    private BigDecimal averageRate (Rating rating) {
        BigDecimal averagerate = BigDecimal.ZERO;
        Game game = myGames.get ( rating.getGameName ( ) );
        if ( game != null ) {
            int currentStars = game.getStars ( );
            int currentVotes = game.getVotes ( );
            int newStarRating = rating.getStars ( );
            int totalstars = (currentStars * currentVotes) + newStarRating;
            int newVotes = currentVotes + 1;
            int average = totalstars / newVotes;
            averagerate = new BigDecimal ( average );
            game.setStars ( totalstars );
            game.setVotes ( newVotes );
            return averagerate;
        } else {
            System.err.println ( "Το παιχνίδι για βαθμολογία δεν βρέθηκε σε αυτόν τον Worker." );
            return null;

        }

    }
}