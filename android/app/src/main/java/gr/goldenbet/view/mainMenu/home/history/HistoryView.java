package gr.goldenbet.view.mainMenu.home.history;

import java.util.List;

import gr.goldenbet.domain.HistoryEntry;

public interface HistoryView {
    void showHistory(List<HistoryEntry> historyList);
    void navigateBack();
}