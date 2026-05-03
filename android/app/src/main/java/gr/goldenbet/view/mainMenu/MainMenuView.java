package gr.goldenbet.view.mainMenu;

import gr.goldenbet.domain.Player;

public interface MainMenuView {
    void showHome();
    void showSearch();
    void showProfile();
    void showRate();
    void updateLocalPlayer(Player player);
    Player getCurrentPlayer();
}