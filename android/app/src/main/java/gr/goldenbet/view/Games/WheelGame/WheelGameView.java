package gr.goldenbet.view.Games.WheelGame;

import java.math.BigDecimal;

public interface WheelGameView {
    void showMessage(String message);
    void showBalance(BigDecimal balance);
    void showSpinResult(String result,int color);
    void spinWheel(int sliceIndex, BigDecimal payout, double multiplier, BigDecimal balance);
}
