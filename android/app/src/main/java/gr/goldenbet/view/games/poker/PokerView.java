package gr.goldenbet.view.games.poker;

import java.math.BigDecimal;
import java.util.ArrayList;

public interface PokerView {
    void showMessage(String message);
    void showBalance(BigDecimal balance);
    void showFlipResult(String result);
    void showDealtCards(ArrayList<String> hand);


}
