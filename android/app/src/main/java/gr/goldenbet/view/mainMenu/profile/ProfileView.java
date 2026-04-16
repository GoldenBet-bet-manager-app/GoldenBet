package gr.goldenbet.view.mainMenu.profile;

public interface ProfileView {
    void setProfileData(String username, String password, String email, String firstName,
                        String lastName, String phone, String street, String houseNo,
                        String city, String zip);

    void showUpdateSuccess();
    void showUpdateError(String message);
}