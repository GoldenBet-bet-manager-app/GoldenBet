package gr.goldenbet.view.mainMenu.home;

public interface HomeView {
    void setPlayerName(String name);
    void setBalance(String balance);

    void navigateToDeposit();
    void navigateToHistory();
    void navigateToStats();
    void navigateToRatings();
    void navigateToLogin();
    void navigateToSettings();
}