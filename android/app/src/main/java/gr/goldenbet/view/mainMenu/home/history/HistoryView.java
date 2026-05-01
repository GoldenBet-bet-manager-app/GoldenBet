package gr.goldenbet.view.mainMenu.home.history;

import java.util.List;

public interface HistoryView {
    void showHistory(List<HistoryEntry> historyList);
    void navigateBack();
}