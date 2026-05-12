package gr.goldenbet.view.mainMenu.home.rate;

import static gr.goldenbet.domain.MasterMessageType.GET_ACTIVE_GAMES;

import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import gr.goldenbet.R;
import gr.goldenbet.domain.Game;
import gr.goldenbet.domain.JsonClient;
import gr.goldenbet.domain.MasterMessage;
import gr.goldenbet.domain.MasterMessageType;

import gr.goldenbet.domain.Rating;

public class RatePresenter {

    private RateView rateView;

    private static  String MASTER_IP;
    private static  int MASTER_PORT ;
    private SharedPreferences prefs;

     Gson gson = new Gson();
    public RatePresenter(RateView rateView, SharedPreferences prefs, String ip, int port) {
        this.rateView = rateView;
        this.prefs=prefs;
        this.MASTER_IP = ip;
        this.MASTER_PORT = port;

    }

    public void onRateClicked(String currentGameName, float currentstars) {

        if (currentGameName == null || currentGameName.trim().isEmpty()) {
            rateView.showMessage(R.string.rate_error_no_game);
            return;
        }

        if (currentstars < 1.0f || currentstars > 5.0f) {
            rateView.showMessage(R.string.rate_error_invalid_stars);
            return;
        }
        boolean hasPlayed = prefs.getBoolean("PLAYED_" + currentGameName, false);
        if (!hasPlayed) {
            rateView.showErrorDialog(R.string.rate_error_not_played_title, R.string.rate_error_not_played_message, currentGameName);            return;
        }

        new Thread(() -> {
            try {
                Rating newRating = new Rating(currentGameName, currentstars);
                MasterMessage requestMessage = new MasterMessage(MasterMessageType.RATE, newRating);
                String jsonMessage = gson.toJson(requestMessage);
                JsonClient rateClient = new JsonClient(MASTER_IP, MASTER_PORT, jsonMessage);
                rateClient.start();
                rateClient.join();
                String responseString = rateClient.getJsonResponse();
                if(responseString==null){
                    throw new RuntimeException("Δεν υπήρξε απάντηση από τον Server.");
                }
                rateView.showMessage(R.string.rate_success);
                rateView.clearRateForm();
            } catch (Exception e) {
               rateView.showMessage(e.getMessage());
            }

        }).start();
    }

    public void initializedata() {
        ArrayList<String> gameNames = new ArrayList<>();
        new Thread(() -> {
            try {
                MasterMessage message = new MasterMessage(GET_ACTIVE_GAMES, null);
                String jsonMessage = gson.toJson(message);
                JsonClient jsonClient = new JsonClient(MASTER_IP, MASTER_PORT, jsonMessage);
                jsonClient.start();
                jsonClient.join();
                String responseString = jsonClient.getJsonResponse();
                if(responseString==null){
                    throw new RuntimeException("Δεν υπήρξε απάντηση από τον Server.");
                }
                Type listType = new TypeToken<ArrayList<Game>>(){}.getType();
                ArrayList<Game> activeGames = gson.fromJson(responseString, listType);
                if(activeGames!=null) {
                    for (Game g : activeGames) {
                        if (g != null) gameNames.add(g.getGameName());
                    }
                }
                rateView.setActiveGames(gameNames);
            } catch (Exception e) {
                rateView.showMessage(e.getMessage());
                rateView.showMessage("Αποτυχία φόρτωσης παιχνιδιών: " + e.getMessage());
            }

        }).start();

    }
}
