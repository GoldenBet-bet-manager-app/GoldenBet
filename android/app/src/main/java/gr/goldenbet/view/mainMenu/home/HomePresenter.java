package gr.goldenbet.view.mainMenu.home;

import android.app.Activity;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import gr.goldenbet.R;
import gr.goldenbet.domain.JsonClient;
import gr.goldenbet.domain.MasterMessage;
import gr.goldenbet.domain.MasterMessageType;
import gr.goldenbet.domain.Player;
import gr.goldenbet.view.register.RegisterActivity;

public class HomePresenter {
    private HomeView view;
    private final String MASTER_IP;
    private final int MASTER_PORT;

    private String playerUsername;
    private String playerPassword;

    public HomePresenter(HomeView view) {
        this.view = view;

        MASTER_IP = ((HomeFragment)view).getResources().getString(R.string.master_ip);
        MASTER_PORT = ((HomeFragment)view).getResources().getInteger(R.integer.master_port);

    }


    /**
     * Fetches the latest player profile data from the Master server
     * and updates the UI accordingly.
     */
    public void initData(String playerUsername) {
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

                        // update playerUserName and playerPassword attributes

                        this.playerUsername = p.getUsername();
                        this.playerPassword = p.getPassword();

                        // We must hop back onto the Main Thread (MainMenuActivity) to update names and balances.
                        view.updatePlayerInfo(p.getUsername(), p.getBalance() + " €");

                    }
                }

            } catch (InterruptedException e) {

                e.printStackTrace();
            }

        }).start();

    }

    public void onDepositClicked() { view.navigateToDeposit(); }
    public void onHistoryClicked() { view.navigateToHistory(); }
    public void onStatsClicked() { view.navigateToStats(); }
    public void onRatingsClicked() { view.navigateToRatings(); }


    public void onLogoutCLicked() {

        Gson gson = new Gson();

        ArrayList<String> credentials = new ArrayList<>();
        credentials.add(this.playerUsername);
        credentials.add(this.playerPassword);

        MasterMessage message = new MasterMessage(MasterMessageType.LOGOUT, credentials);
        String jsonToSend = gson.toJson(message);

        JsonClient client = new JsonClient(MASTER_IP,MASTER_PORT, jsonToSend);
        client.start();

        view.navigateToLogin();
    }
    public void onSettingsClicked() {view.navigateToSettings();}

}