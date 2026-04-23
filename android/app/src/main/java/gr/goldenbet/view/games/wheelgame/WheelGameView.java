package gr.goldenbet.view.games.wheelgame;

import java.math.BigDecimal;

public interface WheelGameView {
    void showMessage(String message);
    void showBalance(BigDecimal balance);
    void showSpinResult(String result,int color);
    void setImageBackground(int imageBackgroundId);

    void spinWheel(int sliceIndex, BigDecimal payout, double multiplier, BigDecimal balance);
}
