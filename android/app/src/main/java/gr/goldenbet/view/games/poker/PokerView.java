package gr.goldenbet.view.games.poker;

import java.math.BigDecimal;
import java.util.ArrayList;

public interface PokerView {
    void showMessage(String message);
    void showBalance(BigDecimal balance);
    void showWinResult(String result);
    void showFlipResult(String result);
    void showErrorDialog(String title, String message);
    void showDealtCards(ArrayList<String> hand);
    void lock(Boolean flag);
    void playSound(boolean isWin);


}
