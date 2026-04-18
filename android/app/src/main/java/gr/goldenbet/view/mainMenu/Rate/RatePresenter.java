package gr.goldenbet.view.mainMenu.Rate;

public class RatePresenter {
    private RateView rateView;

    public RatePresenter(RateView rateView) {
        this.rateView = rateView;
    }

    public void onRateClicked(String currentGameName, float currentstars) {
        if (currentGameName == null || currentGameName.trim().isEmpty()) {
            rateView.showMessage("Παρακαλώ επιλέξτε ένα παιχνίδι από τη λίστα!");
            return;
        }
        if (currentstars < 1.0f || currentstars > 5.0f) {
            rateView.showMessage("Η βαθμολογία πρέπει να είναι μεταξύ 1 και 5 αστέρια!");
            return;
        }

        new Thread(() -> {

        }).start();
    }
}
