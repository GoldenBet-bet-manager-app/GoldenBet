package gr.goldenbet.view.mainMenu.profile;

import gr.goldenbet.domain.Player;

public interface ProfileView {
    void updateProfileInfo(Player player);
    void showSaveConfirmation(Player player);
    void showUpdateSuccess();
    void showUpdateError(String message);

    // Access to MainMenuActivity's player
    Player getPlayerFromActivity();
    void syncPlayerWithActivity(Player updatedPlayer);
}