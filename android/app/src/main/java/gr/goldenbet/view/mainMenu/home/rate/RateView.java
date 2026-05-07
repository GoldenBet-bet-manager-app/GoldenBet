package gr.goldenbet.view.mainMenu.home.rate;

import java.util.ArrayList;

public interface RateView {
    void showMessage(int messageResId);
    void showMessage(String message);
    void showErrorDialog(int titleResId, int messageResId, String gameName);
    void clearRateForm();
    void setActiveGames(ArrayList<String> gameNames);
}