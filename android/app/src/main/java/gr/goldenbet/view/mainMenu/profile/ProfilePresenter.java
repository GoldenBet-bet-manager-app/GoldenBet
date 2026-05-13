package gr.goldenbet.view.mainMenu.profile;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import gr.goldenbet.R;
import gr.goldenbet.domain.JsonClient;
import gr.goldenbet.domain.MasterMessage;
import gr.goldenbet.domain.MasterMessageType;
import gr.goldenbet.domain.Player;
import gr.goldenbet.domain.Address;
import gr.goldenbet.utils.ConfigLoader;

public class ProfilePresenter {
    private ProfileView view;

    private final String MASTER_IP;
    private final int MASTER_PORT;

    public ProfilePresenter(ProfileView view){

        this.view = view;
        MASTER_IP = ConfigLoader.getIp("MASTER");
        MASTER_PORT = ConfigLoader.getPort("MASTER");
    }

    /**
     * Loads player's data instantly from MainMenuActivity's ViewModel
     */
    public void initData() {
        Player p = view.getPlayerFromActivity();
        if (p != null) {
            view.updateProfileInfo(p);
        }
    }


    public void onSaveClicked(String username, String password, String email, String fName,
                              String lName, String phone, String street, String no,
                              String city, String zip) {

        if (email == null || !email.contains("@")) {
            view.showUpdateError("Invalid Email Address");
            return;
        }

        Player playerToUpdate = view.getPlayerFromActivity();

        if (playerToUpdate != null) {


            playerToUpdate.setUsername(username);
            playerToUpdate.setPassword(password);
            playerToUpdate.setEmail(email);
            playerToUpdate.setName(fName);
            playerToUpdate.setSurname(lName);
            playerToUpdate.setPhone(phone);

            Address addr = playerToUpdate.getAddress();
            addr.setStreet(street);
            addr.setStreetNumber(no);
            addr.setCity(city);
            addr.setZipCode(zip);

            byte[] imageBytes = view.getProfileImageBytes();
            if (imageBytes != null) {
                playerToUpdate.setProfileImage(imageBytes);
            }

            view.showSaveConfirmation(playerToUpdate);
        }
    }

    public void performSave(Player player) {
        Gson gson = new Gson();

        MasterMessage infoReq = new MasterMessage(MasterMessageType.MODIFY_PLAYER, player);
        String jsonRequest = gson.toJson(infoReq);

        JsonClient client = new JsonClient(MASTER_IP, MASTER_PORT, jsonRequest);

        new Thread(() -> {

            client.start();

            try {

                client.join();

                String response = client.getJsonResponse();
                if (response != null) {

                    JsonObject responseJson = gson.fromJson(response, JsonObject.class);
                    String status = responseJson.get("status").getAsString();

                    if ("SUCCESS".equals(status)) {

                        // Update MainMenuActivity ViewModel local player
                        view.syncPlayerWithActivity(player);
                        view.showUpdateSuccess();

                    } else {

                        // If player info modification fails
                        String errorMsg = responseJson.has("message") ?
                                responseJson.get("message").getAsString() : "Update failed";
                        view.showUpdateError(errorMsg);

                    }
                }
                else {
                    view.showUpdateError("No response from server");
                }

            } catch (Exception e) {
                e.printStackTrace();
                view.showUpdateError("Connection error");
            }
        }).start();
    }


}