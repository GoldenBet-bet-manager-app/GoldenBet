package gr.goldenbet.view.games.wheelgame;

import java.math.BigDecimal;

public interface WheelGameView {
    void showMessage(String message);
    void showBalance(BigDecimal balance);
    void showSpinResult(String result);
    void setWheelImage(int imageId);

    void spinWheel(int sliceIndex);
    void playSound(boolean isWin);
    void showErrorDialog(String title, String message);
    void lock(Boolean flag);
}
