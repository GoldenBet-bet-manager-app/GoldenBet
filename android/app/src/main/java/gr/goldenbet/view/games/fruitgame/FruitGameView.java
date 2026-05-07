package gr.goldenbet.view.games.fruitgame;

import java.math.BigDecimal;

public interface FruitGameView {
    void showMessage(String message);
    void showBalance(BigDecimal balance);
    void showSpinResult(String result);
    void randomizeGrid(String [] symbols);
    void setImageBackground(int imageBackgroundId);
    void setCellsToSymbol(int[] indices, String[] symbols, int textcolor);
    void clearFruitGrid();
    void lock(Boolean flag);
    void playSound(boolean isWin);
    void showErrorDialog(String title, String message);
}

