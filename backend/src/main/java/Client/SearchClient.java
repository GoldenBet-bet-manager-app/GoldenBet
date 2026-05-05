package Client;

import Player.PlayerApp;
import Models.Game;

import java.util.ArrayList;

public class SearchClient extends Client {
    private PlayerApp playerApp;

    public SearchClient (String ip, int port, Object object, PlayerApp playerApp) {
        super ( ip, port, object );
        this.playerApp = playerApp;
    }

    @Override
    public void run ( ) {
        super.run ( );
        Object response = getResponse ( );

        if ( !playerApp.getProjectIsActive ( ) )
            System.out.println ( "\n===== Search Results ======" );
        else
            System.out.println ( "\n===== Available Games ======" );

        if ( response != null ) {
            ArrayList<Game> foundgames = (ArrayList<Game>) response;
            int i = 0;
            if ( !foundgames.isEmpty ( ) ) {
                playerApp.setSearchedGames ( foundgames );
                for ( Game g : foundgames ) {
                    i++;
                    System.out.println ( i + ") " + g.getGameName ( ) + " | Stars: " + g.getStars ( ) +
                            " | Bet from " + g.getMinBet ( ) + " FUN to " + g.getMaxBet ( ) + " FUN" +
                            " | Risk Level: " + g.getRiskLevel ( ) +
                            " | Bet Limits: " + g.getBetCategory ());
                }
            } else System.out.println ( "No games found!!!!!!" );
        } else
            System.out.println ( "Error: No response received from Master." );
    }
}