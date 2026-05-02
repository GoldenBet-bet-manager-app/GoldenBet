package gr.goldenbet.view.mainMenu.profile;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import gr.goldenbet.R;
import gr.goldenbet.domain.JsonClient;
import gr.goldenbet.domain.MasterMessage;
import gr.goldenbet.domain.MasterMessageType;
import gr.goldenbet.domain.Player;
import gr.goldenbet.utils.Address;
import gr.goldenbet.view.mainMenu.home.HomeFragment;

public class ProfilePresenter {
    private ProfileView view;

    private final String MASTER_IP;
    private final int MASTER_PORT;

    public ProfilePresenter(ProfileView view) {
        this.view = view;

        MASTER_IP = ((ProfileFragment)view).getResources().getString(R.string.master_ip);
        MASTER_PORT = ((ProfileFragment)view).getResources().getInteger(R.integer.master_port);
    }

    public void loadProfile(String playerUsername) {


        Gson gson = new Gson();

        // Master Message creation
        MasterMessage infoReq = new MasterMessage(MasterMessageType.GET_PLAYER_INFO, playerUsername);
        String jsonRequest = gson.toJson(infoReq);

        // Initialize the network client: Set up the socket worker with the
        // server's IP, Port, and our specific request.
        JsonClient client = new JsonClient(MASTER_IP, MASTER_PORT, jsonRequest);


        // Run network operations on background thread to prevent freezing
        new Thread(() -> {
            client.start();
            try {

                // We use 'join()' to pause this background thread until the network
                // communication is complete.
                client.join();

                //  It ensures we don't process a 'null' response.
                String response = client.getJsonResponse();
                if (response != null) {

                    // Parsing the result
                    JsonObject responseJson = gson.fromJson(response, JsonObject.class);
                    String status = responseJson.get("status").getAsString();

                    if ("SUCCESS".equals(status)) {
                        // Extract the Player object from the "requested_player" field
                        Player p = gson.fromJson(responseJson.get("requested_player"), Player.class);


                        // We must hop back onto the Main Thread (MainMenuActivity) to update profile data.
                        view.updateProfileInfo(p);

                    }
                }

            } catch (InterruptedException e) {

                e.printStackTrace();
            }

        }).start();


    }

    public void onSaveClicked(String username, String password, String email, String fName,
                              String lName, String phone, String street, String no,
                              String city, String zip) {

        if (email == null || !email.contains("@")) {
            view.showUpdateError("Invalid Email Address");
            return;
        }

        Address updatedAddress = new Address(street, no, city, zip);

        Player updatedPlayer = new Player(fName, lName, username,
                password, email, phone, updatedAddress);

        view.showSaveConfirmation(updatedPlayer);
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

            } catch (InterruptedException e) {
                e.printStackTrace();
                view.showUpdateError("Connection interrupted");
            }
        }).start();
    }
}