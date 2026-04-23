package gr.goldenbet.view.mainMenu.home.rate;

import gr.goldenbet.R;


public class RatePresenter {

    private RateView rateView;


    public RatePresenter(RateView rateView) {
        this.rateView = rateView;
    }

    public void onRateClicked(String currentGameName, float currentstars) {

        if (currentGameName == null || currentGameName.trim().isEmpty()) {
            rateView.showMessage(R.string.rate_error_no_game); // Από strings.xml
            return;
        }

        if (currentstars < 1.0f || currentstars > 5.0f) {
            rateView.showMessage(R.string.rate_error_invalid_stars); // Από strings.xml
            return;
        }

        new Thread(() -> {

        }).start();
    }
}