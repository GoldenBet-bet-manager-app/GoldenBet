package gr.goldenbet.view.mainMenu.home.history;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class HistoryPresenter {
    private HistoryView view;
    private List<HistoryEntry> allItems; // Η πλήρης λίστα

    public HistoryPresenter(HistoryView view) {
        this.view = view;
        this.allItems = new ArrayList<>();
    }



    public void start() {
        view.showHistory(allItems);
    }

    public void filterData(String query, String filterType) {
        List<HistoryEntry> filtered = new ArrayList<>();
        for (HistoryEntry item : allItems) {
            boolean matchesQuery = item.getTitle().toLowerCase().contains(query.toLowerCase());
            boolean matchesType = filterType.equals("All") || item.getType().name().equalsIgnoreCase(filterType.substring(0, filterType.length()-1));

            if (matchesQuery && matchesType) {
                filtered.add(item);
            }
        }
        view.showHistory(filtered);
    }
}