package gr.goldenbet.view.mainMenu.profile;

import gr.goldenbet.domain.Player;

public interface ProfileView {
    void updateProfileInfo(String username, String password, String email, String firstName,
                           String lastName, String phone, String street, String houseNo,
                           String city, String zip);

    void updateProfileInfo(Player player);
    void showSaveConfirmation(Player player);
    void showUpdateSuccess();
    void showUpdateError(String message);
}