package gr.goldenbet.view.mainMenu.home;

public interface HomeView {
    void updatePlayerInfo(String name, String balance);

    void navigateToDeposit();
    void navigateToHistory();
    void navigateToStats();
    void navigateToRatings();
    void navigateToLogin();
    void navigateToSettings();
}