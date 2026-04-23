package gr.goldenbet.view.games.fruitgame;

import java.math.BigDecimal;

public interface FruitGameView {
    void showMessage(String message);
    void showBalance(BigDecimal balance);
    void showSpinResult(String result);
    void randomizeGrid();
    void setImageBackground(int imageBackgroundId);
    void setCellsToSymbol(int[] indices, String symbol, int textcolor);
    void clearFruitGrid();
}

