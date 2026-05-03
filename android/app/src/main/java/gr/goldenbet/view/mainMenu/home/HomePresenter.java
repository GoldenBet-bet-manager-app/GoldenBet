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

    private Player currentPlayer;

    public HomePresenter(HomeView view) {
        this.view = view;

        MASTER_IP = ((HomeFragment)view).getResources().getString(R.string.master_ip);
        MASTER_PORT = ((HomeFragment)view).getResources().getInteger(R.integer.master_port);

    }


    /**
     * Updates the UI instantly using the local object Player from ViewModel of MainMenuActivity.
     */
    public void initData(Player player) {
        if (player != null) {
            this.currentPlayer = player;
            // Ενημέρωση του View αμέσως από τη μνήμη
            view.updatePlayerInfo(player.getUsername(), player.getBalance() + " €");
        }
    }

    public void onDepositClicked() { view.navigateToDeposit(); }
    public void onHistoryClicked() { view.navigateToHistory(); }
    public void onStatsClicked() { view.navigateToStats(); }
    public void onRatingsClicked() { view.navigateToRatings(); }
    public void onSettingsClicked() {view.navigateToSettings();}



    public void onLogoutClicked() {
        if (currentPlayer == null) return;

        // Εκτέλεση Logout στον Master σε background thread
        new Thread(() -> {
            Gson gson = new Gson();
            ArrayList<String> credentials = new ArrayList<>();
            credentials.add(currentPlayer.getUsername());
            credentials.add(currentPlayer.getPassword());

            MasterMessage message = new MasterMessage(MasterMessageType.LOGOUT, credentials);
            JsonClient client = new JsonClient(MASTER_IP, MASTER_PORT, gson.toJson(message));
            client.start();
            // Δεν περιμένουμε απάντηση για το logout, προχωράμε στο UI
        }).start();

        view.navigateToLogin();
    }


}