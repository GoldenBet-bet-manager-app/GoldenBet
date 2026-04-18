package gr.goldenbet.view.Games.FruitGame;

import java.math.BigDecimal;

public interface FruitGameView {
    void showMessage(String message);
    void showBalance(BigDecimal balance);
    void showSpinResult(String result);
    void randomizeGrid();
}

