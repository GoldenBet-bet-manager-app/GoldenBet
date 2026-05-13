package gr.goldenbet.view.games.fruitgame;

import java.math.BigDecimal;

public interface FruitGameView {
    void showMessage(String message);
    void showBalance(BigDecimal balance);
    void showSpinResult(String result);
    void randomizeGrid(String [] symbols);
    void setImageBackground(String imageBackground);
    void showFinalGrid(int[] winIndices, String winSymbol, String[] allSymbols, int winColor);
    void lock(Boolean flag);
    void playSound(boolean isWin);
    void showErrorDialog(String title, String message);

}

