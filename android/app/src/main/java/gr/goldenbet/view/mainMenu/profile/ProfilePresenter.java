package gr.goldenbet.view.mainMenu.profile;

public class ProfilePresenter {
    private ProfileView view;

    public ProfilePresenter(ProfileView view) {
        this.view = view;
    }

    public void loadProfile() {
        view.setProfileData(
                "PlayerOne", "pass123", "player@goldenbet.gr",
                "PlayerName", "PlayerSurname", "+30 6900000000",
                "StreetName", "15", "Athens", "54622"
        );
    }

    public void onSaveClicked(String username, String password, String email, String fName,
                              String lName, String phone, String street, String no,
                              String city, String zip) {

        if (email == null || !email.contains("@")) {
            view.showUpdateError("Invalid Email Address");
            return;
        }

        boolean success = true;
        if (success) {
            view.showUpdateSuccess();
        } else {
            view.showUpdateError("Server connection failed");
        }
    }
}