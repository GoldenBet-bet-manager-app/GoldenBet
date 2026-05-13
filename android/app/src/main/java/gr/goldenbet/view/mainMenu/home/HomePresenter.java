package gr.goldenbet.view.mainMenu.home;



import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import gr.goldenbet.domain.JsonClient;
import gr.goldenbet.domain.MasterMessage;
import gr.goldenbet.domain.MasterMessageType;
import gr.goldenbet.domain.Player;
import gr.goldenbet.utils.ConfigLoader;

public class HomePresenter {
    private HomeView view;
    private final String MASTER_IP;
    private final int MASTER_PORT;

    private Player currentPlayer;
    private List<Map.Entry<String, BigDecimal>> lastSortedData = new ArrayList<>();



    public HomePresenter(HomeView view) {
        this.view = view;

        MASTER_IP = ConfigLoader.getIp("MASTER");
        MASTER_PORT = ConfigLoader.getPort("MASTER");

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

    public void onLeaderboardClicked() {
        // Στέλνουμε τα τελευταία δεδομένα που έχουμε στη μνήμη
        view.showLeaderboard(lastSortedData);
    }

    /**
     * Fetches live profits data for players leaderboard/.
     * It is called outside main Thread not to occur delays
     */
    public void fetchLiveLeaderboard() {

        // MasterMessage creation
        MasterMessage message = new MasterMessage(MasterMessageType.PLAYERS_PROFITS, null);
        Gson gson = new Gson();

        JsonClient client = new JsonClient(MASTER_IP, MASTER_PORT, gson.toJson(message));
        client.start();

        try {
            client.join(); // Waiting for network thread to join
            String response = client.getJsonResponse();

            if (response != null) {
                Type type = new TypeToken<Map<String, BigDecimal>>(){}.getType();
                Map<String, BigDecimal> results = gson.fromJson(response, type);

                // Conversion to ArrayList (Descending)
                List<Map.Entry<String, BigDecimal>> sortedData = new ArrayList<>(results.entrySet());
                sortedData.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

                this.lastSortedData = sortedData;

                // View update (Fragment)
                view.onLeaderboardUpdated(sortedData);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public List<Map.Entry<String, BigDecimal>> getLastData() { return lastSortedData; }

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