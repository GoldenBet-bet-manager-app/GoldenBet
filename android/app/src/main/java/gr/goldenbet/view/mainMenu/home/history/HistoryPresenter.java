package gr.goldenbet.view.mainMenu.home.history;

import java.util.ArrayList;
import java.util.List;

public class HistoryPresenter {
    private HistoryView view;
    private List<HistoryEntry> allItems; // Η πλήρης λίστα

    public HistoryPresenter(HistoryView view) {
        this.view = view;
        this.allItems = new ArrayList<>();
        loadDummyData();
    }

    private void loadDummyData() {
        allItems.add(new HistoryEntry(HistoryEntry.Type.BET, "Football: Real vs Barca", "24 Oct, 15:30", 50.0));
        allItems.add(new HistoryEntry(HistoryEntry.Type.WIN, "Big Win: Slot Machine", "24 Oct, 16:00", 250.0));
        allItems.add(new HistoryEntry(HistoryEntry.Type.DEPOSIT, "Card Deposit", "23 Oct, 10:00", 100.0));
        allItems.add(new HistoryEntry(HistoryEntry.Type.BET, "Tennis: Nadal vs Federer", "22 Oct, 12:00", 30.0));
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