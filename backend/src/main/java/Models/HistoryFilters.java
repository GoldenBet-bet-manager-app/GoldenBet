package Models;

import java.io.Serializable;
import java.util.ArrayList;

public class HistoryFilters implements Serializable {
    private String username;
    private ArrayList<String> selectedTypes; // ["Wins", "Bets", "Deposits"]
    private String searchQuery;

    public HistoryFilters(String username, ArrayList<String> selectedTypes, String searchQuery) {
        this.username = username;
        this.selectedTypes = selectedTypes;
        this.searchQuery = searchQuery;
    }

    public String getUsername() { return username; }
    public ArrayList<String> getSelectedTypes() { return selectedTypes; }
    public String getSearchQuery() { return searchQuery; }
}