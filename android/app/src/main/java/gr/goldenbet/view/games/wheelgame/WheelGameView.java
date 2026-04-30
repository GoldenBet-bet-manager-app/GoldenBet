package gr.goldenbet.view.games.wheelgame;

import java.math.BigDecimal;

public interface WheelGameView {
    void showMessage(String message);
    void showBalance(BigDecimal balance);
    void showSpinResult(String result);
    void setImageBackground(int imageBackgroundId);

    void spinWheel(int sliceIndex);
}
