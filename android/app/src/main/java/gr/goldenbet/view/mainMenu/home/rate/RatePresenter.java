package gr.goldenbet.view.mainMenu.home.rate;

import android.content.SharedPreferences;

import com.google.gson.Gson;

import gr.goldenbet.R;
import gr.goldenbet.domain.JsonClient;
import gr.goldenbet.domain.MasterMessage;
import gr.goldenbet.domain.MasterMessageType;
import gr.goldenbet.domain.Player;

import gr.goldenbet.domain.Rating;
import gr.goldenbet.view.mainMenu.MainMenuActivity;
import gr.goldenbet.view.register.RegisterActivity;


public class RatePresenter {

    private RateView rateView;

    private static final String MASTER_IP = "10.0.2.2";
    private static final int MASTER_PORT = 43210;
    private SharedPreferences prefs;

     Gson gson = new Gson();
    public RatePresenter(RateView rateView,SharedPreferences prefs) {
        this.rateView = rateView;
        this.prefs=prefs;
        //MASTER_IP = ((MainMenuActivity)rateView).getResources().getString(R.string.master_ip);
        //MASTER_PORT = ((MainMenuActivity)rateView).getResources().getInteger(R.integer.master_port);

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
            rateView.showMessage("Δεν μπορείς να αξιολογήσεις το παιχνίδι επειδή δεν το έχεις παίξει ακόμα!");
            return;
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
}
