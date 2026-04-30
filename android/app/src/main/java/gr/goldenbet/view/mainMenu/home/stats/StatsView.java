package gr.goldenbet.view.mainMenu.home.stats;

import gr.goldenbet.domain.PlayerStats;

public interface StatsView {
    void showStats(PlayerStats stats);
    void showLoading();
    void showError(String message);
}