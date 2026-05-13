package Models;

import java.io.Serializable;
import java.util.ArrayList;

public class HistoryFilters implements Serializable {
    private String playerId;
    private String username;
    private ArrayList<String> selectedTypes; // ["Wins", "Bets", "Deposits"]
    private String searchQuery;

    public HistoryFilters(String playerId, String username, ArrayList<String> selectedTypes, String searchQuery) {
        this.playerId = playerId;
        this.username = username;
        this.selectedTypes = selectedTypes;
        this.searchQuery = searchQuery;
    }

    public String getPlayerId() { return playerId; }
    public String getUsername() {return username;}
    public ArrayList<String> getSelectedTypes() { return selectedTypes; }
    public String getSearchQuery() { return searchQuery; }
}