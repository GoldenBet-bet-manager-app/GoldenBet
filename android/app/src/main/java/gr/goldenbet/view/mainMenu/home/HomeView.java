package gr.goldenbet.view.mainMenu.home;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface HomeView {
    void updatePlayerInfo(String name, String balance);

    void navigateToDeposit();
    void navigateToHistory();
    void navigateToStats();
    void navigateToRatings();
    void navigateToLogin();
    void navigateToSettings();

    void onLeaderboardUpdated(List<Map.Entry<String, BigDecimal>> sortedData);
    void updateLeaderboardPreview(List<Map.Entry<String, BigDecimal>> topFive);
}