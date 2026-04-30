package gr.goldenbet.view.mainMenu.home.rate;

import gr.goldenbet.R;
import gr.goldenbet.domain.Player;
import gr.goldenbet.domain.PlayerApp;


public class RatePresenter {

    private RateView rateView;


    public RatePresenter(RateView rateView) {
        this.rateView = rateView;
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

        new Thread(() -> {
            try {
                PlayerApp.getInstance().rate(currentGameName,currentstars);
                rateView.showMessage(R.string.rate_success);
                rateView.clearRateForm();
            } catch (Exception e) {
               rateView.showMessage(e.getMessage());
            }

        }).start();
    }
}